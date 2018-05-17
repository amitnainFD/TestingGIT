package com.freshdirect.fdstore.content.browse.filter;

import java.math.BigDecimal;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.FilterCacheStrategy;
import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.fdstore.content.ProductFilterModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.customerrating.CustomerRatingsContext;
import com.freshdirect.fdstore.content.customerrating.CustomerRatingsDTO;

public class CustomerRatingFilter extends AbstractRangeFilter {

	public CustomerRatingFilter(ProductFilterModel model, String parentId) {
		super(model, parentId);
	}

	@Override
	public boolean apply(FilteringProductItem ctx) throws FDResourceException {
		if (ctx == null || ctx.getProductModel() == null) {
			return false;
		}
		
		ProductModel node = ctx.getProductModel();
		CustomerRatingsDTO customerRatingsDTO = CustomerRatingsContext.getInstance().getCustomerRatingByProductId(node.getContentKey().getId());
		if (customerRatingsDTO != null /* && (node.getProductRating() == null || node.getProductRatingEnum().getValue() == 0) */) {
			final BigDecimal value = customerRatingsDTO.getAverageOverallRating();
			return invertChecker(isWithinRange( value == null ? 0 : value.doubleValue() ));
		}

		return invertChecker(false);
	}

	@Override
	public FilterCacheStrategy getCacheStrategy() {
		return FilterCacheStrategy.ERPS; //not actually from erps, but it needs to be refreshed dynamically likewise
	}

}
