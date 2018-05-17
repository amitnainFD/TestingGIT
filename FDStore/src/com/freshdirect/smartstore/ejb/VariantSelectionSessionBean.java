package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;

public class VariantSelectionSessionBean extends SessionBeanSupport {
	private static final long serialVersionUID = 7401851794123258702L;

	private static Logger LOGGER = LoggerFactory.getInstance(VariantSelectionSessionBean.class);



	private static String getVariantMapQuery(String parameter) {
		return
			"SELECT T1.ID AS COHORT_ID, T2.VARIANT_ID AS VARIANT_ID " +
			"FROM CUST.SS_COHORTS T1, CUST.SS_VARIANT_ASSIGNMENT T2 " +
			"WHERE T2.COHORT_ID=T1.ID " +
			"AND T2.\"DATE\"=( " +
			"  SELECT MAX(T3.\"DATE\") " +
			"  FROM CUST.SS_VARIANT_ASSIGNMENT T3 " +
			"  WHERE T3.COHORT_ID=T1.ID " +
			"  AND TO_DATE(T3.\"DATE\") < " + parameter + 
			" and T3.variant_id in (SELECT ID FROM CUST.SS_VARIANTS WHERE FEATURE=? AND ARCHIVED = 'N') " +
			") " +
			"AND T2.VARIANT_ID IN ( " +
			"  SELECT ID FROM CUST.SS_VARIANTS WHERE FEATURE=? AND ARCHIVED = 'N' " +
			") " +
			"ORDER BY COHORT_ID";
	}


	// This command returns (Cn, Vn) pairs
	//   'C1', 'Random DYF'
	//   'C2', 'freqbought'
	//   ...
	//
	final static String SQL_GETVARIANTMAP = getVariantMapQuery("current_date");
	
	final static String SQL_GETVARIANTMAP_ON = getVariantMapQuery("?");
	
	public Map<String,String> getVariantMap(EnumSiteFeature feature) throws RemoteException {
		return getVariantMap(feature, null);
	}
	
	/**
	 * Returns (Cn,Vn) pairs where Cn is a Cohort and Vn is the assigned Variant ID
	 * @param feature Name of Site Feature
	 * @return Variant assignment map
	 * @throws RemoteException
	 */
	public Map<String,String> getVariantMap(EnumSiteFeature feature, Date date) throws RemoteException {
		Connection conn = null;
		Map<String,String> cohortVariantMap = new HashMap<String,String>();
		
		try {
			conn = getConnection();
			
			PreparedStatement ps = date == null ?
					conn.prepareStatement(SQL_GETVARIANTMAP) :
					conn.prepareStatement(SQL_GETVARIANTMAP_ON);
			int arg = 1;
			if (date != null) {
				ps.setDate(arg++, new java.sql.Date(date.getTime()));
			}
			ps.setString(arg++, feature.getName());
			ps.setString(arg++, feature.getName());
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				cohortVariantMap.put(rs.getString(1), rs.getString(2));
			}
			
			// free resources
			rs.close();
			ps.close();
		} catch (SQLException e) {
		    LOGGER.error("VariantSelectionSessionBean.getVariantMap failed; exc=" + e, e);
                    throw new EJBException(e);
                } finally {
                    close(conn);
		}

        return cohortVariantMap;
	}
	
	
	
	/**
	 * Returns (cohort ID, weight) pairs indexed by cohort IDs
	 * @return
	 */
	public Map<String, Integer> getCohorts() throws RemoteException {
		Connection conn = null;
		Map<String, Integer> cohortMap = new HashMap<String, Integer>();
		
		try {
			conn = getConnection();
			
			PreparedStatement ps = conn.prepareStatement("SELECT ID, WEIGHT FROM CUST.SS_COHORTS");
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				cohortMap.put(rs.getString(1), new Integer(rs.getInt(2)));
			}

			// free resources
			rs.close();
			ps.close();
		} catch (SQLException e) {
		    LOGGER.error("VariantSelectionSessionBean.getCohorts failed; exc=" + e, e);
		    throw new EJBException(e);
                } finally {
                    close(conn);
		}
                return cohortMap;
	}
	
	/**
	 * Return a cohort ID list.
	 *  
	 * @return
	 */
	public List<String> getCohortNames() throws RemoteException {
		Connection conn = null;
		List<String> cohortNames = new ArrayList<String>();
		
		try {
			conn = getConnection();
			
			PreparedStatement ps = conn.prepareStatement("SELECT ID FROM CUST.SS_COHORTS ORDER BY rowid");
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				cohortNames.add(rs.getString(1));
			}

			// free resources
			rs.close();
			ps.close();
		} catch (SQLException e) {
		    LOGGER.error("VariantSelectionSessionBean.getCohortNames failed; exc=" + e, e);
		    throw new EJBException(e);
                } finally {
                    close(conn);
                }
        
                return cohortNames;
            }
	
	/**
	 * Returns the list of variant IDs belonging to a site feature
	 * @param feature
	 * @return list of variant IDs
	 * @throws RemoteException
	 */
	public List<String> getVariants(EnumSiteFeature feature) throws RemoteException {
		Connection conn = null;
		List<String> variantList = new ArrayList<String>();
		
		try {
			conn = getConnection();
			
			PreparedStatement ps = conn.prepareStatement("SELECT ID FROM CUST.SS_VARIANTS WHERE FEATURE=?  AND ARCHIVED = 'N'");
			ps.setString(1, feature.getName());
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				variantList.add(rs.getString(1));
			}

			// free resources
			rs.close();
			ps.close();
		} catch (SQLException e) {
		    LOGGER.error("VariantSelectionSessionBean.getVariants failed; exc=" + e, e);
                    throw new EJBException(e);
                } finally {
                    close(conn);
		}
                return variantList;
	}

	
	public List<Date> getStartDates() throws RemoteException {
		Connection conn = null;
		List<Date> dateList = new ArrayList<Date>();
		
		try {
			conn = getConnection();
			
			PreparedStatement ps = conn.prepareStatement(
					"SELECT DISTINCT TO_DATE(\"DATE\") AS X " +
					"FROM (SELECT DISTINCT T3.\"DATE\" FROM CUST.SS_VARIANT_ASSIGNMENT T3) " +
					"ORDER BY X DESC");
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				dateList.add(rs.getDate(1));
			}

			// free resources
			rs.close();
			ps.close();
		} catch (SQLException e) {
		    LOGGER.error("VariantSelectionSessionBean.getStartDates failed; exc=" + e, e);
                    throw new EJBException(e);
                } finally {
                    close(conn);
		}
                return dateList;
	}
}
