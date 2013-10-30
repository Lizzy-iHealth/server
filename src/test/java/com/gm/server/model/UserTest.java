package com.gm.server.model;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import com.gm.common.model.Rpc.CheckinPb;
import com.gm.common.model.Rpc.Friendship;
import com.gm.common.model.Rpc.GeoPoint;
import com.gm.server.ModelTest;
import com.google.appengine.api.datastore.GeoPt;


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
		assertTrue(!user.getCreateTime().after(user.getLastLoginTime()));
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
	  u1.addFriend(u2.getUserID(),Friendship.ADDED);
	  u2.addFriend(u1.getUserID(), Friendship.WAIT_MY_CONFIRM);
	  dao.save(u1);
	  dao.save(u2);
	  assertEquals(0,u1.getFriendshipScore(u2.getId()));
	  assertEquals(0,u2.getFriendshipScore(u1.getId()));
	}
	
	 @Test
	  public void testBlockFriend(){
	    User u1 = new User("1","pwd","s");
	    User u2 = new User("2","pass","sec");
	    DAO dao = DAO.get();
	    dao.save(u1);
	    dao.save(u2);
	    u2.addFriend(u1.getId(), Friendship.ADDED);
	    u1.blockFriend(u2.getId());
	    dao.save(u1);
	    assertEquals(Friendship.BLOCKED,u1.getFriendship(u2.getId()));
	  }
	 
	 @Test
		public void testCheckinCheckout () {
		
			User user = new User();
			CheckinPb.Builder msg = CheckinPb.newBuilder()
									.setDescription("home")
									.setCheckinTimes(100)
									.setGeoPoint(GeoPoint.newBuilder().setLatitude(1).setLongitude(1).setTimestamp(new Date().getTime()))
									.setValidUntil(new Date().getTime()+1000*60*60);
			user.checkin(msg.build());
			dao.save(user);
			user = dao.get(user.getEntityKey(), User.class);
	
			assertNotNull(user);
			assertTrue(user.isCheckedIn());
			//System.out.println(user.getGeo());
			
			assertEquals(0,user.getGeo().compareTo(new GeoPt((float)1.0,(float)1.0)));
			
			user.checkout();
			assertFalse(user.isCheckedIn());
			/*CheckinRecord cr= dao.query(CheckinRecord.class).setAncestor(user.getEntityKey()).prepare().asList().get(0);
			assertNotNull(cr);
			assertEquals("home",cr.getDescription());
			assertEquals(100,cr.checkin_times);
			*/
		}
}
