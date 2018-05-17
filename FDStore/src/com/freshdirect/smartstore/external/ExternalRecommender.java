package com.freshdirect.smartstore.external;

import java.util.List;

/**
 * Represents a provider for external recommendations.
 * 
 * @author csongor
 */
public interface ExternalRecommender {
	/**
	 * Returns recommendations given by the external recommender.
	 * 
	 * @param request
	 *            the attributes of the recommendation request
	 * @return a list of recommended items
	 * @throws ExternalRecommenderCommunicationException
	 *             if error occurs while communicating with the external
	 *             recommender
	 */
	public List<RecommendationItem> recommendItems(ExternalRecommenderRequest request) throws ExternalRecommenderCommunicationException;
}
