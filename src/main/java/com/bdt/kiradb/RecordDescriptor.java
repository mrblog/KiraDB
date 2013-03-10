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
 * @author David Beckemeyer and Mark Petrovic
 *
 */
public class RecordDescriptor {

	public static int STORE_MODE_NONE = 0;
	public static int STORE_MODE_INDEX = 1;
	public static int STORE_MODE_BACKING = 2;

	private String recordName;
	private Field primaryKey;
	private List<Field> fields;
	private int storeMode;
	
    /**
	 * Construct a RecordDescriptor with the given name, with default object store mode (STORE_MODE_NONE)
	 * 
	 * @param recordName The globally unique name identifying this record class (table) 
	 */
	public RecordDescriptor(String recordName) {
		setRecordName(recordName);
		setStoreMode(STORE_MODE_NONE);
	}
	/**
	 * Construct a RecordDescriptor with the given name and object store mode
	 * 
	 * @param recordName The globally unique name identifying this record class (table) 
	 * @param storeMode Specifies the storage mode for this record class
	 */
	public RecordDescriptor(String recordName, int storeMode) {
		setRecordName(recordName);
		setStoreMode(storeMode);
	}
	/**
	 * Construct a RecordDescriptor with the given name and primary key using default object store mode (STORE_MODE_NONE)
	 * 
	 * @param recordName The globally unique name
	 * @param primaryKey The primary key
	 */
	public RecordDescriptor(String recordName, Field primaryKey) {
		setRecordName(recordName);
		setPrimaryKey(primaryKey);
		setStoreMode(STORE_MODE_NONE);
	}
	/**
	 * Construct a RecordDescriptor with the given name and primary key and object store mode
	 * 
	 * @param recordName The globally unique name
	 * @param primaryKey The primary key
	 * @param storeMode Specifies the storage mode for this record class
	 */
	public RecordDescriptor(String recordName, Field primaryKey, int storeMode) {
		setRecordName(recordName);
		setPrimaryKey(primaryKey);
		setStoreMode(storeMode);
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
	 * Get Field by name
	 * 
	 * @param name The name of the field
	 * 
	 * @return Field The matching field, or null
	 */
	public Field getFieldByName(String name) {
		if (fields != null) {
			for (Field f: fields) {
				if (f.getName().equals(name))
					return f;
			}
		}
		return null;
	}
	/**
	 * Set the mode for storing objects
	 * <p>
	 * Bitmask
	 * <p>
	 * <ul><li>STORE_MODE_NONE - objects are not stored</li>
	 * <li>STORE_MODE_INDEX - objects are stored in the Index</li>
	 * <li>STORE_MODE_BACKING - objects are stored in the active backing store</li>
	 * </ul>
	 * 
	 * @param storeMode
	 */
	public void setStoreMode(int storeMode) {
		this.storeMode = storeMode;
	}
	/**
	 * 
	 * @return The mode for storing objects
	 */
	public int getStoreMode() {
		return storeMode;
	}
	
}
