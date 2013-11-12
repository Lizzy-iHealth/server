package com.gm.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.gm.server.push.Pusher;

public class PushServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    this.requiresHmac = false;
    execute(req, resp);
  }
  
  
  //Input Param: "user_id"    user id list to push message
  //Output      : push notification


    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      // TODO Auto-generated method stub
      String device_ids[] = ParamKey.device_id.getValues(req);
      String data_key = req.getParameter("data_key");
      String data_value = req.getParameter("data_value");
      Map<String, String> data = new HashMap<String, String>();
      data.put(data_key, data_value);
      if(device_ids==null||device_ids.length==0){
    	  return;
      }

      try {
        new Pusher(device_ids).push(data);
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
}
