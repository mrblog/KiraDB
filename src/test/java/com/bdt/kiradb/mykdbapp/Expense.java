package com.bdt.kiradb.mykdbapp;

import java.math.BigDecimal;
import java.util.Date;

import com.bdt.kiradb.Field;
import com.bdt.kiradb.FieldType;
import com.bdt.kiradb.Record;
import com.bdt.kiradb.RecordDescriptor;

public class Expense implements Record {

	private static final String RECORD_NAME = "ex";
	private static final String PRIMARY_KEY = "txId";
	public static final String DATE = "date";
	public static final String CATEGORY = "cat";
	public static final String PAYEE = "payee";
	public static final String MEMO = "memo";
	
	private BigDecimal amount;
	private String category;
	private Date date;
	private String memo;
	private String payee;
	private String txId;

	@Override
	public RecordDescriptor descriptor() {
		RecordDescriptor dr = new RecordDescriptor(RECORD_NAME);
        dr.setPrimaryKey(new Field(PRIMARY_KEY, FieldType.STRING, this.getTxId()));
        dr.addField(new Field(DATE, FieldType.DATE, this.getDate()));
        dr.addField(new Field(CATEGORY, FieldType.STRING, this.getCategory()));
        dr.addField(new Field(PAYEE, FieldType.FULLTEXT, this.getPayee()));
        dr.addField(new Field(MEMO, FieldType.FULLTEXT, this.getMemo()));
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

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getMemo() {
		return memo;
	}

	public void setPayee(String payee) {
		this.payee = payee;
	}

	public String getPayee() {
		return payee;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public String getTxId() {
		return txId;
	}

}
