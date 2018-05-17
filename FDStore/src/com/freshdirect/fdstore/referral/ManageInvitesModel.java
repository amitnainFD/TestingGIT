/*
 * Created on July 22, 2011
 *
 */
package com.freshdirect.fdstore.referral;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.freshdirect.framework.core.ModelSupport;

/**
 * @author jng
 *
 */
public class ManageInvitesModel extends ModelSupport {

	private static final long serialVersionUID = 1L;
	
	private String recipientEmail;
	private String sentDate;
	private String status;
	private String credit;
	private String saleId;
	private String recipientCustId;
	private Date creditIssuedDate;
	
	public void setRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
	}
	public String getRecipientEmail() {
		return recipientEmail;
	}
	public void setSentDate(String sentDate) {
		this.sentDate = sentDate;
	}
	public String getSentDate() {
		return sentDate;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}
	public void setCredit(String credit) {
		this.credit = credit;
	}
	public String getCredit() {
		return credit;
	}
	
	@Override
	public String toString() {
		return "ManageInvitesModel [credit=" + credit + ", recipientEmail="
				+ recipientEmail + ", sentDate=" + sentDate + ", status="
				+ status + "]";
	}
	public void setSaleId(String saleId) {
		this.saleId = saleId;
	}
	public String getSaleId() {
		return saleId;
	}
	public void setRecipientCustId(String recipientCustId) {
		this.recipientCustId = recipientCustId;
	}
	public String getRecipientCustId() {
		return recipientCustId;
	}
	public void setCreditIssuedDate(Date creditIssuedDate) {
		this.creditIssuedDate = creditIssuedDate;
	}
	public Date getCreditIssuedDate() {
		return creditIssuedDate;
	}
	
	
}
