package com.freshdirect.fdstore.notification.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Category;

import com.freshdirect.customer.EnumAccountActivityType;
import com.freshdirect.customer.ErpActivityRecord;
import com.freshdirect.customer.ejb.ErpLogActivityCommand;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.ejb.FDSessionBeanSupport;
import com.freshdirect.fdstore.notification.FDNotification;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDNotificationSessionBean extends FDSessionBeanSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4523210641026980557L;

	private final static Category LOGGER = LoggerFactory.getInstance(FDNotificationSessionBean.class);

	public Collection<FDNotification> loadCustomerNotifications(
			FDIdentity identity) throws FDResourceException {

		Connection conn = null;

		try {
			conn = getConnection();
			FDNotificationDAO dao = new FDNotificationDAO();

			List<FDNotification> notifications = dao.loadCustomerNotifications(conn, identity);
			
			Collections.sort(notifications);

			return notifications;
		} catch (SQLException e) {
			LOGGER.error("SQL ERROR in loadCustomerNotifications() : " + e.getMessage(), e);
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}

	}

	public Collection<FDNotification> loadAllNotifications()
			throws FDResourceException {

		Connection conn = null;

		try {
			conn = getConnection();
			FDNotificationDAO dao = new FDNotificationDAO();

			Collection<FDNotification> notifications = dao.loadAllNotifications(conn);

			return notifications;
		} catch (SQLException e) {
			LOGGER.error("SQL ERROR in loadAllNotifications() : " + e.getMessage(), e);
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}

	}

	public void insertNotification(FDActionInfo info,
			FDNotification notification) throws FDResourceException {

		Connection conn = null;

		try {
			conn = getConnection();

			FDNotificationDAO dao = new FDNotificationDAO();
			dao.insertNotification(conn, notification);
			ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.NOTIFICATION_CREATED);
			rec.setStandingOrderId(notification.getId());
			this.logActivity(rec);
		} catch (SQLException e) {
			LOGGER.error("SQL ERROR in insertNotification() : " + e.getMessage(), e);
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public void checkNotificationForCustomer(FDActionInfo info,FDIdentity identity, FDNotification notification) throws FDResourceException {
		
		Connection conn=null;
		
		try{
			conn=getConnection();
			
			FDNotificationDAO dao = new FDNotificationDAO();
			dao.checkNotificationForCustomer(conn, notification.getId(), identity.getFDCustomerPK());
			ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.NOTIFICATION_CHECKED);
			rec.setStandingOrderId(notification.getId());
			this.logActivity(rec);
		} catch (SQLException e) {
			LOGGER.error("SQL ERROR in checkNotification() : " + e.getMessage(), e);
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void delete(FDActionInfo info, FDNotification notification)
			throws FDResourceException {

		Connection conn = null;

		try {
			conn = getConnection();

			FDNotificationDAO dao = new FDNotificationDAO();
			dao.deleteNotification(conn, notification.getId());
			ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.NOTIFICATION_DELETED);
			rec.setStandingOrderId(notification.getId());
			this.logActivity(rec);
		} catch (SQLException e) {
			LOGGER.error("SQL ERROR in delete() : " + e.getMessage(), e);
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void logActivity(ErpActivityRecord record) {
		new ErpLogActivityCommand(LOCATOR, record).execute();
	}

}
