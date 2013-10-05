package com.gm.server.model;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gm.server.ModelTest;

public class TokenTest extends ModelTest {

  @Before
  public void setUp() {
    super.setUp();
  }

  @After
  public void tearDown() {
    super.tearDown();
  }

  @Test
  public void test() {
    Token t = new Token("1", "t");
    dao.save(t);
    
    assertNotNull(dao.querySingle("phone", "1", Token.class));
  }

}
