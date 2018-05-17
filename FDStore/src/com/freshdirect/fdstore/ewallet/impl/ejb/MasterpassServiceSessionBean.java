/*
 * Created on Sept 26, 2015
 *
 */
package com.freshdirect.fdstore.ewallet.impl.ejb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Category;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.freshdirect.common.customer.EnumCardType;
import com.freshdirect.customer.ErpCustEWalletModel;
import com.freshdirect.customer.ErpPaymentMethodException;
import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.customer.ErpPaymentMethodModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDCustomerFactory;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.ewallet.EnumUserInfoName;
import com.freshdirect.fdstore.ewallet.EwalletPostBackModel;
import com.freshdirect.fdstore.ewallet.EwalletRequestData;
import com.freshdirect.fdstore.ewallet.EwalletResponseData;
import com.freshdirect.fdstore.ewallet.PaymentData;
import com.freshdirect.fdstore.ewallet.PaymentMethodName;
import com.freshdirect.fdstore.ewallet.ValidationError;
import com.freshdirect.fdstore.ewallet.ValidationResult;
import com.freshdirect.fdstore.ewallet.impl.MasterPassApplicationHelper;
import com.freshdirect.fdstore.ewallet.impl.MasterpassData;
import com.freshdirect.fdstore.ewallet.util.EWalletCryptoUtil;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.payment.EnumBankAccountType;
import com.freshdirect.payment.EnumPaymentMethodType;
import com.freshdirect.payment.PaymentManager;
import com.freshdirect.payment.ewallet.gateway.ejb.EwalletActivityLogModel;
import com.freshdirect.payment.gateway.ewallet.impl.EWalletLogActivity;
import com.mastercard.api.common.openapiexception.MCOpenApiRuntimeException;
import com.mastercard.mcwallet.sdk.MasterPassService;
import com.mastercard.mcwallet.sdk.MasterPassServiceRuntimeException;
import com.mastercard.mcwallet.sdk.RequestTokenResponse;
import com.mastercard.mcwallet.sdk.xml.allservices.Address;
import com.mastercard.mcwallet.sdk.xml.allservices.Card;
import com.mastercard.mcwallet.sdk.xml.allservices.Checkout;
import com.mastercard.mcwallet.sdk.xml.allservices.ExpressCheckoutRequest;
import com.mastercard.mcwallet.sdk.xml.allservices.ExpressCheckoutResponse;
import com.mastercard.mcwallet.sdk.xml.allservices.MerchantTransaction;
import com.mastercard.mcwallet.sdk.xml.allservices.MerchantTransactions;
import com.mastercard.mcwallet.sdk.xml.allservices.PairingDataType;
import com.mastercard.mcwallet.sdk.xml.allservices.PairingDataTypes;
import com.mastercard.mcwallet.sdk.xml.allservices.PrecheckoutCard;
import com.mastercard.mcwallet.sdk.xml.allservices.PrecheckoutDataRequest;
import com.mastercard.mcwallet.sdk.xml.allservices.PrecheckoutDataResponse;
import com.mastercard.mcwallet.sdk.xml.allservices.PrecheckoutShippingAddress;
import com.mastercard.mcwallet.sdk.xml.allservices.ShoppingCartItem;
import com.mastercard.mcwallet.sdk.xml.allservices.ShoppingCartRequest;
import com.mastercard.mcwallet.sdk.xml.allservices.ShoppingCartResponse;
import com.mastercard.mcwallet.sdk.xml.switchapiservices.MerchantInitializationRequest;
import com.mastercard.mcwallet.sdk.xml.switchapiservices.MerchantInitializationResponse;

/**
 * @author Aniwesh Vatsal
 *
 */
public class MasterpassServiceSessionBean extends SessionBeanSupport {

	/**
	 * As MasterpassService is now made session bean.
	 */
	private static final long serialVersionUID = -2076297608075673395L;

	private final static Category LOGGER = LoggerFactory.getInstance(MasterpassServiceSessionBean.class);
	
	private static final String CURRENCY_CODE="USD";
	private static final String MASTERPASS_WALLET_TYPE_NAME="MP";
	private static final String MASTERPASS_CARD_TYPES="CARD";
	private static final String MASTERPASS_BRAND_NAME="MASTER";
	private static final String DISCOVER_BRANDNAME_NAME="DISCOVER";
	private static final String AMEX_BRANDNAME_NAME="AMEX";
	private static final String VISA_BRANDNAME_NAME="VISA";
	private static final String MASTERPASS_CANCEL_STATUS="cancel";
	private static final String PARAM_REQUEST_TOKEN = "oauth_token";
	private static final String PARAM_OAUTH_VERIFIER = "oauth_verifier";
	private static final String PARAM_CHECKOUT_URL = "checkout_resource_url";

	
	// For Audit Log
	private static final String MASTERPASS_REQ_TOKEN_TXN="RequestTokenTxn";
	private static final String MASTERPASS_PAIRING_TOKEN_TXN="PiaringTokenTxn";
	private static final String MASTERPASS_SHOPPING_CART_TXN="PostShoppingTxn";
	private static final String MASTERPASS_ACCESS_TOKEN_TXN="AccessTokenTxn";
	private static final String MASTERPASS_LONG_ACCESS_TOKEN_TXN="LongAccessTokenTxn";
	private static final String MASTERPASS_CHECKOUT_TXN="CheckoutTxn";
	private static final String MASTERPASS_PRECHECKOUT_TXN="PrecheckoutTxn";
	private static final String MASTERPASS_EXPRESSCHECKOUT_TXN="ExpresscheckoutTxn";
	private static final String MASTERPASS_TXN_SUCCESS="SUCCESS";
	private static final String MASTERPASS_TXN_FAIL="FAIL";
	private static final String MASTERPASS_MERCHANT_INIT_TXN="MerchantInitTxn";
	private static final String MASTERPASS_DISCONNECT_WALLET_TXN = "DisconnectTxn";
	private static final String MASTERPASS_POSTBACK_TXN="PostbackTxn";
	private static final String MASTERPASS_STD_CHECKOUT_ACTION="MP_Standard_CheckoutData";

