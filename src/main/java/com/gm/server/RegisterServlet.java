package com.gm.server;

import java.io.IOException;
import java.util.Date;

import static com.gm.server.Filters.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.net.ErrorCode;
import com.gm.server.model.DAO;
import com.gm.server.model.Token;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.gm.server.model.User;
public class RegisterServlet extends APIServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1142920677422141888L;
	
	@Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
	  requiresHmac = false;
	  execute(req, resp);

    }
	 //Input Param: "password"        user's password
  //             "phone"           user's phone to regeister
  //              "token"          verify ownership of phone
  //Output: String key             index of user
  //        String  ,
  //        String secret           for hmac generation

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      String phone = stringNotEmpty(ParamKey.phone.getValue(req),
          ErrorCode.auth_invalid_phone);
      String password = stringNotEmpty(ParamKey.password.getValue(req),
          ErrorCode.auth_invalid_password);
      String token = stringNotEmpty(ParamKey.token.getValue(req),
          ErrorCode.auth_invalid_token);
      // TODO : find token by token key instead of phone
      Token tokenStore = checkNotNull(
          dao.querySingle("phone", phone, Token.class),
          ErrorCode.auth_token_not_sent);
      check(token.equalsIgnoreCase(tokenStore.token),
          ErrorCode.auth_incorrect_token);

      check(!User.existsByPhone(phone), ErrorCode.auth_phone_registered);

      User user = createUser(phone, password);
      
      String results[] = {user.getKey(), user.getSecret(),Long.toString(user.getId())};
      writeResponse(resp,results);
    }



}
