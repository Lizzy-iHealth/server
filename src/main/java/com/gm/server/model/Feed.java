package com.gm.server.model;

import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Server.FeedPb;
import com.gm.common.model.Server.Feeds;
import com.gm.common.model.Server.Feeds.Builder;


@Entity
public class Feed extends Persistable<Feed> {
  
  @Property
  private Feeds.Builder feeds;
  
  
  
  public Feed(){
    feeds = Feeds.newBuilder();
  }




  public Feed(Builder feeds2) {
    // TODO Auto-generated constructor stub
    feeds = feeds2;
  }




  public Feeds.Builder getFeeds() {
    return feeds;
  }



  public void setFeeds(Feeds.Builder feeds) {
    this.feeds = feeds;
  }



  @Override
  public Feed touch() {
    // TODO Auto-generated method stub
    return null;
  }




  public int findQuest(QuestPb.Builder questMsg) {
    for (int i = 0; i < feeds.getFeedCount(); ++i) {
      QuestPb item = feeds.getFeed(i).getQuest();
      if (questMsg.getId()== item.getId() && questMsg.getOwnerId()==item.getOwnerId()) {
        return i;
      }
    }
    return -1;
  }




  public void updateQuest(int i, QuestPb.Builder questMsg) {
    //copy existed referer lists and post records
    questMsg.addAllRefererId(feeds.getFeed(i).getQuest().getRefererIdList());
    questMsg.getPostRecordsBuilder().addAllPost(feeds.getFeed(i).getQuest().getPostRecords().getPostList());
    feeds.getFeedBuilder(i).setQuest(questMsg);
  }




  public void addQuest(int i,QuestPb.Builder questMsg) {
    FeedPb.Builder newFeed = FeedPb.newBuilder().setQuest(questMsg);
    feeds.addFeed(i,newFeed);    
  }
  

}
