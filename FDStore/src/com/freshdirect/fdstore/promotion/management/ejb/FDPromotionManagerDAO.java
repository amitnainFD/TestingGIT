package com.freshdirect.fdstore.promotion.management.ejb;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.freshdirect.fdstore.promotion.AssignedCustomerParam;
import com.freshdirect.fdstore.promotion.EnumDCPDContentType;
import com.freshdirect.fdstore.promotion.PromoVariantModel;
import com.freshdirect.fdstore.promotion.PromoVariantModelImpl;
import com.freshdirect.fdstore.promotion.management.FDPromoCustomerInfo;
import com.freshdirect.fdstore.promotion.management.FDPromoZipRestriction;
import com.freshdirect.fdstore.promotion.management.FDPromotionAttributeParam;
import com.freshdirect.fdstore.promotion.management.FDPromotionModel;
import com.freshdirect.fdstore.promotion.management.FDPromotionNewModel;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.framework.core.ModelI;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.core.SequenceGenerator;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.EnumMonth;
import com.freshdirect.framework.util.FormatterUtil;
import com.freshdirect.framework.util.NVL;
import com.freshdirect.framework.util.StringUtil;

public class FDPromotionManagerDAO {
	
	public static PrimaryKey createPromotion(Connection conn, ModelI model) throws SQLException {
		FDPromotionModel promotion = (FDPromotionModel) model;
		String id = SequenceGenerator.getNextId(conn, "CUST");
		//String id = promotion.getPK().getId()!=null?promotion.getPK().getId():SequenceGenerator.getNextId(conn, "CUST");
		PreparedStatement ps =
			conn.prepareStatement(
				"INSERT INTO CUST.PROMOTION"
				+ " (ID, CODE, NAME, DESCRIPTION, MAX_USAGE, START_DATE, EXPIRATION_DATE, REDEMPTION_CODE,"
				+ " CAMPAIGN_CODE, MIN_SUBTOTAL, MAX_AMOUNT, PERCENT_OFF, WAIVE_CHARGE_TYPE, UNIQUE_USE, CATEGORY_NAME, PRODUCT_NAME,"
				+ " ORDERTYPE_HOME, ORDERTYPE_PICKUP, ORDERTYPE_DEPOT, ORDERTYPE_CORPORATE, NEEDDRYGOODS,"
				+ " ORDERCOUNT, NEEDITEMSFROM, EXCLUDESKUPREFIX, NEEDBRANDS, EXCLUDEBRANDS, RULE_BASED, REF_PROG_CAMPAIGN_CODE,"
				+ " IS_MAX_USAGE_PER_CUST, ROLLING_EXPIRATION_DAYS, NOTES, ACTIVE, MODIFY_DATE, MODIFIED_BY, DONOT_APPLY_FRAUD, PROFILE_OPERATOR,"
				+ " recommended_items_only, allow_header_discount, max_item_count)"
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,SYSDATE,?,?,?,?,?,?)");
		
		int i = 1;
		ps.setString(i++, id);
		i = setupPreparedStatement(ps, promotion, i);
		
		if(promotion.getNotes() !=null) {
			ps.setString(i++, promotion.getNotes());
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}
		
		if(promotion.isActive()){
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}
		
		if (promotion.getModifiedBy() != null) {
			ps.setString(i++, promotion.getModifiedBy());
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}
		
		if(!promotion.isApplyFraud()){
			ps.setString(i++, "X");
		}else{
			ps.setNull(i++, Types.VARCHAR);
		}
		
		if(promotion.getProfileOperator() != null && 
				promotion.getAttributeList() != null && promotion.getAttributeList().size() > 1){
			ps.setString(i++, promotion.getProfileOperator());
		}else{
			ps.setNull(i++, Types.VARCHAR);
		}
		if(promotion.isRecommendedItemsOnly()){
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}
		
		if(promotion.isAllowHeaderDiscount()){
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}
		
		if (promotion.getMaxItemCount()>0) {
			ps.setInt(i++,promotion.getMaxItemCount());
		} else {
			ps.setNull(i++, Types.INTEGER);					
		}


		if (ps.executeUpdate() != 1) {
			ps.close();
			throw new SQLException("row not created");
		}
		ps.close();
		
		FDPromotionManagerDAO.storeAssignedCustomers(conn, id, promotion.getTmpAssignedCustomerUserIds());
		FDPromotionManagerDAO.storeAssignedGroups(conn, id, promotion);
		FDPromotionManagerDAO.storeAttributeList(conn, id, promotion.getAttributeList());
		
		if(!promotion.getZipRestrictions().isEmpty()){
			storeGeography(conn, id, promotion.getZipRestrictions());
		}
		return new PrimaryKey(id);
	}


