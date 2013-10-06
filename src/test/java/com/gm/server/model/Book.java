package com.gm.server.model;

import com.gm.server.model.Test.AuthorList;

@Entity
public class Book extends Persistable<Book> {

  @Property
  public String title;
  
  @Property
  public AuthorList.Builder authors = AuthorList.newBuilder();

  @Override
  public Book touch() {
    return this;
  }
}
