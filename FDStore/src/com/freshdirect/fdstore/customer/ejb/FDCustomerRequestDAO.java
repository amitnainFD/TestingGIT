/*
 * FDSurveySessionBean.java
 *
 * Created on March 11, 2002, 7:39 PM
 */

package com.freshdirect.fdstore.customer.ejb;

/**
 *
 * @author  knadeem
 * @version 
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.Map;
import com.freshdirect.fdstore.customer.FDCustomerRequest;

public class FDCustomerRequestDAO {

	private static String INSERT =
		"INSERT INTO CUST.CUSTOMER_REQUEST(ID, REQUEST_DATE, CASE_SUBJECT_CODE,"
										+ " CUSTOMER_ID, CUSTOMER_EMAIL, LOGGED_BY, SUBJECT,"
										+ " INFO_LINE1,INFO_LINE2,INFO_LINE3,INFO_LINE4,INFO_LINE5,"
										+ " INFO_LINE6,INFO_LINE7,INFO_LINE8,INFO_LINE9,INFO_OTHER)"
		+ " VALUES(?, sysdate, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static String nvl(String str) {
		return (str == null) ? "" : str;
	}
	
	public static void storeCustomerRequest(Connection conn, String id, FDCustomerRequest cr) throws SQLException {
		PreparedStatement ps = 
			conn.prepareStatement(INSERT);

		ps.setString(1, id);
		ps.setString(2, nvl(cr.getCaseSubjectCode()));
		ps.setString(3, nvl(cr.getCustomerID()));
		ps.setString(4, nvl(cr.getCustomerEmail()));
		ps.setString(5, nvl(cr.getLoggedBy()));
		ps.setString(6, nvl(cr.getSubject()));
		ps.setString(7, nvl(cr.getInfoLine1()));
		ps.setString(8, nvl(cr.getInfoLine2()));
		ps.setString(9, nvl(cr.getInfoLine3()));
		ps.setString(10, nvl(cr.getInfoLine4()));
		ps.setString(11, nvl(cr.getInfoLine5()));
		ps.setString(12, nvl(cr.getInfoLine6()));
		ps.setString(13, nvl(cr.getInfoLine7()));
		ps.setString(14, nvl(cr.getInfoLine8()));
		ps.setString(15, nvl(cr.getInfoLine9()));
		ps.setString(16, nvl(cr.getInfoOther()));

		ps.executeUpdate();
		ps.close();
	}
}