package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.ContentKey.InvalidContentKeyException;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.content.EnumWinePrice;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * Session bean implementation.
 * 
 * @author istvan
 *
 */
public class ScoreFactorSessionBean extends SessionBeanSupport {
	
	private static Logger LOGGER = LoggerFactory.getInstance(ScoreFactorSessionBean.class);

	private static final long serialVersionUID = 8645402837591723847L;

	private static final String GLOBAL_FACTORS_TABLE = "CUST.SS_GLOBAL_PRODUCT_SCORES";
	private static final String PERSONALIZED_FACTORS_TABLE = "CUST.SS_PERSONALIZED_PRODUCT_SCORES";
	
	private static final String PERSONALIZED_FACTORS_QUERY =
		"SELECT * FROM " + PERSONALIZED_FACTORS_TABLE + " WHERE E_STORE = ? AND CUSTOMER_ID = ?";
	
	private static final String GLOBAL_FACTORS_QUERY =
		"SELECT * FROM " + GLOBAL_FACTORS_TABLE + " WHERE E_STORE = ?";
	
	private static final String GLOBAL_FACTOR_NAMES_QUERY = GLOBAL_FACTORS_QUERY + " AND PRODUCT_ID = \'CSULKOS BABLEVES\'";
	
	private static final String GLOBAL_PRODUCTS_QUERY =
		"SELECT PRODUCT_ID FROM " + GLOBAL_FACTORS_TABLE + " WHERE E_STORE = ?";

	private static final String PERSONALIZED_PRODUCTS_QUERY =
		"SELECT PRODUCT_ID FROM " + PERSONALIZED_FACTORS_TABLE + " WHERE E_STORE = ? AND CUSTOMER_ID = ?";

	@Deprecated
	private static final String PRODUCT_RECOMMENDATION_TABLE = "CUST.SS_PRODUCT_RECOMMENDATION";
	@Deprecated
	private static final String USER_RECOMMENDATION_TABLE = "CUST.SS_USER_RECOMMENDATION";
	
	@Deprecated
	private static final String PRODUCT_RECOMMENDATION_QUERY = "SELECT RECOMMENDED_PRODUCT FROM " + PRODUCT_RECOMMENDATION_TABLE + " WHERE RECOMMENDER = ? AND CONTENT_KEY = ? ORDER BY PRIORITY ASC";
	@Deprecated
	private static final String USER_RECOMMENDATION_QUERY = "SELECT RECOMMENDED_PRODUCT FROM " + USER_RECOMMENDATION_TABLE + " WHERE RECOMMENDER = ? AND CUSTOMER_ID = ? ORDER BY PRIORITY ASC";
	
	/**
	 * Determine if column name is not a particular factor name
	 * 
	 * @param column
	 * @return
	 */
	private boolean isNotFactorColumn(String column) {
		return "PRODUCT_ID".equalsIgnoreCase(column)
				|| "CUSTOMER_ID".equalsIgnoreCase(column)
				|| "E_STORE".equalsIgnoreCase(column);
	}
	
