package com.gm.server.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.gm.common.model.Rpc.Applicant;
import com.gm.common.model.Rpc.Applicant.Status;
import com.gm.common.model.Rpc.Config;
import com.gm.common.model.Rpc.Currency;
import com.gm.common.model.Rpc.EntityLog;
import com.gm.common.model.Rpc.GeoPoint;
import com.gm.common.model.Rpc.LifeSpan;
import com.gm.common.model.Rpc.PostRecordPb;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Rpc.Applicants;
import com.gm.common.model.Rpc.PostRecordsPb;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PostalAddress;

@Entity
public class Quest extends Persistable<Quest> {

	@Property
	private long status = QuestPb.Status.DRAFT_VALUE; // only accessible to
														// owner

	@Property
	private Applicants.Builder applicants = Applicants.newBuilder();

	@Property
	private Date start_time = new Date(); // set by user

	@Property
	private Date end_time;// = new Date();//set by user

	@Property
	private Date createAt = new Date(); // set by server

	@Property
	private Date updateAt = new Date();// = new Date();//set by server

	@Property
	private String title="";

	@Property
	private String address="";//=new PostalAddress("Input Address Here:");

	@Property
	private GeoPt geo_point;// =new GeoPt(0,0);

	@Property
	private long prize = 0; // at owner's view: <0 give reward, >0 collect
							// reward

	@Property
	private String description = "";

	@Property
	private String attach_link;// =new Link("http://help-hand.appspot.com");

	@Property
	private PostRecordsPb.Builder posts = PostRecordsPb.newBuilder();

	@Property
	private Config.Builder config = Config.newBuilder();

	public Config.Builder getConfig() {
		return config;
	}

	public void setConfig(Config.Builder config) {
		this.config = config;
	}

	public Applicants.Builder getApplicants() {
		return applicants;
	}

	public void setApplicants(Applicants.Builder applicants) {
		this.applicants = applicants;
	}

	public long getPrize() {
		return prize;
	}

