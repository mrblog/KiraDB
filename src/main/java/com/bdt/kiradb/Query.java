package com.bdt.kiradb;

import java.util.ArrayList;
import java.util.List;

/**
 * KiraDb Query class defines a query that is used to fetch KiraDb objects/records
 * 
 * @author David Beckemeyer
 *
 */
public class Query {

	private int limit;
	private int start;
	private Field sortField;
	private Boolean reverse;
	private Record r;
	private List<FieldQuery>queries;
	
	/**
	 * Construct a KiraDb Query
	 * 
	 * @param r the Record class associated with the query
	 */
	public Query(Record r) {
		this.r = r;
		setStart(0);
		setLimit(100);
		setReverse(false);
	}
	
	public int getLimit() {
		return limit;
	}
	
	/**
	 * Limit the number of records returned
	 * Set to Integer.MAX_VALUE to return all records
	 * Default is 100 records
	 * 
	 * @param limit number of records (objects) to return
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public int getStart() {
		return start;
	}
	
	/**
	 * Set the number of records to skip before returning any results.
	 * Default is zero
	 * 
	 * @param start number of records to skip
	 */
	public void setStart(int start) {
		this.start = start;
	}
	public Field getSortField() {
		return sortField;
	}
	
	/**
	 * Define the sort field to use in ordering the results
	 * Default is "date" field 
	 * 
	 * @param sortField sort field
	 */
	public void setSortField(Field sortField) {
		this.sortField = sortField;
	}

	/**
	 * Define the sort field to use in ordering the results and
	 * sort order (reverse true or false)
	 * 
	 * @param sortField field for sort order
	 * @param reverse set to true to reverse the order (descending)
	 */
	public void setSortField(Field sortField, Boolean reverse) {
		setSortField(sortField);
		setReverse(reverse);
	}
	
	/**
	 * Define the sort field to use in ordering the results
	 * 
	 * @param sortFieldName name of the field for sort order
	 */
	public void setSortField(String sortFieldName) {
		setSortField(r.descriptor().getFieldByName(sortFieldName));
	}
	
	/**
	 * Define the sort field to use in ordering the results and
	 * sort order (reverse true or false)
	 * 
	 * @param sortFieldName name of the field for sort order
	 * @param reverse set to true to reverse the order (descending)
	 */
	public void setSortField(String sortFieldName, Boolean reverse) {
		setSortField(r.descriptor().getFieldByName(sortFieldName));
		setReverse(reverse);
	}
	
	public Boolean getReverse() {
		return reverse;
	}
	
	/**
	 * Define the sort order. Default is false.
	 * 
	 * @param reverse set to true to reverse the order (descending)
	 */
	public void setReverse(Boolean reverse) {
		this.reverse = reverse;
	}

	/**
	 * Add a constraint to the query that requires a particular field's value to
	 * match the provided query value.
	 * Provide multiple match constraints, and objects will only be in the 
	 * results if they match all of the constraints (AND behavior)
	 * 
	 * @param queryField field to query
	 * @param querystr the value that the field must contain
	 */
	public void whereMatches(Field queryField, String querystr) {
		if (queries == null) {
			queries = new ArrayList<FieldQuery>();
		}
		queries.add(new FieldQuery(queryField, querystr));
	}
	
	/**
	 * Add a constraint to the query that requires a particular field's value to
	 * match the provided query value.
	 * Provide multiple match constraints, and objects will only be in the 
	 * results if they match all of the constraints (AND behavior)
	 * 
	 * @param queryFieldName name of the field to query
	 * @param querystr the value that the field must contain
	 */
	public void whereMatches(String queryFieldName, String querystr) {
		whereMatches(r.descriptor().getFieldByName(queryFieldName), querystr);
	}
	
	/**
	 * Accessor for the query set
	 */
	public List<FieldQuery>getQueries() {
		return queries;
	}

	@SuppressWarnings("unchecked")
	public<T extends Record> T getRecord() {
		return (T) r;
	}
}
