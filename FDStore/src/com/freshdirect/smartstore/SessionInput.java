package com.freshdirect.smartstore;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.common.context.FulfillmentContext;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductContainer;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.ProductReference;
import com.freshdirect.fdstore.content.YmalSource;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDUserI;

/**
 * Represents session information.
 * 
 * It's not serializable ! BY DESIGN !
 * 
 * @author istvan
 * 
 */
public class SessionInput implements Cloneable {

	private Set<ContentKey> cartContents = null;

	private String customerId;

	private ContentNodeModel currentNode; // used by FI recommenders

	// For debugging purpose
	private boolean noShuffle;

	/**
	 * Helps tracing which datastore feeded a content key
	 */
	private boolean traceMode = false;

	/**
	 * Map to record datasource ids for a generated content key.
	 * This is populated only when traceMode is on. 
	 */
	private Map<ContentKey,Set<String>> dataSourcesMap = new HashMap<ContentKey,Set<String>>();
	
	private CategoryModel category;

	private YmalSource ymalSource;

	private List<? extends ContentNodeModel> explicitList;

	private EnumServiceType customerServiceType;

	private FDCartModel cartModel;

	private Map<String, List<ContentKey>> previousRecommendations;

	int maxRecommendations = Integer.MAX_VALUE;

	/**
	 * Page window size. Defaulted to maxRecommendations if set to 0
	 */
	private int windowSize = 0;
	
	@Deprecated
	private boolean checkForEnoughSavingsMode = false;

	@Deprecated
	private String savingsVariantId;

	private boolean includeCartItems = false;

	private boolean useAlternatives = true;
	
	private boolean showTemporaryUnavailable = false;

	private Set<ContentKey> recentItems;

	//Added for Zone Pricing.
	private PricingContext pricingCtx;
	
	private FulfillmentContext fulfillmentContext = new FulfillmentContext(); //to avoid nulls
	
	/**
	 * Size of the prioritized list. Used by ScriptedRecommender & BrandUniquenessSorter. 
	 */
	private int prioritizedCount = 0;
	
	private boolean brandUniqSort = false;


	/**
	 * [APPDEV-2241] prevent recommending wines
	 */
	private boolean excludeAlcoholicContent = false;

	/**
	 * This variable keeps track of previously invoked CMS recommenders in order to avoid infinite loop.
	 */
	private Set<ContentKey> cmsRecommenderKeys = new HashSet<ContentKey>();
	

	protected SessionInput() {
	}
	
	/**
	 * Constructor.
	 * 
	 * @param customerId
	 *            the customer to recommend for (as ERP id)
	 * @param customerServiceType
	 * 
	 *            the customer's service type (Home, Corporate, etc.)
	 */
	public SessionInput(String customerId, EnumServiceType customerServiceType, PricingContext pricingCtx, FulfillmentContext fulfillmentContext) {
		this.customerId = customerId;
		this.customerServiceType = customerServiceType;
		this.pricingCtx = pricingCtx;
		this.fulfillmentContext = fulfillmentContext;
	}

	/**
	 * Constructor.
	 * 
	 * @param user
	 *            the customer to recommend for.
	 */
	public SessionInput(FDUserI user) {
		if (user != null) {
			this.customerServiceType = user.getUserServiceType();
			this.cartModel = user.getShoppingCart();
			this.savingsVariantId = user.getSavingsVariantId();
			if (user.getIdentity() != null)
				this.customerId = user.getIdentity().getErpCustomerPK();
			initCartContents(user);
			initRecentItems(user);
			this.pricingCtx = user.getUserContext().getPricingContext();
			this.fulfillmentContext = user.getUserContext().getFulfillmentContext(); 
		}
	}

