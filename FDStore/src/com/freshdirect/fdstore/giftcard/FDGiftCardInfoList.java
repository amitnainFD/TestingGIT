package com.freshdirect.fdstore.giftcard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.giftcard.ErpGiftCardModel;

import java.util.Collections;

public class FDGiftCardInfoList implements Serializable {
	
	private static final long	serialVersionUID	= -1730795546450052567L;

	private List<FDGiftCardI> giftcards = null;
	
	public final static Comparator<FDGiftCardI> GIFT_CARD_RECEIVED_COMPARATOR = new Comparator<FDGiftCardI>() {

		public int compare( FDGiftCardI p1, FDGiftCardI p2 ) {
			int ret = new Double(p2.getBalance()).compareTo(new Double(p1.getBalance()));
			return ret;
		}
	};
	
	public FDGiftCardInfoList(Collection<ErpGiftCardModel> erpgiftcards) {
		this.giftcards = new ArrayList<FDGiftCardI>(erpgiftcards.size());
		for( ErpGiftCardModel gcModel : erpgiftcards ){
			this.addGiftCard(new FDGiftCardModel(gcModel));
		}
	}

	public List<FDGiftCardI> getGiftcards() {
		Collections.sort(giftcards, GIFT_CARD_RECEIVED_COMPARATOR);
		return Collections.unmodifiableList(giftcards);
	}

	public List<ErpGiftCardModel> getSelectedGiftcards(){
		List<ErpGiftCardModel> selectedCards = new ArrayList<ErpGiftCardModel>();
		for( FDGiftCardI gc : giftcards ) {
			if(FDStoreProperties.isGivexBlackHoleEnabled()|| !gc.isRedeemable() || !gc.isSelected() || gc.getBalance() <= 0 ) {
				continue;
			} 
			//Clone Gift card model object.
			ErpGiftCardModel cloneGC = (ErpGiftCardModel) gc.getGiftCardModel().deepCopy();
			//Set the balance in order to apply the hold amount during modify.
			cloneGC.setBalance(gc.getBalance());
			selectedCards.add(cloneGC);
		}
		return Collections.unmodifiableList(selectedCards);
	}
	public void addGiftCard(FDGiftCardI giftcard) {
		if(getGiftCard(giftcard.getCertificateNumber()) != null){
			//Certificate already exists. Do not add it again.
			//This is a unlikely scenario. 
			return;
		}
		this.giftcards.add(giftcard);
	}
	
	public FDGiftCardI getGiftCard(String certificationNum) {
		for( FDGiftCardI gc : giftcards ) {
			if(gc.getCertificateNumber().equals(certificationNum)){
				return gc;
			}
		}
		return null;
	}
	
	public double getTotalBalance() {
		double balance = 0.0;
		for( FDGiftCardI gc : giftcards ) {
			if(FDStoreProperties.isGivexBlackHoleEnabled() || !gc.isRedeemable() || !gc.isSelected()) {
				continue;
			}
			balance += gc.getBalance();
			//balance += gc.getHoldAmount();
		}
		return balance;
	}
	
	public void clearAllHoldAmount() {
		for( FDGiftCardI gc : giftcards ) {
			gc.setHoldAmount(0.0);
		}
	}
	public void clearAllSelection(){
		for( FDGiftCardI gc : giftcards ) {
			gc.setSelected(false);
		}
	}
	
	public void setSelected(String certificationNum, boolean selected){
		getGiftCard(certificationNum).setSelected(selected);
	}
	
	public void remove(String certificationNum){
		for(Iterator<FDGiftCardI> it = this.giftcards.iterator(); it.hasNext();) {
			FDGiftCardI gc = it.next();
			if(gc.getCertificateNumber().equals(certificationNum)){
				it.remove();
				break;
			}
		}
	}
	
	public double getGiftcardsTotalBalance() {
		double balance = 0.0;
		for( FDGiftCardI gc : giftcards ) {
			if(FDStoreProperties.isGivexBlackHoleEnabled() || !gc.isRedeemable()) {
				continue;
			}
			balance += gc.getBalance();
			//balance += gc.getHoldAmount();
		}
		return balance;
	}
	
	public int size() {
		return this.giftcards.size();
	}
}
