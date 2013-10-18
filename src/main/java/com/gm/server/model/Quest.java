package com.gm.server.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.gm.common.model.Rpc.Applicant;
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
  private Applicants.Builder applicants = Applicants.newBuilder();
  
  @Property
  private Date start_time ; //set by user

  @Property
  private Date end_time ;//= new Date();//set by user
  
  @Property
  private Date createAt = new Date(); //set by server

  @Property
  private Date updateAt=new Date();//= new Date();//set by server
  
  @Property
  private String title="";

  @Property
  private PostalAddress address;//=new PostalAddress("");

  @Property
  private GeoPt geo_point;//=new GeoPt(0,0);
  
  @Property
  private long prize=0; // at owner's view: <0 give reward, >0 collect reward
  
  @Property
  private String description="";
  
  @Property
  private boolean allow_sharing = false;
  @Property
  private Link attach_link;//=new Link("");
  
  @Property
  private PostRecordsPb.Builder posts=PostRecordsPb.newBuilder();
  
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
    return allow_sharing;
  }

  public void setAllow_sharing(boolean allow_sharing) {
    this.allow_sharing = allow_sharing;
  }

  public Link getAttach_link() {
    return attach_link;
  }

  public void setAttach_link(Link attach_link) {
    this.attach_link = attach_link;
  }

  public PostRecordsPb.Builder getPosts() {
    return posts;
  }

  public void setPosts(PostRecordsPb.Builder posts) {
    this.posts = posts;
  }


  
  public Quest(){
  }
  
  public Quest(String title){
    this.title = title;
  }

  public Quest(QuestPb q) {
    start_time = new Date(q.getLifespan().getCreateTime());
    end_time = new Date(q.getLifespan().getDeleteTime());
    title = q.getTitle();
    address = new PostalAddress(q.getAddress());
    geo_point = new GeoPt(q.getGeoPoint().getLatitude(),q.getGeoPoint().getLongitude());
    prize = q.getReward().getGold();
    description = q.getDescription();
    allow_sharing = q.getAllowSharing();
    attach_link = new Link(q.getUrl());
    
  }
 public QuestPb.Builder getMSG(long id){
    QuestPb.Builder qMsg = getMSG();
    PostRecordsPb somePosts= findPostsById(id);
    if(somePosts!=null){
      qMsg.setPostRecords(somePosts);
    }
    
    if(id==qMsg.getOwnerId()){
      qMsg.setApplicants(applicants);
    }else{
      
    //TODO: applicants can get their own application
      
      int i = findApplicant(id);
      if(i!=-1){
        qMsg.getApplicantsBuilder().addApplicant(applicants.getApplicant(i));
      }
    }
    
    return qMsg;
  }

 //return the index of the applicant in applicants message
 private int findApplicant(long id) {
  for(int i =0; i< applicants.getApplicantCount();i++){
    if(id == applicants.getApplicant(i).getUserId()){
      return i;
    }
  }
   return -1;
}

