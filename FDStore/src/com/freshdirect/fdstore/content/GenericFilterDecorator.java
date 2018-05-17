package com.freshdirect.fdstore.content;

import java.util.Set;

import com.freshdirect.fdstore.util.FilteringNavigator;

public abstract class GenericFilterDecorator<N> {
	
	protected Set<FilteringValue> filters;
	
	protected FilteringNavigator nav;
	
	public GenericFilterDecorator(Set<FilteringValue> filters){
		this.filters=filters;
	}
	
	public abstract void decorateItem(N item);

	public void setFilters(Set<FilteringValue> filters) {
		this.filters = filters;
	}

	public FilteringNavigator getNav() {
		return nav;
	}

	public void setNav(FilteringNavigator nav) {
		this.nav = nav;
	}

}
