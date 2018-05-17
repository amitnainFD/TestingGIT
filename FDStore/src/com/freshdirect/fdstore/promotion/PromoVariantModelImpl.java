package com.freshdirect.fdstore.promotion;

import com.freshdirect.fdstore.util.EnumSiteFeature;

public class PromoVariantModelImpl implements PromoVariantModel {
	private static final long serialVersionUID = 4213257883052956128L;

	private String variantId;
	private String promoCode;
	private int promoPriority;
	private EnumSiteFeature siteFeature;
	private int variantPriority;
	
	public int getVariantPriority() {
		return variantPriority;
	}

	public PromoVariantModelImpl(String variantId, String promoCode,int priority, EnumSiteFeature siteFeature, int variantPriority){
		this.variantId = variantId;
		this.promoCode = promoCode;
		this.promoPriority = priority;
		this.siteFeature = siteFeature;
		this.variantPriority = variantPriority;
	}
	
	public int getPromoPriority() {
		return promoPriority;
	}

	public PromotionI getAssignedPromotion() {
		return PromotionFactory.getInstance().getPromotion(this.promoCode);
	}
	/*
	public void setPromoCode(String promoCode) {
		this.promoCode = promoCode;
	}
	*/
	public EnumSiteFeature getSiteFeature() {
		return siteFeature;
	}
	public String getVariantId() {
		return variantId;
	}
	public void setVariantId(String variantId) {
		this.variantId = variantId;
	}

	public double getPercentageOff() {
		return this.getAssignedPromotion().getLineItemDiscountPercentOff();
	}

}
