package com.bdt.kiradb;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static File makeTemporaryDirectory(String prefix, String suffix) {
        try {
            File f = File.createTempFile(prefix, suffix);
            f.delete();
            f.mkdir();
            return f;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File makeTemporaryDirectory() {
        return makeTemporaryDirectory("kiradb", ".index");
    }

}
