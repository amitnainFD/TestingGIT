package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.freshdirect.fdstore.content.ProductReference;
import com.freshdirect.fdstore.promotion.EnumPromotionType;
import com.freshdirect.fdstore.promotion.FDMinDCPDTotalPromoData;
import com.freshdirect.fdstore.promotion.ProductSampleApplicator;
import com.freshdirect.fdstore.promotion.PromotionApplicatorI;
import com.freshdirect.fdstore.promotion.PromotionFactory;
import com.freshdirect.fdstore.promotion.PromotionI;

public class FDPromotionEligibility implements Serializable {

	private static final long	serialVersionUID	= 3747005615570612091L;

	/** Set of String (promotionCode) */
	private final Set<String> eligibilePromos = new LinkedHashSet<String>();

	/** Set of String (promotionCode) */
	private final Set<String> appliedPromos = new LinkedHashSet<String>();
	
	private final Set<String> recommendedPromos = new LinkedHashSet<String>();
	
	private final Map<String, FDMinDCPDTotalPromoData> minDCPDTotalPromos = new LinkedHashMap<String, FDMinDCPDTotalPromoData>();//<promo code, FDMinDCPDTotalPromoModel from CartStrategy>
	
	
	public FDPromotionEligibility(){
		System.out.println("this is FDPromotionEligibility");
	}
	
	public boolean isEligible(String promotionCode) {
		return this.eligibilePromos.contains(promotionCode);
	}

	public void setEligibility(String promotionCode, boolean eligibile) {
		if (eligibile) {
			this.eligibilePromos.add(promotionCode);
		} else {
			this.eligibilePromos.remove(promotionCode);
		}
	}

	/**
	 * @param redemptionPromos Set of String
	 */
	public void setEligiblity(Set<String> promotionCodes, boolean eligibile) {
		if (eligibile) {
			this.eligibilePromos.addAll(promotionCodes);
		} else {
			this.eligibilePromos.removeAll(promotionCodes);
		}
	}

	public void setApplied(String promotionCode) {
		if (!this.eligibilePromos.contains(promotionCode)) {
			throw new IllegalArgumentException("Attempted to apply non-eligible promotion " + promotionCode);
		}
		this.appliedPromos.add(promotionCode);
	}
	
	public void removeAppliedPromo(String promotionCode){
		if (!this.eligibilePromos.contains(promotionCode)) {
			throw new IllegalArgumentException("Attempted to apply non-eligible promotion " + promotionCode);
		}
		if (!this.appliedPromos.contains(promotionCode)) {
			throw new IllegalArgumentException("Attempted to remove non-applied promotion " + promotionCode);
		}
		this.appliedPromos.remove(promotionCode);
	}
	

	public boolean isApplied(String promotionCode) {
		return this.appliedPromos.contains(promotionCode);
	}

	/** @return Set of String (promotionCode) */
	public Set<String> getAppliedPromotionCodes() {
		return Collections.unmodifiableSet(this.appliedPromos);
	}

	/** @return Set of String (promotionCode) */
	public Set<String> getEligiblePromotionCodes() {
		return Collections.unmodifiableSet(this.eligibilePromos);
	}

	/** @return Set of String (promotionCode) */
	public Set<String> getEligiblePromotionCodes(EnumPromotionType type) {
		Set<String> s = PromotionFactory.getInstance().getPromotionCodesByType(type);
		s.retainAll(this.eligibilePromos);
		return s;
	}

	public boolean isEligibleForType(EnumPromotionType type) {
		return this.getEligiblePromotionCodes(type).size() > 0;
	}
	
	
	public void removeAppliedLineItemPromotions(){			
		for ( Iterator<String> it = appliedPromos.iterator(); it.hasNext(); ) {
			String promoCode = it.next();
			PromotionI promo = PromotionFactory.getInstance().getPromotion(promoCode);
			if(promo.isLineItemDiscount()) {
				it.remove();
			}
		}
	}
	
	public boolean isLineItemCombinable(){			
		for ( String promoCode : appliedPromos ) {
			PromotionI promo = PromotionFactory.getInstance().getPromotion(promoCode);
			if(promo.isLineItemDiscount() && promo.isCombineOffer()) return true;
		}
		return false;
	}
	

	public String toString() {
		return "FDPromotionEligibility[eligible=" + this.eligibilePromos.toString() + ", applied=" + this.appliedPromos + "] ";
	}

	public Set<String> getRecommendedPromos() {
		return Collections.unmodifiableSet(this.recommendedPromos);
	}

	public void addRecommendedPromo(String promoCode) {
		this.recommendedPromos.add(promoCode);
	}
	
	
	public List<ProductReference> getEligibleProductSamples() {
		List<ProductReference> eligibleProdSamples = new ArrayList<ProductReference>();
		ProductSampleApplicator productSampleApplicator = null;
		Set<String> eligibleProdSamplePromotions = getEligiblePromotionCodes(EnumPromotionType.PRODUCT_SAMPLE);
		eligibleProdSamplePromotions.retainAll(this.eligibilePromos);
		
		for ( Iterator<String> it = eligibleProdSamplePromotions.iterator(); it.hasNext(); ) {
			String promoCode = it.next();
			PromotionI promo = PromotionFactory.getInstance().getPromotion(promoCode);
			for(Iterator<PromotionApplicatorI> i = promo.getApplicatorList().iterator(); i.hasNext();){
				PromotionApplicatorI _applicator = i.next();
				if(_applicator instanceof ProductSampleApplicator) {
					productSampleApplicator = (ProductSampleApplicator) _applicator;
				}
			}
			if(null != productSampleApplicator.getSampleProduct())
			eligibleProdSamples.add(productSampleApplicator.getProductReference());
		}
		return eligibleProdSamples;
	}

	public Map<String, FDMinDCPDTotalPromoData> getMinDCPDTotalPromos() {
		return minDCPDTotalPromos;
	}

	

}
