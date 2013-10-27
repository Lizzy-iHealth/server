package com.gm.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.QuestPb;
import com.gm.server.model.Quest;
import com.gm.server.model.User;

/**
 * Servlet implementation class InitServlet
 */
public class InitServlet extends APIServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	  this.requiresHmac = false;
	  execute(request, response);
	}

  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    
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
    }
    if(dao.query(Quest.class).setAncestor(taskAdmin.getEntityKey()).prepare().asList().size()==0){
      Quest checkIn = new Quest("Check in"
        ,"When you check in, your friends will see your location."
        ,1
        ,true
        ,QuestPb.Status.PUBLISHED.getNumber());

    dao.save(checkIn,taskAdmin.getEntityKey());
    }
  }

}