    private static int setupPreparedStatement(PreparedStatement ps, FDPromotionModel promotion, int i) throws SQLException {
        ps.setString(i++, promotion.getPromotionCode());
		ps.setString(i++, promotion.getName());
		ps.setString(i++, promotion.getDescription());		
		String maxUsage = !"".equals(promotion.getMaxUsage()) ? promotion.getMaxUsage() : "0"; 
		ps.setInt(i++, Integer.parseInt(maxUsage));
		if (promotion.getStartDate() != null) {
			ps.setDate(i++, new java.sql.Date(promotion.getStartDate().getTime()));
		} else {
			ps.setNull(i++, Types.DATE);		
		}
		if (promotion.getExpirationDate() != null) {
			ps.setTimestamp(i++, new java.sql.Timestamp(promotion.getExpirationDate().getTime()));
		} else {
			ps.setNull(i++, Types.DATE);		
		}
		if (!"".equals(promotion.getRedemptionCode())) {
			ps.setString(i++, promotion.getRedemptionCode());
		} else {
			ps.setNull(i++, Types.VARCHAR);		
		}
		ps.setString(i++, promotion.getPromotionType());
		if (!"".equals(promotion.getMinSubtotal())) {
			//ps.setDouble(i++, Double.parseDouble(promotion.getMinSubtotal()));
			ps.setBigDecimal(i++, new java.math.BigDecimal(promotion.getMinSubtotal()));
		} else {
			ps.setNull(i++, Types.DOUBLE);					
		}		
		if (!"".equals(promotion.getMaxAmount())) {
			//ps.setDouble(i++, Double.parseDouble(promotion.getMaxAmount()));
			ps.setBigDecimal(i++, new java.math.BigDecimal(promotion.getMaxAmount()));
		} else {
			ps.setNull(i++, Types.DOUBLE);					
		}
		if (!"".equals(promotion.getPercentOff())) {
			ps.setDouble(i++, Double.parseDouble(promotion.getPercentOff())/100);
		} else {
			ps.setNull(i++, Types.DOUBLE);					
		}
		if (!"".equals(promotion.getWaiveChargeType())) {
			ps.setString(i++, promotion.getWaiveChargeType());
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}		
		if(promotion.isUniqueUse()){
			ps.setString(i++, "X");
		}else{
			ps.setNull(i++, Types.VARCHAR);
		}
		if (!"".equals(promotion.getCategoryName())) {
			ps.setString(i++, promotion.getCategoryName());
		} else {
			ps.setNull(i++, Types.VARCHAR);								
		}		
		if (!"".equals(promotion.getProductName())) {
			ps.setString(i++, promotion.getProductName());
		} else {
			ps.setNull(i++, Types.VARCHAR);								
		}
		if (promotion.isOrderTypeHomeAllowed()) {
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);								
		}
		if (promotion.isOrderTypePickupAllowed()) {
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);								
		}
		if (promotion.isOrderTypeDepotAllowed()) {
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);								
		}
		if (promotion.isOrderTypeCorporateAllowed()) {
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);								
		}		
		if (promotion.getNeedDryGoods()) {
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);								
		}
		if (!"".equals(promotion.getOrderCount())) {
			ps.setInt(i++, Integer.parseInt(promotion.getOrderCount()));
		} else {
			ps.setNull(i++, Types.INTEGER);								
		}
		if (!"".equals(promotion.getNeedItemsFrom())) {
			ps.setString(i++, promotion.getNeedItemsFrom());
		} else {
			ps.setNull(i++, Types.VARCHAR);								
		}
		if (!"".equals(promotion.getExcludeSkuPrefix())) {
			ps.setString(i++, promotion.getExcludeSkuPrefix());
		} else {
			ps.setNull(i++, Types.VARCHAR);								
		}
		if (!"".equals(promotion.getNeedBrands())) {
			ps.setString(i++, promotion.getNeedBrands());
		} else {
			ps.setNull(i++, Types.VARCHAR);								
		}
		if (!"".equals(promotion.getExcludeBrands())) {
			ps.setString(i++, promotion.getExcludeBrands());
		} else {
			ps.setNull(i++, Types.VARCHAR);								
		}
		
		if(promotion.isRuleBasedPromotion()) {
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}
		
		if (!"".equals(promotion.getRefProgCampaignCode())) {
			ps.setString(i++, promotion.getRefProgCampaignCode());
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}		

		if(promotion.isMaxUsagePerCustomer()) {
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}

		if(promotion.getRollingExpirationDays() != null) {
			ps.setInt(i++, promotion.getRollingExpirationDays().intValue());
		} else {
			ps.setNull(i++, Types.INTEGER);
		}
        return i;
    }
	
	private final static String getAllPromotionCodes = "SELECT CODE, MODIFY_DATE FROM CUST.PROMOTION";

	/**
	 * This method returns all promotion codes along
	 * with their last modified timestamp.
	 * TODO Later the Promotion codes has to be replaced with Promotion IDs when
	 * new data model changes for AI is implemented.
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static Map<String,Timestamp> getPromotionCodes(Connection conn) throws SQLException {
		Map<String,Timestamp> promoCodes = new HashMap<String,Timestamp>();
		//TODO later the where clause need to be changed to point to ID column instead of Code. 
		PreparedStatement ps = conn.prepareStatement(getAllPromotionCodes);
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
	
	
	private static final String PROMO_VARIANT_QUERY="SELECT VP.VARIANT_ID, VP.PROMO_CODE, VP.PROMO_PRIORITY, V.FEATURE, VP.VARIANT_PRIORITY FROM CUST.PROMO_VARIANTS VP, "+
													"CUST.SS_VARIANTS V, CUST.PROMOTION P WHERE P.CODE = VP.PROMO_CODE AND V.ID = VP.VARIANT_ID AND P.ACTIVE='X' AND (P.EXPIRATION_DATE > (SYSDATE-7) "+
													" OR P.EXPIRATION_DATE IS NULL) AND P.RECOMMENDED_ITEMS_ONLY='X' AND P.CODE=? and v.archived = 'N'";
	
	public static List<PromoVariantModel> getPromotionVariants(Connection conn,String promoId) throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement(PROMO_VARIANT_QUERY);
		ps.setString(1,promoId);
		ResultSet rs = ps.executeQuery();

		List<PromoVariantModel> promoList = new ArrayList<PromoVariantModel>();
		
		while (rs.next()) {			
			//FDPromotionModel promotion = loadPromotionResult(rs);
			
			PromoVariantModel model=new PromoVariantModelImpl(rs.getString("VARIANT_ID"),promoId,rs.getInt("PROMO_PRIORITY"),EnumSiteFeature.getEnum(rs.getString("FEATURE")),rs.getInt("VARIANT_PRIORITY"));			
			
			/*List assignedCustomerUserIds = FDPromotionManagerDAO.loadAssignedCustomerUserIds(conn, promotion.getId());
			if (assignedCustomerUserIds != null && assignedCustomerUserIds.size() > 0) {
				promotion.setAssignedCustomerUserIds(encodeString(assignedCustomerUserIds));
			} else {
				promotion.setAssignedCustomerUserIds("");				
			}*/			
			promoList.add(model);
		}

		rs.close();
		ps.close();

		return promoList;
		
	}
	

	public static List<FDPromotionModel> getPromotions(Connection conn) throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM CUST.PROMOTION");
		ResultSet rs = ps.executeQuery();

		List<FDPromotionModel> promoList = new ArrayList<FDPromotionModel>();
		
		while (rs.next()) {			
			FDPromotionModel promotion = loadPromotionResult(rs);
			/*List assignedCustomerUserIds = FDPromotionManagerDAO.loadAssignedCustomerUserIds(conn, promotion.getId());
			if (assignedCustomerUserIds != null && assignedCustomerUserIds.size() > 0) {
				promotion.setAssignedCustomerUserIds(encodeString(assignedCustomerUserIds));
			} else {
				promotion.setAssignedCustomerUserIds("");				
			}*/
			if (promotion != null) {
				PrimaryKey pk = promotion.getPK();
				promotion.setZipRestrictions(loadZipRestrictions(conn, pk.getId()));
			}
			promoList.add(promotion);
		}

		rs.close();
		ps.close();

		return promoList;
		
	}

	/**
	 * Load Promotion for a given Promotion PK.
	 * @param conn
	 * @param promoId -TODO The current value passed is Promotion CODE. Later when new data model
	 * for AI is implemented, value passed will be promotion ID.
	 * @return
	 * @throws SQLException
	 */
	public static FDPromotionModel getPromotion(Connection conn, String promoId) throws SQLException {

		PreparedStatement ps = conn.prepareStatement("SELECT * FROM CUST.PROMOTION WHERE CODE = ?");
		ps.setString(1, promoId);
		ResultSet rs = ps.executeQuery();
	
		FDPromotionModel promotion = null;
		
		if (rs.next()) {			
			promotion = loadPromotionResult(rs);
			List<String> assignedCustomerUserIds = FDPromotionManagerDAO.loadAssignedCustomerUserIds(conn, promotion.getId());
			if (assignedCustomerUserIds != null && assignedCustomerUserIds.size() > 0) {
				promotion.setAssignedCustomerUserIds(StringUtil.encodeString(assignedCustomerUserIds));
			} else {
				promotion.setAssignedCustomerUserIds("");				
			}
		}

		rs.close();
		ps.close();
		if (promotion != null) {
			String id = promotion.getId();
			Map<String,AssignedCustomerParam> assignedCustomerParams = loadAssignedCustomerParams(conn, id);
			promotion.setAssignedCustomerParams(assignedCustomerParams);
			promotion.setZipRestrictions(loadZipRestrictions(conn, id));
			
			Map<EnumDCPDContentType,List<String>> assignedGroups = loadAssignedGroups(conn, id);
			promotion.setAssignedCategories(StringUtil.encodeString(assignedGroups.get(EnumDCPDContentType.CATEGORY)));
			promotion.setAssignedDepartments(StringUtil.encodeString(assignedGroups.get(EnumDCPDContentType.DEPARTMENT)));
			promotion.setAssignedRecipes(StringUtil.encodeString(assignedGroups.get(EnumDCPDContentType.RECIPE)));
			
			promotion.setAttributeList(loadAttributeList(conn, id));
		}
		return promotion;
	}
	
	private static FDPromotionModel loadPromotionResult(ResultSet rs) throws SQLException {
		
		String id = rs.getString("ID");
		FDPromotionModel promotion = new FDPromotionModel(new PrimaryKey(id));
		promotion.setId(id);
		promotion.setPromotionCode( rs.getString("CODE"));
		promotion.setDescription(rs.getString("DESCRIPTION"));
		promotion.setName(rs.getString("NAME"));
		promotion.setPromotionType(rs.getString("CAMPAIGN_CODE"));
		promotion.setMaxUsage(String.valueOf(rs.getInt("MAX_USAGE")));
		promotion.setStartDate(rs.getTimestamp("START_DATE"));
		if (promotion.getStartDate() != null) {
			Calendar cal = DateUtil.toCalendar(promotion.getStartDate());
			EnumMonth startMonth = EnumMonth.getEnum(String.valueOf(cal.get(Calendar.MONTH)+1));
			if (startMonth != null) {
				promotion.setStartMonth(startMonth.getDescription());
			}
			promotion.setStartDay(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
			promotion.setStartYear(String.valueOf(cal.get(Calendar.YEAR)));
		}		
		promotion.setExpirationDate(rs.getTimestamp("EXPIRATION_DATE"));
		if (promotion.getExpirationDate() != null) {
			Calendar cal = DateUtil.toCalendar(promotion.getExpirationDate());
			EnumMonth expirationMonth = EnumMonth.getEnum(String.valueOf(cal.get(Calendar.MONTH)+1));
			if (expirationMonth != null) {
				promotion.setExpirationMonth(expirationMonth.getDescription());
			}
			promotion.setExpirationDay(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
			promotion.setExpirationYear(String.valueOf(cal.get(Calendar.YEAR)));
		}
		String redemptionCode = rs.getString("REDEMPTION_CODE");
		if (!rs.wasNull()) {
			promotion.setRedemptionCode(redemptionCode);
		} else {
			promotion.setRedemptionCode("");			
		}
		if ("X".equals(rs.getString("ORDERTYPE_HOME"))) {
			promotion.setIsOrderTypeHomeAllowed(true);
		}
		if ("X".equals(rs.getString("ORDERTYPE_DEPOT"))) {
			promotion.setIsOrderTypeDepotAllowed(true);
		}
		if ("X".equals(rs.getString("ORDERTYPE_PICKUP"))) {
			promotion.setIsOrderTypePickupAllowed(true);
		}
		if ("X".equals(rs.getString("ORDERTYPE_CORPORATE"))) {
			promotion.setIsOrderTypeCorporateAllowed(true);
		}
		boolean needDryGoods = "X".equals(rs.getString("NEEDDRYGOODS"));
		if (!rs.wasNull()) {
			promotion.setNeedsDryGoods(true);
		}
		int orderCount = rs.getInt("ORDERCOUNT");
		if (!rs.wasNull()) {
			promotion.setOrderCount(String.valueOf(orderCount));
		} else {
			promotion.setOrderCount("");
		}
		String needItemsFrom = rs.getString("NEEDITEMSFROM");
		if (!rs.wasNull()) {
			promotion.setNeedItemsFrom(needItemsFrom);
		} else {
			promotion.setNeedItemsFrom("");			
		}
		String excludeSkuPrefix = rs.getString("EXCLUDESKUPREFIX");
		if (!rs.wasNull()) {
			promotion.setExcludeSkuPrefix(excludeSkuPrefix);
		} else {
			promotion.setExcludeSkuPrefix("");
			
		}
		String needBrands = rs.getString("NEEDBRANDS");
		if (!rs.wasNull()) {
			promotion.setNeedBrands(needBrands);
		} else {
			promotion.setNeedBrands("");			
		}
		String excludeBrands = rs.getString("EXCLUDEBRANDS");
		if (!rs.wasNull()) {
			promotion.setExcludeBrands(excludeBrands);
		} else {
			promotion.setExcludeBrands("");			
		}
		double minSubtotal = rs.getDouble("MIN_SUBTOTAL");
		if (!rs.wasNull()) {
			promotion.setMinSubtotal(String.valueOf(minSubtotal));
		} else {
			promotion.setMinSubtotal("");
		}
		double maxAmount = rs.getDouble("MAX_AMOUNT");
		if (!rs.wasNull()) {
			promotion.setMaxAmount(FormatterUtil.formatToTwoDecimal(maxAmount));
		} else {
			promotion.setMaxAmount("");			
		}
		double percentOff = rs.getDouble("PERCENT_OFF")*100;
		if (!rs.wasNull()) {
			promotion.setPercentOff(String.valueOf((int)percentOff));
		} else {
			promotion.setPercentOff("");			
		}
		String waiveChargeType = rs.getString("WAIVE_CHARGE_TYPE");
		if (!rs.wasNull()) {
			promotion.setWaiveChargeType(waiveChargeType);
		} else {
			promotion.setWaiveChargeType("");			
		}
		String categoryName = rs.getString("CATEGORY_NAME");
		if (!rs.wasNull()) {
			promotion.setCategoryName(categoryName);
		} else {
			promotion.setCategoryName("");			
		}
		String productName = rs.getString("PRODUCT_NAME");
		if (!rs.wasNull()) {
			promotion.setProductName(productName);
		} else {
			promotion.setProductName("");			
		}
		if (!"".equals(promotion.getCategoryName()) && !"".equals(promotion.getProductName())) {
			promotion.setValueType("sample");
		} else if (!"".equals(promotion.getPromotionType()) && "LINE_ITEM".equalsIgnoreCase(promotion.getPromotionType()) ) {
			promotion.setValueType("lineitem");
		} else if (!"".equals(promotion.getMaxAmount())) {
			promotion.setValueType("discount");
		} else if (!"".equals(promotion.getPercentOff())) {
			promotion.setValueType("percentOff");
		} else if (!"".equals(promotion.getWaiveChargeType())) {
			promotion.setValueType("waiveCharge");
		} else {
			promotion.setValueType("");						
		}
		promotion.setRuleBasePromotion("X".equalsIgnoreCase(rs.getString("RULE_BASED")));
		
		promotion.setRefProgCampaignCode(rs.getString("REF_PROG_CAMPAIGN_CODE"));

		promotion.setIsMaxUsagePerCustomer("X".equalsIgnoreCase(rs.getString("IS_MAX_USAGE_PER_CUST")));

		int expirationDays = rs.getInt("ROLLING_EXPIRATION_DAYS");
		if (!rs.wasNull()) {
			promotion.setRollingExpirationDays(new Integer(expirationDays));
		}

		promotion.setUniqueUse("X".equalsIgnoreCase(rs.getString("UNIQUE_USE")));

		String notes = rs.getString("NOTES");
		if(!rs.wasNull()){
			promotion.setNotes(notes);
		} else {
			promotion.setNotes("");
		}
		
		promotion.setActive("X".equalsIgnoreCase(rs.getString("ACTIVE")));
		promotion.setApplyFraud((!"X".equalsIgnoreCase(rs.getString("DONOT_APPLY_FRAUD"))));
		promotion.setModifyDate(rs.getTimestamp("MODIFY_DATE"));
		
		promotion.setProfileOperator(rs.getString("PROFILE_OPERATOR"));
		
		String modifiedBy = rs.getString("MODIFIED_BY");
		if(!rs.wasNull()){
			promotion.setModifiedBy(modifiedBy);
		} else {
			promotion.setModifiedBy("");
		}
		
		
		promotion.setAllowHeaderDiscount("X".equalsIgnoreCase(rs.getString("allow_header_discount")));
		promotion.setRecommendedItemsOnly("X".equalsIgnoreCase(rs.getString("recommended_items_only")));
		promotion.setMaxItemCount(rs.getInt("max_item_count"));
		
		return promotion;
	}
	
	public static void storePromotion(Connection conn,  ModelI model) throws SQLException {
		FDPromotionModel promotion = (FDPromotionModel) model;
		PreparedStatement ps =
			conn.prepareStatement(
				"UPDATE CUST.PROMOTION"
				+ " SET"
				+ " CODE = ?, NAME = ?, DESCRIPTION = ?, MAX_USAGE = ?, START_DATE = ?, EXPIRATION_DATE = ?, REDEMPTION_CODE = ?,"
				+ " CAMPAIGN_CODE = ?, MIN_SUBTOTAL = ?, MAX_AMOUNT = ?, PERCENT_OFF = ?, WAIVE_CHARGE_TYPE = ?, UNIQUE_USE = ?, CATEGORY_NAME = ?, PRODUCT_NAME = ?,"
				+ " ORDERTYPE_HOME = ?, ORDERTYPE_PICKUP = ?, ORDERTYPE_DEPOT = ?, ORDERTYPE_CORPORATE = ?, NEEDDRYGOODS = ?,"
				+ " ORDERCOUNT = ?, NEEDITEMSFROM = ?, EXCLUDESKUPREFIX = ?, NEEDBRANDS = ?, EXCLUDEBRANDS = ?, RULE_BASED = ?, REF_PROG_CAMPAIGN_CODE = ?, IS_MAX_USAGE_PER_CUST = ?,"
				+ " ROLLING_EXPIRATION_DAYS = ?, NOTES = ?, ACTIVE = ?, MODIFY_DATE = SYSDATE, MODIFIED_BY = ?, DONOT_APPLY_FRAUD = ? , PROFILE_OPERATOR = ?, recommended_items_only=?, allow_header_discount=?, max_item_count=?  "
				+ " WHERE ID = ?");
		int i = 1;
		i = setupPreparedStatement(ps, promotion, i);
		ps.setString(i++, promotion.getNotes());

		if(promotion.isActive()){
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}
		ps.setString(i++, promotion.getModifiedBy());
		if(!promotion.isApplyFraud()){
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}
		
		if(promotion.getProfileOperator() != null && 
				promotion.getAttributeList() != null && promotion.getAttributeList().size() > 1){
			ps.setString(i++, promotion.getProfileOperator());
		}else{
			ps.setNull(i++, Types.VARCHAR);
		}
		
		if(promotion.isRecommendedItemsOnly()){
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}
		
		if(promotion.isAllowHeaderDiscount()){
			ps.setString(i++, "X");
		} else {
			ps.setNull(i++, Types.VARCHAR);
		}
		
		if (promotion.getMaxItemCount()>0) {
			ps.setInt(i++,promotion.getMaxItemCount());
		} else {
			ps.setNull(i++, Types.INTEGER);					
		}
		
		ps.setString(i++, promotion.getPK().getId());
		if (ps.executeUpdate() != 1) {
			ps.close();
			throw new SQLException("row not created to update Promotion");
		}
		ps.close();
		
		storeAssignedCustomers(conn, promotion.getPK().getId(), promotion.getTmpAssignedCustomerUserIds());
		if(!promotion.getZipRestrictions().isEmpty()){
			storeGeography(conn, promotion.getPK().getId(), promotion.getZipRestrictions());
		}
		
		storeAssignedGroups(conn, promotion.getPK().getId(), promotion);
		storeAttributeList(conn, promotion.getPK().getId(), promotion.getAttributeList());
		
	}

	public static void removePromotion (Connection conn, PrimaryKey pk) throws SQLException {
		
		removeAssignedCustomerIds(conn, pk.getId());
		
		removeAssignedGroups(conn, pk.getId());
		
		removeAttributeList(conn, pk.getId());

		PreparedStatement ps =
			conn.prepareStatement("DELETE CUST.PROMOTION WHERE ID = ?");
		ps.setString(1, pk.getId());
		ps.executeUpdate();
		ps.close();
		
	}

	public static void storeAssignedCustomers(Connection conn, String promotionId, String assignedCustomerUserIds) throws SQLException {
		
		Map<String,AssignedCustomerParam> assignedCustomerStrategyParams = loadAssignedCustomerParams(conn, promotionId);
		PreparedStatement ps =
			conn.prepareStatement("INSERT INTO CUST.PROMO_CUSTOMER (PROMOTION_ID, CUSTOMER_ID, USAGE_CNT, EXPIRATION_DATE) VALUES(?,?,?,?)");
		PreparedStatement ps1 =
			conn.prepareStatement("INSERT INTO CUST.PROMO_CUSTOMER (PROMOTION_ID, CUSTOMER_EMAIL, USAGE_CNT, EXPIRATION_DATE) VALUES(?,?,?,?)");
		
		FDPromotionModel promotion = getPromotion(conn, promotionId);
		Calendar calendar = Calendar.getInstance();
		Calendar calendar1 = Calendar.getInstance();
		calendar.setTime(new java.util.Date());
		calendar1.setTime(promotion.getExpirationDate());
		
		// for saftey
		
		if(assignedCustomerUserIds != null) {
			String aci[] = StringUtil.decodeStrings(assignedCustomerUserIds);
			List cust_ids = new ArrayList(Arrays.asList(aci));
			removeExistingMappings(conn, cust_ids, promotionId);
			Iterator iter = cust_ids.iterator();
			while(iter.hasNext()) {
				String cust_email = (String) iter.next();
				String cust_id = getCustomerID(conn, cust_email);
				if(cust_id != null) {
					//existing customer
					AssignedCustomerParam param = (AssignedCustomerParam)assignedCustomerStrategyParams.get(cust_id);					
					ps.setString(1, promotionId);				
					ps.setString(2, cust_id);
					if (param != null && param.getUsageCount() != null) {
						ps.setInt(3, param.getUsageCount().intValue());					
					} else {
						ps.setNull(3, Types.INTEGER);										
					}
					if (param != null && param.getExpirationDate() != null) {
						ps.setDate(4, new Date(param.getExpirationDate().getTime()));					
					} else {
						ps.setNull(4, Types.DATE);										
					}
					ps.addBatch();
				} else {
					//prospect customer
					ps1.setString(1, promotion.getId());				
					ps1.setString(2, cust_email);
					ps1.setNull(3, Types.INTEGER);										
					
					if(promotion.getExpirationDate()!= null && null != promotion.getRollingExpirationDays() && promotion.getRollingExpirationDays() > 0){				
						if(calendar.before(calendar1) || (!calendar.before(calendar1) && !calendar1.before(calendar))){
							ps1.setDate(4, new Date(calendar.getTimeInMillis()));
						}else{
							ps1.setDate(4, new Date(calendar1.getTimeInMillis()));
						}
					}else{
						ps1.setNull(4, Types.DATE);
					}
					ps1.addBatch();
				}
			}
			ps.executeBatch();
			ps1.executeBatch();
			ps.close();
			ps1.close();
		}
		
		/*	
		if (assignedCustomerUserIds != null) {
			String aci[] = StringUtil.decodeStrings(assignedCustomerUserIds); 
			List<String> custIdList = getAssignedCustomerIds(conn, aci);
			if (custIdList == null) return;
			removeDuplicateCustomerIds(conn, promotionId, custIdList);
			PreparedStatement ps =
				conn.prepareStatement("INSERT INTO CUST.PROMO_CUSTOMER (PROMOTION_ID, CUSTOMER_ID, USAGE_CNT, EXPIRATION_DATE) VALUES(?,?,?,?)");
			Iterator<String> iter = custIdList.iterator();
			while (iter.hasNext()) {
				String customerId = iter.next();
				AssignedCustomerParam param = (AssignedCustomerParam)assignedCustomerStrategyParams.get(customerId);
				ps.setString(1, promotionId);				
				ps.setString(2, customerId);
				if (param != null && param.getUsageCount() != null) {
					ps.setInt(3, param.getUsageCount().intValue());					
				} else {
					ps.setNull(3, Types.INTEGER);										
				}
				if (param != null && param.getExpirationDate() != null) {
					ps.setDate(4, new Date(param.getExpirationDate().getTime()));					
				} else {
					ps.setNull(4, Types.DATE);										
				}
				if (ps.executeUpdate() == -1) {
					ps.close();
					throw new SQLException("row not stored");					
				}
			}
			ps.close();
		}
		*/
	}
	
	public static void storeAssignedCustomers(Connection conn, FDPromotionNewModel promotion, String assignedCustomerUserIds) throws SQLException {
		
//		Map<String,AssignedCustomerParam> assignedCustomerStrategyParams = loadAssignedCustomerParams(conn, promotion.getId());
		
		// for saftey
		
		if (assignedCustomerUserIds != null) {
			String aci[] = StringUtil.decodeStrings(assignedCustomerUserIds); 
			List cust_ids = new ArrayList(Arrays.asList(aci));
			removeExistingMappings(conn, cust_ids, promotion.getId());
			List duplCustList = getDuplicateCustomerIds(conn, promotion.getId(), cust_ids);
			PreparedStatement ps =
				conn.prepareStatement("INSERT INTO CUST.PROMO_CUSTOMER (PROMOTION_ID, CUSTOMER_ID, USAGE_CNT, EXPIRATION_DATE) VALUES(?,?,?,?)");
			PreparedStatement ps1 =
				conn.prepareStatement(UPDATE_PROMO_CUSTOMER_QUERY);
			PreparedStatement ps2 =
				conn.prepareStatement("INSERT INTO CUST.PROMO_CUSTOMER (PROMOTION_ID, CUSTOMER_EMAIL, USAGE_CNT, EXPIRATION_DATE) VALUES(?,?,?,?)");
			PreparedStatement ps3 = 
				conn.prepareStatement("UPDATE cust.promo_customer SET usage_cnt = ?, expiration_date = ? WHERE promotion_id = ? AND lower(customer_email) = lower(?)");
			
			Iterator<String> iter = cust_ids.iterator();
			Calendar calendar = Calendar.getInstance();
			Calendar calendar1 = Calendar.getInstance();
			calendar.setTime(new java.util.Date());
			calendar1.setTime(promotion.getExpirationDate());			
			if(null != promotion.getRollingExpirationDays() && promotion.getRollingExpirationDays() > 0){
				calendar.add(Calendar.DATE, promotion.getRollingExpirationDays());
			}else{
				calendar.setTime(promotion.getExpirationDate());
			}
			while (iter.hasNext()) {
				String cust_email = iter.next();
				String cust_id = getCustomerID(conn, cust_email);				
				if(!duplCustList.contains(cust_email)){
					if(cust_id != null) {
		//				AssignedCustomerParam param = (AssignedCustomerParam)assignedCustomerStrategyParams.get(customerId);
						ps.setString(1, promotion.getId());				
						ps.setString(2, cust_id);
						ps.setNull(3, Types.INTEGER);										
						
						if(promotion.getExpirationDate()!= null && null != promotion.getRollingExpirationDays() && promotion.getRollingExpirationDays() > 0){				
							if(calendar.before(calendar1) || (!calendar.before(calendar1) && !calendar1.before(calendar))){
								ps.setDate(4, new Date(calendar.getTimeInMillis()));
							}else{
								ps.setDate(4, new Date(calendar1.getTimeInMillis()));
							}
						}else{
							ps.setNull(4, Types.DATE);
						}
						ps.addBatch();
					} else {
						//prospect customer
						ps2.setString(1, promotion.getId());				
						ps2.setString(2, cust_email);
						ps2.setNull(3, Types.INTEGER);										
						
						if(promotion.getExpirationDate()!= null && null != promotion.getRollingExpirationDays() && promotion.getRollingExpirationDays() > 0){				
							if(calendar.before(calendar1) || (!calendar.before(calendar1) && !calendar1.before(calendar))){
								ps2.setDate(4, new Date(calendar.getTimeInMillis()));
							}else{
								ps2.setDate(4, new Date(calendar1.getTimeInMillis()));
							}
						}else{
							ps2.setNull(4, Types.DATE);
						}
						ps2.addBatch();
					}
				} else{
					if(cust_id != null) {
						ps1.setNull(1, Types.INTEGER);
						if(promotion.getExpirationDate()!= null && null != promotion.getRollingExpirationDays() && promotion.getRollingExpirationDays() > 0){
							if(calendar.before(calendar1) || (!calendar.before(calendar1) && !calendar1.before(calendar))){
								ps1.setDate(2, new Date(calendar.getTimeInMillis()));
							}else{
								ps1.setDate(2, new Date(calendar1.getTimeInMillis()));
							}
						}else{
							ps1.setNull(2, Types.DATE);
						}
						ps1.setString(3, promotion.getId());				
						ps1.setString(4, cust_id);
						ps1.addBatch();
					} else {
						//prospect customer
						ps3.setNull(1, Types.INTEGER);
						if(promotion.getExpirationDate()!= null && null != promotion.getRollingExpirationDays() && promotion.getRollingExpirationDays() > 0){
							if(calendar.before(calendar1) || (!calendar.before(calendar1) && !calendar1.before(calendar))){
								ps3.setDate(2, new Date(calendar.getTimeInMillis()));
							}else{
								ps3.setDate(2, new Date(calendar1.getTimeInMillis()));
							}
						}else{
							ps3.setNull(2, Types.DATE);
						}
						ps3.setString(3, promotion.getId());				
						ps3.setString(4, cust_email);
						ps3.addBatch();
					}
				}
			}
			ps.executeBatch();
			ps1.executeBatch();
			ps2.executeBatch();
			ps3.executeBatch();
			ps.close();
			ps1.close();
			ps2.close();
			ps3.close();
		}
		
		/*		
		if (assignedCustomerUserIds != null) {
			String aci[] = StringUtil.decodeStrings(assignedCustomerUserIds); 
			List<String> custIdList = getAssignedCustomerIds(conn, aci);
			if (custIdList == null) return;
//			removeDuplicateCustomerIds(conn, promotion.getId(), custIdList);
			List duplCustList = getDuplicateCustomerIds(conn, promotion.getId(), custIdList);
			PreparedStatement ps =
				conn.prepareStatement("INSERT INTO CUST.PROMO_CUSTOMER (PROMOTION_ID, CUSTOMER_ID, USAGE_CNT, EXPIRATION_DATE) VALUES(?,?,?,?)");
			PreparedStatement ps1 =
				conn.prepareStatement(UPDATE_PROMO_CUSTOMER_QUERY);
			Iterator<String> iter = custIdList.iterator();
			Calendar calendar = Calendar.getInstance();
			Calendar calendar1 = Calendar.getInstance();
			calendar.setTime(new java.util.Date());
			calendar1.setTime(promotion.getExpirationDate());			
			if(null != promotion.getRollingExpirationDays() && promotion.getRollingExpirationDays() > 0){
				calendar.add(Calendar.DATE, promotion.getRollingExpirationDays());
			}else{
				calendar.setTime(promotion.getExpirationDate());
			}
			while (iter.hasNext()) {
				String customerId = iter.next();
				if(!duplCustList.contains(customerId)){
	//				AssignedCustomerParam param = (AssignedCustomerParam)assignedCustomerStrategyParams.get(customerId);
					ps.setString(1, promotion.getId());				
					ps.setString(2, customerId);
					ps.setNull(3, Types.INTEGER);										
					
					if(promotion.getExpirationDate()!= null && null != promotion.getRollingExpirationDays() && promotion.getRollingExpirationDays() > 0){				
						if(calendar.before(calendar1) || (!calendar.before(calendar1) && !calendar1.before(calendar))){
							ps.setDate(4, new Date(calendar.getTimeInMillis()));
						}else{
							ps.setDate(4, new Date(calendar1.getTimeInMillis()));
						}
					}else{
						ps.setNull(4, Types.DATE);
					}
					ps.addBatch();
					/*if (ps.executeUpdate() == -1) {
						ps.close();
						throw new SQLException("row not stored");					
					}*//*
				}else{
					ps1.setNull(1, Types.INTEGER);
					if(promotion.getExpirationDate()!= null && null != promotion.getRollingExpirationDays() && promotion.getRollingExpirationDays() > 0){
						if(calendar.before(calendar1) || (!calendar.before(calendar1) && !calendar1.before(calendar))){
							ps1.setDate(2, new Date(calendar.getTimeInMillis()));
						}else{
							ps1.setDate(2, new Date(calendar1.getTimeInMillis()));
						}
					}else{
						ps1.setNull(2, Types.DATE);
					}
					ps1.setString(3, promotion.getId());				
					ps1.setString(4, customerId);
					ps1.addBatch();
				}
			}
			ps.executeBatch();
			ps1.executeBatch();
			ps.close();
			ps1.close();
		}
		*/
	}
	
	public static String getCustomerID(Connection conn, String cust_email) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {			
			ps = conn.prepareStatement("select ID from cust.customer where lower(user_id) = lower(?)");
			ps.setString(1, cust_email);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
		} finally {
			if (ps != null) ps.close();			
		}
		return null;
	}
	
	public static final String GET_RESTRICTED_CUSTOMERS="select customer_id from CUST.PROMO_CUSTOMER where promotion_id=? and customer_id in ('";
	
	public static void removeDuplicateCustomerIds(Connection conn, String promotionId, List<String> custIdList) throws SQLException {
		final StringBuffer customerIdStr=new StringBuffer();
		final StringBuffer sql=new StringBuffer(GET_RESTRICTED_CUSTOMERS);
		Iterator<String> iterator=custIdList.iterator();
		int i=0;
        while(iterator.hasNext()){
        	String customerId=(String)iterator.next();        	
        	i=i+1;
        	if(i==1){
        		customerIdStr.append(customerId);
				if(i!=custIdList.size()) customerIdStr.append(customerId).append("',"); 
			}
			else{
				if(i==custIdList.size())	
				{
					customerIdStr.append("'").append(customerId);					
				}
				else{
					customerIdStr.append("'").append(customerId).append("',");					
				}				
			}        	        	        	
        }
        sql.append(customerIdStr.toString()).append("')");
        System.out.println(sql.toString());
        PreparedStatement ps =
			conn.prepareStatement(sql.toString());
        ps.setString(1, promotionId);
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
        	String existingId=(String)rs.getString("customer_id");
        	custIdList.remove(existingId);
        }		
	}
	
	public static List getDuplicateCustomerIds(Connection conn, String promotionId, List<String> custIdList) throws SQLException {
		List<String> duplicateCustList = new ArrayList<String>();
		final StringBuffer customerIdStr=new StringBuffer();
		final StringBuffer sql=new StringBuffer("select pc.customer_id, c.user_id from CUST.PROMO_CUSTOMER pc, cust.customer c where pc.promotion_id=? and pc.customer_id = c.id " +
				"and c.user_id in ('");
		Iterator<String> iterator=custIdList.iterator();
		int i=0;
        while(iterator.hasNext()){
        	String customerId=(String)iterator.next();        	
        	i=i+1;
        	if(i==1){
        		customerIdStr.append(customerId);
				if(i!=custIdList.size()) customerIdStr.append("',"); 
			}
			else{
				if(i==custIdList.size())	
				{
					customerIdStr.append("'").append(customerId);					
				}
				else{
					customerIdStr.append("'").append(customerId).append("',");					
				}				
			}        	        	        	
        }
        sql.append(customerIdStr.toString()).append("')");
        System.out.println(sql.toString());
        PreparedStatement ps =
			conn.prepareStatement(sql.toString());
        ps.setString(1, promotionId);
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
        	String existingId=(String)rs.getString("user_id");
//        	custIdList.remove(existingId);
        	duplicateCustList.add(existingId);
        }
        
        //get duplicate prospect users as well
        StringBuffer sb2 = new StringBuffer("select pc.customer_email from CUST.PROMO_CUSTOMER pc where pc.promotion_id=? and pc.customer_email in ('"); 
        sb2.append(customerIdStr.toString()).append("')");
        System.out.println(sb2.toString());
        PreparedStatement ps1 = conn.prepareStatement(sb2.toString());
        ps1.setString(1, promotionId);
        rs = ps1.executeQuery();
        while(rs.next()){
        	String existingId=(String)rs.getString("customer_email");
        	duplicateCustList.add(existingId);
        }
        
        return duplicateCustList;
	}
	
	public static final String GET_RESTRICTED_CUSTOMERS_BYEMAIL="select pc.customer_id from CUST.PROMO_CUSTOMER pc, cust.customer c where pc.promotion_id=? and pc.customer_id = c.id " +
				"and c.user_id in ('";
	
	public static void removeExistingMappings(Connection conn, List<String> custIdList, String promotionId) throws SQLException {
		final StringBuffer customerIdStr=new StringBuffer();
		final StringBuffer sql=new StringBuffer(GET_RESTRICTED_CUSTOMERS_BYEMAIL);
		Iterator<String> iterator=custIdList.iterator();
		int i=0;
        while(iterator.hasNext()){
        	String customerId=(String)iterator.next().toLowerCase();        	
        	i=i+1;
        	if(i==1){
        		customerIdStr.append(customerId);
				if(i!=custIdList.size()) customerIdStr.append(customerId).append("',"); 
			}
			else{
				if(i==custIdList.size())	
				{
					customerIdStr.append("'").append(customerId);					
				}
				else{
					customerIdStr.append("'").append(customerId).append("',");					
				}				
			}        	        	        	
        }
        sql.append(customerIdStr.toString()).append("')");
        System.out.println(sql.toString());
        PreparedStatement ps =
			conn.prepareStatement(sql.toString());
        ps.setString(1, promotionId);
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
        	String existingId=(String)rs.getString("customer_id");
        	custIdList.remove(existingId);
        }		
	}
	
	private static String INSERT_PROMO_GROUP =  	"INSERT INTO cust.promo_dcpd_data" +
															" (id, promotion_id, content_type, content_id)" +
																	" VALUES(?,?,?,?)";

	private static void storeAssignedGroups(Connection conn, String id, FDPromotionModel promotion) throws SQLException {		
		PreparedStatement ps = null;
		
		removeAssignedGroups(conn, id);
		try {			
			ps = conn.prepareStatement(INSERT_PROMO_GROUP);			
			prepareAssignedGroupSave(conn, ps, StringUtil.decodeStrings(promotion.getAssignedDepartments()), 
											id, EnumDCPDContentType.DEPARTMENT.getName());
			prepareAssignedGroupSave(conn, ps, StringUtil.decodeStrings(promotion.getAssignedCategories()), 
											id, EnumDCPDContentType.CATEGORY.getName());
			prepareAssignedGroupSave(conn, ps, StringUtil.decodeStrings(promotion.getAssignedRecipes()), 
											id, EnumDCPDContentType.RECIPE.getName());
			ps.executeBatch();
		} finally {
			if (ps != null) ps.close();			
		}	
	}
	
	private static void prepareAssignedGroupSave(Connection conn,PreparedStatement ps, String[] dataList
						, String promotionId, String grpType) throws SQLException  {
				
		if(dataList != null) {
			int intLength = dataList.length;
			int intCount = 0;
			for(;intCount<intLength; intCount++) {
				int index = 1;				
				ps.setString(index++, SequenceGenerator.getNextId(conn, "CUST"));
				ps.setString(index++, promotionId);	
				ps.setString(index++, grpType);
				ps.setString(index++, dataList[intCount]);
				ps.addBatch();
			}
		}
	}
	
	private static String INSERT_PROMO_ATTR =  	"INSERT INTO cust.promo_attr" +
							" (id, promotion_id, promo_attr_name, attr_value, attr_index)" +
									" VALUES(?,?,?,?,?)";

	private static void storeAttributeList(Connection conn, String promotionId, List<FDPromotionAttributeParam> attrList) throws SQLException {		
		PreparedStatement ps = null;
		removeAttributeList(conn, promotionId);
		try {			
			ps = conn.prepareStatement(INSERT_PROMO_ATTR);
			if(attrList != null) {
				Iterator<FDPromotionAttributeParam> tmpiterator = attrList.iterator();
				FDPromotionAttributeParam tmpParam = null;
				int indexAttr = 1;
				while(tmpiterator.hasNext()) {
					int index = 1;
					tmpParam = (FDPromotionAttributeParam)tmpiterator.next();
					ps.setString(index++, SequenceGenerator.getNextId(conn, "CUST"));
					ps.setString(index++, promotionId);	
					ps.setString(index++, tmpParam.getAttributeName());
					ps.setString(index++, tmpParam.getDesiredValue());					
					ps.setInt(index++, indexAttr++);
					ps.addBatch();
				}
			}
			ps.executeBatch();
		} finally {
			if (ps != null) ps.close();			
		}	
	}

	protected static List<String> getAssignedCustomerIds(Connection conn, String assignedCustomerUserIds[]) throws SQLException {
		
		List<String> custUserIdList = null;
		String invalidCustomerIds = "";
		
		if (assignedCustomerUserIds != null && assignedCustomerUserIds.length > 0) {
			custUserIdList = new ArrayList<String>();			
			PreparedStatement ps = conn.prepareStatement("SELECT ID FROM CUST.CUSTOMER WHERE USER_ID = LOWER(?)");
			for (int i = 0; i < assignedCustomerUserIds.length; i++) {
				ps.setString(1, assignedCustomerUserIds[i]);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {			
					custUserIdList.add(rs.getString(1));
				} else {
					if (!"".equals(invalidCustomerIds)) invalidCustomerIds += ","; 
					invalidCustomerIds += assignedCustomerUserIds[i];
				}
				rs.close();
			}
			ps.close();
			
			if (!"".equals(invalidCustomerIds)) {
				throw new SQLException("Invalid customer id(s): " + invalidCustomerIds);
			}
		}
		
		return custUserIdList;
	}
	
	protected static void removeAssignedCustomerIds(Connection conn, String promotionId) throws SQLException {
		PreparedStatement ps =	conn.prepareStatement("DELETE CUST.PROMO_CUSTOMER WHERE PROMOTION_ID = ?");
		ps.setString(1, promotionId);
		ps.executeUpdate();		
		ps.close();
	}
	
	protected static void removeAssignedGroups(Connection conn, String promotionId) throws SQLException {
		PreparedStatement ps =	conn.prepareStatement("DELETE CUST.PROMO_DCPD_DATA WHERE PROMOTION_ID = ?");
		ps.setString(1, promotionId);
		ps.executeUpdate();		
		ps.close();
	}
	
	protected static void removeAttributeList(Connection conn, String promotionId) throws SQLException {
		PreparedStatement ps =	conn.prepareStatement("DELETE CUST.PROMO_ATTR WHERE PROMOTION_ID = ?");
		ps.setString(1, promotionId);
		ps.executeUpdate();		
		ps.close();
	}
	
	

	private static TreeMap<java.util.Date,FDPromoZipRestriction> loadZipRestrictions(Connection conn, String promotionId) throws SQLException{
		TreeMap<java.util.Date,FDPromoZipRestriction> map = new TreeMap<java.util.Date,FDPromoZipRestriction>();
		List<java.sql.Date> dateList = loadDatesByZipRestriction(conn, promotionId);
		if(!dateList.isEmpty()){
			for(Iterator<java.sql.Date> i=dateList.iterator(); i.hasNext();){
				FDPromoZipRestriction zipRestriction = new FDPromoZipRestriction();
				java.util.Date curDate = (java.util.Date)i.next();
				zipRestriction.setStartDate(curDate);
				zipRestriction.setType(loadZipRestrictionByDate(conn, promotionId, (Date)curDate));
				
				if(zipRestriction.getType().equals("SUSPENDED")){
					zipRestriction.setType("EXCEPT");
					zipRestriction.setZipCodes("ALL");
				}
				else if(zipRestriction.getType().equals("ALL")){
					zipRestriction.setType("ONLY");
					zipRestriction.setZipCodes("ALL");
				}
				else {
					zipRestriction.setZipCodes(NVL.apply(StringUtil.encodeString(loadZipCodesByDate(conn, promotionId, (Date)curDate, zipRestriction.getType())), ""));
				}
				map.put(curDate, zipRestriction);
			}
		}

		return map;
	}	
	
	private final static String zipRestrictionDates = 
		"SELECT START_DATE FROM CUST.PROMO_GEOGRAPHY WHERE PROMOTION_ID = ? ORDER BY START_DATE DESC";
	
	protected static List<java.sql.Date> loadDatesByZipRestriction (Connection conn, String promotionId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(zipRestrictionDates);
		ps.setString(1, promotionId);
		ResultSet rs = ps.executeQuery();
		
		List<java.sql.Date> list = new ArrayList<java.sql.Date>();
		
		while(rs.next()){
			list.add(rs.getDate("START_DATE"));
		}
		rs.close();
		ps.close();
		return list;
	}
	
	protected static String loadZipRestrictionByDate(Connection conn, String promotionId, Date date) throws SQLException {
		String query = "SELECT CODE, SIGN FROM " +
							" CUST.PROMO_GEOGRAPHY_DATA PGD, CUST.PROMO_GEOGRAPHY PG " +
							" WHERE PGD.GEOGRAPHY_ID = PG.ID AND PG.START_DATE = ? AND " +
							" PG.PROMOTION_ID = ? AND TYPE = 'Z' ORDER BY CODE DESC";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement(query);
			String restriction = "";
			ps.setDate(1, date);
			ps.setString(2, promotionId);
			
			rs = ps.executeQuery();
			while(rs.next()){
				if(rs.getString("SIGN").equals("S") && rs.getString("CODE").equals("ALL")){
					restriction = "ONLY";
				}
				else if(rs.getString("SIGN").equals("A") && rs.getString("CODE").equals("ALL")){
					restriction = "EXCEPT";
				}
				else if (rs.getString("SIGN").equals("A") && restriction.equals("ONLY")){
					return "ONLY";
				}
				else if(rs.getString("SIGN").equals("A")){
					return "ONLY";
				}
				else if(rs.getString("SIGN").equals("S") && restriction.equals("EXCEPT")){
					return "EXCEPT";
				}
			}
			if(restriction.equals("ONLY")){
				return "SUSPENDED";
			}
			else { //if(restriction.equals("EXCEPT")){
				return "ALL";
			}
		}
		finally{
			if (rs != null) {
				rs.close();
			}
			if (ps != null){
				ps.close();	
			}
		}
	}
	
	protected static void removeGeographyData(Connection conn, String promotionId) throws SQLException{
		PreparedStatement ps = null;
		try {	
			ps = conn.prepareStatement("DELETE FROM CUST.PROMO_GEOGRAPHY_DATA WHERE GEOGRAPHY_ID IN (SELECT ID FROM CUST.PROMO_GEOGRAPHY WHERE PROMOTION_ID=?)");
			ps.setString(1, promotionId);
			ps.executeUpdate();
			ps = null;
			ps =conn.prepareStatement("DELETE FROM CUST.PROMO_GEOGRAPHY WHERE PROMOTION_ID = ?");
			ps.setString(1, promotionId);
			ps.executeUpdate();
		} finally {
			if (ps != null){
				ps.close();			
			}
		}	
	}
	
	private final static String GEOGRAPHY_DATA_INSERT = "INSERT INTO CUST.PROMO_GEOGRAPHY_DATA (GEOGRAPHY_ID, TYPE, CODE, SIGN) " +
											" VALUES ( ? ,?, ?, ?)";
	private final static String GEOGRAPHY_INSERT = "INSERT INTO CUST.PROMO_GEOGRAPHY (ID, PROMOTION_ID, START_DATE) " +
											" VALUES (?,?,?)";
	
	private static void storeGeographyData(Connection conn, FDPromoZipRestriction zipRestriction, String geographyId, String promotionId) throws SQLException {
		String finalZipSign = "";
		String finalDepotSign = "";
		PreparedStatement ps = conn.prepareStatement(GEOGRAPHY_DATA_INSERT);
		try{
			String[] zipList = (NVL.apply(zipRestriction.getZipCodes(), "")).split("\\,");
			for(int x=0; x<zipList.length; x++){
				String[] a = getZipRestrictionSign(zipRestriction.getType(), zipRestriction.getZipCodes(), zipList[x]);
				ps.setString(1, geographyId);
				ps.setString(2, "Z");
				ps.setString(3, a[0]);
				ps.setString(4, a[1]);
				if(ps.executeUpdate()!=1){
					throw new SQLException("row not created to store Geography Data");
				}				
				finalZipSign = a[2];
				finalDepotSign=a[3];
			}		
			if(finalZipSign != ""){
				ps.setString(1, geographyId);
				ps.setString(2, "Z");
				ps.setString(3, "ALL");
				ps.setString(4, finalZipSign);
				if(ps.executeUpdate()!=1){
					throw new SQLException("row not created to store Geography Data");
				}
			}
			if(finalDepotSign !=""){
				ps.setString(1, geographyId);
				ps.setString(2, "D");
				ps.setString(3, "ALL");
				ps.setString(4, finalDepotSign);
				if(ps.executeUpdate()!=1){
					throw new SQLException("row not created to store Geography Data");
				}
			}
		}
		finally{
			if(ps!=null){
				ps.close();
			}
		}
	}
	
	/**
	 * 
	 * @param type
	 * @param zips
	 * @param currentZip
	 * @return String [] -> 
	 * 					0 = code
	 * 					1 = sign
	 * 					2 = final Zip sign
	 * 					3 = final Depot sign
	 */
	
	private static String[] getZipRestrictionSign (String type, String zips, String currentZip) {
		String[] a = new String[4];
		if("ONLY".equals(type) && "ALL".equals(zips)){
			a[0] = "ALL";
			a[1] = "A";
			a[2] = "";
			a[3] = "A";
		} else if("EXCEPT".equals(type) && "ALL".equals(zips)){
			a[0] = "ALL";
			a[1] =  "S";
			a[2] = "";
			a[3] = "S";
		} else if(type.equals("EXCEPT")){
			a[0] = currentZip;
			a[1] = "S";
			a[2] = "A";
			a[3] = "A";
		} else if(type.equals("ONLY")){
			a[0] = currentZip;
			a[1] = "A";
			a[2] = "S";
			a[3] = "S";
		}
		
		return a;
	}
	
	protected static void storeGeography(Connection conn, String promotionId, TreeMap<java.util.Date,FDPromoZipRestriction> zipMap) throws SQLException{
		removeGeographyData(conn, promotionId);
		PreparedStatement ps = null;
		try{
			ps =conn.prepareStatement(GEOGRAPHY_INSERT);
			for (Iterator<Map.Entry<java.util.Date,FDPromoZipRestriction>> i = zipMap.entrySet().iterator(); i.hasNext(); ){
				Map.Entry<java.util.Date,FDPromoZipRestriction> e = (Entry<java.util.Date,FDPromoZipRestriction>) i.next();				
				java.util.Date d = e.getKey();
				String id = SequenceGenerator.getNextId(conn, "CUST");
				ps.setString(1, id);
				ps.setString(2, promotionId);
				ps.setDate(3, new Date(d.getTime()));
				if(ps.executeUpdate()==1){
					storeGeographyData(conn, e.getValue(), id, promotionId);
				} else {
					throw new SQLException("row not created to store Geography Information");
				}
			}
		} finally {
			if(ps!=null){
				ps.close();
			}
		}
	}
	
	
	protected static List<String> loadZipCodesByDate(Connection conn, String promotionId, Date date, String restriction) throws SQLException {
		List<String> list = new ArrayList<String>();
		if(restriction.equals("ALL")){
			list.add("ALL");
			return list;
		}
		else {
			String query = "SELECT CODE, SIGN FROM " +
							" CUST.PROMO_GEOGRAPHY_DATA PGD, CUST.PROMO_GEOGRAPHY PG " +
							" WHERE PGD.GEOGRAPHY_ID = PG.ID AND PG.START_DATE = TO_DATE('"+date+"', 'YYYY-MM-DD') AND " +
							" PG.PROMOTION_ID = '"+promotionId+"' AND TYPE = 'Z' AND CODE !='ALL' ";
			PreparedStatement ps = conn.prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			int counter = 0;
			while(rs.next()){
				list.add(rs.getString("CODE"));
				counter++;
			}
			if(counter == 0){
				list.add("");
			}
			rs.close();
			ps.close();
			
			return list;
		}
	}
		
	
	private final static String assignedCustomerStrategyForPromoQuery =
		"select c.user_id, pc.customer_id "
			+ "from cust.promotion_new p, cust.promo_customer pc, cust.customer c "
			+ "where p.id=pc.promotion_id and pc.customer_id=c.id "
			+ "and pc.promotion_id = ?";

	/** @return List of assigned customer ids */
	protected static List<String> loadAssignedCustomerUserIds(Connection conn, String promotionId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(assignedCustomerStrategyForPromoQuery);
		ps.setString(1, promotionId);
		ResultSet rs = ps.executeQuery();

		// promotionPK -> AssignedCustomerStrategy 
		List<String> list = new ArrayList<String>();

		while (rs.next()) {
			list.add(rs.getString("USER_ID"));
		}

		rs.close();
		ps.close();

		return list;
	}
	
	private final static String assignedCustomerStrategyForPromoQuery1 =
		"select count(*) "
			+ "from cust.promotion_new p, cust.promo_customer pc " //, cust.customer c "
			+ "where p.id=pc.promotion_id " //and pc.customer_id=c.id "
			+ "and pc.promotion_id = ?";
	
	protected static int loadAssignedCustomerUserIds1(Connection conn, String promotionId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(assignedCustomerStrategyForPromoQuery1);
		ps.setString(1, promotionId);
		ResultSet rs = ps.executeQuery();
		int size = 0;

		if (rs.next()) {
			size = rs.getInt(1);
		}

		rs.close();
		ps.close();

		return size;
	}
	
	private final static String LOAD_PROMO_GROUP =
		"select id, promotion_id, content_type, content_id "
			+ "from cust.promo_dcpd_data pcpd "
			+ "where pcpd.promotion_id = ?";

	/** @return List of assigned dcpd content data */
	protected static Map<EnumDCPDContentType,List<String>> loadAssignedGroups(Connection conn, String promotionId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(LOAD_PROMO_GROUP);
		ps.setString(1, promotionId);
		ResultSet rs = ps.executeQuery();
		
		Map<EnumDCPDContentType,List<String>> groupMap = new HashMap<EnumDCPDContentType,List<String>>();
		groupMap.put(EnumDCPDContentType.DEPARTMENT, new ArrayList<String>());
		groupMap.put(EnumDCPDContentType.CATEGORY, new ArrayList<String>());
		groupMap.put(EnumDCPDContentType.RECIPE, new ArrayList<String>());
			
		String grpType = null;
		while (rs.next()) {
			grpType = rs.getString("content_type");
			List<String> tmpSet = (List<String>)groupMap.get(EnumDCPDContentType.getEnum(grpType));			
			if(tmpSet != null) {
				tmpSet.add(rs.getString("content_id"));
			}
		}

		rs.close();
		ps.close();

		return groupMap;
	}
	
	private final static String LOAD_PROMO_ATTR =
		"select id, promotion_id, promo_attr_name, attr_value,  attr_index "
			+ "from cust.PROMO_ATTR pa "
			+ "where pa.promotion_id = ? order by attr_index";

	/** @return List of assigned profile attribute list */
	protected static List<FDPromotionAttributeParam> loadAttributeList(Connection conn, String promotionId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(LOAD_PROMO_ATTR);
		ps.setString(1, promotionId);
		ResultSet rs = ps.executeQuery();
		
		List<FDPromotionAttributeParam> attrList = new ArrayList<FDPromotionAttributeParam>();
					
		FDPromotionAttributeParam tmpAttr = null;
		
		while (rs.next()) {
			tmpAttr = new FDPromotionAttributeParam();
			tmpAttr.setId(rs.getString("id"));
			tmpAttr.setAttributeName(rs.getString("promo_attr_name"));
			tmpAttr.setDesiredValue(rs.getString("attr_value"));			
			tmpAttr.setAttributeIndex(rs.getString("attr_index"));
			
			attrList.add(tmpAttr);
			
		}

		rs.close();
		ps.close();

		return attrList;
	}
	
	/** @return Map of assigned customer ids and the usage counts */
	protected static Map<String,AssignedCustomerParam> loadAssignedCustomerParams(Connection conn, String promotionId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT CUSTOMER_ID, USAGE_CNT, EXPIRATION_DATE FROM CUST.PROMO_CUSTOMER WHERE PROMOTION_ID=?");
		ps.setString(1, promotionId);
		ResultSet rs = ps.executeQuery();

		Map<String,AssignedCustomerParam> map = new HashMap<String,AssignedCustomerParam>();

		while (rs.next()) {
			String customerId = rs.getString("CUSTOMER_ID");
			int usageCnt = rs.getInt("USAGE_CNT");
			Integer usageCntInt = null;
			if (!rs.wasNull()) {  // only save if there's a usage count
				usageCntInt = new Integer(usageCnt);
			}
			java.util.Date expirationDate = rs.getDate("EXPIRATION_DATE");
			map.put(customerId, new AssignedCustomerParam(usageCntInt, expirationDate));
		}

		rs.close();
		ps.close();

		return map;
	}

	
	private static String PROMO_CUSTOMER_INFO_QUERY =  "SELECT" + 
														"	p.id AS promotion_id," + 
														"	p.description AS promotion_desc," + 
														"	NVL(p.is_max_usage_per_cust, ' ') AS is_max_usage_per_cust," + 
														"	NVL(p.rolling_expiration_days, 0) AS rolling_expiration_days," + 
														"	c.id AS customer_id," + 
														"	c.user_id," + 
														"	NVL(pc.usage_cnt, DECODE(p.is_max_usage_per_cust, 'X', NULL, p.max_usage)) AS usage_cnt," +
														"	p.max_usage AS promo_usage_cnt," +
														"	NVL(pc.expiration_date, DECODE(rolling_expiration_days, 0, p.expiration_date, NULL, p.expiration_date, pc.expiration_date)) AS expiration_date," +
														"	p.expiration_date AS promo_expiration_date," +
														"	(" +
														"		 SELECT COUNT(1)" + 
														"		 FROM cust.promotion_participation pp, cust.sale s" + 
														"		 WHERE pp.sale_id=s.id" + 
														"		 AND s.customer_id=c.id" +
														"		 AND pp.promotion_id=p.id" + 
														"	) AS num_used" +
														" FROM cust.promotion_new p, cust.promo_customer pc, cust.customer c" + 
														" WHERE p.id=pc.promotion_id" +
														" AND pc.customer_id=c.id";
		
		
	public static List<FDPromoCustomerInfo> getPromoCustomerInfoListFromPromotionId(Connection conn, PrimaryKey pk) throws SQLException {		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(PROMO_CUSTOMER_INFO_QUERY + " AND p.id = ?");
			ps.setString(1, pk.getId());
			rs = ps.executeQuery();	
			List<FDPromoCustomerInfo> list = new ArrayList<FDPromoCustomerInfo>();			
			while (rs.next()) {			
				FDPromoCustomerInfo pci = loadPromoCustomerInfoListResult(rs);
				pci.setIsLoadedFromPromotionId(true);
				list.add(pci);
			}
			return list;
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();			
		}
	}	
	
	public static List<FDPromoCustomerInfo> getPromoCustomerInfoListFromCustomerId(Connection conn, PrimaryKey pk) throws SQLException {		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(PROMO_CUSTOMER_INFO_QUERY + " AND c.id = ?");
			ps.setString(1, pk.getId());
			rs = ps.executeQuery();	
			List<FDPromoCustomerInfo> list = new ArrayList<FDPromoCustomerInfo>();			
			while (rs.next()) {			
				FDPromoCustomerInfo pci = loadPromoCustomerInfoListResult(rs);
				pci.setIsLoadedFromCustomerId(true);
				list.add(pci);
			}
			return list;
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();			
		}
	}

	private static FDPromoCustomerInfo loadPromoCustomerInfoListResult(ResultSet rs) throws SQLException {		
		FDPromoCustomerInfo pci = new FDPromoCustomerInfo();
		pci.setPromotionId(rs.getString("promotion_id"));
		pci.setPromotionDesc(rs.getString("promotion_desc"));
		pci.setIsMaxUsagePerCust("X".equalsIgnoreCase(rs.getString("is_max_usage_per_cust")));
		pci.setRollingExpirationDays(rs.getInt("rolling_expiration_days"));
		pci.setCustomerId(rs.getString("customer_id"));
		pci.setUserId(rs.getString("user_id"));
		pci.setUsageCount(rs.getInt("usage_cnt"));
		if (!rs.wasNull()) { 
			pci.setUsageCountStr(pci.getUsageCount()+"");
		} else {
			pci.setUsageCountStr("");			
		}
		pci.setPromoUsageCount(rs.getInt("promo_usage_cnt"));
		pci.setExpirationDate(rs.getTimestamp("expiration_date"));
		if (pci.getExpirationDate() != null) {
			Calendar cal = DateUtil.toCalendar(pci.getExpirationDate());
			EnumMonth expirationMonth = EnumMonth.getEnum(String.valueOf(cal.get(Calendar.MONTH)+1));
			if (expirationMonth != null) {
				pci.setExpirationMonth(expirationMonth.getDescription());
			}
			pci.setExpirationDay(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
			pci.setExpirationYear(String.valueOf(cal.get(Calendar.YEAR)));			
		}
		pci.setPromoExpirationDate(rs.getTimestamp("promo_expiration_date"));
		pci.setNumUsed(rs.getInt("num_used"));
		return pci;
	}

	private static String AVAILABLE_PROMOS_FOR_CUST_QUERY =  
		 "SELECT p.* FROM cust.promotion_new p" +
		 " WHERE p.id NOT IN"+ 
		 " ("+
		 " SELECT DISTINCT pc.promotion_id FROM cust.promo_customer pc" +
		 " WHERE pc.customer_id = ?" +
		 " )" +
		" AND p.expiration_date >= TO_DATE(TO_CHAR(SYSDATE, 'MM-DD-YYYY'), 'MM-DD-YYYY')";

	public static List<FDPromotionModel> getAvailablePromosForCustomer(Connection conn, PrimaryKey pk) throws SQLException {		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(AVAILABLE_PROMOS_FOR_CUST_QUERY);
			ps.setString(1, pk.getId());
			rs = ps.executeQuery();	
			List<FDPromotionModel> promoList = new ArrayList<FDPromotionModel>();
			
			while (rs.next()) {			
				FDPromotionModel promotion = loadPromotionResult(rs);
				List<String> assignedCustomerUserIds = FDPromotionManagerDAO.loadAssignedCustomerUserIds(conn, promotion.getId());
				if (assignedCustomerUserIds != null && assignedCustomerUserIds.size() > 0) {
					promotion.setAssignedCustomerUserIds(StringUtil.encodeString(assignedCustomerUserIds));
				} else {
					promotion.setAssignedCustomerUserIds("");				
				}
				promoList.add(promotion);
			}
			return promoList;
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();			
		}
	}


	private static String INSERT_PROMO_CUSTOMER_QUERY =  	"INSERT INTO cust.promo_customer" +
															" (promotion_id, customer_id, usage_cnt, expiration_date)" +
															" VALUES(?,?,?,?)";

	public static void insertPromoCustomers(Connection conn, List<FDPromoCustomerInfo> promoCustomers) throws SQLException {		
	PreparedStatement ps = null;
		try {			
			ps = conn.prepareStatement(INSERT_PROMO_CUSTOMER_QUERY);
			for (Iterator<FDPromoCustomerInfo> i = promoCustomers.iterator(); i.hasNext();) {
				FDPromoCustomerInfo oc = i.next();
				int index = 1;
				ps.setString(index++, oc.getPromotionId());
				ps.setString(index++, oc.getCustomerId());				
				if (oc.getUsageCountStr() != null && !"".equals(oc.getUsageCountStr())) {
					ps.setInt(index++, oc.getUsageCount());					
				} else {
					ps.setNull(index++, Types.INTEGER);										
				}
				if (oc.getExpirationDate() != null) {
					ps.setDate(index++, new java.sql.Date(oc.getExpirationDate().getTime()));
				} else {
					ps.setNull(index++, Types.DATE);					
				}
				ps.addBatch();
			}
			ps.executeBatch();
		} finally {
			if (ps != null) ps.close();			
		}	
	}

	private static String UPDATE_PROMO_CUSTOMER_QUERY =  "UPDATE cust.promo_customer" + 
	" SET usage_cnt = ?, expiration_date = ?" + 
	" WHERE promotion_id = ?" +
	" AND customer_id = ?";

	public static void updatePromoCustomers(Connection conn, List<FDPromoCustomerInfo> promoCustomers) throws SQLException {		
		PreparedStatement ps = null;
		try {			
			ps = conn.prepareStatement(UPDATE_PROMO_CUSTOMER_QUERY);
			for (Iterator<FDPromoCustomerInfo> i = promoCustomers.iterator(); i.hasNext();) {
				FDPromoCustomerInfo oc = (FDPromoCustomerInfo) i.next();
				int index = 1;
				if (oc.getUsageCountStr() != null && !"".equals(oc.getUsageCountStr())) {
					ps.setInt(index++, oc.getUsageCount());					
				} else {
					ps.setNull(index++, Types.INTEGER);										
				}
				if (oc.getExpirationDate() != null) {
					ps.setDate(index++, new java.sql.Date(oc.getExpirationDate().getTime()));
				} else {
					ps.setNull(index++, Types.DATE);					
				}
				ps.setString(index++, oc.getPromotionId());
				ps.setString(index++, oc.getCustomerId());				
				ps.addBatch();
			}
			ps.executeBatch();
		} finally {
			if (ps != null) ps.close();			
		}	
	}

	private static String REMOVE_PROMO_CUSTOMER_QUERY =  	"DELETE cust.promo_customer" +
															" WHERE promotion_id = ? AND customer_id=?";

	public static void removePromoCustomers(Connection conn, List<FDPromoCustomerInfo> promoCustomers) throws SQLException {		
		PreparedStatement ps = null;
		try {			
			ps = conn.prepareStatement(REMOVE_PROMO_CUSTOMER_QUERY);
			for (Iterator<FDPromoCustomerInfo> i = promoCustomers.iterator(); i.hasNext();) {
				FDPromoCustomerInfo oc = i.next();
				int index = 1;
				ps.setString(index++, oc.getPromotionId());
				ps.setString(index++, oc.getCustomerId());				
				ps.addBatch();
			}
			ps.executeBatch();
		} finally {
		if (ps != null) ps.close();			
		}	
	}
	
	
}
