package com.freshdirect.fdstore.rollout;

import com.freshdirect.fdstore.customer.FDUserI;

public class GlobalRolloutStrategy extends AbstractRolloutStrategy {
	
	private boolean isFeatureEnabled;
	
	public GlobalRolloutStrategy(String config) throws RolloutStrategyConfigException {
		super(config);
		
		String[] strSplit = config.split(AbstractRolloutStrategy.STRATEGY_ATTR_DELIMITER);
		if(strSplit != null && strSplit.length > 1) {
			if("ENABLED".equalsIgnoreCase(strSplit[0]) && "TRUE".equalsIgnoreCase(strSplit[1])) {
				isFeatureEnabled = true;
			}
		}
	}
	
	@Override
	public boolean isEligibleForFeatureRollout(FDUserI user) {
		// TODO Auto-generated method stub
		return isFeatureEnabled;
	}
	
	@Override
	public EnumFeatureRolloutStrategy getRolloutStrategyType() {
		// TODO Auto-generated method stub
		return EnumFeatureRolloutStrategy.GLOBAL;
	}

	@Override
	public String toString() {
		return "GlobalRolloutStrategy [isFeatureEnabled=" + isFeatureEnabled
				+ "]";
	}

	
}
