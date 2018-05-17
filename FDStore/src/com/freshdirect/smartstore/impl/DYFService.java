package com.freshdirect.smartstore.impl;

import java.util.List;

import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.core.ServiceLocator;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.ejb.DyfModelHome;
import com.freshdirect.smartstore.impl.SessionCache.TimedEntry;
import com.freshdirect.smartstore.sampling.ImpressionSampler;
import com.freshdirect.smartstore.sampling.RankedContent;

/**
 * Base class of DYF Recommendation Services *
 * 
 * @author segabor
 * 
 */
@Deprecated
public abstract class DYFService extends BaseContentKeyRecommendationService {
	private static final Category LOGGER = LoggerFactory.getInstance(AbstractRecommendationService.class);

	// cache the most recently accessed order histories
	private SessionCache<String, TimedEntry<List<RankedContent.Single>>> cache = new SessionCache<String, TimedEntry<List<RankedContent.Single>>>();

	protected ServiceLocator serviceLocator;

	public DYFService(Variant variant, ImpressionSampler sampler, boolean includeCartItems) {
		super(variant, sampler, includeCartItems);

		// connect to database
		try {
			this.serviceLocator = new ServiceLocator(FDStoreProperties.getInitialContext());
		} catch (NamingException e) {
			LOGGER.error("Failed to instantiate MostFrequentlyBoughtVariant", e);
		}
	}

	protected SessionCache<String, TimedEntry<List<RankedContent.Single>>> getCache() {
		return this.cache;
	}

	protected DyfModelHome getModelHome() {
		try {
			return (DyfModelHome) serviceLocator.getRemoteHome("freshdirect.smartstore.DyfModelHome");
		} catch (NamingException e) {
			throw new FDRuntimeException(e);
		}
	}
}
