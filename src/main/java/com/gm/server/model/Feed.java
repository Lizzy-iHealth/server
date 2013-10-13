package com.gm.server.model;

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

}
