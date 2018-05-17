package com.freshdirect.fdstore.temails;

import java.io.Serializable;

public class RHOrderInfo implements Serializable{

	public double getDefaultPrice() {
		return defaultPrice;
	}

	public void setDefaultPrice(double defaultPrice) {
		this.defaultPrice = defaultPrice;
	}

	public String getDefaultPriceUnit() {
		return defaultPriceUnit;
	}

	public void setDefaultPriceUnit(String defaultPriceUnit) {
		this.defaultPriceUnit = defaultPriceUnit;
	}

	public String getProductFullName() {
		return productFullName;
	}

	public void setProductFullName(String productFullName) {
		this.productFullName = productFullName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	private double defaultPrice;
	private String defaultPriceUnit;
	private String productFullName;
	private int quantity;
	
	public RHOrderInfo(){}
	
	
	
	
}
