package com.freshdirect.smartstore.fdstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Category;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductContainer;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.external.ExternalRecommender;
import com.freshdirect.smartstore.external.ExternalRecommenderCommunicationException;
import com.freshdirect.smartstore.external.ExternalRecommenderRegistry;
import com.freshdirect.smartstore.external.ExternalRecommenderRequest;
import com.freshdirect.smartstore.external.ExternalRecommenderType;
import com.freshdirect.smartstore.external.NoSuchExternalRecommenderException;
import com.freshdirect.smartstore.external.RecommendationItem;
import com.freshdirect.smartstore.impl.SessionCache;
import com.freshdirect.smartstore.scoring.DataAccess;
import com.freshdirect.smartstore.scoring.HelperFunctions;

/**
 * 
 * This class is a Singleton.
 * 
 * It implements a caching strategy for personalized scores while caches all global scores at the product level.
 * Some factors come from CMS, these are looked up.
 * 
 * The following is needed to extend the factors.
 * <ul>
 * 	<li>In case of a database factor, add a new instance of {@link FactorRangeConverter}</li>
 * 	<li>In case of a CMS factor, add a new instance of {@link StoreLookup}</li> 
 * </ul>
 * 
 * These go into the {@link #ScoreProvider() constructor}.
 * 
 * Only those factors will be calculated which are necessarily 
 * needed (i.e. {@link #acquireFactors(Collection) explicitly acquired}).
 * 
 * All global (acquired) factors are cached. The personalized (acquired) factors are 
 * stored in an {@link SessionCache LRU cache}.
 * 
 * @author istvan
 *
 */
public class ScoreProvider implements DataAccess {
	
    public static final String GLOBAL_POPULARITY = "Popularity";

    public static final String USER_FREQUENCY = "Frequency";
    
    public static final String RECENCY_NORMALIZED = "Recency_Normalized";
    
    public static final String RECENCY_DISCRETIZED = "Recency_Discretized";
    
    public static final String ORIGINAL_SCORES_GLOBAL = "OriginalScores_Global";

    public static final String ORIGINAL_SCORES_PERSONALIZED = "OriginalScores_Personalized";

    private static final String[] DATASOURCE_NAMES = new String[] { "FeaturedItems", "CandidateLists", "PurchaseHistory", "CustomerRatedItems" };
    
    private static final String EXTERNAL_PERSONALIZED_PREFIX = "Personalized_";

    public static final String[] ZONE_DEPENDENT_FACTORS_ARRAY = new String[] {
    		"DealsPercentage", "DealsPercentage_Discretized",
			"TieredDealsPercentage", "TieredDealsPercentage_Discretized",
			"HighestDealsPercentage", "HighestDealsPercentage_Discretized"
		};
    
    public static final Set<String> ZONE_DEPENDENT_FACTORS2;
    
    static {
    	ZONE_DEPENDENT_FACTORS2 = new HashSet<String>();
    	for (String factor : ZONE_DEPENDENT_FACTORS_ARRAY)
    		ZONE_DEPENDENT_FACTORS2.add(factor);
    }

    // LOGGER
	private static Category LOGGER = LoggerFactory.getInstance(ScoreProvider.class);
	
	/**
	 * Database factors.
	 */
	protected static abstract class DatabaseScoreRangeProvider implements ScoreRangeProvider {
	
		private Map<String,Number> factorIndexes = new HashMap<String,Number>();
		private List<String> factors;
		
		// products in a particular order
		private List<String> products = null;
		
		// double arrays in the same order as factorIndexes.values() 
		// thus factors
		// values in the same order as products
		// List<double[]>
		private List<double[]> values = null;
		
		
		@Override
		public void purge() {
			products = null;
			values = null;
		}
		
		protected List<String> getProductNames() {
			return products;
		}
		
		protected boolean inCache() {
			return products != null && values != null;
		}
	 
		/**
		 * 
		 * @param dbProductScores assumed to contain only the scores in good order Map<ProductId:String,Scores:double[]>
 		 */
		protected void reCache(Map<String,double[]> dbProductScores) {
			
			List<String> newProducts = new ArrayList<String>(dbProductScores.size());
			List<double[]> newValues = new ArrayList<double[]>(factorIndexes.size());

			for(int i = 0; i<  factorIndexes.size(); ++i) {
				newValues.add(new double[dbProductScores.size()]);
			}
			
				
			int r = 0;
			for(Iterator<Map.Entry<String,double[]>> i = dbProductScores.entrySet().iterator(); i.hasNext(); ++r) {
				Map.Entry<String,double[]> entry = i.next();
				newProducts.add(entry.getKey());
				int c = 0;
				for(Iterator<String> j = factors.iterator(); j.hasNext(); ++c) {
						
					String factor = j.next().toString();
					int idx = factorIndexes.get(factor).intValue();
					double[] scores = entry.getValue();
					double[] range = newValues.get(c);
					range[r] = scores[idx];
				}
			}
	
			synchronized (this) {
				products = newProducts;
				values = newValues;
			}
		}
		
		protected DatabaseScoreRangeProvider(List<String> factors) {
			this.factors = factors;
			int idx = 0;
			for(Iterator<String> i = factors.iterator(); i.hasNext();++idx) {
				factorIndexes.put(i.next(), new Integer(idx));
			}
		}
		
