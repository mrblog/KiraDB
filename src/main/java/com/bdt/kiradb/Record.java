package com.bdt.kiradb;

/**
 * Record interface for KiraDB Objects
 *  
 * @author David Beckemeyer and Mark Petrovic
 *
 */
public interface Record {

	/**
	 * 
	 * @return RecordDescriptor The record descriptor for Class
	 */
	RecordDescriptor descriptor();
	
	/**
	 * 
	 * @return String The Record name (table name)
	 */
	String getRecordName();
	
	/**
	 * 
	 * @return String The primary key Name
	 */
	String getPrimaryKeyName();
	
}
