/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.crm.CallLogModel;
import com.freshdirect.crm.CrmClick2CallModel;
import com.freshdirect.crm.CrmVSCampaignModel;
import com.freshdirect.customer.CustomerRatingI;
import com.freshdirect.customer.EnumPaymentResponse;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.customer.EnumSaleType;
import com.freshdirect.customer.ErpComplaintException;
import com.freshdirect.customer.ErpComplaintReason;
import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.customer.ErpRedeliveryModel;
import com.freshdirect.customer.ErpReturnOrderModel;
import com.freshdirect.customer.ErpSaleModel;
import com.freshdirect.customer.ErpSaleNotFoundException;
import com.freshdirect.customer.ErpTransactionException;
import com.freshdirect.customer.VSReasonCodes;
import com.freshdirect.fdstore.content.meal.MealModel;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDAuthInfoSearchCriteria;
import com.freshdirect.fdstore.customer.FDComplaintInfo;
import com.freshdirect.fdstore.customer.FDComplaintReportCriteria;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.adapter.FDOrderAdapter;
import com.freshdirect.fdstore.customer.ejb.CallCenterManagerHome;
import com.freshdirect.fdstore.customer.ejb.CallCenterManagerSB;
import com.freshdirect.framework.util.GenericSearchCriteria;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * Singleton class for accessing functionality in ERP Services.
 *
 * @version $Revision$
 * @author $Author$
 */
public class CallCenterServices {

	private static Category LOGGER = LoggerFactory.getInstance(CallCenterServices.class);

	private static CallCenterManagerHome callCenterHome = null;