	/**
	 * 
	 * @param eStoreId {@link EnumEStoreId}
	 * @param erpCustomerId null means global
	 * @return Set<ProductId:String>
	 */
	private Set<String> retrieveProducts(final String eStoreId, final String erpCustomerId) {
		Connection conn = null;
		try {
			conn = getConnection();
			
			PreparedStatement ps = conn.prepareStatement(erpCustomerId == null ? GLOBAL_PRODUCTS_QUERY : PERSONALIZED_PRODUCTS_QUERY);
			if (erpCustomerId != null) {
				ps.setString(1, eStoreId);
				ps.setString(2, erpCustomerId);
			} else {
				ps.setString(1, eStoreId);
			}
			
			ResultSet rs = ps.executeQuery();
			
			Set<String> productSet = new HashSet<String>();
			
			while(rs.next()) {
				productSet.add(rs.getString(1));
			}
			
			return productSet;
			
		} catch (SQLException e) {
			LOGGER.warn("Could not retrieve products for " +
					(erpCustomerId != null ?
							"<global>" :
							erpCustomerId), e);
			throw new FDRuntimeException(e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch(SQLException e) {
				LOGGER.warn("Could not close connection", e);
			}
		}
	}

	/**
	 * 
	 * @param erpCustomerId null means global
	 * @return Set of factor names
	 */
	private Set<String> retrieveFactorNames(final String erpCustomerId) {
		Connection conn = null;
		
		try {
			conn = getConnection();
				
			PreparedStatement ps = null;
			
			// is global query
			if (erpCustomerId == null) {
				ps = conn.prepareStatement(GLOBAL_FACTOR_NAMES_QUERY);
				ps.setString(1, "FreshDirect");//::FDX:: 
			} else {
				ps = conn.prepareStatement(PERSONALIZED_FACTORS_QUERY);
				ps.setString(1, "FreshDirect");
				ps.setString(2, erpCustomerId);
			}
			
			ResultSet rs = ps.executeQuery();
			
			ResultSetMetaData meta = rs.getMetaData();
			Set<String> names = new HashSet<String>(4*meta.getColumnCount()/3);
			for (int j = 0; j< meta.getColumnCount(); ++j) {
				String column = meta.getColumnName(j+1);
				if (!isNotFactorColumn(column)) {
					names.add(column);
				}
			}
			
			rs.close();
			ps.close();
			
			return names;
		} catch (SQLException e) {
			if (erpCustomerId == null)
				LOGGER.warn("Could not retrieve global factor names", e);
			else
				LOGGER.warn("Could not retrieve factor names for " + erpCustomerId, e);
			throw new FDRuntimeException(e);
		} finally {
                     close(conn);
		}
	}
	
	/**
	 * 
	 * @param eStoreId {@link EnumEStoreId}
	 * @param erpCustomerId null means global
	 * @return
	 * @see {@link EnumEStoreId}
	 */
	private Map<String,double[]> retrieveScores(final String eStoreId, String erpCustomerId, List<String> factors) {
		Connection conn = null;
	
		try {
			conn = getConnection();
				
			PreparedStatement ps = null;
			
			// is global query
			if (erpCustomerId == null) {
				ps = conn.prepareStatement(GLOBAL_FACTORS_QUERY);
				ps.setString(1, eStoreId);
			} else {
				ps = conn.prepareStatement(PERSONALIZED_FACTORS_QUERY);
				ps.setString(1, eStoreId);
				ps.setString(2, erpCustomerId);
			}
			
			ResultSet rs = ps.executeQuery();
			
			Map<String,double[]> scores = new HashMap<String,double[]>();
			
			int factorsSize = factors.size();
			while(rs.next()) {
				String productId = rs.getString("PRODUCT_ID");
				double[] factorValues = scores.get(productId);
				if (factorValues == null) {
					factorValues = new double[factorsSize];
					scores.put(productId, factorValues);
				}
				for(int i = 0; i < factorsSize; ++i) {
					factorValues[i] = rs.getDouble(factors.get(i).toString());
				}
			}
			
			rs.close();
			ps.close();
			
			return scores;
		} catch (SQLException e) {
			if (erpCustomerId == null)
				LOGGER.warn("Could not retrieve global scores " + factors);
			else
				LOGGER.warn("Could not retrieve personalized scores for " + erpCustomerId + " " + factors, e);
			throw new FDRuntimeException(e);
		} finally {
                     close(conn);
		}
	}
	
	
	/**
	 * Get the required personalized factors for user.
	 * 
	 * @param eStoreId {@link EnumEStoreId}
	 * @param erpCustomerId 
	 * @param factors List<FactorName:{@link String}>
	 * @throws RemoteException
	 * @return Map<ProductId:{@link String},double[]>
	 * @see ScoreFactorSB#getPersonalizedFactors(String, List)
	 */
	public Map<String,double[]> getPersonalizedFactors(final String eStoreId, String erpCustomerId, List<String> factors) throws RemoteException {
		return retrieveScores(eStoreId, erpCustomerId, factors);
	}
	
	/**
	 * Get the required global factors.
	 * 
	 * @param eStoreId {@link EnumEStoreId}
	 * @param factors List<FactorName:{@link String}>
	 * @throws RemoteException
	 * @return Map<ProductId:{@link String},double[]>
	 * @see ScoreFactorSB#getGlobalFactors()
	 */	
	public Map<String,double[]> getGlobalFactors(final String eStoreId, List<String> factors) throws RemoteException {
		return retrieveScores(eStoreId, null, factors);
	}
	
	/**
	 * Get personalized factor names.
	 * 
	 * @return Set<{@link String}>
	 * @throws RemoteException
	 * @see ScoreFactorSB#getPersonalizedFactorNames()
	 */
	public Set<String> getPersonalizedFactorNames() throws RemoteException {
		return retrieveFactorNames("BATTHYANYI LAJOS");
	}

	/**
	 * Get the global factor names.
	 * 
	 * @return Set<{@link String>
	 * @throws RemoteException
	 * @see @link ScoreFactorSB#getPersonalizedFactorNames()
	 */
	public Set<String> getGlobalFactorNames() throws RemoteException {
		return retrieveFactorNames(null);
	}

	/**
	 * Get the product ids that have any scores.
	 * 
	 * @param eStoreId {@link EnumEStoreId}
	 * @return Set<ProductId:String>
	 * @throws RemoteException
	 */
	public Set<String> getGlobalProducts(final String eStoreId) throws RemoteException {
		return retrieveProducts(eStoreId, null);
	}
	
	/**
	 * Get the product ids for the user that have any scores.
	 * 
	 * @param eStoreId {@link EnumEStoreId}
	 * @param erpCustomerId 
	 * @return Set<ProductId:String>
	 * @throws RemoteException
	 */
	public Set<String> getPersonalizedProducts(final String eStoreId, final String erpCustomerId) throws RemoteException {
		return retrieveProducts(eStoreId, erpCustomerId);
	}

	/**
	 * Return a list of product recommendation for a given product by a
	 * recommender vendor.
	 * 
	 * @param recommender
	 * @param key
	 * @return List<ContentKey>
	 * @throws RemoteException
	 */
	@Deprecated
	public List<ContentKey> getProductRecommendations(String recommender,
			ContentKey key) throws RemoteException {
		Connection conn = null;
		try {
			conn = getConnection();

			PreparedStatement ps = conn
					.prepareStatement(PRODUCT_RECOMMENDATION_QUERY);
			ps.setString(1, recommender);
			ps.setString(2, key.getId());

			ResultSet rs = ps.executeQuery();

			List<ContentKey> products = new ArrayList<ContentKey>();

			while (rs.next()) {
				String recKey = rs.getString(1);
				try {
					products.add(ContentKey.create(FDContentTypes.PRODUCT,
							recKey));
				} catch (InvalidContentKeyException e) {
					LOGGER.warn("invalid content key '" + recKey + "', for "
							+ key + ", from recommender " + recommender);
				}
			}
			return products;
		} catch (SQLException e) {
			LOGGER.warn("Could not retrieve product recommendations for " + key
					+ " from recommender:" + recommender, e);
			throw new FDRuntimeException(e);
		} finally {
			close(conn);
		}
	}

	/**
	 * Return a list of personal recommendation (ContentKey-s) for a user by a
	 * recommender vendor.
	 * 
	 * @param recommender
	 * @param erpCustomerId
	 * @return List<ContentKey>
	 * @throws RemoteException
	 */
	@Deprecated
	public List<ContentKey> getPersonalRecommendations(String recommender,
			String erpCustomerId) throws RemoteException {
		Connection conn = null;
		try {
			conn = getConnection();

			PreparedStatement ps = conn
					.prepareStatement(USER_RECOMMENDATION_QUERY);
			ps.setString(1, recommender);
			ps.setString(2, erpCustomerId);

			ResultSet rs = ps.executeQuery();

			List<ContentKey> products = new ArrayList<ContentKey>();

			while (rs.next()) {
				String recKey = rs.getString(1);
				try {
					products.add(ContentKey.create(FDContentTypes.PRODUCT,
							recKey));
				} catch (InvalidContentKeyException e) {
					LOGGER.warn("invalid content key '" + recKey + "', for "
							+ erpCustomerId + ", from recommender "
							+ recommender + " for personal recommendation");
				}
			}
			return products;
		} catch (SQLException e) {
			LOGGER.warn("Could not retrieve product recommendations for "
					+ erpCustomerId + " from recommender:" + recommender
					+ " for personal recommendation", e);
			throw new FDRuntimeException(e);
		} finally {
			close(conn);
		}
	}

	@Deprecated
	public EnumWinePrice getPreferredWinePrice(String erpCustomerId)
			throws RemoteException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();

			ps = conn
					.prepareStatement("SELECT WINE_PRICE_CAT FROM CUST.SS_CUSTOMER_WINE_CAT WHERE CUSTOMER_ID = ?");
			ps.setString(1, erpCustomerId);

			rs = ps.executeQuery();
			if (rs.next()) {
				int priceCat = rs.getInt("WINE_PRICE_CAT");
				return EnumWinePrice.getByDollarCount(priceCat);
			}
			return null;
		} catch (SQLException e) {
			LOGGER.warn("Could not retrieve preferred wine price  for "
					+ erpCustomerId, e);
			throw new FDRuntimeException(e);
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}

}
