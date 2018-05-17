package com.freshdirect.fdstore.rules;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.rules.ConditionI;
import com.freshdirect.rules.RuleRuntimeI;
import com.freshdirect.rules.RulesRuntimeException;


public class ServiceTypeCondition implements ConditionI {


	private transient EnumServiceType serviceType;
	private String type;

	public ServiceTypeCondition() {
		
	}
	
	public ServiceTypeCondition (String type) {
		this.type = type;
	}

	@Override
	public boolean evaluate(Object target, RuleRuntimeI ctx) {
		if(this.serviceType == null) {
			this.serviceType = EnumServiceType.getEnum(type);
			if(this.serviceType == null){
				throw new RulesRuntimeException("Could not find service type: "+this.type);
			}
		}
		
		FDRuleContextI dlvCtx = (FDRuleContextI) target;
		return this.serviceType.equals(dlvCtx.getServiceType());
	}

	@Override
	public boolean validate() {
		return EnumServiceType.getEnum(this.type) != null;
	}

	public EnumServiceType getServiceType() {
		return this.serviceType;
	}
	
	public void setServiceType(EnumServiceType serviceType) {
		this.serviceType = serviceType;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
}
