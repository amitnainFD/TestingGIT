package com.freshdirect.fdstore.ewallet;

import java.rmi.RemoteException;

import javax.ejb.CreateException;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.customer.ejb.FDServiceLocator;
import com.freshdirect.fdstore.ewallet.ejb.EwalletServiceHome;
import com.freshdirect.fdstore.ewallet.ejb.EwalletServiceSB;
import com.freshdirect.fdstore.ewallet.impl.MasterpassRuntimeException;
import com.freshdirect.fdstore.ewallet.impl.ejb.MasterpassServiceHome;
import com.freshdirect.fdstore.ewallet.impl.ejb.MasterpassServiceSB;


/**
 * @author Aniwesh Vatsal
 *
 */
public class EwalletServiceFactory {

	private static final Logger LOG = Logger
			.getLogger(EwalletServiceFactory.class);

	/**
	 * @param ewalletRequestData
	 * @return Returns the Ewallet service instance based on the Ewallet type
	 */
	public IEwallet getEwalletService(EwalletRequestData ewalletRequestData){
		LOG.info("EwalletServiceFactory --> getEwalletService -> Entered ");
		IEwallet ewallet = null;
		if(ewalletRequestData.geteWalletType().equals(EnumEwalletType.MP.getName())){
			ewallet = new EwalletServiceRemoteAdapter();
		}
		LOG.info("EwalletServiceFactory --> getEwalletService -> Exit ");
		return ewallet;
	}
	
	/**
	 * @param ewalletRequestData
	 * @return Returns the Ewallet service instance based on the Ewallet type
	 */
	public IEwallet getVendorService(EwalletRequestData ewalletRequestData){
		LOG.info("EwalletServiceFactory --> getEwalletService -> Entered ");
		IEwallet ewallet = null;
		if(ewalletRequestData.geteWalletType().equals(EnumEwalletType.MP.getName())){
			ewallet = new MPVendorServiceRemoteAdapter();
		}
		LOG.info("EwalletServiceFactory --> getEwalletService -> Exit ");
		return ewallet;
	}
	
	/**
	 * @param ewalletRequestData
	 * @return Returns the Ewallet service instance based on the Ewallet type
	 */
	public IEwallet getVendorService(EnumEwalletType type){
		LOG.info("EwalletServiceFactory --> getEwalletService -> Entered ");
		IEwallet ewallet = null;
		if(type.equals(EnumEwalletType.MP)){
			ewallet = new MPVendorServiceRemoteAdapter();
		}
		LOG.info("EwalletServiceFactory --> getEwalletService -> Exit ");
		return ewallet;
	}
	
	/**
	 * @param ewalletRequestData
	 * @return Returns the Ewallet service instance based on the Ewallet type
	 */
	public IEwallet.NotificationService getEwalletNotificationService(EwalletRequestData ewalletRequestData){
		LOG.info("EwalletServiceFactory --> getEwalletService -> Entered ");
		IEwallet.NotificationService ewallet = null;
		if(ewalletRequestData.geteWalletType().equals(EnumEwalletType.MP.getName())){
			ewallet = new EwalletServiceRemoteAdapter();
		}
		LOG.info("EwalletServiceFactory --> getEwalletService -> Exit ");
		return ewallet;
	}
	
	/**
	 * @param ewalletRequestData
	 * @return Returns the Ewallet service instance based on the Ewallet type
	 */
	public IEwallet.NotificationService getVendorNotificationService(EwalletRequestData ewalletRequestData){
		LOG.info("EwalletServiceFactory -->  getVendorNotificationService -> Entered ");
		IEwallet.NotificationService ewallet = null;
		if(ewalletRequestData.geteWalletType().equals(EnumEwalletType.MP.getName())){
			ewallet = new MPVendorServiceRemoteAdapter();
		}
		LOG.info("EwalletServiceFactory --> getEwalletService -> Exit ");
		return ewallet;
	}
	

	/**
	 * @param ewalletRequestData
	 * @return Returns the Ewallet service instance based on the Ewallet type
	 */
	public IEwallet.NotificationService getVendorNotificationService(EnumEwalletType type){
		LOG.info("EwalletServiceFactory -->  getVendorNotificationService -> Entered ");
		IEwallet.NotificationService ewallet = null;
		if(type.equals(EnumEwalletType.MP)){
			ewallet = new MPVendorServiceRemoteAdapter();
		}
		LOG.info("EwalletServiceFactory --> getEwalletService -> Exit ");
		return ewallet;
	}
	
	private class EwalletServiceRemoteAdapter implements IEwallet, IEwallet.NotificationService {
		
		private EwalletServiceHome remoteHome = null;
		
