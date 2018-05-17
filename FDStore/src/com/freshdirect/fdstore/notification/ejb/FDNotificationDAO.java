package com.freshdirect.fdstore.notification.ejb;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.notification.FDNotification;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.framework.core.SequenceGenerator;

/**
 * @author szabi
 * 
 */
public class FDNotificationDAO {

	private static final Logger LOGGER = LoggerFactory.getInstance(FDNotificationDAO.class);

	private final String LOAD_CUSTOMER_NOTIFICATIONS_SQL = "select * from CUST.NOTIFICATIONS where ID not in (select NOTIFICATION_ID from CUST.CHECKED_NOTIFICATIONS where CUSTOMER_ID=?) and VALID_FROM<=? and VALID_TILL>=?";

	private final String LOAD_ALL_NOTIFICATION_SQL = "select * from CUST.NOTIFICATIONS";

	private final String INSERT_NOTIFICATION_SQL = "insert into CUST.NOTIFICATIONS (ID,MESSAGE,CANCELABLE,VALID_FROM,VALID_TILL,IMPORTANT) values(?,?,?,?,?,?)";

	private final String DELETE_NOTIFICATION_SQL = "delete from CUST.NOTIFICATIONS where id=?";

	private final String CHECK_NOTIFICATION_SQL = "insert into CUST.CHECKED_NOTIFICATIONS (NOTIFICATION_ID,CUSTOMER_ID) values(?,?)";

	private final String DELETE_CHECKED_NOTIFICATION_SQL = "delete from CUST.CHECKED_NOTIFICATIONS where NOTIFICATION_ID=?";

	private final String DELETE_CHECKED_NOTIFICATION_FOR_CUSTOMER_SQL = "delete from CUST.CHECKED_NOTIFICATIONS where NOTIFICATION_ID=? and CUSTOMER_ID=?";

	protected String getNextId(Connection conn) throws SQLException {
		return SequenceGenerator.getNextId(conn, "CUST");
	}

	/**
	 * @param conn
	 * @param identity
	 * @return
	 * @throws SQLException
	 * 
	 *             Load all active notifications for a specified customer
	 */
	public List<FDNotification> loadCustomerNotifications(
			Connection conn, FDIdentity identity) throws SQLException {

		LOGGER.debug("FDNotificationDAO.loadCustomerNotifications()");

		PreparedStatement ps = null;
		ResultSet rs = null;

		List<FDNotification> notifications = new ArrayList<FDNotification>();
		Date now = new Date(new java.util.Date().getTime());

		try {
			ps = conn.prepareStatement(LOAD_CUSTOMER_NOTIFICATIONS_SQL);
			ps.setString(1, identity!=null ? identity.getFDCustomerPK() : "0");
			ps.setDate(2, now);
			ps.setDate(3, now);

			rs = ps.executeQuery();

			while (rs.next()) {
				notifications.add(populate(rs, new FDNotification()));
			}

		} catch (SQLException exc) {
			throw exc;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
		}

		return notifications;
	}

	/**
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public List<FDNotification> loadAllNotifications(Connection conn)
			throws SQLException {

		LOGGER.debug("FDNotificationDAO.loadAllNotifications()");

		PreparedStatement ps = null;
		ResultSet rs = null;

		List<FDNotification> notifications = new ArrayList<FDNotification>();

		try {
			ps = conn.prepareStatement(LOAD_ALL_NOTIFICATION_SQL);

			rs = ps.executeQuery();

			while (rs.next()) {
				notifications.add(populate(rs, new FDNotification()));
			}

		} catch (SQLException exc) {
			throw exc;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
		}

		return notifications;
	}

	/**
	 * @param conn
	 * @param notification
	 * @return
	 * @throws SQLException
	 */
	public String insertNotification(Connection conn,
			FDNotification notification) throws SQLException {

		LOGGER.debug("FDNotificationDAO.insertNotification()");

		PreparedStatement ps = null;
		String id = null;

		try {
			ps = conn.prepareStatement(INSERT_NOTIFICATION_SQL);

			id = getNextId(conn);

			int counter = 1;
			ps.setString(counter++, id);
			ps.setString(counter++, notification.getMessage());
			ps.setBoolean(counter++, notification.isCancelable());
			ps.setDate(counter++, new java.sql.Date(notification.getValidFrom().getTime()));
			ps.setDate(counter++, new java.sql.Date(notification.getValidTill().getTime()));
			ps.setBoolean(counter++, notification.isImportant());

			ps.execute();

			notification.setId(id);

		} catch (SQLException exc) {
			throw exc;
		} finally {
			if (ps != null) {
				ps.close();
			}
		}

		return id;
	}

	/**
	 * @param conn
	 * @param notId
	 * @param custId
	 * @param active
	 * @throws SQLException
	 */
	public void checkNotificationForCustomer(Connection conn, String notId,
			String custId) throws SQLException {

		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement(CHECK_NOTIFICATION_SQL);
			ps.setString(1, notId);
			ps.setString(2, custId);

			ps.execute();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	/**
	 * @param conn
	 * @param notId
	 * @throws SQLException
	 */
	public void deleteNotification(Connection conn, String notId)
			throws SQLException {

		PreparedStatement ps = null;

		try {
			// delete from notifications table
			ps = conn.prepareStatement(DELETE_NOTIFICATION_SQL);
			ps.setString(1, notId);
			ps.executeUpdate();
			ps.close();

			// delete all related entries from customer_notification table
			ps = conn.prepareStatement(DELETE_CHECKED_NOTIFICATION_SQL);
			ps.setString(1, notId);
			ps.executeUpdate();
			
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if (ps != null) {
				ps.close();
			}
		}

	}

	/**
	 * @param conn
	 * @param notId
	 * @param custId
	 * @throws SQLException
	 */
	public void deleteCheckedNotificationsForCustomer(Connection conn, String notId,
			String custId) throws SQLException {

		PreparedStatement ps = null;

		try {
			ps = conn.prepareStatement(DELETE_CHECKED_NOTIFICATION_FOR_CUSTOMER_SQL);
			ps.setString(1, notId);
			ps.setString(2, custId);

			ps.executeUpdate();
		} catch (SQLException exc) {
			throw exc;
		} finally {
			if (ps != null) {
				ps.close();
			}
		}

	}

	/**
	 * @param rs
	 * @param notification
	 * @return
	 * @throws SQLException
	 */
	private FDNotification populate(ResultSet rs, FDNotification notification)
			throws SQLException {

		notification.setId(rs.getString("ID"));
		notification.setCancelable(rs.getBoolean("CANCELABLE"));
		notification.setMessage(rs.getString("MESSAGE"));
		notification.setValidFrom(rs.getTime("VALID_FROM"));
		notification.setValidTill(rs.getTime("VALID_TILL"));
		notification.setImportant(rs.getBoolean("IMPORTANT"));

		return notification;
	}
}
