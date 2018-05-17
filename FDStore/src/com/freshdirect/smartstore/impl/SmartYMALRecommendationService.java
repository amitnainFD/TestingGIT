package com.freshdirect.smartstore.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.Recommender;
import com.freshdirect.fdstore.content.RecommenderStrategy;
import com.freshdirect.fdstore.content.YmalSet;
import com.freshdirect.fdstore.content.YmalSource;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.fdstore.FactorRequirer;
import com.freshdirect.smartstore.fdstore.SmartStoreUtil;
import com.freshdirect.smartstore.filter.ContentFilter;
import com.freshdirect.smartstore.filter.FilterFactory;
import com.freshdirect.smartstore.service.CmsRecommenderRegistry;

/**
 * @author csongor
 */
public class SmartYMALRecommendationService extends	AbstractRecommendationService implements FactorRequirer {
	
	private static final Category LOGGER = LoggerFactory.getInstance(SmartYMALRecommendationService.class);

	public SmartYMALRecommendationService(Variant variant, boolean includeCartItems) {
		super(variant, null, includeCartItems);
	}

	/**
	 * Recommends products for the current node
	 */
	@Override
	public List<ContentNodeModel> doRecommendNodes(SessionInput input) {
		return recommendYmalItems(input);
	}



	/**
	 * Does the job, gives YMAL recommendation based on session input
	 * 
	 * @param input
	 * @return
	 */
	public static List<ContentNodeModel> recommendYmalItems(SessionInput input) {

		List<ContentNodeModel> prodList = new ArrayList<ContentNodeModel>();
		final YmalSource ymalSource = input.getYmalSource();
		final ProductModel selectedProduct = (ProductModel) input.getCurrentNode();
		int availSlots = input.getMaxRecommendations();
		ContentFilter filter = FilterFactory.getInstance().createFilter(input.getExclusions(), input.isUseAlternatives(), input.isShowTemporaryUnavailable());

		SmartStoreUtil.clearConfiguredProductCache();

		// related products
		List<ProductModel> relatedProducts = ymalSource != null ? 
				ymalSource.getRelatedProducts() : 
				selectedProduct != null ? selectedProduct.getRelatedProducts() : new ArrayList<ProductModel>();
		for ( int i = 0; i < relatedProducts.size(); i++ ) {
			ProductModel pm = relatedProducts.get( i );
			final ProductModel filteredModel = filter.filter( pm );
			if ( filteredModel != null ) {
				ProductModel p = SmartStoreUtil.addConfiguredProductToCache( filteredModel );
				if ( p != null ) {
					prodList.add( p );
					if (input.isTraceMode()) {
						input.traceContentNode("Related Product of "+ymalSource.getContentName(), p);
					}
				}
			}
		}
		availSlots -= prodList.size();

		// smart YMAL products
		YmalSet ymalSet = null;
		if (ymalSource != null)
			ymalSet = ymalSource.getActiveYmalSet();
		if (ymalSet == null && selectedProduct != null)
			ymalSet = selectedProduct.getActiveYmalSet();

		// true YMAL products
		if (ymalSet != null && availSlots > 0) {
			// smart YMAL
			List<Recommender> recommenders = ymalSet.getRecommenders();

			SessionInput smartInput = new SessionInput(input.getCustomerId(),
					input.getCustomerServiceType(), input.getPricingContext(), input.getFulfillmentContext());
			smartInput.setYmalSource(ymalSource);
			smartInput.setCurrentNode(selectedProduct);
			smartInput.setCartContents(addContentKeys(new HashSet<ContentKey>(input.getCartContents()), prodList));
			if (selectedProduct != null)
				smartInput.getCartContents().add(selectedProduct.getContentKey());
			smartInput.setNoShuffle(input.isNoShuffle());

			// APPDEV-1633
			smartInput.setTraceMode(input.isTraceMode());

			Map<String,String> recServiceAudit = new HashMap<String,String>();
			RECOMMENDER_SERVICE_AUDIT.set(recServiceAudit);
			Map<String,String> recStratServiceAudit = new HashMap<String,String>();
			RECOMMENDER_STRATEGY_SERVICE_AUDIT.set(recStratServiceAudit);

			@SuppressWarnings( {"unchecked"} )
			List<ContentNodeModel>[] recommendations = new List[recommenders.size()];

			for (int i = 0; i < recommenders.size(); i++) {

				Recommender rec = recommenders.get(i);
				RecommenderStrategy strategy = rec.getStrategy();
				if (strategy == null) {
					recommendations[i] = Collections.<ContentNodeModel>emptyList();
					continue;
				}

				List<ContentNodeModel> scope = rec.getScope();

				RecommendationService rs = CmsRecommenderRegistry.getInstance().getService(strategy.getContentName());
				if (rs == null) {
					recommendations[i] = Collections.<ContentNodeModel>emptyList();
					continue;
				}
				smartInput.setExplicitList(scope);
				List<ContentNodeModel> recNodes = rs.recommendNodes(smartInput);

				for (int j = 0; j < recNodes.size(); j++) {
					ContentNodeModel model = recNodes.get(j);
					recServiceAudit.put(model.getContentKey().getId(), rec.getContentKey().getId());
					recStratServiceAudit.put(model.getContentKey().getId(), strategy.getContentKey().getId());
				}
				addContentKeys(smartInput.getCartContents(), recNodes);
				recommendations[i] = recNodes;
				if (input.isTraceMode()) {
					input.traceContentNodes(ymalSet.getContentKey().getEncoded() + "[" + rec.getContentKey().getEncoded() + "]", recNodes);
				}
			}

			if (recommenders.size() == 1) {
				final List<ProductModel> prds = SmartStoreUtil.addConfiguredProductToCache(recommendations[0]);
				prodList.addAll(prds);

			} else if (recommenders.size() > 1) {

				int i = 0;

				while (availSlots > 0 && hasAnyItem(recommendations)) {
					ProductModel next;
					ProductModel p;
					do {
						next = recommendations[i].size() > 0 ? (ProductModel) recommendations[i].remove(0) : null;
						p = SmartStoreUtil.addConfiguredProductToCache(next);
					} while (next != null && prodList.contains(p));
					if (next != null) {
						if (p != null) {
							prodList.add(p);
							availSlots--;
						}
					}
					i = (i + 1) % recommendations.length;
				}

			}
		}

		if (ymalSource != null && availSlots > 0) {
			// classic YMAL set products
			List<ProductModel> ymalProducts = ymalSource.getYmalProducts();
			for (ListIterator<ProductModel> it = ymalProducts.listIterator(); it.hasNext();) {
				ProductModel pm = it.next();
				if (filter.filter(pm) != null) {
					ProductModel q = SmartStoreUtil.addConfiguredProductToCache(pm);
					if (prodList.contains(q) || q == null)
						it.remove();
					else {
						it.set(q);
					}
				} else {
					it.remove();
				}
			}
			prodList.addAll(ymalProducts);
			if (input.isTraceMode()) {
				input.traceContentNodes("Related Product of "+ymalSource.getContentName(), ymalProducts);
			}
		} else {
			LOGGER.info("ymal source is null");
		}

		return prodList;
	}

	private static boolean hasAnyItem(List<? extends ContentNodeModel>[] recommendations) {
		for (int i = 0; i < recommendations.length; i++) {
			if (recommendations[i].size() > 0)
				return true;
		}
		return false;
	}

	private static Set<ContentKey> addContentKeys(Set<ContentKey> keys, Collection<? extends ContentNodeModel> nodes) {
		for ( ContentNodeModel node : nodes ) {
			keys.add(node.getContentKey());
		}
		return keys;
	}

	@Override
	public void collectFactors(Collection<String> buffer) {
		// XXX currently loaded in the SmartStoreServiceConfiguration
	}

	@Override
	public boolean isRefreshable() {
		return true;
	}
}