	public static Map<String, List<ErpComplaintReason>> getComplaintReasons(boolean excludeCartonReq) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getComplaintReasons(excludeCartonReq);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	//get complaint codes
	public static Map<String,String> getComplaintCodes() throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getComplaintCodes();
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static List<FDComplaintInfo> getPendingComplaintOrders(String reasonCode) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getPendingComplaintOrders(reasonCode);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static List runComplaintReport(FDComplaintReportCriteria criteria) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.runComplaintReport(criteria);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}	
	}
	
	public static List locateCompanyCustomers(GenericSearchCriteria criteria) throws FDResourceException {
		if(callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.locateCompanyCustomers(criteria);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static List orderSummarySearch(GenericSearchCriteria criteria) throws FDResourceException {
		if(callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.orderSummarySearch(criteria);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static List runAuthInfoSearch(FDAuthInfoSearchCriteria criteria) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.runAuthInfoSearch(criteria);
		} catch (CreateException ce) {
			callCenterHome = null;
			LOGGER.debug("CreateException: ", ce);
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			LOGGER.debug("RemoteException: ", re);
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static List getSignupPromoAVSExceptions() throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getSignupPromoAVSExceptions();
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static List getCreditSummaryForDate(Date date) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getCreditSummaryForDate(date);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static List getSubjectReport(Date date1, Date date2, boolean showAutoCases) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getSubjectReport(date1,date2,showAutoCases);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static List getLateDeliveryReport(Date date) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getLateDeliveryReport(date);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static List getRouteStopReport(Date date, String wave, String route, String stop1, String stop2, String call_format, String store, String facility) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getRouteStopReport(date, wave, route, stop1, stop2, call_format, store, facility);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static Collection getSupervisorApprovalCodes() throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getSupervisorApprovalCodes();
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	/**
	 * @return Collection of FDOrderAdapters
	 */
	public static Collection getFailedAuthorizationSales() throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			Collection sales = sb.getFailedAuthorizationSales();

			Collection orders = new ArrayList(sales.size());
			for (Iterator it = sales.iterator(); it.hasNext();) {
				orders.add(new FDOrderAdapter((ErpSaleModel) it.next()));
			}
			return orders;
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static void resubmitOrder(String saleId,CustomerRatingI cra,EnumSaleType saleType) throws FDResourceException, ErpTransactionException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.resubmitOrder(saleId,cra,saleType);

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static void resubmitCustomer(String customerID) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.resubmitCustomer(customerID);

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}

	public static void returnOrder(FDIdentity identity, String saleId, ErpReturnOrderModel returnOrder)
		throws FDResourceException, ErpTransactionException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			if (!FDCustomerManager.orderBelongsToUser(identity, saleId)) {
				throw new FDResourceException("Order not found in current user's order history.");
			}
			CallCenterManagerSB sb = callCenterHome.create();
			sb.returnOrder(saleId, returnOrder);

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static void changeRedeliveryToReturn(FDIdentity identity, String saleId)
		throws FDResourceException, ErpTransactionException, ErpSaleNotFoundException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			if (!FDCustomerManager.orderBelongsToUser(identity, saleId)) {
				throw new FDResourceException("Order not found in current user's order history.");
			}
			CallCenterManagerSB sb = callCenterHome.create();
			sb.changeRedeliveryToReturn(saleId);

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static void approveReturn(FDIdentity identity, String saleId, ErpReturnOrderModel returnOrder)
		throws FDResourceException, ErpTransactionException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			if (!FDCustomerManager.orderBelongsToUser(identity, saleId)) {
				throw new FDResourceException("Order not found in current user's order history.");
			}
			CallCenterManagerSB sb = callCenterHome.create();
			sb.approveReturn(saleId, returnOrder);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static void scheduleRedelivery(FDIdentity identity, String saleId, ErpRedeliveryModel redeliveryModel)
		throws FDResourceException, ErpTransactionException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			if (!FDCustomerManager.orderBelongsToUser(identity, saleId)) {
				throw new FDResourceException("Order not found in current user's order history.");
			}
			CallCenterManagerSB sb = callCenterHome.create();
			sb.scheduleRedelivery(saleId, redeliveryModel);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating CallCenterManager session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to CallCenterManager session bean");
		}
	}
	
	public static List getOrdersByStatus(EnumSaleStatus status) throws FDResourceException {
		String[] s = {status.getStatusCode()};
		return getOrderByStatus(s);
	}
	
	public static List getOrderByStatus(String[] status) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getOrdersByStatus(status);

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static List getNSMCustomers() throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getNSMCustomers();

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static List getNSMOrders(String date, String cutOff) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getNSMOrders(date, cutOff);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}

	public static EnumPaymentResponse resubmitPayment(String saleId, ErpPaymentMethodI payment, Collection charges)
		throws FDResourceException, ErpTransactionException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.resubmitPayment(saleId, payment, charges);

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static List getHolidayMeals(FDIdentity identity) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getHolidayMeals(identity);

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static MealModel saveHolidayMeal(FDIdentity identity, String agent, MealModel meal) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.saveHolidayMeal(identity, agent, meal);

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static void reverseCustomerCredit(String saleId, String complaintId)
		throws FDResourceException, ErpTransactionException, ErpComplaintException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.reverseCustomerCredit(saleId, complaintId);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static List getCutoffTimeForDate(Date date) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getCutoffTimeForDate(date);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static List getCutoffTimeReport(java.util.Date day) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getCutoffTimeReport(day);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static void emailCutoffTimeReport(java.util.Date day) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.emailCutoffTimeReport(day);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	protected static void lookupManagerHome() throws FDResourceException {
		Context ctx = null;
		try {
			ctx = FDStoreProperties.getInitialContext();
			callCenterHome = (CallCenterManagerHome) ctx.lookup(FDStoreProperties.getCallCenterManagerHome());
		} catch (NamingException ne) {
			throw new FDResourceException(ne);
		} finally {
			try {
				ctx.close();
			} catch (NamingException e) {
			}
		}
	}

	/**
	 * Utility method for determining whether a given order belongs to a given user.
	 * @param FDIdentity current user
	 * @param String sale id
	 * @return boolean
	 */
