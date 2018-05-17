package com.freshdirect.fdstore.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class GenericFilteringMenuBuilder<I extends FilteringSortingItem<? extends ContentNodeModel>> {

	//left side filtering menu - the key in the map inside is for simplified usage only (it's the same name as the name property in the FilteringMenuItem)
	protected Map<FilteringValue, Map<String,FilteringMenuItem>> domains=new HashMap<FilteringValue, Map<String,FilteringMenuItem>>();
	
	//temporary menu items will be merged to the final menu at the end of the filtering
	//e.g. we need to build a temporary menu domain for departments on quickshop
	protected Map<FilteringValue, Map<String,FilteringMenuItem>> tempDomains=new HashMap<FilteringValue, Map<String,FilteringMenuItem>>();

	protected Set<FilteringValue> filters;
	
	Map<FilteringValue, List<Object>> filterValues;
	
	public GenericFilteringMenuBuilder(Map<FilteringValue, List<Object>> filterValues, Set<FilteringValue> filters) {
		this.filterValues = filterValues;
		this.filters = filters;
	}

	public abstract void buildMenu(List<I> items);
	
	public abstract void buildMenuForFilter(List<I> items, FilteringValue filter, boolean removeFilter, boolean temporary);

	public Map<FilteringValue, Map<String, FilteringMenuItem>> getDomains() {
		return domains;
	}
	
}
