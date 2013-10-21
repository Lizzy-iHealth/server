package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InviteFriendsServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
execute(req, resp);
  }
  
  //Input Param: "key"        user's key
  //             "phone"      phone list to be invited or added
  //Output Param: N/A

    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {

        String key = ParamKey.key.getValue(req);
        String[] friendPhones = ParamKey.phone.getValues(req);
        
        String[] results = inviteFriends(key, friendPhones);
        
        writeResponse(resp,results);
    }
}
