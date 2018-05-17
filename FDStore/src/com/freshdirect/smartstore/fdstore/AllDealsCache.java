package com.freshdirect.smartstore.fdstore;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.ContentType;
import com.freshdirect.cms.application.CmsManager;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.ZonePriceListing;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.PriceCalculator;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.zone.FDZoneInfoManager;
import com.freshdirect.framework.util.log.LoggerFactory;

public class AllDealsCache {
	
	private static final Logger LOGGER = LoggerFactory.getInstance(AllDealsCache.class);

	private static final Logger LOGGER_LOADER = LoggerFactory.getInstance(Loader.class);
	
	private static final int HOUR_IN_MILLIS = 60 * 60 * 1000;

	private static Executor cacheThreadPool = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardPolicy());

	private static AllDealsCache INSTANCE;

	private static final Object reloadSync = new Object();

	public synchronized static AllDealsCache getInstance() {
		if (INSTANCE == null)
			INSTANCE = new AllDealsCache();

		return INSTANCE;
	}

	private class Loader implements Runnable {

		@Override
		public void run() {
			if (FDStoreProperties.isAllDealsCacheEnabled()) {
				LOGGER_LOADER.info("run() entry");
				// avoid doing reload in parallel
				synchronized (reloadSync) {
					LOGGER_LOADER.info("reloading deals cache");
					try {
						@SuppressWarnings("unchecked")
						Collection<String> zones = FDZoneInfoManager.loadAllZoneInfoMaster();

						LOGGER_LOADER.info("found " + zones.size() + ", currently: " + zoneIndexes.size());
						for (String zone : zones) {
							int index = zoneIndexes.size();
							if (!zoneIndexes.containsKey(zone))
								zoneIndexes.put(zone, index);
						}
						LOGGER_LOADER.info("loaded " + zoneIndexes.size() + " zones");

						Collection<ContentKey> products = CmsManager.getInstance().getContentKeysByType(ContentType.get("Product"));
						LOGGER_LOADER.info("found " + products.size() + " product candidates for deals cache reload");
						int i = 0;
						for (ContentKey product : products) {
							double[][] value = deals.get(product);
							if (value == null) {
								value = new double[2][zoneIndexes.size()];
								deals.put(product, value);
							}

							// check if size increased
							if (value[0].length < zoneIndexes.size()) {
								//LOGGER_LOADER.debug("Array before copy ");
								//print(value);
								double newValue[][] = new double[2][zoneIndexes.size()];
								System.arraycopy(value[0], 0, newValue[0], 0, value[0].length);
								System.arraycopy(value[1], 0, newValue[1], 0, value[1].length);
								value=newValue;
								//LOGGER_LOADER.debug("Array after copy ");
								//print(value);
								deals.put(product, value);
							}

							for (Map.Entry<String, Integer> zoneEntry : zoneIndexes.entrySet()) {
								ProductModel contentNode = (ProductModel) ContentFactory.getInstance().getContentNodeByKey(product);
								if (contentNode != null && !contentNode.isDiscontinued()) {
									PriceCalculator pc = new PriceCalculator(new PricingContext(/*::FDX::zoneEntry.getKey()::FDX::*/ZonePriceListing.DEFAULT_ZONE_INFO), contentNode);
									value[0][zoneEntry.getValue()] = pc.getDealPercentage();
									value[1][zoneEntry.getValue()] = pc.getTieredDealPercentage();
								}
							}
							i++;
							if (i % 1000 == 0)
								LOGGER_LOADER.info("processed " + i + " products so far");
						}
					} catch (FDResourceException e) {
						LOGGER_LOADER.error("failed to reload / initialize all deals cache", e);
					}
					LOGGER_LOADER.info("deals cache reloaded");
				}
				LOGGER_LOADER.info("run() exit");
			}
		}
	}

	Map<ContentKey, double[][]> deals;
	Map<String, Integer> zoneIndexes;

	long lastRefresh;
	boolean initialized;

	public static void print(double[][] data) {
		LOGGER_LOADER.debug("[ ");
		for(int i=0;i<data.length;i++) {
			for(int j=0;j<data[i].length;j++) {
				if(j==0)LOGGER_LOADER.debug("[");
				
				LOGGER_LOADER.debug(data[i][j]+", ");
			};
			LOGGER_LOADER.debug("] ");
		}LOGGER_LOADER.debug(" ]");
	}
	private AllDealsCache() {
		zoneIndexes = new ConcurrentHashMap<String, Integer>(20); // 32 entries should be enough
		deals = new ConcurrentHashMap<ContentKey, double[][]>(50000); // 65536 entries should be enough
		lastRefresh = Integer.MIN_VALUE;
		initialized = false;
	}

	public double getRegularDeal(ContentKey key, PricingContext pricingContext) {
		return getDeal(key, pricingContext, 0);
	}

	public double getTieredDeal(ContentKey key, PricingContext pricingContext) {
		return getDeal(key, pricingContext, 1);
	}

	public double getHighestDeal(ContentKey key, PricingContext pricingContext) {
		return getDeal(key, pricingContext, -1);
	}

	private double getDeal(ContentKey key, PricingContext pricingContext, int dealIndex) {
		reload();
		if (key == null || pricingContext == null)
			return 0.;

		double[][] value = deals.get(key);
		if (value != null) {
			Integer zoneIndex = zoneIndexes.get(pricingContext.getZoneInfo().getPricingZoneId()/*::FDX::*/);
			if (zoneIndex == null)
				return 0.;

			if (dealIndex < 0)
				return Math.max(value[0][zoneIndex], value[1][zoneIndex]);
			
			return value[dealIndex][zoneIndex];
		}		
		return 0.;
	}

	// synchronized protects only the lastRefresh and initialized variables
	// as these variables are accessed only in this method
	public synchronized void reload() {
		long now = System.currentTimeMillis();

		if (now > (lastRefresh + HOUR_IN_MILLIS)) {
			forceReload();
			lastRefresh = now;
		}
	}

    /**
     * 
     */
    public void forceReload() {
        if (initialized) {
        	// reload cache asynchronously
        	LOGGER.info("reloading deals cache asynchronously");
        	cacheThreadPool.execute(new Loader());
        } else {
        	initialized = true;
        	// reload cache synchronously
        	LOGGER.info("reloading deals cache synchronously");
        	new Loader().run();
        }
    }
}
