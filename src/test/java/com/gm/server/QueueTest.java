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
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.common.base.Joiner;
import com.gm.common.crypto.Base64;
import com.gm.common.model.Rpc.Currency;
import com.gm.server.model.DAO;
import com.gm.server.model.Feed;
import com.gm.server.model.User;

public abstract class QueueTest {
	
    protected final LocalTaskQueueTestConfig.TaskCountDownLatch latch =
            new LocalTaskQueueTestConfig.TaskCountDownLatch(1);


  
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
    		  new LocalTaskQueueTestConfig().setDisableAutoTaskExecution(false)
    		  								.setCallbackClass(LocalTaskQueueTestConfig.DeferredTaskCallback.class)
    		  								.setTaskExecutionLatch(latch)
    		  ,new LocalDatastoreServiceTestConfig())
          .setEnvAuthDomain("localhost")
          .setEnvEmail("test@localhost")
          ;
  
  
  protected final DAO dao = DAO.get();
  
  public HttpServletRequest getMockRequestWithUser(User user) {
    HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getParameter("phone")).thenReturn(user.getPhone());
      when(request.getParameter("password")).thenReturn(user.getPassword());
      when(request.getParameter("key")).thenReturn(user.getKey());
      when(request.getParameter(ParamKey.device_id.name())).thenReturn("APA91bFWFxgXtR57p3Jj2umYFFV8-U1N9PKKLQydheMybhU_2DxdngHbuYijPRHc1Y2a9dLkhdu9pyLCNd61uRBn9d2i6dggDxjMSkADyAET6rHGCQ9PFQi7HAc_hIsRBA_Z4LAkUddPSH9NxTvIjJZe-ImYHpoNgA");
    return request;
  }
  protected HttpServletRequest mockCurrency(HttpServletRequest req,long amount) {
    String currency = Base64.encodeToString(Currency.newBuilder().setGold(amount).build().toByteArray(),Base64.DEFAULT);
    when(req.getParameter(ParamKey.currency.name())).thenReturn(currency);
    return req;
  }

  protected HttpServletRequest mockUserId( HttpServletRequest req,User B) {
    when(req.getParameter(ParamKey.user_id.name())).thenReturn(Long.toString(B.getId()));
    return req;
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
