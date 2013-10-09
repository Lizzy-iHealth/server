package com.gm.server.model;
import java.util.Date;

import com.gm.server.model.Model.Friend;
import com.gm.server.model.Model.Friend.Type;
import com.gm.server.model.Model.Friendship;
import com.google.appengine.api.datastore.KeyFactory;

@Entity
  public class PendingUser extends Persistable<PendingUser> {


    public static final boolean existsByPhone(String phone) {
      return DAO.get().querySingle("phone", phone, PendingUser.class) != null;
    }
    
    @Property
    private String phone = "";

    @Property
    private Date createTime = new Date();

    @Property
    private Friendship.Builder invitors= Friendship.newBuilder();
    
    
    
    public Friendship.Builder getInvitors() {
      return invitors;
    }

    public void setInvitors(Friendship.Builder invitors) {
      this.invitors = invitors;
    }


    public String getPhone() {
      return phone;
    }

    public void setPhone(String phone) {
      this.phone = phone;
    }



    public String getKey() {
      return KeyFactory.keyToString(getEntityKey());
    }

    public Date getCreateTime() {
      return createTime;
    }


    public void setCreateTime(Date createTime) {
      this.createTime = createTime;
    }



    

    public PendingUser(String mobileNumber, long invitor) {
      super();
      this.phone = mobileNumber;      
      this.createTime = new Date();
      invitors.addFriend(Friend.newBuilder().setType(Type.WAIT_MY_CONFIRM).setId(invitor).build());

    }

    public PendingUser(){}
    
    public void addInvitor(long id) {
      // TODO Auto-generated method stub
      int i = findFriend(id);
      if(i == -1 ){
          invitors.addFriend(Friend.newBuilder().setType(Type.WAIT_MY_CONFIRM).setId(id).build());
        }
    }
    
    private int findFriend(long id) {
      // TODO Auto-generated method stub
      for (int i=0; i<invitors.getFriendCount();i++){
        Friend f = invitors.getFriend(i);
        if (f.getId()==id){
          return i;
        }
      }
      return -1;
    }

    @Override
    public PendingUser touch() {
      // TODO Auto-generated method stub
      return null;
    }

    
   
  

}
