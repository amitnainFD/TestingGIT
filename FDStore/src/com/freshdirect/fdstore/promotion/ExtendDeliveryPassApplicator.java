package com.freshdirect.fdstore.promotion;

import utils.netAddresses;

import com.freshdirect.common.pricing.Discount;
import com.freshdirect.common.pricing.EnumDiscountType;
import com.freshdirect.deliverypass.EnumDlvPassStatus;
import com.freshdirect.fdstore.customer.ExtendDPDiscountModel;

/**
 * Header-level percent off order subtotal.
 */
public class ExtendDeliveryPassApplicator implements PromotionApplicatorI {

	private final int extendDays;
	private DlvZoneStrategy zoneStrategy;
	private final double minSubtotal;
	private CartStrategy cartStrategy;

	/**
	 * @param percentOff between 0 and 1
	 */
	public ExtendDeliveryPassApplicator(int extendDays, double minSubtotal) {
		this.extendDays = extendDays;
		this.minSubtotal = minSubtotal;
	}


	public boolean apply(String promoCode, PromotionContextI context) {
		//If delivery zone strategy is applicable please evaluate before applying the promotion.
		int e = zoneStrategy != null ? zoneStrategy.evaluate(promoCode, context) : PromotionStrategyI.ALLOW;
		if(e == PromotionStrategyI.DENY) return false;
		
		e = cartStrategy != null ? cartStrategy.evaluate(promoCode, context, true) : PromotionStrategyI.ALLOW;
		if(e == PromotionStrategyI.DENY) return false;
		
		PromotionI promo = PromotionFactory.getInstance().getPromotion(promoCode);
		if (context.getSubTotal(promo.getExcludeSkusFromSubTotal()) < this.minSubtotal) {
			return false;
		}
		
		if(context.getUser().getDeliveryPassStatus().equals(EnumDlvPassStatus.ACTIVE)){
			//Only if user has active delivery pass.
			context.getShoppingCart().setDlvPassExtn(new ExtendDPDiscountModel(promoCode, extendDays));
			return true;
		}
		return false;
	}


	public int getExtendDays() {
		return extendDays;
	}
	public void setZoneStrategy(DlvZoneStrategy zoneStrategy) {
		this.zoneStrategy = zoneStrategy;
	}

	public DlvZoneStrategy getDlvZoneStrategy() {
		return this.zoneStrategy;
	}
	
	public double getMinSubtotal() {
		return this.minSubtotal;
	}


	@Override
	public void setCartStrategy(CartStrategy cartStrategy) {
		this.cartStrategy = cartStrategy;		
	}


	@Override
	public CartStrategy getCartStrategy() {
		return this.cartStrategy;
	}
}
