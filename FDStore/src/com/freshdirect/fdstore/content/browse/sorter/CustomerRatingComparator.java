package com.freshdirect.fdstore.content.browse.sorter;

import java.math.BigDecimal;

import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.fdstore.content.customerrating.CustomerRatingsContext;
import com.freshdirect.fdstore.content.customerrating.CustomerRatingsDTO;

public class CustomerRatingComparator extends OptionalObjectComparator<FilteringProductItem, Double> {

	private CustomerRatingsContext context = CustomerRatingsContext.getInstance();
		
	/**
	 * Extreme value, below minimum (1)
	 */
	private static final double MIN_RATING = 0;

	@Override
	protected Double getValue(FilteringProductItem obj) {
		CustomerRatingsDTO cr = context.getCustomerRatingByProductId( obj.getProductModel().getContentName() );
		if (cr != null){

			BigDecimal value = cr.getAverageOverallRating();
			if(value!=null){
				return  -1 * value.doubleValue(); //default is descending
			}
		}
		return MIN_RATING;
	}
}
