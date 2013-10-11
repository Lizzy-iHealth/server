package com.gm.server.model;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gm.server.ModelTest;
import com.google.appengine.api.datastore.KeyFactory;

public class DAOTest extends ModelTest {

  @Before
  public void setUp() {
    super.setUp();
  }

  @After
  public void tearDown() {
    super.tearDown();
  }

  @Test
  public void testGetKeyClass() {
    User userInDB = new User("12345","password","secret");
    dao.save(userInDB);
   User user = dao.get(KeyFactory.stringToKey(userInDB.getKey()), User.class);
   
   assertEquals(user.getKey(),userInDB.getKey());
  }
  
  @Test
  public void testEntityGroup(){
    
  }

}
