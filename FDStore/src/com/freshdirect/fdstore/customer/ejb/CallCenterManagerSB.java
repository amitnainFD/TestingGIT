/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer.ejb;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBObject;

import com.freshdirect.crm.CallLogModel;
import com.freshdirect.crm.CrmClick2CallModel;
import com.freshdirect.crm.CrmVSCampaignModel;
import com.freshdirect.customer.CustomerRatingI;
import com.freshdirect.customer.EnumPaymentResponse;
import com.freshdirect.customer.EnumSaleType;
import com.freshdirect.customer.ErpComplaintException;
import com.freshdirect.customer.ErpComplaintReason;
import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.customer.ErpRedeliveryModel;
import com.freshdirect.customer.ErpReturnOrderModel;
import com.freshdirect.customer.ErpSaleNotFoundException;
import com.freshdirect.customer.ErpTransactionException;
import com.freshdirect.customer.VSReasonCodes;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.meal.MealModel;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDAuthInfoSearchCriteria;
import com.freshdirect.fdstore.customer.FDComplaintInfo;
import com.freshdirect.fdstore.customer.FDComplaintReportCriteria;
import com.freshdirect.fdstore.customer.FDCustomerOrderInfo;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.framework.util.GenericSearchCriteria;

/**
 *
 *
 * @version $Revision$
 * @author $Author$
 */
public interface CallCenterManagerSB extends EJBObject {

	public Map<String, List<ErpComplaintReason>> getComplaintReasons(boolean excludeCartonReq) throws FDResourceException, RemoteException;
	
	public Map<String,String> getComplaintCodes() throws FDResourceException, RemoteException;

	public void rejectMakegoodComplaint(String makegood_sale_id) throws FDResourceException, RemoteException; 
	
	public List<FDComplaintInfo> getPendingComplaintOrders(String reasonCode) throws FDResourceException, RemoteException;
    
    public List getSignupPromoAVSExceptions() throws FDResourceException, RemoteException;

	public Collection getSupervisorApprovalCodes() throws FDResourceException, RemoteException;

	public Collection getFailedAuthorizationSales() throws FDResourceException, RemoteException;

	public void returnOrder(String saleId, ErpReturnOrderModel returnOrder) throws FDResourceException, ErpTransactionException, RemoteException;

	public EnumPaymentResponse resubmitPayment(String saleId, ErpPaymentMethodI payment, Collection charges) throws FDResourceException, ErpTransactionException, RemoteException;

	public void resubmitOrder(String saleId,CustomerRatingI cra,EnumSaleType saleType) throws RemoteException, FDResourceException, ErpTransactionException;
	
	public void resubmitCustomer(String customerID) throws FDResourceException, RemoteException;
	
	public List getNSMCustomers() throws FDResourceException, RemoteException;
	
	public List getNSMOrders(String date, String cutOff) throws FDResourceException, RemoteException; 
	
	public List getOrdersByStatus(String[] status) throws FDResourceException, RemoteException;
		
	public void approveReturn(String saleId, ErpReturnOrderModel returnOrder) throws FDResourceException, ErpTransactionException, RemoteException;
	
	public void scheduleRedelivery(String saleId, ErpRedeliveryModel redeliveryModel) throws FDResourceException, ErpTransactionException, RemoteException;
    
    public void changeRedeliveryToReturn(String saleId) throws FDResourceException, ErpTransactionException, ErpSaleNotFoundException, RemoteException;
    
    public List getHolidayMeals(FDIdentity identity) throws FDResourceException, RemoteException;
    
    public List locateCompanyCustomers(GenericSearchCriteria criteria) throws FDResourceException, RemoteException;
    
    public List orderSummarySearch(GenericSearchCriteria criteria) throws FDResourceException, RemoteException;
    
    public MealModel saveHolidayMeal(FDIdentity identity, String agent, MealModel meal) throws FDResourceException, RemoteException;
    
    public List getCreditSummaryForDate(Date date) throws FDResourceException, RemoteException;
  
	public void reverseCustomerCredit(String saleId, String complaintId) throws FDResourceException, ErpTransactionException, ErpComplaintException , RemoteException;
	
	public List getCutoffTimeForDate(Date date) throws FDResourceException, RemoteException;
	
	public List getCutoffTimeReport(java.util.Date day) throws FDResourceException, RemoteException;
	
	public void emailCutoffTimeReport(java.util.Date day) throws FDResourceException, RemoteException;

	public List getSubjectReport(java.util.Date day1,java.util.Date day2,boolean showAutoCases) throws FDResourceException, RemoteException;

