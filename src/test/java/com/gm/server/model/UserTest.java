package com.gm.server.model;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

public class UserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLogin () {
		String secret = "test secret";
		Date before = new Date();
		User user = new User();
		user.login(secret);
		Date after = new Date();
		assertEquals(secret,user.getSecret());
		
		Date date = user.getLastLoginTime();
	    assertTrue("The date in the entity [" + date + "] is prior to the request being performed",
		        before.before(date) || before.equals(date));
		    assertTrue("The date in the entity [" + date + "] is after to the request completed",
		        after.after(date) || after.equals(date));
	}
	
	@Test
	public void testUserStringStringString () {
		String mobileNumber = "9173489948";
		String password = "my password";
		String secret = "test secret";
		Date before = new Date();
		User user = new User(mobileNumber,password,secret);
		user.login(secret);
		Date after = new Date();
		
		assertEquals(mobileNumber,user.getPhone());
		assertEquals(password,user.getPassword());
		assertEquals(secret,user.getSecret());
		assertEquals(user.getCreateTime(),user.getLastLoginTime());
		Date date = user.getLastLoginTime();
	    assertTrue("The date in the entity [" + date + "] is prior to the request being performed",
		        before.before(date) || before.equals(date));
		assertTrue("The date in the entity [" + date + "] is after to the request completed",
		        after.after(date) || after.equals(date));
	}
}
