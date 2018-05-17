package com.freshdirect.fdstore.promotion;

public class FraudStrategy implements PromotionStrategyI {

	@Override
	public int getPrecedence() {
		return 110;
	}

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		if(context.hasProfileAttribute("signup_promo_eligible", "deny")){
			return DENY;
		}
		
		return ALLOW;
	}
	
	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
