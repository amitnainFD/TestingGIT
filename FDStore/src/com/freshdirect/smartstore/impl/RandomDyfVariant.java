package com.freshdirect.smartstore.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Category;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.fdstore.ProductStatisticsProvider;
import com.freshdirect.smartstore.sampling.ImpressionSampler;
import com.freshdirect.smartstore.sampling.RankedContent;

/**
 * Recommend randomly from purchase history.
 * 
 * The service keeps a {@link SessionCache session cache}, to spare frequent
 * database lookups.
 * 
 * @author istvan
 */
@Deprecated
public class RandomDyfVariant extends DYFService {
	private static final Category LOGGER = LoggerFactory.getInstance(RandomDyfVariant.class);

	private Random random;

	public RandomDyfVariant(Variant variant, ImpressionSampler sampler, boolean includeCartItems) {
		super(variant, sampler, includeCartItems);
		random = new Random();
	}

	/**
	 * Randomly selects keys.
	 * 
	 */
	public List<ContentNodeModel> recommend(SessionInput input) {
		SessionCache.TimedEntry<List<RankedContent.Single>> shoppingHistory = getCache().get(input.getCustomerId());

		if (shoppingHistory == null || shoppingHistory.expired()) {
			LOGGER.debug("Loading order history for " + input.getCustomerId() + (shoppingHistory != null ? " (EXPIRED)" : ""));

			// if not, retrieve history
			@SuppressWarnings("unchecked")
			Set<ContentKey> products = ProductStatisticsProvider.getInstance().getProducts(input.getCustomerId());

			if (products == null)
				return Collections.emptyList();

			List<ContentNodeModel> randomList = new ArrayList<ContentNodeModel>(products.size());
			for (ContentKey key : products) {
				ContentNodeModel product = ContentFactory.getInstance().getContentNodeByKey(key);
				if (product != null)
					randomList.add(product);
			}
			Collections.shuffle(randomList, random);

			List<RankedContent.Single> rankedContent = rankListByOrder(randomList);

			shoppingHistory = new SessionCache.TimedEntry<List<RankedContent.Single>>(rankedContent, 10 * 60 * 1000);
			getCache().put(input.getCustomerId(), shoppingHistory);
		}

		return sample(input, shoppingHistory.getPayload(), true);
	}
}
