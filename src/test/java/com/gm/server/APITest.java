package com.gm.server;

import static junit.framework.Assert.assertEquals;

import static junit.framework.Assert.assertTrue;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.gm.common.net.ErrorCode;
import com.gm.common.crypto.Base64;
import com.gm.common.model.Rpc.Applicant;
import com.gm.common.model.Rpc.CheckinPb;
import com.gm.common.model.Rpc.Friendship;
import com.gm.common.model.Rpc.GeoPoint;
import com.gm.common.model.Rpc.UserPb;
import com.gm.common.model.Rpc.UsersPb;
import com.gm.common.model.Server;
import com.gm.common.model.Rpc.Friend;
import com.gm.server.model.CheckinRecord;
import com.gm.server.model.Feed;
import com.gm.server.model.PendingUser;
import com.gm.server.model.Quest;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.DeferredTask;

public class APITest extends ModelTest {

	 @Test
	public void testApplyQuest() throws IOException {
		User user = new User("a12345", "password", "secret");
		User friend = new User("b12345", "password", "secret");
		dao.save(user);
		dao.save(friend);

		user.addFriend(friend.getId(), Friendship.CONFIRMED);
		friend.addFriend(user.getId(), Friendship.CONFIRMED);
		dao.save(user);
		dao.save(friend);

		String title = "a quest";
		Quest quest = new Quest(title);
		dao.save(quest, user.getEntityKey());

		HttpServletRequest req = super.getMockRequestWithUser(friend);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		ServletOutputStream writer = mock(ServletOutputStream.class);
		when(resp.getOutputStream()).thenReturn(writer);

		Applicant app = Applicant.newBuilder()
				.setType(Applicant.Status.WAIT_MY_CONFIRM)
				.setUserId(friend.getId()).build();
		String applicantMsg = Base64.encodeToString(app.toByteArray(),
				Base64.DEFAULT);
		when(req.getParameter(ParamKey.applicant.name())).thenReturn(
				applicantMsg);
		when(req.getParameter(ParamKey.owner_id.name())).thenReturn(
				String.valueOf(user.getId()));
		when(req.getParameter(ParamKey.id.name())).thenReturn(
				String.valueOf(quest.getId()));

		PrintWriter writer3 = mock(PrintWriter.class);
		when(resp.getWriter()).thenReturn(writer3);
		new ApplyQuestServlet().execute(req, resp, false);
		Quest q = dao.get(quest.getEntityKey(), Quest.class);
		assertNotNull(q);

		assertEquals(1, q.getApplicants().getApplicantCount());
		assertEquals(friend.getId(), q.getApplicants().getApplicant(0)
				.getUserId());

	}

	

	// @Test
	public void testBlockFriends() throws IOException {
		// prepare datastore: user and friend are friends.
		User user = new User("a12345", "password", "secret");
		User friend = new User("b12345", "password", "secret");
		dao.save(user);
		dao.save(friend);
		user.addFriend(friend.getUserID(), Friendship.CONFIRMED);
		friend.addFriend(user.getUserID(), Friendship.CONFIRMED);
		dao.save(user);
		dao.save(friend);

		// verify status before test
		User userInDB = dao.get(user.getKey(), User.class);
		User friendInDB = dao.get(friend.getKey(), User.class);
		assertEquals(1, userInDB.getFriends().getFriendCount());
		assertEquals(1, friendInDB.getFriends().getFriendCount());

		// user block friend.
		HttpServletRequest req = super.getMockRequestWithUser(user);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		ServletOutputStream writer = mock(ServletOutputStream.class);
		String[] deleteIds = { Long.toString(friend.getUserID()) };
		when(req.getParameterValues(ParamKey.user_id.name())).thenReturn(
				deleteIds);
		when(resp.getOutputStream()).thenReturn(writer);
		// APIServlet.block_friends.execute(req, resp,false);

		userInDB = dao.get(user.getKey(), User.class);
		friendInDB = dao.get(friend.getKey(), User.class);
		assertEquals(1, userInDB.getFriends().getFriendCount());
		assertEquals(Friendship.BLOCKED, userInDB.getFriends().getFriend(0)
				.getFriendship());
		assertEquals(0, friendInDB.getFriends().getFriendCount());

		// block again, nothing should happened:

		// API.block_friends.execute(req, resp,false);
		userInDB = dao.get(user.getKey(), User.class);
		friendInDB = dao.get(friend.getKey(), User.class);
		assertEquals(Friendship.BLOCKED, userInDB.getFriends().getFriend(0)
				.getFriendship());
		assertEquals(0, friendInDB.getFriends().getFriendCount());

	}

