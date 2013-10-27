package com.gm.server;




import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

import com.gm.server.model.User;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;



public class DeviceServletTest extends ModelTest{
  private User userInDB;
  @Before
  public void setUpData() throws Exception {
     userInDB = new User("12345","password","secret");
    dao.save(userInDB);
  }


  @Test
  public void testDoPost() throws IOException {
    HttpServletRequest req = getMockRequestWithUser(userInDB);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    
    String  deviceID= new String("My Device ID");
    when(req.getParameter("device_id")).thenReturn(deviceID);
   
   new DeviceServlet().execute(req, resp,false);
    
    User user = dao.get(userInDB.getEntityKey(), User.class);
    
    assertEquals(deviceID,user.getDeviceID());
    
  }

}
