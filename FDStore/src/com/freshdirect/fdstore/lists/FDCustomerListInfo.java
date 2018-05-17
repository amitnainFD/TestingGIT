package com.freshdirect.fdstore.lists;

import com.freshdirect.fdstore.customer.ejb.EnumCustomerListType;

public class FDCustomerListInfo extends FDCustomerList {
	private static final long serialVersionUID = 5113882565285643320L;

	private EnumCustomerListType type = EnumCustomerListType.CC_LIST;
	private int count;
	
	public FDCustomerListInfo() {
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	@Override
	public int getCount() {
		return count;
	}

	public void setType(EnumCustomerListType type) {
		this.type = type;
	}
	
	@Override
	public EnumCustomerListType getType() {
		return type;
	}
	
}
