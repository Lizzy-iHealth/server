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

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class BindMobileTest {
	  private BindMobile binder;
	    String mobileNumber = "9173489948";
	    String verifyCode = "1234";
	    String password = "hello";
	    String secret="mysecret";
	    String key = "not secret";

	  private final LocalServiceTestHelper helper =
	      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
	          .setEnvIsLoggedIn(true)
	          .setEnvAuthDomain("localhost")
	          .setEnvEmail("test@localhost");

	@Before
	public void setUp() throws Exception {
	    helper.setUp();
	    Entity mobiCodeRecord = new Entity("mobiCodeRecord");
        mobiCodeRecord.setProperty("mobileNumber", mobileNumber);
        mobiCodeRecord.setProperty("verifyCode", verifyCode);
      
        DatastoreServiceFactory.getDatastoreService().put(mobiCodeRecord);
	    binder = new BindMobile();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDoPost() throws IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
	    HttpServletResponse response = mock(HttpServletResponse.class);



	    when(request.getParameter("mobileNumber")).thenReturn(mobileNumber);
	    when(request.getParameter("verifyCode")).thenReturn(verifyCode);
	    when(request.getParameter("password")).thenReturn(password);
	    when(request.getParameter("secret")).thenReturn(secret);
	    when(request.getParameter("key")).thenReturn(key);
	    response.setStatus(anyInt());
	    Date priorToRequest = new Date();

	    binder.doPost(request, response);

	    Date afterRequest = new Date();

	    verify(response).setStatus(HttpServletResponse.SC_OK);

	   
	    Query query = new Query("user").setFilter(eq("mobileNumber", mobileNumber));
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
	}


}
