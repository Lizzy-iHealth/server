package com.gm.server;

import javax.servlet.http.HttpServletRequest;


public enum ParamKey {

	phone,
	password,
	token,
	key,
	secret, 
	hmac, 
	device_id,
	user_id,
	life_span,
	id,
	owner_id,
	applicant,
	applicants, //update applicants in batch
	quest;
	
	public String getValue(HttpServletRequest req) {
		return req.getParameter(name());
	}
	
	public long getLong(HttpServletRequest req, long defaultValue){
	  String value = req.getParameter(name());
	  return (value == null)? defaultValue : Long.valueOf(value).longValue();
	}
	
	 public long[] getLongs(HttpServletRequest req, long defaultValue){
	   String[] values = req.getParameterValues(name());
	 
	    
	    long[] longs = new long[values.length];
	    for(int i = 0; i < values.length;i++){
	      try {
	      //  System.out.println(i + " = " + values[i]);
	        longs[i] = Long.parseLong(values[i]) ;
	      } catch (NumberFormatException e) {
	        longs[i] = defaultValue;
	      }
	    }
	    return longs;
	  }
	 
	public String getValue(HttpServletRequest req, String defaultValue) {
		String value = req.getParameter(name());
		return value == null ? defaultValue : value;
	}
	
	public String[] getValues(HttpServletRequest req) {
		return req.getParameterValues(name());
	}
}
