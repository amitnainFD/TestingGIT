package com.freshdirect.fdstore.content.browse.sorter;

import java.util.Comparator;

import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.fdstore.content.PopulatorUtil;
import com.freshdirect.fdstore.content.SkuModel;


public class AvailabilityComparator implements Comparator<FilteringProductItem> {
	
	@Override
	public int compare(FilteringProductItem item1, FilteringProductItem item2) {
		SkuModel sku1 = PopulatorUtil.getDefSku(item1.getProductModel());
		SkuModel sku2 = PopulatorUtil.getDefSku(item2.getProductModel());
		
		boolean availabilty1 = sku1==null ? false : !sku1.isUnavailable();
		boolean availabilty2 = sku2==null ? false : !sku2.isUnavailable();

		if (availabilty1 && !availabilty2){
			return -1;
		} else if (!availabilty1 && availabilty2){
			return 1;
		} else {
			return 0;
		}
	}
}
