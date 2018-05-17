package com.freshdirect.fdstore.customer.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.freshdirect.framework.core.PrimaryKey;

public class FDDonationOptinDAO {

	public static void insert(Connection conn, String custId, String saleId, boolean optIn) throws SQLException {
		
		PreparedStatement ps =	conn.prepareStatement("INSERT INTO CUST.DONATION_OPT_IND(CUSTOMER_ID,SALE_ID, OPTIN_IND) VALUES (?,?,?)");
		ps.setString(1, custId);
		ps.setString(2, saleId);
		if(optIn){			
			ps.setString(3, "Y");
		}else{
			ps.setString(3, "N");
		}	
		ps.executeQuery();
		ps.close();
	}
	
	public static void update(Connection conn, String custId, String saleId, boolean optIn) throws SQLException {	
		
		PreparedStatement ps =	conn.prepareStatement("UPDATE CUST.DONATION_OPT_IND SET OPTIN_IND= ? WHERE CUSTOMER_ID=? AND SALE_ID =? ");
		if(optIn){			
			ps.setString(1, "Y");
		}else{
			ps.setString(1, "N");
		}
		ps.setString(2, custId);
		ps.setString(3, saleId);
			
		ps.executeQuery();
		ps.close();
	}

	public static Set select(Connection conn, PrimaryKey salePk) throws SQLException {
		PreparedStatement ps =
			conn.prepareStatement(
				"SELECT P.CODE FROM CUST.PROMOTION_NEW P, CUST.PROMOTION_PARTICIPATION PP WHERE P.ID=PP.PROMOTION_ID AND PP.SALE_ID=?");
		ps.setString(1, salePk.getId());

		Set promotionCodes = new HashSet();
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			promotionCodes.add(rs.getString(1));
		}

		rs.close();
		ps.close();

		return promotionCodes;
	}
}
