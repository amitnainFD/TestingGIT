package com.freshdirect.fdstore.promotion;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.Discount;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDUserI;

public interface PromotionContextI {
		
	public boolean isFraudulent();

	public boolean isAddressMismatch();

	public double getSubTotal(Set<String> excludeSkus);
	
	public double getPreDeductionTotal(Set<String> excludeSkus);

	public double getApplicableSignupAmount(double amount, double maxAmountPerSku);

	public PromotionI getRedeemedPromotion();

	public FDCartModel getShoppingCart();

	public FDUserI getUser();

	/** @return ID of currently modified sale, or null */
	public String getModifiedSaleId();

	//
	// order history
	//

	public String getSubscribedSignupPromotionCode();

	public int getAdjustedValidOrderCount();

	public int getPromotionUsageCount(String promotionCode);

	//
	// zone/delivery information
	//

	public EnumOrderType getOrderType();

	public String getZipCode();

	public String getDepotCode();

	//
	// promo application
	//

	public void addSampleLine(FDCartLineI cartLine);

	public void setPromotionAddressMismatch(boolean b);

	public void setSignupDiscountRule(SignupDiscountRule discountRule);

	//
	// Customer/ customer profile
	//
	public boolean hasProfileAttribute(String attributeName, String desiredValue);

	public FDIdentity getIdentity();
	
	public void setRulePromoCode(List<String> rulePromoCodes);
	
	public boolean hasRulePromoCode (String promoCode);

	public void addDiscount(Discount discount);
	
	public Date getCurrentDate();
	
	public AssignedCustomerParam getAssignedCustomerParam(String promoId); 
	
	public List<FDCartLineI> getEligibleLinesForDCPDiscount(String promoId, Set<ContentKey> contentKeys);
	
	public boolean applyHeaderDiscount(PromotionI promo, double promotionAmt);
	
	public Discount getHeaderDiscount();
	
	public boolean isPostPromoConflictEnabled();
	
	public void clearLineItemDiscounts();
	
	public double getTotalLineItemDiscount();
	
	public void clearHeaderDiscounts();
	
	public UserContext getUserContext();
	
	public int getSettledECheckOrderCount();
	
	public String getDeliveryZone();
	
	public FDReservation getDeliveryReservation() ;
	
	public boolean applyLineItemDiscount(PromotionI promo, FDCartLineI lineItem, double percentOff, int skuLimit, double maxPercentageDiscount);
	
	public PromotionI getNonCombinableHeaderPromotion();
	
	public boolean applyZoneDiscount(PromotionI promo, double promotionAmt);
	
	public EnumOrderType getOrderType(AddressModel address);
	
	public String getDepotCode(AddressModel addr);
	
	public Set<String> getLineItemDiscountCodes();
	
	public boolean isAlreadyRedeemedPromotion(String promoCode);
	
	public String getUsedWSPromotionCode();
	
	public boolean applyLineItemDollarOffDiscount(PromotionI promo, FDCartLineI lineItem, double promotionAmt, int skuLimit) ;
}
