package com.bdt.kiradb;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyTest {

    @Test
    public void propertyTest() throws IOException {
        // Read base properties
        Properties p = new Properties();
        InputStream inputStream = getClass().getResourceAsStream("/kiradb.properties");
        p.load(inputStream);
        p.storeToXML(System.out, "base properties");

        // Read override properties
        FileInputStream fileInputStream = new FileInputStream("testdata/properties/override.properties");
        Properties fProperties = new Properties();
        fProperties.load(fileInputStream);
        fProperties.storeToXML(System.out, "override properties");

        // Merge them in a last-one-wins fashion
        p.putAll(fProperties);
        p.storeToXML(System.out, "merged properties");
    }
}
