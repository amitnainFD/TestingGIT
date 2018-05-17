package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.freshdirect.payment.EnumBankAccountType;
import com.freshdirect.payment.EnumPaymentMethodType;
import com.freshdirect.common.customer.EnumCardType;
import com.freshdirect.framework.util.DateUtil;

public class FDAuthInfoSearchCriteria implements Serializable {
	private EnumPaymentMethodType paymentMethodType;	
	private EnumCardType cardType;
	private double chargedAmount;
	private String ccKnownNum;
	private String transDate;
	private String transMonth;
	private String transYear;
	private String abaRouteNumber;
	private EnumBankAccountType bankAccountType;
	
	public EnumPaymentMethodType getPaymentMethodType () {
		return this.paymentMethodType;
	}
	
	public void setPaymentMethodType (EnumPaymentMethodType paymentMethodType) {
		this.paymentMethodType = paymentMethodType;
	}

	public EnumCardType getCardType () {
		return this.cardType;
	}
	
	public void setCardType (EnumCardType cardType) {
		this.cardType = cardType;
	}

	public double getChargedAmount(){
		return this.chargedAmount;
	}
	
	public void setChargedAmount(double chargedAmount){
		this.chargedAmount = chargedAmount;
	}

	public String getCCKnownNum(){
		return this.ccKnownNum;
	}
	
	public void setCCKnownNum (String ccKnownNum) {
		this.ccKnownNum = ccKnownNum;
	}

	public String getTransDate(){
		return this.transDate;
	}
	
	public void setTransDate(String transDate) {
		this.transDate = transDate;
	}
	
	public String getTransMonth(){
		return this.transMonth;
	}
	
	public void setTransMonth(String transMonth) {
		this.transMonth = transMonth;
	}
	
	public String getTransYear(){
		return this.transYear;
	}
	
	public void setTransYear(String transYear) {
		this.transYear = transYear;
	}

	public Date getTransactionDate () {
		if("".equals(this.transDate) || "".equals(this.transMonth) || "".equals(this.transYear)){
			return null;
		}
		Calendar transCal = DateUtil.truncate(Calendar.getInstance());
		transCal.set(Calendar.DATE, Integer.parseInt(this.transDate));
		transCal.set(Calendar.MONTH, Integer.parseInt(this.transMonth));
		transCal.set(Calendar.YEAR, Integer.parseInt(this.transYear));
		return transCal.getTime();
	}
	
	public boolean isBlank() {
		return this.chargedAmount <= 0 && "".equals(this.ccKnownNum) && this.cardType == null && "".equals(this.transDate) && "".equals(this.transMonth) && "".equals(this.transYear);
	}

	public String getAbaRouteNumber(){
		return this.abaRouteNumber;
	}
	
	public void setAbaRouteNumber(String abaRouteNumber) {
		this.abaRouteNumber = abaRouteNumber;
	}
	
	public EnumBankAccountType getBankAccountType(){
		return this.bankAccountType;
	}
	
	public void setBankAccountType(EnumBankAccountType bankAccountType) {
		this.bankAccountType = bankAccountType;
	}

}