	java.util.Date date= new java.util.Date();
	 
	
	/**
	 * @param ewalletRequestData
	 * @return
	 * @throws RemoteException
	 */
	public EwalletResponseData preStandardCheckout(EwalletRequestData ewalletRequestData) throws RemoteException {
		EwalletResponseData ewalletResponseData = new EwalletResponseData();
		
		MasterpassData data = new MasterpassData();
		
		ewalletResponseData.setCallbackUrl(data.getStdChkCallbackpath()+"&action="+MASTERPASS_STD_CHECKOUT_ACTION);
		ewalletResponseData.seteWalletIdentifier(data.getCheckoutIdentifier());	// Comes from Properties file
		List<ValidationError> eWalletValidationErrors = new ArrayList<ValidationError>(); 
		try{
			// Request Token Service call
			try{
				data = this.getRequestTokenAndRedirectUrl(data);
				ewalletResponseData.setToken(data.getRequestToken());	// Update the received token to response
				
				if(data.getRequestToken()!=null && !data.getRequestToken().isEmpty()){
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_REQ_TOKEN_TXN,MASTERPASS_TXN_SUCCESS);
				}else{
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_REQ_TOKEN_TXN,MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError("Cannot Connect", "Error while getting Request Token From Masterpass"));
				}
			}catch(MasterPassServiceRuntimeException exception){
				LOGGER.error("Exception While getting Request Token From Masterpass Service"+exception.getMessage());
				logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_REQ_TOKEN_TXN,MASTERPASS_TXN_FAIL);
				eWalletValidationErrors.add(new ValidationError("Cannot Connect", "Error while getting Request Token From Masterpass"));
			}
			
			try{
				// Check is there any error in previous service call
				if(eWalletValidationErrors.isEmpty()){
					// Check If request came from Mobile then change the callBackDomain
					if(ewalletRequestData.getMobileCallbackDomain() != null && ewalletRequestData.getMobileCallbackDomain().length() >0){
						data.setCallbackDomain(ewalletRequestData.getMobileCallbackDomain());
					}
					// Post Shopping Request To Masterpass and receive the response
					String shoppingCart = craeteShoppingCartRequestXML(data,ewalletRequestData);
					data = this.postShoppingCart(data,shoppingCart);
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_SHOPPING_CART_TXN,MASTERPASS_TXN_SUCCESS);
				}
				
			}catch(MasterPassServiceRuntimeException exception){
				LOGGER.error("Exception While posting shopping cart request to Masterpass "+exception.getMessage());
				logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_SHOPPING_CART_TXN,MASTERPASS_TXN_FAIL);
				eWalletValidationErrors.add(new ValidationError("Cannot Connect", "Error while posting Shopping Cart."));
			}
		}catch(Exception exception){
			LOGGER.error("Exception while calling Masterpass GetToken service call.", exception);
			exception.printStackTrace();
			throw new RemoteException(exception.getMessage());
		}
		
		// Error handling
		if(eWalletValidationErrors!=null && !eWalletValidationErrors.isEmpty()){
			ValidationResult result = new ValidationResult();
			result.setErrors(eWalletValidationErrors);
			ewalletResponseData.setValidationResult(result);
			
		}else{
			ewalletResponseData.setAllowedPaymentMethodTypes(data.getAcceptedCards());
			ewalletResponseData.seteWalletExpressCheckout(data.getExpresscheckoutEnable());
			ewalletResponseData.setReqDatatype(data.getReqDatatype());
			ewalletResponseData.setSuppressShippingEnable(data.getShippingSuppression()?"true":"false");
			ewalletResponseData.setLoyaltyEnabled(data.getLoyaltyEnabled());
			ewalletResponseData.setRequestBasicCkt(data.getRequestBasicCkt());
			ewalletResponseData.setVersion(data.getXmlVersion());
		}
		return ewalletResponseData;
	}
	
	/**
	 * This method makes two service calls to MasterPass to complete the Standard Checkout before launching the Light Box UI.
	 * @param ewalletRequestData
	 * @return
	 * @throws RemoteException
	 */
	public EwalletResponseData standardCheckout(EwalletRequestData ewalletRequestData) throws RemoteException {
		
		EwalletResponseData ewalletResponseData = new EwalletResponseData();
		
		// Get the Request Token
		MasterpassData data = new MasterpassData();
		Map<String,String> params =ewalletRequestData.getReqParams();
		
		if(params.containsKey(PARAM_REQUEST_TOKEN)){
			data.setRequestToken(params.get(PARAM_REQUEST_TOKEN));
		}
		if(params.containsKey(PARAM_OAUTH_VERIFIER)){
			data.setVerifier(params.get(PARAM_OAUTH_VERIFIER));
		}
		if(params.containsKey(PARAM_CHECKOUT_URL)){
			data.setCheckoutResourceURL(params.get(PARAM_CHECKOUT_URL));
		}
		
		ewalletResponseData.setCallbackUrl(data.getStdChkCallbackpath()+"&action="+MASTERPASS_STD_CHECKOUT_ACTION);
		data.setCallbackUrl(ewalletResponseData.getCallbackUrl());
		ewalletResponseData.seteWalletIdentifier(data.getCheckoutIdentifier());	// Comes from Properties file
		List<ValidationError> eWalletValidationErrors = new ArrayList<ValidationError>(); 
		try{
			// Get Access Token From Masterpass
			try{
				if(data.getRequestToken() !=null && data.getVerifier()!=null){
					data = this.getAccessToken(data);
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_ACCESS_TOKEN_TXN,MASTERPASS_TXN_SUCCESS);
				}
			}catch(MasterPassServiceRuntimeException exception){
				LOGGER.error("Exception While getting Access Token From Masterpass"+exception.getMessage());
				logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_ACCESS_TOKEN_TXN,MASTERPASS_TXN_FAIL);
				eWalletValidationErrors.add(new ValidationError("Cannot Connect", "Error while getting Access Token."));
			}
			
			try{
				data = this.getCheckoutData(data);
				if(data.getCheckoutXML()!=null && !data.getCheckoutXML().isEmpty()){
					// Encrypt Card Details from the Response before saving into Audit Log table
					Checkout checkout = encryptCardData(data.getCheckout());
					data.setCheckoutXML(MasterPassApplicationHelper.xmlEscapeText(MasterPassApplicationHelper.prettyFormat(MasterPassApplicationHelper.printXML(checkout))));
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_CHECKOUT_TXN,MASTERPASS_TXN_SUCCESS);
					
				}else{
					eWalletValidationErrors.add(new ValidationError("Cannot Connect", "Error while getting Card Details for Checkout Data."));
				}
			}catch(MasterPassServiceRuntimeException exception){
				LOGGER.error("Exception While calling Checkout Masterpass Service"+exception.getMessage());
				logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_CHECKOUT_TXN,MASTERPASS_TXN_FAIL);
				eWalletValidationErrors.add(new ValidationError("Cannot Connect", "Error while calling Checkout Service."));
			}
			
			if(eWalletValidationErrors.isEmpty()){
				Map<String,String> paymentDataMap  = new CheckoutResponseMapper().map(data);
				
				ErpPaymentMethodI paymentMethod = parsePaymentMethodForm(paymentDataMap,ewalletRequestData.getCustomerId());
				ErpPaymentMethodI searchedPM = searchMPWalletCards(ewalletRequestData, paymentMethod);
				
				try{
					if(searchedPM == null){
					// Add the card detail to Database if not found 
						FDCustomerManager.addPaymentMethod(ewalletRequestData.getFdActionInfo(), paymentMethod, ewalletRequestData.isPaymentechEnabled());
						List<ErpPaymentMethodI> paymentMethods = FDCustomerFactory.getErpCustomer(ewalletRequestData.getCustomerId()).getPaymentMethods();
		                if (!paymentMethods.isEmpty() && paymentMethods.size() > 1) {
		                	sortPaymentMethodsByIdReserved(paymentMethods);
		                }
		                ErpPaymentMethodI lastAddedPM = paymentMethods.get(0);
		                lastAddedPM.seteWalletTrxnId(paymentMethod.geteWalletTrxnId());
	            		ewalletResponseData.setPaymentMethod(lastAddedPM);
					}else{
						searchedPM.seteWalletTrxnId(paymentMethod.geteWalletTrxnId());
						FDCustomerManager.updatePaymentMethod(ewalletRequestData.getFdActionInfo(), searchedPM);
						ewalletResponseData.setPaymentMethod(searchedPM);
					}
				}catch(FDResourceException exception){
					eWalletValidationErrors.add(new ValidationError("Invalid Credit Card", "Selected Card is not Valid."));
					ValidationResult result = new ValidationResult();
					result.setErrors(eWalletValidationErrors);
					ewalletResponseData.setValidationResult(result);
				}catch(ErpPaymentMethodException exception){
					eWalletValidationErrors.add(new ValidationError("Invalid Credit Card", "Selected Card is not Valid."));
					ValidationResult result = new ValidationResult();
					result.setErrors(eWalletValidationErrors);
					ewalletResponseData.setValidationResult(result);
				}catch(Exception exception){
					eWalletValidationErrors.add(new ValidationError("Invalid Credit Card", "Selected Card is not Valid."));
					ValidationResult result = new ValidationResult();
					result.setErrors(eWalletValidationErrors);
					ewalletResponseData.setValidationResult(result);
				}
                
				if(data.getCheckout() != null && data.getCheckout().getTransactionId()!= null){
					ewalletResponseData.setTransactionId(data.getCheckout().getTransactionId());
				}
				ewalletResponseData.setRedirectUrl("/expressco/checkout.jsp");
			}
			
		}catch (Exception e){
			LOGGER.error("Exception: while calling Masterpass checkout service call.", e);
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
		return ewalletResponseData;
	}
	
	
	/* This Method makes three calls to Masterpass to complete the Checkout during Pairing with Masterpass. Before Light Box UI launched.
	 * Get Access Token from Masterpass
	 * Get Pairing Token from Masterpass
	 * Post Shopping Cart Request to Masterpass
	 * @see com.freshdirect.webapp.ajax.expresscheckout.ewallet.IEwallet#getToken(com.freshdirect.webapp.ajax.expresscheckout.ewallet.EwalletRequestData)
	 */
	public EwalletResponseData getToken(EwalletRequestData ewalletRequestData) throws RemoteException {
		
		EwalletResponseData ewalletResponseData = new EwalletResponseData();
		
		// Get the Request Token
		MasterpassData data = new MasterpassData();
		
		ewalletResponseData.setCallbackUrl(data.getCallbackUrl());
		ewalletResponseData.seteWalletIdentifier(data.getCheckoutIdentifier());	// Comes from Properties file
		List<ValidationError> eWalletValidationErrors = new ArrayList<ValidationError>(); 
		
		try{
			// Get request token service call
			try{
				data = this.getRequestTokenAndRedirectUrl(data);
				ewalletResponseData.setToken(data.getRequestToken());
				
				if(data.getRequestToken()!=null && !data.getRequestToken().isEmpty()){
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_REQ_TOKEN_TXN,MASTERPASS_TXN_SUCCESS);
				}else{
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_REQ_TOKEN_TXN,MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError("MP_Request_Token", "Error while getting Request Token From Masterpass"));
				}
			}catch(MasterPassServiceRuntimeException exception){
				LOGGER.error("Exception While getting Request Token From Masterpass Service"+exception.getMessage());
				logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_REQ_TOKEN_TXN,MASTERPASS_TXN_FAIL);
				eWalletValidationErrors.add(new ValidationError("Masterpass RequestToken", "Error while getting Request Token From Masterpass"));
			}
			
			try{
				// Get the Pairing token from Masterpass
				data = this.getPairingToken(data);
				
				ewalletResponseData.setPairingToken(data.getPairingToken());
				if(data.getPairingToken()!=null && !data.getPairingToken().isEmpty()){
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_PAIRING_TOKEN_TXN,MASTERPASS_TXN_SUCCESS);
				}
				else{
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_PAIRING_TOKEN_TXN,MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError("Masterpass RequestToken", "Error while getting pairing Token From Masterpass"));
				}
			}catch(MasterPassServiceRuntimeException exception){
				LOGGER.error("Exception While getting Pairing Token From Masterpass Service"+exception.getMessage());
				logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_PAIRING_TOKEN_TXN,MASTERPASS_TXN_FAIL);
				eWalletValidationErrors.add(new ValidationError("Masterpass RequestToken", "Error while getting pairing Token From Masterpass"));
			}
			try{
				// Check If request came from Mobile then change the callBackDomain
				if(ewalletRequestData.getMobileCallbackDomain() != null && ewalletRequestData.getMobileCallbackDomain().length() >0){
					data.setCallbackDomain(ewalletRequestData.getMobileCallbackDomain());
				}
				// Post Shopping Request To Masterpass and receive the response
				String shoppingCart = craeteShoppingCartRequestXML(data,ewalletRequestData);
				data = this.postShoppingCart(data,shoppingCart);
				logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_SHOPPING_CART_TXN,MASTERPASS_TXN_SUCCESS);
				
			}catch(MasterPassServiceRuntimeException exception){
				LOGGER.error("Exception While getting Pairing Token From Masterpass Service"+exception.getMessage());
				logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_SHOPPING_CART_TXN,MASTERPASS_TXN_FAIL);
				eWalletValidationErrors.add(new ValidationError("Masterpass RequestToken", "Error while posting Shopping Cart."));
			}
			
			if(eWalletValidationErrors!=null && !eWalletValidationErrors.isEmpty()){
				ValidationResult result = new ValidationResult();
				result.setErrors(eWalletValidationErrors);
				ewalletResponseData.setValidationResult(result);
				
			}else{
				ewalletResponseData.setAllowedPaymentMethodTypes(data.getAcceptedCards());
				ewalletResponseData.seteWalletExpressCheckout(data.getExpresscheckoutEnable());
				ewalletResponseData.setReqDatatype(data.getReqDatatype());
				ewalletResponseData.setSuppressShippingEnable(data.getShippingSuppression()?"true":"false");
				ewalletResponseData.setLoyaltyEnabled(data.getLoyaltyEnabled());
				ewalletResponseData.setRequestBasicCkt(data.getRequestBasicCkt());
				ewalletResponseData.setVersion(data.getXmlVersion());
				ewalletResponseData.setPairingRequest(data.getReqPairing());
			}
		}catch(Exception exception){
			LOGGER.error("Exception while calling Masterpass GetToken service call.", exception);
			exception.printStackTrace();
			throw new RemoteException(exception.getMessage());
		}
		return ewalletResponseData;
	}
	
	/* (non-Javadoc)
	 * @see com.freshdirect.webapp.ajax.expresscheckout.ewallet.IEwallet#checkout(com.freshdirect.webapp.ajax.expresscheckout.ewallet.EwalletRequestData)
	 */
	public EwalletResponseData checkout(EwalletRequestData ewalletRequestData) throws RemoteException{
		
		List<ValidationError> eWalletValidationErrors = new ArrayList<ValidationError>(); 
		EwalletResponseData ewalletResponseData = new EwalletResponseData();
		try{
			if(ewalletRequestData.geteWalletResponseStatus()!= null && ewalletRequestData.geteWalletResponseStatus().equalsIgnoreCase(MASTERPASS_CANCEL_STATUS)){
				eWalletValidationErrors.add(new ValidationError("Masterpass Light Box UI","User has cancelled payment"));
				ValidationResult result = new ValidationResult();
				result.setErrors(eWalletValidationErrors);
				ewalletResponseData.setValidationResult(result);
				ewalletResponseData.setRedirectUrl("/expressco/checkout.jsp");
			}
			else{
				// If User clicks on "Not Allow" on MP Light UI box then PairingToken/PairingVerifier will be null redirect user to checkout.jsp 
				if(ewalletRequestData.getPairingToken() ==null || ewalletRequestData.getPairingVerifier() ==null){
					eWalletValidationErrors.add(new ValidationError("Masterpass Light Box UI","Pairing denied"));
					ValidationResult result = new ValidationResult();
					result.setErrors(eWalletValidationErrors);
					ewalletResponseData.setValidationResult(result);
					ewalletResponseData.setRedirectUrl("/expressco/checkout.jsp");
				}
				
				MasterpassData data = new MasterpassData();
				Map<String,String> params =ewalletRequestData.getReqParams();
				
				if(params.containsKey(PARAM_REQUEST_TOKEN)){
					data.setRequestToken(params.get(PARAM_REQUEST_TOKEN));
				}
				if(params.containsKey(PARAM_OAUTH_VERIFIER)){
					data.setVerifier(params.get(PARAM_OAUTH_VERIFIER));
				}
				if(params.containsKey(PARAM_CHECKOUT_URL)){
					data.setCheckoutResourceURL(params.get(PARAM_CHECKOUT_URL));
				}
				
				// Get Access Token From Masterpass
				try{
					if(data.getRequestToken()!=null && data.getVerifier()!=null){
						data = this.getAccessToken(data);
						logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_ACCESS_TOKEN_TXN,MASTERPASS_TXN_SUCCESS);
					}
				}catch(MasterPassServiceRuntimeException exception){
					LOGGER.error("Exception While getting Access Token From Masterpass"+exception.getMessage());
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_ACCESS_TOKEN_TXN,MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError("Masterpass AccessToken", "Error while getting Access Token."));
				}
				
				data.setPairingToken(ewalletRequestData.getPairingToken());
				data.setPairingVerifier(ewalletRequestData.getPairingVerifier());
				
				try{
					data = this.getLongAccessToken(data);
					
					if(data.getLongAccessToken()!=null && !data.getLongAccessToken().isEmpty()){
						logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_LONG_ACCESS_TOKEN_TXN,MASTERPASS_TXN_SUCCESS);
					}else{
						logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_LONG_ACCESS_TOKEN_TXN,MASTERPASS_TXN_FAIL);
						eWalletValidationErrors.add(new ValidationError("Masterpass AccessToken", "Error while getting Long Access Token."));
					}
				}catch(MasterPassServiceRuntimeException exception){
					LOGGER.error("Exception While getting Long Access Token From Masterpass"+exception.getMessage());
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_LONG_ACCESS_TOKEN_TXN,MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError("Masterpass LongAccessToken", "Error while getting Long Access Token."));
				}
				
				data.setVerifier(data.getVerifier());
				
				try{
					data = this.getCheckoutData(data);
					if(data.getCheckoutXML()!=null && !data.getCheckoutXML().isEmpty()){
						
						// Encrypt Card Details from the Response before saving into Audit Log table
						Checkout checkout = encryptCardData(data.getCheckout());
						data.setCheckoutXML(MasterPassApplicationHelper.xmlEscapeText(MasterPassApplicationHelper.prettyFormat(MasterPassApplicationHelper.printXML(checkout))));
						logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_CHECKOUT_TXN,MASTERPASS_TXN_SUCCESS);
						
					}else{
						eWalletValidationErrors.add(new ValidationError("Masterpass Checkout Data", "Error while getting Card Details for Checkout Data."));
					}
				}catch(MasterPassServiceRuntimeException exception){
					LOGGER.error("Exception While calling Checkout Masterpass Service"+exception.getMessage());
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_CHECKOUT_TXN,MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError("Masterpass Checkout Service", "Error while calling Checkout Service."));
				}
				
				if(eWalletValidationErrors.isEmpty()){
					Map<String,String> paymentDataMap  = new CheckoutResponseMapper().map(data);
					
					ErpPaymentMethodI paymentMethod = parsePaymentMethodForm(paymentDataMap,ewalletRequestData.getCustomerId());
					paymentMethod.seteWalletTrxnId(data.getCheckout().getTransactionId());
					// Add the card detail to Database
					FDCustomerManager.addPaymentMethod(ewalletRequestData.getFdActionInfo(), paymentMethod, ewalletRequestData.isPaymentechEnabled());
						
	                List<ErpPaymentMethodI> paymentMethods = FDCustomerFactory.getErpCustomer(ewalletRequestData.getCustomerId()).getPaymentMethods();
	                if (!paymentMethods.isEmpty()) {
	                	sortPaymentMethodsByIdReserved(paymentMethods);
	            		final PrimaryKey pmPK = ( (ErpPaymentMethodModel)paymentMethods.get(0)).getPK();
	            		FDCustomerManager.setDefaultPaymentMethod( ewalletRequestData.getFdActionInfo(), pmPK );
	            		ewalletResponseData.setPaymentMethod(paymentMethods.get(0));
	            		
	                }
					ErpCustEWalletModel custEWalletModel = new ErpCustEWalletModel();
					// Insert the Long Access Token to FD database
					custEWalletModel.seteWalletId("1");
					custEWalletModel.setCustomerId(ewalletRequestData.getCustomerId());
					custEWalletModel.setLongAccessToken(EWalletCryptoUtil.encrypt(data.getLongAccessToken()));
					// Delete the Long Access Token for the Customer
					deleteLongAccessToken(ewalletRequestData.getCustomerId(),"1");
					// Insert new Long access token
					insertLongAccessToken(custEWalletModel);
					
					if(data.getCheckout() != null && data.getCheckout().getTransactionId()!= null)
						ewalletResponseData.setTransactionId(data.getCheckout().getTransactionId());
				}
				ewalletResponseData.setRedirectUrl("/expressco/checkout.jsp");
			}		
		}catch (Exception e){
			LOGGER.error("Exception: while calling Masterpass checkout and getLong access token service call.", e);
			throw new RemoteException(e.getMessage());
		}
		return ewalletResponseData;
	}
	
	/**
	 * @param data
	 */
	private Checkout encryptCardData(final Checkout chkoutData){
		Checkout encryptedCheckout = new Checkout();
		String maskCard = "XXXXXXXXXXXX";
		try {
			encryptedCheckout.setAuthenticationOptions(chkoutData.getAuthenticationOptions());
			encryptedCheckout.setContact(chkoutData.getContact());
			encryptedCheckout.setExtensionPoint(chkoutData.getExtensionPoint());
			encryptedCheckout.setPreCheckoutTransactionId(chkoutData.getPreCheckoutTransactionId());
			encryptedCheckout.setRewardProgram(chkoutData.getRewardProgram());
			encryptedCheckout.setShippingAddress(chkoutData.getShippingAddress());
			encryptedCheckout.setTransactionId(chkoutData.getTransactionId());
			encryptedCheckout.setWalletID(chkoutData.getWalletID());
			
			Card card = new Card();
			card.setBillingAddress(chkoutData.getCard().getBillingAddress());
			card.setBrandId(chkoutData.getCard().getBrandId());
			card.setBrandName(chkoutData.getCard().getBrandName());
			card.setCardHolderName(chkoutData.getCard().getCardHolderName());
			card.setExpiryMonth(chkoutData.getCard().getExpiryMonth());
			card.setExpiryYear(chkoutData.getCard().getExpiryYear());
			card.setExtensionPoint(chkoutData.getCard().getExtensionPoint());
			card.setAccountNumber(maskCard+chkoutData.getCard().getAccountNumber().substring(chkoutData.getCard().getAccountNumber().length()-4));
			
			encryptedCheckout.setCard(card);
			
		} catch (Exception e) {
			LOGGER.error("Exception: while encrypting the Account Number.", e);
		}
		
		return encryptedCheckout;
	}
	
	private static final Comparator<ErpPaymentMethodI> PAYMENT_COMPARATOR_BY_ID = new Comparator<ErpPaymentMethodI>() {
        @Override
        public int compare(ErpPaymentMethodI o1, ErpPaymentMethodI o2) {
            Long id1 = Long.parseLong(o1.getPK().getId());
            Long id2 = Long.parseLong(o2.getPK().getId());
            return id1.compareTo(id2);
        }

    };

    private static final Comparator<ErpPaymentMethodI> PAYMENT_COMPARATOR_BY_ID_REVERSED = Collections.reverseOrder(PAYMENT_COMPARATOR_BY_ID);//ComparatorChain
            //.<ErpPaymentMethodI> reverseOrder(ComparatorChain.create(PAYMENT_COMPARATOR_BY_ID));

    private void sortPaymentMethodsByIdReserved(List<ErpPaymentMethodI> paymentMethods) {
        Collections.sort(paymentMethods, PAYMENT_COMPARATOR_BY_ID_REVERSED);
    }
    
	private ErpPaymentMethodI parsePaymentMethodForm(Map<String,String> paymentDataMap,String customerId) {
        String actionName = paymentDataMap.get("action");
        EnumPaymentMethodType paymentMethodType = EnumPaymentMethodType.getEnum(paymentDataMap.get(PaymentMethodName.PAYMENT_METHOD_TYPE));
        ErpPaymentMethodI paymentMethod = null;
        
        paymentMethod = PaymentManager.createInstance(paymentMethodType);
        
        String month = paymentDataMap.get(PaymentMethodName.CARD_EXP_MONTH);
        String year = paymentDataMap.get(PaymentMethodName.CARD_EXP_YEAR);
        String cardType = paymentDataMap.get(PaymentMethodName.CARD_BRAND);
        String accountNumber = paymentDataMap.get(PaymentMethodName.ACCOUNT_NUMBER);
        String abaRouteNumber = paymentDataMap.get(PaymentMethodName.ABA_ROUTE_NUMBER);
        String bankName = paymentDataMap.get(PaymentMethodName.BANK_NAME);
        String bankAccountType = paymentDataMap.get(PaymentMethodName.BANK_ACCOUNT_TYPE);
        String csv = paymentDataMap.get(PaymentMethodName.CSV);
        if ("editPaymentMethod".equalsIgnoreCase(actionName) && EnumPaymentMethodType.ECHECK.equals(paymentMethod.getPaymentMethodType())) {
            accountNumber = paymentMethod.getAccountNumber();
        }
        Calendar expCal = new GregorianCalendar();
        if (EnumPaymentMethodType.CREDITCARD.equals(paymentMethod.getPaymentMethodType())) {
            SimpleDateFormat sf = new SimpleDateFormat("MMyyyy");
            Date date = sf.parse((month.trim().length() == 1 ? "0" + month.trim() : month.trim()) + year.trim(), new ParsePosition(0));
            expCal.setTime(date);
            expCal.set(Calendar.DATE, expCal.getActualMaximum(Calendar.DATE));
        } else if (EnumPaymentMethodType.ECHECK.equals(paymentMethod.getPaymentMethodType()) && abaRouteNumber != null && !"".equals(abaRouteNumber)) {
            abaRouteNumber = StringUtils.leftPad(abaRouteNumber, 9, "0");
        }
        paymentMethod.setExpirationDate(expCal.getTime());
        paymentMethod.setName(paymentDataMap.get(PaymentMethodName.ACCOUNT_HOLDER));
        if (accountNumber != null && !accountNumber.equals(paymentMethod.getMaskedAccountNumber())) {
            paymentMethod.setAccountNumber(scrubAccountNumber(accountNumber));
        }
        paymentMethod.setCardType(EnumCardType.getCardType(cardType));
        paymentMethod.setBankAccountType(EnumBankAccountType.getEnum(bankAccountType));
        paymentMethod.setAbaRouteNumber(abaRouteNumber);
        paymentMethod.setBankName(bankName);
        paymentMethod.setAddress1(paymentDataMap.get(EnumUserInfoName.BIL_ADDRESS_1.getCode()));
        paymentMethod.setAddress2(paymentDataMap.get(EnumUserInfoName.BIL_ADDRESS_2.getCode()));
        paymentMethod.setApartment(paymentDataMap.get(EnumUserInfoName.BIL_APARTMENT.getCode()));
        paymentMethod.setCity(paymentDataMap.get(EnumUserInfoName.BIL_CITY.getCode()));
        paymentMethod.setState(paymentDataMap.get(EnumUserInfoName.BIL_STATE.getCode()));
        paymentMethod.setZipCode(paymentDataMap.get(EnumUserInfoName.BIL_ZIPCODE.getCode()));
        paymentMethod.setCountry(paymentDataMap.get(EnumUserInfoName.BIL_COUNTRY.getCode()));
        
        paymentMethod.seteWalletID(paymentDataMap.get(EnumUserInfoName.EWALLET_ID.getCode()));
        paymentMethod.setVendorEWalletID(paymentDataMap.get(EnumUserInfoName.VENDOR_EWALLETID.getCode()));
        paymentMethod.seteWalletTrxnId(paymentDataMap.get(EnumUserInfoName.EWALLET_TXN_ID.getCode()));
        
        if (EnumPaymentMethodType.ECHECK.equals(paymentMethod.getPaymentMethodType())) {
            paymentMethod.setCountry("US");
        }
        paymentMethod.setCVV(csv);
        paymentMethod.setCustomerId(customerId);
        paymentMethod.setBestNumberForBillingInquiries(paymentDataMap.get("phone"));

        return paymentMethod;
    }
	
	/**
     * this method takes a credit card number and removes dashes or spaces and all non numeric numbers from it
     *
     * @param String number to scrub
     * @return dry cleaned number
     */
     static String scrubAccountNumber(String number){
        StringBuffer digitsOnly = new StringBuffer();
        for (int i=0; i<number.length(); i++) {
            char c = number.charAt(i);
            if (Character.isDigit(c)) {
                digitsOnly.append(c);
            }
        }
        return digitsOnly.toString();
    }
	
    // Masterpass Express Checkout
	
	/* (non-Javadoc)
	 * @see com.freshdirect.webapp.ajax.expresscheckout.ewallet.IEwallet#expressCheckout(com.freshdirect.webapp.ajax.expresscheckout.ewallet.EwalletRequestData)
	 */
	public EwalletResponseData expressCheckout(EwalletRequestData ewalletRequestData) throws RemoteException {
		
		EwalletResponseData ewalletResponseData = new EwalletResponseData();
		List<ValidationError> eWalletValidationErrors = new ArrayList<ValidationError>(); 
		try{
 			ErpCustEWalletModel erpCustEWalletModel = FDCustomerManager.findLongAccessTokenByCustID(ewalletRequestData.getCustomerId(), MASTERPASS_WALLET_TYPE_NAME);
	    	
			if(erpCustEWalletModel != null && erpCustEWalletModel.getLongAccessToken()!=null && !erpCustEWalletModel.getLongAccessToken().isEmpty()) {
				
				MasterpassData data = new MasterpassData();
				
				List<String> cardTypes = new ArrayList<String>();
				cardTypes.add(MASTERPASS_CARD_TYPES);
				
				data.setPairingDataTypes(cardTypes);
				data.setLongAccessToken(EWalletCryptoUtil.decrypt(erpCustEWalletModel.getLongAccessToken()));
				data.setSilentlyPaired(false);
				
				PaymentData paymentData = null;
				String precheckoutCardId="";
				// Get default Masterpass Card Details from Database 
				if(ewalletRequestData.getPaymentData() !=null ) {
					paymentData =ewalletRequestData.getPaymentData();
				}
				if(ewalletRequestData.getPrecheckoutCardId() != null) {
					precheckoutCardId = ewalletRequestData.getPrecheckoutCardId();
				}
				try{
						// 	get Precheckout data
					data = this.getPreCheckoutData(data,paymentData);
					
					if(data.getPreCheckoutData()!=null && data.getPrecheckoutTransactionId()!=null){
						logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_PRECHECKOUT_TXN,MASTERPASS_TXN_SUCCESS);
						
						if(precheckoutCardId != null && precheckoutCardId.length() >0) {
							setPrechekoutCardId(precheckoutCardId,data);
						}
					}
					else{
						eWalletValidationErrors.add(new ValidationError("MP_PRE_CHECKOUT", "Invalid Prechekout Response"));
						logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_PRECHECKOUT_TXN,MASTERPASS_TXN_FAIL);
					}
				}catch(MasterPassServiceRuntimeException exception){
					LOGGER.error("Exception While calling Pre Checkout Masterpass Service"+exception.getMessage());
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_PRECHECKOUT_TXN,MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError("Masterpass Pre Checkout Service", "Error while calling Pre Checkout Service."));
				}
				
				// Express Checkout Service Call
				
				try{
					if(eWalletValidationErrors.isEmpty() && data.getPrecheckoutCardId() != null){
						// Call Masterpass Express Checkout service
						data = this.getExpressCheckoutData(data);
						
						if(data.getCheckout()!=null && data.getLongAccessToken()!=null){
							
							// Check if User has changes the Card from Masterpass Wallet so delete the existing MP card and insert new Card
							if(precheckoutCardId != null && precheckoutCardId.length() >0) {
								
								String mpPairedPaymentMethod = "";
								// Delete the existing Card if exists 
								if(ewalletRequestData.getMpPairedPaymentMethod() !=null){
									mpPairedPaymentMethod = ewalletRequestData.getMpPairedPaymentMethod();
									ErpPaymentMethodI paymentMethod = FDCustomerManager.getPaymentMethod(ewalletRequestData.getFdActionInfo().getIdentity(), mpPairedPaymentMethod);
							        // Delete the Payment data from DB
							        if(paymentMethod!=null)
							        	FDCustomerManager.removePaymentMethod(ewalletRequestData.getFdActionInfo(), paymentMethod);
								}
								// Insert new EWallet card
								
								Map<String,String> paymentDataMap  = new CheckoutResponseMapper().map(data);
								
								ErpPaymentMethodI paymentMethod = parsePaymentMethodForm(paymentDataMap,ewalletRequestData.getCustomerId());
								// Add the card detail to Database
								FDCustomerManager.addPaymentMethod(ewalletRequestData.getFdActionInfo(), paymentMethod, ewalletRequestData.isPaymentechEnabled());
				                List<ErpPaymentMethodI> paymentMethods = FDCustomerFactory.getErpCustomer(ewalletRequestData.getFdActionInfo().getIdentity()).getPaymentMethods();
				                if (!paymentMethods.isEmpty()) {
				                	sortPaymentMethodsByIdReserved(paymentMethods);
				            		final PrimaryKey pmPK = ( (ErpPaymentMethodModel)paymentMethods.get(0)).getPK();
				            		FDCustomerManager.setDefaultPaymentMethod( ewalletRequestData.getFdActionInfo(), pmPK );
				            		ewalletResponseData.setPaymentMethod(paymentMethods.get(0));
				                }
				                
							}
							boolean isValidCard = false;
							//  validate the card.
							if(precheckoutCardId == null || precheckoutCardId.length() == 0) {
								isValidCard = validateCard(paymentData,data.getCheckout());
							}else{
								isValidCard = true;
							}
							if(!isValidCard){
								eWalletValidationErrors.add(new ValidationError("Invalid Card", "Invalid Card received from MP"));
							}else{
								//Encrypt Card Data before saving to Audit Log 
								ExpressCheckoutResponse expressCheckoutResponse = data.getExpressCheckoutResponseData();
								Checkout chkoutData = encryptCardData(data.getCheckout());
								expressCheckoutResponse.setCheckout(chkoutData);
								expressCheckoutResponse.setLongAccessToken(EWalletCryptoUtil.encrypt(data.getLongAccessToken()));
								data.setExpressCheckoutResponse(MasterPassApplicationHelper.xmlEscapeText(MasterPassApplicationHelper.prettyFormat(MasterPassApplicationHelper.printXML(expressCheckoutResponse))));
								logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_EXPRESSCHECKOUT_TXN,MASTERPASS_TXN_SUCCESS);
							}
						}else{
							eWalletValidationErrors.add(new ValidationError("MP_EXPRESS_CHECKOUT", "Invalid Express Checkout Response"));
							logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_EXPRESSCHECKOUT_TXN,MASTERPASS_TXN_FAIL);
						}
					}else{
						eWalletValidationErrors.add(new ValidationError("MP_EXPRESS_CHECKOUT", "Not Valid Precheckout Response received. Payment card not matched"));
					}
				}catch(MasterPassServiceRuntimeException exception){
					LOGGER.error("Exception While calling Pre Checkout Masterpass Service"+exception.getMessage());
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_EXPRESSCHECKOUT_TXN,MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError("Masterpass Pre Checkout Service", "Error while calling Pre Checkout Service."));
				}
				
				// Error Occurred while Precheckout/Express Checkout Service Call
				if(eWalletValidationErrors !=null && !eWalletValidationErrors.isEmpty()){
					// Delete Invalid Long Access Token and Delete the Saved Card Details from FD
					deleteLongAccessToken(ewalletRequestData.getCustomerId(),"1");
					// Delete the Card Details from PaymentMethod table
					if(paymentData!=null){
						
						ErpPaymentMethodI paymentMethod = FDCustomerManager.getPaymentMethod(ewalletRequestData.getFdActionInfo().getIdentity(), paymentData.getId());
				        // Delete the Payment data from DB
				        if(paymentMethod!=null)
				        	FDCustomerManager.removePaymentMethod(ewalletRequestData.getFdActionInfo(), paymentMethod);

				        ewalletRequestData.setPaymentData(null);
					}
				}else{
					// Save the Transaction Id received from Masterpass to User logged in session when User places the order same transaction id should be persisted into DB
					if(data.getCheckout() != null && data.getCheckout().getTransactionId()!= null){
						ewalletResponseData.setTransactionId(data.getCheckout().getTransactionId());
						// Update received the new Long access token  
						updateCustomerLongAccessToken(ewalletRequestData.getCustomerId(),EWalletCryptoUtil.encrypt(data.getLongAccessToken()),MASTERPASS_WALLET_TYPE_NAME);
						if(precheckoutCardId == null || precheckoutCardId.length() == 0) {
							ErpPaymentMethodI paymentMethod = FDCustomerManager.getPaymentMethod(ewalletRequestData.getFdActionInfo().getIdentity(), paymentData.getId());
							paymentMethod.seteWalletTrxnId(data.getCheckout().getTransactionId());
							ewalletResponseData.setPaymentMethod(paymentMethod);
						}
					}
				}
			}else{	// Long Access Doesn't exists in DB, Masterpass EWallet is not Paired
				eWalletValidationErrors.add(new ValidationError("MP_Express_Checkout", "EWallet is not Paired"));
				ValidationResult result = new ValidationResult();
				result.setErrors(eWalletValidationErrors);
				ewalletResponseData.setValidationResult(result);
			}
		}catch(Exception exception){
			LOGGER.error("Exception: while calling Masterpass express checkout service call.", exception);
			throw new RemoteException(exception.getMessage());
		}
		return ewalletResponseData;
	}

	/**
	 * @param ewalletRequestData
	 * @return
	 * @throws RemoteException
	 */
	public EwalletResponseData expressCheckoutWithoutPrecheckout(EwalletRequestData ewalletRequestData) throws RemoteException {
		
		EwalletResponseData ewalletResponseData = new EwalletResponseData();
		List<ValidationError> eWalletValidationErrors = new ArrayList<ValidationError>(); 
		try{
 			ErpCustEWalletModel erpCustEWalletModel = FDCustomerManager.findLongAccessTokenByCustID(ewalletRequestData.getCustomerId(), MASTERPASS_WALLET_TYPE_NAME);
	    	
			if(erpCustEWalletModel != null && erpCustEWalletModel.getLongAccessToken()!=null && !erpCustEWalletModel.getLongAccessToken().isEmpty()) {
				
				MasterpassData data = new MasterpassData();
				
				List<String> cardTypes = new ArrayList<String>();
				cardTypes.add(MASTERPASS_CARD_TYPES);
				
				data.setPairingDataTypes(cardTypes);
				data.setLongAccessToken(EWalletCryptoUtil.decrypt(erpCustEWalletModel.getLongAccessToken()));
				data.setSilentlyPaired(false);
				
				if(ewalletRequestData.getPrecheckoutCardId() != null) {
					data.setPrecheckoutCardId(ewalletRequestData.getPrecheckoutCardId());
				}
				if(ewalletRequestData.getPrecheckoutTransactionId() != null) {
					data.setPrecheckoutTransactionId(ewalletRequestData.getPrecheckoutTransactionId());
				}
				
				// Check If request came from Mobile then change the callBackDomain
				if(ewalletRequestData.getMobileCallbackDomain() != null && ewalletRequestData.getMobileCallbackDomain().length() >0){
					data.setCallbackDomain(ewalletRequestData.getMobileCallbackDomain());
				}
				
				// Express Checkout Service Call
				try{
					if(data.getPrecheckoutCardId() != null && data.getPrecheckoutTransactionId() != null){
						// Call Masterpass Express Checkout service
						data = this.getExpressCheckoutData(data);
						
						if(data.getCheckout()!=null && data.getLongAccessToken()!=null){
							
							if(data.getPrecheckoutCardId() != null && data.getPrecheckoutCardId().length() >0) {
								// Remove if any existing MP Wallet PaymentMethod is stored into DB
								removeMPWalletCards(ewalletRequestData);
								
								// Insert new EWallet card
								Map<String,String> paymentDataMap  = new CheckoutResponseMapper().map(data);
								
								ErpPaymentMethodI paymentMethod = parsePaymentMethodForm(paymentDataMap,ewalletRequestData.getCustomerId());
								// Add the card detail to Database
								FDCustomerManager.addPaymentMethod(ewalletRequestData.getFdActionInfo(), paymentMethod, ewalletRequestData.isPaymentechEnabled());
				                List<ErpPaymentMethodI> paymentMethods = FDCustomerFactory.getErpCustomer(ewalletRequestData.getFdActionInfo().getIdentity()).getPaymentMethods();
				                if (!paymentMethods.isEmpty()) {
				                	sortPaymentMethodsByIdReserved(paymentMethods);
				            		final PrimaryKey pmPK = ( (ErpPaymentMethodModel)paymentMethods.get(0)).getPK();
				            		FDCustomerManager.setDefaultPaymentMethod( ewalletRequestData.getFdActionInfo(), pmPK );
				            		ewalletResponseData.setPaymentMethod(paymentMethods.get(0));
				                }
							}
							logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_EXPRESSCHECKOUT_TXN,MASTERPASS_TXN_SUCCESS);
						}else{
							eWalletValidationErrors.add(new ValidationError("MP_EXPRESS_CHECKOUT", "Invalid Express Checkout Response"));
							logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_EXPRESSCHECKOUT_TXN,MASTERPASS_TXN_FAIL);
						}
					}else{
						eWalletValidationErrors.add(new ValidationError("MP_EXPRESS_CHECKOUT", "Precheckout Card ID is missing"));
					}
				}catch(MasterPassServiceRuntimeException exception){
					LOGGER.error("Exception While calling Pre Checkout Masterpass Service"+exception.getMessage());
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_EXPRESSCHECKOUT_TXN,MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError("Masterpass Pre Checkout Service", "Error while calling Pre Checkout Service."));
				}
				
				// Save the Transaction Id received from Masterpass to User logged in session when User places the order same transaction id should be persisted into DB
				if(data.getCheckout() != null && data.getCheckout().getTransactionId()!= null){
					ewalletResponseData.setTransactionId(data.getCheckout().getTransactionId());
					// Update received the new Long access token  
					updateCustomerLongAccessToken(ewalletRequestData.getCustomerId(),EWalletCryptoUtil.encrypt(data.getLongAccessToken()),MASTERPASS_WALLET_TYPE_NAME);
				}
			}else{	// Long Access Doesn't exists in DB, Masterpass EWallet is not Paired
				eWalletValidationErrors.add(new ValidationError("MP_Express_Checkout", "EWallet is not Paired"));
				ValidationResult result = new ValidationResult();
				result.setErrors(eWalletValidationErrors);
				ewalletResponseData.setValidationResult(result);
			}
		}catch(Exception exception){
			LOGGER.error("Exception: while calling Masterpass express checkout service call.", exception);
			throw new RemoteException(exception.getMessage());
		}
		return ewalletResponseData;
	}
	
	/**
	 * @param ewalletRequestData
	 */
	private ErpPaymentMethodI searchMPWalletCards(EwalletRequestData ewalletRequestData, ErpPaymentMethodI mapPaymentMethod){
		ErpPaymentMethodI paymentMethodModel = null;
		try {
			List<ErpPaymentMethodI> paymentMethods = FDCustomerFactory.getErpCustomer(ewalletRequestData.getFdActionInfo().getIdentity()).getPaymentMethods();
			for(ErpPaymentMethodI paymentMethod:paymentMethods){
				 if(paymentMethod.geteWalletID() != null && paymentMethod.geteWalletID().equals("1")){
					 if(compareCard(paymentMethod,mapPaymentMethod)){
						 paymentMethodModel = paymentMethod;
						 break;
					 }
				 }
			}
		} catch (FDResourceException fdException) {
			LOGGER.error("Exception: while calling removing MP paired cards from database.", fdException);
			fdException.printStackTrace();
		}
		return paymentMethodModel;
	}
	
	/**
	 * @param paymentMethod
	 * @param mpPaymentMethod
	 * @return
	 */
	private boolean compareCard(ErpPaymentMethodI paymentMethod , ErpPaymentMethodI mpPaymentMethod){
		boolean matched = true;
		// Check Vendor Wallet ID
		if(!paymentMethod.getVendorEWalletID().equals(mpPaymentMethod.getVendorEWalletID())){
			return false;
		}
		if(!paymentMethod.getCardType().getName().equals(mpPaymentMethod.getCardType().getName())){
			return false;
		}
		if(!paymentMethod.getMaskedAccountNumber().equals(mpPaymentMethod.getMaskedAccountNumber())){
			return false;
		}if(!paymentMethod.geteWalletID().equals(mpPaymentMethod.geteWalletID())){
			return false;
		}if(!paymentMethod.getName().equals(mpPaymentMethod.getName())){
			return false;
		}
		if(paymentMethod.getExpirationDate().compareTo(mpPaymentMethod.getExpirationDate()) != 0){
			return false;
		}
		
		return matched;
	}
	/**
	 * @param ewalletRequestData
	 */
	private void removeMPWalletCards(EwalletRequestData ewalletRequestData){
		try {
			List<ErpPaymentMethodI> paymentMethods = FDCustomerFactory.getErpCustomer(ewalletRequestData.getFdActionInfo().getIdentity()).getPaymentMethods();
			for(ErpPaymentMethodI paymentMethod:paymentMethods){
				 if(paymentMethod.geteWalletID() != null && paymentMethod.geteWalletID().equals("1")){
			        FDCustomerManager.removePaymentMethod(ewalletRequestData.getFdActionInfo(), paymentMethod);
			        break;
				 }
			}
		} catch (FDResourceException fdException) {
			LOGGER.error("Exception: while calling removing MP paired cards from database.", fdException);
			fdException.printStackTrace();
		}
	}
	/**
	 * This method find and sets the precheckout card ID
	 * @param precheckoutCardId
	 * @param data
	 */
	private void setPrechekoutCardId(String precheckoutCardId, MasterpassData data){
		
		if (data.getPreCheckoutData()!= null && data.getPreCheckoutData().getCards() != null){
			for (PrecheckoutCard preCheckoutCard : data.getPreCheckoutData().getCards().getCard()) {
				if(precheckoutCardId.equals(preCheckoutCard.getCardId())){
					data.setPrecheckoutCardId(preCheckoutCard.getCardId());
					break;
				}
			}
		}
	}
	/**
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private MasterpassData getPreCheckoutData(MasterpassData data, PaymentData paymentData) throws Exception{
		MasterPassService service =null;
		
		try {
			PrecheckoutDataRequest preCheckoutDataRequest;
			PrecheckoutDataResponse response;
			preCheckoutDataRequest = generatePreCheckoutDataRequest(data.getPairingDataTypes());
			String preCheckoutXml= MasterPassApplicationHelper.printXML(preCheckoutDataRequest);
			
			data.setPrecheckoutRequest(MasterPassApplicationHelper.xmlEscapeText(MasterPassApplicationHelper.prettyFormat(preCheckoutXml)));
			
			// Initialize the service of Masterpass SDK
			service=initiateMasterpassService(data);
			
			response = service.getPreCheckoutData(data.getPreCheckoutUrl(),preCheckoutDataRequest,data.getLongAccessToken());
			
			data.setPreCheckoutData(response.getPrecheckoutData());
			data.setLongAccessToken(response.getLongAccessToken());
			data.setWalletName(response.getPrecheckoutData().getWalletName());
			data.setConsumerWalletId(response.getPrecheckoutData().getConsumerWalletId());
			data.setPrecheckoutResponse(MasterPassApplicationHelper.xmlEscapeText(MasterPassApplicationHelper.prettyFormat(MasterPassApplicationHelper.printXML(response))));
			data.setPreCheckoutDataXml(MasterPassApplicationHelper.printXML(response));
//			System.out.print(SampleApplicationHelper.printXML(response));
			
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(response);
			
			data.setPreCheckoutDataJson(json);
			 
			if (response.getPrecheckoutData().getShippingAddresses() != null){
				for (PrecheckoutShippingAddress shippingAddress : response.getPrecheckoutData().getShippingAddresses().getShippingAddress()) {
					data.setPrecheckoutShippingId(shippingAddress.getAddressId());
					break;
				}
			}
			
			String cardLastFourDigit="";
			 if (paymentData !=null && paymentData.getAccountNumber() != null && !paymentData.getAccountNumber().isEmpty()) {
				 if (8 <= paymentData.getAccountNumber().length()) {
					 cardLastFourDigit= paymentData.getAccountNumber().substring(paymentData.getAccountNumber().length()-4);
				 }
			 }
			 
			if (response.getPrecheckoutData().getCards() != null){
				for (PrecheckoutCard preCheckoutCard : response.getPrecheckoutData().getCards().getCard()) {
					if(cardLastFourDigit.equals(preCheckoutCard.getLastFour())){
						data.setPrecheckoutCardId(preCheckoutCard.getCardId());
						break;
					}
				}
			}
			
			data.setPrecheckoutTransactionId(response.getPrecheckoutData().getPrecheckoutTransactionId());
			saveConnectionHeader(data);
			return data;
		} catch (Exception e) {
			saveConnectionHeader(data);
			throw new MasterPassServiceRuntimeException(e);
		}
		
	}
	
	/**
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private MasterpassData getExpressCheckoutData(MasterpassData data) throws Exception{
		MasterPassService service =null;
		try {
			ExpressCheckoutRequest expressCheckoutRequest;
			ExpressCheckoutResponse response;
			expressCheckoutRequest = parseExpressCheckoutFile(data);
			
			String expressCheckoutRequestXml= MasterPassApplicationHelper.printXML(expressCheckoutRequest);
			data.setExpressCheckoutRequest(MasterPassApplicationHelper.xmlEscapeText(MasterPassApplicationHelper.prettyFormat(expressCheckoutRequestXml)));
			data.setExpressCheckoutRequestData(expressCheckoutRequest);
			
			// Initialize the service of Masterpass SDK
			service=initiateMasterpassService(data);
			response = service.getExpressCheckoutData(data.getExpressCheckoutUrl(),expressCheckoutRequest,data.getLongAccessToken());
			
			data.setExpressCheckoutResponse(MasterPassApplicationHelper.xmlEscapeText(MasterPassApplicationHelper.prettyFormat(MasterPassApplicationHelper.printXML(response))));
			data.setExpressCheckoutResponseData(response);
			data.setCheckout(response.getCheckout());
			data.setLongAccessToken(response.getLongAccessToken());
			data.setExpressCheckoutIndicator(true);
			
			if (response.getErrors() != null && response.getErrors().getError().size() > 0){
				for (com.mastercard.mcwallet.sdk.xml.allservices.Error error : response.getErrors().getError()){
					if (error.getSource().equals("3DS Needed")) data.setExpressSecurityRequired(true);
				}
			} else {
				data.setExpressSecurityRequired(false);
			}
			
			saveConnectionHeader(data);
			return data;
		} catch (Exception e) {
			saveConnectionHeader(data);
			throw new MasterPassServiceRuntimeException(e);
		}
		
	}

	/**
	 * @param data
	 * @return
	 */
	private ExpressCheckoutRequest parseExpressCheckoutFile(MasterpassData data) {
		
		ExpressCheckoutRequest expressCheckoutRequest = new ExpressCheckoutRequest();		

		expressCheckoutRequest.setMerchantCheckoutId(data.getCheckoutIdentifier());
		expressCheckoutRequest.setPrecheckoutTransactionId(data.getPrecheckoutTransactionId());
		long orderAmount = 100;
		if(data.getShoppingCart() != null) {
			orderAmount = data.getShoppingCart().getShoppingCart().getSubtotal();
			orderAmount = orderAmount + data.getTax() + (long)data.getShipping();
		}
		expressCheckoutRequest.setCurrencyCode(CURRENCY_CODE);
		expressCheckoutRequest.setOrderAmount(orderAmount);		
		expressCheckoutRequest.setCardId(data.getPrecheckoutCardId());				
		expressCheckoutRequest.setOriginUrl(data.getCallbackDomain());
		expressCheckoutRequest.setAdvancedCheckoutOverride(false);
		expressCheckoutRequest.setDigitalGoods(true);
			    
		return expressCheckoutRequest;
	}
	/**
	 * @param dataTypes
	 * @return
	 */
	protected PrecheckoutDataRequest generatePreCheckoutDataRequest(List<String> dataTypes) {
		PrecheckoutDataRequest precheckout = new PrecheckoutDataRequest();
		PairingDataTypes pairingDataTypes = new PairingDataTypes();
		for (String type : dataTypes) {
			PairingDataType dataType = new PairingDataType();
			dataType.setType(type);
			pairingDataTypes.getPairingDataType().add(dataType);
		}
		precheckout.setPairingDataTypes(pairingDataTypes);
		return precheckout;
	}

	/**
	 * Method that retrieves the Pairing Token from the Payment Site.
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private MasterpassData getPairingToken(MasterpassData data) throws Exception {
		
		MasterPassService service =null;
		
		try {
			
			
			// Initialize the service of Masterpass SDK
			    service=initiateMasterpassService(data);
			    
			RequestTokenResponse pairingTokenResponse = service.getPairingToken(data.getRequestURL(),
					data.getCallbackUrl());			
			data.setPairingToken(pairingTokenResponse.getOauthToken());
			
			saveConnectionHeader(data);
			
			return data;
		} catch (Exception e) {
			saveConnectionHeader(data);
			throw new MasterPassServiceRuntimeException(e);
		}
	}
	
	/**
	 * Method to retrieve the Access Token from the MasterCard Services.
	 * 
	 * @param command
	 * 
	 * @return Command bean with the Access Token set.
	 * 
	 * @throws Exception
	 */
	private MasterpassData getAccessToken(MasterpassData data) throws Exception {
		MasterPassService service =null;
		
		try {
			
			// Initialize the service of Masterpass SDK
			  service=initiateMasterpassService(data);
			  
			data.setAccessTokenResponse(service.getAccessToken(data.getAccessURL(),data.getRequestToken(),data.getVerifier()));
			
			saveConnectionHeader(data);
		} catch (Exception e) {
			saveConnectionHeader(data);
			throw new MasterPassServiceRuntimeException(e);
		}
		return data;
	}
	
	private MasterpassData getLongAccessToken(MasterpassData data) throws Exception {
		MasterPassService service =null;
		
		try {
			
			// Initialize the service of Masterpass SDK
			  service=initiateMasterpassService(data);
			  
			data.setLongAccessTokenResponse(service.getLongAccessToken(data.getAccessURL(),data.getPairingToken(),data.getPairingVerifier()));
			data.setLongAccessToken(data.getLongAccessTokenResponse().getOauthToken());
			saveConnectionHeader(data);
		} catch (Exception e) {
			saveConnectionHeader(data);
			throw new MasterPassServiceRuntimeException(e);
		}
		return data;
	}
	
	/**
	 * Method to get the Checkout Resources from MasterCard services. 
	 * 
	 * @param command
	 * 
	 * @return Command bean with the CheckoutXML set.
	 * 
	 * @throws Exception
	 */
	private MasterpassData getCheckoutData(MasterpassData command) throws Exception{
		MasterPassService service =null;
		
		try {
			
			// Initialize the service of Masterpass SDK
			  service=initiateMasterpassService(command);
			  						
			Checkout checkout = service.getPaymentShippingResource(command.getCheckoutResourceURL(),command.getAccessTokenResponse().getOauthToken());
			command.setCheckout(checkout);
			
			command.setCheckoutXML(MasterPassApplicationHelper.xmlEscapeText(MasterPassApplicationHelper.prettyFormat(MasterPassApplicationHelper.printXML(checkout))));
			
			saveConnectionHeader(command);
			
			return command;
		} catch (Exception e) {
			saveConnectionHeader(command);
			throw new MasterPassServiceRuntimeException(e);
		}
		
	}
	
	/**
	 * Method that retrieves the Request Token and then constructs the URl that redirects the user to the Wallet site.
	 * 
	 * @param data
	 * 
	 * @return command bean with the Request Token and redirect URL set
	 * 
	 * @throws Exception
	 */
	private MasterpassData getRequestTokenAndRedirectUrl(MasterpassData data) throws Exception {
		
		MasterPassService service = null;
		
		try {
			
			// Initialize the service of Masterpass SDK
			  service=initiateMasterpassService(data);
			  
			data.setRequestTokenResponse(service.getRequestTokenAndRedirectUrl(
						data.getRequestURL(),
						data.getCallbackUrl(),
						data.getAcceptedCards(),
						data.getCheckoutIdentifier(), 
						data.getXmlVersion(),
						data.getShippingSuppression(), 
						data.getAuthLevelBasic(),
						data.getRewards(),
						data.getRedirectShippingProfiles()));
			data.setRequestToken(data.getRequestTokenResponse().getOauthToken());
			
			saveConnectionHeader(data);
			
			return data;
		} catch (Exception e) {
			saveConnectionHeader(data);
			throw new MasterPassServiceRuntimeException(e);
		}
	}
	
	
	/**
	 * Initialize the Masterpass SDK Service Class (MasterPassServie)
	 * @param data
	 */
	private MasterPassService initiateMasterpassService(MasterpassData data){
		MasterPassService service=null;
		
		try {			  
			
			 service = serviceFactory(data);
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return service;
	}
	  
	private MasterPassService serviceFactory(MasterpassData data){
		PrivateKey privateKey=null;
		MasterPassService service=null;
		
		try {
			privateKey = this.getPrivateKey(data.getKeystorePath(),data.getKeystorePassword());
			service = new MasterPassService(data.getConsumerKey(), privateKey, data.getCallbackDomain());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return service; 
	}
	
	/**
	 * Abstracts the PrivateKey away from the rest of the code
	 * 
	 * @return PrivateKey
	 * 
	 */
	private PrivateKey getPrivateKey(String fileName, String password) {
				
		KeyStore ks;
		Key	key;
		try {
			ks = KeyStore.getInstance("PKCS12");
			// get user password and file input stream
			ClassLoader cl = this.getClass().getClassLoader();
			InputStream stream = cl.getResourceAsStream(fileName);	
			ks.load(stream, password.toCharArray());
			
			Enumeration<String> enumeration = ks.aliases ();
				
			// uses the default alias
			String keyAlias = (String) enumeration.nextElement();

			key = ks.getKey(keyAlias, password.toCharArray());
		} catch (KeyStoreException e) {
			throw new MasterPassServiceRuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new MasterPassServiceRuntimeException(e);
		} catch (CertificateException e) {
			throw new MasterPassServiceRuntimeException(e);
		} catch (IOException e) {
			throw new MasterPassServiceRuntimeException(e);
		} catch (UnrecoverableKeyException e) {
			throw new MasterPassServiceRuntimeException(e);
		}

		return (PrivateKey) key;
	}
	

	private void saveConnectionHeader(MasterpassData data) {
		MasterPassService service =null;
		
		try {
			// Initialize the service of Masterpass SDK
			  service=initiateMasterpassService(data);
			data.setAuthHeader(service.getAuthHeader());
			data.setEncodedAuthHeader(MasterPassApplicationHelper.xmlEscapeText(data.getAuthHeader()));
			data.setSignatureBaseString(service.getSignatureBaseString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param data
	 * @param requestData
	 * @return
	 */
	private String craeteShoppingCartRequestXML(MasterpassData data,
			EwalletRequestData requestData) {

		StringBuffer shoppingCartRequestXML = new StringBuffer(
				"<?xml version=\"1.0\"?>");
		// Append Start Tag
		shoppingCartRequestXML.append("<ShoppingCartRequest>");
		// Append Oauth Token
		shoppingCartRequestXML.append("<OAuthToken>" + data.getRequestToken()
				+ "</OAuthToken>");
		// Append ShoppingCart
		shoppingCartRequestXML.append("<ShoppingCart>");
		// Append Currency Code
		shoppingCartRequestXML.append("<CurrencyCode>" + CURRENCY_CODE
				+ "</CurrencyCode>");
		// We will change this later as Masterpass SDK supports Subtotal as long
		shoppingCartRequestXML.append(requestData.getShoppingCartItems());
		// Append Subtotal tag
		// Append Ent Tag of ShoppingCart
		shoppingCartRequestXML.append("</ShoppingCart>");
		// Append End Tag
		shoppingCartRequestXML.append("</ShoppingCartRequest>");

		return shoppingCartRequestXML.toString();
	}
	
	/**
	 * @param data
	 * @param shoppingCartRequestasXML
	 * @return
	 * @throws Exception
	 */
	private MasterpassData postShoppingCart(MasterpassData data,String shoppingCartRequestasXML) throws Exception {
		MasterPassService service =null;
		
		try {
				
			ShoppingCartRequest shoppingCartRequest;
			
			// Initialize the service of Masterpass SDK
			  service=initiateMasterpassService(data);
			
			shoppingCartRequest = parseShoppingCartString(data.getRequestTokenResponse().getOauthToken(), data.getCallbackDomain(), shoppingCartRequestasXML);
			String shoppingCartXml= MasterPassApplicationHelper.printXML(shoppingCartRequest);
			shoppingCartXml = MasterPassApplicationHelper.xmlReplaceImageUrl(shoppingCartXml,data);
			data.setShoppingCartRequest(MasterPassApplicationHelper.xmlEscapeText(MasterPassApplicationHelper.prettyFormat(shoppingCartXml)));
			ShoppingCartResponse response = service.postShoppingCartData(data.getShoppingCartUrl(), shoppingCartRequest);
			data.setShoppingCartResponse(MasterPassApplicationHelper.xmlEscapeText(MasterPassApplicationHelper.prettyFormat(MasterPassApplicationHelper.printXML(response))));
			
			saveConnectionHeader(data);
			return data;
		} 
		catch (Exception e) {
			saveConnectionHeader(data);
			throw new MasterPassServiceRuntimeException(e);
		}
	}
	
	/**
	 * Method that parse the shoppingCart string, adds the Request Token to the XML and returns a ShoppingCartRequest object.
	 * 
	 * @param RequestToken 
	 * 
	 * @return ShoppingCartRequest with the data in the Request Token and the data in the shoppingCart.xml file
	 */
	
	private ShoppingCartRequest parseShoppingCartString(String requestToken, String originUrl, String shoppingCartXml) {
		InputStream stream = new ByteArrayInputStream( shoppingCartXml.getBytes() );
		try {
			JAXBContext jaxb = JAXBContext.newInstance(ShoppingCartRequest.class);
			Unmarshaller unmarshaller = jaxb.createUnmarshaller();
			JAXBElement<ShoppingCartRequest> je = (JAXBElement<ShoppingCartRequest>) unmarshaller.unmarshal(stream);
			ShoppingCartRequest shoppingCartRequest = (ShoppingCartRequest) je.getValue();
			
			List<ShoppingCartItem> items = shoppingCartRequest.getShoppingCart().getShoppingCartItem();
			for (ShoppingCartItem item: items) {
				String escapedItemDescr = item.getDescription();
				escapedItemDescr = StringEscapeUtils.escapeXml(escapedItemDescr);
				escapedItemDescr = MasterPassApplicationHelper.replaceSpecialCharsWithBlanks(escapedItemDescr);
				escapedItemDescr = StringEscapeUtils.unescapeXml(escapedItemDescr);
				item.setDescription(escapedItemDescr);
			}
			shoppingCartRequest.setOAuthToken(requestToken);
			shoppingCartRequest.setOriginUrl(originUrl);
			return shoppingCartRequest;
		} catch (JAXBException e) {
			throw new MasterPassServiceRuntimeException(e);
		}
	}

	
	
	/**
	 * @author Aniwesh Vatsal
	 *
	 */
	private class CheckoutResponseMapper {
    	//TODO implement for eCheck as well
    	public Map<String, String> map(MasterpassData response) {
    		Map<String, String> result = new HashMap<String, String>();
    		

    		result.put(PaymentMethodName.PAYMENT_METHOD_TYPE, MASTERPASS_WALLET_TYPE_NAME);
    		Checkout chkOutResponse = response.getCheckout();
    		if(chkOutResponse !=null){
	    		Card cardData = chkOutResponse.getCard();
		    	if(cardData!=null){
		    		result.put(PaymentMethodName.ACCOUNT_NUMBER, cardData.getAccountNumber());
		    		result.put(PaymentMethodName.ACCOUNT_NUMBER_VERIFY, cardData.getAccountNumber());
		    		result.put(PaymentMethodName.ACCOUNT_HOLDER, cardData.getCardHolderName());
		    		
		    		String brand = cardData.getBrandId();
		    		if(cardData.getBrandId().equalsIgnoreCase(MASTERPASS_BRAND_NAME)){
		    			brand = "MC";
		    		}else if(cardData.getBrandId().equalsIgnoreCase(DISCOVER_BRANDNAME_NAME)){
		    			brand = "DISC";
		    		}else if(cardData.getBrandId().equalsIgnoreCase(AMEX_BRANDNAME_NAME)){
		    			brand = "AMEX";
		    		}else if(cardData.getBrandId().equalsIgnoreCase(VISA_BRANDNAME_NAME)){
		    			brand = "VISA";
		    		}
		    		
		    		result.put(PaymentMethodName.CARD_BRAND, brand);
		    		result.put(PaymentMethodName.CARD_EXP_MONTH, Integer.toString(cardData.getExpiryMonth().intValue()));
		    		result.put(PaymentMethodName.CARD_EXP_YEAR, Integer.toString(cardData.getExpiryYear().intValue()));
		    		
		    		Address billingAddress = cardData.getBillingAddress();
		    				    		
		    		if (billingAddress != null) {
			    		result.put("bil_country", (billingAddress.getCountry() != null ? billingAddress.getCountry() : ""));
			    		result.put("bil_address1", (billingAddress.getLine1() != null ? billingAddress.getLine1() : ""));
			    		result.put("bil_address2", (billingAddress.getLine2() != null ? billingAddress.getLine2() : ""));
			    		result.put("bil_city", (billingAddress.getCity() != null ? billingAddress.getCity() : ""));
			    		result.put("bil_state", getMPState(billingAddress.getCountrySubdivision()));
			    		//result.put("bil_zipcode", (billingAddress.getPostalCode() != null ? billingAddress.getPostalCode() : ""));
			    		result.put("bil_zipcode", formatZipCode(billingAddress));
		    		}
		    		
		    		result.put(EnumUserInfoName.EWALLET_ID.getCode(), "1");
		    		result.put(EnumUserInfoName.VENDOR_EWALLETID.getCode(), chkOutResponse.getWalletID());
		    		result.put(EnumUserInfoName.EWALLET_TXN_ID.getCode(), chkOutResponse.getTransactionId());
		    	}
    		}
    		return result;
    	}
    	
    	private String getMPState(String countryState) {
    		String result = "";
    		if (countryState != null) {
    			String[] countryStateArr = countryState.split("-");
    			result = countryStateArr.length > 1 ? countryStateArr[1] : "";
    		}
    		
    		return result;
    	}
    	private String formatZipCode(Address billingAddress) {
    		String result = "";
    		if (billingAddress != null && null !=billingAddress.getPostalCode()) {
    			result = billingAddress.getPostalCode();
    			if("US".equalsIgnoreCase(billingAddress.getCountry())){
    				if(result.length()>5)
    				result = billingAddress.getPostalCode().substring(0,5);
    			}else if("CA".equalsIgnoreCase(billingAddress.getCountry())){
    				if(result.length()>6)
    				result = billingAddress.getPostalCode().substring(0,6);
    			}
   
    		}
    		
    		return result;
    	}
	}


	 /**
	  * This method will update the Long Access Token into database (Table : CUST.CUST_EWALLET)
	 * @param custId
	 * @param longAccessToken
	 * @param eWalletType
	 */
	private void updateCustomerLongAccessToken(String custId, String longAccessToken,String  eWalletType){
	    	FDCustomerManager.updateLongAccessToken(custId, longAccessToken, eWalletType);
	    }
	
	 /**
	  * This method will insert the new Long Access Token into database (Table : CUST.CUST_EWALLET)
	 * @param custEWalletModel
	 */
	private void insertLongAccessToken(ErpCustEWalletModel custEWalletModel){
	    	FDCustomerManager.insertLongAccessToken(custEWalletModel);
	    }
	
	/**
	 * Method is used to Delete the Long Access Token From the database for the given Customer and EWallet
	 * @param customerID
	 * @param eWalletId
	 */
	private void deleteLongAccessToken(String customerID,String eWalletId){
    	FDCustomerManager.deleteLongAccessToken(customerID,eWalletId);
    }
	
	/**
	 * Compare the Payment Card:
	 * Customer Name, Last 4 Digits of the Card, Billing Address, Card Expiration Date ,Vendor_Ewallet_id 
	 * @param fdPaymentData
	 * @param checkOut
	 * @return
	 */
	private boolean validateCard(PaymentData fdPaymentData,Checkout checkOut) {

		boolean isValidCard = true;
		// Search the Card with Last four digits within received MP cards
		String cardLastFourDigit = "";
		if (fdPaymentData != null && fdPaymentData.getAccountNumber() != null && !fdPaymentData.getAccountNumber().isEmpty()) {
			if (8 <= fdPaymentData.getAccountNumber().length()) {
				cardLastFourDigit = fdPaymentData.getAccountNumber().substring(fdPaymentData.getAccountNumber().length() - 4);
			}
		}
		// Get the card from MP Wallet
		Card mpCard = checkOut.getCard();
		// Check Card Last four digits
		if(mpCard.getAccountNumber() != null){
			if (8 <=  mpCard.getAccountNumber().length()) {
				String mpCardLastFourDigit = mpCard.getAccountNumber().substring(mpCard.getAccountNumber().length() - 4);
				if(! cardLastFourDigit.equals(mpCardLastFourDigit)){
					return false;
				}
			}
		}
		
		// Check Wallet ID
		if(!fdPaymentData.getVendorEWalletID().equals(checkOut.getWalletID())){
			return false;
		}
		
		// Check for CC BrandNAme
		if(!fdPaymentData.getType().equalsIgnoreCase(mpCard.getBrandName())) {
			return false;
		}
		// Validate the Card Holder Name
		if( !mpCard.getCardHolderName().equalsIgnoreCase(fdPaymentData.getNameOnCard())){
			return false;
		}
		
		// Check for Card Expiry Date
		String expirationDate = fdPaymentData.getExpiration();
		if(expirationDate != null ){
			String[] mmyy = expirationDate.split("/");
			if(mmyy != null && mmyy.length >= 2 && mpCard.getExpiryMonth() != null && mpCard.getExpiryYear() != null){
				if( Integer.parseInt(mmyy[0]) != mpCard.getExpiryMonth().intValue()){
					return false;
				}
				if( Integer.parseInt(mmyy[1]) != mpCard.getExpiryYear().intValue()){
					return false;
				}
			}else{
				return false;
			}
		}
		// Verify Address
		Address billAddress = mpCard.getBillingAddress();
		if( ! billAddress.getCity().equalsIgnoreCase(fdPaymentData.getCity())){
			return false;
		}
		// Country Not available PaymentData object
//		if(billAddress.getCountry().equalsIgnoreCase(fdPaymentData.get))
		
		// Get the State from CountrySubdivision ex: US-NY
		String countrySubdivision[] = billAddress.getCountrySubdivision().split("-");
		if(countrySubdivision != null && countrySubdivision.length >=2){
			if(! countrySubdivision[1].equalsIgnoreCase(fdPaymentData.getState())){
				return false;
			}
		}else{
			return false;
		}
		
		if( ! billAddress.getLine1().equalsIgnoreCase(fdPaymentData.getAddress1())){
			return false;
		}
		// Check ZIP code 
		if(! billAddress.getPostalCode().equalsIgnoreCase(fdPaymentData.getZip())){
			return false;
		}

		return isValidCard;
	}
	
	private void logMPPostbackEwalletRequestResponse(MasterpassData data,String requestXML, String responseXML, String trxType, String txnStatus, List<EwalletPostBackModel> trxns) {
		try {
			EwalletActivityLogModel eWalletLogModel = new EwalletActivityLogModel();
	        eWalletLogModel.seteWalletID("1");
	        eWalletLogModel.setRequest(requestXML);
	        eWalletLogModel.setResponse(responseXML);
	        
	        StringBuffer custIds = new StringBuffer();
	        StringBuffer trxnIds = new StringBuffer();
	        StringBuffer orderIds = new StringBuffer();
	        String sep = "";
	        for (EwalletPostBackModel trxn : trxns) {
	        	custIds.append(sep);
	        	custIds.append(trxn.getCustomerId());
	        	trxnIds.append(sep);
	        	trxnIds.append(trxn.getTransactionId());
	        	orderIds.append(sep);
	        	orderIds.append(trxn.getOrderId());
	        	sep = ",";
	        }
	        
	        eWalletLogModel.setTransactionType(trxType);
	        Timestamp timeNow = new Timestamp(Calendar.getInstance().getTimeInMillis()); 
	        eWalletLogModel.setCreationTimeStamp(timeNow);
	        eWalletLogModel.setStatus(txnStatus);
	        
	        String orderIdsStr = orderIds.toString();
	        String custIdsStr = custIds.toString();
	        String trxnIdsStr = trxnIds.toString();
	        
	        
	        if (orderIdsStr.length() > 1500) {
	        	eWalletLogModel.setOrderId(orderIdsStr.substring(0, 1500) + "...");
	        } else {
	        	eWalletLogModel.setOrderId(orderIdsStr);
	        }
	        if (custIdsStr.length() > 1500) {
	        	eWalletLogModel.setCustomerId(custIdsStr.substring(0, 1500) + "...");
	        } else {
	        	eWalletLogModel.setCustomerId(custIdsStr);
	        }
	        if (trxnIdsStr.length() > 1500) {
	        	eWalletLogModel.setTransactionId(trxnIdsStr.substring(0, 1500) + "...");
	        } else {
	        	eWalletLogModel.setTransactionId(trxnIdsStr);
	        }

	        EWalletLogActivity.logActivity(eWalletLogModel);
		} catch (Exception e) {
			LOGGER.error("Error while logging to ewallet log", e);
		}
	}
	
	/**
	 * @param data
	 * @param requestData
	 * @param trxType
	 * @throws Exception 
	 */
	private void logMPEwalletRequestResponse(MasterpassData data,EwalletRequestData requestData, String trxType, String txnStatus) {
		
		try{
		EwalletActivityLogModel eWalletLogModel = new EwalletActivityLogModel();
        eWalletLogModel.seteWalletID("1");

       	eWalletLogModel.setCustomerId(requestData.getCustomerId());

        StringBuffer eWalletTxnRequest= new StringBuffer(); 
        StringBuffer eWalletTxnResponse= new StringBuffer();
        
        if(trxType.equalsIgnoreCase(MASTERPASS_REQ_TOKEN_TXN)){
        	// Create Request String
        	eWalletTxnRequest.append("RequestURL="+data.getCallbackUrl());
        	eWalletTxnRequest.append(";CallBackURL="+data.getRequestURL());
        	eWalletTxnRequest.append(";AcceptedCards="+data.getAcceptedCards());
        	eWalletTxnRequest.append(";ChekoutItendifier="+data.getCheckoutIdentifier());
        	eWalletTxnRequest.append(";XMLVersion="+data.getXmlVersion());
        	eWalletTxnRequest.append(";ShippingSuppression="+data.getShippingSuppression());
        	eWalletTxnRequest.append("AuthLevelBasic="+data.getAuthLevelBasic());
        	eWalletTxnRequest.append(";Rewards="+data.getRewards());
        	eWalletTxnRequest.append(";RedirectShippingProfiles="+data.getRedirectShippingProfiles());
        	// Create Response String
        	eWalletTxnResponse= new StringBuffer();
        	if(data.getRequestTokenResponse() != null && data.getRequestTokenResponse().getOauthToken() != null){
        		eWalletTxnResponse.append("RequestToken="+data.getRequestTokenResponse().getOauthToken());
        		eWalletTxnResponse.append(";OauthTokenSecret ="+data.getRequestTokenResponse().getOauthToken());
        	}
        }else if(trxType.equalsIgnoreCase(MASTERPASS_PAIRING_TOKEN_TXN)){
        	eWalletTxnRequest.append("RequestURL:"+data.getRequestURL());
        	eWalletTxnRequest.append(";CallbackUrl:"+data.getCallbackUrl());
        	eWalletTxnResponse.append("PairingToken:"+data.getPairingToken());
        }else if(trxType.equalsIgnoreCase(MASTERPASS_SHOPPING_CART_TXN)){
        	eWalletTxnRequest.append(data.getShoppingCartRequest());
        	eWalletTxnResponse.append(data.getShoppingCartResponse());
        }else if(trxType.equalsIgnoreCase(MASTERPASS_ACCESS_TOKEN_TXN)){
        	eWalletTxnRequest.append("DataRequestURL:"+data.getAccessURL());
        	eWalletTxnRequest.append(";DataRequestURL:"+data.getRequestToken());
        	eWalletTxnRequest.append(";Verifier:"+data.getVerifier());
        	eWalletTxnResponse.append("OauthToken:"+data.getAccessTokenResponse().getOauthToken());
        	eWalletTxnResponse.append("OauthTokenSecret:"+data.getAccessTokenResponse().getOauthTokenSecret());
        }else if(trxType.equalsIgnoreCase(MASTERPASS_LONG_ACCESS_TOKEN_TXN)){
        	eWalletTxnRequest.append("LongAccessTokenRequestURL:"+data.getAccessURL());
        	eWalletTxnRequest.append(";PairingToken:"+data.getPairingToken());
        	eWalletTxnRequest.append(";ParingVerifier:"+data.getPairingVerifier());
        	eWalletTxnResponse.append("LongAccessToken:"+(data.getLongAccessToken()!=null? EWalletCryptoUtil.encrypt(data.getLongAccessToken()) :"NULL"));
        }else if(trxType.equalsIgnoreCase(MASTERPASS_CHECKOUT_TXN)){
        	eWalletTxnRequest.append("CheckoutResourceURL:"+data.getCheckoutResourceURL());
        	if(data.getLongAccessToken() != null){
        		eWalletTxnRequest.append(";Long Access Token:"+EWalletCryptoUtil.encrypt(data.getLongAccessToken()));
        	}
        	eWalletTxnResponse.append(data.getCheckoutXML());
        	if(data.getCheckout() != null)
        		eWalletLogModel.setTransactionId(data.getCheckout().getTransactionId() != null? data.getCheckout().getTransactionId() :"NULL");
        	else
        		eWalletLogModel.setTransactionId("NULL");
        }else if(trxType.equalsIgnoreCase(MASTERPASS_PRECHECKOUT_TXN)){
        	eWalletTxnRequest.append(data.getPrecheckoutRequest());
        	eWalletTxnResponse.append(data.getPrecheckoutResponse());
        }else if(trxType.equalsIgnoreCase(MASTERPASS_EXPRESSCHECKOUT_TXN)){
        	eWalletTxnRequest.append(data.getExpressCheckoutRequest());
        	eWalletTxnResponse.append(data.getExpressCheckoutResponse());
        	if(data.getCheckout()!=null)
        		eWalletLogModel.setTransactionId(data.getCheckout().getTransactionId());
        }else if(trxType.equalsIgnoreCase(MASTERPASS_MERCHANT_INIT_TXN)){
        	eWalletTxnRequest.append("MerchantURL:"+data.getMerchantInitUrl());
        	eWalletTxnResponse.append(data.getMerchantInitResponse());
        }else if (trxType.equalsIgnoreCase(MASTERPASS_DISCONNECT_WALLET_TXN)) {
			eWalletTxnRequest.append("CustomerID: "+requestData.getCustomerId()+", LongAccessToken : "+(data.getLongAccessToken()!=null? EWalletCryptoUtil.encrypt(requestData.getPairingToken()) :"NULL"));
			//eWalletTxnResponse.append("");
			eWalletTxnResponse.append("Success");
        }else if (trxType.equalsIgnoreCase(MASTERPASS_POSTBACK_TXN)) {
			eWalletTxnRequest.append(data.getPostTransactionSentXml());
			eWalletTxnResponse.append(data.getPostTransactionReceivedXml());
			
		}
        // Encrypt the Request string before saving into Database
    //    eWalletLogModel.setRequest(EWalletCryptoUtil.encrypt(eWalletTxnRequest.toString()));
     // Encrypt the Response string before saving into Database
      //  eWalletLogModel.setResponse(EWalletCryptoUtil.encrypt(eWalletTxnResponse.toString()));
        //Without Encryption:
        eWalletLogModel.setRequest(eWalletTxnRequest.toString());
        eWalletLogModel.setResponse(eWalletTxnResponse.toString());
        
        eWalletLogModel.setTransactionType(trxType);
        Timestamp timeNow = new Timestamp(Calendar.getInstance().getTimeInMillis()); 
        eWalletLogModel.setCreationTimeStamp(timeNow);
        eWalletLogModel.setStatus(txnStatus);
        eWalletLogModel.setOrderId("");
        EWalletLogActivity.logActivity(eWalletLogModel);
        
//        System.out.println("Decrypt: Request--->\n"+EWalletCryptoUtil.decrypt(eWalletTxnRequest.toString()));
//        System.out.println("Decrypt: Response--->\n"+EWalletCryptoUtil.decrypt(eWalletTxnResponse.toString()));
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
	public EwalletResponseData connect(EwalletRequestData ewalletRequestData)
			throws RemoteException {
		EwalletResponseData ewalletResponseData = new EwalletResponseData();

		// Get the Request Token
		MasterpassData data = new MasterpassData();

		ewalletResponseData.setCallbackUrl(data.getCallbackpathConnect());
		ewalletResponseData.seteWalletIdentifier(data.getCheckoutIdentifier()); 
		
		List<ValidationError> eWalletValidationErrors = new ArrayList<ValidationError>(); 
		try {

			// Get the Pairing token from Masterpass
			try{
				// Get the Pairing token from Masterpass
				data = this.getPairingToken(data);
				
				ewalletResponseData.setPairingToken(data.getPairingToken());
				if(data.getPairingToken()!=null && !data.getPairingToken().isEmpty()){
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_PAIRING_TOKEN_TXN,MASTERPASS_TXN_SUCCESS);
				}
				else{
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_PAIRING_TOKEN_TXN,MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError("Masterpass RequestToken", "Error while getting pairing Token From Masterpass"));
				}
				
			}catch(MasterPassServiceRuntimeException exception){
				LOGGER.error("Exception While getting Pairing Token From Masterpass Service"+exception.getMessage());
				logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_PAIRING_TOKEN_TXN,MASTERPASS_TXN_FAIL);
				eWalletValidationErrors.add(new ValidationError("Masterpass RequestToken", "Error while getting pairing Token From Masterpass"));
			}
			ewalletResponseData.setPairingToken(data.getPairingToken());
			
			try{
				// postMerchantInit to Masterpass
				data = this.postMerchantInit(data);
				
				if(data.getPairingToken()!=null && !data.getPairingToken().isEmpty()){
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_MERCHANT_INIT_TXN,MASTERPASS_TXN_SUCCESS);
				}
				else{
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_MERCHANT_INIT_TXN,MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError("Masterpass RequestToken", "Error while getting merchant-init From Masterpass"));
				}
				
			}catch(MasterPassServiceRuntimeException exception){
				LOGGER.error("Exception While getting Pairing Token From Masterpass Service"+exception.getMessage());
				logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_MERCHANT_INIT_TXN,MASTERPASS_TXN_FAIL);
				eWalletValidationErrors.add(new ValidationError("Masterpass RequestToken", "Error while getting merchant-init From Masterpass"));
			}
			
			if (eWalletValidationErrors != null
					&& !eWalletValidationErrors.isEmpty()) {

				ValidationResult result = new ValidationResult();
				result.setErrors(eWalletValidationErrors);
				ewalletResponseData.setValidationResult(result);

			}
		} catch (Exception exception) {
			LOGGER.error("Exception: while calling Masterpass reuesttoken and merchant-initialization service call.", exception);
			throw new RemoteException(exception.getMessage());
		}
		return ewalletResponseData;
	}

	/**
	 * @param ewalletRequestData
	 * @return
	 * @throws RemoteException
	 */
	public EwalletResponseData getAllPayMethodInEwallet(
			EwalletRequestData ewalletRequestData) throws RemoteException {
		EwalletResponseData ewalletResponseData = new EwalletResponseData();
		try {
			ErpCustEWalletModel erpCustEWalletModel = FDCustomerManager.findLongAccessTokenByCustID(ewalletRequestData.getCustomerId(),MASTERPASS_WALLET_TYPE_NAME);
			if (erpCustEWalletModel != null
					&& erpCustEWalletModel.getLongAccessToken() != null
					&& !erpCustEWalletModel.getLongAccessToken().isEmpty()) {
				List<ValidationError> eWalletValidationErrors = new ArrayList<ValidationError>();
				MasterpassData data = new MasterpassData();

				List<String> cardTypes = new ArrayList<String>();
				cardTypes.add(MASTERPASS_CARD_TYPES);

				data.setPairingDataTypes(cardTypes);
				data.setLongAccessToken(EWalletCryptoUtil.decrypt(erpCustEWalletModel.getLongAccessToken()));
				data.setSilentlyPaired(false);

				PaymentData paymentData = null;
				// Get default Masterpass Card Details from Database
				if (ewalletRequestData.getPaymentData() != null) {
					paymentData = ewalletRequestData.getPaymentData();
				}

				try {
					// get Precheckout data
					data = getPreCheckoutData(data, paymentData);
					
					if (data.getPreCheckoutData() != null
							&& data.getPrecheckoutTransactionId() != null) {
						logMPEwalletRequestResponse(data, ewalletRequestData,
								MASTERPASS_PRECHECKOUT_TXN,
								MASTERPASS_TXN_SUCCESS);
					} else {
						eWalletValidationErrors.add(new ValidationError(
								"MP_PRE_CHECKOUT",
								"Invalid Prechekout Response"));
						logMPEwalletRequestResponse(data, ewalletRequestData,
								MASTERPASS_PRECHECKOUT_TXN, MASTERPASS_TXN_FAIL);
					}
				} catch (MasterPassServiceRuntimeException exception) {
					LOGGER.error("Exception While calling Pre Checkout Masterpass Service"
							+ exception.getMessage());
					logMPEwalletRequestResponse(data, ewalletRequestData,
							MASTERPASS_PRECHECKOUT_TXN, MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError(
							"Masterpass Pre Checkout Service",
							"Error while calling Pre Checkout Service."));
				}

				// Error Occurred while Precheckout Service
				// Call
				if (eWalletValidationErrors != null
						&& !eWalletValidationErrors.isEmpty()) {
			
				} else {
					if (data.getPreCheckoutData() != null
							&& data.getPrecheckoutTransactionId() != null) {
						ewalletResponseData.setTransactionId(data
								.getPrecheckoutTransactionId());

						List<PaymentData> walletCards = new ArrayList<PaymentData>();
						if (data.getPreCheckoutData().getCards() != null) {
							
							walletCards = mapEwalletCardsToPaymenData(data.getPreCheckoutData().getCards().getCard(),data.getPreCheckoutData().getConsumerWalletId(),ewalletResponseData);
							
						}

						if (walletCards != null && walletCards.size() > 0) {
							ewalletResponseData.setPaymentDatas(walletCards);
						}
						ewalletResponseData.setPreCheckoutTnxId(data.getPrecheckoutTransactionId());
						// Update received the new Long access token
						updateCustomerLongAccessToken(
								ewalletRequestData.getCustomerId(),
								EWalletCryptoUtil.encrypt(data
										.getLongAccessToken()),
								MASTERPASS_WALLET_TYPE_NAME);
					}
				}

			}

		} catch (Exception exception) {
			LOGGER.error("Exception: while calling Masterpass preCheckout service call.", exception);
			throw new RemoteException(exception.getMessage());
		}
		return ewalletResponseData;
	}

	/**
	 * @param cards
	 * @param cutomerEWalletID
	 * @return
	 */
	private List<PaymentData> mapEwalletCardsToPaymenData(List<PrecheckoutCard> cards,String cutomerEWalletID, EwalletResponseData ewalletResponseData){
		List<PaymentData> walletCards = new ArrayList<PaymentData>();
		for (PrecheckoutCard preCheckoutCard : cards) {
			
			if(preCheckoutCard.getBrandId()!=null){
				if(preCheckoutCard.getBrandId().equalsIgnoreCase(VISA_BRANDNAME_NAME)||
						preCheckoutCard.getBrandId().equalsIgnoreCase(MASTERPASS_BRAND_NAME)||
						preCheckoutCard.getBrandId().equalsIgnoreCase(AMEX_BRANDNAME_NAME)||
						preCheckoutCard.getBrandId().equalsIgnoreCase(DISCOVER_BRANDNAME_NAME)){
					
					PaymentData paymentCardData = new PaymentData();
					paymentCardData.setId(preCheckoutCard.getCardId());
					paymentCardData.setType(preCheckoutCard.getBrandId());
					paymentCardData.setAccountNumber("XXXX"+preCheckoutCard.getLastFour());
					paymentCardData.setBankAccountType(preCheckoutCard.getBrandName());
					paymentCardData.setNameOnCard(preCheckoutCard.getCardHolderName());
					
					String expiryMonth = "";
					if(preCheckoutCard.getExpiryMonth() != null && preCheckoutCard.getExpiryYear() != null){
						expiryMonth = ""+preCheckoutCard.getExpiryMonth();
						if(preCheckoutCard.getExpiryMonth() < 10){
							expiryMonth="0"+preCheckoutCard.getExpiryMonth();
						}
						paymentCardData.setExpiration(expiryMonth
								+ "/"
								+ preCheckoutCard.getExpiryYear());
					}
					
					paymentCardData.setVendorEWalletID(cutomerEWalletID);
					paymentCardData.seteWalletID("1");
					if(preCheckoutCard.isSelectedAsDefault()){
						ewalletResponseData.setPreferredMPCard(preCheckoutCard.getCardId());
					}
					
					walletCards.add(paymentCardData);
				}
			}
		}
		
		return walletCards;
	}
	/**
	 * @param ewalletRequestData
	 * @return
	 * @throws RemoteException
	 */
	public EwalletResponseData connectComplete(
			EwalletRequestData ewalletRequestData) throws RemoteException {
		List<ValidationError> eWalletValidationErrors = new ArrayList<ValidationError>(); 
		EwalletResponseData ewalletResponseData = new EwalletResponseData();
		try {
			if (ewalletRequestData.geteWalletResponseStatus() != null
					&& ewalletRequestData.geteWalletResponseStatus()
							.equalsIgnoreCase(MASTERPASS_CANCEL_STATUS)) {
				eWalletValidationErrors.add(new ValidationError("Masterpass Light Box UI","User has cancelled payment"));
				ValidationResult result = new ValidationResult();
				result.setErrors(eWalletValidationErrors);
				ewalletResponseData.setValidationResult(result);
				ewalletResponseData.setRedirectUrl("/your_account/payment_information.jsp");
			} else {
				
				// If User clicks on "Not Allow" on MP Light UI box then PairingToken/PairingVerifier will be null redirect user to checkout.jsp 
				if(ewalletRequestData.getPairingToken() ==null || ewalletRequestData.getPairingVerifier() ==null){
					eWalletValidationErrors.add(new ValidationError("Masterpass Light Box UI","Pairing denied"));
					ValidationResult result = new ValidationResult();
					result.setErrors(eWalletValidationErrors);
					ewalletResponseData.setValidationResult(result);
					ewalletResponseData.setRedirectUrl("/your_account/payment_information.jsp");
				}
				
				MasterpassData data = new MasterpassData();

				data.setPairingToken(ewalletRequestData.getPairingToken());
				data.setPairingVerifier(ewalletRequestData.getPairingVerifier());

				try{
					data = this.getLongAccessToken(data);
					
					if(data.getLongAccessToken()!=null && !data.getLongAccessToken().isEmpty()){
						logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_LONG_ACCESS_TOKEN_TXN,MASTERPASS_TXN_SUCCESS);
					}else{
						logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_LONG_ACCESS_TOKEN_TXN,MASTERPASS_TXN_FAIL);
						eWalletValidationErrors.add(new ValidationError("Masterpass AccessToken", "Error while getting Long Access Token."));
					}
				}catch(MasterPassServiceRuntimeException exception){
					LOGGER.error("Exception While getting Long Access Token From Masterpass"+exception.getMessage());
					logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_LONG_ACCESS_TOKEN_TXN,MASTERPASS_TXN_FAIL);
					eWalletValidationErrors.add(new ValidationError("Masterpass LongAccessToken", "Error while getting Long Access Token."));
				}
				String longAccessToken = data.getLongAccessToken();
				
				data.setLongAccessToken(longAccessToken);
				
				ErpCustEWalletModel custEWalletModel = new ErpCustEWalletModel();
				// Insert the Long Access Token to FD database
				custEWalletModel.seteWalletId("1");
				custEWalletModel.setCustomerId(ewalletRequestData.getCustomerId());
				custEWalletModel.setLongAccessToken(EWalletCryptoUtil.encrypt(data.getLongAccessToken()));
				// Delete the Long Access Token for the Customer
				deleteLongAccessToken(ewalletRequestData.getCustomerId(),"1");
				
				insertLongAccessToken(custEWalletModel);
				ewalletResponseData.setRedirectUrl("/your_account/payment_information.jsp");
				//ewalletRequestData.getResponse().sendRedirect("/your_account/payment_information.jsp");
				
			}
		} catch (Exception exception) {
			LOGGER.error("Exception: while calling Masterpass LongAccess token service call.", exception);
			throw new RemoteException(exception.getMessage());
		}
		return ewalletResponseData;
	}

	/**
	 * @param ewalletRequestData
	 * @return
	 * @throws RemoteException
	 */
	public EwalletResponseData disconnect(EwalletRequestData ewalletRequestData)
			throws RemoteException {
		try {
			
			ErpCustEWalletModel erpCustEWalletModel = FDCustomerManager
					.findLongAccessTokenByCustID(
							ewalletRequestData.getCustomerId(),
							MASTERPASS_WALLET_TYPE_NAME);
			
			MasterpassData data = new MasterpassData();
			
			deleteLongAccessToken(ewalletRequestData.getCustomerId(), "1");
			
			List<ErpPaymentMethodI> paymentMethods = FDCustomerFactory
					.getErpCustomer(
							ewalletRequestData.getFdActionInfo().getIdentity())
					.getPaymentMethods();
			if (!paymentMethods.isEmpty()) {

				try{
				for(ErpPaymentMethodI payment : paymentMethods){
					if(payment.geteWalletID() != null && payment.geteWalletID().equalsIgnoreCase("1")){
						//ewalletResponseData.setPaymentMethod(i);
						FDCustomerManager.removePaymentMethod(
								ewalletRequestData.getFdActionInfo(),
								payment);
					ewalletRequestData.setPaymentData(null);
						break;
					}
				}
				}catch(Exception e){
					LOGGER.error("Exception: while calling Disconnect - removePaymentMethod service call.", e);
					throw new RemoteException(e.getMessage());
				}

			}
			
			ewalletRequestData.setPairingToken(erpCustEWalletModel.getLongAccessToken());
			logMPEwalletRequestResponse(data,ewalletRequestData,MASTERPASS_DISCONNECT_WALLET_TXN,MASTERPASS_TXN_SUCCESS);
			
		} catch (FDResourceException e) {
			LOGGER.error("Exception: while calling Disconnect service call.", e);
			throw new RemoteException(e.getMessage());
		}
		return new EwalletResponseData();
	}
	
	/**
	 * @param ewalletRequestData - contains all trxn Ids for which postback is required
	 * @return
	 * @throws Exception
	 */
	public EwalletResponseData postback(EwalletRequestData req) throws RemoteException {
		EwalletResponseData response = new EwalletResponseData();
		
		response.setTrxns(postBack(req.getTrxns()));
		
		return response;
	}
	
	/**
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public MasterpassData postMerchantInit(MasterpassData data)
			throws Exception {
		MasterPassService service =null;
		try {
			// Initialize the service of Masterpass SDK
			  service=initiateMasterpassService(data);
			  
			MerchantInitializationRequest merchantInitRequest;
			merchantInitRequest = new MerchantInitializationRequest();
			merchantInitRequest.setOAuthToken(data.getPairingToken());
			merchantInitRequest.setOriginUrl(data.getCallbackDomain());
			String merchantInitXml = MasterPassApplicationHelper
					.printXML(merchantInitRequest);
			merchantInitXml = MasterPassApplicationHelper.xmlReplaceImageUrl(
					merchantInitXml, data);
			data.setMerchantInitRequest(MasterPassApplicationHelper
					.xmlEscapeText(MasterPassApplicationHelper
							.prettyFormat(merchantInitXml)));
			MerchantInitializationResponse response = service
					.postMerchantInitData(data.getMerchantInitUrl(),
							merchantInitRequest);
			data.setMerchantInitResponse(MasterPassApplicationHelper
					.xmlEscapeText(MasterPassApplicationHelper
							.prettyFormat(MasterPassApplicationHelper
									.printXML(response))));

			saveConnectionHeader(data);
			return data;
		} catch (Exception e) {
			saveConnectionHeader(data);
			throw new MasterPassServiceRuntimeException(e);
		}
	}
	
	private String getDateStr(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss");
		String dateStr = sdf.format(date);
		
		return dateStr;
	}
	
	//for unit testing purposes only
	public static void main(String args[]) {
/*		EwalletPostBackModel req = new EwalletPostBackModel();
		req.setTransactionId("312973353");
		req.setCurrency("USD");
		req.setApprovalCode("tst71E");
		req.setExpressCheckoutIndicator(true);
		
		GregorianCalendar date = new GregorianCalendar();
		date.set(2015, 9, 6);
		req.setPurchaseDate(date.getTime());
		req.setOrderAmount(3743);
		req.setTransactionStatus(true);
		
		List<EwalletPostBackModel> trxns = new ArrayList<EwalletPostBackModel>();
		trxns.add(req);
		new MasterpassServiceSessionBean().postBack(trxns);*/
		
		/*new MasterpassServiceSessionBean().transformErrorRespToEwalletInterface("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
				"<Errors>" +
					"<Error>" +
						"<Description>checkout ID: 315123150</Description>" +
						"<ReasonCode>AUTHORIZATION_FAILED</ReasonCode>" +
						"<Recoverable>false</Recoverable>" +
						"<Source>HttpHeader.OAuth.ConsumerKey</Source>" +
					"</Error>" +
					"<Error>" +
						"<Description>checkout ID: 315123095</Description>" +
						"<ReasonCode>AUTHORIZATION_FAILED</ReasonCode>" +
						"<Recoverable>false</Recoverable>" +
						"<Source>HttpHeader.OAuth.ConsumerKey</Source>" +
					"</Error>" +
				"<Errors>");*/
		
	}
	
	
	private List<EwalletPostBackModel> postBack(List<EwalletPostBackModel> postTrxns) {
		long time_method_start = System.currentTimeMillis();
		long curr = System.currentTimeMillis(); 
		MasterpassData mpData = new MasterpassData();
		mpData.setXmlVersion(mpData.getXmlVersion());
		mpData.setAuthLevelBasic(mpData.getAuthLevelBasic());
		
		Map<String, String> ewalletKeyMap = new HashMap<String, String>();
		Map<String, Boolean> isGALMap = new HashMap<String, Boolean>();
		Map<String, EwalletPostBackModel> trxnKeyMap =  new HashMap<String, EwalletPostBackModel>();
		
		MasterPassService svc = initiateMasterpassService(mpData);
		
		LOGGER.debug("Time taken for the method postBack - init MP service (millis) " + (System.currentTimeMillis() - curr));
		curr = System.currentTimeMillis();
		
		MerchantTransactions reqTrxns = transformTrxnsToMPInterface(postTrxns, mpData, ewalletKeyMap, isGALMap, trxnKeyMap);
		
		LOGGER.debug("Time taken for the method postBack - transform to MP interface (millis) " + (System.currentTimeMillis() - curr));
		curr = System.currentTimeMillis();
		
		MerchantTransactions returnedTrxns = null;
		try {
			returnedTrxns = svc.postCheckoutTransaction(mpData.getPostbackurl(), reqTrxns);
			LOGGER.debug("Time taken for the method postBack - external postback (millis) " + (System.currentTimeMillis() - curr));
			curr = System.currentTimeMillis();
			logMPPostbackEwalletRequestResponse(mpData, xmlToString(reqTrxns), xmlToString(returnedTrxns), MASTERPASS_POSTBACK_TXN,MASTERPASS_TXN_SUCCESS, postTrxns);
			LOGGER.debug("Time taken for the method postBack - external postback success loggiing (millis) " + (System.currentTimeMillis() - curr));
			curr = System.currentTimeMillis();
		} catch (MasterPassServiceRuntimeException e) {

			logMPPostbackEwalletRequestResponse(mpData,xmlToString(reqTrxns), e.getMessage(),MASTERPASS_POSTBACK_TXN,MASTERPASS_TXN_FAIL, postTrxns);
			LOGGER.debug("Time taken for the method postBack - external postback failure loggiing (millis) " + (System.currentTimeMillis() - curr));
			curr = System.currentTimeMillis();
			return transformErrorRespToEwalletInterface(e.getMessage(), trxnKeyMap);
		} catch (MCOpenApiRuntimeException e) {

			logMPPostbackEwalletRequestResponse(mpData,xmlToString(reqTrxns), e.getMessage(),MASTERPASS_POSTBACK_TXN,MASTERPASS_TXN_FAIL, postTrxns);
			LOGGER.debug("Time taken for the method postBack - external postback failure loggiing 2 (millis) " + (System.currentTimeMillis() - curr));
			curr = System.currentTimeMillis();
			return transformErrorRespToEwalletInterface(e.getMessage(), trxnKeyMap);
		}
		
		List<EwalletPostBackModel> result = new ArrayList<EwalletPostBackModel>();
		if (returnedTrxns != null) {
			result = transformRespToEwalletInterface(returnedTrxns, ewalletKeyMap, isGALMap);
		}
		
		LOGGER.debug("Time taken for the method postBack - result transformation (millis) " + (System.currentTimeMillis() - curr));
		LOGGER.debug("Time taken for the method postBack (millis) " + (System.currentTimeMillis() - time_method_start));
		return result;
	}
	
	private MerchantTransactions transformTrxnsToMPInterface(List<EwalletPostBackModel> postTrxns, MasterpassData mpData, Map<String, String> ewalletKeyMap,
						Map<String, Boolean> isGALMap, Map<String, EwalletPostBackModel> trxnKeyMap) {
		MerchantTransactions trxns = new MerchantTransactions();
		for (EwalletPostBackModel postTrxn : postTrxns) {
			MerchantTransaction trxn = new MerchantTransaction();
			trxn.setTransactionId(postTrxn.getTransactionId());
			trxn.setConsumerKey(mpData.getConsumerKey());
			trxn.setCurrency(postTrxn.getCurrency());
			trxn.setApprovalCode(postTrxn.getApprovalCode());
			trxn.setOrderAmount(postTrxn.getOrderAmount());
			trxn.setExpressCheckoutIndicator(postTrxn.isExpressCheckoutIndicator());
			GregorianCalendar gCal = new GregorianCalendar();
			gCal.setTime(postTrxn.getPurchaseDate());
			XMLGregorianCalendar xmlCal = null;
			try {
				xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);
			} catch (DatatypeConfigurationException e) {
				LOGGER.error("Data type of Purchase Date cannot be invalid " + postTrxn.getPurchaseDate());
			}
			trxn.setPurchaseDate(xmlCal);
			trxn.setTransactionStatus(com.mastercard.mcwallet.sdk.xml.allservices.TransactionStatus.valueOf(postTrxn.getTransactionStatus()));
			trxns.getMerchantTransactions().add(trxn);
			ewalletKeyMap.put(trxn.getTransactionId() + trxn.getApprovalCode(), postTrxn.getKey());
			isGALMap.put(trxn.getTransactionId() + trxn.getApprovalCode(), postTrxn.isgAL());
			trxnKeyMap.put(trxn.getTransactionId(), postTrxn);
			
		}
		
		return trxns;
	}

	private List<EwalletPostBackModel> transformRespToEwalletInterface(MerchantTransactions retTrxns, Map<String, String> ewalletKeyMap, Map<String, Boolean> isGALMap) {
		List<EwalletPostBackModel> trxns = new ArrayList<EwalletPostBackModel>();
		for (MerchantTransaction trxn : retTrxns.getMerchantTransactions()) {
			EwalletPostBackModel m = new EwalletPostBackModel();
			m.setKey(ewalletKeyMap.get(trxn.getTransactionId()+trxn.getApprovalCode()));
			m.setgAL(isGALMap.get(trxn.getTransactionId()+trxn.getApprovalCode()));
			m.setPostBackSuccess(true);
			trxns.add(m);
		}
		return trxns;
	}
	
	private List<EwalletPostBackModel> transformErrorRespToEwalletInterface(String retTrxns, Map<String, EwalletPostBackModel> trxnKeyMap) {
		XmlDocumentHandler docContent = new XmlDocumentHandler();
		List<EwalletPostBackModel> result = new ArrayList<EwalletPostBackModel>();
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(IOUtils.toInputStream(retTrxns), docContent);
			
		} catch (Exception e) {
			LOGGER.error("Error while parsing Error Response from Masterpass. It could be due to change in format ", e);
			return result;
		}
		
		result = docContent.getErrTrxns();
		for (EwalletPostBackModel model : result) {
			EwalletPostBackModel orig = trxnKeyMap.get(model.getTransactionId());
			
			model.setKey(orig.getKey());
			model.setgAL(orig.isgAL());
		}
		
		return result;
	}
	
	private class XmlDocumentHandler extends DefaultHandler {
 
		/*"<Errors>" +
				"<Error>" +
					"<Description>checkout ID: 315123150</Description>" +
					"<ReasonCode>AUTHORIZATION_FAILED</ReasonCode>" +
					"<Recoverable>false</Recoverable>" +
					"<Source>HttpHeader.OAuth.ConsumerKey</Source>" +
				"</Error>" +
				"<Error>" +
					"<Description>checkout ID: 315123095</Description>" +
					"<ReasonCode>AUTHORIZATION_FAILED</ReasonCode>" +
					"<Recoverable>false</Recoverable>" +
					"<Source>HttpHeader.OAuth.ConsumerKey</Source>" +
				"</Error>" +
			"<Errors>");*/
	    private List<EwalletPostBackModel> errTrxns = new ArrayList<EwalletPostBackModel>();
	    EwalletPostBackModel currTrxn;
	    boolean error = false;
	    boolean inDescr = false;
	    boolean inRecov = false;
	    private static final String ERROR_ELEM = "Error";
	    private static final String DESCR_ELEM = "Description";
	    private static final String DESCR_TRNX_PREFIX = "checkout ID: ";
	    private static final String RECOVERY_ELEM = "Recoverable";
	    
		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String namespaceURI, String localName, String qName,
				Attributes attrs) throws SAXException {
			if (qName.equals(ERROR_ELEM)) {
				error = true;
			}
			if (error && qName.equals(DESCR_ELEM)) {
				inDescr = true;
			}
			if (error && qName.equals(RECOVERY_ELEM)) {
				inRecov = true;
			}
		}
	    
	    /* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] chars, int start, int length)
				throws SAXException {
			if (error && inDescr) {
				String descr = new String(chars, start, length);
				int trnxPrefixIdx = descr.indexOf(DESCR_TRNX_PREFIX);
				if (trnxPrefixIdx != -1) {
					String trxId = descr.substring(trnxPrefixIdx + DESCR_TRNX_PREFIX.length());

					if (trxId != null && !"".equals(trxId.trim())) {
						currTrxn = new EwalletPostBackModel();
						currTrxn.setError(true);
						currTrxn.setTransactionId(trxId.trim());
					}
				}
			}
			
			if (error && inRecov) {
				String recovStr = new String(chars, start, length);
				currTrxn.setRecoverable(recovStr);
			}
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String namespaceURI, String localName, String qName)
				throws SAXException {
			if (error && qName.equals(ERROR_ELEM)) {
				error = false;
				errTrxns.add(currTrxn);
			}
			if (error && qName.equals(DESCR_ELEM)) {
				inDescr = false;
			}
			if (error && qName.equals(RECOVERY_ELEM)) {
				inRecov = false;
			}
		}
		
		public List<EwalletPostBackModel> getErrTrxns() {
			return errTrxns;
		}

	}
	
	
    /**
     * Converts a XML class to a String containing all the data in the class in XML format
     *
     * @param xmlClass
     *
     * @return Marshaled string containing the data stored in merchantTransactions in an XML format
     *
     * @throws MCOpenApiRuntimeException
     */
    protected String xmlToString(Object xmlClass) {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(xmlClass.getClass());
            StringWriter st = new StringWriter();
           
            jaxbContext.createMarshaller().marshal(xmlClass, st);
            String xml = st.toString();
            return xml;

        } catch (JAXBException e) {
            throw new MCOpenApiRuntimeException(e);
        }
    }
}
