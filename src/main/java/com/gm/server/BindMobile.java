package com.gm.server;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.gm.server.Filters.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;

public class BindMobile extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1142920677422141888L;
	
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {

        String mobileNumber = req.getParameter("mobileNumber");
        String verifyCode = req.getParameter("verifyCode");
        String passwd = req.getParameter("password");
        boolean succ = verifyMobiCodePair(mobileNumber,verifyCode);
        if(succ){
            Date date = new Date();

            Entity user = new Entity("user");
            user.setProperty("mobileNumber", mobileNumber);
            user.setProperty("password", passwd);
            user.setProperty("generateTime", date);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.put(user);
        }
        	resp.getWriter().println("Verified="+succ);
    }

	private boolean verifyMobiCodePair(String mobileNumber, String verifyCode) {
		// TODO Auto-generated method stub
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		
	    Query query = new Query("mobiCodeRecord").setFilter(eq("mobileNumber", mobileNumber));
	    Entity mobiCodeRecord = datastore.prepare(query).asSingleEntity();
	    if (mobiCodeRecord != null){
	    		if(verifyCode.equalsIgnoreCase((String) mobiCodeRecord.getProperty("verifyCode"))){
	    			return true;
	    		}
	    }
	    return false;
	}
}
