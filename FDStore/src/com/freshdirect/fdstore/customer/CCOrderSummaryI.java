package com.freshdirect.fdstore.customer;

import java.util.Date;

import com.freshdirect.common.pricing.PricingException;
import com.freshdirect.customer.EnumSaleType;

/**
 * @author knadeem
 */

public interface CCOrderSummaryI {
	
	public String getOrderId();
	public double getOrderTotal() throws PricingException;
	public String getOrderStatus();
	public Date getDeliveryDate();
	public Date getCreateDate();
	public String getCreatedBy();
	public String getCreateSource();
	public Date getLastModifiedDate();
	public String getLastModifiedBy();
	public String getLastModifiedSource();
	public String creditIssued();
	public EnumSaleType getOrderType();
}
