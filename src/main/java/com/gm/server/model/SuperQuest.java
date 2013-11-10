package com.gm.server.model;

import java.io.IOException;

import com.gm.common.model.Rpc.Applicant;
import com.gm.common.model.Rpc.Applicants;
import com.gm.common.model.Rpc.QuestPb;
import com.gm.common.model.Rpc.Applicant.Status;
import com.gm.server.ApiException;
//Super Quests is for massive applicants.
// The main difference between Quest is that Super Quests' applicants' status is stored in applicant's user entity.
// while Quests' applicants' status is stored in quest's entity.
public class SuperQuest extends Quest {
	
	public int addApplicant(Applicant applicant) {
		return 1;
	}
	
	public int findApplicant(long id) {
		return 1; 
		//TODO: return applicant entity
	}
	
	public long[] getAllApplicantsIds() {
		return null;
	}
	
	public void restart() {
		// TODO Auto-generated method stub
//reset all applicants
	}
	

	protected Status updateApplicantStatus(Quest quest, long applicantId,
			Applicant.Status status) throws ApiException, IOException {
		return null;
	}
	
	
	public QuestPb.Builder getMSG(long id) {
		return null;
	}
	
}
