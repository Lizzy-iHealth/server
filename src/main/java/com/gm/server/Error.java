package com.gm.server;

public enum Error {

	// auth
	auth_invalid_phone(10000),
	auth_invalid_token(10001),
	auth_invalid_password(10002),
	auth_invalid_key_or_secret(10003),
	auth_incorrect_password(10004),
	auth_phone_not_registered(10005),
	auth_phone_registered(10006), 
	auth_incorrect_token(10007);
	
	public final int code;

	private Error(int code) {
		this.code = code;
	}
}
