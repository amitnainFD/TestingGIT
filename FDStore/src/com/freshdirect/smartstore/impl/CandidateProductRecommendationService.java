package com.freshdirect.smartstore.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ContentNodeModelImpl;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.ProductModelImpl;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.fdstore.ProductStatisticsProvider;
import com.freshdirect.smartstore.sampling.ImpressionSampler;
import com.freshdirect.smartstore.sampling.RankedContent;

public class CandidateProductRecommendationService extends AllProductInCategoryRecommendationService {

    public CandidateProductRecommendationService(Variant variant, ImpressionSampler sampler, boolean includeCartItems) {
        super(variant, sampler, includeCartItems);
    }

    public List doRecommendNodes(SessionInput input) {
        List result = Collections.EMPTY_LIST;
        if (input.getCurrentNode() != null) {
            ContentNodeModel model = input.getCurrentNode();
            if (model instanceof CategoryModel) {
                CategoryModel category = (CategoryModel) model;

                result = new ArrayList(100);
                result = collectNodes(category, result);
                result = sample(input, rankListByOrder(result), false);
            }
        }
        return result;
    }

    /**
     * Collect a list of nodes based on popularity. It check candidate list if specified.  
     * @param category
     * @return List<ContentNodeModel>
     */
    public static List collectCandidateProductsNodes(CategoryModel category) {
        return collectNodes(category, new ArrayList(100));
    }
    
    /**
     * 
     * @param category
     * @param result List<ContentNodeModel> 
     * @return List<ContentNodeModel>
     */
    protected static List collectNodes(CategoryModel category, List result) {
        SortedSet resultSet = collectNodes(category);
        Set manualSlots = result.isEmpty() ? Collections.EMPTY_SET : new HashSet(result);
        for (Iterator iter = resultSet.iterator(); iter.hasNext();) {
            ContentNodeModel model = ((RankedContent.Single) iter.next()).getModel();
            if (!manualSlots.contains(model)) {
                result.add(model);
            }
        }
        return result;
    }

    /**
     * Collect child products, ordered by global popularity.
     * 
     * @param category
     * @return SortedSet<RankedContent.Single>
     */
    static SortedSet collectNodes(CategoryModel category) {
        ProductStatisticsProvider statisticsProvider = ProductStatisticsProvider.getInstance();
        TreeSet resultSet = new TreeSet();
        List candidateList = category.getCandidateList();
        if (candidateList.size() == 0) {
            // fallback, default behaviour
            collectCategories(statisticsProvider, resultSet, category);
        } else {
            for (int i = 0; i < candidateList.size(); i++) {
                ContentNodeModelImpl candidate = (ContentNodeModelImpl) candidateList.get(i);
                if (candidate instanceof ProductModelImpl) {
                    collectProduct(statisticsProvider, resultSet, (ProductModel) candidate);
                } else if (candidate instanceof CategoryModel) {
                    collectCategories(statisticsProvider, resultSet, (CategoryModel) candidate);
                }
            }
        }
        return resultSet;
    }
}
