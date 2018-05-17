package com.freshdirect.smartstore.fdstore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.EnumOrderLineRating;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.customerrating.CustomerRatingsContext;
import com.freshdirect.fdstore.content.customerrating.CustomerRatingsDTO;
import com.freshdirect.framework.util.log.LoggerFactory;


/**
 * Custom {@link FactorRangeConverter} and {@link StoreLookup} implementations.
 * 
 * @author istvan
 *
 */
public class FactorUtil {
    
    final static Logger LOG = LoggerFactory.getInstance(FactorUtil.class);
    
	// GLOBAL FACTORS columns, when moved to JDK 1.5+ these should be enums
	public static String GLOBAL_POPULARITY_COLUMN = "POPULARITY";
	public static String GLOBAL_REORDER_BUYER_COUNT_COLUMN = "REORDERBUYERCOUNT";
	public static String GLOBAL_TOTAL_BUYER_COUNT_COLUMN = "TOTALBUYERCOUNT";
	public static String GLOBAL_WEEKLY_FREQUENCY_COLUMN = "WEEKLYFREQUENCY";
	public static String GLOBAL_AVERAGE_FREQUENCY_COLUMN = "AVERAGEFREQUENCY";
	public static String GLOBAL_POPULARITY_8W_COLUMN = "POPULARITY_8W";
	
	// PERSONALIZED FACTORS columns, when moved to JDK 1.5+ these should be enums
	public static String PERSONALIZED_FREQUENCY_COLUMN = "FREQUENCY";
	public static String PERSONALIZED_RECENT_FREQUENCY_COLUMN = "RECENT_FREQUENCY";
	public static String PERSONALIZED_QUANTITY_COLUMN = "QUANTITY";
	public static String PERSONALIZED_AMOUNT_COLUMN = "AMOUNT";
	
		
    private static class ProduceRatingCache implements StoreLookup {
        
        public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
            try {
                if (contentNode instanceof ProductModel) {
                    EnumOrderLineRating e;
                    e = ((ProductModel) contentNode).getProductRatingEnum();
                    return e.getQualityRating();
                }
                return 0;
            } catch (FDResourceException e) {
                LOG.info("Error looking up " + contentNode + " in " + pricingContext, e);
            }
            return 0;
        }
        
