package com.freshdirect.fdstore.rollout;

import com.freshdirect.fdstore.customer.FDCustomerModel;
import com.freshdirect.fdstore.customer.FDUserI;

public class ProfileRolloutStrategy  extends AbstractRolloutStrategy {
	
	private String profileName;	
	private String profileValue;
	
	public ProfileRolloutStrategy(String config) throws RolloutStrategyConfigException {
		super(config);
		
		String[] strSplit = config.split(AbstractRolloutStrategy.STRATEGY_ATTR_DELIMITER);
		if(strSplit != null && strSplit.length > 1) {
			profileName = strSplit[0];
			profileValue = strSplit[1];
		}
	}
		
	@Override
	public boolean isEligibleForFeatureRollout(FDUserI user) {
		// TODO Auto-generated method stub
		if(user != null && profileName != null && profileValue != null) {
			FDCustomerModel fdCust=null;
			try {
				fdCust = user.getFDCustomer();
			} catch (Exception e) {
				//Ignore Something is wrong with the user, mostly new users throw IllegalStateException
			}
			if(fdCust != null && fdCust.getProfile() != null 
						&& profileValue.equalsIgnoreCase(fdCust.getProfile().getAttribute( profileName ))) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public EnumFeatureRolloutStrategy getRolloutStrategyType() {
		// TODO Auto-generated method stub
		return EnumFeatureRolloutStrategy.PROFILE;
	}

	@Override
	public String toString() {
		return "ProfileRolloutStrategy [profileName=" + profileName
				+ ", profileValue=" + profileValue + "]";
	}
	
	

}
