/**
 * 
 */
package com.freshdirect.smartstore.sorting;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.smartstore.fdstore.ScoreProvider;
import com.freshdirect.smartstore.scoring.DataAccess;
import com.freshdirect.smartstore.scoring.Score;
import com.freshdirect.smartstore.scoring.ScoringAlgorithm;
import com.freshdirect.smartstore.service.SearchScoringRegistry;

public class ScriptedContentNodeComparator implements Comparator<ProductModel> {
    final DataAccess dataAccess;
    final String userId;
    final PricingContext pricingContext;
    final ScoringAlgorithm algorithm;

    private String[] variables;
    private Map<ContentKey, Score> cache = new HashMap<ContentKey, Score>();

    ScriptedContentNodeComparator(DataAccess dataAccess, String userId, PricingContext pricingContext, ScoringAlgorithm algorithm) {
        super();
        this.dataAccess = dataAccess;
        this.userId = userId;
        this.pricingContext = pricingContext;
        this.algorithm = algorithm;
        this.variables = algorithm.getVariableNames();
    }

    public Score getScore(ContentNodeModel contentNode) {
        Score score = cache.get(contentNode.getContentKey());
        if (score == null) {
            double[] vars = dataAccess.getVariables(userId, pricingContext, contentNode, variables);
            score = new Score(contentNode, algorithm.getScores(vars));
            cache.put(contentNode.getContentKey(), score);
        }
        return score;
    }
    
    @Override
    public int compare(ProductModel o1, ProductModel o2) {
        Score s1 = getScore(o1);
        Score s2 = getScore(o2);
        return s1.compareTo2(s2);
    }
    
    public static ScriptedContentNodeComparator createGlobalComparator(String userId, PricingContext pricingContext) {
    	return new ScriptedContentNodeComparator(ScoreProvider.getInstance(), userId, pricingContext,
    			SearchScoringRegistry.getInstance().getGlobalScoringAlgorithm());
    }

    public static ScriptedContentNodeComparator createUserComparator(String userId, PricingContext pricingContext) {
    	return new ScriptedContentNodeComparator(ScoreProvider.getInstance(), userId, pricingContext,
    			SearchScoringRegistry.getInstance().getUserScoringAlgorithm());
    }
    
    public static ScriptedContentNodeComparator createShortTermPopularityComparator(String userId, PricingContext pricingContext) {
    	return new ScriptedContentNodeComparator(ScoreProvider.getInstance(), userId, pricingContext,
    			SearchScoringRegistry.getInstance().getShortTermPopularityScoringAlgorithm());
    }
}