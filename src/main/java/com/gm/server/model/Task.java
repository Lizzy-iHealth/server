package com.gm.server.model;

import java.util.Date;

@Entity
public class Task extends Persistable<Task> {

  
  @Property
  private long owner_id;

  @Property
  private Date start_time = new Date();

  @Property
  private Date end_time = null;
  
  @Property
  private String title;
  
  Task(String title){
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

  Task(){}
  
  @Override
  public Task touch() {
    // TODO Auto-generated method stub
    return null;
  }

}

