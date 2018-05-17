package com.freshdirect.fdstore.rules;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.freshdirect.rules.Rule;
import com.freshdirect.rules.RulesEngineI;
import com.freshdirect.rules.RulesRegistry;

/**
 * Generic rule based eligiblity calculator.
 * 
 * TODO refactor common code with FeeCalculator * 
 */
public class TierDeliveryFeeCalculator implements Serializable {

	private String subsystem;

	public TierDeliveryFeeCalculator(String subsystem) {
		this.subsystem = subsystem;
	}

	private static Set filterRulesWithLowPriority(Collection rules) {
		Set filteredRules = new HashSet();
		int hPriority = Integer.MIN_VALUE;

		for (Iterator i = rules.iterator(); i.hasNext();) {
			Rule r = (Rule) i.next();
			if (hPriority < r.getPriority()) {
				hPriority = r.getPriority();
			}
		}

		for (Iterator i = rules.iterator(); i.hasNext();) {
			Rule r = (Rule) i.next();
			if (r.getPriority() == hPriority) {
				filteredRules.add(r);
			}
		}

		return filteredRules;
	}

	private RulesEngineI getRulesEngine() {
		return RulesRegistry.getRulesEngine(this.subsystem);
	}

	public TieredPrice getTieredDeliveryFee(FDRuleContextI ctx) {

		RulesEngineI rulesEngine = getRulesEngine();
		Map firedRules = null;

		if (ctx != null && rulesEngine != null && (firedRules = rulesEngine.evaluateRules(ctx)) != null) {

			Set rules = filterRulesWithLowPriority(firedRules.values());

			for (Iterator i = rules.iterator(); i.hasNext();) {
				Rule r = (Rule) i.next();
				Object outcome = r.getOutcome();
				
				if(outcome instanceof TieredPrice)
					return ((TieredPrice) r.getOutcome());
				
			}
		}
		return null;
	}
	
}