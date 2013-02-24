package com.bdt.kiradb;

import com.bdt.kiradb.mykdbapp.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class S3BackingStoreTest {
    private KiraDb db;

    @Before
    public void setup() throws KiraCorruptIndexException, IOException {
    }

    @After
    public void teardown() throws IOException {
        if (db != null) {
            db.deleteIndex();
        }
    }

    @Test
    public void testS3BackingStoreWithSystemProperties() throws IOException, InterruptedException, KiraException, ClassNotFoundException {
        String key = System.getProperty("aws.key", null);
        String secret = System.getProperty("aws.secret", null);
        String bucket = System.getProperty("aws.bucket", null);

        if (key == null || secret == null || bucket == null) {
            System.out.printf("Skipping S3 backing store test.  One or more of AWS System properties aws.[key|secret|bucket] not set.");
            return;
        } else {
            // disable caching to ensure the S3 store is read and written
            db = new S3KiraDB(Utils.makeTemporaryDirectory(), true, key, secret, bucket);
            db.createIndex();
            System.out.printf("S3 test:  key=%s, secret=%s, bucket=%s\n", key, secret, bucket);
        }

        Person p = new Person();
        p.setStoreMode(RecordDescriptor.STORE_MODE_BACKING);
        System.out.println("Testing S3 backing store...");

        p.setAccount("3219");
        p.setName("Fred Jones");
        p.setCreatedAt(new Date());
        System.out.println("Writing person...");
        db.storeObject(p);
        System.out.println("Reading person...");
        Person np = db.retrieveObjectByPrimaryKey(p, p.getAccount());
        System.out.println("Read object: " + np.getName());
        assertNotNull("The result should not be null", np);
        assertEquals("The person's name when read is not the same as when written", p.getName(), np.getName());

        db.removeObjectByPrimaryKey(p, p.getAccount());

        try {
            Person nnp = db.retrieveObjectByPrimaryKey(p, p.getAccount());
            assertNotNull("The object was not removed properly", nnp);
        } catch (KiraException e) {
            // This is the expected result
            System.out.println("got expected KiraException: " + e.getMessage());
        }

    }

    @Test(expected = RuntimeException.class)
    public void testS3BackingStoreWithS3Properties() throws IOException, InterruptedException, KiraException, ClassNotFoundException {
        db = new S3KiraDB(Utils.makeTemporaryDirectory(), true);

        // Without a valid /s3.properties on the classpath, this test will not reach the next line of code.

        db.createIndex();

        Person p = new Person();
        p.setStoreMode(RecordDescriptor.STORE_MODE_BACKING);
        System.out.println("Testing S3 backing store...");

        p.setAccount("3219");
        p.setName("Fred Jones");
        p.setCreatedAt(new Date());
        System.out.println("Writing person...");
        db.storeObject(p);
        System.out.println("Reading person...");
        Person np = db.retrieveObjectByPrimaryKey(p, p.getAccount());
        System.out.println("Read object: " + np.getName());
        assertNotNull("The result should not be null", np);
        assertEquals("The person's name when read is not the same as when written", p.getName(), np.getName());

        db.removeObjectByPrimaryKey(p, p.getAccount());

        try {
            Person nnp = db.retrieveObjectByPrimaryKey(p, p.getAccount());
            assertNotNull("The object was not removed properly", nnp);
        } catch (KiraException e) {
            // This is the expected result
            System.out.println("got expected KiraException: " + e.getMessage());
        }

    }

}
