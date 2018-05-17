package com.freshdirect.fdstore.promotion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartLineModel;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDInvalidConfigurationException;

public class LineItemDiscountApplicator implements PromotionApplicatorI {
   
	private double minSubTotal=0.0;
	private double percentOff=0.0;
	private boolean favoritesOnly;
	private DlvZoneStrategy zoneStrategy;
	private HeaderDiscountRule discountRule=null;
	private int skuLimit = 0;
	private double maxPercentageDiscount=0.0;
	
	/*
	 * List of line item strategies to determine the eligibility of a line item before
	 * applying discount.
	 */
	private List lineItemStrategies = new ArrayList();
	private CartStrategy cartStrategy;
	
	public LineItemDiscountApplicator(double minAmount,double percentoff, double maxPercentageDiscount) { //, int maxItemCount,boolean applyHeaderDiscount){
		this.minSubTotal=minAmount;
		this.percentOff=percentoff;
		this.maxPercentageDiscount = maxPercentageDiscount;
	}
	
	public LineItemDiscountApplicator(double minAmount) { //, int maxItemCount,boolean applyHeaderDiscount){
		this.minSubTotal=minAmount;
	}
	
	public double getMinSubtotal() {
		return this.minSubTotal;
	}
	public void addLineItemStrategy(LineItemStrategyI strategy) {
		this.lineItemStrategies.add(strategy);
		Collections.sort(this.lineItemStrategies, PRECEDENCE_COMPARATOR);
	}
	
