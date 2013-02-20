package com.bdt.kiradb;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CACMDocTest {
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
    public void testCACMOneDoc() throws IOException, InterruptedException, KiraException, ClassNotFoundException {
        TextDocument doc = new TextDocument();
        doc.setDocId("CACM-0040");
        doc.setTitle("Fingers or Fists? (The Choice of Decimal or Binary Representation)");
        doc.setBody("\n\nFingers or Fists? (The Choice of Decimal or Binary Representation)\n\nThe binary number system offers many advantages\nover a decimal representation for a high-performance, \ngeneral-purpose computer.  The greater simplicity of\na binary arithmetic unit and the greater compactness \nof binary numbers both contribute directly to arithmetic\nspeed.  Less obvious and perhaps more important \nis the way binary addressing and instruction formats can\nincrease the overall performance.  Binary addresses \nare also essential to certain powerful operations which\nare not practical with decimal instruction formats. \n On the other hand, decimal numbers are essential for\ncommunicating between man and the computer.  In \napplications requiring the processing of a large volume\nof inherently decimal input and output data, \nthe time for decimal-binary conversion needed by a purely\nbinary computer may be significant.  A slower \ndecimal adder may take less time than a fast binary adder\ndoing an addition and two conversions.  A careful \nreview of the significance of decimal and binary addressing\nand both binary and decimal data arithmetic, \nsupplemented by efficient conversion instructions.\n\nCACM December, 1959\n\nBuchholz, W.\n\nCA591202 JB March 22, 1978  3:47 PM\n\n40	5	40\n40	5	40\n40	5	40\n\n");

        db.storeObject(doc);


        List<Object> qResults = db.executeQuery(new TextDocument(), TextDocument.BODY, "decimal", 10, 0, null, true);

        assertNotNull("The CACM query result should not be null", qResults);

        for (Object id : qResults) {
            System.out.println("CACM query matched id: " + (String) id);
        }

    }

}