		EwalletServiceRemoteAdapter() {
			remoteHome = FDServiceLocator.getInstance().getEwalletServiceHome();
			
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#getToken(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData getToken(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				EwalletServiceSB remote = remoteHome.create();
				resp = remote.getToken(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#checkout(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData checkout(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				EwalletServiceSB remote = remoteHome.create();
				resp = remote.checkout(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#expressCheckout(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData expressCheckout(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				EwalletServiceSB remote = remoteHome.create();
				resp = remote.expressCheckout(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#connect(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData connect(EwalletRequestData ewalletRequestData)
				throws Exception {
			EwalletResponseData resp = null;
			try {
				EwalletServiceSB remote = remoteHome.create();
				resp = remote.connect(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#getAllPayMethodInEwallet(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData getAllPayMethodInEwallet(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				EwalletServiceSB remote = remoteHome.create();
				resp = remote.getAllPayMethodInEwallet(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#connectComplete(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData connectComplete(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				EwalletServiceSB remote = remoteHome.create();
				resp = remote.connectComplete(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#disconnect(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData disconnect(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				EwalletServiceSB remote = remoteHome.create();
				resp = remote.disconnect(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}
		
		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#disconnect(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		public EwalletResponseData postbackTrxns(EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				EwalletServiceSB remote = remoteHome.create();

				resp = remote.postbackTrxns(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}

			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		@Override
		public EwalletResponseData preStandardCheckout(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				EwalletServiceSB remote = remoteHome.create();
				resp = remote.preStandardCheckout(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		@Override
		public EwalletResponseData standardCheckout(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				EwalletServiceSB remote = remoteHome.create();
				resp = remote.standardCheckout(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		@Override
		public EwalletResponseData expressCheckoutWithoutPrecheckout(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				EwalletServiceSB remote = remoteHome.create();
				resp = remote.expressCheckoutWithoutPrecheckout(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

	}
	
	private class MPVendorServiceRemoteAdapter implements IEwallet, IEwallet.NotificationService {
		
		private MasterpassServiceHome remoteHome = null;
		
		MPVendorServiceRemoteAdapter() {
			remoteHome = FDServiceLocator.getInstance().getMasterpassServiceHome();
			
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#getToken(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData getToken(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				MasterpassServiceSB remote = remoteHome.create();
				resp = remote.getToken(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#checkout(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData checkout(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				MasterpassServiceSB remote = remoteHome.create();
				resp = remote.checkout(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#expressCheckout(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData expressCheckout(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				MasterpassServiceSB remote = remoteHome.create();
				resp = remote.expressCheckout(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#connect(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData connect(EwalletRequestData ewalletRequestData)
				throws Exception {
			EwalletResponseData resp = null;
			try {
				MasterpassServiceSB remote = remoteHome.create();
				resp = remote.connect(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#getAllPayMethodInEwallet(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData getAllPayMethodInEwallet(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				MasterpassServiceSB remote = remoteHome.create();
				resp = remote.getAllPayMethodInEwallet(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#connectComplete(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData connectComplete(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				MasterpassServiceSB remote = remoteHome.create();
				resp = remote.connectComplete(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#disconnect(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		@Override
		public EwalletResponseData disconnect(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				MasterpassServiceSB remote = remoteHome.create();
				resp = remote.disconnect(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}
		
		/* (non-Javadoc)
		 * @see com.freshdirect.fdstore.ewallet.IEwallet#disconnect(com.freshdirect.fdstore.ewallet.EwalletRequestData)
		 */
		public EwalletResponseData postbackTrxns(EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				MasterpassServiceSB remote = remoteHome.create();
				resp = remote.postback(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		@Override
		public EwalletResponseData preStandardCheckout(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				MasterpassServiceSB remote = remoteHome.create();
				resp = remote.preStandardCheckout(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		@Override
		public EwalletResponseData standardCheckout(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				MasterpassServiceSB remote = remoteHome.create();
				resp = remote.standardCheckout(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

		@Override
		public EwalletResponseData expressCheckoutWithoutPrecheckout(
				EwalletRequestData ewalletRequestData) {
			EwalletResponseData resp = null;
			try {
				MasterpassServiceSB remote = remoteHome.create();
				resp = remote.expressCheckoutWithoutPrecheckout(ewalletRequestData);
			} catch (CreateException e) {
				throw new MasterpassRuntimeException(e);
			} catch (RemoteException e) {
				throw new MasterpassRuntimeException(e);
			}
			
			if (resp == null) {
				resp = new EwalletResponseData();
			}
			return resp;
		}

	}
}
