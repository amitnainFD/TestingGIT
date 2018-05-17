package com.freshdirect.fdstore.promotion;

import java.io.Serializable;

public interface PromotionApplicatorI extends Serializable {

	public boolean apply(String promotionCode, PromotionContextI context);
	
	//Sets the Delivery zone strategy if applicable
	public void setZoneStrategy(DlvZoneStrategy zoneStrategy);
	
	public DlvZoneStrategy getDlvZoneStrategy() ;
	
	public void setCartStrategy(CartStrategy cartStrategy);
	
	public CartStrategy getCartStrategy();
}
