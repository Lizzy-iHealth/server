package com.gm.server;

import static com.gm.server.Filters.eq;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;



public class Register extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(Register.class.getName());
	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {

        String mobileNumber = req.getParameter("mobileNumber");
        
        boolean succ = storeAndSendVerifyCode(mobileNumber);
        
        	if(succ){
        		resp.setStatus(resp.SC_OK);
        	}else{
        		resp.setStatus(resp.SC_SERVICE_UNAVAILABLE);
        	}
    }


private boolean storeAndSendVerifyCode(String mobileNumber) {
		// TODO Auto-generated method stub
	  String verifyCode = generateVerifyCode();
	  
	  //in case of re-send:
	  //TODO: expiring over time
		Query query = new Query("mobiCodeRecord").setFilter(eq("mobileNumber", mobileNumber));
	    Entity exMobiCodeRecord = datastore.prepare(query).asSingleEntity();
	    if (exMobiCodeRecord != null){
	    		verifyCode = (String) exMobiCodeRecord.getProperty("verifyCode");
	    }else{
            Date date = new Date();
          
            Entity mobiCodeRecord = new Entity("mobiCodeRecord");
            mobiCodeRecord.setProperty("mobileNumber", mobileNumber);
            mobiCodeRecord.setProperty("verifyCode", verifyCode);
            mobiCodeRecord.setProperty("generateTime", date);
          
            datastore.put(mobiCodeRecord);
            log.info("new user created");
	    }
	    sendMobileVerifyCode(mobileNumber,verifyCode);
		return true;
}


private void sendMobileVerifyCode(String mobileNumber, String verifyCode) {
	// TODO Auto-generated method stub

}


private String generateVerifyCode() {
	// TODO Auto-generated method stub
	return "1234";
	//return Integer.toString(Calendar.MILLISECOND);
}


}

