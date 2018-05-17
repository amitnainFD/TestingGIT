package com.freshdirect.fdstore.promotion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.customer.FDCartLineI;

public class FDMinDCPDTotalPromoData implements Serializable{
	private String promotionCode;
	private double headerDiscAmount;
	private double dcpdMinTotal;
	private double cartDcpdTotal;
	private List<FDCartLineI> dcpdCartLines = new ArrayList<FDCartLineI>();	
	private ContentKey contentKey;
	private Set<String> brandNames;
	public String getPromotionCode() {
		return promotionCode;
	}
	public void setPromotionCode(String promotionCode) {
		this.promotionCode = promotionCode;
	}
	public double getHeaderDiscAmount() {
		return headerDiscAmount;
	}
	public void setHeaderDiscAmount(double headerDiscAmount) {
		this.headerDiscAmount = headerDiscAmount;
	}
	public double getDcpdMinTotal() {
		return dcpdMinTotal;
	}
	public void setDcpdMinTotal(double dcpdMinTotal) {
		this.dcpdMinTotal = dcpdMinTotal;
	}
	public double getCartDcpdTotal() {
		return cartDcpdTotal;
	}
	public void setCartDcpdTotal(double cartDcpdTotal) {
		this.cartDcpdTotal = cartDcpdTotal;
	}
	public List<FDCartLineI> getDcpdCartLines() {
		return dcpdCartLines;
	}
	public void setDcpdCartLines(List<FDCartLineI> dcpdCartLines) {
		this.dcpdCartLines = dcpdCartLines;
	}
	public ContentKey getContentKey() {
		return contentKey;
	}
	public void setContentKey(ContentKey contentKey) {
		this.contentKey = contentKey;
	}
	public Set<String> getBrandNames() {
		return brandNames;
	}
	public void setBrandNames(Set<String> brandNames) {
		this.brandNames = brandNames;
	}
	

}