	protected void initCartContents(FDUserI user) {
		List<FDCartLineI> orderlines = user.getShoppingCart().getOrderLines();
		Set<ContentKey> products = new HashSet<ContentKey>(orderlines.size());
		Map<String, Set<ContentKey>> tempSavingItems = new HashMap<String, Set<ContentKey>>();
		for (FDCartLineI cartLine : orderlines) {
			ProductReference productRef = cartLine.getProductRef();
			products.add(productRef.getContentKey());
			String savingsId = cartLine.getSavingsId();
			if (savingsId != null && savingsId.length() != 0) {
				if (!tempSavingItems.containsKey(savingsId)) {
					tempSavingItems.put(savingsId, new HashSet<ContentKey>());
				}
				tempSavingItems.get(savingsId).add(productRef.getContentKey());
			}
		}
		cartContents = products;
	}

	protected void initRecentItems(FDUserI user) {
		List<FDCartLineI> orderlines = user.getShoppingCart().getRecentOrderLines();
		Set<ContentKey> products = new HashSet<ContentKey>(orderlines.size());
		for (FDCartLineI cartLine : orderlines) {
			ProductReference productRef = cartLine.getProductRef();
			products.add(productRef.getContentKey());
		}
		recentItems = products;
	}
	
	/**
	 * Set the cart contents of the user.
	 * 
	 * @param cartContents
	 *            (List<@link {@link ContentKey}>)
	 */
	public void setCartContents(Set<ContentKey> cartContents) {
		this.cartContents = cartContents;
	}

	/**
	 * Get cart contents.
	 * 
	 * If not explicitly set, this method will return null.
	 * 
	 * @return The current cart contents as Collection<@link {@link ContentKey}>
	 */
	public Set<ContentKey> getCartContents() {
		return cartContents != null ? cartContents : Collections.<ContentKey>emptySet();
	}
	
	public void setRecentItems(Set<ContentKey> recentItems) {
		this.recentItems = recentItems;
	}
	
	public Set<ContentKey> getRecentItems() {
		if (recentItems != null)
			return recentItems;
		return Collections.emptySet();
	}

	/**
	 * Get customer id.
	 * 
	 * @return customer id.
	 */
	public String getCustomerId() {
		return customerId;
	}

	public ContentNodeModel getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(ContentNodeModel currentNode) {
		this.currentNode = currentNode;
	}

	public boolean isNoShuffle() {
		// return false;
		return noShuffle;
	}

	public void setNoShuffle(boolean noShuffle) {
		this.noShuffle = noShuffle;
	}

	public boolean isTraceMode() {
		return traceMode;
	}

	/**
	 * Enables or disables tracing data sources. It is turned off by default.
	 * @param traceMode
	 */
	public void setTraceMode(boolean traceMode) {
		this.traceMode = traceMode;
	}
	
	/**
	 * Returns a mapping of content key to source names.
	 * This map is available only if trace mode is on.
	 * 
	 * @return
	 */
	public Map<ContentKey, Set<String>> getDataSourcesMap() {
		return dataSourcesMap;
	}
	
	public void setDataSourcesMap(Map<ContentKey, Set<String>> feederMap) {
		this.dataSourcesMap = feederMap;
	}


	public void traceContentNodes(String dsName, Collection<? extends ContentNodeModel> nodes) {
		if (traceMode) {
			for (ContentNodeModel node : nodes) {
				_traceNode(dsName, node);
			}
		}
	}
	
	public void traceContentNode(String dsName, ContentNodeModel node) {
		if (traceMode) {
			_traceNode(dsName, node);
		}
	}

	protected void _traceNode(String dsName, ContentNodeModel node) {
		ContentKey ck = node.getContentKey();
		
		Set<String> ds = dataSourcesMap.get(ck);
		if (ds == null) {
			ds = new HashSet<String>();
			dataSourcesMap.put(ck, ds);
		}
		// store data source name for the given content key
		ds.add(dsName);
	}


