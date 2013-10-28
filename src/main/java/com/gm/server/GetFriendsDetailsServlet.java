package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.UsersPb;

public class GetFriendsDetailsServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    String key = ParamKey.key.getValue(req);
    long qUserIds[] = ParamKey.user_id.getLongs(req, -1);
    UsersPb.Builder users = getFriendsDetails(key, qUserIds);

    // System.out.println(users.build().toString());
    resp.getOutputStream().write(users.build().toByteArray());

  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
}
