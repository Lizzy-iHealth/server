package com.gm.server;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Rpc.Quests;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.Quest;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class GetActivitiesServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
//Input:   key : the key of requester
//
//Output:  Quests message, containing all the quests applied by or assigned to me. 

public void handle(HttpServletRequest req, HttpServletResponse resp)
throws ApiException, IOException {

Key userKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
User user = dao.get(userKey, User.class);
List<String> activityKeys = user.getActivities().getKeyList();
Quests.Builder questsMsg = Quests.newBuilder();
for(String questKeyStr: activityKeys){

Key questKey = KeyFactory.stringToKey(questKeyStr);
// get quest from datastore and add an application 
Quest quest = checkNotNull(dao.get(questKey, Quest.class),ErrorCode.quest_quest_not_found);
QuestPb qMsg = quest.getMSG(user.getId()).build();
questsMsg.addQuest(qMsg);
}
resp.getOutputStream().write(questsMsg.build().toByteArray());

}


}
