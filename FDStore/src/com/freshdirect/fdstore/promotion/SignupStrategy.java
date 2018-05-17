package com.freshdirect.fdstore.promotion;

public class SignupStrategy implements PromotionStrategyI {

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {

		if (EnumOrderType.PICKUP.equals(context.getOrderType())) {
			return DENY;
		}

		if (context.isFraudulent()) {
			return DENY;
		}

		String subPromo = context.getSubscribedSignupPromotionCode();
		if (subPromo == null) {
			// user didn't use any signup promos yet
			return ALLOW;
		}

		// if subscribed to this, stick to it no matter what, deny if subscribed to anything else
		return promotionCode.equals(subPromo) ? FORCE : DENY;
	}

	@Override
	public int getPrecedence() {
		return 50;
	}

	public String toString() {
		return "SignupStrategy[]";
	}

	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
