/*
 * $Workfile$
 *
 * $Date$
 * 
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.warmup;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.oauth.OAuthException;

import org.apache.log4j.Category;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.ContentType;
import com.freshdirect.cms.application.CmsManager;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ContentSearch;
import com.freshdirect.fdstore.content.WineFilterPriceIndex;
import com.freshdirect.fdstore.content.WineFilterRatingIndex;
import com.freshdirect.fdstore.content.customerrating.CustomerRatingsContext;
import com.freshdirect.fdstore.oauth.provider.OAuthProvider;
import com.freshdirect.fdstore.sitemap.SitemapDataFactory;
import com.freshdirect.fdstore.zone.FDZoneInfoManager;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.service.CmsRecommenderRegistry;
import com.freshdirect.smartstore.service.SearchScoringRegistry;
import com.freshdirect.smartstore.service.VariantRegistry;

/**
 * 
 * 
 * @version $Revision$
 * @author $Author$
 */
public class Warmup {

	private static Category LOGGER = LoggerFactory.getInstance(Warmup.class);

	protected Set<String> skuCodes;
	protected ContentFactory contentFactory;

	public Warmup() {
		this(ContentFactory.getInstance());
	}

	public Warmup(ContentFactory contentFactory) {
		this.contentFactory = contentFactory;
		this.skuCodes = new HashSet<String>(8000);
	}

	public void warmup() {

		LOGGER.info("Warmup started");
		warmupOAuthProvider();
		
		long time = System.currentTimeMillis();
		contentFactory.getStore();
		LOGGER.info("Store warmup in " + (System.currentTimeMillis() - time) + " ms");

		Set<ContentKey> skuContentKeys = CmsManager.getInstance().getContentKeysByType(FDContentTypes.SKU);
		for (Iterator<ContentKey> i = skuContentKeys.iterator(); i.hasNext();) {
			ContentKey key = (ContentKey) i.next();
			skuCodes.add(key.getId());
		}

		LOGGER.info(skuCodes.size() + " SKUs found");

		CacheWarmupUtil.warmupFDCaches();

		// load the customer ratings
		CustomerRatingsContext.getInstance().getCustomerRatings();

		LOGGER.info("main warmup in " + (System.currentTimeMillis() - time) + " ms");

		new Thread("warmup-step-2") {
			public void run() {
				try {
					final UserContext ctx = ContentFactory.getInstance().getCurrentUserContext();

					// Warmup
					CacheWarmupUtil.warmupZones();
					CacheWarmupUtil.warmupProducts(skuCodes, ctx);
					if (FDStoreProperties.isPreloadAutocompletions()) {
						ContentSearch.getInstance().getAutocompletions("qwertyuqwerty");
						ContentSearch.getInstance().getBrandAutocompletions("qwertyuqwerty");
					}

					CacheWarmupUtil.warmupProductNewness();

					CacheWarmupUtil.warmupGroupes();
					contentFactory.refreshWineIndex(true);
					WineFilterPriceIndex.getInstance();
					WineFilterRatingIndex.getInstance();

					if (FDStoreProperties.isPreloadSmartStore()) {
						LOGGER.info("preloading Smart Store");
						VariantRegistry.getInstance().reload();
						CmsRecommenderRegistry.getInstance().reload();
						SearchScoringRegistry.getInstance().load();
					} else {
						LOGGER.info("skipped preloading Smart Store");
					}

					warmupSmartCategories();
					
					SitemapDataFactory.create();

					LOGGER.info("Warmup done");
				} catch (FDResourceException e) {
					LOGGER.error("Warmup failed", e);
				}
			}
		}.start();
	}

	private void warmupSmartCategories() {
		Set<ContentKey> categories = CmsManager.getInstance().getContentKeysByType(ContentType.get("Category"));
		LOGGER.info("found " + categories.size() + " categories");
		try {
			@SuppressWarnings("unchecked")
			Collection<String> zones = FDZoneInfoManager.loadAllZoneInfoMaster();
			for (ContentKey catKey : categories) {
				ContentNodeModel node = contentFactory.getContentNodeByKey(catKey);
				if (node instanceof CategoryModel) {
					CategoryModel category = (CategoryModel) node;
					if (category.getRecommender() != null || category.getProductPromotionType() != null) {
						LOGGER.info("category " + category.getContentName() + " is smart or promo, pre-loading child products for " + zones.size() + " zones");
						for (String zone : zones) {
							category.getProducts();
						}
					}
				}
			}
		} catch (FDResourceException e) {
			LOGGER.error("cannot load zones for Smart Categories", e);
		}
	}


	private void warmupOAuthProvider(){
		try {
			OAuthProvider.deleteOldAccessors();
		} catch (OAuthException e) {
			LOGGER.error("OAuth warmup error",e);
		}
	}
}
