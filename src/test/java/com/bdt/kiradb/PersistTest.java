package com.bdt.kiradb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.bdt.kiradb.mykdbapp.Person;

public class PersistTest {

    private final static String testAccounts[] = { "332", "109", "320", "644" };
    private final static String testNames[] = { "Wolpe, H.","Grumette, M.","Halpern, M.","Kenny, B. C." };

    private final static String INDEX = "KiraDBPersistedIndex";
    
    @Test
	public void firstPass() throws IOException, InterruptedException, KiraException, ClassNotFoundException {
    	KiraDb db = new KiraDb(new File(INDEX));
        for (int i = 0; i < testAccounts.length; i++) {
        	Person xp = new Person();
            xp.setAccount(testAccounts[i]);
            xp.setName(testNames[i]);
            xp.setCreatedAt(new Date());
            System.out.println("Writing person... " + xp.getAccount());
            db.storeObject(xp);
        	
        }
        List<Person> q1Results = db.executeQuery(new Person(), (String)null, null, 10, 0, null, true);
        assertNotNull("PASS1 q1Results result should not be null", q1Results);
        System.out.println("PASS1 Found " + q1Results.size() + " records.");
        assertEquals("Incorrect number of records", q1Results.size(), testAccounts.length);

	}
	
	@Test
	public void secondPass() throws IOException, KiraException, ClassNotFoundException {
		KiraDb db = new KiraDb(new File(INDEX));

        List<Person> q1Results = db.executeQuery(new Person(), (String)null, null, 10, 0, null, true);
        assertNotNull("PASS2 q1Results result should not be null", q1Results);
        System.out.println("PASS2 Found " + q1Results.size() + " records.");
        assertEquals("Incorrect number of records", q1Results.size(), testAccounts.length);

    	for (Person p: q1Results) {
    		System.out.println("Account: " + p.getAccount() + " Name: " + p.getName());
    	}
        // ensure each test account is accounted for exactly once
        for (int i = 0; i < testAccounts.length; i++) {
        	int n = 0;
        	for (Person p: q1Results) {
        		if (p.getAccount().equals(testAccounts[i])) {
        			assertEquals("Duplicate match on account " + testAccounts[i], n, 0);
        			n++;
        			assertTrue("Name does not match on account " + testAccounts[i], p.getName().equals(testNames[i]));
        		}
        	}
			assertEquals("No match on account " + testAccounts[i], n, 1);

        }
        
        System.out.println("Deleting index " + INDEX);

        db.deleteIndex();
	}
}
