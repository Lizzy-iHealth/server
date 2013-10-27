package com.gm.server;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.gm.common.model.Rpc.Currency;
import com.gm.server.model.User;

import static org.junit.Assert.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
public class GetCurrencyTest extends ModelTest {

  @Test
  public void test() throws IOException {
    User A = new User("A","p","s");
    long balance = 5;
    A.setGoldBalance(balance);
    dao.save(A);
    
    HttpServletRequest req= super.getMockRequestWithUser(A);
    HttpServletResponse resp = mock (HttpServletResponse.class);
    ServletOutputStream writer = mock(ServletOutputStream.class);
    when(resp.getOutputStream() ).thenReturn(writer);
    new GetCurrencyServlet().execute(req, resp, false);
    verify(writer).write(Currency.newBuilder().setGold(balance).build().toByteArray());
  }

}
