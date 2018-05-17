package com.freshdirect.fdstore.customer.accounts.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;

import com.freshdirect.customer.EnumExternalLoginSource;
import com.freshdirect.framework.util.MD5Hasher;
import com.freshdirect.framework.util.log.LoggerFactory;


public class ExternalAccountDAO {
	
	private static Category LOGGER = LoggerFactory.getInstance(ExternalAccountDAO.class);
	
	public static String getUserIdForUserToken(Connection con, String userToken) {
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String userId = "";

		String sql = "SELECT USER_ID FROM CUST.EXTERNAL_ACCOUNT_LINK WHERE USER_TOKEN=?";
	
		try {
			
			ps = con.prepareStatement(sql);
			ps.setString(1,userToken.trim());
			rs = ps.executeQuery();
		
			while(rs.next()) {
				userId = rs.getString(1);
				
			}
			
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
		} finally {

			try {

				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (SQLException e) {

				LOGGER.error(e.getMessage());
			}

		}

		return userId;

	}
	
	public static List<String> getConnectedProvidersByUserId(String userId, EnumExternalLoginSource source, Connection con)
	{
		List<String> providers = new ArrayList<String>();
		
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "SELECT PROVIDER FROM CUST.EXTERNAL_ACCOUNT_LINK WHERE USER_ID=?";
	
		try {
			
			ps = con.prepareStatement(sql);
			ps.setString(1,userId);
			ps.setString(2, source.value());
			rs = ps.executeQuery();
		
			while(rs.next()) {
				providers.add(rs.getString("PROVIDER"));
			}
			
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
		} finally {

			try {

				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (SQLException e) {

				LOGGER.error(e.getMessage());
			}

		}

		return providers;
	}
	
	public static boolean isUserEmailAlreadyExist(Connection con,String email)
	{
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean isEmailExist = false;

		String sql = "SELECT COUNT(*) FROM CUST.CUSTOMER WHERE USER_ID=?";
	
		try {
			
			ps = con.prepareStatement(sql);
			ps.setString(1,email.trim());
			rs = ps.executeQuery();
		
			while(rs.next()) {
				if(rs.getInt(1) > 0)
					isEmailExist = true;
				
			}
			
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
		} finally {

			try {

				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (SQLException e) {

				LOGGER.error(e.getMessage());
			}

		}

		
		return isEmailExist;
	}
	
	public static int isUserEmailAlreadyExist(Connection con,String email, String provider)
	{		PreparedStatement ps = null;
	ResultSet rs = null;
	String sql = "select c.id as customerid, ec.user_token as usertoken from cust.customer c, CUST.EXTERNAL_ACCOUNT_LINK ec where C.ID = EC.CUSTOMER_ID (+) and C.USER_ID = ? and EC.PROVIDER (+) = ?";

	try {
		
		ps = con.prepareStatement(sql);
		ps.setString(1,email.trim());
		ps.setString(2,provider.trim());
		rs = ps.executeQuery();
	 
		while(rs.next()) {
			if(rs.getString("customerid")!=null && rs.getString("usertoken")!=null) {
				// FD Account and Non matching Social link
				return 1;
			} else if(rs.getString("customerid")!=null){
				// FD Account and No Social link
				return 0;
			}
			
		}
		
		
	} catch (SQLException ex) {
		LOGGER.error(ex.getMessage());
	} finally {

		try {

			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
		} catch (SQLException e) {

			LOGGER.error(e.getMessage());
		}

	}

	// No FD Account and No Social link
	return -1;
	}
	
	public static void linkUserTokenToUserId(Connection con,String customerId,String userId,String userToken, String identityToken, String provider, String displayName, String preferredUserName, String email, String emailVerified)
	{
		String sql="INSERT INTO CUST.EXTERNAL_ACCOUNT_LINK (CUSTOMER_ID,USER_ID,USER_TOKEN,IDENTITY_TOKEN,PROVIDER,DISPLAY_NAME,PREFERRED_USER_NAME,EMAIL,EMAIL_VERIFIED) VALUES(?,?,?,?,?,?,?,?,?)";
		PreparedStatement ps = null;
		
		try {
			//LOGGER.info("inside link user");
			
			ps = con.prepareStatement(sql);
			ps.setString(1,customerId.trim());
			ps.setString(2,userId.trim());
			ps.setString(3,userToken.trim());
			ps.setString(4,identityToken.trim());
			ps.setString(5,provider.trim());
			ps.setString(6,displayName.trim());
			ps.setString(7,preferredUserName);
			ps.setString(8,email.trim());
			ps.setString(9,emailVerified.trim());
			ps.executeUpdate();
		
			//LOGGER.info("inside link user");
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {

			try {

				if (ps != null)
					ps.close();
			} catch (SQLException e) {

				LOGGER.error(e.getMessage());
			}

		}
	}
	
	public static void unlinkExternalAccountWithUser(Connection con,String userId,String userToken, String provider)
	{
		String sql="DELETE FROM CUST.EXTERNAL_ACCOUNT_LINK WHERE USER_ID=? AND USER_TOKEN=? AND PROVIDER=?";
		PreparedStatement ps = null;
		
		try {
			
			ps = con.prepareStatement(sql);
			ps.setString(1,userId.trim());
			ps.setString(2,userToken.trim());
			ps.setString(3,provider.trim());
			ps.executeUpdate();
			
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
		} finally {

			try {

				if (ps != null)
					ps.close();
			} catch (SQLException e) {

				LOGGER.error(e.getMessage());
			}

		}

	}
	
	
	public static void unlinkExternalAccountWithUser(Connection con,String customerId,String provider)
	{
		String sql="DELETE FROM CUST.EXTERNAL_ACCOUNT_LINK WHERE CUSTOMER_ID=? AND PROVIDER=?";
		PreparedStatement ps = null;
		
		try {
			
			ps = con.prepareStatement(sql);
			ps.setString(1,customerId.trim());
			ps.setString(2,provider.trim());
			ps.executeUpdate();
			
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
		} finally {

			try {

				if (ps != null)
					ps.close();
			} catch (SQLException e) {

				LOGGER.error(e.getMessage());
			}

		}

	}	
	
	public static boolean isExternalLoginOnlyUser(String id, EnumExternalLoginSource source, Connection con)
	{
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String sql = "SELECT 1 FROM CUST.CUSTOMER WHERE ID=? AND EXTERNAL_LOGIN_SRC=?";
	
		try {
			
			ps = con.prepareStatement(sql);
			ps.setString(1,id);
			ps.setString(2, source.value());
			rs = ps.executeQuery();
		
			if(rs.next()) {
				return true;
			}
			
			
			
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
		} finally {

			try {

				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (SQLException e) {

				LOGGER.error(e.getMessage());
			}
			

		}
	
		return false;
	}
	
	
	public static boolean isSocialLoginOnlyUser(Connection conn, String customer_id)  {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement("SELECT * FROM CUST.CUSTOMER WHERE USER_ID=?");
			ps.setString(1, customer_id);
			rs = ps.executeQuery();
			if(rs.next()) {
				//String rc = rs.getString("SOCIAL_LOGIN_ONLY");
				String dummyPassword = "^0X!3X!X!1^";
				String dummyPasswordHash = MD5Hasher.hash(dummyPassword);
				String passwordHash = rs.getString("PASSWORDHASH");
				if(passwordHash != null && passwordHash.equalsIgnoreCase(dummyPasswordHash)) 
					return true;
			}
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
		} finally {
			try {

				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (SQLException e) {

				LOGGER.error(e.getMessage());
			}
		}
		return false;
	}
	

	public static List<String> getConnectedProvidersByUserId(String userId,
			Connection con) {
		List<String> providers = new ArrayList<String>();
		
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "SELECT PROVIDER FROM CUST.EXTERNAL_ACCOUNT_LINK WHERE USER_ID=?";
	
		try {
			
			ps = con.prepareStatement(sql);
			ps.setString(1,userId);
			rs = ps.executeQuery();
		
			while(rs.next()) {
				providers.add(rs.getString("PROVIDER"));
			}
			
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
		} finally {

			try {

				if (rs != null)
					rs.close();
				if (ps != null)
					ps.close();
			} catch (SQLException e) {

				LOGGER.error(e.getMessage());
			}

		}

		return providers;
	}
}
