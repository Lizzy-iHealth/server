package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Applicant;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.Quest;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class AcceptQuestServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }

  // Input Param: "key" user's index
  // "id" quest id
  // "owner_id" quest owner id
  // Output: push notification

  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {

    // get quest key

    Key questKey = getQuestKeyFromReq(req);
  
    Key applierKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));

    int status = acceptQuest(questKey, applierKey);
    
    resp.getWriter().write(Integer.toString(status));
	// push message to quest owner.


  }

}
