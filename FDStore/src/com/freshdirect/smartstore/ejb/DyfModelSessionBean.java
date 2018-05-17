package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBException;

import org.apache.log4j.Category;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * Model support for DYF variants
 * 
 * @author segabor
 *
 */
public class DyfModelSessionBean extends SessionBeanSupport {
	private static final long serialVersionUID = 3140508241201048967L;

	private static Category LOGGER = LoggerFactory.getInstance(DyfModelSessionBean.class);

	private static final String SQL_GETPRDS_SCORES = 
		"SELECT PRODUCT_ID, SCORE FROM CUST.SS_PERSONALIZED_PRODUCT_SCORES WHERE CUSTOMER_ID = ? ";
	
	private static final String SQL_GETPRODUCTS = 
		"SELECT PRODUCT_ID FROM CUST.SS_PERSONALIZED_PRODUCT_SCORES WHERE CUSTOMER_ID = ? ";
	
	private static final String SQL_GLOBALPRDS_SCORES = 
		"SELECT PRODUCT_ID, SCORE FROM CUST.SS_GLOBAL_PRODUCT_SCORES";


	/**
	 * Returns product->frequency map ( Map<ContentKey, Float> ) purchased by customer
     *
	 * @param customerID customer's PK
	 * @return Map of frequencies indexed by product ContentKeys
	 * @throws RemoteException
	 */
	public Map getProductFrequencies(String customerID) throws RemoteException {
		Connection conn = null;
		// Map<ContentKey,Integer>
		Map result = new HashMap();
		try {
			conn = getConnection();
			
			PreparedStatement ps = conn.prepareStatement(SQL_GETPRDS_SCORES);
			ps.setString(1, customerID);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				// put Product Key -> Score couples into results set
				result.put(new ContentKey(FDContentTypes.PRODUCT, rs.getString(1)), new Float(rs.getFloat(2)) );
			}

			// free resources
			rs.close();
			ps.close();
		} catch (SQLException e) {
			LOGGER.error("DyfModelSessionBean.getProductIDs failed", e);
            throw new EJBException(e);
        } finally {
            try {
                if (conn != null)
                	conn.close();
            } catch (SQLException sqle) {
                throw new EJBException(sqle);
            }
		}

        return result;
	}

	/**
	 * Returns the list of products purchased by customer
     *
	 * @param customerID customer's PK
	 * @return List of product ContentKeys
	 * @throws RemoteException
	 */
	public Set getProducts(String customerID) throws RemoteException {
		Connection conn = null;
		Set result = new HashSet();
		try {
			conn = getConnection();
			
			PreparedStatement ps = conn.prepareStatement(SQL_GETPRODUCTS);
			ps.setString(1, customerID);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				result.add(new ContentKey(FDContentTypes.PRODUCT, rs.getString(1)));
			}

			// free resources
			rs.close();
			ps.close();
		} catch (SQLException e) {
			LOGGER.error("DyfModelSessionBean.getProducts failed", e);
            throw new EJBException(e);
        } finally {
            try {
                if (conn != null)
                	conn.close();
            } catch (SQLException sqle) {
                throw new EJBException(sqle);
            }
		}

        return result;
	}
	
	public Map getGlobalProductScores() throws RemoteException {
		Connection conn = null;
		// Map<ContentKey,Float>
		Map result = new HashMap();
		try {
			conn = getConnection();
			
			PreparedStatement ps = conn.prepareStatement(SQL_GLOBALPRDS_SCORES);		
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				// put Product Key -> Score couples into results set
				result.put(new ContentKey(FDContentTypes.PRODUCT, rs.getString(1)), new Float(rs.getFloat(2)) );
			}

			// free resources
			rs.close();
			ps.close();
			return result;
		} catch (SQLException e) {
			LOGGER.error("DyfModelSessionBean.getGlobalProductScores failed", e);
            throw new EJBException(e);
        } finally {
            try {
                if (conn != null)
                	conn.close();
            } catch (SQLException sqle) {
                throw new EJBException(sqle);
            }
		}
	}
}
