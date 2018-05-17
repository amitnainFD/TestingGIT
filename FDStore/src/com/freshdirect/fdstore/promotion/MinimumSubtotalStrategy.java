package com.freshdirect.fdstore.promotion;

public class MinimumSubtotalStrategy implements PromotionStrategyI {

	
	private double minSubtotal;

	public MinimumSubtotalStrategy(double minSubtotal){
		this.minSubtotal = minSubtotal;
	}
	
	@Override
	public int getPrecedence() {
		return 0;
	}

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		PromotionI promo = PromotionFactory.getInstance().getPromotion(promotionCode);
		if (context.getSubTotal(promo.getExcludeSkusFromSubTotal()) < this.minSubtotal) {
			return this.DENY;
		}
		return this.ALLOW;
	}

	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
