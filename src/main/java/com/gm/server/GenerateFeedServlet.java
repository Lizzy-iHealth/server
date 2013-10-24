package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.crypto.Base64;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.server.model.Feed;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class GenerateFeedServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    this.requiresHmac = false;
    execute(req, resp);
  }

  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    //get receivers id list
    long receiverIds[] = ParamKey.user_id.getLongs(req, -1);
    //get quest
    String questString = ParamKey.quest.getValue(req);
    QuestPb.Builder questMsg = QuestPb.parseFrom(Base64.decode(questString,Base64.DEFAULT)).toBuilder();
    String message = req.getParameter("message");
    for(long id:receiverIds){

      Key receiverKey =  KeyFactory.createKey("User", id);
      Feed feed = dao.querySingle(Feed.class,receiverKey);
      if(feed==null){
        feed = new Feed();
        dao.save(feed,receiverKey);
      }
      int i = feed.findQuest(questMsg.getId(),questMsg.getOwnerId());
      if(i!=-1){
        feed.updateQuest(i,questMsg);
      }else{
        feed.addQuest(0,questMsg);
      }
      System.out.println(feed.toString());
      dao.save(feed,receiverKey);
    }
    push(receiverIds,"Feed",message);
  }
  
}
