package com.gm.server.model;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

import org.junit.Test;

import com.gm.common.model.Rpc.QuestPb;
import com.gm.server.ModelTest;


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
}
