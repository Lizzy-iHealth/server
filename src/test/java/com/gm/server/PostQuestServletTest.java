package com.gm.server;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.gm.common.crypto.Base64;
import com.gm.common.model.Rpc.Config;
import com.gm.common.model.Rpc.Friendship;
import com.gm.server.model.Feed;
import com.gm.server.model.Quest;
import com.gm.server.model.User;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;

public class PostQuestServletTest extends ModelTest {

    
    @Test
	public void testPostQuest() throws IOException, InterruptedException {
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
		String audiances[] = { String.valueOf(friend.getId()) };
		HttpServletRequest req = super.getMockRequestWithUser(user);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		ServletOutputStream writer = mock(ServletOutputStream.class);
		when(resp.getOutputStream()).thenReturn(writer);
		when(resp.getWriter()).thenReturn(mock(PrintWriter.class));
		String questString = Base64.encodeToString(quest.getMSG().build()
				.toByteArray(), Base64.DEFAULT);
		when(req.getParameter(ParamKey.quest.name())).thenReturn(questString);
		when(req.getParameterValues(ParamKey.user_id.name())).thenReturn(
				audiances);

		new PostQuestServlet().execute(req, resp, false);
		
		Quest q = dao.query(Quest.class).prepare().asSingle();
		assertNotNull(q);

		Quest questInDb = dao.querySingle(Quest.class, user.getEntityKey());
		assertEquals(title, questInDb.getTitle());
		assertEquals(1, questInDb.getPosts().getPostCount());
		
        LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
        QueueStateInfo qsi = ltq.getQueueStateInfo().get(QueueFactory.getDefaultQueue().getQueueName());
        assertEquals(1, qsi.getTaskInfo().size());
		
	}
    
	 @Test
	public void testShareQuest() throws IOException {
		User user = new User("a12345", "password", "secret");
		User friend = new User("b12345", "password", "secret");
		User c = new User("c", "p", "s");
		User us[] = { user, friend, c };
		for (User u : us) {
			u.setDeviceID("APA91bFWFxgXtR57p3Jj2umYFFV8-U1N9PKKLQydheMybhU_2DxdngHbuYijPRHc1Y2a9dLkhdu9pyLCNd61uRBn9d2i6dggDxjMSkADyAET6rHGCQ9PFQi7HAc_hIsRBA_Z4LAkUddPSH9NxTvIjJZe-ImYHpoNgA");
			dao.save(u);
		}

		user.addFriend(friend.getId(), Friendship.CONFIRMED);
		friend.addFriend(user.getId(), Friendship.CONFIRMED);
		friend.addFriend(c.getId(), Friendship.CONFIRMED);
		c.addFriend(friend.getId(), Friendship.CONFIRMED);
		dao.save(user);
		dao.save(friend);
		dao.save(c);

		//quest is not allow sharing
		String title = "a quest";
		Quest quest = new Quest(title);
		long firstReceivers[] = { friend.getId() };
		quest.addPost(user.getId(), firstReceivers);
		dao.save(quest, user.getEntityKey());
		System.out.println(quest.getId());
		String audiances[] = { String.valueOf(c.getId()) };
		assertEquals(1, quest.getPosts().getPostCount());
		HttpServletRequest req = super.getMockRequestWithUser(friend);
		HttpServletResponse resp = mock(HttpServletResponse.class);
		ServletOutputStream writer = mock(ServletOutputStream.class);

		when(resp.getOutputStream()).thenReturn(writer);
		// String questString =
		// Base64.encodeToString(quest.getMSG().build().toByteArray(),Base64.DEFAULT);
		when(req.getParameter(ParamKey.id.name())).thenReturn(
				String.valueOf(quest.getId()));
		when(req.getParameter(ParamKey.owner_id.name())).thenReturn(
				String.valueOf(user.getId()));
		when(req.getParameterValues(ParamKey.user_id.name())).thenReturn(
				audiances);

		new ShareQuestServlet().execute(req, resp, false);

		Quest questInDb = dao.get(quest.getEntityKey(), Quest.class);
		assertEquals(title, questInDb.getTitle());
		assertEquals(1, questInDb.getPosts().getPostCount());

        LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
        QueueStateInfo qsi = ltq.getQueueStateInfo().get(QueueFactory.getDefaultQueue().getQueueName());
        assertEquals(0, qsi.getTaskInfo().size());


	}
	 @Test
		public void testShareQuestAllow() throws IOException {
			User user = new User("a12345", "password", "secret");
			User friend = new User("b12345", "password", "secret");
			User c = new User("c", "p", "s");
			User us[] = { user, friend, c };
			for (User u : us) {
				u.setDeviceID("APA91bFWFxgXtR57p3Jj2umYFFV8-U1N9PKKLQydheMybhU_2DxdngHbuYijPRHc1Y2a9dLkhdu9pyLCNd61uRBn9d2i6dggDxjMSkADyAET6rHGCQ9PFQi7HAc_hIsRBA_Z4LAkUddPSH9NxTvIjJZe-ImYHpoNgA");
				dao.save(u);
			}

			user.addFriend(friend.getId(), Friendship.CONFIRMED);
			friend.addFriend(user.getId(), Friendship.CONFIRMED);
			friend.addFriend(c.getId(), Friendship.CONFIRMED);
			c.addFriend(friend.getId(), Friendship.CONFIRMED);
			dao.save(user);
			dao.save(friend);
			dao.save(c);

			//quest is not allow sharing
			String title = "a quest";
			Quest quest = new Quest(title);
			long firstReceivers[] = { friend.getId() };
			quest.addPost(user.getId(), firstReceivers);
			quest.setConfig(Config.newBuilder().setAllowSharing(true));
			dao.save(quest, user.getEntityKey());
			System.out.println(quest.getId());
			String audiances[] = { String.valueOf(c.getId()) };
			assertEquals(1, quest.getPosts().getPostCount());
			HttpServletRequest req = super.getMockRequestWithUser(friend);
			HttpServletResponse resp = mock(HttpServletResponse.class);
			ServletOutputStream writer = mock(ServletOutputStream.class);

			when(resp.getOutputStream()).thenReturn(writer);
			// String questString =
			// Base64.encodeToString(quest.getMSG().build().toByteArray(),Base64.DEFAULT);
			when(req.getParameter(ParamKey.id.name())).thenReturn(
					String.valueOf(quest.getId()));
			when(req.getParameter(ParamKey.owner_id.name())).thenReturn(
					String.valueOf(user.getId()));
			when(req.getParameterValues(ParamKey.user_id.name())).thenReturn(
					audiances);

			new ShareQuestServlet().execute(req, resp, false);

			Quest questInDb = dao.get(quest.getEntityKey(), Quest.class);
			assertEquals(title, questInDb.getTitle());
			assertEquals(2, questInDb.getPosts().getPostCount());

	        LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
	        QueueStateInfo qsi = ltq.getQueueStateInfo().get(QueueFactory.getDefaultQueue().getQueueName());
	        assertEquals(1, qsi.getTaskInfo().size());


		}


}
