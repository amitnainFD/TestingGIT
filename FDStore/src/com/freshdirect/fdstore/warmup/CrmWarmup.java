package com.freshdirect.fdstore.warmup;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.ContentKey.InvalidContentKeyException;
import com.freshdirect.cms.fdstore.FDContentTypes;
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
import com.freshdirect.framework.core.ServiceLocator;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.test.TestSupport;
import com.freshdirect.test.ejb.TestSupportHome;
import com.freshdirect.test.ejb.TestSupportSB;

public class CrmWarmup implements ServletContextListener {

	private static Logger LOGGER = LoggerFactory.getInstance(CrmWarmup.class.getSimpleName());
	
	private static DeferredWarmup df = null;
	
	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		final ServletContext ctx = event.getServletContext();
		
		if (ctx != null) {
			LOGGER.debug("Event captured from context path: " + ctx.getContextPath() + ", name: '" + ctx.getServletContextName() + "'");
		}
		
		// Trigger deferred warmup
		synchronized (this) {
			if (df == null) {
				df = new DeferredWarmup(this);

				new Thread(df).start();
				
				LOGGER.debug("Deferred warmup kicked off");
			} else {
				LOGGER.debug("Deferred warmup already running");
			}
		}
	}
	
	



	/** 
	 * Main warmup method
	 */
	private void doWarmup() {
		LOGGER.info("Warmup started");
		final long time = System.currentTimeMillis();


		// Obtain SKU CODES
		final Collection<String> skuList = getSkuCodes();
		if (skuList != null) {
			LOGGER.info("Loaded " + skuList.size() + " sku codes");
		} else {
			LOGGER.error("No sku codes found");
			// FIXME
			return;
		}

		
		
		// -- warmup caches, round #1
		CacheWarmupUtil.warmupFDCaches();

		
		LOGGER.info("main warmup in " + (System.currentTimeMillis() - time) + " ms");

		// -- warmup stage #2
		
		try {
			CacheWarmupUtil.warmupZones();
		} catch (FDResourceException e) {
			LOGGER.error("warmupZones failed", e);
		}

		try {
			final UserContext ctx = ContentFactory.getInstance().getCurrentUserContext();
			
			CacheWarmupUtil.warmupProducts(skuList, ctx);
		} catch (FDResourceException e) {
			LOGGER.error("warmupProducts failed", e);
		}

		try {
			CacheWarmupUtil.warmupProductNewness();
		} catch (FDResourceException e) {
			LOGGER.error("warmupProductNewness failed", e);
		}

		try {
			CacheWarmupUtil.warmupGroupes();
		} catch (FDResourceException e) {
			LOGGER.error("warmupGroupes failed", e);
		}

	
	
	}

	

	/**
	 * Pick SKU codes from ERPS
	 * @return
	 */
	private Collection<String> getSkuCodes() {
		TestSupport ts = TestSupport.getInstance();
		Collection<String> skuList = ts.getSkuCodes();
			
		return skuList;
	}









	private static class DeferredWarmup implements Runnable {
		private static Logger LOGGER = LoggerFactory.getInstance(DeferredWarmup.class.getSimpleName());
		private static int NUM_ATTEMPTS = 25;


		private final CrmWarmup task;
		
		private ServiceLocator serviceLocator = null;		
		private TestSupportHome home = null;

		public DeferredWarmup(CrmWarmup warmupTask) {
			
			this.task = warmupTask;

		}
		
		
		@Override
		public void run() {
			TestSupportSB ts = null;
			
			LOGGER.info("** Background thread started, max attempts=" + NUM_ATTEMPTS + " **");
			
			for (int attempt=0; attempt < NUM_ATTEMPTS && ts == null; attempt++) {
				try {
					if (home == null) {
						serviceLocator = new ServiceLocator(ErpServicesProperties.getInitialContext());
						
						home = (TestSupportHome) serviceLocator.getRemoteHome(
									"freshdirect.test.TestSupport");
					}
					ts = home.create();
				} catch (RemoteException e) {
					LOGGER.debug("Attempt failed [remote exception]");
				} catch (CreateException e) {
					LOGGER.debug("Attempt failed [create exception]");
				} catch (NamingException e) {
					LOGGER.debug("Attempt failed [naming exception]");
				}
				
				if (ts == null) {
					LOGGER.warn("Remote object is not yet available. Remaining attempts: " + ( (NUM_ATTEMPTS-1)-attempt));
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// suppress error message
					}
				}
			}

			

			if (ts != null) {
				LOGGER.info("Remote object obtained, proceed with warmup");
			} else {
				LOGGER.error("Remote object is not available, abort");
				return;
			}


			// so do it
			final long t0 = System.currentTimeMillis();
			LOGGER.info("** Warmup ended successfully **");
			
			try {
				task.doWarmup();
			} catch (Exception exc) {
				LOGGER.error("** Warmup task crashed **", exc);
			}
			
			final long t1 = System.currentTimeMillis();
			LOGGER.info("** Warmup ended taking " + ( Math.round( ((double)( t1-t0 )) / 1000.0) ) + "sec **");
		}
	}
}
