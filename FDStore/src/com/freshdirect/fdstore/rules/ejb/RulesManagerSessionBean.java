package com.freshdirect.fdstore.rules.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.rules.Rule;
import com.freshdirect.rules.RulesConfig;
import com.freshdirect.rules.RulesManagerDAO;

public class RulesManagerSessionBean extends SessionBeanSupport{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1314489328918173170L;
	private static Category LOGGER = LoggerFactory.getInstance(RulesManagerSessionBean.class);
	
	public Map<String, Rule> getRules(String subsystem) throws FDResourceException, RemoteException
	{
		Connection conn = null;
		try {
			conn = getConnection();
			return RulesManagerDAO.loadRules(conn, subsystem);
		} catch (SQLException e) {
			LOGGER.error("SQL Error occurred while loading the Dlv Rules.");
			throw new FDResourceException(e, "SQL Error occurred while loading the Dlv Rules.");
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after loading the Dlv Rules.", e);
				}
			}
		}
	}
	
	public Rule getRule(String ruleId) throws FDResourceException, RemoteException
	{
		Connection conn = null;
		try {
			conn = getConnection();
			return RulesManagerDAO.getRule(conn, ruleId);
		} catch (SQLException e) {
			LOGGER.error("SQL Error occurred while getting the Dlv Rule.");
			throw new FDResourceException(e, "SQL Error occurred while getting the Dlv Rule.");
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after getting the Dlv Rule.", e);
				}
			}
		}
	}
	public void deleteRule(String ruleId) throws FDResourceException, RemoteException
	{
		Connection conn = null;
		try {
			conn = getConnection();
			RulesManagerDAO.deleteRule(conn, ruleId);
		} catch (SQLException e) {
			LOGGER.error("SQL Error occurred while deleting the Dlv Rule.");
			throw new FDResourceException(e, "SQL Error occurred while deleting the Dlv Rule.");
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after deleting the Dlv Rule.", e);
				}
			}
		}
	}
	public void storeRule(Rule rule) throws FDResourceException, RemoteException
	{
		Connection conn = null;
		try {
			conn = getConnection();
			RulesManagerDAO.storeRule(conn, rule);
		} catch (SQLException e) {
			LOGGER.error("SQL Error occurred while storing the Dlv Rule.");
			throw new FDResourceException(e, "SQL Error occurred while storing the Dlv Rule.");
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after storing the Dlv Rule.", e);
				}
			}
		}
	}
}


	