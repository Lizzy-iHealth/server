package com.gm.server;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gm.common.model.Rpc.Applicant;
import com.gm.common.model.Rpc.Applicant.Status;
import com.gm.common.net.ErrorCode;
import com.gm.server.model.Quest;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class UpdateActivityStatusServlet extends APIServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  
  // quest owner can update all applicants, 
  // owner can use this api to update the status after: confirm, reject, reward the applicants.
  // applicants can use this api to update the status after :reject, claim reward 
  //Input:       id  :quest id, 
  //         owner_id:quest owner id
  //Applicant pb :Applicant message containing applicant ID and new applicant status.
  //Output:      :updated Applicant message
  //              notify the owner and related applicants
  public void handle(HttpServletRequest req, HttpServletResponse resp)
      throws ApiException, IOException {
     
     // get input
     
     Key questKey = getQuestKeyFromReq(req);
     Key userKey = KeyFactory.stringToKey(ParamKey.key.getValue(req));
     Applicant app = Applicant.parseFrom(ParamKey.pb.getPb(req));
     
     
     Applicant.Status status = updateApplicantStatus(questKey, userKey, app);
     
     
           // response is the status of the applicant
     resp.getWriter().write(Integer.toString(status.getNumber()));
         
         
 }

  protected Status updateApplicantStatus(Key questKey, Key userKey,
	      Applicant app) throws ApiException, IOException {
	    checkNotNull(app, ErrorCode.quest_applicant_not_found);
	

	    // get quest from datastore
	    Quest quest = dao.get(questKey, Quest.class);
	    checkNotNull(quest, ErrorCode.quest_quest_not_found);
	    check(!quest.isDeal(), ErrorCode.quest_is_deal);

	    // only update when user is owner, or the same applicant
	    long ownerId = quest.getParent().getId();
	    long applicantId = app.getUserId();
	    long userId = userKey.getId();
	    
	    check((userId == ownerId || userId == applicantId),
	        ErrorCode.quest_access_denied);
	    
	   Applicant.Status status =  updateApplicantStatus(quest,applicantId,app.getType());
	   
	    if (userId == ownerId) {
	    	long receivers[]={ applicantId};
	        push(receivers, "activity", KeyFactory.keyToString(quest.getEntityKey()));
	      }else{
	    	    long receivers[] = {ownerId};
	    	      push(receivers, "quest", KeyFactory.keyToString(quest.getEntityKey()));
	      }
	    return status;
  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    execute(req, resp);
  }
}
