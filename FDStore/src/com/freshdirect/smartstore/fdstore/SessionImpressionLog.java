package com.freshdirect.smartstore.fdstore;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.HiLoGenerator;
import com.freshdirect.fdstore.customer.IDGenerator;
import com.freshdirect.framework.core.ServiceLocator;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.SessionImpressionLogEntry;
import com.freshdirect.smartstore.ejb.SessionImpressionLogHome;
import com.freshdirect.smartstore.ejb.SessionImpressionLogSB;

public class SessionImpressionLog {
	private static SessionImpressionLog sharedInstance = null;
	private ServiceLocator serviceLocator = null;
	
	private static Logger LOGGER = LoggerFactory.getInstance(SessionImpressionLog.class);

	static IDGenerator ID_GENERATOR = new HiLoGenerator ("CUST","PAGE_IMPRESSION_SEQ");  

	
	private SessionImpressionLog() throws NamingException {
		serviceLocator = new ServiceLocator(FDStoreProperties.getInitialContext());
	}

	private SessionImpressionLogHome getSessionImpressionLogHome() {
		try {
			return (SessionImpressionLogHome) serviceLocator.getRemoteHome(
				SessionImpressionLogHome.JNDI_HOME);
		} catch (NamingException e) {
			throw new FDRuntimeException(e);
		}
	}


	/**
	 * Get shared instance.
	 * @return instance
	 */
	synchronized public static SessionImpressionLog getInstance() {
		if (sharedInstance == null) {
			try {
				sharedInstance = new SessionImpressionLog();
			} catch (NamingException e) {
				throw new FDRuntimeException(e,"Could not create session impression log helper shared instance");
			}
		}
		return sharedInstance;
	}

	public void saveLogEntry(SessionImpressionLogEntry entry) {
		try {
			SessionImpressionLogSB bean = this.getSessionImpressionLogHome().create();
			
			bean.saveLogEntry(entry);
		} catch (RemoteException e) {
			LOGGER.warn("Session impression log",e);
		} catch (CreateException e) {
			LOGGER.warn("Session impression log",e);
		}
	}

	public void saveLogEntries(Collection entries) {
		try {
			SessionImpressionLogSB bean = this.getSessionImpressionLogHome().create();
			
			bean.saveLogEntries(entries);
		} catch (RemoteException e) {
			LOGGER.warn("Session impression log",e);
		} catch (CreateException e) {
			LOGGER.warn("Session impression log",e);
		}
	}
	
	public static String getPageId() {
	    return ID_GENERATOR.getNextId();
	}
	
	/**
	 * This is used for testing, to mock out the 
	 * @param generator
	 */
	public static void setIdGenerator(IDGenerator generator) {
	    ID_GENERATOR = generator;
	}
	
}
