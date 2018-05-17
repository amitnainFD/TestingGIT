package com.freshdirect.fdstore.standingorders.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.freshdirect.crm.ejb.CriteriaBuilder;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDProduct;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSalesUnit;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;
import com.freshdirect.fdstore.standingorders.FDStandingOrderAltDeliveryDate;
import com.freshdirect.fdstore.standingorders.FDStandingOrderFilterCriteria;
import com.freshdirect.fdstore.standingorders.FDStandingOrderInfo;
import com.freshdirect.fdstore.standingorders.FDStandingOrderInfoList;
import com.freshdirect.fdstore.standingorders.FDStandingOrderProductSku;
import com.freshdirect.fdstore.standingorders.FDStandingOrderSkuResultInfo;
import com.freshdirect.fdstore.standingorders.InventoryMapInfoBean;
import com.freshdirect.fdstore.standingorders.UnAvailabilityDetails;
import com.freshdirect.fdstore.standingorders.UnavDetailsReportingBean;
import com.freshdirect.fdstore.standingorders.UnavailabilityReason;
import com.freshdirect.fdstore.standingorders.SOResult.Result;
import com.freshdirect.fdstore.standingorders.SOResult.Status;
import com.freshdirect.framework.core.SequenceGenerator;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDStandingOrderDAO {
	
	
		private static final String ALTERNATE_DELIVERY_DATE = "ALTERNATE_DELIVERY_DATE";

	private static final String CURRENT_DELIVERY_DATE = "current_delivery_date";

	private static final Logger LOGGER = LoggerFactory.getInstance( FDStandingOrderDAO.class );

	private static final String FIELDZ_ALL = "SO.ID, SO.CUSTOMER_ID, SO.CUSTOMERLIST_ID, SO.ADDRESS_ID, SO.PAYMENTMETHOD_ID, SO.START_TIME, SO.END_TIME, SO.NEXT_DATE, SO.FREQUENCY, SO.ALCOHOL_AGREEMENT, SO.DELETED, SO.LAST_ERROR, SO.ERROR_HEADER, SO.ERROR_DETAIL, CCL.NAME, C.USER_ID";

	private static final String LOAD_CUSTOMER_STANDING_ORDERS =
		"select " + FIELDZ_ALL + " " +
		"from CUST.STANDING_ORDER SO " +
		"left join CUST.CUSTOMERLIST CCL on(CCL.id = SO.CUSTOMERLIST_ID) " +
		"left join CUST.CUSTOMER c on (C.ID = SO.CUSTOMER_ID) " +
		"where CCL.CUSTOMER_ID = ? and SO.DELETED<>1 " +
		"order by CCL.NAME";

	private static final String LOAD_ACTIVE_STANDING_ORDERS =
		"select " + FIELDZ_ALL + " " +
		"from CUST.STANDING_ORDER SO " +
		"left join CUST.CUSTOMERLIST CCL on(CCL.id = SO.CUSTOMERLIST_ID) " +
		"left join CUST.CUSTOMER c on (C.ID = SO.CUSTOMER_ID) " +
		"where SO.DELETED<>1 " +
		"order by CCL.NAME";

	private static final String CREATE_EMPTY_STANDING_ORDER = "INSERT INTO CUST.STANDING_ORDER(ID, CUSTOMER_ID, CUSTOMERLIST_ID) VALUES(?,?,?)";
	
	private static final String LOAD_STANDING_ORDER =
		"select " + FIELDZ_ALL + " " +
		"from CUST.STANDING_ORDER SO " +
		"left join CUST.CUSTOMERLIST CCL on(CCL.id = SO.CUSTOMERLIST_ID) " +
		"left join CUST.CUSTOMER c on (C.ID = SO.CUSTOMER_ID) " +
		"where SO.ID=?";

	private static final String INSERT_STANDING_ORDER = "insert into CUST.STANDING_ORDER(ID, CUSTOMER_ID, CUSTOMERLIST_ID, ADDRESS_ID, PAYMENTMETHOD_ID, START_TIME, END_TIME, NEXT_DATE, FREQUENCY, ALCOHOL_AGREEMENT, DELETED, LAST_ERROR, ERROR_HEADER, ERROR_DETAIL) " +
	"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	private static final String UPDATE_STANDING_ORDER = "update CUST.STANDING_ORDER set " +
	"CUSTOMER_ID = ?, " +
	"CUSTOMERLIST_ID = ?, " +
	"ADDRESS_ID = ?, " +
	"PAYMENTMETHOD_ID = ?, " +
	"START_TIME = ?, " +
	"END_TIME = ?, " +
	"NEXT_DATE = ?, " +
	"FREQUENCY = ?, " +
	"ALCOHOL_AGREEMENT = ?, " +
	"DELETED = ?, " +
	"LAST_ERROR = ?, " +
	"ERROR_HEADER = ?, " +	
	"ERROR_DETAIL = ? " +	
	"where ID = ?";
	
	private static final String LOAD_STANDING_ORDER_ALTERNATE_DELIVERY_INFO =
			"select * " +
			"from CUST.SO_HOLIDAY_ALT_DATE SA " +
			"where SA.SO_ID=? AND SA.CURRENT_DELIVERY_DATE=?";
	
	private static final String DELETE_STANDING_ORDER = "update CUST.STANDING_ORDER SET DELETED=1, CUSTOMERLIST_ID=NULL where ID=? and DELETED=0";
	private static final String DELETE_CUSTOMER_LIST = "delete from CUST.CUSTOMERLIST where ID=?";
	private static final String DELETE_CUSTOMER_LIST_DETAILS = "delete from CUST.CUSTOMERLIST_DETAILS where LIST_ID = ?";
	
	private static final String ASSIGN_SO_TO_SALE = "UPDATE CUST.SALE SET STANDINGORDER_ID=? WHERE ID=?";

	private static final String MARK_SALE_HOLIDAYMOVEMENT = "UPDATE CUST.SALE SET SO_HOLIDAY_MOVEMENT='Y' WHERE ID=?";
	
	private static final String SKU_CODE_REPLACE_SQL = "UPDATE CUST.CUSTOMERLIST_DETAILS SET SKU_CODE = ? WHERE SKU_CODE = ? AND LIST_ID IN (SELECT CLIST.ID FROM CUST.CUSTOMERLIST CLIST JOIN CUST.STANDING_ORDER SO ON CLIST.ID = SO.CUSTOMERLIST_ID AND SO.DELETED <> 1)";

	private static final String SKU_CODE_GRID_DISPLAY_SQL = "SELECT CU.USER_ID ,CU.ID AS CUSTOMER_ID,SO.ID AS SO_TEMPLATE_ID,CCLD.LIST_ID,CCLD.SKU_CODE,CCLD.QUANTITY,CCLD.SALES_UNIT,CCLD.FREQUENCY FROM CUST.CUSTOMER CU , CUST.CUSTOMERLIST CCL, CUST.CUSTOMERLIST_DETAILS CCLD,CUST.STANDING_ORDER SO " +"WHERE CU.ID = CCL.CUSTOMER_ID AND CCL.ID = SO.CUSTOMERLIST_ID AND CCLD.LIST_ID = CCL.ID AND SO.DELETED <> 1 AND CCLD.SKU_CODE = ?";
	
	private boolean isSkuValidButDiscontinued;

	private static final String EXISTING_SKU_NOT_EXIST = "Existing SKU does not exist";

	private static final String REPLACEMENT_SKU_NOT_EXIST = "Replacement SKU does not exist";

	private static final String REPLACEMENT_SKU_DISC_OR_UNAVAILABLE = "Replacement SKU is either Disc or Unavailable";
	
	private static final String BASE_UNIT = "base_unit";

	private static final String CONFIGURATION = "CONFIGURATION";
	
	private static final String DELETED_LIST_NAME = "Deleted Standing Order";
	
	private static final String UNAV_ITEM_SQL = "insert into mis.unav_items_inv values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	private static final String UNAV_ITEM_DETAILS_SQL = "select PRODUCT_NAME,SKU_CODE,MATERIAL_NUM,QUANTITY from mis.unav_items_inv where run_instance = ? and reason = ? and run_date > SYSDATE - 1";

	private static final String GET_RUNINSTANCE_SQL = "select max(RUN_INSTANCE) as maxInstance from mis.UNAV_ITEMS_INV where run_date > SYSDATE - 1";
	
	protected String getNextId(Connection conn) throws SQLException {
		return SequenceGenerator.getNextId(conn, "CUST");
	}
	
	
	public String createEmptyStandingOrder(Connection conn, String customerPK, String customerListPK) throws SQLException {
		if (customerPK == null || customerListPK == null)
			return null;
		
		PreparedStatement ps = null;
		boolean flag = false;
		String myId = null;

		try {
			ps = conn.prepareStatement(CREATE_EMPTY_STANDING_ORDER);
			
			myId = getNextId(conn);
			
			ps.setString(1, myId);
			ps.setString(2, customerPK);
			ps.setString(3, customerListPK);

			flag = ps.execute();
			
			ps.close();

			return flag ? myId : null;
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
		}
	}

	
	/**
	 * Load standing orders
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public Collection<FDStandingOrder> loadActiveStandingOrders(Connection conn) throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;

		List<FDStandingOrder> sorders = new ArrayList<FDStandingOrder>();

		try {
			ps = conn.prepareStatement(LOAD_ACTIVE_STANDING_ORDERS);

			rs = ps.executeQuery();
			
			while (rs.next()) {
				FDStandingOrder so = new FDStandingOrder();
				so = populate(rs, so);
				loadStandingOrderAlternateDateForSO(conn, so);
				sorders.add( so );
			}

			rs.close();
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}
		
		return sorders;
	}


	
	/**
	 * Returns customer's active standing orders
	 * 
	 * @param conn
	 * @param identity
	 * @return
	 * @throws SQLException
	 */
	public Collection<FDStandingOrder> loadCustomerStandingOrders(Connection conn, FDIdentity identity) throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		List<FDStandingOrder> sorders;
		try {
			ps = conn.prepareStatement(LOAD_CUSTOMER_STANDING_ORDERS);
			ps.setString(1, identity.getErpCustomerPK());

			sorders = new ArrayList<FDStandingOrder>();
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				FDStandingOrder so = new FDStandingOrder();
				
				sorders.add( populate(rs, so) );
			}

			rs.close();
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}
		
		return sorders;
	}

	
	/**
	 * Load a single standing order record
	 * 
	 * @param conn
	 * @param pk
	 * @return
	 * @throws SQLException
	 */
	public FDStandingOrder load(Connection conn, String pk) throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		FDStandingOrder so = null;
		
		try {
			ps = conn.prepareStatement(LOAD_STANDING_ORDER);
			ps.setString(1, pk);

			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				so = new FDStandingOrder();
				
				populate(rs, so);
				loadStandingOrderAlternateDateForSO(conn, so);
				
			}

			rs.close();
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}
		
		return so;
	}


	private void loadStandingOrderAlternateDateForSO(
			Connection conn, FDStandingOrder so) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(LOAD_STANDING_ORDER_ALTERNATE_DELIVERY_INFO);
			ps.setString(1, so.getId());
			ps.setDate(2, so.getNextDeliveryDate()==null? null : new java.sql.Date(so.getNextDeliveryDate().getTime()));
			rs = ps.executeQuery();
			if(rs.next()){
				FDStandingOrderAltDeliveryDate soAltDeliveryInfo = populateAltDeliveryInfo(rs);
				so.setAltDeliveryInfo(soAltDeliveryInfo);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}
	}

	private FDStandingOrder populate( ResultSet rs, FDStandingOrder so ) throws SQLException {
		
		so.setId(rs.getString("ID"));

		so.setCustomerId( rs.getString("CUSTOMER_ID") );
		so.setCustomerListId( rs.getString("CUSTOMERLIST_ID") );
		so.setAddressId( rs.getString("ADDRESS_ID") );
		so.setPaymentMethodId( rs.getString("PAYMENTMETHOD_ID") );
		
		so.setCustomerEmail(rs.getString("USER_ID"));
		so.setStartTime( rs.getTime("START_TIME") );
		so.setEndTime( rs.getTime("END_TIME") );
		so.setNextDeliveryDate( rs.getDate("NEXT_DATE") );
		so.setPreviousDeliveryDate(so.getNextDeliveryDate());
		
		so.setFrequency( rs.getInt("FREQUENCY") );
		so.setAlcoholAgreement( rs.getBoolean("ALCOHOL_AGREEMENT") );
		
		so.setDeleted( rs.getBoolean("DELETED") );

		so.setLastError( rs.getString( "LAST_ERROR" ), rs.getString( "ERROR_HEADER" ), rs.getString( "ERROR_DETAIL" ) );
		
		String listName = rs.getString( "NAME" );
		if ( listName == null )
			listName = DELETED_LIST_NAME;
		so.setCustomerListName( listName );

		return so;
	}
	
	
	public void deleteStandingOrder( Connection conn, String soPk, String listPk ) throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement( DELETE_STANDING_ORDER );
		ps.setString( 1, soPk );
		ps.executeUpdate();
		ps.close();
		
		ps = conn.prepareStatement( DELETE_CUSTOMER_LIST_DETAILS );
		ps.setString( 1, listPk );
		ps.executeUpdate();		
		ps.close();
		
		ps = conn.prepareStatement( DELETE_CUSTOMER_LIST );
		ps.setString( 1, listPk );
		ps.executeUpdate();		
		ps.close();
	}
	
	public String createStandingOrder(Connection conn, FDStandingOrder so) throws SQLException {
		LOGGER.debug( "FDStandingOrderDAO.createStandingOrder()" );

		String myId = null;
		PreparedStatement ps = null; 
		
		try {
			myId = getNextId(conn);

			ps = conn.prepareStatement(INSERT_STANDING_ORDER);
			
			final Date startDate = so.getStartTime();
			final Date endDate = so.getEndTime();
			final Date nextDeliveryDate = so.getNextDeliveryDate();

			int counter = 1;
			ps.setString(counter++, myId);
			ps.setString(counter++, so.getCustomerId());
			ps.setString(counter++, so.getCustomerListId());
			ps.setString(counter++, so.getAddressId());
			ps.setString(counter++, so.getPaymentMethodId());
			ps.setTime(counter++, startDate != null ? new java.sql.Time( startDate.getTime() ) : null );
			ps.setTime(counter++, endDate != null ? new java.sql.Time( endDate.getTime() ) : null);
			ps.setDate(counter++, nextDeliveryDate != null ? new java.sql.Date( nextDeliveryDate.getTime() ) : null);
			ps.setInt(counter++, so.getFrequency());
			ps.setBoolean(counter++, so.isAlcoholAgreement());
			ps.setBoolean(counter++, so.isDeleted());
			ps.setString(counter++, so.getLastError() == null ? null : so.getLastError().name());
			ps.setString(counter++, so.getErrorHeader());
			ps.setString(counter++, so.getErrorDetail());
			
			ps.execute();
			
			ps.close();

			so.setId(myId);
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
		}
		
		return myId;
	}

	public void updateStandingOrder(Connection conn, FDStandingOrder so) throws SQLException {
		LOGGER.debug( "FDStandingOrderDAO.updateStandingOrder()" );
		
		if (so == null || so.getId() == null)
			return;
				
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(UPDATE_STANDING_ORDER);
			int counter = 1;
			ps.setString(counter++, so.getCustomerId());
			ps.setString(counter++, so.getCustomerListId());
			ps.setString(counter++, so.getAddressId());
			ps.setString(counter++, so.getPaymentMethodId());
			ps.setTime(counter++, so.getStartTime() != null ? new java.sql.Time( so.getStartTime().getTime() ) : null );
			ps.setTime(counter++, so.getEndTime() != null ? new java.sql.Time( so.getEndTime().getTime() ) : null);
			ps.setDate(counter++, so.getNextDeliveryDate() != null ? new java.sql.Date( so.getNextDeliveryDate().getTime() ) : null);
			ps.setInt(counter++, so.getFrequency());
			ps.setBoolean(counter++, so.isAlcoholAgreement());
			ps.setBoolean(counter++, so.isDeleted());
			ps.setString(counter++, so.getLastError() == null ? null : so.getLastError().name());
			ps.setString(counter++, so.getErrorHeader());
			ps.setString(counter++, so.getErrorDetail());

			ps.setString(counter++, so.getId());
			
			ps.executeUpdate();
			
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
		}
	}
	

	public void assignToSale(Connection conn, String soPK, String salePK ) throws SQLException {
		
		LOGGER.debug( "assigning SO["+soPK+"] to SALE["+salePK+"]" );
		
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement(ASSIGN_SO_TO_SALE);
		
			ps.setString(1, soPK);
			ps.setString(2, salePK);
			
			ps.executeUpdate();
			
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
		}
	}

	public void markSaleAltDeliveryDateMovement(Connection conn, String salePK ) throws SQLException {
		
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement(MARK_SALE_HOLIDAYMOVEMENT);
		
			ps.setString(1, salePK);
			
			ps.executeUpdate();
			
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
		}
	}

	
	private static final String GET_ACTIVE_STANDING_ORDERS_CUST_INFO =
		"select  so.id,c.user_id,A.COMPANY_NAME,SO.NEXT_DATE ,SO.CUSTOMER_ID,SO.FREQUENCY, SO.ERROR_HEADER,SO.LAST_ERROR,SO.START_TIME,SO.END_TIME," +
		" A.ADDRESS1||', '||a.ADDRESS2||', '||a.APARTMENT||', '||a.CITY||', '||a.STATE||', '||a.ZIP as address," +
		" NVL(CI.BUSINESS_PHONE||'-'||CI.BUSINESS_EXT,'') as BUSINESS_PHONE,NVL(CI.CELL_PHONE,'') as CELL_PHONE" +
		" from cust.address a, cust.customerinfo ci,cust.customer c,CUST.STANDING_ORDER so " +
		" where SO.ADDRESS_ID=a.id(+) and c.id=ci.customer_id and so.customer_id=c.id  ";
	

	public FDStandingOrderInfoList getActiveStandingOrdersCustInfo(Connection conn,FDStandingOrderFilterCriteria filter) throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
	
		List<FDStandingOrderInfo> soOrders = new ArrayList<FDStandingOrderInfo>();
		FDStandingOrderInfoList infoList = new FDStandingOrderInfoList(soOrders);
	
		try {
			/*String query = 
				"select  so.id,c.user_id,A.COMPANY_NAME,SO.NEXT_DATE ,SO.CUSTOMER_ID,SO.FREQUENCY, SO.ERROR_HEADER,SO.LAST_ERROR,SO.START_TIME,SO.END_TIME," +
				" A.ADDRESS1||', '||a.ADDRESS2||', '||a.APARTMENT||', '||a.CITY||', '||a.STATE||', '||a.ZIP as address," +
				" NVL(CI.BUSINESS_PHONE||'-'||CI.BUSINESS_EXT,'') as BUSINESS_PHONE,NVL(CI.CELL_PHONE,'') as CELL_PHONE" +
				" from cust.address a, cust.customerinfo ci,cust.customer c,CUST.STANDING_ORDER so " +
				" where SO.ADDRESS_ID=a.id(+) and c.id=ci.customer_id and so.customer_id=c.id  ";*/
//			ps = conn.prepareStatement(GET_ACTIVE_STANDING_ORDERS_CUST_INFO);
			
			CriteriaBuilder builder = new CriteriaBuilder();
			boolean isActiveOnly = true;
			if(null!=filter){
				if(filter.getId()!=null){
					builder.addObject("SO.ID", filter.getId());
				}
				if(filter.getFrequency()!=null){
					builder.addObject("SO.FREQUENCY", filter.getFrequency());
				}
				if(null !=filter.getErrorType() && !"".equals(filter.getErrorType().trim())){
					builder.addString("SO.LAST_ERROR", filter.getErrorType());
				}
				if(null !=filter.getDayOfWeek()){
					builder.addObject(" to_char(SO.NEXT_DATE,'D')", filter.getDayOfWeek());
				}
//				if(null != filter.getFromDate()){
//					builder.addObject(" to_char(SO.NEXT_DATE,'MM/dd/yyyy')", filter.getFromDateStr());
//					builder.addSql("SO.NEXT_DATE >= ?", new Object[] { new Timestamp(filter.getFromDate().getTime())});
//				}
//				if(null != filter.getToDate()){
//					builder.addObject(" to_char(SO.NEXT_DATE,'MM/dd/yyyy')", filter.getToDateStr());
//					builder.addSql("SO.NEXT_DATE <= ?", new Object[] { new Timestamp(filter.getToDate().getTime())});
//				}
				if(true == filter.isActiveOnly()){
					builder.addObject(" SO.DELETED", "0");
				}else{
					isActiveOnly = false;
				}
			}
//			ps = conn.prepareStatement(query);
			Object[] par = builder.getParams();
			if(null != par && par.length > 0){
				ps = conn.prepareStatement(GET_ACTIVE_STANDING_ORDERS_CUST_INFO+" and "+builder.getCriteria());
			}else{
				if(isActiveOnly){
					ps = conn.prepareStatement(GET_ACTIVE_STANDING_ORDERS_CUST_INFO+ " and SO.DELETED= 0");
				}else{
					ps = conn.prepareStatement(GET_ACTIVE_STANDING_ORDERS_CUST_INFO);
				}
			}
			for (int i = 0; i < par.length; i++) {
				ps.setObject(i + 1, par[i]);
			}
			rs = ps.executeQuery();
			
			while (rs.next()) {
				FDStandingOrderInfo soInfo = new FDStandingOrderInfo();
				soInfo.setSoID(rs.getString("ID"));
				soInfo.setUserId(rs.getString("USER_ID"));
				soInfo.setCompanyName(rs.getString("COMPANY_NAME"));
				soInfo.setCustomerId(rs.getString("CUSTOMER_ID"));
				soInfo.setAddress(rs.getString("ADDRESS"));
				soInfo.setBusinessPhone(rs.getString("BUSINESS_PHONE"));
				soInfo.setCellPhone(rs.getString("CELL_PHONE"));
				soInfo.setNextDate( rs.getDate("NEXT_DATE") );
				soInfo.setFrequency(rs.getInt("FREQUENCY"));
				soInfo.setLastError(rs.getString("LAST_ERROR"));
				soInfo.setErrorHeader(rs.getString("ERROR_HEADER"));
				soInfo.setStartTime(rs.getTime("START_TIME"));
				soInfo.setEndTime(rs.getTime("END_TIME"));
				soOrders.add( soInfo );
			}
	
			rs.close();
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}
		
		return infoList;
	}


	private static final String CLEAR_ERRORS_STANDING_ORDERS =
		"UPDATE CUST.STANDING_ORDER SO SET SO.LAST_ERROR = null, SO.ERROR_HEADER=null, SO.ERROR_DETAIL= null WHERE ";
	public void clearStandingOrderErrors(Connection conn,String[] soIDs) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		CriteriaBuilder builder = new CriteriaBuilder();
		try {
			
			builder.addInString("SO.ID", soIDs);
			
			Object[] par = builder.getParams();
			if(null != par && par.length > 0){
				ps = conn.prepareStatement(CLEAR_ERRORS_STANDING_ORDERS+builder.getCriteria());
			}
			for (int i = 0; i < par.length; i++) {
				ps.setObject(i + 1, par[i]);
			}
			int total = ps.executeUpdate();			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 finally {
				if(rs != null){
					rs.close();
				}
				if(ps != null) {
					ps.close();
				}
			}
	}
	
	private static final String GET_FAILED_STANDING_ORDERS_CUST_INFO =
		"select  so.id ,cl.name, c.user_id ,NVL(A.COMPANY_NAME,'--') as COMPANY_NAME ,SO.NEXT_DATE ,SO.FREQUENCY,SO.START_TIME,SO.END_TIME,SO.ERROR_HEADER ,SO.CUSTOMER_ID,"+
		"A.ADDRESS1||', '||a.ADDRESS2||', '||a.APARTMENT||', '||a.CITY||', '||a.STATE||', '||a.ZIP as ADDRESS,NVL(CI.BUSINESS_PHONE||'-'||CI.BUSINESS_EXT,'--') as BUSINESS_PHONE,"+
		"NVL(CI.CELL_PHONE,'--') as CELL_PHONE, MAX(AL.TIMESTAMP) as FAILED_ON,case when pm.id is null then 'Not Exists' else 'Exists' end  as PAYMENT_METHOD "+
		"from cust.activity_log al,cust.address a,cust.paymentmethod pm, cust.customerinfo ci,cust.customer c,CUST.STANDING_ORDER so,CUST.CUSTOMERLIST cl "+ 
		"where  AL.ACTIVITY_ID='SO-Failed' and so.id=AL.STANDINGORDER_ID and SO.CUSTOMERLIST_ID=CL.ID and SO.CUSTOMER_ID=AL.CUSTOMER_ID and SO.ADDRESS_ID=a.id(+) and SO.PAYMENTMETHOD_ID=pm.id(+) "+
		"and c.id=ci.customer_id and so.customer_id=c.id and SO.DELETED='0' and SO.ERROR_HEADER is not null group by so.id ,cl.name,c.user_id  ,NVL(CI.BUSINESS_PHONE||'-'||CI.BUSINESS_EXT,'--') , "+
		"NVL(CI.CELL_PHONE,'--') ,A.COMPANY_NAME ,SO.NEXT_DATE  ,SO.FREQUENCY,SO.START_TIME,SO.END_TIME,SO.CUSTOMER_ID ,A.ADDRESS1||', '||a.ADDRESS2||', '||a.APARTMENT||', '||a.CITY||', '||a.STATE||', '||a.ZIP , "+
		"case when pm.id is null then 'Not Exists' else 'Exists' end,SO.ERROR_HEADER order by MAX(AL.TIMESTAMP)  desc";
	
	public FDStandingOrderInfoList getFailedStandingOrdersCustInfo(Connection conn) throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
	
		List<FDStandingOrderInfo> soOrders = new ArrayList<FDStandingOrderInfo>();
		FDStandingOrderInfoList infoList = new FDStandingOrderInfoList(soOrders);
	
		try {
			/*String query = 
				"select  so.id,c.user_id,A.COMPANY_NAME,SO.NEXT_DATE ,SO.CUSTOMER_ID,SO.FREQUENCY, SO.ERROR_HEADER,SO.LAST_ERROR,SO.START_TIME,SO.END_TIME," +
				" A.ADDRESS1||', '||a.ADDRESS2||', '||a.APARTMENT||', '||a.CITY||', '||a.STATE||', '||a.ZIP as address," +
				" NVL(CI.BUSINESS_PHONE||'-'||CI.BUSINESS_EXT,'') as BUSINESS_PHONE,NVL(CI.CELL_PHONE,'') as CELL_PHONE" +
				" from cust.address a, cust.customerinfo ci,cust.customer c,CUST.STANDING_ORDER so " +
				" where SO.ADDRESS_ID=a.id(+) and c.id=ci.customer_id and so.customer_id=c.id  ";*/
//			ps = conn.prepareStatement(GET_ACTIVE_STANDING_ORDERS_CUST_INFO);
			
			
			ps = conn.prepareStatement(GET_FAILED_STANDING_ORDERS_CUST_INFO);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				FDStandingOrderInfo soInfo = new FDStandingOrderInfo();
				soInfo.setSoID(rs.getString("ID"));
				soInfo.setSoName(rs.getString("NAME"));
				soInfo.setUserId(rs.getString("USER_ID"));
				soInfo.setCompanyName(rs.getString("COMPANY_NAME"));
				soInfo.setCustomerId(rs.getString("CUSTOMER_ID"));
				soInfo.setAddress(rs.getString("ADDRESS"));
				soInfo.setBusinessPhone(rs.getString("BUSINESS_PHONE"));
				soInfo.setCellPhone(rs.getString("CELL_PHONE"));
				soInfo.setNextDate( rs.getDate("NEXT_DATE") );
				soInfo.setFrequency(rs.getInt("FREQUENCY"));
//				soInfo.setLastError(rs.getString("LAST_ERROR"));
				soInfo.setErrorHeader(rs.getString("ERROR_HEADER"));
				soInfo.setStartTime(rs.getTime("START_TIME"));
				soInfo.setEndTime(rs.getTime("END_TIME"));
				soInfo.setFailedOn(rs.getTimestamp("FAILED_ON"));
				soInfo.setPaymentMethod(rs.getString("PAYMENT_METHOD"));
				soOrders.add( soInfo );
			}
	
			rs.close();
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}
		
		return infoList;
	}
	
	
	private static final String GET_MECHANICAL_FAILED_STANDING_ORDERS_CUST_INFO =
		"select  so.id ,cl.name,c.user_id ,NVL(A.COMPANY_NAME,'--') as COMPANY_NAME,	SO.CUSTOMER_ID ,	SO.START_TIME, SO.END_TIME, A.ADDRESS1||', '||a.ADDRESS2||', '||a.APARTMENT||', '||a.CITY||', '||a.STATE||', '||a.ZIP as ADDRESS,"+
		"NVL(CI.BUSINESS_PHONE||'-'||CI.BUSINESS_EXT,'--') as BUSINESS_PHONE,		NVL(CI.CELL_PHONE,'--') as CELL_PHONE,	SO.NEXT_DATE "+ 
		"from cust.address a,cust.customerinfo ci,cust.customer c,CUST.STANDING_ORDER so,CUST.CUSTOMERLIST cl,CUST.SO_HOLIDAY_ALT_DATE soh "+ 
		"where 	SO.ADDRESS_ID=a.id(+) and c.id=ci.customer_id and so.customer_id=c.id and SO.CUSTOMERLIST_ID=CL.ID and SO.ERROR_HEADER is null and SO.DELETED='0' and SO.NEXT_DATE <= trunc( sysdate+7) "+
		" and SO.NEXT_DATE=SOH.CURRENT_DELIVERY_DATE(+) and so.id=SOH.SO_ID(+)  and (SOH.ALTERNATE_DELIVERY_DATE is null OR SOH.ALTERNATE_DELIVERY_DATE <= trunc( sysdate+7))  order by so.next_date desc";
	
	public FDStandingOrderInfoList getMechanicalFailedStandingOrdersCustInfo(Connection conn) throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
	
		List<FDStandingOrderInfo> soOrders = new ArrayList<FDStandingOrderInfo>();
		FDStandingOrderInfoList infoList = new FDStandingOrderInfoList(soOrders);
	
		try {
			/*String query = 
				"select  so.id,c.user_id,A.COMPANY_NAME,SO.NEXT_DATE ,SO.CUSTOMER_ID,SO.FREQUENCY, SO.ERROR_HEADER,SO.LAST_ERROR,SO.START_TIME,SO.END_TIME," +
				" A.ADDRESS1||', '||a.ADDRESS2||', '||a.APARTMENT||', '||a.CITY||', '||a.STATE||', '||a.ZIP as address," +
				" NVL(CI.BUSINESS_PHONE||'-'||CI.BUSINESS_EXT,'') as BUSINESS_PHONE,NVL(CI.CELL_PHONE,'') as CELL_PHONE" +
				" from cust.address a, cust.customerinfo ci,cust.customer c,CUST.STANDING_ORDER so " +
				" where SO.ADDRESS_ID=a.id(+) and c.id=ci.customer_id and so.customer_id=c.id  ";*/
//			ps = conn.prepareStatement(GET_ACTIVE_STANDING_ORDERS_CUST_INFO);
			
			
			ps = conn.prepareStatement(GET_MECHANICAL_FAILED_STANDING_ORDERS_CUST_INFO);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				FDStandingOrderInfo soInfo = new FDStandingOrderInfo();
				soInfo.setSoID(rs.getString("ID"));
				soInfo.setSoName(rs.getString("NAME"));
				soInfo.setUserId(rs.getString("USER_ID"));
				soInfo.setCompanyName(rs.getString("COMPANY_NAME"));
				soInfo.setCustomerId(rs.getString("CUSTOMER_ID"));
				soInfo.setStartTime(rs.getTime("START_TIME"));
				soInfo.setEndTime(rs.getTime("END_TIME"));
				soInfo.setAddress(rs.getString("ADDRESS"));
				soInfo.setBusinessPhone(rs.getString("BUSINESS_PHONE"));
				soInfo.setCellPhone(rs.getString("CELL_PHONE"));
				soInfo.setNextDate( rs.getDate("NEXT_DATE") );
//				soInfo.setFrequency(rs.getInt("FREQUENCY"));
//				soInfo.setLastError(rs.getString("LAST_ERROR"));
//				soInfo.setErrorHeader(rs.getString("ERROR_HEADER"));
//				soInfo.setStartTime(rs.getTime("START_TIME"));
//				soInfo.setEndTime(rs.getTime("END_TIME"));
//				soInfo.setFailedOn(rs.getTime("FAILED_ON"));
//				soInfo.setPaymentMethod(rs.getString("PAYMENT_METHOD"));
				soOrders.add( soInfo );
			}
	
			rs.close();
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}
		
		return infoList;
	}

	private static final String GET_STANDING_ORDERS_ALTERNATE_DELIVERY_DATES =	"select * from CUST.SO_HOLIDAY_ALT_DATE WHERE CURRENT_DELIVERY_DATE > SYSDATE-1 ORDER BY CURRENT_DELIVERY_DATE";
	public Map<Date,Date> getStandingOrdersAlternateDeliveryDates(Connection conn) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<Date, Date> map = new HashMap<Date,Date>();
		try {			
			ps = conn.prepareStatement(GET_STANDING_ORDERS_ALTERNATE_DELIVERY_DATES);
			rs = ps.executeQuery();			
			while (rs.next()) {
				Date actualDate = rs.getDate(CURRENT_DELIVERY_DATE) ;
				Date alternateDate = rs.getDate(ALTERNATE_DELIVERY_DATE) ;
				map.put(actualDate, alternateDate);
			}	
			rs.close();
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}		
		
		return map;
	}

	public List<FDStandingOrderAltDeliveryDate> getStandingOrderAltDeliveryDates(Connection conn) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<FDStandingOrderAltDeliveryDate> altDeliveryDates = new ArrayList<FDStandingOrderAltDeliveryDate>();
		try {			
			ps = conn.prepareStatement(GET_STANDING_ORDERS_ALTERNATE_DELIVERY_DATES);
			rs = ps.executeQuery();			
			
			while (rs.next()) {
				FDStandingOrderAltDeliveryDate altDeliveryDateInfo = populateAltDeliveryInfo(rs);
				altDeliveryDates.add(altDeliveryDateInfo);
			}	
			rs.close();
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}		
		return altDeliveryDates;
	}

	private static final String GET_STANDING_ORDERS_ALTERNATE_DELIVERY_DATE_BY_ID =	"select * from CUST.SO_HOLIDAY_ALT_DATE where id=? ";
	public FDStandingOrderAltDeliveryDate getStandingOrderAltDeliveryDateById(Connection conn,String id) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		FDStandingOrderAltDeliveryDate altDeliveryDateInfo = null;
		try {			
			ps = conn.prepareStatement(GET_STANDING_ORDERS_ALTERNATE_DELIVERY_DATE_BY_ID);
			ps.setString(1, id);
			rs = ps.executeQuery();			
			
			if (rs.next()) {
				altDeliveryDateInfo = populateAltDeliveryInfo(rs);
			}	
			rs.close();
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}		
		return altDeliveryDateInfo;
	}
	
	private FDStandingOrderAltDeliveryDate populateAltDeliveryInfo(ResultSet rs)
			throws SQLException {
		FDStandingOrderAltDeliveryDate altDeliveryDateInfo = new FDStandingOrderAltDeliveryDate();
		altDeliveryDateInfo.setOrigDate(rs.getDate(CURRENT_DELIVERY_DATE));
		altDeliveryDateInfo.setAltDate(rs.getDate(ALTERNATE_DELIVERY_DATE));
		altDeliveryDateInfo.setOrigStartTime(rs.getTime("DELIVERY_START_TIME"));
		altDeliveryDateInfo.setOrigEndTime(rs.getTime("DELIVERY_END_TIME"));
		altDeliveryDateInfo.setAltStartTime(rs.getTime("ALTERNATE_DELIVERY_START_TIME"));
		altDeliveryDateInfo.setAltEndTime(rs.getTime("ALTERNATE_DELIVERY_END_TIME"));
		altDeliveryDateInfo.setDescription(null==rs.getString("DESCRIPTION")?"":rs.getString("DESCRIPTION"));
		altDeliveryDateInfo.setSoId(null==rs.getString("SO_ID")?"":rs.getString("SO_ID"));
		altDeliveryDateInfo.setActionType(rs.getString("ACTION_TYPE"));
		altDeliveryDateInfo.setCreatedBy(rs.getString("CREATED_BY"));
		altDeliveryDateInfo.setModifiedBy(null==rs.getString("MODIFIED_BY")?"":rs.getString("MODIFIED_BY"));
		altDeliveryDateInfo.setCreatedTime(rs.getTimestamp("CREATED_TIME"));
		altDeliveryDateInfo.setModifiedTime(rs.getTimestamp("MODIFIED_TIME"));
		altDeliveryDateInfo.setId(rs.getString("ID"));
		return altDeliveryDateInfo;
	}

	private static final String GET_STANDING_ORDERS_GLOBAL_ALTERNATE_DELIVERY_DATES =	"select * from CUST.SO_HOLIDAY_ALT_DATE WHERE SO_ID IS NULL ORDER BY CURRENT_DELIVERY_DATE";
	public Map<Date, List<FDStandingOrderAltDeliveryDate>> getStandingOrdersGlobalAlternateDeliveryDates(Connection conn) throws SQLException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<Date, List<FDStandingOrderAltDeliveryDate>> map = new HashMap<Date, List<FDStandingOrderAltDeliveryDate>>();
		try {			
			ps = conn.prepareStatement(GET_STANDING_ORDERS_GLOBAL_ALTERNATE_DELIVERY_DATES);
			rs = ps.executeQuery();			
			while (rs.next()) {
				Date actualDate = rs.getDate(CURRENT_DELIVERY_DATE) ;
				List<FDStandingOrderAltDeliveryDate> altDates = null;
				if(null != map.get(actualDate)){
					altDates = map.get(actualDate);
				}else{
					altDates = new ArrayList<FDStandingOrderAltDeliveryDate>();
				}
				FDStandingOrderAltDeliveryDate altDeliveryDateInfo = populateAltDeliveryInfo(rs);
				altDates.add(altDeliveryDateInfo);
				map.put(actualDate, altDates);
			}	
			rs.close();
			ps.close();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}		
		
		return map;
	}
	
	private static final String INSERT_STANDINGORDER_ALTERNATE_DELIVERY_DATE = "INSERT INTO CUST.SO_HOLIDAY_ALT_DATE (ID,CURRENT_DELIVERY_DATE, ALTERNATE_DELIVERY_DATE, DESCRIPTION, SO_ID,DELIVERY_START_TIME,DELIVERY_END_TIME,ALTERNATE_DELIVERY_START_TIME,ALTERNATE_DELIVERY_END_TIME,ACTION_TYPE,CREATED_BY,MODIFIED_BY,CREATED_TIME,MODIFIED_TIME) VALUES( CUST.SYSTEM_SEQ.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,? ) ";
	
	public void addStandingOrderAltDeliveryDate(Connection conn, FDStandingOrderAltDeliveryDate altDeliveryDate) throws SQLException {
		
		PreparedStatement ps = null;		
		try {
			ps = conn.prepareStatement(INSERT_STANDINGORDER_ALTERNATE_DELIVERY_DATE);
			int i=1;			
			ps.setDate(i++, altDeliveryDate.getOrigDate() != null ? new java.sql.Date( altDeliveryDate.getOrigDate().getTime() ) : null);
			ps.setDate(i++, altDeliveryDate.getAltDate() != null ? new java.sql.Date( altDeliveryDate.getAltDate().getTime() ) : null);
			ps.setString(i++, altDeliveryDate.getDescription());
			ps.setString(i++,altDeliveryDate.getSoId());
			ps.setTimestamp(i++, null!=altDeliveryDate.getOrigStartTime()?new java.sql.Timestamp(altDeliveryDate.getOrigStartTime().getTime()):null);
			ps.setTimestamp(i++, null!=altDeliveryDate.getOrigEndTime()?new java.sql.Timestamp(altDeliveryDate.getOrigEndTime().getTime()):null);
			ps.setTimestamp(i++, null!=altDeliveryDate.getAltStartTime()?new java.sql.Timestamp(altDeliveryDate.getAltStartTime().getTime()):null);
			ps.setTimestamp(i++, null!=altDeliveryDate.getAltEndTime()?new java.sql.Timestamp(altDeliveryDate.getAltEndTime().getTime()):null);
//			ps.setString(i++, null!=altDeliveryDate.getActionType()?altDeliveryDate.getActionType().getName():null);
			ps.setString(i++, altDeliveryDate.getActionType());
			ps.setString(i++,altDeliveryDate.getCreatedBy());
			ps.setString(i++,altDeliveryDate.getModifiedBy());
			ps.setTimestamp(i++, null !=altDeliveryDate.getCreatedTime()?new java.sql.Timestamp(altDeliveryDate.getCreatedTime().getTime()):null);
			ps.setTimestamp(i++, null !=altDeliveryDate.getModifiedTime()?new java.sql.Timestamp(altDeliveryDate.getModifiedTime().getTime()):null);
			
			ps.execute();	
			ps.close();
		
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
		}
	}

	public void addStandingOrderAltDeliveryDates(Connection conn, List<FDStandingOrderAltDeliveryDate> altDeliveryDates) throws SQLException {
		
		PreparedStatement ps = null;		
		try {
			ps = conn.prepareStatement(INSERT_STANDINGORDER_ALTERNATE_DELIVERY_DATE);
			for (Iterator<FDStandingOrderAltDeliveryDate> iterator = altDeliveryDates.iterator(); iterator.hasNext();) {
				FDStandingOrderAltDeliveryDate altDeliveryDate = iterator.next();				
				int i=1;			
				ps.setDate(i++, altDeliveryDate.getOrigDate() != null ? new java.sql.Date( altDeliveryDate.getOrigDate().getTime() ) : null);
				ps.setDate(i++, altDeliveryDate.getAltDate() != null ? new java.sql.Date( altDeliveryDate.getAltDate().getTime() ) : null);
				ps.setString(i++, altDeliveryDate.getDescription());
				ps.setString(i++,altDeliveryDate.getSoId());
				ps.setTimestamp(i++, null!=altDeliveryDate.getOrigStartTime()?new java.sql.Timestamp(altDeliveryDate.getOrigStartTime().getTime()):null);
				ps.setTimestamp(i++, null!=altDeliveryDate.getOrigEndTime()?new java.sql.Timestamp(altDeliveryDate.getOrigEndTime().getTime()):null);
				ps.setTimestamp(i++, null!=altDeliveryDate.getAltStartTime()?new java.sql.Timestamp(altDeliveryDate.getAltStartTime().getTime()):null);
				ps.setTimestamp(i++, null!=altDeliveryDate.getAltEndTime()?new java.sql.Timestamp(altDeliveryDate.getAltEndTime().getTime()):null);
	//			ps.setString(i++, null!=altDeliveryDate.getActionType()?altDeliveryDate.getActionType().getName():null);
				ps.setString(i++, altDeliveryDate.getActionType());
				ps.setString(i++,altDeliveryDate.getCreatedBy());
				ps.setString(i++,altDeliveryDate.getModifiedBy());
				ps.setTimestamp(i++, null !=altDeliveryDate.getCreatedTime()?new java.sql.Timestamp(altDeliveryDate.getCreatedTime().getTime()):null);
				ps.setTimestamp(i++, null !=altDeliveryDate.getModifiedTime()?new java.sql.Timestamp(altDeliveryDate.getModifiedTime().getTime()):null);
				ps.addBatch();			
			}
			
			ps.executeBatch();	
			ps.close();
		
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
		}
	}
	private static final String UPDATE_STANDINGORDER_ALTERNATE_DELIVERY_DATE = "UPDATE CUST.SO_HOLIDAY_ALT_DATE SET CURRENT_DELIVERY_DATE= ?, ALTERNATE_DELIVERY_DATE=?, DESCRIPTION=?, SO_ID=?,DELIVERY_START_TIME=?,DELIVERY_END_TIME=?,ALTERNATE_DELIVERY_START_TIME=?,ALTERNATE_DELIVERY_END_TIME=?,ACTION_TYPE=?,MODIFIED_BY=?,MODIFIED_TIME=? WHERE ID=?";
	
	public void updateStandingOrderAltDeliveryDate(Connection conn, FDStandingOrderAltDeliveryDate altDeliveryDate) throws SQLException {
		
		PreparedStatement ps = null;		
		try {
			ps = conn.prepareStatement(UPDATE_STANDINGORDER_ALTERNATE_DELIVERY_DATE);
			int i=1;			
			ps.setDate(i++, altDeliveryDate.getOrigDate() != null ? new java.sql.Date( altDeliveryDate.getOrigDate().getTime() ) : null);
			ps.setDate(i++, altDeliveryDate.getAltDate() != null ? new java.sql.Date( altDeliveryDate.getAltDate().getTime() ) : null);
			ps.setString(i++, altDeliveryDate.getDescription());
			ps.setString(i++,altDeliveryDate.getSoId());
			ps.setTimestamp(i++, null!=altDeliveryDate.getOrigStartTime()?new java.sql.Timestamp(altDeliveryDate.getOrigStartTime().getTime()):null);
			ps.setTimestamp(i++, null!=altDeliveryDate.getOrigEndTime()?new java.sql.Timestamp(altDeliveryDate.getOrigEndTime().getTime()):null);
			ps.setTimestamp(i++, null!=altDeliveryDate.getAltStartTime()?new java.sql.Timestamp(altDeliveryDate.getAltStartTime().getTime()):null);
			ps.setTimestamp(i++, null!=altDeliveryDate.getAltEndTime()?new java.sql.Timestamp(altDeliveryDate.getAltEndTime().getTime()):null);
//			ps.setString(i++, null!=altDeliveryDate.getActionType()?altDeliveryDate.getActionType().getName():null);
			ps.setString(i++, altDeliveryDate.getActionType());
			ps.setString(i++,altDeliveryDate.getModifiedBy());
			ps.setTimestamp(i++, null !=altDeliveryDate.getModifiedTime()?new java.sql.Timestamp(altDeliveryDate.getModifiedTime().getTime()):null);
			ps.setString(i++,altDeliveryDate.getId());
			
			
			ps.execute();	
			ps.close();
		
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
		}
	}


	private static final String LOCK_STANDING_ORDER = "UPDATE CUST.STANDING_ORDER SET LOCK_ID = ?, LOCK_DATE = SYSDATE WHERE (LOCK_ID IS NULL OR SYSDATE - LOCK_DATE > ? / 86400 ) AND ID = ?";
	
	public boolean lockStandingOrder(Connection conn, String soPk, String lockId, int lockTimeOut) throws SQLException {
		
		PreparedStatement ps = null;		
		try {
			ps = conn.prepareStatement(LOCK_STANDING_ORDER);
						
			ps.setString(1, lockId);
			ps.setInt(2, lockTimeOut);
			ps.setString(3, soPk);
			
			ps.execute();
			boolean success = ps.getUpdateCount() == 1;
			ps.close();

			return success;
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
		}
	}


	private static final String UNLOCK_STANDING_ORDER = "UPDATE CUST.STANDING_ORDER SET LOCK_ID = NULL, LOCK_DATE = NULL WHERE LOCK_ID = ? AND SYSDATE - LOCK_DATE <= ? / 86400 AND ID = ?";
	
	public boolean unlockStandingOrder(Connection conn, String soPk, String lockId, int lockTimeOut) throws SQLException {
		
		PreparedStatement ps = null;		
		try {
			ps = conn.prepareStatement(UNLOCK_STANDING_ORDER);
						
			ps.setString(1, lockId);
			ps.setInt(2, lockTimeOut);
			ps.setString(3, soPk);
			
			ps.execute();
			boolean success = ps.getUpdateCount() == 1;
			ps.close();

			return success;
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
		}
	}
	
	private static final String GET_LOCKID_LOCKED_STANDING_ORDER = "SELECT LOCK_ID FROM CUST.STANDING_ORDER WHERE (LOCK_ID IS NOT NULL AND SYSDATE - LOCK_DATE <= ? / 86400 ) AND ID = ?";
	
	public String getLockIdStandingOrder(Connection conn, String soPk, int lockTimeOut) throws SQLException {
		
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement(GET_LOCKID_LOCKED_STANDING_ORDER);
						
			ps.setInt(1, lockTimeOut);
			ps.setString(2, soPk);

			rs = ps.executeQuery();
			
			String lockId = null;
			if (rs.next()) {
				lockId = rs.getString("LOCK_ID");
			}
	
			rs.close();
			ps.close();
		
			return lockId;

		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}
	}
	

	private static final String SELECT_DUPLICATE_WARNING_INFO = "select * from CUST.DUPLICATE_SOI_EMAIL_SENT where CUSTOMER_ID=?";
	
	public List<String> getDuplicateWarningInfos(Connection conn,String custId) throws SQLException {
		
		List<String> infos=new ArrayList<String>();
		
		PreparedStatement ps=null;
		ResultSet rs = null;
		try{
			ps=conn.prepareStatement(SELECT_DUPLICATE_WARNING_INFO);
			
			ps.setString(1, custId);		
			rs=ps.executeQuery();
			
			while(rs.next()){
				infos.add(rs.getString("HASH"));
			}
			
			rs.close();
			ps.close();
		}finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}
		
		return infos;
	}
	
	private static final String INSERT_DUPLICATE_WARNING_INFO = "insert into CUST.DUPLICATE_SOI_EMAIL_SENT (CUSTOMER_ID, HASH, SEND_DATE) values(?,?,?)";
	
	public void storeDuplicateWarningInfo(Connection conn,String custId,String hash) throws SQLException{
		
		PreparedStatement ps=null;
		try{
			ps=conn.prepareStatement(INSERT_DUPLICATE_WARNING_INFO);
			ps.setString(1, custId);
			ps.setString(2, hash);
			ps.setDate(3, new java.sql.Date(new Date().getTime()));
			
			ps.execute();
			ps.close();
		}finally {
			if(ps != null) {
				ps.close();
			}
		}
	}


	private static final String DELETE_STANDINGORDER_ALTERNATE_DELIVERY_DATE = "DELETE FROM CUST.SO_HOLIDAY_ALT_DATE WHERE CURRENT_DELIVERY_DATE=?";
	
	public void deleteStandingOrderAltDeliveryDate(Connection conn, FDStandingOrderAltDeliveryDate altDeliveryDate) throws SQLException {
		
		PreparedStatement ps = null;		
		try {
			ps = conn.prepareStatement(DELETE_STANDINGORDER_ALTERNATE_DELIVERY_DATE);
						
			ps.setDate(1, altDeliveryDate.getOrigDate() != null ? new java.sql.Date( altDeliveryDate.getOrigDate().getTime() ) : null);
			
			ps.execute();	
			ps.close();
		
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
		}
	}

	private static final String INSERT_COREMETRICS_USERINFO = "INSERT INTO CUST.COREMETRICS_USERINFO values (?, ?)";
	
	public void insertIntoCoremetricsUserinfo(Connection conn, FDUserI fdUser, int flag) throws SQLException {
		
		PreparedStatement ps = null;		
		try {
			ps = conn.prepareStatement(INSERT_COREMETRICS_USERINFO);
						
			ps.setString(1, fdUser.getPrimaryKey());
			ps.setInt(2, flag);
			
			ps.execute();	
			ps.close();
		
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
		}
	}

	private static final String GET_COREMETRICS_USERINFO = "SELECT id FROM CUST.COREMETRICS_USERINFO WHERE id = ?";
	
	public boolean getCoremetricsUserinfo(Connection conn, FDUserI fdUser) throws SQLException {
		
		PreparedStatement ps = null;		
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(GET_COREMETRICS_USERINFO);
						
			ps.setString(1, fdUser.getPrimaryKey());
			
			rs=ps.executeQuery();
			
			while(rs.next()){
				return true;
			}
			
			rs.close();
			ps.close();
		}finally {
			if(rs != null){
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
		}
		return false;
	}
	
	private static final String DELETE_STANDINGORDER_ALTERNATE_DELIVERY_DATE_BY_ID = "DELETE FROM CUST.SO_HOLIDAY_ALT_DATE WHERE ID =?";
	
	public void deleteStandingOrderAltDeliveryDateById(Connection conn, String[] altIds) throws SQLException {
		
		if(null != altIds && altIds.length > 0){				
			PreparedStatement ps = null;		
			try {
				ps = conn.prepareStatement(DELETE_STANDINGORDER_ALTERNATE_DELIVERY_DATE_BY_ID);
				for (int i = 0; i < altIds.length; i++) {						
					ps.setString(1,altIds[i]);
					ps.addBatch();
				}			
				ps.executeBatch();	
				ps.close();			
			} catch (SQLException exc) {
				throw exc;
			} finally {
				if(ps != null) {
					ps.close();
				}
			}
		}
	}
	
	public boolean checkIfAlreadyExists(Connection conn,FDStandingOrderAltDeliveryDate altDate) throws SQLException{
		boolean isAlreadyExists = false;
		PreparedStatement ps = null;	
		ResultSet rs = null;
		try {
			if(null !=altDate.getId() && !"".equals(altDate.getId())){
				if(null !=altDate.getSoId() && !"".equals(altDate.getSoId())){
					ps = conn.prepareStatement("SELECT * FROM CUST.SO_HOLIDAY_ALT_DATE WHERE CURRENT_DELIVERY_DATE =? AND SO_ID=? AND ID <> ?");
					ps.setDate(1,new java.sql.Date(altDate.getOrigDate().getTime()));
					ps.setString(2,altDate.getSoId());
					ps.setString(3,altDate.getId());
				}else{
					ps = conn.prepareStatement("SELECT * FROM CUST.SO_HOLIDAY_ALT_DATE WHERE CURRENT_DELIVERY_DATE =? AND (SO_ID IS NULL OR SO_ID='') AND ID <> ?");
					ps.setDate(1,new java.sql.Date(altDate.getOrigDate().getTime()));
					ps.setString(2,altDate.getId());
				}
			}else{
				if(null !=altDate.getSoId() && !"".equals(altDate.getSoId())){
					ps = conn.prepareStatement("SELECT * FROM CUST.SO_HOLIDAY_ALT_DATE WHERE CURRENT_DELIVERY_DATE =? AND SO_ID=?");
					ps.setDate(1,new java.sql.Date(altDate.getOrigDate().getTime()));
					ps.setString(2,altDate.getSoId());
				}else{
					ps = conn.prepareStatement("SELECT * FROM CUST.SO_HOLIDAY_ALT_DATE WHERE CURRENT_DELIVERY_DATE =? AND (SO_ID IS NULL OR SO_ID='')");
					ps.setDate(1,new java.sql.Date(altDate.getOrigDate().getTime()));
				}
			}
			rs =ps.executeQuery();
			if(rs.next()){
				isAlreadyExists = true;
			}
			ps.close();
		
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if(ps != null) {
				ps.close();
			}
			if(rs != null) {
				rs.close();
			}
		}
		
		return isAlreadyExists;
	}
	
	public boolean isValidSoId(Connection conn, String soId)
			throws SQLException {
		boolean isValid = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {

			ps = conn
					.prepareStatement("SELECT * FROM CUST.STANDING_ORDER SO WHERE SO.DELETED<>1 AND SO.ID= ?");
			ps.setString(1, soId);

			rs = ps.executeQuery();
			if (rs.next()) {
				isValid = true;
			}
			ps.close();

		} catch (SQLException exc) {
			throw exc;
		} finally {
			if (ps != null) {
				ps.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return isValid;
	}
	
	public FDStandingOrderSkuResultInfo replaceSkuCode(Connection conn,
			String existingSku, String replacementSku) throws SQLException {

		FDStandingOrderSkuResultInfo fdStandingOrderSkuResultInfo = new FDStandingOrderSkuResultInfo();
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(SKU_CODE_REPLACE_SQL);
			ps.setString(1, replacementSku);
			ps.setString(2, existingSku);
			ps.executeUpdate();
			ps.close();			
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
		fdStandingOrderSkuResultInfo.setProductSkuList(displayGridForExport(
				conn, replacementSku));
		return fdStandingOrderSkuResultInfo;
	}

	private List<FDStandingOrderProductSku> displayGridForExport(
			Connection conn, String skuCodeToDisplay) throws SQLException {

		List<FDStandingOrderProductSku> productList = new ArrayList<FDStandingOrderProductSku>();
		FDStandingOrderProductSku product = null;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(SKU_CODE_GRID_DISPLAY_SQL);

			ps.setString(1, skuCodeToDisplay);

			rs = ps.executeQuery();

			while (rs.next()) {
				product = new FDStandingOrderProductSku();
				product.setCustomerEmailId(rs.getString("USER_ID"));
				product.setListId(rs.getString("LIST_ID"));
				product.setSoTemplateId(rs.getString("SO_TEMPLATE_ID"));
				product.setSkuCode(rs.getString("SKU_CODE"));
				product.setQuantity(rs.getString("QUANTITY"));
				product.setSalesUnit(rs.getString("SALES_UNIT"));
				product.setFrequency(rs.getInt("FREQUENCY"));
				product.setCustomerId(rs.getString("CUSTOMER_ID"));
				productList.add(product);
			}
			rs.close();
			ps.close();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
		}
		return productList;
	}

	public FDStandingOrderSkuResultInfo validateSkuCode(Connection conn,
			String existingSku, String replacementSku) throws SQLException {

		FDStandingOrderSkuResultInfo fdStandingOrderSkuResultInfo = new FDStandingOrderSkuResultInfo();

		if (!validateExistingSkuCode(existingSku)) {
			fdStandingOrderSkuResultInfo.setErrorMessage(existingSku + " "
					+ EXISTING_SKU_NOT_EXIST);
			return fdStandingOrderSkuResultInfo;
		}

		else if (!validateReplacementSkuCode(replacementSku)) {
			if (isSkuValidButDiscontinued) {
				fdStandingOrderSkuResultInfo
						.setErrorMessage(REPLACEMENT_SKU_DISC_OR_UNAVAILABLE);
			} else {
				fdStandingOrderSkuResultInfo.setErrorMessage(replacementSku
						+ " " + REPLACEMENT_SKU_NOT_EXIST);
			}
			return fdStandingOrderSkuResultInfo;
		}

		// Check for salesUnit of the existing and replacement SKUs
		else if (!validateSalesUnit(existingSku, replacementSku)) {
			fdStandingOrderSkuResultInfo
					.setErrorMessage("Replacement cannot be performed because Base/Sales unit does not match");
			return fdStandingOrderSkuResultInfo;
		}
		// Check for configuration of the existing and replacement SKUs
		else if (!validateConfigurationDetails(existingSku, replacementSku)) {
			fdStandingOrderSkuResultInfo
					.setErrorMessage("Replacement cannot be performed because Configuration does not matches");
			return fdStandingOrderSkuResultInfo;
		}
		fdStandingOrderSkuResultInfo.setProductSkuList(displayGridForExport(
				conn, existingSku));
		return fdStandingOrderSkuResultInfo;
	}
	
	private boolean validateConfigurationDetails(String existingSku, String replacementSku) throws SQLException {

		if (isSkuConfigurable(existingSku) == isSkuConfigurable(replacementSku)) {
			return true;
		}
		return false;
	}


	private boolean isSkuConfigurable(String skuCode) {

		FDProduct product = null;
		try {
			product = FDCachedFactory.getProduct(FDCachedFactory
					.getProductInfo(skuCode));
		} catch (FDResourceException e) {
			e.printStackTrace();
		} catch (FDSkuNotFoundException e) {
			e.printStackTrace();
		}
		return product.getVariations().length > 0 ? true : false;
	}
	
	private boolean validateSalesUnit(String existingSku, String replacementSku) throws SQLException {

		try {
			boolean isSalesUnitExist = false;
			FDSalesUnit[] existingSkuSalesUnitArray = getSkuSalesUnit(existingSku);
			FDSalesUnit[] replacementSkuSalesUnitArray = getSkuSalesUnit(replacementSku);
			
			if(validateBaseUnitAndLenght(existingSkuSalesUnitArray,replacementSkuSalesUnitArray)){		
					
				for (FDSalesUnit replacementSkuSalesUnit : replacementSkuSalesUnitArray) {
	
					for (FDSalesUnit existingSkuSalesUnit : existingSkuSalesUnitArray) {						
						if(replacementSkuSalesUnit.getName().equalsIgnoreCase(existingSkuSalesUnit.getName())){
							isSalesUnitExist = true;
							break;
						}						
					}
					if(!isSalesUnitExist){
						return false;
					}
				}
			}
			else{
				 return false;
			}
			
		} catch (FDResourceException e) {
			e.printStackTrace();
		} catch (FDSkuNotFoundException e) {
			e.printStackTrace();
		}			
		return true;
	}


	private boolean validateBaseUnitAndLenght(
			FDSalesUnit[] existingSkuSalesUnitArray,
			FDSalesUnit[] replacementSkuSalesUnitArray) {
		return existingSkuSalesUnitArray[0].getBaseUnit().equalsIgnoreCase(
				replacementSkuSalesUnitArray[0].getBaseUnit())
				&& replacementSkuSalesUnitArray.length >= existingSkuSalesUnitArray.length;
	}
	
	private FDSalesUnit[] getSkuSalesUnit(String skuCode)
			throws FDResourceException, FDSkuNotFoundException {

		FDProduct product = FDCachedFactory.getProduct(FDCachedFactory
				.getProductInfo(skuCode));

		return product.getSalesUnits();
	}
		
	private boolean validateReplacementSkuCode(String sourceSku)
			throws SQLException {
		
		boolean isSkuValid = false;
		
		try {
			FDCachedFactory.getProductInfo(sourceSku).getVersion();
			FDProductInfo fdProductInfo = FDCachedFactory.getProductInfo(sourceSku);
			if (fdProductInfo.isDiscontinued("1000","1000") == true) {//::FDX:: 
				isSkuValidButDiscontinued = true;
			}
			else{
				isSkuValid = true;
			}
		} catch (FDResourceException e) {
			e.printStackTrace();
		} catch (FDSkuNotFoundException e) {
			e.printStackTrace();
		}
		return isSkuValid;

	}

	private boolean validateExistingSkuCode(String existingSku)
			throws SQLException {

		boolean isSkuValid = false;

		try {
			FDCachedFactory.getProductInfo(existingSku).getVersion();
			isSkuValid = true;
		} catch (FDResourceException e) {
			e.printStackTrace();
		} catch (FDSkuNotFoundException e) {
			e.printStackTrace();
		}
		return isSkuValid;

	}


	public void persistUnavailableDetailsToDB(Connection conn,
			List<Result> resultsList) throws SQLException {

		PreparedStatement pstmt = null;
		try {
			
			double run_instance = calculateRunInstance(conn);
			
			run_instance = run_instance == 0 ? 1 : run_instance + 1;
			
			pstmt = conn.prepareStatement(UNAV_ITEM_SQL);

			for (Result result : resultsList) {

				//If the standing order is skipped or in error state skip it.
				if (result.getStatus() != Status.SKIPPED && !result.isError()) {

					Map<FDCartLineI, UnAvailabilityDetails> details = result
							.getUnavailabilityDetails();
					if (details != null) {
						for (Map.Entry<FDCartLineI, UnAvailabilityDetails> entry : details
								.entrySet()) {
							pstmt.setDate(1, new java.sql.Date(new Date().getTime()));
							pstmt.setDouble(2, run_instance);	
							pstmt.setDate(3, new java.sql.Date(result.getRequestedDate().getTime()));					
							pstmt.setString(4, result.getSoId());
							pstmt.setString(5, result.getSaleId());
							pstmt.setString(6, result.getCustId());
							pstmt.setString(7, entry.getKey().getSkuCode());
							pstmt.setString(8, entry.getKey()
									.getMaterialNumber());
							pstmt.setDouble(9, entry.getValue().getUnavailQty());
							pstmt.setString(10, entry.getKey().getSalesUnit());
							pstmt.setTimestamp(11, new java.sql.Timestamp(
									new Date().getTime()));
							pstmt.setString(12, entry.getValue().getReason()
									.toString());
							pstmt.setString(13, entry.getValue().getAltSkucode());
							pstmt.setString(14, entry.getKey().getDescription());
							pstmt.addBatch();
						}
					}
				}

			}
			pstmt.executeBatch();
			
		} 
		catch (SQLException exc) {
			throw exc;
		} 	
		finally {
			if(pstmt != null)
			pstmt.close();
		}
	}

	private double calculateRunInstance(Connection conn) throws SQLException {

		double run_instance = 0;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(GET_RUNINSTANCE_SQL);		
			if (rs.next()) {
				run_instance = rs.getInt("maxInstance");
			}
		} 		
		catch (SQLException exc) {
			throw exc;
		} 
		finally {
			if(rs != null)
			rs.close();
			if(stmt != null)
			stmt.close();
		}
		return run_instance;
	}


	public UnavDetailsReportingBean getDetailsForReportGeneration(
			Connection conn) throws SQLException {

		PreparedStatement pstmt = null;
		UnavDetailsReportingBean reportingBean = new UnavDetailsReportingBean();
		String[] errorTypeList = new String[] {"DISC","UNAV","GENERAL","ATP"};
		
		try {

			double run_instance = calculateRunInstance(conn);
			pstmt = conn.prepareStatement(UNAV_ITEM_DETAILS_SQL);
			for (String str : errorTypeList) {
				getNotAvailableProductsByErrorType(pstmt, reportingBean,
						run_instance, str);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			if(pstmt != null)
			pstmt.close();
		}
		return reportingBean;
	}


	private void getNotAvailableProductsByErrorType(PreparedStatement pstmt,
			UnavDetailsReportingBean reportingBean, double run_instance,String errorType)
			throws SQLException {
		
		InventoryMapInfoBean inventoryMapInfoBean;
		List<InventoryMapInfoBean> inventoryMapInfoBeanList = new ArrayList<InventoryMapInfoBean>();
		ResultSet rs = null;
		
			try{
			pstmt.setDouble(1, run_instance);
			pstmt.setString(2,errorType);	
			
			rs = pstmt.executeQuery();
			
			while (rs.next()){
				inventoryMapInfoBean = new InventoryMapInfoBean();
				inventoryMapInfoBean.setProductName(rs.getString("PRODUCT_NAME"));
				inventoryMapInfoBean.setMaterialNum(rs.getString("MATERIAL_NUM"));
				inventoryMapInfoBean.setSkuCode(rs.getString("SKU_CODE"));
				inventoryMapInfoBean.setQnty(rs.getDouble("QUANTITY"));
				inventoryMapInfoBeanList.add(inventoryMapInfoBean);
			}
			
			if(errorType.equalsIgnoreCase("DISC")){
				reportingBean.getDiscProductInfoBeanList().addAll(inventoryMapInfoBeanList);
			}
			else if(errorType.equalsIgnoreCase("UNAV")){
				reportingBean.getUnavProductInfoBeanList().addAll(inventoryMapInfoBeanList);
			}
			else if(errorType.equalsIgnoreCase("GENERAL")){
				reportingBean.getRestrictedProductInfoBeanList().addAll(inventoryMapInfoBeanList);
			}
			else if(errorType.equalsIgnoreCase("ATP")){
				reportingBean.getAtpFailureProductInfoBeanList().addAll(inventoryMapInfoBeanList);
			}
		}
		catch (SQLException exc) {
			throw exc;
		} 
		finally{
			rs.close();
		}		
	}

}
