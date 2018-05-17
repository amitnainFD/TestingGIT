/**
 * 
 */
package com.freshdirect.smartstore.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.fdstore.ProductStatisticsProvider;
import com.freshdirect.smartstore.sampling.ImpressionSampler;
import com.freshdirect.smartstore.sampling.RankedContent;

/**
 * @author zsombor, csongor
 * 
 */
public class AllProductInCategoryRecommendationService extends AbstractRecommendationService {

	public AllProductInCategoryRecommendationService(Variant variant, ImpressionSampler sampler, boolean includeCartItems) {
		super(variant, sampler, includeCartItems);
	}

	@Override
	public List<ContentNodeModel> doRecommendNodes(SessionInput input) {
		if (input.getCurrentNode() != null) {
			ContentNodeModel model = input.getCurrentNode();
			if (model instanceof CategoryModel) {
				ProductStatisticsProvider statisticsProvider = ProductStatisticsProvider.getInstance();
				SortedSet<RankedContent.Single> ordered = new TreeSet<RankedContent.Single>();

				CategoryModel category = (CategoryModel) model;
				collectCategories(statisticsProvider, ordered, category);

				return sample(input, new ArrayList<RankedContent.Single>(ordered), true);
			}
		}
		return Collections.emptyList();
	}

	protected static void collectCategories(ProductStatisticsProvider statisticsProvider, SortedSet<RankedContent.Single> ordered, CategoryModel category) {
		@SuppressWarnings("unchecked")
		List<ProductModel> products = category.getProducts();

		collectProducts(statisticsProvider, ordered, products);

		@SuppressWarnings("unchecked")
		Iterator<CategoryModel> it = category.getSubcategories().iterator();
		while (it.hasNext()) {
			CategoryModel c = it.next();
			collectCategories(statisticsProvider, ordered, c);
		}
	}

	protected static void collectProducts(ProductStatisticsProvider statisticsProvider, SortedSet<RankedContent.Single> ordered, List<ProductModel> products) {
		if ((products == null) || (products.size() == 0)) {
			return;
		}
		for (Iterator<ProductModel> iter = products.iterator(); iter.hasNext();) {
			ProductModel product = (ProductModel) iter.next();
			collectProduct(statisticsProvider, ordered, product);
		}
	}

	protected static void collectProduct(ProductStatisticsProvider statisticsProvider, SortedSet<RankedContent.Single> ordered, ProductModel product) {
		ordered.add(new RankedContent.Single(statisticsProvider.getGlobalProductScore(product.getContentKey()), product));
	}
}
