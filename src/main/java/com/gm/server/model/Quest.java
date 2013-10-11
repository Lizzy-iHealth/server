package com.gm.server.model;

import java.util.Date;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.PostalAddress;

@Entity
public class Quest extends Persistable<Quest> {

  
  @Property
  private long owner_id  = -1;

  @Property
  private Date start_time = new Date();

  @Property
  private Date end_time = null;
  
  @Property
  private String title;

  @Property
  private PostalAddress address;

  @Property
  private GeoPt geo_point;
  
  @Property
  private long prize;

  Quest(String title){
    this.title = title;
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

