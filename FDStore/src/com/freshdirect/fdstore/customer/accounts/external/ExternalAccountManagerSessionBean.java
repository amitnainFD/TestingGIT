package com.freshdirect.fdstore.customer.accounts.external;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.freshdirect.customer.EnumExternalLoginSource;
import com.freshdirect.fdstore.customer.ejb.FDSessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;

public class ExternalAccountManagerSessionBean extends FDSessionBeanSupport {
	
	private static final long serialVersionUID = -4628071128029921271L;
	
	private final static Logger LOGGER = LoggerFactory.getInstance(ExternalAccountManagerSessionBean.class);
	
	public String getUserIdForUserToken(String userToken) 
	{
		String userId= null;
		Connection conn = null;
		try {
			conn = this.getConnection();
			userId = ExternalAccountDAO.getUserIdForUserToken(conn, userToken);
			
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally{
			try {
				if(null != conn){
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e.getMessage());
			}
		}
		
		return userId;
	}
	
	public boolean isUserEmailAlreadyExist(String email)
	{
		Connection conn = null;
		try {
			conn = this.getConnection();
			return ExternalAccountDAO.isUserEmailAlreadyExist(conn, email);
			
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally{
			try {
				if(null != conn){
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e.getMessage());
			}
		}
		return false;
	
	}
	
	public int isUserEmailAlreadyExist(String email, String provider)
	{
		Connection conn = null;
		try {
			conn = this.getConnection();
			return ExternalAccountDAO.isUserEmailAlreadyExist(conn, email, provider);
			
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally{
			try {
				if(null != conn){
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e.getMessage());
			}
		}
		return -1;
	
	}
	
	public void linkUserTokenToUserId(String customerId,String userId,String userToken, String identityToken, String provider, String displayName, String preferredUserName, String email, String emailVerified)
	{
		Connection conn = null;
		try {
			conn = this.getConnection();
			ExternalAccountDAO.linkUserTokenToUserId(conn, customerId, userId, userToken, identityToken, provider, displayName, preferredUserName, email, emailVerified);
			
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally{
			try {
				if(null != conn){
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e.getMessage());
			}
		}
	}
	
	public List<String> getConnectedProvidersByUserId(String userId, EnumExternalLoginSource source)
	{
		Connection conn = null;
		try {
			conn = this.getConnection();
			return ExternalAccountDAO.getConnectedProvidersByUserId(userId, source, conn);
			
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally{
			try {
				if(null != conn){
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e.getMessage());
			}
		}
		return null;
	}
	
	public List<String> getConnectedProvidersByUserId(String userId)
	{
		Connection conn = null;
		try {
			conn = this.getConnection();
			return ExternalAccountDAO.getConnectedProvidersByUserId(userId, conn);
			
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally{
			try {
				if(null != conn){
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e.getMessage());
			}
		}
		return null;
	}
	
	public boolean isExternalLoginOnlyUser(String userId, EnumExternalLoginSource source) 
	{
		Connection conn = null;
		try {
			conn = this.getConnection();
			return ExternalAccountDAO.isExternalLoginOnlyUser(userId, source, conn);
			
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally{
			try {
				if(null != conn){
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e.getMessage());
			}
		}
		return false;
	}
	
	
	
	public void unlinkExternalAccountWithUser(String userId, String userToken, String provider)
	{
		Connection conn = null;
		try {
			conn = this.getConnection();
			ExternalAccountDAO.unlinkExternalAccountWithUser(conn, userId, userToken, provider);
			
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally{
			try {
				if(null != conn){
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e.getMessage());
			}
		}
	}
	
	public void unlinkExternalAccountWithUser(String userId, String provider)
	{
		Connection conn = null;
		try {
			conn = this.getConnection();
			ExternalAccountDAO.unlinkExternalAccountWithUser(conn, userId, provider);
			
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally{
			try {
				if(null != conn){
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e.getMessage());
			}
		}
	}
	
	public boolean isSocialLoginOnlyUser(String customer_id)
	{
		Connection conn = null;
		try {
			conn = this.getConnection();
			return ExternalAccountDAO.isSocialLoginOnlyUser(conn, customer_id);
			
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		} finally{
			try {
				if(null != conn){
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e.getMessage());
			}
		}
		return false;
	
	}	
	
	
}
