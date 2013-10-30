package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.CheckinPb;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.server.model.CheckinRecord;
import com.gm.server.model.Quest;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class CheckinServlet extends APIServlet {
	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		execute(req, resp);
	}

	// Input Param: "key" user's index
	// "pb" Base64 encoded QuestPb object

	public void handle(HttpServletRequest req, HttpServletResponse resp)
			throws ApiException, IOException {

		CheckinPb checkinMsg = CheckinPb.parseFrom(ParamKey.pb.getPb(req));
		Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));

		// save quest and post record to DB
		CheckinRecord cr = new CheckinRecord(checkinMsg);
		dao.create(cr, ownerKey);
		if (checkinMsg.hasGeoPoint()) {
			User user = dao.get(ownerKey, User.class);
			user.checkin(checkinMsg);
			user.addExperience(1);
			dao.save(user);

			resp.getOutputStream().write(
					user.getMSG(user.getId()).build().toByteArray());
			info("new checkin record:" + cr.getParent().getId()
					+ cr.getEntityKey().getKind() + cr.getId() + "by "
					+ ownerKey.getId());
		}
		// push to receivers

	}
}
