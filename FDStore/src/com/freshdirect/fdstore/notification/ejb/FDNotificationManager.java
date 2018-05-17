package com.freshdirect.fdstore.notification.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.notification.FDNotification;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDNotificationManager {
	
	private final static Category LOGGER = LoggerFactory.getInstance(FDNotificationManager.class);

	private static FDNotificationHome notificationHome = null;

	private static void lookupManagerHome() throws FDResourceException {
		if (notificationHome != null) {
			return;
		}
		Context ctx = null;
		try {
			ctx = FDStoreProperties.getInitialContext();
			notificationHome = (FDNotificationHome) ctx.lookup(FDNotificationHome.JNDI_HOME);
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

	private static void invalidateManagerHome() {
		notificationHome = null;
	}


	private static FDNotificationManager sharedInstance;

	protected FDNotificationManager() {}
	
	public static synchronized FDNotificationManager getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new FDNotificationManager();
		}
		return sharedInstance;
	}
	
	public Object loadCostumerNotifications(FDIdentity identity) throws FDResourceException {
		lookupManagerHome();
		try{
			FDNotificationSB sb=notificationHome.create();
			
			return sb.loadCustomerNotifications(identity);
		} catch (RemoteException e) {
			invalidateManagerHome();
			throw new FDResourceException(e);
		} catch (CreateException e) {
			invalidateManagerHome();
			throw new FDResourceException(e);
		}
		
	}
	
	public Object loadAllNotifications() throws FDResourceException {
		lookupManagerHome();
		try{
			FDNotificationSB sb=notificationHome.create();
			
			return sb.loadAllNotifications();
		} catch (RemoteException e) {
			invalidateManagerHome();
			throw new FDResourceException(e);
		} catch (CreateException e) {
			invalidateManagerHome();
			throw new FDResourceException(e);
		}
	}
	
	public void insertNotification(FDActionInfo info, FDNotification notification) throws FDResourceException {
		lookupManagerHome();
		try{
			FDNotificationSB sb=notificationHome.create();
			
			sb.insertNotification(info, notification);
		} catch (RemoteException e) {
			invalidateManagerHome();
			throw new FDResourceException(e);
		} catch (CreateException e) {
			invalidateManagerHome();
			throw new FDResourceException(e);
		}
	}
	
	public void checkNotificationForCustomer(FDActionInfo info,FDIdentity identity, FDNotification notification) throws FDResourceException {
		lookupManagerHome();
		try{
			FDNotificationSB sb=notificationHome.create();
			
			sb.checkNotificationForCustomer(info, identity, notification);
		} catch (RemoteException e) {
			invalidateManagerHome();
			throw new FDResourceException(e);
		} catch (CreateException e) {
			invalidateManagerHome();
			throw new FDResourceException(e);
		}
	}
	
	public void delete(FDActionInfo info, FDNotification notification) throws FDResourceException {
		lookupManagerHome();
		try{
			FDNotificationSB sb=notificationHome.create();
			
			sb.delete(info, notification);
		} catch (RemoteException e) {
			invalidateManagerHome();
			throw new FDResourceException(e);
		} catch (CreateException e) {
			invalidateManagerHome();
			throw new FDResourceException(e);
		}
	}

}
