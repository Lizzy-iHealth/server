package com.gm.server;

import static com.gm.server.Filters.eq;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.*;


public class LoginServlet extends APIServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
	  this.requiresHmac = false;
	  execute(req, resp);

    }
	
//Input Param: "password"        user's password
  //             "phone"           user's phone to regeister
  //           
  //Output: String key             index of user
  //        String  ,
  //        String secret           for hmac generation


    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      String phone = stringNotEmpty(ParamKey.phone.getValue(req),
          ErrorCode.auth_invalid_phone);
      String password = stringNotEmpty(ParamKey.password.getValue(req),
          ErrorCode.auth_invalid_password);

      String[] results = login(phone, password);
      writeResponse(resp, results);
    }


	



}
