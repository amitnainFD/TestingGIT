package com.freshdirect.fdstore.rules;

import com.freshdirect.rules.ConditionI;
import com.freshdirect.rules.RuleRuntimeI;

/**
 * @author knadeem Date Apr 12, 2005
 */
public class CountyCondition implements ConditionI {
	
	private String county;

	public CountyCondition() {
	}
	
	public CountyCondition(String county){
		this.county = county;
	}

	@Override
	public boolean evaluate(Object target, RuleRuntimeI ctx) {
		if(this.county == null || "".equals(this.county)){
			return false;
		}
		
		FDRuleContextI dlvCtx = (FDRuleContextI) target;
		return this.county.equalsIgnoreCase(dlvCtx.getCounty());
	}

	@Override
	public boolean validate() {
		return this.county != null && !"".equals(this.county);
	}
	
	public String getCounty() {
		return county;
	}
	
	public void setCounty(String county) {
		this.county = county;
	}
}
