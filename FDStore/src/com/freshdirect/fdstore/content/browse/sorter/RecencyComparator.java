package com.freshdirect.fdstore.content.browse.sorter;

import java.util.Date;
import java.util.Map;

import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.fdstore.content.ProductModel;

public class RecencyComparator extends OptionalObjectComparator<FilteringProductItem, Long> {
	
	@Override
	protected Long getValue(FilteringProductItem obj) {
		ProductModel prod = obj.getProductModel();
		String key=getProductNewnessKey(prod);
		Date addedDate =null;
		Map<ProductModel, Map<String,Date>> newProducts=ContentFactory.getInstance().getNewProducts();
		if(newProducts.containsKey(prod)) {
			addedDate= newProducts.get(prod).get(key);
		} 
		
		if (addedDate == null){
			newProducts = ContentFactory.getInstance().getBackInStockProducts();
			if(newProducts.containsKey(prod)) {
				addedDate= newProducts.get(prod).get(key);
			} 
		}
		
		return addedDate == null ? null : -1 * addedDate.getTime(); //default is descending
	}
	
	private String getProductNewnessKey(ProductModel product) {
		String key="";
		ZoneInfo zone=product.getUserContext().getPricingContext().getZoneInfo();
		if(zone!=null) {
			key=new StringBuilder(5).append(zone.getSalesOrg()).append(zone.getDistributionChanel()).toString();
		}
		return key;
	}

}
