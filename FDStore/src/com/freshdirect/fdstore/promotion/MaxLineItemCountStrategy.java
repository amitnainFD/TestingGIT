package com.freshdirect.fdstore.promotion;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.freshdirect.fdstore.customer.FDCartI;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartLineModel;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDUserI;

public class MaxLineItemCountStrategy implements LineItemStrategyI {
	
	private int maxItemCount=999999999;
	
	public int getPrecedence() {
		return 100;
	}

	
	public MaxLineItemCountStrategy(int maxItemCount) { 
		this.maxItemCount=maxItemCount;
	}
	
	@Override
	public int evaluate(FDCartLineI lineItem, String promotionCode, PromotionContextI context) {
		// TODO Auto-generated method stub
		int discountCount = context.getShoppingCart().getLineItemDiscountCount(promotionCode);
		if(discountCount < maxItemCount){
			return ALLOW;
		}
		
		return DENY;
	}
	
	@Override
	public boolean isStoreRequired() {
		return false;
	}
}



