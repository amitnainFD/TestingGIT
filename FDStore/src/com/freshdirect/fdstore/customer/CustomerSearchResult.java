/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer;


import com.freshdirect.customer.ErpCustomerModel;
import com.freshdirect.customer.ErpCustomerInfoModel;

/**
 *
 *
 * @version $Revision$
 * @author $Author$
 */
public class CustomerSearchResult implements java.io.Serializable {

	private final ErpCustomerModel erpCustomer;
	private final FDCustomerModel fdCustomer;

	public CustomerSearchResult(ErpCustomerModel ec, FDCustomerModel fc) {
		this.erpCustomer = ec;
		this.fdCustomer = fc;
	}

	public ErpCustomerModel getErpCustomer() {
		return this.erpCustomer;
	}

	public ErpCustomerInfoModel getErpCustomerInfo() {
		return this.erpCustomer.getCustomerInfo();
	}
	public FDCustomerModel getFDCustomer() {
		return this.fdCustomer;
	}

}