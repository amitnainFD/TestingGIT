/*
 * Created on Oct 6, 2015
 *
 */
package com.freshdirect.fdstore.ewallet.ejb;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

import com.freshdirect.fdstore.ewallet.EwalletRequestData;
import com.freshdirect.fdstore.ewallet.EwalletResponseData;

/**
 * @author imohammed
 *
 */
public interface EwalletServiceSB extends EJBObject {
	
	EwalletResponseData getToken(EwalletRequestData ewalletRequestData) throws RemoteException;
	EwalletResponseData checkout(EwalletRequestData ewalletRequestData) throws RemoteException;
	EwalletResponseData expressCheckout(EwalletRequestData ewalletRequestData) throws RemoteException;
	EwalletResponseData connect(EwalletRequestData ewalletRequestData) throws RemoteException;
	EwalletResponseData getAllPayMethodInEwallet(EwalletRequestData ewalletRequestData) throws RemoteException;
	EwalletResponseData connectComplete(EwalletRequestData ewalletRequestData) throws RemoteException;
	EwalletResponseData disconnect(EwalletRequestData ewalletRequestData) throws RemoteException;
	
	//Batch
	EwalletResponseData postbackTrxns(EwalletRequestData req) throws RemoteException;
	
	//Standard checkout
	EwalletResponseData standardCheckout(EwalletRequestData ewalletRequestData) throws RemoteException;
	EwalletResponseData preStandardCheckout(EwalletRequestData ewalletRequestData) throws RemoteException;
	EwalletResponseData expressCheckoutWithoutPrecheckout(EwalletRequestData ewalletRequestData) throws RemoteException;
}