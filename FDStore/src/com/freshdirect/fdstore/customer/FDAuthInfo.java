package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.Date;

import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.payment.EnumBankAccountType;
import com.freshdirect.payment.EnumPaymentMethodType;
import com.freshdirect.customer.EnumSaleType;

public class FDAuthInfo implements Serializable {
	
	private final String saleId;
	private Date deliveryDate;
	private Date txDateTime;
	private EnumSaleStatus saleStatus;
	private double authAmount;
	private String nameOnCard;
	private String cardType;
	private String firstName;
	private String lastName;
	private String authDescription;
	private String authCode;
	private String ccLastFourNum;
	private EnumPaymentMethodType paymentMethodType;
	private String abaRouteNumber;
	private EnumBankAccountType bankAccountType;
	private EnumSaleType orderType;
	private String eStore;
	private String facility;
	
	public FDAuthInfo (String saleId){
		this.saleId = saleId;
	}
	
	public String getSaleId(){
		return this.saleId;
	}
	
	public Date getDeliveryDate(){
		return this.deliveryDate;
	}
	
	public void setDeliveryDate(Date deliveryDate){
		this.deliveryDate = deliveryDate;
	}
	
	public Date getTransactionDateTime(){
		return this.txDateTime;
	}
	
	public void setTransactionDateTime(Date txDateTime){
		this.txDateTime = txDateTime;
	}
	
	public EnumSaleStatus getSaleStatus(){
		return this.saleStatus;
	}
	
	public void setSaleStatus(EnumSaleStatus saleStatus){
		this.saleStatus = saleStatus;
	}
	
	public double getAuthAmount(){
		return this.authAmount;
	}
	
	public void setAuthAmount(double authAmount){
		this.authAmount = authAmount;
	}
	
	public String getNameOnCard(){
		return this.nameOnCard;
	}
	
	public void setNameOnCard(String nameOnCard){
		this.nameOnCard = nameOnCard;
	}
	
	public String getFirstName(){
		return this.firstName;
	}
	
	public void setFirstName(String firstName){
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return this.lastName;
	}
	
	public void setLastName(String lastName){
		this.lastName = lastName;
	}
	
	public String getAuthDescription(){
		return this.authDescription;
	}
	
	public void setAuthDescription(String authDescription){
		this.authDescription = authDescription;
	}
	
	public String getAuthCode(){
		return this.authCode;
	}
	
	public void setAuthCode(String authCode){
		this.authCode = authCode;
	}
	
	public String getCCLastFourNum(){
		return this.ccLastFourNum;
	}
	
	public void setCCLastFourNum(String ccLastFourNum){
		this.ccLastFourNum = ccLastFourNum;
	}
	
	public String getCardType() {
		return this.cardType;
	}
	
	public void setCardType(String cardType){
		this.cardType = cardType;
	}
	
	public EnumPaymentMethodType getPaymentMethodType(){
		return this.paymentMethodType;
	}
	public void setPaymentMethodType(EnumPaymentMethodType paymentMethodType){
		this.paymentMethodType = paymentMethodType;
	}

	public String getAbaRouteNumber(){
		return this.abaRouteNumber;
	}
	public void setAbaRouteNumber(String abaRouteNumber){
		this.abaRouteNumber = abaRouteNumber;
	}

	public EnumBankAccountType getBankAccountType(){
		return this.bankAccountType;
	}
	public void setBankAccountType(EnumBankAccountType bankAccountType){
		this.bankAccountType = bankAccountType;
	}

	public EnumSaleType getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = EnumSaleType.getSaleType(orderType);
	}

	public String geteStore() {
		return eStore;
	}

	public void seteStore(String eStore) {
		this.eStore = eStore;
	}

	public String getFacility() {
		return facility;
	}

	public void setFacility(String facility) {
		this.facility = facility;
	}
	
}
