package com.freshdirect.fdstore.promotion;

public interface StoreRequiredI {
	/**
	 * This flag indicates that strategy implementation requires access to CMS in order to process cart line items.
	 * 
	 * @return
	 */
	public boolean isStoreRequired();
}