	public void mergeDataSourcesMap(Map<ContentKey, Set<String>> otherMap) {
		for (ContentKey ck : otherMap.keySet()) {
			Set<String> sourceNames = otherMap.get(ck);
			if (dataSourcesMap.containsKey(ck)) {
				dataSourcesMap.get(ck).addAll(sourceNames);
			} else {
				dataSourcesMap.put(ck, new HashSet<String>(sourceNames));
			}
		}
		
	}
	
	public YmalSource getYmalSource() {
		return ymalSource;
	}

	public void setYmalSource(YmalSource ymalSource) {
		this.ymalSource = ymalSource;
	}

	public List<? extends ContentNodeModel> getExplicitList() {
		return explicitList != null ? explicitList : Collections.<ContentNodeModel>emptyList();
	}

	public void setExplicitList(List<? extends ContentNodeModel> explicitList) {
		this.explicitList = explicitList;
	}

	public void setCustomerServiceType(EnumServiceType customerServiceType) {
		this.customerServiceType = customerServiceType;
	}

	public EnumServiceType getCustomerServiceType() {
		return customerServiceType;
	}

	public void setCartModel(FDCartModel cartModel) {
		this.cartModel = cartModel;
	}

	public FDCartModel getCartModel() {
		return cartModel;
	}

	public void setPreviousRecommendations(Map<String, List<ContentKey>> previousRecommendations) {
		this.previousRecommendations = previousRecommendations;
	}

	public Map<String, List<ContentKey>> getPreviousRecommendations() {
		return previousRecommendations;
	}

	public void setMaxRecommendations(int maxRecommendations) {
		this.maxRecommendations = maxRecommendations;
	}

	public int getMaxRecommendations() {
		return maxRecommendations;
	}

	public int getWindowSize() {
		return windowSize;
	}
	
