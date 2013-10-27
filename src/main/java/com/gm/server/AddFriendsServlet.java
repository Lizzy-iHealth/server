package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AddFriendsServlet extends APIServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  //Input Param: "key"        user's key
  //             "user_id"    user id list to be added
  //Output Param: N/A
  
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {

      long[] friendIDs = ParamKey.user_id.getLongs(req,-1);
      String key = ParamKey.key.getValue(req);
      int[] results = addFriends(key,friendIDs);
      writeResponse(resp, results);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
}
