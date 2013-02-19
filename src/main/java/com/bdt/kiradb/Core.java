package com.bdt.kiradb;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;


/**
 * The Core KiraDB API
 *
 * @author David Beckemeyer and Mark Petrovic
 *
 */
public class Core {

    private Logger logger = Logger.getLogger(Core.class.getName());

	private final static String TYPE_KEY = "type";

	private String indexPath;

    private XStream xstream;

    private int totalHits;


	public Core(String indexPath) {
		this.indexPath = indexPath;
		xstream = new XStream();
	}


	private IndexWriter getIndexWriter(File indexDir) throws InterruptedException, IOException, CorruptIndexException {
		IndexWriter writer = null;
		int nTries = 0;
		while (true) {
			try {
				writer = new IndexWriter(FSDirectory.open(indexDir),
						new StandardAnalyzer(Version.LUCENE_30),
						IndexWriter.MaxFieldLength.UNLIMITED);
				return writer;
			} catch (CorruptIndexException e) {
				throw e;
			} catch (LockObtainFailedException e) {
				if (++nTries > 4)
					throw e;
				Thread.sleep(100L);
				logger.info("getIndexWriter retry: " + nTries);
			} catch (IOException e) {
				throw e;
			}
		}

	}

	/**
	 * Store object index and optionally the object itself into the DB
	 *
	 * @param object The object being indexed/written
	 */
	/**
	 * @param object
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws KiraCorruptIndexException
	 */
	public void storeObject(Object object) throws IOException, InterruptedException, KiraCorruptIndexException {
		Record r = (Record)object;
		RecordDescriptor dr = r.descriptor();

		Document doc = new Document();

		// add the Record Type field
		doc.add(new org.apache.lucene.document.Field(TYPE_KEY, dr.getRecordName(),
				org.apache.lucene.document.Field.Store.YES,
				org.apache.lucene.document.Field.Index.NOT_ANALYZED));


		// Add the primary key field
		String key = makeKey(dr, dr.getPrimaryKey().getName());

		addField(doc, dr, dr.getPrimaryKey(), key);

		// Add the other fields
		if (dr.getFields() != null) {
			for (Field f : dr.getFields()) {
				addField(doc, dr, f);
			}
		}

		// Write the object if that's what we're doing
		if (dr.getStoreObjects()) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos;

            oos = xstream.createObjectOutputStream(bos);

            oos.writeObject(object);
            // Flush and close the ObjectOutputStream.
            //
            oos.flush();
            oos.close();
            doc.add(new org.apache.lucene.document.Field("object",
            		bos.toString(),
            		org.apache.lucene.document.Field.Store.YES,
            		org.apache.lucene.document.Field.Index.NO));


		}
		// Set the primary key as the Term for the object
		Term t = null;
		switch (dr.getPrimaryKey().getType()) {
		case DATE:
			Calendar c = Calendar.getInstance();
			c.setTime((Date)dr.getPrimaryKey().getValue());
			String s = String.format("%04d%02d%02d%02d%02d%02d.%04d",
					c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
					c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND));
			doc.add(new org.apache.lucene.document.Field(key, s,
					org.apache.lucene.document.Field.Store.YES,
					org.apache.lucene.document.Field.Index.NOT_ANALYZED));
			break;
		case STRING:
			t = new Term(key, (String)dr.getPrimaryKey().getValue());
			break;
		case NUMBER:
			t = new Term(key, ((Integer)dr.getPrimaryKey().getValue())+"");
			break;
		case FULLTEXT:
			// throw an exception
			break;
		}
		if (t != null) {
			File indexDir = new File(indexPath);
            IndexWriter writer = getIndexWriter(indexDir);
            try {
            	writer.updateDocument(t, doc);
            } catch (CorruptIndexException e) {
    			throw new KiraCorruptIndexException(e.getMessage());
            } catch (IOException e) {
            	throw e;
            } finally {
            	writer.close();
            }
		}

	}

	/**
	 * Retrieve an object (record) by primary key
	 *
	 * @param object
	 * @param value
	 *
	 * @return Object or HashMap<String, String> of fields
	 *
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws KiraException
	 */
	public Object retrieveObjectbyPrimaryKey(Object object, String value) throws IOException, ClassNotFoundException, KiraException {
		Record r = (Record)object;
        String key = makeKey(r.descriptor(), r.getPrimaryKeyName());

        List<Document> docs;
		try {
			docs = searchDocuments(r.getRecordName(), key + ":" + value, 1);
		} catch (ParseException e) {
			throw new KiraException("ParseException " + e.getMessage());
		}
        if (docs.size() > 0) {
            Document d = docs.get(0);
            if (r.descriptor().getStoreObjects()) {
            	String obj = d.get("object");

            	ByteArrayInputStream fis = new ByteArrayInputStream(obj.getBytes("UTF-8"));

            	ObjectInputStream ois;

            	ois = xstream.createObjectInputStream(fis);

            	Object result = ois.readObject();

            	ois.close();
            	return result;
            } else {
            	// if object not returned, then return fields as key,value pairs
            	HashMap<String, String> results = new HashMap<String,String>();
            	if (r.descriptor().getFields() != null) {
            		for (Field f : r.descriptor().getFields()) {
            			results.put(f.getName(), d.get(f.getName()));
            		}
            	}
            	return results;
            }
        }
        return null;
	}

	private List<Document> searchDocuments(String querystr, int hitsPerPage) throws CorruptIndexException, IOException, ParseException {
		return searchDocuments(null, querystr, hitsPerPage);
	}

	private List<Document> searchDocuments(String typeStr, String querystr, int hitsPerPage) throws CorruptIndexException, IOException, ParseException {
		return searchDocuments(typeStr, querystr, null, hitsPerPage);
	}
	private List<Document> searchDocuments(String typeStr, String querystr, int hitsPerPage, int skipDocs) throws CorruptIndexException, IOException, ParseException {
		return searchDocuments(typeStr, querystr, null, hitsPerPage, skipDocs);
	}

	private List<Document> searchDocuments(String typeStr, String querystr, Sort sortBy, int hitsPerPage) throws CorruptIndexException, IOException, ParseException {
		return searchDocuments(typeStr, querystr, sortBy, hitsPerPage, 0);
	}
	/**
	 * @param typeStr
	 * @param querystr
	 * @param sortBy
	 * @param hitsPerPage
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParseException
	 */
	private List<Document> searchDocuments(String typeStr, String querystr, Sort sortBy, int hitsPerPage, int skipDocs) throws CorruptIndexException, IOException, ParseException {

		// 1. create the index
		File indexDir = new File(indexPath);
		Directory index = FSDirectory.open(indexDir);

		// the boolean arg in the IndexWriter ctor means to
		// create a new index, overwriting any existing index
		/*IndexWriter w = new IndexWriter(index, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
		addDoc(w, "Lucene in Action");
		addDoc(w, "Lucene for Dummies");
		addDoc(w, "Managing Gigabytes");
		addDoc(w, "The Art of Computer Science");
		w.close();*/

		// 2. query
		BooleanQuery booleanQuery = new BooleanQuery();

		// the "fulltext" arg specifies the default field to use
		// when no field is explicitly specified in the query.
		if (querystr != null) {
			QueryParser parser = null;
			if (querystr.contains(":")) {
				KeywordAnalyzer analyzer = new KeywordAnalyzer();
				parser = new QueryParser(Version.LUCENE_30, "title", analyzer);
			} else {
				StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
				parser = new QueryParser(Version.LUCENE_30, "fulltext", analyzer);
			}
			parser.setDefaultOperator(QueryParser.Operator.AND);
			Query q = parser.parse(querystr);
			booleanQuery.add(q, org.apache.lucene.search.BooleanClause.Occur.MUST);
		}
		Query q1 = new TermQuery(new Term("type", typeStr));
		booleanQuery.add(q1, org.apache.lucene.search.BooleanClause.Occur.MUST);

		// 3. search
		IndexSearcher searcher = new IndexSearcher(index, true);
		if (sortBy == null)
			sortBy = new Sort(new SortField("date", SortField.STRING, true));
		TopFieldDocs tfd = searcher.search(booleanQuery, null, skipDocs+hitsPerPage, sortBy);
		ScoreDoc[] hits = tfd.scoreDocs;
		this.setTotalHits(tfd.totalHits);
		// 4. display results
		List<Document> results = new ArrayList<Document>();
		//System.out.println("Found " + hits.length + " hits.");
		for(int i=0;i<hits.length;++i) {
			if (i < skipDocs)
				continue;
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			results.add(d);
			//System.out.println((i + 1) + ". " + d.get("status"));
		}

		// searcher can only be closed when there
		// is no need to access the documents any more.
		searcher.close();
	    return results;
	}

	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}

	public int getTotalHits() {
		return totalHits;
	}

	private void addField(Document doc, RecordDescriptor dr, Field f) {
		addField(doc, dr, f, f.getName());
	}

	private void addField(Document doc, RecordDescriptor dr, Field f, String key) {
		switch (f.getType()) {
		case DATE:
			Calendar c = Calendar.getInstance();
			c.setTime((Date)f.getValue());
            String s = String.format("%04d%02d%02d%02d%02d%02d.%04d",
            		c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH),
            		c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND));
			doc.add(new org.apache.lucene.document.Field(key, s,
					org.apache.lucene.document.Field.Store.YES,
					org.apache.lucene.document.Field.Index.NOT_ANALYZED));
			break;
		case STRING:
			doc.add(new org.apache.lucene.document.Field(key, (String)f.getValue(),
					org.apache.lucene.document.Field.Store.YES,
					org.apache.lucene.document.Field.Index.NOT_ANALYZED));
			break;
		case NUMBER:
			doc.add(new org.apache.lucene.document.Field(key, ((Integer)f.getValue())+"",
					org.apache.lucene.document.Field.Store.YES,
					org.apache.lucene.document.Field.Index.NOT_ANALYZED));
			break;
		case FULLTEXT:
			StringBuffer finalFulltext = new StringBuffer((String)f.getValue());
            finalFulltext.append("\n");
            finalFulltext.append(Stemmer.stemString((String)f.getValue(), StandardAnalyzer.STOP_WORDS_SET));
            doc.add(new org.apache.lucene.document.Field(key, finalFulltext.toString(),
					org.apache.lucene.document.Field.Store.YES,
					org.apache.lucene.document.Field.Index.ANALYZED));
			break;
		}

	}



	private String makeKey(RecordDescriptor dr, String key) {
		return dr.getRecordName() + "_" + key;
	}

	/**
	 * Initialize the Index
	 * @throws KiraCorruptIndexException
	 * @throws IOException
	 *
	 */
	public void createIndex() throws KiraCorruptIndexException, IOException {
        File indexDir = new File(indexPath);
		IndexWriter writer;
		try {
			writer = new IndexWriter(FSDirectory.open(indexDir), new StandardAnalyzer(Version.LUCENE_30),
					true, IndexWriter.MaxFieldLength.UNLIMITED);
		} catch (CorruptIndexException e) {
			throw new KiraCorruptIndexException(e.getMessage());
		} catch (LockObtainFailedException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}

		writer.close();
	}

	/**
	 * Optimize the Index
	 *
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws KiraCorruptIndexException
	 */
	public void optimizeIndex() throws InterruptedException, IOException, KiraCorruptIndexException {
		File indexDir = new File(indexPath);
		IndexWriter writer = getIndexWriter(indexDir);
        try {
	        writer.optimize();
		} catch (CorruptIndexException e) {
			throw new KiraCorruptIndexException(e.getMessage());
		} catch (IOException e) {
			throw e;
		} finally {
			writer.close();
		}
	}

    public void deleteIndex() throws IOException {
        File directory = new File(indexPath);
        FileUtils.deleteDirectory(directory);
    }

}
