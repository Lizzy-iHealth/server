package com.gm.server.model;

import java.util.Date;


@Entity
public class Token extends Persistable<Token> {

  //public static final Token _ = new Token("", "");
  
  @Property
  public String phone = "";
  
  @Property
  public String token = "";
  
  @Property
  public Date generateTime = new Date();
  
  public Token() {
    
  }
  
  public Token(String phone, String token) {
    this.phone = phone;
    this.token = token;
  }

  @Override
  public Token touch() {
    return this;
  }
}
