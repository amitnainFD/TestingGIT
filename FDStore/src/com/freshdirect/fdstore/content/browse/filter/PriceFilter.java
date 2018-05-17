package com.freshdirect.fdstore.content.browse.filter;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.ZonePriceInfoModel;
import com.freshdirect.fdstore.content.FilterCacheStrategy;
import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.fdstore.content.PriceCalculator;
import com.freshdirect.fdstore.content.ProductFilterModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.pricing.ProductModelPricingAdapter;
import com.freshdirect.fdstore.pricing.ProductPricingFactory;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * Range based product price filter
 * 
 * @author segabor
 *
 */
public class PriceFilter extends AbstractRangeFilter {

	private static final Logger LOGGER = LoggerFactory.getInstance( PriceFilter.class );
	
	private FDUserI user;
	
	public PriceFilter(ProductFilterModel model, String parentId) {
		super(model, parentId);
	}
	
	public PriceFilter(ProductFilterModel model, String parentId, FDUserI user) {
		super(model, parentId);
		this.user = user;
	}

	/**
	 * Value returned upon unsuccessful price determination
	 *
	 * @see {@link PriceCalculator#getDefaultPriceValue()}
	 */
	private static double INVALID_PRICE = 0.0;
	
	@Override
	public boolean apply(FilteringProductItem ctx) throws FDResourceException {
		
		if (ctx == null || ctx.getProductModel() == null) {
			return false;
		}
		
		ProductModel product = ctx.getProductModel();
		
		if(user!=null && !(product instanceof ProductModelPricingAdapter)){
			product = ProductPricingFactory.getInstance().getPricingAdapter( product, user.getPricingContext() );
		}
		
		try {
			PriceCalculator calc = product.getPriceCalculator();
			
			if(calc==null){
				return false;
			}
			
			ZonePriceInfoModel zpi = calc.getZonePriceInfoModel();
			
			if(zpi==null){
				return false;
			}
		
			final double price = zpi.getDefaultPrice();

			return invertChecker(price > INVALID_PRICE ? isWithinRange(price) : false);
		
		} catch (FDSkuNotFoundException e) {
			LOGGER.error("Failed to obtain pricing for product " + ctx.getProductModel().getContentName());
			return false;
		}
	}

	@Override
	public FilterCacheStrategy getCacheStrategy() {
		return FilterCacheStrategy.ERPS_PRICING_ZONE;
	}

}
