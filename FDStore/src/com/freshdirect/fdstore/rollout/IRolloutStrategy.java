package com.freshdirect.fdstore.rollout;

import com.freshdirect.fdstore.customer.FDUserI;

public interface IRolloutStrategy {
	boolean isEligibleForFeatureRollout(FDUserI user);
	EnumFeatureRolloutStrategy getRolloutStrategyType();
}
