package com.bdt.kiradb;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
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
import org.jets3t.service.ServiceException;

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

    private BackingStore backingStore;
    
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
	 * @throws KiraException 
	 */
	public void storeObject(Object object) throws IOException, InterruptedException, KiraException {
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
				// fields are optional, do not store null fields
				if (f.getValue() != null)
					addField(doc, dr, f);
			}
		}

		// Write the object if that's what we're doing
		if ((dr.getStoreMode() & RecordDescriptor.STORE_MODE_INDEX) != 0) {
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
		// Also pass through to the Backing Store if so requested
		if ((r.descriptor().getStoreMode() & RecordDescriptor.STORE_MODE_BACKING) != 0) {
    		if (this.backingStore == null) {
    			throw new KiraException("STORE_MODE_BACKING but no backing store set");
    		}
    		this.backingStore.storeObject(xstream, object);    		
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

        File indexDir = new File(indexPath);
        FSDirectory idx = FSDirectory.open(indexDir);
        IndexReader ir = IndexReader.open(idx);
        
        Term t = new Term(key, value);
        TermDocs tdocs = ir.termDocs(t);
        Object result = null;
        if (tdocs.next()) {
        	Document d = ir.document(tdocs.doc());
        	if (r.descriptor().getStoreMode() == RecordDescriptor.STORE_MODE_NONE) {
        		// if object not returned, then return fields as key,value pairs
            	HashMap<String, String> results = new HashMap<String,String>();
            	results.put(r.getPrimaryKeyName(), (String)d.get(key));
            	if (r.descriptor().getFields() != null) {
            		for (Field f : r.descriptor().getFields()) {
            			// return all existing fields
            			if (d.get(f.getName()) != null)
            				results.put(f.getName(), (String)d.get(f.getName()));
            		}
            	}
            	result = results;
            	
            	
        	} else if ((r.descriptor().getStoreMode() & RecordDescriptor.STORE_MODE_INDEX) != 0) {
        		String obj = d.get("object");


        		ByteArrayInputStream fis = new ByteArrayInputStream(obj.getBytes("UTF-8"));

        		ObjectInputStream ois;

        		ois = xstream.createObjectInputStream(fis);

        		result = ois.readObject();

        		ois.close();
        	} else if ((r.descriptor().getStoreMode() & RecordDescriptor.STORE_MODE_BACKING) != 0) {
        		if (this.backingStore == null) {
        			throw new KiraException("STORE_MODE_BACKING but no backing store set");
        		}
        		result = this.backingStore.retrieveObject(xstream, object, d.get(key));

        	}
        }
        tdocs.close();
        ir.close();
        return result;
	}

	/**
	 * Query for matching records
	 * 
	 * @param object An instance of the Class / Record
	 * @param queryFieldName The name to the field to query
	 * @param querystr The query string
	 * @param hitsPerPage The number of records to return
	 * @param skipDocs The number of records to skip
	 * @param sortFieldName Optional sort field name
	 * @param reverse Set to true to reverse the sort order
	 * 
	 * @return List<Object> list of matching objects or list of matching keys

	 */

	public List<Object> executeQuery(Object object, String queryFieldName, String querystr, int hitsPerPage, int skipDocs, String sortFieldName, Boolean reverse) throws KiraException, IOException, ClassNotFoundException {
		Field queryField = null;
		Record r = (Record)object;
		if (queryFieldName != null) {
			queryField = r.descriptor().getFieldByName(queryFieldName);
		}
		Field sortField =null;
		if (sortFieldName != null) {
			sortField = r.descriptor().getFieldByName(sortFieldName);
		}
		return executeQuery(object, queryField, querystr, hitsPerPage, skipDocs, sortField, reverse);
	}
	/**
	 * Query for matching records
	 * 
	 * @param object An instance of the Class / Record
	 * @param queryField The field to query
	 * @param querystr The query string
	 * @param hitsPerPage The number of records to return
	 * @param skipDocs The number of records to skip
	 * @param sortField Optional sort field or null
	 * @param reverse Set to true to reverse the sort order
	 * 
	 * @return List<Object> list of matching objects or list of matching keys
	 * 
	 * @throws KiraException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public List<Object> executeQuery(Object object, Field queryField, String querystr, int hitsPerPage, int skipDocs, Field sortField, Boolean reverse) throws KiraException, IOException, ClassNotFoundException {
		List<Document> docs;
		Record r = (Record)object;
        String key = makeKey(r.descriptor(), r.getPrimaryKeyName());
        
        Sort sortBy = null;
        if (sortField != null) {
        	sortBy = new Sort(new SortField(sortField.getName(), SortField.STRING, reverse));
        }
        String queryFieldName = null;
        Boolean fullText = false;
        if (queryField != null) {
        	queryFieldName = queryField.getName();
        	fullText = (queryField.getType() == FieldType.FULLTEXT);
        }
		try {
			docs = searchDocuments(r.getRecordName(), queryFieldName + ":" + querystr, fullText, sortBy, hitsPerPage, skipDocs);
		} catch (ParseException e) {
			throw new KiraException("ParseException " + e.getMessage());
		} catch (CorruptIndexException e) {
			throw new KiraCorruptIndexException(e.getMessage());
		} catch (IOException e) {
			throw e;
		}
		List<Object> results = new ArrayList<Object>();
        if (docs.size() > 0) {
        	if (r.descriptor().getStoreMode() == RecordDescriptor.STORE_MODE_NONE) {
        		// if objects are not stored in the index, return list of matching primary keys
                for (Document d: docs) {
                	results.add(d.get(key));
                }
        	} else if ((r.descriptor().getStoreMode() & RecordDescriptor.STORE_MODE_INDEX) != 0) {

        		for (Document d: docs) {
        			String obj = d.get("object");

        			ByteArrayInputStream fis = new ByteArrayInputStream(obj.getBytes("UTF-8"));

        			ObjectInputStream ois;

        			ois = xstream.createObjectInputStream(fis);

        			results.add(ois.readObject());
        			ois.close();

        		}
        	} else if ((r.descriptor().getStoreMode() & RecordDescriptor.STORE_MODE_BACKING) != 0) {
        		if (this.backingStore == null) {
        			throw new KiraException("STORE_MODE_BACKING but no backing store set");
        		}
        		for (Document d: docs) {
        			Object result = this.backingStore.retrieveObject(xstream, object, d.get(key));
					results.add(result);
        		}
        		
        	}
        	
        }
        return results;
	}
	
	public void dumpDocuments(String type) throws KiraException, KiraCorruptIndexException, IOException {
		List<Document> docs;

		try {
			docs = searchDocuments(type, null, false, Integer.MAX_VALUE);
		} catch (ParseException e) {
			throw new KiraException("ParseException " + e.getMessage());
		} catch (CorruptIndexException e) {
			throw new KiraCorruptIndexException(e.getMessage());
		} catch (IOException e) {
			throw e;
		}
        if (docs.size() > 0) {
        	for (Document d: docs) {
        		System.out.println("Doc: " + d);
        	}
        }
	}
	
	private List<Document> searchDocuments(String typeStr, String querystr, Boolean fullText, int hitsPerPage) throws CorruptIndexException, IOException, ParseException {
		return searchDocuments(typeStr, querystr, fullText, null, hitsPerPage);
	}
	private List<Document> searchDocuments(String typeStr, String querystr, Boolean fullText, int hitsPerPage, int skipDocs) throws CorruptIndexException, IOException, ParseException {
		return searchDocuments(typeStr, querystr, fullText, null, hitsPerPage, skipDocs);
	}

	private List<Document> searchDocuments(String typeStr, String querystr, Boolean fullText, Sort sortBy, int hitsPerPage) throws CorruptIndexException, IOException, ParseException {
		return searchDocuments(typeStr, querystr, fullText, sortBy, hitsPerPage, 0);
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
	private List<Document> searchDocuments(String typeStr, String querystr, Boolean fullText, Sort sortBy, int hitsPerPage, int skipDocs) throws CorruptIndexException, IOException, ParseException {

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
			if (fullText) {
				StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
				parser = new QueryParser(Version.LUCENE_30, "fulltext", analyzer);
			} else {
				KeywordAnalyzer analyzer = new KeywordAnalyzer();
				parser = new QueryParser(Version.LUCENE_30, "title", analyzer);
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


	public Core setBackingStore(BackingStore backingStore) {
		this.backingStore = backingStore;
		return this;
	}


}
