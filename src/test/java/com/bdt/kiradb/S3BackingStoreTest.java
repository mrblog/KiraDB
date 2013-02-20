package com.bdt.kiradb;

import com.bdt.kiradb.mykdbapp.Person;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class S3BackingStoreTest {
    private Core db;

    @Before
    public void setup() throws KiraCorruptIndexException, IOException {
        db = new Core(Utils.makeTemporaryDirectory().getAbsolutePath());
        db.createIndex();
    }

    @After
    public void teardown() throws IOException {
        db.deleteIndex();
    }

    @Test
    public void testS3BackingStore() throws IOException, InterruptedException, KiraException, ClassNotFoundException {
        String key = System.getProperty("aws.key", null);
        String secret = System.getProperty("aws.secret", null);
        String bucket = System.getProperty("aws.bucket", null);

        if (key == null || secret == null || bucket == null) {
            System.out.printf("Skipping S3 backing store test.  One or more of AWS aws.[key|secret|bucket] not set.");
        }

        System.out.printf("S3 test:  key=%s, secret=%s, bucket=%s\n", key, secret, bucket);

        S3BackingStore fs = new S3BackingStore(key, secret, bucket);
        db.setBackingStore(fs);

        Person p = new Person();
        p.setStoreMode(RecordDescriptor.STORE_MODE_BACKING);
        System.out.println("Testing S3 backing store...");

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

}
