package com.freshdirect.fdstore.referral;

import java.util.Date;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public class ReferralHistory extends ModelSupport {
		
	private String referralProgramId=null;
	private Date dateCreated=null;
    private String fdUserId=null; 
	private String refTrkKeyDtls;
	private String refprgInvtId;
	
	
	public ReferralHistory(){
		super();
	}
	
	public ReferralHistory(PrimaryKey key){
		super();
		this.setPK(key);
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getFdUserId() {
		return fdUserId;
	}

	public void setFdUserId(String fdUserId) {
		this.fdUserId = fdUserId;
	}

	public String getReferralProgramId() {
		return referralProgramId;
	}

	public void setReferralProgramId(String referralProgramId) {
		this.referralProgramId = referralProgramId;
	}

	public String getRefprgInvtId() {
		return refprgInvtId;
	}

	public void setRefprgInvtId(String refprgInvtId) {
		this.refprgInvtId = refprgInvtId;
	}

	public String getRefTrkKeyDtls() {
		return refTrkKeyDtls;
	}

	public void setRefTrkKeyDtls(String refTrkKeyDtls) {
		this.refTrkKeyDtls = refTrkKeyDtls;
	}
	
}
