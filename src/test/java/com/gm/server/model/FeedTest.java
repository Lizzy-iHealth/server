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
    Feed feedindb = dao.querySingle(Feed.class,user.getEntityKey());
    assertEquals(feed.getFeeds().getFeedCount(), feedindb.getFeeds().getFeedCount());;
  }
  
  @Test
  public void testFindQuest(){
    User user = new User();
    dao.save(user);
    
    Quest quest = new Quest();
    dao.save(quest,user.getEntityKey());
    
    QuestPb.Builder qmsg = quest.getMSG();
    FeedPb.Builder feednull = FeedPb.newBuilder().setQuest(qmsg);
    Feeds.Builder feeds = Feeds.newBuilder().addFeed(feednull);
    Feed feed = new Feed(feeds);
    assertEquals(0,feed.findQuest(qmsg));
  }
  
  @Test
  public void testAddQuestAt(){
    User user = new User();
    dao.save(user);
    
    Quest quests[] = new Quest[10];
    Feed feed = new Feed();
    for(int i=0;i<quests.length;i++){
      quests[i]=new Quest();
      dao.save(quests[i],user.getEntityKey());
      feed.addQuest(i, quests[i].getMSG());
    }
    
    for(int i=0;i<quests.length;i++){
      assertEquals(i,feed.findQuest(quests[i].getMSG()));
    } 
  }
  @Test
  public void testUpdateQuestAt(){
    User user = new User();
    dao.save(user);
    
    Quest quests[] = new Quest[10];
    Feed feed = new Feed();
    for(int i=0;i<quests.length;i++){
      quests[i]=new Quest();
     
      dao.save(quests[i],user.getEntityKey());
      feed.addQuest(i, quests[i].getMSG().addRefererId(user.getId()));
    }
    String newTitle = "new title";
    quests[5].setTitle(newTitle);
    feed.updateQuest(5, quests[5].getMSG().addRefererId(user.getId()+1));
    
    assertEquals(newTitle,feed.getFeeds().getFeed(5).getQuest().getTitle());
    assertEquals(2,feed.getFeeds().getFeed(5).getQuest().getRefererIdCount());
    assertEquals(user.getId(),feed.getFeeds().getFeed(5).getQuest().getRefererId(1));
    assertEquals(user.getId()+1,feed.getFeeds().getFeed(5).getQuest().getRefererId(0));
  }
}
