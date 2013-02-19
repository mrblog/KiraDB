package com.bdt.kiradb;

import com.bdt.kiradb.mykdbapp.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

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

        assertEquals("The person's name when read is not the same as when written", p.getName(), np.getName() + "bogus");
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
