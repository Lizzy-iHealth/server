package com.gm.server.model;

import java.util.Date;

import com.gm.common.model.Rpc.Friendship;
import com.gm.common.model.Server.Friend;
import com.gm.common.model.Server.Friends;
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
  private String deviceID = "";
  
	public void login(String secret) {
		this.secret = secret;
		lastLoginTime = new Date();
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
	}

	public User() {
		// TODO Auto-generated constructor stub
	  createTime = new Date();
	  lastLoginTime = new Date();
	  friends= Friends.newBuilder();
	}
	
  public void addFriend(long id, Friendship type) {
    // TODO Auto-generated method stub
    int i = findFriend(id);
    if(i != -1 ){
        updateFriends(i,type);
    }else{
        friends.addFriend(Friend.newBuilder().setFriendship(type).setId(id).build());
      }
  }
  public void updateFriends(int index, Friendship type) {
    // TODO Auto-generated method stub
    
        Friend f = friends.getFriend(index);
  
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
      
        friends.setFriend(index, f.toBuilder().setFriendship(type).build());
      
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
      friends.getFriendBuilder(index).setFriendship(Friendship.BLOCKED).build();
    }
  }
 
}
