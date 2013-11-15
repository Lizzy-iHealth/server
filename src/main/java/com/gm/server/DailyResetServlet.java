package com.gm.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Applicant;
import com.gm.common.model.Rpc.Config;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Rpc.UserPb;
import com.gm.server.model.Office;
import com.gm.server.model.Quest;
import com.gm.server.model.SystemQuest;
import com.gm.server.model.SystemUser;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.KeyFactory;
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

		SystemQuest[] sq = { SystemQuest.Checkin };

		// Daily quests is a subset of questAdmin's quests.
		Office dailyQuestAdmin = new Office(SystemUser.questAdmin, sq);

		dailyQuestAdmin.init(dao);

		Key[] questKeys = dailyQuestAdmin.getQuestKeys();

		Iterable<User> userItr = dao.query(User.class).prepare().asIterable();
		for (User user : userItr) {
			if (user.getType() != UserPb.Type.PENDING_VALUE) {
				user.getQuota().setUsedDailyQuestNum(0);
				user.setGoldBalance(user.getGoldBalance() + 10);
				for (Key qKey : questKeys) {
					user.addActivity(KeyFactory.keyToString(qKey),
							Applicant.Status.ASSIGN);
				}
				dao.save(user);
			}
		}
	}

}