public void updateQuest(QuestPb q) {
  if(q.hasLifespan()){
    if(q.getLifespan().hasCreateTime()){
      start_time.setTime(q.getLifespan().getCreateTime());
    }
    if(q.getLifespan().hasDeleteTime()){
      end_time .setTime(q.getLifespan().getDeleteTime());
    }
  }
  if(q.hasTitle()){
   title = q.getTitle();
  }
  if(q.hasAddress()){
    address = new PostalAddress(q.getAddress());
  }
  if(q.hasGeoPoint()){
    geo_point = new GeoPt(q.getGeoPoint().getLatitude(),q.getGeoPoint().getLongitude());
  }
  if(q.hasReward()){
    prize = q.getReward().getGold();
  }
  if(q.hasDescription()){
    description = q.getDescription();
  }
  if(q.hasAllowSharing()){
   allow_sharing = q.getAllowSharing();
  }
  if(q.hasUrl()){
   attach_link = new Link(q.getUrl());
  }
   updateAt = new Date();
 }
 
  private PostRecordsPb findPostsById(long id) {
    PostRecordsPb.Builder someposts = PostRecordsPb.newBuilder();
    List<PostRecordPb> allposts = posts.getPostList();
    boolean found=false;
    for(PostRecordPb p : allposts){
      if(p.getOwner()==id){
        found = true;
        someposts.addPost(p);
      }
    }
    if(found){
      return someposts.build();
    }else{
      return null;
    }
    
  }

  public QuestPb.Builder getMSG(){
    LifeSpan.Builder lifespan = LifeSpan.newBuilder();
    if(start_time!=null)lifespan.setCreateTime(start_time.getTime());
    if(end_time!=null)lifespan.setDeleteTime(end_time.getTime());
    
    EntityLog.Builder entitylog = EntityLog.newBuilder().setCreatedAt(createAt.getTime()).setUpdatedAt(updateAt.getTime());
    
    GeoPoint.Builder gmsg=null;
    if(geo_point!=null){
       gmsg= GeoPoint.newBuilder().setLatitude(geo_point.getLatitude()).setLongitude(geo_point.getLongitude());
    }
    Currency reward = Currency.newBuilder().setGold(prize).build();
    QuestPb.Builder qMsg = QuestPb.newBuilder().setLifespan(lifespan)
        .setTitle(title).setReward(reward).setDescription(description)
        .setAllowSharing(allow_sharing).setLog(entitylog);
    
    if(entity!=null){
      qMsg.setId(entity.getKey().getId()).setOwnerId(entity.getParent().getId());
    }
    if(address!=null){
      qMsg.setAddress(address.getAddress());
    }
    if(gmsg!=null){
      qMsg.setGeoPoint(gmsg);
    }
    if(attach_link!=null){
      qMsg.setUrl(attach_link.getValue());
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

  public PostalAddress getAddress() {
    return address;
  }

  public void setAddress(PostalAddress address) {
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
    PostRecordPb.Builder postrecord= PostRecordPb.newBuilder();
    for(int i=0;i<receiverIds.length;i++){
      postrecord.addAudience(receiverIds[i]);
    }
    postrecord.setOwner(id);
    postrecord.setTimestamp(new Date().getTime());
    posts.addPost(postrecord);
  }

  
//can not guarantee the order of receivers.
  public long[] getAllReceiversIds() {
    HashSet <Long> receivers  = new HashSet<Long>();
    
    //get all the audiences
    for(PostRecordPb post : posts.getPostList()){
      for(Long id : post.getAudienceList()){
        if (!receivers.contains(id)){
          receivers.add(id);
        }
      }
    }
    long ids[] = getLongs(receivers.toArray());
    return  ids;
  }
  
  // can not guarantee the order of applicants.
  public long[] getAllApplicantsIds() {
    HashSet <Long> receivers  = new HashSet<Long>();
  
    //get all applicants
    for(Applicant applicant : applicants.getApplicantList()){
      long id = applicant.getUserId();
        if (!receivers.contains(id)){
          receivers.add(id);
        }
    }
    
    long ids[] = getLongs(receivers.toArray());
    
    return  ids;
  }

  private long[] getLongs(Object[] array) {
    long[] la = new long [array.length];
    for(int i=0; i<array.length;i++){
      la[i]  = ((Long)array[i]).longValue();
    }

    return la;
  }

  public void addApplicant(Applicant applicant) {
    //  find applicant
    // if found, update
    // if not found , add at the end
    int i = findApplicant(applicant.getUserId());
    if(i!=-1){
      applicants.setApplicant(i, applicant);
    }else{
    applicants.addApplicant(applicant);
    }
  }

  //only update type and bid
  public void updateApplicant(Applicant app) {

    if(!app.hasUserId()) {
      System.err.println("empty applicant id");
      return;
    }
    int i = findApplicant(app.getUserId());
    Applicant.Builder curApp = applicants.getApplicant(i).toBuilder();
    if(app.hasBid()){
      curApp.setBid(app.getBid());
    }
    if(app.hasType()){
      Applicant.Status newType = app.getType();
      
      
      if(curApp.hasType()&&curApp.getType()!=newType){
        Applicant.Status curType = curApp.getType();
        if((newType==Applicant.Status.ADDED && curType == Applicant.Status.WAIT_MY_CONFIRM )
            || (newType==Applicant.Status.WAIT_MY_CONFIRM && curType == Applicant.Status.ADDED )){
            newType = Applicant.Status.CONFIRMED;
        }
      }  
      curApp.setType(newType);
    }
    applicants.setApplicant(i, curApp.build());
    
  }
}

