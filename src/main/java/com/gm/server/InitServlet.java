package com.gm.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Config;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.server.model.Quest;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.Link;

/**
 * Servlet implementation class InitServlet
 */
public class InitServlet extends APIServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	  this.requiresHmac = false;
	  execute(request, response);
	}

  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    String dailyQuestURL = "http://help-hand.appspot.com/quest/SubmitDailyQuestServlet";
    User bank = dao.querySingle("phone", "8", User.class);
    if(bank == null){
      bank= createUser("8","1234");
      bank.setName("Bank");
      dao.save(bank);
    } 
    User taskAdmin = dao.querySingle("phone", "999", User.class);
    if(taskAdmin == null){
      taskAdmin= createUser("999","1234");
      taskAdmin.setName("Quest Admin");
      dao.save(taskAdmin);
    }else{
      if(taskAdmin.getGoldBalance()<999999)taskAdmin.setGoldBalance(999999999);
    }
    if(dao.query(Quest.class).setAncestor(taskAdmin.getEntityKey()).prepare().asList().size()==0){
      Config.Builder config = Config.newBuilder().setAllowSharing(false).setAutoAccept(false).setAutoClaim(false).setAutoConfirmAll(true)
          .setAutoConfirmFirstApplicant(true).setAutoReward(true).setFavourite(false);
      Quest checkIn = new Quest("Check in"
        ,"When you check in, your friends will see your location."
        ,1
        ,config
        ,QuestPb.Status.PUBLISHED.getNumber());
      checkIn.setAttach_link(new Link(dailyQuestURL));
      dao.save(checkIn,taskAdmin.getEntityKey());
   
    }
  }

}
