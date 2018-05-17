package com.freshdirect.fdstore.promotion.management.ejb;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBObject;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.promotion.PromoVariantModel;
import com.freshdirect.fdstore.promotion.Promotion;
import com.freshdirect.fdstore.promotion.PromotionI;
import com.freshdirect.fdstore.promotion.management.FDDuplicatePromoFieldException;
import com.freshdirect.fdstore.promotion.management.FDPromoCustNotFoundException;
import com.freshdirect.fdstore.promotion.management.FDPromoCustomerInfo;
import com.freshdirect.fdstore.promotion.management.FDPromoTypeNotFoundException;
import com.freshdirect.fdstore.promotion.management.FDPromotionModel;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.framework.core.PrimaryKey;

public interface FDPromotionManagerSB extends EJBObject {

	/**
	 * @return List of FDPromotionModel
	 */
	public List<FDPromotionModel> getPromotions() throws FDResourceException, RemoteException;	

	public PrimaryKey createPromotion(FDPromotionModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException, RemoteException;	
	
	public FDPromotionModel getPromotion(String promoId) throws FDResourceException, RemoteException;
	
	public List<PromoVariantModel> getPromotionVariants(String promoId) throws FDResourceException, RemoteException;	
	
	public void storePromotion(FDPromotionModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException, RemoteException;
	
	public void removePromotion(PrimaryKey pk) throws FDResourceException, RemoteException;
	
	public List<FDPromoCustomerInfo> getPromoCustomerInfoListFromPromotionId(PrimaryKey pk) throws FDResourceException, RemoteException;		
	
	public List<FDPromoCustomerInfo> getPromoCustomerInfoListFromCustomerId(PrimaryKey pk) throws FDResourceException, RemoteException;		

	public List<FDPromotionModel> getAvailablePromosForCustomer(PrimaryKey pk) throws FDResourceException, RemoteException;
	
	public void insertPromoCustomers(FDActionInfo actionInfo, List<FDPromoCustomerInfo> promoCustomers) throws FDResourceException, RemoteException;

	public void updatePromoCustomers(FDActionInfo actionInfo, List<FDPromoCustomerInfo> promoCustomers) throws FDResourceException, RemoteException;
	
	public void removePromoCustomers(FDActionInfo actionInfo, List<FDPromoCustomerInfo> promoCustomers) throws FDResourceException, RemoteException;
	
	/*
	 * New Methods added to implement the new design for how promotions 
	 * will be loaded, evaluated and applied using the new caching framework.
	 * 
	 */
	public List<Promotion> getAllAutomtaticPromotions() throws RemoteException;
	
	public List<Promotion> getModifiedOnlyPromos(Date lastModified) throws RemoteException;
	
	public PromotionI getPromotionForRT(String promoId) throws RemoteException;
	
	public String getRedemptionPromotionId(String  redemptionCode) throws RemoteException;
	
	public Map<String,Timestamp> refreshAutomaticPromotionCodes() throws RemoteException;
	
	public Map<String,Timestamp> getPromotionCodes() throws RemoteException;
	
	public List<PromoVariantModel> getAllActivePromoVariants(List<EnumSiteFeature> smartSavingsFeatures) throws RemoteException;
}
