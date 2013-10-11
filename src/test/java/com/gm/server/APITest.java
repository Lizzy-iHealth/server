package com.gm.server;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.gm.common.net.ErrorCode;
import com.gm.server.model.Model.Friend;
import com.gm.server.model.Model.Friend.Type;
import com.gm.server.model.PendingUser;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.KeyFactory;


public class APITest extends ModelTest {
  @Test
  public void testBlockFriends() throws IOException{
    //prepare datastore: user and friend are friends.
    User user = new User("a12345","password","secret");
    User friend = new User("b12345","password","secret");
    dao.save(user);
    dao.save(friend);
    user.addFriend(friend.getUserID(), Type.CONFIRMED);
    friend.addFriend(user.getUserID(), Type.CONFIRMED);
    dao.save(user);
    dao.save(friend);
    
    //verify status before test
    User userInDB = dao.get(user.getKey(), User.class);
    User friendInDB = dao.get(friend.getKey(), User.class);
    assertEquals(1,userInDB.getFriendship().getFriendCount());
    assertEquals(1,friendInDB.getFriendship().getFriendCount());
    
    //user block friend.
    HttpServletRequest req = super.getMockRequestWithUser(user);
    HttpServletResponse resp  = mock(HttpServletResponse.class);
    PrintWriter writer = mock (PrintWriter.class);
    String[] deleteIds = {Long.toString(friend.getUserID())};
    when(req.getParameterValues(ParamKey.user_id.name())).thenReturn(deleteIds);
    when(resp.getWriter()).thenReturn(writer);
    API.block_friends.execute(req, resp,false);
    
     userInDB = dao.get(user.getKey(), User.class);
     friendInDB = dao.get(friend.getKey(), User.class);
    assertEquals(1,userInDB.getFriendship().getFriendCount());
    assertEquals(Type.BLOCKED,userInDB.getFriendship().getFriend(0).getType());
    assertEquals(0,friendInDB.getFriendship().getFriendCount());

    // block again, nothing should happened:
 
    API.block_friends.execute(req, resp,false);
    userInDB = dao.get(user.getKey(), User.class);
    friendInDB = dao.get(friend.getKey(), User.class);
    assertEquals(Type.BLOCKED,userInDB.getFriendship().getFriend(0).getType());
    assertEquals(0,friendInDB.getFriendship().getFriendCount());
    
    

  }
  
