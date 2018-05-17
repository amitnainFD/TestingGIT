package com.freshdirect.smartstore;

import java.util.List;

import com.freshdirect.fdstore.content.ContentNodeModel;

public abstract class WrapperRecommendationService implements RecommendationService {

    protected RecommendationService internal;

    public WrapperRecommendationService(RecommendationService internal) {
        this.internal = internal;
    }
    
    public Variant getVariant() {
        return internal.getVariant();
    }
    
    public void setInternalRecommendationService(RecommendationService internal) {
        this.internal = internal;
    }

    public abstract List<ContentNodeModel> recommendNodes(SessionInput input);

    public boolean isIncludeCartItems() {
    	return internal.isIncludeCartItems();
    }
    
    public boolean isSmartSavings() {
    	return internal.isSmartSavings();
    }

    public boolean isRefreshable() {
    	return internal.isRefreshable();
    }

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[internalRecommender=" + internal + "]";
	}
}
