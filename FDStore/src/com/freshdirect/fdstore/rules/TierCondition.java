package com.freshdirect.fdstore.rules;

import com.freshdirect.fdlogistics.model.EnumDeliveryFeeTier;
import com.freshdirect.rules.ConditionI;
import com.freshdirect.rules.RuleRuntimeI;
import com.freshdirect.rules.RulesRuntimeException;


public class TierCondition implements ConditionI {


	private transient EnumDeliveryFeeTier tier;
	private String value;

	public TierCondition() {
		
	}
	
	public TierCondition (String value) {
		this.value = value;
	}

	@Override
	public boolean evaluate(Object target, RuleRuntimeI ctx) {
		if(this.tier == null) {
			this.tier = EnumDeliveryFeeTier.getEnum(value);
			if(this.tier == null){
				throw new RulesRuntimeException("Could not find service type: "+this.value);
			}
		}
		
		FDRuleContextI dlvCtx = (FDRuleContextI) target;
		return this.tier.equals(dlvCtx.getDeliverFeeTier());
	}

	@Override
	public boolean validate() {
		return EnumDeliveryFeeTier.getEnum(this.value) != null;
	}

	public EnumDeliveryFeeTier getTier() {
		return tier;
	}

	public void setTier(EnumDeliveryFeeTier tier) {
		this.tier = tier;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
