package com.bdt.kiradb;

import com.thoughtworks.xstream.XStream;

import java.io.IOException;

public class NoOpBackingStore extends BackingStore {
    @Override
    void storeObject(XStream xstream, Record r) throws IOException, KiraException {
    }

    @Override
    public<T extends Record> T retrieveObject(XStream xstream, Record r, String value) throws KiraException, IOException, ClassNotFoundException {
        return null;
    }

    @Override
    void removeObject(XStream xstream, Record r, String value) throws KiraException, IOException, ClassNotFoundException {
    }

	@Override
    public<T extends Record> T firstObject(XStream xstream, Record r) throws KiraException,
			IOException, ClassNotFoundException {
		return null;
	}

	@Override
    public<T extends Record> T nextObject(XStream xstream, Record r) throws KiraException,
			IOException, ClassNotFoundException {
		return null;
	}
}
