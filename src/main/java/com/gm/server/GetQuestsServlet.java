package com.gm.server;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Rpc.Quests;
import com.gm.server.model.Quest;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class GetQuestsServlet extends APIServlet {
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
//Output:  QuestsPb message, containing all the quests owned by me. 

    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {

      Key userKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
      
      // get all the quests owned by the user
      List<Quest> quests = dao.query(Quest.class).setAncestor(userKey).prepare().asList();
      
      // prepare output message
      Quests.Builder questsMsg = Quests.newBuilder();
      for (Quest q : quests){
        QuestPb qMsg = q.getMSG(userKey.getId()).build();
        questsMsg.addQuest(qMsg);
      }
      resp.getOutputStream().write(questsMsg.build().toByteArray());

    }
}
