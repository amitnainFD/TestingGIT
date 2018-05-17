package com.freshdirect.fdstore.rollout;

import java.util.HashSet;
import java.util.Set;

import com.freshdirect.fdstore.customer.FDUserI;

public class CohortRolloutStrategy  extends AbstractRolloutStrategy {

	private Set<String> eligibleCohorts = new HashSet<String>();
	
	public CohortRolloutStrategy(String config) throws RolloutStrategyConfigException {
		super(config);
		String[] strSplit = config.split(AbstractRolloutStrategy.STRATEGY_ATTR_DELIMITER);
		if(strSplit != null && strSplit.length > 0) {
			for(String cohort : strSplit) {
				eligibleCohorts.add(cohort);
			}
		}
	}

	@Override
	public boolean isEligibleForFeatureRollout(FDUserI user) {
		
		return user != null && user.getCohortName() != null && eligibleCohorts.contains(user.getCohortName());
	}

	@Override
	public String toString() {
		return "CohortRolloutStrategy [eligibleCohorts=" + eligibleCohorts
				+ "]";
	}

	@Override
	public EnumFeatureRolloutStrategy getRolloutStrategyType() {
		// TODO Auto-generated method stub
		return EnumFeatureRolloutStrategy.COHORT;
	}	

}
