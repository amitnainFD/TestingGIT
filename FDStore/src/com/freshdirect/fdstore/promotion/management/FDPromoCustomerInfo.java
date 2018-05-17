/*
 * Created on Aug 11, 2005
 *
 */
package com.freshdirect.fdstore.promotion.management;

import java.util.Date;

/**
 * @author jng
 */

public class FDPromoCustomerInfo extends FDPromoCustomer {
	
	private String promotionDesc;
	private boolean isMaxUsagePerCust;
	private int rollingExpirationDays;
	private String userId;
	private int numUsed;
	private int promoUsageCount;
	private Date promoExpirationDate;
	private boolean isLoadedFromCustomerId;
	private boolean isLoadedFromPromotionId;
	
	public String getPromotionDesc() { return this.promotionDesc; }
	public void setPromotionDesc(String promotionDesc) { this.promotionDesc = promotionDesc; } 

	public boolean  getIsMaxUsagePerCust() { return this.isMaxUsagePerCust; }
	public void setIsMaxUsagePerCust(boolean isMaxUsagePerCust) { this.isMaxUsagePerCust = isMaxUsagePerCust; }
	
	public int getRollingExpirationDays() { return this.rollingExpirationDays; }
	public void setRollingExpirationDays(int rollingExpirationDays) { this.rollingExpirationDays = rollingExpirationDays; } 

	public String getUserId() { return this.userId; }
	public void setUserId(String userId) { this.userId = userId; } 
	
	public int getNumUsed() { return this.numUsed; }
	public void setNumUsed(int numUsed) { this.numUsed = numUsed; }
	
	public int getPromoUsageCount() { return this.promoUsageCount; }
	public void setPromoUsageCount(int promoUsageCount) { this.promoUsageCount = promoUsageCount; } 

	public Date getPromoExpirationDate() { return this.promoExpirationDate; }
	public void setPromoExpirationDate(Date promoExpirationDate) { this.promoExpirationDate = promoExpirationDate; } 
	
	public boolean getIsLoadedFromCustomerId() { return this.isLoadedFromCustomerId; }
	public void setIsLoadedFromCustomerId(boolean isLoadedFromCustomerId) { this.isLoadedFromCustomerId = isLoadedFromCustomerId; } 

	public boolean getIsLoadedFromPromotionId() { return this.isLoadedFromPromotionId; }
	public void setIsLoadedFromPromotionId(boolean isLoadedFromPromotionId) { this.isLoadedFromPromotionId = isLoadedFromPromotionId; } 
}
