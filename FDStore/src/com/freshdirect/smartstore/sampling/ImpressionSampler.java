package com.freshdirect.smartstore.sampling;

import java.util.List;
import java.util.Set;

import com.freshdirect.cms.ContentKey;

/**
 * Sampler that produces the Content Keys for impressions.
 * 
 * @author istvan, csongor
 * 
 */
public interface ImpressionSampler {
	/**
	 * produce a sampled (might be randomized) order of the rankedContent
	 * 
	 * @param rankedContent
	 *            ranked content list based either on the order of the items or
	 *            on the scores of the items
	 * @param exclusions
	 *            items to be excluded from the sampled list
	 * @return the sampled items
	 */
	public List<ContentKey> sample(List<RankedContent.Single> rankedContent, boolean aggregatable, Set<ContentKey> exclusions, boolean showTempUnavailable);

	public boolean isDeterministic();

	public ConsiderationLimit getConsiderationLimit();

	public boolean isCategoryAggregationEnabled();
	
	public boolean isUseAlternatives();
}
