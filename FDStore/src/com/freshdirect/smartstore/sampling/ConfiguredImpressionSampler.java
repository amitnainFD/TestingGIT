/**
 * 
 */
package com.freshdirect.smartstore.sampling;

import java.util.List;

public class ConfiguredImpressionSampler extends AbstractImpressionSampler {
	private ListSampler listSampler;

	public ConfiguredImpressionSampler(ConsiderationLimit considerationLimit, boolean categoryAggregationEnabled, boolean useAlternatives, ListSampler listSampler) {
		super(considerationLimit, categoryAggregationEnabled, useAlternatives);
		this.listSampler = listSampler;
	}

	@Override
	protected ListSampler createSampler(List<RankedContent> limitedRankedContent) {
		return listSampler;
	}

	public boolean isDeterministic() {
		return listSampler.isDeterministic();
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[listSampler=" + listSampler
				+ ", considerationLimit=" + getConsiderationLimit()
				+ ", categoryAggregationEnabled=" + isCategoryAggregationEnabled()
				+ ", useAlternatives=" + isUseAlternatives() + "]";
	}
}
