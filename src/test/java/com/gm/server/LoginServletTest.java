package com.gm.server;

import static com.gm.server.Filters.eq;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
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

public class LoginServletTest extends ModelTest{

	private User user;
  @Before
	public void setUpData() throws Exception {
	   user = new User("9173489948","my password","my secret");
	   dao.save(user);
	}

	@Test
	public void testDoPost() throws IOException{
	 
	    LoginServlet login = new LoginServlet();
	    
	    User rightUser = new User("9173489948","my password","my secret");
	    User wrongMobile = new User("9173489949","my password","my secret");
	    User wrongPassword = new User("9173489948","wrong password","my secret");
	    
	    User users[]={rightUser,wrongMobile,wrongPassword};
	    HttpServletRequest[] requests=new HttpServletRequest[3];
	    HttpServletResponse[] responses=new HttpServletResponse[3];
	    PrintWriter writer = mock(PrintWriter.class);
	    Date before = new Date();
	    
	    for(int i =0;i<users.length;i++){
	    		requests[i]=getMockRequestWithUser(users[i]);
	    		responses[i]= mock(HttpServletResponse.class);
	    		when(responses[i].getWriter()).thenReturn(writer);
	    		login.doPost(requests[i], responses[i]);
	    }
	    
	    Date after = new Date();
	    
	    verify(responses[0]).setStatus(HttpServletResponse.SC_OK);
	    verify(responses[1]).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    verify(responses[2]).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    
	  
	    verifyUserInDB(rightUser.getPhone(),user,before,after,writer);
	}
	    public void verifyUserInDB(String mobileNumber,User mockUser, Date before, Date after,PrintWriter writer){
	    	  User userEntity=dao.get(mockUser.getEntityKey(), User.class);
	    	  String secret = userEntity.getSecret();
	    		String key = userEntity.getKey();
	        verify(writer).write(key);
	        verify(writer).write(",");
	        verify(writer).write(secret);
	  	    
	  	    Date dateSeq[] = {
	  		  	   userEntity.getCreateTime()
	  		  	  ,before
	  		  	  ,userEntity.getLastLoginTime()
	  		  	  ,after
	  	    };
	  	    

	  	    for(int i=0;i<dateSeq.length-1;i++){
	  	    	
	  	    	assertTrue("The date of dataSeq[" + i + "] is after the dateSeq["+(i+1)+"]",
	  		       dateSeq[i].before(dateSeq[i+1]) || dateSeq[i].equals(dateSeq[i+1])) ;
	  	    }
	    }
	
	
	
	public HttpServletRequest getMockRequestWithUser(User user) {
		HttpServletRequest request = mock(HttpServletRequest.class);
	    when(request.getParameter("phone")).thenReturn(user.getPhone());
	    when(request.getParameter("password")).thenReturn(user.getPassword());
		return request;
	}
	    

}
