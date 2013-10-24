package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Rpc.Quests;
import com.gm.common.model.Server.FeedPb;
import com.gm.server.model.Feed;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class GetFeedsServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    // TODO Auto-generated method stub
    Key userKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
    Quests.Builder questsMsg = Quests.newBuilder();
    Feed feed = dao.querySingle(Feed.class, userKey);
    if(feed!=null){
    for(FeedPb f : feed.getFeeds().getFeedList()){
      QuestPb questMsg = f.getQuest();
      questsMsg.addQuest(questMsg);
    }
    }
    resp.getOutputStream().write(questsMsg.build().toByteArray());
  }
}
