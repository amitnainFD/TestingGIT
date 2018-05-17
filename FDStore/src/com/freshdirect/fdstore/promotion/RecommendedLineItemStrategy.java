package com.freshdirect.fdstore.promotion;

import com.freshdirect.fdstore.customer.FDCartLineI;

@Deprecated
public class RecommendedLineItemStrategy implements LineItemStrategyI {
	
	public int getPrecedence() {
		return 200;
	}

	public RecommendedLineItemStrategy(){
		
	}
	
	@Override
	public int evaluate(FDCartLineI lineItem, String promotionCode, PromotionContextI context) {
			String savingsId = lineItem.getSavingsId();
			boolean eligibleLine = false;
			if(savingsId != null) {
				PromoVariantModel eligiblePV = (PromoVariantModel) context.getUser().getPromoVariant(savingsId);
				eligibleLine = (eligiblePV != null && eligiblePV.getAssignedPromotion().getPromotionCode().equals(promotionCode));
			} 
			if(eligibleLine){
				String savVariantId = context.getUser().getSavingsVariantId();
				if( savVariantId == null ) return ALLOW;
				boolean smartSavingsFound = context.getUser().isSavingsVariantFound();
				if(smartSavingsFound && savVariantId != null && savVariantId.equals(savingsId)) return ALLOW;
			}
		return DENY;
	}
	
	@Override
	public boolean isStoreRequired() {
		return false;
	}
}



