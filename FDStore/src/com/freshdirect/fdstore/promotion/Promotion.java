package com.freshdirect.fdstore.promotion;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.promotion.management.FDPromoDollarDiscount;
import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.logistics.framework.util.LoggerFactory;

public class Promotion extends ModelSupport implements PromotionI {
	private static final Logger LOGGER = LoggerFactory.getInstance(Promotion.class.getSimpleName());
	
	private static final long	serialVersionUID	= -4069961539775362219L;

	private final EnumPromotionType promotionType;

	private final String promotionCode;

	private final String description;

	private final String name;

	private final List<PromotionStrategyI> strategies = new ArrayList<PromotionStrategyI>();
	
	private final List<PromotionApplicatorI> applicators = new ArrayList<PromotionApplicatorI>();

	private PromotionApplicatorI applicator;
	
	private Timestamp lastModified;
		
	
	private int priority;
	
	private boolean combineOffer=false;
	
	private boolean recommendedItemsOnly=false;
	
	private Set<String> excludeSkusFromSubTotal;
	
	private EnumOfferType offerType;
	
	private final static Comparator<PromotionStrategyI> PRECEDENCE_COMPARATOR = new Comparator<PromotionStrategyI>() {
		public int compare(PromotionStrategyI o1, PromotionStrategyI o2) {			
			int p1 = o1.getPrecedence();
			int p2 = o2.getPrecedence();
			return p1 - p2;
		}
	};
	
	public Promotion(PrimaryKey pk, EnumPromotionType promotionType,
			String promotionCode, String name, String description,
			Timestamp lastModified) {
		this.setPK(pk);
		this.promotionType = promotionType;
		this.promotionCode = promotionCode;
		this.description = description;
		this.name = name;
		this.lastModified = lastModified;
		
	}
	
		
	private Set<String> convertToSkus(String value){
		StringTokenizer tokens = new StringTokenizer(value);
		Set<String> returnSet = new HashSet<String>();
		
		while(tokens.hasMoreTokens()){
			returnSet.add(tokens.nextToken());
		}
		return returnSet;
	}
	

	public void addStrategy(PromotionStrategyI strategy) {
		this.strategies.add(strategy);
		Collections.sort(this.strategies, PRECEDENCE_COMPARATOR);
	}
	
	public void addApplicator(PromotionApplicatorI applicator) {
		this.applicators.add(applicator);
		setPriority();
	}
	
	public List<PromotionApplicatorI> getApplicatorList() {
		return this.applicators;
	}

	@Override
	public EnumPromotionType getPromotionType() {
		return this.promotionType;
	}