	// @Test
	public void testDeleteFriends() throws IOException {
		// prepare datastore: user and friend are friends.
		User user = new User("a12345", "password", "secret");
		User friend = new User("b12345", "password", "secret");
		dao.save(user);
		dao.save(friend);
		user.addFriend(friend.getUserID(), Friendship.CONFIRMED);
		friend.addFriend(user.getUserID(), Friendship.CONFIRMED);
		dao.save(user);
		dao.save(friend);

		// before:
		User userInDB = dao.get(user.getKey(), User.class);
		User friendInDB = dao.get(friend.getKey(), User.class);
		assertEquals(1, userInDB.getFriends().getFriendCount());
		assertEquals(1, friendInDB.getFriends().getFriendCount());

		// delete
		HttpServletRequest req = super.getMockRequestWithUser(user);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		ServletOutputStream writer = mock(ServletOutputStream.class);
		String[] deleteIds = { Long.toString(friend.getUserID()) };
		when(req.getParameterValues(ParamKey.user_id.name())).thenReturn(
				deleteIds);
		when(resp.getOutputStream()).thenReturn(writer);
		// new DeleteFriendsServlet().execute(req, resp,false);

		userInDB = dao.get(user.getKey(), User.class);
		friendInDB = dao.get(friend.getKey(), User.class);
		assertEquals(0, userInDB.getFriends().getFriendCount());
		assertEquals(0, friendInDB.getFriends().getFriendCount());

		// delete again, nothing should happened:

		// API.get_friends.execute(req, resp,false);
		userInDB = dao.get(user.getKey(), User.class);
		friendInDB = dao.get(friend.getKey(), User.class);
		assertEquals(0, userInDB.getFriends().getFriendCount());
		assertEquals(0, friendInDB.getFriends().getFriendCount());

	}

	@Test
	public void testGetFriends() throws IOException {
		User user = new User("a12345", "password", "secret");
		User friend = new User("b12345", "password", "secret");
		dao.save(user);
		dao.save(friend);
		HttpServletRequest req = super.getMockRequestWithUser(user);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		ServletOutputStream writer = mock(ServletOutputStream.class);
		when(resp.getOutputStream()).thenReturn(writer);
		new GetFriendsServlet().execute(req, resp, false);

		assertEquals(0, user.getFriends().getFriendCount());

		// Let user has a friend, then test again:
		user.addFriend(friend.getUserID(), Friendship.CONFIRMED);
		friend.addFriend(user.getUserID(), Friendship.CONFIRMED);
		dao.save(user);
		dao.save(friend);
		new GetFriendsServlet().execute(req, resp, false);

		User userInDB = dao.get(user.getKey(), User.class);
		verify(writer).write(userInDB.getFriends().build().toByteArray());
		assertEquals(1, userInDB.getFriends().getFriendCount());
		assertEquals(friend.getUserID(), userInDB.getFriends().getFriend(0)
				.getId());
		assertEquals(Friendship.CONFIRMED, userInDB.getFriends().getFriend(0)
				.getFriendship());
	}

