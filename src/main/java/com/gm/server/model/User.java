package com.gm.server.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.gm.common.model.Rpc.CheckinsPb;
import com.gm.common.model.Rpc.EntityLog;
import com.gm.common.model.Rpc.Friendship;
import com.gm.common.model.Rpc.GeoPoint;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Rpc.Quests;
import com.gm.common.model.Rpc.Rating;
import com.gm.common.model.Rpc.UserPb;
import com.gm.common.model.Rpc.Thumbnail;
import com.gm.common.model.Rpc.Friend;
import com.gm.common.model.Rpc.Friends;
import com.gm.common.model.Rpc.UsersPb.Builder;
import com.gm.common.model.Server.EntityKeys;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@Entity
public class User extends Persistable<User> {

  //public static final User _ = new User();

  public static final boolean existsByPhone(String phone) {
    return DAO.get().querySingle("phone", phone, User.class) != null;
  }
  
  @Property
  private String phone = "";

  @Property
  private String password = "";

  @Property
  private String secret = "";

  @Property
  private Date createTime ;

  @Property
  private Date lastLoginTime ;
  
  @Property
  private Friends.Builder friends;

  @Property
  private EntityKeys.Builder activities = EntityKeys.newBuilder();
 
  @Property
  private String deviceID = "";

  @Property
  private String name="";
  
  @Property
  private Thumbnail.Builder thumbnail = Thumbnail.newBuilder().setSmallUrl("/images/user-256.png").setLargeUrl("/images/user-512.png");
  
  @Property
  private Rating.Builder rating = Rating.newBuilder();
 
  @Property
  private GeoPt geo = new GeoPt(0,0) ;
  
  @Property
  private long goldBalance = 0;
  
  @Property
  private int experience = 0;
  
  @Property
  private com.gm.common.model.Rpc.Quests.Builder favouriteQuests = null;
  
  @Property
  private CheckinsPb.Builder mostCheckin = null;
  
	public int getExperience() {
    return experience;
  }

  public void setExperience(int experience) {
    this.experience = experience;
  }

  public Quests.Builder getFavouriteQuests() {
    return favouriteQuests;
  }

  public void setFavouriteQuests(Quests.Builder favouriteQuests) {
    this.favouriteQuests = favouriteQuests;
  }

  public CheckinsPb.Builder getMostCheckin() {
    return mostCheckin;
  }

  public void setMostCheckin(CheckinsPb.Builder mostCheckin) {
    this.mostCheckin = mostCheckin;
  }

  public void login(String secret) {
		this.secret = secret;
		lastLoginTime = new Date();
	}

	public long getGoldBalance() {
    return goldBalance;
  }

  public void setGoldBalance(long goldBalance) {
    this.goldBalance = goldBalance;
  }

  public String getPhone() {
		return phone;
	}

	public Friends.Builder getFriends() {
	  if(friends==null){
	    friends= Friends.newBuilder();
	  }
    return friends;
  }

  public void setFriends(Friends.Builder friends) {
    if(friends==null){
      friends = Friends.newBuilder();
    }
    this.friends = friends;
  }

  public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getKey() {
		return KeyFactory.keyToString(getEntityKey());
	}

	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
  
	public long getUserID() {
    return getEntityKey().getId();
  }


  public String getDeviceID() {
    return deviceID;
  }

  public void setDeviceID(String deviceID) {
    this.deviceID = deviceID;
  }
  
  

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Thumbnail.Builder getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(Thumbnail.Builder thumbnail) {
    this.thumbnail = thumbnail;
  }

  public Rating.Builder getRating() {
    return rating;
  }

  public void setRating(Rating.Builder rating) {
    this.rating = rating;
  }

  public GeoPt getGeo() {
    return geo;
  }

  public void setGeo(GeoPt geo) {
    this.geo = geo;
  }
  
  

  public EntityKeys.Builder getActivities() {
    return activities;
  }

  public void setActivities(EntityKeys.Builder activities) {
    this.activities = activities;
  }

  @Override
	public User touch() {
		return this;
	}

	public User(String mobileNumber, String password, String secret) {
		super();
		this.phone = mobileNumber;
		this.password = password;
		this.secret = secret;
		this.createTime = new Date();
		this.lastLoginTime = createTime;
		this.friends = Friends.newBuilder();
    this.favouriteQuests = Quests.newBuilder();
    this.mostCheckin = CheckinsPb.newBuilder();
		
		//TODO:for test only
		this.deviceID = "APA91bFWFxgXtR57p3Jj2umYFFV8-U1N9PKKLQydheMybhU_2DxdngHbuYijPRHc1Y2a9dLkhdu9pyLCNd61uRBn9d2i6dggDxjMSkADyAET6rHGCQ9PFQi7HAc_hIsRBA_Z4LAkUddPSH9NxTvIjJZe-ImYHpoNgA";
	}

	public User() {
		// TODO Auto-generated constructor stub
	  createTime = new Date();
	  lastLoginTime = new Date();
	  friends= Friends.newBuilder();
	  this.favouriteQuests = Quests.newBuilder();
	  this.mostCheckin = CheckinsPb.newBuilder();
	  
	}
	
