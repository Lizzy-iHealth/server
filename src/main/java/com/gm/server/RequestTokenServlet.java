package com.gm.server;

import static com.gm.server.Filters.eq;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.net.ErrorCode;
import com.gm.server.model.Token;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;



public class RequestTokenServlet extends APIServlet {

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
    
    //Input Param:   "phone"           user's phone to regeister
    //           
    //Output:  N/A

      @Override
      public void handle(HttpServletRequest req, HttpServletResponse resp)
          throws ApiException, IOException {
        String phone = stringNotEmpty(ParamKey.phone.getValue(req),
            ErrorCode.auth_invalid_phone);
        String msg = "1234"; // TODO: generate random token and send by sms
        Token token = dao.querySingle("phone", phone, Token.class);
        if (token == null)
          token = new Token(phone, msg);
        token.token = msg;
        dao.save(token);
      }     





}

