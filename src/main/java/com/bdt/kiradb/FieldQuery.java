package com.bdt.kiradb;

/**
 * 
 * A query on a particular field
 * 
 * @author David Beckemeyer
 *
 */
public class FieldQuery {

	private Field queryField;
	private String querystr;
	
	/**
	 * Construct a field query with specified field and query string
	 * 
	 * @param queryField field to query
	 * @param querystr the value to match
	 */
	public FieldQuery(Field queryField, String querystr) {
		setQueryField(queryField);
		setQuerystr(querystr);
	}

	public Field getQueryField() {
		return queryField;
	}
	public void setQueryField(Field queryField) {
		this.queryField = queryField;
	}
	public String getQuerystr() {
		return querystr;
	}
	public void setQuerystr(String querystr) {
		this.querystr = querystr;
	}
	
	
}
