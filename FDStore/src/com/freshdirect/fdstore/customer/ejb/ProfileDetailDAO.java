package com.freshdirect.fdstore.customer.ejb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Category;

import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;


public class ProfileDetailDAO extends SessionBeanSupport{	

	private static final Category LOGGER = LoggerFactory.getInstance(ProfileDetailDAO.class);	
	public static String getCustomersProfile(Connection con,String customerId) throws SQLException{
		LOGGER.info( "customerId # # @ #: "+customerId);
		String profileValue = null;	 		
		try{
			String query = "SELECT * FROM CUST.PROFILE WHERE PROFILE_NAME ='MarketingPromo' AND CUSTOMER_ID = ?" ;
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, customerId);
			ResultSet rs = ps.executeQuery();	
			if(rs.next()){			
			profileValue = rs.getString("PROFILE_VALUE");	
			LOGGER.info( "Profile Value # # #: " + profileValue);

			}
			else{
				profileValue = "NOT_AVAILABLE";
				LOGGER.info( "Profile Value # # #: " + profileValue);

			}
//			LOGGER.info( "Profile Value # # #: " + rs.getString("PROFILE_VALUE"));
			
			rs.close();
			ps.close();
				
		}catch(Exception ex){
			ex.printStackTrace();
		}
		finally{
			if(con!=null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		   }
			
		}
		return profileValue;
			
	}

		
	public static String getCustomerCounty(Connection con,String customerId) throws SQLException{
		LOGGER.info( "county customerid # # @ # : "+customerId);
		String county = null;	 		
		try{
			String query = "select cs.COUNTY from dlv.zipplusfour zpf, dlv.city_state cs where "
		                   + "zipcode =(select di.zip from cust.sale s, cust.salesaction sa, cust.deliveryinfo di where "
					       + "s.id =(select id from (select s.id from cust.sale s, cust.salesaction sa where " 
		                   + "s.customer_id = ? and s.id= sa.sale_id and s.type='REG' and sa.action_type='CRO' order by action_date desc) where rownum =1) "
					       + "and s.id = sa.sale_id and sa.id = di.salesaction_id and s.type = 'REG' and sa.action_type in ('CRO', 'MOD') " 
		                   + "and sa.action_date = (select max(action_date) from cust.salesaction where "
					       + "s.id = sale_id and action_type in ('CRO', 'MOD'))) and zpf.CITY_STATE_KEY = cs.CITY_STATE_KEY group by cs.county";
			PreparedStatement ps = con.prepareStatement(query);
			ps.setString(1, customerId);			
			ResultSet rs = ps.executeQuery();
			if(rs.next()){
			county = rs.getString("county");	
			LOGGER.info( "Profile County # # #: " + county);
			}
			else{
				county ="NOT_AVAILABLE";
				LOGGER.info( "Profile County # # #: " + county);
			}
						
//			LOGGER.info( "Profile county # # #: " + rs.getString("county"));
			
			rs.close();
			ps.close();
				
		}catch(Exception ex){
			ex.printStackTrace();
		}
		finally{
			if(con!=null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		   }
			
		}
		return county;			
	}
	
	
}
