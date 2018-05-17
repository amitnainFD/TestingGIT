package com.freshdirect.fdstore.standingorders;

import java.io.Serializable;

public class InventoryMapInfoBean implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7365126033521930887L;
	private String productName;
	private String skuCode;
	private String materialNum;
	private double qnty = 0.0;
	public String getProductName() {
		return productName;
	}
	public String getSkuCode() {
		return skuCode;
	}
	public String getMaterialNum() {
		return materialNum;
	}
	public double getQnty() {
		return qnty;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public void setSkuCode(String skuCode) {
		this.skuCode = skuCode;
	}
	public void setMaterialNum(String materialNum) {
		this.materialNum = materialNum;
	}
	public void setQnty(double qnty) {
		this.qnty = qnty;
	}

}
