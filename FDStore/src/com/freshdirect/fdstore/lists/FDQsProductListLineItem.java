package com.freshdirect.fdstore.lists;

import java.util.Date;

import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDConfiguration;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSku;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.FDInvalidConfigurationException;
import com.freshdirect.fdstore.customer.FDProductSelection;
import com.freshdirect.fdstore.customer.FDProductSelectionI;
import com.freshdirect.fdstore.customer.OrderLineUtil;

public class FDQsProductListLineItem extends FDCustomerProductListLineItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5112523583498868015L;
	
	private Date deliveryStartDate;
	private String orderId;
	private String orderLineId;
	private EnumSaleStatus saleStatus;

	public FDQsProductListLineItem(String skuCode, FDConfiguration configuration, String recipeSourceId) {
		super(skuCode, configuration, recipeSourceId);
	}
	
	public FDProductSelectionI convertToSelection() throws IllegalStateException, FDSkuNotFoundException, FDResourceException{
		FDProductInfo prodInfo = FDCachedFactory.getProductInfo(this.getSkuCode());
		ProductModel prod = getProduct();
		
		FDProductSelection r = new  FDProductSelection(new FDSku(prodInfo.getSkuCode(), prodInfo.getVersion()),
									  prod,
									  getConfiguration(), getUserContext());
		
		r.setCustomerListLineId(this.getPK() == null ? null : this.getPK().getId());
		r.setDeliveryStartDate(deliveryStartDate);
		r.setOrderId(orderId);
		r.setOrderLineId(orderLineId);
		r.setSaleStatus(saleStatus);
		
		try{
			OrderLineUtil.cleanup(r);
			r.setStatistics(this);
			r.setRecipeSourceId(getRecipeSourceId());
			OrderLineUtil.describe(r);
		} catch (FDInvalidConfigurationException e) {
			r.setInvalidConfig(true);
			r.setStatistics(this);
			r.setDescription(prod.getFullName());
			r.setDepartmentDesc(prod.getDepartment().getFullName());
			r.setConfigurationDesc("");
		}	

		return r;
	}

	public Date getDeliveryStartDate() {
		return deliveryStartDate;
	}

	public void setDeliveryStartDate(Date deliveryStartDate) {
		this.deliveryStartDate = deliveryStartDate;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public EnumSaleStatus getSaleStatus() {
		return saleStatus;
	}

	public void setSaleStatus(EnumSaleStatus saleStatus) {
		this.saleStatus = saleStatus;
	}

	public String getOrderLineId() {
		return orderLineId;
	}

	public void setOrderLineId(String orderLineId) {
		this.orderLineId = orderLineId;
	}

}
