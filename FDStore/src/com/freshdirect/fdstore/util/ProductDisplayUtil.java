package com.freshdirect.fdstore.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.content.ConfiguredProduct;
import com.freshdirect.fdstore.content.ConfiguredProductGroup;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.EnumBurstType;
import com.freshdirect.fdstore.content.Image;
import com.freshdirect.fdstore.content.PriceCalculator;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.ProxyProduct;
import com.freshdirect.fdstore.customer.FDUserI;

public class ProductDisplayUtil {
	public static final String URL_PARAM_SEP = "&amp;";
	public static final String IMPRESSION_ID = "impId";

	public static final String PRODUCT_PAGE_BASE = "/product.jsp";
	public static final String CATEGORY_PAGE_BASE = "/category.jsp";
	public static final String DEPARTMENT_PAGE_BASE = "/department.jsp";
	public static final String CART_CONFIRM_PAGE_BASE = "/cart_confirm.jsp";
	public static final String GR_CART_CONFIRM_PAGE_BASE = "/grocery_cart_confirm.jsp";

	public static final String NEW_PRODUCT_PAGE_BASE = "/pdp.jsp";
	
	public final static ProductModel.RatingComparator RATING_COMP = new ProductModel.RatingComparator();


        public static String getProductBurstCode(FDUserI user,
                EnumSiteFeature siteFeature, ProductModel product) {
            return getProductBurstCode(user, siteFeature, product,  product.getPriceCalculator());
        }
        
	public static String getProductBurstCode(FDUserI user,
			EnumSiteFeature siteFeature, ProductModel product, PriceCalculator calculator) {
		Set<EnumBurstType> hb = new HashSet<EnumBurstType>();
		if (EnumSiteFeature.DYF.equals(siteFeature))
			hb.add(EnumBurstType.YOUR_FAVE);
		return new ProductLabeling(user, calculator, hb).getBurstCode();
	}

	public static String getProductRatingCode(FDUserI user, ProductModel product)
			throws FDResourceException, FDSkuNotFoundException {
		String rating = user.isProduceRatingEnabled() ? product
				.getProductRating() : null;
		if (rating.length() < 2)
			rating = null;
		return rating;
	}

	/**
	 * Get the "real" parent of the content node.
	 * 
	 * In the case of {@link ProxyProduct}s the "real" parent is that of the
	 * product wrapped.
	 * 
	 * @param model
	 * @return
	 */
	public static ContentNodeModel getRealParent(ContentNodeModel model) {
		if (model instanceof ConfiguredProductGroup) {
			ConfiguredProductGroup configuredProductGroup = (ConfiguredProductGroup) model;
			return getRealParent(configuredProductGroup.getProduct());
		} else if (model instanceof ConfiguredProduct) {
			ConfiguredProduct configuredProduct = (ConfiguredProduct) model;
			return getRealParent(configuredProduct.getProduct());
		} else {
			return model.getParentNode();
		}
	}

	/**
	 * Get the "real" product.
	 * 
	 * In the case of {@link ProxyProduct}s the "real" product is the one
	 * wrapped.
	 * 
	 * @param productModel
	 * @return
	 */
	public static ProductModel getRealProduct(ProductModel productModel) {
		if (productModel instanceof ConfiguredProductGroup) {
			return ((ConfiguredProductGroup) productModel).getProduct();
		} else if (productModel instanceof ConfiguredProduct) {
			return ((ConfiguredProduct) productModel).getProduct();
		} else {
			return productModel;
		}
	}

	public static String getProductURI(ProductModel productNode) {
		StringBuffer uri = new StringBuffer();

		uri.append(PRODUCT_PAGE_BASE);
		uri.append("?catId=" + getRealParent(productNode).getContentName());

		uri.append(URL_PARAM_SEP);
		uri.append("productId=" + getRealProduct(productNode).getContentName());

		return uri.toString();
	}

    /**
     * This method returns only regular deals percentage if product's default
     * sku is part of a group scale else returns product's highest deals
     * percentage.
     * 
     * @param productNode
     * @param user
     * @return
     */
    public static int getDealsPercentage(ProductModel productNode, FDUserI user) {
        PriceCalculator calculator = new PriceCalculator(user.getPricingContext(), productNode);
        return getDealsPercentage(calculator);
    }

    /**
     * This method returns only regular deals percentage if product's default
     * sku is part of a group scale else returns product's highest deals
     * percentage.
     * 
     * @param calculator
     * @return
     */
    public static int getDealsPercentage(PriceCalculator calculator) {
    	/*
        try {
            int tieredPercentage = calculator.getTieredDealPercentage();
            FDGroup group = calculator.getFDGroup();
            if (tieredPercentage > 0 && group != null) {
                // Check to see current pricing zone has group price defined.
                if (group != null) {
                    MaterialPrice gsPrice = GroupScaleUtil.getGroupScalePrice(group, calculator.getPricingContext().getZoneId());
                    if (gsPrice != null) {
                        // return regular deal percentage
                        return calculator.getDealPercentage();
                    }
                }
            }
        } catch (FDResourceException e) {
            // ignore
        }
        */
        // At this point there is no gs price defined for default sku.
    	int tieredPercentage = calculator.getTieredDealPercentage();
    	if (tieredPercentage > 0) {
    		return calculator.getHighestDealPercentage();
    	} else if(calculator.getFDGroup() != null) {
    		return calculator.getGroupDealPercentage();
    	}
    	return 0;
    }


    
    
    public static int getMaxHeight(List<? extends ContentNodeModel> nodes) {
    	int maxHeight = 0;
		for (ContentNodeModel node : nodes) {
			if (node instanceof ProductModel) {
				// retrieve product image
				Image prodImage = ((ProductModel) node).getSourceProduct().getCategoryImage();
	
				if (prodImage != null)
					maxHeight = Math.max(maxHeight, prodImage.getHeight());
			}
		}
		
		return maxHeight;
    }
}
