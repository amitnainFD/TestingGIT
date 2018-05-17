/**
 * 
 */
package com.freshdirect.fdstore.ewallet.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.freshdirect.fdstore.FDStoreProperties;
import com.mastercard.mcwallet.sdk.AccessTokenResponse;
import com.mastercard.mcwallet.sdk.MasterPassServiceRuntimeException;
import com.mastercard.mcwallet.sdk.RealmType;
import com.mastercard.mcwallet.sdk.RequestTokenResponse;
import com.mastercard.mcwallet.sdk.xml.allservices.Checkout;
import com.mastercard.mcwallet.sdk.xml.allservices.ExpressCheckoutRequest;
import com.mastercard.mcwallet.sdk.xml.allservices.ExpressCheckoutResponse;
import com.mastercard.mcwallet.sdk.xml.allservices.PrecheckoutData;
import com.mastercard.mcwallet.sdk.xml.allservices.PrecheckoutDataRequest;
import com.mastercard.mcwallet.sdk.xml.allservices.ShoppingCartRequest;

/**
 * @author Aniwesh Vatsal
 *
 */
public class MasterpassData {
	private static String configFile = "";
	
	// URLs
	private String requestURL;
	private String shoppingCartUrl;
	private String accessURL;
	private String postbackurl;
	
	private String appBaseUrl;
	private String contextPath;
	
	
	// OAuth data received from MasterCard services
	private String requestToken = "";
	private RequestTokenResponse requestTokenResponse;
	private AccessTokenResponse accessTokenResponse;
	private AccessTokenResponse longAccessTokenResponse;
	
	
	// Callback return values
	private String accessToken = "";
	private String longAccessToken = "";
	
	private String verifier = "";
	private String checkoutResourceURL = "";
	
	// From the Checkout Project
	private String consumerKey;
	private String checkoutIdentifier;
	private String keystorePath;
	private String keystorePassword;
	
	// URL to your callback web service
	private String callbackUrl;
	private String callbackDomain;
	private String callbackPath = "/OauthCallbackServlet";
	private String callbackpathConnect;

	private String shippingProfiles;
	private RealmType realm = RealmType.eWallet;
	
	// Redirect URL parameters
	
	@JsonProperty
	private String acceptedCards;
	@JsonProperty
	private String xmlVersion;
	@JsonProperty
	private Boolean shippingSuppression;
	@JsonProperty
	private Boolean authLevelBasic;
	@JsonProperty
	private Boolean rewards;
	private String redirectShippingProfiles = "";
	private String walletName;
	private String consumerWalletId;
	
	// SHopping Cart XML strings
	private String shoppingCartRequest;
	private String shoppingCartResponse;
	@JsonProperty
	private ShoppingCartRequest shoppingCart;
	
	private String merchantInitRequest;
	private String merchantInitResponse;
	@JsonProperty
	private ShoppingCartRequest merchantInit;
	private String merchantInitUrl;
	
	//PostTransaction XML Data (for display in test app)
	private String postTransactionSentXml = "";
	private String postTransactionReceivedXml = "";
	
	//Checkout XML Data (for display in test app)
	@JsonProperty
	private Checkout checkout;
	private String checkoutXML = "";
	private String checkoutOutResponse = "";
	
	// Precheckout XML strings
	private String precheckoutRequest;
	private String precheckoutResponse;
	@JsonProperty
	private PrecheckoutDataRequest preCheckout;
	@JsonProperty
	private PrecheckoutData preCheckoutData;
	private String preCheckoutDataXml;
	private String preCheckoutDataJson;
	private String preCheckoutUrl;
	
	private boolean silentlyPaired = false;
	private String spPrecheckoutUrl;
	private String precheckoutCardId;
	private String precheckoutShippingId;
	private String precheckoutTransactionId = "";
	
	
	private String expressCheckoutUrl;
	private String spExpressCheckoutUrl;
	private String expressCheckoutRequest;
	private ExpressCheckoutRequest expressCheckoutRequestData;
	private String expressCheckoutResponse;
	private ExpressCheckoutResponse expressCheckoutResponseData;
	private Boolean expressCheckoutIndicator = false;
	private Boolean expressSecurityRequired = false;
	
