package com.freshdirect.smartstore.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.fdstore.ScoreProvider;
import com.freshdirect.smartstore.sampling.ImpressionSampler;
import com.freshdirect.smartstore.sampling.RankedContent;

/**
 * A SmartStore variant that offers the most frequently bought items.
 * 
 * The instance keeps a session cache to spare frequent database lookups.
 * 
 * @author istvan
 * 
 */
@Deprecated
public class MostFrequentlyBoughtDyfVariant extends DYFService {
	private static final Category LOGGER = LoggerFactory.getInstance(MostFrequentlyBoughtDyfVariant.class);

	public MostFrequentlyBoughtDyfVariant(Variant variant, ImpressionSampler sampler, boolean includeCartItems) {
		super(variant, sampler, includeCartItems);
	}

	/**
	 * Recommend the most frequently bought products.
	 * 
	 * @param max
	 *            maximum number of products to produce
	 * @param input
	 *            session input
	 */
	public List<ContentNodeModel> recommend(SessionInput input) {
		@SuppressWarnings("unchecked")
		SessionCache.TimedEntry<List<RankedContent.Single>> cachedSortedProducts = getCache().get(input.getCustomerId());

		if (cachedSortedProducts == null || cachedSortedProducts.expired()) {
			LOGGER.debug("Loading order history for " + input.getCustomerId() + (cachedSortedProducts != null ? " (EXPIRED)" : ""));

			final Map<ContentKey, ? extends Number> productFrequencies = ScoreProvider.getInstance().getUserProductScores(input.getCustomerId());
			if (productFrequencies == null) {
				return Collections.emptyList();
			}

			List<RankedContent.Single> rankedContent = new ArrayList<RankedContent.Single>(productFrequencies.size());
			for (ContentKey key : productFrequencies.keySet()) {
				Number score = productFrequencies.get(key);
				if (score.doubleValue() == 0.)
					continue;

				rankedContent.add(new RankedContent.Single(key, score.doubleValue()));
			}
			Collections.sort(rankedContent);
			cachedSortedProducts = new SessionCache.TimedEntry<List<RankedContent.Single>>(rankedContent, 60 * 10 * 1000);
			getCache().put(input.getCustomerId(), cachedSortedProducts);
		}

		return sample(input, cachedSortedProducts.getPayload(), true);
	}
}
