package com.freshdirect.fdstore.ewallet;

/**
 * @author Aniwesh Vatsal
 *
 */
public interface IEwallet {

	EwalletResponseData getToken(EwalletRequestData ewalletRequestData) throws Exception;
	EwalletResponseData checkout(EwalletRequestData ewalletRequestData) throws Exception;
	EwalletResponseData expressCheckout(EwalletRequestData ewalletRequestData) throws Exception;
	EwalletResponseData connect(EwalletRequestData ewalletRequestData) throws Exception;
	EwalletResponseData getAllPayMethodInEwallet(EwalletRequestData ewalletRequestData) throws Exception;
	EwalletResponseData connectComplete(EwalletRequestData ewalletRequestData) throws Exception;
	EwalletResponseData disconnect(EwalletRequestData ewalletRequestData) throws Exception;
	
	interface NotificationService {
		EwalletResponseData postbackTrxns(EwalletRequestData ewalletRequestData) throws Exception;
	}
	
	EwalletResponseData standardCheckout(EwalletRequestData ewalletRequestData) throws Exception;
	EwalletResponseData preStandardCheckout(EwalletRequestData ewalletRequestData) throws Exception;
	EwalletResponseData expressCheckoutWithoutPrecheckout(EwalletRequestData ewalletRequestData) throws Exception;
	
}
