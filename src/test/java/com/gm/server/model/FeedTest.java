package com.gm.server.model;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

import org.junit.Test;

import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Server.FeedPb;
import com.gm.common.model.Server.Feeds;
import com.gm.server.ModelTest;

public class FeedTest extends ModelTest {

  @Test
  public void testFeed(){
    User user = new User();
    dao.save(user);
    Feed feednull = new Feed();
    dao.save(feednull,user.getEntityKey());
    Feed feedindb = dao.get(feednull.getEntityKey(), Feed.class);
    assertEquals(feednull.getFeeds().getFeedCount(), feedindb.getFeeds().getFeedCount());
  }
  
  @Test
  public void testFeedQuestMSG(){
    User user = new User();
    dao.save(user);
    
    
    QuestPb.Builder qmsg = new Quest().getMSG();
    FeedPb.Builder feednull = FeedPb.newBuilder().setQuest(qmsg);
    Feeds.Builder feeds = Feeds.newBuilder().addFeed(feednull);
    Feed feed = new Feed(feeds);
    dao.save(feed,user.getEntityKey());
    Feed feedindb = dao.query(Feed.class).setAncestor(user.getEntityKey()).prepare().asSingle();
    assertEquals(feed.getFeeds().getFeedCount(), feedindb.getFeeds().getFeedCount());;
  }

}
