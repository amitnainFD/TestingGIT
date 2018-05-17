package com.freshdirect.fdstore.coremetrics;

/**
 * Simple enum that determines the channel context of CM reporting
 * 
 * @author segabor
 *
 */
public enum CmFacade {
	WEB, PHONE, TABLET;

	public boolean isMobile() {
		return this != PHONE && this != TABLET;
	}
}