	@Test
	public void testGetFriendsDetails() throws IOException {
		User user = new User("a12345", "password", "secret");
		User friend = new User("b12345", "password", "secret");
		dao.save(user);
		dao.save(friend);
		HttpServletRequest req = super.getMockRequestWithUser(user);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		ServletOutputStream writer = mock(ServletOutputStream.class);
		String[] friendIds = { String.valueOf(friend.getId()) };
		when(resp.getOutputStream()).thenReturn(writer);

		// request a user that is not requester's friend, should return basic
		// information
		when(req.getParameterValues(ParamKey.user_id.name())).thenReturn(
				friendIds);

		new GetFriendsDetailsServlet().execute(req, resp, false);
		UsersPb.Builder users = UsersPb.newBuilder();

		User userInDB = dao.get(user.getKey(), User.class);
		User friendInDB = dao.get(friend.getEntityKey(), User.class);
		UserPb.Builder friendMsg = friendInDB.getMSG(userInDB.getId());
		friendMsg.setFriendship(Friendship.UNKNOWN);
		users.addUser(friendMsg);
		assertEquals(0, user.getFriends().getFriendCount());
		verify(writer).write(users.build().toByteArray());

		// Let user has a friends, then test again:
		user.addFriend(friend.getUserID(), Friendship.CONFIRMED);
		friend.addFriend(user.getUserID(), Friendship.CONFIRMED);
		dao.save(user);
		dao.save(friend);
		new GetFriendsDetailsServlet().execute(req, resp, false);

		userInDB = dao.get(user.getKey(), User.class);
		friendInDB = dao.get(friend.getEntityKey(), User.class);
		friendMsg = friendInDB.getMSG(userInDB.getId());
		friendMsg.setFriendship(Friendship.CONFIRMED);
		users.clearUser();
		users.addUser(friendMsg);
		verify(writer).write(users.build().toByteArray());
		assertEquals(1, userInDB.getFriends().getFriendCount());

		// Let user has n more friends, then get new friends' info :

		int n = 10;
		User friends[] = new User[n];
		String newfriendIds[] = new String[n];

		// data in users is for verification
		UsersPb.Builder newusers = UsersPb.newBuilder();

		// prepare data in datastore
		for (int i = 0; i < n; i++) {
			friends[i] = new User(String.valueOf(i), "p", "s");

			dao.save(friends[i]);

			newfriendIds[i] = String.valueOf(friends[i].getId());
			user.addFriend(friends[i].getId(), Friendship.CONFIRMED);
			friends[i].addFriend(user.getId(), Friendship.CONFIRMED);
			friends[i].increaseFriendshipScore(user.getId());
			user.increaseFriendshipScore(friends[i].getId());
			dao.save(user);
			dao.save(friends[i]);
		}

		for (int i = 0; i < n; i++) {
			friendInDB = dao.get(friends[i].getEntityKey(), User.class);
			friendMsg = friendInDB.getMSG(user.getId());
			friendMsg.setFriendship(Friendship.CONFIRMED);
			friendMsg.setFriendScore(1);
			newusers.addUser(friendMsg);
		}
		when(req.getParameterValues(ParamKey.user_id.name())).thenReturn(
				newfriendIds);
		new GetFriendsDetailsServlet().execute(req, resp, false);

		userInDB = dao.get(user.getKey(), User.class);

		assertEquals(10, newusers.getUserCount());
		// System.out.println(newusers.build().toString());
		verify(writer).write(newusers.build().toByteArray());
		assertEquals(n + 1, userInDB.getFriends().getFriendCount());
	}

