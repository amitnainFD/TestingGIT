/**
 * 
 */
package com.freshdirect.fdstore.content;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.framework.util.BalkingExpiringReference;
import com.freshdirect.framework.util.log.LoggerFactory;

public final class WineFilterRatingIndex extends BalkingExpiringReference<Map<EnumWineRating,Set<ContentKey>>> {
	private static final Logger LOGGER = LoggerFactory.getInstance(WineFilterRatingIndex.class);
	
	private static final long FIVE_MINUTES = 5l * 60l * 1000l;
	
	private static WineFilterRatingIndex instance;
	
	public synchronized static WineFilterRatingIndex getInstance() {
		if (instance == null)
			instance = new WineFilterRatingIndex();
		return instance;
	}

	private WineFilterRatingIndex() {
		super(FIVE_MINUTES);
		try {
			set(load());
		} catch (RuntimeException e) {
			LOGGER.warn("failed to initialize rating cache");
		}
	}

	@Override
	protected Map<EnumWineRating,Set<ContentKey>> load() {
		try {
			LOGGER.warn("reloading rating cache...");
			Map<EnumWineRating,Set<ContentKey>> map = new HashMap<EnumWineRating,Set<ContentKey>>();
			Collection<ContentKey> keys = ContentFactory.getInstance().getAllWineProductKeys();
			for (EnumWineRating price : EnumWineRating.values())
				map.put(price, new HashSet<ContentKey>(1000));
			for (ContentKey key : keys) {
				ProductModel p = (ProductModel) ContentFactory.getInstance().getContentNodeByKey(key);
				if (p != null) {
					EnumWineRating rating = EnumWineRating.getEnumByRating(p.getProductRatingEnum());
					map.get(rating).add(key);
				}
			}
			for (EnumWineRating rating : EnumWineRating.values())
				map.put(rating, Collections.unmodifiableSet(map.get(rating)));
			LOGGER.warn("reloaded rating cache on " + keys.size() + " products.");
			return Collections.unmodifiableMap(map);
		} catch (FDResourceException e) {
			throw new RuntimeException(e);
		}
	}
}