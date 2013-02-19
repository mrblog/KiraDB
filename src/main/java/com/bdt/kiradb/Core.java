package com.bdt.kiradb;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

import com.thoughtworks.xstream.XStream;


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

	
	public Core(String indexPath) {
		this.indexPath = indexPath;
		xstream = new XStream();
	}
	
	
	private IndexWriter getIndexWriter(File indexDir) throws InterruptedException, IOException {
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
	 */
	void storeObject(Object object) throws IOException, InterruptedException {
		Record r = (Record)object;
		RecordDescriptor dr = r.descriptor();
		
		Document doc = new Document();

		// add the Record Type field
		doc.add(new org.apache.lucene.document.Field(TYPE_KEY, dr.getRecordName(), 
				org.apache.lucene.document.Field.Store.YES, 
				org.apache.lucene.document.Field.Index.NOT_ANALYZED));

		
		// Add the primary key field
		addField(doc, dr, dr.getPrimaryKey());

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
		String key = makeKey(dr, dr.getPrimaryKey().getName());
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
                    throw e;
            } catch (IOException e) {
                    throw e;
            } finally {
                    writer.close();
            }                       
		}

	}

		
	
	
	private void addField(Document doc, RecordDescriptor dr, Field f) {
		String key = makeKey(dr, dr.getPrimaryKey().getName());
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
	 * 
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	public void createIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
        File indexDir = new File(indexPath);
		IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), new StandardAnalyzer(Version.LUCENE_30), 
				true, IndexWriter.MaxFieldLength.UNLIMITED);

		writer.close();
	}
	
	/**
	 * Optimize the Index
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void optimizeIndex() throws InterruptedException, IOException {
		File indexDir = new File(indexPath);
		IndexWriter writer = getIndexWriter(indexDir);
        try {
	        writer.optimize();
		} catch (CorruptIndexException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			writer.close();
		}
	}


}
