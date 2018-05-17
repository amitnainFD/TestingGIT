package com.freshdirect.smartstore.fdstore;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

import javax.ejb.EJBException;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.customer.ejb.FDServiceLocator;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.ejb.DynamicSiteFeature;
import com.freshdirect.smartstore.ejb.SmartStoreServiceConfigurationSB;

/**
 * Configures SmartStore services.
 * 
 * @author istvan
 * 
 */
public class SmartStoreServiceConfiguration {
	
	// logger instance
	private static Category LOGGER = LoggerFactory.getInstance(SmartStoreServiceConfiguration.class);

	// static instance
	private static SmartStoreServiceConfiguration instance = null;

	// private constructor
	private SmartStoreServiceConfiguration() {
	}

	/**
	 * Get the unique instance.
	 * 
	 * @return the instance or null
	 */
	public static SmartStoreServiceConfiguration getInstance() {
		if (instance == null) {
			instance = new SmartStoreServiceConfiguration();
		}
		return instance;
	}


	public Collection<DynamicSiteFeature> loadDynamicSiteFeatures(String eStoreId) {
		try {
			SmartStoreServiceConfigurationSB sb;
			sb = FDServiceLocator.getInstance().getSmartStoreServiceConfiguration();
			return sb.getSiteFeatures(eStoreId);
		} catch (RemoteException e) {
			LOGGER.warn("SmartStore Service Configuration", e);
			return Collections.emptyList();
		} catch (EJBException e) {
			LOGGER.warn("SmartStore Service Configuration", e);
			return Collections.emptyList();
		} catch (SQLException e) {
			LOGGER.warn("SmartStore Service Configuration", e);
			return Collections.emptyList();
		}
	}
}
