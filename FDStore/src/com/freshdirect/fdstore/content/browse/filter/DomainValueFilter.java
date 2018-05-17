package com.freshdirect.fdstore.content.browse.filter;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.AbstractProductItemFilter;
import com.freshdirect.fdstore.content.DomainValue;
import com.freshdirect.fdstore.content.FilterCacheStrategy;
import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.fdstore.content.ProductFilterModel;

public class DomainValueFilter extends AbstractProductItemFilter {
	protected DomainValue domainValue;
	
	public DomainValueFilter(ProductFilterModel model, String parentId) {
		super(model, parentId);

		this.domainValue = model.getDomainValue();
	}

	@Override
	public boolean apply(FilteringProductItem ctx) throws FDResourceException {
		if (ctx == null || ctx.getProductModel() == null) {
			return false;
		}

		return invertChecker(ctx.getProductModel().getAllDomainValues().contains(domainValue));
	}

	@Override
	public FilterCacheStrategy getCacheStrategy() {
		return FilterCacheStrategy.CMS_ONLY;
	}

}
