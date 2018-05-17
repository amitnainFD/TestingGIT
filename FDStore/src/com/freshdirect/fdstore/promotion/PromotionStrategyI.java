package com.freshdirect.fdstore.promotion;

import java.io.Serializable;

public interface PromotionStrategyI extends StoreRequiredI, Serializable {

	public static final int DENY = 0;
	public static final int ALLOW = 1;
	public static final int FORCE = 2;
	public static final int RESET = -1;
	
	/** Lower gets evaluated first */
	public int getPrecedence();

	/**
	 * @return DENY, ALLOW or FORCE
	 */
	public int evaluate(String promotionCode, PromotionContextI context);
}