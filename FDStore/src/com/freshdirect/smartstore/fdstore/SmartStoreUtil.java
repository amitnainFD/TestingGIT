package com.freshdirect.smartstore.fdstore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.EnumCheckoutMode;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.ConfiguredProduct;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.FDAuthenticationException;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.giftcard.FDGiftCardInfoList;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.service.VariantRegistry;

/**
 * General utilities for SmartStore.
 * 
 * @author istvan
 * 
 */
public class SmartStoreUtil {
	/**
	 * Returns label-description couple for variant. This function is used by
	 * PIP
	 * 
	 * @param v
	 *            {@link Variant variant}
	 * 
	 * @return String[label, inner text] couple
	 * 
	 *         Tags: SmartStore, PIP
	 */
	public static synchronized String[] getVariantPresentation(Variant v) {
		return new String[] { v.getServiceConfig().getPresentationTitle(),
				v.getServiceConfig().getPresentationDescription() };
	}

	/**
	 * Checks if 'anId' is a valid variant ID
	 * 
	 * @param anId
	 *            variant ID
	 * @param feat
	 *            {@link EnumSiteFeature} site feature
	 * 
	 * @return result of check
	 */
	public static boolean checkVariantId(String anId, EnumSiteFeature feat) {
		if (anId == null)
			return false;

		if (VariantRegistry.getInstance().getService(anId) != null)
			return true;

		return false;
	}

