package com.freshdirect.fdstore.coremetrics;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.framework.util.log.LoggerFactory;

public class CmContextUtility {
	private static final Logger LOGGER = LoggerFactory.getInstance(CmContextUtility.class);
	
	
	/**
	 * Check if Coremetrics beaconing is available
	 * 
	 * Know cases when it's not
	 * - CRM Masquerade mode
	 * 
	 * @param user Customer instance, required to
	 * 
	 * @return
	 */
	public static boolean isCoremetricsAvailable( final FDUserI user, final boolean overrideCrm ) {
		if (!FDStoreProperties.isCoremetricsEnabled()) {
			LOGGER.info("Coremetrics is turned off");
			return false;
		}

		final CmContext ctx = CmContext.getContext();
		if ( ! ctx.isEnabled() ) {
			LOGGER.info("Coremetrics is disabled on FDX store front");
			return false;
		}

		if ( user != null && user.getMasqueradeContext() != null) {
			if (overrideCrm) {
				LOGGER.info("Coremetrics reporting is (not) available in CRM Masquerade mode, enabled for this case");
			} else {
				LOGGER.info("Coremetrics reporting is not available in CRM Masquerade mode");
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Convenience method
	 * 
	 * @param user
	 * @return
	 */
	public static boolean isCoremetricsAvailable( final FDUserI user ) {
		return isCoremetricsAvailable( user, false);
	}
}
