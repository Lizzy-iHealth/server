package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DeleteFeedServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
  //Input:       id  :quest id, 
  //       owner_id  :quest owner's id
  //       user_id   :whose feeds need deletion
  //Output: delete related feeds and notify all the receivers

    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      //get receivers id list
      long receiverIds[] = ParamKey.user_id.getLongs(req, -1);
      long questId = ParamKey.id.getLong(req, -1);
      long owner_id = ParamKey.owner_id.getLong(req , -1);

      for(long id:receiverIds){

        deleteFeed(id, questId,owner_id);
      }
      push(receiverIds,"type","feed");
    }
}
