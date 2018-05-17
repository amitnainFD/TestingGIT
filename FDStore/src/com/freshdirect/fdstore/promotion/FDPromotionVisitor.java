package com.freshdirect.fdstore.promotion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;

import com.freshdirect.common.pricing.Discount;
import com.freshdirect.common.pricing.EnumDiscountType;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDPromotionEligibility;
import com.freshdirect.fdstore.customer.adapter.PromoVariantHelper;
import com.freshdirect.framework.util.log.LoggerFactory;


public class FDPromotionVisitor {

	private static Category LOGGER = LoggerFactory.getInstance(FDPromotionVisitor.class);

	public static FDPromotionEligibility evaluateAndApplyPromotions(PromotionContextI context, FDPromotionEligibility eligibilities) {
		long startTime = System.currentTimeMillis();
				
		List<String> ruleBasedPromotions = FDPromotionRulesEngine.getEligiblePromotions(context);
		context.setRulePromoCode(ruleBasedPromotions);
		eligibilities = evaluatePromotions(context, eligibilities);
//		LOGGER.info("Promotion eligibility:after evaluate " + eligibilities);
		resolveConflicts(eligibilities);
		resolveLineItemConflicts(context, eligibilities);
//		LOGGER.info("Promotion eligibility:after resolve conflicts " + eligibilities);					
		Set<String> combinableOffers = applyPromotions(context, eligibilities);
		
        //Add applied line item discounts to the applied list.
        Set<String> appliedSet =  context.getLineItemDiscountCodes();
        for (final String code : appliedSet) {
        	if(eligibilities.isEligible(code)) 
        		eligibilities.setApplied(code);
        }
        
//		LOGGER.info("Promotion eligibility: after apply " + eligibilities);
//		LOGGER.info("Promotion eligibility:context.isPostPromoConflictEnabled() " + context.isPostPromoConflictEnabled());
		
		if(context.isPostPromoConflictEnabled()){
			// post resolve conflict
			boolean e = postResolveConflicts(context,eligibilities);
			if(e)
				context.getUser().setPromoConflictResolutionApplied(true);
//			LOGGER.info("Promotion eligibility: after postResolveConflicts() " + eligibilities);
			context.getUser().setPostPromoConflictEnabled(false);
			
		}		
		//Now Apply redemption promo if any.
		
        //Get the redemption promotion if user redeemed one.
		double redemptionValue = 0.0;
		String redeemCode = "";
        PromotionI redeemedPromotion = context.getRedeemedPromotion();
        if(redeemedPromotion != null){
        	redeemCode = redeemedPromotion.getPromotionCode();
        	if(eligibilities.isEligible(redeemCode)) {
        		boolean e = redeemedPromotion.apply(context);
        		if(e){
        			eligibilities.setApplied(redeemedPromotion.getPromotionCode());
        			if(redeemedPromotion.isDollarValueDiscount())
        				redemptionValue = context.getShoppingCart().getDiscountValue(redeemCode);
        		}
        	}
        }
        

        
        
        //Reconcile the discounts to make sure total header discounts does not exceed pre-deduction total(subtotal + dlv charge + tax).
        reconcileDiscounts(context, eligibilities, combinableOffers, redemptionValue);
        context.getUser().setProductSample(eligibilities.getEligibleProductSamples());
		return eligibilities;
	}




	private static void reconcileDiscounts(PromotionContextI context,
			FDPromotionEligibility eligibilities, Set<String> combinableOffers,
			double redemptionValue) {
		double remainingBalance = context.getShoppingCart().getPreDeductionTotal() - redemptionValue;
        for (Iterator<String> i = combinableOffers.iterator(); i.hasNext();) {
        	String promoCode = i.next();
        	if(remainingBalance <= 0) {
        		context.getShoppingCart().removeDiscount(promoCode);
        	} else {
	        	double oldDiscountAmt = context.getShoppingCart().getDiscountValue(promoCode);
	        	double newDiscountAmt = Math.min(remainingBalance, oldDiscountAmt);
	        	if(oldDiscountAmt != newDiscountAmt) {
		        	context.getShoppingCart().removeDiscount(promoCode);
		        	context.getShoppingCart().addDiscount(new Discount(promoCode, EnumDiscountType.DOLLAR_OFF, newDiscountAmt));
	        	}
	        	//Discount can be applied now.
	        	eligibilities.setApplied(promoCode);
	        	remainingBalance -= newDiscountAmt;
        	}
        }
	}

	
    

