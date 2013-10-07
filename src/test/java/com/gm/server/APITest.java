package com.gm.server;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gm.common.crypto.Hmac;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.org.apache.http.HttpRequest;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class APITest extends ModelTest {

  


@Test
public void testDeviceServlet() throws IOException {
  User userInDB = new User("12345","password","secret");
  dao.save(userInDB);

 HttpServletRequest req = getMockRequestWithUser(userInDB);
 HttpServletResponse resp = mock(HttpServletResponse.class);
 
 String  deviceID= new String("My Device ID");
 when(req.getParameter("deviceID")).thenReturn(deviceID);
 DeviceServlet ds = new DeviceServlet();
 ds.doPost(req, resp);
 
 User user = dao.get(userInDB.getEntityKey(), User.class);
 
 assertEquals(deviceID,user.getDeviceID());
 
}

 /*
  @Test
  public void testPing() {
    String key = UUID.randomUUID().toString();
    String secret = UUID.randomUUID().toString();
    String phone = "1234567";
    
    User user = new User(phone,"password",secret,key);
    dao.create(user);
    
    // |------------ body -------------|
    // ........&key=....&hmac=..........
    // |--- message ---||-- hmacPart --|
    String message = "phone=1234567, friendids=[1,2,3] test message&key=" + key;
    String hmac = Hmac.generate(message, secret);
    String body = message + "&hmac=" + hmac;
    
    //TODO: find a way to mock ServletInputStream
    
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    
    
  }
  */

}
