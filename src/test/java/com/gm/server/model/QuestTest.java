package com.gm.server.model;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

import org.junit.Test;

import com.gm.common.model.Rpc.QuestPb;
import com.gm.server.ModelTest;

import java.lang.Thread;

public class QuestTest extends ModelTest {
  
  @Test
  public void testQuestMSG(){
    User user = new User();
    dao.save(user);
    
    Quest questNotInDB = new Quest("a quest");
    QuestPb qmsg = questNotInDB.getMSG().build();
    Quest questReGen = new Quest(qmsg);
    
    assertEquals(questNotInDB.getTitle(),questReGen.getTitle());
    
    dao.save(questReGen,user.getEntityKey());
    qmsg = questReGen.getMSG().build();
    assertEquals(user.getId(),qmsg.getOwnerId());
    assertEquals(questReGen.getId(),qmsg.getId());
  }
  
  @Test
  public void testQuestMSGByID_AddPost(){
    User user = new User();
    dao.save(user);
    
    Quest questNotInDB = new Quest("a quest");
    QuestPb qmsg = questNotInDB.getMSG().build();
    Quest questReGen = new Quest(qmsg);
    dao.save(questReGen,user.getEntityKey());
    
    QuestPb retriveQuest = dao.querySingle(Quest.class, user.getEntityKey()).getMSG(user.getId()).build();
    assertEquals(0,retriveQuest.getApplicants().getApplicantCount());
    assertEquals(0,retriveQuest.getPostRecords().getPostCount());
    
    //mock n posts
    long receivers[] = {user.getId(), user.getId()+1};
    int n = 10;
    for(int i = 0; i<n; i++){
      questReGen.addPost(user.getId(), receivers);
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    dao.save(questReGen);
    
    retriveQuest = dao.querySingle(Quest.class, user.getEntityKey()).getMSG(user.getId()).build();
    assertEquals(n,retriveQuest.getPostRecords().getPostCount());
    
    for(int i =1; i<n; i++){
    assertTrue(retriveQuest.getPostRecords().getPost(i-1).getTimestamp()
        <retriveQuest.getPostRecords().getPost(i).getTimestamp());
    }
  }
}
