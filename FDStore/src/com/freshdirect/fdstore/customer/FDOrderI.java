/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c); 2001 FreshDirect, Inc.
 *
 */

package com.freshdirect.fdstore.customer;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.freshdirect.affiliate.ErpAffiliate;
import com.freshdirect.customer.EnumChargeType;
import com.freshdirect.customer.EnumDeliveryType;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.customer.EnumSaleType;
import com.freshdirect.customer.EnumTransactionSource;
import com.freshdirect.customer.ErpComplaintModel;
import com.freshdirect.customer.ErpDeliveryInfoModel;
import com.freshdirect.customer.ErpOrderLineModel;
import com.freshdirect.customer.ErpShippingInfo;
import com.freshdirect.giftcard.ErpGCDlvInformationHolder;
import com.freshdirect.giftcard.ErpGiftCardDlvConfirmModel;
import com.freshdirect.giftcard.ErpRecipentModel;

/**
 * FDOrder interface
 *
 * @version    $Revision$
 * @author     $Author$
 * @stereotype fd-interface
 */
public interface FDOrderI extends FDCartI {

	public String getErpSalesId();

	public EnumSaleStatus getOrderStatus();

	public Date getRequestedDate();

	public EnumTransactionSource getOrderSource();

	public EnumDeliveryType getDeliveryType();
    
	public boolean isPending();
	
	public Date getDatePlaced();

	public EnumTransactionSource getOrderSource(String criteria);
	
	public String getTransactionInitiator();

	public String getTransactionInitiator(String criteria);

	public Date getPricingDate();

	public Date getLastModifiedDate();
	
	/** @return true if this order was modified */
	public boolean isModifiedOrder();
    
    public String getDepotFacility();

	public String getCustomerId();

	public int hasCreditIssued();

	public Collection<ErpComplaintModel> getComplaints();

	public Collection getAppliedCredits();
	
	public Collection getActualAppliedCredits();

	public double getActualCustomerCreditsValue();
	
	public double getActualDiscountValue();

	public List getFailedAuthorizations();
    
    public List getAuthorizations();
    
    public boolean containsAlcohol();
    
	/** @return List of WebOrderViewI */
	public List getInvoicedOrderViews();
	
	public WebOrderViewI getInvoicedOrderView(ErpAffiliate affiliate);
    
    public boolean hasInvoice();
    
	public boolean hasRedelivery();
	
	public boolean hasRefusedDelivery();
	
	public boolean hasSettledReturn();
	
	public Date getRedeliveryStartTime();

	public Date getRedeliveryEndTime();
	
	public ErpShippingInfo getShippingInfo();
    
	public double getRestockingCharges();
	
	public double getFDRestockingCharges();
	
	public double getWBLRestockingCharges();
	
	public double getRestockingCharges(ErpAffiliate affiliate);
	
	public double getInvoicedTotal();

	public double getInvoicedSubTotal();

	public double getInvoicedTaxValue();

	public double getInvoicedDepositValue();
	
	public double getInvoicedDeliveryCharge();
	
	public double getInvoicedDeliverySurcharge();
	
	public List getInvoicedCharges();
	
	public boolean isChargedWaivedForReturn(EnumChargeType type);
	
	public String getDiscountDescription();
	
	public List getShortedItems();
	
	public String getSapOrderId();

	public List<FDCartonInfo> getCartonContents();
	
	public Map<String, Integer> getCartonMetrics();

    public boolean hasChargeInvoice();
	
	public double getChargeInvoiceTotal();
	
	public List getActualDiscounts();
	
	public String getDeliveryPassId();
	
	public boolean isDlvPassApplied();
	
	public boolean containsDeliveryPass();
	
	public boolean hasReturn();
	
	public boolean isDlvPassAppliedOnReturn();
	
	public boolean isChargeWaivedByCSROnReturn(EnumChargeType type);
	
	public double getDeliverySurchargeOnReturn();
	
	public EnumSaleType getOrderType();
	
	public String getRedeemedSampleDescription();

	//Gift cards
	public List getGiftcardPaymentMethods() ;
	
	public List getAppliedGiftCards() ;
	
	public double getAppliedAmount(String certificateNum);
	
	public FDRecipientList getGiftCardRecipients();
	
	public ErpGiftCardDlvConfirmModel getGCDeliveryInfo();
	
	public ErpRecipentModel getGCResendInfoFor(String giftCardId);
	
	public double getTotalAppliedGCAmount();
	
	public double getCCPaymentAmount();
	
	public ErpOrderLineModel getOrderLineByNumber(String orderlineNumber);
	
	public ErpGCDlvInformationHolder getGCDlvInformationHolder(String givexNumber);
	
	public String getStandingOrderId();
	
	public boolean hasSignature();
	
	public double getDeliveryCharge();
	
	public int getLineCnt();
	
	public double getDeliveryChargeOnReturn();
	
	public double getChargeAmountDiscountApplied(EnumChargeType chargeType); 
	public double getChargeAmountDiscAppliedOnReturn(EnumChargeType chargeType); 
	public boolean isChargeTaxable(EnumChargeType chargeType);
	public double getChargeAmount(EnumChargeType type);
	public double getInvoicedDeliveryPremium();
	
	Double getEbtPurchaseAmount();
	public boolean isMakeGood();
	public String getAuthFailDescription();
	public ErpDeliveryInfoModel getDeliveryInfo();
}
