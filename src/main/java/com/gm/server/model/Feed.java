package com.gm.server.model;

import java.util.Date;

import com.gm.server.model.Model.QuestMSG;
import com.gm.server.model.Model.QuestMSG.Builder;

@Entity
public class Feed extends Persistable<Feed> {

  @Property
  private QuestMSG.Builder quest = QuestMSG.newBuilder();
  
  @Property
  private Date createTime;
  
  @Property
  private Date deleteTime;
  
  @Property
  private int  priority=0;
  
  @Property
  private long sourceId = -1;
  
  
  Feed(){}
  
  
  public Feed(long sourceId, Builder questMsg, long createTime2,
      long deleteTime2) {
    this.sourceId = sourceId;
    quest = questMsg;
    createTime = new Date(createTime2);
    deleteTime = new Date(deleteTime2);
    
  }


  @Override
  public Feed touch() {
    // TODO Auto-generated method stub
    return null;
  }

}
