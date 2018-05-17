package com.freshdirect.smartstore.fdstore;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.ejb.FDServiceLocator;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.framework.util.log.LoggerFactory;

public class VariantSelection {
	private static VariantSelection sharedInstance = null;
	private FDServiceLocator serviceLocator = null;
	
	private static Category LOGGER = LoggerFactory.getInstance(VariantSelection.class);

	private VariantSelection() throws NamingException {
		serviceLocator = new FDServiceLocator(FDStoreProperties.getInitialContext());
	}


	/**
	 * Get shared instance.
	 * @return instance
	 */
	synchronized public static VariantSelection getInstance() {
		if (sharedInstance == null) {
			try {
				sharedInstance = new VariantSelection();
			} catch (NamingException e) {
				throw new FDRuntimeException(e,"Could not create variant selection helper shared instance");
			}
		}
		return sharedInstance;
	}



	/**
	 * Returns the map of variant selection for a given date
	 * @return
	 */
	public Map<String, String> getVariantMap(EnumSiteFeature feature, Date date) {
		try {
			return serviceLocator.getVariantSelectionSessionBean().getVariantMap(feature, date);
		} catch (RemoteException e) {
			LOGGER.warn("Variant selection",e);
			throw new FDRuntimeException(e);
		}
	}

	/**
	 * Returns the map of variant selection
	 * @return
	 */
        public Map<String, String> getVariantMap(EnumSiteFeature feature) {
		try {
			return serviceLocator.getVariantSelectionSessionBean().getVariantMap(feature);
		} catch (RemoteException e) {
			LOGGER.warn("Variant selection",e);
			throw new FDRuntimeException(e);
		}
	}
	
	/**
	 * Returns the set of (cohort ID, weight) couples
	 * @return
	 */
	public Map<String, Integer> getCohorts() {
		try {
			
			return serviceLocator.getVariantSelectionSessionBean().getCohorts();
		} catch (RemoteException e) {
			LOGGER.warn("Variant selection", e);
			throw new FDRuntimeException(e);
		}
	}
	

	public List<String> getCohortNames() {
		try {
                    List<String> names = serviceLocator.getVariantSelectionSessionBean().getCohortNames();
    		    return names;
		} catch (RemoteException e) {
			LOGGER.warn("Variant selection", e);
			throw new FDRuntimeException(e);
		}
	}

	public List<String> getVariants(EnumSiteFeature feature) {
		try {
			return serviceLocator.getVariantSelectionSessionBean().getVariants(feature);
		} catch (RemoteException e) {
			LOGGER.warn("Variant selection",e);
			throw new FDRuntimeException(e);
		}
	}

	/**
	 * Returns a list of start dates in cohort-variant assignment (history dates)
	 * @return
	 */
	public List<Date> getStartDates() {
		try {
			return serviceLocator.getVariantSelectionSessionBean().getStartDates();
		} catch (RemoteException e) {
			LOGGER.warn("Variant selection",e);
			throw new FDRuntimeException(e);
		}
	}
}
