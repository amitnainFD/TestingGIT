package com.freshdirect.fdstore.promotion;

/**
 * @author knadeem Date Jun 1, 2005
 */
public class RuleBasedPromotionStrategy implements PromotionStrategyI {

	@Override
	public int getPrecedence() {
		return 500;
	}

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		if(context.hasRulePromoCode(promotionCode)) {
			return ALLOW;
		}
		return DENY;
	}

	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
