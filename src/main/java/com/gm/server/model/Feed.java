package com.gm.server.model;

import com.gm.common.model.Rpc.Applicant.Status;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Server.FeedPb;
import com.gm.common.model.Server.Feeds;
import com.gm.common.model.Server.Feeds.Builder;
import com.google.appengine.api.datastore.Key;


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

  
  public int findQuest(long questId,long ownerId) {
    for (int i = 0; i < feeds.getFeedCount(); ++i) {
      QuestPb item = feeds.getFeed(i).getQuest();
      if (questId== item.getId() && ownerId==item.getOwnerId()) {
        return i;
      }
    }
    return -1;
  }

  public void updateQuest(int i, QuestPb.Builder questMsg) {

    QuestPb existedQuest = feeds.getFeed(i).getQuest();
    if(questMsg.getLog().getUpdatedAt()>existedQuest.getLog().getUpdatedAt()){
      mergeQuest(questMsg,existedQuest);
      feeds.getFeedBuilder(i).setQuest(questMsg);
    }else{
      QuestPb.Builder newQuest = QuestPb.newBuilder(existedQuest);
      mergeQuest(newQuest,questMsg.build());
      feeds.getFeedBuilder(i).setQuest(newQuest);
    }
  } 
  
  private void mergeQuest(QuestPb.Builder newquest,QuestPb oldquest){
    //copy existed referer lists and post records
    if(oldquest.getRefererIdCount()>0){
      newquest.addAllRefererId(oldquest.getRefererIdList());
    }
    if(oldquest.getPostRecords().getPostCount()>0){
      newquest.getPostRecordsBuilder().addAllPost(oldquest.getPostRecords().getPostList());  
    }
  }

  



  public void addQuest(int i,QuestPb.Builder questMsg) {
    FeedPb.Builder newFeed = FeedPb.newBuilder().setQuest(questMsg);
    feeds.addFeed(i,newFeed);    
  }




  public void deleteQuest(int i) {
    feeds.removeFeed(i);
    
  }



  //change the feeds' owner's status of the quest in "ith" feed 


}
