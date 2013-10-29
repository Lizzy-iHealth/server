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

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	// Input: key: user key
	// id :quest id,
	// user_id:applicant

	public void handle(HttpServletRequest req, HttpServletResponse resp)
			throws ApiException, IOException {

		// get input

		Key userKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
		long id = ParamKey.id.getLong(req, -1);
		long receiverId[] = ParamKey.user_id.getLongs(req, -1);

		int results[] = new int[receiverId.length];
		for (int i : results) {
			i = -1;
		}

		Key questKey = KeyFactory.createKey(userKey, "Quest", id);
		Quest quest = dao.get(questKey, Quest.class);
		checkNotNull(quest, ErrorCode.quest_quest_not_found);

		long amount = quest.getPrize();
		int k = 0;
		for (long rId : receiverId) {
			int i = quest.findApplicant(rId);
			if (i != -1) {
				try{
				super.transferGold(userKey.getId(), rId, amount);
				results[k] = quest.updateApplicantStatus(i,
						Applicant.Status.REWARDED).getNumber();
				dao.save(quest);
				}catch(ApiException e){
					e.printStackTrace();
					
				}

			}
			k++;
		}
		// response is the status of the applicant

		super.writeResponse(resp, results);

	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		execute(req, resp);
	}
}
