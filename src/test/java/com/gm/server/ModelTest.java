package com.gm.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.gm.server.model.DAO;
import com.gm.server.model.User;

public abstract class ModelTest {
  
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
          .setEnvAuthDomain("localhost")
          .setEnvEmail("test@localhost");
  
  protected final DAO dao = DAO.get();
  
  public HttpServletRequest getMockRequestWithUser(User user) {
    HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getParameter("phone")).thenReturn(user.getPhone());
      when(request.getParameter("password")).thenReturn(user.getPassword());
      when(request.getParameter("key")).thenReturn(user.getKey());
    return request;
  }

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

}
