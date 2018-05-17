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

public class ScarabPersonalRecommender extends AbstractScarabExternalRecommender {
	public ScarabPersonalRecommender(ScarabService service) {
		super(service);
	}

	@Override
	public List<RecommendationItem> recommendItems(ExternalRecommenderRequest request)
			throws ExternalRecommenderCommunicationException {
		try {
			BasicContext context = new BasicContext(ScarabProperties.getMerchantId(), null, null, null, null);
			context.setCustomerId(request.getCustomerId());
			Query query = new Query(Feature.PERSONAL, 0, ScarabProperties.getScarabQueryItemCount());
			service.invoke(context, query);
			List<TrackedItem> items = query.getRecommendations();
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