	@Override
	public String getPromotionCode() {
		return this.promotionCode;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public void setPriority() {
		if(this.isWaiveCharge() && this.isSampleItem()) {
			//Sample && Dlv promo
			setPriority(25);
		} else if(this.isSampleItem() && !(this.isLineItemDiscount()||this.isHeaderDiscount()))
			//Sample Promo
			setPriority(10);
		else if(this.isProductSampleItem() && !(this.isLineItemDiscount()||this.isHeaderDiscount()))
			//Sample Promo
			setPriority(15);
		else if(this.isWaiveCharge() && !(this.isLineItemDiscount()||this.isHeaderDiscount()))
			//Delivery Promo
			setPriority(20);	
		else if(this.isExtendDeliveryPass())
			//Extend Delivery Pass Promo
			setPriority(30);	
		else if(this.isLineItemDiscount() && this.isCombineOffer())
			//Combine offer promotions are guaranteed to apply. 
			setPriority(35);
		else if(this.isHeaderDiscount() && this.isCombineOffer())
			//Combine offer promotions are guaranteed to apply. 
			setPriority(37);
		else if(this.isSignupDiscount())
			//Signup promo
			setPriority(40);
		else if(this.isRedemption())
			//Redemption promo
			setPriority(50);
		else if(this.isLineItemDiscount())
			//DCPD promotion
			setPriority(60);
		else{
			//Any other automatic percent off or dollar off.
			setPriority(70);
		}
	}
	
	
	public boolean isProductSampleItem() {
		for(Iterator<PromotionApplicatorI> i = this.applicators.iterator(); i.hasNext();){
			PromotionApplicatorI _applicator = i.next();
			if(_applicator instanceof ProductSampleApplicator) {
				return true;
			}
		}
		return false;
	}



	/**
	 * @return true if the Promotion is configured properly.
	 */
	public boolean isValid() {
		if (this.applicators == null || this.applicators.size() == 0)
			return false;
		return true;
	}

	@Override
	public boolean evaluate(PromotionContextI context) {
		final boolean isCrm = context != null && context.getUser() != null
				&& context.getUser().isCrmMode();

		for (PromotionStrategyI strategy : this.strategies) {
			// check if particular strategy requires CMS in order to evaluate
			// As CRM became store-less, avoid evaluating these strategies
			if ( isCrm && strategy.isStoreRequired() ) {
				LOGGER.debug("Skipping " + strategy + " in CRM mode");
				continue;
			}
			
			int response = strategy.evaluate(this.promotionCode, context);

			 //System.out.println("Evaluated " + this.promotionCode + " / " +
			 //strategy.getClass().getName() + " -> " + response + " / executing strategy -> " + strategy);
			 

			switch (response) {

			case PromotionStrategyI.ALLOW:
				// check next rule
				continue;

			case PromotionStrategyI.FORCE:
				// eligible, terminate evaluation
				return true;

			default:
				// not eligible, terminate evaluation
				return false;
			}
		}

		return true;
	}

	public boolean apply(PromotionContextI context) {
		if(context.getUser().isCrmMode()) return false;
		boolean applied = false;
		 for (PromotionApplicatorI _applicator : this.applicators) {
			applied = _applicator.apply(this.promotionCode, context);
		}
		return applied;
	}

	public Collection<PromotionStrategyI> getStrategies() {
		return Collections.unmodifiableCollection(this.strategies);
	}
	
	public PromotionStrategyI getStrategy(Class<?> strategyClass) {
		for (PromotionStrategyI strategy : this.strategies) {
			if (strategyClass.isAssignableFrom(strategy.getClass())) {
				return strategy;
			}
		}
		return null;
	}

	@Override
	public Date getExpirationDate() {
		for (PromotionStrategyI obj : this.strategies) {
			if (obj instanceof DateRangeStrategy) {
				return ((DateRangeStrategy) obj).getExpirationDate();
			}
		}
		return null;
	}

	//FIXME List of what? Types are mixed up here (HeaderDiscountRule <--> DCPDiscountRule <--> SignupDiscountRule), 
	// this will cause ClassCastException-s ( getHeaderDiscountTotal() will try to cast to HeaderDiscountRule for example)
	@Override
	public List<? extends HeaderDiscountRule> getHeaderDiscountRules() {
		for (PromotionApplicatorI _applicator : this.applicators) {
			if (_applicator instanceof SignupDiscountApplicator) {
				return ((SignupDiscountApplicator) _applicator).getDiscountRules();
			} else if (_applicator instanceof HeaderDiscountApplicator) {
				HeaderDiscountRule rule = ((HeaderDiscountApplicator) _applicator).getDiscountRule();
				return Arrays.asList(new HeaderDiscountRule[] { rule });
			}
		}		
		return null;
	}

	@Override
	public double getHeaderDiscountTotal() {
		List<? extends HeaderDiscountRule> discountRules = this.getHeaderDiscountRules();
		if (discountRules == null) {
			return 0;
		}
		double sum = 0;
		for (HeaderDiscountRule discountRule : discountRules) {
			sum += discountRule.getMaxAmount();
		}
		return sum;
	}

	@Override
	public double getLineItemDiscountPercentage() {
		 for (PromotionApplicatorI _applicator : this.applicators) {
			if(_applicator instanceof LineItemDiscountApplicator){
				return ((LineItemDiscountApplicator)_applicator).getPercentOff();
			}
		}
		return 0.0;
	}
	
	public boolean isSampleItem() {
		 for (PromotionApplicatorI _applicator : this.applicators) {
			if(_applicator instanceof SampleLineApplicator){
				return true;
			}
		}
		return false;
	}

	public boolean isWaiveCharge() {
		 for (PromotionApplicatorI _applicator : this.applicators) {
			if(_applicator instanceof WaiveChargeApplicator){
				return true;
			}
		}
		return false;
	}
	
	public boolean isExtendDeliveryPass() {
		 for (PromotionApplicatorI _applicator : this.applicators) {
			if(_applicator instanceof ExtendDeliveryPassApplicator){
				return true;
			}
		}
		return false;
	}
	
	public boolean isHeaderDiscount() {
		 for (PromotionApplicatorI _applicator : this.applicators) {
			if(_applicator instanceof HeaderDiscountApplicator || 
					_applicator instanceof PercentOffApplicator){
				return true;
			}
		}
		return false;
	}
	
	public boolean isDollarValueDiscount() {
		return this.isHeaderDiscount() || this.isLineItemDiscount();
	}
	
	
	public boolean isSignupDiscount() {
		 for (PromotionApplicatorI _applicator : this.applicators) {
			if(_applicator instanceof SignupDiscountApplicator){
				return true;
			}
		}
		return false;
	}
	
	public double getMinSubtotal() {
		 for (PromotionApplicatorI _applicator : this.applicators) {
			if (_applicator instanceof SampleLineApplicator) {
				return ((SampleLineApplicator) _applicator).getMinSubtotal();
			}
			if (_applicator instanceof PercentOffApplicator) {
				return ((PercentOffApplicator) _applicator).getMinSubtotal();
			}
			if (_applicator instanceof WaiveChargeApplicator) {
				return ((WaiveChargeApplicator) _applicator).getMinSubtotal();
			}
			if (_applicator instanceof LineItemDiscountApplicator) {
				return ((LineItemDiscountApplicator) _applicator).getMinSubtotal();
			}
		}
		List<? extends HeaderDiscountRule> discountRules = this.getHeaderDiscountRules();
		if (discountRules == null) {
			return 0;
		}
		
		/*APPDEV-1792 - apply the streatchable dollar discount*/
		if(((HeaderDiscountRule) discountRules.get(0)).getDollarList().size() > 0) {
			//get the minimum amount from the dollar list
			HeaderDiscountRule discountRule = (HeaderDiscountRule) discountRules.get(0);
			List<FDPromoDollarDiscount> dollarList = discountRule.getDollarList();
			double minTotal = ((FDPromoDollarDiscount)dollarList.get(0)).getOrderSubtotal();
			for (int i=1; i< dollarList.size(); i++) {
				FDPromoDollarDiscount fdpdd = (FDPromoDollarDiscount) dollarList.get(i);
				if(fdpdd.getOrderSubtotal() < minTotal) {
					minTotal = fdpdd.getOrderSubtotal();
				}
			}
			return minTotal;
		}
		return ((HeaderDiscountRule) discountRules.get(0)).getMinSubtotal();
	}
	
	public Timestamp getModifyDate() {
		return lastModified;
	}

	public boolean isRedemption(){
		boolean value = false;
		for (PromotionStrategyI obj : this.strategies) {
			if (obj instanceof RedemptionCodeStrategy) {
				//This is redemption promo.
				value = true;
				break;
			}
		}
		return value;
	}
	
	public String getRedemptionCode(){		
			String value = "";
			for (PromotionStrategyI obj : this.strategies) {
				if (obj instanceof RedemptionCodeStrategy) {
					//This is redemption promo.
					value = ((RedemptionCodeStrategy)obj).getRedemptionCode();
					break;
				}
			}
			return value;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Promotion[");
		sb.append(this.promotionType).append(" / ").append(this.promotionCode);
		sb.append(" (").append(this.description).append(")");
		sb.append("\n\tStrategies=[");
		for (PromotionStrategyI strategy : this.strategies) {
			sb.append("\n\t\t");
			sb.append(strategy);
		}
		sb.append("\n\t], Applicator=");
		 for (PromotionApplicatorI _applicator : this.applicators) {
			sb.append("\n\t\t");
			sb.append(_applicator);
		}
		return sb.toString();
	}

	public boolean isLineItemDiscount() {
		 for (PromotionApplicatorI _applicator : this.applicators) {
			if(_applicator instanceof LineItemDiscountApplicator) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean isCombineOffer() {
		return combineOffer;
	}

	public void setCombineOffer(boolean combineOffer) {
		this.combineOffer = combineOffer;
	}

	public void setRecommendedItemsOnly(boolean recommendedItemsOnly) {
		this.recommendedItemsOnly = recommendedItemsOnly;
	}

	@Override
	public boolean isFavoritesOnly(){
		 for (PromotionApplicatorI _applicator : this.applicators) {
			if(_applicator instanceof LineItemDiscountApplicator){
				 LineItemDiscountApplicator app = (LineItemDiscountApplicator) _applicator;
				 return app.isFavoritesOnly();
			 }
		 }
		 return false;
	 }

	 
	@Override
	public double getLineItemDiscountPercentOff(){
		 double percentOff=0;
		 for (PromotionApplicatorI _applicator : this.applicators) {
			if(_applicator instanceof LineItemDiscountApplicator) {
				LineItemDiscountApplicator app=(LineItemDiscountApplicator)applicator;
				 percentOff=app.getPercentOff();
			}
		 }
		 return percentOff;
	 }
	 
	@Override
	public boolean isFraudCheckRequired(){
		 boolean value = false;
		 for (PromotionStrategyI obj : getStrategies()) {
				if (obj instanceof FraudStrategy) {
					//Fruad Check is required for this promo.
					value = true;
					break;
				}
			}
			return value;		 
	 }

	@Override
	public Set<String> getExcludeSkusFromSubTotal() {
		return excludeSkusFromSubTotal;
	}

	public void setExcludeSkusFromSubTotal(String excludeSkusFromSubTotal) {
		this.excludeSkusFromSubTotal = convertToSkus(excludeSkusFromSubTotal);
	}


	@Override
	public EnumOfferType getOfferType() {
		return offerType;
	}

	public void setOfferType(EnumOfferType offerType) {
		this.offerType = offerType;
	}
	 

}
