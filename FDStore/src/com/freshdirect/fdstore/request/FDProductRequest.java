package com.freshdirect.fdstore.request;

import java.io.Serializable;

public class FDProductRequest implements Serializable {
	
	private String id;
	private String customerId;
	private String dept;
	private String category;
	private String subCategory;
	private String productName;
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		if(isValued(category))
			this.category = category;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		if(isValued(customerId))
			this.customerId = customerId;
	}
	public String getDept() {
		return dept;
	}
	public void setDept(String dept) {
		if(isValued(dept))
			this.dept = dept;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		if(isValued(id))
			this.id = id;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		
		if(isValued(productName))
			this.productName = productName;
	}
	public String getSubCategory() {
		return subCategory;
	}
	public void setSubCategory(String subCategory) {
		if(isValued(subCategory))
			this.subCategory = subCategory;
	}
	
    private boolean isValued(String input) {
    	if(input==null || "".equals(input.trim())) {
    		return false;
    	}
    	else {
    		return true;
    	}
    }
}
