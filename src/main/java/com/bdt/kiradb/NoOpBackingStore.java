package com.bdt.kiradb;

import com.thoughtworks.xstream.XStream;

import java.io.IOException;

public class NoOpBackingStore extends BackingStore {
    @Override
    void storeObject(XStream xstream, Record r) throws IOException, KiraException {
    }

    @Override
    Object retrieveObject(XStream xstream, Record r, String value) throws KiraException, IOException, ClassNotFoundException {
        return null;
    }

    @Override
    void removeObject(XStream xstream, Record r, String value) throws KiraException, IOException, ClassNotFoundException {
    }
}
