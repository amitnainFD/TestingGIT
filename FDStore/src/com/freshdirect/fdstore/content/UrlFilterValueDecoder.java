package com.freshdirect.fdstore.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class UrlFilterValueDecoder extends GenericFilterValueDecoder {

		private static final String FILTER_SEPARATOR = ",";
		private static final String FILTER_VALUE_SEPARATOR = "=";

		public UrlFilterValueDecoder(Set<FilteringValue> filters) {
			super(filters);
		}

		public String getEncoded(Map<FilteringValue, List<Object>> filterValues) {
			StringBuilder buf = new StringBuilder();
			
			for(FilteringValue key: filterValues.keySet()){
				for(Object value: filterValues.get(key)){
					buf.append(key+FILTER_VALUE_SEPARATOR);
					buf.append(value+FILTER_SEPARATOR);
				}				
			}
			return buf.substring(0, buf.lastIndexOf(FILTER_SEPARATOR));
		}
		
		public Map<FilteringValue, List<Object>> decode(String encoded) {
			Map<FilteringValue, List<Object>> filterValues=new HashMap<FilteringValue, List<Object>>();
			for (String item : encoded.split(FILTER_SEPARATOR)) {
				decodeFilterValue(item, filterValues);
			}
			
			return filterValues;
		}
		
		private void decodeFilterValue(String item, Map<FilteringValue, List<Object>> filterValues) {
			String[] split = item.split(FILTER_VALUE_SEPARATOR);
			
			if (split.length != 2){
				return;			
			}
			
			for(FilteringValue filter: filters){
				if(filter.getName().equals(split[0])){
					if(filterValues.containsKey(filter)){
						filterValues.get(filter).add(split[1]);
					}else{
						List<Object> values=new ArrayList<Object>();
						values.add(split[1]);
						filterValues.put(filter, values);
					}
				}
			}
		}

}
