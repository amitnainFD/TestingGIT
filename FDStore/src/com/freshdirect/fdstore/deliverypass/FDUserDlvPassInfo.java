package com.freshdirect.fdstore.deliverypass;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import com.freshdirect.deliverypass.DeliveryPassType;
import com.freshdirect.deliverypass.DlvPassConstants;
import com.freshdirect.deliverypass.EnumDlvPassStatus;

public class FDUserDlvPassInfo implements Serializable{
	private EnumDlvPassStatus status;
	private DeliveryPassType type;
	private Date expDate;
	private String originalOrderId;
	private int remainingCount;
	private int usedCount;
	private int usablePassCount;
	private boolean isFreeTrialRestricted;
	private int autoRenewUsablePassCount;
	private DeliveryPassType autoRenewDPType;
	private double autoRenewPrice;
	private NumberFormat CURRENCY_FORMATTER=NumberFormat.getCurrencyInstance(Locale.US);
	private int daysSinceDPExpiry=0;
	private int daysToDPExpiry=0;
	private double dPSavings;
	private Date purchaseDate;
	
	public FDUserDlvPassInfo(EnumDlvPassStatus status, DeliveryPassType type, Date expDate, String originalOrderId, int remCnt, int usedCount,int usablePassCount, boolean isFreeTrialRestricted,int autoRenewUsablePassCount, DeliveryPassType autoRenewDPType, double autoRenewPrice, Date purchaseDate) {
		super();
		this.status = status;
		this.type = type;
		this.expDate = expDate;
		this.originalOrderId = originalOrderId;
		this.remainingCount = remCnt;
		this.usedCount = usedCount;
		this.usablePassCount=usablePassCount;
		this.isFreeTrialRestricted=isFreeTrialRestricted;
		this.autoRenewUsablePassCount=autoRenewUsablePassCount;
		this.autoRenewDPType=autoRenewDPType;
		this.autoRenewPrice=autoRenewPrice;
		this.purchaseDate = purchaseDate;
	}

	public int getUsablePassCount() {
		return usablePassCount;
	}
	
	public void setUsablePassCount(int _usablePassCount) {
		usablePassCount=_usablePassCount;
	}

	public Date getExpDate() {
		return expDate;
	}


	public boolean isUnlimited() {
		if(type != null) {
			return type.isUnlimited();	
		}
		return false;
	}


	public EnumDlvPassStatus getStatus() {
		return status;
	}
	
	public void setStatus(EnumDlvPassStatus status) {
		this.status = status;
	}
	
	public String getOriginalOrderId() {
		return originalOrderId;
	}

	public int getRemainingCount() {
		return remainingCount;
	}

	public int getUsedCount() {
		return usedCount;
	}
	
	public DeliveryPassType getTypePurchased(){
		return type;
	}
	
	public boolean isFreeTrialRestricted() {
		return isFreeTrialRestricted;
	}
	
	public void setIsFreeTrialRestricted(boolean isFreeTrialRestricted) {
		this.isFreeTrialRestricted=isFreeTrialRestricted;
	}
	
	public int getAutoRenewUsablePassCount() {
		return autoRenewUsablePassCount;
	}
	
	public void setAutoRenewUsablePassCount(int _autoRenewUsablePassCount) {
		autoRenewUsablePassCount=_autoRenewUsablePassCount;
	}
	
	public double getAutoRenewPrice() {
		return autoRenewPrice;
	}
	
	public String getAutoRenewPriceAsText() {
		return CURRENCY_FORMATTER.format(autoRenewPrice);
	}
	
	public DeliveryPassType getAutoRenewDPType() {
		return autoRenewDPType;
	}
	
	public String getAutoRenewDPTerm() {
		
		String name=autoRenewDPType.getName();
		
		if(name.indexOf(DlvPassConstants.UNLIMITED)!=-1) {
			int months=autoRenewDPType.getDuration()/30;
			
			return DeliveryPassUtil.getAsText(months)+" months";
			//return name.substring(0,name.indexOf(DlvPassConstants.UNLIMITED)).trim().toLowerCase();
			
		}
		else {
			return name;
		}
	}
	public void setDaysSinceDPExpiry(int _daysSinceDPExpiry) {
		this.daysSinceDPExpiry=_daysSinceDPExpiry;
	}
	
	public int getDaysSinceDPExpiry() {
		return daysSinceDPExpiry;
	}
	
	public void setDaysToDPExpiry(int _daysToDPExpiry) {
		this.daysToDPExpiry=_daysToDPExpiry;
	}
	
	public int getDaysToDPExpiry() {
		return daysToDPExpiry;
	}

	public double getDPSavings() {
		return dPSavings;
	}
	
	public void setDPSavings(double savings) {
		dPSavings = savings;
}

	public Date getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	
}
