package com.freshdirect.fdstore.giftcard;

import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.framework.util.FormatterUtil;
import com.freshdirect.giftcard.EnumGiftCardStatus;
import com.freshdirect.giftcard.ErpGiftCardModel;

public class FDGiftCardModel implements FDGiftCardI {
	
	private static final long	serialVersionUID	= -5597089769677225165L;
	
	private ErpGiftCardModel giftCardModel;
	private boolean selected = true;
	private double holdAmount;
	
	public FDGiftCardModel(ErpGiftCardModel model) {
		this.giftCardModel = model;
	}
			
	public ErpGiftCardModel getGiftCardModel() {
		return giftCardModel;
	}
	
	public String getPurchaseSaleId(){
		return this.giftCardModel.getPurchaseSaleId();
	}
	
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public String getCertificateNumber() {
		return giftCardModel.getCertificateNumber();
	}

	public double getBalance() {
		if(getStatus().equals(EnumGiftCardStatus.UNKNOWN) && !FDStoreProperties.isGivexBlackHoleEnabled()){
			try{
				ErpGiftCardModel model = FDCustomerManager.verifyStatusAndBalance(giftCardModel, true);
				giftCardModel.setStatus(model.getStatus());
				giftCardModel.setBalance(model.getBalance());
			}catch (Exception e) {
				throw new FDRuntimeException(e);
			}
			
		}
		return giftCardModel.getBalance() + this.getHoldAmount();
	}

	public double getOriginalAmount() {
		return giftCardModel.getOriginalAmount();
	}
	
	public EnumGiftCardStatus getStatus() {
		return giftCardModel.getStatus();
	}
	
	public EnumGiftCardStatus verifyStatus() {
		if(getStatus().equals(EnumGiftCardStatus.UNKNOWN) && !FDStoreProperties.isGivexBlackHoleEnabled()){
			try{
				ErpGiftCardModel model = FDCustomerManager.verifyStatusAndBalance(giftCardModel, false);
				giftCardModel.setStatus(model.getStatus());
			}catch (Exception e) {
				throw new FDRuntimeException(e);
			}
		}
		return giftCardModel.getStatus();
	}
	
	public boolean isRedeemable() {
		return giftCardModel.isRedeemable();
	}

	public double getHoldAmount() {
		return holdAmount;
	}

	public void setHoldAmount(double holdAmount) {
		this.holdAmount = holdAmount;
	}
	
	public void invalidate() {
		this.giftCardModel.setStatus(EnumGiftCardStatus.UNKNOWN);
	} 
	
	public String getFormattedBalance(){
		return FormatterUtil.formatToTwoDecimal(getBalance());
	}
	public String getFormattedOrigAmount(){
		return FormatterUtil.formatToTwoDecimal(getOriginalAmount());
	}	
	
}
