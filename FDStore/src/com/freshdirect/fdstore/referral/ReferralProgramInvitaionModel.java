package com.freshdirect.fdstore.referral;

import java.util.Date;

import com.freshdirect.fdstore.mail.TellAFriend;
import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public class ReferralProgramInvitaionModel extends ModelSupport  {

	private String referrerCustomerId;
	private String referrerFirstName;
	private String referrerLastName;	
	private Date referralCreatedDate;
	private Date referralModifiedDate;
	private EnumReferralStatus status;	
	private String referralProgramId;
	private String referralName;
	private String referrelEmailAddress;
	
	public ReferralProgramInvitaionModel(){		
	}
	
	public ReferralProgramInvitaionModel(PrimaryKey key){
		this.setPK(key);
	}
	
	public Date getReferralCreatedDate() {
		return referralCreatedDate;
	}
	public void setReferralCreatedDate(Date referralCreatedDate) {
		this.referralCreatedDate = referralCreatedDate;
	}
	public Date getReferralModifiedDate() {
		return referralModifiedDate;
	}
	public void setReferralModifiedDate(Date referralModifiedDate) {
		this.referralModifiedDate = referralModifiedDate;
	}
	public String getReferralName() {
		return referralName;
	}
	public void setReferralName(String referralName) {
		this.referralName = referralName;
	}
	public String getReferralProgramId() {
		return referralProgramId;
	}
	public void setReferralProgramId(String referralProgramId) {
		this.referralProgramId = referralProgramId;
	}
	public String getReferrelEmailAddress() {
		return referrelEmailAddress;
	}
	public void setReferrelEmailAddress(String referrelEmailAddress) {
		this.referrelEmailAddress = referrelEmailAddress;
	}
	public String getReferrerCustomerId() {
		return referrerCustomerId;
	}
	public void setReferrerCustomerId(String referrerCustomerId) {
		this.referrerCustomerId = referrerCustomerId;
	}
	public String getReferrerFirstName() {
		return referrerFirstName;
	}
	public void setReferrerFirstName(String referrerFirstName) {
		this.referrerFirstName = referrerFirstName;
	}
	public String getReferrerLastName() {
		return referrerLastName;
	}
	public void setReferrerLastName(String referrerLastName) {
		this.referrerLastName = referrerLastName;
	}
	public EnumReferralStatus getStatus() {
		return status;
	}
	public void setStatus(EnumReferralStatus status) {
		this.status = status;
	}
	
	public void loadReferralProgInvtModel(TellAFriend friend)
	{
		this.referrerFirstName=friend.getCustomerFirstName();
		this.referrerLastName=friend.getCustomerLastName();		
		this.referrelEmailAddress=friend.getFriendEmail();
		this.referrerCustomerId=friend.getCustomerIdentity().getErpCustomerPK();
		this.referralName=friend.getFriendName();
	}
	
}
