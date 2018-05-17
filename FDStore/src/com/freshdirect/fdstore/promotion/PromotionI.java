package com.freshdirect.fdstore.promotion;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface PromotionI extends Serializable {

	public String getPromotionCode();

	public boolean evaluate(PromotionContextI context);
	
	public boolean apply(PromotionContextI context);
	
	public String getDescription();

	public Date getExpirationDate();

	/** @return List of HeaderDiscountRule */
	public List<? extends HeaderDiscountRule> getHeaderDiscountRules();

	/** @return total amount of header discount rules */
	public double getHeaderDiscountTotal();
	
	public boolean isSampleItem();
	
	public double getMinSubtotal();
	
	public boolean isProductSampleItem();
	
	public List getApplicatorList();
	
	public EnumPromotionType getPromotionType();
	
	public Timestamp getModifyDate();
	//The following methods were added as part of Category Discount Implementation.
	public boolean isRedemption();
	
	public boolean isWaiveCharge();
	
	public boolean isHeaderDiscount();
	
	public boolean isSignupDiscount();
	
	public boolean isLineItemDiscount();
	
	public boolean isFavoritesOnly();
	
	public boolean isCombineOffer();
	
	public double getLineItemDiscountPercentOff();
	
	public boolean isFraudCheckRequired();
	
	public Set<String> getExcludeSkusFromSubTotal();
	
	public EnumOfferType getOfferType();
	
	public boolean isExtendDeliveryPass();
	
	public int getPriority();
	
	public boolean isDollarValueDiscount();
	
	public double getLineItemDiscountPercentage();
	
	public String getRedemptionCode();
}