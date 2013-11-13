package com.gm.server.model;

import java.util.Date;

import com.gm.common.model.Rpc.Applicant;
import com.gm.common.model.Rpc.Config;
import com.gm.common.model.Rpc.Currency;
import com.gm.common.model.Rpc.QuestPb;
import com.google.appengine.api.datastore.Key;

public 	enum SystemQuest{
	
	Checkin("到此一游", 
			"点击\"到此一游\"，告诉好友你正在哪座城市",
			1,
			QuestPb.Type.CHECKIN),
			
	AddFriend("添加好友", 
			"点击\"添加好友\"，选择通讯录里的好友,就可以给他/她发送好友申请，一旦得到对方确认，就可以请他/她做任务了",
			1,
			QuestPb.Type.ADD_FRIEND),
	PostQuest("发布任务", 
			"点击\"发布\"，填写任务内容，奖励，选择发布对象，好友就能收到你的任务请求了，一旦好友接受任务，你随时可以给他/她发奖表示感谢",
			1,
			QuestPb.Type.POST_QUEST);

	private QuestPb content;
	private Key key;

	private SystemQuest(String title, String description, int prize, QuestPb.Type type
			) {
		Config.Builder config = Config.newBuilder().setAllowSharing(false)
				.setAutoAccept(false)
				.setAutoClaim(true)
				.setAutoConfirmAll(true)
				.setAutoReward(true)
				.setQuestType(type)
				.setSystemQuest(true);
		
		QuestPb.Builder msg = QuestPb.newBuilder().setTitle(title)
				.setDescription(description)
				.setReward(Currency.newBuilder().setGold(prize))
				.setConfig(config)
				.setStatus(QuestPb.Status.PUBLISHED);
				
		content = msg.build();
	
	}
	private SystemQuest(QuestPb msg){
		content = msg;
	}
	public String getTitle() {
		return content.getTitle();
	}

	public QuestPb getContent() {
		return content;
	}
	public void setContent(QuestPb content) {
		this.content = content;
	}
	public QuestPb.Type getType() {
		return content.getConfig().getQuestType();
	}

	public Key getKey() {
		return key;
	}
	public void setKey(Key key) {
		this.key = key;
	}
}