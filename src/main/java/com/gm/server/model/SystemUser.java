package com.gm.server.model;

import com.gm.common.model.Rpc.Applicant;
import com.gm.common.model.Rpc.Config;
import com.gm.common.model.Rpc.Currency;
import com.gm.common.model.Rpc.QuestPb;
import com.google.appengine.api.datastore.Key;

public 	enum SystemUser{
	
	questAdmin("999","Quest Admin"),
	bank("8","Bank");

	private String phone;
	private String name;
	private Key key;

	private SystemUser(String phone, String name) {
		this.phone = phone;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Key getKey() {
		return key;
	}
	public void setKey(Key key) {
		this.key = key;
	}
}