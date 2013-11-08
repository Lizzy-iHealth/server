package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.net.ErrorCode;
import com.gm.server.model.Quest;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class DeleteActivityServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }

  // Input Param: "key" user's index
  // "id" quest id to be deleted
  //"owner_id" quest owner id

  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {

    // retrieve quest

    Key userKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
    Key questKey = super.getQuestKeyFromReq(req);
  
    User user = dao.get(userKey, User.class);
    user.deleteActivity(KeyFactory.keyToString(questKey));
    dao.save(user);

  }



}
