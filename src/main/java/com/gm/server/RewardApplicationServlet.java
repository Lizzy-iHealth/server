package com.gm.server;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Applicant;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.Quest;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class RewardApplicationServlet extends APIServlet {

	private static final long serialVersionUID = 1L;

	// Input: key: user key
	// id :quest id,
	// user_id:applicant

	public void handle(HttpServletRequest req, HttpServletResponse resp)
			throws ApiException, IOException {

		// get input

		Key userKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
		long id = ParamKey.id.getLong(req, -1);
		long receiverIds[] = ParamKey.user_id.getLongs(req, -1);
		Key questKey = KeyFactory.createKey(userKey, "Quest", id);
		int[] results = rewardApplications(userKey, receiverIds, questKey);

		// response is the status of the applicant
		super.writeResponse(resp, results);

	}

	protected int[] rewardApplications(Key senderKey, long[] receiverIds,
			Key questKey) throws ApiException, IOException {
		Quest quest = dao.get(questKey, Quest.class);
		checkNotNull(quest, ErrorCode.quest_quest_not_found);
		int results[] = new int[receiverIds.length];

		int k = 0;
		for (long rId : receiverIds) {
			try {
				results[k] = super.rewardUser(senderKey, rId, quest);
				increaseFriendshipScore(senderKey.getId(), rId);
				increaseFriendshipScore(rId, senderKey.getId());
			} catch (ApiException e) {
				e.printStackTrace();

			}
			k++;
		}
		return results;
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		execute(req, resp);
	}
}
