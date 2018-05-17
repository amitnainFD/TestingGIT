package com.freshdirect.fdstore.promotion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.promotion.management.FDPromotionNewManager;
import com.freshdirect.framework.cache.CacheI;
import com.freshdirect.framework.cache.ManagedCache;
import com.freshdirect.framework.cache.SimpleLruCache;
import com.freshdirect.framework.util.ExpiringReference;
import com.freshdirect.framework.util.log.LoggerFactory;
/**
 * This cache factory class is provide methods used by website/CRM for runtime 
 * evaluations of promotions.
 * The cache contains a Map of all automatic promotion codes with value as last
 * modified timestamp. This map is refreshed every few miniutes which reloads all automatic
 * promo codes with recent modified timestamp along with any newly added promo codes.  
 * The requested automatic promotion object residing in the cache are refreshed only if the corresponding
 * timestamp in the map has changed.
 * @author skrishnasamy
 *
 */
public class PromotionFactory {
	private final static Object lock = new Object();
	private static PromotionFactory sharedInstance = null;
	
	private static final Category LOGGER = LoggerFactory.getInstance(PromotionFactory.class);

	private CacheI<String, PromotionI> redeemPromotions;
	private CacheI<String, Map<Date, Integer>> redemptions;

	private Map<String, PromotionI> promotionMap = new LinkedHashMap<String, PromotionI>();
	private Date maxLastModified;
	
	private ExpiringReference<List<PromotionI>> automaticpromotions = new ExpiringReference<List<PromotionI>>(10 * 60 * 1000) {
		protected List<PromotionI> load() {
			try {
				LOGGER.info("REFRESHING AUTOMATIC PROMOTION MAP FOR ANY NEW PROMOTIONS FROM LAST MODIFIED TIME "+maxLastModified);
				List<PromotionI> promoList = new ArrayList<PromotionI>();
				if(null !=maxLastModified){
					promoList = FDPromotionNewManager.getModifiedOnlyPromos(maxLastModified);
				}else{
					loadAutomaticPromotions();
				}
				LOGGER.info("REFRESHED AUTOMATIC PROMOTION MAP FOR ANY NEW PROMOTIONS. FOUND "+promoList.size());
				return promoList;
			} catch (FDResourceException ex) {
				throw new FDRuntimeException(ex);
			}
		}
	};

	private PromotionFactory() {
		this.redeemPromotions = new ManagedCache<String, PromotionI>("PROMOTION", constructCache());
		this.redemptions = new ManagedCache<String, Map<Date, Integer>>("REDEMPTION", constructRedemptionCache());
		loadAutomaticPromotions();
	}

	private void loadAutomaticPromotions(){
		try {
			List<PromotionI> promoList = FDPromotionNewManager.getAllAutomaticPromotions();
			for ( PromotionI promo : promoList ) {
				Date promoModifyDate = promo.getModifyDate();
				Date now = new Date();
				if(this.maxLastModified == null || (this.maxLastModified.before(promoModifyDate) && !promoModifyDate.after(now))){
					this.maxLastModified = new Date(promoModifyDate.getTime());
				}
				this.promotionMap.put(promo.getPromotionCode(), promo);
			}
		} catch (FDResourceException ex) {
			LOGGER.error("Failed to load automatic promotions", ex);
		}
	}

	public static PromotionFactory getInstance() {
		synchronized(lock) {
			if (sharedInstance == null) {
				sharedInstance = new PromotionFactory();
			}
		}
		
		return sharedInstance;
	}

	protected synchronized Map<String, PromotionI> getAutomaticPromotionMap() {
		List<PromotionI> promoList = this.automaticpromotions.get();
		if(promoList.size() > 0){
			for ( PromotionI promo : promoList ) {
				Date promoModifyDate = promo.getModifyDate();
				Date now = new Date();
				if(this.maxLastModified == null  || (this.maxLastModified.before(promoModifyDate) && !promoModifyDate.after(now))){
					this.maxLastModified = new Date(promoModifyDate.getTime());
				}
				String promoId = promo.getPromotionCode();
				if(!promo.isRedemption()){
					//If the modified promo is a redemption ignore it.
					this.promotionMap.put(promoId, promo);	
				}else{
					//If the modified promo was a automatic before, after modification it is 
					//redemption then remove from the Map.
					if(this.promotionMap.containsKey(promoId)){
						this.promotionMap.remove(promoId);
					}
				}
				
			}
			promoList.clear();
		}
		return this.promotionMap;
	}
	
	public Collection<PromotionI> getAllAutomaticPromotions() {
		return this.getAutomaticPromotionMap().values();
	}

	public Collection<String> getAllAutomaticCodes() {
			//Returns Collection containing Automatic Promo codes -->
			return this.getAutomaticPromotionMap().keySet();
	}

	public PromotionI getPromotion(String promoId) {
		PromotionI promotion = null;
		if(getAllAutomaticCodes().contains(promoId)){
			/*
			 * This is an active/recently expired automatic promotion.
			 * In this case, Call getAutomaticPromotion(promoId)
			 */ 
			promotion = getAutomaticPromotion(promoId);
			
		}else {
			/* 
			 * It is a redemption promotion or a expired promotion that
			 * part of a past order. In this case,  
			 * Call getRedemptionPromotion(promoId) */
			promotion = getRedemptionPromotion(promoId);
		}
		return promotion;
	}
	
