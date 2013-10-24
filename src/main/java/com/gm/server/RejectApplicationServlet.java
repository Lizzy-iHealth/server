package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class RejectApplicationServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
//Input: key: quest owner key
//id : quest id
//user_id: whose application is rejected.
//Output: notify the applicant that is reject.


@Override
public void handle(HttpServletRequest req, HttpServletResponse resp)
throws ApiException, IOException {

Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));

// get quest
long questId = ParamKey.id.getLong(req, -1);
Key questKey = KeyFactory.createKey(ownerKey,"Quest",questId);
long toReject = ParamKey.user_id.getLong(req, -1);

rejectApplication(questKey, toReject);

}

}
