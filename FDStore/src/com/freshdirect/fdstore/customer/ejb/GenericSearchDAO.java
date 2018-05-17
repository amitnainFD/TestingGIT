package com.freshdirect.fdstore.customer.ejb;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;

import com.freshdirect.common.address.PhoneNumber;
import com.freshdirect.crm.ejb.CriteriaBuilder;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.delivery.model.RestrictedAddressModel;
import com.freshdirect.delivery.restriction.AlcoholRestriction;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionCriterion;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionReason;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionType;
import com.freshdirect.delivery.restriction.OneTimeRestriction;
import com.freshdirect.delivery.restriction.OneTimeReverseRestriction;
import com.freshdirect.delivery.restriction.RecurringRestriction;
import com.freshdirect.fdlogistics.model.EnumRestrictedAddressReason;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.BulkModifyOrderInfo;
import com.freshdirect.fdstore.customer.FDBrokenAccountInfo;
import com.freshdirect.fdstore.customer.FDCustomerOrderInfo;
import com.freshdirect.fdstore.customer.FDCustomerReservationInfo;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDUserCouponUtil;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.EnumSearchType;
import com.freshdirect.framework.util.GenericSearchCriteria;
import com.freshdirect.framework.util.NVL;
import com.freshdirect.framework.util.TimeOfDay;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.logistics.controller.data.request.SearchRequest;
import com.freshdirect.logistics.delivery.model.EnumReservationStatus;
import com.freshdirect.logistics.delivery.model.EnumReservationType;
import com.freshdirect.payment.SettlementBatchInfo;

public class GenericSearchDAO {
	
	//private static Map queryMap = new HashMap();
	//.put(EnumSearchType.COMPANY_SEARCH.getName(), CUSTOMER_QUERY);
	private static Category LOGGER = LoggerFactory.getInstance(GenericSearchDAO.class);
	
	public static List genericSearch(Connection conn, GenericSearchCriteria criteria) throws SQLException, FDResourceException {
		List searchResults = null;
		if(criteria == null || criteria.isBlank()){
			return Collections.emptyList();
		} else if(EnumSearchType.COMPANY_SEARCH.equals(criteria.getSearchType())){
			CriteriaBuilder builder = buildSQLFromCriteria(criteria);
			searchResults = findCustomersByCriteria(conn, criteria, builder);
		} else if(EnumSearchType.EXEC_SUMMARY_SEARCH.equals(criteria.getSearchType())){
			searchResults = orderSummaryByDate(conn, criteria);
		} else if(EnumSearchType.RESERVATION_SEARCH.equals(criteria.getSearchType())){
			//Fetch uncommitted pre reservations based on given criteria.
			searchResults = findReservationsByCriteria(conn, criteria);
			
		} else if(EnumSearchType.ORDERS_BY_RESV_SEARCH.equals(criteria.getSearchType())){			
			//Fetch the orders that has standard and pre reservations based on the given criteria.
			CriteriaBuilder builder = buildOrderSearchForResv(criteria);
			searchResults = findOrderForResvByCriteria(conn,criteria, builder);
		}else if(EnumSearchType.BROKEN_ACCOUNT_SEARCH.equals(criteria.getSearchType())){
			searchResults = findBrokenAccounts(conn);
		}else if(EnumSearchType.CANCEL_ORDER_SEARCH.equals(criteria.getSearchType())){
			CriteriaBuilder builder = buildOrderSearchForResv(criteria);
			//Fetch the orders for cancellations based on the given criteria.
			searchResults = findOrderForResvByCriteria(conn,criteria, builder);
		}else if(EnumSearchType.RETURN_ORDER_SEARCH.equals(criteria.getSearchType())){
			CriteriaBuilder builder = buildSQLFromCriteria(criteria);
			searchResults = findOrderForReturnsByCriteria(conn, criteria, builder);
		}else if(EnumSearchType.SETTLEMENT_BATCH_SEARCH.equals(criteria.getSearchType())){
			searchResults = findFailedSettlementBatch(conn);
		}
		else if(EnumSearchType.DEL_RESTRICTION_SEARCH.equals(criteria.getSearchType())||EnumSearchType.PLATTER_RESTRICTION_SEARCH.equals(criteria.getSearchType())){
			CriteriaBuilder builder = buildSQLFromCriteria(criteria);
			searchResults = processDeliveryRestriction(conn, criteria, builder);
		}
		else if(EnumSearchType.ALCOHOL_RESTRICTION_SEARCH.equals(criteria.getSearchType())){
			CriteriaBuilder builder = buildSQLFromCriteria(criteria);
			searchResults = processAlcoholRestriction(conn, criteria, builder);
		}		
		else if(EnumSearchType.ADDR_RESTRICTION_SEARCH.equals(criteria.getSearchType())){
			CriteriaBuilder builder = buildSQLFromCriteria(criteria);
			searchResults = processAddressRestriction(conn, criteria, builder);
		}
		else if(EnumSearchType.ORDER_SEARCH_BY_SKUS.equals(criteria.getSearchType())){
			CriteriaBuilder builder = buildSQLFromCriteria(criteria);
			searchResults = findOrderForSkusByCriteria(conn, criteria, builder);
		}
		else if(EnumSearchType.GET_ORDERS_TO_MODIFY.equals(criteria.getSearchType())){
			CriteriaBuilder builder = buildSQLFromCriteria(criteria);
			searchResults =getOrdersToModify(conn, criteria, builder);
		}
		
		return searchResults;
		
	}

	private static List findReservationsByCriteria(
			Connection conn, GenericSearchCriteria criteria) throws FDResourceException {
		
		try{
			
			List<String> types = new ArrayList<String>();
			types.add("WRR");
			types.add("OTR");
			criteria.setCriteriaMap("types", types);
			criteria.setCriteriaMap("statusCode", EnumReservationStatus.RESERVED.getCode());
			if(criteria.getCriteriaMap().get("zoneArray")!=null){
				String[] zoneArray = (String[])criteria.getCriteriaMap().get("zoneArray");
				List<String> zones = Arrays.asList(zoneArray);
				criteria.getCriteriaMap().put("zoneArray", zones);
			}
			List<FDReservation> reservations = FDDeliveryManager.getInstance().getReservationsByCriteria(
					new SearchRequest(criteria.getSearchType().getName(), criteria.getCriteriaMap()));
			
			if(reservations == null || reservations.isEmpty()) {
				List<FDCustomerReservationInfo> rsvInfo = new ArrayList<FDCustomerReservationInfo>();
				return rsvInfo;
				}
			
			Set<String> customerIds = new HashSet<String>();
			Map<String, FDCustomerReservationInfo> custMap = new HashMap<String, FDCustomerReservationInfo>();
			for(FDReservation rsv: reservations){
				customerIds.add(rsv.getCustomerId());
			}
			List<FDCustomerReservationInfo> customers = findCustomersById(conn, customerIds);
			for(FDCustomerReservationInfo cust: customers){
				custMap.put(cust.getIdentity().getErpCustomerPK(), cust);
			}		
			
			return addReservationInfo(reservations, custMap);
		}catch(SQLException e){
			throw new FDResourceException(e);
		}
		
	}

