package com.freshdirect.fdstore.promotion;

public class ActiveInactiveStrategy implements PromotionStrategyI {
	
	private final boolean active;
	
	public ActiveInactiveStrategy(boolean active){
		this.active = active;
	}

	@Override
	public int getPrecedence() {
		return 30;
	}

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		PromotionI promo = PromotionFactory.getInstance().getPromotion(promotionCode);
		
		if(active || (null != promo.getOfferType() && promo.getOfferType().equals(EnumOfferType.WINDOW_STEERING) && context.isAlreadyRedeemedPromotion(promotionCode))){
			return ALLOW;
		}
		return DENY;
	}
	
	public boolean isActive() {
		return this.active;
	}

	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
