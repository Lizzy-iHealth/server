package com.gm.server;

import static com.gm.server.Filters.eq;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.gm.server.model.*;


public class LoginServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private enum Status{
    		OK, MIS_MATCH, NOT_FOUND
    }
    private final DAO dao = DAO.get();
	@Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
	  API.login.execute(req, resp);
/*
        String mobileNumber = req.getParameter("mobileNumber");
   
        String password = req.getParameter("password");
        String secret = req.getParameter("secret");
        String key = req.getParameter("key");
        Status result = verifyAndLogin(mobileNumber,password,secret,key);
        switch (result){
        		case OK:
 	            resp.setStatus(HttpServletResponse.SC_OK);
	            break;
        		case MIS_MATCH:
        			resp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        			break;
        		case NOT_FOUND:
        			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        	*/
    }
	


	private Status verifyAndLogin(String mobileNumber, String password, String secret, String key) {
		// TODO Auto-generated method stub
		User user = dao.get(KeyFactory.stringToKey(key),User.class);
		
		 if(user!=null){
			 if(password.equals(user.getPassword())){
				 user.login(secret);
				 dao.save(user);
				 return Status.OK;
			 }else{
				 return Status.MIS_MATCH;
			 }
		 }else{
			 return Status.NOT_FOUND;
		 }
	}

}
