package com.gm.server;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gm.server.model.Token;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;


public class RequestTokenServletTest extends ModelTest{



	@Test
	public void testDoPost() throws IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
	    HttpServletResponse response = mock(HttpServletResponse.class);

	    String phone = "9173489948";
	    String verifyCode = "1234";
	    //String password = "hello";

	    when(request.getParameter("phone")).thenReturn(phone);
	    when(request.getParameter("token")).thenReturn(verifyCode);
	    //when(request.getParameter("password")).thenReturn(password);
	    
	    Date priorToRequest = new Date();
	    RequestTokenServlet rt = new RequestTokenServlet();
	    rt.doPost(request, response);

	    Date afterRequest = new Date();

	    Token e = dao.querySingle("phone",phone,Token.class);

	 
	    assertEquals(phone, e.getPhone());
	    assertEquals(verifyCode, e.getToken());

	    Date date = (Date) e.getGenerateTime();
	    assertTrue("The date in the entity [" + date + "] is prior to the request being performed",
	        priorToRequest.before(date) || priorToRequest.equals(date));
	    assertTrue("The date in the entity [" + date + "] is after to the request completed",
	        afterRequest.after(date) || afterRequest.equals(date));
	}

}
