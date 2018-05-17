package com.freshdirect.fdstore.pricing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;

import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * @version $Revision:18$
 * @author $Author:Robert Gayle$
 */
public class ProductPricingFactory {

	final static Category LOGGER = LoggerFactory.getInstance(ProductPricingFactory.class);

	private static ProductPricingFactory instance = new ProductPricingFactory();

	protected ProductPricingFactory() {
	}
	
	public static ProductPricingFactory getInstance() {
		return instance;
	}

	public ProductModelPricingAdapter getPricingAdapter(ProductModel pm, PricingContext pCtx){
		if (pm == null) {
			return null;
			
		} else if(pm instanceof ProductModelPricingAdapter) {
			return (ProductModelPricingAdapter)pm;
		} else {
			return new ProductModelPricingAdapter(pm);
		}
	}

	public List<ProductModel> getPricingAdapter(List<ProductModel> list, PricingContext pCtx) {
		if (list == null) {
			return null;
		}
		List<ProductModel> res = new ArrayList<ProductModel>(list.size());
		for (ProductModel p : list) {
			res.add(getPricingAdapter(p,pCtx));
		}
		return res;
	}


}
