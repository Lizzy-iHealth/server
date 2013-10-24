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

public class UpdateQuestServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
  
  //Input Param: "key"        user's index
  //             "quest"      Base64 encoded QuestPb object with questId filled in.
  //                          those fields will be updated if provided:lifespan,title,address,geopoint,reward,description,allowsharing,url
  //              
  //Output: N/A
  //only quest owner can update a quest.
  //option 1:
  //updates will be available in data store. Others will get a notification, they will see the update when they request for it.
  //
  //
  //option 2:
  //all the receivers' related feed will be updated.


      @Override
      public void handle(HttpServletRequest req, HttpServletResponse resp)
          throws ApiException, IOException {
        
        // retrieve quest
        QuestPb questMsg = getQuestPb(req);
        Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
        Key questKey = KeyFactory.createKey(ownerKey, "Quest", questMsg.getId());
        updateQuestByMsg(questMsg, ownerKey, questKey);

      }

 
        
}