  public void addFriend(long id, Friendship type) {
    // TODO Auto-generated method stub
    int i = findFriend(id);
    if(i != -1 ){
        updateFriends(i,type);
    }else{
      Friend.Builder newFriend = Friend.newBuilder().setFriendship(type).setId(id);
    
        if(type == Friendship.CONFIRMED){
          newFriend.setScore(0);
        }
        friends.addFriend(newFriend.build());
      }
  }
  public void updateFriends(int index, Friendship type) {
    // TODO Auto-generated method stub
    
        Friend.Builder f = friends.getFriendBuilder(index);
  
        if(type==f.getFriendship()){
            return;
        }
       
        if(type==Friendship.WAIT_MY_CONFIRM||type==Friendship.ADDED){
          switch (f.getFriendship()){
            case ADDED:
              type = Friendship.CONFIRMED;
              break;
            case WAIT_MY_CONFIRM:
              type = Friendship.CONFIRMED;
              break;
            case BLOCKED:
               if(type == Friendship.WAIT_MY_CONFIRM){
                  return;
               }
            case INVITED:
              if(type == Friendship.WAIT_MY_CONFIRM){
                  type = Friendship.CONFIRMED;
              }
              break;
            default:
            return;
         }
        }
        
        if(type==Friendship.CONFIRMED){
          f.setScore(0);
        }
      
        friends.setFriend(index, f.setFriendship(type).build());
      
  }
  private int findFriend(long id) {
    // TODO Auto-generated method stub
    if(friends==null) return -1;
    for (int i=0; i<friends.getFriendCount();i++){
      Friend f = friends.getFriend(i);
      if (f.getId()==id){
        return i;
      }
    }
    return -1;
  }

  public void addFriends(long[] friendIDs) {
    // TODO Auto-generated method stub
    for(int i=0;i<friendIDs.length;i++){
      int index = findFriend(friendIDs[i]);
      if(index!=-1){
        updateFriends(index,Friendship.ADDED);
      }else{
        friends.addFriend(Friend.newBuilder().setFriendship(Friendship.ADDED).setId(friendIDs[i]).build());
      }
      
    }
  }
  
  //return the index of the added key
  public int addActivity(String questKey){
    int i = findActivity(questKey);
    if(i==-1){
      activities.addKey(questKey);
      i=0;
    }
    return i;
  }
  
  
  private int findActivity(String questKey) {
    for(int i = 0; i< activities.getKeyCount(); i++){
      if (activities.getKey(i).equals(questKey)){
        return i;
      }
    }
    return -1;
  }

  public void deleteFriend(long l) {
    // TODO Auto-generated method stub
    int index = findFriend(l);
    if(index!=-1){
      friends.removeFriend(index);
    }
  }
  public void muteFriend(long l) {
    // TODO Auto-generated method stub
    int index = findFriend(l);
    if(index!=-1){
        updateFriends(index, Friendship.MUTED);
    }
  }
  public void blockFriend(long l) {
    // TODO Auto-generated method stub
    int index = findFriend(l);
    if(index!=-1){
      updateFriends(index,Friendship.BLOCKED);
    }else{
      friends.addFriend(Friend.newBuilder().setId(l).setFriendship(Friendship.BLOCKED));
    }
  }

  public UserPb.Builder getMSG(long id) {
    
    EntityLog log = EntityLog.newBuilder().setCreatedAt(createTime.getTime()).setUpdatedAt(lastLoginTime.getTime()).build();
    GeoPoint geopt = GeoPoint.newBuilder().setLatitude(geo.getLatitude()).setLongitude(geo.getLatitude()).build();
    
    UserPb.Builder msg = UserPb.newBuilder().setId(getId())
                                            .setLog(log)
                                            .setRating(rating)
                                            .setPhone(phone);
    
    if(id==this.getId()
        ||getFriendship(id)==Friendship.ADDED
        ||getFriendship(id)==Friendship.CONFIRMED
        ||getFriendship(id)==Friendship.STARED
        ||getFriendship(id)==Friendship.INVITED
        ||getFriendship(id)==Friendship.MUTED ){
        msg.setName(name)
           .setLocation(geopt)
           .setThumbnail(thumbnail);
    }else{
        msg.setName("-")
           .setThumbnail(Thumbnail.newBuilder().setSmallUrl("/images/user-256.png").setLargeUrl("/images/user-512.png"));
    }

    return msg;
  }
  
  public Friendship getFriendship(long id){
    int i = findFriend(id);
    if(i!=-1){
      
      return friends.getFriend(i).getFriendship();
      
    }else{
    //set default value for required fields:
    
     return Friendship.UNKNOWN;
    }
    
  }

  public long[] getFriendIds() {
    long ids[] = new long[friends.getFriendCount()];
    int i=0;
    for(Friend f : friends.getFriendList()){
      ids[i]=f.getId();
    }
    return ids;
  }

  public void deleteActivity(String key) {
    List<String> keys = activities.getKeyList();
    activities.clearKey();
    for(String k:keys){
      if(key.equals(k)) continue;
      activities.addKey(k);
    }
    
  }
  
  
}
