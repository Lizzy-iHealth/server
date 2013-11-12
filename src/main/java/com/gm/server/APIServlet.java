package com.gm.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.gm.common.crypto.Hmac;
import com.gm.common.model.Rpc.Applicant;
import com.gm.common.model.Rpc.Applicant.Status;
import com.gm.common.model.Rpc.Applicants;
import com.gm.common.model.Rpc.Config;
import com.gm.common.model.Rpc.Currency;
import com.gm.common.model.Rpc.Friendship;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Rpc.Quests;
import com.gm.common.model.Rpc.TransactionPb;
import com.gm.common.model.Rpc.TransactionsPb;
import com.gm.common.model.Rpc.TransactionsPb.Builder;
import com.gm.common.model.Rpc.UserPb;
import com.gm.common.model.Rpc.UsersPb;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.DAO;
import com.gm.server.model.Feed;
import com.gm.server.model.Office;
import com.gm.server.model.PendingUser;
import com.gm.server.model.Quest;
import com.gm.server.model.Token;
import com.gm.server.model.TransactionRecord;
import com.gm.server.model.User;
import com.gm.server.push.Pusher;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gm.common.crypto.Base64;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import com.gm.common.model.Rpc.Friend;
import com.gm.common.model.Rpc.Friends;
import com.gm.common.model.Server.FeedPb;
import com.gm.common.model.Server.Feeds;

public abstract class APIServlet extends HttpServlet {

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	public static final DAO dao = DAO.get();
	public boolean requiresHmac = true;

	private static Queue queue = QueueFactory.getDefaultQueue();

	protected static Office questAdmin = new Office("999");
	protected static Office bank = new Office("8");
	protected static boolean ready = false;

	public APIServlet() {
		if (!ready)
			init();
	}

