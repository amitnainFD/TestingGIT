package com.freshdirect.fdstore.lists;

import com.freshdirect.fdstore.customer.ejb.EnumCustomerListType;

public class FDCustomerCreatedList extends FDCustomerProductList {
	
	private static final long serialVersionUID = -584907125438726272L;
	
	/** Maximum allowed length in characters for a list name. */
	public static final int MAX_NAME_LENGTH = 35;
	
	public FDCustomerCreatedList() {
	}
	
	/**
	 *  Return the type of list this implementation handles.
	 *  
	 *  @return the list type corresponding to customer created lists
	 *  @see EnumCustomerListType#CC_LIST
	 */
	@Override
	public EnumCustomerListType getType() {
		return EnumCustomerListType.CC_LIST; 
	}

}
