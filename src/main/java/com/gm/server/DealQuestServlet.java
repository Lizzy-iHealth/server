package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.Quest;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class DealQuestServlet extends APIServlet {
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
  //
  // Output: N/A
  // only quest owner can stop posting a quest when reach a deal
  // option 1:
  // deal will only be pushed to applicants. Others will see the deal status
  // when they request for it.
  //
  // option 2:
  // all the receivers' related feed will be deleted.

  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {

    // retrieve quest

    Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
    long questId = ParamKey.id.getLong(req, -1);
    Key questKey = KeyFactory.createKey(ownerKey, "Quest", questId);
    // save quest and post record to DB
    Quest quest = checkNotNull(dao.get(questKey, Quest.class),
        ErrorCode.quest_quest_not_found);
    quest.setStatus(QuestPb.Status.DEAL);

    dao.save(quest);
    // option 2 implementation:
    // prepare feed
    long[] receiverIds = quest.getNoActionReceiversIds();
    deleteFeed(receiverIds, questId, ownerKey.getId());

  }

}