	public void init() {
		// init data store:
		String dailyQuestURL = "http://help-hand.appspot.com/quest/SubmitDailyQuestServlet";
		try {
			initBank();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		User taskAdmin;
		try {
			taskAdmin = initTaskAdmin();
			createDailyQuest("Check In",
					"When you check in, your friends will see your location.",
					dailyQuestURL, taskAdmin);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// init static variables:
		if (questAdmin.getAdminKey() == null
				|| questAdmin.getQuestKeys().length == 0) {
			questAdmin.init(dao);
		}
		if (bank.getAdminKey() == null) {
			bank.init(dao);
		}
		ready = true;

	}

	protected int applyQuest(Key questKey, Key applierKey, Applicant applicant)
			throws ApiException, IOException {
		Applicant newApplicant = applicant.toBuilder()
				.setUserId(applierKey.getId())
				.setType(Applicant.Status.WAIT_MY_CONFIRM).build();

		// get quest from datastore and add an application
		Quest quest = checkNotNull(dao.get(questKey, Quest.class),
				ErrorCode.quest_quest_not_found);

		// check the quest hasn't reach a deal
		check(!quest.isDeal(), ErrorCode.quest_is_deal);

		int status = quest.addApplicant(newApplicant); // add at the end
		User applier = dao.get(applierKey, User.class);
		applier.addActivity(KeyFactory.keyToString(questKey));
		dao.save(applier);
		dao.save(quest);
		deleteFeed(applier.getId(), quest.getId(), quest.getParent().getId());
		// TODO: delete feed

		// return the status of the applicant.

		// push message to quest owner.
		long[] receivers = { quest.getParent().getId() };
		push(receivers, "type", "quest");
		push(applier.getId(), "type", "activity");
		return status;
	}
	
	
	protected int acceptQuest(Key questKey, Key applierKey)
			throws ApiException, IOException {
		
		// get quest from datastore and add an application
		Quest quest = checkNotNull(dao.get(questKey, Quest.class),
				ErrorCode.quest_quest_not_found);
		
		// check the quest hasn't reach a deal
		check(!quest.isDeal(), ErrorCode.quest_is_deal);
		int status = updateActivityStatus(quest,applierKey.getId(), Applicant.Status.CONFIRMED).getNumber();
		dao.save(quest);
		if(!quest.isSystemQuest()){
			push(quest.getParent().getId(), "type", "quest");
		}
		return status;
	}
	
	protected String[] login(String phone, String password) throws ApiException {
		User user = checkNotNull(dao.querySingle("phone", phone, User.class),
				ErrorCode.auth_phone_not_registered);
		check(password.equals(user.getPassword()),
				ErrorCode.auth_incorrect_password);

		String secret = UUID.randomUUID().toString();

		user.login(secret);

		// initLoginUser(user);
		dao.save(user);
		String results[] = { user.getKey(), secret, Long.toString(user.getId()) };
		return results;
	}

	protected static void initUser(User user) throws ApiException, IOException {
		// assign a checkin quest

		if (questAdmin.getAdminKey() == null
				|| questAdmin.getQuestKeys().length == 0) {
			questAdmin.init(dao);
		}
		for (Quest quest : getQuests(questAdmin.getAdminKey())) {
			
			if(quest != null){
					
			assignQuest(quest, user, false);
			}
		}

	}

	private static List<Quest> getQuests(Key userKey ) {
		// TODO Auto-generated method stub
		return dao.query(Quest.class).setAncestor(userKey).prepare().asList();
	}

	protected static void deleteActivity(long[] applierIds, Key entityKey)
			throws ApiException, IOException {
		// TODO Auto-generated method stub
		ApiException error = null;
		for (long id : applierIds) {
			User applier = null;
			try {
				applier = checkNotNull(
						dao.get(KeyFactory.createKey("User", id), User.class),
						ErrorCode.quest_receiver_not_found);
			} catch (ApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error = e;
				continue;
			}

			applier.deleteActivity(KeyFactory.keyToString(entityKey));
			dao.save(applier);
			push(id, "type", "activity");
		}

		if (error != null)
			throw error;
	}

	protected Applicant getApplicant(HttpServletRequest req)
			throws InvalidProtocolBufferException {
		String questString = ParamKey.applicant.getValue(req);

		Applicant app = Applicant.parseFrom(Base64.decode(questString,
				Base64.DEFAULT));

		return app;
	}

	protected void deleteFeed(long[] receiverIds, long questId, long ownerId) {

		if (receiverIds == null || receiverIds.length == 0)
			return;
		TaskOptions task = withUrl("/queue/delete_feed").method(
				TaskOptions.Method.POST);
		for (long id : receiverIds) {
			task.param("user_id", Long.toString(id));
		}
		task.param("id", Long.toString(questId));
		task.param(ParamKey.owner_id.name(), Long.toString(ownerId));
		queue.add(task);

	}

	/*
	 * protected static void push(long[] ids, String data_key, String
	 * data_value) throws IOException { if (ids == null || ids.length == 0)
	 * return; String[] device_ids = new String[ids.length]; int i = 0; for
	 * (long id : ids) { device_ids[i] = dao.get(KeyFactory.createKey("User",
	 * id), User.class).getDeviceID(); i++; }
	 * 
	 * Map<String, String> data = new HashMap<String, String>();
	 * data.put(data_key, data_value);
	 * 
	 * try { new Pusher(device_ids).push(data); } catch (JSONException e) { //
	 * TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * }
	 */
	protected static void push(long[] ids, String data_key, String data_value)
			throws IOException {
		if (ids == null || ids.length == 0)
			return;

		TaskOptions task = withUrl("/queue/push").method(
				TaskOptions.Method.POST);
		task.param("data_key", data_key).param("data_value", data_value);
		for (long id : ids) {
			User user = dao.get(KeyFactory.createKey("User", id),
					User.class);
			if(user!=null){
			String device_id = user.getDeviceID();
			task.param("device_id", device_id);
			}
		}
		queue.add(task);
	}

	protected static void push(long id, String data_key, String data_value)
			throws IOException {

		TaskOptions task = withUrl("/queue/push").method(
				TaskOptions.Method.POST);
		task.param("data_key", data_key).param("data_value", data_value);

		User user = dao.get(KeyFactory.createKey("User", id),
				User.class);
		if(user!=null){
		String device_id = user.getDeviceID();
		task.param("device_id", device_id);
		
		queue.add(task);
		}
	}

	/*
	 * protected static void push(long id, String data_key, String data_value)
	 * throws IOException {
	 * 
	 * String device_id = dao .get(KeyFactory.createKey("User", id), User.class)
	 * .getDeviceID();
	 * 
	 * Map<String, String> data = new HashMap<String, String>();
	 * data.put(data_key, data_value);
	 * 
	 * try { new Pusher(device_id).push(data); } catch (JSONException e) { //
	 * TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * }
	 */
	protected static void generateFeed(long[] receiverIds,
			QuestPb.Builder questMsg, String pushMsg) {
		// TODO Auto-generated method stub

		if (receiverIds == null || receiverIds.length == 0)
			return;
		TaskOptions task = withUrl("/queue/generate_feed").method(
				TaskOptions.Method.POST);
		for (long id : receiverIds) {
			task.param("user_id", Long.toString(id));
		}
		task.param("quest",
				(Base64.encode(questMsg.build().toByteArray(), Base64.DEFAULT)));
		task.param("message", pushMsg);

		queue.add(task);

	}

	protected static void transferGold(long senderId, long receiverId,
			long amount) throws ApiException {
		Key senderKey = KeyFactory.createKey("User", senderId);
		Key receiverKey = KeyFactory.createKey("User", receiverId);
		TransactionOptions option = TransactionOptions.Builder.withXG(true);
		/*
		 * Transaction txn = dao.datastore.beginTransaction(option); try{
		 */
		User sender = dao.get(senderKey, User.class);
		User receiver = dao.get(receiverKey, User.class);

		long beforeSend = sender.getGoldBalance();
		long beforeReceive = receiver.getGoldBalance();
		long afterSend = beforeSend - amount;
		check(!(afterSend < 0), ErrorCode.currency_less_than_zero);
		long afterReceive = beforeReceive + amount;
		sender.setGoldBalance(afterSend);
		receiver.setGoldBalance(afterReceive);
		saveRecord(sender, receiver, amount);
		dao.save(sender);
		dao.save(receiver);
		/*
		 * txn.commit(); }finally{ if(txn.isActive()){ txn.rollback(); } }
		 */

	}

	protected static TransactionsPb getTransactionHistory(long id) {
		TransactionsPb.Builder records = TransactionsPb.newBuilder();
		Key ownerKey = KeyFactory.createKey("User", id);
		int maxNo = 20;

		// get up to 20 the latest transaction records.
		FetchOptions option = FetchOptions.Builder.withLimit(maxNo);
		List<TransactionRecord> recordList = dao.query(TransactionRecord.class)
				.setAncestor(ownerKey).sortBy("time", true).prepare()
				.asList(option);

		for (TransactionRecord record : recordList) {
			records.addRecord(record.getRecordPb());
		}

		return records.build();
	}

	private static void saveRecord(User sender, User receiver, long amount) {
		TransactionRecord record = new TransactionRecord(sender.getId(),
				receiver.getId(), Currency.newBuilder().setGold(amount));
		dao.create(record, sender.getEntityKey());
		dao.create(record, receiver.getEntityKey());

	}

	protected static void rejectApplication(Key questKey, long toReject)
			throws ApiException, IOException {
		Quest quest = checkNotNull(dao.get(questKey, Quest.class),
				ErrorCode.quest_quest_not_found);
		updateActivityStatus(quest,toReject,Applicant.Status.REJECTTED);
	
		/*
		 * User applier = checkNotNull( dao.get(KeyFactory.createKey("User",
		 * toReject), User.class), ErrorCode.quest_applicant_not_found);
		 * applier.deleteActivity(KeyFactory.keyToString(questKey));
		 * dao.save(applier);
		 */
		dao.save(quest);

		// push message to quest owner.
		long[] receivers = { toReject };
		push(receivers, "type", "activity");
	}

	protected int rejectAssignment(Key questKey, Key applierKey)
			throws ApiException, IOException {
		Quest quest = checkNotNull(dao.get(questKey, Quest.class),
				ErrorCode.quest_quest_not_found);

		/*
		int index = quest.findApplicant(applierKey.getId());
		check(index != -1, ErrorCode.quest_applicant_not_found);

		if (quest.isAutoReward()
				&& quest.getApplicants().getApplicant(index).getType() == Applicant.Status.ASSIGN) {
			returnGold(quest, quest.getPrize());
		}
		*/
		int status = quest.updateApplicantStatus(applierKey.getId(),
				Applicant.Status.REJECTTED).getNumber();

		dao.save(quest);

		// delete the activity at the applier's view.
		/*
		 * User applier = dao.get(applierKey, User.class);
		 * applier.deleteActivity(KeyFactory.keyToString(questKey));
		 * dao.save(applier);
		 */
		// push message to quest owner.
		long[] receivers = { quest.getParent().getId() };
		push(receivers, "type", "quest");
		return status;
	}

	private void returnGold(Quest quest, long prize) throws ApiException {
		transferGold(bank.getAdminKey().getId(), quest.getParent().getId(),
				prize);

	}

	protected static void rewardInvitors(long newUserID, Friends friends)
			throws ApiException {
		// TODO Auto-generated method stub

		for (Friend invitor : friends.getFriendList()) {
			Key invKey = KeyFactory.createKey("User", invitor.getId());

			User inv = null;
			try {
				inv = checkNotNull(dao.get(invKey, User.class),
						ErrorCode.auth_user_not_registered);
			} catch (ApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			inv.addFriend(newUserID, Friendship.INVITED);
			dao.save(inv);
		}

	}

	protected void updateQuestByMsg(QuestPb questMsg, Key ownerKey, Key questKey)
			throws ApiException, IOException {
		// save quest and post record to DB
		Quest quest = checkNotNull(dao.get(questKey, Quest.class),
				ErrorCode.quest_quest_not_found);
		quest.updateQuest(questMsg);
		dao.save(quest, ownerKey);

		HashSet<Long> receiversSet = quest.getAllReceiversIdsSet();
		HashSet<Long> applicantsSet = quest.getAllApplicantsIdsSet();
		receiversSet.removeAll(applicantsSet);
		long applicants[] = getLongs(applicantsSet.toArray());
		long diff[] = getLongs(receiversSet.toArray());
		if (applicantsSet.size() > 0) {
			// notify user some quest is updated with the quest key.
			push(applicants, "type", "activity");

			// option 2 implementation:
			// also update related feeds: receivers - applicants
		}

		if (diff.length > 0) {
			QuestPb.Builder questFeed = quest.getMSG();

			generateFeed(diff, questFeed, "update");
		}

	}

	protected Status processApplicantStatus(Quest quest, long applicantId,
			Applicant.Status status) throws ApiException, IOException {

	    status = updateActivityStatus(quest, applicantId, status);

		// claimed + autoReward + transferBalance = rewarded
		if (status == Applicant.Status.CLAIMED && quest.isAutoReward()) {

			try {
				transferGold(quest.getParent().getId(), applicantId,
						quest.getPrize());

				status = Applicant.Status.REWARDED;
				status = updateActivityStatus(quest,applicantId,Applicant.Status.REWARDED);
		
			} catch (ApiException e) {
				if (e.error == ErrorCode.currency_less_than_zero)
					;
				else {
					throw e;
				}
			}
		}

		return status;
	}

	protected static Applicant.Status updateActivityStatus(Quest quest,
			long applicantId, Applicant.Status status) {
		if(quest.isSystemQuest()){
			User user = dao.get(KeyFactory.createKey("User", applicantId), User.class);
			status = user.updateActivityStatus(quest.getEntityKey(),status);
			dao.save(user);
		}else{
			status = quest.updateApplicantStatus(applicantId, status);
			dao.save(quest);
		}
		return status;
	}

	protected void increaseFriendshipScore(long userId, long friendId) {
		// TODO Auto-generated method stub
		User owner = dao.get(KeyFactory.createKey("User", userId), User.class);

		owner.increaseFriendshipScore(friendId);

		dao.save(owner);

	}

	protected static long getId(String key) {
		// TODO Auto-generated method stub
		return KeyFactory.stringToKey(key).getId();
	}

	static void check(boolean condition, int error) throws ApiException {
		if (!condition)
			throw new ApiException(error);
	}

	static String stringNotEmpty(String string, int error) throws ApiException {
		check(!Strings.isNullOrEmpty(string), error);
		return string;
	}

	static <T> T checkNotNull(T reference, int error) throws ApiException {
		if (reference == null)
			throw new ApiException(error);
		return reference;
	}

	/**
	 * Each API enum item will implement this method, handling individual
	 * business logic
	 * 
	 * @param req
	 * @param resp
	 * @throws ApiException
	 * @throws IOException
	 */
	public abstract void handle(HttpServletRequest req, HttpServletResponse resp)
			throws ApiException, IOException;

	/**
	 * The common logic for all HTTP servlets.
	 * 
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	void execute(HttpServletRequest req, HttpServletResponse resp,
			boolean withHmac) throws IOException {
		if (withHmac) {
			execute(req, resp);
		} else {
			try {
				handle(req, resp);
				resp.setStatus(HttpServletResponse.SC_OK); // API executed
															// successfully
			} catch (ApiException e) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // API
																	// failed on
																	// validation
				resp.getOutputStream().write(
						Integer.toString(e.error).getBytes());
				info(e, "API error %s", e.getMessage()); // TODO:delete
			} catch (Exception e) {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Unknown
																				// error
				info(e, "unknow API error %s", e.getMessage());
				e.printStackTrace(new PrintWriter(resp.getOutputStream())); // TODO:
																			// remove
																			// me
																			// when
																			// release

			}
		}

	}

	public void execute(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			if (requiresHmac) {
				String key = stringNotEmpty(ParamKey.key.getValue(req),
						ErrorCode.auth_invalid_key_or_secret);
				String hmac = stringNotEmpty(ParamKey.hmac.getValue(req),
						ErrorCode.auth_invalid_key_or_secret);
				String body = new String(readStream(req.getInputStream()));

				info("key = %s, hmac = %s, body = %s", key, hmac, body);

				// |------------ body -------------|
				// ........&key=....&hmac=..........
				// |--- message ---||-- hmacPart --|

				String hmacPart = ParamKey.hmac.name() + "="
						+ URLEncoder.encode(hmac, "UTF-8");
				int index = body.indexOf(hmacPart);
				check(index > 1, ErrorCode.auth_invalid_key_or_secret);
				String message = body.substring(0, index - 1); // get rid of
																// last '&'

				User user = checkNotNull(
						dao.get(KeyFactory.stringToKey(key), User.class),
						ErrorCode.auth_invalid_key_or_secret);
				String match = Hmac.generate(message, user.getSecret());

				info("match = %s", match);
				check(hmac.equals(match), ErrorCode.auth_invalid_key_or_secret);

				// TODO: need test
			}

			handle(req, resp);
			resp.setStatus(HttpServletResponse.SC_OK); // API executed
														// successfully
		} catch (ApiException e) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // API failed on
																// validation
			info("api error with code = %d", e.error);
			info(e, "unknow API error %s", e.getMessage());
			resp.getOutputStream().write(Integer.toString(e.error).getBytes());
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Unknown
																			// error
			info(e, "unknow API error %s", e.getMessage());
			e.printStackTrace(new PrintWriter(resp.getOutputStream())); // TODO:
																		// remove
																		// me
																		// when
																		// release

		}
	}

	protected static void postExistedQuest(Key ownerKey, long[] receiverIds,
			Quest quest) {
		quest.addPost(ownerKey.getId(), receiverIds); // add at the end

		// if only one receiver, add him as the pre-confirmed applicant
		// once he apply for the quest, he will be automatically confirmed, and
		// the
		// quest change to "deal"
		/*
		 * if(receiverIds.length==1){ // add applicants: long id =
		 * receiverIds[0]; Applicant applicant = Applicant.newBuilder()
		 * .setUserId(id)
		 * .setBid(Currency.newBuilder().setGold(quest.getPrize()))
		 * .setType(Applicant.Status.ASSIGN).build();
		 * quest.addApplicant(applicant); User receiver =
		 * dao.get(KeyFactory.createKey("User", id), User.class);
		 * receiver.addActivity(KeyFactory.keyToString(quest.getEntityKey()));
		 * dao.save(receiver); }
		 */

		quest.setStatus(QuestPb.Status.PUBLISHED);
		dao.save(quest);

		// TODO: filter the receivers with friend lists, only allow friends as
		// receivers
		// prepare feed
		QuestPb.Builder questFeed = quest.getMSG();
		// if(receiverIds.length==1){
		// generateFeed(receiverIds,questFeed,"assign");
		// }else{
		generateFeed(receiverIds, questFeed, "post");
		// }
	}

	protected static User assignQuest(Quest quest, long id, boolean deleteFeed)
			throws ApiException, IOException {

		Key applierKey = KeyFactory.createKey("User", id);
		User applier = checkNotNull(dao.get(applierKey, User.class),
				ErrorCode.quest_receiver_not_found);

		return assignQuest(quest, applier, deleteFeed);
	}

	protected static User assignQuest(Quest quest, User applier,
			boolean deleteFeed) throws ApiException, IOException {

		/*
		 * if (quest.isAutoReward()) { transferGold(quest.getParent().getId(),
		 * bank.getAdminKey().getId(), quest.getPrize()); }
		 */
		if(quest.isSystemQuest()){
			applier.addActivity(KeyFactory.keyToString(quest.getEntityKey()), Status.ASSIGN);
			dao.save(applier);
		}else{
		Applicant applicant = Applicant.newBuilder().setUserId(applier.getId())
				.setBid(Currency.newBuilder().setGold(quest.getPrize()))
				.setType(Applicant.Status.ASSIGN).build();
		quest.addApplicant(applicant);
		dao.save(quest);
		applier.addActivity(KeyFactory.keyToString(quest.getEntityKey()));
		dao.save(applier);
		}
		if (deleteFeed) {
			deleteFeed(applier.getId(), quest.getId(), quest.getParent()
					.getId());
			// push(applier.getId(),"type","feed");
		}
		
		return applier;
	}

	protected static void deleteFeed(long feedOwnerId, long questId,
			long questOwnerId) throws IOException {
		Key receiverKey = KeyFactory.createKey("User", feedOwnerId);
		Feed feed = dao.querySingle(Feed.class, receiverKey);
		if (feed != null) {

			int i = feed.findQuest(questId, questOwnerId);
			if (i != -1) {
				feed.deleteQuest(i);

				// System.out.println(feed.toString());
				dao.save(feed);
				push(feedOwnerId, "type", "feed");
			}
		}
	}

	protected static User createUser(String phone, String password)
			throws ApiException, IOException {
		String secret = UUID.randomUUID().toString();
		User user = new User(phone, password, secret);
		dao.create(user);
		PendingUser pu = dao.querySingle("phone", phone, PendingUser.class);

		if (pu != null) {
			user.setFriends(pu.getInvitors());
			rewardInvitors(user.getUserID(), pu.getInvitors().build());
			dao.delete(pu);
		}
		
		dao.save(user);
		return user;
	}

	protected static int[] inviteFriends(String key, String[] friendPhones)
			throws ApiException {
		long invitorId = getId(key);
//		String[] results = new String[friendPhones.length]; // "0" not our user,
															// "1"
															// already our user.
	
		int[] results = new int[friendPhones.length];//friend Status
		ArrayList<Long> toNotify = new ArrayList<Long>();
		User invitor = dao.get(key, User.class);

			
		int i = 0;

		for (String phone : friendPhones) {
			results[i] = Friendship.INVITED_VALUE;
			if (phone.equals(invitor.getId())) { // add oneself, response 0, do
				results[i] = Friendship.SELF_VALUE;									// nothing
				continue;
			}
			PendingUser pu = dao.querySingle("phone", phone, PendingUser.class);
			// already invited;
			if (pu != null) {
				pu.addInvitor(invitorId);
				dao.save(pu);
			} else {
				User temp = dao.querySingle("phone", phone, User.class);
				// already our user
				if (temp != null) {
					
					results[i] = addFriend(temp.getId(),invitor);
					toNotify.add(Long.valueOf(temp.getId()));
				
					/*
					results[i] = "1";
					temp.addFriend(invitorId, Friendship.WAIT_MY_CONFIRM);
					dao.save(temp);

					invitor.addFriend(temp.getEntityKey().getId(),
							Friendship.ADDED);
					dao.save(invitor);
					continue;
					*/
				} else {
					// newly invited user
					pu = new PendingUser(phone, invitorId);
					dao.create(pu);
				}
			}
			i++;
		}
		try {
			push(getLongs(toNotify.toArray()),"type","friend");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return results;
	}

	protected static void delete_friends(String key, long[] friendIDs)
			throws ApiException {
		long myId = getId(key);
		// KeyFactory.stringToKey(key).getId();
		User user = dao.get(key, User.class);

		Key friendKeys[] = new Key[friendIDs.length];

		ApiException error = null;

		for (int i = 0; i < friendIDs.length; i++) {
			friendKeys[i] = KeyFactory.createKey("User", friendIDs[i]);
			User friend;
			try {
				friend = checkNotNull(dao.get(friendKeys[i], User.class),
						ErrorCode.auth_user_not_registered);
			} catch (ApiException e) {
				error = e;
				continue;
			}
			user.deleteFriend(friendIDs[i]);
			friend.deleteFriend(myId);
			dao.save(friend);

		}
		dao.save(user);
		if (error != null) {
			throw error;
		}
	}

	protected static int[] addFriends(String key, long[] friendIDs)
			throws IOException, ApiException {
		long myId = getId(key);

		User user = dao.get(key, User.class);
		check(user.getQuota().getFriendNum() > user.getFriends()
				.getFriendCount(), ErrorCode.quota_friend_usedup);
		Key friendKeys[] = new Key[friendIDs.length];
		int results[] = new int[friendIDs.length];
		
		/* for pusher
		Map<String, String> notice = new HashMap<String, String>();
		notice.put("type", "friend");

		ArrayList<String> idsToNotify = new ArrayList<String>(friendIDs.length);
	*/
		int i = 0;
		for (long friendID:friendIDs) {
			results[i]=addFriend(friendID, user);
			i++;
		}

		dao.save(user);
		
		push(friendIDs,"type","friend");
		
		/*
		if (idsToNotify.size() > 0) {
			String[] device_ids = new String[idsToNotify.size()];
			idsToNotify.toArray(device_ids);
			try {
				new Pusher(device_ids).push(notice);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/

		return results;
	}

	protected static int  addFriend(long friendID, User user) throws ApiException
			 {
		check(user.getQuota().getFriendNum() > user.getFriends()
				.getFriendCount(), ErrorCode.quota_friend_usedup);
		Key friendKey= KeyFactory.createKey("User", friendID);

		User friend = null;
		try {
			check(friendKey != user.getEntityKey(),
					ErrorCode.social_add_self_friend);
		} catch (ApiException e) {
			return Friendship.SELF_VALUE;
		}
		;

		try {
			friend = checkNotNull(dao.get(friendKey, User.class),
					ErrorCode.auth_user_not_registered);
		} catch (ApiException e) {
			return Friendship.UNKNOWN_VALUE;
			
		}
		;
		user.addFriend(friendID, Friendship.ADDED);
		friend.addFriend(user.getId(), Friendship.WAIT_MY_CONFIRM);

		
		/* for pusher
		if (friend.getDeviceID() != null) {
			idsToNotify.add(friend.getDeviceID());
		}
		*/
		dao.save(friend);
		dao.save(user);
		return user.getFriendship(friendID).getNumber();
	}

	protected static UserPb.Builder getPhoneDetails(String key, String qPhone)
			throws ApiException {
		User user = dao.get(key, User.class);
		User qUser = checkNotNull(dao.querySingle("phone", qPhone, User.class),
				ErrorCode.auth_phone_not_registered);
		UserPb.Builder qUserMsg = qUser.getMSG(user.getId());
		qUserMsg.setFriendship(user.getFriendship(qUser.getId()));
		return qUserMsg;
	}

	protected static UsersPb.Builder getFriendsDetails(String key,
			long[] qUserIds) throws ApiException {
		User user = dao.get(key, User.class);
		UsersPb.Builder users = UsersPb.newBuilder();
		if (qUserIds == null) {
			qUserIds = user.getFriendIds();
		}
		/*
		 * HashSet<Long> friendset = new HashSet<Long>(); //used to verify the
		 * relationship with the requested id.
		 * 
		 * 
		 * for (Friend f : user.getFriends().getFriendList()){
		 * if(f.getFriendship()==Friendship.CONFIRMED
		 * ||f.getFriendship()==Friendship.WAIT_MY_CONFIRM
		 * ||f.getFriendship()==Friendship.STARED){
		 * friendset.add(Long.valueOf(f.getId())); } }
		 */

		ApiException err = null;
		for (long id : qUserIds) {
			// if(friendset.contains(id)){
			User friendUser;
			try {
				friendUser = checkNotNull(
						dao.get(KeyFactory.createKey("User", id), User.class),
						ErrorCode.social_user_not_found);
			} catch (ApiException e) {
				err = e;
				continue;
			}
			UserPb.Builder friend = friendUser.getMSG(user.getId());
			friend.setFriendship(user.getFriendship(id));
			users.addUser(friend);
		}

		if (err != null) {
			throw err;
		}
		return users;
	}

	/*
	 * protected int[] rewardApplications(Key senderKey, long[] receiverIds, Key
	 * questKey) throws ApiException, IOException { Quest quest =
	 * dao.get(questKey, Quest.class); checkNotNull(quest,
	 * ErrorCode.quest_quest_not_found); int results[] = new
	 * int[receiverIds.length];
	 * 
	 * long amount = quest.getPrize(); int k = 0; for (long rId : receiverIds) {
	 * try { transferGold(senderKey.getId(), rId, amount); results[k] =
	 * updateApplicantStatus(quest, rId, Applicant.Status.REWARDED).getNumber();
	 * } catch (ApiException e) { e.printStackTrace();
	 * 
	 * } k++; } return results; }
	 */
	protected int rewardUser(Key senderKey, long userId, Quest quest)
			throws ApiException, IOException {

		long amount = quest.getPrize();

		transferGold(senderKey.getId(), userId, amount);
		return processApplicantStatus(quest, userId, Applicant.Status.REWARDED)
				.getNumber();
	

	}

	protected static Quest saveDraftQuest(QuestPb questMsg, Key ownerKey)
			throws ApiException {
		Quest quest = null;
		// repeated save:
		if (questMsg.hasId() && questMsg.hasOwnerId()
				&& questMsg.getOwnerId() == ownerKey.getId()) {
			Key questKey = KeyFactory.createKey(ownerKey, "Quest",
					questMsg.getId());
			quest = checkNotNull(dao.get(questKey, Quest.class),
					ErrorCode.quest_quest_not_found);
			check(quest.isDraft(), ErrorCode.quest_not_draft);
			quest.updateQuest(questMsg);
		} else {
			// first time save
			quest = new Quest(questMsg);
		}

		quest.setStatus(QuestPb.Status.DRAFT);
		dao.save(quest, ownerKey);
		return quest;
	}

	protected static QuestPb getQuestPb(HttpServletRequest req)
			throws InvalidProtocolBufferException {
		String questString = ParamKey.quest.getValue(req);

		QuestPb questMsg = QuestPb.parseFrom(Base64.decode(questString,
				Base64.DEFAULT));
		return questMsg;
	}

	protected static List<Applicant> getApplicants(HttpServletRequest req)
			throws InvalidProtocolBufferException {
		String questString = ParamKey.applicants.getValue(req);

		Applicants apps = Applicants.parseFrom(Base64.decode(questString,
				Base64.DEFAULT));

		return apps.getApplicantList();
	}

	protected static Key getQuestKeyFromReq(HttpServletRequest req) {
		long questId = ParamKey.id.getLong(req, -1);
		long questOwnerId = ParamKey.owner_id.getLong(req, -1);
		Key questOwnerKey = KeyFactory.createKey("User", questOwnerId);
		Key questKey = KeyFactory.createKey(questOwnerKey, "Quest", questId);
		return questKey;
	}

	protected static byte[] readStream(InputStream in) throws IOException {
		byte[] buf = new byte[1024];
		int count = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		while ((count = in.read(buf)) != -1)
			out.write(buf, 0, count);
		return out.toByteArray();
	}

	protected static void writeResponse(HttpServletResponse resp,
			Object[] results) throws IOException {
		if (results == null)
			return;
		Joiner joiner = Joiner.on(",").skipNulls();
		System.out.println(joiner.join(results));
		resp.getOutputStream().write(joiner.join(results).getBytes());
	}

	static final void info(Throwable t, String msg, Object... args) {
		logger.log(Level.INFO, String.format(msg, args), t);
	}

	static final void info(String msg, Object... args) {
		logger.log(Level.INFO, String.format(msg, args));
	}

	private static long[] getLongs(Object[] array) {
		long[] la = new long[array.length];
		for (int i = 0; i < array.length; i++) {
			la[i] = ((Long) array[i]).longValue();
		}

		return la;
	}

	private static final Logger logger = Logger.getLogger(APIServlet.class
			.getName());

	public void writeResponse(HttpServletResponse resp, int[] results)
			throws IOException {
		String r[] = new String[results.length];
		int i = 0;
		for (int s : results) {
			r[i] = Integer.toString(s);
			i++;
		}
		writeResponse(resp, r);

	}

	public Quest createQuestEntity(Key owner) {
		Quest q = new Quest();
		dao.save(q, owner);
		return q;
	}

	public Feed createFeedEntity(Key owner) {
		Feed f = new Feed();
		dao.save(f, owner);
		return f;
	}

	public void getQuestQuota(Key userKey) throws ApiException {
		// TODO Auto-generated method stub
		User user = dao.get(userKey, User.class);
		int dailyUsed = user.getQuota().getUsedDailyQuestNum();
		int totalUsed = user.getQuota().getUsedQuestNum();
		check(user.getQuota().getDailyQuestNum() > dailyUsed,
				ErrorCode.quota_daily_quest_usedup);
		check(user.getQuota().getQuestNum() > totalUsed,
				ErrorCode.quota_total_quest_usedup);
		user.getQuota().setUsedDailyQuestNum(dailyUsed + 1);
		user.getQuota().setUsedQuestNum(totalUsed + 1);
		dao.save(user);
	}

	protected void returnTotalQuestQuota(Key ownerKey) throws ApiException {
		// TODO Auto-generated method stub
		User user = dao.get(ownerKey, User.class);
		int totalUsed = user.getQuota().getUsedQuestNum();
		user.getQuota().setUsedQuestNum(totalUsed - 1);
		dao.save(user);
	}

	protected void refreshDailyQuestQuota(Key ownerKey) throws ApiException {
		// TODO Auto-generated method stub
		User user = dao.get(ownerKey, User.class);
		int dailyUsed = user.getQuota().getUsedDailyQuestNum();
		user.getQuota().setUsedDailyQuestNum(dailyUsed + 1);
		dao.save(user);
	}

	protected void getDailyQuestQuota(Key userKey) throws ApiException {
		// TODO Auto-generated method stub
		User user = dao.get(userKey, User.class);
		int dailyUsed = user.getQuota().getUsedDailyQuestNum();
		check(user.getQuota().getDailyQuestNum() > dailyUsed,
				ErrorCode.quota_daily_quest_usedup);
		user.getQuota().setUsedDailyQuestNum(dailyUsed + 1);
		dao.save(user);
	}

	protected void createDailyQuest(String title, String description,
			String dailyQuestURL, User taskAdmin) {
		if (dao.querySingle("title", title, Quest.class) == null) {
			Config.Builder config = Config.newBuilder()
					.setSystemQuest(true)
					.setAllowSharing(false)
					.setAutoAccept(false).setAutoClaim(false)
					.setAutoConfirmAll(true).setAutoConfirmFirstApplicant(true)
					.setAutoReward(true).setFavourite(false);
			Quest quest = new Quest(title, description, 1, config,
					QuestPb.Status.PUBLISHED.getNumber());
			quest.setAttach_link(dailyQuestURL);
			dao.save(quest, taskAdmin.getEntityKey());

		}
	}

	protected User initTaskAdmin() throws ApiException, IOException {
		User taskAdmin = dao.querySingle("phone", "999", User.class);
		if (taskAdmin == null) {
			taskAdmin = createUser("999", "1234");
			taskAdmin.setName("Quest Admin");
			dao.save(taskAdmin);
		} else {
			if (taskAdmin.getGoldBalance() < 999999)
				taskAdmin.setGoldBalance(999999999);
			dao.save(taskAdmin);
		}
		return taskAdmin;
	}

	protected void initBank() throws ApiException, IOException {
		User bank = dao.querySingle("phone", "8", User.class);
		if (bank == null) {
			bank = createUser("8", "1234");
			bank.setName("Bank");
			dao.save(bank);
		}
	}
	
	/*
	private Applicant.Status updateApplicantStatus(Quest quest, long userId, Applicant.Status status){
		Applicant.Status result = null;
		if(quest.isSystemQuest()){
			
		}else{
			result = quest.updateApplicantStatus(userId, status);
		}
		return result;
	}
	*/
}
