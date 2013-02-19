package com.bdt.kiradb;

/**
 * An abstraction for a field type. Instances are immutable.
 * 
 * @author David Beckemeyer and Mark Petrovic
 *
 */
public enum FieldType {
		/**
		 * simple string (not analyzed)
		 */
		STRING,
		/**
		 * numeric value
		 */
		NUMBER,
		/**
		 * Date
		 */
		DATE,
		/**
		 * a field with full-text search capabilities
		 */
		FULLTEXT;
		
}


