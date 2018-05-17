package com.freshdirect.fdstore.promotion.ejb;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.freshdirect.customer.EnumChargeType;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.ProductReferenceImpl;
import com.freshdirect.fdstore.promotion.ActiveInactiveStrategy;
import com.freshdirect.fdstore.promotion.AssignedCustomerParam;
import com.freshdirect.fdstore.promotion.AudienceStrategy;
import com.freshdirect.fdstore.promotion.CompositeStrategy;
import com.freshdirect.fdstore.promotion.DCPDiscountApplicator;
import com.freshdirect.fdstore.promotion.DCPDiscountRule;
import com.freshdirect.fdstore.promotion.DateRangeStrategy;
import com.freshdirect.fdstore.promotion.EnumOrderType;
import com.freshdirect.fdstore.promotion.EnumPromotionType;
import com.freshdirect.fdstore.promotion.FraudStrategy;
import com.freshdirect.fdstore.promotion.GeographyStrategy;
import com.freshdirect.fdstore.promotion.HeaderDiscountApplicator;
import com.freshdirect.fdstore.promotion.HeaderDiscountRule;
import com.freshdirect.fdstore.promotion.LimitedUseStrategy;
import com.freshdirect.fdstore.promotion.LineItemDiscountApplicator;
import com.freshdirect.fdstore.promotion.MaxLineItemCountStrategy;
import com.freshdirect.fdstore.promotion.OrderTypeStrategy;
import com.freshdirect.fdstore.promotion.PercentOffApplicator;
import com.freshdirect.fdstore.promotion.ProfileAttributeStrategy;
import com.freshdirect.fdstore.promotion.PromoVariantModel;
import com.freshdirect.fdstore.promotion.PromoVariantModelImpl;
import com.freshdirect.fdstore.promotion.Promotion;
import com.freshdirect.fdstore.promotion.PromotionApplicatorI;
import com.freshdirect.fdstore.promotion.PromotionGeography;
import com.freshdirect.fdstore.promotion.PromotionI;
import com.freshdirect.fdstore.promotion.PromotionStrategyI;
import com.freshdirect.fdstore.promotion.RecommendationStrategy;
import com.freshdirect.fdstore.promotion.RecommendedLineItemStrategy;
import com.freshdirect.fdstore.promotion.RedemptionCodeStrategy;
import com.freshdirect.fdstore.promotion.RuleBasedPromotionStrategy;
import com.freshdirect.fdstore.promotion.SampleLineApplicator;
import com.freshdirect.fdstore.promotion.SampleStrategy;
import com.freshdirect.fdstore.promotion.StateCountyStrategy;
import com.freshdirect.fdstore.promotion.UniqueUseStrategy;
import com.freshdirect.fdstore.promotion.WaiveChargeApplicator;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.framework.core.PrimaryKey;

public class FDPromotionDAO {

