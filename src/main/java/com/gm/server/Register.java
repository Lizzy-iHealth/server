package com.gm.server;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;



public class Register extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(SignGuestbookServlet.class.getName());

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {

        String mobileNumber = req.getParameter("mobileNumber");
        boolean succ = storeAndSendVerifyCode(mobileNumber);
        if(succ){
        		//200
        		resp.setStatus(resp.SC_OK);
        }else{
        		//503
        		resp.setStatus(resp.SC_SERVICE_UNAVAILABLE);
        }
   
        	
    }


private boolean storeAndSendVerifyCode(String mobileNumber) {
		// TODO Auto-generated method stub
    	
            Date date = new Date();
            String verifyCode = generateVerifyCode();
            Entity mobiCodeRecord = new Entity("mobiCodeRecord");
            mobiCodeRecord.setProperty("mobileNumber", mobileNumber);
            mobiCodeRecord.setProperty("verifyCode", verifyCode);
            mobiCodeRecord.setProperty("generateTime", date);

            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.put(mobiCodeRecord);
		return true;
}


private String generateVerifyCode() {
	// TODO Auto-generated method stub
	return "1234";
}


}
