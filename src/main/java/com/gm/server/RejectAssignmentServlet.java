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

public class RejectAssignmentServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }

  // Input: key : requester's key
  // id : quest id;
  // owner_id : quest owner id;
  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {

    // get quest key

    Key questKey = getQuestKeyFromReq(req);
    Key applierKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));

    // get quest from datastore and add an application
    int status = rejectAssignment(questKey, applierKey);
    resp.getOutputStream().write(Integer.toString(status).getBytes());
  }

}
