package com.freshdirect.fdstore.customer.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.freshdirect.framework.core.SequenceGenerator;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDCustomerCounterDAO {

	@SuppressWarnings( "unused" )
	private static final Logger	LOGGER				= LoggerFactory.getInstance( FDCustomerCounterDAO.class );

	private static final String	GET_COUNTER			= "select value from cust.counters where customer_id = ? and counter_id = ?";
	private static final String	CREATE_COUNTER		= "insert into cust.counters (id, customer_id, counter_id, value) values (?,?,?,?)";
	private static final String	UPDATE_COUNTER		= "update cust.counters set value=? where customer_id = ? and counter_id = ?";


	public static Integer getCounter( Connection conn, String customerId, String counterId ) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Integer value = null;

		try {
			ps = conn.prepareStatement( GET_COUNTER );
			ps.setString( 1, customerId );
			ps.setString( 2, counterId );

			rs = ps.executeQuery();

			if ( rs.next() ) {
				value = rs.getInt( "value" );
			}

		} catch ( SQLException exc ) {
			throw exc;
		} finally {
			if ( rs != null ) {
				rs.close();
			}
			if ( ps != null ) {
				ps.close();
			}
		}
		return value;
	}

	public static void createCounter( Connection conn, String customerId, String counterId, int initialValue ) throws SQLException {
		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement( CREATE_COUNTER );
			ps.setString( 1, SequenceGenerator.getNextId(conn, "CUST") );
			ps.setString( 2, customerId );
			ps.setString( 3, counterId );
			ps.setInt( 4, initialValue );

			ps.executeUpdate();

		} catch ( SQLException exc ) {
			throw exc;
		} finally {
			if ( ps != null ) {
				ps.close();
			}
		}
	}
	
	public static void updateCounter( Connection conn, String customerId, String counterId, int newValue ) throws SQLException {

		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement( UPDATE_COUNTER );
			ps.setInt( 1, newValue );
			ps.setString( 2, customerId );
			ps.setString( 3, counterId );

			ps.executeUpdate();

			ps.close();

		} catch ( SQLException exc ) {
			throw exc;
		} finally {
			if ( ps != null ) {
				ps.close();
			}
		}
	}

}
