package com.freshdirect.smartstore.fdstore;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletRequest;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.ProductReference;
import com.freshdirect.fdstore.content.YmalSource;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.ymal.YmalUtil;

public class FDStoreRecommender {

	/**
	 * 
	 * @param user
	 * @return Set<ProductModel>
	 */
	public static Set<ProductModel> getShoppingCartContents( FDUserI user ) {
		return getShoppingCartContents( user.getShoppingCart() );
	}

	/**
	 * 
	 * @param user
	 * @return
	 */
	public static Set<ContentKey> getShoppingCartContentKeys( FDUserI user ) {
		return getShoppingCartContentKeys( user.getShoppingCart() );
	}

	/**
	 * helper to turn a shopping cart into a set of products
	 * 
	 * @return Set<ProductModel>
	 */
	protected static Set<ProductModel> getShoppingCartContents( FDCartModel cart ) {
		List<FDCartLineI> orderlines = cart.getOrderLines();
		Set<ProductModel> products = new HashSet<ProductModel>();
		for ( FDCartLineI cartLine : orderlines ) {
			products.add( cartLine.getProductRef().lookupProductModel() );
		}
		return products;
	}

	protected static Set<ContentKey> getShoppingCartContentKeys( FDCartModel cart ) {
		List<FDCartLineI> orderlines = cart.getOrderLines();
		Set<ContentKey> products = new HashSet<ContentKey>();
		for ( FDCartLineI cartLine : orderlines ) {
			ProductReference productRef = cartLine.getProductRef();
			products.add( productRef.getContentKey() );
		}
		return products;
	}

	/**
	 * Selects the 'best' fitting product from list. This is currently the most
	 * expensive.
	 * 
	 * @param products
	 *            List of ProductModel instances
	 * 
	 * @return The most expensive product as YmalSource
	 */
	public static YmalSource resolveYmalSource(Collection<ProductModel> products, ServletRequest request) {
		if (products == null || products.isEmpty()) {
			return null;
		}
		YmalSource source = Collections.min(products, ProductModel.PRODUCT_MODEL_PRICE_COMPARATOR_INVERSE);
		YmalUtil.resetActiveYmalSetSession(source, request);
		return source;
	}

	/**
	 * This method selects a good ymal source product, with it's parent
	 * category, and assign to the given SessionInput.
	 * 
	 * @param input
	 *            sessionInput
	 * @param products
	 *            List<ProductModel>
	 */
	public static void initYmalSource(SessionInput input, FDUserI user, ServletRequest request) {
		Set<ProductModel> cartContents = FDStoreRecommender.getShoppingCartContents(user);
		YmalSource ymal = resolveYmalSource(cartContents, request);
		if (ymal != null) {
			input.setYmalSource(ymal);
			if (ymal instanceof ProductModel) {
				input.setCategory((CategoryModel) ((ProductModel) ymal).getParentNode());
			}
		}
	}

	public Recommendations getRecommendations(EnumSiteFeature siteFeature, FDUserI user, SessionInput input) throws FDResourceException {
		return getRecommendations(siteFeature, user, input, false);
	}

	public Recommendations getRecommendations(EnumSiteFeature siteFeature, FDUserI user, SessionInput input, boolean ignoreOverriddenVariants) throws FDResourceException {
		Variant variant = VariantSelectorFactory.getSelector(siteFeature).select(user, ignoreOverriddenVariants);
		if (variant == null)
			throw new FDResourceException("error in configuration, no variant for site feature " + siteFeature.getName() + " has been found");

		return getRecommendations(variant, user, input);
	}

	public Recommendations getRecommendations(Variant variant, FDUserI user, SessionInput input) throws FDResourceException {
		RecommendationService service = variant.getRecommender();
		if (service == null)
			throw new FDResourceException("error in configuration, recommender not configured for variant " + variant.getId() + " (site feature " + variant.getSiteFeature().getName() + ")");

		input.setShowTemporaryUnavailable(variant.getServiceConfig().isShowTempUnavailable());
		input.setBrandUniqSort( variant.getServiceConfig().isBrandUniqSort() );
		List<ContentNodeModel> contentModels = service.recommendNodes(input);

		return new Recommendations(variant, contentModels, input, service.isRefreshable(), service.isSmartSavings());
	}

	private static FDStoreRecommender instance = null;

	public static synchronized FDStoreRecommender getInstance() {
		if (instance == null) {
			instance = new FDStoreRecommender();
		}
		return instance;
	}
}
