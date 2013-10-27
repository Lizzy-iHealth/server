package com.gm.server.model;

import java.util.Date;

import com.gm.common.model.Rpc.Currency;
import com.gm.common.model.Rpc.TransactionPb;

@Entity
public class TransactionRecord extends Persistable<TransactionRecord>{

  @Property
  private Date time ;
  
  @Property
  private long fromId;
  
  @Property
  private long toId;

  @Property
  private Currency.Builder amount;

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public long getFromId() {
    return fromId;
  }

  public void setFromId(long fromId) {
    this.fromId = fromId;
  }

  public long getToId() {
    return toId;
  }

  public void setToId(long toId) {
    this.toId = toId;
  }

  public Currency.Builder getAmount() {
    return amount;
  }

  public void setAmount(Currency.Builder amount) {
    this.amount = amount;
  }
  
  public TransactionRecord(){
    time = new Date();
  }
  public TransactionRecord(long from, long to, Currency.Builder amount){
    time = new Date();
    fromId = from;
    toId = to;
    this.amount = amount;
  }

  @Override
  public TransactionRecord touch() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public TransactionPb getRecordPb() {
    // TODO Auto-generated method stub
    TransactionPb record = TransactionPb.newBuilder().setFrom(fromId)
                              .setTo(toId)
                              .setAmount(amount)
                              .setTime(time.getTime()).build();
    return record;
    
  }
}
