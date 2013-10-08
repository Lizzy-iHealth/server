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
  private Friendship.Builder friendship;
  
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
  
	public int getUserID() {
    return userID;
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

  public void addFriend(long id) {
    // TODO Auto-generated method stub
    Type type = Type.WAIT_MY_CONFIRM;
    for (int i=0; i<friendship.getFriendCount();i++){
      Friend f = friendship.getFriend(i);
      if (f.getId()==id){
      
        switch (f.getType()){
          case ADDED:
            type = Type.CONFIRMED;
            break;
          case WAIT_MY_CONFIRM:
            //TODO: update time
            break;
          case INVITED:
            type = Type.CONFIRMED;
            break;
          default:
             return;
         }
        friendship.setFriend(i, f.toBuilder().setType(type).build());
          
      }
    }
    friendship.addFriend(Friend.newBuilder().setType(Type.ADDED).setId(myId).build());
  }
}