	@Test
	public void testGetPhoneDetails() throws IOException {
		User user = new User("a12345", "password", "secret");
		User friend = new User("b12345", "password", "secret");
		dao.save(user);
		dao.save(friend);

		HttpServletRequest req = super.getMockRequestWithUser(user);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		ServletOutputStream writer = mock(ServletOutputStream.class);

		when(resp.getOutputStream()).thenReturn(writer);

		// request a user that is not requester's friend
		// TODO: should ask the user(if not friend) to prove information
		// disclosure
		// for privacy
		when(req.getParameter(ParamKey.phone.name())).thenReturn(
				friend.getPhone());

		new GetPhoneDetailsServlet().execute(req, resp, false);
		UserPb.Builder returnMsg = friend.getMSG(user.getId());
		returnMsg.setFriendship(Friendship.UNKNOWN);

		assertEquals(0, user.getFriends().getFriendCount());
		verify(writer).write(returnMsg.build().toByteArray());

		// request self info

		when(req.getParameter(ParamKey.phone.name())).thenReturn(
				user.getPhone());

		new GetPhoneDetailsServlet().execute(req, resp, false);
		returnMsg = user.getMSG(user.getId());
		returnMsg.setFriendship(Friendship.UNKNOWN);

		assertEquals(0, user.getFriends().getFriendCount());
		verify(writer).write(returnMsg.build().toByteArray());

		// request a friend's info
		// TODO: should ask the user(if not friend) to prove information
		// disclosure
		// for privacy

		user.addFriend(friend.getId(), Friendship.CONFIRMED);
		friend.addFriend(user.getId(), Friendship.CONFIRMED);
		dao.save(user);
		dao.save(friend);

		when(req.getParameter(ParamKey.phone.name())).thenReturn(
				friend.getPhone());

		new GetPhoneDetailsServlet().execute(req, resp, false);
		returnMsg = friend.getMSG(user.getId());
		returnMsg.setFriendship(Friendship.CONFIRMED);

		assertEquals(1, user.getFriends().getFriendCount());
		verify(writer).write(returnMsg.build().toByteArray());

		// request a friend to be confirmed
		// TODO: should ask the user(if not friend) to prove information
		// disclosure
		// for privacy
		User newuser = new User("abc", "p", "s");
		dao.save(newuser);
		newuser.addFriend(friend.getId(), Friendship.ADDED);
		friend.addFriend(newuser.getId(), Friendship.WAIT_MY_CONFIRM);
		dao.save(newuser);
		dao.save(friend);
		req = super.getMockRequestWithUser(friend);
		when(req.getParameter(ParamKey.phone.name())).thenReturn(
				newuser.getPhone());

		new GetPhoneDetailsServlet().execute(req, resp, false);
		returnMsg = newuser.getMSG(friend.getId());
		returnMsg.setFriendship(Friendship.WAIT_MY_CONFIRM);

		assertEquals(2, friend.getFriends().getFriendCount());
		verify(writer).write(returnMsg.build().toByteArray());
	}

	@Test
	public void testInviteFriends() throws IOException {
		User user = new User("a12345", "password", "secret");
		String[] friend_phone = { "22345", "33445" };
		dao.save(user);
		HttpServletRequest req = super.getMockRequestWithUser(user);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		when(req.getParameterValues(ParamKey.phone.name())).thenReturn(
				friend_phone);
		PrintWriter writer = mock(PrintWriter.class);
		ServletOutputStream swriter = mock(ServletOutputStream.class);
		when(resp.getWriter()).thenReturn(writer);
		when(resp.getOutputStream()).thenReturn(swriter);
		new InviteFriendsServlet().execute(req, resp, false);
		
		List<User> pu = dao.query(User.class)
				.sortBy("phone", false).prepare().asList();
		assertEquals(3, pu.size());
		assertEquals(pu.get(0).getPhone(), friend_phone[0]);
		assertEquals(pu.get(0).getFriends().getFriend(0).getId(), user
				.getEntityKey().getId());
		assertEquals(pu.get(1).getPhone(), friend_phone[1]);
		assertEquals(pu.get(1).getFriends().getFriend(0).getId(), user
				.getEntityKey().getId());
		verify(swriter).write("3,3".getBytes());

		// invite a person already our user:
		User f = new User("42345", "p", "s");
		String phoneList[] = { "42345" };
		dao.save(f);
		when(req.getParameterValues(ParamKey.phone.name())).thenReturn(
				phoneList);
		new InviteFriendsServlet().execute(req, resp, false);

		pu = dao.query(User.class).sortBy("phone", false).prepare()
				.asList();
		assertEquals(4, pu.size());
		verify(swriter).write("1".getBytes());

	}

