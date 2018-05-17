package com.freshdirect.fdstore.content.browse.filter;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.FilterCacheStrategy;
import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.fdstore.content.ProductFilterModel;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FreshnessFilter extends AbstractRangeFilter {
	private static final Logger LOGGER = LoggerFactory.getInstance( FreshnessFilter.class ); 

	public FreshnessFilter(ProductFilterModel model, String parentId) {
		super(model, parentId);
	}

	@Override
	public boolean apply(FilteringProductItem ctx) throws FDResourceException {
		if (ctx == null || ctx.getProductModel() == null) {
			return false;
		}
		String plantID=ContentFactory.getInstance().getCurrentUserContext().getFulfillmentContext().getPlantId();
		// Perishable product - freshness warranty
		FDProductInfo productInfo = ctx.getFdProductInfo();
		if (productInfo.getFreshness(plantID) != null) {
			// method above returns either a positive integer encoded in string
			// or null
			try {
				final double value = Integer.parseInt(productInfo.getFreshness(plantID));
				return invertChecker(isWithinRange(value));
			} catch (NumberFormatException exc) {
				LOGGER.error("Failed to parse freshness value " + productInfo.getFreshness(plantID), exc);
				return false;
			}
		}

		return invertChecker(false);
	}

	@Override
	public FilterCacheStrategy getCacheStrategy() {
		return FilterCacheStrategy.ERPS;
	}

}
