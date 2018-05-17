/*
 * Created on Jun 22, 2005
 *
 */
package com.freshdirect.fdstore.referral;

import java.io.Serializable;
import java.util.Date;


/**
 * @author jng
 *
 */
public class FDReferralReportLine implements Serializable {

	private String referralId;
	private String referrerCustomerId;
	private Date referralDate;
	private EnumReferralStatus referralStatus;
	private String emailAddress;
	private String name;
	private String referralProgramId;
	private String referralProgramCampaignCode;
	private String referralProgramDesc;
	private EnumReferralProgramStatus referralProgramStatus;
	private Date referralProgramStartDate;
	private Date referralProgramExpirationDate;
	private boolean isReferralAccepted;
	private int numDeliveredOrders;
	private String emailAddress2;
	
	public String getReferralId() {return this.referralId;}
	public void setReferralId(String referralId) {this.referralId=referralId;}

	public String getReferrerCustomerId() {return this.referrerCustomerId;}
	public void setReferrerCustomerId(String referrerCustomerId) {this.referrerCustomerId=referrerCustomerId;}
	
	public Date getReferralDate() {return this.referralDate;}
	public void setReferralDate(Date referralDate) {this.referralDate=referralDate;}
	
	public EnumReferralStatus getReferralStatus() {return this.referralStatus;}
	public void setReferralStatus(EnumReferralStatus referralStatus) {this.referralStatus=referralStatus;}
	
	public String getEmailAddress() {return this.emailAddress;}
	public void setEmailAddress(String emailAddress) {this.emailAddress=emailAddress;}
	
	public String getName() {return this.name;}
	public void setName(String name) {this.name=name;}

	public String getReferralProgramId() {return this.referralProgramId;}
	public void setReferralProgramId(String referralProgramId) {this.referralProgramId=referralProgramId;}
	
	public String getReferralProgramCampaignCode() {return this.referralProgramCampaignCode;}
	public void setReferralProgramCampaignCode(String referralProgramCampaignCode) {this.referralProgramCampaignCode=referralProgramCampaignCode;}

	public String getReferralProgramDesc() {return this.referralProgramDesc;}
	public void setReferralProgramDesc(String referralProgramDesc) {this.referralProgramDesc=referralProgramDesc;}
	
	public EnumReferralProgramStatus getReferralProgramStatus() {return this.referralProgramStatus;}
	public void setReferralProgramStatus(EnumReferralProgramStatus referralProgramStatus) {this.referralProgramStatus=referralProgramStatus;}

	public Date getReferralProgramStartDate() {return this.referralProgramStartDate;}
	public void setReferralProgramStartDate(Date referralProgramStartDate) {this.referralProgramStartDate=referralProgramStartDate;}
	
	public Date getReferralProgramExpirationDate() {return this.referralProgramExpirationDate;}
	public void setReferralProgramExpirationDate(Date referralProgramExpirationDate) {this.referralProgramExpirationDate=referralProgramExpirationDate;}

	public boolean getIsReferralAccepted() {return this.isReferralAccepted;}
	public void setIsReferralAccepted(boolean isReferralAccepted) {this.isReferralAccepted=isReferralAccepted;}
	
	public int getNumDeliveredOrders() {return this.numDeliveredOrders;}
	public void setNumDeliveredOrders(int numDeliveredOrders) {this.numDeliveredOrders=numDeliveredOrders;}

	public String getEmailAddress2() {return this.emailAddress2;}
	public void setEmailAddress2(String emailAddress2) {this.emailAddress2=emailAddress2;}
	
}
