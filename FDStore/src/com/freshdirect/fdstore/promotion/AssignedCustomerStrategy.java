package com.freshdirect.fdstore.promotion;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.framework.util.DateUtil;

public class AssignedCustomerStrategy implements PromotionStrategyI {

	private final Map<String,AssignedCustomerParam> assignedCustomers;
	private Integer rollingExpirationDays = null;
	private boolean isMaxUsagePerCustomer = false;
	
	public AssignedCustomerStrategy() {
		this.assignedCustomers = new HashMap<String,AssignedCustomerParam>();
	}

	public AssignedCustomerStrategy(Map<String,AssignedCustomerParam> assignedCustomers, Integer rollingExpirationDays, boolean isMaxUsagePerCustomer) {
		this();
		this.assignedCustomers.putAll(assignedCustomers);
		setRollingExpirationDays(rollingExpirationDays);
		setIsMaxUsagePerCustomer(isMaxUsagePerCustomer);
	}

	public void addCustomer(String customerId, AssignedCustomerParam param) {		
		this.assignedCustomers.put(customerId, param);
	}

	public AssignedCustomerParam getParam(String customerId) {
		return (AssignedCustomerParam)this.assignedCustomers.get(customerId);
	}
	
	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		FDIdentity identity = context.getIdentity();
		if(identity != null) {
			AssignedCustomerParam param = (AssignedCustomerParam) this.assignedCustomers.get(identity.getErpCustomerPK());
			if (param != null && isValidUsageCount(promotionCode, context, param) && isBeforeExpirationDate(param)) {
					return ALLOW;
				}
			}				
		return DENY;		
	}

	public boolean isValidForCustomer(String customerId) {
		return customerId == null ? false : this.assignedCustomers.containsKey(customerId);
	}

	private boolean isValidUsageCount(String promotionCode, PromotionContextI context, AssignedCustomerParam param) {
		int promoUsageCnt = context.getPromotionUsageCount(promotionCode);		
		return (!this.isMaxUsagePerCustomer || (param != null && param.getUsageCount() != null && param.getUsageCount().intValue() > promoUsageCnt)); 
	}

	private boolean isBeforeExpirationDate(AssignedCustomerParam param) {
		Date today = DateUtil.truncate(new Date());
		return (this.rollingExpirationDays == null || (param.getExpirationDate() != null && !today.after(DateUtil.truncate(param.getExpirationDate()))));					
	}

	public Integer getRollingExpirationDays() { return this.rollingExpirationDays; }
	
	public void setRollingExpirationDays(Integer rollingExpirationDays) { this.rollingExpirationDays = rollingExpirationDays; }

	public boolean isMaxUsagePerCustomer() { return this.isMaxUsagePerCustomer; }
	
	public void setIsMaxUsagePerCustomer(boolean isMaxUsagePerCustomer) { this.isMaxUsagePerCustomer = isMaxUsagePerCustomer; }

	@Override
	public int getPrecedence() {
		return 800;
	}

	public String toString() {
		return "AssignedCustomerStrategy [" + this.assignedCustomers.size() + " customers]";
	}

	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
