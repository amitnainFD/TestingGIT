package com.freshdirect.fdstore.services.tax;

import com.freshdirect.fdstore.customer.FDCartI;

public class AvalaraContext {
	
	private FDCartI cart;
	private boolean commit = false;
	private String docCode;
	private Double returnTaxValue;

	public AvalaraContext(FDCartI cart){
		this.cart = cart;
	}

	public FDCartI getCart() {
		return cart;
	}

	public void setCart(FDCartI cart) {
		this.cart = cart;
	}

	public boolean isCommit() {
		return commit;
	}

	public void setCommit(boolean commit) {
		this.commit = commit;
	}

	public String getDocCode() {
		return docCode;
	}

	public void setDocCode(String docCode) {
		this.docCode = docCode;
	}

	public Double getReturnTaxValue() {
		return returnTaxValue;
	}

	public void setReturnTaxValue(Double returnTaxValue) {
		this.returnTaxValue = returnTaxValue;
	}
}
