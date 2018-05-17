package com.freshdirect.fdstore.standingorders.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import com.freshdirect.framework.core.SequenceGenerator;

public class FDStandingOrderAuditDataDAO {

	private static final String MIS = "mis";
	private static final String SO_AUDIT_SEQUENCE = "SO_AUDIT_SEQUENCE";
	private final String SO_AUDIT_SQL = "insert into mis.so_audit_log values(?,?,?,?,?)";

	public void auditCall(Connection conn,AuditDataBeanInfo auditDataBeanInfo) throws SQLException {
		PreparedStatement ps;
		ps = conn.prepareStatement(SO_AUDIT_SQL);

		String id = SequenceGenerator.getNextId(conn, MIS,
				SO_AUDIT_SEQUENCE);
		ps.setString(1, id);
		ps.setString(2, auditDataBeanInfo.getUserId());
		ps.setString(3, auditDataBeanInfo.getType());
		ps.setTimestamp(4, new java.sql.Timestamp(new Date().getTime()));
		ps.setString(5, auditDataBeanInfo.getComment());
		ps.executeUpdate();
		ps.close();
	}
}