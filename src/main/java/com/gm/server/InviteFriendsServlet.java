package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.UserPb;
import com.gm.common.model.Rpc.UsersPb;

public class InviteFriendsServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
execute(req, resp);
  }
  
  //Input Param: "key"        user's key
  //             "phone"      phone list to be invited or added
  //			"pb"		  users' profile list to be invited
  //Output Param: FriendStatus

    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {

        String key = ParamKey.key.getValue(req);
        UsersPb usersMsg=null;
        String[] friendPhones=null;
        try{
        	usersMsg = UsersPb.parseFrom(ParamKey.pb.getPb(req));
        }catch(Exception e){
        	friendPhones = ParamKey.phone.getValues(req);
        }
        
        if(usersMsg==null&&friendPhones!=null){
        	UsersPb.Builder msg = UsersPb.newBuilder();
        	for(String phone:friendPhones){
        		msg.addUser(UserPb.newBuilder().setPhone(phone));
        	}
        	usersMsg = msg.build();
        }
        int[] results = inviteFriends(key, usersMsg);
        
        writeResponse(resp,results);
    }
}
