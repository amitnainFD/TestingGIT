package com.freshdirect.smartstore.sampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.SkuModel;
import com.freshdirect.smartstore.filter.ContentFilter;
import com.freshdirect.smartstore.filter.FilterFactory;

/**
 * Sampler that produces the Content Keys for impressions.
 * 
 * @author istvan, csongor
 * 
 */
public abstract class AbstractImpressionSampler implements ImpressionSampler {
	private final ConsiderationLimit considerationLimit;
	private final boolean categoryAggregationEnabled;
	private final boolean useAlternatives;

	/**
	 * Get the highest node the content key is aggregated at.
	 * 
	 * The label is a string. If the product or sku is not aggregated, then the
	 * product key is returned (unless it is orphan). If it is aggregated, then
	 * the highest level category is returned.
	 * 
	 * @param key
	 * @return key of aggregation level
	 */
	protected static ContentKey getAggregationKey(ContentKey key) {
		return new Object() {

			ContentKey getKey(ProductModel product) {
				if (product == null)
					return null; // in case argument is a getParent of a sku
				CategoryModel parent = (CategoryModel) product.getParentNode();
				if (parent == null)
					return null; // orphan
				ContentKey cat = getKey(parent);
				return cat == null ? product.getContentKey() : cat;
			}

			ContentKey getKey(CategoryModel category) {
				ContentKey cat = null;

				while (category != null) {
					if (category.isDYFAggregated())
						cat = category.getContentKey();
					if (category.getParentNode() instanceof CategoryModel) {
						category = (CategoryModel) category.getParentNode();
					} else
						break;
				}
				return cat;
			}

			ContentKey getKey(ContentKey key) {

				ContentNodeModel model = ContentFactory.getInstance().getContentNodeByKey(key);
				if (model == null) {
					return null;
				} else if (model instanceof SkuModel) {
					return getKey((ProductModel) model.getParentNode());
				} else if (model instanceof ProductModel) {
					return getKey((ProductModel) model);
				} else if (model instanceof CategoryModel) {
					return getKey((CategoryModel) model);
				} else
					return key;
			}
		}.getKey(key);
	}

	/**
	 * @param considerationLimit
	 *            the top N and top percentage limit of items (see
	 *            {@link ConsiderationLimit} and {@link SimpleLimit})
	 * @param categoryAggregationEnabled
	 *            tells whether to handle the aggregated weight of the
	 *            categories of products
	 */
	public AbstractImpressionSampler(ConsiderationLimit considerationLimit, boolean categoryAggregationEnabled, boolean useAlternatives) {
		this.considerationLimit = considerationLimit;
		this.categoryAggregationEnabled = categoryAggregationEnabled;
		this.useAlternatives = useAlternatives;
	}

	protected abstract ListSampler createSampler(List<RankedContent> limitedRankedContent);

	@Override
	public final List<ContentKey> sample(List<RankedContent.Single> rankedContent, boolean aggregatable, Set<ContentKey> exclusions, boolean showTempUnavailable) {
		List<? extends RankedContent> rankedAggregatedContent = categoryAggregationEnabled && aggregatable ? aggregateContentList(rankedContent) : rankedContent;
		ContentFilter filter = FilterFactory.getInstance().createFilter(exclusions, useAlternatives, showTempUnavailable);
		ContentSampler sampler = new ContentSampler(rankedAggregatedContent, filter, getConsiderationLimit());
		return sampler.drawWithoutReplacement(createSampler(sampler.getSortedItems()));
	}

	@Override
	public abstract boolean isDeterministic();

	@Override
	public ConsiderationLimit getConsiderationLimit() {
		return considerationLimit;
	}

	@Override
	public boolean isCategoryAggregationEnabled() {
		return categoryAggregationEnabled;
	}
	
	@Override
	public boolean isUseAlternatives() {
		return useAlternatives;
	}

	/**
	 * Aggregate RankedContent.Single items into RankedContent.Aggregate /s
	 * 
	 * @param rankedContentNodes
	 * @return
	 */
	protected List<RankedContent> aggregateContentList(List<RankedContent.Single> rankedContentNodes) {
		List<RankedContent> result = new ArrayList<RankedContent>();

		Map<ContentKey, RankedContent.Aggregate> aggregateMap = new HashMap<ContentKey, RankedContent.Aggregate>();

		for (RankedContent.Single e : rankedContentNodes) {
			ContentKey aggregationLevelKey = getAggregationKey(e.getContentKey());
			if ((aggregationLevelKey != null) && (!aggregationLevelKey.equals(e.getContentKey()))) {
				RankedContent.Aggregate aggregateContent = aggregateMap.get(aggregationLevelKey);
				if (aggregateContent == null) {
					aggregateContent = isDeterministic() ? 
							new RankedContent.DeterministicAggregate(aggregationLevelKey.getId()) :
							new RankedContent.Aggregate(aggregationLevelKey.getId());
					aggregateMap.put(aggregationLevelKey, aggregateContent);
				}
				aggregateContent.add(e);
			} else {
				result.add(e);
			}
		}

		for (Iterator<Map.Entry<ContentKey, RankedContent.Aggregate>> i = aggregateMap.entrySet().iterator(); i.hasNext();) {
			RankedContent.Aggregate agg = i.next().getValue();
			if (agg.getCount() == 1) { // not aggregate
				result.add(agg.takeFirst());
			} else {
				result.add(agg);
			}
		}

		Collections.sort(result);

		return result;
	}
	
	public final List<RankedContent> testAggregateContentList(List<RankedContent.Single> rankedContentNodes) {
		return aggregateContentList(rankedContentNodes);
	}
}