	// Connection details (for display in test app) 
	private String signatureBaseString = "";
	private String authHeader = "";
	private String encodedAuthHeader = "";
	
	
	// New properties Added
	private String expresscheckoutEnable="";
	private String loyaltyEnabled="";
	private String reqPairing="";
	private String reqDatatype="";
	private String requestBasicCkt="false";
	private String stdChkCallbackpath="";
	private String requestBasicCheckout="";
	
	public void setEncodedAuthHeader(String encodedAuthHeader){
		this.encodedAuthHeader = encodedAuthHeader;
	}
	
	public String getEncodedAuthHeader() {
		return encodedAuthHeader;
	}

	

	private String errorMessage;
	
	//Checkout static data
	@JsonProperty
	private long tax = 0;
	@JsonProperty
	private long shipping = 0;
	// Pairing callback
	private String pairingCallbackPath;
	// pairing token
	@JsonProperty
	private String pairingToken;
	// pairing verifier
	@JsonProperty
	private String pairingVerifier;
	@JsonProperty
	private List<String> pairingDataTypes;
	
	private String expressCallbackPath;
	
	private String connectedCallbackPath;
	
	private String lightboxUrl;
	 
	private String cardId;
	private String shippingId;
	
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
	 * @return the merchantInitRequest
	 */
	public String getMerchantInitRequest() {
		return merchantInitRequest;
	}

	/**
	 * @param merchantInitRequest the merchantInitRequest to set
	 */
	public void setMerchantInitRequest(String merchantInitRequest) {
		this.merchantInitRequest = merchantInitRequest;
	}

	/**
	 * @return the merchantInitResponse
	 */
	public String getMerchantInitResponse() {
		return merchantInitResponse;
	}

	/**
	 * @param merchantInitResponse the merchantInitResponse to set
	 */
	public void setMerchantInitResponse(String merchantInitResponse) {
		this.merchantInitResponse = merchantInitResponse;
	}

	/**
	 * @return the merchantInit
	 */
	public ShoppingCartRequest getMerchantInit() {
		return merchantInit;
	}

	/**
	 * @param merchantInit the merchantInit to set
	 */
	public void setMerchantInit(ShoppingCartRequest merchantInit) {
		this.merchantInit = merchantInit;
	}
	/**
	 * 
	 */
	public MasterpassData() {
		this(configFile);
	}
	
