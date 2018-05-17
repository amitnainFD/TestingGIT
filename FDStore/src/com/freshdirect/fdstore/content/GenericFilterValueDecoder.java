package com.freshdirect.fdstore.content;

import java.util.List;
import java.util.Map;
import java.util.Set;


public abstract class GenericFilterValueDecoder {
	
	protected Set<FilteringValue> filters;

	public GenericFilterValueDecoder(Set<FilteringValue> filters) {
		this.filters = filters;
	}
	
	public abstract Map<FilteringValue, List<Object>> decode(String encoded);
	
	public abstract String getEncoded(Map<FilteringValue, List<Object>> filterValues);

}
