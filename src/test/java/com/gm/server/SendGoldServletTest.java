package com.gm.server;

import static org.junit.Assert.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletOutputStream;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.gm.common.crypto.Base64;
import com.gm.common.model.Rpc.Currency;
import com.gm.server.model.User;
import com.gm.common.model.*;
import com.gm.common.net.ErrorCode;

public class SendGoldServletTest extends ModelTest {

  @Test
  public void testA2B_LessThanZero() throws IOException {
    User A = new User("A","p","s");
    User B = new User("B","p","s");
    long amount = 5;
    dao.save(A);
    dao.save(B);
    
    HttpServletRequest req = super.getMockRequestWithUser(A);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    mockUserId( req, B);
    mockCurrency(req,amount);
    
    ServletOutputStream writer = mock (ServletOutputStream.class);
    when(resp.getOutputStream()).thenReturn(writer);
    
    new SendGoldServlet().execute(req, resp,false);
        
    verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST); // API failed on
    // validation
    verify(writer).write(Integer.toString(ErrorCode.currency_less_than_zero).getBytes());
    
    A = dao.get(A.getEntityKey(), User.class);
    B = dao.get(B.getEntityKey(), User.class);
    assertEquals(0, A.getGoldBalance());
    assertEquals(0, B.getGoldBalance());
  }

  @Test
  public void testA2B_Success() throws IOException {
    User A = new User("A","p","s");
    User B = new User("B","p","s");
    
    long balance = 100;
    A.setGoldBalance(balance);
    B.setGoldBalance(balance);
    long amount = 5;
    dao.save(A);
    dao.save(B);
    
    HttpServletRequest req = super.getMockRequestWithUser(A);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    mockUserId( req, B);
    mockCurrency(req,amount);
    
    ServletOutputStream writer = mock (ServletOutputStream.class);
    when(resp.getOutputStream()).thenReturn(writer);
    
    new SendGoldServlet().execute(req, resp,false);
        
    verify(resp).setStatus(HttpServletResponse.SC_OK); // API failed on
    // validation
     
    A = dao.get(A.getEntityKey(), User.class);
    B = dao.get(B.getEntityKey(), User.class);
    assertEquals(balance-amount, A.getGoldBalance());
    assertEquals(balance+amount, B.getGoldBalance());
  }

}
