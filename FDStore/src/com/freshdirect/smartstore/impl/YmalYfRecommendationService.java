package com.freshdirect.smartstore.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.smartstore.CartTabStrategyPriority;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.RecommendationServiceConfig;
import com.freshdirect.smartstore.RecommendationServiceType;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.dsl.CompileException;
import com.freshdirect.smartstore.fdstore.FactorRequirer;
import com.freshdirect.smartstore.service.RecommendationServiceFactory;
import com.freshdirect.smartstore.service.VariantRegistry;

public class YmalYfRecommendationService extends AbstractRecommendationService implements FactorRequirer {
	private ScriptedRecommendationService popularity;
	private RecommendationService smartYmal = null;
	
	public YmalYfRecommendationService(Variant variant, boolean includeCartItems) throws CompileException {
		super(variant, null, includeCartItems);
		popularity = createPopularityRecommender();
	}

	private ScriptedRecommendationService createPopularityRecommender() throws CompileException {
		RecommendationServiceConfig config = new RecommendationServiceConfig("ymal_yf_popularity",
				RecommendationServiceType.SCRIPTED);
		config.set(RecommendationServiceFactory.CKEY_SAMPLING_STRATEGY, "deterministic");
		config.set(RecommendationServiceFactory.CKEY_TOP_N, Integer.toString(1));
		config.set(RecommendationServiceFactory.CKEY_TOP_PERC, Double.toString(0.0));
		config.set(RecommendationServiceFactory.CKEY_GENERATOR, "PurchaseHistory");
		config.set(RecommendationServiceFactory.CKEY_SCORING, "Popularity_Discretized");
		
		Variant v = new Variant("ymal_yf_popularity", EnumSiteFeature.YMAL, config, new TreeMap<Integer, SortedMap<Integer,CartTabStrategyPriority>>());
		
		ScriptedRecommendationService rs = (ScriptedRecommendationService)
				RecommendationServiceFactory.configure(v);
		if (rs == null)
			throw new CompileException("cannot compile popularity recommender, see previous error message");
		return rs;
	}

	public List<ContentNodeModel> doRecommendNodes(SessionInput input) {
		RecommendationService smart = getSmartYmalRecommender();
		if (smart == null)
			return Collections.emptyList();
		
		List<ContentNodeModel> favorites = popularity.recommendNodes(input);
		if (favorites.isEmpty())
			return Collections.emptyList();
		
		ProductModel currentNode = (ProductModel) favorites.get(0);
		SessionInput i2 = new SessionInput(input.getCustomerId(), input.getCustomerServiceType(), input.getPricingContext(), input.getFulfillmentContext());
		i2.setMaxRecommendations(input.getMaxRecommendations());
		i2.setCartContents(input.getCartContents());
		i2.setCurrentNode(currentNode);
		i2.setYmalSource(currentNode);
		i2.setNoShuffle(input.isNoShuffle());
		i2.setTraceMode(input.isTraceMode());
		
		List<ContentNodeModel> prods = smart.recommendNodes(i2);
		
		if (i2.isTraceMode()) {
			input.mergeDataSourcesMap(i2.getDataSourcesMap());
		}
		
		return prods;
	}

	private synchronized RecommendationService getSmartYmalRecommender() {
		if (smartYmal == null) {
			Map<String, Variant> variantMap = VariantRegistry.getInstance().getServices(EnumSiteFeature.YMAL);
			for (Variant v : variantMap.values()) {
				RecommendationService rs = v.getRecommender();
				if (rs instanceof SmartYMALRecommendationService) {
					return smartYmal = rs;
				}
			}
			return null;
		} else
			return smartYmal;
	}

    public void collectFactors(Collection<String> factors) {
        popularity.collectFactors(factors);
    }
    
    public boolean isRefreshable() {
    	return true;
    }
}
