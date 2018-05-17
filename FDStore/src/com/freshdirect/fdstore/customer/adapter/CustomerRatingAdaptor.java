package com.freshdirect.fdstore.customer.adapter;

import java.io.Serializable;

import com.freshdirect.customer.CustomerRatingI;
import com.freshdirect.fdstore.customer.ProfileModel;

public class CustomerRatingAdaptor implements CustomerRatingI, Serializable {
	
	private static final long	serialVersionUID	= -818326345113597405L;
	
	private final ProfileModel profile;
	private final int validOrderCount;
	private final boolean unDeclared;
	private final boolean cosCustomer;
	
 	public CustomerRatingAdaptor(ProfileModel profile, boolean cosCustomer,int validOrdCount) {
 		if (profile==null) {
 			throw new IllegalArgumentException("the profile parameter cannot be null.");
 		}
 		this.profile = profile;
 		this.cosCustomer = cosCustomer;
 		this.validOrderCount = validOrdCount;
 		unDeclared = ( this.validOrderCount >=2 && 
 		               this.validOrderCount <= 4);
 	}
 	
 	public int getCustomerStarRating() /*throws FDResourceException */{
		/* return
		 *  0 : if no relevant profile info
		 *  1 : if Undelcared(customer has 2-4 orders completed)or 
		 *  2 : if customer is in Gold segment or a COS customer
		 *  3 : if Customer is a VIP Customer.
		 */
		
		if (profile.isVIPCustomer()) return 3;
		if (cosCustomer || "1".equals(profile.getCustomerMetalType())) return 2;
		if (this.unDeclared) return 1;
		return 0;
	}
	
	public boolean isVIPCustomer() {
		return profile.isVIPCustomer();
	}
	
	public boolean isChefsTableMember() {
		return profile.isChefsTable();
	}
	
	public String getMetalCategory() {
		return profile.getCustomerMetalType();
	}
	
	public boolean isOnFDAccount(){
		return this.profile.isOnFDAccount();
	}
	
	public boolean isPhonePrivate() {
		return this.profile.isPhonePrivate();
	}

}
