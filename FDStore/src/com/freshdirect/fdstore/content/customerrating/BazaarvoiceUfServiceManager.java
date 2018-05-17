package com.freshdirect.fdstore.content.customerrating;

import java.rmi.RemoteException;
import java.util.Map;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.util.log.LoggerFactory;

@Deprecated
public class BazaarvoiceUfServiceManager {
	
	private final static Category LOGGER = LoggerFactory.getInstance(BazaarvoiceUfServiceManager.class);

	private static BazaarvoiceUfServiceHome soHome = null;
	
	private static void lookupServiceHome() throws FDResourceException {
		if(soHome != null){
			return;
		}
		Context ctx = null;
		try {
			ctx = FDStoreProperties.getInitialContext();
			soHome = (BazaarvoiceUfServiceHome) ctx.lookup(BazaarvoiceUfServiceHome.JNDI_HOME);
		} catch (NamingException ne) {
			throw new FDResourceException(ne);
		} finally {
			try {
				if (ctx != null) {
					ctx.close();
				}
			} catch (NamingException ne) {
				LOGGER.warn("Cannot close Context while trying to cleanup", ne);
			}
		}
	}
	
	private static void invalidateHome() {
		soHome = null;
	}
	
	private static BazaarvoiceUfServiceManager sharedInstance;

	protected BazaarvoiceUfServiceManager() {}
	
	public static synchronized BazaarvoiceUfServiceManager getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new BazaarvoiceUfServiceManager();
		}
		return sharedInstance;
	}
	
	public long getLastRefresh() throws FDResourceException{
		lookupServiceHome();
		try {
			BazaarvoiceUfServiceSB sb = soHome.create();			
			return sb.getLastRefresh();
		} catch (CreateException ce) {
			invalidateHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public Map<String,CustomerRatingsDTO> getCustomerRatings() throws FDResourceException{
		lookupServiceHome();
		try {
			BazaarvoiceUfServiceSB sb = soHome.create();			
			return sb.getCustomerRatings();
		} catch (CreateException ce) {
			invalidateHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

}
