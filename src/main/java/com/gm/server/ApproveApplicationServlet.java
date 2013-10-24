package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.server.model.Quest;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class ApproveApplicationServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
  
  //Input Param: "key"        user's index
  //             "id"      quest id
  //              "user_id"   assignment list
  //Output: push notification
  
   
      public void handle(HttpServletRequest req, HttpServletResponse resp)
          throws ApiException, IOException {
        
       
        Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
        long questId = ParamKey.id.getLong(req, -1);
        long userIds[] = ParamKey.user_id.getLongs(req, -1);
        Key questKey = KeyFactory.createKey(ownerKey, "Quest", questId);
        // retrive quest from data store  
        Quest quest = dao.get(questKey, Quest.class);
        for(long userId:userIds){
          assignQuest(quest,userId,false);
        }
        dao.save(quest);
        
        
        push(userIds,"activity","assign");
        
        // push to receivers
        
      }
        
   
}
