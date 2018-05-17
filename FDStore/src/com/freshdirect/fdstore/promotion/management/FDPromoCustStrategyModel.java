package com.freshdirect.fdstore.promotion.management;

import java.util.Date;

import com.freshdirect.common.customer.EnumCardType;
import com.freshdirect.delivery.EnumComparisionType;
import com.freshdirect.delivery.EnumDeliveryOption;
import com.freshdirect.delivery.EnumPromoFDXTierType;
import com.freshdirect.framework.core.ModelSupport;

public class FDPromoCustStrategyModel extends ModelSupport {

	
	private String promotionId;
	private Integer orderRangeStart;
	private Integer orderRangeEnd;
	private String[] cohorts;
	private String[] dpTypes;
	private String dpStatus;
	private Date dpExpStart;
	private Date dpExpEnd;
	private boolean orderTypeHome;
	private boolean orderTypePickup;
	private boolean orderTypeCorporate;
	private boolean orderTypeFDX;
	private EnumPromoFDXTierType fdxTierType;
	private EnumCardType[] paymentType;
	private String priorEcheckUse;
	private boolean excludeSameDayDlv;
	private EnumDeliveryOption deliveryDayType;
	private EnumComparisionType echeckMatchType;
	

	public FDPromoCustStrategyModel() {
		super();
	}
	public FDPromoCustStrategyModel(String promotionId,
			Integer orderRangeStart, Integer orderRangeEnd, String[] cohorts,
			String[] dpTypes,String dpStatus, Date dpExpStart, Date dpExpEnd,
			boolean orderTypeHome, boolean orderTypePickup,
			boolean orderTypeCorporate, boolean orderTypeFDX,
			EnumCardType[] paymentType,
			String priorEcheckUse,EnumComparisionType echeckMatchType, EnumPromoFDXTierType fdxTierType) {
		super();
		this.promotionId = promotionId;
		this.orderRangeStart = orderRangeStart;
		this.orderRangeEnd = orderRangeEnd;
		this.cohorts = cohorts;
		this.dpStatus = dpStatus;
		this.dpExpStart = dpExpStart;
		this.dpExpEnd = dpExpEnd;
		this.orderTypeHome = orderTypeHome;
		this.orderTypePickup = orderTypePickup;
		this.orderTypeCorporate = orderTypeCorporate;
		this.orderTypeFDX = orderTypeFDX;
		this.paymentType = paymentType;
		this.priorEcheckUse = priorEcheckUse;
		this.dpTypes=dpTypes;
		this.echeckMatchType=echeckMatchType;
		this.fdxTierType = fdxTierType;
	}
	/*public FDPromoCustStrategyModel(String promotionId,
			Integer orderRangeStart, Integer orderRangeEnd, String[] cohorts,
			String dpStatus, Date dpExpStart, Date dpExpEnd) {
		super();
		this.promotionId = promotionId;
		this.orderRangeStart = orderRangeStart;
		this.orderRangeEnd = orderRangeEnd;
		this.cohorts = cohorts;
		this.dpStatus = dpStatus;
		this.dpExpStart = dpExpStart;
		this.dpExpEnd = dpExpEnd;
	}*/
	
	public String getPromotionId() {
		return promotionId;
	}
	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}
	public Integer getOrderRangeStart() {
		return orderRangeStart;
	}
	public void setOrderRangeStart(Integer orderRangeStart) {
		this.orderRangeStart = orderRangeStart;
	}
	public Integer getOrderRangeEnd() {
		return orderRangeEnd;
	}
	public void setOrderRangeEnd(Integer orderRangeEnd) {
		this.orderRangeEnd = orderRangeEnd;
	}
	public String[] getCohorts() {
		return cohorts;
	}
	public void setCohorts(String[] cohorts) {
		this.cohorts = cohorts;
	}
	public String getDpStatus() {
		return dpStatus;
	}
	public void setDpStatus(String dpStatus) {
		this.dpStatus = dpStatus;
	}
	public Date getDpExpStart() {
		return dpExpStart;
	}
	public void setDpExpStart(Date dpExpStart) {
		this.dpExpStart = dpExpStart;
	}
	public Date getDpExpEnd() {
		return dpExpEnd;
	}
	public void setDpExpEnd(Date dpExpEnd) {
		this.dpExpEnd = dpExpEnd;
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
	public boolean isExcludeSameDayDlv() {
		return excludeSameDayDlv;
	}
	public void setExcludeSameDayDlv(boolean excludeSameDayDlv) {
		this.excludeSameDayDlv = excludeSameDayDlv;
	}
	
	public String[] getDpTypes() {
		return dpTypes;
	}
	public void setDpTypes(String[] dpTypes) {
		this.dpTypes = dpTypes;
	}
	public EnumDeliveryOption getDeliveryDayType() {
		return deliveryDayType;
	}
	public void setDeliveryDayType(EnumDeliveryOption deliveryDayType) {
		this.deliveryDayType = deliveryDayType;
	}
	public EnumComparisionType getEcheckMatchType() {
		return echeckMatchType;
	}
	public void setEcheckMatchType(EnumComparisionType echeckMatchType) {
		this.echeckMatchType = echeckMatchType;
	}
	public boolean isOrderTypeFDX() {
		return orderTypeFDX;
	}
	public void setOrderTypeFDX(boolean orderTypeFDX) {
		this.orderTypeFDX = orderTypeFDX;
	}
	/**
	 * @return the fdxTierType
	 */
	public EnumPromoFDXTierType getFdxTierType() {
		return fdxTierType;
	}
	/**
	 * @param fdxTierType the fdxTierType to set
	 */
	public void setFdxTierType(EnumPromoFDXTierType fdxTierType) {
		this.fdxTierType = fdxTierType;
	}
	
	
	
}
