package com.freshdirect.fdstore.rollout;


public abstract class AbstractRolloutStrategy implements IRolloutStrategy {

	protected static final String STRATEGY_DELIMITER = ";";
	
	protected static final String STRATEGY_CONFIG_DELIMITER = ":";
	
	protected static final String STRATEGY_ATTR_DELIMITER = ",";
	
	public AbstractRolloutStrategy(String config) throws RolloutStrategyConfigException {
		if(config == null || config.trim().length() == 0) {
			throw new RolloutStrategyConfigException();
		}
	}

}
