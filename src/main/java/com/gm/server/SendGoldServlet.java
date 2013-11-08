package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Currency;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class SendGoldServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
  //Input : key: sender's key
  //         id: receivers' id
  //       currency: gold
  //Output: currency: containing gold balance     

   @Override
   public void handle(HttpServletRequest req, HttpServletResponse resp)
       throws ApiException, IOException {
     
     Key senderKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
     Currency amount = Currency.parseFrom(ParamKey.currency.getPb(req));
     
     long receiverId = ParamKey.user_id.getLong(req, -1);
     check(receiverId!=-1,ErrorCode.quest_receiver_not_found);
     transferGold(senderKey.getId(),receiverId,amount.getGold());
     amount = amount.toBuilder().setGold(dao.get(senderKey, User.class).getGoldBalance()).build();
     resp.getOutputStream().write(amount.toByteArray());
     push(receiverId,"type","currency");
   }
}
