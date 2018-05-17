package com.freshdirect.fdstore.content.browse.sorter;

import java.util.Comparator;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.EnumSustainabilityRating;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.framework.util.log.LoggerFactory;

public class SustainabilityRatingComparator implements Comparator<FilteringProductItem> {
	
	private static final Logger LOGGER = LoggerFactory.getInstance(SustainabilityRatingComparator.class); 
			
	@Override
	public int compare(FilteringProductItem o1, FilteringProductItem o2) {

		/*EnumSustainabilityRating oli1=null;
		try {
			
			
			oli1 = o1.getFdProductInfo().getSustainabilityRating();
		} catch (FDResourceException e) {
        	LOGGER.error(e);
		}
		
		EnumSustainabilityRating oli2=null;
		try {
			oli2 = o2.getFdProductInfo().getSustainabilityRating();
		} catch (FDResourceException e) {
        	LOGGER.error(e);
		}
		
		
		if(oli1==null && oli2==null) return 0;
		
		if(oli1!=null && oli2==null) return -1; //default is desc
		
		if(oli1==null && oli2!=null) return 1; //default is desc
		
		if (oli1.getId()>oli2.getId()) {
			return -1; //default is desc
		} 
		if (oli1.getId()<oli2.getId()) {
			return 1; //default is desc
		}*/ //::FDX::
		return 0;
	}

}
