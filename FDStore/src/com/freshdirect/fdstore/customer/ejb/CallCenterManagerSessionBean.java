/*
 * $Workfile:CallCenterManagerSessionBean.java$
 *
 * $Date:8/27/2003 12:11:49 PM$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.mail.MessagingException;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Category;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.common.address.PhoneNumber;
import com.freshdirect.common.pricing.Discount;
import com.freshdirect.common.pricing.EnumDiscountType;
import com.freshdirect.crm.CallLogModel;
import com.freshdirect.crm.CrmCaseOrigin;
import com.freshdirect.crm.CrmClick2CallModel;
import com.freshdirect.crm.CrmClick2CallTimeModel;
import com.freshdirect.crm.CrmOrderStatusReportLine;
import com.freshdirect.crm.CrmSettlementProblemReportLine;
import com.freshdirect.crm.CrmVSCampaignModel;
import com.freshdirect.crm.VoiceShotResponseParser;
import com.freshdirect.crm.ejb.CriteriaBuilder;
import com.freshdirect.customer.CustomerRatingI;
import com.freshdirect.customer.EnumAccountActivityType;
import com.freshdirect.customer.EnumChargeType;
import com.freshdirect.customer.EnumComplaintStatus;
import com.freshdirect.customer.EnumComplaintType;
import com.freshdirect.customer.EnumPaymentResponse;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.customer.EnumSaleType;
import com.freshdirect.customer.EnumTransactionSource;
import com.freshdirect.customer.EnumTransactionType;
import com.freshdirect.customer.EnumVSStatus;
import com.freshdirect.customer.ErpAbstractOrderModel;
import com.freshdirect.customer.ErpActivityRecord;
import com.freshdirect.customer.ErpAuthorizationModel;
import com.freshdirect.customer.ErpCaptureModel;
import com.freshdirect.customer.ErpChargeLineModel;
import com.freshdirect.customer.ErpComplaintException;
import com.freshdirect.customer.ErpComplaintReason;
import com.freshdirect.customer.ErpDeliveryInfoModel;
import com.freshdirect.customer.ErpInvoiceLineI;
import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.customer.ErpRedeliveryModel;
import com.freshdirect.customer.ErpReturnLineModel;
import com.freshdirect.customer.ErpReturnOrderModel;
import com.freshdirect.customer.ErpSaleModel;
import com.freshdirect.customer.ErpSaleNotFoundException;
import com.freshdirect.customer.ErpTransactionException;
import com.freshdirect.customer.ErpVoidCaptureModel;
import com.freshdirect.customer.VSReasonCodes;
import com.freshdirect.customer.ejb.ErpComplaintManagerHome;
import com.freshdirect.customer.ejb.ErpComplaintManagerSB;
import com.freshdirect.customer.ejb.ErpCustomerManagerHome;
import com.freshdirect.customer.ejb.ErpCustomerManagerSB;
import com.freshdirect.customer.ejb.ErpLogActivityCommand;
import com.freshdirect.delivery.ejb.DlvManagerHome;
import com.freshdirect.deliverypass.DeliveryPassModel;
import com.freshdirect.deliverypass.DlvPassConstants;
import com.freshdirect.deliverypass.EnumDlvPassExtendReason;
import com.freshdirect.deliverypass.EnumDlvPassStatus;
import com.freshdirect.deliverypass.ejb.DlvPassManagerHome;
import com.freshdirect.deliverypass.ejb.DlvPassManagerSB;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.meal.MealModel;
import com.freshdirect.fdstore.content.meal.ejb.MealPersistentBean;
import com.freshdirect.fdstore.customer.CustomerCreditModel;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDAuthInfo;
import com.freshdirect.fdstore.customer.FDAuthInfoSearchCriteria;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDComplaintInfo;
import com.freshdirect.fdstore.customer.FDComplaintReportCriteria;
import com.freshdirect.fdstore.customer.FDCreditSummary;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDCustomerOrderInfo;
import com.freshdirect.fdstore.customer.FDCutoffTimeInfo;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.customer.LateDlvReportLine;
import com.freshdirect.fdstore.customer.MakeGoodOrderInfo;
import com.freshdirect.fdstore.customer.RouteStopReportLine;
import com.freshdirect.fdstore.customer.SubjectReportLine;
import com.freshdirect.fdstore.customer.adapter.FDOrderAdapter;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.core.SequenceGenerator;
import com.freshdirect.framework.core.ServiceLocator;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.GenericSearchCriteria;
import com.freshdirect.framework.util.NVL;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.mail.ErpMailSender;
import com.freshdirect.payment.EnumBankAccountType;
import com.freshdirect.payment.EnumPaymentMethodType;
import com.freshdirect.payment.GatewayAdapter;
import com.freshdirect.payment.PaylinxResourceException;
import com.freshdirect.payment.gateway.BillingInfo;
import com.freshdirect.payment.gateway.Gateway;
import com.freshdirect.payment.gateway.GatewayType;
import com.freshdirect.payment.gateway.Merchant;
import com.freshdirect.payment.gateway.Request;
import com.freshdirect.payment.gateway.Response;
import com.freshdirect.payment.gateway.TransactionType;
import com.freshdirect.payment.gateway.impl.BillingInfoFactory;
import com.freshdirect.payment.gateway.impl.GatewayFactory;
import com.freshdirect.payment.gateway.impl.PaymentMethodFactory;
import com.freshdirect.payment.gateway.impl.Paymentech;
import com.freshdirect.payment.gateway.impl.RequestFactory;

import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

/**
 *
 *
 * @version $Revision:25$
 * @author $Author:Mike Rose$
 */
public class CallCenterManagerSessionBean extends SessionBeanSupport {
	private static final long serialVersionUID = -3900228735015308987L;

	private final static Category LOGGER = LoggerFactory.getInstance(CallCenterManagerSessionBean.class);

	private final static ServiceLocator LOCATOR = new ServiceLocator();

