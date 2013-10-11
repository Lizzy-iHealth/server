package com.gm.server.model;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

import java.util.List;

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
  public void testSavePersitableKey(){
    User user = new User("phone","password","secret");
    dao.save(user);
    String title = "Important task";
    Task task = new Task(title);
    dao.save(task,user.getEntityKey());
    
    Task taskInDB = dao.get(task.getEntityKey(), Task.class);
    assertEquals(title, taskInDB.getTitle());
    assertEquals(user.getEntityKey(), taskInDB.getEntityKey().getParent());
    
  }
  
  @Test
  public void testQuerySingleWithAncestor(){
    User user = new User("phone","password","secret");
    dao.save(user);
    String title = "Important task";
    Task task = new Task(title);
    dao.save(task,user.getEntityKey());
    
    Task taskInDB = dao.get(task.getEntityKey(), Task.class);
    Task taskByQuery = dao.querySingle("title", title, Task.class, user.getEntityKey());
    assertEquals(taskInDB.getEntityKey(), taskByQuery.getEntityKey());
    assertEquals(user.getEntityKey(), taskByQuery.getEntityKey().getParent());
    
  }
  
  @Test
  public void testSetAncestor(){
    User user = new User("phone","password","secret");
    dao.save(user);
    String[] titles = {"Important task","nomal task"};
    Task task[] = {new Task(titles[0]), new Task(titles[1]) };
    
    dao.save(task[0],user.getEntityKey());
    dao.save(task[1],user.getEntityKey());
    
    List<Task> taskInDB = dao.query(Task.class).setAncestor(user.getEntityKey()).prepare().asList();
    assertEquals(2,taskInDB.size());
    assertEquals(user.getEntityKey(), taskInDB.get(0).getEntityKey().getParent());
    assertEquals(user.getEntityKey(), taskInDB.get(1).getEntityKey().getParent());
    assertEquals(titles[0], taskInDB.get(0).getTitle());
    assertEquals(titles[1], taskInDB.get(1).getTitle());
  }

}
