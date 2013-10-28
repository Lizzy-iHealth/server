package com.gm.server;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Applicant;
import com.gm.common.model.Rpc.Applicant.Status;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.CheckinRecord;
import com.gm.server.model.Quest;
import com.gm.server.model.User;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class SubmitDailyQuestServlet extends APIServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
  
  //Input Param: "key"        user's index
  //             "id"      quest id
  //             "applicant"   applicant information
  //Output: push notification
  
   
      public void handle(HttpServletRequest req, HttpServletResponse resp)
          throws ApiException, IOException {
        
       
        Key userKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
        long questId = ParamKey.id.getLong(req, -1);
        Applicant applicant = Applicant.parseFrom(ParamKey.applicant.getPb(req));
        Key questKey = KeyFactory.createKey(questAdmin.getAdminKey(), "Quest", questId);
        // retrive quest from data store  
        Quest quest = checkNotNull(dao.get(questKey, Quest.class),ErrorCode.quest_quest_not_found);
        Applicant.Status status = applicant.getType();
        if(status == Applicant.Status.CONFIRMED){
           status = submitDailyQuest(quest,applicant);
        }
        resp.getOutputStream().write(Integer.toString(status.getNumber()).getBytes());
        // push to receivers
        
      }

      private Status submitDailyQuest(Quest quest, Applicant applicant) {
        if (quest.getId()==questAdmin.getQuestKeys()[0].getId()){//Check-IN Quest
            long userId = applicant.getUserId();
            Key userKey = KeyFactory.createKey("User",userId);
            
            //query the lateds checkin record
            FetchOptions option = FetchOptions.Builder.withLimit(1);
            List<CheckinRecord> crs = dao.query(CheckinRecord.class).setAncestor(userKey).sortBy("last_checkin_time", true)
                                  .prepare().asList(option);
          
            if(crs!=null&&crs.size()>0){
              Date now = new Date();
              @SuppressWarnings("deprecation")
              Date cutoff = new Date(now.getYear(),now.getMonth(),now.getDate(),0,0,0);
              Date checkin =  crs.get(0).getLast_checkin_time();
              if(checkin.after(cutoff)){
                return Applicant.Status.PASS;
              }
            }
          }
        return Applicant.Status.FAIL;
      }
        
   
}
