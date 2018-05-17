/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer;

import java.util.Date;

import java.io.Serializable;


/**
 * FDCustomer model class.
 *
 * @version    $Revision$
 * @author     $Author$
 * @stereotype fd-model
 */
public class FDCustomerRequest implements Serializable {
    
    /**
     * Default constructor.
     */
    public FDCustomerRequest() {
        super();
    }

    /**
	 * @return Returns the caseSubjectCode.
	 */
	public String getCaseSubjectCode() {
		return caseSubjectCode;
	}
	/**
	 * @param caseSubjectCode The caseSubjectCode to set.
	 */
	public void setCaseSubjectCode(String caseSubjectCode) {
		this.caseSubjectCode = caseSubjectCode;
	}
	/**
	 * @return Returns the customerEmail.
	 */
	public String getCustomerEmail() {
		return customerEmail;
	}
	/**
	 * @param customerEmail The customerEmail to set.
	 */
	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}
	/**
	 * @return Returns the customerID.
	 */
	public String getCustomerID() {
		return customerID;
	}
	/**
	 * @param customerID The customerID to set.
	 */
	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}
	/**
	 * @return Returns the infoLine1.
	 */
	public String getInfoLine1() {
		return infoLine1;
	}
	/**
	 * @param infoLine1 The infoLine1 to set.
	 */
	public void setInfoLine1(String infoLine1) {
		this.infoLine1 = infoLine1;
	}
	/**
	 * @return Returns the infoLine2.
	 */
	public String getInfoLine2() {
		return infoLine2;
	}
	/**
	 * @param infoLine2 The infoLine2 to set.
	 */
	public void setInfoLine2(String infoLine2) {
		this.infoLine2 = infoLine2;
	}
	/**
	 * @return Returns the infoLine3.
	 */
	public String getInfoLine3() {
		return infoLine3;
	}
	/**
	 * @param infoLine3 The infoLine3 to set.
	 */
	public void setInfoLine3(String infoLine3) {
		this.infoLine3 = infoLine3;
	}
	/**
	 * @return Returns the infoLine4.
	 */
	public String getInfoLine4() {
		return infoLine4;
	}
	/**
	 * @param infoLine4 The infoLine4 to set.
	 */
	public void setInfoLine4(String infoLine4) {
		this.infoLine4 = infoLine4;
	}
	/**
	 * @return Returns the infoLine5.
	 */
	public String getInfoLine5() {
		return infoLine5;
	}
	/**
	 * @param infoLine5 The infoLine5 to set.
	 */
	public void setInfoLine5(String infoLine5) {
		this.infoLine5 = infoLine5;
	}
	/**
	 * @return Returns the infoLine6.
	 */
	public String getInfoLine6() {
		return infoLine6;
	}
	/**
	 * @param infoLine6 The infoLine6 to set.
	 */
	public void setInfoLine6(String infoLine6) {
		this.infoLine6 = infoLine6;
	}
	/**
	 * @return Returns the infoLine7.
	 */
	public String getInfoLine7() {
		return infoLine7;
	}
	/**
	 * @param infoLine7 The infoLine7 to set.
	 */
	public void setInfoLine7(String infoLine7) {
		this.infoLine7 = infoLine7;
	}
	/**
	 * @return Returns the infoLine8.
	 */
	public String getInfoLine8() {
		return infoLine8;
	}
	/**
	 * @param infoLine8 The infoLine8 to set.
	 */
	public void setInfoLine8(String infoLine8) {
		this.infoLine8 = infoLine8;
	}
	/**
	 * @return Returns the infoLine9.
	 */
	public String getInfoLine9() {
		return infoLine9;
	}
	/**
	 * @param infoLine9 The infoLine9 to set.
	 */
	public void setInfoLine9(String infoLine9) {
		this.infoLine9 = infoLine9;
	}
	/**
	 * @return Returns the infoLineOther.
	 */
	public String getInfoOther() {
		return infoOther;
	}
	/**
	 * @param infoLineOther The infoLineOther to set.
	 */
	public void setInfoOther(String infoOther) {
		this.infoOther = infoOther;
	}
	/**
	 * @return Returns the loggedBy.
	 */
	public String getLoggedBy() {
		return loggedBy;
	}
	/**
	 * @param loggedBy The loggedBy to set.
	 */
	public void setLoggedBy(String loggedBy) {
		this.loggedBy = loggedBy;
	}
	/**
	 * @return Returns the requestDate.
	 */
	public Date getRequestDate() {
		return requestDate;
	}
	/**
	 * @param requestDate The requestDate to set.
	 */
	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}
	/**
	 * @return Returns the subject.
	 */
	public String getSubject() {
		return subject;
	}
	/**
	 * @param subject The subject to set.
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
    private Date requestDate;
    private String caseSubjectCode="";
    private String customerID="";
    private String customerEmail="";
    private String loggedBy="";
    private String subject="";
    private String infoLine1="";
    private String infoLine2="";
    private String infoLine3="";
    private String infoLine4="";
    private String infoLine5="";
    private String infoLine6="";
    private String infoLine7="";
    private String infoLine8="";
    private String infoLine9="";
    private String infoOther="";    
}