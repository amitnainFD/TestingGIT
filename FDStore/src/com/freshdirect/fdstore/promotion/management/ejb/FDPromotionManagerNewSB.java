package com.freshdirect.fdstore.promotion.management.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBObject;

import com.freshdirect.crm.CrmAgentModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.promotion.EnumPromotionStatus;
import com.freshdirect.fdstore.promotion.PromotionI;
import com.freshdirect.fdstore.promotion.management.FDDuplicatePromoFieldException;
import com.freshdirect.fdstore.promotion.management.FDPromoChangeDetailModel;
import com.freshdirect.fdstore.promotion.management.FDPromoChangeModel;
import com.freshdirect.fdstore.promotion.management.FDPromoCustNotFoundException;
import com.freshdirect.fdstore.promotion.management.FDPromoTypeNotFoundException;
import com.freshdirect.fdstore.promotion.management.FDPromotionNewModel;
import com.freshdirect.fdstore.promotion.management.WSAdminInfo;
import com.freshdirect.fdstore.promotion.management.WSPromotionInfo;
import com.freshdirect.framework.core.PrimaryKey;

public interface FDPromotionManagerNewSB extends EJBObject {

	public List<FDPromotionNewModel> getPromotions() throws FDResourceException, RemoteException;	
	
	public PrimaryKey createPromotion(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException, RemoteException;
	
	public FDPromotionNewModel getPromotion(String promoId) throws FDResourceException, RemoteException;
	
	public FDPromotionNewModel getPromotionByPk(String pk) throws FDResourceException,RemoteException;
	
	public void storePromotion(FDPromotionNewModel promotion, boolean saveLog) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException, RemoteException;
	
	public void createPromotions(List<FDPromotionNewModel> promotions)throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException, RemoteException;
	
	public List<FDPromotionNewModel> getPublishablePromos() throws FDResourceException, RemoteException;
	
	public List<FDPromoChangeModel> getPromoAuditChanges(String promotionId) throws FDResourceException, RemoteException;

	
	public PrimaryKey createPromotionBasic(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException, RemoteException;
	
	public void storeChangeLogEntries(String promoPk, List<FDPromoChangeModel> changes) throws FDResourceException, RemoteException;

	
	
	public void storePromotionBasic(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException, RemoteException;


	public void deletePromotion(String promotionId) throws FDResourceException, RemoteException;
	
	public boolean isPromotionCodeUsed(String promoCode) throws FDResourceException, RemoteException;

	public boolean isPromotionNameUsed(String promoName) throws FDResourceException, RemoteException;

	public String findPromotion(String promoCode) throws FDResourceException, RemoteException;

	public boolean publishPromotion(FDPromotionNewModel promo) throws FDResourceException, RemoteException;

	public boolean cancelPromotion(FDPromotionNewModel promo) throws FDResourceException, RemoteException;

	public boolean fixPromoStatusAfterPublish(Collection<String> codes) throws FDResourceException, RemoteException;

	public void logPublishEvent(CrmAgentModel agent,
			java.util.Date start, java.util.Date end, String destURL,
			Map<String, Boolean> publishResults,
			Map<String, EnumPromotionStatus> preStatuses,
			Map<String, EnumPromotionStatus> postStatuses,
			Map<String, String> changeIDs) throws FDResourceException, RemoteException;
	
	public void storePromotionOfferInfo(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException, RemoteException;
	
	public void storePromotionCartInfo(FDPromotionNewModel promotion) throws FDResourceException, FDDuplicatePromoFieldException, FDPromoTypeNotFoundException, FDPromoCustNotFoundException,RemoteException;
	
	public void storePromotionPaymentInfo(FDPromotionNewModel promotion)throws FDResourceException, FDDuplicatePromoFieldException,	FDPromoTypeNotFoundException, FDPromoCustNotFoundException,RemoteException ;
	
	public List<PromotionI> getModifiedOnlyPromos(Date lastModified) throws FDResourceException, RemoteException;
	
	public  List<PromotionI>  getAllAutomaticPromotions() throws FDResourceException, RemoteException;
	
	public PromotionI getPromotionForRT(String  promoId) throws FDResourceException, RemoteException;
	
	public void storePromotionDlvZoneInfo(FDPromotionNewModel promotion)throws FDResourceException, FDDuplicatePromoFieldException,	FDPromoTypeNotFoundException, FDPromoCustNotFoundException,RemoteException ;
	
	public void storePromotionCustReqInfo(FDPromotionNewModel promotion)throws FDResourceException, FDDuplicatePromoFieldException,	FDPromoTypeNotFoundException, FDPromoCustNotFoundException,RemoteException ;
	
	public Integer getRedemptionCount(String promoId, Date requestedDate) throws FDResourceException, RemoteException;
	
	public String getRedemptionPromotionId(String  redemptionCode) throws RemoteException;
	
	public boolean isRedemptionCodeExists(String redemptionCode)throws FDResourceException,RemoteException;
	
	public boolean isRedemptionCodeExists(String redemptionCode, String promotionId)throws FDResourceException,RemoteException;
	
	public boolean isRafPromoCodeExists(String rafPromoCode)throws FDResourceException,RemoteException;
	
	public boolean isRafPromoCodeExists(String rafPromoCode, String promotionId)throws FDResourceException,RemoteException;
	
	public void storePromotionStatus(FDPromotionNewModel promotion,EnumPromotionStatus status)throws FDResourceException,RemoteException;
	
	public void storePromotionHoldStatus(FDPromotionNewModel promotion)throws FDResourceException,RemoteException;
	
	public void storeAssignedCustomers(FDPromotionNewModel promotion, String assignedCustomerUserIds) throws FDResourceException, FDPromoCustNotFoundException, RemoteException;
	
	public List<String> loadAssignedCustomerUserIds(String promotionId) throws RemoteException, FDResourceException;
	
	public boolean lookupPromotion(String promotionCode) throws RemoteException, FDResourceException;
	
	public List<FDPromotionNewModel> getModifiedOnlyPromotions(Date lastModified) throws FDResourceException, RemoteException;
	
	public List<WSPromotionInfo> getWSPromotionInfos(Date fromDate, Date toDate, Date dlvDate, String zone, String status) throws FDResourceException,RemoteException;
	
	public WSPromotionInfo getWSPromotionInfo(String zoneCode, String startTime, 
			String endTime, Date effectiveDate, String[] windowTypes) throws FDResourceException, RemoteException;
	
	public boolean isCustomerInAssignedList(String userId, String promotionId) throws FDResourceException, RemoteException;
	
	public void setDOWLimit(int dayofweek, double limit) throws FDResourceException, RemoteException;
	
	public Map<Integer, Double> getDOWLimits() throws FDResourceException, RemoteException;
	
	public List<WSAdminInfo> getActualAmountSpentByDays() throws FDResourceException, RemoteException;
	
	public List<WSPromotionInfo> getAllActiveWSPromotions(java.util.Date effectiveDate) throws FDResourceException, RemoteException;
	
	public List<PromotionI> getReferralPromotions(String customerId) throws FDResourceException, RemoteException;
	
	public boolean isTSAPromoCodeExists(String tsaPromoCode)throws FDResourceException,RemoteException;
	
	public boolean isTSAPromoCodeExists(String tsaPromoCode, String promotionId)throws FDResourceException,RemoteException;
	
	public String getRedemptionCode(String tsaPromoCode)throws FDResourceException,RemoteException;
	
	public String createPromotionBatch(FDPromotionNewModel promotion) throws FDResourceException,RemoteException;
	
	public List<FDPromotionNewModel> getBatchPromotions(String batchId) throws FDResourceException , RemoteException;

}

