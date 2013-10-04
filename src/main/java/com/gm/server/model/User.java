package com.gm.server.model;

import java.util.Date;

@Entity
public class User extends Persistable<User> {
	public void login(String secret, String key){
		this.secret=secret;
		this.key = key;
		lastLoginTime = new Date();
	}
	
	public String getMobileNumber() {
		return mobileNumber;
	}


	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
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
	  private String mobileNumber="";
	
	@Property
	  private String password="";
	
	@Property
	  private String secret="";
	
	@Property
	  private String key="";
	
	@Property
	  private Date createTime= new Date();
	
	@Property
	  private Date lastLoginTime=new Date();
	
		private static final User _ = new User();
	@Override
	public User touch() {
		return this;
	}

	public User(String mobileNumber, String password, String secret,
			String key) {
		super();
		this.mobileNumber = mobileNumber;
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
