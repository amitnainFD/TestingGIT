package com.freshdirect.fdstore.promotion.ejb;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.freshdirect.customer.EnumChargeType;
import com.freshdirect.delivery.EnumComparisionType;
import com.freshdirect.delivery.EnumDeliveryOption;
import com.freshdirect.delivery.EnumPromoFDXTierType;
import com.freshdirect.deliverypass.EnumDlvPassStatus;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.ProductReferenceImpl;
import com.freshdirect.fdstore.promotion.ActiveInactiveStrategy;
import com.freshdirect.fdstore.promotion.AssignedCustomerParam;
import com.freshdirect.fdstore.promotion.AudienceStrategy;
import com.freshdirect.fdstore.promotion.CartStrategy;
import com.freshdirect.fdstore.promotion.CompositeStrategy;
import com.freshdirect.fdstore.promotion.CustomerStrategy;
import com.freshdirect.fdstore.promotion.DCPDLineItemStrategy;
import com.freshdirect.fdstore.promotion.DateRangeStrategy;
import com.freshdirect.fdstore.promotion.DlvZoneStrategy;
import com.freshdirect.fdstore.promotion.EnumDCPDContentType;
import com.freshdirect.fdstore.promotion.EnumOfferType;
import com.freshdirect.fdstore.promotion.EnumOrderType;
import com.freshdirect.fdstore.promotion.EnumPromotionType;
import com.freshdirect.fdstore.promotion.ExtendDeliveryPassApplicator;
import com.freshdirect.fdstore.promotion.FraudStrategy;
import com.freshdirect.fdstore.promotion.GeographyStrategy;
import com.freshdirect.fdstore.promotion.HeaderDiscountApplicator;
import com.freshdirect.fdstore.promotion.HeaderDiscountRule;
import com.freshdirect.fdstore.promotion.LimitedUseStrategy;
import com.freshdirect.fdstore.promotion.LineItemDiscountApplicator;
import com.freshdirect.fdstore.promotion.MaxLineItemCountStrategy;
import com.freshdirect.fdstore.promotion.MaxRedemptionStrategy;
import com.freshdirect.fdstore.promotion.PercentOffApplicator;
import com.freshdirect.fdstore.promotion.PerishableLineItemStrategy;
import com.freshdirect.fdstore.promotion.ProductSampleApplicator;
import com.freshdirect.fdstore.promotion.MinimumSubtotalStrategy;
import com.freshdirect.fdstore.promotion.ProfileAttributeStrategy;
import com.freshdirect.fdstore.promotion.PromoVariantModel;
import com.freshdirect.fdstore.promotion.PromoVariantModelImpl;
import com.freshdirect.fdstore.promotion.Promotion;
import com.freshdirect.fdstore.promotion.PromotionApplicatorI;
import com.freshdirect.fdstore.promotion.PromotionDlvDate;
import com.freshdirect.fdstore.promotion.PromotionDlvDay;
import com.freshdirect.fdstore.promotion.PromotionDlvTimeSlot;
import com.freshdirect.fdstore.promotion.PromotionGeography;
import com.freshdirect.fdstore.promotion.PromotionI;
import com.freshdirect.fdstore.promotion.PromotionStrategyI;
import com.freshdirect.fdstore.promotion.RecommendationStrategy;
import com.freshdirect.fdstore.promotion.RecommendedLineItemStrategy;
import com.freshdirect.fdstore.promotion.RedemptionCodeStrategy;
import com.freshdirect.fdstore.promotion.ReferAFriendStrategy;
import com.freshdirect.fdstore.promotion.RuleBasedPromotionStrategy;
import com.freshdirect.fdstore.promotion.SampleLineApplicator;
import com.freshdirect.fdstore.promotion.SampleStrategy;
import com.freshdirect.fdstore.promotion.SkuLimitStrategy;
import com.freshdirect.fdstore.promotion.StateCountyStrategy;
import com.freshdirect.fdstore.promotion.WaiveChargeApplicator;
import com.freshdirect.fdstore.promotion.management.FDPromoDollarDiscount;
import com.freshdirect.fdstore.promotion.management.ejb.FDPromotionManagerNewDAO;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.util.StringUtil;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDPromotionNewDAO {
	private static final int DEFAULT_ASSIGNED_CUSTOMER_PARAMS_QUERY_ID=1;
	private final static Logger LOGGER = LoggerFactory.getInstance(FDPromotionNewDAO.class);

	/**
	 * Loads all active automatic promotions(redemption_code is null) which were not expired in last 7 days.
	 * 
	 * @param promoStrategyMap Map of String(promotionCode) -> PromotionStrategy
	 * @return List of Promotion
	 */
	private final static String getAllAutomaticPromotions = "SELECT * FROM CUST.PROMOTION_NEW p where p.status STATUSES and " +
	 							"(p.expiration_date > (sysdate-7) or p.expiration_date is null) and p.redemption_code is null " +
	 							//"order by p.modify_date desc";
	 							"and (p.REFERRAL_PROMO = 'N' or p.REFERRAL_PROMO is null) order by p.modify_date desc";
	public static List<PromotionI> loadAllAutomaticPromotions(Connection conn) throws SQLException {
		final String query = getAllAutomaticPromotions.replace("STATUSES", getStatusReplacementString());
		LOGGER.debug("Query is "+query);

		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		List<PromotionI> promotions =  loadPromotions(conn, rs, null);
		rs.close();
		ps.close();
		return promotions;
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
	
	private final static String getModifiedOnlyPromotions = "SELECT * FROM CUST.PROMOTION_NEW where modify_date > ? " +
						"and (REFERRAL_PROMO = 'N' or REFERRAL_PROMO is null) " +
						"order by modify_date asc";
	
	public static List<PromotionI> loadModifiedOnlyPromotions(Connection conn, Date lastModified) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(getModifiedOnlyPromotions);
		ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		ResultSet rs = ps.executeQuery();
		List<PromotionI> promotions = loadPromotions(conn, rs, lastModified);
		rs.close();
		ps.close();
		return promotions;
	}
	
	private static List<PromotionI> loadPromotions(Connection conn, ResultSet rs, Date paramLastModified) throws SQLException {
		Map<PrimaryKey,GeographyStrategy> geoStrategies = loadGeographyStrategies(conn, paramLastModified);
		//List audiencePromoCodes = getAudienceBasedPromotionCodes(conn, paramLastModified);
		Map<PrimaryKey, CustomerStrategy> customerStrategies = loadCustomerStrategies(conn, paramLastModified);
		Map<PrimaryKey,PromotionStrategyI> profileStrategies = loadAllProfiles(conn, paramLastModified);
		Map<PrimaryKey, DCPDLineItemStrategy> dcpdData = loadDCPDData(conn, paramLastModified);
		Map<PrimaryKey, CartStrategy> cartStrategyData = loadCartStrategies(conn, paramLastModified);
		Map<String,DlvZoneStrategy> dlvZoneStrategies = loadDlvZoneStrategies(conn,paramLastModified);
		Map<String,StateCountyStrategy> stateCountyStrategies = loadStateCountyStrategies(conn,paramLastModified);
		List<PromotionI> promos = new ArrayList<PromotionI>();
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
			EnumOfferType offerType = EnumOfferType.getEnum(rs.getString("OFFER_TYPE"));
			promo.setOfferType(offerType);
			if("Y".equalsIgnoreCase(rs.getString("NEEDCUSTOMERLIST")) || "Y".equalsIgnoreCase(rs.getString("rolling_from_first_order"))){
				//This is customer restricted Promotion. Create Audience Strategy.
				int rollingExpirationDays = rs.getInt("ROLLING_EXPIRATION_DAYS");
				boolean isRollingExpFrom1stOrder = "Y".equalsIgnoreCase(rs.getString("rolling_from_first_order"));
				//APPDEV-659 - Made isMaxUsagePerCustomer obsolete.
				AudienceStrategy aStrategy = new AudienceStrategy(false, rollingExpirationDays, isRollingExpFrom1stOrder);
				promo.addStrategy(aStrategy);
			}
			String excludeSkusFromSubTotal = rs.getString("EXCLUDE_SKU_SUBTOTAL");
			if(excludeSkusFromSubTotal != null && excludeSkusFromSubTotal.length() > 0){
				promo.setExcludeSkusFromSubTotal(excludeSkusFromSubTotal);
			}
			String status = rs.getString("STATUS");
			boolean onHold = "Y".equalsIgnoreCase(rs.getString("ON_HOLD"));
			promo.addStrategy(new ActiveInactiveStrategy(FDStoreProperties.getPromoValidRTStatuses().contains(status) && !onHold));
			
			promo.addStrategy(new LimitedUseStrategy(rs.getInt("MAX_USAGE")));
			int redeemCnt = rs.getInt("REDEEM_CNT");
			if(!rs.wasNull() && redeemCnt > 0)
				promo.addStrategy(new MaxRedemptionStrategy(redeemCnt));
			
			if("X".equalsIgnoreCase(rs.getString("RULE_BASED"))) {
				promo.addStrategy(new RuleBasedPromotionStrategy());
			}

			promo.addStrategy(new DateRangeStrategy(rs.getTimestamp("START_DATE"), rs.getTimestamp("EXPIRATION_DATE")));

			String redemptionCode = rs.getString("REDEMPTION_CODE");
			if (!rs.wasNull()) {
				promo.addStrategy(new RedemptionCodeStrategy(redemptionCode));
			}
			
			if(!"X".equals(rs.getString("DONOT_APPLY_FRAUD"))){
				promo.addStrategy(new FraudStrategy());
			}
			//TODO This needs to replaced by Customer strategy. 	
			//decorateOrderTypestrategy(rs, promo);

			PromotionStrategyI geoStrategy = geoStrategies.get(pk);
			if (geoStrategy != null) {
				promo.addStrategy(geoStrategy);
			}

			//TODO This needs to replaced by Cart strategy 	
			decorateSampleStrategy(rs, promo);
			
//			loadProductSampleStrategy(rs, promo);
			
			//Load the profile strategy
			PromotionStrategyI profStrategy = profileStrategies.get(pk);
			if (profStrategy != null) {
				promo.addStrategy(profStrategy);
			}
			
			if(promoType.getName().equals(EnumPromotionType.LINE_ITEM.getName())){
				//boolean recItemsOnly = "X".equalsIgnoreCase(rs.getString("RECOMMENDED_ITEMS_ONLY"));
				boolean favoritesOnly = "Y".equalsIgnoreCase(rs.getString("FAVORITES_ONLY"));
				if(favoritesOnly) {
					promo.addStrategy(new RecommendationStrategy());
				}
				//promo.addLineItemStrategy(new RecommendedItemStrategy());
			}
			if("Y".equalsIgnoreCase(rs.getString("COMBINE_OFFER"))){				
				promo.setCombineOffer(true);		
			}
				
			//Add Customer Strategy
			PromotionStrategyI custStrategy = customerStrategies.get(pk);
			if (custStrategy != null) {
				promo.addStrategy(custStrategy);
			}

			
			PromotionStrategyI cartStrategyI = cartStrategyData.get(pk);
			if(null != cartStrategyI){
				CartStrategy cartStrategy = (CartStrategy)cartStrategyI;
				cartStrategy.setNeedDryGoods("X".equalsIgnoreCase(rs.getString("NEEDDRYGOODS"))?true:false);
				cartStrategy.setMinSkuQuantity(rs.getInt("HASSKUQUANTITY"));
				cartStrategy.setTotalDcpdSubtotal(rs.getDouble("DCPD_MIN_SUBTOTAL"));
				promo.addStrategy(cartStrategy);				
			}
			
			PromotionStrategyI stateCountyStrategyI = stateCountyStrategies.get(pk.getId());
			//System.out.println("\n\n##############Trying to see if promotion has state and county restriction:" + pk.getId() + "\nstateCountyStrategies:" + stateCountyStrategies);
			if(null != stateCountyStrategyI){
				//System.out.println("Adding the StateCounty Strategy");
				StateCountyStrategy scStrategy = (StateCountyStrategy)stateCountyStrategyI;
				promo.addStrategy(scStrategy);				
			}
			
			
			
			//PromotionApplicatorI applicator = loadApplicator(rs, conn, promo);
			PromotionStrategyI dlvZoneStrategyI = dlvZoneStrategies.get(pk.getId());
			DCPDLineItemStrategy dcpdStrategy = dcpdData.get(pk);
			loadApplicator(rs, conn, promo, dlvZoneStrategyI, dcpdStrategy,(CartStrategy)cartStrategyI);

			promos.add(promo);
		}
		return promos;
	}
	
	private static void loadMinSubtotalStrategy(ResultSet rs, Promotion promo) throws SQLException {
		double minSubtotal = rs.getDouble("min_subtotal");
		MinimumSubtotalStrategy minimumSubtotalStrategy = new MinimumSubtotalStrategy(minSubtotal);	
		promo.addStrategy(minimumSubtotalStrategy);
	}

	private final static String getAllActiveAutomaticPromotionCodes = "SELECT CODE, MODIFY_DATE FROM CUST.PROMOTION_NEW p where p.status STATUSES and " +
		"(p.expiration_date > (sysdate-7) or p.expiration_date is null) and p.redemption_code is null " +
		"and (p.REFERRAL_PROMO = 'N' or p.REFERRAL_PROMO is null)";
	
	/**
	 * This method returns all active automatic promotion codes along
	 * with their last modified timestamp.
	 * TODO Later the Promotion codes has to be replaced with Promotion IDs when
	 * new data model changes for AI is implemented.
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map<String,Timestamp> getAllAutomaticPromotionCodes(Connection conn) throws SQLException {
		Map<String,Timestamp> promoCodes = new HashMap<String,Timestamp>();
		//TODO later the where clause need to be changed to point to ID column instead of Code. 
		String query = getAllActiveAutomaticPromotionCodes.replace("STATUSES", getStatusReplacementString());
		PreparedStatement ps = conn.prepareStatement(query);
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
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM CUST.PROMOTION_NEW where code = ?");
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
		PreparedStatement ps = conn.prepareStatement("SELECT code FROM CUST.PROMOTION_NEW where UPPER(redemption_code) = UPPER(?) and status "+getStatusReplacementString()+" order by expiration_date desc");
		ps.setString(1, redemptionCode);
		ResultSet rs = ps.executeQuery();

		if (rs.next()) {
			promoId = rs.getString("CODE");
		}

		rs.close();
		ps.close();

		return promoId;
	}
	
	public static String getRedemptionCode(Connection conn, String tsaPromoCode) throws SQLException{
		String redemptionCode = null;
		PreparedStatement ps = conn.prepareStatement("SELECT REDEMPTION_CODE FROM CUST.PROMOTION_NEW where UPPER(tsa_promo_code) = UPPER(?) and status "+getStatusReplacementString()+" order by expiration_date desc");
		ps.setString(1, tsaPromoCode);
		ResultSet rs = ps.executeQuery();

		if (rs.next()) {
			redemptionCode = rs.getString("REDEMPTION_CODE");
		}

		rs.close();
		ps.close();

		return redemptionCode;
	}
	
	public static String promoRedemptionsWithNoDlvDateSQL =
			"SELECT COUNT(sale_id) redemptions FROM CUST.PROMOTION_PARTICIPATION WHERE PROMOTION_ID = (SELECT ID FROM CUST.PROMOTION_NEW WHERE CODE = ?)";

	public static String promoRedemptionsWithDlvDateSQL =
			"SELECT COUNT(sale_id) redemptions FROM CUST.PROMOTION_PARTICIPATION WHERE PROMOTION_ID = (SELECT ID FROM CUST.PROMOTION_NEW WHERE CODE = ?) and REQUESTED_DATE = ?";
	
	/**
	 * Load a redemption promotion
	 * @param conn
	 * @param redemptionCode
	 * @return redemption promoId - The current value returned is Promotion CODE. Later when new data model
	 * for AI is implemented, value returned will be promotion ID.
	 * @throws SQLException
	 */
	public static Integer getRedemptions(Connection conn, String promoId, Date requestedDate) throws SQLException {
		
		PreparedStatement ps = null;
		if(requestedDate != null) {
			ps = conn.prepareStatement(promoRedemptionsWithDlvDateSQL);
		} else {
			ps = conn.prepareStatement(promoRedemptionsWithNoDlvDateSQL);
		}			
		ps.setString(1, promoId);
		if(requestedDate != null) {
			ps.setDate(2, new java.sql.Date(requestedDate.getTime()));
		}
		ResultSet rs = ps.executeQuery();
		int redemptions = 0;
		if (rs.next()) {
			redemptions = rs.getInt("redemptions");
		}

		rs.close();
		ps.close();

		return redemptions;
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
		EnumOfferType offerType = EnumOfferType.getEnum(rs.getString("OFFER_TYPE"));
		promo.setOfferType(offerType);		
		if("Y".equalsIgnoreCase(rs.getString("NEEDCUSTOMERLIST"))|| "Y".equalsIgnoreCase(rs.getString("rolling_from_first_order"))){
			//This is customer restricted Promotion. Create Audience Strategy.
			int rollingExpirationDays = rs.getInt("ROLLING_EXPIRATION_DAYS");
			boolean isRollingExpFrom1stOrder = "Y".equalsIgnoreCase(rs.getString("rolling_from_first_order"));
			//APPDEV-659 - Made isMaxUsagePerCustomer obsolete.
			AudienceStrategy aStrategy = new AudienceStrategy(false, rollingExpirationDays, isRollingExpFrom1stOrder);
			promo.addStrategy(aStrategy);
		}

		String status = rs.getString("STATUS");
		boolean onHold = "Y".equalsIgnoreCase(rs.getString("ON_HOLD"));
		promo.addStrategy(new ActiveInactiveStrategy(FDStoreProperties.getPromoValidRTStatuses().contains(status) && !onHold));
		
		promo.addStrategy(new LimitedUseStrategy(rs.getInt("MAX_USAGE")));
		int redeemCnt = rs.getInt("REDEEM_CNT");
		if(!rs.wasNull() && redeemCnt > 0)
			promo.addStrategy(new MaxRedemptionStrategy(redeemCnt));
		
		if("X".equalsIgnoreCase(rs.getString("RULE_BASED"))) {
			promo.addStrategy(new RuleBasedPromotionStrategy());
		}

		promo.addStrategy(new DateRangeStrategy(rs.getTimestamp("START_DATE"), rs.getTimestamp("EXPIRATION_DATE")));

		String redemptionCode = rs.getString("REDEMPTION_CODE");
		if (!rs.wasNull()) {
			promo.addStrategy(new RedemptionCodeStrategy(redemptionCode));
		}
		
		if(!"X".equals(rs.getString("DONOT_APPLY_FRAUD"))){
			promo.addStrategy(new FraudStrategy());
		}
		//TODO This needs to replaced by Customer strategy 	
		//decorateOrderTypestrategy(rs, promo);
		
		String rafPromoCode=rs.getString("RAF_PROMO_CODE");
		
		
	//	String rafPromoCode=rs.getString("RAF_PROMO_CODE");
		
		
		GeographyStrategy geoStrategy = loadGeographyStrategy(conn, promoId);
		if (geoStrategy != null) {
			promo.addStrategy(geoStrategy);
		}
		
		//TODO This needs to replaced by Cart strategy. 	
		decorateSampleStrategy(rs, promo);
		
		PromotionStrategyI profStrategy = loadProfiles(conn, promoId);
		if(profStrategy != null){
			promo.addStrategy(profStrategy);	
		}
		
		if(promoType.getName().equals(EnumPromotionType.LINE_ITEM.getName())){
			boolean favoritesOnly = "Y".equalsIgnoreCase(rs.getString("FAVORITES_ONLY"));
			if(favoritesOnly) {
				promo.addStrategy(new RecommendationStrategy());
			}
		}
		if("Y".equalsIgnoreCase(rs.getString("COMBINE_OFFER"))){				
			   promo.setCombineOffer(true);		
		}		
		//Add Customer Strategy
		PromotionStrategyI custStrategy = loadCustomerStrategy(conn, promoId);
		if (custStrategy != null) {
			promo.addStrategy(custStrategy);
		}
		
		//System.out.println("Loading promotion:" + promoId);
		if("STCO".equals(rs.getString("GEO_RESTRICTION_TYPE"))) {
			//Add the StateCountyStrategy
			//System.out.println("#########adding the statecounty strategy");
			PromotionStrategyI scStrategy = loadStateCountyStrategy(conn, promoId);
			if(scStrategy != null)
				promo.addStrategy(scStrategy);
		}
		

		
		PromotionStrategyI cartStrategyI = loadCartStrategy(conn,promoId);
		if(null != cartStrategyI){
			CartStrategy cartStrategy = (CartStrategy)cartStrategyI;
			cartStrategy.setNeedDryGoods("X".equalsIgnoreCase(rs.getString("NEEDDRYGOODS"))?true:false);
			cartStrategy.setMinSkuQuantity(rs.getInt("HASSKUQUANTITY"));
			cartStrategy.setTotalDcpdSubtotal(rs.getDouble("DCPD_MIN_SUBTOTAL"));
			promo.addStrategy(cartStrategy);				
		}
		
		//PromotionApplicatorI applicator = loadApplicator(rs, conn, promo);		
		//Set the zone strategy if applicable.
		PromotionStrategyI dlvZoneStrategyI = loadDlvZoneStrategy(conn, promoId);
		DCPDLineItemStrategy strategy = loadDCPDData(conn, promoId);
		loadApplicator(rs, conn, promo, dlvZoneStrategyI, strategy, (CartStrategy)cartStrategyI);

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
	
	private static void decorateSampleStrategy(ResultSet rs, Promotion promo) throws SQLException {
		
		SampleStrategy s = new SampleStrategy();

		boolean valid = true;
		
		//TODO to be modified based on new rules
		/*
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
	*/
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

	private static void loadApplicator(ResultSet rs, Connection conn, Promotion promo, PromotionStrategyI dlvZoneStrategyI, DCPDLineItemStrategy dcpdStrategy, CartStrategy cartStrategy) throws SQLException {

		//
		// header discount applicator
		//
		
		boolean wasNull = false;

		double minSubtotal = rs.getDouble("min_subtotal");
		wasNull |= rs.wasNull();
		
		if("HEADER".equals(rs.getString("CAMPAIGN_CODE"))) {
			/*APPDEV-1792 - Changes to support Streatchable dollar discount*/
			List<FDPromoDollarDiscount> dollarList = FDPromotionManagerNewDAO.loadDollarOffers(conn, rs.getString("ID"));
			
			double maxAmount = rs.getDouble("max_amount");
			wasNull |= rs.wasNull();		


			if(dollarList != null && dollarList.size() > 0) {
				//Header discount can be a discount list offer
				promo.addApplicator(new HeaderDiscountApplicator(new HeaderDiscountRule(dollarList)));
				//return new HeaderDiscountApplicator(new HeaderDiscountRule(dollarList));
			} else if (!wasNull && "HEADER".equals(rs.getString("CAMPAIGN_CODE"))) {
				//The discount is a dollar off discount
				promo.addApplicator(new HeaderDiscountApplicator(new HeaderDiscountRule(minSubtotal, maxAmount)));
				//return new HeaderDiscountApplicator(new HeaderDiscountRule(minSubtotal, maxAmount));
			} else {
				//
				// percent-off discount applicator
				//
				wasNull = false;
				double maxPercentageDiscount = rs.getDouble("MAX_PERCENTAGE_DISCOUNT");
				double percentOff = rs.getDouble("percent_off");		
				
				wasNull |= rs.wasNull();
				if (!wasNull && "HEADER".equals(rs.getString("CAMPAIGN_CODE"))) {
					
					promo.addApplicator(new PercentOffApplicator(minSubtotal, percentOff, maxPercentageDiscount));
				} else {
					//
					// Extend delivery pass applicator
					//
					wasNull = false;
					int extendDPDays = rs.getInt("extend_dp_days");
					wasNull |= rs.wasNull();
					if (!wasNull) {
						promo.addApplicator( new ExtendDeliveryPassApplicator(extendDPDays, minSubtotal));
					}
				}
			}
			
			
		}
		
		if("LINE_ITEM".equals(rs.getString("CAMPAIGN_CODE"))){			
			LineItemDiscountApplicator applicator = null;
			int skulimit = rs.getInt("SKU_LIMIT");
			double maxPercentageDiscount = rs.getDouble("MAX_PERCENTAGE_DISCOUNT");
			double percentOff = rs.getDouble("percent_off");
			double maxAmount = rs.getDouble("max_amount");
			wasNull = rs.wasNull();
			if (!wasNull) {
				//dollar off discount applicator
				applicator = new LineItemDiscountApplicator(minSubtotal);
				applicator.setDiscountRule(new HeaderDiscountRule(minSubtotal, maxAmount));
			}else{
				//percent-off discount applicator
				applicator = new LineItemDiscountApplicator(minSubtotal, percentOff, maxPercentageDiscount);
			}
			if(skulimit > 0) {
				applicator.setSkuLimit(skulimit);
				applicator.addLineItemStrategy(new SkuLimitStrategy(skulimit));
			}
			boolean recItemsOnly = "Y".equalsIgnoreCase(rs.getString("FAVORITES_ONLY"));
			applicator.setFavoritesOnly(recItemsOnly);
			if(recItemsOnly){
				applicator.addLineItemStrategy(new RecommendedLineItemStrategy());
			}
			int maxItemCount = rs.getInt("MAX_ITEM_COUNT");
			if(!rs.wasNull() && maxItemCount > 0){
				applicator.addLineItemStrategy(new MaxLineItemCountStrategy(maxItemCount));
			}
			boolean perishableOnly = "Y".equalsIgnoreCase(rs.getString("PERISHABLEONLY"));
			if(perishableOnly){
				applicator.addLineItemStrategy(new PerishableLineItemStrategy());
			}
			
			if(null != applicator && applicator instanceof LineItemDiscountApplicator){
				/*
				 * If the promotion is a Line item Discount Promotion, add the corresponding 
				 * DCPD line item strategy if present.
				 */
				if(dcpdStrategy != null) {
					((LineItemDiscountApplicator) applicator).addLineItemStrategy(dcpdStrategy);
				}
			} 
			
			promo.addApplicator(applicator);
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
		if(!wasNull){
			if ("SAMPLE".equals(rs.getString("CAMPAIGN_CODE"))) {
				promo.addApplicator( new SampleLineApplicator(new ProductReferenceImpl(categoryName, productName), minSubtotal));
			}else
			if("PRODUCT_SAMPLE".equals(rs.getString("CAMPAIGN_CODE"))){
				loadMinSubtotalStrategy(rs, promo);
				promo.addApplicator(new ProductSampleApplicator(new ProductReferenceImpl(categoryName, productName), minSubtotal));
			}
		}
		
		//Set the zone strategy if applicable.
		if((promo.getApplicatorList() != null && promo.getApplicatorList().size() > 0) && null != dlvZoneStrategyI){
			DlvZoneStrategy dlvZoneStrategy = (DlvZoneStrategy)dlvZoneStrategyI;
			if(dlvZoneStrategy.getDlvDayType()!=null || (null !=dlvZoneStrategy.getDlvDates() && !dlvZoneStrategy.getDlvDates().isEmpty()) || null != dlvZoneStrategy.getDlvZoneId()){			
				for (Iterator<PromotionApplicatorI> i = promo.getApplicatorList().iterator(); i.hasNext();) {
					PromotionApplicatorI _applicator = i.next();
					_applicator.setZoneStrategy(dlvZoneStrategy);
				}
			}
		}
		
		//Set the zone strategy if applicable.
		if((promo.getApplicatorList() != null && promo.getApplicatorList().size() > 0) && null != cartStrategy){
			
			if(null != cartStrategy.getTotalDcpdSubtotal() && cartStrategy.getTotalDcpdSubtotal() > 0){			
				for (Iterator<PromotionApplicatorI> i = promo.getApplicatorList().iterator(); i.hasNext();) {
					PromotionApplicatorI _applicator = i.next();
					_applicator.setCartStrategy(cartStrategy);
				}
			}
		}
	}

	/** @return Map of promotionPK -> GeographyStrategy */
	private final static String getPromoGeographyData = "select pg.id, pg.promotion_id, pg.start_date from cust.promo_geography_new pg, " +
									"(SELECT ID FROM CUST.PROMOTION_NEW where status STATUSES and (expiration_date > (sysdate-7) " +
									"or expiration_date is null) and redemption_code is null " +
									"and (REFERRAL_PROMO = 'N' or REFERRAL_PROMO is null) " +
									") p where p.ID = pg.PROMOTION_ID";

	private final static String getModifiedOnlyPromoGeographyData = "select pg.id, pg.promotion_id, pg.start_date from cust.promo_geography_new pg, " +
	"(SELECT ID FROM CUST.PROMOTION_NEW where modify_date > ? ) p where p.ID = pg.PROMOTION_ID";
	
	protected static Map<PrimaryKey,GeographyStrategy> loadGeographyStrategies(Connection conn, Date lastModified) throws SQLException {
		PreparedStatement ps;
		
		if(lastModified != null){
			ps = conn.prepareStatement(getModifiedOnlyPromoGeographyData);	
			ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		}else {
			final String query = getPromoGeographyData.replace("STATUSES", getStatusReplacementString());
			ps = conn.prepareStatement(query);
		}
		
		ResultSet rs = ps.executeQuery();

		// promotionPK -> GeographyStrategy 
		Map<PrimaryKey,GeographyStrategy> strategies = new HashMap<PrimaryKey,GeographyStrategy>();

		// geographyPK -> PromotionGeography		
		Map<PrimaryKey,PromotionGeography> geographies = new HashMap<PrimaryKey,PromotionGeography>();

		while (rs.next()) {

			PrimaryKey promoPK = new PrimaryKey(rs.getString("PROMOTION_ID"));
			PrimaryKey geoPK = new PrimaryKey(rs.getString("ID"));
			PromotionGeography geo = new PromotionGeography(geoPK, rs.getDate("START_DATE"));

			GeographyStrategy strategy = strategies.get(promoPK);
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
	
	protected static GeographyStrategy loadGeographyStrategy(Connection conn, String promoPK) throws SQLException {
		GeographyStrategy strategy = null;
		// geographyPK -> PromotionGeography		
		Map<PrimaryKey,PromotionGeography> geographies = new HashMap<PrimaryKey,PromotionGeography>();
		
		PreparedStatement ps = conn.prepareStatement("select id, start_date from cust.promo_geography_new where promotion_id = ?");
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

	private static void loadGeographyData(Connection conn, Map<PrimaryKey,PromotionGeography> geographies, String promoPK) throws SQLException {
		PreparedStatement ps = null;
		if(promoPK == null){
			ps = conn.prepareStatement("select geography_id, type, code, sign from cust.promo_geography_data_new");	
		} else{
			ps = conn.prepareStatement("select geography_id, type, code, sign from cust.promo_geography_data_new " +
										"where GEOGRAPHY_ID IN (select ID from cust.promo_geography_new where " +
										"promotion_id = ?)");
			ps.setString(1, promoPK);
		}
		
		ResultSet rs = ps.executeQuery();

		List<GeoTuple> tuples = new ArrayList<GeoTuple>();

		while (rs.next()) {
			tuples.add(new GeoTuple(rs));
		}

		// deal with GEO_CODE_ALL entries
		for (ListIterator<GeoTuple> i = tuples.listIterator(); i.hasNext();) {
			GeoTuple t = i.next();
			if (GEO_CODE_ALL.equals(t.code)) {
				PromotionGeography geo = geographies.get(t.geoPK);
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
		for (GeoTuple t : tuples) {
			PromotionGeography geo = geographies.get(t.geoPK);
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

	
	private final static String GET_DCPD_DATA = "select pg.promotion_id, pg.content_type, pg.content_id, exclude, pg.child_loop, pg.recommended from cust.PROMO_DCPD_DATA_NEW pg, " +
	"(SELECT ID FROM CUST.PROMOTION_NEW where status STATUSES and (expiration_date > (sysdate-7) " +
	"or expiration_date is null) and redemption_code is null " +
	"and (REFERRAL_PROMO = 'N' or REFERRAL_PROMO is null)" +
	") p where p.ID = pg.PROMOTION_ID";

	private final static String GET_MODIFIED_ONLY_DCPD_DATA = "select pg.id, pg.promotion_id, pg.content_type, pg.content_id, exclude, pg.child_loop, pg.recommended from cust.PROMO_DCPD_DATA_NEW pg, " +
	"(SELECT ID  FROM CUST.PROMOTION_NEW where modify_date > ? " +
	"and (REFERRAL_PROMO = 'N' or REFERRAL_PROMO is null)" +
	") p where p.ID = pg.PROMOTION_ID";
	
	protected static Map<PrimaryKey, DCPDLineItemStrategy> loadDCPDData(Connection conn, Date lastModified) throws SQLException {
		PreparedStatement ps;
		if(lastModified != null){
			ps = conn.prepareStatement(GET_MODIFIED_ONLY_DCPD_DATA);	
			ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		}else {
			final String query = GET_DCPD_DATA.replace("STATUSES", getStatusReplacementString());		
			ps = conn.prepareStatement(query);
		}
		//Map PromoPK --> DCPDLineItemStrategy
		Map<PrimaryKey, DCPDLineItemStrategy> dcpdDataMap = new HashMap<PrimaryKey, DCPDLineItemStrategy>();
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			String promoId = rs.getString("PROMOTION_ID");
			PrimaryKey promoPK = new PrimaryKey(promoId);
			DCPDLineItemStrategy strategy = dcpdDataMap.get(promoPK);
			if( strategy == null){
				strategy = new DCPDLineItemStrategy();
			}
			populateDCPDStrategy(rs,strategy);
			dcpdDataMap.put(promoPK, strategy);				
		}
		rs.close();
		ps.close();
		
		return dcpdDataMap;
	}
	
	protected static DCPDLineItemStrategy loadDCPDData(Connection conn, String promoId) throws SQLException {
		DCPDLineItemStrategy strategy = null;
		PreparedStatement ps = conn.prepareStatement("select pg.content_type, pg.content_id, exclude, pg.child_loop, pg.recommended from cust.PROMO_DCPD_DATA_NEW pg, " +
								"cust.promotion_new p where p.ID = pg.PROMOTION_ID and pg.promotion_id = ?");
		ps.setString(1, promoId);
		ResultSet rs = ps.executeQuery();
		
		while(rs.next()){
			if( strategy == null){
				strategy = new DCPDLineItemStrategy();
			}
			populateDCPDStrategy(rs,strategy);
		}
		rs.close();
		ps.close();
		
		return strategy;
	}

	private static void populateDCPDStrategy(ResultSet rs, DCPDLineItemStrategy strategy) throws SQLException {
		String contentType =  rs.getString("CONTENT_TYPE");
		String contentId = rs.getString("CONTENT_ID");
		if(contentType.equalsIgnoreCase("Sku")){
			if(strategy.getSkus().isEmpty()) {
				//First time exclude is set.
				strategy.setExcludeSkus("Y".equalsIgnoreCase(rs.getString("EXCLUDE")));
			}
			strategy.addSku(contentId);
		}
		else if(contentType.equalsIgnoreCase("Brand")){
			if(strategy.getBrands().isEmpty()) {
				//First time exclude is set.
				strategy.setExcludeBrands("Y".equalsIgnoreCase(rs.getString("EXCLUDE")));
			}
			strategy.addBrand(contentId);
		} else {
			//Content Type is department,category or recipe.
			strategy.addContent(contentType, contentId);
		}
		strategy.setLoopEnabled("Y".equalsIgnoreCase(rs.getString("CHILD_LOOP")));
		strategy.setRecCategory("Y".equalsIgnoreCase(rs.getString("RECOMMENDED")));
	}

	
	/** @return Map of promotionPK -> ProfileAttributeStrategy */
	private final static String GET_PROMO_PROFILE_ATTR = "select pa.promotion_id, pa.promo_attr_name, pa.attr_value, p.profile_operator from cust.promo_attr_new pa, " +
									"(SELECT ID, PROFILE_OPERATOR FROM CUST.PROMOTION_NEW where status STATUSES and (expiration_date > (sysdate-7) " +
									"or expiration_date is null) and redemption_code is null) p where p.ID = pa.PROMOTION_ID";

	private final static String GET_MODIFIED_ONLY_PROMO_PROFILE = "select pa.promotion_id, pa.promo_attr_name, pa.attr_value, p.profile_operator from cust.promo_attr_new pa, " +
	"(SELECT ID, PROFILE_OPERATOR FROM CUST.PROMOTION_NEW where modify_date > ? ) p where p.ID = pa.PROMOTION_ID";
	
	protected static Map<PrimaryKey,PromotionStrategyI> loadAllProfiles(Connection conn, Date lastModified) throws SQLException {
		PreparedStatement ps;
		if(lastModified != null){
			ps = conn.prepareStatement(GET_MODIFIED_ONLY_PROMO_PROFILE);	
			ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		}else {
			final String query = GET_PROMO_PROFILE_ATTR.replace("STATUSES", getStatusReplacementString());				
			ps = conn.prepareStatement(query);
		}
		//Map PromoPK --> CategoryDiscountApplicator
		Map<PrimaryKey,PromotionStrategyI> strategies = new HashMap<PrimaryKey,PromotionStrategyI>();
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
		PreparedStatement ps = conn.prepareStatement("select pa.promo_attr_name, pa.attr_value, p.profile_operator from cust.promo_attr_new pa, cust.promotion_new p " +
														"where p.ID = pa.PROMOTION_ID and pa.promotion_id = ?");
		ps.setString(1, promoId);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
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
	
	/** @return Map of promotionPK -> CustomerStrategy */
	private final static String GET_CUST_PROMO_STRATEGY = "select cs.promotion_id, cs.cohort,cs.dp_types, cs.dp_exp_end,cs.dp_exp_start, cs.dp_status, cs.order_range_end, " +
														   "cs.order_range_start,cs.payment_type,cs.prior_echeck_use,cs.echeck_match_type, ordertype_home, ordertype_pickup, ordertype_corporate, ordertype_fdx " +
														   "from cust.promo_cust_strategy cs, " +
														   "(SELECT ID FROM CUST.PROMOTION_NEW where status STATUSES and (expiration_date > (sysdate-7) " +
														   "or expiration_date is null) and redemption_code is null) p where p.ID = cs.PROMOTION_ID";

	private final static String GET_MODIFIED_CUST_PROMO_STRATEGY  =  "select cs.promotion_id, cs.cohort,cs.dp_types, cs.dp_exp_end,cs.dp_exp_start, cs.dp_status, cs.order_range_end, " +
																	 "cs.order_range_start,cs.payment_type,cs.prior_echeck_use,cs.echeck_match_type, ordertype_home, ordertype_pickup, ordertype_corporate, ordertype_fdx " +
																	 "from cust.promo_cust_strategy cs, " +
																	 "(SELECT ID FROM CUST.PROMOTION_NEW where modify_date > ? ) p where p.ID = cs.PROMOTION_ID";
	
	protected static Map<PrimaryKey, CustomerStrategy> loadCustomerStrategies(Connection conn, Date lastModified) throws SQLException {
		PreparedStatement ps;
		if(lastModified != null){
			ps = conn.prepareStatement(GET_MODIFIED_CUST_PROMO_STRATEGY);	
			ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		}else {
			final String query = GET_CUST_PROMO_STRATEGY.replace("STATUSES", getStatusReplacementString());		
			ps = conn.prepareStatement(query);
		}
		//Map PromoPK --> CategoryDiscountApplicator
		Map<PrimaryKey, CustomerStrategy> strategies = new HashMap<PrimaryKey, CustomerStrategy>();
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			CustomerStrategy strategy = new CustomerStrategy();
			String promoId = rs.getString("PROMOTION_ID");				
			PrimaryKey promoPK = new PrimaryKey(promoId);
			populateCustomerStrategy(rs, strategy);
			strategies.put(promoPK, strategy);
		}

		rs.close();
		ps.close();

		return strategies;
	}
	
	protected static PromotionStrategyI loadCustomerStrategy(Connection conn, String promoId) throws SQLException {
		CustomerStrategy strategy = null;
		PreparedStatement ps = conn.prepareStatement("select cs.promotion_id, cs.cohort,cs.dp_types, cs.dp_exp_end,cs.dp_exp_start, cs.dp_status, cs.order_range_end, " +
													 "cs.order_range_start,cs.payment_type,cs.prior_echeck_use,cs.echeck_match_type, ordertype_home, ordertype_pickup, ordertype_corporate, ordertype_fdx, fdx_tier_type " +
													 "from cust.promo_cust_strategy cs, cust.promotion_new p " +
													 "where p.ID = cs.PROMOTION_ID and cs.promotion_id = ?");
		ps.setString(1, promoId);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			strategy = new CustomerStrategy();
			populateCustomerStrategy(rs, strategy);
		}

		rs.close();
		ps.close();

		return strategy;
	}
	
	private static void populateCustomerStrategy(ResultSet rs, CustomerStrategy strategy) throws SQLException {
		String cohorts = rs.getString("COHORT");
		if(cohorts != null && cohorts.length() > 0){
			strategy.setCohorts(cohorts);
		}
		String dpTypes = rs.getString("DP_TYPES");
		if(dpTypes != null && dpTypes.length() > 0){
			strategy.setDpTypes(dpTypes);
		}
		String status = rs.getString("dp_status");
		if(status != null){
			strategy.setDPStatus(EnumDlvPassStatus.getEnum(status));
			Date dpStartDate = rs.getTimestamp("dp_exp_start");
			if(dpStartDate != null){
				strategy.setDPStartDate(dpStartDate);
			}
			Date dpEndDate = rs.getTimestamp("dp_exp_end");
			
			if(dpEndDate != null){
				strategy.setDPEndDate(dpEndDate);
			}
		}
		int orderStartRange = rs.getInt("order_range_start");
		strategy.setOrderStartRange(orderStartRange);
		int orderEndRange = rs.getInt("order_range_end");
		strategy.setOrderEndRange(orderEndRange);
		
		String paymentTypes = rs.getString("payment_type");
		if(paymentTypes != null && paymentTypes.length() > 0)
			strategy.setPaymentTypes(paymentTypes);
		
		int priorEcheckUse = rs.getInt("prior_echeck_use");
		strategy.setPriorEcheckUse(priorEcheckUse);
		Set<EnumOrderType> orderTypes = new HashSet<EnumOrderType>();
		
		strategy.setECheckMatchType(EnumComparisionType.getEnum(rs.getString("echeck_match_type")));

		if ("X".equals(rs.getString("ORDERTYPE_HOME"))) {
			orderTypes.add(EnumOrderType.HOME);
		}
		
		if ("X".equals(rs.getString("ORDERTYPE_PICKUP"))) {
			orderTypes.add(EnumOrderType.PICKUP);
		}

		if ("X".equals(rs.getString("ORDERTYPE_CORPORATE"))) {
			orderTypes.add(EnumOrderType.CORPORATE);
		}		

		if ("X".equals(rs.getString("ORDERTYPE_FDX"))) {
			orderTypes.add(EnumOrderType.FDX);
		}			
		
		strategy.setAllowedOrderTypes(orderTypes);					
	}
	
	
	
	/**
	 * Query to retreive all the Audience-Based promotions for a given customer.
	 * TODO Replace the Promotion Code with the Promotion ID when new AI database model
	 * is implemented.
	 */
	private final static String getABPromotionsForCustomer = "select p.code, pc.usage_cnt, pc.expiration_date "
		+ "from cust.promotion_new p, cust.promo_customer pc "
		+ "where p.id=pc.promotion_id and (p.expiration_date > (sysdate-7) or p.expiration_date is null) and (pc.customer_id = ? or upper(PC.CUSTOMER_EMAIL) = upper(?))";
	
	private static final String getABPromotionsForCustomer_Optimzed ="select p.code, pc.usage_cnt, pc.expiration_date "
	   +" from cust.promotion_new  p, cust.promo_customer pc "
	   +" where p.id = pc.promotion_id and (p.expiration_date > (sysdate - 7) OR p.expiration_date is null) and (pc.customer_id =?) "
	   +"    UNION "
	   +"   select p.code, pc.usage_cnt, pc.expiration_date "
	   +" from cust.promotion_new  p, cust.promo_customer pc "
	   +" where p.id = pc.promotion_id and (p.expiration_date > (sysdate - 7) or p.expiration_date is null) and "
	   +"   PC.CUSTOMER_ID is null and upper(PC.CUSTOMER_EMAIL) = upper(?) ";
	
	private static final String getABPromotionsForNewCustomer="select p.code, pc.usage_cnt, pc.expiration_date "
	   +" from cust.promotion_new  p, cust.promo_customer pc "
	   +" where p.id = pc.promotion_id and (p.expiration_date > (sysdate - 7) or p.expiration_date is null) and "
	   +"   PC.CUSTOMER_ID is null and upper(PC.CUSTOMER_EMAIL) = upper(?) ";
	/** @return Map of promotionPK -> AssignedCustomerStrategy */
	public static Map<String,AssignedCustomerParam> loadAssignedCustomerParams(Connection conn, String erpCustomerId, String email) throws SQLException {
		
		PreparedStatement ps =null;
		ResultSet rs =null;
		if(DEFAULT_ASSIGNED_CUSTOMER_PARAMS_QUERY_ID==FDStoreProperties.getAssignedCustomerParamsQueryId()) {
			if(StringUtil.isEmpty(erpCustomerId)) {
				ps = conn.prepareStatement(getABPromotionsForNewCustomer);
				ps.setString(1, email);
				
			} else {
				ps = conn.prepareStatement(getABPromotionsForCustomer_Optimzed);
				ps.setString(1, erpCustomerId);
				ps.setString(2, email);
			}
		} else {
			ps = conn.prepareStatement(getABPromotionsForCustomer);
			ps.setString(1, erpCustomerId);
			ps.setString(2, email);
			
		}
		rs = ps.executeQuery();
		// promotionPK -> FDPromoAudience 
		Map<String,AssignedCustomerParam> audiencePromoDtls = new ConcurrentHashMap<String,AssignedCustomerParam>();

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
	
	@Deprecated
	private final static String GET_ALL_ACTIVE_PROMO_VARIANTS = "select vp.VARIANT_ID, vp.PROMO_CODE, vp.PROMO_PRIORITY, v.FEATURE, vp.VARIANT_PRIORITY from cust.PROMO_VARIANTS vp, " +
			"cust.SS_VARIANTS v, cust.PROMOTION_NEW p where p.CODE = vp.PROMO_CODE and v.ID = vp.VARIANT_ID and p.status STATUSES and (p.expiration_date > (sysdate-7) " +
			"or p.expiration_date is null) and p.FAVORITES_ONLY='X' and v.archived = 'N'";
	
	@Deprecated
	public static List<PromoVariantModel> loadAllActivePromoVariants(Connection conn, List<EnumSiteFeature> smartSavingFeatures) throws SQLException {
		StringBuffer preparedStmtQry = new StringBuffer( GET_ALL_ACTIVE_PROMO_VARIANTS.replace("STATUSES", getStatusReplacementString()) ); 
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

	@Deprecated
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
	
	private final static String GET_ALL_PROMOTION_IDS = "SELECT ID FROM CUST.PROMOTION_NEW where status STATUSES and (expiration_date > (sysdate-7) " +
	"or expiration_date is null) and redemption_code is null " + 
	"and (REFERRAL_PROMO = 'N' or REFERRAL_PROMO is null) ";
    private final static String GET_ALL_PROMOTION_IDS_BY_MODIFY_DATE = "SELECT ID FROM CUST.PROMOTION_NEW where modify_date > ? and (REFERRAL_PROMO = 'N' or REFERRAL_PROMO is null)";
    
	protected static Map<String, DlvZoneStrategy> loadDlvZoneStrategies(Connection conn, Date lastModified) throws SQLException {
		Map<String, DlvZoneStrategy> dlvZoneStrategies = new HashMap<String, DlvZoneStrategy>();
		PreparedStatement ps;
		if(lastModified != null){
			ps = conn.prepareStatement(GET_ALL_PROMOTION_IDS_BY_MODIFY_DATE);	
			ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		}else {
			final String query = GET_ALL_PROMOTION_IDS.replace("STATUSES", getStatusReplacementString());	
			ps = conn.prepareStatement(query);
		}
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			String promoPK = rs.getString("ID");
			DlvZoneStrategy dlvZoneStrategy = new DlvZoneStrategy();
			loadDlvDates(conn, promoPK, dlvZoneStrategy);
			loadDlvZones(conn, promoPK, dlvZoneStrategy);
			dlvZoneStrategies.put(promoPK, dlvZoneStrategy);
		}
		rs.close();
		ps.close();
		return dlvZoneStrategies;
	}
	
	private final static String GET_PROMO_DLV_DATES = " select * from CUST.PROMO_DELIVERY_DATES where PROMOTION_ID = ?";
	private final static String GET_PROMO_DLV_ZONES = " select * from CUST.PROMO_DLV_ZONE_STRATEGY where PROMOTION_ID = ?";
	private final static String GET_PROMO_DLV_TIMESLOTS = " select * from CUST.PROMO_DLV_TIMESLOT where PROMO_DLV_ZONE_ID = ?";
	private final static String GET_PROMO_DLV_DAYS = " select * from CUST.PROMO_DLV_DAY where PROMO_DLV_ZONE_ID = ?";
	
	protected static DlvZoneStrategy loadDlvZoneStrategy(Connection conn, String promoPK) throws SQLException {
		DlvZoneStrategy dlvZoneStrategy = new DlvZoneStrategy();
		loadDlvDates(conn, promoPK, dlvZoneStrategy);
		loadDlvZones(conn, promoPK, dlvZoneStrategy);
		return dlvZoneStrategy;
	}

	private static void loadDlvZones(Connection conn, String promoPK,
			DlvZoneStrategy dlvZoneStrategy)
			throws SQLException {
		Map<Integer,List<PromotionDlvTimeSlot>> dlvTimeSlots = new HashMap<Integer,List<PromotionDlvTimeSlot>>();
		Map<Integer, PromotionDlvDay> dlvDays = new HashMap<Integer, PromotionDlvDay>();
		PreparedStatement ps;
		ResultSet rs;
		ps = conn.prepareStatement(GET_PROMO_DLV_ZONES);
		ps.setString(1, promoPK);
		rs = ps.executeQuery();
		if(rs.next()) {
			String dlvZoneId =rs.getString("id");
			dlvZoneStrategy.setDlvZoneId(dlvZoneId);
			dlvZoneStrategy.setDlvDays(rs.getString("DLV_DAYS"));
			Array array = rs.getArray(4);
			String[] zoneCodes = (String[])array.getArray();
			dlvZoneStrategy.setDlvZones(Arrays.asList(zoneCodes));;			
		}
		rs.close();
		ps.close();
		if(null != dlvZoneStrategy.getDlvZoneId() && !"".equals(dlvZoneStrategy.getDlvZoneId().trim())){
			ps = conn.prepareStatement(GET_PROMO_DLV_TIMESLOTS);
			ps.setString(1, dlvZoneStrategy.getDlvZoneId());
			rs = ps.executeQuery();
			while (rs.next()) {
				Integer dayId = rs.getInt("DAY_ID");
				Array windowArray = rs.getArray("DLV_WINDOWTYPE");
				String[] windowType = windowArray != null ? (String[]) windowArray.getArray() : null;
				PromotionDlvTimeSlot dlvTimeSlot = new PromotionDlvTimeSlot(dayId,rs.getString("START_TIME"),rs.getString("END_TIME"), windowType);
				List<PromotionDlvTimeSlot> dlvTimeSlotList = dlvTimeSlots.get(dayId);
				if(null == dlvTimeSlotList){
					dlvTimeSlotList = new ArrayList<PromotionDlvTimeSlot>();
				}
				dlvTimeSlotList.add(dlvTimeSlot);
				dlvTimeSlots.put(dayId, dlvTimeSlotList);			
			}
			rs.close();
			ps.close();
			
			ps = conn.prepareStatement(GET_PROMO_DLV_DAYS);
			ps.setString(1, dlvZoneStrategy.getDlvZoneId());
			rs = ps.executeQuery();
			while (rs.next()) {
				PromotionDlvDay dlvDay = new PromotionDlvDay(rs.getInt("DAY_ID"), rs.getInt("REDEEM_CNT"));
				if(!dlvDays.containsKey(dlvDay.getDayId())){
					dlvDays.put(dlvDay.getDayId(), dlvDay);
				}			
			}
			rs.close();
			ps.close();
		}
		dlvZoneStrategy.setDlvTimeSlots(dlvTimeSlots);
		dlvZoneStrategy.setDlvDayRedemtions(dlvDays);
		loadDlvDayTypeStrategy(conn, promoPK, dlvZoneStrategy);
	}

	private static void loadDlvDayTypeStrategy(Connection conn, String promoPK,
			DlvZoneStrategy dlvZoneStrategy) throws SQLException {
		PreparedStatement ps;
		ResultSet rs;
		ps = conn.prepareStatement("SELECT DELIVERY_DAY_TYPE, FDX_TIER_TYPE FROM CUST.PROMO_CUST_STRATEGY WHERE PROMOTION_ID=?");
		ps.setString(1, promoPK);
		rs = ps.executeQuery();
		if(rs.next()) {
			String dlvDayType = rs.getString("DELIVERY_DAY_TYPE");
			dlvZoneStrategy.setDlvDayType(EnumDeliveryOption.getEnum(dlvDayType));
			String fdxTierType = rs.getString("FDX_TIER_TYPE");
			dlvZoneStrategy.setFdxTierType(EnumPromoFDXTierType.getEnum(fdxTierType));
		}
		rs.close();
		ps.close();
	}

	private static void loadDlvDates(Connection conn, String promoPK,
			DlvZoneStrategy dlvZoneStrategy) throws SQLException {
		List<PromotionDlvDate> dlvDates = new ArrayList<PromotionDlvDate>();
		PreparedStatement ps = conn.prepareStatement(GET_PROMO_DLV_DATES);
		ps.setString(1, promoPK);
		ResultSet rs = ps.executeQuery();	
		while (rs.next()) {
			PrimaryKey pk  = new PrimaryKey(rs.getString("ID"));
			Date startDate = rs.getDate("START_DATE");
			Date endDate = rs.getDate("END_DATE");
			PromotionDlvDate dlvDate = new PromotionDlvDate(pk,startDate,endDate);
			dlvDates.add(dlvDate);
		}
		rs.close();
		ps.close();
		dlvZoneStrategy.setDlvDates(dlvDates);
	}


	private static CartStrategy loadCartStrategy(Connection conn, String promoPK) throws SQLException{
		CartStrategy cartStrategy = new CartStrategy();
		PreparedStatement ps = conn.prepareStatement("select id, promotion_id, content_type, content_id, content_set_num from cust.promo_cart_strategy pcs where pcs.promotion_id = ? ");
		ps.setString(1, promoPK);
		ResultSet rs = ps.executeQuery();

	    Map<EnumDCPDContentType, Set<String>> dcpdData = new HashMap<EnumDCPDContentType, Set<String>>();
		
		while (rs.next()) {
			populateCartStrategy(rs, cartStrategy);
		}
	//	cartStrategy.setDcpdData(dcpdData);		
		
		return cartStrategy;
	}
	
	private final static String GET_CART_STRATEGY_DATA = "select pcs.id, pcs.promotion_id, pcs.content_type, pcs.content_id, pcs.content_set_num from cust.PROMO_CART_STRATEGY pcs, " +
	"(SELECT ID FROM CUST.PROMOTION_NEW where status STATUSES and (expiration_date > (sysdate-7) " +
	"or expiration_date is null) and redemption_code is null) p where p.ID = pcs.PROMOTION_ID";

	private final static String GET_MODIFIED_ONLY_CART_STRATEGY_DATA = "select pcs.id, pcs.promotion_id, pcs.content_type, pcs.content_id, pcs.content_set_num from cust.PROMO_CART_STRATEGY pcs, " +
	"(SELECT ID  FROM CUST.PROMOTION_NEW where modify_date > ? ) p where p.ID = pcs.PROMOTION_ID";
	

	private static Map<PrimaryKey, CartStrategy> loadCartStrategies(Connection conn, Date lastModified) throws SQLException{
		
		PreparedStatement ps = null;
		if(lastModified != null){
			ps = conn.prepareStatement(GET_MODIFIED_ONLY_CART_STRATEGY_DATA);
			ps.setTimestamp(1, new Timestamp(lastModified.getTime()));
		}else {
			final String query = GET_CART_STRATEGY_DATA.replace("STATUSES", getStatusReplacementString());		
			ps = conn.prepareStatement(query);	
		}
		ResultSet rs = ps.executeQuery();

	    Map<PrimaryKey, CartStrategy> cartStrategyDataMap = new HashMap<PrimaryKey, CartStrategy>();
		
		while (rs.next()) {
			String promoId = rs.getString("PROMOTION_ID");
			PrimaryKey promoPK = new PrimaryKey(promoId);

			CartStrategy strategy = cartStrategyDataMap.get(promoPK);
			if( strategy == null){
				strategy = new CartStrategy();
			}
			populateCartStrategy(rs, strategy);
			cartStrategyDataMap.put(promoPK, strategy);
		}	
		
		return cartStrategyDataMap;
	}


	private static void populateCartStrategy(ResultSet rs, CartStrategy strategy) throws SQLException {
		String rawContentType = rs.getString("content_type");
		Integer contentSetNum = rs.getInt("content_set_num");
		final EnumDCPDContentType contentType =EnumDCPDContentType.getEnum(rawContentType);

		if(EnumDCPDContentType.DEPARTMENT.equals(contentType) || EnumDCPDContentType.CATEGORY.equals(contentType)) {
			strategy.addContent(contentType.getName(), rs.getString("content_id"));
		}
		Set<String> set = strategy.getDcpdData().get(EnumDCPDContentType.getEnum(rawContentType));
		if(null == set){
			set = new HashSet<String>();
		}

		set.add(rs.getString("content_id"));
		strategy.getDcpdData().put(EnumDCPDContentType.getEnum(rawContentType),set);
		
		if(2 == contentSetNum && EnumDCPDContentType.SKU.equals(contentType)){
			strategy.getCombinationSku().add(rs.getString("content_id"));
		}
	}
	
    
	public static final String GET_REF_PROMO =  "SELECT p.* " +  
			"FROM CUST.PROMOTION_NEW p, " +
			    "CUST.FDCUSTOMER fc, " +
			    "CUST.REFERRAL_PRGM rp, " + 
			    "CUST.REFERRAL_CUSTOMER_LIST rcl " + 
			"where FC.ERP_CUSTOMER_ID = ? " +
			"and     FC.REFERER_CUSTOMER_ID = RCL.ERP_CUSTOMER_ID " +
			"and     RCL.REFERAL_PRGM_ID = RP.ID " +
			"and     RP.EXPIRATION_DATE > trunc(sysdate) " + 
			"and     RP.PROMOTION_ID = P.ID " +
			"and     p.status = 'LIVE' " +
			"and    (p.expiration_date > (sysdate-7) or p.expiration_date is null) " +  
			"and     p.redemption_code is null " +
			"and    (rp.Delete_flag is null or rp.delete_flag != 'Y')" +
			"order by p.modify_date desc";
	
	public static final String GET_REF_EXTOLE_PROMO = "SELECT p.* "
			+ "FROM  CUST.PROMOTION_NEW p, "
			+ "CUST.FDCUSTOMER fc, "
			+ "CUST.CUSTOMER c "
			+ "where fc.ERP_CUSTOMER_ID = ? "
			+ "and     fc.ERP_CUSTOMER_ID = c.ID "
			+ "and     fc.RAF_PROMO_CODE=p.RAF_PROMO_CODE and p.referral_promo='Y' "
			+ "and     fc.raf_promo_code is not null "
			+ "and     p.status in ('LIVE') "
			+ "and    (p.expiration_date > (sysdate-7) or p.expiration_date is null) "
			+ "and     p.redemption_code is  null "
			+ "order by p.modify_date desc";
	
	
	public static final String GET_DEFAULT_REF_PROMO = "SELECT p.* " +
				"FROM CUST.PROMOTION_NEW p, " +
				    "CUST.FDCUSTOMER fc, " +
				    "CUST.REFERRAL_PRGM rp, " +
				    "CUST.CUSTOMER c " +
				"where FC.ERP_CUSTOMER_ID = ? " + 
				"and     (FC.REFERER_CUSTOMER_ID is not null and FC.REFERER_CUSTOMER_ID = C.ID) " + 
				"and     RP.EXPIRATION_DATE > trunc(sysdate) " +
				"and     RP.DEFAULT_PROMO = 'Y' " +
				"and     RP.PROMOTION_ID = P.ID " +
				"and     p.status = 'LIVE' " +
				"and    (p.expiration_date > (sysdate-7) or p.expiration_date is null) " +   
				"and     p.redemption_code is null " +
				"and    (rp.Delete_flag is null or rp.delete_flag != 'Y')";

	public static List<PromotionI> getReferralPromotions(String customerId, Connection conn) throws SQLException {
		LOGGER.debug("Query is "+GET_REF_PROMO);
		String query = GET_REF_PROMO;
		if(FDStoreProperties.isExtoleRafEnabled()){
			query = GET_REF_EXTOLE_PROMO;
		}
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, customerId);
		ResultSet rs = ps.executeQuery();
		List<PromotionI> promotions =  new ArrayList<PromotionI>();
		while(rs.next()) {
			Promotion promotion = constructPromotionFromResultSet(conn, rs);
			System.out.println("Adding ReferAFriendStrategy for promocode" + promotion.getPromotionCode());
			promotion.addStrategy(new ReferAFriendStrategy());
			promotions.add(promotion);
		}
		
		if(!FDStoreProperties.isExtoleRafEnabled() && promotions.size() == 0) {
			//see if there is a default promo available
			LOGGER.debug("Query is "+GET_DEFAULT_REF_PROMO);
			ps = conn.prepareStatement(GET_DEFAULT_REF_PROMO);
			ps.setString(1, customerId);
			rs = ps.executeQuery();
			while(rs.next()) {
				Promotion promotion = constructPromotionFromResultSet(conn, rs);
				System.out.println("Adding ReferAFriendStrategy for promocode" + promotion.getPromotionCode());
				promotion.addStrategy(new ReferAFriendStrategy());
				promotions.add(promotion);
			}
		}
		rs.close();
		ps.close();
		return promotions;
	}
	
	
	
	
}
