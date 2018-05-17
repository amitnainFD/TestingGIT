/*
 * Created on Jun 2, 2005
 */
package com.freshdirect.fdstore.referral;

import java.util.Date;
import java.io.Serializable;


/**
 * @author jng
 */
public interface FDReferralI extends Serializable {

	public String getReferrerCustomerId();
	public void setReferrerCustomerId(String referrerCustomerId);

	public String getReferrerFirstName();
	public void setReferrerFirstName(String referrerFirstName);

	public String getReferrerLastName();
	public void setReferrerLastName(String referrerLastName);

	public String getReferrerEmailAddress();
	public void setReferrerEmailAddress(String referrerEmailAddress);

	public Date getReferralDate();
	public void setReferralDate(Date referralDate);

	public EnumReferralStatus getStatus();
	public void setStatus(EnumReferralStatus status);

	public String getReferralProgramId();
	public void setReferralProgramId(String referralProgramId);
}