		protected List<String> getFactorNames() {
			return factors;
		}
		
		protected int getFactorIndex(String factor) {
			return factorIndexes.get(factor).intValue();
		}
		
		protected double[] getCachedRange(String factor) {
			return values.get(factorIndexes.get(factor).intValue());
		}
	}
	
	/**
	 * Global database factors.
	 */
	protected static class GlobalScoreRangeProvider extends DatabaseScoreRangeProvider {

		@Override
		public void cache(String userId) {
			if (!inCache()) {
				reCache(DatabaseScoreFactorProvider.getInstance().getGlobalFactors(getFactorNames()));
			}
		}
					
		protected GlobalScoreRangeProvider(List<String> factors) {
			super(factors);
		}
		

		@Override
		public List<String> products(String userId) {
			cache(userId);
			return getProductNames();
		}

		@Override
		public double[] getRange(String userId, String factor) {
			cache(userId);
			return getCachedRange(factor);
		}
	}
	
	/**
	 * Personalized database factors.
	 */
	protected static class PersonalizedScoreRangeProvider extends DatabaseScoreRangeProvider {
		
		private String userId = null;
		
		protected PersonalizedScoreRangeProvider(List<String> factors) {
			super(factors);
		}
		
		@Override
		public void cache(String userId) {
			if (!inCache() || this.userId == null || !this.userId.equals(userId)) {
				synchronized (this) {
					this.userId = userId;
					reCache(DatabaseScoreFactorProvider.getInstance().getPersonalizedFactors(this.userId,getFactorNames()));
				}
			}
		}
		
		@Override
		public void purge() {
			userId = null;
			super.purge();
		}

		@Override
		public double[] getRange(String userId, String factor) {
			cache(userId);
			return getCachedRange(factor);
		}
		
		public ScoreRangeProvider replicate() {
			return new PersonalizedScoreRangeProvider(getFactorNames());
		}

		@Override
		public List<String> products(String userId) {
			cache(userId);
			return getProductNames();
		}
	}
	
	/**
	 * Information for factors;
	 */
	protected class FactorInfo {
		private Comparator<String> lowerCaseComparator = new Comparator<String>() {

			public int compare(String s1, String s2) {
				return s1.toString().compareToIgnoreCase(s2.toString());
			}
		};
		
		private SortedSet<String> globalDBFactors = new TreeSet<String>(lowerCaseComparator);
		private SortedSet<String> personalizedDBFactors = new TreeSet<String>(lowerCaseComparator);
		
		private FactorInfo() {	
			reloadNames();
		}
		
		public synchronized void reloadNames() {
			globalDBFactors.clear();
			personalizedDBFactors.clear();
			DatabaseScoreFactorProvider dbProvider = DatabaseScoreFactorProvider.getInstance();
			globalDBFactors.addAll(dbProvider.getGlobalFactorNames());
			personalizedDBFactors.addAll(dbProvider.getPersonalizedFactorNames());			
		}

		public boolean isGlobal(String name) {
			return isOnline(name) || globalDBFactors.contains(name);
		}

		public boolean isOffline(String name) {
			return globalDBFactors.contains(name) || personalizedDBFactors.contains(name);
		}

		public boolean isPersonalized(String name) {
			return personalizedDBFactors.contains(name);
		}

		public boolean isOnline(String name) {
			return storeLookups.containsKey(name);
		}
	}
	
	
	private FactorInfo factorInfo = null;
	protected ScoreRangeProvider personalizedScoreRangeProvider;
	
	private static ScoreProvider instance = null;

       // Map<Factor:String,IndexInDoubleArray:Integer> score index
        // Score indexes tell what position the score is stored in globalScores or personalizesScores
        // in the double array
	protected Map<String, Integer> globalIndexes = new TreeMap<String, Integer>();
    protected Map<String, Integer> personalizedIndexes = new TreeMap<String, Integer>();
        
        
    // Map<ContentKey, double[]>
    protected Map<ContentKey, double[]> globalScores = new HashMap<ContentKey, double[]>();
    
    // SessionCache<UserId:String, SessionCache.TimedEntry<Map<ContentKey,double[]>>>
    private SessionCache<String, SessionCache.TimedEntry<Map<ContentKey,double[]>>> personalizedScores 
        = new SessionCache<String, SessionCache.TimedEntry<Map<ContentKey,double[]>>>(FDStoreProperties.getSmartstorePersonalizedScoresCacheEntries(),0.75f);
        

	/**
	 * Get instance.
	 * 
	 * @return the only thread local instance
	 */
	public static synchronized ScoreProvider getInstance() {
		if (instance == null) {
			instance = new ScoreProvider();
		}
		return instance;
	}
	
	/**
	 * Just for testing purposes, do not use!
	 * @param ins
	 */
	public static synchronized void setInstance(ScoreProvider ins) {
	    instance = ins;
	}
	
	
	/**
	 * Get available factors.
	 * 
	 * @return Set<String> name of the factors which have associated handlers
	 */
	public Set<String> getAvailableFactors() {
		Set<String> result = new HashSet<String>();
		result.addAll(storeLookups.keySet());
		result.addAll(rangeConverters.keySet());
		return result;
	}
	
	
	/**
	 * Get currently loaded factors.
	 * @return Set<String> name of the factors that are actually loaded
	 */
	public Set<String> getLoadedFactors() {
		Set<String> result = new HashSet<String>();
		result.addAll(loadedStoreLookups);
		result.addAll(personalizedIndexes.keySet());
		result.addAll(globalIndexes.keySet());
		return result;
	}
	