	/**
	 * Smart Savings no longer effective
	 * 
	 * @param context
	 * @param eligibilities
	 */
	@Deprecated
	private static void resolveLineItemConflicts(PromotionContextI context, FDPromotionEligibility eligibilities) {
		//Reload the promo variant map based on new promotion eligibilities.	
		Map pvMap = PromoVariantHelper.getPromoVariantMap(context.getUser(), eligibilities);
		context.getUser().setPromoVariantMap(pvMap);
	}

	
	 private static FDPromotionEligibility evaluatePromotions(PromotionContextI context, FDPromotionEligibility eligibilities) {
         long startTime = System.currentTimeMillis();
         int counter = 0;
         boolean apply_raf_promo = true;
         
         //Get All Automatic Promo codes.  Evaluate them.
         Collection<PromotionI> promotions = PromotionFactory.getInstance().getAllAutomaticPromotions();
         for (PromotionI autopromotion : promotions) {
               String promoCode = autopromotion.getPromotionCode();               
               boolean e = autopromotion.evaluate(context);
               eligibilities.setEligibility(promoCode, e);
               if(e && autopromotion.isFavoritesOnly()) eligibilities.addRecommendedPromo(promoCode); 
        }
         
         //Get the redemption promotion if user redeemed one and evaluate it.
         PromotionI redeemedPromotion = context.getRedeemedPromotion();
         if(redeemedPromotion != null){
        	   apply_raf_promo = false;
               boolean e = redeemedPromotion.evaluate(context);
               String promoCode = redeemedPromotion.getPromotionCode();
               eligibilities.setEligibility(promoCode, e);
               if(e && redeemedPromotion.isFavoritesOnly()) eligibilities.addRecommendedPromo(promoCode);
               if(!e) { apply_raf_promo = true; }
         }
         String wsPromoCode =  context.getUsedWSPromotionCode();
         if(wsPromoCode != null && !eligibilities.isEligible(wsPromoCode)) {
        	 //Evaluate for an already redeemed WS promotion.
        	 PromotionI promo = PromotionFactory.getInstance().getPromotion(wsPromoCode);
        	 boolean e = promo.evaluate(context);
        	 eligibilities.setEligibility(wsPromoCode, e);
         }
         
       //Evaluate the referral promotions
         if(FDStoreProperties.isExtoleRafEnabled() ? context.getUser().getRafPromoCode() != null : context.getUser().getReferralCustomerId() != null) {
        	 if(apply_raf_promo) {
	        	 //User did not use any redemption code, so its ok to check the eligibility of the referral promotion
		         Collection<PromotionI> referralPromotions = context.getUser().getReferralPromoList();         
		         for (Iterator<PromotionI> i = referralPromotions.iterator(); i.hasNext();) {        	 
		             PromotionI autopromotion  = (PromotionI) i.next(); 
		             String promoCode = autopromotion.getPromotionCode();
		             LOGGER.debug("---------------------Referral promotion: " + promoCode);
		             boolean e = autopromotion.evaluate(context);
		             eligibilities.setEligibility(promoCode, e);
		             if(e && autopromotion.isFavoritesOnly()) eligibilities.addRecommendedPromo(promoCode); 
		         }
        	 }
         }
         
         
         long endTime = System.currentTimeMillis();
         return eligibilities;
   }

