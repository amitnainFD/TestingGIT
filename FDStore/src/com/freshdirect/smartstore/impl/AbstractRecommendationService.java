package com.freshdirect.smartstore.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;

import com.freshdirect.WineUtil;
import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ContentNodeModelUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.sampling.ConfiguredImpressionSampler;
import com.freshdirect.smartstore.sampling.ImpressionSampler;
import com.freshdirect.smartstore.sampling.ListSampler;
import com.freshdirect.smartstore.sampling.RankedContent;

/**
 * Simple abstract implementation of recommendation service It does nothing but
 * store it's variant ID
 * 
 * Subclasses must implement recommendNodes() method.
 * 
 * @author segabor
 * 
 */
public abstract class AbstractRecommendationService implements RecommendationService {
	
	private static Category LOGGER = LoggerFactory.getInstance(AbstractRecommendationService.class);

	protected Variant variant;

	protected ImpressionSampler sampler;

	private ImpressionSampler deterministicSampler;

	private boolean includeCartItems;
	
	/**
	 * ThreadLocal<Map<String:ContentKey.id,String:Recommender.id>>
	 */
	public static ThreadLocal<Map<String, String>> RECOMMENDER_SERVICE_AUDIT = new ThreadLocal<Map<String, String>>();

	/**
	 * ThreadLocal<Map<String:ContentKey.id,String:RecommenderStrategy.id>>
	 */
	public static ThreadLocal<Map<String, String>> RECOMMENDER_STRATEGY_SERVICE_AUDIT = new ThreadLocal<Map<String, String>>();

	protected static List<RankedContent.Single> rankListByOrder(List<? extends ContentNodeModel> nodes) {
		int size = nodes.size();
		List<RankedContent.Single> rankedContents = new ArrayList<RankedContent.Single>(size);
		for (int i = 0; i < size; i++) {
			rankedContents.add(new RankedContent.Single(size + 1 - i, nodes.get(i)));
		}
		return rankedContents;
	}

	public AbstractRecommendationService(Variant variant, ImpressionSampler sampler, boolean includeCartItems) {
		this.variant = variant;
		this.sampler = sampler;
		if (sampler != null)
			this.deterministicSampler =
					new ConfiguredImpressionSampler(sampler.getConsiderationLimit(), sampler.isCategoryAggregationEnabled(), false, ListSampler.ZERO);
		this.includeCartItems = includeCartItems;
	}

	public Variant getVariant() {
		return this.variant;
	}

	final public List<ContentNodeModel> recommendNodes(SessionInput input) {
		boolean saveIncludeCartItems = input.isIncludeCartItems();
		if (!input.isIncludeCartItems()) {
			input.setIncludeCartItems(includeCartItems);
		}
		if (sampler != null) {
			input.setUseAlternatives(sampler.isUseAlternatives());
		}
		input.setShowTemporaryUnavailable(variant.getServiceConfig().isShowTempUnavailable());
		input.setBrandUniqSort( variant.getServiceConfig().isBrandUniqSort() );
		
		List<ContentNodeModel> recNodes = doRecommendNodes(input);
		input.setIncludeCartItems(saveIncludeCartItems);
		return recNodes;
	}

	protected abstract List<ContentNodeModel> doRecommendNodes(SessionInput input);

	public String getDescription() {
		return "";
	}

	protected List<ContentNodeModel> sample(SessionInput input, List<RankedContent.Single> nodes, boolean aggregatable) {
		Set<ContentNodeModel> emptySet = Collections.emptySet();
		return sample(input, nodes, aggregatable, emptySet);
	}

	/**
	 * randomize the list of RankedContent.Single
	 * 
	 * @param input
	 * @param nodes
	 *            List<RankedContent.Single>
	 * @return List<ContentNodeModel>
	 */
	protected List<ContentNodeModel> sample(SessionInput input, List<RankedContent.Single> nodes, boolean aggregatable, Collection<ContentNodeModel> excludeNodes) {
		Set<ContentKey> exclusions = new HashSet<ContentKey>();

		exclusions.addAll( input.getExclusions() );
		// prefer explicit exclusion list
		if (!excludeNodes.isEmpty())
			for (ContentNodeModel excludeNode : excludeNodes) {
				exclusions.add(excludeNode.getContentKey());
			}


		// [APPDEV-2241] sort out wines
		if (input.isExcludeAlcoholicContent()) {
			LOGGER.debug("sampler: exclude alcoholic item enabled");
			for (RankedContent.Single rc : nodes) {
				ContentNodeModel m = rc.getModel();
				
				final ContentNodeModel dept = ContentNodeModelUtil.findDepartment(m);
				if (dept != null) {
					final ContentKey aKey = dept.getContentKey();

					if (aKey != null && WineUtil.getWineAssociateId().toLowerCase().equalsIgnoreCase(aKey.getId())) {
						LOGGER.debug("sampler: exclude alcoholic item: " + aKey );
						exclusions.add(rc.getContentKey());
					}
				}
			}
		}
		
		return sample(input, nodes, aggregatable, exclusions);
	}

	private List<ContentNodeModel> sample(SessionInput input, List<RankedContent.Single> nodes, boolean aggregatable, Set<ContentKey> exclusions) {
		List<ContentKey> samplingResult = (input.isNoShuffle() ? deterministicSampler : sampler).sample(nodes, aggregatable, exclusions, input.isShowTemporaryUnavailable());
		List<ContentNodeModel> result = new ArrayList<ContentNodeModel>(samplingResult.size());
		for (ContentKey contentKey : samplingResult) {
			ContentNodeModel node = ContentFactory.getInstance().getContentNodeByKey(contentKey);
			if (node != null)
				result.add(node);
		}
		return result;
	}

	public boolean isIncludeCartItems() {
		return includeCartItems;
	}

	public boolean isSmartSavings() {
		return false;
	}

	public boolean isRefreshable() {
		return !(sampler == null || sampler.isDeterministic());
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[sampler=" + sampler + ", includeCartItems=" + isIncludeCartItems() + "]";
	}
}
