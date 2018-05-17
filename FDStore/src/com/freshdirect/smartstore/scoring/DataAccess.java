package com.freshdirect.smartstore.scoring;

import java.util.List;

import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.smartstore.SessionInput;

/**
 * This interface encapsulates the API, which the DataGenerator use to access various variables for a ContentNodes, 
 * or the defined datasources.
 * 
 * @author zsombor
 *
 */
public interface DataAccess {
    /**
     * Return the associated factor values for the given content node, in the defined order
     * @param userId the id of the user
     * @param contentNode the node
     * @param variables the name of the factors
     * @return a list of values 
     */
    double[] getVariables(String userId, PricingContext pricingContext, ContentNodeModel contentNode, String[] variables); 
    
    /**
     * Return a named collection of nodes, for example 'FeaturedItems' or 'CartHistory', etc.
     * @param name
     * @return
     */
    public List<? extends ContentNodeModel> fetchContentNodes(SessionInput input, String name);
    
    public boolean addPrioritizedNode(ContentNodeModel model);
    
    public List<? extends ContentNodeModel> getPrioritizedNodes();

    public boolean addPosteriorNode(ContentNodeModel model);

    public List<? extends ContentNodeModel> getPosteriorNodes();
}
