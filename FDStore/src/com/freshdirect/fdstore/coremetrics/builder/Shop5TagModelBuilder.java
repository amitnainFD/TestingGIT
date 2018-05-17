package com.freshdirect.fdstore.coremetrics.builder;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.content.ProductReference;
import com.freshdirect.fdstore.coremetrics.tagmodel.ShopTagModel;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.framework.util.log.LoggerFactory;

public class Shop5TagModelBuilder extends AbstractShopTagModelBuilder{
	private static final Logger LOGGER = LoggerFactory.getInstance(Shop5TagModelBuilder.class);
	
	/**
	 * Set of cart lines. Overrides recent order lines if set, even list has zero items.
	 */
	private List<FDCartLineI> explicitList;

	public void setExplicitList(List<FDCartLineI> explicitList) {
		this.explicitList = explicitList;
	}


	public List<ShopTagModel> buildTagModels() throws SkipTagException {
		
		if (cart == null) {
			LOGGER.error("cart is null");
			throw new SkipTagException("cart is null");

		} else {
			createTagModels(collectRecentKeys());
		}
		return tagModels;
	}

	
	/**
	 * Return cart lines for CM logging.
	 * By default these are the recent order lines.
	 * But explicit list, if set, takes precedence over recent cart lines.
	 * Note, that even empty list can override!
	 * 
	 * @return
	 */
	protected Collection<FDCartLineI> getCartLines() {
		Collection<FDCartLineI> items = explicitList;
		if (items == null) {
			// ok, fall back to recent order lines
			items = cart.getRecentOrderLines();

			LOGGER.debug("Use recent order lines (" + ( items != null ? items.size() : 0 ) + " items)");
		} else {
			LOGGER.debug("Use explicit items (" + ( items != null ? items.size() : 0 ) + " items)");
		}
		return items;
	}


	private Set<ContentKey> collectRecentKeys(){
		Set<ContentKey> recentKeys = new HashSet<ContentKey>();

		for (FDCartLineI cartLine : getCartLines()) {

			ProductReference recentProductRef = cartLine.getProductRef();
			if (recentProductRef != null) {
				ContentKey recentKey = recentProductRef.getContentKey();
				recentKeys.add(recentKey);
			}
		}
		return recentKeys;
	}

	private void createTagModels(Set<ContentKey> recentKeys) throws SkipTagException{
		
		for (FDCartLineI cartLine : cart.getOrderLines()){
			ProductReference productRef = cartLine.getProductRef();
			
			if (productRef != null && recentKeys.contains(productRef.getContentKey())) {
				tagModels.add(createTagModel(cartLine, productRef));
			}
		}
	}
	
}