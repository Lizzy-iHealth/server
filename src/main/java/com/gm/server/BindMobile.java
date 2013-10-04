package com.gm.server;

import java.io.IOException;
import java.util.Date;

import static com.gm.server.Filters.*;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

public class BindMobile extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1142920677422141888L;
	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private enum Status{
    		OK, MIS_MATCH, NOT_FOUND
    }
	@Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {

        String mobileNumber = req.getParameter("mobileNumber");
        String verifyCode = req.getParameter("verifyCode");
        String passwd = req.getParameter("password");
        String secret = req.getParameter("secret");
        String key = req.getParameter("key");
        Status result = verifyMobiCodePair(mobileNumber,verifyCode);
        switch (result){
        		case OK:
        			createUser(mobileNumber,passwd,secret,key);        			
	            resp.setStatus(HttpServletResponse.SC_OK);
	            break;
        		case MIS_MATCH:
        			resp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        			break;
        		case NOT_FOUND:
        			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        	
    }
	

	private void createUser(String mobileNumber, String passwd, String secret,String key) {
		// TODO Auto-generated method stub
		Date date = new Date();
        Entity user = new Entity("user");
        user.setProperty("mobileNumber", mobileNumber);
        user.setProperty("password", passwd);
        user.setProperty("createTime", date);
        user.setProperty("lastLoginTime",date);
        user.setProperty("secret",secret);
        user.setProperty("key", key);
        
        datastore.put(user);	
	}


	private Status verifyMobiCodePair(String mobileNumber, String verifyCode) {
		// TODO Auto-generated method stub
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		
	    Query query = new Query("mobiCodeRecord").setFilter(eq("mobileNumber", mobileNumber));
	    Entity mobiCodeRecord = datastore.prepare(query).asSingleEntity();
	    if (mobiCodeRecord != null){
	    		if(verifyCode.equalsIgnoreCase((String) mobiCodeRecord.getProperty("verifyCode"))){
	    			return Status.OK;
	    		}else{
	    			return Status.MIS_MATCH;
	    		}
	    }
	    return Status.NOT_FOUND;
	}
}
