package com.freshdirect.fdstore.promotion.management;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.crm.CrmAgentModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.promotion.EnumPromotionStatus;
import com.freshdirect.fdstore.promotion.FDPromotionNewModelFactory;
import com.freshdirect.fdstore.promotion.PromotionFactory;
import com.freshdirect.fdstore.promotion.PromotionI;
import com.freshdirect.fdstore.promotion.management.ejb.FDPromotionManagerNewHome;
import com.freshdirect.fdstore.promotion.management.ejb.FDPromotionManagerNewSB;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.util.StringUtil;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDPromotionNewManager {

	private static Category LOGGER = LoggerFactory.getInstance(FDPromotionNewManager.class);
	
	private static FDPromotionManagerNewHome managerHome = null;
	
	public static PrimaryKey createPromotion(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			PrimaryKey pk = sb.createPromotion(promotion);
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionNewModelFactory.getInstance().forceRefresh();
			return pk;
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}			
	}
	
	public static List<FDPromotionNewModel> getPromotions() throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getPromotions();
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}
	
	public static List<FDPromotionNewModel> getModifiedOnlyPromotions(Date lastModified) throws FDResourceException{
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getModifiedOnlyPromotions(lastModified);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}
	
	public static FDPromotionNewModel loadPromotion(String promotionId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getPromotion(promotionId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}
	
	public static FDPromotionNewModel getPromotionByPk(String pk) throws FDResourceException{
		lookupManagerHome();
		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getPromotionByPk(pk);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static List<WSPromotionInfo> getWSPromotionInfos(Date fromDate, Date toDate, Date dlvDate, String zone, String status) throws FDResourceException {
		lookupManagerHome();
		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			List<WSPromotionInfo> promotions =  sb.getWSPromotionInfos(fromDate, toDate, dlvDate, zone, status);
			return promotions;
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		
	}
	
	/**
	 * @deprecated use {@link FDPromotionNewManager#storePromotion(FDPromotionNewModel, boolean)} instead.
	 * 
	 * @param promotion Promotion to store
	 * 
	 * @throws FDResourceException
	 * @throws FDDuplicatePromoFieldException
	 * @throws FDPromoTypeNotFoundException
	 * @throws FDPromoCustNotFoundException
	 */
	public static void storePromotion(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		FDPromotionNewManager.storePromotion(promotion, false);
	}


	public static void storePromotion(FDPromotionNewModel promotion, boolean saveLog) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.storePromotion(promotion, saveLog);
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionNewModelFactory.getInstance().forceRefresh();
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
	
	public static void createPromotions(List<FDPromotionNewModel> promotions) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.createPromotions(promotions);
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionNewModelFactory.getInstance().forceRefresh();
			
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}			
	}

	public static List<FDPromotionNewModel> loadPublishablePromotions() throws FDResourceException, FinderException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getPublishablePromos();
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}

	
	public static boolean isPromotionCodeUsed(String promoCode) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.isPromotionCodeUsed(promoCode);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	
	public static boolean isPromotionNameUsed(String promoName) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.isPromotionNameUsed(promoName);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	
	public static String findPromotion(String promoCode) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.findPromotion(promoCode);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	
	public static boolean publishPromotion(FDPromotionNewModel promo) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.publishPromotion(promo);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}


	public static boolean cancelPromotion(FDPromotionNewModel promo) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.cancelPromotion(promo);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	
	public static boolean fixPromoStatusAfterPublish(Collection<String> codes) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			boolean val = sb.fixPromoStatusAfterPublish(codes);
