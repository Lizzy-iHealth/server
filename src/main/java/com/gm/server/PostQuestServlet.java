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

public class PostQuestServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
  
  
 //Input Param: "key"        user's index
 //             "quest"      Base64 encoded QuestPb object
 //              "user_id"   receiver list
 //Output:       quest id
  //             push notification
 


     @Override
     public void handle(HttpServletRequest req, HttpServletResponse resp)
         throws ApiException, IOException {
       
       QuestPb questMsg = getQuestPb(req);
       Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
       long receiverIds[]= ParamKey.user_id.getLongs(req,-1);
       checkNotNull(receiverIds,ErrorCode.quest_receiver_not_found);
       // save quest and post record to DB  
       Quest quest = new Quest(questMsg);
       dao.save(quest, ownerKey);
       postExistedQuest(ownerKey, receiverIds, quest);
       
       resp.getWriter().write(Long.toString(quest.getId()));
     
       
     }

}
