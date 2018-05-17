package com.freshdirect.fdstore.ecoupon;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.ecoupon.model.FDCouponInfo;

public class FDCustomerCoupon implements Serializable {
	
	private final static NumberFormat FORMAT_CURRENCY = NumberFormat.getCurrencyInstance(Locale.US);
	private final static NumberFormat FORMAT_PERCENTAGE = NumberFormat.getPercentInstance(Locale.US);
	private static final DateFormat MONTH_DATE_YEAR_FORMATTER = new SimpleDateFormat("MM.dd.yyyy");
			
	private String couponId;
	private int version;
	private String displayDescription;
	private String detailedDescription;
	private String value;
	private String quantity;
	private EnumCouponOfferType offerType;
	private String expirationDate;
	
	private EnumCouponStatus status;
	
	private EnumCouponDisplayStatus displayStatus;
	
	private FDCouponProductInfo couponProductInfo ;
	private boolean displayStatusMessage;
	private EnumCouponContext context;
	
	public FDCustomerCoupon(FDCouponInfo coupon, EnumCouponStatus status, FDCouponProductInfo couponProductInfo, boolean displayStatusMessage,EnumCouponContext context) {
		
		this.couponId = coupon.getCouponId();
		this.version = coupon.getVersion();
		this.context = context;
		if(coupon.getValue() != null && coupon.getValue().length() > 0) {
			if(EnumCouponContext.CHECKOUT.equals(context) && EnumCouponStatus.COUPON_APPLIED.equals(status)){
				if(EnumCouponOfferType.PERCENT_OFF.equals(coupon.getOfferType())){
					this.displayDescription = "Saved "+FORMAT_PERCENTAGE.format(Double.parseDouble(coupon.getValue())/100)+" with coupon";
				}else{
					this.displayDescription = "Saved "+FORMAT_CURRENCY.format(Double.parseDouble(coupon.getValue()))+" with coupon";
				}
			}else{
				if(EnumCouponOfferType.PERCENT_OFF.equals(coupon.getOfferType())){
					this.displayDescription = "Save "+FORMAT_PERCENTAGE.format(Double.parseDouble(coupon.getValue())/100);
				}else{
					this.displayDescription = "Save "+FORMAT_CURRENCY.format(Double.parseDouble(coupon.getValue()));
				}
			}
		}
		this.detailedDescription = coupon.getRequirementDescription();// + ". Coupon expires on: "+coupon.getExpirationDate();
		this.status = status;
		this.value = coupon.getValue();
		this.quantity = coupon.getRequiredQuantity();
		this.offerType = coupon.getOfferType();
		if(null !=coupon.getExpirationDate()){								
			this.expirationDate = "Expires "+MONTH_DATE_YEAR_FORMATTER.format(coupon.getExpirationDate());
		}
		this.couponProductInfo = couponProductInfo;
		this.displayStatusMessage = displayStatusMessage;
	}
	
	public FDCustomerCoupon(FDCartLineI cartLine,EnumCouponStatus status,FDCouponProductInfo couponProductInfo,boolean displayStatusMessage) {
		if(null !=cartLine.getCouponDiscount()){
			this.couponId = cartLine.getCouponDiscount().getCouponId();
			this.displayDescription = "Saved "+FORMAT_CURRENCY.format(cartLine.getCouponDiscount().getDiscountAmt())+" with coupon";
			this.detailedDescription = cartLine.getCouponDiscount().getCouponDesc();
			this.status = status;
			this.value = ""+cartLine.getCouponDiscount().getDiscountAmt();
			this.quantity= ""+cartLine.getCouponDiscount().getRequiredQuantity();
			this.couponProductInfo = couponProductInfo;
			this.displayStatusMessage = displayStatusMessage;
		}
	}
	
	public String getCouponId() {
		return couponId;
	}

	public int getVersion() {
		return version;
	}

	public String getDisplayDescription() {
		return displayDescription;
	}

	public String getDetailedDescription() {
		return detailedDescription;
	}

	public EnumCouponStatus getStatus() {
		return status;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the quantity
	 */
	public String getQuantity() {
		return quantity;
	}

	public EnumCouponDisplayStatus getDisplayStatus() {
		return displayStatus;
	}

	public void setDisplayStatus(EnumCouponDisplayStatus displayStatus) {
		this.displayStatus = displayStatus;
	}

	public EnumCouponOfferType getOfferType() {
		return offerType;
	}

	public String getExpirationDate() {
		return expirationDate;
	}

	public FDCouponProductInfo getCouponProductInfo() {
		return couponProductInfo;
	}

	public boolean isDisplayStatusMessage() {
		return displayStatusMessage;
	}

	public void setDisplayStatusMessage(boolean displayStatusMessage) {
		this.displayStatusMessage = displayStatusMessage;
	}

	public EnumCouponContext getContext() {
		return context;
	}

	public void setStatus(EnumCouponStatus status) {
		this.status = status;
	}
	
	
}
