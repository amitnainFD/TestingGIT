package com.freshdirect.smartstore.external;

import java.util.Collections;
import java.util.List;

import com.freshdirect.framework.util.IndexedList;
import com.freshdirect.framework.util.TimedLruCache;

public class CachingExternalRecommender implements ExternalRecommender {
	final private ExternalRecommender originalRecommender;

	final private TimedLruCache<ExternalRecommenderRequest, List<RecommendationItem>> cache;

	protected CachingExternalRecommender(ExternalRecommender originalRecommender, int capacity, long timeout) {
		this.originalRecommender = originalRecommender;
		this.cache = new TimedLruCache<ExternalRecommenderRequest, List<RecommendationItem>>(capacity, timeout);
	}

	@Override
	public List<RecommendationItem> recommendItems(ExternalRecommenderRequest request) throws ExternalRecommenderCommunicationException {
		if (request.isCacheable()) {
			List<RecommendationItem> items = cache.get(request);
			if (items != null)
				return items;
			items = originalRecommender.recommendItems(request);
			if (items != null) {
				items = new IndexedList<RecommendationItem>(items);
				cache.put(request, items);
				return items;
			}
			return Collections.emptyList();
		}
		return originalRecommender.recommendItems(request);
	}
	
	public ExternalRecommender getOriginalRecommender() {
		return originalRecommender;
	}
}
