package com.gm.server.model;

import java.util.Date;

import com.gm.server.model.Model.Friend;
import com.gm.server.model.Model.Friend.Type;
import com.gm.server.model.Model.Friendship;
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
  private Date createTime = new Date();

  @Property
  private Date lastLoginTime = new Date();
  
  @Property
  private Friendship.Builder friendship= Friendship.newBuilder();
  
  private int userID = 0;
  
  @Property
  private String deviceID = "";
  
	public void login(String secret) {
		this.secret = secret;
		lastLoginTime = new Date();
	}

	public String getPhone() {
		return phone;
	}

	public Friendship.Builder getFriendship() {
    return friendship;
  }

  public void setFriendship(Friendship.Builder friendship) {
    this.friendship = friendship;
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

  public void setUserID(int userID) {
    this.userID = userID;
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
	}

	public User() {
		// TODO Auto-generated constructor stub
	}
	
  public void addFriend(long id, Type type) {
    // TODO Auto-generated method stub
    int i = findFriend(id);
    if(i != -1 ){
        updateFriendship(i,type);
    }else{
        friendship.addFriend(Friend.newBuilder().setType(type).setId(id).build());
      }
  }
  public void updateFriendship(int index, Type type) {
    // TODO Auto-generated method stub
    
        Friend f = friendship.getFriend(index);
  
        if(type==f.getType()){
            return;
        }
        if(type==Type.WAIT_MY_CONFIRM||type==Type.ADDED){
          switch (f.getType()){
            case ADDED:
              type = Type.CONFIRMED;
              break;
            case WAIT_MY_CONFIRM:
              type = Type.CONFIRMED;
              break;
            case BLOCKED:
               if(type == Type.WAIT_MY_CONFIRM){
                  return;
               }
            case INVITED:
              if(type == Type.WAIT_MY_CONFIRM){
                  type = Type.CONFIRMED;
              }
              break;
            default:
            return;
         }
        }
      
        friendship.setFriend(index, f.toBuilder().setType(type).build());
      
  }
  private int findFriend(long id) {
    // TODO Auto-generated method stub
    for (int i=0; i<friendship.getFriendCount();i++){
      Friend f = friendship.getFriend(i);
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
        updateFriendship(index,Type.ADDED);
      }else{
        friendship.addFriend(Friend.newBuilder().setType(Type.ADDED).setId(friendIDs[i]).build());
      }
      
    }
  }

  public void deleteFriend(long l) {
    // TODO Auto-generated method stub
    int index = findFriend(l);
    if(index!=-1){
      friendship.removeFriend(index);
    }
  }
  public void muteFriend(long l) {
    // TODO Auto-generated method stub
    int index = findFriend(l);
    if(index!=-1){
        updateFriendship(index, Type.MUTED);
    }
  }
  public void blockFriend(long l) {
    // TODO Auto-generated method stub
    int index = findFriend(l);
    if(index!=-1){
      friendship.getFriendBuilder(index).setType(Type.BLOCKED).build();
    }
  }
 
}
