package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.freshdirect.customer.EnumComplaintLineMethod;
import com.freshdirect.customer.ErpCustomerCreditModel;

/**@author ekracoff*/
public class FDCustomerCreditHistoryModel implements Serializable {

	private List creditHistory;
	private String customerId;

	public FDCustomerCreditHistoryModel(FDIdentity identity, List creditHistory) {
		this.customerId = identity.getErpCustomerPK();
		this.creditHistory = creditHistory;
	}

	public List getCreditHistory() {
		return this.creditHistory;
	}

	public void setCreditHistory(List creditHistory) {
		this.creditHistory = creditHistory;
	}

	public String getCustomerId() {
		return this.customerId;
	}

	public void setCustomerId(FDIdentity identity) {
		this.customerId = identity.getErpCustomerPK();
	}

	public double getRemainingAmount() {
		double remainingAmount = 0.0;
		for (Iterator i = creditHistory.iterator(); i.hasNext();) {
			FDCustomerCreditModel credit = (FDCustomerCreditModel) i.next();
			remainingAmount = remainingAmount + credit.getRemainingAmount();
		}
		return remainingAmount;
	}
	
	public List getCreditsForComplaint(String complaintId){
		List complaintCredits = new ArrayList();
		for(Iterator i = this.creditHistory.iterator(); i.hasNext();){
			FDCustomerCreditModel customerCredit = (FDCustomerCreditModel) i.next();
			if(customerCredit.getComplaintPk().equals(complaintId)){
				complaintCredits.add(customerCredit);
			}
		}
		return complaintCredits;
	}

	public double getTotalCashBack() {
		double cashBack = 0.0;
		for (Iterator i = creditHistory.iterator(); i.hasNext();) {
			FDCustomerCreditModel credit = (FDCustomerCreditModel) i.next();
			if (EnumComplaintLineMethod.CASH_BACK.equals(credit.getMethod())) {
				cashBack = cashBack + credit.getOriginalAmount();
			}
		}
		return cashBack;
	}
	
	public int getSumRefund() {
		int sumRefund = 0;
		for (Iterator i = creditHistory.iterator(); i.hasNext();) {
			FDCustomerCreditModel credit = (FDCustomerCreditModel) i.next();
			if (EnumComplaintLineMethod.CASH_BACK.equals(credit.getMethod())) {
				sumRefund++;
			}
		}
		return sumRefund;
	}

	public double getTotalCreditsIssued() {
		double total = 0.0;
		for (Iterator i = creditHistory.iterator(); i.hasNext();) {
			FDCustomerCreditModel credit = (FDCustomerCreditModel) i.next();
			if (EnumComplaintLineMethod.STORE_CREDIT.equals(credit.getMethod())) {
				total = total + credit.getOriginalAmount();
			}
		}
		return total;
	}
	
	public int getSumCredit() {
		int sumCredit = 0;
		for (Iterator i = creditHistory.iterator(); i.hasNext();) {
			FDCustomerCreditModel credit = (FDCustomerCreditModel) i.next();
			if (EnumComplaintLineMethod.STORE_CREDIT.equals(credit.getMethod())) {
				sumCredit++;
			}
		}
		return sumCredit;
	}

}
