package com.gm.server;

public class ApiException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final Error error;

	public ApiException(Error error) {
		this.error = error;
	}
}
