package com.gm.server.model;

import java.util.ArrayList;
import java.util.List;

import com.gm.common.model.Rpc.QuestPb;
import com.google.appengine.api.datastore.Key;

public class Office {
	SystemUser admin;
	List<SystemQuest> quests;

	public Office(SystemUser admin, SystemQuest[] systemQuests) {
		this.admin = admin;
	
		if (systemQuests != null) {
			quests = new ArrayList<SystemQuest>();
			for (SystemQuest sq : systemQuests) {
				quests.add(sq);
			}
		}

	}

	public void init(DAO dao) {
		User qa = dao.querySingle("phone", admin.getPhone(), User.class);
		if (qa == null) {
			qa = new User(admin.getPhone(), "123", "321");
			qa.setName(admin.getName());
			qa.setGoldBalance(99999999);
			dao.save(qa);

		}
		if (admin.getKey() != qa.getEntityKey()) {
			admin.setKey(qa.getEntityKey());
		}
		Key adminKey = qa.getEntityKey();
		if (quests != null) {
			for (SystemQuest sQuest : quests) {
				String title = sQuest.getTitle();
				Quest quest = dao.querySingle("title", title, Quest.class,
						adminKey);
				if (quest == null) {
					quest = new Quest(sQuest.getContent());
					dao.save(quest, adminKey);
				}
				sQuest.setKey(quest.getEntityKey());

			}
		}
	}

	public String getPhone() {
		return admin.getPhone();
	}

	public Key getAdminKey() {
		return admin.getKey();
	}

	public Key[] getQuestKeys() {
		if (quests == null) {
			return null;
		}
		Key[] questKeys = new Key[quests.size()];
		int i = 0;
		for (SystemQuest sq : quests) {
			questKeys[i] = sq.getKey();
		}
		return questKeys;
	}

	public Key getQuestByType(QuestPb.Type type) {
		if (quests == null) {
			return null;
		}

		for (SystemQuest sq : quests) {
			if (type == sq.getType()) {
				return sq.getKey();
			}
		}
		return null;
	}

}