	private final static Comparator PRECEDENCE_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			
			int p1 = ((LineItemStrategyI) o1).getPrecedence();
			int p2 = ((LineItemStrategyI) o2).getPrecedence();
			return p1 - p2;
		}
	};
	
	private final static Comparator PRICE_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			
			double p1 = ((FDCartLineI) o1).getBasePrice();
			double p2 = ((FDCartLineI) o2).getBasePrice();
			if(p1 == p2) {
				//prices are same. so order by line number
				p2 = Double.parseDouble( ((FDCartLineI) o1).getCartlineId() != null && ((FDCartLineI) o1).getCartlineId().length() > 0 
																							? ((FDCartLineI) o1).getCartlineId() : "0");
				p1 = Double.parseDouble( ((FDCartLineI) o2).getCartlineId() != null && ((FDCartLineI) o2).getCartlineId().length() > 0 
																							? ((FDCartLineI) o2).getCartlineId() : "0");
			}			
			return Double.compare(p1, p2);
		}
	};
	
	public boolean apply(String promotionCode, PromotionContextI context) {
		//If delivery zone strategy is applicable please evaluate before applying the promotion.
		int ev = zoneStrategy != null ? zoneStrategy.evaluate(promotionCode, context) : PromotionStrategyI.ALLOW;
		if(ev == PromotionStrategyI.DENY) return false;
		
		ev = cartStrategy != null ? cartStrategy.evaluate(promotionCode, context, true) : PromotionStrategyI.ALLOW;
		if(ev == PromotionStrategyI.DENY) return false;
		
		PromotionI promo = PromotionFactory.getInstance().getPromotion(promotionCode);
		double preDeduction = context.getSubTotal(promo.getExcludeSkusFromSubTotal());
		if (preDeduction < this.minSubTotal) {
			return false;
		} 
		//If discount is applied only for favorite items only(smart savings) then check the promo variant
		//map is not empty.
		if(promo.isFavoritesOnly() &&( context.getUser().getPromoVariantMap() == null ||
				context.getUser().getPromoVariantMap().size() == 0)) return false;
		
		FDCartModel cart= context.getShoppingCart();
		List orderLines=cart.getOrderLines();
        Map recommendedItemMap=new HashMap();
        int appliedCnt = 0;
		if(orderLines!=null){
			
			/*
			for(int i=0;i<orderLines.size();i++) {
				  FDCartLineI cartLine=(FDCartLineI)orderLines.get(i);
				  System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + cartLine.getLabel() + " - " + cartLine.getQuantity() + " - " + cartLine.getPrice() + " - " + cartLine.getBasePrice() + " - " + cartLine.isDiscountFlag() + " - " + cartLine.getSalesUnitDescription() + " - " + cartLine.getUnitPrice() +
						  " - " +  cartLine.getOrderLineId() + " - " + cartLine.getOrderLineNumber() + "-" + cartLine.getCartlineId()) ;
			}
			*/
			/*
			 * APPDEV-1784: Sorting the list by price, so that the line item discounts 
			 * with sku limits can be applied to higher priced items first 
			 */
			List newOrderLines = Arrays.asList(new Object[orderLines.size()]);
			Collections.copy(newOrderLines, orderLines);
			Collections.sort(newOrderLines, PRICE_COMPARATOR);
			/*
			for(int i=newOrderLines.size() - 1;i>=0;i--)  {
				  FDCartLineI cartLine=(FDCartLineI)newOrderLines.get(i);
				  System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + cartLine.getLabel() + " - " + cartLine.getQuantity() + " - " + cartLine.getPrice() + " - " + cartLine.getBasePrice() + " - " + cartLine.isDiscountFlag() + " - " + cartLine.getSalesUnitDescription() + " - " + cartLine.getUnitPrice() +
						  " - " +  cartLine.getOrderLineId() + " - " + cartLine.getOrderLineNumber() + "-" + cartLine.getCartlineId()) ;
			}
			*/
			Map<String,Integer>skuCountMap =cart.getSkuCount();
			Integer skuCount = skuCountMap.get(promo.getPromotionCode());
			if(null ==skuCount){
				skuCount= new Integer(0);
			}
			skuCountMap.put(promo.getPromotionCode(),skuCount);
			if(null != discountRule){
				/*
				System.out.println("Processing order lines with a discountRule:" + discountRule.toString());
				double amount = Math.min(context.getShoppingCart().getPreDeductionTotal(), this.discountRule.getMaxAmount());
				
				
				for(int i=0;i<orderLines.size();i++) {
					  FDCartLineI cartLine=(FDCartLineI)orderLines.get(i);
					  if(cartLine.isDiscountFlag()){
							boolean e = evaluate(cartLine, promotionCode, context);
							if(e) {
								context.applyLineItemDollarOffDiscount(promo, cartLine, discountRule.getMaxAmount());
								if(favoritesOnly){
									String savingsId = cartLine.getSavingsId();
									String productId = cartLine.getProductRef().getContentKey().getId();							
									recommendedItemMap.put(productId, savingsId);
								}
								appliedCnt++;
							}		
					  }
				}
				//Now run through all recently added items.
				for(int i=0;i<orderLines.size();i++) {		
					FDCartLineI cartLine=(FDCartLineI)orderLines.get(i);
					if(!cartLine.isDiscountFlag()) {
						  	boolean e = evaluate(cartLine, promotionCode, context);
							if(e) {
								context.applyLineItemDollarOffDiscount(promo, cartLine, discountRule.getMaxAmount());
								if(favoritesOnly){
									String savingsId = cartLine.getSavingsId();
									String productId = cartLine.getProductRef().getContentKey().getId();							
									recommendedItemMap.put(productId, savingsId);
								}
								cartLine.setDiscountFlag(true);
								appliedCnt++;
							}		
							
					  }
				}
				*/				
				
				for(int i=newOrderLines.size() - 1;i>=0;i--) {
					  FDCartLineI cartLine=(FDCartLineI)newOrderLines.get(i);
					  boolean e = evaluate(cartLine, promotionCode, context);
					  if(e) {
						skuCount = skuCountMap.get(promo.getPromotionCode());
						int availableSkuLimit = skuLimit - skuCount;
						if(availableSkuLimit > (int)cartLine.getQuantity()) {
							availableSkuLimit = (int)cartLine.getQuantity();
						}
						boolean applied = context.applyLineItemDollarOffDiscount(promo, cartLine, discountRule.getMaxAmount(), availableSkuLimit);
						if(applied && skuLimit > 0) {
							if(cartLine.getUnitPrice().indexOf("/lb") != -1) {
								cart.incrementSkuCount(promo.getPromotionCode(), 1);
							} else {
								cart.incrementSkuCount(promo.getPromotionCode(), (int)cartLine.getQuantity());
							}
						}
						if(favoritesOnly){
							String savingsId = cartLine.getSavingsId();
							String productId = cartLine.getProductRef().getContentKey().getId();							
							recommendedItemMap.put(productId, savingsId);
						}
						if(!cartLine.isDiscountFlag()){
							cartLine.setDiscountFlag(true);
						}
						appliedCnt++;
					  }
				}
				
				
//				return context.applyHeaderDiscount(promo, amount);
			}else{		
				/*
				for(int i=0;i<orderLines.size();i++) {
					  FDCartLineI cartLine=(FDCartLineI)orderLines.get(i);
					  if(cartLine.isDiscountFlag()){
							boolean e = evaluate(cartLine, promotionCode, context);
							if(e) {
								context.applyLineItemDiscount(promo, cartLine, percentOff);
								if(favoritesOnly){
									String savingsId = cartLine.getSavingsId();
									String productId = cartLine.getProductRef().getContentKey().getId();							
									recommendedItemMap.put(productId, savingsId);
								}
								appliedCnt++;
							}		
					  }
				}
				//Now run through all recently added items.
				for(int i=0;i<orderLines.size();i++) {		
					FDCartLineI cartLine=(FDCartLineI)orderLines.get(i);
					if(!cartLine.isDiscountFlag()) {
						  	boolean e = evaluate(cartLine, promotionCode, context);
							if(e) {
								context.applyLineItemDiscount(promo, cartLine, percentOff);
								if(favoritesOnly){
									String savingsId = cartLine.getSavingsId();
									String productId = cartLine.getProductRef().getContentKey().getId();							
									recommendedItemMap.put(productId, savingsId);
								}
								cartLine.setDiscountFlag(true);
								appliedCnt++;
							}		
							
					  }
				}
			    // now apply discount to any duplicate sku from the recommended List for favorites only
							   
				if(favoritesOnly && orderLines.size()>0){
					for(int i=0;i<orderLines.size();i++){
						FDCartLineI cartLine=(FDCartLineModel)orderLines.get(i);	 
							String productId = cartLine.getProductRef().getContentKey().getId();
							if(!cartLine.hasDiscount(promotionCode) && recommendedItemMap.containsKey(productId)){								
								context.applyLineItemDiscount(promo, cartLine, percentOff);
								cartLine.setSavingsId((String)recommendedItemMap.get(productId));
								cartLine.setDiscountFlag(true);
								appliedCnt++;
							}
						
					}	
				}*/				
				
				
				for(int i=newOrderLines.size() - 1;i>=0;i--) {
					FDCartLineI cartLine=(FDCartLineI)newOrderLines.get(i);
					boolean e = evaluate(cartLine, promotionCode, context);
					if(e) {
						skuCount = skuCountMap.get(promo.getPromotionCode());
						int availableSkuLimit = skuLimit - skuCount;
						if(availableSkuLimit > (int)cartLine.getQuantity()) {
							availableSkuLimit = (int)cartLine.getQuantity();
						}
						boolean applied = context.applyLineItemDiscount(promo, cartLine, percentOff, availableSkuLimit, maxPercentageDiscount);
						if(applied && skuLimit > 0) {							
							if(cartLine.getUnitPrice().indexOf("/lb") != -1) {
								cart.incrementSkuCount(promo.getPromotionCode(), 1);
							} else {
								cart.incrementSkuCount(promo.getPromotionCode(), (int)cartLine.getQuantity());
							}
						}
						if(favoritesOnly){
							String savingsId = cartLine.getSavingsId();
							String productId = cartLine.getProductRef().getContentKey().getId();							
							recommendedItemMap.put(productId, savingsId);
						}
						if(!cartLine.isDiscountFlag()) {
							cartLine.setDiscountFlag(true);
						}
						appliedCnt++;
					}
				}
				
			}
			if(appliedCnt <= 0) return false;
			//Update Pricing after discount application.
			try {
					//by pass recalculating group scale as it is not required.
					cart.refreshAll(false);
			} catch (FDResourceException e) {
				// TODO Auto-generated catch block
				throw new FDRuntimeException(e);
			}
			catch (FDInvalidConfigurationException e) {
				throw new FDRuntimeException(e);
			}			      						
			return true;
		}						
		return false;
	}		
	
	/**
	 * This method will run through given line item against list of line item strategies
	 * and determine if the line is eligible for promotion passed as a parameter.
	 * @param promoCode
	 * @param context
	 * @param lineItemStrategies
	 * @return
	 */
	protected boolean evaluate(FDCartLineI lineItem, String promoCode, PromotionContextI context) {
		for (Iterator i = this.lineItemStrategies.iterator(); i.hasNext();) {
			LineItemStrategyI strategy = (LineItemStrategyI) i.next();
			int response = strategy.evaluate(lineItem, promoCode, context);
			//System.out.println("Calling LineItemStrategy:" + strategy + "------for product:"+ lineItem.getDescription()  + "--------response:" + response );

			 //System.out.println("Evaluated " + promoCode + " / " +
			 //strategy.getClass().getName() + " -> " + response);

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

	public double getPercentOff() {
		return percentOff;
	}

	public void setPercentOff(double percentOff) {
		this.percentOff = percentOff;
	}

	public boolean isFavoritesOnly() {
		return favoritesOnly;
	}

	public void setFavoritesOnly(boolean favoritesOnly) {
		this.favoritesOnly = favoritesOnly;
	}
	
	public void setZoneStrategy(DlvZoneStrategy zoneStrategy) {
		this.zoneStrategy = zoneStrategy;
	}

	public DlvZoneStrategy getDlvZoneStrategy() {
		return this.zoneStrategy;
	}

	public HeaderDiscountRule getDiscountRule() {
		return discountRule;
	}
	
	public void setDiscountRule(HeaderDiscountRule discountRule){
		this.discountRule = discountRule;
	}

	public void setSkuLimit(int skuLimit) {
		this.skuLimit = skuLimit;
	}

	public int getSkuLimit() {
		return skuLimit;
	}

	@Override
	public void setCartStrategy(CartStrategy cartStrategy) {
		this.cartStrategy = cartStrategy;
	}

	@Override
	public CartStrategy getCartStrategy() {
		return this.cartStrategy;
	}
}
