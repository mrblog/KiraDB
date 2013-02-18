package com.bdt.kiradb;

import java.util.ArrayList;
import java.util.List;

/**
 * RecordDescriptor are the unit of indexing and querying, analogous to a SQL "table".
 * A RecordDescriptor consists of a unique record name (analogous to the table name),
 * a primary key. and an optional set of additional fields (analogous to a SQL column).
 * Each field has a name, a type, and a value.
 * A Record may be configured to store user-defined objects, in which case the object is returned with query results.
 * 
 * @author David Beckemeyer
 *
 */
public class RecordDescriptor {

	private String recordName;
	private Field primaryKey;
	private List<Field> fields;
	private Boolean	storeObjects;
	
	/**
	 * Construct a RecordDescriptor with the given name, with default object store mode (false)
	 * 
	 * @param recordName The globally unique name identifying this record class (table) 
	 */
	public RecordDescriptor(String recordName) {
		setRecordName(recordName);
		setStoreObjects(false);
	}
	/**
	 * Construct a RecordDescriptor with the given name and object store mode
	 * 
	 * @param recordName The globally unique name identifying this record class (table) 
	 * @param storeObjects Specifies whether objects should be stored.
	 */
	public RecordDescriptor(String recordName, Boolean storeObjects) {
		setRecordName(recordName);
		setStoreObjects(storeObjects);
	}
	/**
	 * Construct a RecordDescriptor with the given name and primary key using default object store mode (false)
	 * 
	 * @param recordName The globally unique name
	 * @param primaryKey The primary key
	 */
	public RecordDescriptor(String recordName, Field primaryKey) {
		setRecordName(recordName);
		setPrimaryKey(primaryKey);
		setStoreObjects(false);
	}
	/**
	 * Construct a RecordDescriptor with the given name and primary key and object store mode
	 * 
	 * @param recordName The globally unique name
	 * @param primaryKey The primary key
	 * @param storeObjects Specifies whether objects should be stored.
	 */
	public RecordDescriptor(String recordName, Field primaryKey, Boolean storeObjects) {
		setRecordName(recordName);
		setPrimaryKey(primaryKey);
		setStoreObjects(storeObjects);
	}
	/**
	 * Set unique record name (table name)
	 * 
	 * @param recordName The globally unique name
	 */
	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}
	/**
	 * The record name
	 * 
	 * @return String The record name
	 */
	public String getRecordName() {
		return recordName;
	}
	/**
	 * Set the record primary key
	 * 
	 * @param primaryKey The record primary key
	 */
	public void setPrimaryKey(Field primaryKey) {
		this.primaryKey = primaryKey;
	}
	/**
	 * The record primary key
	 * 
	 * @return Field The record primary key
	 */
	public Field getPrimaryKey() {
		return primaryKey;
	}
	/**
	 * Add a Field to the record field set
	 * 
	 * @param field The field to add
	 */
	public void addField(Field field) {
		if (this.fields == null)
			this.fields = new ArrayList<Field>();
		this.fields.add(field);
	}
	/**
	 * The record field set
	 * 
	 * @return List<Field> The record field set as an array
	 */
	public List<Field> getFields() {
		return fields;
	}
	/**
	 * Set the store object mode on the record
	 * 
	 * @param storeObjects Set to true to store objects, false otherwise
	 */
	public void setStoreObjects(Boolean storeObjects) {
		this.storeObjects = storeObjects;
	}
	/**
	 * The store object mode of the record
	 * 
	 * @return Boolean The store object mode of the record
	 */
	public Boolean getStoreObjects() {
		return storeObjects;
	}
	
}
