/**
 * 
 */
package com.freshdirect.smartstore.impl;

import java.util.List;
import java.util.Map;

import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ProductModelImpl;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.fdstore.ScoreProvider;
import com.freshdirect.smartstore.sampling.ImpressionSampler;

/**
 * @author zsombor
 *
 */
public class YourFavoritesInCategoryRecommendationService extends ManualOverrideRecommendationService {

    /**
     * @param variant
     */
    public YourFavoritesInCategoryRecommendationService(Variant variant, ImpressionSampler sampler, boolean includeCartItems) {
        super(variant, sampler, includeCartItems);
    }

    protected void fillManualSlots(SessionInput input, CategoryModel category, int slots, List result) {
        String customerId = input.getCustomerId();
        if (customerId!=null) {
            Map userProductScores = ScoreProvider.getInstance().getUserProductScores(customerId);
            if (userProductScores!=null && !userProductScores.isEmpty()) {
                ProductModelImpl pm = findMyMostFavoriteProduct(input, category, userProductScores, 0);
                if (pm!=null) {
                    slots++;
                    result.add(pm);
                }
            }
        }
        
        if (slots>0) {
            super.fillManualSlots(input, category, slots, result);
        }
    }

    ProductModelImpl findMyMostFavoriteProduct(SessionInput input, CategoryModel category, Map userProductScores, float maxScore) {
        ProductModelImpl maxProd = null;

        List subcategories = category.getSubcategories();
        for (int i = 0; i < subcategories.size(); i++) {
            CategoryModel cm = (CategoryModel) subcategories.get(i);
            ProductModelImpl pi = findMyMostFavoriteProduct(input, cm, userProductScores, maxScore);
            if (pi!=null) {
                Float score = (Float) userProductScores.get(pi.getContentKey());
                maxScore = score.floatValue();
                maxProd = pi;
            }
        }
        
        List products = category.getProducts();
        for (int i = 0; i < products.size(); i++) {
            ProductModelImpl p = (ProductModelImpl) products.get(i);
            Float score = (Float) userProductScores.get(p.getContentKey());
            if (score != null && score.floatValue() > maxScore) {
                if (p.isDisplayable()) {
                    if (input.isIncludeCartItems() || !input.getCartContents().contains(p.getContentKey())) {
                        maxScore = score.floatValue();
                        maxProd = p;
                    }
                }
            }
        }
        return maxProd;
    }
}
