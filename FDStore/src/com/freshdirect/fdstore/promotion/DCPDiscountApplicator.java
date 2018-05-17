package com.freshdirect.fdstore.promotion;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.ContentNodeModelUtil;
import com.freshdirect.fdstore.customer.adapter.OrderPromotionHelper;

/**
 * Header-level percent off order subtotal.
 */
public class DCPDiscountApplicator  implements PromotionApplicatorI {
	Set contentKeys = new HashSet();
	private final DCPDiscountRule discountRule;
	
	/**
	 * @param percentOff between 0 and 1
	 */
	public DCPDiscountApplicator(DCPDiscountRule rule) {
		super();
		this.discountRule = rule;
	}
	
	public void addContent(String type, String id){
		Object refKey = ContentNodeModelUtil.getAliasCategoryRef(type, id);
		if(FDStoreProperties.isDCPDAliasHandlingEnabled() && refKey != null){
			/*
			 * refKey is not null when content id is pointing to a ALIAS category.
			 * So instead of adding the alias category id add the referencing category
			 * id which is refKey.
			 */
			contentKeys.add(refKey);
		} else {
			//Regular category or department or recipe id or virtual group.
			contentKeys.add(ContentNodeModelUtil.getContentKey(type, id));	
		}
	}
	
	public boolean apply(String promoCode, PromotionContextI context) {
		PromotionI promo = PromotionFactory.getInstance().getPromotion(promoCode);
		if (context.getSubTotal(promo.getExcludeSkusFromSubTotal()) < discountRule.getMinSubtotal()) {
			return false;
		}
		if(this.contentKeys.isEmpty()){
			return false;
		}
		//Find the eligible cartlines for DCPD discount.
		List eligibleCartLines = context.getEligibleLinesForDCPDiscount(promoCode, 
													Collections.unmodifiableSet(this.contentKeys));
		if(eligibleCartLines.isEmpty()){
			// there are no eligible cart lines. do not apply the promo.
			return false;
		}
		double amount = OrderPromotionHelper.getApplicableCategoryDiscount(eligibleCartLines, 
																			discountRule.getPercentOff());
		return context.applyHeaderDiscount(promo, amount);

	}
	
	public DCPDiscountRule getDiscountRule() {
		return this.discountRule;
	}
	
	public void setZoneStrategy(DlvZoneStrategy zoneStrategy) {
		//default implementatio since this applicator is obsolete.
	}
	
	public DlvZoneStrategy getDlvZoneStrategy() {
		return null;
	}

	@Override
	public void setCartStrategy(CartStrategy cartStrategy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CartStrategy getCartStrategy() {
		// TODO Auto-generated method stub
		return null;
	}
}
