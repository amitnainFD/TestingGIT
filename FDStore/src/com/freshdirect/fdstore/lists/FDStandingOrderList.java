package com.freshdirect.fdstore.lists;

import com.freshdirect.fdstore.customer.ejb.EnumCustomerListType;

/**
 * Lightweight subclass represents a Standing Order list
 * 
 * @author segabor
 *
 */
public class FDStandingOrderList extends FDCustomerShoppingList {

	private static final long	serialVersionUID	= -3381209158583746794L;

	@Override
	public EnumCustomerListType getType() {
		return EnumCustomerListType.SO;
	}
}
