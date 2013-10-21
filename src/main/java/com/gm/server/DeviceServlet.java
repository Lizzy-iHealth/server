package com.gm.server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.net.ErrorCode;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.KeyFactory;

public class DeviceServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    // TODO Auto-generated method stub
    String key = stringNotEmpty(ParamKey.key.getValue(req),
        ErrorCode.auth_invalid_key_or_secret);
    String deviceID = stringNotEmpty(ParamKey.device_id.getValue(req),
        ErrorCode.auth_invalid_device_id);

    User user = checkNotNull(
        dao.get(KeyFactory.stringToKey(key), User.class),
        ErrorCode.auth_user_not_registered);
    user.setDeviceID(deviceID);
    dao.save(user);
  }
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    API.device.execute(req, resp);
  }
}

