package com.gm.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.gm.common.crypto.Hmac;
import com.gm.common.model.Rpc.Applicant;
import com.gm.common.model.Rpc.Applicants;
import com.gm.common.model.Rpc.Currency;
import com.gm.common.model.Rpc.Friendship;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Rpc.Quests;
import com.gm.common.model.Rpc.UserPb;
import com.gm.common.model.Rpc.UsersPb;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.DAO;
import com.gm.server.model.Feed;
import com.gm.server.model.PendingUser;
import com.gm.server.model.Quest;
import com.gm.server.model.Token;
import com.gm.server.model.User;
import com.gm.server.push.Pusher;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gm.common.crypto.Base64;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import com.gm.common.model.Rpc.Friend;
import com.gm.common.model.Rpc.Friends;
import com.gm.common.model.Server.FeedPb;


public enum API {
  
  //Input Param: "key"        user's key
  //             "device_id"  device_id get upon request to google
  //Output      : N/A
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
  
  //Input Param: "key"        user's key
  //             "user_id"    user id list whose details are returned, if null, then return all friends details
  //Output Param: byte[] UsersPb :    users' details
  get_friends_details("/social",true){

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      String key = ParamKey.key.getValue(req);
      User user = dao.get(key, User.class);
      long qUserIds[] = ParamKey.user_id.getLongs(req,-1);
      UsersPb.Builder users = UsersPb.newBuilder();
      if(qUserIds==null){
        qUserIds = user.getFriendIds();
      }
      /*
      HashSet<Long> friendset = new HashSet<Long>(); //used to verify the relationship with the requested id.
      
      
      for (Friend f : user.getFriends().getFriendList()){
        if(f.getFriendship()==Friendship.CONFIRMED
            ||f.getFriendship()==Friendship.WAIT_MY_CONFIRM
            ||f.getFriendship()==Friendship.STARED){
            friendset.add(Long.valueOf(f.getId()));
        }
      }
      */
      
      ApiException err = null;
      for(long id:qUserIds){
     //   if(friendset.contains(id)){
          User friendUser;
          try{
            friendUser = checkNotNull(dao.get(KeyFactory.createKey("User", id), User.class),ErrorCode.social_user_not_found);
          }catch(ApiException e){
            err = e;
            continue;
          }
            UserPb.Builder friend = friendUser.getMSG(user.getId());
            friend.setFriendship(user.getFriendship(id));
            users.addUser(friend);
        }
      
      if(err!=null){
        throw err;
      }

//      System.out.println(users.build().toString());
      resp.getOutputStream().write(users.build().toByteArray());

    }      
    
    
  },
  
  //Input Param: "key"        user's key
  //             "phone"    user phone whose details are returned
  //Output Param: UserPb :    user' details
  //Zhiyu's commen: not provide this api, for users' privacy. With this API, strangers can get users' detail info by phone number.
  //TODO: let the phone's owner approve the information disclosure.
  get_phone_details("/social",true){

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      String key = ParamKey.key.getValue(req);
      User user = dao.get(key, User.class);
      String qPhone = checkNotNull(ParamKey.phone.getValue(req),ErrorCode.auth_invalid_phone);
      
      User qUser = checkNotNull(dao.querySingle("phone", qPhone, User.class),ErrorCode.auth_phone_not_registered);
      UserPb.Builder qUserMsg = qUser.getMSG(user.getId());
      qUserMsg.setFriendship(user.getFriendship(qUser.getId()));
      
      //System.out.println("============");
      //System.out.println(qUserMsg.build().toString());
      resp.getOutputStream().write(qUserMsg.build().toByteArray());

    }      
    
    
  },
  
  //Input Param: "key"        user's key
  //             
  //Output Param: byte[] Friends :    all friends id and type
  get_friends("/social", true){

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {

      String key = ParamKey.key.getValue(req);
      User user = dao.get(key, User.class);
      Friends friends = user.getFriends().build();

      resp.setContentType("application/x-protobuf");
      resp.getOutputStream().write(friends.toByteArray());
    }
    
  },
  
  //Input Param: "key"        user's key
  //             "user_id"    user id list to be added
  //Output Param: N/A
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
          
          User friend = null;
          try{
             check(friendKeys[i]!=user.getEntityKey(),ErrorCode.social_add_self_friend);
             friend = checkNotNull(dao.get(friendKeys[i], User.class),ErrorCode.auth_user_not_registered);
          }catch(ApiException e){
            error = e;
            continue;
          }
            user.addFriend(friendIDs[i],Friendship.ADDED);
            friend.addFriend(myId,Friendship.WAIT_MY_CONFIRM);
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
  
  //Input Param: "key"        user's key
  //             "user_id"    user id list to be deleted
  //Output Param: N/A

 
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
  
  //Input Param: "key"        user's key
  //             "user_id"    user id list to be blocked
  //Output Param: N/A

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
  
  //Input Param: "key"        user's key
  //             "phone"      phone list to be invited or added
  //Output Param: N/A

  
  invite_friends("/social", true){//true) {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {

        String key = ParamKey.key.getValue(req);
        String[] friendPhones = ParamKey.phone.getValues(req);
        long invitorId = getId(key);
        String[] results = new String[friendPhones.length]; // "0" not our user, "1" already our user.
        
        int i = 0;
        for(String phone:friendPhones){
          results[i]="0";
          PendingUser pu = dao.querySingle("phone", phone, PendingUser.class);
          //already invited;
          if(pu!=null){
            pu.addInvitor(invitorId);
            dao.save(pu);
          }else {
            User temp = dao.querySingle("phone", phone, User.class);
          //already our user
            if(temp != null){
              results[i]="1";
              temp.addFriend(invitorId, Friendship.WAIT_MY_CONFIRM);
              dao.save(temp);
              User invitor = dao.get(key, User.class);
              invitor.addFriend(temp.getEntityKey().getId(), Friendship.ADDED);
              dao.save(invitor);
              continue;
            } else{
              // newly invited user
              pu = new PendingUser(phone,invitorId);
              dao.create(pu);
            }
          }
          i++;
        }
        Joiner joiner = Joiner.on(",").skipNulls();
        System.out.println(joiner.join(results));
        resp.getWriter().write(joiner.join(results));
    }
 
  },
  
  //Input Param: "user_id"    user id list to push message
  //Output      : push notification
        
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
  
  //Input Param: "password"        user's password
  //             "phone"           user's phone to regeister
  //           
  //Output: String key             index of user
  //        String  ,
  //        String secret           for hmac generation

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
      resp.getWriter().write(",");
      resp.getWriter().write(Long.toString(user.getId()));
    }
  },

  //Input Param: "password"        user's password
  //             "phone"           user's phone to regeister
  //              "token"          verify ownership of phone
  //Output: String key             index of user
  //        String  ,
  //        String secret           for hmac generation
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
        user.setFriends(pu.getInvitors());
        rewardInvitors(user.getUserID(),pu.getInvitors().build());
        dao.delete(pu);
      }
      dao.save(user);

      String key = user.getKey();
      resp.getWriter().write(key);
      resp.getWriter().write(",");
      resp.getWriter().write(secret);
      resp.getWriter().write(",");
      resp.getWriter().write(Long.toString(user.getId()));
    }
  },

  //Input Param:   "phone"           user's phone to regeister
  //           
  //Output:  N/A
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
  },
  
  //Input Param: "key"        user's index
  //             "quest"      Base64 encoded QuestPb object
  //         
  //Output: quest ID.
  
    save_quest("/quest/",true){

      @Override
      public void handle(HttpServletRequest req, HttpServletResponse resp)
          throws ApiException, IOException {
        
        QuestPb questMsg = getQuestPb(req);
        Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
        

        Quest quest = saveQuest(questMsg, ownerKey);
        resp.getWriter().write(Long.toString(quest.getId()));
        
      }
        
   },
   
  //Input Param: "key"        user's index
  //             "quest"      Base64 encoded QuestPb object
  //              "user_id"   receiver list
  //Output:       quest id
   //             push notification
  
    post_quest("/quest/",true){

      @Override
      public void handle(HttpServletRequest req, HttpServletResponse resp)
          throws ApiException, IOException {
        
        QuestPb questMsg = getQuestPb(req);
        Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
        long receiverIds[]= ParamKey.user_id.getLongs(req,-1);
        check(receiverIds.length>0,ErrorCode.quest_receiver_not_found);
        // save quest and post record to DB  
        Quest quest = new Quest(questMsg);
        quest.addPost(ownerKey.getId(),receiverIds); //add at the end
        
        //if only one receiver, add him as the pre-confirmed applicant
        //once he apply for the quest, he will be automatically confirmed, and the quest change to "deal"
        if(receiverIds.length==1){
          // add applicants:
          long id = receiverIds[0];
            Applicant applicant = Applicant.newBuilder()
                                  .setUserId(id)
                                  .setBid(Currency.newBuilder().setGold(quest.getPrize()))
                                  .setType(Applicant.Status.ASSIGN).build();
            quest.addApplicant(applicant);
            User receiver = dao.get(KeyFactory.createKey("User", id), User.class);
            receiver.addActivity(KeyFactory.keyToString(quest.getEntityKey()));
            dao.save(receiver);
          }
        
        quest.setStatus(QuestPb.Status.PUBLISHED);
        dao.save(quest, ownerKey);
        resp.getWriter().write(Long.toString(quest.getId()));
        //TODO: filter the receivers with friend lists, only allow friends as receivers
        // prepare feed
        QuestPb.Builder questFeed = quest.getMSG();
        if(receiverIds.length==1){
          generateFeed(receiverIds,questFeed,"assign");
        }else{
          generateFeed(receiverIds,questFeed,"post");
        }
        
        // push to receivers
        
      }
        
   },
   
   //Input Param: "key"        user's index
   //             "quest"      Base64 encoded QuestPb object
   //              "user_id"   assignment list
   //Output: push notification
   
     assign_quest("/quest/",true){

       @Override
       public void handle(HttpServletRequest req, HttpServletResponse resp)
           throws ApiException, IOException {
         
         QuestPb questMsg = getQuestPb(req);
         Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
         long receiverIds[]= ParamKey.user_id.getLongs(req,-1);
         
         // save quest and post record to DB  
         Quest quest = new Quest(questMsg);
         quest.addPost(ownerKey.getId(),receiverIds); //add at the end
         
         // add applicants:
         for(long id:receiverIds){
           Applicant applicant = Applicant.newBuilder()
                                 .setUserId(id)
                                 .setBid(Currency.newBuilder().setGold(quest.getPrize()))
                                 .setType(Applicant.Status.ASSIGN).build();
           quest.addApplicant(applicant);
           Key applierKey = KeyFactory.createKey("User", id);
           User applier = dao.get(applierKey, User.class);
           applier.addActivity(KeyFactory.keyToString(quest.getEntityKey()));
           dao.save(applier);
         }
         
         quest.setStatus(QuestPb.Status.PUBLISHED);
         dao.save(quest, ownerKey);
         
         //TODO: filter the receivers with friend lists, only allow friends as receivers
         // prepare feed
         QuestPb.Builder questFeed = quest.getMSG();

         generateFeed(receiverIds,questFeed,"assign");
         
         // push to receivers
         
       }
         
    },
   
   
   //Input Param: "key"        user's index
   //             "quest"      Base64 encoded QuestPb object with questId filled in.
   //                          those fields will be updated if provided:lifespan,title,address,geopoint,reward,description,allowsharing,url
   //              
   //Output: N/A
   //only quest owner can update a quest.
   //option 1:
   //updates will be available in data store. Others will get a notification, they will see the update when they request for it.
   //
   //
   //option 2:
   //all the receivers' related feed will be updated.
   update_quest("/quest/",true){

       @Override
       public void handle(HttpServletRequest req, HttpServletResponse resp)
           throws ApiException, IOException {
         
         // retrieve quest
         QuestPb questMsg = getQuestPb(req);
         Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
         Key questKey = KeyFactory.createKey(ownerKey, "Quest", questMsg.getId());
         // save quest and post record to DB  
         Quest quest = checkNotNull(dao.get(questKey,Quest.class), ErrorCode.quest_quest_not_found);
         quest.updateQuest(questMsg);
         dao.save(quest, ownerKey);
       

         long[] receiverIds = quest.getAllReceiversIds();
         if(receiverIds.length>0){
           //notify user some quest is updated with the quest key.
           push(receiverIds,"quest",KeyFactory.keyToString(quest.getEntityKey()));
         
           //option 2 implementation:
           //also update related feeds:
           QuestPb.Builder questFeed = quest.getMSG();

           generateFeed(receiverIds,questFeed,"update");
         }

       }
         
    },
    
    //Input Param: "key"        user's index
    //             "id"         quest id to be deleted
    //              
    //Output: N/A
    //only quest owner can stop posting a quest when reach a deal
    //option 1:
    //deal will only be pushed to applicants. Others will see the deal status when they request for it.
    //
    //option 2:
    //all the receivers' related feed will be deleted.
    stop_posting_quest("/quest/",true){

        @Override
        public void handle(HttpServletRequest req, HttpServletResponse resp)
            throws ApiException, IOException {
          
          // retrieve quest
                   
          Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
          long questId = ParamKey.id.getLong(req, -1);
          Key questKey = KeyFactory.createKey(ownerKey, "Quest", questId);
          // save quest and post record to DB  
          Quest quest = checkNotNull(dao.get(questKey,Quest.class), ErrorCode.quest_quest_not_found);
          quest.setStatus(QuestPb.Status.DEAL);
          
          dao.save(quest);
          //option 2 implementation:
          // prepare feed
          long[] receiverIds = quest.getAllReceiversIds();
          deleteFeed(receiverIds,questId,ownerKey.getId());
          
        }
          
     },
     
    //Input Param: "key"        user's index
    //             "id"         quest id to be deleted
    //              
    //Output: N/A
    //only quest owner can delete a quest.
    //option 1:
    //deletion will only be pushed to applicants. Others will see the deletion when they request for it.
    //
    //option 2:
    //all the receivers' related feed will be deleted.
    delete_quest("/quest/",true){

        @Override
        public void handle(HttpServletRequest req, HttpServletResponse resp)
            throws ApiException, IOException {
          
          // retrieve quest
                   
          Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
          long questId = ParamKey.id.getLong(req, -1);
          Key questKey = KeyFactory.createKey(ownerKey, "Quest", questId);
          // save quest and post record to DB  
          Quest quest = checkNotNull(dao.get(questKey,Quest.class), ErrorCode.quest_quest_not_found);

          //option 2 implementation:
          // prepare feed
          long[] receiverIds = quest.getAllReceiversIds();
          deleteFeed(receiverIds,questId,ownerKey.getId());
          dao.delete(quest);
          

        }
          
     },
     
     
    
    
   test_post_quest("/test/",false){

     @Override
     public void handle(HttpServletRequest req, HttpServletResponse resp)
         throws ApiException, IOException {
       
       // retrieve quest
       User user = new User("a12345","password","secret");
       User friend = new User("b12345","password","secret");
       dao.save(user);
       dao.save(friend);
       
       user.addFriend(friend.getId(), Friendship.CONFIRMED);
       friend.addFriend(user.getId(), Friendship.CONFIRMED);
       dao.save(user);
       dao.save(friend);
       
       String title = "a quest";
       Quest quest = new Quest(title);

       // save quest and post record to DB  
       long receiverIds[] = {friend.getId()};
       quest.addPost(user.getId(),receiverIds); //add at the end
       dao.save(quest, user.getEntityKey());
       
       //TODO: filter the receivers with friend lists, only allow friends as receivers
       //TODO: redirect to backend
       // prepare feed
       QuestPb.Builder questFeed = quest.getMSG();

       generateFeed(receiverIds,questFeed,"test");
       
       // push to receivers
       
     }
       
  },
  
   
   //Input Param: "key"        sharer's index
   //             "owner_id"    quest's onwer's id
   //             "id"          quest id
   //              "user_id"   receiver list
   //Output: push notification
      share_quest("/quest/",true){

        @Override
        public void handle(HttpServletRequest req, HttpServletResponse resp)
            throws ApiException, IOException {
          
          // retrieve quest key
          
          Key questKey = getQuestKeyFromReq(req);
          Key sharerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
          long receiverIds[]= ParamKey.user_id.getLongs(req,-1);

          // get quest from datastore and add a post record the quest entity 
          Quest quest = checkNotNull(dao.get(questKey, Quest.class),ErrorCode.quest_quest_not_found);
          check(quest.isAllow_sharing(),ErrorCode.quest_not_allow_sharing);
          check(!quest.isDeal(),ErrorCode.quest_is_deal);     
          quest.addPost(sharerKey.getId(),receiverIds); //add at the end
          dao.save(quest);
                  
          //TODO: redirect to backend
          // prepare feed
          QuestPb.Builder questFeed = quest.getMSG();
          questFeed.addRefererId(sharerKey.getId());
          generateFeed(receiverIds,questFeed,"share");

        }
   },
   
  // applicants can update their own application(identified by their user id) by apply again with new application info
   //Input:   id: quest id
   //         owner_id: quest owner's id
   //         applicant: application information
   //Output:  applicant status
   //         push "quest:new application" to owner
   apply_quest("/quest/",true){

     @Override
     public void handle(HttpServletRequest req, HttpServletResponse resp)
         throws ApiException, IOException {
       
       // get quest key
       
       Key questKey = getQuestKeyFromReq(req);
       Key applierKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
       
       //ensure the applicant's user_id field is the one send the request.
       Applicant applicant  = getApplicant(req);
       Applicant newApplicant = applicant.toBuilder()
                                 .setUserId(applierKey.getId())
                                 .setType(Applicant.Status.WAIT_MY_CONFIRM).build();
       
       // get quest from datastore and add an application 
       Quest quest = checkNotNull(dao.get(questKey, Quest.class),ErrorCode.quest_quest_not_found);
       
       // check the quest hasn't reach a deal
       check(!quest.isDeal(),ErrorCode.quest_is_deal);  
       
       int status = quest.addApplicant(newApplicant); //add at the end
       User applier = dao.get(applierKey, User.class);
       applier.addActivity(KeyFactory.keyToString(questKey));
       dao.save(applier);
       dao.save(quest);
       
       //return the status of the applicant.
      
      resp.getWriter().write(Integer.toString(status));
      
       //push message to quest owner.
       long[] receivers = {quest.getParent().getId()};
       push(receivers,"quest","new application");
     }
    
},

