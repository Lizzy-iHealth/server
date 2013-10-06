package com.gm.server.model;

import static org.junit.Assert.*;

import org.junit.Test;

import com.gm.server.ModelTest;

public class BookTest extends ModelTest {

  @Test
  public void test() {
    Book b = new Book();
    b.title = "Hello";
    b.authors.addAuthor("wentao").addAuthor("zhiyu");
    
    dao.create(b);
    
    Book read = dao.querySingle("title", "Hello", Book.class);
    assertNotNull(read);
    assertEquals(b.title, read.title);
    assertEquals(b.authors.getAuthorList(), read.authors.getAuthorList());
  }
}
