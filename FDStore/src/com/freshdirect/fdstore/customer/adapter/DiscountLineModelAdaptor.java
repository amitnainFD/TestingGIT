package com.freshdirect.fdstore.customer.adapter;

import com.freshdirect.common.pricing.Discount;
import com.freshdirect.customer.ErpDiscountLineModel;
import com.freshdirect.fdstore.promotion.EnumPromotionType;
import com.freshdirect.fdstore.promotion.PromotionFactory;
import com.freshdirect.fdstore.promotion.PromotionI;

public class DiscountLineModelAdaptor   
{
	ErpDiscountLineModel model;
	public DiscountLineModelAdaptor(ErpDiscountLineModel model)
	{
		this.model=model;
	}
	public String getDescription()
	{
		String desc=null;
		Discount discount = getModel().getDiscount();
		String code = discount.getPromotionCode();
		PromotionI promotion = PromotionFactory.getInstance().getPromotion(code);
		if (promotion != null) {
			if (EnumPromotionType.SIGNUP.equals(promotion.getPromotionType())) {
				desc = "FREE FOOD";
				
			} else {
				desc = promotion.getDescription();
			}
		}
	return desc;
	}

	public ErpDiscountLineModel getModel() {
		return model;
	}

	public void setModel(ErpDiscountLineModel model) {
		this.model = model;
	}	

}