	/**
	 * Load all the properties
	 * @param configFile
	 * @return
	 */
	private Properties loadProperties(String configFile) {
		Properties props = new Properties();
		InputStream stream = null;
		try {
			stream = ClassLoader.getSystemResourceAsStream(configFile);
			if (stream != null) {
				props.load(stream);
			}
			return props;
		} catch (IOException ioe) {
			throw new MasterPassServiceRuntimeException(ioe);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// Trying to close. IOException is okay here
				}
			}
		}
	}
	
	/**
	 * @param configFile
	 */
	public MasterpassData(String configFile){
		
		configFile = FDStoreProperties.getMasterpassEnvironment();
		Properties props = loadProperties(configFile);
		
		this.callbackDomain = props.getProperty("callbackdomain");
		this.requestURL = props.getProperty("requesturl");
		this.shoppingCartUrl = props.getProperty("shoppingcarturl");
		this.accessURL = props.getProperty("accessurl");
		this.postbackurl = props.getProperty("postbackurl");
		this.checkoutIdentifier = props.getProperty("checkoutidentifier");
		this.consumerKey = props.getProperty("consumerkey");
		this.stdChkCallbackpath=this.callbackDomain + props.getProperty("stdChkCallbackpath");
		this.callbackUrl = this.callbackDomain + props.getProperty("stdChkCallbackpath");
		this.keystorePassword = props.getProperty("keystorepassword");
		this.keystorePath = props.getProperty("keystorepath");
		this.lightboxUrl = props.getProperty("lightboxurl");
		this.acceptedCards = props.getProperty("allowedCardTypes");
		this.xmlVersion = props.getProperty("xmlVersion");
		this.requestBasicCheckout = props.getProperty("requestBasicCheckout"); 
		this.reqDatatype=props.getProperty("reqDatatype");
		this.redirectShippingProfiles = props.getProperty("redirectShippingProfiles");
		this.loyaltyEnabled = props.getProperty("loyaltyEnabled");
		if(props.getProperty("shippingSuppression") != null){
			this.shippingSuppression=  props.getProperty("shippingSuppression").equalsIgnoreCase("true") ? true : false;
		}
		if(props.getProperty("authLevelBasic") != null){
			this.authLevelBasic = props.getProperty("authLevelBasic").equalsIgnoreCase("true") ? true : false;
		}
		this.shippingProfiles = props.getProperty("shippingprofiles");
		if(props.getProperty("rewards") != null){
			this.rewards=props.getProperty("rewards").equalsIgnoreCase("true") ? true : false;
		}
		
		
		
		/*
		 * Required for Master Pass Express Checkout
		this.callbackPath = props.getProperty("callbackpath");
		this.callbackUrl = this.callbackDomain + this.callbackPath;
		this.callbackpathConnect = this.callbackDomain + props.getProperty("callbackpathConnect");
		this.setPreCheckoutUrl(props.getProperty("precheckouturl"));
		this.setSpPrecheckoutUrl(props.getProperty("spprecheckouturl"));
		this.setSpExpressCheckoutUrl(props.getProperty("spexpresscheckouturl"));
		this.merchantInitUrl = props.getProperty("merchantiniturl");
		this.pairingCallbackPath = this.callbackDomain + props.getProperty("pairingcallbackpath");
		this.expressCallbackPath = this.callbackDomain + props.getProperty("expresscallbackpath");
		this.expressCheckoutUrl = props.getProperty("expresscheckouturl");
		this.connectedCallbackPath = props.getProperty("connectedcallbackpath");
		this.expresscheckoutEnable=  props.getProperty("expresscheckoutEnable");
		this.loyaltyEnabled = props.getProperty("loyaltyEnabled");
		this.reqPairing=props.getProperty("reqPairing");
		*/

	}
	
	
	public String parseKey(String strLine) {
		return strLine.substring(0, strLine.indexOf("=")).trim();
	}
	public String parseValue(String strLine) {
		return strLine.substring(strLine.indexOf("\"")+1, strLine.lastIndexOf("\"")).trim();
	}
	
	public String getRequestURL() {
		return requestURL;
	}
	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}
	public String getAccessURL() {
		return accessURL;
	}
	public void setAccessURL(String accessURL) {
		this.accessURL = accessURL;
	}
	public String getCallbackUrl() {
		return callbackUrl;
	}
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	public String getCallbackDomain() {
		return callbackDomain;
	}
	public void setCallbackDomain(String callbackDomain) {
		this.callbackDomain = callbackDomain;
	}
	public String getCallbackPath() {
		return callbackPath;
	}
	public void setCallbackPath(String callbackPath) {
		this.callbackPath = callbackPath;
	}
	public String getPostbackurl() {
		return postbackurl;
	}
	public void setPostbackurl(String postbackurl) {
		this.postbackurl = postbackurl;
	}
	public String getAppBaseUrl() {
		return appBaseUrl;
	}
	public void setAppBaseUrl(String appBaseUrl) {
		this.appBaseUrl = appBaseUrl;
	}
	public String getContextPath() {
		return contextPath;
	}
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	public String getShoppingCartUrl() {
		return shoppingCartUrl;
	}
	public void setShoppingCartUrl(String shoppingCartUrl) {
		this.shoppingCartUrl = shoppingCartUrl;
	}
	public String getCheckoutResourceURL() {
		return checkoutResourceURL;
	}
	public void setCheckoutResourceURL(String checkoutResourceURL) {
		this.checkoutResourceURL = checkoutResourceURL;
	}
	public RequestTokenResponse getRequestTokenResponse() {
		return requestTokenResponse;
	}
	public void setRequestTokenResponse(RequestTokenResponse requestTokenResponse) {
		this.requestTokenResponse = requestTokenResponse;
	}
	public AccessTokenResponse getAccessTokenResponse() {
		return accessTokenResponse;
	}
	public void setAccessTokenResponse(AccessTokenResponse accessTokenResponse) {
		this.accessTokenResponse = accessTokenResponse;
	}
	public AccessTokenResponse getLongAccessTokenResponse() {
		return longAccessTokenResponse;
	}
	public void setLongAccessTokenResponse(AccessTokenResponse longAccessTokenResponse) {
		this.longAccessTokenResponse = longAccessTokenResponse;
	}
	public String getRequestToken() {
		return requestToken;
	}
	public void setRequestToken(String requestToken) {
		this.requestToken = requestToken;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getVerifier() {
		return verifier;
	}
	public void setVerifier(String verifier) {
		this.verifier = verifier;
	}
	public String getConsumerKey() {
		return consumerKey;
	}
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}
	public String getCheckoutIdentifier() {
		return checkoutIdentifier;
	}
	public void setCheckoutIdentifier(String checkoutIdentifier) {
		this.checkoutIdentifier = checkoutIdentifier;
	}
	public Checkout getCheckout() {
		return checkout;
	}
	public void setCheckout(Checkout checkout) {
		this.checkout = checkout;
	}
	public String getCheckoutXML() {
		return checkoutXML;
	}
	public void setCheckoutXML(String checkoutXML) {
		this.checkoutXML = checkoutXML;
	}
	public String getPostTransactionReceivedXml() {
		return postTransactionReceivedXml;
	}
	public void setPostTransactionReceivedXml(String postTransactionReceivedXml) {
		this.postTransactionReceivedXml = postTransactionReceivedXml;
	}

	public String getAcceptedCards() {
		return acceptedCards;
	}

	public void setAcceptedCards(String acceptedCards) {
		this.acceptedCards = acceptedCards;
	}

	public String getSignatureBaseString() {
		return signatureBaseString;
	}

	public void setSignatureBaseString(String signatureBaseString) {
		this.signatureBaseString = signatureBaseString;
	}

	public String getAuthHeader() {
		return authHeader;
	}
	public void setAuthHeader(String authHeader) {
		this.authHeader = authHeader;
	}
	public String getCheckoutOutResponse() {
		return checkoutOutResponse;
	}
	public void setCheckoutOutResponse(String checkoutOutResponse) {
		this.checkoutOutResponse = checkoutOutResponse;
	}
	public String getErrorMessage() {
		if(errorMessage != null) {
			return MasterpassData.formatErrorMessage(errorMessage);
		}
		else {
			return errorMessage;
		}
	}
	
	public static String formatErrorMessage(String errorMessage) {
		if (errorMessage.contains("<Errors>")) {
			return xmlEscapeText(prettyFormat(errorMessage));
		}
		else {
			return errorMessage;
		}
	}
	
	private static String xmlEscapeText(String t) {

		   StringBuilder sb = new StringBuilder();
		   for(int i = 0; i < t.length(); i++){
		      char c = t.charAt(i);
		      switch(c){
		      case '<': sb.append("&lt;"); break;
		      case '>': sb.append("&gt;"); break;
		      case '\"': sb.append("&guot;"); break;
		      case '&': sb.append("&amp;"); break;
		      case '\'': sb.append("&apos;"); break;
		      default:
//		         if(c>0x7e) {
//		            sb.append("&#"+((int)c)+";");
//		         }else
		            sb.append(c);
		      }
		   }
		   return sb.toString();
		}
	
	private static String prettyFormat(String input, String indent) {
	    try {
	    	//
	    	if (input == null || input.equals("")) { 
	    		return input;
	    	}
	    	input = input.replace(">  <", "><");
	    	if (input.contains("<html>") ) {
	    		return input;
	    	}
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",indent);
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        transformer.transform(xmlInput, xmlOutput);

	        return xmlOutput.getWriter().toString();
	    } catch (Exception e) {
	    	throw new MasterPassServiceRuntimeException(e);
	    }
	}
	
	private static String prettyFormat(String input) {
	    return prettyFormat(input,"4");
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public RealmType getRealm() {
		return realm;
	}

	public void setRealm(RealmType realm) {
		this.realm = realm;
	}

	public String getKeystorePath() {
		return keystorePath;
	}

	public void setKeystorePath(String keystorePath) {
		this.keystorePath = keystorePath;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}
	public String getXmlVersion() {
		return xmlVersion;
	}
	public void setXmlVersion(String xmlVersion) {
		this.xmlVersion = xmlVersion;
	}
	public Boolean getShippingSuppression() {
		return shippingSuppression;
	}
	public void setShippingSuppression(Boolean shippingSuppression) {
		this.shippingSuppression = shippingSuppression;
	}
	public boolean getAuthLevelBasic() {
		return authLevelBasic;
	}
	public void setAuthLevelBasic(boolean authLevelBasic) {
		this.authLevelBasic = authLevelBasic;
	}
	public Boolean getRewards() {
		return rewards;
	}
	public void setRewards(Boolean rewards) {
		this.rewards = rewards;
	}
	public String getShippingProfiles() {
		return shippingProfiles;
	}
	public void setShippingProfiles(String shippingProfiles) {
		this.shippingProfiles = shippingProfiles;
	}
	public String getRedirectShippingProfiles() {
		return redirectShippingProfiles;
	}
	public void setRedirectShippingProfiles(String redirectShippingProfiles) {
		this.redirectShippingProfiles = redirectShippingProfiles;
	}
	public String getShoppingCartRequest() {
		return shoppingCartRequest;
	}
	public void setShoppingCartRequest(String shoppingCartRequest) {
		this.shoppingCartRequest = shoppingCartRequest;
	}
	public String getPostTransactionSentXml() {
		return postTransactionSentXml;
	}
	public void setPostTransactionSentXml(String postTransactionSentXml) {
		this.postTransactionSentXml = postTransactionSentXml;
	}
	public String getShoppingCartResponse() {
		return shoppingCartResponse;
	}
	public void setShoppingCartResponse(String shoppingCartResponse) {
		this.shoppingCartResponse = shoppingCartResponse;
	}
	public ShoppingCartRequest getShoppingCart() {
		return shoppingCart;
	}
	public void setShoppingCart(ShoppingCartRequest shoppingCart) {
		this.shoppingCart = shoppingCart;
	}
	public long getTax() {
		return tax;
	}
	public void setTax(long tax) {
		this.tax = tax;
	}
	public long getShipping() {
		return shipping;
	}
	public void setShipping(long shipping) {
		this.shipping = shipping;
	}
	/**
	 * @return the longAccessToken
	 */
	public String getLongAccessToken() {
		return longAccessToken;
	}

	/**
	 * @param longAccessToken the longAccessToken to set
	 */
	public void setLongAccessToken(String longAccessToken) {
		this.longAccessToken = longAccessToken;
	}

	/**
	 * @return the merchantInitUrl
	 */
	public String getMerchantInitUrl() {
		return merchantInitUrl;
	}

	/**
	 * @param merchantInitUrl the merchantInitUrl to set
	 */
	public void setMerchantInitUrl(String merchantInitUrl) {
		this.merchantInitUrl = merchantInitUrl;
	}

	/**
	 * @return the pairingCallbackPath
	 */
	public String getPairingCallbackPath() {
		return pairingCallbackPath;
	}

	/**
	 * @param pairingCallbackPath the pairingCallbackPath to set
	 */
	public void setPairingCallbackPath(String pairingCallbackPath) {
		this.pairingCallbackPath = pairingCallbackPath;
	}

	/**
	 * @return the precheckoutRequest
	 */
	public String getPrecheckoutRequest() {
		return precheckoutRequest;
	}

	/**
	 * @param precheckoutRequest the precheckoutRequest to set
	 */
	public void setPrecheckoutRequest(String precheckoutRequest) {
		this.precheckoutRequest = precheckoutRequest;
	}

	/**
	 * @return the precheckoutResponse
	 */
	public String getPrecheckoutResponse() {
		return precheckoutResponse;
	}

	/**
	 * @param precheckoutResponse the precheckoutResponse to set
	 */
	public void setPrecheckoutResponse(String precheckoutResponse) {
		this.precheckoutResponse = precheckoutResponse;
	}

	/**
	 * @return the preCheckout
	 */
	public PrecheckoutDataRequest getPreCheckout() {
		return preCheckout;
	}

	/**
	 * @param preCheckout the preCheckout to set
	 */
	public void setPreCheckout(PrecheckoutDataRequest preCheckout) {
		this.preCheckout = preCheckout;
	}

	/**
	 * @return the preCheckoutData
	 */
	public PrecheckoutData getPreCheckoutData() {
		return preCheckoutData;
	}

	/**
	 * @param preCheckoutData the preCheckoutData to set
	 */
	public void setPreCheckoutData(PrecheckoutData preCheckoutData) {
		this.preCheckoutData = preCheckoutData;
	}

	/**
	 * @return the preCheckoutUrl
	 */
	public String getPreCheckoutUrl() {
		return preCheckoutUrl;
	}

	/**
	 * @param preCheckoutUrl the preCheckoutUrl to set
	 */
	public void setPreCheckoutUrl(String preCheckoutUrl) {
		this.preCheckoutUrl = preCheckoutUrl;
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
	 * @return the precheckoutShippingId
	 */
	public String getPrecheckoutShippingId() {
		return precheckoutShippingId;
	}

	/**
	 * @param precheckoutShippingId the precheckoutShippingId to set
	 */
	public void setPrecheckoutShippingId(String precheckoutShippingId) {
		this.precheckoutShippingId = precheckoutShippingId;
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
	 * @return the expressCheckoutUrl
	 */
	public String getExpressCheckoutUrl() {
		return expressCheckoutUrl;
	}

	/**
	 * @param expressCheckoutUrl the expressCheckoutUrl to set
	 */
	public void setExpressCheckoutUrl(String expressCheckoutUrl) {
		this.expressCheckoutUrl = expressCheckoutUrl;
	}

	/**
	 * @return the expressCheckoutRequest
	 */
	public String getExpressCheckoutRequest() {
		return expressCheckoutRequest;
	}

	/**
	 * @param expressCheckoutRequest the expressCheckoutRequest to set
	 */
	public void setExpressCheckoutRequest(String expressCheckoutRequest) {
		this.expressCheckoutRequest = expressCheckoutRequest;
	}

	/**
	 * @return the expressCheckoutRequestData
	 */
	public ExpressCheckoutRequest getExpressCheckoutRequestData() {
		return expressCheckoutRequestData;
	}

	/**
	 * @param expressCheckoutRequestData the expressCheckoutRequestData to set
	 */
	public void setExpressCheckoutRequestData(ExpressCheckoutRequest expressCheckoutRequestData) {
		this.expressCheckoutRequestData = expressCheckoutRequestData;
	}

	/**
	 * @return the expressCheckoutResponse
	 */
	public String getExpressCheckoutResponse() {
		return expressCheckoutResponse;
	}

	/**
	 * @param expressCheckoutResponse the expressCheckoutResponse to set
	 */
	public void setExpressCheckoutResponse(String expressCheckoutResponse) {
		this.expressCheckoutResponse = expressCheckoutResponse;
	}

	/**
	 * @return the expressCheckoutResponseData
	 */
	public ExpressCheckoutResponse getExpressCheckoutResponseData() {
		return expressCheckoutResponseData;
	}

	/**
	 * @param expressCheckoutResponseData the expressCheckoutResponseData to set
	 */
	public void setExpressCheckoutResponseData(ExpressCheckoutResponse expressCheckoutResponseData) {
		this.expressCheckoutResponseData = expressCheckoutResponseData;
	}

	/**
	 * @return the expressCallbackPath
	 */
	public String getExpressCallbackPath() {
		return expressCallbackPath;
	}

	/**
	 * @param expressCallbackPath the expressCallbackPath to set
	 */
	public void setExpressCallbackPath(String expressCallbackPath) {
		this.expressCallbackPath = expressCallbackPath;
	}

	/**
	 * @return the lightboxUrl
	 */
	public String getLightboxUrl() {
		return lightboxUrl;
	}

	/**
	 * @param lightboxUrl the lightboxUrl to set
	 */
	public void setLightboxUrl(String lightboxUrl) {
		this.lightboxUrl = lightboxUrl;
	}

	/**
	 * @return the pairingDataTypes
	 */
	public List<String> getPairingDataTypes() {
		return pairingDataTypes;
	}

	/**
	 * @param pairingDataTypes the pairingDataTypes to set
	 */
	public void setPairingDataTypes(List<String> pairingDataTypes) {
		this.pairingDataTypes = pairingDataTypes;
	}

	/**
	 * @return the preCheckoutDataXml
	 */
	public String getPreCheckoutDataXml() {
		return preCheckoutDataXml;
	}

	/**
	 * @param preCheckoutDataXml the preCheckoutDataXml to set
	 */
	public void setPreCheckoutDataXml(String preCheckoutDataXml) {
		this.preCheckoutDataXml = preCheckoutDataXml;
	}

	/**
	 * @return the expressCheckoutIndicator
	 */
	public Boolean getExpressCheckoutIndicator() {
		return expressCheckoutIndicator;
	}

	/**
	 * @param expressCheckoutIndicator the expressCheckoutIndicator to set
	 */
	public void setExpressCheckoutIndicator(Boolean expressCheckoutIndicator) {
		this.expressCheckoutIndicator = expressCheckoutIndicator;
	}

	/**
	 * @return the expressSecurityRequired
	 */
	public Boolean getExpressSecurityRequired() {
		return expressSecurityRequired;
	}

	/**
	 * @param expressSecurityRequired the expressSecurityRequired to set
	 */
	public void setExpressSecurityRequired(Boolean expressSecurityRequired) {
		this.expressSecurityRequired = expressSecurityRequired;
	}

	/**
	 * @return the preCheckoutDataJson
	 */
	public String getPreCheckoutDataJson() {
		return preCheckoutDataJson;
	}

	/**
	 * @param preCheckoutDataJson the preCheckoutDataJson to set
	 */
	public void setPreCheckoutDataJson(String preCheckoutDataJson) {
		this.preCheckoutDataJson = preCheckoutDataJson;
	}

	/**
	 * @return the connectedCallbackPath
	 */
	public String getConnectedCallbackPath() {
		return connectedCallbackPath;
	}

	/**
	 * @param connectedCallbackPath the connectedCallbackPath to set
	 */
	public void setConnectedCallbackPath(String connectedCallbackPath) {
		this.connectedCallbackPath = connectedCallbackPath;
	}

	/**
	 * @return the walletName
	 */
	public String getWalletName() {
		return walletName;
	}

	/**
	 * @param walletName the walletName to set
	 */
	public void setWalletName(String walletName) {
		this.walletName = walletName;
	}

	/**
	 * @return the consumerWalletId
	 */
	public String getConsumerWalletId() {
		return consumerWalletId;
	}

	/**
	 * @param consumerWalletId the consumerWalletId to set
	 */
	public void setConsumerWalletId(String consumerWalletId) {
		this.consumerWalletId = consumerWalletId;
	}
	
	/**
	 * Start -Code added by Sandip for Silent Pairing functionality
	*/

	/**
	 * @return the spPrecheckoutUrl
	 */
	public String getSpPrecheckoutUrl() {
		return spPrecheckoutUrl;
	}

	/**
	 * @param spPrecheckoutUrl the spPrecheckoutUrl to set
	 */
	public void setSpPrecheckoutUrl(String spPrecheckoutUrl) {
		this.spPrecheckoutUrl = spPrecheckoutUrl;
	}

	/**
	 * @return the spExpressCheckoutUrl
	 */
	public String getSpExpressCheckoutUrl() {
		return spExpressCheckoutUrl;
	}

	/**
	 * @param spExpressCheckoutUrl the spExpressCheckoutUrl to set
	 */
	public void setSpExpressCheckoutUrl(String spExpressCheckoutUrl) {
		this.spExpressCheckoutUrl = spExpressCheckoutUrl;
	}

	/**
	 * @return the silentlyPaired
	 */
	public boolean isSilentlyPaired() {
		return silentlyPaired;
	}

	/**
	 * @param silentlyPaired the silentlyPaired to set
	 */
	public void setSilentlyPaired(boolean silentlyPaired) {
		this.silentlyPaired = silentlyPaired;
	}

	/**
	 * @return the cardId
	 */
	public String getCardId() {
		return cardId;
	}

	/**
	 * @param cardId the cardId to set
	 */
	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	/**
	 * @return the shippingId
	 */
	public String getShippingId() {
		return shippingId;
	}

	/**
	 * @param shippingId the shippingId to set
	 */
	public void setShippingId(String shippingId) {
		this.shippingId = shippingId;
	}

	public String getCallbackpathConnect() {
		return callbackpathConnect;
	}

	public void setCallbackpathConnect(String callbackpathConnect) {
		this.callbackpathConnect = callbackpathConnect;
	}
	
		/**
	 * @return the expresscheckoutEnable
	 */
	public String getExpresscheckoutEnable() {
		return expresscheckoutEnable;
	}

	/**
	 * @param expresscheckoutEnable the expresscheckoutEnable to set
	 */
	public void setExpresscheckoutEnable(String expresscheckoutEnable) {
		this.expresscheckoutEnable = expresscheckoutEnable;
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
	 * @return the loyaltyEnabled
	 */
	public String getLoyaltyEnabled() {
		return loyaltyEnabled;
	}

	/**
	 * @param loyaltyEnabled the loyaltyEnabled to set
	 */
	public void setLoyaltyEnabled(String loyaltyEnabled) {
		this.loyaltyEnabled = loyaltyEnabled;
	}

	/**
	 * @return the reqPairing
	 */
	public String getReqPairing() {
		return reqPairing;
	}

	/**
	 * @param reqPairing the reqPairing to set
	 */
	public void setReqPairing(String reqPairing) {
		this.reqPairing = reqPairing;
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
	 * @return the stdChkCallbackpath
	 */
	public String getStdChkCallbackpath() {
		return stdChkCallbackpath;
	}

	/**
	 * @param stdChkCallbackpath the stdChkCallbackpath to set
	 */
	public void setStdChkCallbackpath(String stdChkCallbackpath) {
		this.stdChkCallbackpath = stdChkCallbackpath;
	}

	/**
	 * @return the requestBasicCheckout
	 */
	public String getRequestBasicCheckout() {
		return requestBasicCheckout;
	}

	/**
	 * @param requestBasicCheckout the requestBasicCheckout to set
	 */
	public void setRequestBasicCheckout(String requestBasicCheckout) {
		this.requestBasicCheckout = requestBasicCheckout;
	}

	/**
	 * @param authLevelBasic the authLevelBasic to set
	 */
	public void setAuthLevelBasic(Boolean authLevelBasic) {
		this.authLevelBasic = authLevelBasic;
	}
	
}