	public Map<String, List<ErpComplaintReason>> getComplaintReasons(boolean excludeCartonReq) throws FDResourceException {
		try {
			ErpComplaintManagerSB complaintSB = this.getComplaintManagerHome().create();
			return complaintSB.getReasons(excludeCartonReq);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}

	public Map<String,String> getComplaintCodes() throws FDResourceException {
		try {
			ErpComplaintManagerSB complaintSB = this.getComplaintManagerHome().create();
			return complaintSB.getComplaintCodes();
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}
	
	public void rejectMakegoodComplaint(String makegood_sale_id) throws FDResourceException {
		try {
			ErpComplaintManagerSB complaintSB = this.getComplaintManagerHome().create();
			complaintSB.rejectMakegoodComplaint(makegood_sale_id);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}
	private static final String PEN_COMPLAINT_QUERY_1 = "select c.sale_id, c.id as complaint_id, c.amount as complaint_amount, "
		+ "c.note as complaint_note, c.complaint_type "
		+ "from cust.complaint c "
		+ "where c.status = 'PEN' ";

	private static final String PEN_COMPLAINT_FILTER_QUERY = "and exists (select * from cust.complaintline cl where cl.complaint_id=c.id and "
		+ "complaint_dept_code_id in (select id from cust.complaint_dept_code where comp_code=?))";

	private static final String PEN_COMPLAINT_QUERY_2 = "select s.id, s.status,s.type, sa.requested_date, ci.first_name, ci.last_name, ci.email, (select max(amount) from cust.salesaction where sale_id=s.id and action_type in ('CRO','MOD','INV') "
		+ "and action_date = (select max(action_date) from cust.salesaction where action_type in ('CRO','MOD','INV') and sale_id = s.id)) as order_amount, NVL(S.E_STORE,'FreshDirect') as Store, NVL(DI.PLANT_ID,'1000') as Facility "
		+ "from cust.sale s, cust.salesaction sa, cust.customerinfo ci, CUST.DELIVERYINFO di "
		+ "where sa.sale_id in ( ? ) and sa.action_type in ('CRO', 'MOD') "
		+ "and sa.action_date = (select max(action_date) from cust.salesaction where sale_id = sa.sale_id and action_type in ('CRO', 'MOD')) "
		+ "and s.id = sa.sale_id and s.customer_id = ci.customer_id and sa.id=di.salesaction_id ";

	private String substitute(String original, char marker, String replaceWith) {
		StringBuffer sb = new StringBuffer(original);
		int pos = original.indexOf(marker);
		sb.replace(pos, pos + 1, replaceWith);
		return sb.toString();
	}

	private static final Comparator<FDComplaintInfo> COMPLAINTINFO_COMPARATOR = new Comparator<FDComplaintInfo>() {
		public int compare(FDComplaintInfo info1, FDComplaintInfo info2) {
			return info1.getDeliveryDate().compareTo(info2.getDeliveryDate());
		}
	};

	public List<FDComplaintInfo> getPendingComplaintOrders(String reasonCode) throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			String complaintQuery = PEN_COMPLAINT_QUERY_1;
			if (reasonCode!=null && !"".equals(reasonCode)) {
				complaintQuery += PEN_COMPLAINT_FILTER_QUERY;
			}

			PreparedStatement ps = conn.prepareStatement(complaintQuery);
			if (reasonCode!=null && !"".equals(reasonCode)) {
				ps.setString(1,reasonCode);
			}

			ResultSet rs = ps.executeQuery();
			Map<String,FDComplaintInfo> infoMap = new HashMap<String,FDComplaintInfo>();
			StringBuffer saleIds = new StringBuffer();

			int idCount = 0; // !!! this is just to work around the limitation of 1000 in clause in SQL
			while (rs.next() && idCount <= 900) {
				String saleId = rs.getString("SALE_ID");
				if (idCount > 0) {
					saleIds.append(",");
				}
				saleIds.append("'").append(saleId).append("'");
				FDComplaintInfo info = new FDComplaintInfo(saleId);
				info.setComplaintId(rs.getString("COMPLAINT_ID"));
				info.setComplaintAmount(rs.getDouble("COMPLAINT_AMOUNT"));
				info.setComplaintType(EnumComplaintType.getEnum(NVL.apply(rs.getString("COMPLAINT_TYPE"), "FDC")).getName());
				info.setComplaintNote(rs.getString("COMPLAINT_NOTE"));

				infoMap.put(saleId, info);
				idCount++;
			}

			rs.close();
			ps.close();
			if (infoMap.isEmpty()) {
				return Collections.emptyList();
			}

			ps = conn.prepareStatement(this.substitute(PEN_COMPLAINT_QUERY_2, '?', saleIds.toString()));
			rs = ps.executeQuery();

			while (rs.next()) {
				String saleId = rs.getString("ID");
				FDComplaintInfo info = (FDComplaintInfo) infoMap.get(saleId);
				info.setSaleStatus(EnumSaleStatus.getSaleStatus(rs.getString("STATUS")));
				info.setOrderAmount(rs.getDouble("ORDER_AMOUNT"));
				info.setDeliveryDate(rs.getDate("REQUESTED_DATE"));
				info.setEmail(rs.getString("EMAIL"));
				info.setFirstName(rs.getString("FIRST_NAME"));
				info.setLastName(rs.getString("LAST_NAME"));
				info.setOrderType(rs.getString("TYPE"));
				
				EnumEStoreId eStoreId = EnumEStoreId.valueOfContentId(rs.getString("STORE"));
				if (eStoreId != null) {
					info.seteStore(eStoreId.toString());
				}
				info.setFacility(rs.getString("FACILITY"));
			}

			rs.close();
			ps.close();

			List<FDComplaintInfo> complaintInfos = new ArrayList<FDComplaintInfo>(infoMap.values());
			Collections.sort(complaintInfos, COMPLAINTINFO_COMPARATOR);
			return complaintInfos;

		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
	}

	public List locateCompanyCustomers(GenericSearchCriteria criteria) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return GenericSearchDAO.genericSearch(conn, criteria);
		} catch (SQLException e) {
			throw new FDResourceException(e, "Counld not find customers matching criteria entered.");
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after locateCustomers", e);
				}
			}
		}
	}

	public List orderSummarySearch(GenericSearchCriteria criteria) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return GenericSearchDAO.genericSearch(conn, criteria);
		} catch (SQLException e) {
			throw new FDResourceException(e, "Counld not find customers matching criteria entered.");
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after locateCustomers", e);
				}
			}
		}
	}

	private String getComplaintType (int fdcCount, int cshCount){
		if (fdcCount > 0 && cshCount > 0) {
			return "MIXED";
		} else if (cshCount > 0) {
			return "Refund";
		} else {
			return "Store Credit";
		}
	}

	private static final String COMPLAINT_REPORT = "select c.amount , c.status, c.created_by, c.approved_by, c.sale_id, ci.first_name, ci.last_name, "
		+"(select count(1) from cust.complaintline xc where xc.method ='CSH' and xc.complaint_id =c.id) COUNT_CSH, "
		+"(select count(1) from cust.complaintline yc where yc.method ='FDC' and yc.complaint_id =c.id) COUNT_FDC "
		+"from cust.complaint c, cust.sale s, cust.customerinfo ci "
		+"where c.sale_id = s.id and s.customer_id = ci.customer_id ";

	public List runComplaintReport(FDComplaintReportCriteria criteria) throws FDResourceException {
		if (criteria.isBlank()) {
			return Collections.EMPTY_LIST;
		}
		CriteriaBuilder builder = new CriteriaBuilder();

		Date d = criteria.getStartDate();
		if(d != null){
			builder.addSql("c.create_date >= ?", new Object[] { new java.sql.Date(d.getTime())});
		}
		d = criteria.getEndDate();
		if(d != null){
			builder.addSql("c.create_date < ?", new Object[] { new java.sql.Date(d.getTime())});
		}
		String value = criteria.getIssuedBy();
		if (!"".equals(value)) {
			builder.addString("c.created_by", value);
		}
		value = criteria.getApprovedBy();
		if(!"".equals(value)){
			builder.addString("c.approved_by", value);
		}

		Connection conn = null;
		try{
			conn = this.getConnection();
			PreparedStatement ps = conn.prepareStatement(COMPLAINT_REPORT + " and " + builder.getCriteria());
			Object[] par = builder.getParams();
			for (int i = 0; i < par.length; i++) {
				LOGGER.debug("Setting param[" + par[i] + "] at position[" + i + "]");
				ps.setObject(i + 1, par[i]);
			}
			ResultSet rs = ps.executeQuery();
			List l = new ArrayList();
			while(rs.next()){
				String saleId = rs.getString("SALE_ID");
				FDComplaintInfo info = new FDComplaintInfo(saleId);
				info.setComplaintAmount(rs.getDouble("AMOUNT"));
				info.setComplaintStatus(EnumComplaintStatus.getComplaintStatus(rs.getString("STATUS")));
				info.setIssuedBy(rs.getString("CREATED_BY"));
				info.setApprovedBy(rs.getString("APPROVED_BY"));
				info.setFirstName(rs.getString("FIRST_NAME"));
				info.setLastName(rs.getString("LAST_NAME"));
				info.setComplaintType(this.getComplaintType(rs.getInt("COUNT_FDC"), rs.getInt("COUNT_CSH")));

				l.add(info);
			}

			return l;

		} catch (SQLException e){
			throw new FDResourceException(e);
		} finally {
			if(conn != null){
				try{
					conn.close();
				}catch(SQLException e){
					LOGGER.debug("SQLException while closing connection: ", e);
				}
			}
		}
	}

	public static String authInfoSearchQuery =
		"select distinct sa1.sale_id, sa1.requested_date, sa.amount, s.status, s.type, sa1.action_date, ci.first_name, ci.last_name, pi.name, p.description, p.auth_code, pi.card_type, p.ccnum_last4, pi.payment_method_type, pi.aba_route_number, pi.bank_account_type "
		+"from (select sale_id, amount from cust.salesaction where "
		+"action_date >= to_date(?) - 10 "
		+"and action_date < to_date(?) + 10 "
		+"and action_type = 'CAP' and amount = ?) sa, "
		+"cust.salesaction sa1, cust.salesaction sa2, cust.paymentinfo pi, cust.sale s, cust.customerinfo ci, cust.payment p "
		+"where sa.sale_id = sa1.sale_id and sa.sale_id = sa2.sale_id and sa.sale_id = s.id and ci.customer_id = s.customer_id and sa1.action_type in ('CRO', 'MOD') and sa2.action_type = 'AUT' and  p.salesaction_id = sa2.id "
		+"and sa1.id = pi.salesaction_id and pi.account_number like ?";

	public List runAuthInfoSearch(FDAuthInfoSearchCriteria criteria) throws FDResourceException {
		if(criteria.isBlank()){
			return Collections.EMPTY_LIST;
		}

		Connection conn = null;
		try{
			conn = this.getConnection();
			String sql = authInfoSearchQuery;
			if (criteria.getCardType() != null) {
				sql += " and pi.card_type = ?";
			}
			if (criteria.getPaymentMethodType() != null) {
				sql += " and pi.payment_method_type = ?";
			}
			if (criteria.getAbaRouteNumber() != null) {
				sql += " and pi.aba_route_number = ?";
			}
			if (criteria.getBankAccountType() != null) {
				sql += " and pi.bank_account_type = ?";
			}

			PreparedStatement ps = conn.prepareStatement(sql);
			int index = 1;
			ps.setDate(index++, new java.sql.Date(criteria.getTransactionDate().getTime()));
			ps.setDate(index++, new java.sql.Date(criteria.getTransactionDate().getTime()));
			//ps.setDouble(index++, criteria.getChargedAmount());
			ps.setBigDecimal(index++, new java.math.BigDecimal(criteria.getChargedAmount()));
			ps.setString(index++, criteria.getCCKnownNum().replace('*', '%'));
			if (criteria.getCardType() != null) {
				ps.setString(index++, criteria.getCardType().getFdName());
			}
			if (criteria.getPaymentMethodType() != null) {
				ps.setString(index++, criteria.getPaymentMethodType().getName());
			}
			if (criteria.getAbaRouteNumber() != null) {
				ps.setString(index++, criteria.getAbaRouteNumber());
			}
			if (criteria.getBankAccountType() != null) {
				ps.setString(index++, criteria.getBankAccountType().getName());
			}
			ResultSet rs = ps.executeQuery();
			List l = new ArrayList();
			while(rs.next()){
				String saleId = rs.getString("SALE_ID");
				FDAuthInfo info = new FDAuthInfo(saleId);
				info.setDeliveryDate(rs.getDate("REQUESTED_DATE"));
				info.setTransactionDateTime(rs.getDate("ACTION_DATE"));
				info.setSaleStatus(EnumSaleStatus.getSaleStatus(rs.getString("STATUS")));
				info.setAuthAmount(rs.getDouble("AMOUNT"));
				info.setFirstName(rs.getString("FIRST_NAME"));
				info.setLastName(rs.getString("LAST_NAME"));
				info.setNameOnCard(rs.getString("NAME"));
				info.setAuthDescription(rs.getString("DESCRIPTION"));
				info.setAuthCode(rs.getString("AUTH_CODE"));
				info.setCardType(rs.getString("CARD_TYPE"));
				info.setCCLastFourNum(rs.getString("CCNUM_LAST4"));
				info.setPaymentMethodType(EnumPaymentMethodType.getEnum(rs.getString("PAYMENT_METHOD_TYPE")));
				info.setAbaRouteNumber(rs.getString("ABA_ROUTE_NUMBER"));
				info.setBankAccountType(EnumBankAccountType.getEnum(rs.getString("BANK_ACCOUNT_TYPE")));
				info.setOrderType(rs.getString("TYPE"));

				//TODO FDX - add these columns to query
				info.seteStore("TODO");
				info.setFacility("TODO");
				
				l.add(info);
			}

			return l;

		} catch (SQLException e){
			throw new FDResourceException(e);
		} finally {
			if(conn != null){
				try{
					conn.close();
				}catch(SQLException e){
					LOGGER.debug("SQLException while closing connection: ", e);
				}
			}
		}
	}

	public Collection getSupervisorApprovalCodes() throws FDResourceException {
		//
		// !!! reads codes from erpservices.properties MUST CHANGE EVENTUALLY
		//
		Collection codes = new ArrayList();
		String codeString = ErpServicesProperties.getCallCenterSupervisorCodes();
		StringTokenizer tokenizer = new StringTokenizer(codeString, ",");
		while (tokenizer.hasMoreTokens()) {
			codes.add(tokenizer.nextToken());
		}
		return codes;
	}

	public Collection getFailedAuthorizationSales() throws FDResourceException {
		try {
			ErpCustomerManagerSB customerManagerSB = this.getErpCustomerManagerHome().create();
			return customerManagerSB.getFailedAuthorizationSales();
		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}

	public void returnOrder(String saleId, ErpReturnOrderModel returnOrder) throws FDResourceException, ErpTransactionException {
		try {
			ErpCustomerManagerSB customerManagerSB = this.getErpCustomerManagerHome().create();
			customerManagerSB.processSaleReturn(saleId, returnOrder);
		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}

	private void returnDeliveryPass(String saleId) throws FDResourceException, CreateException, RemoteException {
		DlvPassManagerSB dlvPassManagerSB = this.getDlvPassManagerHome().create();
		//Delivery pass is returned.
		List dpasses = dlvPassManagerSB.getDlvPassesByOrderId(saleId);
		if(dpasses == null || dpasses.size() == 0){
			throw new FDResourceException("Unable to locate the delivery pass linked with this order.");
		}
		DeliveryPassModel model = (DeliveryPassModel)dpasses.get(0);
		model.setStatus(EnumDlvPassStatus.PASS_RETURNED);
		dlvPassManagerSB.cancel(model);
		//Create a activity log to track the delivery credits.
		ErpActivityRecord activityRecord = createActivity(EnumAccountActivityType.CANCEL_DLV_PASS,
															"SYSTEM",
															DlvPassConstants.CANCEL_NOTE,
															model,
															saleId,
															EnumDlvPassExtendReason.OTHER.getName());
		logActivity(activityRecord);

	}

	private void handleDeliveryPass(String saleId, ErpReturnOrderModel returnOrder) throws FDResourceException, CreateException, RemoteException {
		boolean isDlvChargeWaived = false;
		List charges = returnOrder.getCharges();
		for(Iterator i = charges.iterator(); i.hasNext(); ){
			ErpChargeLineModel charge =(ErpChargeLineModel)i.next();
			if(EnumChargeType.DELIVERY.equals(charge.getType())){
				if(charge.getDiscount() != null){
					String promoCode = charge.getDiscount().getPromotionCode();
					//Not waived due to a Delivery Pass .
					isDlvChargeWaived = !(promoCode != null && promoCode.equals(DlvPassConstants.PROMO_CODE));
					break;
				}
			}
		}
		/*
		 * Check if delivery charge has been waived by CSR due to FD's Fault or
		 * If restocking fee was not applied to the order.
		 * If either one of them is true and if delivery pass was applied then
		 * credit the delivery back to the pass.
		 */
		if(isDlvChargeWaived || !returnOrder.isRestockingApplied()){
			//Get the delivery pass id.
			String dlvPassId = returnOrder.getDeliveryPassId();
			DlvPassManagerSB dlvPassManagerSB = this.getDlvPassManagerHome().create();
			//Get the Model.
			DeliveryPassModel passModel = dlvPassManagerSB.getDeliveryPassInfo(dlvPassId);
			//Increment the pass by 1 if BSGS pass. For Unlimited it will handled on a case by case basis.
			if(!(passModel.getType().isUnlimited())){
				dlvPassManagerSB.creditDelivery(passModel, 1);
				//Create a activity log to track the delivery credits.
				ErpActivityRecord activityRecord = createActivity(EnumAccountActivityType.CREDIT_DLV_PASS,
																	"SYSTEM",
																	DlvPassConstants.RETURN_NOTE,
																	passModel,
																	saleId,
																	EnumDlvPassExtendReason.OTHER.getName());
				logActivity(activityRecord);

			}

		}
	}

	public void approveReturn(String saleId, ErpReturnOrderModel returnOrder) throws FDResourceException, ErpTransactionException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			sb.approveReturn(saleId, returnOrder);
			/*
			 * Check if the return order contains a delivery pass. if yes cancel the
			 * delivery pass.
			 */
			if(returnOrder.isContainsDeliveryPass()){
				returnDeliveryPass(saleId);
			}else if(returnOrder.isDlvPassApplied()){
				//Delivery pass was applied.
				handleDeliveryPass(saleId,returnOrder);
			}


		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public void scheduleRedelivery(String saleId, ErpRedeliveryModel redeliveryModel)
		throws FDResourceException,
		ErpTransactionException {
		try {
			ErpCustomerManagerSB customerManagerSB = this.getErpCustomerManagerHome().create();
			customerManagerSB.scheduleRedelivery(saleId, redeliveryModel);
		} catch (CreateException ce) {
			throw new FDResourceException(ce, "Error Creating CustomerManagerSessionBean");
		} catch (RemoteException re) {
			throw new FDResourceException(re, "Error talking to CustomerManagerSessionBean");
		}
	}

	public void changeRedeliveryToReturn(String saleId)
		throws FDResourceException,
		ErpTransactionException,
		ErpSaleNotFoundException {
		try {
			ErpCustomerManagerSB customerManagerSB = this.getErpCustomerManagerHome().create();
			customerManagerSB.markAsReturn(saleId, true, false);
		} catch (CreateException ce) {
			throw new FDResourceException(ce, "Error Creating CustomerManagerSessionBean");
		} catch (RemoteException re) {
			throw new FDResourceException(re, "Error talking to CustomerManagerSessionBean");
		}
	}

	private static final String orderByStatusQuery = "select s.id as sale_id, "
		+ "(select requested_date from cust.salesaction where sale_id=s.id and action_type in ('CRO','MOD') "
		+ "and action_date=(select max(action_date) from cust.salesaction where action_type in ('CRO','MOD') and sale_id=s.id)) as requested_date, "
		+ "s.status, "
		+ "(select amount from cust.salesaction where sale_id=s.id and action_type in ('CRO','MOD','INV') "
		+ "and action_date=(select max(action_date) from cust.salesaction where action_type in ('CRO','MOD','INV') and sale_id=s.id)) as amount, "
		+ "ci.last_name, ci.first_name, ci.customer_id as erp_id, fdc.id as fd_id, ci.email, ci.home_phone, ci.cell_phone, ci.business_phone "
		+ "from cust.sale s, cust.customerinfo ci, cust.fdcustomer fdc, cust.customer c "
		+ "where s.customer_id=c.id and c.id=ci.customer_id and c.id=fdc.erp_customer_id ";

	public List getOrdersByStatus(String[] status) throws FDResourceException {
		Connection conn = null;
		List retval = new ArrayList();
		try {
			conn = this.getConnection();
			CriteriaBuilder builder = new CriteriaBuilder();
			builder.addInString("s.status", status);
			PreparedStatement ps = conn.prepareStatement(orderByStatusQuery + " and " +builder.getCriteria());

			Object[] par = builder.getParams();
			for (int i = 0; i < par.length; i++) {
				ps.setObject(i + 1, par[i]);
			}
			ResultSet rs = ps.executeQuery();

			processOrderQueryResults(rs, retval);
			rs.close();
			ps.close();
			return retval;
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
	}
	/*private static final String NSM_ORDERS_QUERY =
		"select  s.id as sale_id, s.status, sa.requested_date, sa.amount, sa.action_date, ci.last_name, ci.first_name "
			+ "from cust.sale s, cust.sale_cro_mod_date scm, cust.salesaction sa, cust.customerinfo ci "
			+ "where sa.action_date <= (sysdate - 1/48) and s.id = scm.sale_id "
			+ "and scm.sale_id = sa.sale_id and scm.max_date = sa.action_date "
			+ "and s.customer_id = ci.customer_id and s.status in ('NSM', 'MOD', 'MOC', 'NEW') "
			+" AND ((sa.requested_date >= SYSDATE) OR ( s.TYPE IN ('SUB','GCD','DON') AND sa.requested_date<=(SYSDATE))) ORDER BY action_date ";*/

	private static final String NSM_ORDERS_QUERY ="select s.id as sale_id, s.status, sa.requested_date, sa.amount, sa.action_date, ci.last_name, ci.first_name "+
			" from cust.sale s, cust.salesaction sa,cust.customerinfo ci "+
			" where s.status in ('NSM', 'MOD', 'MOC', 'NEW') and s.id=sa.sale_id and SA.ACTION_TYPE IN ('CRO','MOD') and S.CROMOD_DATE=SA.ACTION_DATE "+
			" and sa.action_date <= (sysdate - 1/144) AND ((sa.requested_date >= TRUNC(SYSDATE)) OR ( s.TYPE IN ('SUB','GCD','DON') AND sa.requested_date<=(SYSDATE)))"+
			" and S.CUSTOMER_ID=CI.CUSTOMER_ID ORDER BY action_date";
	
	private static final String NSM_ORDERS_QUERY_BY_DATE ="select s.id as sale_id, s.status, sa.requested_date, sa.amount, sa.action_date, ci.last_name, ci.first_name "+ 
    "from cust.sale s, cust.salesaction sa,cust.customerinfo ci "+ 
     "where s.status in ('NSM', 'MOD', 'MOC', 'NEW') and s.id=sa.sale_id and SA.ACTION_TYPE IN ('CRO','MOD') and S.CROMOD_DATE=SA.ACTION_DATE "+ 
     "and sa.action_date <= (sysdate - 1/144) AND ((sa.requested_date =TO_DATE(?, 'YYYY-MM-DD')) "+ 
     "OR ( s.TYPE IN ('SUB','GCD','DON') AND sa.requested_date=TO_DATE(?, 'YYYY-MM-DD'))) "+
     "and S.CUSTOMER_ID=CI.CUSTOMER_ID ORDER BY action_date ";

     private static final String NSM_ORDERS_QUERY_BY_DATE_AND_CUTOFF ="select s.id as sale_id, s.status, sa.requested_date, sa.amount, sa.action_date, ci.last_name, ci.first_name "+ 
    "from cust.deliveryinfo di, cust.sale s, cust.salesaction sa,cust.customerinfo ci "+
     "where "+
    " sa.id=DI.SALESACTION_ID and to_char( DI.CUTOFFTIME,'HH12:MI AM')=? "+  
    "and  s.status in ('NSM', 'MOD', 'MOC', 'NEW') and s.id=sa.sale_id and SA.ACTION_TYPE IN ('CRO','MOD') and S.CROMOD_DATE=SA.ACTION_DATE "+ 
     "and sa.action_date <= (sysdate - 1/144) AND ((sa.requested_date =TO_DATE(?, 'YYYY-MM-DD')) "+ 
     "OR ( s.TYPE IN ('SUB','GCD','DON') AND sa.requested_date=TO_DATE(?, 'YYYY-MM-DD'))) "+
     "and S.CUSTOMER_ID=CI.CUSTOMER_ID ORDER BY action_date ";
     
	public List getNSMOrders(String date, String cutOff) throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			List lst = new ArrayList();
			PreparedStatement ps =null;
			if((null==date && null==cutOff)||("".equals(date) && "".equals(cutOff))) {
				ps= conn.prepareStatement(NSM_ORDERS_QUERY);
			} else if(!"".equals(date) && ("".equals(cutOff)||null==cutOff)) {
				ps= conn.prepareStatement(NSM_ORDERS_QUERY_BY_DATE);
				ps.setString(1,date);
				ps.setString(2,date);
			} else if(!"".equals(date) && !"".equals(cutOff)) {
				ps= conn.prepareStatement(NSM_ORDERS_QUERY_BY_DATE_AND_CUTOFF);
				if(cutOff.length()==7) {
					cutOff="0"+cutOff;
				}
				ps.setString(1,cutOff);
				ps.setString(2,date);
				ps.setString(3,date);
				
			} else {
				ps= conn.prepareStatement(NSM_ORDERS_QUERY);
			}
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				FDCustomerOrderInfo info = new FDCustomerOrderInfo();
				info.setSaleId(rs.getString("SALE_ID"));
				info.setDeliveryDate(rs.getDate("REQUESTED_DATE"));
				info.setOrderStatus(EnumSaleStatus.getSaleStatus(rs.getString("STATUS")));
				info.setAmount(rs.getDouble("AMOUNT"));
				info.setFirstName(rs.getString("FIRST_NAME"));
				info.setLastName(rs.getString("LAST_NAME"));
				info.setLastCroModDate(rs.getTimestamp("ACTION_DATE"));

				//TODO FDX - add these columns to query
				info.seteStore("TODO");
				info.setFacility("TODO");
				
				lst.add(info);
			}
			rs.close();
			ps.close();

			return lst;
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
	}

	private static final String NSM_CUST_QUERY = "select c.id, c.user_id, ci.first_name, ci.last_name "
		+ "from cust.customer c, cust.customerinfo ci "
		+ "where c.sap_id is null and c.id = ci.customer_id "
		+ "order by c.id";

	public List getNSMCustomers() throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			List lst = new ArrayList();
			PreparedStatement ps = conn.prepareStatement(NSM_CUST_QUERY);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				FDCustomerOrderInfo info = new FDCustomerOrderInfo();
				info.setIdentity(new FDIdentity(rs.getString("ID")));
				info.setFirstName(rs.getString("FIRST_NAME"));
				info.setLastName(rs.getString("LAST_NAME"));
				info.setEmail(rs.getString("USER_ID"));
				lst.add(info);
			}
			rs.close();
			ps.close();

			return lst;
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
	}

	private static void processOrderQueryResults(ResultSet rs, List orders) throws SQLException {
		while (rs.next()) {
			FDCustomerOrderInfo coi = new FDCustomerOrderInfo();
			coi.setSaleId(rs.getString("SALE_ID"));
			coi.setDeliveryDate(rs.getDate("REQUESTED_DATE"));
			coi.setOrderStatus(EnumSaleStatus.getSaleStatus(rs.getString("STATUS")));
			coi.setAmount(rs.getDouble("AMOUNT"));
			coi.setFirstName(rs.getString("FIRST_NAME"));
			coi.setLastName(rs.getString("LAST_NAME"));
			coi.setIdentity(new FDIdentity(rs.getString("ERP_ID"), rs.getString("FD_ID")));
			coi.setEmail(rs.getString("EMAIL"));
			coi.setPhone(rs.getString("HOME_PHONE"));
			coi.setAltPhone(rs.getString("CELL_PHONE"));
			if (coi.getAltPhone() == null || "".equals(coi.getAltPhone()))
				coi.setAltPhone(rs.getString("BUSINESS_PHONE"));
			
			//TODO FDX - add these columns to query
			coi.seteStore("TODO");
			coi.setFacility("TODO");
			
			orders.add(coi);
		}
	}

	private final static String signupPromoAVSQuery = "select s.id as sale_id, sa.requested_date, s.status, sa.amount, ci.last_name, ci.first_name, c.id as erp_id, fdc.id as fd_id, ci.email, ci.home_phone, ci.business_phone, ci.cell_phone "
		+ "from cust.sale s, cust.salesaction sa, cust.customer c, cust.customerinfo ci, cust.fdcustomer fdc "
		+ "where s.id=sa.sale_id and s.customer_id=c.id and c.id=ci.customer_id and fdc.erp_customer_id=c.id "
		+ "and s.type = 'REG' "
		+ "and sa.requested_date >= trunc(SYSDATE) and s.status='AVE' and sa.promotion_campaign='SIGNUP' and sa.action_type in ('CRO','MOD') "
		+ "and sa.action_date=(select max(action_date) from cust.salesaction where sale_id=s.id and action_type in ('CRO','MOD'))";

	public List getSignupPromoAVSExceptions() throws FDResourceException {
		Connection conn = null;
		List retval = new ArrayList();
		try {
			conn = this.getConnection();
			PreparedStatement ps = conn.prepareStatement(signupPromoAVSQuery);
			ResultSet rs = ps.executeQuery();
			processOrderQueryResults(rs, retval);
			rs.close();
			ps.close();
			return retval;
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException se) {
					LOGGER.debug("Error while cleaning:", se);
				}
			}
		}
	}

	private static final String creditSummaryQuery = "select ci.first_name || ' ' || ci.last_name as customer_name, s.id as order_number, sao.requested_date as delivery, "
		+ "sai.amount as invoice, cmp.amount as pending_credit, cmp.status, cmp.note, cd.name as department, cml.line_number, "
		+ "(select description from cust.orderline where salesaction_id=sao.id and substr(orderline_number,1,4)=lpad(cml.line_number+1,4,'0')) as item_desc, "
		+ "(select configuration_desc from cust.orderline where salesaction_id=sao.id and substr(orderline_number,1,4)=lpad(cml.line_number+1,4,'0')) as item_config, "
		+ "(select sku_code from cust.orderline where salesaction_id=sao.id and substr(orderline_number,1,4)=lpad(cml.line_number+1,4,'0')) as item_sku, "
		+ "cml.quantity, cc.name as reason, (select count(*) from cust.sale where customer_id=c.id and status <> 'CAN') as number_of_orders, "
		+ "(select sum(original_amount) from cust.customercredit where customer_id=c.id) as previous_credits "
		+ "from cust.sale s, cust.salesaction sao, cust.customer c, cust.customerinfo ci, cust.salesaction sai, "
		+ "cust.complaint cmp, cust.complaintline cml, cust.complaint_dept_code cdc, cust.complaint_code cc, cust.complaint_dept cd "
		+ "where s.id=sao.sale_id and s.customer_id=c.id and c.id=ci.customer_id and s.id=cmp.sale_id and s.id=sai.sale_id(+) "
		+ "and cmp.id=cml.complaint_id and cml.complaint_dept_code_id=cdc.id and cdc.comp_code=cc.code and cdc.comp_dept=cd.code "
		+ "and sao.action_type in ('CRO','MOD') and sao.action_date=(select max(action_date) from cust.salesaction where sale_id=s.id and action_type in ('CRO','MOD')) "
		+ "and 'INV'=sai.action_type(+) and cmp.create_date>=? and cmp.create_date<? "
		+ "order by delivery, order_number, department, line_number ";

	public List getCreditSummaryForDate(java.util.Date reportDate) throws FDResourceException {
		Calendar reportDateStart = Calendar.getInstance();
		reportDateStart.setTime(reportDate);
		reportDateStart.set(Calendar.HOUR_OF_DAY, 0);
		reportDateStart.set(Calendar.MINUTE, 0);
		reportDateStart.set(Calendar.SECOND, 0);
		Calendar reportDateEnd = Calendar.getInstance();
		reportDateEnd.setTime(reportDateStart.getTime());
		reportDateEnd.add(Calendar.DATE, 1);

		Connection conn = null;
		List retval = new ArrayList();
		try {
			conn = this.getConnection();
			PreparedStatement ps = conn.prepareStatement(
				creditSummaryQuery,
				ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
			ps.setDate(1, new java.sql.Date(reportDateStart.getTime().getTime()));
			ps.setDate(2, new java.sql.Date(reportDateEnd.getTime().getTime()));
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				FDCreditSummary cs = new FDCreditSummary();
				String ordNum = rs.getString("ORDER_NUMBER");
				cs.setOrderNumber(ordNum);
				cs.setCustomerName(rs.getString("CUSTOMER_NAME"));
				cs.setDeliveryDate(rs.getDate("DELIVERY"));
				cs.setInvoiceAmount(rs.getDouble("INVOICE"));
				cs.setCreditAmount(rs.getDouble("PENDING_CREDIT"));
				cs.setNote(rs.getString("NOTE"));
				cs.setNumberOfOrders(rs.getInt("NUMBER_OF_ORDERS"));
				cs.setPreviousCreditAmount(rs.getDouble("PREVIOUS_CREDITS"));
				cs.setStatus(com.freshdirect.customer.EnumComplaintStatus.getComplaintStatus(rs.getString("STATUS")).getName());
				retval.add(cs);
				do {
					FDCreditSummary.Item item = new FDCreditSummary.Item();
					item.setConfiguration(rs.getString("ITEM_CONFIG"));
					item.setDescription(rs.getString("ITEM_DESC"));
					item.setQuantity(rs.getDouble("QUANTITY"));
					item.setReason(rs.getString("REASON"));
					item.setSkuCode(rs.getString("ITEM_SKU"));
					cs.addItem(item);
				} while (rs.next() && ordNum.equals(rs.getString("ORDER_NUMBER")));
				rs.previous();
			}
			rs.close();
			ps.close();
			return retval;
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException se) {
					LOGGER.debug("Error while cleaning:", se);
				}
			}
		}
	}

	public EnumPaymentResponse resubmitPayment(String saleId, ErpPaymentMethodI payment, Collection charges)
		throws FDResourceException,
		ErpTransactionException {
		try {
			ErpCustomerManagerSB customerManagerSB = this.getErpCustomerManagerHome().create();
			return customerManagerSB.resubmitPayment(saleId, payment, charges);
		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}

	public void resubmitOrder(String saleId, CustomerRatingI cra,EnumSaleType saleType) throws FDResourceException, ErpTransactionException {
        try {
            ErpCustomerManagerSB customerManagerSB = (ErpCustomerManagerSB) this.getErpCustomerManagerHome().create();
            ErpSaleModel _order=customerManagerSB.getOrder(new PrimaryKey(saleId));
            ErpAbstractOrderModel order =_order.getCurrentOrder();
            ErpDeliveryInfoModel dlvInfo=order.getDeliveryInfo();
            //@TODO Logistics ReIntegration Task - Need to determine if SAP is using the region send as part of Create/Change Sales Order. If not this logic will be removed. 
            /*
             * 
            EnumRegionServiceType serviceType  = null;
            if(!(dlvInfo.getDeliveryReservationId() == null || "1".equals(dlvInfo.getDeliveryReservationId()))){
            DlvManagerSB sb = this.getDlvManagerHome().create();
  			DlvReservationModel _reservation=sb.getReservation(dlvInfo.getDeliveryReservationId());
  			if(_reservation!=null)
  				serviceType = _reservation.getRegionSvcType();
            }
            FDDeliveryZoneInfo zInfo = FDDeliveryManager.getInstance().getZoneInfo(dlvInfo.getDeliveryAddress(),dlvInfo.getDeliveryStartTime(), null, serviceType);
            */
            customerManagerSB.resubmitOrder(saleId, cra, saleType, dlvInfo.getDeliveryRegionId());
              
              if(!EnumSaleType.REGULAR.equals(saleType) && EnumSaleStatus.NEW.equals(_order.getStatus())) {
            	  FDCustomerManager.authorizeSale(saleId);
              }
              
        } catch (CreateException ce) {
              throw new FDResourceException(ce);
        } catch (RemoteException re) {
              throw new FDResourceException(re);
        } /*catch (FDInvalidAddressException e) {
              throw new FDResourceException(e);
        } catch (FinderException fe) {
        	throw new FDResourceException(fe);
		}*/
  }
  
	

	public void resubmitCustomer(String customerID) throws FDResourceException {
		try {
			PrimaryKey customerPk = new PrimaryKey(customerID);
			ErpCustomerManagerSB customerManagerSB = (ErpCustomerManagerSB) this.getErpCustomerManagerHome().create();
			customerManagerSB.resubmitCustomer(customerPk);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	public List getHolidayMeals(FDIdentity identity) throws FDResourceException {
		Connection conn = null;
		java.util.List lst = new java.util.LinkedList();
		try {
			conn = this.getConnection();

			PreparedStatement ps = conn.prepareStatement("SELECT ID FROM CUST.HOLIDAYMEAL WHERE CUSTOMER_ID=?");
			ps.setString(1, identity.getErpCustomerPK());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				MealPersistentBean bean = new MealPersistentBean(new PrimaryKey(rs.getString(1)), conn);
				bean.setParentPK(new PrimaryKey(identity.getErpCustomerPK()));
				lst.add(bean.getModel());
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			return lst;

		} catch (SQLException se) {
			getSessionContext().setRollbackOnly();
			throw new FDResourceException(se);
		} finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException se) {
					LOGGER.debug("Error while cleaning:", se);
				}
			}
		}
	}

	public MealModel saveHolidayMeal(FDIdentity identity, String agent, MealModel meal) throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();

			MealPersistentBean mpb = new MealPersistentBean(meal);
			mpb.setAgent(agent);
			mpb.setParentPK(new PrimaryKey(identity.getErpCustomerPK()));
			PrimaryKey mpk = null;
			if (meal.isAnonymous()) {
				mpk = mpb.create(conn);
			} else {
				mpk = meal.getPK();
				mpb.store(conn);
			}

			mpb = new MealPersistentBean(mpk, conn);
			return (MealModel) mpb.getModel();

		} catch (SQLException se) {
			getSessionContext().setRollbackOnly();
			throw new FDResourceException(se);
		} finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException se) {
					LOGGER.debug("Error while cleaning:", se);
				}
			}
		}
	}

	private static final String CUTOFFTME_QUERY = "select cutofftime from cust.deliveryInfo where starttime >= ? and endtime < ? group by cutofftime";

	public List getCutoffTimeForDate(java.util.Date date) throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			PreparedStatement ps = conn.prepareStatement(CUTOFFTME_QUERY);
			date = DateUtil.truncate(date);
			ps.setTimestamp(1, new Timestamp(date.getTime()));
			date = DateUtil.addDays(date, 1);
			ps.setTimestamp(2, new Timestamp(date.getTime()));
			ResultSet rs = ps.executeQuery();
			List ret = new ArrayList();
			while (rs.next()) {
				ret.add(rs.getTimestamp("CUTOFFTIME"));
			}
			ps.close();
			rs.close();
			return ret;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
					LOGGER.warn("Error while cleaning:", e);
				}
			}
		}
	}

	private static final String CUTOFF_REPORT_QUERY = "select s.status, di.handofftime " +
			" , count(*) as order_count from cust.sale s, cust.salesaction sa, cust.deliveryinfo di " +
			"where s.id=sa.sale_id and sa.id=di.salesaction_id and s.type<>'SUB' and sa.action_type in ('CRO','MOD') and sa.requested_date=? and s.type = 'REG' " +
			"and sa.action_date=(select max(action_date) from cust.salesaction where sale_id=s.id and action_type in ('CRO','MOD')) and di.starttime > ? " +
			"and di.starttime < ? group by s.status, di.handofftime order by di.handofftime, s.status";

	public List getCutoffTimeReport(java.util.Date day) throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			PreparedStatement ps = conn.prepareStatement(CUTOFF_REPORT_QUERY);
			day = DateUtil.truncate(day);
			ps.setDate(1, new java.sql.Date(day.getTime()));
			ps.setTimestamp(2, new Timestamp(day.getTime()));
			Calendar cal = DateUtil.toCalendar(day);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			ps.setTimestamp(3, new Timestamp(cal.getTime().getTime()));
			ResultSet rs = ps.executeQuery();
			List ret = new ArrayList();

			while (rs.next()) {
				EnumSaleStatus s = EnumSaleStatus.getSaleStatus(rs.getString("STATUS"));
				ret.add(new FDCutoffTimeInfo(s, rs.getTimestamp("handofftime"), rs.getInt("ORDER_COUNT")));
			}
			ps.close();
			rs.close();
			return ret;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
					LOGGER.warn("Error while cleaning:", e);
				}
			}
		}
	}

	public void emailCutoffTimeReport(Date deliveryDate) throws FDResourceException{
		try {
			deliveryDate = DateUtil.truncate(deliveryDate);
			SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, MMM d, yyyy");
			
			List cReport = getCutoffTimeReport(deliveryDate);
			StringBuffer buff = new StringBuffer();
			String br = "\n";

			buff.append("Handoff Report for ").append(dateFormatter.format(deliveryDate)).append(br);
			buff.append(br);
			buff.append("Handoff Time").append("\t\t\t").append("Order Count").append("\t\t").append("Sale Status").append(br);
			buff.append("----------------------------------------------------------------------------------").append(br);

			for(Iterator i = cReport.iterator(); i.hasNext();){
				FDCutoffTimeInfo info = (FDCutoffTimeInfo) i.next();
					buff.append(new SimpleDateFormat().format(info.getCutoffTime())).append("\t\t").append(info.getOrderCount()).append("\t\t\t").append(info.getStatus()).append(br);
	
			}

			buff.append("----------------------------------------------------------------------------------");

			ErpMailSender mailer = new ErpMailSender();
			mailer.sendMail(ErpServicesProperties.getSapMailFrom(),
							ErpServicesProperties.getSapMailTo(),
							ErpServicesProperties.getSapMailCC(),
							"Handoff Report For " + dateFormatter.format(deliveryDate), buff.toString());

		} catch (MessagingException e) {
			LOGGER.warn("Error Sending cutoff time report: ", e);
		}
	}

	public List getSubjectReport(Date day1, Date day2, boolean showAutoCases) throws FDResourceException {
		String sql = "select cq.name as queue, cs.name as subject, count(*) as caseCount"
			+ " from cust.case c, cust.case_queue cq, cust.case_subject cs, cust.caseaction ca "
			+ " where c.case_subject=cs.code"
			+ " and cs.case_queue=cq.code"
			+ " and c.id=ca.case_id"
			+ " and ca.timestamp >= ? and ca.timestamp <=? "
			+ " and ca.timestamp=(select max(timestamp) from cust.caseaction where case_id=c.id and timestamp >= ? and timestamp <=? )"
			+ " and (cq.obsolete is null or cq.obsolete <> 'X') and (cs.obsolete is null or cs.obsolete <> 'X')"
			+ (!showAutoCases ? " AND c.CASE_ORIGIN NOT IN ('" + CrmCaseOrigin.CODE_SYS + "')" : "")
			+ " group by  cq.name, cs.name"
			+ " order by  cq.name, cs.name";

		Connection conn = null;
		try {
			conn = this.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setTimestamp(1, new Timestamp(day1.getTime()));
			ps.setTimestamp(3, new Timestamp(day1.getTime()));
			ps.setTimestamp(2, new Timestamp(day2.getTime()));
			ps.setTimestamp(4, new Timestamp(day2.getTime()));

			ResultSet rs = ps.executeQuery();
			List rpt = new ArrayList();

			while (rs.next()) {
				rpt.add(new SubjectReportLine(rs.getString("QUEUE"), rs.getString("Subject"), rs.getInt("CaseCount")));
			}
			ps.close();
			rs.close();
			return rpt;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
					LOGGER.warn("Error while cleaning:", e);
				}
			}
		}
	}

	private static final String LATE_DLVRY_REPORT_QRY = "select * from ( "
		+ " select create_date,s.wave_number,s.truck_number, s.stop_sequence, s.id as order_number, di.first_name, di.last_name,di.starttime, di.endtime, 'case' as source,"
		+ "(select 'X' from cust.fdcustomer fdc, cust.profile p where s.customer_id=fdc.erp_customer_id and fdc.id=p.customer_id(+) and p.profile_name='ChefsTable') as chefs_table,"
		+ " (select decode(count(*),2,'X',3,'X',4,'X',NULL) from cust.sale where customer_id=s.customer_id and status<>'CAN') as undeclared"
		+ " from cust.case c, cust.sale s, cust.salesaction sa, cust.deliveryinfo di"
		+ " where c.sale_id=s.id and s.id=sa.sale_id and sa.id=di.salesaction_id"
		+ " and s.type = 'REG' "
		+ " and sa.requested_date= ? and sa.action_type in ('CRO','MOD') and s.status <> 'CAN'"
		+ " and sa.action_date=(select max(action_date) from cust.salesaction where sale_id=s.id and action_type in ('CRO','MOD'))"
		+ " and c.case_subject in ('LDQ-005','LDQ-006','LDQ-007')"
		+ " union all"
		+ " select create_date,s.wave_number,s.truck_number, s.stop_sequence, s.id as order_number, di.first_name, di.last_name,di.starttime, di.endtime,'complaint' as source,"
		+ " (select 'X' from cust.fdcustomer fdc, cust.profile p where s.customer_id=fdc.erp_customer_id and fdc.id=p.customer_id(+) and p.profile_name='ChefsTable') as chefs_table,"
		+ " (select decode(count(*),2,'X',3,'X',4,'X',NULL) from cust.sale where customer_id=s.customer_id and status<>'CAN') as undeclared"
		+ " from cust.complaint_dept_code cdc, cust.complaintline cl, cust.complaint c, cust.sale s, cust.salesaction sa, cust.deliveryinfo di"
		+ " where cdc.id=cl.complaint_dept_code_id and cl.complaint_id=c.id and c.sale_id=s.id and s.id=sa.sale_id and sa.id=di.salesaction_id"
		+ " and s.type = 'REG' "
		+ " and sa.requested_date=? and sa.action_type in ('CRO','MOD') and s.status <> 'CAN'"
		+ " and sa.action_date=(select max(action_date) from cust.salesaction where sale_id=s.id and action_type in ('CRO','MOD'))"
		+ " and cdc.comp_code='LATEDEL' and cdc.comp_dept='TRN'"
		+ ") order by wave_number, truck_number, stop_sequence";

	public List getLateDeliveryReport(Date day1) throws FDResourceException {
		Connection conn = null;
		try {
			conn = this.getConnection();
			PreparedStatement ps = conn.prepareStatement(LATE_DLVRY_REPORT_QRY);
			Timestamp truncDate = new Timestamp(DateUtil.truncate(day1).getTime());
			ps.setTimestamp(1, truncDate);
			ps.setTimestamp(2, truncDate);
			ResultSet rs = ps.executeQuery();
			List rpt = new ArrayList();

			while (rs.next()) {
				LateDlvReportLine rl = new LateDlvReportLine();
				rs.getString("chefs_table");
				rl.setChefsTable(!rs.wasNull());
				rs.getString("undeclared");
				rl.setUndeclared(!rs.wasNull());
				rl.setFirstName(rs.getString("first_name"));
				rl.setLastName(rs.getString("last_name"));
				rl.setOrderNumber(rs.getString("order_number"));
				rl.setSource(rs.getString("source"));
				rl.setStopSequence(rs.getString("stop_Sequence"));
				rl.setTruckNumber(rs.getString("truck_number"));
				rl.setWaveNumber(rs.getString("wave_number"));
				rl.setTimeCaseOpened(rs.getTimestamp("create_date"));
				rl.setStartTime(rs.getTimestamp("starttime"));
				rl.setEndTime(rs.getTimestamp("endtime"));
				rpt.add(rl);
			}
			ps.close();
			rs.close();
			return rpt;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
					LOGGER.warn("Error while cleaning:", e);
				}
			}
		}
	}

	private static final String ROUTE_STOP_QRY = "select * from ( "
		+ " select s.customer_id, s.wave_number, s.truck_number, s.stop_sequence, s.id as order_number, di.first_name, di.last_name, di.phone, di.phone_ext,"
		+ " ci.email, decode(ci.email_plain_text, 'X', 'TEXT', 'HTML') as email_format_type"
		+ " from cust.sale s, cust.salesaction sa, cust.deliveryinfo di, cust.customerinfo ci"
		+ " where s.id=sa.sale_id and s.type ='REG' and sa.id=di.salesaction_id and s.customer_id=ci.customer_id and sa.requested_date=?";
	
	private static final String ROUTE_STOP_QRY_SMS = "select * from ( "
		+ " select s.customer_id, s.wave_number, s.truck_number, s.stop_sequence, s.id as order_number, di.first_name, di.last_name, ci.mobile_number as phone, di.phone_ext,"
		+ " ci.email, decode(ci.email_plain_text, 'X', 'TEXT', 'HTML') as email_format_type"
		+ " from cust.sale s, cust.salesaction sa, cust.deliveryinfo di, cust.customerinfo ci, cust.fdcustomer_estore ce"
		+ " where s.id=sa.sale_id and s.type ='REG' and sa.id=di.salesaction_id and s.customer_id=ci.customer_id and ce.fdcustomer_id=ci.customer_id and sa.requested_date=?";
	
	private static final String SMS_NOTIFICATION = " and ce.delivery_notification = 'Y' ";
	
	private String finalRouteStopQuery;

	private static final String ROUTE_STOP_QRY_WHERE_WAVE = " and s.wave_number=LPAD(?, 6, '0')";

	private static final String ROUTE_STOP_QRY_WHERE_ROUTE = " and s.truck_number=LPAD(?, 6, '0')";

	private static final String ROUTE_STOP_QRY_WHERE_STOP = " and (s.stop_sequence between LPAD(?, 5, '0') and LPAD(?, 5, '0'))";

	private static final String ROUTE_STOP_QRY_WHERE_FD = " and (s.e_store = 'FreshDirect')";
	private static final String ROUTE_STOP_QRY_WHERE_FDX = " and (s.e_store = 'FDX')";
	

	private static final String ROUTE_STOP_QRY_END = " and sa.action_type in ('CRO','MOD') and s.status <> 'CAN'"
		+ " and s.CROMOD_DATE = sa.action_date "
		+ ") order by wave_number, truck_number, stop_sequence";

	public List getRouteStopReport(Date date, String wave, String route, String stop1, String stop2, String call_format, String store, String facility) throws FDResourceException {

		Connection conn = null;
		try {
			conn = this.getConnection();		
			
			if("SMS".equals(call_format)) {
				finalRouteStopQuery = ROUTE_STOP_QRY_SMS;
				finalRouteStopQuery += SMS_NOTIFICATION;
			} else {
				finalRouteStopQuery = ROUTE_STOP_QRY;
			}

			System.out.println("wave: " + wave +  " route: " + route + " stop: " + stop1 + " to " + stop2);

			if (wave != null && !"".equals(wave)) {
				finalRouteStopQuery += ROUTE_STOP_QRY_WHERE_WAVE;
			}

			if (route != null && !"".equals(route)) {
				finalRouteStopQuery += ROUTE_STOP_QRY_WHERE_ROUTE;
			}

			if ((stop1 != null && !"".equals(stop1)) || (stop2 != null && !"".equals(stop2))) {
				finalRouteStopQuery += ROUTE_STOP_QRY_WHERE_STOP;
			}
			
			if(store!=null && EnumEStoreId.FD.name().equalsIgnoreCase(store)){
				finalRouteStopQuery += ROUTE_STOP_QRY_WHERE_FD;
			}else if(store!=null && EnumEStoreId.FDX.name().equalsIgnoreCase(store)){
				finalRouteStopQuery += ROUTE_STOP_QRY_WHERE_FDX;
			}

			finalRouteStopQuery += ROUTE_STOP_QRY_END;
			
			PreparedStatement ps = conn.prepareStatement(finalRouteStopQuery);
			date = DateUtil.truncate(date);
			//Timestamp truncDate = new Timestamp(DateUtil.truncate(date).getTime());
			int index = 1;
			//ps.setTimestamp(index++, truncDate);
			ps.setDate(index++, new java.sql.Date(date.getTime()));
			
			if (wave != null && !"".equals(wave)) {
				ps.setString(index++, wave);
			}

			if (route != null && !"".equals(route)) {
				ps.setString(index++, route);
			}

			if (stop1 != null && !"".equals(stop1)) {
				ps.setString(index++, stop1);
				if (stop2 == null || "".equals(stop2)) {
					ps.setString(index++, stop1);
				}
			}

			if (stop2 != null && !"".equals(stop2)) {
				if (stop1 == null || "".equals(stop1)) {
					ps.setString(index++, stop2);
				}
				ps.setString(index++, stop2);
			}

			ResultSet rs = ps.executeQuery();
			List rpt = new ArrayList();

			while (rs.next()) {
				RouteStopReportLine rl = new RouteStopReportLine();
				rl.setOrderNumber(rs.getString("order_number"));
				rl.setFirstName(rs.getString("first_name"));
				rl.setLastName(rs.getString("last_name"));
				rl.setDlvPhone(rs.getString("phone"));
				rl.setDlvPhoneExt(rs.getString("phone_ext"));
				rl.setPhoneNumber(rs.getString("phone"), rs.getString("phone_ext"));
				rl.setWaveNumber(rs.getString("wave_number"));
				rl.setTruckNumber(rs.getString("truck_number"));
				rl.setStopSequence(rs.getString("stop_Sequence"));
				rl.setEmail(rs.getString("email"));
				rl.setEmailFormatType(rs.getString("email_format_type"));
				rl.setCustomerId(rs.getString("customer_id"));
				rpt.add(rl);
			}
			ps.close();
			rs.close();
			return rpt;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
					LOGGER.warn("Error while cleaning:", e);
				}
			}
		}
	}

	public List getOrderStatusReport(String[] statusCodes) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {

			String sql = "SELECT id, status, sap_number FROM cust.sale s WHERE ";
			CriteriaBuilder builder = new CriteriaBuilder();
			builder.addInString("s.status", statusCodes);

			conn = this.getConnection();
			ps = conn.prepareStatement(sql + builder.getCriteria());

			Object[] par = builder.getParams();
			for (int i = 0; i < par.length; i++) {
				ps.setObject(i + 1, par[i]);
			}
			rs = ps.executeQuery();
			
			//TODO FDX - add these columns to query
			String eStore = "TODO";
			String facility = "TODO";

			List lst = new ArrayList();
			while (rs.next()) {
				CrmOrderStatusReportLine rl = new CrmOrderStatusReportLine(rs.getString("ID"), EnumSaleStatus.getSaleStatus(rs.getString("STATUS")), rs.getString("SAP_NUMBER"), eStore, facility);
				lst.add(rl);
			}

			return lst;

		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				LOGGER.warn("error while cleanup", e);
			}

		}

	}

	private static final String SETTLEMENT_PROBLEM_QUERY =
		"SELECT s1.id, s1.status, sa1.amount, sa1.action_type, sa1.action_date AS failure_date,"
		+ " ci.first_name || ' ' || ci.last_name AS customer_name, p.payment_method_type,"
			+ " ("
			+ "	SELECT di.starttime "
				+ " FROM cust.DELIVERYINFO di, cust.SALESACTION sa2, cust.SALE s2"
				+ " WHERE di.SALESACTION_ID = sa2.ID"
				+ " AND sa2.sale_id = s2.id"
				+ " AND s2.id = s1.id"
				+ " AND sa2.ACTION_TYPE IN ('CRO', 'MOD')"
				+ " AND sa2.action_date = ("
									+ " SELECT MAX(sa3.action_date)"
									+ " FROM cust.SALESACTION sa3"
									+ " WHERE sa3.sale_id=s1.id AND sa3.action_type IN ('CRO','MOD')"
									+ " )"
			+ " ) AS delivery_date"
		+ " FROM  CUST.SALE s1, CUST.SALESACTION sa1, cust.CUSTOMERINFO ci, cust.PAYMENT p"
		+ " WHERE sa1.sale_id=s1.id"
		+ " AND s1.customer_id = ci.customer_id"
		+ " AND sa1.id = p.salesaction_id"
		+ "	AND  sa1.action_type = ?"
		+ " AND sa1.action_date = (SELECT MAX(sa4.action_date) FROM CUST.SALESACTION sa4 WHERE sa4.sale_id=s1.id AND sa4.action_type = ?)"
		+ " AND  s1.status = ?";


	private static final String SETTLEMENT_PROBLEM_STF_QUERY =
		"SELECT s1.id, s1.status, sa1.amount, sa1.action_type, sa1.action_date AS failure_date,"
		+ " ci.first_name || ' ' || ci.last_name AS customer_name, p.payment_method_type,"
			+ " ("
			+ "	SELECT di.starttime "
				+ " FROM cust.DELIVERYINFO di, cust.SALESACTION sa2, cust.SALE s2"
				+ " WHERE di.SALESACTION_ID = sa2.ID"
				+ " AND sa2.sale_id = s2.id"
				+ " AND s2.id = s1.id"
				+ " AND sa2.ACTION_TYPE IN ('CRO', 'MOD')"
				+ " AND sa2.action_date = ("
									+ " SELECT MAX(sa3.action_date)"
									+ " FROM cust.SALESACTION sa3"
									+ " WHERE sa3.sale_id=s1.id AND sa3.action_type IN ('CRO','MOD')"
									+ " )"
			+ " ) AS delivery_date"
		+ " FROM  CUST.SALE s1, CUST.SALESACTION sa1, cust.CUSTOMERINFO ci, cust.PAYMENT p"
		+ " WHERE sa1.sale_id=s1.id"
		+ " AND s1.customer_id = ci.customer_id"
		+ " AND sa1.id = p.salesaction_id"
		+ "	AND  sa1.action_type = 'STF'"
		+ " AND sa1.action_date = (SELECT MAX(sa4.action_date) FROM CUST.SALESACTION sa4 WHERE sa4.sale_id=s1.id AND sa4.action_type = 'STF')"
		+ " AND  s1.status = 'STF'";
	private static final String SETTLEMENT_PROBLEM_FRD_QUERY =
		"SELECT s1.id, s1.status, sa1.amount, sa1.action_type, sa1.action_date AS failure_date,"
		+ " ci.first_name || ' ' || ci.last_name AS customer_name, p.payment_method_type,"
			+ " ("
			+ "	SELECT di.starttime "
				+ " FROM cust.DELIVERYINFO di, cust.SALESACTION sa2, cust.SALE s2"
				+ " WHERE di.SALESACTION_ID = sa2.ID"
				+ " AND sa2.sale_id = s2.id"
				+ " AND s2.id = s1.id"
				+ " AND sa2.ACTION_TYPE IN ('CRO', 'MOD')"
				+ " AND sa2.action_date = ("
									+ " SELECT MAX(sa3.action_date)"
									+ " FROM cust.SALESACTION sa3"
									+ " WHERE sa3.sale_id=s1.id AND sa3.action_type IN ('CRO','MOD')"
									+ " )"
			+ " ) AS delivery_date"
		+ " FROM  CUST.SALE s1, CUST.SALESACTION sa1, cust.CUSTOMERINFO ci, cust.PAYMENT p"
		+ " WHERE sa1.sale_id=s1.id"
		+ " AND s1.customer_id = ci.customer_id"
		+ " AND sa1.id = p.salesaction_id"
		+ "	AND  sa1.action_type = 'FRD'"
		+ " AND sa1.action_date = (SELECT MAX(sa4.action_date) FROM CUST.SALESACTION sa4 WHERE sa4.sale_id=s1.id AND sa4.action_type = 'FRD')"
		+ " AND  s1.status = 'STL'";
	
	private static final String SETTLEMENT_PROBLEM_CBK_QUERY =
		"SELECT s1.id, s1.status, sa1.amount, sa1.action_type, sa1.action_date AS failure_date,"
		+ " ci.first_name || ' ' || ci.last_name AS customer_name, p.payment_method_type,"
			+ " ("
			+ "	SELECT di.starttime "
				+ " FROM cust.DELIVERYINFO di, cust.SALESACTION sa2, cust.SALE s2"
				+ " WHERE di.SALESACTION_ID = sa2.ID"
				+ " AND sa2.sale_id = s2.id"
				+ " AND s2.id = s1.id"
				+ " AND sa2.ACTION_TYPE IN ('CRO', 'MOD')"
				+ " AND sa2.action_date = ("
									+ " SELECT MAX(sa3.action_date)"
									+ " FROM cust.SALESACTION sa3"
									+ " WHERE sa3.sale_id=s1.id AND sa3.action_type IN ('CRO','MOD')"
									+ " )"
			+ " ) AS delivery_date"
		+ " FROM  CUST.SALE s1, CUST.SALESACTION sa1, cust.CUSTOMERINFO ci, cust.PAYMENT p"
		+ " WHERE sa1.sale_id=s1.id"
		+ " AND s1.customer_id = ci.customer_id"
		+ " AND sa1.id = p.salesaction_id"
		+ "	AND  sa1.action_type = 'CBK'"
		+ " AND sa1.action_date = (SELECT MAX(sa4.action_date) FROM CUST.SALESACTION sa4 WHERE sa4.sale_id=s1.id AND sa4.action_type = 'CBK')"
		+ " AND  s1.status = 'CBK'";
	public List getSettlementProblemReport(String[] statusCodes, String[] transactionTypes, Date failureStartDate, Date failureEndDate) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String saleStatus="";
		String transactionType=""; 
		try {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < statusCodes.length && i < transactionTypes.length; i++) {
				if (i != 0) {
					sb.append(" UNION ");
				}
				saleStatus=statusCodes[i];
				transactionType=transactionTypes[i];
				if( EnumSaleStatus.SETTLEMENT_FAILED.getStatusCode().equals(saleStatus) &&
					EnumTransactionType.SETTLEMENT_FAILED.getCode().equals(transactionType)	
						
				   ) {
					sb.append(SETTLEMENT_PROBLEM_STF_QUERY);
				} else if( EnumSaleStatus.CHARGEBACK.getStatusCode().equals(saleStatus)&&
						   EnumTransactionType.CHARGEBACK.getCode().equals(transactionType)
				           ) {
					sb.append(SETTLEMENT_PROBLEM_CBK_QUERY);
				}else if( EnumSaleStatus.SETTLED.getStatusCode().equals(saleStatus)&&
						  EnumTransactionType.FUNDS_REDEPOSIT.getCode().equals(transactionType)
				         ) {
					sb.append(SETTLEMENT_PROBLEM_FRD_QUERY);
				}
				if (failureStartDate != null) {
					sb.append(" AND sa1.action_date >= ?");
				}
				if (failureEndDate != null) {
					sb.append(" AND sa1.action_date <= ?");
				}
			}
			sb.append(" ORDER BY failure_date");
			
			System.err.println("SALE_STATUS_OR_LAST_ACTION_TYPE_QUERY sql = " + sb.toString());
			LOGGER.debug("The Settlement report query to be executed is "+sb.toString());

			conn = this.getConnection();
			ps = conn.prepareStatement(sb.toString());

			int index = 1;
			for (int i = 0; i < statusCodes.length && i < transactionTypes.length; i++) {
				LOGGER.debug("Setting transactionType:"+transactionTypes[i]+" and status Code:"+statusCodes[i]);
				/*ps.setString(index++, transactionTypes[i]);
				ps.setString(index++, transactionTypes[i]);
				ps.setString(index++, statusCodes[i]);
				*/
				if (failureStartDate != null) {
					ps.setDate(index++, new java.sql.Date(DateUtil.truncate(failureStartDate).getTime()));
				}
				if (failureEndDate != null) {
					ps.setDate(index++, new java.sql.Date(DateUtil.truncate(failureEndDate).getTime()));
				}
			}

			rs = ps.executeQuery();

			List<CrmSettlementProblemReportLine> lst = new ArrayList<CrmSettlementProblemReportLine>();
			while (rs.next()) {
				CrmSettlementProblemReportLine rl = new CrmSettlementProblemReportLine(
																			rs.getString("ID"),
																			rs.getString("CUSTOMER_NAME"),
																			rs.getDouble("AMOUNT"),
																			rs.getDate("DELIVERY_DATE"),
																			rs.getDate("FAILURE_DATE"),
																			EnumSaleStatus.getSaleStatus(rs.getString("STATUS")),
																			EnumTransactionType.getTransactionType(rs.getString("ACTION_TYPE")),
																			EnumPaymentMethodType.getEnum(rs.getString("PAYMENT_METHOD_TYPE"))
																			);
				lst.add(rl);
			}

			return lst;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				LOGGER.warn("error while cleanup", e);
			}
		}
	}

	public List getMakeGoodOrder(Date date) throws FDResourceException {
		if(date == null){
			return Collections.EMPTY_LIST;
		}

		Connection conn = null;
		try{
			conn = this.getConnection();
			PreparedStatement ps = conn.prepareStatement(
				"select sa.sale_id, sa.requested_date, sa.action_date, s.status, sa.amount, ci.first_name, ci.last_name, s.truck_number as route, s.stop_sequence as stop from cust.salesaction sa, cust.sale s, cust.customerinfo ci, cust.paymentinfo pi  "
				+ "where requested_date=trunc(to_date(?)) "
				+ "and action_type in ('CRO','MOD') "
				+ "and action_date=(select max(action_date) from cust.salesaction where sale_id=sa.sale_id and action_type in ('CRO','MOD')) "
				+ "and sa.sale_id = s.id "
				+ "and s.type = 'REG' "
				+ "and s.customer_id=ci.customer_id "
				+ "and pi.salesaction_id=sa.id "
				+ "and pi.on_fd_account='M'"
			);
			ps.setDate(1, new java.sql.Date(date.getTime()));
			ResultSet rs = ps.executeQuery();
			List l = new ArrayList();
			while(rs.next()){
				String saleId = rs.getString("SALE_ID");
				MakeGoodOrderInfo info = new MakeGoodOrderInfo(saleId);
				info.setDeliveryDate(rs.getDate("REQUESTED_DATE"));
				info.setOrderPlacedDate(rs.getDate("ACTION_DATE"));
				info.setSaleStatus(EnumSaleStatus.getSaleStatus(rs.getString("STATUS")));
				info.setFirstName(rs.getString("FIRST_NAME"));
				info.setLastName(rs.getString("LAST_NAME"));
				info.setAmount(rs.getDouble("AMOUNT"));
				info.setRoute(rs.getString("ROUTE"));
				info.setStop(rs.getString("STOP"));
				l.add(info);
			}

			return l;

		} catch (SQLException e){
			throw new FDResourceException(e);
		} finally {
			if(conn != null){
				try{
					conn.close();
				}catch(SQLException e){
					LOGGER.debug("SQLException while closing connection: ", e);
				}
			}
		}
	}

	public void reverseCustomerCredit(String saleId, String complaintId)
		throws FDResourceException,
		ErpTransactionException,
		ErpComplaintException {
		try {
			ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
			sb.reverseCustomerCredit(saleId, complaintId);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		}
	}

	private ErpComplaintManagerHome getComplaintManagerHome() {
		try {
			return (ErpComplaintManagerHome) LOCATOR.getRemoteHome(
				"java:comp/env/ejb/ComplaintManager");
		} catch (NamingException e) {
			throw new EJBException(e);
		}
	}

	private ErpCustomerManagerHome getErpCustomerManagerHome() {
		try {
			return (ErpCustomerManagerHome) LOCATOR.getRemoteHome("freshdirect.erp.CustomerManager");
		} catch (NamingException e) {
			throw new EJBException(e);
		}
	}

	/**
	 * Template method that returns the cache key to use for caching resources.
	 *
	 * @return the bean's home interface name
	 */
	protected String getResourceCacheKey() {
		return "com.freshdirect.fdstore.customer.ejb.CallCenterManagerHome";
	}

    private DlvPassManagerHome getDlvPassManagerHome() {
        try {
            return (DlvPassManagerHome) LOCATOR.getRemoteHome("java:comp/env/ejb/DlvPassManager");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    public DlvManagerHome getDlvManagerHome() {
		try {
			return (DlvManagerHome) LOCATOR.getRemoteHome(FDStoreProperties.getDeliveryManagerHome());
		} catch (NamingException ne) {
			throw new EJBException(ne);
		}
	}

	private ErpActivityRecord createActivity(EnumAccountActivityType type,
			String initiator,
			String note,
			DeliveryPassModel model,
			String saleId,
			String reasonCode) {
			ErpActivityRecord rec = new ErpActivityRecord();
			rec.setActivityType(type);

			rec.setSource(EnumTransactionSource.SYSTEM);
			rec.setInitiator(initiator);
			rec.setCustomerId(model.getCustomerId());

			StringBuffer sb = new StringBuffer();
			if (note != null) {
			sb.append(note);
			}
			rec.setNote(sb.toString());
			rec.setDeliveryPassId(model.getPK().getId());
			rec.setChangeOrderId(saleId);
			rec.setReason(reasonCode);
			return rec;
	}

	private void logActivity(ErpActivityRecord record) {
		new ErpLogActivityCommand(LOCATOR, record).execute();
	}

	public List doGenericSearch(GenericSearchCriteria criteria) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return GenericSearchDAO.genericSearch(conn, criteria);
		} catch (SQLException e) {
			throw new FDResourceException(e, "Could not find reservations matching criteria entered.");
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after searchCustomerReservations", e);
				}
			}
		}
	}

	private static final String DELETE_FROM_MODIFY_ORDERS = "DELETE FROM CUST.MODIFY_ORDERS";
	
	private static final String INSERT_INTO_MODIFY_ORDERS = "INSERT INTO CUST.MODIFY_ORDERS (SALE_ID, ERP_CUSTOMER_ID, " +
			"FIRST_NAME, LAST_NAME, EMAIL, HOME_PHONE, ALT_PHONE, " +
			"REQUESTED_DATE, SALE_STATUS, STATUS, CREATE_DATE) VALUES ( ?,?,?,?,?,?,?,?,?,?,SYSDATE )";
	
	private void saveModifyOrders(Connection conn, List<FDCustomerOrderInfo> searchResults) throws SQLException{
		PreparedStatement ps =  null;
		try {
			//Truncate all exiting rows
			ps = conn.prepareStatement(DELETE_FROM_MODIFY_ORDERS);
			ps.execute();
			ps.close();
			ps = conn.prepareStatement(INSERT_INTO_MODIFY_ORDERS);
			for (Iterator<FDCustomerOrderInfo> iterator = searchResults.iterator(); iterator.hasNext();) {
				FDCustomerOrderInfo orderInfo = (FDCustomerOrderInfo) iterator.next();
				ps.setString(1, orderInfo.getSaleId());
				ps.setString(2, orderInfo.getIdentity().getErpCustomerPK());
				ps.setString(3, orderInfo.getFirstName());
				ps.setString(4, orderInfo.getLastName());
				if(orderInfo.getEmail()!=null) {
					ps.setString(5,orderInfo.getEmail());
				} else {
					ps.setNull(5, Types.VARCHAR);
				}	
				ps.setString(6, orderInfo.getPhone());
				if(orderInfo.getAltPhone()!=null) {
					ps.setString(7,orderInfo.getAltPhone());
				} else {
					ps.setNull(7, Types.VARCHAR);
				}	
				ps.setDate(8, new java.sql.Date(orderInfo.getDeliveryDate().getTime()));
				
				ps.setString(9, orderInfo.getOrderStatus().getStatusCode());
				ps.setString(10, "Pending");
				ps.addBatch();
			}
			int[] result = ps.executeBatch();			
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw sqle;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
	}
	
	public void createSnapShotForModifyOrders(GenericSearchCriteria criteria) throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			List<FDCustomerOrderInfo> searchResults =  GenericSearchDAO.genericSearch(conn, criteria);
			saveModifyOrders(conn, searchResults);
		} catch (SQLException e) {
			throw new FDResourceException(e, "Could not creating snapshot for modifying orders.");
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after createSnapShotForModifyOrders", e);
				}
			}
		}
		
	}

	private static final String UPDATE_ORDER_MODIFIED_STATUS = "UPDATE CUST.MODIFY_ORDERS SET STATUS = ?, ERROR_DESC = ? WHERE SALE_ID = ?";

	public void updateOrderModifiedStatus(String saleId, String status, String errorDesc) throws FDResourceException{
		Connection conn = null;
		
		try {
			conn = getConnection();
			//Truncate all exiting rows
			PreparedStatement ps = conn.prepareStatement(UPDATE_ORDER_MODIFIED_STATUS);
			ps.setString(1,status);
			ps.setString(2,errorDesc);
			ps.setString(3, saleId);
			ps.executeUpdate();			
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle, "Could not update Order modified status.");
		}finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
	}


	public int cancelReservations(GenericSearchCriteria resvCriteria, String initiator, String notes) throws FDResourceException{
				
		return FDDeliveryManager.getInstance().cancelReservations(resvCriteria, initiator, notes);
	}
		
	public int fixBrokenAccounts() throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return AdminToolsDAO.fixBrokenAccounts(conn);
		} catch (SQLException e) {
			LOGGER.error("SQL Error occurred while fixing the Broken Accounts.");
			throw new FDResourceException(e, "Could not fix Broken Accounts due to SQL Error.");
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after fixing Broken Accounts.", e);
				}
			}
		}
	}

	public Map returnOrders(FDActionInfo info, List customerOrders) throws FDResourceException{
			List successOrders = new ArrayList();
			List failureOrders = new ArrayList();
			String saleId = null;
			try {
				ErpCustomerManagerSB sb = this.getErpCustomerManagerHome().create();
				for(Iterator iter = customerOrders.iterator();iter.hasNext();){
					FDCustomerOrderInfo orderInfo = (FDCustomerOrderInfo)iter.next();
					try{
						//Set it to actionInfo object to write to the activity log.
						saleId = orderInfo.getSaleId();
						ErpSaleModel saleModel = sb.getOrder(new PrimaryKey(saleId));
						FDOrderI order =  new FDOrderAdapter(saleModel);
						ErpReturnOrderModel returnModel = getReturnModel(order);
						//Process Full Return.
						this.returnOrder(saleId, returnModel);
						//Approve Full Return.
						this.approveReturn(saleId, returnModel);
						successOrders.add(orderInfo);
						FDIdentity identity = orderInfo.getIdentity();
						//Set it to actionInfo object to write to the activity log.
						info.setIdentity(identity);
						ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.MASS_RETURN);
						this.logActivity(rec);
					}catch(FDResourceException fe){
						fe.printStackTrace();
						LOGGER.error("System Error occurred while processing Sale ID : "+saleId+"\n"+fe.getMessage());
						failureOrders.add(orderInfo);
					}catch(ErpTransactionException te){
						te.printStackTrace();
						LOGGER.error("Transaction Error occurred while processing Sale ID : "+saleId+"\n"+te.getMessage());
						failureOrders.add(orderInfo);
					}
				}
			} catch (RemoteException ex) {
				throw new FDResourceException(ex);
			} catch (CreateException ce) {
				throw new FDResourceException(ce);
			}

			Map results = new HashMap();
			results.put("SUCCESS_ORDERS", successOrders);
			results.put("FAILURE_ORDERS", failureOrders);
			return results;
	}
	private ErpReturnOrderModel getReturnModel(FDOrderI order){
		List returnLines = new ArrayList();
		List orderLines = order.getOrderLines();
		boolean containsDeliveryPass = false;
		for(Iterator iter = orderLines.iterator();iter.hasNext();){
			FDCartLineI line = (FDCartLineI) iter.next();
			ErpInvoiceLineI invoiceLine = line.getInvoiceLine();
			ErpReturnLineModel returnLine = new ErpReturnLineModel();
			returnLine.setLineNumber(invoiceLine.getOrderLineNumber());
			returnLine.setQuantity(invoiceLine.getQuantity());
			//Since it is a full return
			returnLine.setRestockingOnly(false);
			returnLines.add(returnLine);
			if(line.lookupFDProduct().isDeliveryPass()) {
				//Return order contains a delivery pass.
				containsDeliveryPass = true;
			}

		}
		List charges = new ArrayList();
		for(Iterator i = order.getCharges().iterator(); i.hasNext(); ){
			ErpChargeLineModel charge = new ErpChargeLineModel((ErpChargeLineModel)i.next());
			charges.add(charge);
			//Waive all the charges since it is full return.
			if(EnumChargeType.DELIVERY.equals(charge.getType())) {
				charge.setDiscount(new Discount("DELIVERY", EnumDiscountType.PERCENT_OFF, 1.0));
				continue;
			}
			if(EnumChargeType.PHONE.equals(charge.getType())) {
				charge.setDiscount(new Discount(null, EnumDiscountType.PERCENT_OFF, 1.0));
				continue;
			}
			if(EnumChargeType.MISCELLANEOUS.equals(charge.getType())) {
				charge.setDiscount(new Discount("DELIVERY", EnumDiscountType.PERCENT_OFF, 1.0));
				continue;
			}
		}
		ErpReturnOrderModel returnModel = new ErpReturnOrderModel();
		returnModel.setInvoiceLines(returnLines);
		returnModel.setCharges(charges);
		returnModel.setDlvPassApplied(order.isDlvPassApplied());
		returnModel.setDeliveryPassId(order.getDeliveryPassId());
		returnModel.setContainsDeliveryPass(containsDeliveryPass);
		//Since it is a full return
		returnModel.setRestockingApplied(false);
		return returnModel;
	}


	public int fixSettlemnentBatch(String batch_id) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return AdminToolsDAO.fixSettlemnentBatch(conn, batch_id);
		} catch (SQLException e) {
			LOGGER.error("SQL Error occurred while fixing the settlement batch.");
			throw new FDResourceException(e, "Could not fix settlement batch due to SQL Error.");
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after fixing settlement batch.", e);
				}
			}
		}
	}
	
	private static final String INSERT_TOP_FAQS =
		"INSERT INTO CUST.TOP_FAQS(CMSNODE_ID, TIME_STAMP) VALUES(?,?)";
	public void saveTopFaqs(List faqIds) throws FDResourceException, RemoteException{
		Connection conn = null;
		try {
			conn = this.getConnection();
			List lst = new ArrayList();
			Date date = new Date();
			PreparedStatement ps = conn.prepareStatement(INSERT_TOP_FAQS);
			for (Iterator iterator = faqIds.iterator(); iterator.hasNext();) {
				String faqNodeId = (String) iterator.next();
				ps.setString(1, faqNodeId);
				ps.setTimestamp(2, new java.sql.Timestamp(date.getTime()));	
				ps.addBatch();
			}
			int[] result = ps.executeBatch();			
			ps.close();			
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
	}
   
	
	private static final String CLICK2CALL_QUERY = "select * from CUST.CLICK2CALL where cro_mod_date = (select max(cro_mod_date) from CUST.CLICK2CALL)";
	
	public CrmClick2CallModel getClick2CallInfo() throws FDResourceException{
		Connection conn = null;
		CrmClick2CallModel click2callModel = new CrmClick2CallModel();
		try{
			conn = this.getConnection();
			PreparedStatement ps = conn.prepareStatement(CLICK2CALL_QUERY);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next()){
				click2callModel.setId(rs.getString(1));
				click2callModel.setStatus(("Y"==rs.getString(2))?true:false);
				click2callModel.setEligibleCustomers(rs.getString(3));
				click2callModel.setNextDayTimeSlot(("Y"==rs.getString(4))?true:false);
				click2callModel.setUserId(rs.getString(5));
				click2callModel.setCroModDate(rs.getDate(6));
				PreparedStatement ps1 = conn.prepareStatement("select * from CUST.CLICK2CALL_TIME where click2call_id="+rs.getString(1));
				ResultSet rs1 = ps1.executeQuery();
				CrmClick2CallTimeModel[] click2CallTimeModel = new CrmClick2CallTimeModel[7];
				int i=0;
				while(rs1.next()){
					click2CallTimeModel[i++] = new CrmClick2CallTimeModel(rs1.getString(1),rs1.getString(2),rs1.getString(3),("Y"==rs1.getString(4))?true:false,rs1.getString(5));					
				}
				click2callModel.setDays(click2CallTimeModel);
				rs1.close();
				ps1.close();
			}
			rs.close();
			ps.close();
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
		
		return click2callModel;
	}

	private static final String INSERT_CLICK2CALL =
		"INSERT INTO CUST.Click2Call(id, status,eligible_customers,delivery_zones,nextday_timeslot,userId ,cro_mod_date) VALUES(?,?,?,?,?,?,?)";
	
	private static final String INSERT_CLICK2CALL_TIME =
		"INSERT INTO CUST.CLICK2CALL_TIME(day_name,start_time, end_time, show_flag, click2call_id) VALUES(?,?,?,?,?)";
	public void saveClick2CallInfo(CrmClick2CallModel click2CallModel) throws FDResourceException{
		Connection conn = null;
//		CrmClick2CallModel click2callModel = new CrmClick2CallModel();
		try{
			conn = this.getConnection();
			String id =SequenceGenerator.getNextId(conn, "CUST");
			PreparedStatement ps = conn.prepareStatement(INSERT_CLICK2CALL);
			ps.setString(1, id);
			ps.setString(2, (click2CallModel.isStatus()?"Y":"N"));
			ps.setString(3,click2CallModel.getEligibleCustomers());
			ArrayDescriptor desc = ArrayDescriptor.createDescriptor("CUST.CLICK2CALLZONECODES", conn);
			ARRAY newArray = new ARRAY(desc, conn, click2CallModel.getDeliveryZones());

			ps.setArray(4, newArray);
			ps.setString(5, (click2CallModel.isNextDayTimeSlot()?"Y":"N"));
			ps.setString(6, click2CallModel.getUserId());
			ps.setTimestamp(7, new java.sql.Timestamp(new Date().getTime()));
			ps.execute();		
			PreparedStatement ps1 = conn.prepareStatement(INSERT_CLICK2CALL_TIME);
			CrmClick2CallTimeModel[] daysArray = click2CallModel.getDays();
			for (CrmClick2CallTimeModel crmClick2CallTimeModel : daysArray) {
				ps1.setString(1, crmClick2CallTimeModel.getDayName());
				ps1.setString(2, crmClick2CallTimeModel.getStartTime());
				ps1.setString(3, crmClick2CallTimeModel.getEndTime());
				ps1.setString(4, (crmClick2CallTimeModel.isShow()?"Y":"N"));
				ps1.setString(5, id);	
				ps1.addBatch();
			}
			ps1.executeBatch();
			ps1.close();
			ps.close();
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
	}
	
	private static final String UPDATE_CLICK2CALL =
		"UPDATE CUST.Click2Call set userId=?, status = ? where id =?";
	public void saveClick2CallStatus(String id, String userId, boolean status) throws FDResourceException{
		Connection conn = null;
//		CrmClick2CallModel click2callModel = new CrmClick2CallModel();
		try{
			conn = this.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(UPDATE_CLICK2CALL);
			ps.setString(1, userId);
			ps.setString(2, (status?"Y":"N"));
			ps.setString(3, id);
			
			ps.execute();			
			ps.close();
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
	}
	
	public List<CrmVSCampaignModel> getVSCampaignList() throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<CrmVSCampaignModel> cList = new ArrayList<CrmVSCampaignModel>();
		
		try{
			conn = this.getConnection();
			ps = conn.prepareStatement("select * from CUST.VOICESHOT_CAMPAIGN order by upper(campaign_name)");
			rs = ps.executeQuery();			
			while(rs.next()) {
				CrmVSCampaignModel model = new CrmVSCampaignModel();
				model.setCampaignId(rs.getString("CAMPAIGN_ID"));
				model.setCampaignName(rs.getString("CAMPAIGN_NAME"));
				model.setCampaignMenuId(rs.getString("CAMPAIGN_MENU_ID"));
				model.setAddByDate(rs.getString("ADD_BY_DATE"));
				model.setAddByUser(rs.getString("ADD_BY_USER"));
				model.setChangeByDate(rs.getString("CHANGE_BY_DATE"));
				model.setChangeByUser(rs.getString("CHANGE_BY_USER"));
				model.setSoundfileName(rs.getString("SOUND_FILE_NAME"));
				model.setSoundFileText(rs.getString("SOUND_FILE_TEXT"));
				model.setDelay(rs.getInt("DELAY_IN_MINUTES"));
				cList.add(model);
			}
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rs != null)
					rs.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}			
		}
		return cList;
	}
	
	private static String getStops(List<String> phonenumbers) {
		StringBuffer stopSeq = new StringBuffer();
		Hashtable stopHash = new Hashtable();
		for(int i=0;i<phonenumbers.size(); i++) {
			String pStr = (String) phonenumbers.get(i);
			StringTokenizer st = new StringTokenizer(pStr, "|");
			String phone = st.nextToken();
			String saleId = st.nextToken();
			String customerId = st.nextToken();
			String stopNumber = st.nextToken();
			if(stopNumber != null)
				stopNumber = Integer.parseInt(stopNumber) + "";
			if(!stopHash.containsKey(stopNumber)) {
				stopSeq.append(stopNumber);
				stopHash.put(stopNumber, stopNumber);
				if(i+1 != phonenumbers.size())
					stopSeq.append(",");
			}			
		}
		return stopSeq.toString();
	}
	
	private void createLateIssue(CrmVSCampaignModel model, long vsId) {
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		Date now = new Date();
		String id = null;
		try {			
			conn = this.getConnection();
			Enumeration enumer = model.getRouteList().keys();
			while(enumer.hasMoreElements()) {
				String key = (String) enumer.nextElement();
				List phones = (List) model.getRouteList().get(key);			
			
				id = SequenceGenerator.getNextId(conn, "CUST");
				ps = conn.prepareStatement(
						"INSERT INTO CUST.LATEISSUE(ID, ROUTE, STOPSTEXT, DELIVERY_DATE, AGENT_USER_ID, REPORTED_AT, REPORTED_BY,DELAY_MINUTES,DELIVERY_WINDOW,COMMENTS,ACTUAL_STOPSTEXT,ACTUAL_STOPSCOUNT) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
				ps.setString(1, id);
				ps.setString(2, key);
				ps.setString(3,getStops(phones));
				ps.setDate(4, new java.sql.Date(now.getTime()));
				ps.setString(5,model.getAddByUser());
				ps.setTimestamp(6, new java.sql.Timestamp(now.getTime()));
				ps.setString(7, "Driver");
				ps.setInt(8, Integer.parseInt(model.getDelayMinutes()));
				ps.setString(9, "");
				ps.setString(10, "Comments");
				ps.setString(11, "");
				ps.setInt(12, phones.size());
				ps.executeUpdate();
				ps1 = conn.prepareStatement("INSERT INTO CUST.VOICESHOT_LATEISSUE(VS_ID, LATEISSUE_ID) VALUES(?,?)");
				ps1.setLong(1, vsId);
				ps1.setString(2, id);
				ps1.execute();
				
				ps2 = conn.prepareStatement("insert into cust.lateissue_orders columns(lateissue_id,stop_number,sale_id) values(?,?,?)");
				for(int i=0;i<phones.size(); i++) {
					String pStr = (String) phones.get(i);
					System.out.println(pStr + "-lateid" + id);
					StringTokenizer st = new StringTokenizer(pStr, "|");
					String phone = st.nextToken();
					String saleId = st.nextToken();
					String customerId = st.nextToken();
					String stopNumber = st.nextToken();				
					ps2.setString(1, id);
					ps2.setString(2, stopNumber);
					ps2.setString(3, saleId);
					ps2.execute();
				}
				
				
			}
		} catch (Exception e) {
			LOGGER.error("LateIssue row not created for: Route:"+model.getRoute(),e);
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null) 				
					ps.close();
				if(ps1 != null)
					ps1.close();
				if(ps2 != null)
					ps2.close();
				
			} catch(Exception e1) {}
		}
	}
	
	public String saveVSCampaignInfo(CrmVSCampaignModel model) throws FDResourceException {		
		//Create voiceshot details for this late issue
		Connection conn = null;
		PreparedStatement ps = null;
		long id = 1; 
		String call_id = "CID_" + id;
		try{
			conn = this.getConnection();
			id = Long.parseLong(SequenceGenerator.getNextId(conn, "CUST", "VOICESHOT_SEQUENCE"));
			call_id = "CID_" + id;
			if(model.getManual()) {						
				ps = conn.prepareStatement("INSERT INTO CUST.VOICESHOT_SCHEDULED(VS_ID,CAMPAIGN_ID,REASON_ID,CREATED_BY_USER,CREATED_BY_DATE,START_TIME, CALL_ID, CAMPAIGN_TYPE, CALL_DATA_PULLED)" +
											" VALUES(?,?,?,?,?,TO_DATE(?, 'HH:MI AM'),?,'MANUAL', 'Y')");
			} else {
				ps = conn.prepareStatement("INSERT INTO CUST.VOICESHOT_SCHEDULED(VS_ID,CAMPAIGN_ID,REASON_ID,CREATED_BY_USER,CREATED_BY_DATE,START_TIME, CALL_ID, CAMPAIGN_TYPE)" +
											" VALUES(?,?,?,?,?,TO_DATE(?, 'HH:MI AM'),?,'LATEISSUE')");
			}
			System.out.println(model.toString());
			ps.setLong(1,id);			
			ps.setString(2, model.getCampaignId());
			ps.setString(3, model.getReasonId());
			ps.setString(4, model.getAddByUser());
			ps.setTimestamp(5, new Timestamp(new Date().getTime()));
			ps.setString(6, model.getStartTime());
			ps.setString(7, call_id);
			ps.execute();
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
		
		//Create lateissue_orders rows
		storePhoneNumbers(id, model);
		//Create LASTISSUE 
		createLateIssue(model, id);
			
		return call_id;
	}	
	
	private void storePhoneNumbers(long id, CrmVSCampaignModel model) {
		Connection conn = null;
		PreparedStatement ps = null;
		List<String> phonenumbers = model.getPhonenumbers();
		
		try {
			conn = this.getConnection();
			if(model.getManual()) {
				ps = conn.prepareStatement("INSERT INTO CUST.voiceshot_customers(VS_ID,PHONE, CUSTOMER_ID, SALE_ID, status)" +
											" VALUES(?,?,?,?,0)");
			} else {
				ps = conn.prepareStatement("INSERT INTO CUST.voiceshot_customers(VS_ID,PHONE, CUSTOMER_ID, SALE_ID)" +
											" VALUES(?,?,?,?)");
			}
			for(int i=0;i<phonenumbers.size(); i++) {
				String pStr = (String) phonenumbers.get(i);
				StringTokenizer st = new StringTokenizer(pStr, "|");
				String phone = st.nextToken();
				String saleId = st.nextToken();
				String customerId = st.nextToken();
				String stopNumber = st.nextToken();				
				ps.setLong(1, id);
				ps.setString(2, phone);
				ps.setString(3, customerId);
				ps.setString(4, saleId);
				ps.execute();
			}
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);			
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
		//record the call activity
		updateActivity(model);
	}
	
	public static final String GET_VS_LOG = "select vl.lateissue_id, vl.vs_ID, L.ROUTE, VC.CAMPAIGN_ID, VC.CAMPAIGN_MENU_ID, VC.CAMPAIGN_NAME, L.STOPSTEXT, vs.redial, " + 
    "vs.created_by_user, vc.sound_file_text, " +
    "to_char(vs.created_by_date,  'MM/DD/YYYY HH12:MI AM') created_by_Date, VC.SOUND_FILE_NAME, to_char(vs.start_time, 'HH12:MI AM') start_time, " + 
    "vs.call_id, vs.call_data_pulled, vr.reason, vs.campaign_type " +    		
    "from CUST.LATEISSUE l, " +
    "CUST.VOICESHOT_SCHEDULED vs, " + 
    "CUST.VOICESHOT_CAMPAIGN vc, " +
    "cust.voiceshot_reasoncodes vr, " +
    "cust.voiceshot_lateissue vl " +
    "where L.ID = vl.lateissue_id " +
    "and    vl.vs_id = vs.vs_id " +
    "and    vs.campaign_id = VC.CAMPAIGN_ID " + 
    "and    vs.reason_id = vr.reason_id " +
    "order by vs.created_by_date desc";
	
	public static final String GET_VS_LOG_BY_DATE = "select vl.lateissue_id, vl.vs_ID, L.ROUTE, VC.CAMPAIGN_ID, VC.CAMPAIGN_MENU_ID, VC.CAMPAIGN_NAME, L.STOPSTEXT, vs.redial, " + 
    "vs.created_by_user, vc.sound_file_text, " +
    "to_char(vs.created_by_date,  'MM/DD/YYYY HH12:MI AM') created_by_Date, VC.SOUND_FILE_NAME, to_char(vs.start_time, 'HH12:MI AM') start_time, " + 
    "vs.call_id, vs.call_data_pulled, vr.reason, vs.campaign_type " +    		
    "from CUST.LATEISSUE l, " +
    "CUST.VOICESHOT_SCHEDULED vs, " + 
    "CUST.VOICESHOT_CAMPAIGN vc, " +
    "cust.voiceshot_reasoncodes vr, " +
    "cust.voiceshot_lateissue vl " +
    "where L.ID = vl.lateissue_id " +
    "and    vl.vs_id = vs.vs_id " +
    "and    vs.campaign_id = VC.CAMPAIGN_ID " + 
    "and    vs.reason_id = vr.reason_id " +
    "and    vs.created_by_date > ? " +  
    "order by vs.created_by_date desc";
	
	public List<CrmVSCampaignModel> getVoiceShotLog(Date date) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<CrmVSCampaignModel> cList = new ArrayList<CrmVSCampaignModel>();
		
		try{
			conn = this.getConnection();
			if(date == null)
				ps = conn.prepareStatement(GET_VS_LOG);
			else {
				ps = conn.prepareStatement(GET_VS_LOG_BY_DATE);
				ps.setDate(1, new java.sql.Date(date.getTime()));
			}
			rs = ps.executeQuery();			
			while(rs.next()) {
				CrmVSCampaignModel model = new CrmVSCampaignModel();
				String vs_id = rs.getString("VS_ID");
				String lateissue_id = rs.getString("lateissue_id");
				model.setVsDetailsID(rs.getString("VS_ID"));
				model.setLateIssueId(rs.getString("lateissue_id"));
				model.setRoute(rs.getString("ROUTE"));				
				model.setCampaignId(rs.getString("CAMPAIGN_ID"));
				model.setCampaignName(rs.getString("CAMPAIGN_NAME"));
				model.setCampaignMenuId(rs.getString("CAMPAIGN_MENU_ID"));
				model.setReasonId(rs.getString("REASON"));
				//model.setStopSequence(rs.getString("STOPSTEXT"));
				model.setStopSequence(getStopsTextInSequence(lateissue_id, vs_id));
				model.setRedial(rs.getString("REDIAL"));				
				model.setAddByDate(rs.getString("CREATED_BY_DATE"));
				model.setAddByUser(rs.getString("CREATED_BY_USER"));
				model.setSoundfileName(rs.getString("SOUND_FILE_NAME"));
				model.setSoundFileText(rs.getString("SOUND_FILE_TEXT"));
				model.setStartTime(rs.getString("START_TIME"));
				model.setCallId(rs.getString("CALL_ID"));
				model.setUpdatable(true);
				if("MANUAL".equals(rs.getString("CAMPAIGN_TYPE"))) {
					model.setManual(true);
				} else {
					model.setManual(false);
				}
				boolean data_pulled = "Y".equals(rs.getString("CALL_DATA_PULLED"))?true:false;
				if(!data_pulled && rs.getString("CALL_ID") != null) {
					StringBuffer sb = new StringBuffer("<campaign action=\"3\" menuid=\"");
					sb.append(rs.getString("CAMPAIGN_MENU_ID"));
					//sb.append("\" username=\"mtrachtenberg\" password=\"whitshell\"><phonenumbers><phonenumber callid=\"");
					sb.append("\" username=\"");
					sb.append(FDStoreProperties.getVSUserName());
					sb.append("\" password=\"");
					sb.append(FDStoreProperties.getVSPassword());
					sb.append("\"><phonenumbers><phonenumber callid=\"");
					sb.append(rs.getString("CALL_ID"));
					sb.append("\" /></phonenumbers></campaign>");
					System.out.println(sb.toString());
					VoiceShotResponseParser vsrp = getCallData(sb.toString());
					if(vsrp != null) {
						model.setUpdatable(true);
						updateVSLog(rs.getLong("VS_ID"), vsrp);
						updateUserStatus(rs.getLong("VS_ID"), vsrp.getPhonenumbers());
					} else {
						model.setUpdatable(false);
					}
				}
				
				//get call data
				getCallStats(model, vs_id, lateissue_id);
				cList.add(model);
			}
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rs != null)
					rs.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}			
		}
		return cList;
	}
	
	private String getStopsTextInSequence(String lateissueId, String vsId) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			conn = this.getConnection();
			
			ps = conn.prepareStatement("SELECT SUBSTR (SYS_CONNECT_BY_PATH (stop_number , ', '), 2) stop_number " +
										      "FROM (SELECT to_number(stop_number) stop_number , ROW_NUMBER () OVER (ORDER BY to_number(stop_number) ) rn, " +
										                   "COUNT (*) OVER () cnt " +
										                   "FROM  cust.lateissue_orders lo, " +
										                   "CUST.VOICESHOT_LATEISSUE vl, " +
										                   "CUST.VOICESHOT_CUSTOMERS vc " +
										                   "where vl.lateissue_id = ? " +
										                   "and    VL.VS_ID = ? " +
										                   "and    VC.VS_ID = VL.VS_ID " +
										                   "and    VC.SALE_ID = LO.SALE_ID " +
										                   "and    LO.LATEISSUE_ID = VL.LATEISSUE_ID ) " +
										     "WHERE rn = cnt " +
										"START WITH rn = 1 " +
										"CONNECT BY rn = PRIOR rn + 1");
			ps.setString(1, lateissueId);
			ps.setString(2, vsId);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rs != null)
					rs.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}			
		}	
		return "";
	}
	
	private void getCallStats(CrmVSCampaignModel model, String vsId, String lateissueId) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			conn = this.getConnection();
			
			ps = conn.prepareStatement("select NVL(vo.status,1) from CUST.LATEISSUE_ORDERS lo, cust.voiceshot_lateissue vl, cust.voiceshot_customers vo " +
									   "where LO.LATEISSUE_ID = ? and LO.LATEISSUE_ID = vl.lateissue_id and vl.vs_id = vo.vs_id " +
									   "and   vl.vs_id = ? and LO.SALE_ID = vo.sale_id");
			ps.setString(1, lateissueId);
			ps.setString(2, vsId);
			rs = ps.executeQuery();
			int unsucessful = 0;
			int am_calls = 0;
			int live_calls = 0;
			int total_calls = 0;
			while (rs.next()) {
				total_calls++;
				int status = rs.getInt(1);
				if(status == EnumVSStatus.UNSUCCESSFUL.getValue())
					unsucessful++;
				else if(status == EnumVSStatus.ANS_MACHINE.getValue())
					am_calls++;
				else if(status == EnumVSStatus.LIVE_ANS.getValue())
					live_calls++;
			}
			model.setDeliveredCallsLive(live_calls);
			model.setDeliveredCallsAM(am_calls);
			model.setUndeliveredCalls(unsucessful);
			model.setScheduledCalls(total_calls);
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rs != null)
					rs.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}			
		}	
		
	}

	private void updateUserStatus(long long1, Hashtable<String, String> phonenumbers) {
		Connection conn = null;
		PreparedStatement ps = null;
		
		try{
			conn = this.getConnection();
			ps = conn.prepareStatement("update CUST.voiceshot_customers set STATUS=? where VS_ID=? and PHONE=?");
			Enumeration<String> enumber = phonenumbers.keys();
			while(enumber.hasMoreElements()) {
				String key = (String)enumber.nextElement();
				String phone = key;
				if(phone.length() == 11) {
					//remove 1 and make it 10
					phone = phone.substring(1);
				}
				PhoneNumber phonenumber = new PhoneNumber(phone);
				
				String value = phonenumbers.get(key);
				ps.setInt(1, Integer.parseInt(value));
				ps.setLong(2, long1);
				ps.setString(3, phonenumber.getPhone());
				ps.execute();
			}			
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}			
		}		
	}

	private void updateVSLog(long id, VoiceShotResponseParser vsrp) {
		Connection conn = null;
		PreparedStatement ps = null;
		
		try{
			conn = this.getConnection();
			ps = conn.prepareStatement("update CUST.VOICESHOT_SCHEDULED set CALL_DATA_PULLED='Y' where VS_ID=?");
			ps.setLong(1, id);
			ps.execute();
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}			
		}		
	}

	private VoiceShotResponseParser getCallData(String xmlPost) {
		try {
			
			java.net.URL programUrl = new java.net.URL(FDStoreProperties.getVSURL());   
			java.net.HttpURLConnection connection = (java.net.HttpURLConnection)programUrl.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setUseCaches(false); 
			connection.setRequestProperty("Content-Type", "text/xml");
			java.io.PrintWriter output = new java.io.PrintWriter(new java.io.OutputStreamWriter(connection.getOutputStream()));
			output.println(xmlPost);
			output.close(); 
			connection.connect();
			java.io.InputStream is = connection.getInputStream();
			java.io.InputStreamReader isr = new java.io.InputStreamReader(is);
			java.io.BufferedReader br = new java.io.BufferedReader(isr);
		 
			String line = null;
			String firstresult = "";
		 
			while ((line = br.readLine()) != null) {
				 firstresult += "\n" + line;
			}
			
			System.out.println(firstresult);
			
			
			//String firstresult = "<?xml version=\"1.0\"?><campaign menuid=\"4766-159328639\" ><phonenumbers><phonenumber number=\"12038430301\"  dateandtime=\"11/8/2011 2:13:45 PM\"     callid=\"CID_42\"   duration=\"7\" status=\"Successful\"     lasterror=\"Human Answer\"><prompts></prompts></phonenumber><phonenumber number=\"12034469229\"  dateandtime=\"11/8/2011 2:14:22 PM\"     callid=\"CID_42\"   duration=\"44\" status=\"Successful\"     lasterror=\"Answering Machine\"><prompts><prompt promptid=\"1\" keypress=\"\" /></prompts></phonenumber></phonenumbers></campaign> ";
			
			if(firstresult.indexOf("status=\"Pending\"") == -1) {
				LOGGER.debug("Ready to update the call record");
				VoiceShotResponseParser vsrp = new VoiceShotResponseParser(firstresult);				
				vsrp.populateCallData();
				if(vsrp.getTotalCalls() > 0) {
					LOGGER.debug("Total calls:" + vsrp.getTotalCalls());
					LOGGER.debug("Total Successful calls:" + vsrp.getSuccessfulCalls());
					LOGGER.debug("Total UnSuccessful calls:" + vsrp.getUnsuccessfulCalls());
					LOGGER.debug("Total Human Answered calls:" + vsrp.getHumanAnsweredCalls());
					LOGGER.debug("Total Answer Machine calls:" + vsrp.getAnswerMachineCalls());
					return vsrp;
				}
			} else {
				LOGGER.debug("Call is still happening. Don't let the user redial.");
			}
		} catch(Exception e) {
			LOGGER.error("",e);
		}
		return null;
	}
	
	public List<CrmVSCampaignModel> getVoiceShotCallDetails(String id, String lateId) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		List<CrmVSCampaignModel> cList = new ArrayList<CrmVSCampaignModel>();
		try{
			conn = this.getConnection();
			ps = conn.prepareStatement("select  vo.customer_id, vo.sale_id, vo.phone, vo.status, S.TRUCK_NUMBER, S.STOP_SEQUENCE " +
                    "from cust.voiceshot_customers vo, " +
                    "CUST.LATEISSUE_ORDERS lo, " +
                    "cust.voiceshot_lateissue vl, " +
                    "cust.sale s " +
                    "where vl.lateissue_id = ? " +
                    "and    vl.vs_id = ? " +
                    "and    LO.LATEISSUE_ID = vl.lateissue_id " +
                    "and    vo.vs_id = vl.vs_id " +
                    "and    LO.SALE_ID = vo.sale_id " + 
                    "and     vo.sale_id = s.id");
			ps.setString(1, lateId);
			ps.setLong(2, Long.parseLong(id));
			rset = ps.executeQuery();
			while(rset.next()) {
				String phone = rset.getString("PHONE");
				int status = rset.getInt("STATUS");
				CrmVSCampaignModel model = new CrmVSCampaignModel();
				model.setPhonenumber(phone);
				model.setStatus(status);
				model.setCustomerId(rset.getString("CUSTOMER_ID"));
				model.setSaleId(rset.getString("SALE_ID"));
				model.setRoute(rset.getString("TRUCK_NUMBER"));
				String sSeq = rset.getString("STOP_SEQUENCE");
				int iSeq = Integer.parseInt(sSeq);
				model.setStopSequence(iSeq + "");
				cList.add(model);
			}
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rset != null)
					rset.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}			
		}
		return cList;
	}
	
	public static final String GET_REDIAL_LIST = "select phone, nvl(vo.last_redialed_date, vd.created_by_date) last_redialed_Date, vo.sale_id, vo.customer_id, S.TRUCK_NUMBER, S.STOP_SEQUENCE " + 
									    "from cust.voiceshot_customers  vo, " +
									    "CUST.VOICESHOT_SCHEDULED vd, " +
									    "CUST.LATEISSUE_ORDERS lo, " +
									    "cust.voiceshot_lateissue vl, " +
									    "cust.sale s " +
									"where  vl.lateissue_id = ? " +
									 "and    vl.vs_id = ? " +
									 "and    LO.LATEISSUE_ID = vl.lateissue_id " +
									 "and    vo.vs_id = vl.vs_id " +
									 "and    LO.SALE_ID = vo.sale_id " +  
									 "and    vo.vs_id = vd.vs_id  " +
									 "and     vo.status = ? " +
									 "and     vo.sale_id = s.id";   
	
	public List<CrmVSCampaignModel> getVSRedialList(String id, String lateId) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		List<CrmVSCampaignModel> cList = new ArrayList<CrmVSCampaignModel>();
		try{
			conn = this.getConnection();
			ps = conn.prepareStatement(GET_REDIAL_LIST);
			ps.setString(1, lateId);
			ps.setLong(2, Long.parseLong(id));
			ps.setInt(3, EnumVSStatus.UNSUCCESSFUL.getValue());
			rset = ps.executeQuery();
			while(rset.next()) {
				String phone = rset.getString("PHONE");
				Date last_date = rset.getDate("last_redialed_Date");
				Calendar c1 = Calendar.getInstance();
				c1.setTime(last_date);
				Calendar c2 = Calendar.getInstance();
				c2.setTime(new Date());
				long timeDiff = (c2.getTime().getTime() - c1.getTime().getTime());
				long hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(timeDiff);
				System.out.println("hours Between " + c1.getTime() + " and "
						+ c2.getTime() + " is:" + hours);
				if (hours < 24) {
					CrmVSCampaignModel model = new CrmVSCampaignModel();
					model.setPhonenumber(phone);
					model.setVsDetailsID(id);
					model.setSaleId(rset.getString("sale_id"));
					model.setCustomerId(rset.getString("customer_id"));
					model.setRoute(rset.getString("TRUCK_NUMBER"));
					String sSeq = rset.getString("STOP_SEQUENCE");
					int iSeq = Integer.parseInt(sSeq);
					model.setStopSequence(iSeq + "");
					cList.add(model);
				}
			}
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rset != null)
					rset.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}			
		}
		return cList;
	}
	
	public String saveVSRedialInfo(CrmVSCampaignModel model) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		long id = 1;
		String call_id = "CID_" + id;
		try {
			conn = this.getConnection();
			id = Long.parseLong(SequenceGenerator.getNextId(conn, "CUST", "VOICESHOT_SEQUENCE"));
			ps = conn.prepareStatement("INSERT INTO CUST.VOICESHOT_SCHEDULED(VS_ID,CAMPAIGN_ID,REASON_ID,CREATED_BY_USER,CREATED_BY_DATE,START_TIME, CALL_ID, CAMPAIGN_TYPE, redial) " +
										"select ?, campaign_id, reason_id, ?, sysdate, TO_DATE(?, 'HH:MI AM'),?,'LATEISSUE', 'Y' " +
										"from cust.voiceshot_scheduled where vs_id = ?");
			
			call_id = "CID_" + id;
			System.out.println(model.toString());
			ps.setLong(1,id);
			ps.setString(2, model.getAddByUser());
			Calendar today_date = Calendar.getInstance();
			today_date.setTime(new Date());
			int hour = today_date.get(Calendar.HOUR);
			int minute = today_date.get(Calendar.MINUTE);
			if(hour == 0)
				hour = 12;
			String am_pm = today_date.get(Calendar.AM_PM) == 0?"AM":"PM";
			String start_time = hour + ":" + minute + " " + am_pm;	
			ps.setString(3, start_time);
			ps.setString(4, call_id);
			ps.setLong(5, Long.parseLong(model.getVsDetailsID()));
			ps.execute();
			
			ps1 = conn.prepareStatement("INSERT INTO CUST.VOICESHOT_LATEISSUE(VS_ID, LATEISSUE_ID) VALUES(?,?)");
			ps1.setLong(1, id);
			ps1.setString(2, model.getLateIssueId());
			ps1.execute();
			
			ps2 = conn.prepareStatement("INSERT INTO CUST.voiceshot_customers(VS_ID,PHONE, CUSTOMER_ID, SALE_ID, LAST_REDIALED_DATE)" +
											" VALUES(?,?,?,?, sysdate)");
			for(int i=0;i<model.getPhonenumbers().size(); i++) {
				String pStr = (String) model.getPhonenumbers().get(i);
				StringTokenizer st = new StringTokenizer(pStr, "|");
				String phone = st.nextToken();
				String saleId = st.nextToken();
				String customerId = st.nextToken();
				ps2.setLong(1, id);
				ps2.setString(2, phone);
				ps2.setString(3, customerId);
				ps2.setString(4, saleId);
				ps2.execute();
			}
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(ps1 != null)
					ps1.close();
				if(ps2 != null)
					ps2.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
		updateActivity(model);
		return call_id;
	}
	
	private void updatePhonenumbers(List<String> phonenumbers, String detailId) {
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("UPDATE CUST.voiceshot_customers set LAST_REDIALED_DATE=sysdate where sale_id = ? and customer_id = ? and vs_id = ?");
			for(int i=0;i<phonenumbers.size(); i++) {
				String pStr = (String) phonenumbers.get(i);
				StringTokenizer st = new StringTokenizer(pStr, "|");
				String phone = st.nextToken();
				String saleId = st.nextToken();
				String customerId = st.nextToken();
				ps.setString(1, saleId);
				ps.setString(2, customerId);
				ps.setLong(3, Long.parseLong(detailId));				
				ps.execute();
			}
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);			
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
	}

	private void updateActivity(CrmVSCampaignModel model) {
		List<String> phonenumbers = model.getPhonenumbers();
		for(int i=0;i<phonenumbers.size(); i++) {
			String pStr = (String) phonenumbers.get(i);
			StringTokenizer st = new StringTokenizer(pStr, "|");
			String phone = st.nextToken();
			String saleId = st.nextToken();
			String customerId = st.nextToken();
				
			//record the call activity
			ErpActivityRecord rec = new ErpActivityRecord();
			rec.setActivityType(EnumAccountActivityType.VOICE_SHOT);
			rec.setSource(EnumTransactionSource.CUSTOMER_REP);
			rec.setInitiator(model.getAddByUser());
			rec.setChangeOrderId(saleId);
			rec.setCustomerId(customerId);
			rec.setDate(new Date());
			rec.setNote(model.getCampaignName() + " - " + model.getReasonId());
			this.logActivity(rec);				
		}
	}
	
	public void addNewCampaign(CrmVSCampaignModel model) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("INSERT INTO CUST.VOICESHOT_CAMPAIGN(CAMPAIGN_ID,CAMPAIGN_NAME,ADD_BY_DATE,ADD_BY_USER,SOUND_FILE_NAME,SOUND_FILE_TEXT,CAMPAIGN_MENU_ID,DELAY_IN_MINUTES)" +
										" VALUES(?,?,sysdate,?,?,?,?,?)");
			long id = Long.parseLong(SequenceGenerator.getNextId(conn, "CUST", "VOICESHOT_SEQUENCE"));
			ps.setLong(1, id);
			ps.setString(2, model.getCampaignName());
			ps.setString(3, model.getAddByUser());
			ps.setString(4, model.getSoundfileName());
			ps.setString(5, model.getSoundFileText());
			ps.setString(6, model.getCampaignMenuId());
			ps.setInt(7, model.getDelay());
			ps.execute();
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);			
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
	}
	
	public CrmVSCampaignModel getCampaignDetails(String id) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		CrmVSCampaignModel model = new CrmVSCampaignModel();
		
		try{
			conn = this.getConnection();
			ps = conn.prepareStatement("select * from CUST.VOICESHOT_CAMPAIGN where CAMPAIGN_ID=?");
			ps.setLong(1, Long.parseLong(id));
			rs = ps.executeQuery();			
			while(rs.next()) {
				model.setCampaignId(rs.getString("CAMPAIGN_ID"));
				model.setCampaignName(rs.getString("CAMPAIGN_NAME"));
				model.setCampaignMenuId(rs.getString("CAMPAIGN_MENU_ID"));
				model.setAddByDate(rs.getString("ADD_BY_DATE"));
				model.setAddByUser(rs.getString("ADD_BY_USER"));
				model.setChangeByDate(rs.getString("CHANGE_BY_DATE"));
				model.setChangeByUser(rs.getString("CHANGE_BY_USER"));
				model.setSoundfileName(rs.getString("SOUND_FILE_NAME"));
				model.setSoundFileText(rs.getString("SOUND_FILE_TEXT"));
				model.setDelay(rs.getInt("DELAY_IN_MINUTES"));
			}
		}catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rs != null)
					rs.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}			
		}
		return model;
	}
	
	public void updateCampaign(CrmVSCampaignModel model) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("UPDATE CUST.VOICESHOT_CAMPAIGN set CAMPAIGN_NAME=?, SOUND_FILE_NAME=?, SOUND_FILE_TEXT=?, CAMPAIGN_MENU_ID=?," +
										" CHANGE_BY_USER=?, CHANGE_BY_DATE=SYSDATE, DELAY_IN_MINUTES=? where CAMPAIGN_ID=?");
			ps.setString(1, model.getCampaignName());			
			ps.setString(2, model.getSoundfileName());
			ps.setString(3, model.getSoundFileText());
			ps.setString(4, model.getCampaignMenuId());
			ps.setString(5, model.getChangeByUser());
			ps.setInt(6, model.getDelay());
			ps.setLong(7, Long.parseLong(model.getCampaignId()));
			ps.execute();
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);			
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
	}
	
	public void deleteCampaign(String id) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("DELETE CUST.VOICESHOT_CAMPAIGN where CAMPAIGN_ID=?");
			ps.setLong(1, Long.parseLong(id));
			ps.execute();
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);			
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
	}
	
	public static final String GET_VS_DETAILS_FOR_ORDER = "select vc.campaign_name, Vs.created_BY_USER,  to_char(vs.created_BY_DATE, 'MM/DD/YYYY HH:MI AM') ADD_BY_DATE, Vr.REASON, vo.vs_id " +
            "from CUST.voiceshot_customers vo, cust.voiceshot_campaign vc, CUST.VOICESHOT_SCHEDULED vs, " +
            " cust.voiceshot_Reasoncodes vr " +
            "where vo.sale_id = ? " +
            "and    vs.vs_id = vo.vs_id " +
            "and    vo.status is not null " +
            "and  Vs.CAMPAIGN_ID = VC.CAMPAIGN_ID " + 
            "and  vs.reason_id = vr.reason_id " +
            "order by vo.vs_id desc";
	
	public String getVSMsgForOrderPage(String orderId) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		String vsMsg = null;
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement(GET_VS_DETAILS_FOR_ORDER);
			ps.setString(1, orderId);			
			rset = ps.executeQuery();
			if(rset.next()) {
				StringBuffer sb = new StringBuffer("Late Report: ");
				sb.append(rset.getString("ADD_BY_DATE"));
				sb.append(", ");
				sb.append(rset.getString("created_BY_USER"));
				sb.append(", ");
				sb.append( rset.getString("CAMPAIGN_NAME"));
				sb.append(", ");
				sb.append(rset.getString("REASON"));
				
				vsMsg = sb.toString();  
			}
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);			
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rset != null)
					rset.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
		return vsMsg;
	}
	
	public List<VSReasonCodes> getVSReasonCodes() throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		List<VSReasonCodes> lst = new ArrayList<VSReasonCodes>();
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("select * from cust.voiceshot_reasoncodes");
			rset = ps.executeQuery();
			while(rset.next()) {
				VSReasonCodes vrc = new VSReasonCodes();
				vrc.setReasonId(rset.getString("REASON_ID"));
				vrc.setReason(rset.getString("REASON"));
				lst.add(vrc);
			}
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);			
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rset != null)
					rset.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
		return lst;
	}
	
	public String getSoundFileMessage(String campaignId) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		String vsMsg = null;
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("select sound_file_text from cust.voiceshot_campaign where campaign_id = ?");
			ps.setString(1, campaignId);			
			rset = ps.executeQuery();
			if(rset.next()) {
				vsMsg = rset.getString(1);  
			}
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);			
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rset != null)
					rset.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
		return vsMsg;
	}
	
	public List getAutoLateDeliveryCredits() throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		List<CustomerCreditModel> ccmList = new ArrayList<CustomerCreditModel>();
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("select ID, order_Date as order_date, status, approved_by from cust.AUTO_LATE_DELIVERY order by order_date desc");
			rset = ps.executeQuery();
			while(rset.next()) {
				CustomerCreditModel ccm = new CustomerCreditModel();
				ccm.setId(rset.getString("ID"));
				ccm.setOrderDate(rset.getDate("order_date") != null ? DateUtil.formatDate(rset.getDate("order_date")) : null);
				ccm.setStatus(rset.getString("status"));
				ccm.setApprovedBy(rset.getString("approved_by"));
				ccmList.add(ccm);
			}
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);			
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rset != null)
					rset.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
		return ccmList;
	}
	
	public List getAutoLateDeliveryOrders(String id) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		List<CustomerCreditModel> ccmList = new ArrayList<CustomerCreditModel>();
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("select sale_id, a.customer_id, rem_amount, rem_type, original_amount, tax_amount, tax_rate, dlv_pass_id, CI.FIRST_NAME, CI.LAST_NAME, complaint_code, c.user_id, a.status " +
											"from cust.AUTO_LATE_DELIVERY_ORDERS a, " +
											"cust.customerinfo ci, " +
											"cust.customer c " +
											"where auto_late_delivery_id = ? " +
											"and a.customer_id = ci.customer_id " +
											"and a.customer_id = c.id " +
											"and a.dlv_pass_id is null");
			ps.setString(1, id);
			rset = ps.executeQuery();
			while(rset.next()) {
				CustomerCreditModel ccm = new CustomerCreditModel();
				ccm.setId(id);
				ccm.setSaleId(rset.getString("sale_id"));
				ccm.setCustomerId(rset.getString("customer_id"));
				ccm.setRemainingAmout(rset.getDouble("rem_amount"));
				ccm.setRemType(rset.getString("rem_type"));
				ccm.setOriginalAmount(rset.getDouble("original_amount"));
				ccm.setTaxAmount(rset.getDouble("tax_amount"));
				ccm.setTaxRate(rset.getDouble("tax_rate"));
				ccm.setDlvPassId(rset.getString("dlv_pass_id"));
				ccm.setFirstName(rset.getString("first_name"));
				ccm.setLastName(rset.getString("last_name"));
				ccm.setNewCode(rset.getString("complaint_code"));
				ccm.setEmail(rset.getString("user_id"));
				ccm.setStatus(rset.getString("status"));
				ccmList.add(ccm);
			}
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);			
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rset != null)
					rset.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
		return ccmList;
	}
	
	public List getAutoLateDlvPassOrders(String id) throws FDResourceException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rset = null;
		List<CustomerCreditModel> ccmList = new ArrayList<CustomerCreditModel>();
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("select sale_id, a.customer_id, rem_amount, rem_type, original_amount, tax_amount, tax_rate, dlv_pass_id, CI.FIRST_NAME, CI.LAST_NAME, complaint_code, c.user_id, a.status " +
											"from cust.AUTO_LATE_DELIVERY_ORDERS a, " +
											"cust.customerinfo ci, " +
											"cust.customer c " +
											"where auto_late_delivery_id = ? " +
											"and a.customer_id = ci.customer_id " +
											"and a.customer_id = c.id " +
											"and a.dlv_pass_id is not null");
			ps.setString(1, id);
			rset = ps.executeQuery();
			while(rset.next()) {
				CustomerCreditModel ccm = new CustomerCreditModel();
				ccm.setId(id);
				ccm.setSaleId(rset.getString("sale_id"));
				ccm.setCustomerId(rset.getString("customer_id"));
				ccm.setRemainingAmout(rset.getDouble("rem_amount"));
				ccm.setRemType(rset.getString("rem_type"));
				ccm.setOriginalAmount(rset.getDouble("original_amount"));
				ccm.setTaxAmount(rset.getDouble("tax_amount"));
				ccm.setTaxRate(rset.getDouble("tax_rate"));
				ccm.setDlvPassId(rset.getString("dlv_pass_id"));
				ccm.setFirstName(rset.getString("first_name"));
				ccm.setLastName(rset.getString("last_name"));
				ccm.setNewCode(rset.getString("complaint_code"));
				ccm.setEmail(rset.getString("user_id"));
				ccm.setStatus(rset.getString("status"));
				ccmList.add(ccm);
			}
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);			
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
				if(rset != null)
					rset.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
		return ccmList;
	}
	
	public ErpComplaintReason getReasonByCompCode(String cCode) throws FDResourceException {
		try {
			ErpComplaintManagerSB complaintSB = this.getComplaintManagerHome().create();
			return complaintSB.getReasonByCompCode(cCode);
		} catch (RemoteException re) {
			throw new FDResourceException(re);
		} catch (CreateException ce) {
			throw new FDResourceException(ce);
		}
	}
	
	public void addNewIVRCallLog(CallLogModel model) throws FDResourceException {
		
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = this.getConnection();
			ps = conn.prepareStatement("INSERT INTO CUST.IVR_CALLLOG(ID,CALLERID,ORDERNUMBER,CALLTIME,CALLDURATION,TALKTIME,PHONE_NUMBER,CALL_OUTCOME,MENU_OPTION,INSERT_TIMESTAMP)" +
										" VALUES(?,?,?,?,?,?,?,?,?,?)");
				
			ps.setString(1, model.getCallerGUIId());
			ps.setString(2, model.getCallerId());
			ps.setString(3, model.getOrderNumber());
			ps.setTimestamp(4, new java.sql.Timestamp(model.getStartTime().getTime()));
			ps.setInt(5, model.getDuration());
			ps.setInt(6, model.getTalkTime());
			ps.setString(7, model.getPhoneNumber());
			ps.setString(8, model.getCallOutcome());
			ps.setString(9, model.getMenuOption());
			ps.setTimestamp(10, new java.sql.Timestamp(System.currentTimeMillis()));
			ps.execute();
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage(), sqle);			
		} finally {
			try {
				if (conn != null) 				
					conn.close();
				if(ps != null)
					ps.close();
			} catch (SQLException sqle) {
				LOGGER.debug("Error while cleaning:", sqle);
			}
		}
		
	}
	
	/*"select DISTINCT A.sale_id, A.status, A.requested_date, A.amount, A.action_date,A.last_name,A.first_name from "+
			"(select  s.id as sale_id, s.status, sa.requested_date, sa.amount, sa.action_date, ci.last_name, ci.first_name from cust.sale s, "+ 
			"cust.salesaction sa,cust.customerinfo ci, cust.paymentinfo pi  where s.status='CAN' "+ 
			"and s.id=sa.sale_id and SA.ACTION_TYPE IN ('CRO','MOD') and S.CROMOD_DATE=SA.ACTION_DATE "+ 
			" AND sa.requested_date =TO_DATE(?, 'YYYY-MM-DD') and s.customer_id=ci.customer_id and PI.SALESACTION_ID=sa.id and PI.PAYMENT_METHOD_TYPE='CC' "+ 
			"and PI.ON_FD_ACCOUNT='R' ) A, "+
			"cust.salesaction sa1, cust.payment p "+
			"where A.sale_id=sa1.sale_id and sa1.id=P.SALESACTION_ID and sa1.action_type='AUT' "+
			"and  exists (select 1 from MIS.GATEWAY_ACTIVITY_LOG gal where P.GATEWAY_ORDER=GAL.ORDER_ID and transaction_type='AUTHORIZE' and GAL.IS_APPROVED='Y' )";
	*/
	private static final String REVERSE_AUTH_ORDERS_QUERY_BY_DATE =
	"select DISTINCT A.sale_id, A.status,  A.amount, A.action_date,A.last_name||',' ||A.first_name as CUSTOMER_NAME, A.e_store from "+
    " (select  s.id as sale_id, s.status, sa.requested_date, sa.amount, sa.action_date, ci.last_name, ci.first_name,s.e_store from cust.sale s, "+
    " cust.salesaction sa,cust.customerinfo ci, cust.paymentinfo pi  where s.status='CAN' "+
    " and s.id=sa.sale_id and SA.ACTION_TYPE IN ('CRO','MOD') and S.CROMOD_DATE=SA.ACTION_DATE "+
    " AND sa.requested_date =TO_DATE(?, 'YYYY-MM-DD') and s.customer_id=ci.customer_id and PI.SALESACTION_ID=sa.id and PI.PAYMENT_METHOD_TYPE='CC' "+
    " and PI.ON_FD_ACCOUNT='R' ) A, "+
    " cust.salesaction sa1, cust.payment p "+
    " where A.sale_id=sa1.sale_id and sa1.id=P.SALESACTION_ID and sa1.action_type='AUT' "+
    " and  exists (select 1 from MIS.GATEWAY_ACTIVITY_LOG gal where P.GATEWAY_ORDER=GAL.ORDER_ID and transaction_type='AUTHORIZE' and GAL.IS_APPROVED='Y' ) "+
    " UNION "+
    " select GAL.ORDER_ID SALE_ID, 'N/A', GAL.AMOUNT, GAL.TRANSACTION_TIME ACTION_DATE,GAL.CUSTOMER_NAME,GAL.E_STORE from MIS.GATEWAY_ACTIVITY_LOG gal where GAL.TRANSACTION_TIME between TO_DATE(?, 'YYYY-MM-DD')  and  "+
    " TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS') and transaction_type='AUTHORIZE' and GAL.IS_APPROVED='Y' and GAL.IS_AVS_MATCH!='Y' ";
	
	public List<FDCustomerOrderInfo> getReverseAuthOrders(String date) throws FDResourceException {
		Connection conn = null;
		if(StringUtils.isEmpty(date))
			throw new FDResourceException("Please pass a valid date");
		try {
			System.out.println(REVERSE_AUTH_ORDERS_QUERY_BY_DATE);
			conn = this.getConnection();
			List lst = new ArrayList();
			PreparedStatement ps =null;
			
				ps= conn.prepareStatement(REVERSE_AUTH_ORDERS_QUERY_BY_DATE);
				ps.setString(1,date);
				ps.setString(2,date);
				ps.setString(3,date+" 23:59:59");
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				FDCustomerOrderInfo info = new FDCustomerOrderInfo();
				info.setSaleId(rs.getString("SALE_ID"));
				//info.setDeliveryDate(rs.getDate("REQUESTED_DATE"));
				info.setOrderStatus(EnumSaleStatus.CANCELED);
				info.setAmount(rs.getDouble("AMOUNT"));
				info.setFirstName(rs.getString("CUSTOMER_NAME"));
				info.setLastCroModDate(rs.getTimestamp("ACTION_DATE"));
				info.seteStore(rs.getString("E_STORE"));
				lst.add(info);
}
			rs.close();
			ps.close();

			return lst;
		} catch (SQLException sqle) {
			LOGGER.error(sqle.getMessage());
			throw new FDResourceException(sqle);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqle) {
					LOGGER.debug("Error while cleaning:", sqle);
				}
			}
		}
	}	
	
	private static final String VOID_CAPTURE_ORDERS_QUERY_BY_DATE ="select DISTINCT A.sale_id, A.status, A.requested_date, A.amount, A.action_date,A.last_name,A.first_name from "+
            " (select  s.id as sale_id, s.status, sa.requested_date, sa.amount, sa.action_date, ci.last_name, ci.first_name from cust.sale s, "+ 
            " cust.salesaction sa,cust.customerinfo ci, cust.paymentinfo pi  where s.status='PPG' "+ 
            " and s.id=sa.sale_id and SA.ACTION_TYPE IN ('CRO','MOD') and S.CROMOD_DATE=SA.ACTION_DATE "+ 
            " AND sa.requested_date =TO_DATE(?, 'YYYY-MM-DD') and s.customer_id=ci.customer_id and PI.SALESACTION_ID=sa.id and PI.PAYMENT_METHOD_TYPE IN ('EC','CC' ) "+
            " and PI.ON_FD_ACCOUNT='R' ) A, "+
            " cust.salesaction sa1, cust.payment p "+
            " where A.sale_id=sa1.sale_id and sa1.id=P.SALESACTION_ID and sa1.action_type='CAP' "+
            " and  exists (select 1 from MIS.GATEWAY_ACTIVITY_LOG gal where P.GATEWAY_ORDER=GAL.ORDER_ID and transaction_type='CAPTURE' and GAL.IS_APPROVED='Y' ) ";
			
			public List<FDCustomerOrderInfo> getOrdersForVoidCapture(String date) throws FDResourceException {
				Connection conn = null;
				if(StringUtils.isEmpty(date))
					throw new FDResourceException("Please pass a valid date");
				try {
					conn = this.getConnection();
					List lst = new ArrayList();
					PreparedStatement ps =null;
					
						ps= conn.prepareStatement(VOID_CAPTURE_ORDERS_QUERY_BY_DATE);
						ps.setString(1,date);
						//ps.setString(2,date);
					
					ResultSet rs = ps.executeQuery();
					while(rs.next()){
						FDCustomerOrderInfo info = new FDCustomerOrderInfo();
						info.setSaleId(rs.getString("SALE_ID"));
						info.setDeliveryDate(rs.getDate("REQUESTED_DATE"));
						info.setOrderStatus(EnumSaleStatus.getSaleStatus(rs.getString("STATUS")));
						info.setAmount(rs.getDouble("AMOUNT"));
						info.setFirstName(rs.getString("FIRST_NAME"));
						info.setLastName(rs.getString("LAST_NAME"));
						info.setLastCroModDate(rs.getTimestamp("ACTION_DATE"));
						lst.add(info);
					}
					rs.close();
					ps.close();

					return lst;
				} catch (SQLException sqle) {
					LOGGER.error(sqle.getMessage());
					throw new FDResourceException(sqle);
				} finally {
					if (conn != null) {
						try {
							conn.close();
						} catch (SQLException sqle) {
							LOGGER.debug("Error while cleaning:", sqle);
						}
					}
				}
			}

            private String TxFOR_REVERSE_AUTH="select * from MIS.GATEWAY_ACTIVITY_LOG gal where GAL.ORDER_ID=? and transaction_type='AUTHORIZE' and GAL.IS_APPROVED='Y' and GAL.IS_AVS_MATCH!='Y'";
			public void reverseAuthOrder(String saleId) throws RemoteException, FDResourceException, ErpTransactionException {
				
				if(StringUtils.isEmpty(saleId))
					return ;
				Gateway gateway=GatewayFactory.getGateway(GatewayType.PAYMENTECH);
				 try {
					 if(saleId.indexOf("X")<0) {
		              ErpCustomerManagerSB customerManagerSB = (ErpCustomerManagerSB) this.getErpCustomerManagerHome().create();
		              ErpSaleModel _order=customerManagerSB.getOrder(new PrimaryKey(saleId));
		              if(_order==null) return ;
		              if(EnumSaleStatus.CANCELED.equals(_order.getStatus())) {
		            	  
			              ErpAbstractOrderModel order =_order.getCurrentOrder();
			              List<ErpAuthorizationModel> auths= _order.getAuthorizations();
			              for (ErpAuthorizationModel auth : auths) {
			            	  
			          		if(!StringUtils.isEmpty(auth.getAuthCode())) {
			          			Request _request=RequestFactory.getRequest(TransactionType.REVERSE_AUTHORIZE);
					              BillingInfo billinginfo=null;
					              
					              billinginfo=BillingInfoFactory.getBillingInfo(Merchant.valueOf(auth.getMerchantId()),GatewayAdapter.getCreditCardModel(order.getPaymentMethod()));
					              billinginfo.setTransactionID(auth.getGatewayOrderID());
					              billinginfo.getPaymentMethod().setCustomerID(_order.getCustomerPk().getId());
					              billinginfo.setTransactionRefIndex(auth.getTrasactionRefIndex());
					              billinginfo.setTransactionRef(auth.getSequenceNumber());
					              billinginfo.setAmount(auth.getAmount());
					              _request.setBillingInfo(billinginfo);
					              Response _response=gateway.reverseAuthorize(_request);
					              if (!_response.isSuccess())
					              	throw new ErpTransactionException(_response.getStatusMessage()) ;
			          		}
			          	  }
			  			  
			              
		              }
					 } else {
						 Connection conn = null;
						 Request _request=null;
						 try {
								conn = this.getConnection();
								List lst = new ArrayList();
								PreparedStatement ps =null;
		              
									ps= conn.prepareStatement(TxFOR_REVERSE_AUTH);
									ps.setString(1,saleId);
									//ps.setString(2,date);
		              
								ResultSet rs = ps.executeQuery();
								if(rs.next()){
									_request=RequestFactory.getRequest(TransactionType.REVERSE_AUTHORIZE);
						              BillingInfo billinginfo=null;
						              
						              billinginfo=BillingInfoFactory.getBillingInfo(Merchant.valueOf(rs.getString("MERCHANT")),PaymentMethodFactory.getCreditCard());
						              billinginfo.setTransactionID(saleId);
						              billinginfo.getPaymentMethod().setCustomerID(rs.getString("CUSTOMER_ID"));
						              billinginfo.setTransactionRefIndex(rs.getString("TX_REF_IDX"));
						              billinginfo.setTransactionRef(rs.getString("TX_REF_NUM"));
						              billinginfo.setAmount(rs.getDouble("AMOUNT"));
						              _request.setBillingInfo(billinginfo);
						              
								}
								rs.close();
								ps.close();
								if(_request!=null) {
									Response _response=gateway.reverseAuthorize(_request);
						              if (!_response.isSuccess())
						              	throw new ErpTransactionException(_response.getStatusMessage()) ;
								}
								
							} catch (SQLException sqle) {
								LOGGER.error(sqle.getMessage());
								throw new FDResourceException(sqle);
							} finally {
								if (conn != null) {
									try {
										conn.close();
									} catch (SQLException sqle) {
										LOGGER.debug("Error while cleaning:", sqle);
									}
								}
							}
						 
					 }
		              
		              
		        } catch (CreateException ce) {
		              throw new FDResourceException(ce);
		        } catch (RemoteException re) {
		              throw new FDResourceException(re);
		        } 
			}
			
			/*select A.sale_id, A.status, A.requested_date, A.amount, A.action_date,A.last_name,A.first_name from
			(select  s.id as sale_id, s.status, sa.requested_date, sa.amount, sa.action_date, ci.last_name, ci.first_name from cust.sale s, 
			cust.salesaction sa,cust.customerinfo ci, cust.paymentinfo pi  where s.status='CAN' 
			and s.id=sa.sale_id and SA.ACTION_TYPE IN ('CRO','MOD') and S.CROMOD_DATE=SA.ACTION_DATE 
			AND sa.requested_date =TO_DATE('2013-07-27', 'YYYY-MM-DD') and s.customer_id=ci.customer_id and PI.SALESACTION_ID=sa.id and PI.PAYMENT_METHOD_TYPE='CC' 
			and PI.ON_FD_ACCOUNT='R' ) A,
			cust.salesaction sa1, cust.payment p
			where A.sale_id=sa1.sale_id and sa1.id=P.SALESACTION_ID and sa1.action_type='AUT'
			and  exists (select 1 from MIS.GATEWAY_ACTIVITY_LOG gal where P.GATEWAY_ORDER=GAL.ORDER_ID and transaction_type='AUTHORIZE' and GAL.IS_APPROVED='Y' )*/ 
			
			public void voidCaptureOrder(String saleId) throws RemoteException, FDResourceException, ErpTransactionException {
				

				if(StringUtils.isEmpty(saleId))
					return;
				Paymentech gateway=(Paymentech)GatewayFactory.getGateway(GatewayType.PAYMENTECH);
				 try {
		              ErpCustomerManagerSB customerManagerSB = (ErpCustomerManagerSB) this.getErpCustomerManagerHome().create();
		              ErpSaleModel _order=customerManagerSB.getOrder(new PrimaryKey(saleId));
		              if(_order==null) return;
		              if(EnumSaleStatus.PAYMENT_PENDING.equals(_order.getStatus())) {
		            	  
			              ErpAbstractOrderModel order =_order.getCurrentOrder();
			              List<ErpCaptureModel> captures= _order.getCaptures();
			              for (ErpCaptureModel capture : captures) {
			            	
			          			Request _request=RequestFactory.getRequest(TransactionType.VOID_CAPTURE);
					              BillingInfo billinginfo=null;
					              
					              if(EnumPaymentMethodType.CREDITCARD.equals(order.getPaymentMethod().getPaymentMethodType())) {
					              
					      			billinginfo=BillingInfoFactory.getBillingInfo(Merchant.valueOf(capture.getMerchantId()), GatewayAdapter.getCreditCardModel(order.getPaymentMethod()));
					      		  }
					      		  else if(EnumPaymentMethodType.ECHECK.equals(order.getPaymentMethod().getPaymentMethodType())) {
					      			billinginfo=BillingInfoFactory.getBillingInfo(Merchant.valueOf(capture.getMerchantId()), GatewayAdapter.getECheckModel(order.getPaymentMethod()));
					      		  } else
					      			  return;

					              billinginfo.setTransactionID(capture.getGatewayOrderID());
					              billinginfo.setTransactionRefIndex(capture.getTrasactionRefIndex());
					              billinginfo.setTransactionRef(capture.getSequenceNumber());
					              billinginfo.setAmount(capture.getAmount());
					              billinginfo.getPaymentMethod().setCustomerID(_order.getCustomerPk().getId());
					              
					              _request.setBillingInfo(billinginfo);
					              Response _response=gateway.voidCapture(_request);
					              if (!_response.isSuccess())
						              	throw new ErpTransactionException(_response.getStatusMessage());
					              ErpVoidCaptureModel voidCapture=GatewayAdapter.getVoidCaptureResponse(_response, capture);
					              _order.addVoidCapture(voidCapture);
			          		
			          	  }
			  			  
			              
		              }
		              
		        } catch (CreateException ce) {
		              throw new FDResourceException(ce);
		        } catch (RemoteException re) {
		              throw new FDResourceException(re);
		        } catch ( PaylinxResourceException pre) {
		        	throw new FDResourceException(pre);
		        }
			}
			
}