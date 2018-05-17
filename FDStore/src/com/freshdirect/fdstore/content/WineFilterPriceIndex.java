/**
 * 
 */
package com.freshdirect.fdstore.content;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.zone.FDZoneInfoManager;
import com.freshdirect.framework.util.BalkingExpiringReference;
import com.freshdirect.framework.util.log.LoggerFactory;

public final class WineFilterPriceIndex extends BalkingExpiringReference<Map<PricingContext,Map<EnumWinePrice,Set<ContentKey>>>> {
	private static final Logger LOGGER = LoggerFactory.getInstance(WineFilterPriceIndex.class);

	private static final long FIVE_MINUTES = 5l * 60l * 1000l;
	
	private static WineFilterPriceIndex instance;
	
	public synchronized static WineFilterPriceIndex getInstance() {
		if (instance == null)
			instance = new WineFilterPriceIndex();
		return instance;
	}

	private WineFilterPriceIndex() {
		super(FIVE_MINUTES);
		try {
			set(load());
		} catch (RuntimeException e) {
			LOGGER.warn("failed to initialize price cache");
		}
	}

	@Override
	protected Map<PricingContext,Map<EnumWinePrice,Set<ContentKey>>> load() {
		try {
			LOGGER.warn("reloading price cache...");
			Map<PricingContext,Map<EnumWinePrice,Set<ContentKey>>> map = new HashMap<PricingContext,Map<EnumWinePrice,Set<ContentKey>>>();
			@SuppressWarnings("unchecked")
			Collection<String> zones = FDZoneInfoManager.loadAllZoneInfoMaster();
			Collection<ContentKey> keys = ContentFactory.getInstance().getAllWineProductKeys();
			Map<EnumWinePrice, Set<ContentKey>> tmp;
			for (String zone : zones) {
				PricingContext context = new PricingContext(new ZoneInfo(zone,"1000","1000"));//::FDX::
				tmp = new HashMap<EnumWinePrice, Set<ContentKey>>(10);
				for (EnumWinePrice price : EnumWinePrice.values())
					tmp.put(price, new HashSet<ContentKey>(1000));
				for (ContentKey key : keys) {
					ProductModel p = (ProductModel) ContentFactory.getInstance().getContentNodeByKey(key);
					if (p != null) {
						PriceCalculator pc = new PriceCalculator(context, p);
						EnumWinePrice price = EnumWinePrice.getEnumByPrice(pc.getDefaultPriceValue());
						tmp.get(price).add(key);
					}
				}
				for (EnumWinePrice price : EnumWinePrice.values())
					tmp.put(price, Collections.unmodifiableSet(tmp.get(price)));
				map.put(context, Collections.unmodifiableMap(tmp));
			}
			LOGGER.warn("reloaded price cache on " + keys.size() + " products in " + zones.size() + " pricing contexts.");
			return Collections.unmodifiableMap(map);
		} catch (FDResourceException e) {
			throw new RuntimeException(e);
		}
	}
}