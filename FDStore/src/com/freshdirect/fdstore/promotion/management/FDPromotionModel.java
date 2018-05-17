package com.freshdirect.fdstore.promotion.management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.freshdirect.fdstore.promotion.AssignedCustomerParam;
import com.freshdirect.fdstore.promotion.EnumPromotionStatus;
import com.freshdirect.fdstore.promotion.PromoVariantModel;
import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public class FDPromotionModel extends ModelSupport {
	private static final long serialVersionUID = -8981915060102783750L;

	private Set<String> assignedCustomerUserIds = new HashSet<String>();
	private TreeMap<Date,FDPromoZipRestriction> zipRestrictions = new TreeMap<Date,FDPromoZipRestriction>();
	private String zipValidationCheckWarningMessage;
	private String categoryName;
	private String description;
	private String excludeBrands;
	private String excludeSkuPrefix;
	private Date expirationDate;
	private String expirationDay;
	private String expirationMonth;
	private String expirationYear;
	private String id;
	private boolean isOrderTypeCorporateAllowed;
	private boolean isOrderTypeDepotAllowed;
	private boolean isOrderTypeHomeAllowed;
	private boolean isOrderTypePickupAllowed;
	private String maxAmount;
	private String percentOff;
	private String waiveChargeType;
	private String maxUsage;
	private String minSubtotal;
	private String name;
	private String needBrands;
	private boolean needDryGoods;
	private String needItemsFrom;
	private String orderCount;
	private String productName;
	private String promotionCode;
	private String promotionType;
	private String redemptionCode;
	private Date startDate;
	private String startDay;
	private String startMonth;
	private String startYear;
	private String valueType;
	private boolean ruleBasedPromotion;
	private String refProgCampaignCode;
	private boolean isMaxUsagePerCustomer;
	private Integer rollingExpirationDays;
	private Map<String,AssignedCustomerParam> assignedCustomerParams;
	private boolean uniqueUse;
	
	private int maxItemCount=0;
	private boolean recommendedItemsOnly=false;
	private boolean allowHeaderDiscount=false;

	private String notes;
	private boolean active;
	private Date modifyDate;
	private String modifiedBy;
	private boolean applyFraud = true;
	
	private String assignedDepartments;
	
	private String assignedCategories;
	
	private String assignedRecipes;
	
	private List<FDPromotionAttributeParam> attributeList = new ArrayList<FDPromotionAttributeParam>();
	
	private String profileOperator;
	
	private String tmpAssignedCustomerUserIds;
	
	private List<PromoVariantModel> promoVariants;
	
	public FDPromotionModel() {
		super();
	}

	public FDPromotionModel(PrimaryKey pk) {
		this();
		this.setPK(pk);
	}

	// getters
	public String getAssignedCustomerUserIds() {
		String str = "";
		if (this.assignedCustomerUserIds != null && !this.assignedCustomerUserIds.isEmpty()) {
			Iterator<String> iter = this.assignedCustomerUserIds.iterator();
			while (iter.hasNext()) {
				if (!"".equals(str)) {
					str += ",";
				}
				str += ((String)iter.next()).trim();
			}
		}

		return str; //(!"".equals(str)) ? str : null;
	}

	public int getAssignedCustomerSize(){
		if (this.assignedCustomerUserIds != null && !this.assignedCustomerUserIds.isEmpty()) {
			return assignedCustomerUserIds.size();
		}
		return 0;
	}

	public boolean isCustomerInAssignedList(String userId){
		if (this.assignedCustomerUserIds != null && !this.assignedCustomerUserIds.isEmpty()) {
			return this.assignedCustomerUserIds.contains(userId);
		}
		return false;
	}

	public void setZipValidationCheckWarningMessage(String zipValidationCheckWarningMessage){
		this.zipValidationCheckWarningMessage = zipValidationCheckWarningMessage;
	}

	public String getZipValidationCheckWarningMessage(){
		return this.zipValidationCheckWarningMessage;
	}

	public TreeMap<Date,FDPromoZipRestriction> getZipRestrictions() {
		return zipRestrictions;
	}

	public String getCategoryName() {
		return this.categoryName;
	}

	public String getDescription() {
		return this.description;
	}

	public String getExcludeBrands() {
		return this.excludeBrands;
	}

	public String getExcludeSkuPrefix() {
		return this.excludeSkuPrefix;
	}

	public Date getExpirationDate() {
		return this.expirationDate;
	}

	public String getExpirationDay() {
		return this.expirationDay;
	}

	public String getExpirationMonth() {
		return this.expirationMonth;
	}

	public String getExpirationYear() {
		return this.expirationYear;
	}

	public String getId() {
		return this.id;
	}

	public String getMaxAmount() {
		return this.maxAmount;
	}

	public String getPercentOff() {
		return percentOff;
	}

	public String getWaiveChargeType() {
		return waiveChargeType;
	}

	public String getMaxUsage() {
		return this.maxUsage;
	}

	public String getMinSubtotal() {
		return this.minSubtotal;
	}

	public String getName() {
		return this.name;
	}

	public String getNeedBrands() {
		return this.needBrands;
	}

	public boolean getNeedDryGoods() {
		return this.needDryGoods;
	}

	public String getNeedItemsFrom() {
		return this.needItemsFrom;
	}

	public String getOrderCount() {
		return this.orderCount;
	}

	public String getProductName() {
		return this.productName;
	}

	public String getPromotionType() {
		return this.promotionType;
	}

	public String getPromotionCode() {
		return this.promotionCode;
	}

	public String getRedemptionCode() {
		return redemptionCode;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public String getStartDay() {
		return this.startDay;
	}

	public String getStartMonth() {
		return this.startMonth;
	}

	public String getStartYear() {
		return this.startYear;
	}

	public boolean isOrderTypeCorporateAllowed() {
		return this.isOrderTypeCorporateAllowed;
	}

	public boolean isOrderTypeDepotAllowed() {
		return this.isOrderTypeDepotAllowed;
	}

	public boolean isOrderTypeHomeAllowed() {
		return this.isOrderTypeHomeAllowed;
	}

	public boolean isOrderTypePickupAllowed() {
		return this.isOrderTypePickupAllowed;
	}

	public String getValueType() {
		return this.valueType;
	}

	public String getRefProgCampaignCode() {
		return this.refProgCampaignCode;
	}

	public boolean isMaxUsagePerCustomer () {
		return this.isMaxUsagePerCustomer;
	}

	public Integer getRollingExpirationDays () {
		return this.rollingExpirationDays;
	}

	public Map<String,AssignedCustomerParam> getAssignedCustomerParams() {
		return this.assignedCustomerParams;
	}

	public boolean isActive() {
		return active;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public String getNotes() {
		return notes;
	}

	// setters

	public void setAssignedCustomerUserIds(String assignedCustomerUserIds) {
		this.assignedCustomerUserIds.clear();
		this.assignedCustomerUserIds.addAll(Arrays.asList(StringUtils.split(assignedCustomerUserIds, ",")));
	}

	public void setAssignedCustomerUserIds(Set<String> assignedCustomerUserIds){
		this.assignedCustomerUserIds = assignedCustomerUserIds;
	}


	public void setZipRestrictions(TreeMap<Date,FDPromoZipRestriction> zipRestrictions) {
		this.zipRestrictions = zipRestrictions;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setExcludeBrands(String excludeBrands) {
		this.excludeBrands = excludeBrands;
	}

	public void setExcludeSkuPrefix(String excludeSkuPrefix) {
		this.excludeSkuPrefix = excludeSkuPrefix;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public void setExpirationDay(String expirationDay) {
		this.expirationDay = expirationDay;
	}

	public void setExpirationMonth(String expirationMonth) {
		this.expirationMonth = expirationMonth;
	}

	public void setExpirationYear(String expirationYear) {
		this.expirationYear = expirationYear;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setIsOrderTypeCorporateAllowed(boolean isOrderTypeCorporateAllowed) {
		this.isOrderTypeCorporateAllowed = isOrderTypeCorporateAllowed;
	}

	public void setIsOrderTypeDepotAllowed(boolean isOrderTypeDepotAllowed) {
		this.isOrderTypeDepotAllowed = isOrderTypeDepotAllowed;
	}

	public void setIsOrderTypeHomeAllowed(boolean isOrderTypeHomeAllowed) {
		this.isOrderTypeHomeAllowed = isOrderTypeHomeAllowed;
	}

	public void setIsOrderTypePickupAllowed(boolean isOrderTypePickupAllowed) {
		this.isOrderTypePickupAllowed = isOrderTypePickupAllowed;
	}

	public void setMaxAmount(String maxAmount) {
		this.maxAmount = maxAmount;
	}

	public void setPercentOff(String percentOff) {
		this.percentOff = percentOff;
	}

	public void setWaiveChargeType(String waiveChargeType) {
		this.waiveChargeType = waiveChargeType;
	}

	public void setMaxUsage(String maxUsage) {
		this.maxUsage = maxUsage;
	}

	public void setMinSubtotal(String minSubtotal) {
		this.minSubtotal = minSubtotal;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNeedBrands(String needBrands) {
		this.needBrands = needBrands;
	}

	public void setNeedsDryGoods(boolean needDryGoods) {
		this.needDryGoods = needDryGoods;
	}

	public void setNeedItemsFrom(String needItemsFrom) {
		this.needItemsFrom = needItemsFrom;
	}

	public void setOrderCount(String orderCount) {
		this.orderCount = orderCount;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public void setPromotionCode(String promotionCode) {
		this.promotionCode = promotionCode;
	}

	public void setPromotionType(String promotionType) {
		this.promotionType = promotionType;
	}

	public void setRedemptionCode(String redemptionCode) {
		this.redemptionCode = redemptionCode;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setStartDay(String startDay) {
		this.startDay = startDay;
	}

	public void setStartMonth(String startMonth) {
		this.startMonth = startMonth;
	}

	public void setStartYear(String startYear) {
		this.startYear = startYear;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public void setRuleBasePromotion(boolean ruleBasedPromotion) {
		this.ruleBasedPromotion = ruleBasedPromotion;
	}

	public boolean isRuleBasedPromotion () {
		return this.ruleBasedPromotion;
	}

	public void setRefProgCampaignCode(String refProgCampaignCode) {
		this.refProgCampaignCode = refProgCampaignCode;
	}

	public void setIsMaxUsagePerCustomer(boolean isMaxUsagePerCustomer) {
		this.isMaxUsagePerCustomer = isMaxUsagePerCustomer;
	}

	public void setRollingExpirationDays(Integer rollingExpirationDays) {
		this.rollingExpirationDays = rollingExpirationDays;
	}

	public void setAssignedCustomerParams(Map<String,AssignedCustomerParam> assignedCustomerParams) {
		this.assignedCustomerParams = assignedCustomerParams;
	}

	public void setUniqueUse(boolean uniqueUse) {
		this.uniqueUse = uniqueUse;
	}

	public boolean isUniqueUse () {
		return this.uniqueUse;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public void setApplyFraud(boolean applyFraud) {
		this.applyFraud = applyFraud;
	}
	
	public boolean isApplyFraud(){
		return this.applyFraud;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("FDPromotionModel[");
		sb.append(this.promotionType).append(" / ").append(this.promotionCode);
		sb.append(" (").append(this.description).append(")");
		return sb.toString();
	}

	
	public List<FDPromotionAttributeParam> getAttributeList() {
		return attributeList;
	}

	public void setAttributeList(List<FDPromotionAttributeParam> attributeList) {
		this.attributeList = attributeList;
	}

	public String getAssignedCategories() {
		return assignedCategories;
	}

	public void setAssignedCategories(String assignedCategories) {
		this.assignedCategories = assignedCategories;
	}

	public String getAssignedDepartments() {
		return assignedDepartments;
	}

	public void setAssignedDepartments(String assignedDepartments) {
		this.assignedDepartments = assignedDepartments;
	}

	public String getAssignedRecipes() {
		return assignedRecipes;
	}

	public void setAssignedRecipes(String assignedRecipes) {
		this.assignedRecipes = assignedRecipes;
	}

	public String getProfileOperator() {
		return profileOperator;
	}

	public void setProfileOperator(String profileOperator) {
		this.profileOperator = profileOperator;
	}

	public String getTmpAssignedCustomerUserIds() {
		return tmpAssignedCustomerUserIds;
	}

	public void setTmpAssignedCustomerUserIds(String tmpAssignedCustomerUserIds) {
		this.tmpAssignedCustomerUserIds = tmpAssignedCustomerUserIds;
	}

	public boolean isAllowHeaderDiscount() {
		return allowHeaderDiscount;
	}

	public void setAllowHeaderDiscount(boolean applyHeaderDiscount) {
		this.allowHeaderDiscount = applyHeaderDiscount;
	}

	public int getMaxItemCount() {
		return maxItemCount;
	}

	public void setMaxItemCount(int maxItemCount) {
		this.maxItemCount = maxItemCount;
	}

	public boolean isRecommendedItemsOnly() {
		return recommendedItemsOnly;
	}

	public void setRecommendedItemsOnly(boolean recommendedItemsOnly) {
		this.recommendedItemsOnly = recommendedItemsOnly;
	}

	public List<PromoVariantModel> getPromoVariants() {
		return promoVariants;
	}

	public void setPromoVariants(List<PromoVariantModel> promoVariants) {
		this.promoVariants = promoVariants;
	}

	

}
