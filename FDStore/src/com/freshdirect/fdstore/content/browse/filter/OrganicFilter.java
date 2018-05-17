package com.freshdirect.fdstore.content.browse.filter;

import com.freshdirect.content.nutrition.ErpNutritionInfoType;
import com.freshdirect.fdstore.content.FilterCacheStrategy;
import com.freshdirect.fdstore.content.ProductFilterModel;

public class OrganicFilter extends NutritionInfoFilter {

	public OrganicFilter(ProductFilterModel model, String parentId) {
		super(model, parentId);
	}

	public OrganicFilter(String id, String parentId, String claimCode, String name) { //'virtual' organicFilter for search page 
		super(id, parentId, claimCode, name);
	}

	@Override
	protected ErpNutritionInfoType getType() {
		return ErpNutritionInfoType.ORGANIC;
	}

	@Override
	public FilterCacheStrategy getCacheStrategy() {
		return FilterCacheStrategy.ERPS;
	}

}
