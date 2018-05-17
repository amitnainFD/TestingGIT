package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;

import org.apache.log4j.Category;

import com.freshdirect.framework.core.SequenceGenerator;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.SessionImpressionLogEntry;

public class SessionImpressionLogSessionBean extends SessionBeanSupport {
	private static final long serialVersionUID = 5459916728525393678L;

	private static Category LOGGER = LoggerFactory
			.getInstance(SessionImpressionLogSessionBean.class);

	private static final String SEQUENCE = "CUST.LOG_SESSION_IMPRESSIONS_SEQ";

	private static String INSERT = "INSERT INTO CUST.LOG_SESSION_IMPRESSIONS "
			+ "(ID, FDUSER_ID, SESSION_ID, START_TIME,"
			+ " END_TIME, VARIANT_ID, PRODUCT_IMPRESSIONS,"
			+ " FEATURE_IMPRESSIONS, TAB_IMPRESSIONS,ZONEID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	public void saveLogEntry(SessionImpressionLogEntry entry) throws RemoteException {
		Connection conn = null;

		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(INSERT);
			
			setupStatement(conn, ps, entry);
			
			int count = ps.executeUpdate();

			// free resources
			ps.close();
			
			// check insert count
			if (count < 1) {
				throw new EJBException("failed to insert entry (unknown reason)");
			}
		} catch (SQLException e) {
			LOGGER.error("SessionImpressionLogSessionBean.saveLogEntry failed; exc="
							+ e, e);
			throw new EJBException(e);
		} finally {
		    close(conn);
		}
	}

    private void setupStatement(Connection conn, PreparedStatement ps, SessionImpressionLogEntry entry) throws SQLException {
        String id = SequenceGenerator.getNextIdFromSequence(conn, SEQUENCE);
        ps.setString(1, id);
        ps.setString(2, entry.getUserPrimaryKey());
        ps.setString(3, entry.getSessionId());
        ps.setTimestamp(4, new java.sql.Timestamp(entry.getStartTime().getTime()));
        ps.setTimestamp(5, new java.sql.Timestamp(entry.getEndTime() != null
        		? entry.getEndTime().getTime()
        		: new java.util.Date().getTime()));
        ps.setString(6, entry.getVariantId());
        ps.setInt(7, entry.getProductImpressions());
        ps.setInt(8, entry.getFeatureImpressions());
        ps.setInt(9, entry.getTabImpressions());
        ps.setString(10, entry.getZoneId());
    }

	public void saveLogEntries(Collection entries) throws RemoteException {
		Connection conn = null;

		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(INSERT);
			
			Iterator it = entries.iterator();
			while (it.hasNext()) {
				SessionImpressionLogEntry entry = (SessionImpressionLogEntry) it.next();
				
				setupStatement(conn, ps, entry);
				
				ps.addBatch();
			}
			
			ps.executeBatch();

			// free resources
			ps.close();			
		} catch (SQLException e) {
			LOGGER.error("SessionImpressionLogSessionBean.saveLogEntry failed; exc="
							+ e, e);
			throw new EJBException(e);
		} finally {
		    close(conn);
		}
	}
}
