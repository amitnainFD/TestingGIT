package com.freshdirect.fdstore.ewallet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.freshdirect.customer.ErpPaymentMethodI;

/**
 * @author Aniwesh Vatsal
 *
 */
public class EwalletResponseData  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6854039991654688991L;
	
	@JsonProperty
	private String callbackUrl;
	@JsonProperty
	private String token;
	@JsonProperty
	private String pairingToken;
	@JsonProperty
	private String eWalletIdentifier;
	@JsonProperty
	private String pairingRequest;
	@JsonProperty
	private String eWalletExpressCheckout;
	@JsonProperty
	private String version;
	@JsonProperty
	private String allowedPaymentMethodTypes;
	private String LoyaltyEnabled;
	@JsonProperty
	private String reqDatatype;
	@JsonProperty
	private String suppressShippingEnable;
	@JsonProperty
	private String requestBasicCkt;
	
	private String redirectUrl;
	private ValidationResult validationResult;
	private String transactionId;
	private ErpPaymentMethodI paymentMethod;
	private String preferredMPCard;
	private String preCheckoutTnxId;
	
	//Batch
	private List<EwalletPostBackModel> trxns;
	
	/**
	 * @return the trxns
	 */
	public List<EwalletPostBackModel> getTrxns() {
		return trxns;
	}
	/**
	 * @param trxns the trxns to set
	 */
	public void setTrxns(List<EwalletPostBackModel> trxns) {
		this.trxns = trxns;
	}
	@JsonProperty
	private List<PaymentData> paymentDatas = new ArrayList<PaymentData>();
	/**
	 * @return the callbackUrl
	 */
	public String getCallbackUrl() {
		return callbackUrl;
	}
	/**
	 * @param callbackUrl the callbackUrl to set
	 */
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	/**
	 * @return the pairingToken
	 */
	public String getPairingToken() {
		return pairingToken;
	}
	/**
	 * @param pairingToken the pairingToken to set
	 */
	public void setPairingToken(String pairingToken) {
		this.pairingToken = pairingToken;
	}
	
	/**
	 * @return the eWalletIdentifier
	 */
	public String geteWalletIdentifier() {
		return eWalletIdentifier;
	}
	/**
	 * @param eWalletIdentifier the eWalletIdentifier to set
	 */
	public void seteWalletIdentifier(String eWalletIdentifier) {
		this.eWalletIdentifier = eWalletIdentifier;
	}
	/**
	 * @return the pairingRequest
	 */
	public String getPairingRequest() {
		return pairingRequest;
	}
	/**
	 * @param pairingRequest the pairingRequest to set
	 */
	public void setPairingRequest(String pairingRequest) {
		this.pairingRequest = pairingRequest;
	}
	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}
	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}
	/**
	 * @return the eWalletExpressCheckout
	 */
	public String geteWalletExpressCheckout() {
		return eWalletExpressCheckout;
	}
	/**
	 * @param eWalletExpressCheckout the eWalletExpressCheckout to set
	 */
	public void seteWalletExpressCheckout(String eWalletExpressCheckout) {
		this.eWalletExpressCheckout = eWalletExpressCheckout;
	}
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return the allowedPaymentMethodTypes
	 */
	public String getAllowedPaymentMethodTypes() {
		return allowedPaymentMethodTypes;
	}
	/**
	 * @param allowedPaymentMethodTypes the allowedPaymentMethodTypes to set
	 */
	public void setAllowedPaymentMethodTypes(String allowedPaymentMethodTypes) {
		this.allowedPaymentMethodTypes = allowedPaymentMethodTypes;
	}
	/**
	 * @return
	 */
	public ValidationResult getValidationResult() {
		return validationResult;
	}
	/**
	 * @param validationResult
	 */
	public void setValidationResult(ValidationResult validationResult) {
		this.validationResult = validationResult;
	}
	/**
	 * @return the redirectUrl
	 */
	public String getRedirectUrl() {
		return redirectUrl;
	}
	/**
	 * @param redirectUrl the redirectUrl to set
	 */
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
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
	 * @return the paymentMethod
	 */
	public ErpPaymentMethodI getPaymentMethod() {
		return paymentMethod;
	}
	/**
	 * @param paymentMethod the paymentMethod to set
	 */
	public void setPaymentMethod(ErpPaymentMethodI paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public List<PaymentData> getPaymentDatas() {
		return paymentDatas;
	}
	public void setPaymentDatas(List<PaymentData> paymentDatas) {
		this.paymentDatas = paymentDatas;
	}
	/**
	 * @return the reqDatatype
	 */
	public String getReqDatatype() {
		return reqDatatype;
	}
	/**
	 * @param reqDatatype the reqDatatype to set
	 */
	public void setReqDatatype(String reqDatatype) {
		this.reqDatatype = reqDatatype;
	}
	/**
	 * @return the suppressShippingEnable
	 */
	public String getSuppressShippingEnable() {
		return suppressShippingEnable;
	}
	/**
	 * @param suppressShippingEnable the suppressShippingEnable to set
	 */
	public void setSuppressShippingEnable(String suppressShippingEnable) {
		this.suppressShippingEnable = suppressShippingEnable;
	}
	/**
	 * @return the loyaltyEnabled
	 */
	public String getLoyaltyEnabled() {
		return LoyaltyEnabled;
	}
	/**
	 * @param loyaltyEnabled the loyaltyEnabled to set
	 */
	public void setLoyaltyEnabled(String loyaltyEnabled) {
		LoyaltyEnabled = loyaltyEnabled;
	}
	/**
	 * @return the requestBasicCkt
	 */
	public String getRequestBasicCkt() {
		return requestBasicCkt;
	}
	/**
	 * @param requestBasicCkt the requestBasicCkt to set
	 */
	public void setRequestBasicCkt(String requestBasicCkt) {
		this.requestBasicCkt = requestBasicCkt;
	}
	/**
	 * @return the preferredMPCard
	 */
	public String getPreferredMPCard() {
		return preferredMPCard;
	}
	/**
	 * @param preferredMPCard the preferredMPCard to set
	 */
	public void setPreferredMPCard(String preferredMPCard) {
		this.preferredMPCard = preferredMPCard;
	}
	/**
	 * @return the preCheckoutTnxId
	 */
	public String getPreCheckoutTnxId() {
		return preCheckoutTnxId;
	}
	/**
	 * @param preCheckoutTnxId the preCheckoutTnxId to set
	 */
	public void setPreCheckoutTnxId(String preCheckoutTnxId) {
		this.preCheckoutTnxId = preCheckoutTnxId;
	}
	
}
