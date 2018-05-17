package com.freshdirect.fdstore.promotion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds additional strategies/applicator to a promotion, that are not declared from the database. 
 */
public class PromotionDecorator {

	private final static PromotionDecorator INSTANCE = new PromotionDecorator();

	static {

		//
		// trial offers
		// 

		INSTANCE.addStrategy("SIGNUP", new SignupStrategy());
		INSTANCE.addApplicator(
			"SIGNUP",
			new SignupDiscountApplicator(new SignupDiscountRule[] { new SignupDiscountRule(100.0, 50.0, 15.0)}));

		INSTANCE.addStrategy("SIGNUP_P1", new SignupStrategy());
		INSTANCE.addApplicator(
			"SIGNUP_P1",
			new SignupDiscountApplicator(
				new SignupDiscountRule[] {
					new SignupDiscountRule(100.0, 25.0, 15.0),
					new SignupDiscountRule(75.0, 20.0, 15.0),
					new SignupDiscountRule(40.0, 5.0, 15.0)}));

		INSTANCE.addStrategy("SIGNUP_P2", new SignupStrategy());
		INSTANCE.addApplicator(
			"SIGNUP_P2",
			new SignupDiscountApplicator(
				new SignupDiscountRule[] {
					new SignupDiscountRule(100.0, 20.0, 15.0),
					new SignupDiscountRule(75.0, 10.0, 15.0),
					new SignupDiscountRule(75.0, 10.0, 15.0),
					new SignupDiscountRule(75.0, 10.0, 15.0)}));

		INSTANCE.addStrategy("SIGNUP_P3", new SignupStrategy());
		INSTANCE.addApplicator(
			"SIGNUP_P3",
			new SignupDiscountApplicator(
				new SignupDiscountRule[] {
					new SignupDiscountRule(80.0, 20.0, 15.0),
					new SignupDiscountRule(80.0, 15.0, 15.0),
					new SignupDiscountRule(80.0, 15.0, 15.0)}));

		INSTANCE.addStrategy("SIGNUP_P4", new SignupStrategy());
		INSTANCE.addApplicator(
			"SIGNUP_P4",
			new SignupDiscountApplicator(
				new SignupDiscountRule[] {
					new SignupDiscountRule(75.0, 25.0, 15.0),
					new SignupDiscountRule(75.0, 20.0, 15.0),
					new SignupDiscountRule(40.0, 5.0, 15.0)}));
		
		INSTANCE.addStrategy("SIGNUP_FRESHFOOD", new SignupStrategy());
		INSTANCE.addApplicator("SIGNUP_FRESHFOOD", new SignupDiscountApplicator(new SignupDiscountRule[] {new SignupDiscountRule(
			40.0,
			20.0,
			20.0)}));
		
		INSTANCE.addStrategy("SIGNUP_FRESH_25", new SignupStrategy());
		INSTANCE.addApplicator("SIGNUP_FRESH_25", 
			new SignupDiscountApplicator(
				new SignupDiscountRule[] {
					new SignupDiscountRule(40.0, 25.0, 25.0),
					new SignupDiscountRule(40.0, 25.0, 25.0)}));
		//
		// other
		//

		ProfileAttributeStrategy profStrategy = new ProfileAttributeStrategy();
		profStrategy.setAttributeName("MAKE_GOOD");
		INSTANCE.addStrategy("PROMO_FD_LSGN_P1", profStrategy);
		INSTANCE.addStrategy("PROMO_FD_LSGN_P2", profStrategy);

		profStrategy = new ProfileAttributeStrategy();
		profStrategy.setAttributeName("signup_survey_v2");
		profStrategy.setDesiredValue("FILL");
		INSTANCE.addStrategy("SIGNUP_SURVEY_V2", profStrategy);

		profStrategy = new ProfileAttributeStrategy();
		profStrategy.setAttributeName("fourth_order_survey");
		profStrategy.setDesiredValue("FILL");
		INSTANCE.addStrategy("4TH_ORDER_SURVEY", profStrategy);
		
		profStrategy = new ProfileAttributeStrategy();
		profStrategy.setAttributeName("second_order_survey");
		profStrategy.setDesiredValue("FILL");
		profStrategy.setAttributeName("second_order_promo");
		profStrategy.setDesiredValue("true");
		INSTANCE.addStrategy("2ND_ORDER_SURVEY", profStrategy);

	}

	private PromotionDecorator() {
	}

	public static PromotionDecorator getInstance() {
		return INSTANCE;
	}

	/** Map of promo code -> List of PromotionStrategyI */
	private final Map<String,List<PromotionStrategyI>> strategyMap = new HashMap<String,List<PromotionStrategyI>>();

	/** Map of promo code -> PromotionApplicatorI */
	private final Map<String,PromotionApplicatorI> applicators = new HashMap<String,PromotionApplicatorI>();

	private void addStrategy(String promotionCode, PromotionStrategyI strategy) {
		List<PromotionStrategyI> strategies = this.strategyMap.get(promotionCode);
		if (strategies == null) {
			strategies = new ArrayList<PromotionStrategyI>();
			this.strategyMap.put(promotionCode, strategies);
		}
		strategies.add(strategy);
	}

	private List<PromotionStrategyI> getStrategies(String promotionCode) {
		return this.strategyMap.get(promotionCode);
	}

	private void addApplicator(String promotionCode, PromotionApplicatorI applicator) {
		this.applicators.put(promotionCode, applicator);
	}

	private PromotionApplicatorI getApplicator(String promotionCode) {
		return this.applicators.get(promotionCode);
	}

	public void decorate(PromotionI promotion) {
		List<PromotionStrategyI> strategies = this.getStrategies(promotion.getPromotionCode());
		if (strategies != null) {
			for ( PromotionStrategyI ps : strategies ) {
				((Promotion)promotion).addStrategy( ps );
			}
		}

		PromotionApplicatorI applicator = this.getApplicator(promotion.getPromotionCode());
		if (applicator != null) {
			//((Promotion)promotion).setApplicator(applicator);
			((Promotion)promotion).addApplicator(applicator);
		}

	}

}