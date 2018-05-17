package com.freshdirect.fdstore.promotion;

import java.util.Set;

public class OrderTypeStrategy implements PromotionStrategyI {
	private static final long serialVersionUID = -7759595137261818162L;
	private final Set<EnumOrderType> allowedOrderTypes;

	/**
	 * @param allowedOrderTypes Set of EnumOrderType
	 */
	public OrderTypeStrategy(Set<EnumOrderType> allowedOrderTypes) {
		this.allowedOrderTypes = allowedOrderTypes;
	}

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		return allowedOrderTypes.contains(context.getOrderType()) ? ALLOW : DENY;
	}

	@Override
	public int getPrecedence() {
		return 200;
	}

	public String toString() {
		return "OrderTypeStrategy[" + allowedOrderTypes + "]";
	}

	@Override
	public boolean isStoreRequired() {
		return false;
	}
}