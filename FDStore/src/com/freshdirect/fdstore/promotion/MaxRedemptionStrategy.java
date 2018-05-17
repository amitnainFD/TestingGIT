package com.freshdirect.fdstore.promotion;


public class MaxRedemptionStrategy implements PromotionStrategyI {
	
	private final int maxRedemptions;
	
	public MaxRedemptionStrategy(int maxRedemptions){
		this.maxRedemptions = maxRedemptions;
	}

	@Override
	public int getPrecedence() {
		return 30;
	}

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		if(context.isAlreadyRedeemedPromotion(promotionCode) || context.getUser().getAllAppliedPromos().contains(promotionCode)){
			//allow if modifying that order OR Promotion already applied beginning of the session. Give it to the customer. 
			return ALLOW;
		} else {
			Integer redeemCount = PromotionFactory.getInstance().getRedemptions(promotionCode, null);
			if(redeemCount != null && redeemCount.intValue() < this.maxRedemptions)
				return ALLOW;
		}
		context.getUser().addPromoErrorCode(promotionCode, PromotionErrorType.ERROR_REDEMPTION_EXCEEDED.getErrorCode());
		return DENY;
	}
	
	public int reEvaluate(String promotionCode, PromotionContextI context) {
		if(context.isAlreadyRedeemedPromotion(promotionCode)){
			//allow if modifying that order. 
			return ALLOW;
		} else {
			Integer redeemCount = PromotionFactory.getInstance().getRedemptions(promotionCode, null);
			if(redeemCount != null && redeemCount.intValue() < this.maxRedemptions)
				return ALLOW;
		}
		context.getUser().addPromoErrorCode(promotionCode, PromotionErrorType.ERROR_REDEMPTION_EXCEEDED.getErrorCode());
		return DENY;
	}
	
	public int getMaxRedemptions() {
		return this.maxRedemptions;
	}

	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
