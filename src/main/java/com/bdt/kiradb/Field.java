package com.bdt.kiradb;

/**
 * A field is a member of a record. Each field has two parts, a name and a value,
 * where the value is of a specified type
 * 
 * @author David Beckemeyer
 *
 */
public class Field {

	private String name;
	private FieldType type;
	private Object value;
	
	/**
	 * Create a Field specifying its name, value and type
	 * 
	 * @param name The name of the field
	 * @param type The field type (type of value)
	 * @param value The value as specified by the type
	 */
	public Field(String name, FieldType type, Object value) {
		setName(name);
		setType(type);
		setValue(value);
	}
	/**
	 * Set the field name
	 * 
	 * @param name The field name
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * The name of the field
	 *  
	 * @return String The field name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Set the field type
	 * 
	 * @param type The field type
	 */
	public void setType(FieldType type) {
		this.type = type;
	}
	/**
	 * The field type
	 * 
	 * @return FieldType The field type
	 */
	public FieldType getType() {
		return type;
	}
	/**
	 * Set the value for the field
	 * 
	 * @param value Value object of specified type
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	/**
	 * The value for the field
	 * 
	 * @return Object The field value 
	 */
	public Object getValue() {
		return value;
	}
	
}
