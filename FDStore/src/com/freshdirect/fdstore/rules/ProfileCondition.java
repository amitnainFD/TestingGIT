package com.freshdirect.fdstore.rules;

import com.freshdirect.rules.ConditionI;
import com.freshdirect.rules.RuleRuntimeI;

public class ProfileCondition implements ConditionI {
	
	private String attributeName;
	private String attributeValue;

	@Override
	public boolean evaluate(Object target, RuleRuntimeI ctx) {
		FDRuleContextI context = (FDRuleContextI)target;
		return context.hasProfileAttribute(attributeName, attributeValue);
	}


	@Override
	public boolean validate() {
		if(attributeName == null || attributeValue == null){
			return false;
		}
		if("".equals(attributeName) || "".equals(attributeValue)){
			return false;
		}
		return true;
	}

	public String getAttributeName() {
		return attributeName;
	}
	
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	public String getAttributeValue() {
		return attributeValue;
	}
	
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
}
