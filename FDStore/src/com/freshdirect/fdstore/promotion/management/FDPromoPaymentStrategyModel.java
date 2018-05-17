package com.freshdirect.fdstore.promotion.management;

import com.freshdirect.common.customer.EnumCardType;
import com.freshdirect.delivery.EnumComparisionType;
import com.freshdirect.framework.core.ModelSupport;

public class FDPromoPaymentStrategyModel extends ModelSupport {

	
	private String promotionId;
	private boolean orderTypeHome;
	private boolean orderTypePickup;
	private boolean orderTypeCorporate;	
	private EnumCardType[] paymentType;
	private String priorEcheckUse;
	private EnumComparisionType echeckMatchType;
	
	
	public FDPromoPaymentStrategyModel() {
		super();
	}

	public FDPromoPaymentStrategyModel(String promotionId,
			boolean orderTypeHome, boolean orderTypePickup,
			boolean orderTypeCorporate, EnumCardType[] paymentType, String priorEcheckUse,EnumComparisionType echeckMatchType) {
		super();
		this.promotionId = promotionId;
		this.orderTypeHome = orderTypeHome;
		this.orderTypePickup = orderTypePickup;
		this.orderTypeCorporate = orderTypeCorporate;		
		this.paymentType = paymentType;
		this.priorEcheckUse = priorEcheckUse;
		this.echeckMatchType=echeckMatchType;
	}

	public String getPromotionId() {
		return promotionId;
	}

	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}

	public boolean isOrderTypeHome() {
		return orderTypeHome;
	}

	public void setOrderTypeHome(boolean orderTypeHome) {
		this.orderTypeHome = orderTypeHome;
	}

	public boolean isOrderTypePickup() {
		return orderTypePickup;
	}

	public void setOrderTypePickup(boolean orderTypePickup) {
		this.orderTypePickup = orderTypePickup;
	}

	public boolean isOrderTypeCorporate() {
		return orderTypeCorporate;
	}

	public void setOrderTypeCorporate(boolean orderTypeCorporate) {
		this.orderTypeCorporate = orderTypeCorporate;
	}

	public EnumCardType[] getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(EnumCardType[] paymentType) {
		this.paymentType = paymentType;
	}

	public String getPriorEcheckUse() {
		return priorEcheckUse;
	}

	public void setPriorEcheckUse(String priorEcheckUse) {
		this.priorEcheckUse = priorEcheckUse;
	}

	public EnumComparisionType getEcheckMatchType() {
		return echeckMatchType;
	}

	public void setEcheckMatchType(EnumComparisionType echeckMatchType) {
		this.echeckMatchType = echeckMatchType;
	}
	
	
}