	public List getLateDeliveryReport(java.util.Date date) throws FDResourceException, RemoteException;
	
	public List getRouteStopReport(java.util.Date date, String wave, String route, String stop1, String stop2, String call_format, String store, String facility) throws FDResourceException, RemoteException;
	
	public List getOrderStatusReport(String[] statusCodes) throws FDResourceException, RemoteException;
	
	public List getSettlementProblemReport(String[] statusCodes, String[] transactionTypes, Date failureStartDate, Date failureEndDate) throws FDResourceException, RemoteException;

	public List runComplaintReport(FDComplaintReportCriteria criteria) throws FDResourceException, RemoteException;
	
	public List runAuthInfoSearch(FDAuthInfoSearchCriteria criteria) throws FDResourceException, RemoteException; 
	
	public List getMakeGoodOrder(Date date) throws FDResourceException, RemoteException;
	
	public List doGenericSearch(GenericSearchCriteria criteria) throws FDResourceException, RemoteException;
	
	public int cancelReservations(GenericSearchCriteria resvCriteria, String initiator, String notes) throws FDResourceException, RemoteException;
	
	public int fixBrokenAccounts() throws FDResourceException, RemoteException;
	
	public Map returnOrders(FDActionInfo info, List returnOrders) throws FDResourceException, RemoteException;
	
	public int fixSettlemnentBatch(String batch_id) throws FDResourceException, RemoteException;	
	
	public void saveTopFaqs(List faqIds) throws FDResourceException, RemoteException;
	
	public void saveClick2CallInfo(CrmClick2CallModel click2CallModel) throws FDResourceException, RemoteException;
	
	public void saveClick2CallStatus(String id, String userId, boolean status) throws FDResourceException, RemoteException;
	
	public void createSnapShotForModifyOrders(GenericSearchCriteria criteria) throws FDResourceException, RemoteException;
	
	public void updateOrderModifiedStatus(String saleId, String status, String errorDesc) throws FDResourceException, RemoteException;
	
//	public CrmClick2CallModel getClick2CallInfo() throws FDResourceException, RemoteException;
	
//	public int cancelReservations(Set<String> reservationIds, String agent) throws FDResourceException, RemoteException;
	
	public List<CrmVSCampaignModel> getVSCampaignList() throws FDResourceException, RemoteException;
	
	public String saveVSCampaignInfo(CrmVSCampaignModel model) throws FDResourceException, RemoteException;
	
	public List<CrmVSCampaignModel> getVoiceShotLog(Date date) throws FDResourceException, RemoteException;
	
	public List<CrmVSCampaignModel> getVoiceShotCallDetails(String id, String lateId) throws FDResourceException, RemoteException;
	
	public List<CrmVSCampaignModel> getVSRedialList(String id, String lateId) throws FDResourceException, RemoteException;
	
	public String saveVSRedialInfo(CrmVSCampaignModel model) throws FDResourceException, RemoteException;
	
	public void addNewCampaign(CrmVSCampaignModel model) throws FDResourceException, RemoteException;
	
	public CrmVSCampaignModel getCampaignDetails(String id) throws FDResourceException, RemoteException;
	
	public void updateCampaign(CrmVSCampaignModel model) throws FDResourceException, RemoteException;
	
	public void deleteCampaign(String id) throws FDResourceException, RemoteException;
	
	public String getVSMsgForOrderPage(String orderId) throws FDResourceException, RemoteException;
	
	public List<VSReasonCodes> getVSReasonCodes() throws FDResourceException, RemoteException;
	
	public String getSoundFileMessage(String campaignId) throws FDResourceException, RemoteException;
	
	public List getAutoLateDeliveryCredits() throws FDResourceException, RemoteException;
	
	public List getAutoLateDeliveryOrders(String id) throws FDResourceException, RemoteException;
	
	public List getAutoLateDlvPassOrders(String id) throws FDResourceException, RemoteException;
	
	public ErpComplaintReason getReasonByCompCode(String cCode) throws FDResourceException, RemoteException;
	
	public void addNewIVRCallLog(CallLogModel callLogModel) throws FDResourceException, RemoteException;
	
	public List<FDCustomerOrderInfo> getReverseAuthOrders(String date) throws FDResourceException, RemoteException; 
	public List<FDCustomerOrderInfo> getOrdersForVoidCapture(String date) throws FDResourceException, RemoteException;
	public void reverseAuthOrder(String saleId) throws RemoteException, FDResourceException, ErpTransactionException;
	public void voidCaptureOrder(String saleId) throws RemoteException, FDResourceException, ErpTransactionException;
	
	
}
