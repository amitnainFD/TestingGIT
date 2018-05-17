package com.freshdirect.fdstore.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class GenericFilter<T> {
	
	Map<FilteringValue, List<Object>> filterValues;
	Set<FilteringValue> filters;
	
	public GenericFilter(Map<FilteringValue, List<Object>>  filterValues, Set<FilteringValue> filters) { 
		this.filters=filters;
		this.filterValues=filterValues;
	}
	
	public void addFilterValue(FilteringValue key, Object value){
		if(filterValues.get(key)!=null){
			filterValues.get(key).add(value);
		}else{
			List<Object> values=new ArrayList<Object>();
			values.add(value);
			filterValues.put(key, values);
		}
	}

	public void removeFilterValues(FilteringValue key){
		filterValues.remove(key);
	}
	
	@Override
	public abstract GenericFilter<T> clone();

	public abstract void applyFilters(List<T> items, GenericFilteringMenuBuilder menuBuilder, FilteringFlow filteringFlow);
	
	public Map<FilteringValue, List<Object>> getFilterValues(){
		return filterValues;
	}

}
