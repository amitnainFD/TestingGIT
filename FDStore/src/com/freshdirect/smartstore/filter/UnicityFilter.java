package com.freshdirect.smartstore.filter;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * This filter removes item duplicates producing a list of unique items
 * 
 * @author segabor
 * 
 */
public final class UnicityFilter extends ContentFilter {
	private static final Logger LOGGER = LoggerFactory.getInstance(UnicityFilter.class);
	
	Set<ContentKey> keys;

	public UnicityFilter() {
		this.keys = new HashSet<ContentKey>();
	}

	public <X extends ContentNodeModel> X filter(X model) {
		if (!keys.contains(model.getContentKey())) {
			keys.add(model.getContentKey());
			return model;
		}
		LOGGER.debug("not unique: " + model.getContentKey());
		return null;
	}

	public void reset() {
		keys.clear();
	}
}