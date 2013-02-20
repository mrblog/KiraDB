package com.bdt.kiradb;

import com.bdt.kiradb.mykdbapp.Expense;
import com.bdt.kiradb.mykdbapp.Person;
import com.bdt.kiradb.mykdbapp.TextDocument;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CoreTest {
    Core db;

    // Gets run before each method annotated with @Test
    @Before
    public void setup() throws KiraCorruptIndexException, IOException {
        db = new Core("KiraDBIndex");
        System.out.println("Creating Index...");
        db.createIndex();
    }

    // Gets run after any method annotated with @Test
    @After
    public void teardown() throws IOException {
        db.deleteIndex();
    }

    @Test
    public void testPersonStuff() throws KiraException, IOException, ClassNotFoundException, InterruptedException {
        // try writing to the index
        Person p = new Person();
        p.setAccount("1234");
        p.setName("John Smith");
        p.setCreatedAt(new Date());
        System.out.println("Writing person...");
        db.storeObject(p);
        System.out.println("Reading person...");
        Person np = (Person) db.retrieveObjectbyPrimaryKey(p, p.getAccount());
        System.out.println("Read object: " + np.getName());
        assertNotNull("The result should not be null", np);
        assertEquals("The person's name when read is not the same as when written", p.getName(), np.getName());
    }

    @Test
    public void testExpenseStuff() throws IOException, InterruptedException, ClassNotFoundException, KiraException {

    	Expense exp1 = new Expense();
    	exp1.setCategory("Clothing");
        exp1.setDate(new Date());
        exp1.setMemo("-");
        exp1.setPayee("Marshalls");
        exp1.setTxId("14856");
        System.out.println("Writing expense 1...");
        db.storeObject(exp1);
        
        Expense exp2 = new Expense();
    	exp2.setCategory("Utilities");
        exp2.setDate(new Date());
        exp2.setMemo("garbage bill");
        exp2.setPayee("petaluma refuse and recycling");
        exp2.setTxId("11564");
        System.out.println("Writing expense... 2");
        db.storeObject(exp2);
        
        System.out.println("Reading expense... 1");
        Map<String,String> res1 = (Map<String, String>) db.retrieveObjectbyPrimaryKey(exp1, exp1.getTxId());
        assertNotNull("The result should not be null", res1);

        db.dumpDocuments(exp1.getRecordName());
        
        for (String key : res1.keySet()) {
        	System.out.println("  1 key: " + key + " value: "
        			+ res1.get(key));
        }

        System.out.println("Reading expense... 2");

        Map<String,String> res2 = (Map<String, String>) db.retrieveObjectbyPrimaryKey(exp2, exp2.getTxId());
        assertNotNull("The result should not be null", res2);
        for (String key : res2.keySet()) {
        	System.out.println("  2 key: " + key + " value: "
        			+ res2.get(key));
        }

        assertEquals("Expected txId " +  exp1.getTxId() + " but got: " + res1.get(exp1.getPrimaryKeyName()), exp1.getTxId(), res1.get(exp1.getPrimaryKeyName()));
        assertEquals("Expected txId " +  exp2.getTxId() + " but got: " + res2.get(exp2.getPrimaryKeyName()), exp2.getTxId(), res2.get(exp2.getPrimaryKeyName()));

        List<Object> q1Results = db.executeQuery(exp1, Expense.CATEGORY, "Clothing", 10, 0, Expense.DATE, true);
        assertNotNull("The result should not be null", q1Results);
        System.out.println("query 1 matched " + q1Results.size() + " records");
        for (Object id: q1Results) {
        	System.out.println("query 1 matched id: " + (String)id);

        }
        assertEquals("Category query 1 failed", (String)q1Results.get(0), exp1.getTxId());

        List<Object> q2Results = db.executeQuery(exp1, Expense.MEMO, "garbage", 10, 0, Expense.DATE, true);
        assertNotNull("The result should not be null", q2Results);
        System.out.println("query 2 matched " + q2Results.size() + " records");
        for (Object id: q2Results) {
        	System.out.println("query 2 matched id: " + (String)id);
        }
        assertEquals("Memo query 2 failed", (String)q2Results.get(0), exp2.getTxId());

    }
    
    @Test
    public void testFileBackingStore() throws IOException, InterruptedException, KiraException, ClassNotFoundException {
    	FileBackingStore fs = new FileBackingStore(".");
    	db.setBackingStore(fs);
    	Person p = new Person();
    	p.setStoreMode(RecordDescriptor.STORE_MODE_BACKING);
        System.out.println("Testing File backing store...");

        p.setAccount("3219");
        p.setName("Fred Jones");
        p.setCreatedAt(new Date());
        System.out.println("Writing person...");
        db.storeObject(p);
        System.out.println("Reading person...");
        Person np = (Person) db.retrieveObjectbyPrimaryKey(p, p.getAccount());
        System.out.println("Read object: " + np.getName());
        assertNotNull("The result should not be null", np);
        assertEquals("The person's name when read is not the same as when written", p.getName(), np.getName());

        File directory = new File(p.getRecordName());
        FileUtils.deleteDirectory(directory);
    }


    @Test
    public void testCACMOneDoc() throws IOException, InterruptedException, KiraException, ClassNotFoundException {
    	TextDocument doc = new TextDocument();
    	doc.setDocId("CACM-0040");
    	doc.setTitle("Fingers or Fists? (The Choice of Decimal or Binary Representation)");
    	doc.setBody("\n\nFingers or Fists? (The Choice of Decimal or Binary Representation)\n\nThe binary number system offers many advantages\nover a decimal representation for a high-performance, \ngeneral-purpose computer.  The greater simplicity of\na binary arithmetic unit and the greater compactness \nof binary numbers both contribute directly to arithmetic\nspeed.  Less obvious and perhaps more important \nis the way binary addressing and instruction formats can\nincrease the overall performance.  Binary addresses \nare also essential to certain powerful operations which\nare not practical with decimal instruction formats. \n On the other hand, decimal numbers are essential for\ncommunicating between man and the computer.  In \napplications requiring the processing of a large volume\nof inherently decimal input and output data, \nthe time for decimal-binary conversion needed by a purely\nbinary computer may be significant.  A slower \ndecimal adder may take less time than a fast binary adder\ndoing an addition and two conversions.  A careful \nreview of the significance of decimal and binary addressing\nand both binary and decimal data arithmetic, \nsupplemented by efficient conversion instructions.\n\nCACM December, 1959\n\nBuchholz, W.\n\nCA591202 JB March 22, 1978  3:47 PM\n\n40	5	40\n40	5	40\n40	5	40\n\n");

        db.storeObject(doc);


        List<Object> qResults = db.executeQuery(new TextDocument(), TextDocument.BODY, "decimal", 10, 0, null, true);

        assertNotNull("The CACM query result should not be null", qResults);

        for (Object id: qResults) {
        	System.out.println("CACM query matched id: " + (String)id);
        }

    }

    @Test
    public void testRelated() throws IOException, InterruptedException, KiraException, ClassNotFoundException {
    	Map<String,String> sourceDocs = new HashMap<String,String>();
    	sourceDocs.put("CACM-0212", "Bisection Routine (Algorithm 4)");
    	sourceDocs.put("CACM-0213", "Numerical Inversion of Laplace Transforms");
    	sourceDocs.put("CACM-0214", "An Algorithm Defining ALGOL Assignment Statements");
    	sourceDocs.put("CACM-0215", "The Execute Operations-A Fourth Mode of Instruction Sequencing");
    	sourceDocs.put("CACM-0216", "A Note on the Use of the Abacus in Number Conversion");
    	sourceDocs.put("CACM-0217", "Soviet Computer Technology-1959");
    	sourceDocs.put("CACM-0218", "Computer Preparation of a Poetry Concordance");
    	sourceDocs.put("CACM-0219", "Marriage-with Problems");
    	sourceDocs.put("CACM-0220", "A New Method of Computation of Square Roots Without Using Division");
    	sourceDocs.put("CACM-0221", "The Basic Side of Tape Labeling");
    	sourceDocs.put("CACM-0222", "Coding Isomorphisms");
    	sourceDocs.put("CACM-0223", "Selfcipher: Programming");
    	sourceDocs.put("CACM-0224", "Sequential Formula Translation");
    	sourceDocs.put("CACM-0225", "A Techniquefor Handling Macro Instructions (Corrigendum)");
    	sourceDocs.put("CACM-0226", "Solution of Polynomial Equation by");
    	sourceDocs.put("CACM-0227", "ROOTFINDER (Algorithm 2)");
    	sourceDocs.put("CACM-0228", "QUADI (Algorithm 1)");
    	sourceDocs.put("CACM-0229", "A Terminology Proposal");
    	sourceDocs.put("CACM-0230", "A Proposal for Character Code Compatibility");
    	sourceDocs.put("CACM-0231", "A Proposal for a Set of Publication Standards for Use by the ACM");

    	Map<String,String[]>expectedResults = new HashMap<String,String[]>();
    	expectedResults.put("CACM-0231", new String[] { "CACM-0229", "CACM-0216", "CACM-0230"});
    	expectedResults.put("CACM-0230", new String[] { "CACM-0222", "CACM-0229", "CACM-0231"});
    	expectedResults.put("CACM-0214", new String[] { "CACM-0228", "CACM-0227", "CACM-0212"});
    	expectedResults.put("CACM-0212", new String[] { "CACM-0228", "CACM-0227", "CACM-0214"});
    	expectedResults.put("CACM-0228", new String[] { "CACM-0227", "CACM-0212", "CACM-0214"});
    	expectedResults.put("CACM-0227", new String[] { "CACM-0228", "CACM-0212", "CACM-0214"});
    	expectedResults.put("CACM-0218", new String[] { "CACM-0217"});
    	expectedResults.put("CACM-0217", new String[] { "CACM-0218"});
    	expectedResults.put("CACM-0229", new String[] { "CACM-0230", "CACM-0231"});
    	expectedResults.put("CACM-0216", new String[]{ "CACM-0231"});

    	for (String docId : sourceDocs.keySet()) {
    	   	TextDocument doc = new TextDocument();
        	doc.setDocId(docId);
        	doc.setTitle(sourceDocs.get(docId));
        	// body is a copy of the title, but indexed full-text
        	doc.setBody(sourceDocs.get(docId));

            db.storeObject(doc);

    	}
    	String[] fieldNames = { TextDocument.BODY };
    	for (String docId : sourceDocs.keySet()) {
    		String testStr = sourceDocs.get(docId);
    		List<String> matches = db.relatedObjects(new TextDocument(), testStr, fieldNames, 5, docId);
            assertNotNull("The relatedObjects query result should not be null", matches);
            System.out.println("'" + testStr + "' hits: " + matches.size());
            String[] expectedDocs = expectedResults.get(docId);
            if (expectedDocs != null) {
            	assertEquals("Expected " + expectedDocs.length + " hits on " + docId, matches.size(), expectedDocs.length);
            	for (String hit: expectedDocs ) {
            		assertTrue("Expected " + hit + " in results for " + docId, matches.contains(hit));
            	}
            } else {
            	assertTrue("Expected zero hits on " + docId, (matches.size() == 0));
            }
    		/*for (String hit : matches) {
                System.out.println(docId + " ~ " + hit + " '" + sourceDocs.get(hit) + "'");

    		}*/
    	}
    }

/*
    @Test
    public void testSomeResource() {
        InputStream inputStream = getClass().getResourceAsStream("/opaquedata.bin");
        Assert.assertNotNull(inputStream);
        System.out.printf("got an inputstream on some test data\n");

        // now do something useful with the inputstream...
        // ...
    }
*/

}
