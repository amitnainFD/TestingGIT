package com.freshdirect.smartstore.impl;

import java.util.List;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.sampling.ImpressionSampler;

@Deprecated
public abstract class BaseContentKeyRecommendationService extends AbstractRecommendationService {
	/**
	 * Recommend a list of {@link ContentKey}s.
	 * 
	 * 
	 * @param input
	 *            session information.
	 * @param max
	 *            the maximum number of recommendations to be produced
	 * @return a List<{@link ContentKey}> of recommendations, expected to be
	 *         sorted by relevance
	 */
	abstract public List<ContentNodeModel> recommend(SessionInput input);

	public BaseContentKeyRecommendationService(Variant variant, ImpressionSampler sampler, boolean includeCartItems) {
		super(variant, sampler, includeCartItems);
	}

	public List<ContentNodeModel> doRecommendNodes(SessionInput input) {
		return recommend(input);
	}

}
