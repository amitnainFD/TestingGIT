package com.freshdirect.fdstore.promotion;

import com.freshdirect.customer.EnumChargeType;

/**
 * Waives charge of the specified type.
 */
public class WaiveChargeApplicator implements PromotionApplicatorI {

	private final EnumChargeType chargeType;
	private final double minSubtotal;
	private DlvZoneStrategy zoneStrategy;
	private boolean fuelSurcharge;
	private CartStrategy cartStrategy;
	
	public double getMinSubtotal() {
		return minSubtotal;
	}
	public EnumChargeType getChargeType() {
		return chargeType;
	}

	public WaiveChargeApplicator(double minSubtotal, EnumChargeType chargeType, boolean fuelSurcharge) {
		if (chargeType == null) {
			throw new IllegalArgumentException("ChargeType cannot be null");
		}
		this.chargeType = chargeType;
		this.minSubtotal = minSubtotal;
		this.fuelSurcharge = fuelSurcharge;
	}

	public boolean apply(String promotionCode, PromotionContextI context) {
		//If delivery zone strategy is applicable please evaluate before applying the promotion.
		int e = zoneStrategy != null ? zoneStrategy.evaluate(promotionCode, context) : PromotionStrategyI.ALLOW;
		if(e == PromotionStrategyI.DENY) return false;
		
		e = cartStrategy != null ? cartStrategy.evaluate(promotionCode, context, true) : PromotionStrategyI.ALLOW;
		if(e == PromotionStrategyI.DENY) return false;
		
		PromotionI promo = PromotionFactory.getInstance().getPromotion(promotionCode);
		if (context.getSubTotal(promo.getExcludeSkusFromSubTotal()) < this.getMinSubtotal()) {
			return false;
		}
		if(fuelSurcharge) {
			context.getShoppingCart().setChargeWaived(chargeType, true, promotionCode);
		} else {
			context.getShoppingCart().setChargeWaived(chargeType, true, promotionCode, fuelSurcharge);
		}
		if(chargeType == EnumChargeType.DELIVERY){
			//If it is a Delivery Promotion
			context.getShoppingCart().setDlvPromotionApplied(true);
			context.getShoppingCart().setDlvPassApplied(false);
		}
		return true;
	}

	public void setZoneStrategy(DlvZoneStrategy zoneStrategy) {
		this.zoneStrategy = zoneStrategy;
	}

	public DlvZoneStrategy getDlvZoneStrategy() {
		return this.zoneStrategy;
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
