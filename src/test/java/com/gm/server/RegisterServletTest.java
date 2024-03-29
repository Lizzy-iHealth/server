package com.gm.server;

import static com.gm.server.Filters.eq;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gm.server.model.PendingUser;
import com.gm.server.model.Token;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.images.ImagesServicePb.OutputSettingsOrBuilder;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.gm.common.model.Rpc.Friendship;
import com.gm.common.model.Rpc.UserPb;
import com.gm.common.net.ErrorCode;

public class RegisterServletTest extends ModelTest{
	  private RegisterServlet binder;
	    String phone = "1234567890";
	    String verifyCode = "1234";
	    String password = "hello";
	    String secret="mysecret";



	@Before
	public void setUpData() throws Exception {

      Token token = new Token(phone,verifyCode);
      dao.save(token);
        binder = new RegisterServlet();
	}
  @Test
  public void testInvitedRegistration() throws IOException {
    
    //setup a invitation:
    User invitor = new User("invitors phone","password","secret");
    
    dao.save(invitor);
 //   PendingUser pu = new PendingUser(phone,invitor.getUserID());
    User pu = new User(phone,"p","s");
    pu.setType(UserPb.Type.PENDING_VALUE);
    pu.addFriend(invitor.getId(), Friendship.WAIT_MY_CONFIRM);
   
    dao.save(pu);
    invitor.addFriend(pu.getId(), Friendship.INVITED);
    dao.save(invitor);
    
    
    HttpServletRequest request = mock(HttpServletRequest.class);
      HttpServletResponse response1 = mock(HttpServletResponse.class);
      PrintWriter writer = mock(PrintWriter.class);
      ServletOutputStream swriter = mock(ServletOutputStream.class);
      
      when(request.getParameter("phone")).thenReturn(phone);
      when(request.getParameter("token")).thenReturn(verifyCode);
      when(request.getParameter("password")).thenReturn(password);
      when(response1.getWriter()).thenReturn(writer);
      when(response1.getOutputStream()).thenReturn(swriter);
      Date priorToRequest = new Date();

      binder.doPost(request, response1);

      Date afterRequest = new Date();

      verify(response1).setStatus(HttpServletResponse.SC_OK);
      
      User userInDB = dao.get(invitor.getEntityKey(), User.class);
      
      assertEquals(1,userInDB.getFriends().getFriendCount());
      assertEquals(Friendship.INVITED,userInDB.getFriends().getFriend(0).getFriendship());
      
      long newUserId = userInDB.getFriends().getFriend(0).getId();
      User newUser = dao.get(KeyFactory.createKey("User", newUserId), User.class);
      String secret = newUser.getSecret();
      String key = newUser.getKey();
      String res[]={key,secret,Long.toString(newUserId)};
      verify(swriter).write(getResponse(res).getBytes());

      
      assertEquals(1,newUser.getFriends().getFriendCount());
      assertEquals(Friendship.WAIT_MY_CONFIRM,newUser.getFriends().getFriend(0).getFriendship());
      
      assertEquals(phone, newUser.getPhone());
      assertEquals(password, newUser.getPassword());

     
      
      assertEquals(null,dao.get(pu.getEntityKey(), PendingUser.class));
      
  }

	@Test
	public void testDoPost() throws IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
	    HttpServletResponse response1 = mock(HttpServletResponse.class);
	    PrintWriter writer = mock(PrintWriter.class);
	    ServletOutputStream swriter = mock (ServletOutputStream.class);

	    when(request.getParameter("phone")).thenReturn(phone);
	    when(request.getParameter("token")).thenReturn(verifyCode);
	    when(request.getParameter("password")).thenReturn(password);
	    when(response1.getWriter()).thenReturn(writer);
	    when(response1.getOutputStream()).thenReturn(swriter);
	    Date priorToRequest = new Date();

	    binder.doPost(request, response1);

	    Date afterRequest = new Date();