//Input:        key : requester's key
//              id  : quest id;
//         owner_id : quest owner id;
reject_assignment("/quest",true){

  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    
    // get quest key
    
    Key questKey = getQuestKeyFromReq(req);
    Key applierKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
    
    // get quest from datastore and add an application 
    Quest quest = checkNotNull(dao.get(questKey, Quest.class),ErrorCode.quest_quest_not_found);
    
    
        int index = quest.findApplicant(applierKey.getId());
        check(index!=-1,ErrorCode.quest_applicant_not_found);
        quest.updateApplicantStatus(index, Applicant.Status.REJECTTED);
        
    User applier = dao.get(applierKey, User.class);
    applier.deleteActivity(KeyFactory.keyToString(questKey));
    dao.save(applier);
    dao.save(quest);
   
    //push message to quest owner.
    long[] receivers = {quest.getParent().getId()};
    push(receivers,"quest","reject assignment");
  }
  
},
//Input: key: quest owner key
//       id : quest id
//       user_id: whose application is rejected.
//Output: notify the applicant that is reject.
reject_application("/quest",true){

  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    
    Key ownerKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
    
    // get quest
    long questId = ParamKey.id.getLong(req, -1);
    Key questKey = KeyFactory.createKey(ownerKey,"Quest",questId);
    Quest quest = checkNotNull(dao.get(questKey, Quest.class),ErrorCode.quest_quest_not_found);
    
    long toReject = ParamKey.user_id.getLong(req, -1);
    
        int index = quest.findApplicant(toReject);
        check(index!=-1,ErrorCode.quest_applicant_not_found);
        quest.updateApplicantStatus(index, Applicant.Status.REJECTTED);
        
    User applier = checkNotNull(dao.get(KeyFactory.createKey("User", toReject), User.class), ErrorCode.quest_applicant_not_found);
    applier.deleteActivity(KeyFactory.keyToString(questKey));
    dao.save(applier);
    dao.save(quest);
   
    //push message to quest owner.
    long[] receivers = {toReject};
    push(receivers,"delete_quest",Long.toString(quest.getId()));
    
  }
  
},
//Input:   key : the key of requester
//        
//Output:  QuestsPb message, containing all the quests applied by or assigned to me. 
get_activities("/quest/",true){

  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    
    Key userKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
    User user = dao.get(userKey, User.class);
    List<String> activityKeys = user.getActivities().getKeyList();
    Quests.Builder questsMsg = Quests.newBuilder();
    for(String questKeyStr: activityKeys){
    
        Key questKey = KeyFactory.stringToKey(questKeyStr);
        // get quest from datastore and add an application 
        Quest quest = checkNotNull(dao.get(questKey, Quest.class),ErrorCode.quest_quest_not_found);
        QuestPb qMsg = quest.getMSG(user.getId()).build();
        questsMsg.addQuest(qMsg);
    }
    resp.getOutputStream().write(Base64.encode(questsMsg.build().toByteArray(), Base64.DEFAULT));
   
  }
 
},

