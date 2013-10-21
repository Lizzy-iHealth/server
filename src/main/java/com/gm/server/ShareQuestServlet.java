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

public class ShareQuestServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }

  //Input Param: "key"        sharer's index
  //             "owner_id"    quest's onwer's id
  //             "id"          quest id
  //              "user_id"   receiver list
  //Output: push notification

       @Override
       public void handle(HttpServletRequest req, HttpServletResponse resp)
           throws ApiException, IOException {
         
         // retrieve quest key
         
         Key questKey = getQuestKeyFromReq(req);
         Key sharerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
         long receiverIds[]= ParamKey.user_id.getLongs(req,-1);

         // get quest from datastore and add a post record the quest entity 
         Quest quest = checkNotNull(dao.get(questKey, Quest.class),ErrorCode.quest_quest_not_found);
         check(quest.isAllow_sharing(),ErrorCode.quest_not_allow_sharing);
         check(!quest.isDeal(),ErrorCode.quest_is_deal);     
         quest.addPost(sharerKey.getId(),receiverIds); //add at the end
         dao.save(quest);
                 
         //TODO: redirect to backend
         // prepare feed
         QuestPb.Builder questFeed = quest.getMSG();
         questFeed.addRefererId(sharerKey.getId());
         generateFeed(receiverIds,questFeed,"share");

       }
}
