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

import com.freshdirect.deliverypass.EnumDlvPassStatus;
import com.freshdirect.fdstore.promotion.AssignedCustomerParam;
import com.freshdirect.fdstore.promotion.EnumDCPDContentType;
import com.freshdirect.fdstore.promotion.EnumPromotionStatus;
import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public class FDPromotionNewModel extends ModelSupport {

	private Set<String> assignedCustomerUserIds = new HashSet<String>();
	private TreeMap<Date, FDPromoZipRestriction> zipRestrictions = new TreeMap<Date, FDPromoZipRestriction>();
	private List<FDPromotionAttributeParam> attributeList = new ArrayList<FDPromotionAttributeParam>();
	private Map<String, AssignedCustomerParam> assignedCustomerParams;
	// private String id;
	private String promotionCode;
	private String name;
	private String description;
	private String redemptionCode;
	private Date startDate;
	private String startDay;
	private String startMonth;
	private String startYear;
	private Date expirationDate;
	private String expirationDay;
	private String expirationMonth;
	private String expirationYear;
	private Integer rollingExpirationDays;
	private boolean isRollingExpDayFrom1stOrder;
	private String maxUsage;
	private String promotionType;
	private String minSubtotal;
	private String maxAmount;
	private String percentOff;
	private String waiveChargeType;
	private EnumPromotionStatus status;
	private String offerDesc;
	private String audienceDesc;
	private String terms;
	private Integer redeemCount;
	private Integer skuQuantity;
	private boolean perishable;
	private String tmpAssignedCustomerUserIds;
	private List<FDPromoContentModel> dcpdData;
	private List<FDPromoContentModel> cartStrategies;
	private List<FDPromoCustStrategyModel> custStrategies;// Always one record
	private List<FDPromoPaymentStrategyModel> paymentStrategies;
	private List<FDPromoDlvZoneStrategyModel> dlvZoneStrategies;
	private List<FDPromoDlvDateModel> dlvDates;
	private List<FDPromoChangeModel> auditChanges;
	private boolean needDryGoods;
	private boolean needCustomerList;
	private boolean ruleBased;
	private boolean favoritesOnly;
	private boolean combineOffer;
	private Date createdDate;
	private Date modifiedDate;
	private String createdBy;
	private String modifiedBy;
	private Date lastPublishedDate;
	private boolean applyFraud = false;
	private String startDateStr;
	private String startTimeStr;
	private String expirationDateStr;
	private String expirationTimeStr;
	private String productName;
	private String categoryName;
	private Integer extendDpDays;
	private String offerType;
	private String subTotalExcludeSkus;
	private String profileOperator;
	private Integer maxItemCount;
	private boolean onHold;
	private String geoRestrictionType;

	private String dcpdDepts = "";
	private String dcpdCats = "";
	private String dcpdRecCats = "";
	private String dcpdRecps = "";
	private String dcpdSkus = "";
	private String dcpdBrands = "";

	private String cartDepts = "";
	private String cartCats = "";
	private String cartSkus = "";
	private String cartBrands = "";
	private FDPromoStateCountyRestriction scRestriction;
	private int restrictedCustomerSize;
	private boolean fuelSurcharge = false;
	private List<FDPromoDollarDiscount> dollarOffList = new ArrayList<FDPromoDollarDiscount>();
	private boolean isReferralPromo = false;
	private Integer skuLimit;
	private String tsaPromoCode;
	private String radius;
	private String maxPercentageDiscount;
	private String batchNumber;
	private String batchId;
	private boolean batchPromo = false;
	private String DcpdMinSubtotal;
	private String sapConditionType;
	private String rafPromoCode;

	private int publishes = 0;
	

	public FDPromotionNewModel() {
		super();
	}

	public FDPromotionNewModel(PrimaryKey pk) {
		this();
		this.setPK(pk);
	}

	public String getPromotionCode() {
		return promotionCode;
	}

	public void setPromotionCode(String promotionCode) {
		this.promotionCode = promotionCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRedemptionCode() {
		return redemptionCode;
	}

	public void setRedemptionCode(String redemptionCode) {
		this.redemptionCode = redemptionCode;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Integer getRollingExpirationDays() {
		return rollingExpirationDays;
	}

	public void setRollingExpirationDays(Integer rollingExpirationDays) {
		this.rollingExpirationDays = rollingExpirationDays;
	}

	public String getMaxUsage() {
		return maxUsage;
	}

	public void setMaxUsage(String maxUsage) {
		this.maxUsage = maxUsage;
	}

	public String getPromotionType() {
		return promotionType;
	}

	public void setPromotionType(String promotionType) {
		this.promotionType = promotionType;
	}

	public String getMinSubtotal() {
		return minSubtotal;
	}

	public void setMinSubtotal(String minSubtotal) {
		this.minSubtotal = minSubtotal;
	}

	public String getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(String maxAmount) {
		this.maxAmount = maxAmount;
	}

	public String getPercentOff() {
		return percentOff;
	}

	public void setPercentOff(String percentOff) {
		this.percentOff = percentOff;
	}

	public String getWaiveChargeType() {
		return waiveChargeType;
	}

	public void setWaiveChargeType(String waiveChargeType) {
		this.waiveChargeType = waiveChargeType;
	}

	public EnumPromotionStatus getStatus() {
		return status;
	}

	public void setStatus(EnumPromotionStatus status) {
		this.status = status;
	}

	public String getOfferDesc() {
		return offerDesc;
	}

	public void setOfferDesc(String offerDesc) {
		this.offerDesc = offerDesc;
	}

	public String getAudienceDesc() {
		return audienceDesc;
	}

	public void setAudienceDesc(String audienceDesc) {
		this.audienceDesc = audienceDesc;
	}

	public String getTerms() {
		return terms;
	}

	public void setTerms(String terms) {
		this.terms = terms;
	}

	public Integer getRedeemCount() {
		return redeemCount;
	}

	public void setRedeemCount(Integer redeemCount) {
		this.redeemCount = redeemCount;
	}

	public Integer getSkuQuantity() {
		return skuQuantity;
	}

	public void setSkuQuantity(Integer skuQuantity) {
		this.skuQuantity = skuQuantity;
	}

	public boolean isPerishable() {
		return perishable;
	}

	public void setPerishable(boolean perishable) {
		this.perishable = perishable;
	}

	public String getStartDay() {
		return startDay;
	}

	public void setStartDay(String startDay) {
		this.startDay = startDay;
	}

	public String getStartMonth() {
		return startMonth;
	}

	public void setStartMonth(String startMonth) {
		this.startMonth = startMonth;
	}

	public String getStartYear() {
		return startYear;
	}

	public void setStartYear(String startYear) {
		this.startYear = startYear;
	}

	public String getExpirationDay() {
		return expirationDay;
	}

	public void setExpirationDay(String expirationDay) {
		this.expirationDay = expirationDay;
	}

	public String getExpirationMonth() {
		return expirationMonth;
	}

	public void setExpirationMonth(String expirationMonth) {
		this.expirationMonth = expirationMonth;
	}

	public String getExpirationYear() {
		return expirationYear;
	}

	public void setExpirationYear(String expirationYear) {
		this.expirationYear = expirationYear;
	}

	public String getAssignedCustomerUserIds() {
		String str = "";
		if (this.assignedCustomerUserIds != null
				&& !this.assignedCustomerUserIds.isEmpty()) {
			Iterator<String> iter = this.assignedCustomerUserIds.iterator();
			while (iter.hasNext()) {
				if (!"".equals(str)) {
					str += ",";
				}
				str += ((String) iter.next()).trim();
			}
		}

		return str; // (!"".equals(str)) ? str : null;
	}

	public int getAssignedCustomerSize() {		
		/*if (this.assignedCustomerUserIds != null
				&& !this.assignedCustomerUserIds.isEmpty()) {
			return assignedCustomerUserIds.size();
		}
		
		return 0;
		*/
		return restrictedCustomerSize;
	}
	
	public void setAssignedCustomerSize(int restrictedCustomerSize) {
		this.restrictedCustomerSize = restrictedCustomerSize;
	}

	public boolean isCustomerInAssignedList(String userId) {
		if (this.assignedCustomerUserIds != null
				&& !this.assignedCustomerUserIds.isEmpty()) {
			return this.assignedCustomerUserIds.contains(userId);
		}
		return false;
	}

	public void setAssignedCustomerUserIds(String assignedCustomerUserIds) {
		this.assignedCustomerUserIds.clear();
		this.assignedCustomerUserIds.addAll(Arrays.asList(StringUtils.split(
				assignedCustomerUserIds, ",")));
	}

	public void clearAssignedCustomerUserIds() {
		this.assignedCustomerUserIds.clear();
	}

	public void setAssignedCustomerUserIds(Set<String> assignedCustomerUserIds) {
		this.assignedCustomerUserIds = assignedCustomerUserIds;
	}

	public TreeMap<Date, FDPromoZipRestriction> getZipRestrictions() {
		return zipRestrictions;
	}

	public void setZipRestrictions(
			TreeMap<Date, FDPromoZipRestriction> zipRestrictions) {
		this.zipRestrictions = zipRestrictions;
	}

	public Map<String, AssignedCustomerParam> getAssignedCustomerParams() {
		return assignedCustomerParams;
	}

	public void setAssignedCustomerParams(
			Map<String, AssignedCustomerParam> assignedCustomerParams) {
		this.assignedCustomerParams = assignedCustomerParams;
	}

	public List<FDPromotionAttributeParam> getAttributeList() {
		return attributeList;
	}

	public void setAttributeList(List<FDPromotionAttributeParam> attributeList) {
		this.attributeList = attributeList;
	}

	public String getTmpAssignedCustomerUserIds() {
		return tmpAssignedCustomerUserIds;
	}

	public void setTmpAssignedCustomerUserIds(String tmpAssignedCustomerUserIds) {
		this.tmpAssignedCustomerUserIds = tmpAssignedCustomerUserIds;
	}

	public List<FDPromoContentModel> getDcpdData() {
		return dcpdData;
	}

	public void getDcpdDataString() {
		if (null != dcpdData && !dcpdData.isEmpty()) {
			StringBuffer deptBuffer = new StringBuffer();
			StringBuffer catBuffer = new StringBuffer();
			StringBuffer recCatBuffer = new StringBuffer();
			StringBuffer recpBuffer = new StringBuffer();
			StringBuffer skuBuffer = new StringBuffer();
			StringBuffer brandBuffer = new StringBuffer();
			for (Iterator iterator = dcpdData.iterator(); iterator.hasNext();) {
				FDPromoContentModel contentModel = (FDPromoContentModel) iterator
						.next();
				if (EnumDCPDContentType.DEPARTMENT.equals(contentModel
						.getContentType())) {
					deptBuffer.append(contentModel.getContentId() + ",");
				}
				if (EnumDCPDContentType.CATEGORY.equals(contentModel
						.getContentType())) {
					if (contentModel.isRecCategory()) {						
						recCatBuffer.append(contentModel.getContentId() + ",");
					} else {
						catBuffer.append(contentModel.getContentId() + ",");
					}
				}
				if (EnumDCPDContentType.RECIPE.equals(contentModel
						.getContentType())) {
					recpBuffer.append(contentModel.getContentId() + ",");
				}
				if (EnumDCPDContentType.SKU.equals(contentModel
						.getContentType())) {
					if (skuBuffer.length() <= 0) {
						if (contentModel.isExcluded())
							skuBuffer.append("Excluded Skus: ");
					}
					skuBuffer.append(contentModel.getContentId() + ",");
				}
				if (EnumDCPDContentType.BRAND.equals(contentModel
						.getContentType())) {
					if (brandBuffer.length() <= 0) {
						if (contentModel.isExcluded())
							brandBuffer.append("Excluded Brands: ");
					}
					brandBuffer.append(contentModel.getContentId() + ",");
				}				
			}
			dcpdDepts = deptBuffer.toString();
			dcpdCats = catBuffer.toString();
			dcpdRecCats = recCatBuffer.toString();
			dcpdRecps = recpBuffer.toString();
			dcpdSkus = skuBuffer.toString();
			dcpdBrands = brandBuffer.toString();
		}
	}

	public void getCartContentString() {
		if (null != cartStrategies && !cartStrategies.isEmpty()) {
			StringBuffer deptBuffer = new StringBuffer();
			StringBuffer catBuffer = new StringBuffer();
			// StringBuffer recpBuffer = new StringBuffer();
			StringBuffer skuBuffer = new StringBuffer();
			StringBuffer brandBuffer = new StringBuffer();
			for (Iterator iterator = cartStrategies.iterator(); iterator
					.hasNext();) {
				FDPromoContentModel contentModel = (FDPromoContentModel) iterator
						.next();
				if (EnumDCPDContentType.DEPARTMENT.equals(contentModel
						.getContentType())) {
					deptBuffer.append(contentModel.getContentId() + ",");
				}
				if (EnumDCPDContentType.CATEGORY.equals(contentModel
						.getContentType())) {
					catBuffer.append(contentModel.getContentId() + ",");
				}
				/*
				 * if(EnumDCPDContentType.RECIPE.equals(contentModel.getContentType
				 * ())){ if(recpBuffer.length()<=0){
				 * recpBuffer.append("Recipes: "); }
				 * recpBuffer.append(contentModel.getContentId()+","); }
				 */
				if (EnumDCPDContentType.SKU.equals(contentModel
						.getContentType())) {
					skuBuffer.append(contentModel.getContentId() + ",");
				}
				if (EnumDCPDContentType.BRAND.equals(contentModel
						.getContentType())) {
					brandBuffer.append(contentModel.getContentId() + ",");
				}
			}
			cartDepts = deptBuffer.toString();
			cartCats = catBuffer.toString();
			// cartRecps = recpBuffer.toString();
			cartSkus = skuBuffer.toString();
			cartBrands = brandBuffer.toString();
		}
	}

	public void setDcpdData(List<FDPromoContentModel> dcpdData) {
		this.dcpdData = dcpdData;
	}

	public List<FDPromoContentModel> getCartStrategies() {
		return cartStrategies;
	}

	public void setCartStrategies(List<FDPromoContentModel> cartStrategies) {
		this.cartStrategies = cartStrategies;
	}

	public List<FDPromoCustStrategyModel> getCustStrategies() {
		return custStrategies;
	}

	public void setCustStrategies(List<FDPromoCustStrategyModel> custStrategies) {
		this.custStrategies = custStrategies;
	}

	public List<FDPromoPaymentStrategyModel> getPaymentStrategies() {
		return paymentStrategies;
	}

	public void setPaymentStrategies(
			List<FDPromoPaymentStrategyModel> paymentStrategies) {
		this.paymentStrategies = paymentStrategies;
	}

	public List<FDPromoChangeModel> getAuditChanges() {
		return auditChanges;
	}

	public void setAuditChanges(List<FDPromoChangeModel> auditChanges) {
		this.auditChanges = auditChanges;
	}

	public void clearAuditChanges() {
		if (this.auditChanges == null|| this.auditChanges.size()==0 ||this.auditChanges.isEmpty())
			this.auditChanges = new ArrayList<FDPromoChangeModel>();

		this.auditChanges.clear();
	}

	public void addAuditChange(FDPromoChangeModel aChange) {
		if (this.auditChanges == null|| this.auditChanges.size()==0 ||this.auditChanges.isEmpty())
			this.auditChanges = new ArrayList<FDPromoChangeModel>();

		this.auditChanges.add(aChange);
	}

	public boolean isNeedDryGoods() {
		return needDryGoods;
	}

	public void setNeedDryGoods(boolean needDryGoods) {
		this.needDryGoods = needDryGoods;
	}

	public boolean isNeedCustomerList() {
		return needCustomerList;
	}

	public void setNeedCustomerList(boolean needCustomerList) {
		this.needCustomerList = needCustomerList;
	}

	public List<FDPromoDlvZoneStrategyModel> getDlvZoneStrategies() {
		return dlvZoneStrategies;
	}

	public void setDlvZoneStrategies(
			List<FDPromoDlvZoneStrategyModel> dlvZoneStrategies) {
		this.dlvZoneStrategies = dlvZoneStrategies;
	}

	public List<FDPromoDlvDateModel> getDlvDates() {
		return dlvDates;
	}

	public void setDlvDates(List<FDPromoDlvDateModel> dlvDates) {
		this.dlvDates = dlvDates;
	}

	public boolean isRuleBased() {
		return ruleBased;
	}

	public void setRuleBased(boolean ruleBased) {
		this.ruleBased = ruleBased;
	}

	public boolean isFavoritesOnly() {
		return favoritesOnly;
	}

	public void setFavoritesOnly(boolean favoritesOnly) {
		this.favoritesOnly = favoritesOnly;
	}

	public boolean isCombineOffer() {
		return combineOffer;
	}

	public void setCombineOffer(boolean combineOffer) {
		this.combineOffer = combineOffer;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getLastPublishedDate() {
		return lastPublishedDate;
	}

	public void setLastPublishedDate(Date lastPublishedDate) {
		this.lastPublishedDate = lastPublishedDate;
	}

	public boolean isApplyFraud() {
		return applyFraud;
	}

	public void setApplyFraud(boolean applyFraud) {
		this.applyFraud = applyFraud;
	}

	public String getStartDateStr() {
		return startDateStr;
	}

	public void setStartDateStr(String startDateStr) {
		this.startDateStr = startDateStr;
	}

	public String getStartTimeStr() {
		return startTimeStr;
	}

	public void setStartTimeStr(String startTimeStr) {
		this.startTimeStr = startTimeStr;
	}

	public String getExpirationDateStr() {
		return expirationDateStr;
	}

	public void setExpirationDateStr(String expirationDateStr) {
		this.expirationDateStr = expirationDateStr;
	}

	public String getExpirationTimeStr() {
		return expirationTimeStr;
	}

	public void setExpirationTimeStr(String expirationTimeStr) {
		this.expirationTimeStr = expirationTimeStr;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public Integer getExtendDpDays() {
		return extendDpDays;
	}

	public void setExtendDpDays(Integer extendDpDays) {
		this.extendDpDays = extendDpDays;
	}

	public String getOfferType() {
		return offerType;
	}

	public void setOfferType(String offerType) {
		this.offerType = offerType;
	}

	public String getSubTotalExcludeSkus() {
		return subTotalExcludeSkus;
	}

	public void setSubTotalExcludeSkus(String subTotalExcludeSkus) {
		this.subTotalExcludeSkus = subTotalExcludeSkus;
	}

	public String getProfileOperator() {
		return profileOperator;
	}

	public void setProfileOperator(String profileOperator) {
		this.profileOperator = profileOperator;
	}

	public Integer getMaxItemCount() {
		return maxItemCount;
	}

	public void setMaxItemCount(Integer maxItemCount) {
		this.maxItemCount = maxItemCount;
	}

	public boolean isOnHold() {
		return onHold;
	}

	public void setOnHold(boolean onHold) {
		this.onHold = onHold;
	}

	public String getGeoRestrictionType() {
		return geoRestrictionType;
	}

	public void setGeoRestrictionType(String geoRestrictionType) {
		this.geoRestrictionType = geoRestrictionType;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("FDPromotionModel[");
		sb.append(this.promotionType).append(" / ").append(this.promotionCode);
		sb.append(" (").append(this.description).append(")");
		return sb.toString();
	}

	/**
	 * Utility method to remove references from freshly imported promotions to
	 * avoid integrity violation issue
	 */
	public void removeReferences() {
		if (custStrategies != null)
			for (FDPromoCustStrategyModel model : custStrategies) {
				model.setPromotionId(null);
			}

		if (cartStrategies != null)
			for (FDPromoContentModel model : cartStrategies) {
				model.setPromotionId(null);
			}

		if (dcpdData != null)
			for (FDPromoContentModel model : dcpdData) {
				model.setPromotionId(null);
			}

		if (paymentStrategies != null)
			for (FDPromoPaymentStrategyModel model : paymentStrategies) {
				model.setPromotionId(null);
			}

		if (dlvZoneStrategies != null)
			for (FDPromoDlvZoneStrategyModel model : dlvZoneStrategies) {
				model.setPromotionId(null);
			}

		if (dlvDates != null)
			for (FDPromoDlvDateModel model : dlvDates) {
				model.setPromoId(null);
			}

		if (auditChanges != null)
			for (FDPromoChangeModel model : auditChanges) {
				if (model.getChangeDetails() != null)
					for (FDPromoChangeDetailModel model2 : model
							.getChangeDetails()) {
						model2.setPromoChangeId(null);
					}

				model.setPromotionId(null);
			}
	}

	/**
	 * Returns the number of successful publish events
	 * 
	 * @return
	 */
	public int getPublishes() {
		return publishes;
	}

	public void setPublishes(int cnt) {
		this.publishes = cnt;
	}

	public boolean isForChef() {
		boolean isMatched = false;
		List<FDPromotionAttributeParam> attrList = this.getAttributeList();
		for (FDPromotionAttributeParam promotionAttributeParam : attrList) {
			if ("ChefsTable".equalsIgnoreCase(promotionAttributeParam
					.getAttributeName())) {
				isMatched = true;
				break;
			}
		}
		return isMatched;
	}

	public boolean isForCOS() {
		boolean isMatched = false;
		List<FDPromoCustStrategyModel> custStrategies = this
				.getCustStrategies();
		if (null != custStrategies) {
			// FIXME: I doubt this piece reflects the original intention of
			// evaluating isMatched flag
			for (Iterator<FDPromoCustStrategyModel> iterator = custStrategies
					.iterator(); iterator.hasNext();) {
				FDPromoCustStrategyModel promoCustStrategyModel = iterator
						.next();
				isMatched = promoCustStrategyModel.isOrderTypeCorporate();
				break;
			}
		}
		return isMatched;
	}
	

	public boolean isForCOSNew() {
		boolean isMatched = false;
		List<FDPromoCustStrategyModel> custStrategies = this
				.getCustStrategies();
		if (null != custStrategies) {
			// FIXME: I doubt this piece reflects the original intention of
			// evaluating isMatched flag
			for (Iterator<FDPromoCustStrategyModel> iterator = custStrategies
					.iterator(); iterator.hasNext();) {
				FDPromoCustStrategyModel promoCustStrategyModel = iterator
						.next();
				isMatched = (promoCustStrategyModel.isOrderTypeCorporate()
						&& (promoCustStrategyModel.getOrderRangeStart() == 1) || (promoCustStrategyModel
						.getOrderRangeEnd() == 1));
				break;
			}
		}
		return isMatched;
	}

	public boolean isForFDX() {
		boolean retBool = false;
		List<FDPromoCustStrategyModel> custStrategies = this.getCustStrategies();
		if (null != custStrategies) {
			for (Iterator<FDPromoCustStrategyModel> iterator = custStrategies.iterator(); iterator.hasNext();) {
				FDPromoCustStrategyModel promoCustStrategyModel = iterator.next();
				retBool = promoCustStrategyModel.isOrderTypeFDX();
				break;
			}
		}
		return retBool;
	}
	
	public boolean isForHOME() {
		boolean retBool = false;
		List<FDPromoCustStrategyModel> custStrategies = this.getCustStrategies();
		if (null != custStrategies) {
			for (Iterator<FDPromoCustStrategyModel> iterator = custStrategies.iterator(); iterator.hasNext();) {
				FDPromoCustStrategyModel promoCustStrategyModel = iterator.next();
				retBool = promoCustStrategyModel.isOrderTypeHome();
				break;
			}
		}
		return retBool;
	}

	public boolean isForPICKUP() {
		boolean retBool = false;
		List<FDPromoCustStrategyModel> custStrategies = this.getCustStrategies();
		if (null != custStrategies) {
			for (Iterator<FDPromoCustStrategyModel> iterator = custStrategies.iterator(); iterator.hasNext();) {
				FDPromoCustStrategyModel promoCustStrategyModel = iterator.next();
				retBool = promoCustStrategyModel.isOrderTypePickup();
				break;
			}
		}
		return retBool;
	}

	public boolean isForNew() {
		boolean isMatched = false;
		List<FDPromoCustStrategyModel> custStrategies = this
				.getCustStrategies();
		if (null != custStrategies) {
			// FIXME: I doubt this piece reflects the original intention of
			// evaluating isMatched flag
			for (Iterator<FDPromoCustStrategyModel> iterator = custStrategies
					.iterator(); iterator.hasNext();) {
				FDPromoCustStrategyModel promoCustStrategyModel = iterator
						.next();
				isMatched = (promoCustStrategyModel.getOrderRangeStart() == 1)
						|| (promoCustStrategyModel.getOrderRangeEnd() == 1);
				break;
			}
		}
		return isMatched;
	}
	
	public boolean isForDPActiveOrRTU() {
		boolean isMatched = false;
		List<FDPromoCustStrategyModel> custStrategies = this
				.getCustStrategies();
		if (null != custStrategies) {
			// FIXME: I doubt this piece reflects the original intention of
			// evaluating isMatched flag
			for (Iterator<FDPromoCustStrategyModel> iterator = custStrategies
					.iterator(); iterator.hasNext();) {
				FDPromoCustStrategyModel promoCustStrategyModel = iterator
						.next();
				isMatched = EnumDlvPassStatus.ACTIVE.getName()
						.equalsIgnoreCase(promoCustStrategyModel.getDpStatus())
						|| EnumDlvPassStatus.READY_TO_USE.getName()
								.equalsIgnoreCase(
										promoCustStrategyModel.getDpStatus());
				break;
			}
		}
		return isMatched;
	}

	public boolean isForMarketing() {
		boolean isMatched = false;
		List<FDPromotionAttributeParam> attrList = this.getAttributeList();
		for (FDPromotionAttributeParam promotionAttributeParam : attrList) {
			if ("MarketingPromo".equalsIgnoreCase(promotionAttributeParam
					.getAttributeName())) {
				isMatched = true;
				break;
			}
		}
		return isMatched;
	}

	/**
	 * Remove certain attributes of a cloned / imported promotion
	 */
	public void doCleanup() {
		clearAssignedCustomerUserIds();
		clearAuditChanges();
	}

	public String getDcpdDepts() {
		return dcpdDepts;
	}

	public void setDcpdDepts(String dcpdDepts) {
		this.dcpdDepts = dcpdDepts;
	}

	public String getDcpdCats() {
		return dcpdCats;
	}

	public void setDcpdCats(String dcpdCats) {
		this.dcpdCats = dcpdCats;
	}
	
	public String getDcpdRecCats() {
		return dcpdRecCats;
	}

	public void setDcpdRecCats(String dcpdRecCats) {
		this.dcpdRecCats = dcpdRecCats;
	}

	public String getDcpdRecps() {
		return dcpdRecps;
	}

	public void setDcpdRecps(String dcpdRecps) {
		this.dcpdRecps = dcpdRecps;
	}

	public String getDcpdSkus() {
		return dcpdSkus;
	}

	public void setDcpdSkus(String dcpdSkus) {
		this.dcpdSkus = dcpdSkus;
	}

	public String getDcpdBrands() {
		return dcpdBrands;
	}

	public void setDcpdBrands(String dcpdBrands) {
		this.dcpdBrands = dcpdBrands;
	}

	public String getCartDepts() {
		return cartDepts;
	}

	public void setCartDepts(String cartDepts) {
		this.cartDepts = cartDepts;
	}

	public String getCartCats() {
		return cartCats;
	}

	public void setCartCats(String cartCats) {
		this.cartCats = cartCats;
	}

	public String getCartSkus() {
		return cartSkus;
	}

	public void setCartSkus(String cartSkus) {
		this.cartSkus = cartSkus;
	}

	public String getCartBrands() {
		return cartBrands;
	}

	public void setCartBrands(String cartBrands) {
		this.cartBrands = cartBrands;
	}
	
	public FDPromoStateCountyRestriction getStateCountyList() {
		return scRestriction;
	}

	public void setStateCountyList(FDPromoStateCountyRestriction scRestriction) {
		this.scRestriction = scRestriction;
	}
	
	public void setFuelSurchargeIncluded(boolean fs) {		        
		this.fuelSurcharge = fs;
	}
	
	public boolean isFuelSurchargeIncluded() {
		return this.fuelSurcharge;
	}

	public String getWSSelectedZone() {
		 String selectedZone = "";
		 List<FDPromoDlvZoneStrategyModel> list = getDlvZoneStrategies(); 
		 String[] selectedZones = null;
		 if(null != list && !list.isEmpty()){
			 FDPromoDlvZoneStrategyModel zoneStrgyModel =(FDPromoDlvZoneStrategyModel)list.get(0);
			 if(zoneStrgyModel != null) {
				 selectedZones = zoneStrgyModel.getDlvZones();
				 if(selectedZones != null && selectedZones.length > 0){
					 selectedZone = selectedZones[0];
				 }
			 }
		 } 
		 return selectedZone;
	}
	
	public String getWSSelectedStartTime() {
		String startTime = "";
		 List<FDPromoDlvZoneStrategyModel> list = getDlvZoneStrategies(); 
		 if(null != list && !list.isEmpty()){
			 FDPromoDlvZoneStrategyModel zoneStrgyModel =(FDPromoDlvZoneStrategyModel)list.get(0);
			 if(zoneStrgyModel != null) {
				 String selectedZoneId = zoneStrgyModel.getId();
				 List<FDPromoDlvTimeSlotModel> dlvTimeSlots = zoneStrgyModel.getDlvTimeSlots();
				 if(dlvTimeSlots != null && dlvTimeSlots.size() > 0){
					 for(Iterator<FDPromoDlvTimeSlotModel> it = dlvTimeSlots.iterator(); it.hasNext();){
						 FDPromoDlvTimeSlotModel timeSlotModel = it.next();
						 if(timeSlotModel != null && timeSlotModel.getPromoDlvZoneId().equals(selectedZoneId))
							 startTime = timeSlotModel.getDlvTimeStart();
					 }
				 }
			 }
		 } 
		 return startTime;
	}
	
	public String getWSSelectedEndTime() {
		String endTime = "";
		 List<FDPromoDlvZoneStrategyModel> list = getDlvZoneStrategies(); 
		 if(null != list && !list.isEmpty()){
			 FDPromoDlvZoneStrategyModel zoneStrgyModel =(FDPromoDlvZoneStrategyModel)list.get(0);
			 if(zoneStrgyModel != null) {
				 String selectedZoneId = zoneStrgyModel.getId();
				 List<FDPromoDlvTimeSlotModel> dlvTimeSlots = zoneStrgyModel.getDlvTimeSlots();
				 if(dlvTimeSlots != null && dlvTimeSlots.size() > 0){
					 for(Iterator<FDPromoDlvTimeSlotModel> it = dlvTimeSlots.iterator(); it.hasNext();){
						 FDPromoDlvTimeSlotModel timeSlotModel = it.next();
						 if(timeSlotModel != null && timeSlotModel.getPromoDlvZoneId().equals(selectedZoneId))
							 endTime = timeSlotModel.getDlvTimeEnd();
					 }
				 }
			 }
		 } 
		 return endTime;
	}
	
	public Date getWSSelectedDlvDate() {
		Date dlvDate = null;
		 List<FDPromoDlvDateModel> list = getDlvDates(); 
		 if(null != list && !list.isEmpty()){
			 FDPromoDlvDateModel dlvDateModel =(FDPromoDlvDateModel)list.get(0);
			 if(dlvDateModel != null) {
				 dlvDate = dlvDateModel.getDlvDateStart();
			 }
		 } 
		 return dlvDate;
	}
	
	public List<FDPromoDollarDiscount> getDollarOffList() {
		return dollarOffList;
	}

	public void setDollarOffList(List<FDPromoDollarDiscount> dollarOffList) {
		this.dollarOffList = dollarOffList;
	}

	public void setReferralPromo(boolean isReferralPromo) {
		this.isReferralPromo = isReferralPromo;
	}

	public boolean isReferralPromo() {
		return isReferralPromo;
	}

	public void setSkuLimit(Integer skuLimit) {
		this.skuLimit = skuLimit;
	}

	public Integer getSkuLimit() {
		return skuLimit;
	}

	public void setTsaPromoCode(String tsaPromoCode) {
		this.tsaPromoCode = tsaPromoCode;
	}

	public String getTsaPromoCode() {
		return tsaPromoCode;
	}

	public String getRadius() {
		return radius;
	}

	public void setRadius(String radius) {
		this.radius = radius;
	}
	
	public String getMaxPercentageDiscount() {
		return maxPercentageDiscount;
	}

	public void setMaxPercentageDiscount(String amount) {
		this.maxPercentageDiscount = amount;
	}

	public String[] getWSSelectedWindows() {
		 String[] windowTypes = null;
		 List<FDPromoDlvZoneStrategyModel> list = getDlvZoneStrategies(); 
		 if(null != list && !list.isEmpty()){
			 FDPromoDlvZoneStrategyModel zoneStrgyModel =(FDPromoDlvZoneStrategyModel)list.get(0);
			 if(zoneStrgyModel != null) {
				 String selectedZoneId = zoneStrgyModel.getId();
				 List<FDPromoDlvTimeSlotModel> dlvTimeSlots = zoneStrgyModel.getDlvTimeSlots();
				 if(dlvTimeSlots != null && dlvTimeSlots.size() > 0){
					 for(Iterator<FDPromoDlvTimeSlotModel> it = dlvTimeSlots.iterator(); it.hasNext();){
						 FDPromoDlvTimeSlotModel timeSlotModel = it.next();
						 if(timeSlotModel != null && timeSlotModel.getPromoDlvZoneId().equals(selectedZoneId))
							 windowTypes = timeSlotModel.getWindowTypes();
					 }
				 }
			 }
		 } 
		 return windowTypes;
	}

	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}

	public String getBatchNumber() {
		return batchNumber;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getBatchId() {
		return batchId;
	}
	
	public boolean isBatchPromo() {
		return batchPromo;
	}
	
	public void setBatchPromo(boolean batchPromo) {
		this.batchPromo = batchPromo;
	}
	
	public String getDcpdMinSubtotal() {
		return DcpdMinSubtotal;
	}

	public void setDcpdMinSubtotal(String dcpdMinSubtotal) {
		DcpdMinSubtotal = dcpdMinSubtotal;
	}

	/**
	 * @return the rafPromoCode
	 */
	public String getRafPromoCode() {
		return rafPromoCode;
	}

	/**
	 * @param rafPromoCode the rafPromoCode to set
	 */
	public void setRafPromoCode(String rafPromoCode) {
		this.rafPromoCode = rafPromoCode;
	}
	/**
	 * @return the isRollingExpDayFrom1stOrder
	 */
	public boolean isRollingExpDayFrom1stOrder() {
		return isRollingExpDayFrom1stOrder;
	}

	/**
	 * @param isRollingExpDayFrom1stOrder the isRollingExpDayFrom1stOrder to set
	 */
	public void setRollingExpDayFrom1stOrder(boolean isRollingExpDayFrom1stOrder) {
		this.isRollingExpDayFrom1stOrder = isRollingExpDayFrom1stOrder;
	}

	/**
	 * @return the sapConditionType
	 */
	public String getSapConditionType() {
		return sapConditionType;
	}

	/**
	 * @param sapConditionType the sapConditionType to set
	 */
	public void setSapConditionType(String sapConditionType) {
		this.sapConditionType = sapConditionType;
	}
	
	
}