//Input:   key : the key of requester
//
//Output:  QuestsPb message, containing all the quests owned by me. 
  get_quests("/quest/", true) {

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {

      Key userKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
      
      // get all the quests owned by the user
      List<Quest> quests = dao.query(Quest.class).setAncestor(userKey).prepare().asList();
      
      // prepare output message
      Quests.Builder questsMsg = Quests.newBuilder();
      for (Quest q : quests){
        QuestPb qMsg = q.getMSG(userKey.getId()).build();
        questsMsg.addQuest(qMsg);
      }
      resp.getOutputStream().write(
          Base64.encode(questsMsg.build().toByteArray(), Base64.DEFAULT));

    }

  },



/*

// quest owner can update all applicants, 
// owner can use this api to : confirm, reject, reward the applicants.
// owner is suggested to use "assign_quest" API to create a quest and assign it to receivers. 
// applicants can use this api to accept, reject , change the bid
//Input:       id  :quest id, 
//       user_id   :whose application is updated
//       applicant  :new applicant message.
//Output: notify the owner and related applicants
update_applicants("/quest/",true){

  @Override
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
    
    // get input
    
    Key questKey = getQuestKey(req);
    Key userKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
    List<Applicant> applicants  = getApplicants(req);
    
    // get quest from datastore 
    Quest quest = dao.get(questKey, Quest.class);
    
    check(!quest.isDeal(),ErrorCode.quest_is_deal);  
    
    ArrayList<Long> receivers = new ArrayList<Long>(applicants.size()+1);
    receivers.add(userKey.getId());
    
      // only update when user is  owner, or the same applicant 
    
      for(Applicant app:applicants){
        if(quest.getParent().getId()==userKey.getId() ||app.getUserId()==userKey.getId()){

          int index = quest.updateApplicant(app);
          receivers.add(app.getUserId());
          
          // response is the status of the applicant
          resp.getWriter().write(Integer.toString(quest.getApplicants().getApplicant(index).getType().getNumber()));
          resp.getWriter().write(",");
        }
      }
      dao.save(quest);
   
    //TODO: redirect to back-end
    //push message to quest owner and related bidders
    
    push(receivers,"quest",KeyFactory.keyToString(quest.getEntityKey()));

  }


 
}, */
   
   //Input:       id  :quest id, 
   //       owner_id  :quest owner's id
   //       user_id   :whose feeds need deletion
   //Output: delete related feeds and notify all the receivers
   delete_feed("/queue/",false){

     @Override
     public void handle(HttpServletRequest req, HttpServletResponse resp)
         throws ApiException, IOException {
       //get receivers id list
       long receiverIds[] = ParamKey.user_id.getLongs(req, -1);
       long questId = ParamKey.id.getLong(req, -1);

       for(long id:receiverIds){

         Key receiverKey =  KeyFactory.createKey("User", id);
         Feed feed = dao.querySingle(Feed.class,receiverKey);
         if(feed!=null){

           int i = feed.findQuest(questId,id);
           if(i!=-1){
             feed.deleteQuest(i);

             System.out.println(feed.toString());
             dao.save(feed,receiverKey);
           }
         }
       }
       push(receiverIds,"Feed","delete");
     }
     
      
    },
   
   
   generate_feed("/queue/",false){

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      //get receivers id list
      long receiverIds[] = ParamKey.user_id.getLongs(req, -1);
      //get quest
      String questString = ParamKey.quest.getValue(req);
      QuestPb.Builder questMsg = QuestPb.parseFrom(Base64.decode(questString,Base64.DEFAULT)).toBuilder();
      String message = req.getParameter("message");
      for(long id:receiverIds){

        Key receiverKey =  KeyFactory.createKey("User", id);
        Feed feed = dao.querySingle(Feed.class,receiverKey);
        if(feed==null){
          feed = new Feed();
        }
        int i = feed.findQuest(questMsg.getId(),questMsg.getOwnerId());
        if(i!=-1){
          feed.updateQuest(i,questMsg);
        }else{
          feed.addQuest(0,questMsg);
        }
        System.out.println(feed.toString());
        dao.save(feed,receiverKey);
      }
      push(receiverIds,"Feed",message);
    }
    
     
   },
   
   //Input : key: sender's key
   //         id: receivers' id
   //       long: gold
   //Output: long: gold balance     
   
   send_gold("/reward/",true){

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp)
        throws ApiException, IOException {
      
      Key senderKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
      Currency amount = Currency.parseFrom(ParamKey.currency.getPb(req));
      
      long receiverId = ParamKey.user_id.getLong(req, -1);
      check(receiverId!=-1,ErrorCode.quest_receiver_not_found);
      transferGold(senderKey.getId(),receiverId,amount.getGold());
      

    }
    
    
     
   }
   
   ;
   
  
  public final String url;
  public final boolean requiresHmac;
  
  private Queue queue = QueueFactory.getDefaultQueue();


  private API(String urlPrefix, boolean requiresHmac) {
    url = urlPrefix + name();
    this.requiresHmac = requiresHmac;
  }


  protected Applicant getApplicant(HttpServletRequest req) throws InvalidProtocolBufferException {
    String questString = ParamKey.applicant.getValue(req);
    
    Applicant app= Applicant.parseFrom(Base64.decode(questString,Base64.DEFAULT));
    
   return app;
  }


  protected void deleteFeed(long[] receiverIds, long questId, long ownerId) {

    
    
    TaskOptions task  = withUrl("/queue/delete_feed").method(TaskOptions.Method.POST);
    for(long id:receiverIds){
      task.param("user_id", Long.toString(id));
    }
    task.param("id", Long.toString(questId));
    task.param(ParamKey.owner_id.name(), Long.toString(ownerId));
    queue.add(task);
  
    
  }


  protected static void push(long[] ids, String data_key, String data_value) throws IOException {

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
  
  private static void push(ArrayList<Long> receivers, String data_key,
      String data_value) throws IOException {
    long ids[] = getLongs(receivers.toArray());
    push(ids,data_key,data_value);
    
  }
  
  private static long[] getLongs(Object[] array) {
    long[] la = new long [array.length];
    for(int i=0; i<array.length;i++){
      la[i]  = ((Long)array[i]).longValue();
    }

    return la;
  }

  protected void generateFeed(long[] receiverIds, QuestPb.Builder questMsg, String pushMsg) {
    // TODO Auto-generated method stub
    
    
    TaskOptions task  = withUrl("/queue/generate_feed").method(TaskOptions.Method.POST);
    for(long id:receiverIds){
      task.param("user_id", Long.toString(id));
    }
    task.param("quest",(Base64.encode(questMsg.build().toByteArray(),Base64.DEFAULT)));
    task.param("message",pushMsg);
    
    queue.add(task);
    
  }
   


  private static void transferGold(long senderId, long receiverId, long amount){
    Key senderKey = KeyFactory.createKey("User", senderId);
    Key receiverKey = KeyFactory.createKey("User", receiverId);
    
    dao.begin(true);
    User sender = dao.get(senderKey, User.class);
    User receiver = dao.get(receiverKey, User.class);

    long beforeSend = sender.getGoldBalance();
    long beforeReceive = receiver.getGoldBalance();
    long afterSend = beforeSend - amount;
    long afterReceive = beforeReceive + amount;
    sender.setGoldBalance(afterSend);
    receiver.setGoldBalance(afterReceive);

    dao.save(sender);
    dao.save(receiver);
    dao.commit();
    
  }


  protected void rewardInvitors(long newUserID, Friends friends) throws ApiException {
    // TODO Auto-generated method stub
    ApiException error=null;
    for (Friend invitor:friends.getFriendList()){
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
        inv.addFriend(newUserID, Friendship.INVITED);
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
  void execute(HttpServletRequest req, HttpServletResponse resp,
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
        resp.getOutputStream().write(Integer.toString(e.error).getBytes());
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
      info("api error with code = %d", e.error);
      resp.getOutputStream().write(Integer.toString(e.error).getBytes());
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

  private static Quest saveQuest(QuestPb questMsg, Key ownerKey) throws ApiException {
    Quest quest = null;
    // repeated save:
    if(questMsg.hasId()&&questMsg.hasOwnerId()&&questMsg.getOwnerId()==ownerKey.getId()){
        Key questKey = KeyFactory.createKey(ownerKey, "Quest", questMsg.getId());
        quest = checkNotNull(dao.get(questKey, Quest.class),ErrorCode.quest_quest_not_found);
        check(quest.isDraft(),ErrorCode.quest_not_draft);
        quest.updateQuest(questMsg);
    }else{
    // first time save
        quest = new Quest(questMsg);
    }
    
    quest.setStatus(QuestPb.Status.DRAFT);
    dao.save(quest, ownerKey);
    return quest;
  }


  private static QuestPb getQuestPb(HttpServletRequest req)
      throws InvalidProtocolBufferException {
    String questString = ParamKey.quest.getValue(req);
     
     QuestPb questMsg = QuestPb.parseFrom(Base64.decode(questString,Base64.DEFAULT));
    return questMsg;
  }

  private static List<Applicant> getApplicants(HttpServletRequest req)
      throws InvalidProtocolBufferException {
    String questString = ParamKey.applicants.getValue(req);
     
     Applicants apps= Applicants.parseFrom(Base64.decode(questString,Base64.DEFAULT));
     
    return apps.getApplicantList();
  }

  private static Key getQuestKeyFromReq(HttpServletRequest req) {
    long questId = ParamKey.id.getLong(req, -1);
     long questOwnerId = ParamKey.owner_id.getLong(req, -1);
     Key questOwnerKey = KeyFactory.createKey("User", questOwnerId);
     Key questKey = KeyFactory.createKey(questOwnerKey, "Quest", questId);
    return questKey;
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
