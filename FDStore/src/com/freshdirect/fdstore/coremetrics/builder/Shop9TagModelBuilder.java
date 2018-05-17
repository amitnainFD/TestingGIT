package com.freshdirect.fdstore.coremetrics.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.content.ProductReference;
import com.freshdirect.fdstore.coremetrics.tagmodel.ShopTagModel;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.framework.util.log.LoggerFactory;

public class Shop9TagModelBuilder extends AbstractShopTagModelBuilder {
	private static final Logger LOGGER = LoggerFactory.getInstance(Shop9TagModelBuilder.class);

	protected FDOrderI order;
	protected FDUserI user;

	public List<ShopTagModel> buildTagModels() throws SkipTagException {

		if (order == null) {
			LOGGER.error("order is null");
			throw new SkipTagException("order is null");

		} else if (tagModels == null) {
			LOGGER.error("tagModels is null");
			throw new SkipTagException("tagModels is null");

		} else {
			for (ShopTagModel tagModel : tagModels) {
				tagModel.setRegistrationId(user.getPrimaryKey());
				tagModel.setOrderId(TagModelUtil.getCmOrderId(order));
			}
		}
		return tagModels;
	}

	public List<ShopTagModel> createTagModelPrototypesFromCart() throws SkipTagException {

		if (cart == null) {
			LOGGER.error("cart is null");
			throw new SkipTagException("cart is null");
		
		} else {
			tagModels = new ArrayList<ShopTagModel>();
			for (FDCartLineI cartLine : cart.getOrderLines()) {
				ProductReference productRef = cartLine.getProductRef();
				tagModels.add(createTagModel(cartLine, productRef));
			}
		}
		return tagModels;
	}

	public void setOrder(FDOrderI order) {
		this.order = order;
	}

	public void setUser(FDUserI user) {
		this.user = user;
	}

}