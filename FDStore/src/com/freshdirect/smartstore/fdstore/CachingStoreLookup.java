/**
 * 
 */
package com.freshdirect.smartstore.fdstore;

import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.content.ContentNodeModel;

public abstract class CachingStoreLookup implements StoreLookup {
    StoreLookup cache;
	
	public CachingStoreLookup(StoreLookup cache) {
		this.cache = cache;
	}

	public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
		return cache.getVariable(contentNode, pricingContext);
	}

	public void reloadCache() {
		cache.reloadCache();
	}
}