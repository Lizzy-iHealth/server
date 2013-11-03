package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Rpc.Thumbnail;
import com.gm.common.model.Rpc.UserPb;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.Quest;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class UpdateProfileServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
  
  //Input Param: "key"        user's index
  //             "pb"      Base64 encoded UserPb object with name or thumbnail
  //Output: UserPb

      @Override
      public void handle(HttpServletRequest req, HttpServletResponse resp)
          throws ApiException, IOException {
        
        // retrieve quest
   
        Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
        UserPb profile = UserPb.parseFrom(ParamKey.pb.getPb(req));
        User user = dao.get(ownerKey, User.class);
        if(profile.hasName()){
        user.setName(profile.getName());
        }
        if(profile.hasThumbnail()){
        	user.setThumbnail(profile.getThumbnail().toBuilder());
        }
        dao.save(user);
        resp.getOutputStream().write(user.getMSG(user.getId()).build().toByteArray());

      }

 
        
}
