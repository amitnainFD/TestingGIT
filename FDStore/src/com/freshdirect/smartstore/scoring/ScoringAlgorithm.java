package com.freshdirect.smartstore.scoring;

import java.util.Map;

import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.content.ContentNodeModel;

/**
 * Base class for the scoring algorithm implementation. Subclasses overrides the getScores and the getVariableNames methods.
 * @author zsombor
 *
 */
public class ScoringAlgorithm {

    /**
     * Return the needed variables to calculate a score.
     * @return
     */
    public String[] getVariableNames() {
        return new String[0];
    }
    
    /**
     * Return the expressions which generates the scores.
     * 
     * @return
     */
    public String[] getExpressions() {
        return new String[0];
    }
    
    /**
     * Calculate a score from the given variables. 
     * @param variables
     * @return
     */
    public Score getScores(Map variables) {
        return null;
    }
    
    /**
     * Calculate a score with the given variables  
     * @param variables
     * @return
     */
    public double[] getScores(double[] variables) {
        return null;
    }
    
    public int getReturnSize() {
        return 0;
    }
    
    
    public OrderingFunction createOrderingFunction() {
        return new OrderingFunction();
    }

    public double[] getScoreOf(String userId, PricingContext pricingCtx, DataAccess dataAccess, ContentNodeModel node) {
        double[] variables = dataAccess.getVariables(userId, pricingCtx, node, getVariableNames());
        return getScores(variables);
    }
}
