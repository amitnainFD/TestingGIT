package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJBException;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.Image;
import com.freshdirect.fdstore.content.PriceCalculator;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.YmalSource;
import com.freshdirect.fdstore.customer.FDAuthenticationException;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDUser;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.ejb.FDCustomerManagerSB;
import com.freshdirect.fdstore.customer.ejb.FDSessionBeanSupport;
import com.freshdirect.fdstore.pricing.ProductPricingFactory;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.fdstore.util.ProductDisplayUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.fdstore.FDStoreRecommender;
import com.freshdirect.smartstore.fdstore.Recommendations;

public class OfflineRecommenderSessionBean extends FDSessionBeanSupport {
	private static final long serialVersionUID = 8606750945494173083L;

	private static final Logger LOGGER = LoggerFactory
			.getInstance(OfflineRecommenderSessionBean.class);


	private FDUserI getUserById(String customerId) throws FDResourceException {
		try {
			FDCustomerManagerSB sb = getFDCustomerManager();
			FDIdentity identity = new FDIdentity(customerId);
			FDUser user = sb.recognize(identity);
			return user;
		} catch (RemoteException re) {
			throw new FDResourceException(re, "Error talking to session bean");
		} catch (FDAuthenticationException ae) {
			throw new FDResourceException(ae, "Unrecognized user");
		}
	}

	private SessionInput createSessionInput(FDUserI user,
			ContentNodeModel currentNode) {
		SessionInput input = new SessionInput(user);
		if (currentNode != null) {
			input.setCurrentNode(currentNode);
			if (currentNode instanceof YmalSource)
				input.setYmalSource((YmalSource) currentNode);
		}
		input.setNoShuffle(true);
		input.setIncludeCartItems(true);
		return input;
	}