	private static List addReservationInfo(List<FDReservation> reservations,
			Map<String, FDCustomerReservationInfo> custMap) {
		
		List<FDCustomerReservationInfo> rsvInfo = new ArrayList<FDCustomerReservationInfo>();
		for(FDReservation rsv: reservations){
			FDCustomerReservationInfo info = custMap.get(rsv.getCustomerId());
			if(info==null){
				rsvInfo.add(new FDCustomerReservationInfo(rsv.getId(), rsv.getDeliveryDate(), rsv.getCutoffTime(), 
						"", "", new FDIdentity(rsv.getCustomerId()), "", 
						"", "", "",
						rsv.getStartTime(), rsv.getEndTime(), rsv.getZoneCode(), rsv.getType()));
			}
			else{
				rsvInfo.add(new FDCustomerReservationInfo(rsv.getId(), rsv.getDeliveryDate(), rsv.getCutoffTime(), 
						info.getFirstName(), info.getLastName(), info.getIdentity(), info.getEmail(), 
						info.getPhone(), info.getAltPhone(), info.getBusinessPhone(),
						rsv.getStartTime(), rsv.getEndTime(), rsv.getZoneCode(), rsv.getType()));
			}
			
		}
		
		return rsvInfo;
	}

	private static CriteriaBuilder buildSQLFromCriteria(GenericSearchCriteria criteria) {
		CriteriaBuilder builder = new CriteriaBuilder();
		if(criteria.getCriteriaMap()!=null && !criteria.getCriteriaMap().isEmpty()){
			if(EnumSearchType.COMPANY_SEARCH.equals(criteria.getSearchType())){
				builder.addSql(" lower(a.company_name) = ?", 
						new Object[] { 
							criteria.getCriteriaMap().get("companyName").toString() });
			}
			else if(EnumSearchType.RESERVATION_SEARCH.equals(criteria.getSearchType())){
				buildReservationSearch(criteria, builder);
			} else if(EnumSearchType.RETURN_ORDER_SEARCH.equals(criteria.getSearchType())){
				buildOrderSearchForReturns(criteria, builder);
			}
			else if(EnumSearchType.DEL_RESTRICTION_SEARCH.equals(criteria.getSearchType())||(EnumSearchType.PLATTER_RESTRICTION_SEARCH.equals(criteria.getSearchType()))){
				buildDeliveryRestrictionDays(criteria, builder);
			}
			else if(EnumSearchType.ALCOHOL_RESTRICTION_SEARCH.equals(criteria.getSearchType())){
				buildAlcoholRestrictionCriteria(criteria, builder);
			}
			else if(EnumSearchType.ADDR_RESTRICTION_SEARCH.equals(criteria.getSearchType())){
				buildAddressRestrictionCriteria(criteria, builder);
			}
			else if(EnumSearchType.ORDER_SEARCH_BY_SKUS.equals(criteria.getSearchType())){
				buildOrderSearchBySkus(criteria, builder);
			}
			else if(EnumSearchType.GET_ORDERS_TO_MODIFY.equals(criteria.getSearchType())){
				buildStatuses(criteria, builder);
			}			
		}
		return builder;
	}

	private static CriteriaBuilder buildOrderSearchBySkus(GenericSearchCriteria criteria, CriteriaBuilder builder) {
		Object skuArray = criteria.getCriteriaMap().get("skuArray");
		if(skuArray != null){
			builder.addInString("OL.SKU_CODE", (String[])skuArray);	
		}
		java.util.Date fromDlvDate = (java.util.Date) criteria.getCriteriaMap().get("baseDate");
		if(fromDlvDate != null){
			builder.addSql("SA.REQUESTED_DATE >= ?", 
						new Object[] {new Date(fromDlvDate.getTime())});
			java.util.Date toDlvDate = (java.util.Date) criteria.getCriteriaMap().get("toBaseDate");
			if(toDlvDate != null){
				builder.addSql("SA.REQUESTED_DATE <= ?", 
							new Object[] {new Date(toDlvDate.getTime())});
			}
		} else {
			//default to SA.REQUESTED_DATE >= trunc(sysdate + 1)
			builder.addSql("SA.REQUESTED_DATE >= ?", 
					new Object[] {"trunc(sysdate + 1)"});
			
		}
		return builder;
	}

	private static CriteriaBuilder buildStatuses(GenericSearchCriteria criteria, CriteriaBuilder builder) {
		Object statuses = criteria.getCriteriaMap().get("statuses");
		if(statuses != null){
			builder.addInString("M.STATUS", (String[])statuses);	
		}
		return builder;
	}
	
	private static void buildReservationSearch(GenericSearchCriteria criteria, CriteriaBuilder builder) {
		java.util.Date baseDate = (java.util.Date) criteria.getCriteriaMap().get("baseDate");
		builder.addSql("ts.base_date = ?", 
					new Object[] {new Date(baseDate.getTime())});
		Object cutoffTime = criteria.getCriteriaMap().get("cutoffTime");
		if(cutoffTime != null){
			builder.addSql("to_date(to_char(ts.cutoff_time, 'HH:MI AM'),'HH:MI AM') = to_date(?,'HH:MI AM')", 
					new Object[] { 
					cutoffTime.toString() });
		}
		Object zoneArray = criteria.getCriteriaMap().get("zoneArray");
		if(zoneArray != null){
			builder.addInString("ze.zone_code", (String[])zoneArray);	
		}
		Object startTime = criteria.getCriteriaMap().get("startTime");
		if(startTime != null){
			builder.addSql("to_date(to_char(ts.start_time, 'HH:MI AM'),'HH:MI AM') >= to_date(?,'HH:MI AM')", 
					new Object[] {startTime.toString() });
		}
		Object endTime = criteria.getCriteriaMap().get("endTime");
		if(endTime != null){
			builder.addSql("to_date(to_char(ts.end_time, 'HH:MI AM'),'HH:MI AM') <= to_date(?,'HH:MI AM')", 
					new Object[] {endTime.toString() });
		}
	}

	private static CriteriaBuilder buildOrderSearchForResv(GenericSearchCriteria criteria) {
		CriteriaBuilder builder = new CriteriaBuilder();
		java.util.Date baseDate = (java.util.Date) criteria.getCriteriaMap().get("baseDate");
		builder.addSql("sa.requested_date = ?", 
					new Object[] {new Date(baseDate.getTime())});
		Object cutoffTime = criteria.getCriteriaMap().get("cutoffTime");
		if(cutoffTime != null){
			builder.addSql("to_date(to_char(di.cutofftime, 'HH:MI AM'),'HH:MI AM') = to_date(?,'HH:MI AM')", 
					new Object[] { 
					cutoffTime.toString() });
		}
		Object zoneArray = criteria.getCriteriaMap().get("zoneArray");
		if(zoneArray != null){
			builder.addInString("di.zone", (String[])zoneArray);	
		}
		Object startTime = criteria.getCriteriaMap().get("startTime");
		if(startTime != null){
			builder.addSql("to_date(to_char(di.starttime, 'HH:MI AM'),'HH:MI AM') >= to_date(?,'HH:MI AM')", 
					new Object[] {startTime.toString() });
		}
		Object endTime = criteria.getCriteriaMap().get("endTime");
		if(endTime != null){
			builder.addSql("to_date(to_char(di.endtime, 'HH:MI AM'),'HH:MI AM') <= to_date(?,'HH:MI AM')", 
					new Object[] {endTime.toString() });
		}
		return builder;
	}
	