	@Test
	public void testAddFriends() throws IOException {
		User[] users = { new User("a12345", "password", "secret"),
				new User("b12345", "password", "secret"),
				new User("c12345", "password", "secret") };

		String[] userkeys = new String[users.length];
		long[] ids = new long[users.length];
		for (int i = 0; i < users.length; i++) {
			users[i].setDeviceID("APA91bFWFxgXtR57p3Jj2umYFFV8-U1N9PKKLQydheMybhU_2DxdngHbuYijPRHc1Y2a9dLkhdu9pyLCNd61uRBn9d2i6dggDxjMSkADyAET6rHGCQ9PFQi7HAc_hIsRBA_Z4LAkUddPSH9NxTvIjJZe-ImYHpoNgA");
			dao.save(users[i]);
			userkeys[i] = users[i].getKey();
			ids[i] = KeyFactory.stringToKey(userkeys[i]).getId();
		}
		AddFriendsServlet af = new AddFriendsServlet();

		// a add b
		HttpServletRequest req = getMockRequestWithUser(users[0]);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		ServletOutputStream writer = mock(ServletOutputStream.class);
		String[] fl = { Long.toString(ids[1]) };
		when(req.getParameterValues(ParamKey.user_id.name())).thenReturn(fl);
		when(resp.getOutputStream()).thenReturn(writer);
		new AddFriendsServlet().execute(req, resp, false);
		verify(resp).setStatus(HttpServletResponse.SC_OK);

		User ua = dao.get(users[0].getEntityKey(), User.class);
		List<Friend> uaf = ua.getFriends().getFriendList();
		assertEquals(uaf.size(), 1);
		assertEquals(uaf.get(0).getId(), ids[1]);
		assertEquals(uaf.get(0).getFriendship(), Friendship.ADDED);

		User ub = dao.get(users[1].getEntityKey(), User.class);
		List<Friend> ubf = ub.getFriends().getFriendList();
		assertEquals(ubf.size(), 1);
		assertEquals(ubf.get(0).getId(), ids[0]);
		assertEquals(ubf.get(0).getFriendship(), Friendship.WAIT_MY_CONFIRM);

		// b add a and c
		HttpServletRequest req2 = getMockRequestWithUser(users[1]);
		HttpServletResponse resp2 = mock(HttpServletResponse.class);
		String[] bfl = { Long.toString(ids[0]), Long.toString(ids[2]) };
		when(req2.getParameterValues(ParamKey.user_id.name())).thenReturn(bfl);
		when(resp2.getOutputStream()).thenReturn(writer);
		new AddFriendsServlet().execute(req2, resp2, false);
		// af.doPost(req2, resp2);
		verify(resp2).setStatus(HttpServletResponse.SC_OK);

		ua = dao.get(users[0].getEntityKey(), User.class);
		uaf = ua.getFriends().getFriendList();
		assertEquals(uaf.size(), 1);
		assertEquals(uaf.get(0).getId(), ids[1]);
		assertEquals(uaf.get(0).getFriendship(), Friendship.CONFIRMED);

		ub = dao.get(users[1].getEntityKey(), User.class);
		ubf = ub.getFriends().getFriendList();
		assertEquals(ubf.size(), 2);
		assertEquals(ubf.get(0).getId(), ids[0]);
		assertEquals(ubf.get(1).getId(), ids[2]);
		assertEquals(ubf.get(0).getFriendship(), Friendship.CONFIRMED);
		assertEquals(ubf.get(1).getFriendship(), Friendship.ADDED);

		User uc = dao.get(users[2].getEntityKey(), User.class);
		List<Friend> ucf = uc.getFriends().getFriendList();
		assertEquals(ucf.size(), 1);
		assertEquals(ucf.get(0).getId(), ids[1]);
		assertEquals(ucf.get(0).getFriendship(), Friendship.WAIT_MY_CONFIRM);

		// repeated add after confirm: a add b again
		HttpServletRequest req3 = getMockRequestWithUser(users[0]);
		HttpServletResponse resp3 = mock(HttpServletResponse.class);
		String[] fl3 = { Long.toString(ids[1]) };
		when(req3.getParameterValues(ParamKey.user_id.name())).thenReturn(fl3);
		when(resp3.getOutputStream()).thenReturn(writer);
		new AddFriendsServlet().execute(req3, resp3, false);
		// af.doPost(req3, resp3);
		verify(resp3).setStatus(HttpServletResponse.SC_OK);

		ua = dao.get(users[0].getEntityKey(), User.class);
		uaf = ua.getFriends().getFriendList();
		assertEquals(uaf.size(), 1);
		assertEquals(uaf.get(0).getId(), ids[1]);
		assertEquals(uaf.get(0).getFriendship(), Friendship.CONFIRMED);

		ub = dao.get(users[1].getEntityKey(), User.class);
		ubf = ub.getFriends().getFriendList();
		assertEquals(ubf.size(), 2);
		assertEquals(ubf.get(0).getId(), ids[0]);
		assertEquals(ubf.get(0).getFriendship(), Friendship.CONFIRMED);

		uc = dao.get(users[2].getEntityKey(), User.class);
		ucf = uc.getFriends().getFriendList();
		assertEquals(ucf.size(), 1);
		assertEquals(ucf.get(0).getId(), ids[1]);
		assertEquals(ucf.get(0).getFriendship(), Friendship.WAIT_MY_CONFIRM);

		// add wrong user id: a add "5"
		HttpServletRequest req4 = getMockRequestWithUser(users[0]);
		HttpServletResponse resp4 = mock(HttpServletResponse.class);
		String[] fl4 = { "5555" };
		when(req4.getParameterValues(ParamKey.user_id.name())).thenReturn(fl4);
		ServletOutputStream writer4 = mock(ServletOutputStream.class);
		when(resp4.getOutputStream()).thenReturn(writer4);

		new AddFriendsServlet().execute(req4, resp4, false);
		// af.doPost(req4, resp4);

		verify(resp4).setStatus(HttpServletResponse.SC_OK);

		verify(writer4).write("8".getBytes());
		ua = dao.get(users[0].getEntityKey(), User.class);
		uaf = ua.getFriends().getFriendList();
		assertEquals(uaf.size(), 1);
		assertEquals(uaf.get(0).getId(), ids[1]);
		assertEquals(uaf.get(0).getFriendship(), Friendship.CONFIRMED);

		// add 3 friends, with a wrong user id in the middle: a add b, "5" and
		// c,
		// b,c will be successfully added.
		HttpServletRequest req5 = getMockRequestWithUser(users[0]);
		HttpServletResponse resp5 = mock(HttpServletResponse.class);
		String[] fl5 = { Long.toString(ids[1]), "5", Long.toString(ids[2]) };
		when(req5.getParameterValues(ParamKey.user_id.name())).thenReturn(fl5);
		ServletOutputStream writer5 = mock(ServletOutputStream.class);
		when(resp5.getOutputStream()).thenReturn(writer5);
		when(resp.getOutputStream()).thenReturn(writer);
		new AddFriendsServlet().execute(req5, resp5, false);
		// af.doPost(req5, resp5);
		verify(resp5).setStatus(HttpServletResponse.SC_OK);

		verify(writer5).write("0,8,1".getBytes());
		ua = dao.get(users[0].getEntityKey(), User.class);
		uaf = ua.getFriends().getFriendList();
		assertEquals(2, uaf.size());
		assertEquals(ids[1], uaf.get(0).getId());
		assertEquals(Friendship.CONFIRMED, uaf.get(0).getFriendship());
		assertEquals(ids[2], uaf.get(1).getId());
		assertEquals(Friendship.ADDED, uaf.get(1).getFriendship());

		ub = dao.get(users[1].getEntityKey(), User.class);
		ubf = ub.getFriends().getFriendList();
		assertEquals(ubf.size(), 2);
		assertEquals(ubf.get(0).getId(), ids[0]);
		assertEquals(ubf.get(0).getFriendship(), Friendship.CONFIRMED);
		assertEquals(ubf.get(1).getId(), ids[2]);
		assertEquals(ubf.get(1).getFriendship(), Friendship.ADDED);

		uc = dao.get(users[2].getEntityKey(), User.class);
		ucf = uc.getFriends().getFriendList();
		assertEquals(ucf.size(), 2);
		assertEquals(ucf.get(0).getId(), ids[1]);
		assertEquals(ucf.get(0).getFriendship(), Friendship.WAIT_MY_CONFIRM);
		assertEquals(ucf.get(1).getId(), ids[0]);
		assertEquals(ucf.get(1).getFriendship(), Friendship.WAIT_MY_CONFIRM);
	}

