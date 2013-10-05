package com.gm.server.model;

import java.util.Date;

@Entity
public class User extends Persistable<User> {

  //public static final User _ = new User();

  public static final boolean existsByPhone(String phone) {
    return DAO.get().querySingle("phone", phone, User.class) != null;
  }
  
	public void login(String secret, String key) {
		this.secret = secret;
		this.key = key;
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
		return key;
	}

	public void setKey(String key) {
		this.key = key;
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

	@Property
	private String phone = "";

	@Property
	private String password = "";

	@Property
	private String secret = "";

	@Property
	private String key = "";

	@Property
	private Date createTime = new Date();

	@Property
	private Date lastLoginTime = new Date();

	@Override
	public User touch() {
		return this;
	}

	public User(String mobileNumber, String password, String secret, String key) {
		super();
		this.phone = mobileNumber;
		this.password = password;
		this.secret = secret;
		this.key = key;
		this.createTime = new Date();
		this.lastLoginTime = createTime;
	}

	public User() {
		// TODO Auto-generated constructor stub
	}

}