	public int getWindowSize(int defaultValue) {
		return windowSize > 0 ? windowSize : (
				maxRecommendations > 0 ? maxRecommendations : defaultValue
		);
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	@Deprecated
	public boolean isCheckForEnoughSavingsMode() {
		return checkForEnoughSavingsMode;
	}

	@Deprecated
	public void setCheckForEnoughSavingsMode(boolean checkForSavings) {
		this.checkForEnoughSavingsMode = checkForSavings;
	}

	public void setCategory(CategoryModel category) {
		this.category = category;
	}

	// clone support
	@Deprecated
	protected void setSavingsVariantId(String savingsVariantId) {
		this.savingsVariantId = savingsVariantId;
	}

	@Deprecated
	public String getSavingsVariantId() {
		return savingsVariantId;
	}

	public CategoryModel getCategory() {
		if ((category == null) && currentNode instanceof CategoryModel) {
			return (CategoryModel) currentNode;
		} else if (currentNode instanceof ProductModel)
			return (CategoryModel) ((ProductModel) currentNode).getParentNode();
		else
			return category;
	}

	public ProductContainer getFICategory() {
		if ( currentNode instanceof ProductContainer)
			return (ProductContainer)currentNode;
		else if ( currentNode instanceof ProductModel && currentNode.getParentNode() instanceof ProductContainer )
			return (CategoryModel)currentNode.getParentNode();
		else
			return null;
	}

	public boolean isIncludeCartItems() {
		return includeCartItems;
	}

	public void setIncludeCartItems(boolean includeCartItems) {
		this.includeCartItems = includeCartItems;
	}

	public Set<ContentKey> getExclusions() {
		if (includeCartItems) {
			return Collections.emptySet();
		}
		return getCartContents();
	}

	public void setUseAlternatives(boolean useAlternatives) {
		this.useAlternatives = useAlternatives;
	}

	public boolean isUseAlternatives() {
		return useAlternatives;
    }
	
	public boolean isShowTemporaryUnavailable() {
        return showTemporaryUnavailable;
    }
	
	public void setShowTemporaryUnavailable(boolean showTemporaryUnavailable) {
        this.showTemporaryUnavailable = showTemporaryUnavailable;
    }

	public PricingContext getPricingContext() {
		return pricingCtx;
	}

	public void setPricingContext(PricingContext pricingCtx) {
		this.pricingCtx = pricingCtx;
	}
	
	public int getPrioritizedCount() {
		return prioritizedCount;
	}
	
	public void setPrioritizedCount( int prioritizedCount ) {
		this.prioritizedCount = prioritizedCount;
	}

	public void setBrandUniqSort( boolean brandUniqSort ) {
		this.brandUniqSort = brandUniqSort;
	}

	public boolean isBrandUniqSort() {
		return brandUniqSort;
	}


	public boolean isExcludeAlcoholicContent() {

    	//Modified to Include Alcohol Restricted from User Origin : [APPDEV-2857] Blocking Alcohol for customers outside of Alcohol Delivery Area
		return excludeAlcoholicContent || (this.getPricingContext() != null 
												&& this.getFulfillmentContext() != null
													&& this.getFulfillmentContext().isAlcoholRestricted());
	}


	public void setExcludeAlcoholicContent(boolean excludeAlcoholicContent) {
		this.excludeAlcoholicContent = excludeAlcoholicContent;
	}
	
	
	/**
	 * Only for clone support!
	 * 
	 * @param cmsRecommenderKeys
	 */
	public void setCmsRecommenderKeys(Set<ContentKey> cmsRecommenderKeys) {
		this.cmsRecommenderKeys = cmsRecommenderKeys;
	}
	
	public Set<ContentKey> getCmsRecommenderKeys() {
		return cmsRecommenderKeys;
	}

	
	/* User builder function instead */
	@Deprecated
	@Override
	public Object clone() throws CloneNotSupportedException {
		SessionInput cloned = new SessionInput(this.customerId, this.customerServiceType, this.pricingCtx, this.fulfillmentContext);
		
		cloned.setCartContents(this.cartContents);
		// customerId <-- already set by constructor
		cloned.setCurrentNode(currentNode);
		cloned.setNoShuffle(noShuffle);
		cloned.setTraceMode(traceMode);
		cloned.setDataSourcesMap(dataSourcesMap);
		cloned.setCategory(category);
		cloned.setExplicitList(explicitList);
		// customerServiceType <-- already set by constructor
		cloned.setCartModel(cartModel);
		cloned.setCheckForEnoughSavingsMode(checkForEnoughSavingsMode);
		cloned.setSavingsVariantId(savingsVariantId);
		cloned.setIncludeCartItems(includeCartItems);
		cloned.setUseAlternatives(useAlternatives);
		cloned.setShowTemporaryUnavailable(showTemporaryUnavailable);
		cloned.setRecentItems(recentItems);
		// pricingCtx <-- already set by constructor
		cloned.setPrioritizedCount(prioritizedCount);
		
		cloned.setMaxRecommendations(maxRecommendations);
		cloned.setWindowSize(windowSize);
		
		return cloned;
	}





	public FulfillmentContext getFulfillmentContext() {
		return fulfillmentContext;
	}

	public void setFulfillmentContext(FulfillmentContext fulfillmentContext) {
		this.fulfillmentContext = fulfillmentContext;
	}





	public static class Builder {
		private int maxRecommendations = Integer.MAX_VALUE;
		private int windowSize = 0;
		
		private FDUserI user = null;
		
		private String customerId = null;
		private EnumServiceType customerServiceType;
		
		private PricingContext pricingContext;
		private FulfillmentContext fulfillmentContext;
		
		private ContentNodeModel currentNode;
		
		private Set<ContentKey> cartContents = null;
		private boolean excludeCartContent = false;
		private boolean excludeAlcoholicContent = false;

		
		private YmalSource ymalSource = null;


		private Map<String, List<ContentKey>> previousRecommendations;

		private boolean noShuffle;
		private boolean traceMode;
		private Map<ContentKey, Set<String>> dataSourcesMap;

		private List<? extends ContentNodeModel> explicitList;

		private Set<ContentKey> cmsRecommenderKeys;
		
		
		public Builder setMaxRecommendations(int maxRecommendations) {
			this.maxRecommendations = maxRecommendations; return this;
		}
		
		public Builder setWindowSize(int windowSize) {
			this.windowSize = windowSize; return this;
		}

		public Builder setUser(FDUserI user) {
			this.user = user; return this;
		}
		
		public Builder setCustomerId(String customerId) {
			this.customerId = customerId; return this;
		}
		
		public Builder setServiceType(EnumServiceType serviceType) {
			this.customerServiceType = serviceType; return this;
		}
		
		public Builder setPricingContext(PricingContext pricingContext) {
			this.pricingContext = pricingContext; return this;
		}
		
		public Builder setFulfillmentContext(FulfillmentContext fulfillmentContext) {
			this.fulfillmentContext = fulfillmentContext; return this;
		}
		
		public Builder setCurrentNode(ContentNodeModel currentNode) {
			this.currentNode = currentNode; return this;
		}

		public Builder setCartContents(Set<ContentKey> cartContent) {
			this.cartContents = cartContent; return this;
		}
		
		public Builder setExcludeCartContent(boolean excludeCartContent) {
			this.excludeCartContent = excludeCartContent; return this;
		}
		
		public Builder setExcludeAlcoholicContent(boolean excludeAlcoholicContent) {
			this.excludeAlcoholicContent = excludeAlcoholicContent; return this;
		}
		
		
		public Builder setYmalSource(YmalSource ymalSource) {
			this.ymalSource = ymalSource; return this;
		}


		
		public Builder setPreviousRecommendations(
				Map<String, List<ContentKey>> previousRecommendations) {
			this.previousRecommendations = previousRecommendations;
			return this;
		}

		public Builder setNoShuffle(boolean noShuffle) {
			this.noShuffle = noShuffle;
			return this;
		}

		public Builder setTraceMode(boolean traceMode) {
			this.traceMode = traceMode;
			return this;
		}

		public Builder setExplicitList(
				List<? extends ContentNodeModel> explicitList) {
			this.explicitList = explicitList;
			return this;
		}

		public Builder setCmsRecommenderKeys(Set<ContentKey> cmsRecommenderKeys) {
			this.cmsRecommenderKeys = cmsRecommenderKeys; return this;
		}


		public SessionInput build() {
			SessionInput si;
			
			if (user != null) {
				si = new SessionInput(user);
			} else if (customerId != null) {
				si = new SessionInput(customerId, customerServiceType, pricingContext, fulfillmentContext);
			} else {
				si = new SessionInput();
			}

			if (maxRecommendations < Integer.MAX_VALUE) {
				si.setMaxRecommendations(maxRecommendations);
			}
			if (windowSize > 0) {
				si.setWindowSize(windowSize);
			}

			
			if (currentNode != null) {
				si.setCurrentNode(currentNode);
			}
			
			si.setNoShuffle(this.noShuffle);
			si.setTraceMode(traceMode);
			if (traceMode && dataSourcesMap != null) {
				si.setDataSourcesMap(dataSourcesMap);
			}

			if (cartContents != null) {
				si.setCartContents(cartContents);
				si.setIncludeCartItems(!excludeCartContent);
			}
			si.setExcludeAlcoholicContent(excludeAlcoholicContent);

			if (ymalSource != null) {
				si.setYmalSource(ymalSource);
			}
			
			
			if (previousRecommendations != null) {
				si.setPreviousRecommendations(previousRecommendations);
			}

			if (explicitList != null) {
				si.setExplicitList(explicitList);
			}

			if (cmsRecommenderKeys != null) {
				si.setCmsRecommenderKeys(this.cmsRecommenderKeys);
			}

			return si;
		}

		public Builder setDataSourceMap(
				Map<ContentKey, Set<String>> dataSourcesMap) {
			this.dataSourcesMap = dataSourcesMap;
			return this;
		}

	}
}
