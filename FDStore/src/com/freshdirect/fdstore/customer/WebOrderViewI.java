/*
 * Created on May 3, 2003
 */
package com.freshdirect.fdstore.customer;

import java.util.List;

import com.freshdirect.affiliate.ErpAffiliate;

/**
 * @author knadeem
 */
public interface WebOrderViewI {
	
	public ErpAffiliate getAffiliate();
	
	public List<FDCartLineI> getOrderLines();
	public List<List<FDCartLineI>> getNewOrderLinesSeparated();
	public List<FDCartLineI> getSampleLines();

	public boolean isEstimatedPrice();
	
	public boolean isDisplayDepartment();
	public double getTax();
	public double getDepositValue();
	public double getSubtotal();
	public String getDescription();

	public double getETip();

}
