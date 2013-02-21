package com.bdt.kiradb;

import java.io.IOException;


import com.thoughtworks.xstream.XStream;

public abstract class BackingStore {
	
	abstract void storeObject(XStream xstream, Record r) throws IOException, KiraException;
	
	abstract Object retrieveObject(XStream xstream, Record r, String value) throws KiraException, IOException, ClassNotFoundException;

	abstract void removeObject(XStream xstream, Record r, String value) throws KiraException, IOException, ClassNotFoundException;

	protected String makeKey(Record r) {
		return makeKey(r, (String)r.descriptor().getPrimaryKey().getValue());
	}
	protected String makeKey(Record r, String value) {
		return r.getRecordName() + "/" + value;
	}

}