//			FDPromotionNewModelFactory.getInstance().forceRefresh();
			return val;
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}			
	}

	
	public static void logPublishEvent(CrmAgentModel agent,
			Date start, Date end, String destURL,
			Map<String, Boolean> publishResults,
			Map<String, EnumPromotionStatus> preStatuses,
			Map<String, EnumPromotionStatus> postStatuses,
			Map<String, String> changeIDs) throws FDResourceException {
		
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.logPublishEvent(agent,
					start, end, destURL,
					publishResults, preStatuses, postStatuses, changeIDs);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}			
	}

	public static List<FDPromoChangeModel> loadPromoAuditChanges(String promotionId) throws FDResourceException, FinderException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getPromoAuditChanges(promotionId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}
	
	private static void lookupManagerHome() throws FDResourceException {
		if (managerHome != null) {
			return;
		}
		Context ctx = null;
		try {
			ctx = FDStoreProperties.getInitialContext();
			managerHome = (FDPromotionManagerNewHome) ctx.lookup(FDStoreProperties.getFDPromotionManagerNewHome());
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
	
	public static PrimaryKey createPromotionBasic(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			PrimaryKey pk = sb.createPromotionBasic(promotion);
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionNewModelFactory.getInstance().forceRefresh();
			return pk;
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}			
	}
	
	public static void storePromotionBasic(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.storePromotionBasic(promotion);
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionNewModelFactory.getInstance().forceRefresh();
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
	
	public static void storePromotionOfferInfo(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.storePromotionOfferInfo(promotion);
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionNewModelFactory.getInstance().forceRefresh();
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
	public static void storePromotionCartInfo(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.storePromotionCartInfo(promotion);
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionNewModelFactory.getInstance().forceRefresh();
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

	public static List<PromotionI> getModifiedOnlyPromos(Date lastModified) throws FDResourceException {
		lookupManagerHome();
		
		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getModifiedOnlyPromos(lastModified);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
		 
	}
	
	public static void storePromotionPaymentInfo(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.storePromotionPaymentInfo(promotion);
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionNewModelFactory.getInstance().forceRefresh();
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
	public static List<PromotionI> getAllAutomaticPromotions() throws FDResourceException {
		lookupManagerHome();
		
		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getAllAutomaticPromotions();
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
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getPromotionForRT(promoId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}				
	}
	
	
	public static void storePromotionDlvZoneInfo(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.storePromotionDlvZoneInfo(promotion);
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionNewModelFactory.getInstance().forceRefresh();
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
	
	public static void storePromotionCustReqInfo(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.storePromotionCustReqInfo(promotion);
			// this forces a refresh of the promotions cache
			//FDPromotionFactory.getInstance().forceRefresh();
			FDPromotionNewModelFactory.getInstance().forceRefresh();
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
	
	public static Integer getRedemptionCount(String promoId, Date requestedDate) throws FDResourceException{
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getRedemptionCount(promoId, requestedDate);
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
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getRedemptionPromotionId(redemptionCode);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static boolean isRedemptionCodeExists(String redemptionCode)throws FDResourceException{
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.isRedemptionCodeExists(redemptionCode);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static boolean isRedemptionCodeExists(String redemptionCode, String promotionId)throws FDResourceException{
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.isRedemptionCodeExists(redemptionCode, promotionId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static boolean isRafPromoCodeExists(String rafPromoCode)throws FDResourceException{
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.isRafPromoCodeExists(rafPromoCode);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static boolean isRafPromoCodeExists(String rafPromoCode, String promotionId)throws FDResourceException{
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.isRafPromoCodeExists(rafPromoCode, promotionId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	
	
	
	public static boolean isTSAPromoCodeExists(String tsaPromoCode)throws FDResourceException{
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.isTSAPromoCodeExists(tsaPromoCode);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static boolean isTSAPromoCodeExists(String tsaPromoCode, String promotionId)throws FDResourceException{
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.isTSAPromoCodeExists(tsaPromoCode, promotionId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static String getRedemptionCode(String  tsaPromoCode) throws FDResourceException{
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getRedemptionCode(tsaPromoCode);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static void storePromotionStatus(FDPromotionNewModel promotion,EnumPromotionStatus status,boolean refresh)throws FDResourceException{
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.storePromotionStatus(promotion,status);
			if(refresh){
				FDPromotionNewModelFactory.getInstance().forceRefresh();
			}
			PromotionFactory.getInstance().forceRefresh(promotion.getPromotionCode());
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static void storePromotionHoldStatus(FDPromotionNewModel promotion)throws FDResourceException{
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.storePromotionHoldStatus(promotion);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static void storeAssignedCustomers(FDPromotionNewModel promotion, String assignedCustomerUserIds)throws FDResourceException,FDPromoCustNotFoundException{
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.storeAssignedCustomers(promotion,assignedCustomerUserIds);
			List<String> assignedCustomerUserIdsList = sb.loadAssignedCustomerUserIds(promotion.getId());
			if (assignedCustomerUserIdsList != null
					&& assignedCustomerUserIdsList.size() > 0) {
				promotion.setAssignedCustomerUserIds(StringUtil
						.encodeString(assignedCustomerUserIdsList));
			} else {
				promotion.setAssignedCustomerUserIds("");
			}
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}



	public static void storeChangeLogEntries(String promoPk, List<FDPromoChangeModel> changes) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			
			sb.storeChangeLogEntries(promoPk, changes);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static boolean lookupPromotion(String promotionCode) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.lookupPromotion(promotionCode);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}
	
	public static WSPromotionInfo getWSPromotionInfo(String zoneCode, String startTime, 
			String endTime, Date effectiveDate, String[] windowTypes) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getWSPromotionInfo(zoneCode, startTime, endTime, effectiveDate, windowTypes);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		
	}
	
	public static void setDOWLimit(int dayofweek, double limit) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			sb.setDOWLimit(dayofweek, limit);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
		
	}
	
	public static Map<Integer, Double> getDOWLimits() throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getDOWLimits();
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
	
	public static boolean isCustomerInAssignedList(String userId, String promotionId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.isCustomerInAssignedList(userId, promotionId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static List<WSAdminInfo> getActualAmountSpentByDays() throws FDResourceException {
		lookupManagerHome(); 

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getActualAmountSpentByDays();
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}
	
	public static List<WSPromotionInfo> getAllActiveWSPromotions(java.util.Date effectiveDate) throws FDResourceException {
		lookupManagerHome(); 

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getAllActiveWSPromotions(effectiveDate);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}
	
	public static List<PromotionI> getReferralPromotions(String customerId) throws FDResourceException {
		lookupManagerHome(); 

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getReferralPromotions(customerId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}		
	}
	
	public static boolean isHrPromoCode(String redemptionCode) {
		String hrPromoCode = FDStoreProperties.getHRPromoCodes();
		StringTokenizer stokens = new StringTokenizer(hrPromoCode, ",");
		while(stokens.hasMoreTokens()) {
			String redCode = stokens.nextToken();
			if(redemptionCode.equalsIgnoreCase(redCode)) {
				return true;
			}
		}
		return false;
	}
	
	public static String createPromotionBatch(FDPromotionNewModel promotion) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.createPromotionBatch(promotion);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}			
	}
	
	public static List<FDPromotionNewModel> getBatchPromotions(String batchId) throws FDResourceException {
		lookupManagerHome();

		try {
			FDPromotionManagerNewSB sb = managerHome.create();
			return sb.getBatchPromotions(batchId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}			
	}


}