	protected static List<PromotionI> resolveConflicts(boolean allowMultipleHeader, List<PromotionI> promotions) {
		if (promotions.isEmpty() || promotions.size() == 1) {
			return promotions;
		}

		List<PromotionI> l = new ArrayList<PromotionI>(promotions);
		Collections.sort(l, new Comparator<PromotionI>() {
			public int compare(PromotionI o1, PromotionI o2) {
				return o1.getPriority() - o2.getPriority();
			}
		});
		/*
		 * The following block of code eliminates all other automatic
		 * header discounts if a signup or a redemption header discount
		 * is present. Otherwise it restores all the automatic header discounts.
		 */
		boolean found = false;
		for (Iterator<PromotionI> i = l.iterator(); i.hasNext();) {
			PromotionI p = (PromotionI) i.next();
			if (!found && (
							(!allowMultipleHeader && (p.isHeaderDiscount() || p.isLineItemDiscount())&& p.isRedemption()) 
							|| 
							p.isSignupDiscount())
						  ){
				found = true;
			}else{ 
				if (found && !p.isCombineOffer()) {
					//Any promotion after this point can be removed.
					i.remove();
				}
			}
		}
		return l;
	}
	
	
	/**
	 * Resolve potential conflicts b/w promotions (by altering eligibilities).
	 */
	private static void resolveConflicts(FDPromotionEligibility eligibilities) {
		Set<String> promoCodes = eligibilities.getEligiblePromotionCodes();
		List<PromotionI> promos = new ArrayList<PromotionI>(promoCodes.size());
		for (final String promoCode : eligibilities.getEligiblePromotionCodes()) {
			PromotionI promo = PromotionFactory.getInstance().getPromotion(promoCode);
			promos.add(promo);
		}

		promos = resolveConflicts(FDStoreProperties.useMultiplePromotions(), promos);

		if (promos.size() <= promoCodes.size()) {
			Set<String> actualPromoCodes = new LinkedHashSet<String>(promos.size());
			for (Iterator<PromotionI> i = promos.iterator(); i.hasNext();) {
				PromotionI promo = (PromotionI) i.next();
				actualPromoCodes.add(promo.getPromotionCode());
			}

			//LOGGER.warn("Promotion conflict resolution from " + promoCodes + " retained " + actualPromoCodes);

			eligibilities.setEligiblity(promoCodes, false);
			eligibilities.setEligiblity(actualPromoCodes, true);
		} 

	}	
	
	
	/**
	 * Resolve potential conflicts b/w promotions (by altering eligibilities).
	 */
	private static boolean postResolveConflicts(PromotionContextI context, FDPromotionEligibility eligibilities) {
		
		// check if line item discount exists along with non combinable header promo.
		PromotionI nonCombinableHeaderPromo = context.getNonCombinableHeaderPromotion();
		double lineItemDiscAmount = context.getTotalLineItemDiscount();
		if(nonCombinableHeaderPromo == null || lineItemDiscAmount <= 0) return false;
		
		// also check if the allow header promotion flag is off 		
		boolean isLineItemCombinable = eligibilities.isLineItemCombinable();
		String headerPromoCode=nonCombinableHeaderPromo.getPromotionCode();
		if(isLineItemCombinable){
			//CLear the automatic non combinable header discount. Keep the line item.
			context.getShoppingCart().removeDiscount(headerPromoCode);
			eligibilities.removeAppliedPromo(headerPromoCode);
		}else{
			//Check which one has a higher value. Give the higher value promotion.
			Discount headerDiscount = context.getShoppingCart().getDiscount(headerPromoCode);
			if(lineItemDiscAmount >=  headerDiscount.getAmount()){
				//CLear the automatic header discount. Keep the line item discount(s).
				context.getShoppingCart().removeDiscount(headerPromoCode);
				eligibilities.removeAppliedPromo(headerPromoCode);
			} else {
				//CLear the line item discount(s). Keep the automatic header discount.
				context.clearLineItemDiscounts();
				eligibilities.removeAppliedLineItemPromotions();
			}
		}
		return true;
	}			

	private static Set<String> applyPromotions(PromotionContextI context, FDPromotionEligibility eligibilities) {
        String headerPromoCode = "";
      //Step 1: Process all sample, delivery promo, extend DP promo, automatic non-combinable header and line item offers.
        for (final String promoCode : eligibilities.getEligiblePromotionCodes()) {
              PromotionI promo = PromotionFactory.getInstance().getPromotion(promoCode);
              if(!promo.isRedemption())
              if(!promo.isDollarValueDiscount() || (!promo.isCombineOffer())) {
            	  	
                    boolean applied = promo.apply(context);
                    if (applied) {
                          if(promo.isHeaderDiscount()){
                                //This logic has been added to filter the max discount promocode
                                //when there are more than one. Currently this happens only in the
                                //case of automatic header discounts.
                                headerPromoCode = promoCode;
                          } else if(!promo.isLineItemDiscount()){
                                //Add any non-line item/header promos to the applied list. 
                                eligibilities.setApplied(promoCode);      
                          }
                    }
              }
        }
        boolean isCombinableOfferApplied = false;
        //Step 2: Process all automatic combinable header and line item offers.
        Set<String> combinableOffers = new HashSet<String>();
        for (final String promoCode : eligibilities.getEligiblePromotionCodes()) {
            PromotionI promo = PromotionFactory.getInstance().getPromotion(promoCode);
            if(promo.isDollarValueDiscount() && !promo.isRedemption() && promo.isCombineOffer()) {
          	  	//Process all automatic combinable header and line item offers.
                  boolean applied = promo.apply(context);
                  if (applied ) {
                	  if(promo.isHeaderDiscount()){
                		  isCombinableOfferApplied = true;  
                		  combinableOffers.add(promoCode);
                	  } 
                  }
            }
      }
        //Add the final header promo code to the applied list from Step 1 if isCombinableOfferApplied is false. 
        if(headerPromoCode.length() > 0 && eligibilities.isEligible(headerPromoCode)){
        	if(!isCombinableOfferApplied)
              eligibilities.setApplied(headerPromoCode);
        	else{
        		//remove non combinable header promo.
        		String code = context.getNonCombinableHeaderPromotion().getPromotionCode();
        		context.getShoppingCart().removeDiscount(code);
        	}
        }
        return combinableOffers;

  }

}
