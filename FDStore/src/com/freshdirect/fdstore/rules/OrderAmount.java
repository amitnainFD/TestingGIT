package com.freshdirect.fdstore.rules;

import com.freshdirect.framework.util.MathUtil;
import com.freshdirect.rules.ConditionI;
import com.freshdirect.rules.RuleRuntimeI;

public class OrderAmount implements ConditionI {

	private Double minimum;
	private Double maximum;

	@Override
	public boolean evaluate(Object target, RuleRuntimeI ctx) {
		FDRuleContextI c = (FDRuleContextI) target;
		double orderTotal = MathUtil.roundDecimal(c.getOrderTotal());
		
		if(this.minimum == null && this.maximum == null){
			return false;
		}
		
		boolean ret = true;
		if(this.minimum != null){
			ret &= MathUtil.roundDecimal(this.minimum.doubleValue()) <= orderTotal;
		}
		if(this.maximum != null){
			ret &= MathUtil.roundDecimal(this.maximum.doubleValue()) > orderTotal;
		}
		return ret;
	}
	
	public Double getMaximum() {
		return maximum;
	}

	public void setMaximum(Double maximum) {
		this.maximum = maximum;
	}

	public Double getMinimum() {
		return minimum;
	}

	public void setMinimum(Double minimum) {
		this.minimum = minimum;
	}

	@Override
	public boolean validate() {
		if(minimum == null && maximum == null){
			return false;
		}
		if((minimum != null && maximum == null) || (minimum == null && maximum != null)){
			return true;
		}
		if(MathUtil.roundDecimal(minimum.doubleValue()) >= MathUtil.roundDecimal(maximum.doubleValue())) {
			return false;
		}
		return true;
	}
}
