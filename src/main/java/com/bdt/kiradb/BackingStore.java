package com.bdt.kiradb;

import java.io.IOException;

import org.jets3t.service.ServiceException;

import com.thoughtworks.xstream.XStream;

public abstract class BackingStore {
	
	abstract void storeObject(XStream xstream, Object object) throws IOException, KiraException;
	
	abstract Object retrieveObject(XStream xstream, Object object, String value) throws KiraException, IOException, ClassNotFoundException;


}
