package com.freshdirect.fdstore.warmup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.fdstore.FDAttributeCache;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDGroup;
import com.freshdirect.fdstore.FDInventoryCache;
import com.freshdirect.fdstore.FDNutritionCache;
import com.freshdirect.fdstore.FDNutritionPanelCache;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSku;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.GroupScalePricing;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.grp.FDGrpInfoManager;
import com.freshdirect.fdstore.zone.FDZoneInfoManager;
import com.freshdirect.framework.util.log.LoggerFactory;

public class CacheWarmupUtil {
	private static Logger LOGGER = LoggerFactory.getInstance(CacheWarmupUtil.class.getSimpleName());

	private final static int MAX_THREADS = 2;
	private final static int GRAB_SIZE = 100;



	/**
	 * Trigger various FD caches
	 */
	public static void warmupFDCaches() {
		// Get instance loads up the inventory
		FDInventoryCache.getInstance();
		// Get instance loads up the Attributes
		FDAttributeCache.getInstance();
		// Get instance loads up the Nutrition
		FDNutritionCache.getInstance();
		// Get instance loads up the Drug Nutrition
		FDNutritionPanelCache.getInstance();
	}

	
	
	
	
	@SuppressWarnings("rawtypes")
	public static void warmupZones() throws FDResourceException {
		LOGGER.info("Loading zone data");
		Collection zoneInfoList = FDZoneInfoManager.loadAllZoneInfoMaster();
		@SuppressWarnings("unchecked")
		final List zoneInfos = new ArrayList(FDCachedFactory.getZoneInfos((String[]) zoneInfoList.toArray(new String[0])));
		LOGGER.info("Lightweight zone data loaded size is :" + zoneInfos.size());
	}

	
	
	public static void warmupProducts(final Collection<String> skuCodes, final UserContext userCtx) throws FDResourceException {
		LOGGER.info("Loading lightweight product data");

		@SuppressWarnings("unchecked")
		final List<FDProductInfo> prodInfos = new ArrayList<FDProductInfo>(FDCachedFactory.getProductInfos((String[]) skuCodes.toArray(new String[0])));
		// Filter discontinued Products
		filterDiscontinuedProducts(prodInfos, userCtx);

		LOGGER.info("Loading heavyweight product data in " + MAX_THREADS + " threads");

		final FDSku[] DUMMY_ARRAY = new FDSku[0];
		for (int i = 0; i < MAX_THREADS; i++) {
			Thread t = new Thread("Warmup " + i) {
				public void run() {
					LOGGER.info("started " + Thread.currentThread().getName());
					while (true) {
						FDSku[] skus;
						synchronized (prodInfos) {
							int pSize = prodInfos.size();
							if (pSize == 0) {
								// nothing left to process
								break;
							}
							// grab some items, and remove from prodInfos list
							List<FDProductInfo> subList = prodInfos.subList(0, Math.min(pSize, GRAB_SIZE));
							skus = (FDSku[]) subList.toArray(DUMMY_ARRAY);
							subList.clear();
							LOGGER.debug(pSize + " items left to load");
						}
						try {
							FDCachedFactory.getProducts(skus);
						} catch (FDResourceException ex) {
							LOGGER.warn("Error during warmup", ex);
						}
					}
					LOGGER.info("completed " + Thread.currentThread().getName());
				}
			};
			t.setDaemon(true);
			t.start();
		}
	}

	
	
	private static void filterDiscontinuedProducts(List<FDProductInfo> prodInfos, UserContext userCtx) {
		// UserContext userCtx=FACTORY.getCurrentUserContext();
		ZoneInfo zone=userCtx.getPricingContext().getZoneInfo();
		for (Iterator<FDProductInfo> it = prodInfos.iterator(); it.hasNext();) {
			FDProductInfo prodInfo = it.next();
			if (prodInfo.isDiscontinued(zone.getSalesOrg(),zone.getDistributionChanel())) {
				it.remove();
			}
		}
	}

	
	
	
	public static void warmupProductNewness() throws FDResourceException {
		ContentFactory factory = ContentFactory.getInstance();
		
		// initiating the asynchronous load of new and reintroduced products cache
		if (FDStoreProperties.isPreloadNewness()) {
			LOGGER.info("preloading product newness");
			factory.getNewProducts();
			LOGGER.info("finished preloading product newness");
		} else {
			LOGGER.info("skipped preloading product newness");
		}

		if (FDStoreProperties.isPreloadReintroduced()) {
			LOGGER.info("preloading reintroduced products");
			factory.getBackInStockProducts();
			LOGGER.info("finished preloading reintroduced products");
		} else {
			LOGGER.info("skipped preloading reintroduced products");
		}
	}

	
	
	
	public static void warmupGroupes() throws FDResourceException {
		LOGGER.info("Loading grp data");
		Collection<FDGroup> grpInfoList=FDGrpInfoManager.loadAllGrpInfoMaster();
		@SuppressWarnings("unchecked")
		final Collection<GroupScalePricing> grpInfos = (Collection<GroupScalePricing>) FDCachedFactory.getGrpInfos((FDGroup[]) grpInfoList.toArray(new FDGroup[0]));
		LOGGER.info("Lightweight grp data loaded size is :"+grpInfos.size());
	}

}
