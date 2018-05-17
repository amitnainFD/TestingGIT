package com.freshdirect.fdstore.promotion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.promotion.management.FDPromotionManager;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.framework.util.ExpiringReference;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * This class caches all active promo variants in a linked hash map(variantId -> List of PromoVariantModel). Refreshes the map every 30 minutes.
 * @author skrishnasamy
 *
 */
public class PromoVariantCache {

	private final static PromoVariantCache INSTANCE = new PromoVariantCache();
	
	@SuppressWarnings( "unused" )
	private static Category LOGGER = LoggerFactory.getInstance(PromoVariantCache.class);
	
	public final static Comparator<PromoVariantModel> PRIORITY_COMPARATOR = new Comparator<PromoVariantModel>() {

		public int compare(PromoVariantModel pv1, PromoVariantModel pv2) {
			return new Integer(pv2.getPromoPriority()).compareTo(new Integer(pv1.getPromoPriority()));
		}
	};
	
	public final static Comparator<PromoVariantModel> FEATURE_PRIORITY_COMPARATOR = new Comparator<PromoVariantModel>() {

		public int compare(PromoVariantModel pv1, PromoVariantModel pv2) {
			return new Integer(pv2.getVariantPriority()).compareTo(new Integer(pv1.getVariantPriority()));
		}
	};
	
	private ExpiringReference<Map<String, List<PromoVariantModel>>> activePromoVariants = 
		new ExpiringReference<Map<String, List<PromoVariantModel>>>(60 * 60 * 1000) {
			protected Map<String, List<PromoVariantModel>> load() {
				try {
					@SuppressWarnings( "unchecked" )
					List<PromoVariantModel> promoVariants = FDPromotionManager.getAllActivePromoVariants(EnumSiteFeature.getSmartSavingsFeatureList());
					String prevVariantId = "";
					Map<String, List<PromoVariantModel>> pvMap = new HashMap<String, List<PromoVariantModel>>();
					List<PromoVariantModel> valueList = null;
					for( PromoVariantModel pv : promoVariants ) {
						String variantId = pv.getVariantId();
						if(!variantId.equals(prevVariantId)){
							valueList = new ArrayList<PromoVariantModel>();
							pvMap.put(variantId, valueList);
							prevVariantId = variantId;
						}
						valueList.add(pv);
						Collections.sort(valueList, PRIORITY_COMPARATOR);
					}
					return pvMap;
				} catch (FDResourceException ex) {
					throw new FDRuntimeException(ex);
				}
			}
	};

	private PromoVariantCache() {
		
	}
			

	public static PromoVariantCache getInstance() {
		return INSTANCE;
	}
	
	
	public void refreshAll(){
		activePromoVariants.forceRefresh();
		// just to confirm this reloads all the cached data
		//this.activePromoVariants.get();		
	}
	

	public Map<String, List<PromoVariantModel>> getPromoVariantMap() {
		return activePromoVariants.get();
	}
	
	public Collection<String> getPromoVariantIds() {
		//Returns Collection containing variant Ids that are attached to one or more Promo codes.
		return getPromoVariantMap().keySet();
	}

	/**
	 * 
	 * @param variantId
	 * @return a collection of PromoVariantModel attached to the given variant.
	 */
	public List<PromoVariantModel> getAllPromoVariants(String variantId) {
		List<PromoVariantModel> pvlist = getPromoVariantMap().get( variantId );
		if ( pvlist != null )
			return Collections.unmodifiableList( pvlist );
		return Collections.<PromoVariantModel>emptyList();
	}

}
