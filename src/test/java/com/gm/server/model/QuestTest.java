package com.gm.server.model;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

import org.junit.Test;

import com.gm.common.model.Rpc.Currency;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.server.ModelTest;

import java.lang.Thread;
import java.util.HashSet;

import com.gm.common.model.Rpc.Applicant;
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
  
  @Test
  public void testUpdateQuest(){
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
  
  @Test
  public void testAddApplicant(){
    User user = new User();
    dao.save(user);
    
    Quest quest = new Quest("a quest");
    dao.save(quest,user.getEntityKey());
    
    //mock n posts
    long receivers[] = {user.getId(), user.getId()+1};
    int n = 10;
    for(int i = 0; i<n; i++){
      quest.addPost(user.getId(), receivers);
    }
    dao.save(quest);
    
    //mock m applicants:
    int m = 20;
    long applicantIds[] = new long[m];
    User applicantUsers[] = new User[m];
    
    for(int i =0; i<m;i++){
      applicantUsers[i]=new User();
      dao.save(applicantUsers[i]);
      applicantIds[i]=applicantUsers[i].getId(); 
      Applicant app = Applicant.newBuilder().setUserId(applicantIds[i]).build();
      quest.addApplicant(app);
    }
    dao.save(quest);
    

    
    Quest retriveQuest = dao.get(quest.getEntityKey(),Quest.class);
    assertEquals(n,retriveQuest.getPosts().getPostCount());
    assertEquals(m,retriveQuest.getApplicants().getApplicantCount());
    
    for(int i =1; i<m; i++){
      assertEquals(applicantIds[i],retriveQuest.getApplicants().getApplicant(i).getUserId());
    }
    
    //everyone apply again with a new bid:
    for(int i =0; i<m;i++){
      
      Applicant app = Applicant.newBuilder().setUserId(applicantIds[i]).setBid(Currency.newBuilder().setGold(i)).build();
      quest.addApplicant(app);
    }
    dao.save(quest);
    
    retriveQuest = dao.get(quest.getEntityKey(),Quest.class);
    assertEquals(n,retriveQuest.getPosts().getPostCount());
    assertEquals(m,retriveQuest.getApplicants().getApplicantCount());
    
    for(int i =0; i<m; i++){
      assertEquals(applicantIds[i],retriveQuest.getApplicants().getApplicant(i).getUserId());
      assertEquals(i,retriveQuest.getApplicants().getApplicant(i).getBid().getGold());
      
    }
    //System.out.println(quest.getMSG(quest.getParent().getId()).build().toString());
  }
  
  @Test
  public void testGetApplicantAndReceiversId(){
    User user = new User();
    dao.save(user);
    
    Quest quest = new Quest("a quest");
    dao.save(quest,user.getEntityKey());
    
    //mock n posts
    long receivers[] = {user.getId(), user.getId()+1};
    int n = 10;
    for(int i = 0; i<n; i++){
      quest.addPost(user.getId(), receivers);
    }
    dao.save(quest);
    
    //mock m applicants:
    int m = 20;
    long applicantIds[] = new long[m];
    User applicantUsers[] = new User[m];
    
    for(int i =0; i<m;i++){
      applicantUsers[i]=new User();
      dao.save(applicantUsers[i]);
      applicantIds[i]=applicantUsers[i].getId(); 
      Applicant app = Applicant.newBuilder().setUserId(applicantIds[i]).build();
      quest.addApplicant(app);
    }
    dao.save(quest);
    

    
    Quest retriveQuest = dao.get(quest.getEntityKey(),Quest.class);
    HashSet<Long> aIds = retriveQuest.getAllApplicantsIdsSet();
    long rIds[] = retriveQuest.getAllReceiversIds();
    assertEquals(receivers.length, rIds.length);
    assertEquals(m,aIds.size());
    
    // can not garentee the order of applicants.

    //System.out.println(quest.getMSG(quest.getParent().getId()).build().toString());
  }
}
