package com.freshdirect.fdstore.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.freshdirect.rules.Rule;
import com.freshdirect.rules.RulesEngineI;
import com.freshdirect.rules.RulesRegistry;

/**
 * Generic rule-based fee calculator that operates with a single base price and adjustments.
 */
public class OrderMinimumCalculator {
	
	private String subsystem;

	public OrderMinimumCalculator(String subsystem) {
		this.subsystem = subsystem;
	}

	private static Set<Rule> resolveConflicts(Collection<Rule> rules) {

		Set<Rule> applicableRules = filterBasePriceRulesWithLowPriority(rules);
		Set<Rule> finalRules = new HashSet<Rule>();

		Rule baseRule = null;
		for (Rule r : applicableRules) {
			if (r.getOutcome() == null) {
				continue;
			}
			String currOrderMin = (String) r.getOutcome();
			String baseOrderMin = (baseRule == null ? null : (String) baseRule.getOutcome());
			if (baseOrderMin == null || Double.parseDouble(baseOrderMin) > Double.parseDouble(currOrderMin)) {
					baseRule = r;
			}
		} 
		if (baseRule != null) {
			finalRules.add(baseRule);
		}
		return finalRules;
	}

	private static Set<Rule> filterBasePriceRulesWithLowPriority(Collection<Rule> rules) {
		Set<Rule> filteredRules = new HashSet<Rule>();
		int hPriority = Integer.MIN_VALUE;

		for (Rule r : rules) {
			if (r.getOutcome() == null) {
				continue;
			}
			if (hPriority < r.getPriority()) {
					hPriority = r.getPriority();
			}
		}
		
		for (Rule r : rules) {
			if (r.getOutcome() == null) {
				continue;
			}
			else if (r.getPriority() == hPriority) {
				filteredRules.add(r);
			}
		}

		return filteredRules;
	}

	private RulesEngineI getRulesEngine() {
		return RulesRegistry.getRulesEngine(this.subsystem);
	}

	public double getOrderMinimum(FDRuleContextI ctx) {
		RulesEngineI rulesEngine = getRulesEngine();
		String value = null;

		if (rulesEngine != null) {
			Map<?,Rule> firedRules = (Map<?,Rule>)rulesEngine.evaluateRules(ctx);
			//Set<Rule> rules = resolveConflicts(firedRules.values());
			for (Rule r : firedRules.values()) {
				value = (String) r.getOutcome();
			}
		}

		return value == null ? 0 : Double.parseDouble(value);
	}
}