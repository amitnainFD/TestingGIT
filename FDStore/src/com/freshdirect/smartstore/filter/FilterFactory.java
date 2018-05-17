package com.freshdirect.smartstore.filter;

import java.util.Collection;

import com.freshdirect.cms.ContentKey;

public class FilterFactory {
    private final static ContentFilter AVAILABLE_ITEMS_W_ALTS = new ProductAvailabilityFilterEx(false);
    private final static ContentFilter AVAILABLE_ITEMS = new ProductAvailabilityFilter(false);

    private final static ContentFilter TEMP_AVAILABLE_ITEMS_W_ALTS = new ProductAvailabilityFilterEx(true);
    private final static ContentFilter TEMP_AVAILABLE_ITEMS = new ProductAvailabilityFilter(true);
    
    private static FilterFactory INSTANCE = null;
    
    public synchronized static final FilterFactory getInstance() {
    	if (INSTANCE == null) {
    		INSTANCE = new FilterFactory();
    	}
    	
    	return INSTANCE;
    }
	
	public synchronized static void mockInstance(FilterFactory newInstance) {
		INSTANCE = newInstance;
	}
    
    protected FilterFactory() {
    }
    
	public ContentFilter createFilter(Collection<ContentKey> exclusions, boolean useAlternatives, boolean showTempUnavailable) {
		ArrayFilter filter = new ArrayFilter();
		
		if ((exclusions != null) && !exclusions.isEmpty()) {
			filter.addFilter(new ExclusionFilter(exclusions));
		}
		filter.addFilter(showTempUnavailable ?
		            (useAlternatives ? TEMP_AVAILABLE_ITEMS_W_ALTS : TEMP_AVAILABLE_ITEMS)
                        : 
                            (useAlternatives ? AVAILABLE_ITEMS_W_ALTS : AVAILABLE_ITEMS));
		filter.addFilter(new UnicityFilter());
		
		return filter;
	}
}
