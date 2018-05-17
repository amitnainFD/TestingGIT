/*
 * Created on Jul 21, 2005
 *
 */
package com.freshdirect.fdstore.promotion;

import java.io.Serializable;
import java.util.Date;

/**
 * @author jng
 *
 */
public class AssignedCustomerParam implements Serializable {

	private Integer usageCount;
	private Date expirationDate;
	
	public AssignedCustomerParam(Integer usageCount, Date expirationDate) {
		this.usageCount = usageCount;
		this.expirationDate = expirationDate;
	}
	
	public Integer getUsageCount() { return this.usageCount; } 
	public Date getExpirationDate() { return this.expirationDate; } 

	public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; } 
	public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; } 
}
	