/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer;

import java.rmi.RemoteException;
import java.util.Date;

import com.freshdirect.fdstore.customer.ejb.FDCustomerEStoreModel;
import com.freshdirect.fdstore.customer.ejb.FDCustomerSmsPreferencePersistentBean;
import com.freshdirect.framework.core.ModelSupport;


/**
 * FDCustomer model class.
 *
 * @version    $Revision$
 * @author     $Author$
 * @stereotype fd-model
 */
public class FDCustomerModel extends ModelSupport implements FDCustomerI {
    
    private String erpCustomerPK;
    private int loginCount;
    private Date lastLogin;
    private String defaultShipToAddressPK;
    private String defaultPaymentMethodPK;
    private String defaultDepotLocationPK;   
	private ProfileModel profile;
    private String passwordHint;
    private String depotCode;
    private java.util.Date passwordRequestExpiration;
    private FDCustomerEStoreModel customerEStoreModel;
    private FDCustomerEStoreModel customerSmsPreferenceModel;
    private String rafClickId;
    private String rafPromoCode;
    
	/**
     * Default constructor.
     */
    public FDCustomerModel() {
        super();
        customerEStoreModel = new FDCustomerEStoreModel();
        customerSmsPreferenceModel= new FDCustomerEStoreModel();
    }
    
    public String getErpCustomerPK() {
        return this.erpCustomerPK;
    }
    
    public void setErpCustomerPK(String erpCustomerPK) {
        this.erpCustomerPK = erpCustomerPK;
    }
    
    
    public void incrementLoginCount() {
        this.loginCount++;
        this.lastLogin = new Date();
    }
    
    public int getLoginCount() {
        return this.loginCount;
    }
    
    public void setLoginCount(int i) {
        this.loginCount = i;
    }
    
    public Date getLastLogin() {
        return this.lastLogin;
    }
    
    public void setLastLogin(Date d) {
        this.lastLogin = d;
    }
    
    public String getDefaultShipToAddressPK() {
        return this.defaultShipToAddressPK;
    }
    
    public void setDefaultShipToAddressPK(String addressPK) {
        this.defaultShipToAddressPK = addressPK;
    }
    
    public String getDefaultPaymentMethodPK() {
        return this.defaultPaymentMethodPK;
    }
    
    public void setDefaultPaymentMethodPK(String pmPK) {
        this.defaultPaymentMethodPK = pmPK;
    }
    
    /**
     * method to get a default depot location id for a depot customer
     * 
     * @return String id of the default location
     */
    public String getDefaultDepotLocationPK(){
    	return this.defaultDepotLocationPK;
    }
    
    /**
     * method to set a default depot location id for a depot customer
     * 
     * @param String id of the depot location to set
     */
    public void setDefaultDepotLocationPK(String defaultDepotLocationPK){
    	this.defaultDepotLocationPK = defaultDepotLocationPK;
    }
    
    public ProfileModel getProfile(){
        return this.profile;
    }
    public void setProfile(ProfileModel profile){
        this.profile = profile;
    }
    
    public String getPasswordHint() {
        return this.passwordHint;
    }
    
    public void setPasswordHint(String s) {
        this.passwordHint = s;
    }
    
    public String getDepotCode(){
    	return this.depotCode;
    }
    
    public void setDepotCode(String depotCode){
    	this.depotCode = depotCode;
    }
    
    public Date getPasswordRequestExpiration(){
    	return this.passwordRequestExpiration;
    }
    
    public void setPasswordRequestExpiration(Date expiration){
    	this.passwordRequestExpiration = expiration;
    }


	public boolean isEligibleForSignupPromo() {
		String eligibleAttr = this.profile.getAttribute("signup_promo_eligible");
		if ("deny".equalsIgnoreCase(eligibleAttr)) {
			return false;
		}
		return true;
	}

	@Override
	public int getPymtVerifyAttempts() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int incrementPymtVerifyAttempts() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void resetPymtVerifyAttempts() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	

	public boolean isEligibleForDDPP() {
		String eligibleAttr = this.profile.getAttribute("DDPP_ELIGIBLE");
		if ("true".equalsIgnoreCase(eligibleAttr)) {
			return true;
		}
		return false;
	}
	
	public boolean isEligibleForCoupons() {
		String eligibleAttr = this.profile.getAttribute("COUPONS_ELIGIBLE");
		if ("true".equalsIgnoreCase(eligibleAttr)) {
			return true;
		}
		return false;
	}
 
	 /**
	 * @return the customerEStoreModel
	 */
	public FDCustomerEStoreModel getCustomerEStoreModel() {
		return customerEStoreModel;
	}

	/**
	 * @param customerEStoreModel the customerEStoreModel to set
	 */
	public void setCustomerEStoreModel(FDCustomerEStoreModel customerEStoreModel) {
		this.customerEStoreModel = customerEStoreModel;
	}
	
	public String getRafClickId() {
		return rafClickId;
	}

	public void setRafClickId(String rafClickId) {
		this.rafClickId = rafClickId;
	}

	
	public FDCustomerEStoreModel getCustomerSmsPreferenceModel() {
		return customerSmsPreferenceModel;
	}

	public void setCustomerSmsPreferenceModel(
			FDCustomerEStoreModel customerSmsPreferenceModel) {
		this.customerSmsPreferenceModel = customerSmsPreferenceModel;
	}

	@Override
	public void setPymtVerifyAttempts(int pymtVerifyAttempts)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public String getRafPromoCode() {
		return rafPromoCode;
	}

	public void setRafPromoCode(String rafPromoCode) {
		this.rafPromoCode = rafPromoCode;
	}


}