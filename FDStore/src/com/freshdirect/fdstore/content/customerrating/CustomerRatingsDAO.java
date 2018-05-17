package com.freshdirect.fdstore.content.customerrating;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.framework.core.DataSourceLocator;
import com.freshdirect.framework.core.SequenceGenerator;
import com.freshdirect.framework.util.SqlUtil;
import com.freshdirect.framework.util.log.LoggerFactory;


@Deprecated
public class CustomerRatingsDAO {

	private static final Logger LOGGER = LoggerFactory.getInstance(CustomerRatingsDAO.class);
	
	private String getNextId(Connection conn) throws SQLException {
		return SequenceGenerator.getNextId(conn, "MIS");
	}
	
	public void purgeFeedFileContentStorage() throws FDResourceException {
		String productSql = "delete from MIS.BV_PRODUCT_RATINGS";
		String reviewSql = "delete from MIS.BV_PRODUCT_REVIEWS";
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = getConnection();
			ps = connection.prepareStatement(reviewSql);
			ps.execute();
			ps = connection.prepareStatement(productSql);
			ps.execute();
			
		} catch (SQLException e) { 
			LOGGER.error("Purging feed storage failed!",e);
        	throw new FDResourceException("purgeFeedFileContentStorage", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (connection != null) {
					connection.close();		
				}
			} catch (SQLException e) {
				LOGGER.error("Purging feed storage failed!",e);
	        	throw new FDResourceException("purgeFeedFileContentStorage", e);
			}		
		}
	}
	
	public void storeFeedFileContent(List<CustomerRatingsDTO> ratedProducts) throws FDResourceException {
		String ratingsSql = "insert into MIS.BV_PRODUCT_RATINGS " +
				"(ID, PRODUCT_ID, PRODUCT_PAGE_URL, PRODUCT_REVIEWS_URL, IMAGE_URL, NUM_NATIVE_QUESTIONS, NUM_QUESTIONS, NUM_NATIVE_ANSWERS, NUM_ANSWERS, " +
				"NUM_REVIEWS, NUM_STORIES, AVERAGE_OVERALL_RATING, OVERALL_RATING_RANGE, TOTAL_REVIEW_COUNT, RATINGS_ONLY_REVIEW_COUNT, RECOMMENDED_COUNT, " +
				"NOT_RECOMMENDED_COUNT, AVERAGE_RATING_QUALITY, AVERAGE_RATING_QUALITY_RANGE, AVERAGE_RATING_VALUE, AVERAGE_RATING_VALUE_RANGE, RATING_VALUE, " +
				"COUNT, NAT_AVERAGE_OVERALL_RATING, NAT_OVERALL_RATING_RANGE, NAT_TOTAL_REVIEW_COUNT, NAT_RATINGS_ONLY_REVIEW_COUNT, NAT_RECOMMENDED_COUNT, " +
				"NAT_NOT_RECOMMENDED_COUNT, NAT_AVERAGE_RATING_QUALITY, NAT_AVERAGE_RATING_QUALITY_R, NAT_AVERAGE_RATING_VALUE, NAT_AVERAGE_RATING_VALUE_RANGE, " +
				"NAT_RATING_VALUE, NAT_COUNT, EXTRACT_DATE) " +
				"values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		String reviewsSql = "insert into MIS.BV_PRODUCT_REVIEWS " +
				"(ID, PRODUCT_ID, BV_REVIEW_ID, MODERATION_STATUS, LAST_MODIFICATION_TIME, EXTERNAL_ID, DISPLAY_NAME, ANONYMUS, HYPERLINKING_ENABLED, RATINGS_ONLY, " +
				"TITLE, REVIEW_TEXT, NUM_COMMENTS, CAMPAIGN_ID, RATING, RATING_RANGE, RECOMMENDED, NUM_FEEDBACKS, NUM_POSITIVE_FEEDBACKS, NUM_NEGATIVE_FEEDBACKS, " +
				"REVIEWER_LOCATION, IP_ADDRESS, DISPLAY_LOCALE, SUBMISSION_TIME, BADGE_NAME, BADGE_CONTENT_TYPE, PRODUCT_REVIEWS_URL, PRODUCT_REVIEWS_DL_URL, FEATURED, " +
				"NET_PROMOTER_SCORE, NET_PROMOTER_COMMENT, AUTHENTICATION_TYPE, USER_EMAIL_ADDRESS, PUBLISHED_EMAIL_ALERT, COMMENTED_EMAIL_ALERT, ORIGINATING_DISPLAY_CODE, " +
				"CONTENT_CODES, FIRST_PUBLISH_TIME, LAST_PUBLISH_TIME)" +
				"values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = getConnection();
			ps = connection.prepareStatement(ratingsSql);
			 
			final int batchSize = 500; 
			//WARNING
			//"java.sql.SQLException: executeBatch, Exception = -32373" in case of 1000 (maybe 36*1000 values are to much for the old 16bit driver)
			//works with 500, but fails after setting to 1000 and back to 500 during debug
			//more info at http://www-01.ibm.com/support/docview.wss?uid=swg21432290 fixed by Oracle in BUG-6396242
			
			int count = 0;
			 
			for (CustomerRatingsDTO ratedProduct: ratedProducts) {
				
				ps.setString(1, getNextId(connection));
				SqlUtil.setString(ps, 2, ratedProduct.getProductId());
				SqlUtil.setString(ps, 3, ratedProduct.getProductPageURL());
				SqlUtil.setString(ps, 4, ratedProduct.getProductReviewsURL());
				SqlUtil.setString(ps, 5, ratedProduct.getImageURL());
				SqlUtil.setInt(ps, 6, ratedProduct.getNumNativeQuestions());
				SqlUtil.setInt(ps, 7, ratedProduct.getNumQuestions());
				SqlUtil.setInt(ps, 8, ratedProduct.getNumNativeAnswers());
				SqlUtil.setInt(ps, 9, ratedProduct.getNumAnswers());
				SqlUtil.setInt(ps, 10, ratedProduct.getNumReviews());
				SqlUtil.setInt(ps, 11, ratedProduct.getNumStories());
				SqlUtil.setBigDecimal(ps, 12, ratedProduct.getAverageOverallRating());
				SqlUtil.setInt(ps, 13, ratedProduct.getOverallRatingRange());
				SqlUtil.setInt(ps, 14, ratedProduct.getTotalReviewCount());
				SqlUtil.setInt(ps, 15, ratedProduct.getRatingsOnlyReviewCount());
				SqlUtil.setInt(ps, 16, ratedProduct.getRecommendedCount());
				SqlUtil.setInt(ps, 17, ratedProduct.getNotRecommendedCount());
				SqlUtil.setBigDecimal(ps, 18, ratedProduct.getAverageRatingValuesQuality());
				SqlUtil.setInt(ps, 19, ratedProduct.getAverageRatingValuesQualityRange());
				SqlUtil.setBigDecimal(ps, 20, ratedProduct.getAverageRatingValuesValue());
				SqlUtil.setInt(ps, 21, ratedProduct.getAverageRatingValuesValueRange());
				SqlUtil.setInt(ps, 22, ratedProduct.getRatingValue());
				SqlUtil.setInt(ps, 23, ratedProduct.getCount());
				SqlUtil.setBigDecimal(ps, 24, ratedProduct.getNatAverageOverallRating());
				SqlUtil.setInt(ps, 25, ratedProduct.getNatOverallRatingRange());
				SqlUtil.setInt(ps, 26, ratedProduct.getNatTotalReviewCount());
				SqlUtil.setInt(ps, 27, ratedProduct.getNatRatingsOnlyReviewCount());
				SqlUtil.setInt(ps, 28, ratedProduct.getNatRecommendedCount());
				SqlUtil.setInt(ps, 29, ratedProduct.getNatNotRecommendedCount());
				SqlUtil.setBigDecimal(ps, 30, ratedProduct.getNatAverageRatingValuesQuality());
				SqlUtil.setInt(ps, 31, ratedProduct.getNatAverageRatingValuesQualityRange());
				SqlUtil.setBigDecimal(ps, 32, ratedProduct.getNatAverageRatingValuesValue());
				SqlUtil.setInt(ps, 33, ratedProduct.getNatAverageRatingValuesValueRange());
				SqlUtil.setInt(ps, 34, ratedProduct.getNatRatingValue());
				SqlUtil.setInt(ps, 35, ratedProduct.getNatCount());
				SqlUtil.setTimestamp(ps, 36, ratedProduct.getExtractDate());

			    ps.addBatch();
			     
			    if(++count % batchSize == 0) {
			        ps.executeBatch();
			    }
			}
			ps.executeBatch();

			ps = connection.prepareStatement(reviewsSql);
			 
			count = 0;
			 
			for (CustomerRatingsDTO ratedProduct: ratedProducts) {
				for (CustomerReviewsDTO customerReview: ratedProduct.getReviews()) {
			 
					ps.setString(1, getNextId(connection));
					SqlUtil.setString(ps, 2, ratedProduct.getProductId());
					SqlUtil.setString(ps, 3, customerReview.getBVReviewId());
					SqlUtil.setString(ps, 4, customerReview.getModerationStatus());
					SqlUtil.setTimestamp(ps, 5, customerReview.getLastModificationTime());
					SqlUtil.setString(ps, 6, customerReview.getExternalId());
					SqlUtil.setString(ps, 7, customerReview.getDisplayName());
					SqlUtil.setBoolean(ps, 8, customerReview.isAnonymous());
					SqlUtil.setBoolean(ps, 9, customerReview.isHyperlinkingEnabled());
					SqlUtil.setBoolean(ps, 10, customerReview.isRatingsOnly());
					SqlUtil.setString(ps, 11, customerReview.getTitle());
					SqlUtil.setString(ps, 12, customerReview.getReviewText());
					SqlUtil.setInt(ps, 13, customerReview.getNumComments());
					SqlUtil.setString(ps, 14, customerReview.getCampaignId());
					SqlUtil.setBigDecimal(ps, 15, customerReview.getRating());
					SqlUtil.setInt(ps, 16, customerReview.getRatingRange());
					SqlUtil.setBoolean(ps, 17, customerReview.isRecommended());
					SqlUtil.setInt(ps, 18, customerReview.getNumFeedbacks());
					SqlUtil.setInt(ps, 19, customerReview.getNumPositiveFeedbacks());
					SqlUtil.setInt(ps, 20, customerReview.getNumNegativeFeedbacks());
					SqlUtil.setString(ps, 21, customerReview.getReviewerLocation());
					SqlUtil.setString(ps, 22, customerReview.getIpAddress());
					SqlUtil.setString(ps, 23, customerReview.getDisplayLocale());
					SqlUtil.setTimestamp(ps, 24, customerReview.getSubmissionTime());
					SqlUtil.setString(ps, 25, customerReview.getBadgeName());
					SqlUtil.setString(ps, 26, customerReview.getBadgeContentType());
					SqlUtil.setString(ps, 27, customerReview.getProductReviewsURL());
					SqlUtil.setString(ps, 28, customerReview.getProductReviewsDLURL());
					SqlUtil.setBoolean(ps, 29, customerReview.isFeatured());
					SqlUtil.setBigDecimal(ps, 30, customerReview.getNetPromoterScore());
					SqlUtil.setString(ps, 31, customerReview.getNetPromoterComment());
					SqlUtil.setString(ps, 32, customerReview.getAuthenticationType());
					SqlUtil.setString(ps, 33, customerReview.getUserEmailAddress());
					SqlUtil.setBoolean(ps, 34, customerReview.isPublishedEmailAlert());
					SqlUtil.setBoolean(ps, 35, customerReview.isCommentedEmailAlert());
					SqlUtil.setString(ps, 36, customerReview.getOriginatingDisplayCode());
					SqlUtil.setString(ps, 37, customerReview.getContentCodes());
					SqlUtil.setTimestamp(ps, 38, customerReview.getFirstPublishTime());
					SqlUtil.setTimestamp(ps, 39, customerReview.getLastPublishTime());
	
					ps.addBatch();
				     
				    if(++count % batchSize == 0) {
				    	LOGGER.debug("count:"+ count);
				    	ps.executeBatch();
				    }
				     
				}
			}
			LOGGER.debug("count:"+ count);
			ps.executeBatch();

		} catch (SQLException e) { 
			LOGGER.error("Storing feed failed!",e);
        	throw new FDResourceException("storeFeedFileContent", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (connection != null) {
					connection.close();		
				}
			} catch (SQLException e) {
				LOGGER.error("Storing feed failed!",e);
	        	throw new FDResourceException("storeFeedFileContent", e);
			}		
		}
		
	}
	
	public Map<String,CustomerRatingsDTO> getCustomerRatings() throws FDResourceException {
		String sql = "select * from MIS.BV_PRODUCT_RATINGS";
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map<String,CustomerRatingsDTO> ratedProducts = new HashMap<String,CustomerRatingsDTO>();
		
		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery(); 
			 
			while (rs.next()) {
				
				CustomerRatingsDTO ratedProduct = new CustomerRatingsDTO();
				ratedProduct.setId(rs.getString("id"));
				String productId = rs.getString("product_id");
				ratedProduct.setProductId(productId);
				ratedProduct.setAverageOverallRating(rs.getBigDecimal("average_overall_rating"));
				ratedProduct.setTotalReviewCount(rs.getInt("total_review_count"));
				ratedProduct.setRatingValue(rs.getInt("rating_value"));
			    
				if (productId!=null){
					ratedProducts.put(productId, ratedProduct);
				}
				
			}
		} catch (SQLException e) { 
			LOGGER.error("Getting rated products failed!",e);
        	throw new FDResourceException("getCustomerRatings", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (connection != null) {
					connection.close();		
				}
			} catch (SQLException e) {
				LOGGER.error("Getting rated products failed!!",e);
	        	throw new FDResourceException("getCustomerRatings", e);
			}		
		}
		return ratedProducts;
		
	}
	
	public long getTimestamp() throws FDResourceException {
		
		long timestamp = -1;
		
		String sql = "select extract_date from MIS.BV_PRODUCT_RATINGS where rownum = 1";
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			connection = getConnection();
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery(); 
			 
			if (rs.next()) {
				timestamp = rs.getTimestamp("extract_date").getTime();
			}
		} catch (SQLException e) { 
			LOGGER.error("Getting ratings timestamp failed!",e);
        	throw new FDResourceException("getTimeStamp", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (connection != null) {
					connection.close();		
				}
			} catch (SQLException e) {
				LOGGER.error("Getting ratings timestamp failed!",e);
	        	throw new FDResourceException("getTimeStamp", e);
			}		
		}
		return timestamp;

	}
	
	public Connection getConnection() throws SQLException {
   		return DataSourceLocator.getConnectionByDatasource("fddatasource");
    }
	
}
