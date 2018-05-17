package com.freshdirect.fdstore.content.browse.filter;

import org.apache.log4j.Logger;

import com.freshdirect.content.nutrition.EnumClaimValue;
import com.freshdirect.content.nutrition.NutritionValueEnum;
import com.freshdirect.fdstore.FDProduct;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.AbstractProductItemFilter;
import com.freshdirect.fdstore.content.FilterCacheStrategy;
import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.fdstore.content.ProductFilterModel;
import com.freshdirect.framework.util.log.LoggerFactory;

@Deprecated
public class ClaimBasedFilter extends AbstractProductItemFilter {
	private static final Logger LOGGER = LoggerFactory.getInstance( ClaimBasedFilter.class );

	protected Class<? extends NutritionValueEnum> claimType;
	
	/**
	 * ERPSy Claim Code
	 * @see {@link EnumClaimValue#getCode()}
	 */
	protected String claimCode;
	
	public ClaimBasedFilter(ProductFilterModel model, Class<? extends NutritionValueEnum> claimType, String claimCode, String parentId) {
		super(model, parentId);
		
		this.claimType = claimType;
		this.claimCode = claimCode;
	}

	@Override
	public boolean apply(FilteringProductItem ctx) throws FDResourceException {
		if (ctx == null || ctx.getProductModel() == null || claimCode == null) {
			return false;
		}

		try {
			FDProduct fdProd = ctx.getFdProduct();
			for (EnumClaimValue claim : fdProd.getClaims()) {
				if (claimCode.equalsIgnoreCase(claim.getCode())) {
					return invertChecker(true);
				}
			}
		} catch (FDResourceException e) {
			LOGGER.error("Failed to obtain fdProduct for product " + ctx.getProductModel().getContentName());
			return false;
		}

		return invertChecker(false);
	}

	@Override
	public FilterCacheStrategy getCacheStrategy() {
		return FilterCacheStrategy.ERPS;
	}

}
