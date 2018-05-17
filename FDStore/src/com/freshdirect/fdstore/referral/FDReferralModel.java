/*
 * Created on Jun 2, 2005
 */
package com.freshdirect.fdstore.referral;

import java.util.Date;
import java.util.List;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

/**
 * @author jng
 */
public class FDReferralModel extends ModelSupport implements FDReferralI {

	private String referrerCustomerId;
	private String referrerFirstName;
	private String referrerLastName;
	private String referrerEmailAddress;
	private Date referralDate;
	private EnumReferralStatus status;
	private List referralLogList;
	private String referralProgramId;

	public FDReferralModel() {super();}
	
	public FDReferralModel(PrimaryKey pk) {
		super.setPK(pk);
	}
	
	public String getReferrerCustomerId() {
		return this.referrerCustomerId;
	}

	public void setReferrerCustomerId(String referrerCustomerId) {
		this.referrerCustomerId=referrerCustomerId;
	}

	public String getReferrerFirstName() {
		return this.referrerFirstName;
	}

	public void setReferrerFirstName(String referrerFirstName) {
		this.referrerFirstName=referrerFirstName;
	}

	public String getReferrerLastName() {
		return this.referrerLastName;
	}

	public void setReferrerLastName(String referrerLastName) {
		this.referrerLastName=referrerLastName;
	}

	public String getReferrerEmailAddress() {
		return this.referrerEmailAddress;
	}

	public void setReferrerEmailAddress(String referrerEmailAddress) {
		this.referrerEmailAddress=referrerEmailAddress;
	}
	
	public Date getReferralDate() {
		return referralDate;
	}

	public void setReferralDate(Date referralDate) {
		this.referralDate=referralDate;
	}

	public EnumReferralStatus getStatus() {
		return this.status;
	}

	public void setStatus(EnumReferralStatus status) {
		this.status=status;
	}

	public List getReferralLogList() {
		return this.referralLogList;
	}

	public void setReferralLogList(List referralLogList) {
		this.referralLogList=referralLogList;
	}
	
	public String getReferralProgramId() {
		return this.referralProgramId;
	}

	public void setReferralProgramId(String referralProgramId) {
		this.referralProgramId=referralProgramId;
	}
	
}