	private static CriteriaBuilder buildOrderSearchForReturns(GenericSearchCriteria criteria, CriteriaBuilder builder) {
		java.util.Date baseDate = (java.util.Date) criteria.getCriteriaMap().get("baseDate");
		builder.addSql("sa.requested_date = ?", 
					new Object[] {new Date(baseDate.getTime())});
		Object zoneArray = criteria.getCriteriaMap().get("zoneArray");
		if(zoneArray != null){
			builder.addInString("di.zone", (String[])zoneArray);	
		}
		Object startTime = criteria.getCriteriaMap().get("startTime");
		if(startTime != null){
			
			builder.addSql("to_date(to_char(di.starttime, 'HH:MI AM'),'HH:MI AM') >= to_date(?,'HH:MI AM')", 
					new Object[] {startTime.toString() });
		}
		Object endTime = criteria.getCriteriaMap().get("endTime");
		if(endTime != null){
			builder.addSql("to_date(to_char(di.endtime, 'HH:MI AM'),'HH:MI AM') <= to_date(?,'HH:MI AM')", 
					new Object[] {endTime.toString() });
		}
		Object fromWaveNum = criteria.getCriteriaMap().get("fromWaveNumber");
		Object toWaveNum = criteria.getCriteriaMap().get("toWaveNumber");
		if(fromWaveNum != null && toWaveNum != null){
			//Searching for a range of wave numbers.
			builder.addSql("s.wave_number between to_number(?) and to_number(?)", 
					new Object[] {fromWaveNum.toString(), toWaveNum.toString()});
		}else if(fromWaveNum != null && toWaveNum == null){
			//Searching for a specific wave number.
			builder.addSql("s.wave_number = to_number(?)", 
					new Object[] {fromWaveNum.toString()});

		}
		Object fromTruckNum = criteria.getCriteriaMap().get("fromTruckNumber");
		Object toTruckNum = criteria.getCriteriaMap().get("toTruckNumber");
		if(fromTruckNum != null && toTruckNum != null){
			//Searching for a range of truck numbers.
			builder.addSql("s.truck_number between to_number(?) and to_number(?)", 
					new Object[] {fromTruckNum.toString(), toTruckNum.toString()});
		}else if(fromTruckNum != null && toTruckNum == null){
			//Searching for a specific truck number.
			builder.addSql("s.truck_number = to_number(?)", 
					new Object[] {fromTruckNum.toString()});

		}
		return builder;
	}
	
	private static String CUSTOMER_COMP_QUERY = 
		"select distinct "
		+ " c.id, ci.first_name, ci.last_name, c.user_id, ci.home_phone, ci.business_phone, ci.cell_phone, " 
		+ " (select p.profile_value from cust.profile p where p.customer_id = fc.id and p.profile_name='VIPCustomer') VIP_CUST, " 
		+ " (select p.profile_value from cust.profile p where p.customer_id = fc.id and p.profile_name='ChefsTable') CHEFS_TABLE "
		+ " from cust.customer c, cust.customerinfo ci, cust.fdcustomer fc, cust.address a "  
		+ " where c.id = ci.customer_id and ci.customer_id = fc.erp_customer_id and " 
		+ " fc.erp_customer_id = a.customer_id "; 
	
	public static List findCustomersByCriteria(Connection conn, GenericSearchCriteria criteria, CriteriaBuilder builder) throws SQLException {
		String query = CUSTOMER_COMP_QUERY + " and " + builder.getCriteria();
		PreparedStatement ps = conn.prepareStatement(query);
		Object[] obj = builder.getParams();
		for(int i = 0; i < obj.length; i++) {
			ps.setObject(i+1, obj[i]);
		}
		ResultSet rs = ps.executeQuery();
		List lst = processCustomerResultSet(rs);
		rs.close();
		ps.close();
		return lst;
	}

	private static List processCustomerResultSet(ResultSet rs) throws SQLException {
		List lst = new ArrayList();
		while (rs.next()) {
			FDCustomerOrderInfo oInfo = new FDCustomerOrderInfo();
			oInfo.setIdentity(new FDIdentity(rs.getString("ID")));
			oInfo.setFirstName(rs.getString("FIRST_NAME"));
			oInfo.setLastName(rs.getString("LAST_NAME"));
			oInfo.setEmail(rs.getString("USER_ID"));
			
			oInfo.setPhone(new PhoneNumber(rs.getString("HOME_PHONE")).getPhone());
			String bizPhone = new PhoneNumber(rs.getString("BUSINESS_PHONE")).getPhone();
			String cellPhone = new PhoneNumber(rs.getString("CELL_PHONE")).getPhone();
			
			oInfo.setAltPhone(NVL.apply(cellPhone, ""));
			if("".equals(cellPhone)) {
				oInfo.setAltPhone(NVL.apply(bizPhone, ""));
			}
			
			oInfo.setVip("true".equals(rs.getString("VIP_CUST")));
			oInfo.setChefsTable("1".equals(rs.getString("CHEFS_TABLE")));
						
			lst.add(oInfo);
		}
		return lst;
	}

	
	private static String EXEC_SUMMARY_QUERY = 
		"select "
		+ " sales, total_promotions, total_orders, avg_order_size, avg_promotion, "
		+ " promotion_count,(promotion_count/total_orders) as promotion_percentage " 
		+ " from ( select "
		+ " status, sum(amount) as sales, sum(promotion_amt) as total_promotions, count(*) as total_orders, " 
		+ " sum(amount)/count(*) as avg_order_size, sum(promotion_amt)/count(*) as Avg_promotion, "
		+ " (select count(*) from cust.sale s, cust.salesaction sa, cust.deliveryinfo di, cust.discountline dl "
		+ " where s.id = sa.sale_id and sa.id = dl.salesaction_id and di.salesaction_id = dl.salesaction_id and " 
		+ " sa.action_date = (select /*USE_NL (salesaction) */ max(action_date) from cust.salesaction where action_type in ('CRO', 'MOD') and sale_id = sa.sale_id) and " 
		+ " sa.requested_date = ? and "
		+ " di.starttime >= ? and di.endtime < ? and "
		+ " s.status not in ('NSM','AUF','CAN') "
		+ " ) as promotion_count from (  " 
		+ " select /*+ INDEX (s pk_sale) INDEX (sa pk_salesaction) USE_NL (s sa) */ "
		+ " 'OK' as status,sa.action_date, sa.amount, (select sum(PROMOTION_AMT) from cust.discountline dl where dl.salesaction_id = sa.id) as promotion_amt " 
		+ " from cust.sale s, cust.salesaction sa, cust.deliveryinfo di " 
		+ " where s.id = sa.sale_id and sa.id = di.salesaction_id and " 
		+ " sa.action_date = (select /*+ USE_NL (salesaction) */ max(action_date) from cust.salesaction where action_type in ('CRO', 'MOD') and sale_id = sa.sale_id) and " 
		+ " sa.requested_date = ? AND "
		+ " di.starttime >= ? AND di.endtime < ? and s.status not in ('NSM','AUF','CAN') "
		+ " ) group by status )";
	
	public static List orderSummaryByDate(Connection conn, GenericSearchCriteria criteria) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(EXEC_SUMMARY_QUERY);
		Date startDate = new Date(((java.util.Date)criteria.getCriteriaMap().get("summaryDate")).getTime());
		Date endDate = new Date(DateUtil.addDays(startDate, 1).getTime());

		ps.setDate(1, startDate);
		ps.setDate(2, startDate);
		ps.setDate(3, endDate);
		ps.setDate(4, startDate);
		ps.setDate(5, startDate);
		ps.setDate(6, endDate);
		
