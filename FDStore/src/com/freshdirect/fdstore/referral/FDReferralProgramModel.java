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
public class FDReferralProgramModel extends ModelSupport implements FDReferralProgramI {

	private String campaignCode;
	private Date startDate;
	private Date expirationDate;
	private EnumReferralProgramStatus status;
	private String description;
	
	public FDReferralProgramModel() {super();}

	public FDReferralProgramModel(PrimaryKey pk) {
		super.setPK(pk);
	}

	public String getCampaignCode() {
		return this.campaignCode;
	}

	public void setCampaignCode(String campaignCode) {
		this.campaignCode=campaignCode;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate=startDate;
	}

	public Date getExpirationDate() {
		return this.expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate=expirationDate;
	}

	public EnumReferralProgramStatus getStatus() {
		return status;
	}

	public void setStatus(EnumReferralProgramStatus status) {
		this.status=status;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description=description;
	}
	
}
