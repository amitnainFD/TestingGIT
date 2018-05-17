/*
 * Created on Jun 2, 2005
  */
package com.freshdirect.fdstore.referral;

import java.lang.String;
import java.io.Serializable;
import java.util.Date;


/**
 * @author jng
  */
public interface FDReferralProgramI extends Serializable {

	public String getCampaignCode();
	public void setCampaignCode(String campaignCode);

	public EnumReferralProgramStatus getStatus();
	public void setStatus(EnumReferralProgramStatus status);
	
	public Date getStartDate();
	public void setStartDate(Date startDate);

	public Date getExpirationDate();
	public void setExpirationDate(Date expirationDate);
	
	public String getDescription();
	public void setDescription(String description);
	
}
