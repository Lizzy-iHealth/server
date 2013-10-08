package com.gm.server;

public enum FriendTag {
  added, //add by me, not confirmed
  confirmed,//confirmed both direction
  needMyConfirm,//others add me as their friend
  invited,//friends as pending users
  watched,//whose post has high priority
  blocked;//whose friend request and all activities are hidden
  
}
