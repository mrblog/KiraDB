package com.bdt.kiradb;

/**
 * @author David Beckemeyer and Mark Petrovic
 */
public class KiraCorruptIndexException extends KiraException {

	private static final long serialVersionUID = 1L;

	public KiraCorruptIndexException(String message) {
        super(message);
    }
}
