package com.freshdirect.fdstore.promotion;

import java.util.Date;

import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.framework.util.DateUtil;

public class AudienceStrategy implements PromotionStrategyI {

	private boolean maxUsagePerCust;
	private int rollingExpirationDays;
	private boolean isRollingExpFrom1stOrder;
	
	public AudienceStrategy(boolean maxUsagePerCust, int rollingExpirationDays) {
		this.maxUsagePerCust = maxUsagePerCust;
		this.rollingExpirationDays = rollingExpirationDays;
	}
	
	public AudienceStrategy(boolean maxUsagePerCust, int rollingExpirationDays, boolean isRollingExpFrom1stOrder) {
		this.maxUsagePerCust = maxUsagePerCust;
		this.rollingExpirationDays = rollingExpirationDays;
		this.isRollingExpFrom1stOrder = isRollingExpFrom1stOrder;
	}
	
	public boolean isMaxUsagePerCustomer() { return this.maxUsagePerCust; } 
	public int getRollingExpirationDays() { return this.rollingExpirationDays; } 
	
	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		AssignedCustomerParam param = context.getAssignedCustomerParam(promotionCode);
		if (param != null && isValidUsageCount(promotionCode, context, param) && isBeforeExpirationDate(promotionCode, context, param)) {
					return ALLOW;
		}else if(isRollingExpFrom1stOrder){
			return isBeforeExpirationDate(promotionCode, context)?ALLOW:DENY;
		}
		return DENY;		
	}

	private boolean isValidUsageCount(String promotionCode, PromotionContextI context, AssignedCustomerParam param) {
		int promoUsageCnt = context.getPromotionUsageCount(promotionCode);		
		Integer usageCount = param.getUsageCount();
		return (!this.maxUsagePerCust || (usageCount != null && usageCount.intValue() > promoUsageCnt)); 
	}

	private boolean isBeforeExpirationDate(String promotionCode, PromotionContextI context, AssignedCustomerParam param) {
		Date today = DateUtil.truncate(new Date());
		Date expirationDate = param.getExpirationDate();
		return (this.rollingExpirationDays == 0 || (expirationDate != null && !today.after(DateUtil.truncate(expirationDate))));					
	}
	
	private boolean isBeforeExpirationDate(String promotionCode, PromotionContextI context) {
		boolean isBeforeExpDate = true;
		Date firstOrderDate = null;
		EnumEStoreId eStoreId= context.getUser().getUserContext().getStoreContext().getEStoreId();
		try {
			firstOrderDate = context.getUser().getFirstOrderDateByStore(eStoreId);
		} catch (FDResourceException e) {
			//ignore
		}
		if(null !=firstOrderDate && this.rollingExpirationDays > 0){
			Date today = DateUtil.truncate(new Date());
			Date expirationDate = DateUtil.addDays(firstOrderDate, rollingExpirationDays);
			isBeforeExpDate = (!today.after(DateUtil.truncate(expirationDate)));	
		}
		return isBeforeExpDate;
	}

	@Override
	public int getPrecedence() {
		return 800;
	}

	public String toString() {
		return "AudienceCustomerStrategy [ UsageCount = "+ maxUsagePerCust+", ExpirationDate "+rollingExpirationDays+"]";
	}

	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
