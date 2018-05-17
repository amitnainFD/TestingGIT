/*
 * Created on Oct 6, 2015
 *
 */
package com.freshdirect.fdstore.ewallet.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.fdstore.ewallet.EnumEwalletType;
import com.freshdirect.fdstore.ewallet.EwalletRequestData;
import com.freshdirect.fdstore.ewallet.EwalletResponseData;
import com.freshdirect.fdstore.ewallet.EwalletServiceFactory;
import com.freshdirect.fdstore.ewallet.IEwallet;
import com.freshdirect.fdstore.ewallet.EwalletPostBackModel;
import com.freshdirect.framework.core.DataSourceLocator;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * @author Ismail Mohammed
 *
 */
public class EwalletServiceSessionBean extends SessionBeanSupport {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2639838110668070128L;
	public EwalletResponseData getToken(EwalletRequestData ewalletRequestData) throws RemoteException {
		try {
			return new EwalletServiceFactory().getVendorService(ewalletRequestData).getToken(ewalletRequestData);
		} catch (Exception e) {
			throw new RemoteException("Exception occurred during postback to " + ewalletRequestData.geteWalletType(), e);
		}
	}
	public EwalletResponseData checkout(EwalletRequestData ewalletRequestData) throws RemoteException {
		try {
			return new EwalletServiceFactory().getVendorService(ewalletRequestData).checkout(ewalletRequestData);
		} catch (Exception e) {
			throw new RemoteException("Exception occurred during postback to " + ewalletRequestData.geteWalletType(), e);
		}
	}
	public EwalletResponseData expressCheckout(EwalletRequestData ewalletRequestData) throws RemoteException {
		try {
			return new EwalletServiceFactory().getVendorService(ewalletRequestData).expressCheckout(ewalletRequestData);
		} catch (Exception e) {
			throw new RemoteException("Exception occurred during postback to " + ewalletRequestData.geteWalletType(), e);
		}
	}
	public EwalletResponseData connect(EwalletRequestData ewalletRequestData) throws RemoteException {
		try {
			return new EwalletServiceFactory().getVendorService(ewalletRequestData).connect(ewalletRequestData);
		} catch (Exception e) {
			throw new RemoteException("Exception occurred during postback to " + ewalletRequestData.geteWalletType(), e);
		}
	}
	public EwalletResponseData getAllPayMethodInEwallet(EwalletRequestData ewalletRequestData) throws RemoteException {
		try {
			return new EwalletServiceFactory().getVendorService(ewalletRequestData).getAllPayMethodInEwallet(ewalletRequestData);
		} catch (Exception e) {
			throw new RemoteException("Exception occurred during postback to " + ewalletRequestData.geteWalletType(), e);
		}
	}
	public EwalletResponseData connectComplete(EwalletRequestData ewalletRequestData) throws RemoteException {
		try {
			return new EwalletServiceFactory().getVendorService(ewalletRequestData).connectComplete(ewalletRequestData);
		} catch (Exception e) {
			throw new RemoteException("Exception occurred during postback to " + ewalletRequestData.geteWalletType(), e);
		}
	}
	public EwalletResponseData disconnect(EwalletRequestData ewalletRequestData) throws RemoteException {
		try {
			return new EwalletServiceFactory().getVendorService(ewalletRequestData).disconnect(ewalletRequestData);
		} catch (Exception e) {
			throw new RemoteException("Exception occurred during postback to " + ewalletRequestData.geteWalletType(), e);
		}
	}
	
	//Batch
	public EwalletResponseData postbackTrxns(EwalletRequestData req) throws RemoteException {
		try {
			return new EwalletServiceFactory().getVendorNotificationService(req).postbackTrxns(req);
		} catch (Exception e) {
			throw new RemoteException("Exception occurred during postback to " + req.geteWalletType(), e);
		}
	}
	
	//Standard checkout
	public EwalletResponseData standardCheckout(EwalletRequestData ewalletRequestData) throws RemoteException {
		try {
			return new EwalletServiceFactory().getVendorService(ewalletRequestData).standardCheckout(ewalletRequestData);
		} catch (Exception e) {
			throw new RemoteException("Exception occurred during postback to " + ewalletRequestData.geteWalletType(), e);
		}
	}
	public EwalletResponseData preStandardCheckout(EwalletRequestData ewalletRequestData) throws RemoteException {
		try {
			return new EwalletServiceFactory().getVendorService(ewalletRequestData).preStandardCheckout(ewalletRequestData);
		} catch (Exception e) {
			throw new RemoteException("Exception occurred during postback to " + ewalletRequestData.geteWalletType(), e);
		}
	}
	public EwalletResponseData expressCheckoutWithoutPrecheckout(EwalletRequestData ewalletRequestData) throws RemoteException {
		try {
			return new EwalletServiceFactory().getVendorService(ewalletRequestData).expressCheckoutWithoutPrecheckout(ewalletRequestData);
		} catch (Exception e) {
			throw new RemoteException("Exception occurred during postback to " + ewalletRequestData.geteWalletType(), e);
		}
	}
}
