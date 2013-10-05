package com.gm.server;

import static com.gm.server.Filters.eq;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gm.server.model.User;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class BindMobileTest extends ModelTest{
	  private RegisterServlet binder;
	    String mobileNumber = "1234567890";
	    String verifyCode = "1234";
	    String password = "hello";
	    String secret="mysecret";
	    String key = "not secret";


	@Before
	public void setUpData() throws Exception {
		//entity in data store of kind "mobiCodeRecord"
	    Entity mobiCodeRecord = new Entity("mobiCodeRecord");
        mobiCodeRecord.setProperty("mobileNumber", mobileNumber);
        mobiCodeRecord.setProperty("verifyCode", verifyCode);
        DatastoreServiceFactory.getDatastoreService().put(mobiCodeRecord);
 	  
        binder = new RegisterServlet();
	}


	@Test
	public void testDoPost() throws IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
	    HttpServletResponse response1 = mock(HttpServletResponse.class);



	    when(request.getParameter("mobileNumber")).thenReturn(mobileNumber);
	    when(request.getParameter("verifyCode")).thenReturn(verifyCode);
	    when(request.getParameter("password")).thenReturn(password);
	    when(request.getParameter("secret")).thenReturn(secret);
	    when(request.getParameter("key")).thenReturn(key);
	    
	    Date priorToRequest = new Date();

	    binder.doPost(request, response1);

	    Date afterRequest = new Date();

	    verify(response1).setStatus(HttpServletResponse.SC_OK);

	   
	    Query query = new Query("User").setFilter(eq("mobileNumber", mobileNumber));
	    Entity e = DatastoreServiceFactory.getDatastoreService().prepare(query).asSingleEntity();

	 
	    assertEquals(mobileNumber, e.getProperty("mobileNumber"));
	    assertEquals(password, e.getProperty("password"));
	    assertEquals(secret, e.getProperty("secret"));
	    assertEquals(key, e.getProperty("key"));
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
	     priorToRequest = new Date();

	    binder.doPost(request, response2);

	     afterRequest = new Date();

	    verify(response2).setStatus(HttpServletResponse.SC_CONFLICT);

	   
	     query = new Query("User").setFilter(eq("mobileNumber", mobileNumber));
	     e = DatastoreServiceFactory.getDatastoreService().prepare(query).asSingleEntity();

	 
	    assertEquals(mobileNumber, e.getProperty("mobileNumber"));
	    assertEquals(password, e.getProperty("password"));
	    assertEquals(secret, e.getProperty("secret"));
	    assertEquals(key, e.getProperty("key"));
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



	    when(request.getParameter("mobileNumber")).thenReturn(mobileNumber);
	    when(request.getParameter("verifyCode")).thenReturn("wrong verify Code");
	    when(request.getParameter("password")).thenReturn(password);
	    when(request.getParameter("secret")).thenReturn(secret);
	    when(request.getParameter("key")).thenReturn(key);
	    
	    
	    binder.doPost(request, response);

	    verify(response).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
	}
	
	@Test
	public void testWrongMobileNumber() throws IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
	    HttpServletResponse response = mock(HttpServletResponse.class);



	    when(request.getParameter("mobileNumber")).thenReturn("wrong mobile number");
	    when(request.getParameter("verifyCode")).thenReturn(verifyCode);
	    when(request.getParameter("password")).thenReturn(password);
	    when(request.getParameter("secret")).thenReturn(secret);
	    when(request.getParameter("key")).thenReturn(key);
	    
	    
	    binder.doPost(request, response);

	    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
}
