package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.QuestPb;
import com.gm.server.model.Quest;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class AssignQuestServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }

  // Input Param: "key" user's index
  // "quest" Base64 encoded QuestPb object
  // "user_id" assignment list
  // Output: push notification

  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {

    QuestPb questMsg = getQuestPb(req);
    Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
    long receiverIds[] = ParamKey.user_id.getLongs(req, -1);

    // save quest and post record to DB
    Quest quest = new Quest(questMsg);
    dao.create(quest, ownerKey);
    info("new quest posted:" + quest.getParent().getId()
        + quest.getEntityKey().getKind() + quest.getId() + "by "
        + ownerKey.getId());

    quest.addPost(ownerKey.getId(), receiverIds); // add at the end

    // add applicants and activities
    for (long id : receiverIds) {
      assignQuest(quest, id, false);

    }

    quest.setStatus(QuestPb.Status.PUBLISHED);
    dao.save(quest);

    push(receiverIds, "activity", "assign");

    // push to receivers

  }
}
