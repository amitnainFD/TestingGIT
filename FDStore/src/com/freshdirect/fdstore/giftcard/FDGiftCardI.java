package com.freshdirect.fdstore.giftcard;

import java.io.Serializable;

import com.freshdirect.giftcard.EnumGiftCardStatus;
import com.freshdirect.giftcard.ErpGiftCardModel;

public interface FDGiftCardI extends Serializable {
	
	public String getCertificateNumber() ;

	public double getBalance() ;

	public EnumGiftCardStatus getStatus();

	public boolean isRedeemable();

	public boolean isSelected();
	
	public void setSelected(boolean selected);
	
	public double getHoldAmount();

	public void setHoldAmount(double holdAmount);
	
	public ErpGiftCardModel getGiftCardModel();
	
}
