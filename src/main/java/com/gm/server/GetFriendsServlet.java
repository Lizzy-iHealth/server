package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Friends;
import com.gm.server.model.User;

public class GetFriendsServlet extends APIServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
  execute(req, resp);
  }
  // Hidden now.
  //Input Param: "key"        user's key
  //             
  //Output Param: byte[] Friends :    all friends id and type

    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {

      String key = ParamKey.key.getValue(req);
      User user = dao.get(key, User.class);
      Friends friends = user.getFriends().build();

      resp.setContentType("application/x-protobuf");
      resp.getOutputStream().write(friends.toByteArray());
    }
    
}
