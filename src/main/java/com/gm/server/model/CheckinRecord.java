package com.gm.server.model;

import java.util.Date;

import com.gm.common.model.Rpc.CheckinPb;
import com.google.appengine.api.datastore.GeoPt;

@Entity
public class CheckinRecord extends Persistable<CheckinRecord> {

  @Property
  GeoPt geoPoint;

  @Property
  String description;

  @Property
  long checkin_times = 0;

  @Property
  Date last_checkin_time = new Date();
  
  @Property
  Date valid_until = new Date();

  public CheckinRecord() {

  }

  public CheckinRecord(CheckinPb msg) {
    if (msg.hasCheckinTimes()) {
      checkin_times = msg.getCheckinTimes();
    }


    if (msg.hasGeoPoint()) {
      this.geoPoint = new GeoPt(msg.getGeoPoint().getLatitude(), msg
          .getGeoPoint().getLongitude());
      if(msg.getGeoPoint().hasAddress()){
    	  description = msg.getGeoPoint().getAddress();
      }
    }
    
    //overwrite address in GeoPoint
    if (msg.hasDescription()) {
        description = msg.getDescription();
      }
    
    if(msg.hasValidUntil()){
    	this.valid_until = new Date(msg.getValidUntil());
    }
  }

  public GeoPt getGeoPoint() {
    return geoPoint;
  }

  public void setGeoPoint(GeoPt geoPoint) {
    this.geoPoint = geoPoint;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getCheckin_times() {
    return checkin_times;
  }

  public void setCheckin_times(long checkin_times) {
    this.checkin_times = checkin_times;
  }

  public Date getLast_checkin_time() {
    return last_checkin_time;
  }

  public void setLast_checkin_time(Date last_checkin_time) {
    this.last_checkin_time = last_checkin_time;
  }

  @Override
  public CheckinRecord touch() {
    // TODO Auto-generated method stub
    last_checkin_time = new Date();
    return null;
  }

}