	private static final String DELETE_OFFLINE_RECOMMENDATION = "DELETE FROM CUST.SS_OFFLINE_RECOMMENDATION"
			+ " WHERE CUSTOMER_ID = ? AND SITE_FEATURE_ID = ?";
	private static final String INSERT_OFFLINE_RECOMMENDATION = "INSERT INTO CUST.SS_OFFLINE_RECOMMENDATION"
			+ " (LAST_MODIFIED, CUSTOMER_ID, SITE_FEATURE_ID, VARIANT_ID, PRODUCT_ID, POSITION, NAME, LINK, IMAGE_PATH, IMAGE_WIDTH, IMAGE_HEIGHT, RATING, PRICE, WAS_PRICE, TIERED_PRICE, ABOUT_PRICE, BURST)"
			+ " VALUES (SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private int saveRecommendations(FDUserI user,
			Recommendations recommendations) {
		Connection conn = null;

		try {
			conn = getConnection();
			PreparedStatement ps = conn
					.prepareStatement(DELETE_OFFLINE_RECOMMENDATION);
			ps.setString(1, user.getIdentity().getErpCustomerPK());
			ps.setString(2, recommendations.getVariant().getSiteFeature()
					.getName());
			ps.executeUpdate();
			ps.close();
			ps = null;

			int rowCount = 0;
			int n = Math.min(5, recommendations.getProducts().size());
			EnumSiteFeature siteFeature = recommendations.getVariant()
					.getSiteFeature();

			for (int i = 0; i < n; i++) {
				ps = conn.prepareStatement(INSERT_OFFLINE_RECOMMENDATION);
				ps.setString(1, user.getIdentity().getErpCustomerPK());
				ps.setString(2, siteFeature.getName());
				ps.setString(3, recommendations.getVariant().getId());
				ProductModel product = recommendations.getProducts().get(i);
				ps.setString(4, product.getContentName());
				ps.setInt(5, i + 1);

				// name
				String fullName = product.getFullName();
				ps.setString(6, fullName != null
						&& !"".equalsIgnoreCase(fullName) ? fullName
						: "(this product)");

				// link
				ps.setString(7, ProductDisplayUtil.getProductURI(product));

				// image
				Image productImage = product.getProdImage();
				ps.setString(8, productImage.getPath());
				ps.setInt(9, productImage.getWidth());
				ps.setInt(10, productImage.getHeight());

				// rating
				try {
					String rating = ProductDisplayUtil.getProductRatingCode(
							user, product);
					if (rating == null)
						ps.setNull(11, Types.VARCHAR);
					else
						ps.setString(11, rating);
				} catch (FDResourceException e) {
					ps.setNull(11, Types.VARCHAR);
				} catch (FDSkuNotFoundException e) {
					ps.setNull(11, Types.VARCHAR);
				}
				//Get Product Pricing calculator
				PriceCalculator pc = new PriceCalculator(user.getPricingContext(), product);
				// price, pricing
				ps.setString(12, pc.getPriceFormatted(0));
				ps.setString(13, pc.getWasPriceFormatted(0));
				ps.setString(14, pc.getTieredPrice(0));
				ps.setString(15, pc.getAboutPriceFormatted(0));

				// burst
				String burst = ProductDisplayUtil.getProductBurstCode(user,
						siteFeature, product, pc);
				if (burst == null)
					ps.setNull(16, Types.VARCHAR);
				else
					ps.setString(16, burst);
				rowCount += ps.executeUpdate();
				ps.close();
				ps = null;
			}

			return rowCount;
		} catch (SQLException e) {
			LOGGER.error("saving recommendations failed : "+ e.getMessage(), e);
			throw new EJBException(e);
		} finally {
		    close(conn);
		}
	}

	private static final String QUERY_RECENT_CUSTOMERS = "SELECT DISTINCT customer_id FROM cust.sale"
			+ " WHERE status = 'STL' AND cromod_date > sysdate - ?";

	public Set<String> getRecentCustomers(int days) throws RemoteException,
			FDResourceException {
		Set<String> customerIds = new HashSet<String>(200000);
		Connection conn = null;

		try {
			conn = getConnection();
			PreparedStatement ps = conn
					.prepareStatement(QUERY_RECENT_CUSTOMERS);
			ps.setInt(1, days);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				customerIds.add(rs.getString(1));
			}
			rs.close();
			rs = null;

			ps.close();
			ps = null;

		} catch (SQLException e) {
			LOGGER.error("retrieving recent customers failed " + e.getMessage(), e);
			throw new EJBException(e);
		} finally {
		    close(conn);
		}
		return customerIds;
	}

       private static final String REMOVE_NOT_UPDATED_RECOMMENDATIONS = "DELETE FROM CUST.SS_OFFLINE_RECOMMENDATION"
                   + " WHERE last_modified < sysdate - ?";

	public int removeOldRecommendation(int age) {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                    conn = getConnection();
                    ps = conn.prepareStatement(REMOVE_NOT_UPDATED_RECOMMENDATIONS);
                    ps.setInt(1, age);
                    int count = ps.executeUpdate();
                    return count;
            } catch (SQLException e) {
                    LOGGER.error("removing old customers failed: "+e.getMessage(), e);
                    throw new EJBException(e);
            } finally {
                close(ps);
                close(conn);
            }
	}
	
	
	private static final String QUERY_UPDATED_CUSTOMERS = "SELECT DISTINCT customer_id FROM CUST.SS_OFFLINE_RECOMMENDATION"
			+ " WHERE last_modified > sysdate - ?";

	
	
	public Set<String> getUpdatedCustomers(int age) throws RemoteException,
			FDResourceException {
		Set<String> customerIds = new HashSet<String>(200000);
		Connection conn = null;

		try {
			conn = getConnection();
			PreparedStatement ps = conn
					.prepareStatement(QUERY_UPDATED_CUSTOMERS);
			ps.setInt(1, age);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				customerIds.add(rs.getString(1));
			}
			rs.close();
			rs = null;

			ps.close();
			ps = null;

		} catch (SQLException e) {
			LOGGER.error("retrieving recent customers failed : "+e.getMessage(), e);
			throw new EJBException(e);
		} finally {
		    close(conn);
		}
		return customerIds;
	}

	public void checkSiteFeature(String siteFeatureName)
			throws RemoteException, FDResourceException {
		EnumSiteFeature siteFeature = EnumSiteFeature.getEnum(siteFeatureName);
		if (siteFeature == null)
			throw new FDResourceException("unknown site feature: "
					+ siteFeatureName);
	}

	public int recommend(String[] siteFeatureNames, String customerId,
			String currentNodeId) throws RemoteException, FDResourceException {
		int n = 0;
		for (String siteFeatureName : siteFeatureNames) {
			EnumSiteFeature siteFeature = EnumSiteFeature.getEnum(siteFeatureName);
			if (siteFeature == null)
				throw new FDResourceException("unknown site feature: "
						+ siteFeatureName);
			ContentNodeModel currentNode = null;
			if (currentNodeId != null) {
				currentNode = ContentFactory.getInstance().getContentNode(
						currentNodeId);
				if (currentNode == null)
					throw new FDResourceException("unknown current node: "
							+ currentNodeId);
			}
			FDUserI user = getUserById(customerId);
			SessionInput input = createSessionInput(user, currentNode);
			input.setMaxRecommendations(5);
			input.setIncludeCartItems(false);
			Recommendations recs = FDStoreRecommender.getInstance()
					.getRecommendations(siteFeature, user, input);
			saveRecommendations(user, recs);
			n += recs.getProducts().size();
		}
		return n;
	}
}
