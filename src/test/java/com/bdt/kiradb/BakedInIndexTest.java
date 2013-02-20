package com.bdt.kiradb;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class BakedInIndexTest {

    private File testIndexDirectory;

    @Before
    public void setup() throws IOException {
        testIndexDirectory = File.createTempFile("kiradb", ".testindex");
        testIndexDirectory.delete();
        testIndexDirectory.mkdir();
        FileUtils.copyDirectory(new File("testdata/1"), testIndexDirectory);
        System.out.printf("working test index=%s\n", testIndexDirectory);
    }

    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(testIndexDirectory);
    }

    @Test
    @Ignore
    public void test1() {
        // do something
    }
}
