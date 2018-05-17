package com.freshdirect.fdstore.promotion;

import java.util.ArrayList;
import java.util.List;

public class CompositeStrategy implements PromotionStrategyI {

	private List<PromotionStrategyI> strategies = new ArrayList<PromotionStrategyI>();
	private final int operator;
	
	public static final int OR = 0;
	public static final int AND = 1;

	public CompositeStrategy(int operator) {
		this.operator = operator;
	}
	
	public void addStrategy(PromotionStrategyI strategy){
		strategies.add(strategy);
	}
	
	public void addStrategies(List<PromotionStrategyI> strategies) {
		strategies.addAll(strategies);
	}

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		final boolean isCrm = context != null && context.getUser() != null
				&& context.getUser().isCrmMode();
		int result = -1;
		for (PromotionStrategyI strategy : this.strategies) {
			// skip CMS heavy strategies in CRM ...
			if (isCrm && strategy.isStoreRequired()) {
				continue;
			}

			int response = strategy.evaluate(promotionCode, context);
			switch(operator) {
				case OR: result = (result == -1) ? response : result | response;
						 break;
				case AND: result = (result == -1) ? response : result & response;
					     break;
				default: result = DENY;
			}
		} 
		return result;
	}


	@Override
	public int getPrecedence() {
		return 1100;
	}

	
	public String toString() {
		return "CompositeAttributeStrategy[...]";
	}

	/**
	 * Be permissive here, defer to evaluate phase
	 */
	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