        @Override
        public void reloadCache() {
            
        }
        
    }
	
	private static StoreLookup produceRatingCache = new ProduceRatingCache();
	
	/**
	 * Get a CSM lookup which returns "Deals Percentage".
	 * 
	 * @return StoreLookup
	 */
	public static StoreLookup getDealsPercentageLookup() {
		return new StoreLookup() {
			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return AllDealsCache.getInstance().getRegularDeal(contentNode.getContentKey(), pricingContext) / 100.0;
			}

			@Override
			public void reloadCache() {
				AllDealsCache.getInstance().reload();
			}	
		};
	}
	
	public static StoreLookup getDealsPercentageDiscretized() {
		return new StoreLookup() {
			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return Math.floor(AllDealsCache.getInstance().getRegularDeal(contentNode.getContentKey(), pricingContext) / 5.0);
			}

			@Override
			public void reloadCache() {
				AllDealsCache.getInstance().reload();
			}	
		};
	}

	/**
	 * Get a CSM lookup which returns "Tiered Deals Percentage".
	 * 
	 * @return StoreLookup
	 */
	public static StoreLookup getTieredDealsPercentageLookup() {
		return new StoreLookup() {
			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return AllDealsCache.getInstance().getTieredDeal(contentNode.getContentKey(), pricingContext) / 100.0;
			}	

			@Override
			public void reloadCache() {
				AllDealsCache.getInstance().reload();
			}	
		};
	}
	
	public static StoreLookup getTieredDealsPercentageDiscretized() {
		return new StoreLookup() {
			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return Math.floor(AllDealsCache.getInstance().getTieredDeal(contentNode.getContentKey(), pricingContext) / 5.0);
			}

			@Override
			public void reloadCache() {
				AllDealsCache.getInstance().reload();
			}	
		};
	}
	
	/**
	 * Get a CSM lookup which returns "Highest Deals Percentage".
	 * 
	 * @return StoreLookup
	 */
	public static StoreLookup getHighestDealsPercentageLookup() {
		return new StoreLookup() {
			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return AllDealsCache.getInstance().getHighestDeal(contentNode.getContentKey(), pricingContext) / 100.0;
			}	

			@Override
			public void reloadCache() {
				AllDealsCache.getInstance().reload();
			}	
		};
	}
	
	public static StoreLookup getHighestDealsPercentageDiscretized() {
		return new StoreLookup() {
			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return Math.floor(AllDealsCache.getInstance().getHighestDeal(contentNode.getContentKey(), pricingContext) / 5.0);
			}

			@Override
			public void reloadCache() {
				AllDealsCache.getInstance().reload();
			}	
		};
	}
	
	/**
	 * Get CMS lookup which returns "Expert Weight".
	 * @return StoreLookup
	 */
	public static StoreLookup getExpertWeightLookup() {
		return new StoreLookup() {

			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return contentNode instanceof ProductModel ? 
						((ProductModel)contentNode).getExpertWeight() : 0;
			}
			
			@Override
			public void reloadCache() {			                
			}
		};
	}
	
	/**
	 * Get CMS lookup which returns "Expert Weight" on a 0 - 1 scale.
	 * @return StoreLookup
	 */
	public static StoreLookup getNormalizedExpertWeightLookup() {
		return new StoreLookup() {
		        @Override
			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				int ew = 
					contentNode instanceof ProductModel ?
						((ProductModel)contentNode).getExpertWeight() : 0;
				switch(ew) {
					case -5: return 0;
					case -4: return 0.1;
					case -3: return 0.2;
					case -2: return 0.3;
					case -1: return 0.4;
					case 0: return 0.5;
					case 1: return 0.6;
					case 2: return 0.7;
					case 3: return 0.8;
					case 4: return 0.9;
					case 5: return 1;
					default:
						return 0.5;
				}
			}
			@Override
			public void reloadCache() {
			                
			}
		};
	}
	
	
	
	public static StoreLookup getCustomerRatingLookup() {
		return new StoreLookup() {
			@Override
			public void reloadCache() {}

			/**
			 * It should return a number between 0 and 5
			 */
			@Override
			public double getVariable(ContentNodeModel contentNode,
					PricingContext pricingContext) {
				if (!(contentNode instanceof ProductModel))
					return 0.0;

				ProductModel prd = (ProductModel) contentNode;
				CustomerRatingsDTO entity = CustomerRatingsContext
						.getInstance().getCustomerRatingByProductId(
								prd.getContentKey().getId());
				if (entity == null)
					return 0.0;

				BigDecimal rating = entity.getAverageOverallRating();
				return rating != null ? rating.doubleValue() : 0.0;
			}
		};
	}



	public static StoreLookup getNormalizedCustomerRatingLookup() {
		return new StoreLookup() {
			@Override
			public void reloadCache() {}

			@Override
			public double getVariable(ContentNodeModel contentNode,
					PricingContext pricingContext) {
				if (!(contentNode instanceof ProductModel))
					return 0;

				ProductModel prd = (ProductModel) contentNode;
				CustomerRatingsDTO entity = CustomerRatingsContext
						.getInstance().getCustomerRatingByProductId(
								prd.getContentKey().getId());

				if ( entity == null ) {
					return 0.0;
				}
				BigDecimal rating = entity.getAverageOverallRating();
				return rating != null ? rating.doubleValue()/5 : 0.0;
			}
		};
	}



	public static StoreLookup getProduceRatingLookup() {
		return new CachingStoreLookup(produceRatingCache) {
			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return super.getVariable(contentNode, pricingContext);
			}
		};
	}
	
	public static StoreLookup getNormalizedProduceRatingLookup() {
		return new CachingStoreLookup(produceRatingCache) {		
			//   0,    1,    2,    3,    4,   5,   6,    7,   8,   9,  10,  11,  12,  13,  14,  15
			private double[] scores = 
				{0, -0.8, -0.6, -0.4, -0.2, 0.0,  0.0, 0.0, 0.2, 0.4, 0.6, 0.6, 0.8, 0.8, 1.0, 1.0};

			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return scores[(int) super.getVariable(contentNode, pricingContext)];
			}
			
			
		};
	}
	
	public static StoreLookup getDescretizedProduceRatingLookup1() {
		return new CachingStoreLookup(produceRatingCache) {		
			//   0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15
			private double[] scores = 
				{0,  1,  2,  3,  4,  0,  0,  5,  6,  7,  8,  8,  9,  9, 10, 10};

			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return scores[(int) super.getVariable(contentNode, pricingContext)];
			}	
		};		
	}
	
	public static StoreLookup getDescretizedProduceRatingLookup2() {
		return new CachingStoreLookup(produceRatingCache) {		
			//   0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15
			private double[] scores = 
				{0, -4, -3, -2, -1,  0,  0,  0,  1,  2,  3,  3,  4,  4,  5,  5};

			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return scores[(int) super.getVariable(contentNode, pricingContext)];
			}	
		};		
	}
	
	public static StoreLookup getNewnessLookup() {
		return new StoreLookup() {
			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return -ContentFactory.getInstance().getProductAge((ProductModel) contentNode);
			}
			
			@Override
			public void reloadCache() {
			                
			}
		};
	}
	
	public static StoreLookup getBackInStockLookup() {
		return new StoreLookup() {
			public double getVariable(ContentNodeModel contentNode, PricingContext pricingContext) {
				return -ContentFactory.getInstance().getBackInStockProductAge((ProductModel) contentNode);
			}
			
			@Override
			public void reloadCache() {
			                
			}
		};
	}

	protected static class ReorderRateConverter extends FactorRangeConverter {

		private Set<String> dbColumns =
			new HashSet<String>(Arrays.asList(new String[]{GLOBAL_REORDER_BUYER_COUNT_COLUMN, GLOBAL_TOTAL_BUYER_COUNT_COLUMN}));
		
		private int minPurchases;
		
		protected ReorderRateConverter(int minPurchases) {
			this.minPurchases = minPurchases;
		}
		
		public double[] map(String userId, ScoreRangeProvider provider)
			throws Exception {
			double [] reordersCounts = dup(provider.getRange(userId, GLOBAL_REORDER_BUYER_COUNT_COLUMN));
			double [] totalCounts = provider.getRange(userId, GLOBAL_TOTAL_BUYER_COUNT_COLUMN);
			for(int i = 0; i< reordersCounts.length; ++i) {
				reordersCounts[i] = totalCounts[i] > minPurchases ? reordersCounts[i]/totalCounts[i] : 0;
			}			
			return reordersCounts;
		}
		
		public Set<String> requiresGlobalDatabaseColumns() {
			return dbColumns;
		}
		
		public boolean isPersonalized() {
			return false;
		}
		
		
	}
	
	public static FactorRangeConverter getReorderRateConverter(int minPurchases) {
		return new ReorderRateConverter(minPurchases);
	}
	
	/**
	 * Get factor range converter for reorder rate.
	 * 
	 * Takes the ratio of two database columns: RerorderBuyerCount and TotalBuyerCount.
	 * 
	 * @param minPurchases if purchased less, the score is zero.
	 * @return FactorRangeConverter
	 */
	public static FactorRangeConverter getNormalizedReorderRateConverter(int minPurchases) {
		return new ReorderRateConverter(minPurchases) {
			

			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				double [] reordersCounts = super.map(userId, provider);
				
				divide(reordersCounts,max(reordersCounts));
				return reordersCounts;
			}		
		};
	}
	
	
	/**
	 * 
	 * @param minPurchases
	 * @return
	 */
	public static FactorRangeConverter getDepartmentNormalizedReorderRateConverter(int minPurchases) {
		return new ReorderRateConverter(minPurchases) {
		
			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				final double[] range = super.map(userId, provider);
				
				return new DepartmentSpecificConverter() {

					public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
						return maxNormalize(range, userId, provider);
					}
					
				}.map(userId, provider);
			}
			
		};
	}
	
	public static FactorRangeConverter getDiscretizedReorderRateConverter(double base) {
		return new LogCDFDiscretizingConverter(base) {
			
			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				return discretizeRange(provider.getRange(userId, GLOBAL_POPULARITY_COLUMN));
			}
			
			public boolean isPersonalized() {
				return false;
			}
			
			public Set<String> requiresGlobalDatabaseColumns() {
				return Collections.singleton(GLOBAL_POPULARITY_COLUMN);
			}
		};
	}	

	/**
	 * Frequencies divided by the max frequency a 0 - 1 rate.
	 * 
	 * Frequencies are divided by the max frequency.
	 * @return converter
	 */
	public static FactorRangeConverter getNormalizedFrequencyConverter() {
		return new FactorRangeConverter() {

			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				double [] values = dup(provider.getRange(userId, PERSONALIZED_FREQUENCY_COLUMN));
				double m = max(values);
				if (m > 0) {
					for(int i = 0; i< values.length; ++i) {
						values[i] /= m; 
					}
				}
				return values;
			}
			
			public Set<String> requiresPersonalizedDatabaseColumns() {
				return Collections.singleton(PERSONALIZED_FREQUENCY_COLUMN);
			}
			
			public boolean isPersonalized() {
				return true;
			}
		};
	}
	
	
	/**
	 * Discretizer utility.
	 * 
	 * Discretization consists of:
	 * <ol>
	 *  <li>Calculating the CDF of value occurrences such that
	 *      products are sorted in decreasing value order</li>
	 *  <li>Putting the values into buckets depending where logarithm intersects
	 *      the CDF curve</li> 
	 * </ol>
	 * Thus, it is a relatively expensive process.
	 * 
	 * @author istvan
	 */
	protected static abstract class LogCDFDiscretizingConverter extends FactorRangeConverter {
		
		private double base;
		
		// Frequency bucket
		class Bucket {
			int value = 1; // count
			int cdf = 0; 
			int norm = 0;
			
			public void inc() {
				++value;
			}
			
			public int intValue() {
				return value;
			}
			
			public void accumulate(int c) {
				cdf += c;
			}
			
			public void caculateNorm(double total) {
				double bar = total /= base;
				norm = 1;
				while(cdf < bar) {
					bar /= base;
					++norm;
				}
			}
			
			public int normValue() {
				return norm;
			}
			
		}
		
		protected LogCDFDiscretizingConverter(double base) {
			this.base = base;
		}
		
		protected double[] discretizeRange(double[] range) {
			
			// store values in decreasing order
			SortedMap<Number,Bucket> histo = new TreeMap<Number,Bucket>(
				new Comparator<Number>() {
					public int compare(Number o1, Number o2) {
						double diff = o2.doubleValue() - o1.doubleValue() ;
						return diff < 0 ? -1 : diff > 0 ? +1 : 0;
					}
				}
			);
					
			// sum up the value occurrences
			for(int i = 0; i< range.length; ++i) {
				Double value = new Double(range[i]);
				Bucket count = histo.get(value);
				if (count == null) {
					histo.put(value,new Bucket());
				} else {
					count.inc();
				}
			}
			
			// calculate the CDF
			int total = 0;
			for(Iterator<Map.Entry<Number,Bucket>> i = histo.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<Number,Bucket> entry = i.next();
				Bucket bucket = entry.getValue();
				total += bucket.intValue();
				bucket.accumulate(total);
			}
			
			// calculate the norms
			for(Iterator<Bucket> i = histo.values().iterator(); i.hasNext();) {
				Bucket bucket = i.next();
				bucket.caculateNorm(total);
			}
			
			double [] results = new double[range.length];
			
			// assign the values
			for(int i = 0; i< range.length; ++i) {
				results[i] = range[i] == 0 ? 0 : histo.get(new Double(range[i])).normValue();
			}
			
			return results;
		}
	}
	
	public static FactorRangeConverter getLogDiscretizedPersonalConverter(final String column, double base) {

		return new LogCDFDiscretizingConverter(base) {
			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				return discretizeRange(provider.getRange(userId, column));
			}
			
			public boolean isPersonalized() {
				return true;
			}
			
			public Set<String> requiresPersonalizedDatabaseColumns() {
				return Collections.singleton(column);
			}
		};
	}
	
	public static FactorRangeConverter getLogDiscretizedGlobalConverter(final String column, double base) {
		
		return new LogCDFDiscretizingConverter(base) {
			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				return discretizeRange(provider.getRange(userId, column));
			}
			
			public boolean isPersonalized() {
				return false;
			}
			
			public Set<String> requiresGlobalDatabaseColumns() {
				return Collections.singleton(column);
			}		
		};
	}

	/**
	 * Get max normalized converter for the global factor with given source column.
	 * 
	 * Adjust the range such that all values are divided by the range max.
	 * 
	 */
	public static FactorRangeConverter getMaxNormalizedGlobalConvereter(final String column) {
		return new FactorRangeConverter() {

			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				double[] values = dup(provider.getRange(null, column));
				divide(values,max(values));
				return values;
			}
			
			public boolean isPersonalized() {
				return false;
			}
			
			public Set<String> requiresGlobalDatabaseColumns() {
				return Collections.singleton(column);
			}
			
		};
	}
	
	/**
	 * Get max normalized personalized converter for the given source column.
	 * 
	 */	
	public static FactorRangeConverter getMaxNormalizedPersonalConverter(final String column) {
		return new FactorRangeConverter() {

			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				double[] values = dup(provider.getRange(userId, column));
				divide(values,max(values));
				return values;
			}
			
			public boolean isPersonalized() {
				return true;
			}
			
			public Set<String> requiresPersonalizedDatabaseColumns() {
				return Collections.singleton(column);
			}
			
		};
	}
	
	
	/**
	 * Department specific statistics.
	 *
	 */
	protected static abstract class DepartmentSpecificConverter extends FactorRangeConverter {
		
		/**
		 * 
		 * @param userId
		 * @param provider
		 * @return Map<DepartmentId:String,List<Index:Integer>>
		 */
		protected static Map<String, List<Integer>> departmentMap(String userId, ScoreRangeProvider provider) {
			List products = provider.products(userId);
			Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
			for(int i=0; i< products.size(); ++i) {
				String dept = "";
				try {
					ProductModel productNode = 
						(ProductModel)ContentFactory.getInstance().getContentNodeByKey(
								new ContentKey(FDContentTypes.PRODUCT,products.get(i).toString()));
					dept = productNode.getDepartment().getContentKey().getId();
				} catch (Exception e) {
				}
				List<Integer> indexes = map.get(dept);
				if (indexes == null) {
					indexes = new ArrayList<Integer>();
					map.put(dept, indexes);
				}
				indexes.add(new Integer(i));
			}
			return map;
		}
		
		protected double[] maxNormalize(double[] values, String userId, ScoreRangeProvider provider) throws Exception {
			Map<String, List<Integer>> deptMap = departmentMap(userId, provider);
			for(Iterator<List<Integer>> i = deptMap.values().iterator(); i.hasNext();) {
				List<Integer> indexes = i.next();
				double max = Double.MIN_VALUE;
				for(Iterator<Integer> j = indexes.iterator(); j.hasNext();) {
					int index = j.next().intValue();
					if (values[index] > max) {
						max = values[index];
					}
				}
				if (max != 0) {
					for(Iterator<Integer> j = indexes.iterator(); j.hasNext();) {
						int index = j.next().intValue();
						values[index] /= max;
					}
				}
			}
			return values;
		}
	};
	
	public static FactorRangeConverter getDepartmentMaxNormalizedPersonalizedConverter(final String factor) {
		return new DepartmentSpecificConverter() {

			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				return maxNormalize(dup(provider.getRange(userId, factor)),userId, provider);
			}
			
			public boolean isPersonalized() {
				return true;
			}
			
			public Set<String> requiresPersonalizedDatabaseColumns() {
				return Collections.singleton(factor);
			}
		};
	}
	
	public static FactorRangeConverter getDepartmentMaxNormalizedGlobalConverter(final String factor) {
		return new DepartmentSpecificConverter() {

			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				return maxNormalize(dup(provider.getRange(userId, factor)),userId, provider);
			}
			
			public boolean isPersonalized() {
				return false;
			}
			
			public Set<String> requiresGlobalDatabaseColumns() {
				return Collections.singleton(factor);
			}
		};
	}
	
	public static FactorRangeConverter getSeasonalityConverter(final double asymptote) {
		
		return new FactorRangeConverter() {

			private Set<String> dbColumns =
				new HashSet<String>(Arrays.asList(new String[]{GLOBAL_WEEKLY_FREQUENCY_COLUMN, GLOBAL_AVERAGE_FREQUENCY_COLUMN}));
			
			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				double[] range = dup(provider.getRange(userId, GLOBAL_WEEKLY_FREQUENCY_COLUMN));
				double[] averageFrequencies = provider.getRange(userId, GLOBAL_AVERAGE_FREQUENCY_COLUMN);
				for(int i = 0; i< range.length; ++i) {
					if (averageFrequencies[i] == 0) {
						range[i] = Double.MAX_VALUE;
					} else {
						range[i] /= averageFrequencies[i];
					}
				}
				positiveBiasedSigmoidNormalize(range, asymptote);
				
				return range;
			}
			
			public boolean isPersonalized() {
				return false;
			}
			
			public Set<String> requiresGlobalDatabaseColumns() {
				return dbColumns;
			}
			
		};
	}
	
	
	public static FactorRangeConverter getDiscretizedSeasonality() {
		return new FactorRangeConverter() {
			
			private Set<String> dbColumns =
				new HashSet<String>(Arrays.asList(new String[]{GLOBAL_WEEKLY_FREQUENCY_COLUMN, GLOBAL_AVERAGE_FREQUENCY_COLUMN}));
			
			public boolean isPersonalized() {
				return false;
			}
			
			public Set<String> requiresGlobalDatabaseColumns() {
				return dbColumns;
			}

			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				double[] weeks = provider.getRange(userId, GLOBAL_WEEKLY_FREQUENCY_COLUMN);
				double[] averages = provider.getRange(userId, GLOBAL_AVERAGE_FREQUENCY_COLUMN);
				double[] scores = new double[weeks.length];
				 
				for(int i = 0; i< weeks.length; ++i) {
					if (averages[i] > 0) {
						double rat = weeks[i] / averages[i];
						if (rat < 1./3.) {
							scores[i] = 0;
						} else if (rat < 2./3.) {
							scores[i] = 1;
 						} else if (rat < 1.) {
 							scores[i] = 2;
 						} else if (rat < 2.) {
 							scores[i] = 3;
 						} else if (rat < 4.) {
 							scores[i] = 5;
 						}
					} else {
						scores[i] = 0;
					}
				}
				
				return scores;
			}
		};
	}
	
	
}
