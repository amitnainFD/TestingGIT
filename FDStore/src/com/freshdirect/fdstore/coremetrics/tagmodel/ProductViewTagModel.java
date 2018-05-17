package com.freshdirect.fdstore.coremetrics.tagmodel;

import java.util.List;


public class ProductViewTagModel extends AbstractTagModel  {

	private static final long	serialVersionUID	= 2608741714902410603L;
	
	private String productId; 
	private String productName; 
	private String categoryId;
	private String virtualCategoryId;
	
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
	
	public String getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getVirtualCategoryId() {
		return virtualCategoryId;
	}

	public void setVirtualCategoryId(String virtualCategoryId) {
		this.virtualCategoryId = virtualCategoryId;
	} 
	
	@Override
	public String getFunctionName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> toStringList() {
		throw new UnsupportedOperationException();
	} 

}