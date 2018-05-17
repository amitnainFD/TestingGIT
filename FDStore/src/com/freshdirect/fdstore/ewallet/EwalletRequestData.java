package com.freshdirect.fdstore.ewallet;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.freshdirect.fdstore.customer.FDActionInfo;

/**
 * @author Aniwesh Vatsal
 *
 */
public class EwalletRequestData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2108583405975394387L;
	private EnumEwalletType eWalletType;
	private String eWalletAction;
	private String shoppingCartItems;
	private String customerId;
	private String appBaseUrl;
	private String contextPath;
	private String pairingVerifier;
	private String pairingToken;
	private String eWalletResponseStatus;
	private FDActionInfo fdActionInfo;
	private boolean isPaymentechEnabled;
	private PaymentData paymentData;
	private String precheckoutCardId;
	private String mpPairedPaymentMethod;
	private String mobileCallbackDomain;
	private String precheckoutTransactionId;
	private Map<String,String> reqParams;
	
	
	//batch interface related trxns
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
	
	/**
	 * @return the eWalletType
	 */
	public EnumEwalletType getEnumeWalletType() {
		return eWalletType;
	}
	/**
	 * @param eWalletType the eWalletType to set
	 */
	public void setEnumeWalletType(EnumEwalletType eWalletType) {
		this.eWalletType = eWalletType;
	}
	
	/**
	 * @return the eWalletType
	 */
	public String geteWalletType() {
		return eWalletType.getName();
	}
	/**
	 * @param eWalletType the eWalletType to set
	 */
	public void seteWalletType(String eWalletType) {
		this.eWalletType = EnumEwalletType.getEnum(eWalletType);
	}
	
	/**
	 * @return the eWalletAction
	 */
	public String geteWalletAction() {
		return eWalletAction;
	}
	/**
	 * @param eWalletAction the eWalletAction to set
	 */
	public void seteWalletAction(String eWalletAction) {
		this.eWalletAction = eWalletAction;
	}
	/**
	 * @return the shoppingCartItems
	 */
	public String getShoppingCartItems() {
		return shoppingCartItems;
	}
	/**
	 * @param shoppingCartItems the shoppingCartItems to set
	 */
	public void setShoppingCartItems(String shoppingCartItems) {
		this.shoppingCartItems = shoppingCartItems;
	}
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
	 * @return the appBaseUrl
	 */
	public String getAppBaseUrl() {
		return appBaseUrl;
	}
	/**
	 * @param appBaseUrl the appBaseUrl to set
	 */
	public void setAppBaseUrl(String appBaseUrl) {
		this.appBaseUrl = appBaseUrl;
	}
	/**
	 * @return the contextPath
	 */
	public String getContextPath() {
		return contextPath;
	}
	/**
	 * @param contextPath the contextPath to set
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	/**
	 * @return the pairingVerifier
	 */
	public String getPairingVerifier() {
		return pairingVerifier;
	}
	/**
	 * @param pairingVerifier the pairingVerifier to set
	 */
	public void setPairingVerifier(String pairingVerifier) {
		this.pairingVerifier = pairingVerifier;
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
	 * @return the eWalletResponseStatus
	 */
	public String geteWalletResponseStatus() {
		return eWalletResponseStatus;
	}
	/**
	 * @param eWalletResponseStatus the eWalletResponseStatus to set
	 */
	public void seteWalletResponseStatus(String eWalletResponseStatus) {
		this.eWalletResponseStatus = eWalletResponseStatus;
	}
	
	/**
	 * @return the fdActionInfo
	 */
	public FDActionInfo getFdActionInfo() {
		return fdActionInfo;
	}
	/**
	 * @param fdActionInfo the fdActionInfo to set
	 */
	public void setFdActionInfo(FDActionInfo fdActionInfo) {
		this.fdActionInfo = fdActionInfo;
	}
	/**
	 * @return the isPaymentechEnabled
	 */
	public boolean isPaymentechEnabled() {
		return isPaymentechEnabled;
	}
	/**
	 * @param isPaymentechEnabled the isPaymentechEnabled to set
	 */
	public void setPaymentechEnabled(boolean isPaymentechEnabled) {
		this.isPaymentechEnabled = isPaymentechEnabled;
	}
	/**
	 * @return the paymentData
	 */
	public PaymentData getPaymentData() {
		return paymentData;
	}
	/**
	 * @param paymentData the paymentData to set
	 */
	public void setPaymentData(PaymentData paymentData) {
		this.paymentData = paymentData;
	}
	/**
	 * @return the precheckoutCardId
	 */
	public String getPrecheckoutCardId() {
		return precheckoutCardId;
	}
	/**
	 * @param precheckoutCardId the precheckoutCardId to set
	 */
	public void setPrecheckoutCardId(String precheckoutCardId) {
		this.precheckoutCardId = precheckoutCardId;
	}
	/**
	 * @return the mpPairedPaymentMethod
	 */
	public String getMpPairedPaymentMethod() {
		return mpPairedPaymentMethod;
	}
	/**
	 * @param mpPairedPaymentMethod the mpPairedPaymentMethod to set
	 */
	public void setMpPairedPaymentMethod(String mpPairedPaymentMethod) {
		this.mpPairedPaymentMethod = mpPairedPaymentMethod;
	}
	/**
	 * @return the mobileCallbackDomain
	 */
	public String getMobileCallbackDomain() {
		return mobileCallbackDomain;
	}
	/**
	 * @param mobileCallbackDomain the mobileCallbackDomain to set
	 */
	public void setMobileCallbackDomain(String mobileCallbackDomain) {
		this.mobileCallbackDomain = mobileCallbackDomain;
	}
	/**
	 * @return the precheckoutTransactionId
	 */
	public String getPrecheckoutTransactionId() {
		return precheckoutTransactionId;
	}
	/**
	 * @param precheckoutTransactionId the precheckoutTransactionId to set
	 */
	public void setPrecheckoutTransactionId(String precheckoutTransactionId) {
		this.precheckoutTransactionId = precheckoutTransactionId;
	}

	/**
	 * @return the reqParams
	 */
	public Map<String, String> getReqParams() {
		return reqParams;
	}

	/**
	 * @param reqParams the reqParams to set
	 */
	public void setReqParams(Map<String, String> reqParams) {
		this.reqParams = reqParams;
	}
}