	/**
	 * 
	 * @param promoId
	 * @return
	 */
	public PromotionI getAutomaticPromotion(String promoId) {
		return this.getAutomaticPromotionMap().get(promoId);
	}
	/**
	 * 
	 * @param promoId
	 * @return
	 */
	public PromotionI getRedemptionPromotion(String promoId) {
		PromotionI promotion = null;
		try{
			if(promoId == null){
				//This happens when promotion_popup page passes a null promotion code.
				return null;
			}
			promotion = (Promotion) getRedeemPromotions().get(promoId);
			
			if(promotion == null){
				LOGGER.info("REFRESHING REDEMPTION PROMOTION "+promoId);
				//The object has become stale or it's yet to be loaded into the cache.
				promotion = FDPromotionNewManager.getPromotionForRT(promoId);
				if(promotion != null){
					//Promotion can be null if the promotion has a incomplete configuration.
					getRedeemPromotions().put(promoId, promotion);	
				}
				LOGGER.info("REFRESHING REDEMPTION PROMOTION DONE.");
			}
		}catch (FDResourceException ex) {
			LOGGER.error("Exception Occurred while getting Redemption Promotion "+promoId, ex);
			throw new FDRuntimeException(ex);
		}
		return promotion;
	}
	

	/**
	 * 
	 * @param promoId
	 * @return
	 */
	public Integer getRedemptions(String promoId, Date requestedDate) {
		Integer redeemCount = null;
		try{
			if(promoId == null){
				//This happens when promotion_popup page passes a null promotion code.
				return null;
			}
			
			if(getRedemptions().get(promoId) != null) {
				redeemCount = (Integer) getRedemptions().get(promoId).get(requestedDate);
			}
			
			if(redeemCount == null){
				LOGGER.info("REFRESHING REDEMPTION COUNT FOR PROMOTION "+promoId);
				//The object has become stale or it's yet to be loaded into the cache.
				redeemCount = FDPromotionNewManager.getRedemptionCount(promoId, requestedDate);
				if(redeemCount != null){
					//Promotion can be null if the promotion has a incomplete configuration.
					if(getRedemptions().get(promoId) == null) {
						getRedemptions().put(promoId, new HashMap<Date, Integer>());
					}
					getRedemptions().get(promoId).put(requestedDate, redeemCount);
					
				}
				LOGGER.info("REFRESHING REDEMPTION COUNT FOR PROMOTION DONE.");
			}
		}catch (FDResourceException ex) {
			LOGGER.error("Exception Occurred while getting Redemption count for Promotion "+promoId, ex);
			throw new FDRuntimeException(ex);
		}
		return redeemCount;
	}
	
	/**
	 * Thie method returns a set of Automatic Promotion codes that matches
	 * with the EnumPromotionType passed as an arguement. 
	 * TODO this method in future need to modified to get all promotions from DB
	 * if used for searching redemption promotions as well for the matching 
	 * EnumPromotionType.
	 * @param type
	 * @return
	 * @throws FDResourceException
	 */
	public Set<String> getPromotionCodesByType(EnumPromotionType type) {
		Set<String> s = new HashSet<String>();
		for ( PromotionI promo : getAllAutomaticPromotions() ) {
			if (promo.getPromotionType().equals(type)) {
				s.add(promo.getPromotionCode());
			}
		}
		return s;
	}

	public void forceRefresh(String promoId) {
		automaticpromotions.forceRefresh();
		if(getRedeemPromotions().get(promoId) != null){
			/*
			 * The updated promotion is a redemption promotion.So Remove the promotion 
			 * object to reload during next request.
			 */
			getRedeemPromotions().remove(promoId);
		}
	}
	
	public void forceRefreshRedemptionCnt(String promoId) {
		if(getRedemptions().get(promoId) != null){
			/*
			 * The updated promotion is a redemption promotion.So Remove the promotion 
			 * object to reload during next request.
			 */
			getRedemptions().remove(promoId);
		}
	}
	

	private CacheI<String, PromotionI> getRedeemPromotions() {
		return this.redeemPromotions;
	}
	
	private CacheI<String, Map<Date, Integer>> getRedemptions() {
		return this.redemptions;
	}
	
	private CacheI<String, PromotionI> constructCache(){
		SimpleLruCache<String, PromotionI> lruCache = new SimpleLruCache<String, PromotionI>();
		lruCache.setName("PROMOTION");
		lruCache.setCapacity(ErpServicesProperties.getPromotionRTSizeLimit());
		lruCache.setTimeout(FDStoreProperties.getPromotionRTRefreshPeriod());
		return lruCache;
	}
	
	private CacheI<String, Map<Date, Integer>> constructRedemptionCache(){
		SimpleLruCache<String, Map<Date, Integer>> lruCache = new SimpleLruCache<String, Map<Date, Integer>>();
		lruCache.setName("REDEMPTION");
		lruCache.setCapacity(2000);
		lruCache.setTimeout(FDStoreProperties.getRedeemCntRefreshPeriod());
		return lruCache;
	}
}
