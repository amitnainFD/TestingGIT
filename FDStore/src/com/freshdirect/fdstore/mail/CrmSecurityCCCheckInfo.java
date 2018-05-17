package com.freshdirect.fdstore.mail;

import java.io.Serializable;

import com.freshdirect.customer.EnumAccountActivityType;

public class CrmSecurityCCCheckInfo implements Serializable{

	private String agentId;
	private String customerId;
	private String customerFirstName;
	private String customerLastName;
	private EnumAccountActivityType activityType;
	
	
	
	public String getAgentId() {
		return agentId;
	}
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	
	public EnumAccountActivityType getActivityType() {
		return activityType;
	}
	public void setActivityType(EnumAccountActivityType activityType) {
		this.activityType = activityType;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getCustomerFirstName() {
		return customerFirstName;
	}
	public void setCustomerFirstName(String customerFirstName) {
		this.customerFirstName = customerFirstName;
	}
	public String getCustomerLastName() {
		return customerLastName;
	}
	public void setCustomerLastName(String customerLastName) {
		this.customerLastName = customerLastName;
	}
	
	
}
