/*
 * Created on Jun 2, 2005
 *
 */
package com.freshdirect.fdstore.referral;

import java.util.Date;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

/**
 * @author jng
 *
 */
public class FDReferralLogModel extends ModelSupport implements FDReferralLogI {

	private String referralId;
	private EnumReferralActionType actionType;
	private Date actionDate;
	private String referralName;
	private String referralEmailAddress;
	
	public FDReferralLogModel() {super();}

	public FDReferralLogModel(PrimaryKey pk) {
		super.setPK(pk);
	}
	
	public String getReferralId() {
		return this.referralId;
	}

	public void setReferralId(String referralId) {
		this.referralId=referralId;
	}

	public EnumReferralActionType getActionType() {
		return this.actionType;
	}

	public void setActionType(EnumReferralActionType actionType) {
		this.actionType=actionType;
	}

	public Date getActionDate() {
		return this.actionDate;
	}
	public void setActionDate(Date actionDate) {
		this.actionDate=actionDate;
	}

	public String getReferralName() {
		return this.referralName;
	}

	public void setReferralName(String referralName) {
		this.referralName=referralName;
	}

	public String getReferralEmailAddress() {
		return this.referralEmailAddress;
	}

	public void setReferralEmailAddress(String referralEmailAddress) {
		this.referralEmailAddress=referralEmailAddress;
	}
	
}
