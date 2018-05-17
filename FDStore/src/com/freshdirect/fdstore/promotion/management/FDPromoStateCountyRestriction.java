package com.freshdirect.fdstore.promotion.management;

import java.util.Date;
import java.util.HashSet;

import com.freshdirect.framework.core.ModelSupport;

public class FDPromoStateCountyRestriction extends ModelSupport {

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
	
	
	public FDPromoStateCountyRestriction() {
		super();
	}
	
	public FDPromoStateCountyRestriction(String promotionId, String userId,
			Date actionDate, HashSet<String> county, HashSet<String> states, String county_option, String state_option) {
		super();
		this.promotionId = promotionId;
		this.userId = userId;
		this.actionDate = actionDate;
		this.county_option = county_option;
		this.state_option = state_option;
		this.states = states;
		this.county = county;
	}

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

	public HashSet<String> getCounty() {
		if(county != null)
			return county;
		else 
			return new HashSet();
	}

	public void setCounty(HashSet<String> county) {
		this.county = county;
	}

	public HashSet<String> getStates() {
		if(states != null)
			return states;
		else
			return new HashSet();
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
	public String toString() {
		return "FDPromoStateCountyRestriction [actionDate=" + actionDate
				+ ", county=" + county + ", county_option=" + county_option
				+ ", promotionId=" + promotionId + ", state_option="
				+ state_option + ", states=" + states + ", userId=" + userId
				+ "]";
	}

	
	
	
}
