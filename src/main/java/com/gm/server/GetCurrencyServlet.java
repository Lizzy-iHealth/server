package com.gm.server;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Currency;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.Key;

/**
 * Servlet implementation class GetCurrency
 */
public class GetCurrencyServlet extends APIServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public GetCurrencyServlet() {
        this.requiresHmac = true;
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		execute(request,response);
	}


  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    String userKey = ParamKey.key.getValue(req);
    User user = dao.get(userKey, User.class);
    Currency c = Currency.newBuilder().setGold(user.getGoldBalance()).build();
    resp.getOutputStream().write(c.toByteArray());
    
  }

}
