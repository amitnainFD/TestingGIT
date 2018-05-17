package com.freshdirect.fdstore.rollout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.FDStoreProperties.ConfigLoadedListener;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.framework.util.log.LoggerFactory;

//Controlled Rollout fdstore.properties samples
// feature.rollout.pdplayout2014=GLOBAL:ENABLED,false;PROFILE:VIPCustomer,true;COHORT:C9;
// feature.rollout.pdplayout2014=GLOBAL:ENABLED,false;PROFILE:VIPCustomer,false;

//Full Rollout fdstore.properties sample
// feature.rollout.pdplayout2014=GLOBAL:ENABLED,true;

public class FeatureRolloutArbiter implements ConfigLoadedListener {
	
	private static final Category LOGGER = LoggerFactory.getInstance(FeatureRolloutArbiter.class);
	
	private static Map<EnumRolloutFeature, List<IRolloutStrategy>> featureStrategyConfig = new HashMap<EnumRolloutFeature, List<IRolloutStrategy>>(); 
	
	private static Map<EnumRolloutFeature, String> rawFeatureStrategyConfig = new HashMap<EnumRolloutFeature, String>();
	
    private static final String FEATUREROLLOUT_PROPERTY_PREFIX = "feature.rollout.";
    
    static {
    	refresh();
    	FDStoreProperties.addConfigLoadedListener(new FeatureRolloutArbiter());
    }
    
	public static EnumFeatureRolloutStrategy getFeatureRolloutStrategy(EnumRolloutFeature feature, FDUserI user) {
		
		if(featureStrategyConfig.containsKey(feature)) {
			for(IRolloutStrategy strategy : featureStrategyConfig.get(feature)) {
				if(strategy.isEligibleForFeatureRollout(user)) {
					return strategy.getRolloutStrategyType();
				}
			}
		}
		return EnumFeatureRolloutStrategy.NONE;
	}
	
	private synchronized static void refresh() {
		String propValue = null;
    	for (EnumRolloutFeature e : EnumRolloutFeature.values()) {
    		
    		propValue = FDStoreProperties.get(FEATUREROLLOUT_PROPERTY_PREFIX + e.name());
    		
    		if(propValue != null && propValue.trim().length() > 0) {
    			
    			if(rawFeatureStrategyConfig.containsKey(e)) {
    				
    				if(!rawFeatureStrategyConfig.get(e).equals(propValue)) {
    					rawFeatureStrategyConfig.put(e, propValue);
        				featureStrategyConfig.put(e, decodeRolloutStrategy(propValue));
    				}
    			} else {
    				rawFeatureStrategyConfig.put(e, propValue);
    				featureStrategyConfig.put(e, decodeRolloutStrategy(propValue));
    			}
    		}
    	}
	}
	
	private static List<IRolloutStrategy> decodeRolloutStrategy(String propValue) {
		
		List<IRolloutStrategy> strategies = new ArrayList<IRolloutStrategy>();
		
		if(propValue != null && propValue.trim().length() > 0) {
			
			String[] strStrategies = propValue.split(AbstractRolloutStrategy.STRATEGY_DELIMITER); // Split Strategies
			
			if(strStrategies != null && strStrategies.length > 0) {
				
				for(String _strStg : strStrategies) {
					
					String[] strIsolate = _strStg.split(AbstractRolloutStrategy.STRATEGY_CONFIG_DELIMITER); // Split Strategy Configuration
					
					if(strIsolate != null && strIsolate.length > 0) {
						
						try {
							if(EnumFeatureRolloutStrategy.GLOBAL.name().equals(strIsolate[0])) {
								strategies.add(new GlobalRolloutStrategy(strIsolate[1]));
							} else if(EnumFeatureRolloutStrategy.PROFILE.name().equals(strIsolate[0])) {
								strategies.add(new ProfileRolloutStrategy(strIsolate[1]));
							} else if(EnumFeatureRolloutStrategy.COHORT.name().equals(strIsolate[0])) {
								strategies.add(new CohortRolloutStrategy(strIsolate[1]));
							}
						} catch(RolloutStrategyConfigException e) {
							//Incorrect configuration
							LOGGER.warn("INCORRECT ROLLOUT CONFIG:"+propValue);
						}
					}
				}
				
			}
		}
		return strategies;
	}

	@Override
	public void configLoaded() {
		
		refresh();
		LOGGER.warn("ROLLOUT CONFIG RELOADED");
	}
	
	public static boolean isFeatureRolledOut(EnumRolloutFeature feature, FDUserI user) {
		 return !EnumFeatureRolloutStrategy.NONE.equals(FeatureRolloutArbiter.getFeatureRolloutStrategy(feature, user));
	}
}
