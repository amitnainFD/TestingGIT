package com.freshdirect.fdstore.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilteringSortingItemFilter<N extends ContentNodeModel> extends GenericFilter<FilteringSortingItem<N>> {

	public FilteringSortingItemFilter(Map<FilteringValue, List<Object>> filterValues, Set<FilteringValue> filters) {
		super(filterValues, filters);
	}
	
	public void applyFilters(List<FilteringSortingItem<N>> items, GenericFilteringMenuBuilder menuBuilder, FilteringFlow filteringFlow){
		
		List<FilteringValue> sortedFilters = new ArrayList<FilteringValue>(filters);
		//sort filters (domains in the menu) based on position
		Collections.sort(sortedFilters, new Comparator<FilteringValue>() {

			@Override
			public int compare(FilteringValue o1, FilteringValue o2) {
				return o1.getPosition().compareTo(o2.getPosition());
			}
		});
		
		
		for ( FilteringValue filter : sortedFilters ) {
				
			if(filter.isMultiSelect()){
				//check whether temporary menus needed. temporary menus only needed if we have to store the temporary state of the items (build a temporary menu before any other filtering)
				//e.g on quickshop, preferences filter has an effect back to the higher positioned departments (dept numbers will be recalculated and we need to show menus with 0 items in it)
				boolean tempMenuNeeded = false;
				if(filter.isTempMenuNeeded()){
					for(FilteringValue fv: filterValues.keySet()){
						if(fv.getPosition()>filter.getPosition()){ //temp menu only needed when lower priority filter is present (e.g minimum one preferences filter is present in quickshop)
							tempMenuNeeded = true;
							break;
						}
					}
				}
				menuBuilder.buildMenuForFilter(items, filter, !tempMenuNeeded, tempMenuNeeded);
			}
				
			if ( filterValues.get( filter ) != null ) {			
				applyFilter(items, filter);
			}
			
			if( filter.isMidProcessingNeeded() ){
				filteringFlow.midProcess(items);
			}
		}
		
	}

	private void applyFilter(List<FilteringSortingItem<N>> items, FilteringValue filter) {
		
		Iterator<FilteringSortingItem<N>> it = items.iterator();
		
		while ( it.hasNext() ) {
			
			FilteringSortingItem<N> item = it.next();
			Object itemFilteringValue = item.getFilteringValue(filter);
			
			boolean passed = false;
			if (itemFilteringValue != null) {
				if(itemFilteringValue instanceof Set){
					Set<?> fvSet=(Set<?>)itemFilteringValue;
					for (Object filteringValue : filterValues.get(filter)) {
						if (fvSet.contains(filteringValue)) {
							passed = true;
							break;
						}
					}
				} else{
					for (Object filteringValue : filterValues.get(filter)) {
						if (itemFilteringValue.equals(filteringValue)) {
							passed = true;
							break;
						}
					}					
				}
			}
			
			if ( !passed ) {
				it.remove();
			}
		}
	}

	@Override
	public GenericFilter<FilteringSortingItem<N>> clone() {
		return new FilteringSortingItemFilter<N>(new HashMap<FilteringValue, List<Object>>(this.filterValues), new HashSet<FilteringValue>(this.filters));
	}

}