	public void setPrize(long prize) {
		this.prize = prize;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isAllow_sharing() {
		return config.getAllowSharing();
	}

	public void setAllow_sharing(boolean allow_sharing) {
		config.setAllowSharing(allow_sharing);
	}

	public String getAttach_link() {
		return attach_link;
	}

	public void setAttach_link(String attach_link) {
		this.attach_link = attach_link;
	}

	public PostRecordsPb.Builder getPosts() {
		return posts;
	}

	public void setPosts(PostRecordsPb.Builder posts) {
		this.posts = posts;
	}

	public long getStatus() {
		return status;
	}

	public void setStatus(QuestPb.Status status) {
		this.status = status.getNumber();
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public boolean isAutoConfirm() {
		return config.getAutoConfirmAll();
	}

	public boolean isAutoAccept() {
		return config.getAutoAccept();
	}

	public boolean isAutoClaim() {
		return config.getAutoClaim();
	}

	public boolean isAutoReward() {
		return config.getAutoReward();
	}

	public void setAutoConfirm(boolean autoConfirm) {
		config.setAutoConfirmAll(autoConfirm);
	}
	
	public boolean isSystemQuest(){
		return config.getSystemQuest();
	}
	public Quest() {
	}

	public Quest(String title) {
		this.title = title;
	}

	public Quest(QuestPb q) {
		if (q.hasLifespan()) {
			if (q.getLifespan().hasCreateTime()) {
				start_time = new Date(q.getLifespan().getCreateTime());
			}
			if (q.getLifespan().hasDeleteTime()) {
				end_time = new Date(q.getLifespan().getDeleteTime());
			}else{
				end_time = new Date(start_time.getTime()+24*60*60*1000);
			}
		}
		if (q.hasTitle()) {
			title = q.getTitle();
		}
		if (q.hasAddress()) {
			address = q.getAddress();
		}
		if (q.hasGeoPoint()) {
			geo_point = new GeoPt(q.getGeoPoint().getLatitude(), q
					.getGeoPoint().getLongitude());
		}

		if (q.hasReward()) {
			prize = q.getReward().getGold();
		}
		if (q.hasDescription()) {
			description = q.getDescription();
		}
		if (q.hasConfig()) {
			config = q.getConfig().toBuilder();
		}
		if (q.hasUrl()) {
			attach_link = q.getUrl();
		}
		if (q.hasStatus()) {
			status = q.getStatus().getNumber();
		}

	}

	public Quest(String title, String description, int prize,
			Config.Builder config, int status) {
		this.title = title;
		this.description = description;
		start_time = new Date();
		this.config = config;
		this.prize = prize;
		this.status = status;
	}

	public QuestPb.Builder getMSG(long id) {
		QuestPb.Builder qMsg = getMSG();
		PostRecordsPb somePosts = findPostsById(id);
		if (somePosts != null) {
			qMsg.setPostRecords(somePosts);
		}

		if (id == qMsg.getOwnerId()) {
			qMsg.setApplicants(applicants);
			qMsg.setStatus(QuestPb.Status.valueOf((int) status));
		} else {

			// TODO: applicants can get their own application

			int i = findApplicant(id);
			if (i != -1) {
				qMsg.getApplicantsBuilder().addApplicant(
						applicants.getApplicant(i));
			}
		}

		return qMsg;
	}

	// return the index of the applicant in applicants message
	private int findApplicant(long id) {
		for (int i = 0; i < applicants.getApplicantCount(); i++) {
			if (id == applicants.getApplicant(i).getUserId()) {
				return i;
			}
		}
		return -1;
	}

	public void updateQuest(QuestPb q) {
		if (q.hasLifespan()) {
			if (q.getLifespan().hasCreateTime()) {
				start_time.setTime(q.getLifespan().getCreateTime());
			}
			if (q.getLifespan().hasDeleteTime()) {
				end_time.setTime(q.getLifespan().getDeleteTime());
			}
		}
		if (q.hasTitle()) {
			title = q.getTitle();
		}
		if (q.hasAddress()) {
			address = q.getAddress();
		}
		if (q.hasGeoPoint()) {
			geo_point = new GeoPt(q.getGeoPoint().getLatitude(), q
					.getGeoPoint().getLongitude());
		}
		if (q.hasReward()) {
			prize = q.getReward().getGold();
		}
		if (q.hasDescription()) {
			description = q.getDescription();
		}
		if (q.hasConfig()) {
			config = q.getConfig().toBuilder();
		}
		if (q.hasUrl()) {
			attach_link = q.getUrl();
		}
		if (q.hasStatus()) {
			status = q.getStatus().getNumber();
		}
		updateAt = new Date();
	}

	private PostRecordsPb findPostsById(long id) {
		PostRecordsPb.Builder someposts = PostRecordsPb.newBuilder();
		List<PostRecordPb> allposts = posts.getPostList();
		boolean found = false;
		for (PostRecordPb p : allposts) {
			if (p.getOwner() == id) {
				found = true;
				someposts.addPost(p);
			}
		}
		if (found) {
			return someposts.build();
		} else {
			return null;
		}

	}

	public QuestPb.Builder getMSG() {

		LifeSpan.Builder lifespan = LifeSpan.newBuilder();
		if (start_time != null)
			lifespan.setCreateTime(start_time.getTime());
		if (end_time != null)
			lifespan.setDeleteTime(end_time.getTime());

		EntityLog.Builder entitylog = EntityLog.newBuilder()
				.setCreatedAt(createAt.getTime())
				.setUpdatedAt(updateAt.getTime());

		GeoPoint.Builder gmsg = null;
		if (geo_point != null) {
			gmsg = GeoPoint.newBuilder().setLatitude(geo_point.getLatitude())
					.setLongitude(geo_point.getLongitude());
		}
		Currency reward = Currency.newBuilder().setGold(prize).build();
		QuestPb.Builder qMsg = QuestPb.newBuilder().setLifespan(lifespan)
				.setTitle(title).setReward(reward).setDescription(description)
				.setConfig(config).setLog(entitylog);

		if (entity != null) {
			qMsg.setId(entity.getKey().getId()).setOwnerId(
					entity.getParent().getId());
		}
		if (address != null) {
			qMsg.setAddress(address);
		}
		if (gmsg != null) {
			qMsg.setGeoPoint(gmsg);
		}
		if (attach_link != null) {
			qMsg.setUrl(attach_link);
		}

		return qMsg;
	}

	public Date getStart_time() {
		return start_time;
	}

	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}

	public Date getEnd_time() {
		return end_time;
	}

	public void setEnd_time(Date end_time) {
		this.end_time = end_time;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public GeoPt getGeo_point() {
		return geo_point;
	}

	public void setGeo_point(GeoPt geo_point) {
		this.geo_point = geo_point;
	}

	@Override
	public Quest touch() {
		// TODO Auto-generated method stub
		updateAt = new Date();
		return null;
	}

	public void addPost(long id, long[] receiverIds) {
		// TODO Auto-generated method stub
		PostRecordPb.Builder postrecord = PostRecordPb.newBuilder();
		for (int i = 0; i < receiverIds.length; i++) {
			postrecord.addAudience(receiverIds[i]);
		}
		postrecord.setOwner(id);
		postrecord.setTimestamp(new Date().getTime());
		posts.addPost(postrecord);
	}

	// can not guarantee the order of receivers.
	public long[] getAllReceiversIds() {
		HashSet<Long> receivers = getAllReceiversIdsSet();
		long ids[] = getLongs(receivers.toArray());
		return ids;
	}

	public HashSet<Long> getAllReceiversIdsSet() {
		HashSet<Long> receivers = new HashSet<Long>();

		// get all the audiences
		for (PostRecordPb post : posts.getPostList()) {
			for (Long id : post.getAudienceList()) {
				if (!receivers.contains(id)) {
					receivers.add(id);
				}
			}
		}
		return receivers;
	}

	// can not guarantee the order of applicants.
	public HashSet<Long> getAllApplicantsIdsSet() {
		HashSet<Long> receivers = new HashSet<Long>();

		// get all applicants
		for (Applicant applicant : applicants.getApplicantList()) {
			long id = applicant.getUserId();
			if (!receivers.contains(id)) {
				receivers.add(id);
			}
		}

		return receivers;
	}

	// can not guarantee the order of applicants.
	public long[] getAllApplicantsIds() {
		HashSet<Long> receivers = getAllApplicantsIdsSet();
		long ids[] = getLongs(receivers.toArray());
		return ids;

	}

	private long[] getLongs(Object[] array) {
		long[] la = new long[array.length];
		for (int i = 0; i < array.length; i++) {
			la[i] = ((Long) array[i]).longValue();
		}

		return la;
	}

	public int addApplicant(Applicant applicant) {
		// find applicant
		// if found, update
		// if not found , add at the end
		int i = findApplicant(applicant.getUserId());
		int appStatus = applicant.getType().getNumber();
		if (i != -1) {
			updateApplicant(i, applicant);
			appStatus = applicants.getApplicant(i).getType().getNumber();
		} else {
			applicants.addApplicant(applicant);
		}
		return appStatus;
	}

	public void updateApplicant(int i, Applicant app) {
		Applicant.Builder curApp = applicants.getApplicant(i).toBuilder();
		if (app.hasBid() && !isDeal()) {
			curApp.setBid(app.getBid());
		}
		if (app.hasType()) {
			Applicant.Status newType = app.getType();

			// Assign + Wait_My_Confirm = Confirmed
			if (curApp.hasType() && curApp.getType() != newType) {
				Applicant.Status curType = curApp.getType();
				if ((newType == Applicant.Status.ASSIGN && curType == Applicant.Status.WAIT_MY_CONFIRM)
						|| (newType == Applicant.Status.WAIT_MY_CONFIRM && curType == Applicant.Status.ASSIGN)) {
					newType = Applicant.Status.CONFIRMED;
				}

				// confirmed + autoAccept = pass
				if (newType == Applicant.Status.CONFIRMED
						&& this.isAutoAccept()) {
					newType = Applicant.Status.PASS;
				}

				// pass + autoClaim = claimed
				if (newType == Applicant.Status.PASS && this.isAutoClaim()) {
					newType = Applicant.Status.CLAIMED;
				}

				if (newType == Applicant.Status.DONE) {
					doneWithApplicant(i);
				}
			}
			curApp.setType(newType);
		}
		applicants.setApplicant(i, curApp.build());

	}

	private void doneWithApplicant(int i) {
		// TODO Auto-generated method stub

	}

	public boolean isDraft() {

		return status == QuestPb.Status.DRAFT_VALUE;
	}

	public boolean isDeal() {

		return status == QuestPb.Status.DEAL_VALUE;
	}

	public Applicant.Status updateApplicantStatus(int index, Status status) {

		Applicant.Builder newApp = applicants.getApplicantBuilder(index)
				.setType(status);
		updateApplicant(index, newApp.build());
		return applicants.getApplicantBuilder(index).getType();
	}
	public Applicant.Status updateApplicantStatus(long id, Status status) {
		int index = findApplicant(id);
		if(index!=-1){
		Applicant.Builder newApp = applicants.getApplicantBuilder(index)
				.setType(status);
		updateApplicant(index, newApp.build());
		return applicants.getApplicantBuilder(index).getType();
		}else{
			return Applicant.Status.REJECTTED;
		}
	}

	public void restart() {
		// TODO Auto-generated method stub
		Applicants.Builder newApps = Applicants.newBuilder();
		for (Applicant app : applicants.getApplicantList()) {
			newApps.addApplicant(app.toBuilder().setType(
					Applicant.Status.ASSIGN));
		}
		setApplicants(newApps);
	}

	public long[] getNoActionReceiversIds() {
		HashSet<Long> rSet = getAllReceiversIdsSet();
	    HashSet<Long> aSet = getAllApplicantsIdsSet();
	    rSet.removeAll(aSet);
	    return getLongs(rSet.toArray());

	}

}
