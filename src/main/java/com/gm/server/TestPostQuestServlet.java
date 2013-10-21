package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Friendship;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.server.model.Quest;
import com.gm.server.model.User;

public class TestPostQuestServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    this.requiresHmac = false;
    execute(req, resp);
  }
  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    
    // retrieve quest
    User user = new User("a12345","password","secret");
    User friend = new User("b12345","password","secret");
    dao.save(user);
    dao.save(friend);
    
    user.addFriend(friend.getId(), Friendship.CONFIRMED);
    friend.addFriend(user.getId(), Friendship.CONFIRMED);
    dao.save(user);
    dao.save(friend);
    
    String title = "a quest";
    Quest quest = new Quest(title);

    // save quest and post record to DB  
    long receiverIds[] = {friend.getId()};
    quest.addPost(user.getId(),receiverIds); //add at the end
    dao.save(quest, user.getEntityKey());
    
    //TODO: filter the receivers with friend lists, only allow friends as receivers
    //TODO: redirect to backend
    // prepare feed
    QuestPb.Builder questFeed = quest.getMSG();

    generateFeed(receiverIds,questFeed,"test");
    
    // push to receivers
    
  }
}
