package com.gm.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.base.Joiner;
import com.gm.server.model.DAO;
import com.gm.server.model.Feed;
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
      when(request.getParameter(ParamKey.device_id.name())).thenReturn("APA91bFWFxgXtR57p3Jj2umYFFV8-U1N9PKKLQydheMybhU_2DxdngHbuYijPRHc1Y2a9dLkhdu9pyLCNd61uRBn9d2i6dggDxjMSkADyAET6rHGCQ9PFQi7HAc_hIsRBA_Z4LAkUddPSH9NxTvIjJZe-ImYHpoNgA");
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
  
  public static String getResponse(String[] results)
      throws IOException {
    Joiner joiner = Joiner.on(",").skipNulls();
    System.out.println(joiner.join(results));
    return joiner.join(results);
  }

}