	/**
	 * Get currently loaded, non personalized factors. This is used in the scripting framework, to decide which generator can be cached.
	 * 
	 * @return
	 */
	public Set<String> getNonPersonalizedFactors() {
            Set<String> result = new HashSet<String>();
            result.addAll(storeLookups.keySet());
            for (Iterator<Map.Entry<String, FactorRangeConverter>> iter=rangeConverters.entrySet().iterator();iter.hasNext();) {
                Map.Entry<String, FactorRangeConverter> e = iter.next();
                FactorRangeConverter converter = e.getValue();
                if (!converter.isPersonalized()) {
                    String name = e.getKey();
                    result.add(name);
                }
            }
            return result;
	}
	
	protected Map<ContentKey,double[]> storePersonalizedScores(String userId) {
		SessionCache.TimedEntry<Map<ContentKey,double[]>> entry = personalizedScores.get(userId);
		if (entry == null || entry.expired()) {
			
			try {
				entry = new SessionCache.TimedEntry<Map<ContentKey,double[]>>(
						loadPersonalizedDBScores(userId),
						1000*FDStoreProperties.getSmartstorePersonalizedScoresCacheTimeout()
				);
			} catch (Exception e) {
				LOGGER.debug("Could not load personalized scores for " + userId, e);
				return Collections.emptyMap();
			}
			LOGGER.info("Caching personalized scores for " + userId);
			personalizedScores.put(userId,entry);
		}
		return entry.getPayload();
	}
	
	protected void purgePersonalizedScores(String userId) {
		personalizedScores.remove(userId);
	}
	
	protected double getPersonalizedScore(String userId, ContentKey key, int idx) {
		if (userId == null) {
			return 0;
		}
		Map<ContentKey,double[]> userScores = storePersonalizedScores(userId);
		double[] scores = userScores.get(key);
		return scores == null ? 0 : scores[idx];
	}
	
	protected double getGlobalScore(ContentKey key, int idx) {
		double[] scores = globalScores.get(key);
		return scores == null ? 0 : scores[idx];
	}
	
	/**
	 * Get the scores for the requested content key.
	 * 
	 * @param userId customer id, may be null for non-personalized factors
	 * @param contentKey 
	 * @param variables requested variables
	 * @return scores in the order of variables
	 */
	public double[] getVariables(String userId, PricingContext pricingContext, ContentKey contentKey, String[] variables) {
		return getVariables(userId, pricingContext, ContentFactory.getInstance().getContentNodeByKey(contentKey),variables);
	}
	
	/**
	 * Get the scores for the requested content node.
	 * 
	 * @param userId customerId, may be null for non-personalized factors
	 * @param contentNode 
	 * @param variables requested variables
	 * @return scores in the order of variables  
	 */
	@Override
	public double[] getVariables(String userId, PricingContext pricingContext, ContentNodeModel contentNode, String[] variables) {
		double[] result = new double[variables.length];
		
		for(int i = 0; i < variables.length; ++i) {
			String var = variables[i];
			if (contentNode == null) {
				result[i] = 0;
			} else if (personalizedIndexes.containsKey(var)) { // personalized factor
				result[i] = getPersonalizedScore(userId,contentNode.getContentKey(),personalizedIndexes.get(var).intValue());
			} else if (globalIndexes.containsKey(var)) {
				result[i] = getGlobalScore(contentNode.getContentKey(),globalIndexes.get(var).intValue());
			} else if (storeLookups.containsKey(var)) {
				result[i] = (storeLookups.get(var)).getVariable(contentNode, pricingContext);
			} else if (var.startsWith(EXTERNAL_PERSONALIZED_PREFIX)) {
				String providerName = var.substring(EXTERNAL_PERSONALIZED_PREFIX.length());
				try {
					ExternalRecommender recommender = ExternalRecommenderRegistry.getInstance(providerName, ExternalRecommenderType.PERSONALIZED);
					List<RecommendationItem> items = recommender.recommendItems(new ExternalRecommenderRequest(userId));
					result[i] = items.size() - items.indexOf(new RecommendationItem(contentNode.getContentKey().getId()));
				} catch (ExternalRecommenderCommunicationException e) {
					LOGGER.debug("Error while communicating with external recommender (" + var + ")", e);
					result[i] = 0;
				} catch (IllegalArgumentException e) {
					LOGGER.debug("Illegal argument passed to external recommender registry (" + var + ")", e);
					result[i] = 0;
				} catch (NoSuchExternalRecommenderException e) {
					LOGGER.debug("No such external recommender registered (" + var + ")", e);
					result[i] = 0;
				}
			} else {
				LOGGER.debug("Unknown variable " + var);
				result[i] = 0;
			}
		}
		return result;	
	}
	
