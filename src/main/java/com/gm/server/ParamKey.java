package com.gm.server;

import javax.servlet.http.HttpServletRequest;


public enum ParamKey {

	phone,
	password,
	token,
	key,
	secret, 
	hmac;
	
	public String getValue(HttpServletRequest req) {
		return req.getParameter(name());
	}
	
	public String getValue(HttpServletRequest req, String defaultValue) {
		String value = req.getParameter(name());
		return value == null ? defaultValue : value;
	}
	
	public String[] getValues(HttpServletRequest req) {
		return req.getParameterValues(name());
	}
}
