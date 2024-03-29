package com.gm.server;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Applicant;
import com.gm.common.model.Rpc.Applicants;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Rpc.Quests;
import com.gm.common.model.Server.Activity;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.Feed;
import com.gm.server.model.Quest;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class GetActivitiesServlet extends APIServlet {
	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		execute(req, resp);
	}

	// Input: key : the key of requester
	//
	// Output: Quests message, containing all the quests applied by or assigned
	// to me.

	public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException
			 {

		Key userKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
		User user = dao.get(userKey, User.class);
		List<Activity> activities = user.getActivities().getActivityList();
		Quests.Builder questsMsg = Quests.newBuilder();
		for (Activity activity : activities) {

			Key questKey = KeyFactory.stringToKey(activity.getKey());
			// get quest from datastore and add an application
			Quest quest = dao.get(questKey, Quest.class);
			if (quest == null) {
				user.deleteActivity(KeyFactory.keyToString(questKey));
				Feed feed = dao.querySingle(Feed.class, user.getEntityKey());
				if (feed != null) {
					feed.deleteQuest(questKey);
					dao.save(feed);
				}
				dao.save(user);

			} else {
				QuestPb.Builder qMsg = null;
				if(quest.isSystemQuest()){
					qMsg = quest.getMSG();
					Applicant.Builder app = Applicant.newBuilder()
							.setUserId(user.getId())
							.setType(user.getActivityStatus(quest.getEntityKey()));
					qMsg.setApplicants(Applicants.newBuilder().addApplicant(app));
				}else{
				 qMsg = quest.getMSG(user.getId());
				}
				questsMsg.addQuest(qMsg.build());
			}
		}
		resp.getOutputStream().write(questsMsg.build().toByteArray());

	}

}
