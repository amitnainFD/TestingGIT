package com.freshdirect.fdstore.promotion;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Category;

import com.freshdirect.framework.util.log.LoggerFactory;

public class StateCountyStrategy implements PromotionStrategyI {
	
	private static final Category LOGGER = LoggerFactory.getInstance(StateCountyStrategy.class);

	private static final long serialVersionUID = 1L;
	private String promotionId;
	private String userId;
	private Date actionDate;
	private HashSet<String> county;
	private HashSet<String> states;
	private String county_option;
	private String state_option;
	private String[] stateArray;
	private String[] countyArray;
	
	
	public String getPromotionId() {
		return promotionId;
	}

	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getActionDate() {
		return actionDate;
	}

	public void setActionDate(Date actionDate) {
		this.actionDate = actionDate;
	}

	public Set<String> getCounty() {
		if(county != null)
			return county;
		else 
			return Collections.emptySet();
	}

	public void setCounty(HashSet<String> county) {
		this.county = county;
	}

	public Set<String> getStates() {
		if(states != null)
			return states;
		else
			return Collections.emptySet();
	}

	public void setStates(HashSet<String> states) {
		this.states = states;
	}

	public String getCounty_option() {
		return county_option;
	}

	public void setCounty_option(String countyOption) {
		county_option = countyOption;
	}

	public String getState_option() {
		return state_option;
	}

	public void setState_option(String stateOption) {
		state_option = stateOption;
	}
	
	public String[] getStateArray() {
		return stateArray;
	}

	public void setStateArray(String[] stateArray) {
		this.stateArray = stateArray;
	}

	public String[] getCountyArray() {
		return countyArray;
	}

	public void setCountyArray(String[] countyArray) {
		this.countyArray = countyArray;
	}

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		System.out.println("\n\n\n\n\nApplying StateCountyStrategy begin \n\n\n\n");
		System.out.println("PromotionID: " + this.getPromotionId());
		
		String county = null;
		String state = null;
		if(context.getShoppingCart() != null) {
			if (context.getShoppingCart().getDeliveryAddress() != null) {
				state =context.getShoppingCart().getDeliveryAddress().getState();
				String city = context.getShoppingCart().getDeliveryAddress().getCity();
				if(context.getShoppingCart().getDeliveryAddress().getAddressInfo()!=null){
				 county = context.getShoppingCart().getDeliveryAddress().getAddressInfo().getCounty();
				}
				if(county == null && city != null && state != null) {
					try {
						county = com.freshdirect.fdstore.FDDeliveryManager.getInstance().getCounty(city, state);
					} catch (Exception e) {
						LOGGER.error("Error getting the county for city:" + city + ",state:" + state, e);
					}
				}
			}
		}		
		System.out.println("County:" + county);
		System.out.println("State:" + state);
		if(county != null || state != null) {
			System.out.println("Checking for eligibility");
			boolean eligible_for_state = false;
			boolean eligible_for_county = false;
			if("A".equals(getState_option())) {
				//All EXCEPT the listed states
				System.out.println("State_option is A:" + this.getStates());
				if(!this.getStates().contains(state)) {
					System.out.println("State is eligible");
					eligible_for_state = true;
				}
			} else {
				System.out.println("State_option is O:" + this.getStates());
				if(this.getStates().contains(state)) {
					System.out.println("State is eligible");
					eligible_for_state = true;
				}
			}
			
			if(eligible_for_state) {
				//Time to check county
				if("A".equals(this.getCounty_option())) {
					//All EXCEPT the listed states
					System.out.println("County_option is A:" + this.getCounty());
					if(!this.getCounty().contains(state+"_"+county)) {
						System.out.println("County is eligible");
						return ALLOW;
					}
				} else {
					System.out.println("County_option is O:" + this.getCounty());
					if(this.getCounty().contains(state+"_"+county)) {
						System.out.println("County is eligible");
						return ALLOW;
					}
				}
			}
		}
		
		System.out.println("\n\n\n\n\nApplying StateCountyStrategy end \n\n\n\n");
		if(context.getUser().getRedeemedPromotion() != null && context.getUser().getRedeemedPromotion().getRedemptionCode() != null)
			context.getUser().addPromoErrorCode(promotionCode, PromotionErrorType.NO_ELIGIBLE_ADDRESS_SELECTED.getErrorCode());
		return DENY;
		
	}

	@Override
	public int getPrecedence() {
		return 0;
	}

	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
