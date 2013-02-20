package com.bdt.kiradb.mykdbapp;

import com.bdt.kiradb.Field;
import com.bdt.kiradb.FieldType;
import com.bdt.kiradb.Record;
import com.bdt.kiradb.RecordDescriptor;

public class TextDocument implements Record {

	private static final String RECORD_NAME = "cacm";
	private static final String PRIMARY_KEY = "docId";
	private static final String TITLE = "title";
	public static final String BODY = "body";
	
	private String docId;
	private String title;
	private String body;
	
	@Override
	public RecordDescriptor descriptor() {
		RecordDescriptor dr = new RecordDescriptor(RECORD_NAME);
        dr.setPrimaryKey(new Field(PRIMARY_KEY, FieldType.STRING, this.getDocId()));
        dr.addField(new Field(TITLE, FieldType.STRING, this.getTitle()));
        dr.addField(new Field(BODY, FieldType.FULLTEXT, this.getBody()));
        return dr;
	}

	@Override
	public String getRecordName() {
		return RECORD_NAME;
	}

	@Override
	public String getPrimaryKeyName() {
		return PRIMARY_KEY;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public String getDocId() {
		return docId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getBody() {
		return body;
	}

}
