package com.gm.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.gm.common.crypto.Hmac;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.DAO;
import com.gm.server.model.Model.Friend;
import com.gm.server.model.Model.Friend.Type;
import com.gm.server.model.Model.Friendship;
import com.gm.server.model.PendingUser;
import com.gm.server.model.Token;
import com.gm.server.model.User;
import com.gm.server.push.Pusher;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.base.Strings;

public enum API {
  device("/auth/", true) {

    @Override
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
  },
  
  get_friends("/social", true){

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      // TODO Auto-generated method stub
      String key = ParamKey.key.getValue(req);
      User user = dao.get(key, User.class);
      Friendship friendship = user.getFriendship().build();
   //   System.out.print(friendship.toByteArray());
      resp.getWriter().print(friendship.toByteArray());
    }
    
  },
  add_friends("/social", true){//true) {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {

        long[] friendIDs = ParamKey.user_id.getLongs(req,-1);
        String key = ParamKey.key.getValue(req);
        long myId = getId(key);
        //KeyFactory.stringToKey(key).getId();
        User user = dao.get(key, User.class);

        
        Key friendKeys[] = new Key[friendIDs.length];
        
        ApiException error = null;   
        Map<String, String> notice = new HashMap<String, String>();
        notice.put("notice", "friend");
        ArrayList<String> idsToNotify = new ArrayList<String>(friendIDs.length); 
        for(int i=0;i<friendIDs.length;i++){
          friendKeys[i]=KeyFactory.createKey("User",friendIDs[i]);
          User friend;
          try{
             friend = checkNotNull(dao.get(friendKeys[i], User.class),ErrorCode.auth_user_not_registered);
          }catch(ApiException e){
            error = e;
            continue;
          }
            user.addFriend(friendIDs[i],Type.ADDED);
            friend.addFriend(myId,Type.WAIT_MY_CONFIRM);
            if(friend.getDeviceID()!=null){
              idsToNotify.add(friend.getDeviceID());
            }
            dao.save(friend);
            
        }
        dao.save(user);
      if (idsToNotify.size() > 0) {
        String[] device_ids = new String[idsToNotify.size()];
        idsToNotify.toArray(device_ids);
        try {
          new Pusher(device_ids).push(notice);
        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
        if(error!=null){
          throw error;
        }
    
    }
  },

 
  delete_friends("/social", true){//true) {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {

        long[] friendIDs = ParamKey.user_id.getLongs(req,-1);
        String key = ParamKey.key.getValue(req);
        long myId = getId(key);
        //KeyFactory.stringToKey(key).getId();
        User user = dao.get(key, User.class);

        
        Key friendKeys[] = new Key[friendIDs.length];
        
        ApiException error = null;   
                
        for(int i=0;i<friendIDs.length;i++){
          friendKeys[i] = KeyFactory.createKey("User", friendIDs[i]);
          User friend;
          try {
            friend = checkNotNull(dao.get(friendKeys[i], User.class),
                ErrorCode.auth_user_not_registered);
          } catch (ApiException e) {
            error = e;
            continue;
          }
          user.deleteFriend(friendIDs[i]);
          friend.deleteFriend(myId);
          dao.save(friend);
            
        }
        dao.save(user);
        if(error!=null){
          throw error;
        }
    
    }
  },

  block_friends("/social", true){//true) {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {

        long[] friendIDs = ParamKey.user_id.getLongs(req,-1);
        String key = ParamKey.key.getValue(req);
        long myId = getId(key);
     
        User user = dao.get(key, User.class);

        
        Key friendKeys[] = new Key[friendIDs.length];
        
        ApiException error = null;   
                
        for(int i=0;i<friendIDs.length;i++){
          friendKeys[i] = KeyFactory.createKey("User", friendIDs[i]);
          User friend;
          try {
            friend = checkNotNull(dao.get(friendKeys[i], User.class),
                ErrorCode.auth_user_not_registered);
          } catch (ApiException e) {
            error = e;
            continue;
          }
          user.blockFriend(friendIDs[i]);
          friend.deleteFriend(myId);
          dao.save(friend);
            
        }
        dao.save(user);
        if(error!=null){
          throw error;
        }
    
    }
  },

  
  invite_friends("/social", true){//true) {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {

        String key = ParamKey.key.getValue(req);
        String[] friendPhones = ParamKey.phone.getValues(req);
        long invitorId = getId(key);
          
        for(String phone:friendPhones){
          PendingUser pu = dao.querySingle("phone", phone, PendingUser.class);
          //already invited;
          if(pu!=null){
            pu.addInvitor(invitorId);
            dao.save(pu);
          }else {
            User temp = dao.querySingle("phone", phone, User.class);
          //already our user
            if(temp != null){
              
              temp.addFriend(invitorId, Type.WAIT_MY_CONFIRM);
              dao.save(temp);
              User invitor = dao.get(key, User.class);
              invitor.addFriend(temp.getEntityKey().getId(), Type.ADDED);
              dao.save(invitor);
              continue;
            } else{
              // newly invited user
              pu = new PendingUser(phone,invitorId);
              dao.create(pu);
            }
          }
        }
    }
 
  },
        
  push("/util/",false){

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      // TODO Auto-generated method stub
      long ids[] = ParamKey.user_id.getLongs(req, -1);
      String data_key = "key";
      String data_value = "This is a push message";
      Map<String, String> data = new HashMap<String, String>();
      data.put(data_key, data_value);
      List<String> device_ids = new ArrayList<String>(ids.length);
      for(long id:ids){
        String device_id = dao.get(KeyFactory.createKey("User", id), User.class).getDeviceID();
        device_ids.add(device_id);
      }
      try {
        new Pusher(device_ids.toArray(new String[device_ids.size()])).push(data);
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
  }
,
  
  ping("/", true) {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
    }
  },

  login("/auth/", false) {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      String phone = stringNotEmpty(ParamKey.phone.getValue(req),
          ErrorCode.auth_invalid_phone);
      String password = stringNotEmpty(ParamKey.password.getValue(req),
          ErrorCode.auth_invalid_password);

      User user = checkNotNull(dao.querySingle("phone", phone, User.class),
          ErrorCode.auth_phone_not_registered);
      check(password.equals(user.getPassword()),
          ErrorCode.auth_incorrect_password);

      String secret = UUID.randomUUID().toString();

      user.login(secret);
      dao.save(user);
      String key = user.getKey();
      resp.getWriter().write(key);
      resp.getWriter().write(",");
      resp.getWriter().write(secret);
    }
  },

  register("/auth/", false) {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      String phone = stringNotEmpty(ParamKey.phone.getValue(req),
          ErrorCode.auth_invalid_phone);
      String password = stringNotEmpty(ParamKey.password.getValue(req),
          ErrorCode.auth_invalid_password);
      String token = stringNotEmpty(ParamKey.token.getValue(req),
          ErrorCode.auth_invalid_token);
      // TODO : find token by token key instead of phone
      Token tokenStore = checkNotNull(
          dao.querySingle("phone", phone, Token.class),
          ErrorCode.auth_token_not_sent);
      check(token.equalsIgnoreCase(tokenStore.token),
          ErrorCode.auth_incorrect_token);

      check(!User.existsByPhone(phone), ErrorCode.auth_phone_registered);

      String secret = UUID.randomUUID().toString();
      User user = new User(phone, password, secret);
      dao.create(user);
      PendingUser pu = dao.querySingle("phone", phone, PendingUser.class);
    
      if(pu!=null){
        user.setFriendship(pu.getInvitors());
        rewardInvitors(user.getUserID(),pu.getInvitors().build());
        dao.delete(pu);
      }
      dao.save(user);

      String key = user.getKey();
      resp.getWriter().write(key);
      resp.getWriter().write(",");
      resp.getWriter().write(secret);
    }
  },

  request_token("/auth/", false) {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      String phone = stringNotEmpty(ParamKey.phone.getValue(req),
          ErrorCode.auth_invalid_phone);
      String msg = "1234"; // TODO: generate random token and send by sms
      Token token = dao.querySingle("phone", phone, Token.class);
      if (token == null)
        token = new Token(phone, msg);
      token.token = msg;
      dao.save(token);
    }
  };

  public final String url;
  public final boolean requiresHmac;

  private API(String urlPrefix, boolean requiresHmac) {
    url = urlPrefix + name();
    this.requiresHmac = requiresHmac;
  }

  protected void rewardInvitors(long newUserID, Friendship invitors) throws ApiException {
    // TODO Auto-generated method stub
    ApiException error=null;
    for (Friend invitor:invitors.getFriendList()){
      Key invKey = KeyFactory.createKey("User", invitor.getId());
      
      User inv = null;
      try {
        inv= checkNotNull(dao.get(invKey, User.class),ErrorCode.auth_user_not_registered);
      } catch (ApiException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        error = e;
        continue;
      }
        inv.addFriend(newUserID, Type.INVITED);
        dao.save(inv);
    }
    if(error != null){
      throw error;
    }
  }

  protected long getId(String key) {
    // TODO Auto-generated method stub
    return KeyFactory.stringToKey(key).getId();
  }

  private static final DAO dao = DAO.get();

  static void check(boolean condition, int error) throws ApiException {
    if (!condition)
      throw new ApiException(error);
  }

  static String stringNotEmpty(String string, int error) throws ApiException {
    check(!Strings.isNullOrEmpty(string), error);
    return string;
  }

  static <T> T checkNotNull(T reference, int error) throws ApiException {
    if (reference == null)
      throw new ApiException(error);
    return reference;
  }

  /**
   * Each API enum item will implement this method, handling individual business
   * logic
   * 
   * @param req
   * @param resp
   * @throws ApiException
   * @throws IOException
   */
  public abstract void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException;

  /**
   * The common logic for all HTTP servlets.
   * 
   * @param req
   * @param resp
   * @throws IOException
   */
  public void execute(HttpServletRequest req, HttpServletResponse resp,
      boolean withHmac) throws IOException {
    if (withHmac) {
      execute(req, resp);
    } else {
      try {
        handle(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK); // API executed successfully
      } catch (ApiException e) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // API failed on
                                                            // validation
        resp.getWriter().print(e.error);
      } catch (Exception e) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Unknown
                                                                      // error
        info(e, "unknow API error %s", e.getMessage());
        e.printStackTrace(new PrintWriter(resp.getOutputStream())); // TODO:
                                                                    // remove me
                                                                    // when
                                                                    // release

      }
    }

  }
  public void execute(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    try {
      if (requiresHmac) {
        String key = stringNotEmpty(ParamKey.key.getValue(req),
            ErrorCode.auth_invalid_key_or_secret);
        String hmac = stringNotEmpty(ParamKey.hmac.getValue(req),
            ErrorCode.auth_invalid_key_or_secret);
        String body = new String(readStream(req.getInputStream()));
        
        info("key = %s, hmac = %s, body = %s", key, hmac, body);

        // |------------ body -------------|
        // ........&key=....&hmac=..........
        // |--- message ---||-- hmacPart --|

        String hmacPart = ParamKey.hmac.name() + "="
            + URLEncoder.encode(hmac, "UTF-8");
        int index = body.indexOf(hmacPart);
        check(index > 1, ErrorCode.auth_invalid_key_or_secret);
        String message = body.substring(0, index - 1); // get rid of last '&'

        User user = checkNotNull(
            dao.get(KeyFactory.stringToKey(key), User.class),
            ErrorCode.auth_invalid_key_or_secret);
        String match = Hmac.generate(message, user.getSecret());
        
        info("match = %s", match);
        check(hmac.equals(match), ErrorCode.auth_invalid_key_or_secret);

        // TODO: need test
      }

      handle(req, resp);
      resp.setStatus(HttpServletResponse.SC_OK); // API executed successfully
    } catch (ApiException e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // API failed on
                                                          // validation
      resp.getWriter().print(e.error);
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Unknown
                                                                    // error
      info(e, "unknow API error %s", e.getMessage());
      e.printStackTrace(new PrintWriter(resp.getOutputStream())); // TODO:
                                                                  // remove me
                                                                  // when
                                                                  // release

    }
  }

  private static byte[] readStream(InputStream in) throws IOException {
    byte[] buf = new byte[1024];
    int count = 0;
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    while ((count = in.read(buf)) != -1)
      out.write(buf, 0, count);
    return out.toByteArray();
  }

  static final void info(Throwable t, String msg, Object... args) {
    logger.log(Level.INFO, String.format(msg, args), t);
  }

  static final void info(String msg, Object... args) {
    logger.log(Level.INFO, String.format(msg, args));
  }

  private static final Logger logger = Logger.getLogger(API.class.getName());
}
