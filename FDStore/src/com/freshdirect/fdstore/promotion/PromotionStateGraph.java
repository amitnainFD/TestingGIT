package com.freshdirect.fdstore.promotion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.freshdirect.fdstore.promotion.management.FDPromotionNewModel;
import com.freshdirect.framework.state.StateGraph;

public class PromotionStateGraph {
	private StateGraph<EnumPromotionStatus> graph;

	
	FDPromotionNewModel promotion;

	/**
	 * Edges connecting states in all available ways
	 */
	private static final Map<EnumPromotionStatus,Set<EnumPromotionStatus>> PD_EDGES = StateGraph.toEdgesMap(new EnumPromotionStatus[]{
			EnumPromotionStatus.DRAFT, EnumPromotionStatus.PROGRESS,EnumPromotionStatus.CANCELLED,null,
			EnumPromotionStatus.PROGRESS, EnumPromotionStatus.TEST, EnumPromotionStatus.CANCELLING,EnumPromotionStatus.CANCELLED,null,
			EnumPromotionStatus.TEST, EnumPromotionStatus.PROGRESS, EnumPromotionStatus.APPROVE, EnumPromotionStatus.CANCELLING,EnumPromotionStatus.CANCELLED,null,
			EnumPromotionStatus.APPROVE, EnumPromotionStatus.PROGRESS,EnumPromotionStatus.PUBLISHED, EnumPromotionStatus.CANCELLING,EnumPromotionStatus.CANCELLED,null,
			EnumPromotionStatus.PUBLISHED, EnumPromotionStatus.PROGRESS, EnumPromotionStatus.CANCELLING, EnumPromotionStatus.CANCELLED,EnumPromotionStatus.EXPIRED, null,
			EnumPromotionStatus.EXPIRED, EnumPromotionStatus.PROGRESS, null,
			EnumPromotionStatus.LIVE, EnumPromotionStatus.PROGRESS,EnumPromotionStatus.CANCELLING, null,
			EnumPromotionStatus.CANCELLING, EnumPromotionStatus.CANCELLED, EnumPromotionStatus.PROGRESS, null,
			EnumPromotionStatus.CANCELLED, EnumPromotionStatus.PROGRESS, null,
	});


	/**
	 * Thinner set of edges describing state transfers that a customer can manually make
	 */
	private static final Map<EnumPromotionStatus,Set<EnumPromotionStatus>> PD_EDGES_M = StateGraph.toEdgesMap(new EnumPromotionStatus[]{
			EnumPromotionStatus.DRAFT, EnumPromotionStatus.PROGRESS,EnumPromotionStatus.CANCELLED, null,
			EnumPromotionStatus.PROGRESS, EnumPromotionStatus.TEST, EnumPromotionStatus.CANCELLING, EnumPromotionStatus.CANCELLED,null,
			EnumPromotionStatus.TEST, EnumPromotionStatus.PROGRESS, EnumPromotionStatus.APPROVE, EnumPromotionStatus.CANCELLING, EnumPromotionStatus.CANCELLED,null,
			EnumPromotionStatus.APPROVE, EnumPromotionStatus.PROGRESS, EnumPromotionStatus.CANCELLING,EnumPromotionStatus.CANCELLED, null,
			EnumPromotionStatus.PUBLISHED, EnumPromotionStatus.PROGRESS, EnumPromotionStatus.CANCELLING, null,
			EnumPromotionStatus.EXPIRED, EnumPromotionStatus.PROGRESS, null,
			EnumPromotionStatus.LIVE, EnumPromotionStatus.PROGRESS, EnumPromotionStatus.CANCELLING,null,
			EnumPromotionStatus.CANCELLING, EnumPromotionStatus.PROGRESS, null,
			EnumPromotionStatus.CANCELLED, EnumPromotionStatus.PROGRESS, null
	}); 


	public PromotionStateGraph(FDPromotionNewModel promotion) {
		initGraph();
		
		this.promotion = promotion;

		// adjust graph state
		EnumPromotionStatus current = promotion.getStatus();
		graph.setStateUnsafe( current );		
	}

	private void initGraph() {
		// initialize state graph
		final Set<EnumPromotionStatus> pd_states = new HashSet<EnumPromotionStatus>();

		pd_states.add(EnumPromotionStatus.DRAFT);
		pd_states.add(EnumPromotionStatus.PROGRESS);
		pd_states.add(EnumPromotionStatus.TEST);
		pd_states.add(EnumPromotionStatus.APPROVE);
		pd_states.add(EnumPromotionStatus.PUBLISHED);
		pd_states.add(EnumPromotionStatus.EXPIRED);
		pd_states.add(EnumPromotionStatus.LIVE);
		pd_states.add(EnumPromotionStatus.CANCELLING);
		pd_states.add(EnumPromotionStatus.CANCELLED);

		final Set<EnumPromotionStatus> pd_terms = new HashSet<EnumPromotionStatus>();
//		pd_terms.add(EnumPromotionStatus.CANCELLED);

		
		graph = new StateGraph<EnumPromotionStatus>(
			pd_states, PD_EDGES, EnumPromotionStatus.DRAFT, pd_terms);		
	}

	public FDPromotionNewModel getPromotion() {
		return promotion;
	}


	/**
	 * Return states from which customer can choose
	 * @return
	 */
	public Collection<EnumPromotionStatus> getStates() {
		final Set<EnumPromotionStatus> manualEdges = PD_EDGES_M.get(graph.getState());
		if (manualEdges == null || manualEdges.size() == 0)
			return Collections.<EnumPromotionStatus>emptySet();

		Set<EnumPromotionStatus> validStates = new HashSet<EnumPromotionStatus>();
		validStates.addAll(graph.getSubsequentStates());
		validStates.retainAll(manualEdges);

		return validStates;
	}
}