	/**
	 * Sort cohort names numerically
	 * 
	 * @param names
	 */
	public static void sortCohortNames(List<Object> names) {
		Collections.sort(names, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				if (o1 == null) {
					if (o2 == null)
						return 0;
					return -1;
				}
				if (o2 == null)
					return -1;
				String s1 = o1.toString();
				String s2 = o2.toString();
				String prefix, candidate;
				prefix = candidate = "";
				while (true) {
					if (s1.length() <= candidate.length())
						break;
					candidate = s1.substring(0,candidate.length() + 1);

					if (s2.startsWith(candidate)) {
						prefix = candidate;
						continue;
					}
					break;
				}
				int pLen = prefix.length();
				if (pLen != 0) {
					s1 = s1.substring(pLen);
					s2 = s2.substring(pLen);
					try {
						int i1 = Integer.parseInt(s1);
						int i2 = Integer.parseInt(s2);
						return i1 - i2;
					} catch (NumberFormatException e) {
						return s1.compareTo(s2);
					}
				}
				return s1.compareTo(s2);
			}
		});
	}

	/**
	 * get the currently active variants sorted in weight
	 * 
	 * @param feature
	 * @return
	 */
	public static SortedMap<String, Integer> getVariantsSortedInWeight(EnumSiteFeature feature) {
		return getVariantsSortedInWeight(feature, null, true);
	}

	/**
	 * get the variant mappings in the DB for a given date (null means latest)
	 * @param feature
	 * @param date
	 * @return
	 */
	public static SortedMap<String, Integer> getVariantsSortedInWeight(EnumSiteFeature feature, Date date) {
		return getVariantsSortedInWeight(feature, date, false);
	}
	
	/**
	 * get the currently active variant map or the latest or the one for a
	 * specific date in DB, depending on the specified parameters
	 * 
	 * @param feature
	 * @param date
	 *            if current is false, specifies the date for the configuration
	 *            is searched (if null and current is false then get the latest)
	 * @param current
	 *            it true, get the current
	 * @return
	 */
	public static SortedMap<String, Integer> getVariantsSortedInWeight(EnumSiteFeature feature,
			Date date, boolean current) {

		final VariantSelection vs = VariantSelection.getInstance();
		Map<String, Integer> cohorts = vs.getCohorts();
		Map<String, String> assignment = getAssignment(feature, date, current);
		final Map<String, Integer> clone = new HashMap<String, Integer> ();
		List<String> variants = vs.getVariants(feature);
		Iterator<String> it = variants.iterator();
		while (it.hasNext()) {
			clone.put(it.next(), new Integer(0));
		}

		it = cohorts.keySet().iterator();
		while (it.hasNext()) {
			String cohort = it.next();
			String variant = assignment.get(cohort);
			if (variant != null) {
				int weight = clone.get(variant).intValue();
				weight += cohorts.get(cohort).intValue();
				clone.put(variant, new Integer(weight));
			}
		}

		SortedMap<String, Integer> weights = new TreeMap<String, Integer>(new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				if (o1 == null) {
					if (o2 == null)
						return 0;
					return -1;
				}
				if (o2 == null)
					return -1;
				String s1 = o1.toString();
				String s2 = o2.toString();
				int i1 = clone.get(s1).intValue();
				int i2 = clone.get(s2).intValue();
				if (i1 == i2)
					return s1.compareToIgnoreCase(s2);
				return i2 - i1;
			}
		});
		weights.putAll(clone);

		return weights;
	}

	public static List<String> getVariantNamesSortedInUse(EnumSiteFeature feature) {
		return getVariantNamesSortedInUse(feature, null, true);
	}

	public static List<String> getVariantNamesSortedInUse(EnumSiteFeature feature, Date date) {
		return getVariantNamesSortedInUse(feature, date, false);
	}

	public static List<String> getVariantNamesSortedInUse(EnumSiteFeature feature, Date date, boolean current) {
		final VariantSelection vs = VariantSelection.getInstance();
		List<String> variants = vs.getVariants(feature);
		Map<String,String> assignment = getAssignment(feature, date, current);

		getVariantNamesSortedInUse(variants, assignment);
		return variants;
	}

	/**
	 * get the currently active cohort-variant assignment for a given feature
	 * 
	 * @param feature
	 * @return
	 */
	public static Map<String,String> getAssignment(EnumSiteFeature feature) {
		return getAssignment(feature, null, true);
	}

	/**
	 * get the cohort-variant assignment for a given feature and for a given date
	 * 
	 * Null date means get the latest in the DB (does not necessary equal to the
	 * currently active 
	 * 
	 * @param feature
	 * @param date
	 * @return
	 */
	public static Map<String,String> getAssignment(EnumSiteFeature feature, Date date) {
		return getAssignment(feature, date, false);
	}

	/**
	 * get the currently active cohort-variant assignment or the one specific to
	 * a given date depending on the specified parameters
	 * 
	 * @param feature
	 * @param date
	 * @param current
	 * @return
	 */
	public static Map<String,String> getAssignment(EnumSiteFeature feature, Date date,
			boolean current) {
		Map<String,String> assignment;
		final VariantSelection vs = VariantSelection.getInstance();
		if (current) {
			List<String> cohorts = vs.getCohortNames();
			VariantSelector vsr = VariantSelectorFactory.getSelector(feature);
			assignment = new HashMap<String,String>(cohorts.size());
			for (int i = 0; i < cohorts.size(); i++) {
				if (vsr.getVariant(cohorts.get(i)) != null)
					assignment.put(cohorts.get(i), vsr.getVariant(cohorts.get(i)).getId());
			}
		} else {
			assignment = vs.getVariantMap(feature, date);
		}
		return assignment;
	}

    private static void getVariantNamesSortedInUse(List<String> variants, final Map<String,String> assignment) {
		Collections.sort(variants, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				if (o1 == null) {
					if (o2 == null) {
						return 0;
					}
					return -1;
				}
				if (o2 == null) {
					return -1;
				}
				String s1 = o1.toString();
				String s2 = o2.toString();
				if (assignment.containsValue(s1)) {
					if (assignment.containsValue(s2)) {
						return s1.compareTo(s2);
					}
					return -1;
				}
				if (assignment.containsValue(s2)) {
					return 1;
				}
				return s1.compareTo(s2);
			}
		});
	}
	
	public static boolean isCohortAssigmentUptodate() {
		List<EnumSiteFeature > siteFeatures = EnumSiteFeature.getSmartStoreEnumList();
		Iterator<EnumSiteFeature> it = siteFeatures.iterator();
		while (it.hasNext()) {
			EnumSiteFeature sf = it.next();
			Map<String,String> curVars = getAssignment(sf, null, true);
			Map<String,String> asgmnt = getAssignment(sf, null, false);
			if (!curVars.equals(asgmnt)) {
				return false;
			}
		}

		return true;
	}


	/**
	 * Convenience method to get Variant by its ID
	 * 
	 * @param featureId Site Feature
	 * @param variantId Variant ID
	 * 
	 * @return variant
	 */
	public static Variant getVariant(String featureId, String variantId) {
		if (featureId == null || variantId == null)
			return null;

		EnumSiteFeature feature = EnumSiteFeature.getEnum(featureId);
		if (feature == null)
			return null;

		Variant variant = VariantRegistry.getInstance().getService(variantId);
		if (variant != null && variant.getSiteFeature().equals(feature))
			return variant;
		
		return null;
	}
	
	
	/**
	 * 
	 * @param models List<ContentNodeModel>
	 * @return List<ContentKey>
	 */
        public static List<ContentKey> toContentKeysFromModels(Collection<ContentNodeModel> models) {
            if (models==null) {
                return null;
            }
            List<ContentKey> result = new ArrayList<ContentKey>(models.size());
            for (ContentNodeModel model : models) {
                result.add(model.getContentKey());
            }
            return result;
        }

        
        /**
         * 
         * @param models List<ContentNodeModel>
         * @return Set<ContentKey>
         */
        public static Set<ContentKey> toContentKeySetFromModels(Collection<ContentNodeModel> models) {
            if (models==null) {
                return null;
            }
            Set<ContentKey> result = new HashSet<ContentKey>(models.size());
            for (ContentNodeModel model : models) {
                result.add(model.getContentKey());
            }
            return result;
        }

        public static List<ContentNodeModel> toContentNodesFromKeys(List<ContentKey> keys) {
            if (keys==null) {
                return null;
            }
            List<ContentNodeModel> result = new ArrayList<ContentNodeModel>(keys.size());
            ContentFactory factory = ContentFactory.getInstance();
            for (ContentKey contentKey : keys) {
                ContentNodeModel model = factory.getContentNodeByKey(contentKey);
                result.add(model);
            }
            return result;
        }

	private static ThreadLocal<Map<String, ProductModel>> CFG_PRODS = new ThreadLocal<Map<String, ProductModel>>() {
	    protected Map<String, ProductModel> initialValue() {
	        return new HashMap<String, ProductModel>();
	    }
	};

	public static void clearConfiguredProductCache() {
		CFG_PRODS.get().clear();
	}

	/**
	 * This method returns a ProductModel from a ConfiguredProduct or ConfiguredProductGroup and store the original CP or CPG in a thread local cache, this is used later by the configurator.
	 * @param pm
	 * @return
	 */
	public static ProductModel addConfiguredProductToCache(ProductModel pm) {
		ProductModel orig = pm;
		while (pm instanceof ConfiguredProduct)
			pm = ((ConfiguredProduct) pm).getProduct();
		if (pm != orig && pm != null) {
			CFG_PRODS.get().put(pm.getContentKey().getId(), orig);
		}
		return pm;
	}

	public static Map<String, ProductModel> getConfiguredProductCache() {
		return CFG_PRODS.get();
	}

    public static List<ProductModel> addConfiguredProductToCache(List<ContentNodeModel> list) {
        List<ProductModel> ret = new ArrayList<ProductModel>(list.size());
        for (ContentNodeModel current : list) {
        	if ( current instanceof ProductModel ) {
	            ProductModel replace = addConfiguredProductToCache((ProductModel)current);
	            if (replace != null) {
	                ret.add(replace);
	            }
        	}
        }
        return ret;
    }	
	

	/**
	 * Helper method to decide if the product should be 'greyed out'
	 * That is the product recommended by a savings variant is already in the cart
	 * 
	 * @param v actual recommender variant (or tab)
	 * @param prod current product
	 * @param user actual user
	 * @return render product opaque
	 */
	public static boolean isSavingProductInCart(Variant v, ProductModel prod, FDUserI user) {
		// null check, variant is NOT savings -> bye
		if (v == null || !v.isSmartSavings() || prod == null || user == null)
			return false;

		final String prodName = prod.getContentName();

		for ( FDCartLineI cl : user.getShoppingCart().getOrderLines() ) {
			final boolean isSavingsItem = v.getId().equals(cl.getSavingsId());

			// is cart item 'saving' and equals to this product?
			if (isSavingsItem && prodName.equals(cl.getProductName()) ) {
				return true; // make it opaque
			}
		}

		return false;
	}
	
	public static int countSavingProductsInCart(Variant v, FDUserI user, Map<String, List<ContentKey>> previousRecommendations) {
		if (v == null || !v.isSmartSavings() || user == null)
			return 0;

		//int count = 0;
		List<ContentKey> cachedItems = previousRecommendations.get(v.getId());
		if (cachedItems == null)
			return 0;
		Set<String> uniqueKeys = new HashSet<String>();
		OUTER: for ( FDCartLineI cl : user.getShoppingCart().getOrderLines() ) {
			final boolean isSavingsItem =  v.getId().equals(cl.getSavingsId());

			if (isSavingsItem) {
				String productId = cl.getProductName();
				for (int i = 0; i < cachedItems.size(); i++) {
					ContentKey key = cachedItems.get(i);
					if (key.getId().equals(productId)) {
						//count++;
						uniqueKeys.add(productId);
						continue OUTER;
					}
				}
			}
		}
		int count = uniqueKeys.size();
		if (cachedItems.size() == count)
			return -count;
		
		return count;
	}
	
	public static double sumOfSavingsInCart(Variant v, FDUserI user, Map<String, List<ContentKey>> previousRecommendations) {
		if (v == null || !v.isSmartSavings() || user == null)
			return 0.;

		double sum = 0.;
		List<ContentKey> cachedItems = previousRecommendations.get(v.getId());
		if (cachedItems == null)
			return 0.;
		
		OUTER: for ( FDCartLineI cl : user.getShoppingCart().getOrderLines() ) {
			final boolean isSavingsItem = v.getId().equals(cl.getSavingsId());

			if (isSavingsItem) {
				String productId = cl.getProductName();
				for (int i = 0; i < cachedItems.size(); i++) {
					ContentKey key = cachedItems.get(i);
					if (key.getId().equals(productId)) {
						sum += cl.getDiscountAmount();
						continue OUTER;
					}
				}
			}
		}
		return sum;
	}
}
