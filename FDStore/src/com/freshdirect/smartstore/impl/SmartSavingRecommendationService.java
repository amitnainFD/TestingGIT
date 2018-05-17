/**
 * 
 */
package com.freshdirect.smartstore.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.pricing.ProductModelPricingAdapter;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.WrapperRecommendationService;
import com.freshdirect.smartstore.fdstore.SmartStoreUtil;

/**
 * @author zsombor
 * 
 */
public class SmartSavingRecommendationService extends WrapperRecommendationService {

    /**
     * @param variant
     */
    public SmartSavingRecommendationService(RecommendationService internal) {
        super(internal);
    }

    public List<ContentNodeModel> recommendNodes(SessionInput input) {
    	if(input.isCheckForEnoughSavingsMode()) {
    		return internal.recommendNodes(input);
    	}
    		
        String variantId = getVariant().getId();
        if(!variantId.equals(input.getSavingsVariantId())) {
            return Collections.emptyList();
        }

        List<ContentNodeModel> recNodes;
        LinkedHashSet<ContentNodeModel> itemsInCart = savingItemsInCart(variantId, input.getCartModel());
        Map<String, List<ContentKey>> prevMap = input.getPreviousRecommendations();
        if (prevMap != null && prevMap.get(variantId) != null) {
            recNodes = SmartStoreUtil.toContentNodesFromKeys(prevMap.get(variantId));
        } else {
	        recNodes = getRecommendations(input, itemsInCart);
	        if (prevMap == null) {
	            prevMap = new HashMap<String, List<ContentKey>>();
	            input.setPreviousRecommendations(prevMap);
	        }
	        prevMap.put(variantId, SmartStoreUtil.toContentKeysFromModels(recNodes));
        }

        return recNodes;
    }

	private boolean isAllItemsInCart(List<ContentNodeModel> recNodes, LinkedHashSet<ContentNodeModel> itemsInCart) {
		Set<ContentNodeModel> items = new HashSet<ContentNodeModel>(recNodes);
		items.removeAll(itemsInCart);
		return items.isEmpty();
	}

	private List<ContentNodeModel> getRecommendations(SessionInput input, LinkedHashSet<ContentNodeModel> itemsInCart) {
		if (itemsInCart.size() < input.getMaxRecommendations()) {
            List<ContentNodeModel> internalRec = internal.recommendNodes(input);
            itemsInCart.addAll(internalRec);
        }
        Iterator<ContentNodeModel> it = itemsInCart.iterator();
        List<ContentNodeModel> recNodes = new ArrayList<ContentNodeModel>(input.getMaxRecommendations());
        while (it.hasNext() && recNodes.size() < input.getMaxRecommendations()) {
        	recNodes.add(it.next());
        }
		return recNodes;
	}

	private LinkedHashSet<ContentNodeModel> savingItemsInCart(String variantId, FDCartModel cartModel) {
		LinkedHashSet<ContentNodeModel> cartSuggestions = new LinkedHashSet<ContentNodeModel>();
        for (int i = 0; i < cartModel.numberOfOrderLines(); i++) {
            FDCartLineI orderLine = cartModel.getOrderLine(i);
            if (!cartSuggestions.contains(orderLine.lookupProduct()) && (variantId.equals(orderLine.getSavingsId()))) {
            	ProductModel model = orderLine.lookupProduct();
				// TODO !!! Eliminate the ProductModelPricingAdapter class as it
				// violates the OO contract of ProductModel/ContentNodeModel
				// (equals / hashCode + pricing context) !!!
            	if (model instanceof ProductModelPricingAdapter)
            		model = ((ProductModelPricingAdapter) model).getRealProduct();
                cartSuggestions.add(model);
            }
        }
		return cartSuggestions;
	}
	
	public boolean isSmartSavings() {
		return true;
	}
	
	public boolean isRefreshable() {
		return false;
	}
}
