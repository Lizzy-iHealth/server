package com.gm.server.model;


@Entity
public class Token extends Persistable<Token> {

  private static final Token _ = new Token("", "");
  
  @Property
  public String phone;
  
  @Property
  public String token;
  
  public Token(String phone, String token) {
    this.phone = phone;
    this.token = token;
  }

  @Override
  public Token touch() {
    return this;
  }
}
