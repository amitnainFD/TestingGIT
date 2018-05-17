/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer;

import java.util.Date;

/**
 * Lightweight information about a Sale.
 *
 * @version $Revision$
 * @author $Author$
 */
public class BulkModifyOrderInfo extends FDCustomerOrderInfo {

	//add bulk modification status
	private String modStatus;
	private String errorDesc;
	private Date createDate;
	

	public String getModStatus() {
		return modStatus;
	}

	public void setModStatus(String modStatus) {
		this.modStatus = modStatus;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

}