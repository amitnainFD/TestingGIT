package com.freshdirect.fdstore.promotion;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.promotion.management.FDPromoDollarDiscount;





public class HeaderDiscountApplicator implements PromotionApplicatorI {

	private final HeaderDiscountRule discountRule;
	private DlvZoneStrategy zoneStrategy;
	private CartStrategy cartStrategy;
	/**
	 * minSubTotal > amount
	 */
	public HeaderDiscountApplicator(HeaderDiscountRule discountRule) {
		this.discountRule = discountRule;
	}

	public boolean apply(String promoCode, PromotionContextI context) {
		//If delivery zone strategy is applicable please evaluate before applying the promotion.
		int e = zoneStrategy != null ? zoneStrategy.evaluate(promoCode, context) : PromotionStrategyI.ALLOW;
		if(e == PromotionStrategyI.DENY) return false;
		
		e = cartStrategy != null ? cartStrategy.evaluate(promoCode, context, true) : PromotionStrategyI.ALLOW;
		if(e == PromotionStrategyI.DENY){
			if(cartStrategy.getCartDcpdSubtotal()>0 && cartStrategy.getTotalDcpdSubtotal() > 0 && cartStrategy.getCartDcpdSubtotal() < cartStrategy.getTotalDcpdSubtotal()){
				//Set the following details for messaging
				//[APPBUG-3605]- To display the correct amount from the promotion, rather than the relevant min amount.
//				double amount = Math.min(context.getShoppingCart().getPreDeductionTotal(), this.discountRule.getMaxAmount());
				double amount = this.discountRule.getMaxAmount();
				if(null !=cartStrategy.getContentKeys() && cartStrategy.getContentKeys().size() > 0){
					cartStrategy.getMinDcpdTotalPromoData().setContentKey((ContentKey)cartStrategy.getContentKeys().toArray()[0]);
				}else{
					cartStrategy.getMinDcpdTotalPromoData().setBrandNames(cartStrategy.getDcpdData().get(EnumDCPDContentType.BRAND));
				}
				cartStrategy.getMinDcpdTotalPromoData().setPromotionCode(promoCode);
				cartStrategy.getMinDcpdTotalPromoData().setDcpdMinTotal(cartStrategy.getTotalDcpdSubtotal());
				cartStrategy.getMinDcpdTotalPromoData().setCartDcpdTotal(cartStrategy.getCartDcpdSubtotal());
				cartStrategy.getMinDcpdTotalPromoData().setHeaderDiscAmount(amount);
//				String message="Spend $"+balanceRequired+" more on promotional products to save $"+amount;
				context.getUser().getPromotionEligibility().getMinDCPDTotalPromos().put(promoCode, cartStrategy.getMinDcpdTotalPromoData());
			}
			return false;
		}
					
		PromotionI promo = PromotionFactory.getInstance().getPromotion(promoCode);
		double subTotal = context.getSubTotal(promo.getExcludeSkusFromSubTotal());
		
		/*APPDEV-1792 - apply the streatchable dollar discount*/
		if(this.discountRule.getDollarList().size() > 0) {
			//check which discount is applicable		
			System.out.println("============Subtotal:" + subTotal);
			double tempTotal = 0;
			double tempDiscount = 0;
			for (Iterator<FDPromoDollarDiscount> i = this.discountRule.getDollarList().iterator(); i.hasNext();) {
				FDPromoDollarDiscount fdpdd = (FDPromoDollarDiscount) i.next();
				if(fdpdd.getOrderSubtotal() < subTotal) {
					if(tempTotal < fdpdd.getOrderSubtotal()) {
						tempTotal = fdpdd.getOrderSubtotal();
						tempDiscount = fdpdd.getDollarOff();
						System.out.println("=========tempTotal:" + tempTotal + "=====tempDiscount:" + tempDiscount);
					}
				}
			}
			if(tempDiscount != 0) {
				System.out.println("=========applying a tempDiscount:" + tempDiscount);
				return context.applyHeaderDiscount(promo, tempDiscount);
			} else {
				return false;
			}
		} 		
				
		if (subTotal < this.discountRule.getMinSubtotal()) {
			return false;
		}
       
		double amount = Math.min(context.getShoppingCart().getPreDeductionTotal(), this.discountRule.getMaxAmount());
		if(promo.getOfferType() != null && promo.getOfferType().equals(EnumOfferType.WINDOW_STEERING)){
			return context.applyZoneDiscount(promo, amount);
		}
		return context.applyHeaderDiscount(promo, amount);
	}

	public HeaderDiscountRule getDiscountRule() {
		return this.discountRule;
	}

	public void setZoneStrategy(DlvZoneStrategy zoneStrategy) {
		this.zoneStrategy = zoneStrategy;
	}


	public DlvZoneStrategy getDlvZoneStrategy() {
		return this.zoneStrategy;
	}
	
	public CartStrategy getCartStrategy() {
		return cartStrategy;
	}

	public void setCartStrategy(CartStrategy cartStrategy) {
		this.cartStrategy = cartStrategy;
	}

	public String toString() {
		return "HeaderDiscountApplicator[" + this.discountRule + "]";
	}
}
