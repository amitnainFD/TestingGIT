package com.freshdirect.smartstore.external.scarab;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.freshdirect.framework.util.ConfigHelper;
import com.freshdirect.framework.util.log.LoggerFactory;

public class ScarabProperties {
	private static final Logger LOGGER = LoggerFactory.getInstance(ScarabProperties.class);

	private static final String SCARAB_SERVICE = "scarab.service";
	private static final String SCARAB_MERCHANT_ID = "scarab.merchantId";
	private static final String SCARAB_SERVICE_TIMEOUT = "scarab.serviceTimeout";
	private static final String SCARAB_QUERY_ITEM_COUNT = "scarab.queryItemCount";

	private final static long REFRESH_PERIOD = 5 * 60 * 1000; // 5mins

	private static long lastRefresh = 0;

	private static Properties config;

	private final static Properties defaults = new Properties();

	static {
		defaults.put(SCARAB_MERCHANT_ID, "17B3DD8229B8FB27");
		defaults.put(SCARAB_SERVICE_TIMEOUT, "2000");
		defaults.put(SCARAB_QUERY_ITEM_COUNT, "100");
		refresh();
	}

	private static void refresh() {
		refresh(false);
	}

	private synchronized static void refresh(boolean force) {
		long t = System.currentTimeMillis();
		if (force || (t - lastRefresh > REFRESH_PERIOD)) {
			config = ConfigHelper.getPropertiesFromClassLoader("scarab.properties", defaults);
			lastRefresh = t;
			LOGGER.info("Loaded configuration from scarab.properties: " + config);
		}
	}

	private static String get(String key) {
		refresh();
		return config.getProperty(key);
	}

	public static String getScarabServiceString() {
		return get(SCARAB_SERVICE);
	}

	public static String getMerchantId() {
		return get(SCARAB_MERCHANT_ID);
	}
	
	public static int getScarabServiceTimeout() {
		try {
			return Integer.parseInt(SCARAB_SERVICE_TIMEOUT);
		} catch (NumberFormatException e) {
			return 2000; // default
		}
	}

	public static int getScarabQueryItemCount() {
		try {
			return Integer.parseInt(SCARAB_QUERY_ITEM_COUNT);
		} catch (NumberFormatException e) {
			return 100; // default
		}
	}
	
	private ScarabProperties() {
	}
}
