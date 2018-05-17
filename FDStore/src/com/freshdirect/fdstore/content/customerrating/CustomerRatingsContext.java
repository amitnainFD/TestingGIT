package com.freshdirect.fdstore.content.customerrating;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.util.BalkingExpiringReference;
import com.freshdirect.framework.util.log.LoggerFactory;

@Deprecated
public class CustomerRatingsContext extends BalkingExpiringReference<Map<String,CustomerRatingsDTO>> {

	private static CustomerRatingsContext instance = null;
	
	public static synchronized CustomerRatingsContext getInstance() {
		return getInstance(FDStoreProperties.getProductRatingRefreshInterval() * 60 * 60 * 1000);
	}

	public static synchronized CustomerRatingsContext getInstance(long refreshPeriod) {
		if (instance == null) {
			instance = new CustomerRatingsContext(refreshPeriod);
		}
		return instance;
	}
	
	private CustomerRatingsContext(long refreshPeriod) {
		super(refreshPeriod);
		this.referent = new HashMap<String,CustomerRatingsDTO>();
	}

	private static final Logger LOGGER = LoggerFactory.getInstance( CustomerRatingsContext.class );

	public static long LAST_REFRESH = 0;
	
	public Map<String,CustomerRatingsDTO> getCustomerRatings() {
		if (FDStoreProperties.isBazaarvoiceEnabled()) {
			return get();
		} else {
			return new HashMap<String,CustomerRatingsDTO>();
		}
	}
	
	public CustomerRatingsDTO getCustomerRatingByProductId(String productId) {
		
		
		Map<String,CustomerRatingsDTO> ratedProducts = getCustomerRatings();
		if(ratedProducts!=null && !ratedProducts.isEmpty()){
			return ratedProducts.get(productId);
		}
		return null;
	}
	
	protected Map<String,CustomerRatingsDTO> load() {
		return Collections.emptyMap();
	}
}