	/**
	 * Loads all active automatic promotions(redemption_code is null) which were not expired in last 7 days.
	 * 
	 * @param promoStrategyMap Map of String(promotionCode) -> PromotionStrategy
	 * @return List of Promotion
	 */
	private final static String getAllAutomaticPromotions = "SELECT * FROM CUST.PROMOTION p where p.active='X' and " +
	 							"(p.expiration_date > (sysdate-7) or p.expiration_date is null) and p.redemption_code is null order by p.modify_date desc";
	public static List loadAllAutomaticPromotions(Connection conn) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(getAllAutomaticPromotions);
		ResultSet rs = ps.executeQuery();
		List promotions =  loadPromotions(conn, rs, null);
		rs.close();
		ps.close();
		return promotions;
	}

	private final static String getModifiedOnlyPromotions = "SELECT * FROM CUST.PROMOTION where modify_date > ? order by modify_date desc";
	
	public static List loadModifiedOnlyPromotions(Connection conn, Date lastModified) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(getModifiedOnlyPromotions);
		ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		ResultSet rs = ps.executeQuery();
		List promotions = loadPromotions(conn, rs, lastModified);
		rs.close();
		ps.close();
		return promotions;
	}
	
	private static List loadPromotions(Connection conn, ResultSet rs, Date paramLastModified) throws SQLException {
		Map geoStrategies = loadGeographyStrategies(conn, paramLastModified);
		//Map custStrategies = FDPromotionDAO.loadAssignedCustomerStrategies(conn);
		List audiencePromoCodes = getAudienceBasedPromotionCodes(conn, paramLastModified);
		Map uniqueUseParticipation = loadUniqueUseParticipation(conn, paramLastModified);
		Map profileStrategies = loadAllProfiles(conn, paramLastModified);
		Map dcpdDiscApplicators = loadDCPDiscountApplicators(conn, paramLastModified);
		Map<String,StateCountyStrategy> stateCountyStrategies = loadStateCountyStrategies(conn,paramLastModified);
		List promos = new ArrayList();
		while (rs.next()) {
			PrimaryKey pk = new PrimaryKey(rs.getString("ID"));
			String promoCode = rs.getString("CODE");
			String name = rs.getString("NAME");
			String description = rs.getString("DESCRIPTION");
			EnumPromotionType promoType = EnumPromotionType.getEnum(rs.getString("CAMPAIGN_CODE"));
			if (promoType == null)
				continue;

			Timestamp lastModified = rs.getTimestamp("MODIFY_DATE");	
			Promotion promo = new Promotion(pk, promoType, promoCode, name, description, lastModified);
			if(audiencePromoCodes.contains(pk.getId())){
				boolean isMaxUsagePerCustomer = "X".equalsIgnoreCase(rs.getString("IS_MAX_USAGE_PER_CUST"));
				int rollingExpirationDays = rs.getInt("ROLLING_EXPIRATION_DAYS");
				AudienceStrategy aStrategy = new AudienceStrategy(isMaxUsagePerCustomer, rollingExpirationDays);
				promo.addStrategy(aStrategy);
			}

			promo.addStrategy(new ActiveInactiveStrategy("X".equalsIgnoreCase(rs.getString("ACTIVE"))));

			promo.addStrategy(new LimitedUseStrategy(rs.getInt("MAX_USAGE")));
			
			if("X".equalsIgnoreCase(rs.getString("RULE_BASED"))) {
				promo.addStrategy(new RuleBasedPromotionStrategy());
			}

			promo.addStrategy(new DateRangeStrategy(rs.getTimestamp("START_DATE"), rs.getTimestamp("EXPIRATION_DATE")));
			/*if (custStrategies.get(promo.getPK()) != null) {
				promo.addStrategy((AssignedCustomerStrategy) custStrategies.get(promo.getPK()));
			}*/

			String redemptionCode = rs.getString("REDEMPTION_CODE");
			if (!rs.wasNull()) {
				promo.addStrategy(new RedemptionCodeStrategy(redemptionCode));
			}
			
			if(!"X".equals(rs.getString("DONOT_APPLY_FRAUD"))){
				promo.addStrategy(new FraudStrategy());
			}
				
			decorateOrderTypestrategy(rs, promo);

			PromotionStrategyI geoStrategy = (PromotionStrategyI) geoStrategies.get(pk);
			if (geoStrategy != null) {
				promo.addStrategy(geoStrategy);
			}
						
			/*PromotionStrategyI custStrategy = (PromotionStrategyI) custStrategies.get(pk);
			if (custStrategy != null) {
				promo.addStrategy(custStrategy);
			}*/
			boolean uniqueUse = "X".equalsIgnoreCase(rs.getString("UNIQUE_USE"));
			if (uniqueUse) {
				Set saleIds = (Set) uniqueUseParticipation.get(promo.getPK().getId());
				promo.addStrategy(new UniqueUseStrategy(saleIds == null ? Collections.EMPTY_SET : saleIds));
			}

			if(promoType.getName().equals(EnumPromotionType.LINE_ITEM.getName())){
				boolean recItemsOnly = "X".equalsIgnoreCase(rs.getString("RECOMMENDED_ITEMS_ONLY"));
				promo.setRecommendedItemsOnly(recItemsOnly);
				if(recItemsOnly) {
					promo.addStrategy(new RecommendationStrategy());
				}
				if("X".equalsIgnoreCase(rs.getString("ALLOW_HEADER_DISCOUNT"))){				
				   promo.setCombineOffer(true);		
				}
				//promo.addLineItemStrategy(new RecommendedItemStrategy());
			}
			
			PromotionStrategyI stateCountyStrategyI = stateCountyStrategies.get(pk.getId());
			//System.out.println("\n\n##############Trying to see if promotion has state and county restriction:" + pk.getId() + "\nstateCountyStrategies:" + stateCountyStrategies);
			if(null != stateCountyStrategyI){
				//System.out.println("Adding the StateCounty Strategy");
				StateCountyStrategy scStrategy = (StateCountyStrategy)stateCountyStrategyI;
				promo.addStrategy(scStrategy);				
			}
				
			

			decorateSampleStrategy(rs, promo);
			//Load the profile strategy
			PromotionStrategyI profStrategy = (PromotionStrategyI) profileStrategies.get(pk);
			if (profStrategy != null) {
				promo.addStrategy(profStrategy);
			}
			PromotionApplicatorI applicator = null;
			if(promoType.getName().equals(EnumPromotionType.DCP_DISCOUNT.getName())){
				/*
				 * If the promotion is a Group Discount Promotion, get the correspoding group
				 * discount applicator object.
				 */
				applicator = (PromotionApplicatorI) dcpdDiscApplicators.get(pk);
				promo.addApplicator(applicator);
			} else {
				loadApplicator(rs, promo);				
			}

			/*
			if (applicator != null) {
				promo.setApplicator(applicator);
			}
			*/
			promos.add(promo);
		}
		return promos;
	}
	
	private final static String getPromoStateCountyData = 
		"select psc.id, psc.promotion_id, psc.states, psc.state_option, psc.county, psc.county_option " + 
		"from   CUST.PROMOTION_STATE_COUNTY psc " +
		"where  exists (SELECT 1 FROM CUST.PROMOTION_NEW " + 
				  	    "where status STATUSES " +
					      "and (expiration_date > (sysdate-7) or expiration_date is null) " + 
						  "and redemption_code is null " +
						  "and ID = psc.promotion_id " +
					  ")";

	private final static String getModifiedOnlyStateCountyData = 
		"SELECT psc.id, psc.promotion_id, psc.states,  psc.state_option, psc.county, psc.county_option " +
		  "FROM CUST.PROMOTION_STATE_COUNTY psc " +
		 "WHERE EXISTS " +
		          "(SELECT 1 " +
		             "FROM CUST.PROMOTION_NEW " +
		            "WHERE modify_date > ? AND ID = psc.promotion_id)";
	
	protected static Map<String,StateCountyStrategy> loadStateCountyStrategies(Connection conn, Date lastModified) throws SQLException {
		PreparedStatement ps;
		
		if(lastModified != null){			
			//System.out.println("\n\n\n\n\n\n\n\nLastmodified is: " + lastModified + "\nquery:" + getModifiedOnlyStateCountyData);
			ps = conn.prepareStatement(getModifiedOnlyStateCountyData);	
			ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		}else {
			String query = getPromoStateCountyData.replace("STATUSES", getStatusReplacementString());
			//System.out.println("\n\n\n\n\n\n\n\nLastmodified is null: " + query);
			ps = conn.prepareStatement(query);
		}
		
		ResultSet rs = ps.executeQuery();
		
		Map<String,StateCountyStrategy> strategies = new HashMap<String,StateCountyStrategy>();
		
		while (rs.next()) {
			String promoID = rs.getString("promotion_id");
			//System.out.println("PromoID: " + promoID);
			StateCountyStrategy scs = new StateCountyStrategy();
			scs.setPromotionId(promoID);
			scs.setState_option(rs.getString("state_option"));
			scs.setCounty_option(rs.getString("county_option"));
			Array array = rs.getArray("states");
			String[] states = (String[])array.getArray();
			scs.setStates(new java.util.HashSet(Arrays.asList(states)));
			Array array2 = rs.getArray("county");
			String[] county = (String[])array2.getArray();
			scs.setCounty(new java.util.HashSet(Arrays.asList(county)));
			strategies.put(promoID, scs);
		}
		rs.close();
		ps.close();
		
		return strategies;
	}
	
	private static String getStatusReplacementString() {
		
		String[] statuses = FDStoreProperties.getPromoValidRTStatuses().split(",");
		StringBuffer buf = new StringBuffer();
		if(statuses.length > 1) {
			buf.append("in (");	
			for (int i = 0; i < statuses.length; i++) {
	 			buf.append("'").append(statuses[i].trim()).append("'");
				if(i < statuses.length - 1) 
					buf.append(",");
			}
			buf.append(")");	
		}else {
 			buf.append("='").append(statuses[0].trim()).append("'");
		}
		return buf.toString();
	}
	
	private final static String getAllActiveAutomaticPromotionCodes = "SELECT CODE, MODIFY_DATE FROM CUST.PROMOTION p where p.active='X' and " +
		"(p.expiration_date > (sysdate-7) or p.expiration_date is null) and p.redemption_code is null";
	
	/**
	 * This method returns all active automatic promotion codes along
	 * with their last modified timestamp.
	 * TODO Later the Promotion codes has to be replaced with Promotion IDs when
	 * new data model changes for AI is implemented.
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map getAllAutomaticPromotionCodes(Connection conn) throws SQLException {
		Map promoCodes = new HashMap();
		//TODO later the where clause need to be changed to point to ID column instead of Code. 
		PreparedStatement ps = conn.prepareStatement(getAllActiveAutomaticPromotionCodes);
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			String promoCode = rs.getString("CODE");
			Timestamp lastModified = rs.getTimestamp("MODIFY_DATE");
			promoCodes.put(promoCode, lastModified);
		}

		rs.close();
		ps.close();

		return promoCodes;
	}
	
	/**
	 * Load Promotion for a given Promotion PK.
	 * @param conn
	 * @param promoId - The current value passed is Promotion CODE. Later when new data model
	 * for AI is implemented, value passed will be promotion ID.
	 * @return
	 * @throws SQLException
	 */
	public static PromotionI loadPromotion(Connection conn, String promoId) throws SQLException {
		Promotion promo = null;
		//TODO later the where clause need to be changed to point to ID column instead of Code. 
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM CUST.PROMOTION where code = ?");
		ps.setString(1, promoId);
		ResultSet rs = ps.executeQuery();

		if (rs.next()) {
			promo = constructPromotionFromResultSet(conn, rs);
		}

		rs.close();
		ps.close();

		return promo;
	}

	/**
	 * Load a redemption promotion
	 * @param conn
	 * @param redemptionCode
	 * @return redemption promoId - The current value returned is Promotion CODE. Later when new data model
	 * for AI is implemented, value returned will be promotion ID.
	 * @throws SQLException
	 */
	public static String getRedemptionPromotionId(Connection conn, String redemptionCode) throws SQLException {
		String promoId = null;
		//TODO later the select query need to be changed to return ID column instead of Code. 
		PreparedStatement ps = conn.prepareStatement("SELECT code FROM CUST.PROMOTION where UPPER(redemption_code) = UPPER(?) and " +
													"(expiration_date > (sysdate-1) or expiration_date is null)");
		ps.setString(1, redemptionCode);
		ResultSet rs = ps.executeQuery();

		if (rs.next()) {
			promoId = rs.getString("CODE");
		}

		rs.close();
		ps.close();

		return promoId;
	}
	
	/**
	 * @param conn
	 * @param promoPK
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private static Promotion constructPromotionFromResultSet(Connection conn, ResultSet rs) throws SQLException {
		Promotion promo;
		String promoId = rs.getString("ID");
		String promoCode = rs.getString("CODE");
		String name = rs.getString("NAME");
		String description = rs.getString("DESCRIPTION");
		EnumPromotionType promoType = EnumPromotionType.getEnum(rs.getString("CAMPAIGN_CODE"));


		Timestamp lastModified = rs.getTimestamp("MODIFY_DATE");	
		promo = new Promotion(new PrimaryKey(promoId), promoType, promoCode, name, description, lastModified);
		boolean audienceBased = isAudienceBasedPromotion(conn, promoId);
		if(audienceBased){
			boolean isMaxUsagePerCustomer = "X".equalsIgnoreCase(rs.getString("IS_MAX_USAGE_PER_CUST"));
			int rollingExpirationDays = rs.getInt("ROLLING_EXPIRATION_DAYS");
			AudienceStrategy aStrategy = new AudienceStrategy(isMaxUsagePerCustomer, rollingExpirationDays);
			promo.addStrategy(aStrategy);
		}
		promo.addStrategy(new ActiveInactiveStrategy("X".equalsIgnoreCase(rs.getString("ACTIVE"))));

		promo.addStrategy(new LimitedUseStrategy(rs.getInt("MAX_USAGE")));
		
		if("X".equalsIgnoreCase(rs.getString("RULE_BASED"))) {
			promo.addStrategy(new RuleBasedPromotionStrategy());
		}

		promo.addStrategy(new DateRangeStrategy(rs.getTimestamp("START_DATE"), rs.getTimestamp("EXPIRATION_DATE")));
		/*if (custStrategies.get(promo.getPK()) != null) {
			promo.addStrategy((AssignedCustomerStrategy) custStrategies.get(promo.getPK()));
		}*/

		String redemptionCode = rs.getString("REDEMPTION_CODE");
		if (!rs.wasNull()) {
			promo.addStrategy(new RedemptionCodeStrategy(redemptionCode));
		}
		
		if(!"X".equals(rs.getString("DONOT_APPLY_FRAUD"))){
			promo.addStrategy(new FraudStrategy());
		}

		decorateOrderTypestrategy(rs, promo);
		GeographyStrategy geoStrategy = loadGeographyStrategy(conn, promoId);
		if (geoStrategy != null) {
			promo.addStrategy(geoStrategy);
		}

		/*PromotionStrategyI custStrategy = (PromotionStrategyI) custStrategies.get(pk);
		if (custStrategy != null) {
			promo.addStrategy(custStrategy);
		}*/
		boolean uniqueUse = "X".equalsIgnoreCase(rs.getString("UNIQUE_USE"));
		if (uniqueUse) {
			Set saleIds = loadUniqueUseParticipationForPromo(conn, promoId);
			promo.addStrategy(new UniqueUseStrategy(saleIds == null ? Collections.EMPTY_SET : saleIds));
		}

		decorateSampleStrategy(rs, promo);
		PromotionStrategyI profStrategy = loadProfiles(conn, promoId);
		if(profStrategy != null){
			promo.addStrategy(profStrategy);	
		}
		
		if(promoType.getName().equals(EnumPromotionType.LINE_ITEM.getName())){
			boolean recItemsOnly = "X".equalsIgnoreCase(rs.getString("RECOMMENDED_ITEMS_ONLY"));
			promo.setRecommendedItemsOnly(recItemsOnly);
			if(recItemsOnly) {
				promo.addStrategy(new RecommendationStrategy());
			}
			if("X".equalsIgnoreCase(rs.getString("ALLOW_HEADER_DISCOUNT"))){				
			   promo.setCombineOffer(true);		
			}
			//promo.addLineItemStrategy(new RecommendedItemStrategy());
		}
		
		//System.out.println("Loading promotion:" + promoId);
		if("STCO".equals(rs.getString("GEO_RESTRICTION_TYPE"))) {
			//Add the StateCountyStrategy
			//System.out.println("#########adding the statecounty strategy");
			PromotionStrategyI scStrategy = loadStateCountyStrategy(conn, promoId);
			if(scStrategy != null)
				promo.addStrategy(scStrategy);
		}
				
		
		PromotionApplicatorI applicator = null;
		if(promoType.getName().equals(EnumPromotionType.DCP_DISCOUNT.getName())){
			/*
			 * If the promotion is a Group Discount Promotion, load the group
			 * discount applicator object.
			 */
			applicator = loadDCPDiscountApplicator(conn, promoId);
			promo.addApplicator(applicator);
		} else {
			 loadApplicator(rs, promo);				
		}
		/*
		if (applicator != null) {
			promo.setApplicator(applicator);
		}
		*/
		return promo;
	}
	
	protected static PromotionStrategyI loadStateCountyStrategy(Connection conn, String promoId) throws SQLException {
		PromotionStrategyI strategy = null;
		PreparedStatement ps = conn.prepareStatement("SELECT psc.id, psc.promotion_id, psc.states,  psc.state_option, psc.county, psc.county_option " + 
													 "FROM CUST.PROMOTION_STATE_COUNTY psc WHERE psc.promotion_id = ?");
		ps.setString(1, promoId);
		ResultSet rs = ps.executeQuery();
		StateCountyStrategy scs = null;
		while (rs.next()) {
			String promoID = rs.getString("promotion_id");
			//System.out.println("PromoID: " + promoID);
			scs = new StateCountyStrategy();
			scs.setPromotionId(promoID);
			scs.setState_option(rs.getString("state_option"));
			scs.setCounty_option(rs.getString("county_option"));
			Array array = rs.getArray("states");
			String[] states = (String[])array.getArray();
			scs.setStates(new java.util.HashSet(Arrays.asList(states)));
			Array array2 = rs.getArray("county");
			String[] county = (String[])array2.getArray();
			scs.setCounty(new java.util.HashSet(Arrays.asList(county)));
		}
		rs.close();
		ps.close();

		return scs;
	}
	
	private static void decorateOrderTypestrategy(ResultSet rs, Promotion promo) throws SQLException {
		Set orderTypes = new HashSet();

		if ("X".equals(rs.getString("ORDERTYPE_HOME"))) {
			orderTypes.add(EnumOrderType.HOME);
		}

		if ("X".equals(rs.getString("ORDERTYPE_DEPOT"))) {
			orderTypes.add(EnumOrderType.DEPOT);
		}
		if ("X".equals(rs.getString("ORDERTYPE_PICKUP"))) {
			orderTypes.add(EnumOrderType.PICKUP);
		}

		if ("X".equals(rs.getString("ORDERTYPE_CORPORATE"))) {
			orderTypes.add(EnumOrderType.CORPORATE);
		}

		if (!orderTypes.isEmpty()) {
			promo.addStrategy(new OrderTypeStrategy(orderTypes));
		}
	}

	private static void decorateSampleStrategy(ResultSet rs, Promotion promo) throws SQLException {
		SampleStrategy s = new SampleStrategy();

		boolean valid = false;

		boolean needDryGoods = "X".equals(rs.getString("NEEDDRYGOODS"));
		if (!rs.wasNull()) {
			valid = true;
			s.setNeedDryGoods(needDryGoods);
		}

		int orderCount = rs.getInt("ORDERCOUNT");
		if (!rs.wasNull()) {
			valid = true;
			s.setOrderCount(new Integer(orderCount));
		}

		String needItemsFrom = rs.getString("NEEDITEMSFROM");
		if (!rs.wasNull()) {
			valid = true;
			s.setNeedItemsFrom(decodeStrings(needItemsFrom));
		}

		String excludeSkuPrefix = rs.getString("EXCLUDESKUPREFIX");
		if (!rs.wasNull()) {
			valid = true;
			s.setExcludeSkuPrefix(excludeSkuPrefix);
		}

		String needBrands = rs.getString("NEEDBRANDS");
		if (!rs.wasNull()) {
			valid = true;
			s.setNeedBrands(decodeStrings(needBrands));
		}

		String excludeBrands = rs.getString("EXCLUDEBRANDS");
		if (!rs.wasNull()) {
			valid = true;
			s.setExcludeBrands(decodeStrings(excludeBrands));
		}

		if (valid) {
			promo.addStrategy(s);
		}
	}

	private static String[] decodeStrings(String string) {
		StringTokenizer st = new StringTokenizer(string, ",");
		String[] strings = new String[st.countTokens()];
		for (int i = 0; i < strings.length; i++) {
			strings[i] = st.nextToken().trim();
		}
		return strings;
	}

	private static void loadApplicator(ResultSet rs, Promotion promo) throws SQLException {

		//
		// header discount applicator
		//
		boolean wasNull = false;

		double minSubtotal = rs.getDouble("min_subtotal");
		wasNull |= rs.wasNull();

		double maxAmount = rs.getDouble("max_amount");
		wasNull |= rs.wasNull();


/*		
		if("LINE_ITEM".equalsIgnoreCase(rs.getString("CAMPAIGN_CODE")))
		{   double percentOff = rs.getDouble("percent_off");
		    System.out.println("returning the LineItemDiscountApplicator"+percentOff);						
			return new RecommendedDiscountApplicator(minSubtotal,percentOff,maxItemCount,"X".equalsIgnoreCase(rs.getString("apply_header_discount"))?true:false);							   	
		}
*/
		
		// this code is for line item discount
//		if(maxItemCount>0){
//			System.out.println("*************loading the max item count applicator ******************"+maxItemCount);
//			double percentOff = rs.getDouble("percent_off");									
//			Discount dis=new Discount("SORI_TEST",EnumDiscountType.PERCENT_OFF,percentOff);				
//			return new MaxLineItemCountApplicator(maxItemCount,dis);
//			 
//		}
		double maxPercentageDiscount = rs.getDouble("MAX_PERCENTAGE_DISCOUNT");
		double percentOff = rs.getDouble("percent_off");
		
		if("HEADER".equals(rs.getString("CAMPAIGN_CODE"))) {
			if (!wasNull) {
				promo.addApplicator(new HeaderDiscountApplicator(new HeaderDiscountRule(minSubtotal, maxAmount)));
				//return new HeaderDiscountApplicator(new HeaderDiscountRule(minSubtotal, maxAmount));
			} else {
				//
				// percent-off applicator
				//
				wasNull = false;
				wasNull |= rs.wasNull();
				if (!wasNull && "REDEMPTION".equals(rs.getString("CAMPAIGN_CODE"))) {
					
					promo.addApplicator( new PercentOffApplicator(minSubtotal, percentOff, maxPercentageDiscount));
				}
			}
		}
		
		if("LINE_ITEM".equals(rs.getString("CAMPAIGN_CODE"))){
			LineItemDiscountApplicator applicator = new LineItemDiscountApplicator(minSubtotal, percentOff, maxPercentageDiscount);
			boolean recItemsOnly = "X".equalsIgnoreCase(rs.getString("RECOMMENDED_ITEMS_ONLY"));
			if(recItemsOnly){
				applicator.addLineItemStrategy(new RecommendedLineItemStrategy());
			}
			int maxItemCount = rs.getInt("MAX_ITEM_COUNT");
			if(!rs.wasNull() && maxItemCount > 0){
				applicator.addLineItemStrategy(new MaxLineItemCountStrategy(maxItemCount));
			}
			promo.addApplicator( applicator);
		}

		//
		// waive-charge applicator
		//
		wasNull = false;
		String waiveChargeType = rs.getString("waive_charge_type");
		wasNull |= rs.wasNull();
		if (!wasNull) {
			boolean fuelSurcharge = "Y".equals(rs.getString("INCL_FUEL_SURCHARGE"));
			promo.addApplicator( new WaiveChargeApplicator(minSubtotal, EnumChargeType.getEnum(waiveChargeType), fuelSurcharge));
		}

		//	
		// sample item applicator
		//
		wasNull = false;
		String categoryName = rs.getString("category_name");
		wasNull |= rs.wasNull();
		String productName = rs.getString("product_name");
		wasNull |= rs.wasNull();
		if (!wasNull) {
			promo.addApplicator( new SampleLineApplicator(new ProductReferenceImpl(categoryName, productName), minSubtotal));
		}

		
	}

	/** @return Map of promotionPK -> GeographyStrategy */
	private final static String getPromoGeographyData = "select pg.id, pg.promotion_id, pg.start_date from cust.promo_geography pg, " +
									"(SELECT ID FROM CUST.PROMOTION where active='X' and (expiration_date > (sysdate-7) " +
									"or expiration_date is null) and redemption_code is null) p where p.ID = pg.PROMOTION_ID";

	private final static String getModifiedOnlyPromoGeographyData = "select pg.id, pg.promotion_id, pg.start_date from cust.promo_geography pg, " +
	"(SELECT ID FROM CUST.PROMOTION where modify_date > ? ) p where p.ID = pg.PROMOTION_ID";
	
	protected static Map loadGeographyStrategies(Connection conn, Date lastModified) throws SQLException {
		PreparedStatement ps;
		if(lastModified != null){
			ps = conn.prepareStatement(getModifiedOnlyPromoGeographyData);	
			ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		}else {
			ps = conn.prepareStatement(getPromoGeographyData);
		}
		
		ResultSet rs = ps.executeQuery();

		// promotionPK -> GeographyStrategy 
		Map strategies = new HashMap();

		// geographyPK -> PromotionGeography		
		Map geographies = new HashMap();

		while (rs.next()) {

			PrimaryKey promoPK = new PrimaryKey(rs.getString("PROMOTION_ID"));
			PrimaryKey geoPK = new PrimaryKey(rs.getString("ID"));
			PromotionGeography geo = new PromotionGeography(geoPK, rs.getDate("START_DATE"));

			GeographyStrategy strategy = (GeographyStrategy) strategies.get(promoPK);
			if (strategy == null) {
				strategy = new GeographyStrategy();
				strategies.put(promoPK, strategy);
			}
			strategy.addGeography(geo);

			geographies.put(geoPK, geo);
		}

		loadGeographyData(conn, geographies, null);

		rs.close();
		ps.close();

		return strategies;
	}
	
	protected static GeographyStrategy loadGeographyStrategy(Connection conn, String promoPK) throws SQLException {
		GeographyStrategy strategy = null;
		// geographyPK -> PromotionGeography		
		Map geographies = new HashMap();
		
		PreparedStatement ps = conn.prepareStatement("select id, start_date from cust.promo_geography where promotion_id = ?");
		ps.setString(1, promoPK);
		ResultSet rs = ps.executeQuery();
		
		while (rs.next()) {
			//PrimaryKey promoPK = new PrimaryKey(rs.getString("PROMOTION_ID"));
			PrimaryKey geoPK = new PrimaryKey(rs.getString("ID"));
			PromotionGeography geo = new PromotionGeography(geoPK, rs.getDate("START_DATE"));

			if (strategy == null) {
				strategy = new GeographyStrategy();
			}
			strategy.addGeography(geo);
			geographies.put(geoPK, geo);
		}

		loadGeographyData(conn, geographies, promoPK);

		rs.close();
		ps.close();

		return strategy;
	}

	private final static String GEO_CODE_ALL = "ALL";

	private static class GeoTuple {
		public final PrimaryKey geoPK;
		public final boolean add;
		public final boolean zip;
		public final String code;

		public GeoTuple(ResultSet rs) throws SQLException {
			geoPK = new PrimaryKey(rs.getString("geography_id"));
			add = "A".equals(rs.getString("sign"));
			zip = "Z".equals(rs.getString("type"));
			code = rs.getString("code");
		}
	}

	private static void loadGeographyData(Connection conn, Map geographies, String promoPK) throws SQLException {
		PreparedStatement ps = null;
		if(promoPK == null){
			ps = conn.prepareStatement("select geography_id, type, code, sign from cust.promo_geography_data");	
		} else{
			ps = conn.prepareStatement("select geography_id, type, code, sign from cust.promo_geography_data " +
										"where GEOGRAPHY_ID IN (select ID from cust.promo_geography where " +
										"promotion_id = ?)");
			ps.setString(1, promoPK);
		}
		
		ResultSet rs = ps.executeQuery();

		List tuples = new ArrayList();

		while (rs.next()) {
			tuples.add(new GeoTuple(rs));
		}

		// deal with GEO_CODE_ALL entries
		for (ListIterator i = tuples.listIterator(); i.hasNext();) {
			GeoTuple t = (GeoTuple) i.next();
			if (GEO_CODE_ALL.equals(t.code)) {
				PromotionGeography geo = (PromotionGeography) geographies.get(t.geoPK);
				if(geo != null){
					/*
					 * geo object can be null if there is no matching geoPk found in Map geographies. This can happen
					 * due to the fact from now on we don't load Promo_Geography records for expired promotions.
					 */
					if (t.zip) {
						geo.setAllowAllZipCodes(t.add);
					} else {
						geo.setAllowAllDepotCodes(t.add);
					}
				}
				i.remove();
			}
		}

		// deal with exclusions
		for (Iterator i = tuples.iterator(); i.hasNext();) {
			GeoTuple t = (GeoTuple) i.next();
			PromotionGeography geo = (PromotionGeography) geographies.get(t.geoPK);
			if(geo != null){
				/*
				 * geo object can be null if there is no matching geoPk found in Map geographies. This can happen
				 * due to the fact from now on we don't load Promo_Geography records for expired promotions.
				 */
				if (t.zip && t.add != geo.isAllowAllZipCodes()) {
					geo.excludeZipCode(t.code);
				} else if (t.add != geo.isAllowAllDepotCodes()) {
					geo.excludeDepotCode(t.code);
				}
			}
		}

		rs.close();
		ps.close();

	}
	
	/** @return Map of promotionPK -> GeographyStrategy */
	private final static String GET_DCPD_DISCOUNT = "select pg.promotion_id, pg.content_type, pg.content_id, p.percent_off, p.min_subtotal from cust.PROMO_DCPD_DATA pg, " +
									"(SELECT ID, PERCENT_OFF, MIN_SUBTOTAL FROM CUST.PROMOTION where active='X' and (expiration_date > (sysdate-7) " +
									"or expiration_date is null) and redemption_code is null) p where p.ID = pg.PROMOTION_ID";

	private final static String GET_MODIFIED_ONLY_DCPD_DISCOUNT = "select pg.id, pg.promotion_id, pg.content_type, pg.content_id, p.percent_off, p.min_subtotal from cust.PROMO_DCPD_DATA pg, " +
	"(SELECT ID, PERCENT_OFF, MIN_SUBTOTAL  FROM CUST.PROMOTION where modify_date > ? ) p where p.ID = pg.PROMOTION_ID";
	
	protected static Map loadDCPDiscountApplicators(Connection conn, Date lastModified) throws SQLException {
		PreparedStatement ps;
		if(lastModified != null){
			ps = conn.prepareStatement(GET_MODIFIED_ONLY_DCPD_DISCOUNT);	
			ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		}else {
			ps = conn.prepareStatement(GET_DCPD_DISCOUNT);
		}
		//Map PromoPK --> CategoryDiscountApplicator
		Map applicators = new HashMap();
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			String promoId = rs.getString("PROMOTION_ID");
			PrimaryKey promoPK = new PrimaryKey(promoId);
			DCPDiscountApplicator app = (DCPDiscountApplicator)applicators.get(promoPK);
			if( app == null){
				double percentOff = rs.getDouble("PERCENT_OFF");
				double minSubTotal = rs.getDouble("MIN_SUBTOTAL");
				app = new DCPDiscountApplicator(new DCPDiscountRule(minSubTotal, percentOff));
			}
			String contentType =  rs.getString("CONTENT_TYPE");
			String contentId = rs.getString("CONTENT_ID");
			app.addContent(contentType, contentId);
			applicators.put(promoPK, app);				
		}


		rs.close();
		ps.close();

		return applicators;
	}
	
	protected static PromotionApplicatorI loadDCPDiscountApplicator(Connection conn, String promoId) throws SQLException {
		DCPDiscountApplicator app = null;
		PreparedStatement ps = conn.prepareStatement("select pg.content_type, pg.content_id, p.percent_off, p.min_subtotal from cust.PROMO_DCPD_DATA pg, " +
														"cust.promotion p where p.ID = pg.PROMOTION_ID and pg.promotion_id = ?");
		ps.setString(1, promoId);
		ResultSet rs = ps.executeQuery();
		
		while(rs.next()){
			double percentOff = rs.getDouble("PERCENT_OFF");
			double minSubTotal = rs.getDouble("MIN_SUBTOTAL");
			if(app == null){
				app = new DCPDiscountApplicator(new DCPDiscountRule(minSubTotal, percentOff));	
			}
			String contentType =  rs.getString("CONTENT_TYPE");
			String contentId = rs.getString("CONTENT_ID");
			app.addContent(contentType, contentId);
		}
		rs.close();
		ps.close();

		return app;
	}
	
	/** @return Map of promotionPK -> GeographyStrategy */
	private final static String GET_PROMO_PROFILE_ATTR = "select pa.promotion_id, pa.promo_attr_name, pa.attr_value, p.profile_operator from cust.promo_attr pa, " +
									"(SELECT ID, PROFILE_OPERATOR FROM CUST.PROMOTION where active='X' and (expiration_date > (sysdate-7) " +
									"or expiration_date is null) and redemption_code is null) p where p.ID = pa.PROMOTION_ID";

	private final static String GET_MODIFIED_ONLY_PROMO_PROFILE = "select pa.promotion_id, pa.promo_attr_name, pa.attr_value, p.profile_operator from cust.promo_attr pa, " +
	"(SELECT ID, PROFILE_OPERATOR FROM CUST.PROMOTION where modify_date > ? ) p where p.ID = pa.PROMOTION_ID";
	
	protected static Map loadAllProfiles(Connection conn, Date lastModified) throws SQLException {
		PreparedStatement ps;
		if(lastModified != null){
			ps = conn.prepareStatement(GET_MODIFIED_ONLY_PROMO_PROFILE);	
			ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		}else {
			ps = conn.prepareStatement(GET_PROMO_PROFILE_ATTR);
		}
		//Map PromoPK --> CategoryDiscountApplicator
		Map strategies = new HashMap();
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			String promoId = rs.getString("PROMOTION_ID");				
			PrimaryKey promoPK = new PrimaryKey(promoId);
			String attrName =  rs.getString("PROMO_ATTR_NAME");
			String attrValue = rs.getString("ATTR_VALUE");
			String operator = rs.getString("PROFILE_OPERATOR");
			if(operator == null || operator.length() == 0){
				//this promotion has a single profile attribute. Create a ProfileAttributeStrategy
				ProfileAttributeStrategy strategy = new ProfileAttributeStrategy();
				strategy.setAttributeName(attrName);
				strategy.setDesiredValue(attrValue);
				strategies.put(promoPK, strategy);
			}else {
				//It has more than one profile attribute. Go For compositestrategy in this case.
				CompositeStrategy compStrategy = (CompositeStrategy)strategies.get(promoPK);	
				if(compStrategy == null){
					compStrategy = new CompositeStrategy(operator.equals("AND") ? CompositeStrategy.AND : CompositeStrategy.OR);
				}
				ProfileAttributeStrategy profileStrategy = new ProfileAttributeStrategy();
				profileStrategy.setAttributeName(attrName);
				profileStrategy.setDesiredValue(attrValue);
				compStrategy.addStrategy(profileStrategy);
				strategies.put(promoPK, compStrategy);
			}
		}

		rs.close();
		ps.close();

		return strategies;
	}
	
	protected static PromotionStrategyI loadProfiles(Connection conn, String promoId) throws SQLException {
		PromotionStrategyI strategy = null;
		PreparedStatement ps = conn.prepareStatement("select pa.promo_attr_name, pa.attr_value, p.profile_operator from cust.promo_attr pa, cust.promotion p " +
														"where p.ID = pa.PROMOTION_ID and pa.promotion_id = ?");
		ps.setString(1, promoId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			PrimaryKey promoPK = new PrimaryKey(promoId);
			String attrName =  rs.getString("PROMO_ATTR_NAME");
			String attrValue = rs.getString("ATTR_VALUE");
			String operator = rs.getString("PROFILE_OPERATOR");
			if(operator == null || operator.length() == 0){
				//this promotion has a single profile attribute. Create a ProfileAttributeStrategy
				strategy = new ProfileAttributeStrategy();
				((ProfileAttributeStrategy)strategy).setAttributeName(attrName);
				((ProfileAttributeStrategy)strategy).setDesiredValue(attrValue);
			}else {
				//It has more than one profile attribute. Go For compositestrategy in this case.
				if(strategy == null){
					strategy= new CompositeStrategy(operator.equals("AND") ? CompositeStrategy.AND : CompositeStrategy.OR);
				}
				ProfileAttributeStrategy profileStrategy = new ProfileAttributeStrategy();
				profileStrategy.setAttributeName(attrName);
				profileStrategy.setDesiredValue(attrValue);
				((CompositeStrategy)strategy).addStrategy(profileStrategy);
			}
		}
		rs.close();
		ps.close();

		return strategy;
	}
	
	/** @return Map of String (promo Id) -> Set of String (sale Id) */
	
	private final static String getUniqueUseParticipationQuery = "select PROMOTION_ID, SALE_ID from cust.promotion_participation pp, " +
								"(SELECT ID,UNIQUE_USE FROM CUST.PROMOTION where active='X' and (expiration_date > (sysdate-7) " +
								"or expiration_date is null) and redemption_code is null) p where p.id=pp.promotion_id and p.unique_use='X'";
	
	private final static String getUniqueUseForModifiedOnlyPromos = "select PROMOTION_ID, SALE_ID from cust.promotion_participation pp, " +
	"(SELECT ID,UNIQUE_USE FROM CUST.PROMOTION where modify_date > ?) p where p.id=pp.promotion_id and p.unique_use='X'";
	
	protected static Map loadUniqueUseParticipation(Connection conn, Date lastModified) throws SQLException {
		PreparedStatement ps;
		if(lastModified != null){
			ps = conn.prepareStatement(getUniqueUseForModifiedOnlyPromos);	
			ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		} else {
			ps = conn.prepareStatement(getUniqueUseParticipationQuery);
		}
		
		ResultSet rs = ps.executeQuery();
		Map promoSales = new HashMap();
		while (rs.next()) {
			String promoId = rs.getString("PROMOTION_ID");
			Set s = (Set) promoSales.get(promoId);
			if (s == null) {
				s = new HashSet();
				promoSales.put(promoId, s);
			}
			s.add(rs.getString("SALE_ID"));
		}
		rs.close();
		ps.close();
		return promoSales;
	}

	/** @return Set of String (sale Id) */
	protected static Set loadUniqueUseParticipationForPromo(Connection conn, String promoPK) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("select SALE_ID from cust.promotion_participation pp, cust.promotion p where p.id=pp.promotion_id and p.unique_use='X' and pp.promotion_id = ?");
		ps.setString(1, promoPK);
		ResultSet rs = ps.executeQuery();
		Set promoSales = new HashSet();
		while (rs.next()) {
			promoSales.add(rs.getString("SALE_ID"));
		}
		rs.close();
		ps.close();
		return promoSales;
	}

	/**
	 * Query to retreive all the Audience-Based promotions for a given customer.
	 * TODO Replace the Promotion Code with the Promotion ID when new AI database model
	 * is implemented.
	 */
	private final static String getABPromotionsForCustomer = "select p.code, pc.usage_cnt, pc.expiration_date "
		+ "from cust.promotion p, cust.promo_customer pc "
		+ "where p.id=pc.promotion_id and (p.expiration_date > (sysdate-7) or p.expiration_date is null) and pc.customer_id = ?";
	
	/** @return Map of promotionPK -> AssignedCustomerStrategy */
	public static Map loadAssignedCustomerParams(Connection conn, String erpCustomerId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(getABPromotionsForCustomer);
		ps.setString(1, erpCustomerId);
		ResultSet rs = ps.executeQuery();
		// promotionPK -> FDPromoAudience 
		Map audiencePromoDtls = new HashMap();

		while (rs.next()) {
			String promoId = rs.getString("CODE");
			java.util.Date expirationDate = rs.getDate("EXPIRATION_DATE");
			int usageCnt = rs.getInt("USAGE_CNT");
			AssignedCustomerParam assignedCustParam = new AssignedCustomerParam(new Integer(usageCnt), expirationDate);
			audiencePromoDtls.put(promoId, assignedCustParam);
		}

		rs.close();
		ps.close();
		return audiencePromoDtls;
	}
	
	
	/**
	 * Query to get audience-based info for all active automatic promotions.
	 */
	private final static String getAudienceCountForPromotions = "select p.id, count(customer_id) as cnt from cust.promo_customer pc, " +
																	"(SELECT ID FROM CUST.PROMOTION where active='X' and (expiration_date > (sysdate-7) " +
																	"or expiration_date is null) and redemption_code is null) p where p.ID = pc.PROMOTION_ID " +
																	"group by p.id";
	
	private final static String getAudienceCountForModifiedOnlyPromos = "select p.id, count(customer_id) as cnt from cust.promo_customer pc, " +
																"(SELECT ID FROM CUST.PROMOTION where modify_date > ?) p " +
																"where p.ID = pc.PROMOTION_ID group by p.id";
	
	/** @return Map of promotionPK -> AssignedCustomerStrategy */
	protected static List<String> getAudienceBasedPromotionCodes(Connection conn, Date lastModified) throws SQLException {
		List<String> audiencePromoCodes = new ArrayList<String>();
		PreparedStatement ps;
		if(lastModified != null){
			ps = conn.prepareStatement(getAudienceCountForModifiedOnlyPromos);	
			ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		} else {
			ps = conn.prepareStatement(getAudienceCountForPromotions);
		}
		
		ResultSet rs = ps.executeQuery();
		// promotionPK -> AudienceCustomerStrategy 
		while (rs.next()) {
			String promoId = rs.getString("ID");
			int audienceCount = rs.getInt("CNT");
			if(audienceCount > 0)
				audiencePromoCodes.add(promoId);
		}
		rs.close();
		ps.close();
		return audiencePromoCodes;
	}
	
	/**
	 * Query Check if a Promotion is audience-based or not.
	 */
	private final static String isAudienceBasedPromotionQuery = "select count(customer_id) as cnt from cust.promo_customer where promotion_id = ?";
	
	/** @return Map of promotionPK -> AssignedCustomerStrategy */
	protected static boolean isAudienceBasedPromotion(Connection conn, String promoId) throws SQLException {
		boolean audienceBased = false;
		PreparedStatement ps = conn.prepareStatement(isAudienceBasedPromotionQuery);
		ps.setString(1, promoId);
		ResultSet rs = ps.executeQuery();
		// promotionPK -> AudienceCustomerStrategy 
		if (rs.next()) {
			int rowCount = rs.getInt("CNT");
			if(rowCount > 0)
				audienceBased = true; 
		}

		rs.close();
		ps.close();
		return audienceBased;
	}

