package com.freshdirect.smartstore.filter;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * Filter out items which are on the cart already.
 * 
 * @author zsombor
 *
 */
public final class ExclusionFilter extends ContentFilter {
    private static final Logger LOGGER = LoggerFactory.getInstance(ExclusionFilter.class);
    
	private final Collection<ContentKey> cartItems;

    public ExclusionFilter(Collection<ContentKey> cartItems) {
        this.cartItems = cartItems;
    }

    public <X extends ContentNodeModel> X filter(X model) {
    	boolean exclude = model == null || cartItems.contains(model.getContentKey());
    	
    	return exclude ? null : model;
    }
}