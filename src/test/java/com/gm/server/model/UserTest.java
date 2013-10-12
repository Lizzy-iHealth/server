package com.gm.server.model;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

import java.util.Date;


import org.junit.Test;

import com.gm.server.ModelTest;
import com.gm.server.model.Model.Type;


public class UserTest extends ModelTest{



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
	
	@Test
	public void testAddFriend(){
	  User u1 = new User("1","pwd","s");
	  User u2 = new User("2","pass","sec");
	  DAO dao = DAO.get();
	  dao.save(u1);
	  dao.save(u2);
	  u1.addFriend(u2.getUserID(),Type.ADDED);
	  u2.addFriend(u1.getUserID(), Type.WAIT_MY_CONFIRM);
	  dao.save(u1);
	  dao.save(u2);
	}
}
