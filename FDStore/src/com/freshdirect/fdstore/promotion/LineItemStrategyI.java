package com.freshdirect.fdstore.promotion;

import java.io.Serializable;

import com.freshdirect.fdstore.customer.FDCartLineI;

public interface LineItemStrategyI extends StoreRequiredI, Serializable {

	public static final int DENY = 0;
	public static final int ALLOW = 1;
	public static final int FORCE = 2;
	
	/** Lower gets evaluated first */
	public int getPrecedence();

	/**
	 * @return DENY, ALLOW or FORCE
	 */
	public int evaluate(FDCartLineI lineItem, String promotionCode, PromotionContextI context);
}