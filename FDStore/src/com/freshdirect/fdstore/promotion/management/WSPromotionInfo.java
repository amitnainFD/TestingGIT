package com.freshdirect.fdstore.promotion.management;

import java.util.Date;

import com.freshdirect.delivery.EnumDeliveryOption;
import com.freshdirect.delivery.EnumPromoFDXTierType;
import com.freshdirect.fdstore.promotion.EnumPromotionStatus;
import com.freshdirect.framework.core.ModelSupport;

public class WSPromotionInfo extends ModelSupport {
	private String promotionCode; 
	private String name;
	private Date effectiveDate;
	private Date startDate;
	private Date endDate;
	private EnumDeliveryOption deliveryDayType;
	private Date requestedDate;
	private boolean isRecurringPromo;
	private String waiveChargeType;
	private EnumPromoFDXTierType fdxTierType;
	
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	private String zoneCode;
	private String startTime;
	private String endTime;
	private double discount;
	private int redeemCount;
	private int redemptions;
	private int dayofweek;
	private String[] windowType;
	private String[] dayOfWeek;
	
	public int getRedemptions() {
		return redemptions;
	}
	public void setRedemptions(int redemptions) {
		this.redemptions = redemptions;
	}
	public int getDayofweek() {
		return dayofweek;
	}
	public void setDayofweek(int dayofweek) {
		this.dayofweek = dayofweek;
	}
	public int getRedeemCount() {
		return redeemCount;
	}
	public void setRedeemCount(int redeemCount) {
		this.redeemCount = redeemCount;
	}
	private EnumPromotionStatus status;
	
	public String getPromotionCode() {
		return promotionCode;
	}
	public void setPromotionCode(String promotionCode) {
		this.promotionCode = promotionCode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getEffectiveDate() {
		return effectiveDate;
	}
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	public String getZoneCode() {
		return zoneCode;
	}
	public void setZoneCode(String zoneCode) {
		this.zoneCode = zoneCode;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public double getDiscount() {
		return discount;
	}
	public void setDiscount(double discount) {
		this.discount = discount;
	}
	public EnumPromotionStatus getStatus() {
		return status;
	}
	public void setStatus(EnumPromotionStatus status) {
		this.status = status;
	}
	public String[] getWindowType() {
		return windowType;
	}
	public void setWindowType(String[] windowType) {
		this.windowType = windowType;
	}
	public EnumDeliveryOption getDeliveryDayType() {
		return deliveryDayType;
	}
	public void setDeliveryDayType(EnumDeliveryOption deliveryDayType) {
		this.deliveryDayType = deliveryDayType;
	}
	public String[] getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(String[] dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}	
	public Date getRequestedDate() {
		return requestedDate;
	}
	public void setRequestedDate(Date requestedDate) {
		this.requestedDate = requestedDate;
	}	
	public boolean isRecurringPromo() {
		return isRecurringPromo;
	}
	public void setRecurringPromo(boolean isRecurringPromo) {
		this.isRecurringPromo = isRecurringPromo;
	}
	public String getWindowTypeStr() {
		StringBuffer buf = new StringBuffer();
		if(this.windowType != null && this.windowType.length > 0){
			for(int i=0;i < this.windowType.length;i++){
				buf.append(this.windowType[i]);
				if(i < this.windowType.length-1)
				buf.append(",");
			}
			buf.append(" min(s)");
		}
		return buf.toString();
	}
	public String getDayOfWeekStr() {
		StringBuffer buf = new StringBuffer();
		if(this.dayOfWeek != null && this.dayOfWeek.length > 0){
			for(int i=0;i < this.dayOfWeek.length;i++){
				buf.append(this.dayOfWeek[i]);
				if(i < this.dayOfWeek.length-1)
				buf.append(",");
			}
		}
		return buf.toString();
	}
	/**
	 * @return the waiveChargeType
	 */
	public String getWaiveChargeType() {
		return waiveChargeType;
	}
	/**
	 * @param waiveChargeType the waiveChargeType to set
	 */
	public void setWaiveChargeType(String waiveChargeType) {
		this.waiveChargeType = waiveChargeType;
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
