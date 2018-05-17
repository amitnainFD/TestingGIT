/*
 * Created on Jun 2, 2005
 */
package com.freshdirect.fdstore.referral;

import java.io.Serializable;
import java.util.Date;


/**
 * @author jng
 *
 */
public interface FDReferralLogI extends Serializable {

	public String getReferralId();
	public void setReferralId(String referralId);
	
	public EnumReferralActionType getActionType();
	public void setActionType(EnumReferralActionType actionType);

	public Date getActionDate();
	public void setActionDate(Date actionDate);

	public String getReferralName();
	public void setReferralName(String referralName);
	
	public String getReferralEmailAddress();
	public void setReferralEmailAddress(String referralEmailAddress);
	
}
