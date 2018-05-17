package com.freshdirect.fdstore.rules;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.freshdirect.framework.util.MathUtil;
import com.freshdirect.rules.Rule;
import com.freshdirect.rules.RulesEngineI;
import com.freshdirect.rules.RulesRegistry;

/**
 * Generic rule-based fee calculator that operates with a single base price and adjustments.
 */
public class FeeCalculator implements Serializable {
	private static final long serialVersionUID = 6083766851608087582L;

	private String subsystem;

	public FeeCalculator(String subsystem) {
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

			if (r.getOutcome() instanceof BasePrice) {
				BasePrice currPrice = (BasePrice) r.getOutcome();
				BasePrice basePrice = (baseRule == null ? null : (BasePrice) baseRule.getOutcome());
				if (basePrice == null || basePrice.getPrice() > currPrice.getPrice()) {
					baseRule = r;
				}
			} else {
				finalRules.add(r);
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

			if (r.getOutcome() instanceof BasePrice) {
				if (hPriority < r.getPriority()) {
					hPriority = r.getPriority();
				}
			}
		}

		for (Rule r : rules) {
			if (r.getOutcome() == null) {
				continue;
			}
			if (r.getOutcome() instanceof BasePrice) {
				if (r.getPriority() == hPriority) {
					filteredRules.add(r);
				}
			} else {
				filteredRules.add(r);
			}
		}

		return filteredRules;
	}

	private RulesEngineI getRulesEngine() {
		return RulesRegistry.getRulesEngine(this.subsystem);
	}

	public double calculateFee(FDRuleContextI ctx) {
		Map<?,Rule> firedRules = (Map<?,Rule>)getRulesEngine().evaluateRules(ctx);

		//System.out.println("FIRED RULES: " + firedRules);

		Set<Rule> rules = resolveConflicts(firedRules.values());

		//System.out.println("AFTER RESOLVE CONFLICT: " + rules);
		Map<String, Double> fees = new HashMap<String, Double>();
		double value = 0.0;
		double premium = 0.0;
		for (Rule r : rules) {
			Object outcome = r.getOutcome();
			if (outcome instanceof BasePrice) {
				value += ((BasePrice) r.getOutcome()).getPrice();
			}
			else if (outcome instanceof Adjustment) {
				value -= ((Adjustment) r.getOutcome()).getValue();
			}
		}

		return value < 0 ? 0 : MathUtil.roundDecimal(value);
	}
}