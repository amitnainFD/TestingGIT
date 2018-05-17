package com.freshdirect.fdstore.promotion;

import java.util.Date;

import com.freshdirect.fdstore.FDStoreProperties;

@Deprecated
public class RecommendationStrategy implements PromotionStrategyI {

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		if (FDStoreProperties.isSmartSavingsEnabled()) {
			return ALLOW;
		}

		return DENY;
	}

	@Override
	public int getPrecedence() {
		return 30;
	}

	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
