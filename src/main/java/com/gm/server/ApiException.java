package com.gm.server;

public class ApiException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final int error;

	public ApiException(int error) {
		this.error = error;
	}
}
