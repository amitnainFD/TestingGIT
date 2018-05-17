package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import com.freshdirect.common.pricing.PricingException;
import com.freshdirect.customer.EnumDeliveryType;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.customer.EnumSaleType;
import com.freshdirect.customer.EnumTransactionSource;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.payment.EnumPaymentMethodType;


public interface FDOrderInfoI extends Serializable {

	public String getErpSalesId();

	public EnumSaleStatus getOrderStatus();

	public Date getRequestedDate();

	public Date getCreateRequestedDate();

	public double getTotal() throws PricingException;

	public EnumTransactionSource getOrderSource();

	public EnumDeliveryType getDeliveryType();
    
    public boolean isPending();

	public Date getCreateDate();
    
	public String getCreatedBy();
	
	public EnumTransactionSource getModificationSource();
    
	public Date getModificationDate();
    
	public String getModifiedBy();
	
	public double getPendingCreditAmount();
    
	public double getApprovedCreditAmount();
    
	@Deprecated
	public EnumSaleStatus getSaleStatus();

	public Date getDeliveryStartTime();
    
	public Date getDeliveryEndTime();
    
	public Date getDeliveryCutoffTime();

	public EnumPaymentMethodType getPaymentMethodType();
	
	public boolean isDlvPassApplied(); 
	
	public String getDlvPassId();
	
	public EnumSaleType getSaleType();
	
	public String getTruckNumber();
	
	public String getStopSequence();
	
	public String getStandingOrderId();
	
	public boolean isSoHolidayMovement();

	public boolean isModifiable();
	
	public boolean isShopFromThisOrder();
	
	public static final Comparator<FDOrderInfoI> COMPARE_BY_CREATE_DATE = new Comparator<FDOrderInfoI>() {
		@Override
		public int compare( FDOrderInfoI o1, FDOrderInfoI o2 ) {
			return o1.getCreateDate().compareTo( o2.getCreateDate() );
		}
	};
	public static final Comparator<FDOrderInfoI> COMPARE_BY_REQUESTED_DATE = new Comparator<FDOrderInfoI>() {
		@Override
		public int compare( FDOrderInfoI o1, FDOrderInfoI o2 ) {
			return o1.getRequestedDate().compareTo( o2.getRequestedDate() );
		}
	};
	
	public boolean isMakeGood();
	
	public boolean isNewOrder();
	
	public EnumEStoreId getEStoreId();
	
	public String getPlantId();
	
	public String getSalesOrg();
	
	public String getDistributionChannel();
}
