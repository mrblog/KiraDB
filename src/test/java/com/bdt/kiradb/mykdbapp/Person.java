package com.bdt.kiradb.mykdbapp;

import com.bdt.kiradb.Field;
import com.bdt.kiradb.FieldType;
import com.bdt.kiradb.Record;
import com.bdt.kiradb.RecordDescriptor;

import java.util.Date;


public class Person implements Record {

    private static final String RECORD_NAME = "person";
    private static final String PRIMARY_KEY = "acct";
    private static final String DATE = "createAt";

    private String account;
    private String name;
    private Date createdAt;

    @Override
    public RecordDescriptor descriptor() {
        RecordDescriptor dr = new RecordDescriptor(RECORD_NAME);
        dr.setPrimaryKey(new Field(PRIMARY_KEY, FieldType.STRING, getAccount()));
        dr.addField(new Field(DATE, FieldType.DATE, getCreatedAt()));
        dr.setStoreObjects(true);
        return dr;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String getPrimaryKeyName() {
        return PRIMARY_KEY;
    }

    @Override
    public String getRecordName() {
        return RECORD_NAME;
    }


}