		ResultSet rs = ps.executeQuery();
		List lst = processOrderSummaryResultSet(rs);
		rs.close();
		ps.close();
		return lst;
		
	}
	
	public static List processOrderSummaryResultSet(ResultSet rs) throws SQLException {
		List lst =  new ArrayList();
		while(rs.next()) {
			Map m = new HashMap();
			m.put("sales", new Double(rs.getDouble("sales")));
			m.put("Total Orders", new Integer(rs.getInt("TOTAL_ORDERS")));
			m.put("Average Order Size", new Double(rs.getDouble("AVG_ORDER_SIZE")));
			m.put("Total Promotions", new Double(rs.getDouble("TOTAL_PROMOTIONS")));
			m.put("Average Promotion", new Double(rs.getDouble("AVG_PROMOTION")));
			m.put("Promotion Count", new Integer(rs.getInt("PROMOTION_COUNT")));
			m.put("Promotion Percentage", new Double(rs.getDouble("PROMOTION_PERCENTAGE")));
			lst.add(m);
			
		}
		return lst;
	}
	
	private static String CUSTOMER_SEARCH_QUERY = 
			"SELECT "
			+ "ci.customer_id, ci.first_name, ci.last_name, c.user_id, ci.home_phone, ci.business_phone, "
			+ "ci.cell_phone "
			+ "from cust.customerinfo ci, cust.customer c "
			+ "where ci.customer_id = c.id and c.id in (";
	
	public static List<FDCustomerReservationInfo> findCustomersById(Connection conn, Set<String> customerIds) throws SQLException {
		StringBuffer updateQ = new StringBuffer();
		if(customerIds != null && customerIds.size() > 0) {			
			updateQ.append(CUSTOMER_SEARCH_QUERY);
			int intCount = 0;
			for(String rsvId : customerIds) {
				updateQ.append("'").append(rsvId).append("'");
				intCount++;
				if(intCount != customerIds.size()) {
					updateQ.append(",");
				}
			}
			updateQ.append(")");
		}
		PreparedStatement ps = conn.prepareStatement(updateQ.toString());
		ResultSet rs = ps.executeQuery();
		List<FDCustomerReservationInfo> lst = processReservationResultSet(rs);
		rs.close();
		ps.close();
		return lst;

	}
	
	private static List<FDCustomerReservationInfo> processReservationResultSet(ResultSet rs) throws SQLException {
		List<FDCustomerReservationInfo> lst = new ArrayList<FDCustomerReservationInfo>();
		while (rs.next()) {
			String firstName = rs.getString("FIRST_NAME");
			String lastName = rs.getString("LAST_NAME");
			FDIdentity identity  = new FDIdentity(rs.getString("CUSTOMER_ID"));
			String email = rs.getString("USER_ID");
			String phone =  new PhoneNumber(rs.getString("HOME_PHONE")).getPhone();
			String cellPhone = new PhoneNumber(rs.getString("CELL_PHONE")).getPhone();
			String altPhone = NVL.apply(cellPhone, "");
			String bizPhone = NVL.apply(new PhoneNumber(rs.getString("BUSINESS_PHONE")).getPhone(), "");
			if("".equals(cellPhone)) {
				altPhone = NVL.apply(bizPhone, "");
			}

      	FDCustomerReservationInfo rInfo = new FDCustomerReservationInfo(firstName, lastName, identity, email,phone, altPhone, bizPhone);
			lst.add(rInfo);
		}
		return lst;
	}
	
	private static String RESERVATION_SEARCHBYID_QUERY = 
		"SELECT "
		+ "ci.customer_id, ci.first_name, ci.last_name, c.user_id, ci.home_phone, ci.business_phone, "
		+ "ci.cell_phone, ts.base_date, ts.start_time, ts.end_time, ts.cutoff_time, ze.zone_code, rs.id, rs.type, rs.address_id  "
		+ "from dlv.reservation rs, dlv.timeslot ts, dlv.zone ze, cust.customerinfo ci, cust.customer c "
		+ "where ts.id = rs.timeslot_id and ze.id = ts.zone_id and rs.customer_id = c.id and ci.customer_id = c.id "
		+ "and rs.id in (";

	public static List<FDCustomerReservationInfo> findReservationsById(Connection conn, Set<String> rsvIds) throws SQLException {
		StringBuffer updateQ = new StringBuffer();
		if(rsvIds != null && rsvIds.size() > 0) {			
			updateQ.append(RESERVATION_SEARCHBYID_QUERY);
			int intCount = 0;
			for(String rsvId : rsvIds) {
				updateQ.append("'").append(rsvId).append("'");
				intCount++;
				if(intCount != rsvIds.size()) {
					updateQ.append(",");
				}
			}
			updateQ.append(")");
		}
		PreparedStatement ps = conn.prepareStatement(updateQ.toString());
		ResultSet rs = ps.executeQuery();
		List<FDCustomerReservationInfo> lst = processReservationResultSet(rs);
		rs.close();
		ps.close();
		return lst;	
	}
	
	private static String ORDER_SEARCH_FOR_RESERVATION = 
		"SELECT "
		+ "c.id customer_id, fdc.id fdc_id, ci.first_name, ci.last_name, c.user_id, ci.home_phone, ci.business_phone, "
		+ "ci.cell_phone, s.id, sa.requested_date, s.status, sa.amount, di.starttime, di.endtime, "
		+ "di.cutofftime, rs.type "
		+ "from cust.customer c, cust.fdcustomer fdc, cust.customerinfo ci, cust.sale s, cust.salesaction sa, cust.deliveryinfo di, dlv.reservation rs "
		+ "where c.id = ci.customer_id and c.id = fdc.erp_customer_id and c.id = s.customer_id and s.id = sa.sale_id and sa.action_type IN ('CRO', 'MOD') "
		+ "and s.type ='REG' "
		+ "and s.status in('SUB','AVE','AUT','AUF') and sa.action_date = "
		+ "(SELECT MAX(action_date) FROM cust.salesaction WHERE sale_id = s.id AND action_type IN ('CRO', 'MOD')) "
		+ "and sa.id = di.salesaction_id and rs.id = di.reservation_id";
	
	
	public static List findOrderForResvByCriteria(Connection conn, GenericSearchCriteria criteria, CriteriaBuilder builder) throws SQLException {
		String query = ORDER_SEARCH_FOR_RESERVATION + " and " + builder.getCriteria();
		PreparedStatement ps = conn.prepareStatement(query);
		Object[] obj = builder.getParams();
		for(int i = 0; i < obj.length; i++) {
			ps.setObject(i+1, obj[i]);
		}
		ResultSet rs = ps.executeQuery();
		List lst = processOrderForResvResultSet(rs);
		rs.close();
		ps.close();
		return lst;

	}
	
	private static List processOrderForResvResultSet(ResultSet rs) throws SQLException {
		List lst = new ArrayList();
		while (rs.next()) {
			FDCustomerOrderInfo oInfo = new FDCustomerOrderInfo();
			oInfo.setIdentity(new FDIdentity(rs.getString("CUSTOMER_ID"), rs.getString("FDC_ID")));
			oInfo.setFirstName(rs.getString("FIRST_NAME"));
			oInfo.setLastName(rs.getString("LAST_NAME"));
			oInfo.setEmail(rs.getString("USER_ID"));
			
			oInfo.setPhone(new PhoneNumber(rs.getString("HOME_PHONE")).getPhone());
			String bizPhone = new PhoneNumber(rs.getString("BUSINESS_PHONE")).getPhone();
			String cellPhone = new PhoneNumber(rs.getString("CELL_PHONE")).getPhone();
			
			oInfo.setAltPhone(NVL.apply(cellPhone, ""));
			if("".equals(cellPhone)) {
				oInfo.setAltPhone(NVL.apply(bizPhone, ""));
			}
			oInfo.setSaleId(rs.getString("ID"));
			oInfo.setDeliveryDate(rs.getDate("REQUESTED_DATE"));
			oInfo.setOrderStatus(EnumSaleStatus.getSaleStatus(rs.getString("STATUS")));
			oInfo.setAmount(rs.getDouble("AMOUNT"));
			oInfo.setStartTime(rs.getTimestamp("STARTTIME"));
			oInfo.setEndTime(rs.getTimestamp("ENDTIME"));
			oInfo.setCutoffTime(rs.getTimestamp("CUTOFFTIME"));
			oInfo.setRsvType(EnumReservationType.getEnum(rs.getString("TYPE")));

			//TODO FDX - add these columns to query
			oInfo.seteStore("TODO");
			oInfo.setFacility("TODO");
			
			lst.add(oInfo);
		}
		return lst;
	}
	private static final String BROKEN_ACCOUNT_QUERY = "select "
			+ "user_id, c.id as erp_id, fdc.fdcustid as fd_id, a.zip, fdc.depot_code "
			+ "from cust.customer c, cust.customerinfo ci, cust.address a, "
			+ "(select id as fdcustid, erp_customer_id, depot_code "
			+ "from cust.fdcustomer where id not in "
			+ "(select fdcustomer_id from cust.fduser where fdcustomer_id is not null)) fdc "
			+ "where c.id=fdc.erp_customer_id and c.id=ci.customer_id and c.id=a.customer_id(+)";
	
	
	public static List findBrokenAccounts(Connection conn) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(BROKEN_ACCOUNT_QUERY);
		ResultSet rs = ps.executeQuery();
		List lst = processBrokenAccountResultSet(rs);
		rs.close();
		ps.close();
		return lst;

	}	

	private static List processBrokenAccountResultSet(ResultSet rs) throws SQLException {
		List lst = new ArrayList();
		while (rs.next()) {
			String userId = rs.getString("USER_ID");
			String customerId = rs.getString("ERP_ID");
			String fdCustomerId = rs.getString("FD_ID");
			String zipCode = rs.getString("ZIP");
			String depotCode = rs.getString("DEPOT_CODE");
			FDBrokenAccountInfo baInfo = new FDBrokenAccountInfo(userId, customerId, fdCustomerId, zipCode, depotCode);
			lst.add(baInfo);
		}
		return lst;
	}
	
	private static String ORDER_SEARCH_FOR_RETURNS = 
		"SELECT "
		+ "c.id customer_id, fdc.id fdc_id, ci.first_name, ci.last_name, c.user_id, ci.home_phone, ci.business_phone, "
		+ "ci.cell_phone, decode(ci.email_plain_text, 'X', 'TEXT', 'HTML') email_type, s.id, sa.requested_date, s.status, sa.amount, di.starttime, di.endtime, s.wave_number, s.truck_number "
		+ "from cust.customer c, cust.fdcustomer fdc, cust.customerinfo ci, cust.sale s, cust.salesaction sa, cust.deliveryinfo di "
		+ "where c.id = ci.customer_id and c.id = fdc.erp_customer_id and c.id = s.customer_id and s.id = sa.sale_id and sa.action_type IN ('CRO', 'MOD') "
		+ "and s.status = 'REF' and sa.action_date = "
		+ "(SELECT MAX(action_date) FROM cust.salesaction WHERE sale_id = s.id AND action_type IN ('CRO', 'MOD')) "
		+ "and sa.id = di.salesaction_id";
	
	public static List findOrderForReturnsByCriteria(Connection conn, GenericSearchCriteria criteria, CriteriaBuilder builder) throws SQLException {
		String query = ORDER_SEARCH_FOR_RETURNS + " and " + builder.getCriteria();
		PreparedStatement ps = conn.prepareStatement(query);
		Object[] obj = builder.getParams();
		for(int i = 0; i < obj.length; i++) {
			ps.setObject(i+1, obj[i]);
		}
		ResultSet rs = ps.executeQuery();
		List lst = processOrderForReturnsResultSet(rs);
		rs.close();
		ps.close();
		return lst;
	}
	
	private static List processOrderForReturnsResultSet(ResultSet rs) throws SQLException {
		List lst = new ArrayList();
		while (rs.next()) {
			FDCustomerOrderInfo oInfo = new FDCustomerOrderInfo();
			oInfo.setIdentity(new FDIdentity(rs.getString("CUSTOMER_ID"), rs.getString("FDC_ID")));
			oInfo.setFirstName(rs.getString("FIRST_NAME"));
			oInfo.setLastName(rs.getString("LAST_NAME"));
			oInfo.setEmail(rs.getString("USER_ID"));
			oInfo.setEmailType(rs.getString("EMAIL_TYPE"));
			oInfo.setPhone(new PhoneNumber(rs.getString("HOME_PHONE")).getPhone());
			String bizPhone = new PhoneNumber(rs.getString("BUSINESS_PHONE")).getPhone();
			String cellPhone = new PhoneNumber(rs.getString("CELL_PHONE")).getPhone();
			
			oInfo.setAltPhone(NVL.apply(cellPhone, ""));
			if("".equals(cellPhone)) {
				oInfo.setAltPhone(NVL.apply(bizPhone, ""));
			}
			oInfo.setSaleId(rs.getString("ID"));
			oInfo.setDeliveryDate(rs.getDate("REQUESTED_DATE"));
			oInfo.setOrderStatus(EnumSaleStatus.getSaleStatus(rs.getString("STATUS")));
			oInfo.setAmount(rs.getDouble("AMOUNT"));
			oInfo.setStartTime(rs.getTimestamp("STARTTIME"));
			oInfo.setEndTime(rs.getTimestamp("ENDTIME"));
			oInfo.setWaveNum(rs.getString("WAVE_NUMBER"));
			oInfo.setRouteNum(rs.getString("TRUCK_NUMBER"));
			
			//TODO FDX - add these columns to query
			oInfo.seteStore("TODO");
			oInfo.setFacility("TODO");
			
			lst.add(oInfo);
		}
		return lst;
	}

	private static String ORDER_SEARCH_BY_SKUS = 
		"SELECT distinct s.id, S.CUSTOMER_ID, CI.FIRST_NAME, CI.LAST_NAME,CI.EMAIL, CI.HOME_PHONE, "
		+ "CI.BUSINESS_PHONE,CI.CELL_PHONE, SA.REQUESTED_DATE, S.STATUS from cust.orderline ol, "
		+ "cust.salesaction sa, cust.sale s ,cust.customerinfo ci where "
		+ "S.ID = SA.SALE_ID and "
		+ "S.CUSTOMER_ID = SA.CUSTOMER_ID and "
		+ "OL.SALESACTION_ID = SA.ID and "
		+ "CI.CUSTOMER_ID = S.CUSTOMER_ID "
		+ "and SA.ACTION_TYPE in ('MOD','CRO') "
		+ "and SA.ACTION_DATE = S.CROMOD_DATE "
		+ "and S.STATUS IN ('SUB','AUT','AUF','AVE') ";
		//+ "and SA.REQUESTED_DATE >= trunc(sysdate + 1) "
	
	
	public static  List<FDCustomerOrderInfo> findOrderForSkusByCriteria(Connection conn, GenericSearchCriteria criteria, CriteriaBuilder builder) throws SQLException {
		String query = ORDER_SEARCH_BY_SKUS + " and " + builder.getCriteria();
		PreparedStatement ps = conn.prepareStatement(query);
		Object[] obj = builder.getParams();
		for(int i = 0; i < obj.length; i++) {
			ps.setObject(i+1, obj[i]);
		}
		ResultSet rs = ps.executeQuery();
		List<FDCustomerOrderInfo> lst = processOrderBySkusResultSet(rs);
		rs.close();
		ps.close();
		return lst;
	}
	
	private static List<FDCustomerOrderInfo> processOrderBySkusResultSet(ResultSet rs) throws SQLException {
		List<FDCustomerOrderInfo> lst = new ArrayList<FDCustomerOrderInfo>();
		while (rs.next()) {
			FDCustomerOrderInfo oInfo = new FDCustomerOrderInfo();
			oInfo.setIdentity(new FDIdentity(rs.getString("CUSTOMER_ID")));
			oInfo.setFirstName(rs.getString("FIRST_NAME"));
			oInfo.setLastName(rs.getString("LAST_NAME"));
			oInfo.setEmail(rs.getString("EMAIL"));
			oInfo.setPhone(new PhoneNumber(rs.getString("HOME_PHONE")).getPhone());
			String bizPhone = new PhoneNumber(rs.getString("BUSINESS_PHONE")).getPhone();
			String cellPhone = new PhoneNumber(rs.getString("CELL_PHONE")).getPhone();
			
			oInfo.setAltPhone(NVL.apply(cellPhone, ""));
			if("".equals(cellPhone)) {
				oInfo.setAltPhone(NVL.apply(bizPhone, ""));
			}
			oInfo.setSaleId(rs.getString("ID"));
			oInfo.setDeliveryDate(rs.getDate("REQUESTED_DATE"));
			oInfo.setOrderStatus(EnumSaleStatus.getSaleStatus(rs.getString("STATUS")));
			lst.add(oInfo);
		}
		return lst;
	}

	private static String GET_ORDERS_TO_MODIFY = 
		"SELECT M.SALE_ID, M.ERP_CUSTOMER_ID, M.FIRST_NAME, " 
		+"M.LAST_NAME, M.EMAIL, M.HOME_PHONE,  "  
		+"M.ALT_PHONE, M.REQUESTED_DATE, M.SALE_STATUS, M.CREATE_DATE, STATUS, ERROR_DESC "
		+"FROM CUST.MODIFY_ORDERS M WHERE ";
	
	public static  List<FDCustomerOrderInfo> getOrdersToModify(Connection conn, GenericSearchCriteria criteria, CriteriaBuilder builder) throws SQLException {
		String query = GET_ORDERS_TO_MODIFY + builder.getCriteria();
		PreparedStatement ps = conn.prepareStatement(query);
		Object[] obj = builder.getParams();
		for(int i = 0; i < obj.length; i++) {
			ps.setObject(i+1, obj[i]);
		}
		ResultSet rs = ps.executeQuery();
		List<FDCustomerOrderInfo> lst = processOrdersToModifyResultSet(rs);
		rs.close();
		ps.close();
		return lst;
	}
	
	private static List<FDCustomerOrderInfo> processOrdersToModifyResultSet(ResultSet rs) throws SQLException {
		List<FDCustomerOrderInfo> lst = new ArrayList<FDCustomerOrderInfo>();
		while (rs.next()) {
			BulkModifyOrderInfo oInfo = new BulkModifyOrderInfo();
			oInfo.setIdentity(new FDIdentity(rs.getString("ERP_CUSTOMER_ID")));
			oInfo.setFirstName(rs.getString("FIRST_NAME"));
			oInfo.setLastName(rs.getString("LAST_NAME"));
			oInfo.setEmail(rs.getString("EMAIL"));
			oInfo.setPhone(new PhoneNumber(rs.getString("HOME_PHONE")).getPhone());
			String altPhone = new PhoneNumber(rs.getString("ALT_PHONE")).getPhone();
			oInfo.setAltPhone(NVL.apply(altPhone, ""));
			oInfo.setSaleId(rs.getString("SALE_ID"));
			oInfo.setDeliveryDate(rs.getDate("REQUESTED_DATE"));
			oInfo.setOrderStatus(EnumSaleStatus.getSaleStatus(rs.getString("SALE_STATUS")));
			oInfo.setModStatus(rs.getString("STATUS"));
			oInfo.setErrorDesc(rs.getString("ERROR_DESC"));
			oInfo.setLastCroModDate(rs.getDate("CREATE_DATE"));
			lst.add(oInfo);
		}
		return lst;
	}

	private static CriteriaBuilder buildAddressRestrictionCriteria(GenericSearchCriteria criteria, CriteriaBuilder builder) {
		
		String address1 = (String)criteria.getCriteriaMap().get("address1");
		if(address1 != null  && address1.trim().length()>0){
			builder.addSql(" scrubbed_address like ? ", new Object[]{"%"+address1+"%"});	
		}
		
		String apartment = (String)criteria.getCriteriaMap().get("apartment");
		if(apartment != null && apartment.trim().length()>0){
			builder.addSql(" apartment like ? ", new Object[]{"%"+apartment+"%"});	
		}

		String zipCode = (String)criteria.getCriteriaMap().get("zipCode");
		if(zipCode != null && zipCode.trim().length()>0){
			builder.addSql(" zipcode like ? ", new Object[]{"%"+zipCode+"%"});	
		}

		
		EnumRestrictedAddressReason reason = (EnumRestrictedAddressReason)criteria.getCriteriaMap().get("reason");
		if(reason!=null && !"N".equalsIgnoreCase(reason.getCode())){
			builder.addSql(" reason = ? ", new String[]{reason.getCode()});
		}
		
		
		//builder.addOrderBy(sortColumn,true);
		
		return builder;
	}

	
	private static CriteriaBuilder buildDeliveryRestrictionDays(GenericSearchCriteria criteria, CriteriaBuilder builder) {
		java.util.Date startDate = (java.util.Date) criteria.getCriteriaMap().get("startDate");
		if(startDate!=null){
		  builder.addSql("start_time > ?", 
			 		new Date(startDate.getTime()));
		}
		Object message = criteria.getCriteriaMap().get("message");
		if(message != null){
			builder.addSql(" message like ? ", new Object[]{"%"+message+"%"});	
		}
		
		EnumDlvRestrictionReason reason = (EnumDlvRestrictionReason)criteria.getCriteriaMap().get("reason");
		if(reason!=null && !"All".equalsIgnoreCase(reason.getName())){
			builder.addSql(" reason = ? ", new String[]{reason.getName()});
		}
		EnumDlvRestrictionType type = (EnumDlvRestrictionType)criteria.getCriteriaMap().get("type");
		if(type!=null){
			builder.addSql(" type = ?", new String[]{type.getName()});
		}
		
		return builder;
	}
	
	private static CriteriaBuilder buildAlcoholRestrictionCriteria(GenericSearchCriteria criteria, CriteriaBuilder builder) {
		String state = (String)criteria.getCriteriaMap().get("state");
		if(state != null  && state.trim().length()>0){
			builder.addSql(" state = ? ", new Object[]{state});	
		}
		
		String county = (String)criteria.getCriteriaMap().get("county");
		if(county != null && county.trim().length()>0){
			builder.addSql(" county = ? ", new Object[]{county});	
		} else {
			builder.addSql(" county IS NULL ", new Object[]{});
		}

		EnumDlvRestrictionReason reason = (EnumDlvRestrictionReason)criteria.getCriteriaMap().get("reason");
		if(reason!=null && !"All".equalsIgnoreCase(reason.getName())){
			builder.addSql(" reason = ? ", new String[]{reason.getName()});
		}
		EnumDlvRestrictionType type = (EnumDlvRestrictionType)criteria.getCriteriaMap().get("type");
		if(type!=null){
			builder.addSql(" type = ?", new String[]{type.getName()});
		}
		
		return builder;
	}
	
	private static String DELIVERY_RESTRICTIONS_RETURN = 
		"select ID,TYPE,NAME,DAY_OF_WEEK,START_TIME,END_TIME,REASON,MESSAGE,CRITERION,MEDIA_PATH FROM CUST.restricted_days";

	private static List processDeliveryRestriction(Connection conn, GenericSearchCriteria criteria, CriteriaBuilder builder) throws SQLException {
		List restrictions=new ArrayList();		
		
		String query ="";
		String sortColumn = (String)criteria.getCriteriaMap().get("sortColumn");
		String ascending = (String)criteria.getCriteriaMap().get("ascending");
		
		if(sortColumn == null  || sortColumn.trim().length()==0){
			sortColumn="start_time";			
		}
		if(ascending == null  || ascending.trim().length()==0){
			ascending="asc";			
		}
		
		if(builder.getParams()!=null && builder.getParams().length>0){
		    query = new StringBuffer(DELIVERY_RESTRICTIONS_RETURN).append(" where ").append(builder.getCriteria()).append(" and reason not  in ('ACL','BER','WIN') ").append(" order by ").append(sortColumn).append(" "+ascending).toString();
		}else{
			query = new StringBuffer(DELIVERY_RESTRICTIONS_RETURN).append(builder.getCriteria()).append(" and reason not  in ('ACL','BER','WIN') ").append(" order by ").append(sortColumn).append(" "+ascending).toString();
		}
		
		
		LOGGER.debug("query :"+query);
		PreparedStatement ps = conn.prepareStatement(query);
		
		Object[] obj = builder.getParams();
		for(int i = 0; i < obj.length; i++) {
			LOGGER.debug("i:"+i+":"+obj[i]);
			LOGGER.debug(obj[i].getClass().getName());
			ps.setObject(i+1, obj[i]);
		}
		
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			String id = rs.getString("ID");
			String name = rs.getString("NAME");
			String msg = rs.getString("MESSAGE");
			String path = rs.getString("MEDIA_PATH");
			EnumDlvRestrictionCriterion criterion = EnumDlvRestrictionCriterion.getEnum(rs.getString("CRITERION"));
			if (criterion == null) {
				// skip unknown criteria
				continue;
			}

			EnumDlvRestrictionReason reason = EnumDlvRestrictionReason.getEnum(rs.getString("REASON"));
			if (reason == null) {
				// skip unknown reasons
				continue;
			}

			java.util.Date startDate = new java.util.Date(rs.getTimestamp("START_TIME").getTime());
			java.util.Date endDate = new java.util.Date(rs.getTimestamp("END_TIME").getTime());
			int dayOfWeek = rs.getInt("DAY_OF_WEEK");

			String typeCode = rs.getString("TYPE");
			EnumDlvRestrictionType type = EnumDlvRestrictionType.getEnum(typeCode);
			if (type == null && "PTR".equals(typeCode)) {
				type = EnumDlvRestrictionType.RECURRING_RESTRICTION;
			}

			if (EnumDlvRestrictionType.ONE_TIME_RESTRICTION.equals(type)) {

				if(!EnumDlvRestrictionReason.PLATTER.equals(reason)){
					endDate = DateUtil.roundUp(endDate);
				}

				// FIXME one-time reverse restrictions should have a different EnumDlvRestrictionType 
				if (reason.isSpecialHoliday()) {
					restrictions.add(new OneTimeReverseRestriction(id,criterion, reason, name, msg, startDate, endDate,path));
				} else {
					restrictions.add(new OneTimeRestriction(id,criterion, reason, name, msg, startDate, endDate,path));
				}

			} else if (EnumDlvRestrictionType.RECURRING_RESTRICTION.equals(type)) {

				TimeOfDay startTime = new TimeOfDay(startDate);
				TimeOfDay endTime = new TimeOfDay(endDate);
				// round up 11:59 to next midnight
				if (JUST_BEFORE_MIDNIGHT.equals(endTime)) {
					endTime = TimeOfDay.NEXT_MIDNIGHT;
				}
				restrictions.add(new RecurringRestriction(id,criterion, reason, name, msg, dayOfWeek, startTime, endTime,path));

			} else {
				// ignore	
			}



		}

		LOGGER.debug("restrictions size :"+restrictions.size());
		rs.close();
		ps.close();

		return restrictions;
	}
	
	private static final String GET_ALCOHOL_RESTRICTION = "select  r.ID,r.TYPE,r.NAME,r.START_TIME,r.END_TIME,r.REASON,r.MESSAGE,r.CRITERION,r.MEDIA_PATH, "+
											"D.DAY_OF_WEEK, D.RES_START_TIME,D.RES_END_TIME, M.ID MUNICIPALITY_ID,M.STATE,M.COUNTY,M.CITY,M.ALCOHOL_RESTRICTED "+ 
											"from CUST.restricted_days r,CUST.RESTRICTION_DETAIL d, CUST.MUNICIPALITY_INFO m, CUST.MUNICIPALITY_RESTRICTION_DATA mr "+
											"where R.ID = D.RESTRICTION_ID(+) "+
											"and R.ID = MR.RESTRICTION_ID "+
											"and M.ID = MR.MUNICIPALITY_ID ";
	private static List<AlcoholRestriction> processAlcoholRestriction(Connection conn, GenericSearchCriteria criteria, CriteriaBuilder builder) throws SQLException {
		List<AlcoholRestriction> restrictions=new ArrayList<AlcoholRestriction>();		
		
		String query ="";
		String sortColumn = (String)criteria.getCriteriaMap().get("sortColumn");
		String ascending = (String)criteria.getCriteriaMap().get("ascending");
		
		if(sortColumn == null  || sortColumn.trim().length()==0){
			sortColumn="start_time";			
		}
		if(ascending == null  || ascending.trim().length()==0){
			ascending="asc";			
		}
		
		if(builder.getParams()!=null && builder.getParams().length>0){
		    query = new StringBuffer(GET_ALCOHOL_RESTRICTION).append(" and ").append(builder.getCriteria()).append(" order by ").append(" R.ID ").toString();
		}else{
			query = new StringBuffer(GET_ALCOHOL_RESTRICTION).append(" order by ").append(" R.ID ").toString();
		}

		LOGGER.debug("query :"+query);
		PreparedStatement ps = conn.prepareStatement(query);
		
		Object[] obj = builder.getParams();
		for(int i = 0; i < obj.length; i++) {
			LOGGER.debug("i:"+i+":"+obj[i]);
			LOGGER.debug(obj[i].getClass().getName());
			ps.setObject(i+1, obj[i]);
		}
		String restrictionId = "";
		String name = null;
		String msg = null;
		String path =null;
		EnumDlvRestrictionReason reason = null;
		EnumDlvRestrictionCriterion criterion = null;
		EnumDlvRestrictionType type = null;
		java.util.Date startDate = null;
		java.util.Date endDate = null;
		String state = null;
		String county = null;
		String municipalityId = null;
		boolean alcoholRestricted = false;
		
		ResultSet rs = ps.executeQuery();
		int count = 0;
		//Map<Integer, List<TimeOfDayRange>> timeRangeMap = new HashMap<Integer, List<TimeOfDayRange>>();
		while (rs.next()) {
			if(restrictionId.length() == 0 || restrictionId.equals(rs.getString("ID"))){
				restrictionId = rs.getString("ID");
				name = rs.getString("NAME");
				msg = rs.getString("MESSAGE");
				path = rs.getString("MEDIA_PATH");
				criterion = EnumDlvRestrictionCriterion.getEnum(rs.getString("CRITERION"));
				if (criterion == null) {
					// skip unknown criteria
					continue;
				}
	
				reason = EnumDlvRestrictionReason.getEnum(rs.getString("REASON"));
				if (reason == null) {
					// skip unknown reasons
					continue;
				}
	
				startDate = new java.util.Date(rs.getTimestamp("START_TIME").getTime());
				endDate = new java.util.Date(rs.getTimestamp("END_TIME").getTime());
				/*
				int dayOfWeek = rs.getInt("DAY_OF_WEEK");
				TimeOfDay startTime = new TimeOfDay(rs.getString("RES_START_TIME"));
				TimeOfDay endTime = new TimeOfDay(rs.getString("RES_END_TIME"));
				Integer key = new Integer(dayOfWeek);
				if(timeRangeMap.get(key) == null) {
					List<TimeOfDayRange> timeRanges = new ArrayList<TimeOfDayRange>();
					timeRanges.add(new TimeOfDayRange(startTime, endTime));
					timeRangeMap.put(key, timeRanges);
				} else {
					List<TimeOfDayRange> timeRanges = timeRangeMap.get(key);
					timeRanges.add(new TimeOfDayRange(startTime, endTime));
					timeRangeMap.put(key, timeRanges);
				}*/
				String typeCode = rs.getString("TYPE");
				type = EnumDlvRestrictionType.getEnum(typeCode);
				if (type == null && "PTR".equals(typeCode)) {
					type = EnumDlvRestrictionType.RECURRING_RESTRICTION;
				}
				state = rs.getString("state");
				county = rs.getString("county");
				municipalityId = rs.getString("MUNICIPALITY_ID");
				alcoholRestricted = Boolean.getBoolean(rs.getString("ALCOHOL_RESTRICTED"));
				count ++;
			} else {
				AlcoholRestriction restriction = new AlcoholRestriction(restrictionId, criterion, reason, name, msg, startDate, endDate,type,
						path, state, county, null, municipalityId, alcoholRestricted);
				//restriction.setTimeRangeMap(new HashMap<Integer, List<TimeOfDayRange>>(timeRangeMap));
				restrictions.add(restriction);
				//timeRangeMap.clear();
				restrictionId = rs.getString("ID");
				name = rs.getString("NAME");
				msg = rs.getString("MESSAGE");
				path = rs.getString("MEDIA_PATH");
				criterion = EnumDlvRestrictionCriterion.getEnum(rs.getString("CRITERION"));
				if (criterion == null) {
					// skip unknown criteria
					continue;
				}
	
				reason = EnumDlvRestrictionReason.getEnum(rs.getString("REASON"));
				if (reason == null) {
					// skip unknown reasons
					continue;
				}
	
				startDate = new java.util.Date(rs.getTimestamp("START_TIME").getTime());
				endDate = new java.util.Date(rs.getTimestamp("END_TIME").getTime());
				/*
				int dayOfWeek = rs.getInt("DAY_OF_WEEK");
				TimeOfDay startTime = new TimeOfDay(rs.getString("RES_START_TIME"));
				TimeOfDay endTime = new TimeOfDay(rs.getString("RES_END_TIME"));
				Integer key = new Integer(dayOfWeek);
				if(timeRangeMap.get(key) == null) {
					List<TimeOfDayRange> timeRanges = new ArrayList<TimeOfDayRange>();
					timeRanges.add(new TimeOfDayRange(startTime, endTime));
					timeRangeMap.put(key, timeRanges);
				} else {
					List<TimeOfDayRange> timeRanges = timeRangeMap.get(key);
					timeRanges.add(new TimeOfDayRange(startTime, endTime));
					timeRangeMap.put(key, timeRanges);
				}
				*/
				String typeCode = rs.getString("TYPE");
				type = EnumDlvRestrictionType.getEnum(typeCode);
				if (type == null && "PTR".equals(typeCode)) {
					type = EnumDlvRestrictionType.RECURRING_RESTRICTION;
				}
				state = rs.getString("state");
				county = rs.getString("county");
				municipalityId = rs.getString("MUNICIPALITY_ID");
				alcoholRestricted = Boolean.getBoolean(rs.getString("ALCOHOL_RESTRICTED"));
				
			}
		}
		if(count > 0) {
			//Add the last element.
			AlcoholRestriction restriction = new AlcoholRestriction(restrictionId, criterion, reason, name, msg, startDate, endDate,type,
					path, state, county, null, municipalityId, alcoholRestricted);
			restrictions.add(restriction);
		}
		LOGGER.debug("restrictions size :"+restrictions.size());
		rs.close();
		ps.close();

		return restrictions;
	}
	
	private static String ADDRESS_RESTRICTIONS_RETURN = 
		"select scrubbed_address, apartment, zipcode, reason, date_modified, modified_by from CUST.restricted_address ";

	
	private static List processAddressRestriction(Connection conn, GenericSearchCriteria criteria, CriteriaBuilder builder) throws SQLException {
		List restrictions=new ArrayList();
		String query="";
		
		String sortColumn = (String)criteria.getCriteriaMap().get("sortColumn");
		String ascending = (String)criteria.getCriteriaMap().get("ascending");
		
		if(sortColumn == null  || sortColumn.trim().length()==0){
			sortColumn="date_modified";			
		}
		if(ascending == null  || ascending.trim().length()==0){
			ascending="asc";			
		}
		
		if(builder.getParams()!=null && builder.getParams().length>0){
		    query = new StringBuffer(ADDRESS_RESTRICTIONS_RETURN).append(" where ").append(builder.getCriteria()).append(" order by ").append(sortColumn).append(" "+ascending).toString();
		}else{
			query = new StringBuffer(ADDRESS_RESTRICTIONS_RETURN).append(" order by ").append(sortColumn).append(" "+ascending).toString() ;
		}
		
		LOGGER.debug("query2 :"+query);
		PreparedStatement ps = conn.prepareStatement(query);
		
		Object[] obj = builder.getParams();
		for(int i = 0; i < obj.length; i++) {
			LOGGER.debug("i:"+i+":"+obj[i]);
			LOGGER.debug(obj[i].getClass().getName());
			ps.setObject(i+1, obj[i]);
		}
		
		RestrictedAddressModel restriction=null;
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			restriction= new  RestrictedAddressModel();
			restriction.setAddress1(rs.getString("scrubbed_address"));
			restriction.setApartment(rs.getString("apartment"));
			restriction.setZipCode(rs.getString("zipCode"));
			EnumRestrictedAddressReason reason = EnumRestrictedAddressReason.getRestrictionReason(rs.getString("reason"));
			restriction.setReason(reason);	
			java.util.Date dateModified = new java.util.Date(rs.getTimestamp("date_modified").getTime());			
			restriction.setLastModified(dateModified);
			restriction.setModifiedBy(rs.getString("modified_by"));
			
			restrictions.add(restriction);
		
		}

		LOGGER.debug("address restrictions size :"+restrictions.size());
		rs.close();
		ps.close();

		return restrictions;
	}

	
	private final static TimeOfDay JUST_BEFORE_MIDNIGHT = new TimeOfDay("11:59 PM");

	private static final String SETTLEMENT_BATCH_QUERY = "select merchant_id, batch_id, settle_date_time, batch_status, " +
			"batch_response_msg, processor_batch_id, submission_id, sales_transactions,sales_amount, return_transactions, return_amount " +
			"from paylinx.cc_settlement where batch_status<>'00'";


	public static List findFailedSettlementBatch(Connection conn) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(SETTLEMENT_BATCH_QUERY);
		ResultSet rs = ps.executeQuery();
		List lst = processSettlementBatchResultSet(rs);
		rs.close();
		ps.close();
		return lst;

	}	

	private static List processSettlementBatchResultSet(ResultSet rs) throws SQLException {
		List lst = new ArrayList();
		while (rs.next()) {
			SettlementBatchInfo sbInfo = new SettlementBatchInfo();
			sbInfo.setMerchant_id(rs.getString("MERCHANT_ID"));
			sbInfo.setBatch_id(rs.getString("BATCH_ID"));
			sbInfo.setSettle_date_time(rs.getTimestamp("SETTLE_DATE_TIME"));
			sbInfo.setBatch_status(rs.getString("BATCH_STATUS"));
			sbInfo.setBatch_response_msg(rs.getString("BATCH_RESPONSE_MSG"));
			sbInfo.setProcessor_batch_id(rs.getString("PROCESSOR_BATCH_ID"));
			sbInfo.setSubmission_id(rs.getString("SUBMISSION_ID"));
			sbInfo.setSales_transactions(rs.getInt("SALES_TRANSACTIONS"));
			sbInfo.setSales_amount(rs.getDouble("SALES_AMOUNT"));
			sbInfo.setReturn_transactions(rs.getInt("RETURN_TRANSACTIONS"));
			sbInfo.setReturn_amount(rs.getDouble("RETURN_AMOUNT"));
			lst.add(sbInfo);
		}
		return lst;
	}


}
