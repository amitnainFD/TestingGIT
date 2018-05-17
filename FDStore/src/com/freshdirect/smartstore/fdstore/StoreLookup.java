package com.freshdirect.smartstore.fdstore;

import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.content.ContentNodeModel;

/**
 * Derive scores from store front.
 * 
 * @author istvan
 *
 */
public interface StoreLookup {

	/**
	 * Get factor value.
	 * @param contentNode
	 * @return factor value
	 */
	public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext);
	
	public void reloadCache();
	
}
