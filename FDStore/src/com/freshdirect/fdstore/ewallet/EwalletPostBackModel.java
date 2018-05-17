package com.freshdirect.fdstore.ewallet;

import java.io.Serializable;
import java.util.Date;

public class EwalletPostBackModel implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -4086478823551508416L;
	private String transactionId = "";
    private String consumerKey = ""; //Optional
    //TODO obtain from enum
    private String currency = "USD";
    private long orderAmount = 0;
    private Date purchaseDate = null;

    private String[] transactionStatus = {"FAILURE", "SUCCESS"};
    private int status;
    
    private String approvalCode = "";
    private String preCheckoutTransactionId = ""; //Optional
    //TODO Change it for Standard checkout impl
    private Boolean expressCheckoutIndicator = false; //Optional
    private boolean postBackSuccess = true;
    
    //Tracks errors of data if any.
    private boolean error = false;
	private String errorStr = "";
	private String recoverable = "";
	
	private boolean gAL = false;
	private String orderId = "";
	private String customerId = "";
	
	/**
	 * @return the customerId
	 */
	public String getCustomerId() {
		return customerId;
	}

	/**
	 * @param customerId the customerId to set
	 */
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	/**
	 * @return the orderId
	 */
	public String getOrderId() {
		return orderId;
	}

	/**
	 * @param orderId the orderId to set
	 */
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	/**
	 * @return the gAL
	 */
	public boolean isgAL() {
		return gAL;
	}

	/**
	 * @param gAL the gAL to set
	 */
	public void setgAL(boolean gAL) {
		this.gAL = gAL;
	}

	//either SA Id or GAL Id
	private String key = "";

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param recoverable the recoverable to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * @return the recoverable
	 */
	public String getRecoverable() {
		return recoverable;
	}
	/**
	 * @param recoverable the recoverable to set
	 */
	public void setRecoverable(String recoverable) {
		this.recoverable = recoverable;
	}

	private String salesActionId = "";
	/**
	 * @return the saleasActionId
	 */
	public String getSalesActionId() {
		return salesActionId;
	}
	/**
	 * @param saleasActionId the saleasActionId to set
	 */
	public void setSalesActionId(String salesActionId) {
		this.salesActionId = salesActionId;
		key = salesActionId;
	}
	/**
	 * @return the gALId
	 */
	public String getgALId() {
		return gALId;
	}
	/**
	 * @param gALId the gALId to set
	 */
	public void setgALId(String gALId) {
		this.gALId = gALId;
		key = gALId;
	}

	private String gALId = "";
	
    /**
	 * @return the errorStr
	 */
	public String getErrorStr() {
		return errorStr;
	}
	/**
	 * @param errorStr the errorStr to set
	 */
	public void setErrorStr(String errorStr) {
		this.errorStr = errorStr;
	}
	/**
	 * @return the dataError
	 */
	public boolean isError() {
		return error;
	}
	/**
	 * @param dataError the dataError to set
	 */
	public void setError(boolean error) {
		this.error = error;
	}
   
    /**
	 * @return the postBackSuccess
	 */
	public boolean isPostBackSuccess() {
		return postBackSuccess;
	}
	/**
	 * @param postBackSuccess the postBackSuccess to set
	 */
	public void setPostBackSuccess(boolean postBackSuccess) {
		this.postBackSuccess = postBackSuccess;
	}

	private Object extension = null; //Do not use
	/**
	 * @return the transactionId
	 */
	public String getTransactionId() {
		return transactionId;
	}
	/**
	 * @param transactionId the transactionId to set
	 */
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	/**
	 * @return the consumerKey
	 */
	public String getConsumerKey() {
		return consumerKey;
	}
	/**
	 * @param consumerKey the consumerKey to set
	 */
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
	/**
	 * @return the currency
	 */
	public String getCurrency() {
		return currency;
	}
	/**
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	/**
	 * @return the orderAmount
	 */
	public long getOrderAmount() {
		return orderAmount;
	}
	/**
	 * @param orderAmount the orderAmount to set
	 */
	public void setOrderAmount(long orderAmount) {
		this.orderAmount = orderAmount;
	}
	/**
	 * @return the purchaseDate
	 */
	public Date getPurchaseDate() {
		return purchaseDate;
	}
	/**
	 * @param purchaseDate the purchaseDate to set
	 */
	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}
	/**
	 * @return the transactionStatus
	 */
	public String getTransactionStatus() {
		return transactionStatus[status];
	}
	/**
	 * @param transactionStatus the transactionStatus to set
	 */
	public void setTransactionStatus(int status) {
		if (status != 0 || status != 1) {
			throw new AssertionError("Postback request cannot have transaction status other than Success or Failure");
		}
		this.status = status;
	}
	/**
	 * @param transactionStatus the transactionStatus to set
	 */
	public void setTransactionStatus(boolean status) {
		if (status)
			this.status = 1;
		else
			this.status = 0;
	}
	/**
	 * @return the approvalCode
	 */
	public String getApprovalCode() {
		return approvalCode;
	}
	/**
	 * @param approvalCode the approvalCode to set
	 */
	public void setApprovalCode(String approvalCode) {
		this.approvalCode = approvalCode;
	}
	/**
	 * @return the preCheckoutTransactionId
	 */
	public String getPreCheckoutTransactionId() {
		return preCheckoutTransactionId;
	}
	/**
	 * @param preCheckoutTransactionId the preCheckoutTransactionId to set
	 */
	public void setPreCheckoutTransactionId(String preCheckoutTransactionId) {
		this.preCheckoutTransactionId = preCheckoutTransactionId;
	}
	/**
	 * @return the expressCheckoutIndicator
	 */
	public boolean isExpressCheckoutIndicator() {
		return expressCheckoutIndicator;
	}
	/**
	 * @param expressCheckoutIndicator the expressCheckoutIndicator to set
	 */
	public void setExpressCheckoutIndicator(boolean expressCheckoutIndicator) {
		this.expressCheckoutIndicator = expressCheckoutIndicator;
	}
	/**
	 * @return the extension
	 */
	public Object getExtension() {
		return extension;
	}
	/**
	 * @param extension the extension to set
	 */
	public void setExtension(Object extension) {
		this.extension = extension;
	}
	
	@Override
	public String toString() {
		return "Trxn Id : " + getTransactionId() + ", " +
				"Sale Action Id : " + getSalesActionId() + ", " +
				"GAL Id : " + getgALId() + ", " +
				"consumerKey : " + getConsumerKey() + ", " +
				"Currency : " + getCurrency() + ", " +
				"order amount : " + getOrderAmount() + ", " +
				"purchase date : " + getPurchaseDate() + ", " +
				"Trxn status : " + getTransactionStatus() + ", " +
				"approval or auth code : " + getApprovalCode() + ", " +
				"precheckout trxn id " + getPreCheckoutTransactionId() + ", " +
				"Express Checkout Indicator : " + isExpressCheckoutIndicator() + ", " +
				" Extension : " + getExtension();
	}
}