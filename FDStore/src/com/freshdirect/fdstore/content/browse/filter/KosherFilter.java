package com.freshdirect.fdstore.content.browse.filter;

import org.apache.log4j.Logger;

import com.freshdirect.content.nutrition.EnumKosherSymbolValue;
import com.freshdirect.fdstore.FDKosherInfo;
import com.freshdirect.fdstore.FDProduct;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.AbstractProductItemFilter;
import com.freshdirect.fdstore.content.BrandModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.FilterCacheStrategy;
import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.fdstore.content.ProductFilterModel;
import com.freshdirect.framework.util.log.LoggerFactory;

public class KosherFilter extends AbstractProductItemFilter {
	private static final Logger LOGGER = LoggerFactory.getInstance( KosherFilter.class );

	public KosherFilter(ProductFilterModel model, String parentId) {
		super(model, parentId);
	}

	public KosherFilter(String id, String parentId, String name) { //'virtual' kosherFilter for search page 
		super(id, parentId, name);
	}
	
	/**
	 * This value is used when product is not given valid kosher info
	 * 
	 * @see {@link FDProduct#getKosherInfo()}
	 * @see {@link EnumKosherSymbolValue}
	 */
	private static final int NON_KOSHER_PRI = 999;
	
	@Override
	public boolean apply(FilteringProductItem ctx) throws FDResourceException {
		if (ctx == null || ctx.getProductModel() == null) {
			return false;
		}

		try {
			FDProduct fdProd = ctx.getFdProduct();
			String plantID=ContentFactory.getInstance().getCurrentUserContext().getFulfillmentContext().getPlantId();
			FDKosherInfo kInfo = fdProd.getKosherInfo(plantID);
			
			final int kosherPriority = kInfo != null ? kInfo.getPriority() : NON_KOSHER_PRI;

			return invertChecker(EnumKosherSymbolValue.NONE.getPriority() < kosherPriority && kosherPriority < NON_KOSHER_PRI);
		} catch (FDResourceException e) {
			//LOGGER.error("Failed to obtain fdProduct for product " + ctx.getProductModel().getContentName());
			return false;
		}
	}

	@Override
	public FilterCacheStrategy getCacheStrategy() {
		return FilterCacheStrategy.ERPS;
	}

}
