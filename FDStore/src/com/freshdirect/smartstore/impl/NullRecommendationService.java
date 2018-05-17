package com.freshdirect.smartstore.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;

/**
 * A service that always returns the empty list.

 *
 */
public class NullRecommendationService implements RecommendationService {
	private Variant variant;
	
	public Variant getVariant() {
		return variant;
	}
	
	public NullRecommendationService(Variant variant) {
		this.variant = variant;
	}

	public List<ContentNodeModel> recommendNodes(SessionInput input) {
		return Collections.emptyList();
	}

	public String toString() {
            return getClass().getSimpleName();
	}
	
	public String getDescription() {
		return "";
	}

	public Map getConfiguration() {
		return Collections.emptyMap();
	}

    public boolean isIncludeCartItems() {
    	return false;
    }
    
    public boolean isSmartSavings() {
    	return false;
    }

    public boolean isRefreshable() {
    	return false;
    }
}
