package com.freshdirect.fdstore.customer.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Category;

import com.freshdirect.customer.EnumAccountActivityType;
import com.freshdirect.fdstore.customer.FDCustomerReservationInfo;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.log.LoggerFactory;

public class AdminToolsDAO {	
	
	@SuppressWarnings( "unused" )
	private static Category LOGGER = LoggerFactory.getInstance(AdminToolsDAO.class);
	
	private static final String FIX_BROKEN_ACCOUNT_QUERY = "insert into cust.fduser(id, cookie, created, zipcode, fdcustomer_id)"
														   + "select cust.system_seq.nextval, " 
														   + "dbms_random.string('X',12) || to_char(sysdate,'MMSSSSSYYYYSSDD') ||   dbms_random.string('X',10), "
														   + "sysdate, "
														   + "zip, "
														   + "fdcustid "
														   + "from ("
														   + "select "
														   + "fdc.fdcustid, "
														   + "max(a.zip) zip "
														   + "from cust.customer c, cust.customerinfo ci, cust.address a, "
														   + "(select id as fdcustid, erp_customer_id, depot_code from cust.fdcustomer where id not in " 
														   + "(select fdcustomer_id from cust.fduser where fdcustomer_id is not null)) fdc "
														   + "where c.id=fdc.erp_customer_id and c.id=ci.customer_id and c.id=a.customer_id(+) "
														   + "group by fdc.fdcustid) ";

	public static int fixBrokenAccounts( Connection conn ) throws SQLException {
		PreparedStatement ps = conn.prepareStatement( FIX_BROKEN_ACCOUNT_QUERY );
		int updateCount = ps.executeUpdate();
		return updateCount;
	}
	
	private static final String CREATE_ACITIVITY_LOG = "INSERT INTO CUST.ACTIVITY_LOG(TIMESTAMP, CUSTOMER_ID, ACTIVITY_ID,SOURCE,INITIATOR,NOTE)VALUES(sysdate,?,?,?,?,? )";

	public static int logCancelledReservations( Connection conn, List<FDCustomerReservationInfo> resvs, String initiator, String notes ) throws SQLException {
		PreparedStatement ps = conn.prepareStatement( CREATE_ACITIVITY_LOG );

		for ( FDCustomerReservationInfo info : resvs ) {
			ps.setString( 1, info.getIdentity().getErpCustomerPK() );
			ps.setString( 2, EnumAccountActivityType.CANCEL_PRE_RESERVATION.getCode() );
			ps.setString( 3, "CSR" );
			ps.setString( 4, initiator );
			ps.setString( 5, getReservationActivityNotes( info ) + " " + notes );
			ps.addBatch();
		}
		ps.executeBatch();
		return 0;
	}
	
	private static final String FIX_SETTLEMENT_BATCH_QUERY = "update paylinx.cc_settlement set batch_status ='00', batch_response_msg='BATCH COMPLETE' where batch_id =?";

	public static int fixSettlemnentBatch( Connection conn, String batch_id ) throws SQLException {
		PreparedStatement ps = conn.prepareStatement( FIX_SETTLEMENT_BATCH_QUERY );
		ps.setString( 1, batch_id );
		int updateCount = ps.executeUpdate();
		return updateCount;
	}
	
	private static String getReservationActivityNotes( FDCustomerReservationInfo info ) {
		StringBuffer strBuf = new StringBuffer();
		if ( info != null ) {
			strBuf.append( DateUtil.formatDay( info.getBaseDate() ) );
			strBuf.append( "  " );
			strBuf.append( DateUtil.formatDate( info.getBaseDate() ) );
			strBuf.append( " " );
			strBuf.append( DateUtil.formatTime( info.getStartTime() ) );
			strBuf.append( "-" );
			strBuf.append( DateUtil.formatTime( info.getEndTime() ) );
		}

		if ( info.getType() != null ) {
			strBuf.append( " " );
			strBuf.append( info.getType().getDescription() );
		}

		return strBuf.toString();
	}
		
}

