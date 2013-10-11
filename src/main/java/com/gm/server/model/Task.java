package com.gm.server.model;

import java.util.Date;

@Entity
public class Task extends Persistable<Task> {

  
  @Property
  private long owner_id  = -1;

  @Property
  private Date start_time = new Date();

  @Property
  private Date end_time = null;
  
  @Property
  private String secret = "";
  
  
  @Override
  public Task touch() {
    // TODO Auto-generated method stub
    return null;
  }

}

