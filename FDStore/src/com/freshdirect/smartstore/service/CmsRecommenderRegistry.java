package com.freshdirect.smartstore.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.application.CmsManager;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.RecommenderStrategy;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.CartTabStrategyPriority;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.RecommendationServiceConfig;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.external.scarab.ScarabInfrastructure;
import com.freshdirect.smartstore.fdstore.FactorRequirer;
import com.freshdirect.smartstore.fdstore.ScoreProvider;

public class CmsRecommenderRegistry {
	
	private static final Logger LOGGER = LoggerFactory.getInstance(CmsRecommenderRegistry.class);

	private static CmsRecommenderRegistry instance = null;

	private Map<String, RecommendationService> smartCatVariants = null;
	private long lastReload = Long.MIN_VALUE;

	private CmsRecommenderRegistry() {
	}

	public static synchronized CmsRecommenderRegistry getInstance() {
		if (instance == null) {
			instance = new CmsRecommenderRegistry();
		}
		return instance;
	}

	private void load(boolean forceReload) {
		ScarabInfrastructure.reload();
		Map<String, RecommendationService> tmpSmartCatVariants = new HashMap<String, RecommendationService>();
		Set<ContentKey> rss = CmsManager.getInstance().getContentKeysByType(FDContentTypes.RECOMMENDER_STRATEGY);
		if (smartCatVariants == null) {
			LOGGER.info("first time initialization");
			forceReload = true;
		}

		if (!forceReload) {
			if (System.currentTimeMillis() - lastReload > FDStoreProperties.getCmsRecommenderRefreshRate() * 60 * 1000) {
				forceReload = true;
				LOGGER.info("cache timed out, need reload");
			}
		}
		
		if (!forceReload) {
			LOGGER.debug("reload not forced");
			return;
		}

		LOGGER.info("loading CMS recommenders:" + rss);

		Set<String> factors = new HashSet<String>();

		Iterator<ContentKey> it = rss.iterator();
		while (it.hasNext()) {
			ContentKey key = it.next();
			RecommenderStrategy strat = (RecommenderStrategy) ContentFactory.getInstance().getContentNodeByKey(key);
			RecommendationServiceConfig config = RecommendationServiceFactory.createServiceConfig(strat);

			Variant v = new Variant("cms_" + strat.getContentName(),
					EnumSiteFeature.SMART_CATEGORY, config, new TreeMap<Integer, SortedMap<Integer, CartTabStrategyPriority>>());
			RecommendationService rs = RecommendationServiceFactory.configure(v);
			if (rs instanceof FactorRequirer) {
				((FactorRequirer) rs).collectFactors(factors);
			}
			tmpSmartCatVariants.put(strat.getContentName(), rs);
		}

		LOGGER.info("needed factors :" + factors);
		ScoreProvider.getInstance().acquireFactors(factors);
		LOGGER.info("configured CMS recommenders:" + tmpSmartCatVariants.keySet());

		smartCatVariants = tmpSmartCatVariants;
		lastReload = System.currentTimeMillis();
	}

	public synchronized RecommendationService getService(String recommenderStrategyId) {
		load(false);
		return smartCatVariants.get(recommenderStrategyId);
	}

	public synchronized void reload() {
		load(true);
	}
}