  @Test
  public void testDeleteFriends() throws IOException{
    //prepare datastore: user and friend are friends.
    User user = new User("a12345","password","secret");
    User friend = new User("b12345","password","secret");
    dao.save(user);
    dao.save(friend);
    user.addFriend(friend.getUserID(), Type.CONFIRMED);
    friend.addFriend(user.getUserID(), Type.CONFIRMED);
    dao.save(user);
    dao.save(friend);
    
    //before:
    User userInDB = dao.get(user.getKey(), User.class);
    User friendInDB = dao.get(friend.getKey(), User.class);
    assertEquals(1,userInDB.getFriendship().getFriendCount());
    assertEquals(1,friendInDB.getFriendship().getFriendCount());
    
    //delete
    HttpServletRequest req = super.getMockRequestWithUser(user);
    HttpServletResponse resp  = mock(HttpServletResponse.class);
    PrintWriter writer = mock (PrintWriter.class);
    String[] deleteIds = {Long.toString(friend.getUserID())};
    when(req.getParameterValues(ParamKey.user_id.name())).thenReturn(deleteIds);
    when(resp.getWriter()).thenReturn(writer);
    API.delete_friends.execute(req, resp,false);
    
     userInDB = dao.get(user.getKey(), User.class);
     friendInDB = dao.get(friend.getKey(), User.class);
    assertEquals(0,userInDB.getFriendship().getFriendCount());
    assertEquals(0,friendInDB.getFriendship().getFriendCount());

    // delete again, nothing should happened:
 
    API.get_friends.execute(req, resp,false);
    userInDB = dao.get(user.getKey(), User.class);
    friendInDB = dao.get(friend.getKey(), User.class);
    assertEquals(0,userInDB.getFriendship().getFriendCount());
    assertEquals(0,friendInDB.getFriendship().getFriendCount());

  }
  @Test
  public void testGetFriends() throws IOException{
    User user = new User("a12345","password","secret");
    User friend = new User("b12345","password","secret");
    dao.save(user);
    dao.save(friend);
    HttpServletRequest req = super.getMockRequestWithUser(user);
    HttpServletResponse resp  = mock(HttpServletResponse.class);
    PrintWriter writer = mock (PrintWriter.class);
    when(resp.getWriter()).thenReturn(writer);
    API.get_friends.execute(req, resp,false);
    
    assertEquals(0,user.getFriendship().getFriendCount());
    
    // Let user has a friend, then test again:
    user.addFriend(friend.getUserID(), Type.CONFIRMED);
    friend.addFriend(user.getUserID(), Type.CONFIRMED);
    dao.save(user);
    dao.save(friend);
    API.get_friends.execute(req, resp,false);
    
    User userInDB = dao.get(user.getKey(), User.class);
    verify(writer).print(userInDB.getFriendship().build().toByteArray());
    assertEquals(1,userInDB.getFriendship().getFriendCount());
    assertEquals(friend.getUserID(),userInDB.getFriendship().getFriend(0).getId());
    assertEquals(Type.CONFIRMED,userInDB.getFriendship().getFriend(0).getType());
  }
  @Test
  public void testInviteFriends() throws IOException{
    User user = new User("a12345","password","secret");
    String[] friend_phone = {"22345","33445"};
    dao.save(user);
    HttpServletRequest req = super.getMockRequestWithUser(user);
    HttpServletResponse resp  = mock(HttpServletResponse.class);
    when(req.getParameterValues(ParamKey.phone.name())).thenReturn(friend_phone);
    API.invite_friends.execute(req, resp,false);
    
    List<PendingUser> pu = dao.query(PendingUser.class).sortBy("phone",false).prepare().asList();
    assertEquals(2,pu.size());
    assertEquals(pu.get(0).getPhone(),friend_phone[0]);
    assertEquals(pu.get(0).getInvitors().getFriend(0).getId(),user.getEntityKey().getId());
    assertEquals(pu.get(1).getPhone(),friend_phone[1]);
    assertEquals(pu.get(1).getInvitors().getFriend(0).getId(),user.getEntityKey().getId());
  }
  
