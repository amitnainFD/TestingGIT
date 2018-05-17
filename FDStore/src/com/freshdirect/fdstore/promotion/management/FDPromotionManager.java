package com.freshdirect.fdstore.promotion.management;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.promotion.FDPromotionModelFactory;
import com.freshdirect.fdstore.promotion.PromotionFactory;
import com.freshdirect.fdstore.promotion.PromotionI;
import com.freshdirect.fdstore.promotion.management.ejb.FDPromotionManagerHome;
import com.freshdirect.fdstore.promotion.management.ejb.FDPromotionManagerSB;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.util.log.LoggerFactory;

@Deprecated
public class FDPromotionManager {

	private static Category LOGGER = LoggerFactory.getInstance(FDPromotionManager.class);

	private static FDPromotionManagerHome managerHome = null;	
	
	public static PrimaryKey createPromotion(FDPromotionModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			PrimaryKey pk = sb.createPromotion(promotion);
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionModelFactory.getInstance().forceRefresh();
			return pk;
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		
			
	}

	public static List getPromotions() throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.getPromotions();
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}
	
	public static FDPromotionModel loadPromotion(String promotionId) throws FDResourceException, FinderException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.getPromotion(promotionId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}
	
	
	public static List loadPromotionVariants(String promotionId) throws FDResourceException, FinderException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.getPromotionVariants(promotionId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}
	
	
	public static void storePromotion(FDPromotionModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			sb.storePromotion(promotion);
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionModelFactory.getInstance().forceRefresh();
			//Refresh the RT cache as well for placing orders through CRM.
			PromotionFactory.getInstance().forceRefresh(promotion.getPromotionCode());
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}

	public static void removePromotion(String promotionId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			sb.removePromotion(new PrimaryKey(promotionId));
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionModelFactory.getInstance().forceRefresh();
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}

	private static void invalidateManagerHome() {
		managerHome = null;
	}
	
	private static void lookupManagerHome() throws FDResourceException {
		if (managerHome != null) {
			return;
		}
		Context ctx = null;
		try {
			ctx = FDStoreProperties.getInitialContext();
			managerHome = (FDPromotionManagerHome) ctx.lookup(FDStoreProperties.getFDPromotionManagerHome());
		} catch (NamingException ne) {
			throw new FDResourceException(ne);
		} finally {
			try {
				if (ctx != null) {
					ctx.close();
				}
			} catch (NamingException ne) {
				LOGGER.warn("Cannot close Context while trying to cleanup", ne);
			}
		}
	}
	
	public static List getPromoCustomerInfoListFromPromotionId(String promotionId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.getPromoCustomerInfoListFromPromotionId(new PrimaryKey(promotionId));
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}
	
	public static List getPromoCustomerInfoListFromCustomerId(String customerId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.getPromoCustomerInfoListFromCustomerId(new PrimaryKey(customerId));
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}				
	}

	public static List getAvailablePromosForCustomer(String customerId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.getAvailablePromosForCustomer(new PrimaryKey(customerId));
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}				
		
	}
	
	public static void insertPromoCustomers(FDActionInfo actionInfo, List promoCustomers) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			sb.insertPromoCustomers(actionInfo, promoCustomers);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}				
	}

	public static void updatePromoCustomers(FDActionInfo actionInfo, List promoCustomers) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			sb.updatePromoCustomers(actionInfo, promoCustomers);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}				
	}
	
	public static void removePromoCustomers(FDActionInfo actionInfo, List promoCustomers) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			sb.removePromoCustomers(actionInfo, promoCustomers);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}				
	}
	public static List getAllAutomtaticPromotions() throws FDResourceException {
		lookupManagerHome();
		
		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.getAllAutomtaticPromotions();
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
		 
	}

	public static List getModifiedOnlyPromos(Date lastModified) throws FDResourceException {
		lookupManagerHome();
		
		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.getModifiedOnlyPromos(lastModified);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
		 
	}

	
	public static PromotionI getPromotionForRT(String  promoId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.getPromotionForRT(promoId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}				
	}
	
	public static String getRedemptionPromotionId(String  redemptionCode) throws FDResourceException{
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.getRedemptionPromotionId(redemptionCode);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static Map refreshAutomaticPromotionCodes() throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.refreshAutomaticPromotionCodes();
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static Map getPromotionCodes() throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.getPromotionCodes();
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static List getAllActivePromoVariants(List smartSavingsFeatures) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerSB sb = managerHome.create();
			return sb.getAllActivePromoVariants(smartSavingsFeatures);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
}
