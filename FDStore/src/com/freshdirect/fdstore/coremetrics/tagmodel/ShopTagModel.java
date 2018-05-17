package com.freshdirect.fdstore.coremetrics.tagmodel;

import java.util.ArrayList;
import java.util.List;


public class ShopTagModel extends AbstractTagModel  {
	
	private static final long	serialVersionUID	= -2386079863255849379L;
	
	private static final String functionName = "cmCreateShopAction5Tag";
	
	//common
	private String productId; 
	private String productName; 
	private String quantity; 
	private String unitPrice; 
	private String categoryId;

	//shop9
	private String registrationId; 
	private String orderId; 
	private String orderSubtotal;

	
	public String getProductId() {
		return productId;
	}
	
	public void setProductId(String productId) {
		this.productId = productId;
	}
	
	public String getProductName() {
		return productName;
	}
	
	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	public String getQuantity() {
		return quantity;
	}
	
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}
	
	public String getUnitPrice() {
		return unitPrice;
	}
	
	public void setUnitPrice(String unitPrice) {
		this.unitPrice = unitPrice;
	}
	
	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	
	//shop9
	public String getRegistrationId() {
		return registrationId;
	}
	
	public void setRegistrationId(String registrationId) {
		this.registrationId = registrationId;
	}
	
	public String getOrderId() {
		return orderId;
	}
	
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public String getOrderSubtotal() {
		return orderSubtotal;
	}
	
	public void setOrderSubtotal(String orderSubtotal) {
		this.orderSubtotal = orderSubtotal;
	} 
	
	@Override
	public String getFunctionName() {
		return functionName;
	}
	
	@Override
	public List<String> toStringList() {		
		List<String> shop5Data = new ArrayList<String>();
		shop5Data.add( getFunctionName() );
		shop5Data.add( getProductId() );		
		shop5Data.add( getProductName() ); 
		shop5Data.add( getQuantity() );
		shop5Data.add( getUnitPrice() ); 
		shop5Data.add( getCategoryId() ); 
		shop5Data.add( mapToAttrString( getAttributesMaps() ) );
		return shop5Data;
	}

}