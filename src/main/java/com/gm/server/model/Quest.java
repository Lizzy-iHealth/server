package com.gm.server.model;

import java.util.Date;

import com.gm.server.model.Model.Applicants;
import com.gm.server.model.Model.Friendship;
import com.gm.server.model.Model.PostRecordsMSG;
import com.gm.server.model.Model.QuestMSG;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PostalAddress;

@Entity
public class Quest extends Persistable<Quest> {

  @Property
  private long id;
  @Property
  private long owner_id  = -1;
  
  @Property
  private Friendship.Builder members = Friendship.newBuilder();

  @Property
  private Applicants.Builder applicants = Applicants.newBuilder();
  
  @Property
  private Date start_time = new Date();

  @Property
  private Date end_time = new Date();
  
  @Property
  private String title="";

  @Property
  private PostalAddress address=new PostalAddress("");

  @Property
  private GeoPt geo_point=new GeoPt(0,0);
  
  @Property
  private long prize=0; // at owner's view: <0 give reward, >0 collect reward
  
  @Property
  private String description="";
  
  @Property
  private boolean allow_sharing = false;
  @Property
  private Link attach_link=new Link("");
  
  @Property
  private PostRecordsMSG.Builder posts=PostRecordsMSG.newBuilder();
  
  Quest(){}
  
  Quest(String title){
    this.title = title;
  }

  public Quest(QuestMSG q) {
    id = q.getId();
    owner_id = q.getOwnerId();
    start_time = new Date(q.getLifspan().getCreateTime());
    end_time = new Date(q.getLifspan().getDeleteTime());
    title = q.getTitle();
    address = new PostalAddress(q.getAddress());
    geo_point = new GeoPt(q.getGeoPoint().getLatitude(),q.getGeoPoint().getLongitude());
    prize = q.getReward().getGoldPrice();
    description = q.getDescription();
    allow_sharing = q.getAllowSharing();
    attach_link = new Link(q.getUrl());
  }

  public long getOwner_id() {
    return owner_id;
  }

  public void setOwner_id(long owner_id) {
    this.owner_id = owner_id;
  }

  public Date getStart_time() {
    return start_time;
  }

  public void setStart_time(Date start_time) {
    this.start_time = start_time;
  }

  public Date getEnd_time() {
    return end_time;
  }

  public void setEnd_time(Date end_time) {
    this.end_time = end_time;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public PostalAddress getAddress() {
    return address;
  }

  public void setAddress(PostalAddress address) {
    this.address = address;
  }

  public GeoPt getGeo_point() {
    return geo_point;
  }

  public void setGeo_point(GeoPt geo_point) {
    this.geo_point = geo_point;
  }

  @Override
  public Quest touch() {
    // TODO Auto-generated method stub
    return null;
  }

}

