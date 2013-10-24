package com.gm.server.model;

import java.util.List;

import com.google.appengine.api.datastore.Key;


public class Office {
    String phone;
    Key adminKey;
    Key questKeys[];

    public Office(String phone){
      this.phone = phone;
      questKeys = null;
    }

    public void init(DAO dao) {
      User qa = dao.querySingle("phone", phone, User.class);
      adminKey = qa.getEntityKey();
      List<Quest> quests = dao.query(Quest.class).setAncestor(qa.getEntityKey()).prepare().asList();
      questKeys = new Key[quests.size()];
      int i = 0;
      for(Quest q : quests){
        questKeys[i] = q.getEntityKey();
        i++;
      }
     
    }
    
    public String getPhone() {
      return phone;
    }


    public void setPhone(String phone) {
      this.phone = phone;
    }


    public Key getAdminKey() {
      return adminKey;
    }


    public void setAdminKey(Key adminKey) {
      this.adminKey = adminKey;
    }


    public Key[] getQuestKeys() {
      return questKeys;
    }

    public void setQuestKeys(Key[] questKeys) {
      this.questKeys = questKeys;
    }

}