  @Test
  public void testAddFriends() throws IOException {
    User[] users = {
        new User("a12345","password","secret"),
        new User("b12345","password","secret"),
        new User("c12345","password","secret")
    };
    
    String[] userkeys = new String[users.length];
    long[] ids = new long[users.length];
    for(int i=0;i<users.length;i++){
      users[i].setDeviceID("APA91bFWFxgXtR57p3Jj2umYFFV8-U1N9PKKLQydheMybhU_2DxdngHbuYijPRHc1Y2a9dLkhdu9pyLCNd61uRBn9d2i6dggDxjMSkADyAET6rHGCQ9PFQi7HAc_hIsRBA_Z4LAkUddPSH9NxTvIjJZe-ImYHpoNgA");
      dao.save(users[i]);
      userkeys[i]=users[i].getKey();
      ids[i]=KeyFactory.stringToKey(userkeys[i]).getId();
    }
    AddFriendsServlet af = new AddFriendsServlet();

    
    //a add b
    HttpServletRequest req = getMockRequestWithUser(users[0]);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    String[] fl = {Long.toString(ids[1])} ;
    when(req.getParameterValues(ParamKey.user_id.name())).thenReturn(fl);

    API.add_friends.execute(req, resp,false);
   verify(resp).setStatus(HttpServletResponse.SC_OK);
   
   User ua = dao.get(users[0].getEntityKey(), User.class);
   List<Friend> uaf = ua.getFriendship().getFriendList();
   assertEquals(uaf.size(),1);
   assertEquals(uaf.get(0).getId(),ids[1]);
   assertEquals(uaf.get(0).getType(),Type.ADDED);
   
   User ub = dao.get(users[1].getEntityKey(), User.class);
   List<Friend> ubf = ub.getFriendship().getFriendList();
   assertEquals(ubf.size(),1);
   assertEquals(ubf.get(0).getId(),ids[0]);
   assertEquals(ubf.get(0).getType(),Type.WAIT_MY_CONFIRM);
   
   //b add a and c
   HttpServletRequest req2 = getMockRequestWithUser(users[1]);
   HttpServletResponse resp2= mock(HttpServletResponse.class);
   String[] bfl = {Long.toString(ids[0]),Long.toString(ids[2])} ;
   when(req2.getParameterValues(ParamKey.user_id.name())).thenReturn(bfl);
   API.add_friends.execute(req2, resp2,false);
  // af.doPost(req2, resp2);
  verify(resp2).setStatus(HttpServletResponse.SC_OK);
  
   ua = dao.get(users[0].getEntityKey(), User.class);
   uaf = ua.getFriendship().getFriendList();
  assertEquals(uaf.size(),1);
  assertEquals(uaf.get(0).getId(),ids[1]);
  assertEquals(uaf.get(0).getType(),Type.CONFIRMED);
  
  ub = dao.get(users[1].getEntityKey(), User.class);
  ubf = ub.getFriendship().getFriendList();
  assertEquals(ubf.size(),2);
  assertEquals(ubf.get(0).getId(),ids[0]);
  assertEquals(ubf.get(1).getId(),ids[2]);
  assertEquals(ubf.get(0).getType(),Type.CONFIRMED);
  assertEquals(ubf.get(1).getType(),Type.ADDED);
  
  User uc = dao.get(users[2].getEntityKey(), User.class);
  List<Friend> ucf = uc.getFriendship().getFriendList();
  assertEquals(ucf.size(),1);
  assertEquals(ucf.get(0).getId(),ids[1]);
  assertEquals(ucf.get(0).getType(),Type.WAIT_MY_CONFIRM);
  
  //repeated add after confirm: a add b again
  HttpServletRequest req3 = getMockRequestWithUser(users[0]);
  HttpServletResponse resp3 = mock(HttpServletResponse.class);
  String[] fl3 = {Long.toString(ids[1])} ;
  when(req3.getParameterValues(ParamKey.user_id.name())).thenReturn(fl3);

  API.add_friends.execute(req3, resp3,false);
  //af.doPost(req3, resp3);
 verify(resp3).setStatus(HttpServletResponse.SC_OK);
 
  ua = dao.get(users[0].getEntityKey(), User.class);
  uaf = ua.getFriendship().getFriendList();
 assertEquals(uaf.size(),1);
 assertEquals(uaf.get(0).getId(),ids[1]);
 assertEquals(uaf.get(0).getType(),Type.CONFIRMED);
 
 ub = dao.get(users[1].getEntityKey(), User.class);
 ubf = ub.getFriendship().getFriendList();
 assertEquals(ubf.size(),2);
 assertEquals(ubf.get(0).getId(),ids[0]);
 assertEquals(ubf.get(0).getType(),Type.CONFIRMED);
 
 uc = dao.get(users[2].getEntityKey(), User.class);
 ucf = uc.getFriendship().getFriendList();
 assertEquals(ucf.size(),1);
 assertEquals(ucf.get(0).getId(),ids[1]);
 assertEquals(ucf.get(0).getType(),Type.WAIT_MY_CONFIRM);
   
 //add wrong user id: a add "5"
 HttpServletRequest req4 = getMockRequestWithUser(users[0]);
 HttpServletResponse resp4 = mock(HttpServletResponse.class);
 String[] fl4 = {"5"} ;
 when(req4.getParameterValues(ParamKey.user_id.name())).thenReturn(fl4);
 PrintWriter writer = mock(PrintWriter.class);
 when(resp4.getWriter()).thenReturn(writer);
 
 API.add_friends.execute(req4, resp4,false);
 //af.doPost(req4, resp4);

 verify(resp4).setStatus(HttpServletResponse.SC_BAD_REQUEST);

 verify(writer).print(ErrorCode.auth_user_not_registered);
 ua = dao.get(users[0].getEntityKey(), User.class);
 uaf = ua.getFriendship().getFriendList();
assertEquals(uaf.size(),1);
assertEquals(uaf.get(0).getId(),ids[1]);
assertEquals(uaf.get(0).getType(),Type.CONFIRMED);

//add 3 friends, with a wrong user id in the middle: a add b, "5" and c, b,c will be successfully added.
HttpServletRequest req5 = getMockRequestWithUser(users[0]);
HttpServletResponse resp5 = mock(HttpServletResponse.class);
String[] fl5 = {Long.toString(ids[1]),"5",Long.toString(ids[2])} ;
when(req5.getParameterValues(ParamKey.user_id.name())).thenReturn(fl5);
PrintWriter writer5 = mock(PrintWriter.class);
when(resp5.getWriter()).thenReturn(writer5);

API.add_friends.execute(req5, resp5,false);
//af.doPost(req5, resp5);
verify(resp5).setStatus(HttpServletResponse.SC_BAD_REQUEST);

verify(writer5).print(ErrorCode.auth_user_not_registered);
ua = dao.get(users[0].getEntityKey(), User.class);
uaf = ua.getFriendship().getFriendList();
assertEquals(2,uaf.size());
assertEquals(ids[1],uaf.get(0).getId());
assertEquals(Type.CONFIRMED,uaf.get(0).getType());
assertEquals(ids[2],uaf.get(1).getId());
assertEquals(Type.ADDED,uaf.get(1).getType());

ub = dao.get(users[1].getEntityKey(), User.class);
ubf = ub.getFriendship().getFriendList();
assertEquals(ubf.size(),2);
assertEquals(ubf.get(0).getId(),ids[0]);
assertEquals(ubf.get(0).getType(),Type.CONFIRMED);
assertEquals(ubf.get(1).getId(),ids[2]);
assertEquals(ubf.get(1).getType(),Type.ADDED);

uc = dao.get(users[2].getEntityKey(), User.class);
ucf = uc.getFriendship().getFriendList();
assertEquals(ucf.size(),2);
assertEquals(ucf.get(0).getId(),ids[1]);
assertEquals(ucf.get(0).getType(),Type.WAIT_MY_CONFIRM);
assertEquals(ucf.get(1).getId(),ids[0]);
assertEquals(ucf.get(1).getType(),Type.WAIT_MY_CONFIRM);
}


@Test
public void testDeviceServlet() throws IOException {
  User userInDB = new User("12345","password","secret");
  dao.save(userInDB);

 HttpServletRequest req = getMockRequestWithUser(userInDB);
 HttpServletResponse resp = mock(HttpServletResponse.class);
 
 String  deviceID= new String("My Device ID");
 when(req.getParameter("device_id")).thenReturn(deviceID);
 DeviceServlet ds = new DeviceServlet();
 
 API.device.execute(req, resp,false);
 //ds.doPost(req, resp);
 
 User user = dao.get(userInDB.getEntityKey(), User.class);
 
 assertEquals(deviceID,user.getDeviceID());
 
}

 /*
  @Test
  public void testPing() {
    String key = UUID.randomUUID().toString();
    String secret = UUID.randomUUID().toString();
    String phone = "1234567";
    
    User user = new User(phone,"password",secret,key);
    dao.create(user);
    
    // |------------ body -------------|
    // ........&key=....&hmac=..........
    // |--- message ---||-- hmacPart --|
    String message = "phone=1234567, friendids=[1,2,3] test message&key=" + key;
    String hmac = Hmac.generate(message, secret);
    String body = message + "&hmac=" + hmac;
    
    //TODO: find a way to mock ServletInputStream
    
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    
    
  }
  */

}