	    verify(response1).setStatus(HttpServletResponse.SC_OK);
	    
	   
	    Query query = new Query("User").setFilter(eq("phone", phone));
	    Entity e = DatastoreServiceFactory.getDatastoreService().prepare(query).asSingleEntity();
	    String secret = (String)e.getProperty("secret");
	    String key = KeyFactory.keyToString(e.getKey());
      String res[]={key,secret,Long.toString(e.getKey().getId())};
      verify(swriter).write(getResponse(res).getBytes());

      
	    assertEquals(phone, e.getProperty("phone"));
	    assertEquals(password, e.getProperty("password"));
	    
	    
	    Date createTime = (Date) e.getProperty("createTime");
	    Date lastLogin = (Date) e.getProperty("lastLoginTime");
	    
	    assertTrue("The date of last login [" + createTime + "] is after to the request completed",
		        lastLogin.after(createTime) || lastLogin.equals(createTime));
	    assertTrue("The date in the entity [" + createTime + "] is prior to the request being performed",
	        priorToRequest.before(createTime) || priorToRequest.equals(createTime));
	    assertTrue("The date in the entity [" + createTime + "] is after to the request completed",
	        afterRequest.after(createTime) || afterRequest.equals(createTime));
	    
	    // Bind Again, test exist(...)
	    HttpServletResponse response2 = mock(HttpServletResponse.class);
	    when(response2.getWriter()).thenReturn(writer);
	    ServletOutputStream streamwriter = mock(ServletOutputStream.class);
	    when(response2.getOutputStream()).thenReturn(streamwriter);
	     priorToRequest = new Date();

	    binder.doPost(request, response2);

	     afterRequest = new Date();

	    verify(response2).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    verify(streamwriter).write(Integer.toString(ErrorCode.auth_phone_registered).getBytes());
	   
	     query = new Query("User").setFilter(eq("phone", phone));
	     e = DatastoreServiceFactory.getDatastoreService().prepare(query).asSingleEntity();
	 
	    assertEquals(phone, e.getProperty("phone"));
	    assertEquals(password, e.getProperty("password"));
	    assertEquals(secret,e.getProperty("secret"));

	    createTime = (Date) e.getProperty("createTime");
	    lastLogin = (Date) e.getProperty("lastLoginTime");
	    
	    assertTrue("The date of last login [" + createTime + "] is before creation",
		        lastLogin.after(createTime) || lastLogin.equals(createTime));
	    assertFalse("The create time in the entity [" + createTime + "] shoudn't be changed",
	        priorToRequest.before(createTime) || priorToRequest.equals(createTime));
	    
	}
	
	@Test
	public void testWrongVerifyCode() throws IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
	    HttpServletResponse response = mock(HttpServletResponse.class);
	    PrintWriter writer = mock(PrintWriter.class);
	    when(response.getWriter()).thenReturn(writer);
	    
	     ServletOutputStream streamwriter = mock(ServletOutputStream.class);
	      when(response.getOutputStream()).thenReturn(streamwriter);



	    when(request.getParameter("phone")).thenReturn(phone);
	    when(request.getParameter("token")).thenReturn("wrong verify Code");
	    when(request.getParameter("password")).thenReturn(password);
	    
	    binder.doPost(request, response);

	    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    verify(streamwriter).write(Integer.toString(ErrorCode.auth_incorrect_token).getBytes());
	}
	
	 @Test
	  public void testWrongPhone() throws IOException {
	    HttpServletRequest request = mock(HttpServletRequest.class);
	      HttpServletResponse response = mock(HttpServletResponse.class);
	      PrintWriter writer = mock(PrintWriter.class);
	      when(response.getWriter()).thenReturn(writer);
	      ServletOutputStream streamwriter = mock(ServletOutputStream.class);
	      when(response.getOutputStream()).thenReturn(streamwriter);
	      
	      when(request.getParameter("phone")).thenReturn("wrong phone number");
	      when(request.getParameter("token")).thenReturn(verifyCode);
	      when(request.getParameter("password")).thenReturn(password);

	      
	      binder.doPost(request, response);

	      verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	      verify(streamwriter).write(Integer.toString(ErrorCode.auth_token_not_sent).getBytes());
	  }

}