	@Test
	public void testDeviceServlet() throws IOException {
		User userInDB = new User("12345", "password", "secret");
		dao.save(userInDB);

		HttpServletRequest req = getMockRequestWithUser(userInDB);
		HttpServletResponse resp = mock(HttpServletResponse.class);

		String deviceID = new String("My Device ID");
		when(req.getParameter("device_id")).thenReturn(deviceID);
		DeviceServlet ds = new DeviceServlet();

		new DeviceServlet().execute(req, resp, false);
		// ds.doPost(req, resp);

		User user = dao.get(userInDB.getEntityKey(), User.class);

		assertEquals(deviceID, user.getDeviceID());

	}
	
	 @Test
		public void testCheckin () throws IOException {
		
			User user = new User();
			dao.save(user);
			CheckinPb.Builder msg = CheckinPb.newBuilder()
									.setDescription("home")
									.setCheckinTimes(100)
									.setGeoPoint(GeoPoint.newBuilder().setLatitude(1).setLongitude(1).setTimestamp(new Date().getTime()))
									.setValidUntil(new Date().getTime()+1000*60*60);
			
			HttpServletRequest req = super.getMockRequestWithUser(user);
			when(req.getParameter(ParamKey.pb.name())).thenReturn(Base64.encodeToString(msg.build().toByteArray(),Base64.DEFAULT));
			HttpServletResponse resp = mock (HttpServletResponse.class);
			ServletOutputStream writer = mock(ServletOutputStream.class);
			when(resp.getOutputStream()).thenReturn(writer);
			new CheckinServlet().execute(req, resp,false);
			user = dao.get(user.getEntityKey(), User.class);
	
			assertTrue(user.isCheckedIn());
			//System.out.println(user.getGeo());
			
			assertEquals(0,user.getGeo().compareTo(new GeoPt((float)1.0,(float)1.0)));
			assertEquals(1,user.getExperience());
		
			CheckinRecord cr= dao.query(CheckinRecord.class).setAncestor(user.getEntityKey()).prepare().asList().get(0);
			assertEquals("home",cr.getDescription());
			assertEquals(100,cr.getCheckin_times());
			verify(writer).write(user.getMSG(user.getId()).build().toByteArray());
			
		}

	/*
	 * @Test public void testPing() { String key = UUID.randomUUID().toString();
	 * String secret = UUID.randomUUID().toString(); String phone = "1234567";
	 * 
	 * User user = new User(phone,"password",secret,key); dao.create(user);
	 * 
	 * // |------------ body -------------| // ........&key=....&hmac=..........
	 * // |--- message ---||-- hmacPart --| String message =
	 * "phone=1234567, friendids=[1,2,3] test message&key=" + key; String hmac =
	 * Hmac.generate(message, secret); String body = message + "&hmac=" + hmac;
	 * 
	 * //TODO: find a way to mock ServletInputStream
	 * 
	 * HttpServletRequest req = mock(HttpServletRequest.class);
	 * HttpServletResponse resp = mock(HttpServletResponse.class);
	 * 
	 * 
	 * }
	 */

}