	/**
	 * Test if factor is global.
	 * 
	 * @param factor factor name
	 * @return whether factor is global
	 */
	public boolean isGlobal(String factor) {
		if (storeLookups.containsKey(factor)) return true;
		try {
			return !(rangeConverters.get(factor)).isPersonalized();
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	public boolean isStoreLookup(String factor) {
		return storeLookups.containsKey(factor);
	}
	
	/**
	 * Test if factor is personalized.
	 * @param factor factor name.
	 * @return whether factor is personalized
	 */
	public boolean isPersonalized(String factor) {
		try {
			return (rangeConverters.get(factor)).isPersonalized();
		} catch (NullPointerException e) {
			return false;
		}
	}

	/**
	 * Get content associated with data source.
	 * 
	 * @param name of data source
	 * @param input session input
	 * @return List<{@link ContentNodeModel>}
	 */
	@Override
	public List<? extends ContentNodeModel> fetchContentNodes(SessionInput input, String name) {
		List<? extends ContentNodeModel> nodez = _fetchContentNodes(input, name);

		if (input.isTraceMode()) {
			input.traceContentNodes(name, nodez);
		}

		return nodez;
	}

	protected List<? extends ContentNodeModel> _fetchContentNodes(SessionInput input, String name) {
		ProductContainer category = input.getFICategory();
		if ("FeaturedItems".equals(name)) {
			if (category != null) {
				return HelperFunctions.getFeaturedItems(category);
			}
		} else if ("CandidateLists".equals(name)) {
			if (category != null) {
				return HelperFunctions.getCandidateLists(category);
			}
		} else if ("PurchaseHistory".equals(name)) {
			if (input.getCustomerId() != null) {
				try {
					Map<ContentKey,double[]> scores = storePersonalizedScores(input.getCustomerId());
					List<ContentNodeModel> result = new ArrayList<ContentNodeModel>(scores.size());
					for(Iterator<ContentKey> i = scores.keySet().iterator(); i.hasNext();) {
						ContentKey key = i.next();
						try {
						    ContentNodeModel nodeModel = ContentFactory.getInstance().getContentNodeByKey(key);
						    if (nodeModel!=null) {
						        result.add(nodeModel);
						    }
						} catch (Exception e) {
							LOGGER.debug("Problem with " + key,e);
						}
					}
					return result;
				} catch (Exception e) {
					LOGGER.debug("Could not load history for " + input.getCustomerId());
				}
			}
	    } else if ("CustomerRatedItems".equals(name)) {
	    	return HelperFunctions.getCustomerRatedProducts();
	    }
	    return Collections.emptyList();
	}
	
        /**
         * Get user specific product scores.
         * 
         * The higher the score, the more the user likes.
         * @return Map<{@link ContentKey},{@link Float}> productId->Score, never null
         */
        public Map<ContentKey,Float> getUserProductScores(String erpCustomerId) {

        	if (erpCustomerId == null) {
        		return Collections.emptyMap();
        	}

            Map<ContentKey,double[]> scores = storePersonalizedScores(erpCustomerId);
            
            if (scores != null && !scores.isEmpty()) {
                Number position = (personalizedIndexes.get(ORIGINAL_SCORES_PERSONALIZED));
                if (position != null) {
                    int value = position.intValue();
                    Map<ContentKey,Float> originalScores = new HashMap<ContentKey,Float>();
                    for (Iterator<Map.Entry<ContentKey,double[]>> iter = scores.entrySet().iterator();iter.hasNext();) {
                        Map.Entry<ContentKey,double[]> entry = iter.next();
                        double[] values = entry.getValue();
                        originalScores.put( entry.getKey(), new Float(values[value]));
                    }
                    return originalScores;
                }
            }
            return Collections.emptyMap();
        }

        /**
         * return true, if the user has score for that given content key.
         * 
         * @param erpCustomerId
         * @param key
         * @return
         */
        public boolean isUserHasScore(String erpCustomerId, ContentKey key) {
            Map<ContentKey,double[]> scores = storePersonalizedScores(erpCustomerId);
            if (scores != null && !scores.isEmpty()) {
                return scores.get(key)!=null;
            }
            return false;
        }

        /**
         * Return the original personalized score for a customer.
         * 
         * @param erpCustomerId
         * @param key
         * @return
         */
        public Float getUserProductScore(String erpCustomerId, ContentKey key) {
        	if (erpCustomerId == null) {
        		return null;
        	}
            Map<ContentKey,double[]> scores = storePersonalizedScores(erpCustomerId);
            
            if (scores != null && !scores.isEmpty()) {
                Number position = (personalizedIndexes.get(ORIGINAL_SCORES_PERSONALIZED));
                if (position != null) {
                    int value = position.intValue();
                    double[] values = scores.get(key);
                    if(values!=null){
                    	return new Float(values[value]);                    	
                    }
                }
            }
            return null;
        }
        
	public String[] getDatasourceNames() {
	    return DATASOURCE_NAMES;
	}
	
	
	/**
	 * Acquire all available factors.
	 * 
	 * @return actually acquired factors
	 */
	public List<String> acquireAllFactors() {
		return acquireFactors(getAvailableFactors());
	}
	
	/**
	 * Acquire the given factors.
	 * 
	 * All of the following will happen:
	 * <ol>
	 * 	<li>The database columns will be read</li>
	 * 	<li>All global factors will be (re)cached</li>
	 * 	<li>Available personalized factors will be validated</li>
	 * </ol>
	 *   
	 * @param requestedFactors Collection<{@link String}>
	 * @return the names of actually acquired factors
	 */
	public synchronized List<String> acquireFactors(Collection<String> names) {
		
		Set<String> requestedFactors = new HashSet<String>(names);
	    requestedFactors.add(ORIGINAL_SCORES_GLOBAL);
	    requestedFactors.add(ORIGINAL_SCORES_PERSONALIZED);
	    requestedFactors.add("Newness");
	    requestedFactors.add("QualityRating_Discretized1");
	    	    
	    // extra factors can be parameterized
	    requestedFactors.addAll(FDStoreProperties.getSmartstorePreloadFactors());
	    
	    Set<String> newFactors = new HashSet<String>(requestedFactors);
	    Set<String> loadedFactors = getLoadedFactors();
		LOGGER.debug("loaded factors " + loadedFactors);
	    newFactors.removeAll(loadedFactors);
	    requestedFactors.addAll(loadedFactors);
	    if (newFactors.isEmpty()) {
	    	LOGGER.info("no new factors needed, factors will not be reloaded");
	    	return new ArrayList<String>(requestedFactors);
	    }
	    LOGGER.info("found new factors " + newFactors + ", reloading factors");
	    
	    factorInfo.reloadNames();
	
		Set<String> personalizedFactors = new HashSet<String>();

		Set<String> globalDBFactors = new HashSet<String>();
		
		Set<String> rawPersonalizedFactors = new HashSet<String>();
		Set<String> rawGlobalFactors = new HashSet<String>();
		
		
		List<String> result = new ArrayList<String>();
		
		loadedStoreLookups.clear();

		NEXT_FACTOR: for(Iterator<String> i = requestedFactors.iterator(); i.hasNext();) {
			String name = i.next();
			if (rangeConverters.containsKey(name)) {
				FactorRangeConverter converter = rangeConverters.get(name);
				for (Iterator<String> j = converter.requiresGlobalDatabaseColumns().iterator(); j.hasNext();) {
					String column = j.next();
					if (!factorInfo.isOffline(column) || !factorInfo.isGlobal(column)) {
						LOGGER.warn("No column " + column + " in global factors");
						continue NEXT_FACTOR;
					}
					rawGlobalFactors.add(column);
				}
				for(Iterator<String> j = converter.requiresPersonalizedDatabaseColumns().iterator(); j.hasNext();) {
					String column = j.next();
					if (!factorInfo.isOffline(column) && !factorInfo.isPersonalized(column)) {
						LOGGER.warn("No column " + column + " in personalized factors");
						continue NEXT_FACTOR;
					}
					rawPersonalizedFactors.add(column);
				}
				if (converter.isPersonalized()) {
					personalizedFactors.add(name);
				} else {
					globalDBFactors.add(name);
				}
				result.add(name);
			} else if (storeLookups.containsKey(name)) {
				StoreLookup lookup = storeLookups.get(name);
				lookup.reloadCache();
				loadedStoreLookups.add(name);
				result.add(name);
			} else if (name.startsWith(EXTERNAL_PERSONALIZED_PREFIX)) {
				String providerName = name.substring(EXTERNAL_PERSONALIZED_PREFIX.length());
				try {
					ExternalRecommender recommender = ExternalRecommenderRegistry.getInstance(providerName, ExternalRecommenderType.PERSONALIZED);
					if (recommender == null)
						LOGGER.warn("Unknown factor");
				} catch (IllegalArgumentException e) {
					LOGGER.warn("No such factor - Illegal argument passed to external recommender registry (" + providerName + ")", e);
				} catch (NoSuchExternalRecommenderException e) {
					LOGGER.warn("No such factor - No such external recommender registered (" + providerName + ")", e);
				}
			} else {
				LOGGER.warn("Unknown factor");
			}
		}
		
		GlobalScoreRangeProvider globalScoreRangeProvider = new GlobalScoreRangeProvider(new ArrayList<String>(rawGlobalFactors));
		personalizedScoreRangeProvider = new PersonalizedScoreRangeProvider(new ArrayList<String>(rawPersonalizedFactors));
		
		personalizedIndexes.clear();
		for(Iterator<String> i = personalizedFactors.iterator(); i.hasNext();) {
			String factor = i.next();
			personalizedIndexes.put(factor,new Integer(personalizedIndexes.size()));
		}
		
		globalIndexes.clear();
		for(Iterator<String> i = globalDBFactors.iterator(); i.hasNext();) {
			String factor = i.next();
			globalIndexes.put(factor, new Integer(globalIndexes.size()));
		}
		
		personalizedScores.clear();
		globalScores = null;
		globalScores = new HashMap<ContentKey, double[]>();
		
		try {
			globalScores = loadGlobalDBScores(globalScoreRangeProvider);
			LOGGER.info("Caching global scores");
		} catch (Exception e) {
			LOGGER.debug("Could not cache global scores");
			e.printStackTrace();
			throw new FDRuntimeException(e);
		}	
		
		return result;
	}
	
	private Map<ContentKey, double[]> loadGlobalDBScores(GlobalScoreRangeProvider globalScoreRangeProvider) throws Exception {
		
		
		List<String> products = globalScoreRangeProvider.products(null);
		
		Map<ContentKey, double[]> result = new HashMap<ContentKey, double[]>(5*products.size()/3+1,0.75f);
		
		for(Iterator<String> i = products.iterator(); i.hasNext();) {
			result.put(new ContentKey(FDContentTypes.PRODUCT,i.next().toString()), new double[globalIndexes.size()]);
		}
		
		for (Iterator<Map.Entry<String, Integer>> i = globalIndexes.entrySet().iterator(); i.hasNext();) {
			// Map.Entry<String,Number>
			Map.Entry<String, Integer> entry = i.next();
			FactorRangeConverter converter = rangeConverters.get(entry.getKey());
			double[] values = converter.map(null,globalScoreRangeProvider);
			
			storeScores(products, result, entry, values);
		}
		globalScoreRangeProvider.purge();
		return result;
	}
	
	
	private Map<ContentKey,double[]> loadPersonalizedDBScores(String erpCustomerId) throws Exception {
		
		if(personalizedScoreRangeProvider != null) {
			ScoreRangeProvider personalScores = ((PersonalizedScoreRangeProvider)personalizedScoreRangeProvider).replicate();
			
			List<String> products = personalScores.products(erpCustomerId);
			
			Map<ContentKey,double[]> result = new HashMap<ContentKey,double[]>(5*products.size()/3+1,0.75f);
			
			for(Iterator<String> i = products.iterator(); i.hasNext();) {
				result.put(new ContentKey(FDContentTypes.PRODUCT,i.next().toString()), new double[personalizedIndexes.size()]);
			}
			
			for (Iterator<Map.Entry<String, Integer>> i = personalizedIndexes.entrySet().iterator(); i.hasNext();) {
				// Map.Entry<String,Number>
				Map.Entry<String, Integer> entry = i.next();
				FactorRangeConverter converter = rangeConverters.get(entry.getKey().toString());
				double[] values = converter.map(erpCustomerId,personalScores);
				
				storeScores(products, result, entry, values);
			}
			return result;
		}
		return null;
	}

    private void storeScores(List<String> products, Map<ContentKey,double[]> result, Map.Entry<String, Integer> entry, double[] values) {
        if (values.length != products.size()) {
        	throw new FDRuntimeException(
        		"Product list length and range values size differ: " + values.length + " and " + products.size());
        }
        for(int j=0; j< values.length; ++j) {
        	double[] productScores = result.get(new ContentKey(FDContentTypes.PRODUCT,products.get(j).toString()));
        	productScores[((Number)entry.getValue()).intValue()] = values[j];
        }
    }
	
	/**
	 * Get all scores for the given users.
	 * 
	 * @param userIds customer id, if null, all available global scores are returned
	 * @return scores in a table
	 * @throws Exception
	 */
	public ScoresTable getAllScores(List<String> userIds, ZoneInfo zoneInfo) throws Exception {
		
		
		if (userIds != null) { // personalized scores
			final String[] factors = new String[personalizedIndexes.size() + globalIndexes.size() + storeLookups.size()];
			
			int j = 0;
			for(Iterator<String> i = personalizedIndexes.keySet().iterator(); i.hasNext();++j) {
				String factor = i.next().toString();
				factors[j] = factor;
			}
			
			for(Iterator<String> i = globalIndexes.keySet().iterator(); i.hasNext(); ++j) {
				String factor = i.next().toString();
				factors[j] = factor;
				
			}
			
			for(Iterator<String> i = storeLookups.keySet().iterator(); i.hasNext();++j) {
				String factor = i.next().toString();
				factors[j] = factor;
			}
			
			@SuppressWarnings("rawtypes")
			final List<List<Comparable>> values = new ArrayList<List<Comparable>>();
			
			for (String userId : userIds) {
				FDUserI fdUser = FDCustomerManager.getFDUser(new FDIdentity(userId));
				PricingContext pricingCtx = fdUser.getUserContext().getPricingContext();
				Set<String> productIds = DatabaseScoreFactorProvider.getInstance().getPersonalizedProducts(userId);
				
				for (String productId : productIds) {
					List<Comparable> row = new ArrayList<Comparable>(factors.length + 2);
					row.add(userId);
					row.add(productId);
					
					double[] scores = getVariables(userId, pricingCtx, new ContentKey(FDContentTypes.PRODUCT, productId), factors);
					for(int s = 0; s < scores.length; ++s) {
						row.add(new Double(scores[s]));
					}
					values.add(row);
				}
			}
			
			return new ScoresTable() {				
				
				private static final long serialVersionUID = 4253324339031347406L;

				protected void init() {
					addColumn("CUSTOMER_ID", String.class);
					addColumn("PRODUCT_ID", ContentKey.class);
					
					for(int i = 0; i < factors.length; ++i) {
						addColumn(factors[i], Number.class);
					}
				}

				@SuppressWarnings("rawtypes")
				public Iterator<List<Comparable>> getRows() {
					return values.iterator();
				}
			};
		} else {
			final String[] factors = new String[globalIndexes.size() + storeLookups.size()];
			PricingContext pricingCtx = zoneInfo != null  ? new PricingContext(zoneInfo) : PricingContext.DEFAULT;
			
			int j = 0;
			
			for(Iterator<String> i = globalIndexes.keySet().iterator(); i.hasNext(); ++j) {
				String factor = i.next().toString();
				factors[j] = factor;
				
			}
			
			for(Iterator<String> i = storeLookups.keySet().iterator(); i.hasNext();++j) {
				String factor = i.next().toString();
				factors[j] = factor;
			}
			
			@SuppressWarnings("rawtypes")
			final List<List<Comparable>> values = new ArrayList<List<Comparable>>();
			
			Set<String> productIds = DatabaseScoreFactorProvider.getInstance().getGlobalProducts();
			
			for (String productId : productIds) {
				@SuppressWarnings("rawtypes")
				List<Comparable> row = new ArrayList<Comparable>(factors.length + 1);
				row.add(productId);
				
				double[] scores = getVariables(null, pricingCtx, new ContentKey(FDContentTypes.PRODUCT, productId), factors);
				for(int s = 0; s < scores.length; ++s) {
					row.add(new Double(scores[s]));
				}
				values.add(row);
			}
			
			return new ScoresTable() {

				private static final long serialVersionUID = -3700648053073552014L;
				
				protected void init() {
					addColumn("PRODUCT_ID", ContentKey.class);
					
					for(int i = 0; i < factors.length; ++i) {
						addColumn(factors[i], Number.class);
					}
				}

				@SuppressWarnings("rawtypes")
				public Iterator<List<Comparable>> getRows() {
					return values.iterator();
				}
				
			};
		}
		
	}
	
	
	// Map<Factor:String,FactorRangeConverter>
	private Map<String, FactorRangeConverter> rangeConverters = new HashMap<String, FactorRangeConverter>();
	
	// Map<Factor:String,StoreLookup>
	private Map<String, StoreLookup> storeLookups = new HashMap<String, StoreLookup> ();
	
	private Set<String> loadedStoreLookups = new HashSet<String>();
	
	/**
	 * Get all cached users.
	 * @return Set<CustomerId:String>
	 */
	public Set<String> getCachedCustomers() {
		return personalizedScores.keySet();
	}
	
	protected ScoreProvider(boolean init) {
    	if (init) {
			LOGGER.info("Personalized cache entries: " + FDStoreProperties.getSmartstorePersonalizedScoresCacheEntries());
			LOGGER.info("Personalized cache timeout (seconds): " + FDStoreProperties.getSmartstorePersonalizedScoresCacheTimeout());
			
			factorInfo = new FactorInfo();
			
			reloadFactorHandlers();
	    }
	}
	
	protected ScoreProvider() {
	    this(true);
	}
	
	
	
	private void reloadFactorHandlers() {
		
		globalScores.clear();
		personalizedScores.clear();
		
		// Store lookups
		storeLookups.put(
			"DealsPercentage", 
			FactorUtil.getDealsPercentageLookup()
		);
		
		storeLookups.put(
			"DealsPercentage_Discretized",
			FactorUtil.getDealsPercentageDiscretized()
		);

		storeLookups.put(
			"TieredDealsPercentage", 
			FactorUtil.getTieredDealsPercentageLookup()
		);
		
		storeLookups.put(
			"TieredDealsPercentage_Discretized",
			FactorUtil.getTieredDealsPercentageDiscretized()
		);
		
		storeLookups.put(
			"HighestDealsPercentage", 
			FactorUtil.getHighestDealsPercentageLookup()
		);
			
		storeLookups.put(
			"HighestDealsPercentage_Discretized",
			FactorUtil.getHighestDealsPercentageDiscretized()
		);
			
		storeLookups.put(
			"ExpertWeight",
			FactorUtil.getExpertWeightLookup()
		);
		
		storeLookups.put(
			"ExpertWeight_Normalized",
			FactorUtil.getNormalizedExpertWeightLookup()
		);
		
		storeLookups.put(
			"CustomerRating",
			FactorUtil.getCustomerRatingLookup()
		);
		
		storeLookups.put(
			"CustomerRating_Normalized",
			FactorUtil.getNormalizedCustomerRatingLookup()
		);

		storeLookups.put(
			"QualityRating",
			FactorUtil.getProduceRatingLookup()
		);
		
		storeLookups.put(
			"QualityRating_Normalized",
			FactorUtil.getNormalizedProduceRatingLookup()
		);
		
		storeLookups.put(
			"QualityRating_Discretized1",
			FactorUtil.getDescretizedProduceRatingLookup1()
		);
		
		storeLookups.put(
			"QualityRating_Discretized2",
			FactorUtil.getDescretizedProduceRatingLookup2()
		);

		storeLookups.put(
			"Newness",
			FactorUtil.getNewnessLookup()
		);

		storeLookups.put(
			"BackInStock",
			FactorUtil.getBackInStockLookup()
		);
		// Database scores
		
		
		// FREQUENCY 
		rangeConverters.put(
		        USER_FREQUENCY,
			FactorRangeConverter.getRawPersonalizedScores(FactorUtil.PERSONALIZED_FREQUENCY_COLUMN)
		);
		
		rangeConverters.put(
			"Frequency_Normalized",
			FactorUtil.getMaxNormalizedPersonalConverter(FactorUtil.PERSONALIZED_FREQUENCY_COLUMN)
		);
			
		rangeConverters.put(
			"Frequency_Discretized",
			FactorUtil.getLogDiscretizedPersonalConverter(FactorUtil.PERSONALIZED_FREQUENCY_COLUMN, 2)
		);
		
		
		// GLOBAL POPULARITY
		rangeConverters.put(
		        GLOBAL_POPULARITY,
			FactorRangeConverter.getRawGlobalScores(FactorUtil.GLOBAL_POPULARITY_COLUMN)
		);
			
		rangeConverters.put(
			"Popularity_Normalized",
			FactorUtil.getMaxNormalizedGlobalConvereter(FactorUtil.GLOBAL_POPULARITY_COLUMN)
		);
		
		rangeConverters.put(
			"Popularity_NormalizedDepartment",
			FactorUtil.getDepartmentMaxNormalizedGlobalConverter(FactorUtil.GLOBAL_POPULARITY_COLUMN)
		);
		
		rangeConverters.put(
			"Popularity_Discretized",
			FactorUtil.getLogDiscretizedGlobalConverter(FactorUtil.GLOBAL_POPULARITY_COLUMN, 2)
		);

		// SHORT TERM POPULARITY
		rangeConverters.put(
		        "Popularity8W",
			FactorRangeConverter.getRawGlobalScores(FactorUtil.GLOBAL_POPULARITY_8W_COLUMN)
		);
			
		rangeConverters.put(
			"Popularity8W_Normalized",
			FactorUtil.getMaxNormalizedGlobalConvereter(FactorUtil.GLOBAL_POPULARITY_8W_COLUMN)
		);
		
		rangeConverters.put(
			"Popularity8W_NormalizedDepartment",
			FactorUtil.getDepartmentMaxNormalizedGlobalConverter(FactorUtil.GLOBAL_POPULARITY_8W_COLUMN)
		);
		
		rangeConverters.put(
			"Popularity8W_Discretized",
			FactorUtil.getLogDiscretizedGlobalConverter(FactorUtil.GLOBAL_POPULARITY_8W_COLUMN, 2)
		);
		
		
		// QUANTITY
		rangeConverters.put(
			"Quantity",
			FactorRangeConverter.getRawPersonalizedScores(FactorUtil.PERSONALIZED_QUANTITY_COLUMN)
		);
		
		rangeConverters.put(
			"Quantity_Normalized",
			FactorUtil.getMaxNormalizedPersonalConverter(FactorUtil.PERSONALIZED_QUANTITY_COLUMN)
		);
		
		
		rangeConverters.put(
			"Quantity_Discretized",
			FactorUtil.getLogDiscretizedPersonalConverter(FactorUtil.PERSONALIZED_QUANTITY_COLUMN, 2)
		);
		
		// AMOUNT SPENT
		rangeConverters.put(
			"AmountSpent",
			FactorRangeConverter.getRawPersonalizedScores(FactorUtil.PERSONALIZED_AMOUNT_COLUMN)
		);
		
		rangeConverters.put(
				"AmountSpent_Normalized",
				FactorUtil.getMaxNormalizedPersonalConverter(FactorUtil.PERSONALIZED_AMOUNT_COLUMN)
			);
		
		rangeConverters.put(
			"AmountSpent_Discretized",
			FactorUtil.getLogDiscretizedPersonalConverter(FactorUtil.PERSONALIZED_AMOUNT_COLUMN, 2)
		);
		
		// RECENCY
		rangeConverters.put(
			"Recency",
			FactorRangeConverter.getRawPersonalizedScores(FactorUtil.PERSONALIZED_RECENT_FREQUENCY_COLUMN)
		);
		
		rangeConverters.put(
			"Recency_Normalized",
			FactorUtil.getMaxNormalizedPersonalConverter(FactorUtil.PERSONALIZED_RECENT_FREQUENCY_COLUMN)
		);
		
		rangeConverters.put(
			"Recency_Discretized",
			FactorUtil.getLogDiscretizedPersonalConverter(FactorUtil.PERSONALIZED_RECENT_FREQUENCY_COLUMN, 3)
		);
			
		
		// REORDER RATE	
		rangeConverters.put(
			"ReorderRate",
			FactorUtil.getReorderRateConverter(66)
		);
		
		rangeConverters.put(
			"ReorderRate_Normalized",
			FactorUtil.getNormalizedReorderRateConverter(66)
		);
		
		rangeConverters.put(
			"ReorderRate_DepartmentNormalized",
			FactorUtil.getDepartmentNormalizedReorderRateConverter(66)
		);
		
		rangeConverters.put(
			"ReorderRate_Discretized",
			FactorUtil.getDiscretizedReorderRateConverter(2)
		);
		
		// SEASONALITY
		rangeConverters.put(
			"Seasonality",
			FactorUtil.getSeasonalityConverter(3.)
		);
		
		rangeConverters.put(
			"Seasonality_Discretized",
			FactorUtil.getDiscretizedSeasonality()
		);
		
		rangeConverters.put("ProfitMargin",
		        FactorRangeConverter.getRawGlobalScores("GROSS_PROFIT_PCTG"));
		
		// ORIGINAL SCORES
		rangeConverters.put(
			ORIGINAL_SCORES_PERSONALIZED,
			FactorRangeConverter.getRawPersonalizedScores("Score")
		);
		
		rangeConverters.put(
			ORIGINAL_SCORES_GLOBAL,
			FactorRangeConverter.getRawGlobalScores("Score")
		);
	}

	@Override
	public boolean addPrioritizedNode(ContentNodeModel model) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<? extends ContentNodeModel> getPrioritizedNodes() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addPosteriorNode(ContentNodeModel model) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<? extends ContentNodeModel> getPosteriorNodes() {
		throw new UnsupportedOperationException();
	}
}
