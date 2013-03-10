package com.bdt.kiradb;

import com.thoughtworks.xstream.XStream;

import java.io.IOException;

/**
 * @author David Beckemeyer and Mark Petrovic
 */
public abstract class BackingStore {

    abstract void storeObject(XStream xstream, Record r) throws IOException, KiraException;

    abstract <T extends Record> T retrieveObject(XStream xstream, Record r, String value) throws KiraException, IOException, ClassNotFoundException;

    abstract void removeObject(XStream xstream, Record r, String value) throws KiraException, IOException, ClassNotFoundException;

    abstract <T extends Record> T firstObject(XStream xstream, Record r) throws KiraException, IOException, ClassNotFoundException;

    abstract <T extends Record> T nextObject(XStream xstream, Record r) throws KiraException, IOException, ClassNotFoundException;

    protected String makeKey(Record r) {
        return makeKey(r, (String) r.descriptor().getPrimaryKey().getValue());
    }

    protected String makeKey(Record r, String value) {
        return r.getRecordName() + "/" + value;
    }
}
