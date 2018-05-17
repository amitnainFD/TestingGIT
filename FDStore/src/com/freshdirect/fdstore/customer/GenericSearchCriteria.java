package com.freshdirect.fdstore.customer;

import java.util.HashMap;
import java.util.Map;

public class GenericSearchCriteria {
	private EnumSearchType searchType;
	private Map criteriaMap = new HashMap();
	
	public GenericSearchCriteria(EnumSearchType searchType){
		this.searchType = searchType;
	}
	
	public Map getCriteriaMap() {
		return criteriaMap;
	}
	
	public void setCriteriaMap(Object name, Object value){
		this.criteriaMap.put(name, value);
	}
	public EnumSearchType getSearchType() {
		return searchType;
	}
	public void setSearchType(EnumSearchType searchType) {
		this.searchType = searchType;
	}
	public boolean isBlank() {
		return this.searchType== null && this.criteriaMap.isEmpty();
	}
}