//	private final static String assignedCustomerStratgeyQuery = "select p.id, pc.customer_id, pc.usage_cnt, pc.expiration_date, p.rolling_expiration_days, p.is_max_usage_per_cust "
//		+ "from cust.promotion p, cust.promo_customer pc "
//		+ "where p.id=pc.promotion_id and p.active='X' and (p.expiration_date > (sysdate-7) or p.expiration_date is null)";
//	
//	/** @return Map of promotionPK -> AssignedCustomerStrategy */
//	protected static Map loadAssignedCustomerStrategies(Connection conn) throws SQLException {
//		PreparedStatement ps = conn.prepareStatement(assignedCustomerStratgeyQuery);
//		ResultSet rs = ps.executeQuery();
//
//		// promotionPK -> AssignedCustomerStrategy 
//		Map strategies = new HashMap();
//
//		while (rs.next()) {
//
//			PrimaryKey promoPK = new PrimaryKey(rs.getString("ID"));
//			String custId = rs.getString("CUSTOMER_ID");
//			java.util.Date expirationDate = rs.getDate("EXPIRATION_DATE");
//			boolean isMaxUsagePerCustomer = "X".equalsIgnoreCase(rs.getString("IS_MAX_USAGE_PER_CUST"));
//			int rollingExpirationDays = rs.getInt("ROLLING_EXPIRATION_DAYS");
//			Integer rollingExpirationDaysInt = null;
//			if (!rs.wasNull()) {
//				rollingExpirationDaysInt = new Integer(rollingExpirationDays);
//			}
//			int usageCnt = rs.getInt("USAGE_CNT");
//			Integer usageCntInt = null;
//			if (!rs.wasNull()) {
//				usageCntInt = new Integer(usageCnt);
//			}
//			if (strategies.get(promoPK) == null) {
//				AssignedCustomerStrategy strategy = new AssignedCustomerStrategy();
//				strategy.setRollingExpirationDays(rollingExpirationDaysInt);
//				strategy.setIsMaxUsagePerCustomer(isMaxUsagePerCustomer);
//				AssignedCustomerParam param = new AssignedCustomerParam(usageCntInt, expirationDate); 
//				strategy.addCustomer(custId, param);
//				strategies.put(promoPK, strategy);
//			} else {
//				AssignedCustomerStrategy strategy = (AssignedCustomerStrategy) strategies.get(promoPK);
//				AssignedCustomerParam param = new AssignedCustomerParam(usageCntInt, expirationDate); 
//				strategy.addCustomer(custId, param);
//			}
//
//		}
//
//		rs.close();
//		ps.close();
//
//		return strategies;
//	}
	private final static String GET_ALL_ACTIVE_PROMO_VARIANTS = "select vp.VARIANT_ID, vp.PROMO_CODE, vp.PROMO_PRIORITY, v.FEATURE, vp.VARIANT_PRIORITY from cust.PROMO_VARIANTS vp, " +
			"cust.SS_VARIANTS v, cust.PROMOTION p where p.CODE = vp.PROMO_CODE and v.ID = vp.VARIANT_ID and p.active='X' and (p.expiration_date > (sysdate-7) " +
			"or p.expiration_date is null) and p.RECOMMENDED_ITEMS_ONLY='X' and v.archived = 'N'";
	
	public static List<PromoVariantModel> loadAllActivePromoVariants(Connection conn, List<EnumSiteFeature> smartSavingFeatures) throws SQLException {
		StringBuffer preparedStmtQry = new StringBuffer(GET_ALL_ACTIVE_PROMO_VARIANTS); 
		StringBuffer buffer = new StringBuffer();
		if(smartSavingFeatures != null && smartSavingFeatures.size() > 0) {
			buffer.append(" AND ").append("V.FEATURE IN ").append("(");
			for(Iterator<EnumSiteFeature> it = smartSavingFeatures.iterator(); it.hasNext();){
				EnumSiteFeature siteFeature = it.next();
				buffer.append("\'").append(siteFeature.getName()).append("\'");
				if (it.hasNext()){
					buffer.append(",");
				}else{
					buffer.append(")");
				}
			}
		}
		
		if(buffer.length() > 0){
			preparedStmtQry.append(buffer);
		}
		preparedStmtQry.append(" order by vp.VARIANT_ID desc");
		PreparedStatement ps = conn.prepareStatement(preparedStmtQry.toString());
		ResultSet rs = ps.executeQuery();
		List<PromoVariantModel> promoVariants =  constructPromoVariants(rs);
		rs.close();
		ps.close();
		return promoVariants;
	}
	
	private static List<PromoVariantModel> constructPromoVariants(ResultSet rs) throws SQLException {
		List<PromoVariantModel> promoVariants = new ArrayList<PromoVariantModel>();
		while(rs.next()){
			String variantId = rs.getString("VARIANT_ID");
			String promoCode = rs.getString("PROMO_CODE");
			int priority = rs.getInt("PROMO_PRIORITY");
			String featureId = rs.getString("FEATURE");
			int featurePriority = rs.getInt("VARIANT_PRIORITY");
			PromoVariantModel promoVariant = new PromoVariantModelImpl(variantId, promoCode, priority, EnumSiteFeature.getEnum(featureId),featurePriority);
			promoVariants.add(promoVariant);
		}
		return promoVariants;
	}
}
