/*
 * Created on Aug 11, 2005
 *
 */
package com.freshdirect.fdstore.promotion.management;

import java.io.Serializable;
import java.util.Date;

/**
 * @author jng
 */
public class FDPromoCustomer implements Serializable {

	private String promotionId;
	private String customerId;
	private int usageCount;
	private String usageCountStr;
	private Date expirationDate;
	private String expirationDay;
	private String expirationMonth;
	private String expirationYear;
	
	public String getPromotionId() { return this.promotionId; }
	public void setPromotionId(String promotionId) { this.promotionId = promotionId; } 

	public String getCustomerId() { return this.customerId; }
	public void setCustomerId(String customerId) { this.customerId = customerId; } 

	public int getUsageCount() { return this.usageCount; }
	public void setUsageCount(int usageCount) { this.usageCount = usageCount; } 

	public String getUsageCountStr() { return this.usageCountStr; }
	public void setUsageCountStr(String usageCountStr) { this.usageCountStr = usageCountStr; } 

	public Date getExpirationDate() { return this.expirationDate; }
	public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; } 

	public String getExpirationDay() { return this.expirationDay; }
	public void setExpirationDay(String expirationDay) { this.expirationDay = expirationDay; } 

	public String getExpirationMonth() { return this.expirationMonth; }
	public void setExpirationMonth(String expirationMonth) { this.expirationMonth = expirationMonth; } 

	public String getExpirationYear() { return this.expirationYear; }
	public void setExpirationYear(String expirationYear) { this.expirationYear = expirationYear; } 
}
