package com.freshdirect.smartstore.impl;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.fdstore.SmartStoreUtil;
import com.freshdirect.smartstore.sampling.ImpressionSampler;

/**
 * @author csongor
 */
public class ClassicYMALRecommendationService extends AbstractRecommendationService {
	private static final Category LOGGER = LoggerFactory.getInstance(ClassicYMALRecommendationService.class);
	
	public ClassicYMALRecommendationService(Variant variant, ImpressionSampler sampler, boolean includeCartItems) {
		super(variant, sampler, includeCartItems);
	}

	/**
	 * Recommends products for the current YMAL source
	 */
	public List<ContentNodeModel> doRecommendNodes(SessionInput input) {
		List prodList;

		if (input.getYmalSource() != null) {
			prodList = input.getYmalSource().getYmalProducts();
			SmartStoreUtil.clearConfiguredProductCache();
			prodList = SmartStoreUtil.addConfiguredProductToCache(prodList);
			return sample(input, rankListByOrder(prodList), false);
		} else {
			LOGGER.info("ymal source is null: returning empty list");
			return Collections.emptyList();
		}
	}
}
