package com.freshdirect.smartstore.external.scarab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.freshdirect.smartstore.external.ExternalRecommenderCommunicationException;
import com.freshdirect.smartstore.external.ExternalRecommenderRequest;
import com.freshdirect.smartstore.external.RecommendationItem;
import com.scarabresearch.recommendation.api.BasicContext;
import com.scarabresearch.recommendation.api.Query;
import com.scarabresearch.recommendation.api.ScarabService;
import com.scarabresearch.recommendation.api.ScarabServiceInvokeException;
import com.scarabresearch.recommendation.api.TrackedItem;
import com.scarabresearch.recommendation.api.Query.Feature;

public class ScarabRelatedRecommender extends AbstractScarabExternalRecommender {
	private Feature feature;
	
	public ScarabRelatedRecommender(ScarabService service, Feature feature) {		
		super(service);
		if (feature == Feature.CART || feature == Feature.PERSONAL) {
			throw new IllegalArgumentException("Unsupported feature for ScarabRelatedRecommender");
		}
		this.feature = feature;
	}

	@Override
	public List<RecommendationItem> recommendItems(ExternalRecommenderRequest request)
			throws ExternalRecommenderCommunicationException {
		try {
			BasicContext context = new BasicContext(ScarabProperties.getMerchantId(), null, null, null, null);
			context.setCustomerId(request.getCustomerId());
			List<TrackedItem> items = new ArrayList<TrackedItem>(request.getItems().size());
			
			for(RecommendationItem r : request.getItems()) {
			    items.add(new TrackedItem(r.getId(), r.getTrackId()));
			}
			context.setViewedItems(items);
			Query query = new Query(feature, 0, ScarabProperties.getScarabQueryItemCount());
			service.invoke(context, query);
			items = query.getRecommendations();
			if (items == null)
				return Collections.emptyList();
			List<RecommendationItem> results = new ArrayList<RecommendationItem>();
			for (TrackedItem item : items)
				results.add(new RecommendationItem(item.getId(), item.getTrackingCode()));
			return results;
		} catch (RuntimeException e) {
			throw new ExternalRecommenderCommunicationException(e);
		} catch (ScarabServiceInvokeException e) {
			throw new ExternalRecommenderCommunicationException(e);
		}
	}

}