/*	
    This method is obsolete. Replaced by FDCustomerManager.orderBelongsToUser(FDIdentity identity, String saleId)
    
    private static boolean orderBelongsToUser(FDIdentity identity, String saleId) throws FDResourceException {
		Collection orders = FDCustomerManager.getOrderHistoryInfo(identity).getFDOrderInfos();
		for (Iterator it = orders.iterator(); it.hasNext();) {
			FDOrderInfoI orderInfo = (FDOrderInfoI) it.next();
			if (orderInfo.getErpSalesId().equals(saleId)) {
				LOGGER.debug("verified order belongs to user");
				return true;
			}
		}
		return false;
	}*/
	
	public static List getOrderStatusReport(String[] statusCodes) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getOrderStatusReport(statusCodes);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static List getSettlementProblemReport(String[] statusCodes, String [] transactionTypes, Date failureStartDate, Date failureEndDate) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getSettlementProblemReport(statusCodes, transactionTypes, failureStartDate, failureEndDate);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static List getMakeGoodOrder(Date date) throws FDResourceException { 
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getMakeGoodOrder(date);
		} catch (CreateException ce) {
			callCenterHome = null;
			LOGGER.debug("CreateException: ", ce);
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			LOGGER.debug("RemoteException: ", re);
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static List doGenericSearch(GenericSearchCriteria criteria) throws FDResourceException {
		if(callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.doGenericSearch(criteria);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static void createSnapShotForModifyOrders(GenericSearchCriteria criteria) throws FDResourceException {
		if(callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.createSnapShotForModifyOrders(criteria);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static void updateOrderModifiedStatus(String saleId, String status, String errorDesc) throws FDResourceException {
		if(callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.updateOrderModifiedStatus(saleId, status, errorDesc);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	public static int cancelReservations(GenericSearchCriteria resvCriteria, String initiator, String notes) throws FDResourceException {
		if(callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.cancelReservations(resvCriteria, initiator, notes);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static int fixBrokenAccounts() throws FDResourceException {
		if(callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.fixBrokenAccounts();
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static Map returnOrders(FDActionInfo info, List returnOrders) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.returnOrders(info, returnOrders);
	
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}


	public static int fixSettlemnentBatch(String batch_id) throws FDResourceException {
		if(callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.fixSettlemnentBatch(batch_id);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	public static void rejectMakegoodComplaint(String makegood_sale_id) throws FDResourceException {
		if(callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.rejectMakegoodComplaint(makegood_sale_id);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	/**
	 * 
	 * @return Content keys of top faq entries
	 * @throws FDResourceException
	 * @see {@link ContentKey}
	 */
	public static List<String> getTopFaqs() throws FDResourceException {
		return FDCustomerManager.getTopFaqs();
	}
	
	public static void saveTopFaqs(List<String> faqIds) throws FDResourceException{
		
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.saveTopFaqs(faqIds);

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}

	public static void saveClick2CallInfo(CrmClick2CallModel click2CallModel) throws FDResourceException{
		
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.saveClick2CallInfo(click2CallModel);

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}

	public static CrmClick2CallModel getClick2CallInfo() throws FDResourceException {
		/*if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getClick2CallInfo();

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}*/
		return FDCustomerManager.getClick2CallInfo();
	}
	
	public static void saveClick2CallStatus(String id, String userId, boolean status) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.saveClick2CallStatus(id, userId, status);

		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static List<CrmVSCampaignModel> getVSCampaignList() throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getVSCampaignList();
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static String saveVSCampaignInfo(CrmVSCampaignModel model) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.saveVSCampaignInfo(model);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
		
	public static List<CrmVSCampaignModel> getVoiceShotLog(Date date) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getVoiceShotLog(date);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static List<CrmVSCampaignModel> getVoiceShotCallDetails(String id, String lateId) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getVoiceShotCallDetails(id, lateId);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static List<CrmVSCampaignModel> getVSRedialList(String id, String lateId) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getVSRedialList(id, lateId);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static String saveVSRedialInfo(CrmVSCampaignModel model) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.saveVSRedialInfo(model);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static void addNewCampaign(CrmVSCampaignModel model) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.addNewCampaign(model);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static CrmVSCampaignModel getCampaignDetails(String id) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getCampaignDetails(id);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static void updateCampaign(CrmVSCampaignModel model) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.updateCampaign(model);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static void deleteCampaign(String id) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.deleteCampaign(id);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static String getVSMsgForOrderPage(String orderId) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getVSMsgForOrderPage(orderId);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static List<VSReasonCodes> getVSReasonCodes() throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getVSReasonCodes();
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static String getSoundFileMessage(String campaignId) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getSoundFileMessage(campaignId);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static List getAutoLateDeliveryCredits() throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getAutoLateDeliveryCredits();
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static List getAutoLateDeliveryOrders(String id) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getAutoLateDeliveryOrders(id);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static List getAutoLateDlvPassOrders(String id) throws FDResourceException{
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getAutoLateDlvPassOrders(id);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static ErpComplaintReason getReasonByCompCode(String cCode) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getReasonByCompCode(cCode);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static void addNewIVRCallLog(CallLogModel callLogModel) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.addNewIVRCallLog(callLogModel);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static void reverseAuthOrder(String saleId) throws FDResourceException, ErpTransactionException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			 sb.reverseAuthOrder(saleId);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	public static void voidCaptureOrder(String saleId) throws FDResourceException, ErpTransactionException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			sb.voidCaptureOrder(saleId);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static List getReverseAuthOrders(String date) throws FDResourceException {
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getReverseAuthOrders(date);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
	public static List getVoidCaptureOrders(String date) throws FDResourceException {
		System.out.println("Hi");
		if (callCenterHome == null) {
			lookupManagerHome();
		}
		try {
			CallCenterManagerSB sb = callCenterHome.create();
			return sb.getOrdersForVoidCapture(date);
		} catch (CreateException ce) {
			callCenterHome = null;
			throw new FDResourceException(ce, "Error creating bean");
		} catch (RemoteException re) {
			callCenterHome = null;
			throw new FDResourceException(re, "Error talking to bean");
		}
	}
	
} // class CallCenterServices
