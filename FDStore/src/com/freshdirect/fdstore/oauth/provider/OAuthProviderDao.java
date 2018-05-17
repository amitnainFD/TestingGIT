package com.freshdirect.fdstore.oauth.provider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.Map;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;

import org.apache.log4j.Category;

import com.freshdirect.framework.core.SequenceGenerator;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * @author Tamas Gelesz
 */
public class OAuthProviderDao {
	private final static Category LOGGER = LoggerFactory.getInstance(OAuthProviderDao.class);
	
	private static final String SELECT_ACCESSOR_BY_TOKEN = 
		"select a.id, a.consumer_key, a.request_token, a.access_token, a.token_secret, p.name, p.value " +
		"from cust.oauth_accessors a left join cust.oauth_accessor_properties p on a.id=p.oauth_accessor_id " +
		"where a.request_token = ? or a.access_token = ?";

	private static final String DB_SCHEMA = "cust";
	private static final String INSERT_ACCESSOR = "insert into cust.oauth_accessors (consumer_key, request_token, access_token, token_secret, last_modified_date, id) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_ACCESSOR = "update cust.oauth_accessors set consumer_key=?, request_token=?, access_token=?, token_secret=?, last_modified_date=? where id=?";
	private static final String DELETE_ACCESSOR_BY_TIME = "delete from cust.oauth_accessors where last_modified_date < ?";
	
	private static final String INSERT_ACCESSOR_PROPERTY = "insert into cust.oauth_accessor_properties (oauth_accessor_id, name, value) VALUES (?, ?, ?)";
	private static final String DELETE_ACCESSOR_PROPERTY = "delete from cust.oauth_accessor_properties where oauth_accessor_id=?";

	public static void deleteAccessors(Connection conn, Date untilDate) throws SQLException{
		PreparedStatement ps = null;
		try {			
			ps = conn.prepareStatement(DELETE_ACCESSOR_BY_TIME);
			ps.setTime(1, new Time(untilDate.getTime()));
			
			int deleteCnt = ps.executeUpdate(); 

			LOGGER.info("Statement ("+DELETE_ACCESSOR_BY_TIME+") for date (" + untilDate +"): "+deleteCnt+" rows deleted");
			
		} finally {
			if (ps != null) ps.close();
		}
	}
	
	public static OAuthAccessor getAccessorByToken(Connection conn, String token, Map<String, OAuthConsumer> consumerMap) throws SQLException{
		OAuthAccessor accessor = null;

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {			
			ps = conn.prepareStatement(SELECT_ACCESSOR_BY_TOKEN);
			ps.setString(1, token);
			ps.setString(2, token);
			rs = ps.executeQuery();

			while (rs.next()) { 
				if (rs.isFirst()){
					accessor = new OAuthAccessor(consumerMap.get(rs.getString("consumer_key")));
					accessor.id = rs.getString("id");
					accessor.requestToken = rs.getString("request_token");
					accessor.accessToken = rs.getString("access_token");
					accessor.tokenSecret = rs.getString("token_secret");
				}
				String name = rs.getString("name");
				if (name != null){
					accessor.setProperty(name, rs.getString("value"));
				}
			}

		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		
        return accessor;
	}
	
	public static void storeAccessor(Connection conn, OAuthAccessor accessor) throws SQLException{
		
		//update
		String accessorSql = UPDATE_ACCESSOR;
		boolean deleteAccessorProperties = true;
		
		//insert
		if (accessor.id == null){
			accessor.id = SequenceGenerator.getNextId(conn, DB_SCHEMA);
			accessorSql = INSERT_ACCESSOR;
			deleteAccessorProperties = false;
		}

		
		PreparedStatement psOuathAccessor = null;
		PreparedStatement psOuathAccessorPropertyInsert = null;
		PreparedStatement psOuathAccessorPropertyDelete = null;
		
		try {
			conn.setAutoCommit(false);
			
			//insert or update oauth_accessors
			psOuathAccessor = conn.prepareStatement(accessorSql);
			psOuathAccessor.setString(1, accessor.consumer.consumerKey);
			psOuathAccessor.setString(2, accessor.requestToken);
			psOuathAccessor.setString(3, accessor.accessToken);
			psOuathAccessor.setString(4, accessor.tokenSecret);
			psOuathAccessor.setTime(5, new Time(new Date().getTime()));
			psOuathAccessor.setString(6, accessor.id);

			if (psOuathAccessor.executeUpdate() != 1) {
				throw new SQLException("Statement ("+accessorSql+") failed for OAuthAccessor (" + accessor +")");
			}


			//delete oauth_accessor_properties
			if (deleteAccessorProperties) {
				psOuathAccessorPropertyDelete = conn.prepareStatement(DELETE_ACCESSOR_PROPERTY);
				psOuathAccessorPropertyDelete.setString(1, accessor.id);
				int deleteCnt = psOuathAccessorPropertyDelete.executeUpdate();
				LOGGER.debug("Statement ("+DELETE_ACCESSOR_PROPERTY+"): " +deleteCnt+" rows deleted");
			}
			
			
			//insert oauth_accessor_properties
			psOuathAccessorPropertyInsert = conn.prepareStatement(INSERT_ACCESSOR_PROPERTY);

			for (Map.Entry<String, Object> entry : accessor.properties.entrySet()){
				String name = entry.getKey();
				
				if (name != null) {
					Object valueObj = entry.getValue();
					
					psOuathAccessorPropertyInsert.setString(1, accessor.id);
					psOuathAccessorPropertyInsert.setString(2, name);
					psOuathAccessorPropertyInsert.setString(3, valueObj == null ? null : valueObj.toString());
					
					if (psOuathAccessorPropertyInsert.executeUpdate() != 1) {
						throw new SQLException("Statement ("+INSERT_ACCESSOR_PROPERTY+") failed for OAuthAccessor: " + accessor + " property: " + entry);
					}
				}
			}
			
			conn.commit();
			
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} finally {
			if (psOuathAccessor != null) {
				psOuathAccessor.close();			
			}
			if (psOuathAccessorPropertyInsert != null) {
				psOuathAccessorPropertyInsert.close();			
			}
			if (psOuathAccessorPropertyDelete != null) {
				psOuathAccessorPropertyDelete.close();			
			}
		}
	}
}
