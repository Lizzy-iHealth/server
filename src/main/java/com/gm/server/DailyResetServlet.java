package com.gm.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Config;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.server.model.Quest;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.Key;

/**
 * Servlet implementation class InitServlet
 */
public class DailyResetServlet extends APIServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.requiresHmac = false;
		execute(request, response);
	}

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp)
			throws ApiException, IOException {
		Key[] questKeys = questAdmin.getQuestKeys();
		Quest[] quests = new Quest[questKeys.length];
		int i = 0;
		for (Key key : questKeys) {
			Quest quest= dao.get(key, Quest.class);
			quest.restart();
			dao.save(quest);
			quests[i]  = quest;
			i++;
		}

		Iterable<User> userItr = dao.query(User.class).prepare().asIterable();
		for (User user : userItr) {

			user.getQuota().setUsedDailyQuestNum(0);
			dao.save(user);

		}
	}

}
