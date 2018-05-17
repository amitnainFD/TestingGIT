package com.freshdirect.smartstore.scoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ContentNodeModelUtil;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.WineUtil;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.fdstore.ScoreProvider;
import com.freshdirect.smartstore.filter.ContentFilter;
import com.freshdirect.smartstore.filter.FilterFactory;

public class PrioritizedDataAccess implements DataAccess {
	/**
	 * List of priority nodes.
	 */
	private List<ContentNodeModel> nodes;
	
	/**
	 * List of those nodes
	 */
	private List<ContentNodeModel> posteriorNodes;

	ContentFilter filter;
	
	private boolean excludeAlcoholic;

	public PrioritizedDataAccess(Collection<ContentKey> cartItems, boolean useAlternatives, boolean showTempUnavailable, boolean excludeAlcoholic) {
		nodes = new ArrayList<ContentNodeModel>();
		posteriorNodes = new ArrayList<ContentNodeModel>();
		filter = FilterFactory.getInstance().createFilter(cartItems, useAlternatives, showTempUnavailable);
		this.excludeAlcoholic = excludeAlcoholic;
	}

	@Override
	public List<? extends ContentNodeModel> fetchContentNodes(SessionInput input, String name) {
		final List<? extends ContentNodeModel> dsNodes = ScoreProvider.getInstance().fetchContentNodes(input, name);
		
		if (input.isTraceMode()) {
			input.traceContentNodes(name, dsNodes);
		}
		
		return dsNodes;
	}

	@Override
	public double[] getVariables(String userId, PricingContext pricingContext, ContentNodeModel contentNode,
			String[] variables) {
		return ScoreProvider.getInstance().getVariables(userId, pricingContext, contentNode, variables);
	}

	@Override
	public boolean addPrioritizedNode(ContentNodeModel model) {
		if (model instanceof ProductModel) {
		    ContentNodeModel filteredModel = filter.filter(model);
		    if (filteredModel != null) {
		    	if (excludeAlcoholic && isAlcoholic(model))
		    		return false;
		        nodes.add(filteredModel);
		        return true;
		    }
		} 
		return false;
	}


	/**
	 * Check whether product m is an alcoholic content
	 * 
	 * @param m
	 * @return
	 */
	private boolean isAlcoholic(ContentNodeModel m) {
		final ContentNodeModel dept = ContentNodeModelUtil.findDepartment(m);
		if (dept != null) {
			final ContentKey aKey = dept.getContentKey();

			if (aKey != null && WineUtil.getWineAssociateId().toLowerCase().equalsIgnoreCase(aKey.getId())) {
				return true;
			}
		}
		
		return false;
	}


	@Override
	public List<ContentNodeModel> getPrioritizedNodes() {
		return Collections.unmodifiableList(nodes);
	}

	@Override
	public boolean addPosteriorNode(ContentNodeModel model) {
		if (model instanceof ProductModel) {
		    ContentNodeModel filteredModel = filter.filter(model);
		    if (filteredModel != null) {
		    	if (excludeAlcoholic && isAlcoholic(model))
		    		return false;
		    	posteriorNodes.add(filteredModel);
		        return true;
		    }
		} 
		return false;
	}

	@Override
	public List<ContentNodeModel> getPosteriorNodes() {
		return Collections.unmodifiableList(posteriorNodes);
	}

}
