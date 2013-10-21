package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.UserPb;
import com.gm.common.net.ErrorCode;

public class GetPhoneDetailsServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
  
  //Input Param: "key"        user's key
  //             "phone"    user phone whose details are returned
  //Output Param: UserPb :    user' details
  //Zhiyu's commen: not provide this api, for users' privacy. With this API, strangers can get users' detail info by phone number.
  //TODO: let the phone's owner approve the information disclosure.

    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      String key = ParamKey.key.getValue(req);
      
      String qPhone = checkNotNull(ParamKey.phone.getValue(req),ErrorCode.auth_invalid_phone);
      UserPb.Builder qUserMsg = getPhoneDetails(key, qPhone);
      
      //System.out.println("============");
      //System.out.println(qUserMsg.build().toString());
      resp.getOutputStream().write(qUserMsg.build().toByteArray());

    }      
    
}
