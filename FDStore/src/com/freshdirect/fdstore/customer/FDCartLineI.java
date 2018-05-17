package com.freshdirect.fdstore.customer;

import java.util.Set;

import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.Discount;
import com.freshdirect.common.pricing.EnumTaxationType;
import com.freshdirect.customer.EnumATCContext;
import com.freshdirect.customer.ErpCouponDiscountLineModel;
import com.freshdirect.customer.ErpInvoiceLineI;
import com.freshdirect.customer.ErpOrderLineModel;
import com.freshdirect.customer.ErpReturnLineI;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionReason;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDGroup;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.ecoupon.EnumCouponStatus;
import com.freshdirect.framework.event.EnumEventSource;


public interface FDCartLineI extends FDProductSelectionI {

	public ErpOrderLineModel buildErpOrderLines(int baseLineNumber) throws FDResourceException, FDInvalidConfigurationException;

	public FDCartLineI createCopy();
		
	public int getErpOrderLineSize();

	public int getRandomId();

	public Discount getDiscount();
	
	public void setTaxCode(String taxCode);
	
	public String getTaxCode();
	
	/**
	 * Get configured price for this orderline (without promotion).
	 *
	 * @return price in USD
	 */
	public double getPrice();

	/**
	 * Get the value of the promotion applied to this orderline.
	 *
	 * @return promotion value in USD
	 */
	public double getPromotionValue();
	
	/**
	 * Get the tax rate as percentage. 
	 *
	 * @return tax rate as percentage (0.0825 means 8.25%).
	 */
	public double getTaxRate();
	public void setTaxRate(double taxRate);

    /**
	 * Get the value of the tax charged on this orderline.
	 *
	 * @return tax value in USD
	 */
    public double getTaxValue();

	public boolean isSample();
    
	public boolean isEstimatedPrice();
	public boolean hasTax();
	public boolean hasScaledPricing();
	
	public boolean hasDepositValue();
	public double getDepositValue();
	public void setDepositValue(double depositRate);
	
	public boolean hasInvoiceLine();
	public ErpInvoiceLineI getInvoiceLine();

	public boolean hasReturnLine();
	public ErpReturnLineI getReturnLine();
	
	
	public String getOrderedQuantity();
	public String getDeliveredQuantity();
	public String getReturnedQuantity();
	public String getUnitsOfMeasure();
	public String getReturnDisplayQuantity();
	public boolean hasRestockingFee();
	
	public Set<EnumDlvRestrictionReason> getApplicableRestrictions();
	
	public String getOrderLineId();
	public void setOrderLineId(String orderLineId);
	public String getOrderLineNumber();
	
	public String getMaterialNumber();
	
	public String getCartlineId();
	
	/**
	 *  Set the source of the event.
	 *  
	 *  @param source the part of the site this event was generated from.
	 */
	public void setSource(EnumEventSource source);
	
	/**
	 *  Get the source of the event.
	 *  
	 *  @return the part of the site this event was generated from.
	 */
	public EnumEventSource getSource();

	/**
	 *  Get the Advance Order flag setting.
	 *  
	 *  @return Advance Order flag setting.
	 */

	public boolean hasAdvanceOrderFlag();


	/**
	 * Returns the Variant ID if product was recommended
	 * 
	 * @return Variant ID
	 */
	public String getVariantId();
	
	/**
	 * Returns the Discount Amount for the line item
	 * 
	 * @return line discount amount.	 */
	
	public double getDiscountAmount();
	
	/**
	 * Returns boolean if this line item has discount applied.
	 * 
	 * @return boolean
	 */
	public boolean isDiscountFlag();
	
	public boolean isDiscountApplied();
	
	public void setDiscountFlag(boolean b);
	
	public void setDiscount(Discount d);
	
	public boolean hasDiscount(String promoCode);
	
	public void setSavingsId(String savingsId);
	
	public String getSavingsId();
	
	public void removeLineItemDiscount();
	
	public double getActualPrice();
	
	//Returns the zone under which this product has been priced.
	public UserContext getUserContext();
	
	public void setCartonNumber(String no);
	public String getCartonNumber();
	public FDGroup getOriginalGroup();
	public boolean isAddedFromSearch();
	public void setAddedFromSearch(boolean addedFromSearch);
	public ErpCouponDiscountLineModel getCouponDiscount();
	public void setCouponDiscount(ErpCouponDiscountLineModel discount);
	public EnumCouponStatus getCouponStatus();
	public void setCouponStatus(EnumCouponStatus couponStatus);
	public void clearCouponDiscount();
	public EnumTaxationType getTaxationType();
	public void setTaxationType(EnumTaxationType taxationType) ;	
	@Deprecated	public void setCouponApplied(boolean applied);
	@Deprecated	public boolean hasCouponApplied();
	public String getCoremetricsPageId();
	public void setCoremetricsPageId(String coremetricsPageId);
	public String getCoremetricsPageContentHierarchy();
	public void setCoremetricsPageContentHierarchy(String coremetricsPageContentHierarchy);
	public void setAddedFrom(EnumATCContext atcContext) ;
	public EnumATCContext getAddedFrom();
	public String getAtcItemId();
	public void setAtcItemId(String atcItemId);
	public String getCoremetricsVirtualCategory();
	public void setCoremetricsVirtualCategory(String coremetricsVirtualCategory);
	public void setEStoreId(EnumEStoreId eStore);
	public EnumEStoreId getEStoreId();
	public void setPlantId(String plantId);
	public String getPlantId();
	public void setErpOrderLineSource(EnumEventSource source);		
	public EnumEventSource getErpOrderLineSource();	

}
