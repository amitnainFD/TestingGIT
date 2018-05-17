package com.freshdirect.smartstore.filter;

import com.freshdirect.fdstore.content.ContentNodeModel;


/**
 * Filtering predicate for product models
 * 
 * @author zsombor
 */
public abstract class ContentFilter {
	protected ContentFilter() {
	}

    public abstract <X extends ContentNodeModel> X filter(X key);
    
    public void reset() {
    }
}