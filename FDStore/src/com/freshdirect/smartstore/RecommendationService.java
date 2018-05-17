package com.freshdirect.smartstore;

import java.util.List;

import com.freshdirect.fdstore.content.ContentNodeModel;

/**
 * Recommendation service provider interface.
 * 
 * @author istvan
 */
public interface RecommendationService {
	/**
	 * Get the variant representing this service.
	 * 
	 * @return variant
	 */
	public Variant getVariant();

	/**
	 * Recommend a list of {@link ContentNodeModel}s.
	 * 
	 * @param input
	 *            session information.
	 * 
	 * 
	 * @return a List<{@link ContentNodeModel}> of recommendations, expected to
	 *         be sorted by relevance
	 */
	public List<ContentNodeModel> recommendNodes(SessionInput input);

	public boolean isIncludeCartItems();

	/**
	 * Recommends savings items
	 * @return
	 */
	public boolean isSmartSavings();


	/**
	 * May provide different results on refresh
	 * 
	 * @return
	 */
	public boolean isRefreshable();
}
