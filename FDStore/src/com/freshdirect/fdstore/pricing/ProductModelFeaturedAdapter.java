package com.freshdirect.fdstore.pricing;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.freshdirect.cms.AttributeDefI;
import com.freshdirect.cms.ContentKey;
import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.content.nutrition.ErpNutritionInfoType;
import com.freshdirect.erp.model.ErpProductInfoModel;
import com.freshdirect.fdstore.EnumOrderLineRating;
import com.freshdirect.fdstore.EnumSustainabilityRating;
import com.freshdirect.fdstore.FDConfigurableI;
import com.freshdirect.fdstore.FDGroup;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSku;
import com.freshdirect.fdstore.content.BrandModel;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ComponentGroupModel;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.DepartmentModel;
import com.freshdirect.fdstore.content.Domain;
import com.freshdirect.fdstore.content.DomainValue;
import com.freshdirect.fdstore.content.EnumLayoutType;
import com.freshdirect.fdstore.content.EnumProductLayout;
import com.freshdirect.fdstore.content.EnumTemplateType;
import com.freshdirect.fdstore.content.Html;
import com.freshdirect.fdstore.content.Image;
import com.freshdirect.fdstore.content.MediaI;
import com.freshdirect.fdstore.content.PriceCalculator;
import com.freshdirect.fdstore.content.PrioritizedI;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.Recipe;
import com.freshdirect.fdstore.content.SkuModel;
import com.freshdirect.fdstore.content.TagModel;
import com.freshdirect.fdstore.content.YmalSet;
import com.freshdirect.fdstore.content.YmalSetSource;
import com.freshdirect.framework.util.DayOfWeekSet;

public class ProductModelFeaturedAdapter implements ProductModel, Serializable,
		Cloneable, PrioritizedI {
	
	private ProductModel productModel;
	private boolean isFeatured;
	private String featuredHeader;
	private ErpProductInfoModel erpProductInfoModel;

	
	public ProductModel getProductModel() {
		return productModel;
	}

	public void setProductModel(ProductModel productModel) {
		this.productModel = productModel;
	}

	public boolean isFeatured() {
		return isFeatured;
	}

	public void setFeatured(boolean isFeatured) {
		this.isFeatured = isFeatured;
	}

	public String getFeaturedHeader() {
		return featuredHeader;
	}

	public void setFeaturedHeader(String featuredHeader) {
		this.featuredHeader = featuredHeader;
	}

	public ProductModelFeaturedAdapter(ProductModel productModel,
			boolean isFeatured, String featuredHeader) {
		if (productModel == null) {
			throw new IllegalArgumentException("product model cannot be null");
		}
		this.productModel = productModel;
		this.isFeatured = isFeatured;
		this.featuredHeader = featuredHeader;
	}

	@Override
	public boolean enforceQuantityMax() {
		 return productModel.enforceQuantityMax();
	}

	@Override
	public String getAboutPriceFormatted(double savingsPercentage) {
		return productModel.getAboutPriceFormatted(savingsPercentage);
	}

	@Override
	public YmalSet getActiveYmalSet() {
		return productModel.getActiveYmalSet();
	}

	@Override
	public double getAge() {
		return productModel.getAge();
	}

	@Override
	public String getAka() {		 
		return productModel.getAka();
	}

	@Override
	public ProductModel getAlsoSoldAs(int idx) {
		return productModel.getAlsoSoldAs(idx);
	}

	@Override
	public List<ProductModel> getAlsoSoldAs() {
		return productModel.getAlsoSoldAs();
	}

	@Override
	public String getAlsoSoldAsName() {		 
		return productModel.getAlsoSoldAsName();
	}

	@Override
	public List<ProductModel> getAlsoSoldAsRefs() {		 
		return productModel.getAlsoSoldAsRefs();
	}

	@Override
	public String getAltText() {		 
		return productModel.getAltText();
	}

	@Override
	public Image getAlternateImage() {		 
		return productModel.getAlternateImage();
	}

	@Override
	public Image getAlternateProductImage() {		 
		return productModel.getAlternateProductImage();
	}

	@Override
	public FDConfigurableI getAutoconfiguration() {		 
		return productModel.getAutoconfiguration();
	}

	@Override
	public double getBackInStockAge() {		 
		return productModel.getBackInStockAge();
	}

	@Override
	public Date getBackInStockDate() {		 
		return productModel.getBackInStockDate();
	}

	@Override
	public DayOfWeekSet getBlockedDays() {		 
		return productModel.getBlockedDays();
	}

	@Override
	public String getBlurb() {		 
		return productModel.getBlurb();
	}

	@Override
	public List<BrandModel> getBrands() {		 
		return productModel.getBrands();
	}

	@Override
	public CategoryModel getCategory() {		 
		return productModel.getCategory();
	}

	@Override
	public Image getCategoryImage() {		 
		return productModel.getCategoryImage();
	}

	@Override
	public Set getCommonNutritionInfo(ErpNutritionInfoType type)
			throws FDResourceException {		 
		return productModel.getCommonNutritionInfo(type);
	}

	@Override
	public List<ComponentGroupModel> getComponentGroups() {		 
		return productModel.getComponentGroups();
	}

	@Override
	public Image getConfirmImage() {		 
		return productModel.getConfirmImage();
	}

	@Override
	public Double getContainerWeightHalfPint() {		 
		return productModel.getContainerWeightHalfPint();
	}

	@Override
	public Double getContainerWeightPint() {		 
		return productModel.getContainerWeightPint();
	}

	@Override
	public Double getContainerWeightQuart() {		 
		return productModel.getContainerWeightQuart();
	}

	@Override
	public List<String> getCountryOfOrigin() throws FDResourceException {		 
		return productModel.getCountryOfOrigin();
	}

	@Override
	public int getDealPercentage() {		 
		return productModel.getDealPercentage();
	}

	@Override
	public int getDealPercentage(String skuCode) {		 
		return productModel.getDealPercentage();
	}

	@Override
	public String getDefaultPrice() {		 
		return productModel.getDefaultPrice();
	}

	@Override
	public SkuModel getDefaultSku() {		 
		return productModel.getDefaultSku();
	}

	@Override
	public SkuModel getDefaultSku(PricingContext context) {		 
		return productModel.getDefaultSku(context);
	}
	
	@Override
	public SkuModel getDefaultTemporaryUnavSku() {
		return productModel.getDefaultTemporaryUnavSku();
	}

	@Override
	public String getDefaultSkuCode() {		 
		return productModel.getDefaultSkuCode();
	}

	@Override
	public DepartmentModel getDepartment() {		 
		return productModel.getDepartment();
	}

	@Override
	public Image getDescriptiveImage() {		 
		return productModel.getDescriptiveImage();
	}

	@Override
	public Image getDetailImage() {		 
		return productModel.getDetailImage();
	}

	@Override
	public List getDisplayableBrands() {		 
		return productModel.getDisplayableBrands();
	}

	@Override
	public List getDisplayableBrands(int numberOfBrands) {		 
		return productModel.getDisplayableBrands(numberOfBrands);
	}

	@Override
	public List<Html> getDonenessGuide() {		 
		return productModel.getDonenessGuide();
	}

	@Override
	public int getExpertWeight() {		 
		return productModel.getExpertWeight();
	}

	@Override
	public FDGroup getFDGroup() throws FDResourceException {		 
		return productModel.getFDGroup();
	}

	@Override
	public Html getFddefFrenching() {		 
		return productModel.getFddefFrenching();
	}

	@Override
	public Html getFddefGrade() {		 
		return productModel.getFddefGrade();
	}

	@Override
	public Html getFddefRipeness() {		 
		return productModel.getFddefRipeness();
	}

	@Override
	public Html getFddefSource() {		 
		return productModel.getFddefSource();
	}

	@Override
	public Image getFeatureImage() {		 
		return productModel.getFeatureImage();
	}

	@Override
	public Html getFreshTips() {		 
		return productModel.getFreshTips();
	}

	@Override
	public String getFreshnessGuaranteed() throws FDResourceException {		 
		return productModel.getFreshnessGuaranteed();
	}

	@Override
	public String getFullName() {		 
		return productModel.getFullName();
	}

	@Override
	public List getGiftcardType() {		 
		return productModel.getGiftcardType();
	}

	@Override
	public String getGlanceName() {		 
		return productModel.getGlanceName();
	}

	@Override
	public String getHideUrl() {		 
		return productModel.getHideUrl();
	}

	@Override
	public int getHighestDealPercentage() {		 
		return productModel.getHighestDealPercentage();
	}

	@Override
	public int getHighestDealPercentage(String skuCode) {		 
		return productModel.getHighestDealPercentage(skuCode);
	}

	@Override
	public List<CategoryModel> getHowtoCookitFolders() {		 
		return productModel.getHowtoCookitFolders();
	}

	@Override
	public String getKeywords() {		 
		return productModel.getKeywords();
	}

	@Override
	public EnumLayoutType getLayout() {		 
		return productModel.getLayout();
	}

	@Override
	public MediaI getMedia(String name) {		 
		return productModel.getMedia(name);
	}

	@Override
	public String getNavName() {		 
		return productModel.getNavName();
	}

	@Override
	public double getNewAge() {		 
		return productModel.getNewAge();
	}

	@Override
	public Date getNewDate() {		 
		return productModel.getNewDate();
	}

	@Override
	public List<DomainValue> getNewWineRegion() {		 
		return productModel.getNewWineRegion();
	}

	@Override
	public List<DomainValue> getNewWineType() {		 
		return productModel.getNewWineType();
	}

	@Override
	public String getPackageDescription() {		 
		return productModel.getPackageDescription();
	}

	@Override
	public Html getPartallyFrozen() {		 
		return productModel.getPartallyFrozen();
	}

	@Override
	public CategoryModel getPerfectPair() {		 
		return productModel.getPerfectPair();
	}

	@Override
	public SkuModel getPreferredSku() {		 
		return productModel.getPreferredSku();
	}

	@Override
	public double getPrice(double savingsPercentage) {		 
		return productModel.getPrice(savingsPercentage);
	}

	@Override
	public PriceCalculator getPriceCalculator() {		 
		return productModel.getPriceCalculator();
	}

	@Override
	public PriceCalculator getPriceCalculator(String skuCode) {		 
		return productModel.getPriceCalculator(skuCode);
	}

	@Override
	public PriceCalculator getPriceCalculator(SkuModel sku) {		 
		return productModel.getPriceCalculator(sku);
	}

	@Override
	public PriceCalculator getPriceCalculator(PricingContext pricingContext) {		 
		return productModel.getPriceCalculator(pricingContext);
	}

	@Override
	public PriceCalculator getPriceCalculator(String skuCode,
			PricingContext pricingContext) {		 
		return productModel.getPriceCalculator(skuCode, pricingContext);
	}

	@Override
	public PriceCalculator getPriceCalculator(SkuModel sku,
			PricingContext pricingContext) {		 
		return productModel.getPriceCalculator(sku, pricingContext);
	}

	@Override
	public String getPriceFormatted(double savingsPercentage) {		 
		return productModel.getPriceFormatted(savingsPercentage);
	}

	@Override
	public String getPriceFormatted(double savingsPercentage, String skuCode) {		 
		return productModel.getPriceFormatted(savingsPercentage, skuCode);
	}

	@Override
	public UserContext getUserContext() {		 
		return productModel.getUserContext();
	}

	@Override
	public String getPrimaryBrandName() {		 
		return productModel.getPrimaryBrandName();
	}

	@Override
	public String getPrimaryBrandName(String productName) {		 
		return productModel.getPrimaryBrandName(productName);
	}

	@Override
	public CategoryModel getPrimaryHome() {		 
		return productModel.getPrimaryHome();
	}

	@Override
	public ProductModel getPrimaryProductModel() {
		return productModel.getPrimaryProductModel();
	}

	@Override
	public List<SkuModel> getPrimarySkus() {		 
		return productModel.getPrimarySkus();
	}

	@Override
	public Image getProdImage() {		 
		return productModel.getProdImage();
	}

	@Override
	public String getProdPageRatings() {		 
		return productModel.getProdPageRatings();
	}

	@Override
	public String getProdPageTextRatings() {		 
		return productModel.getProdPageTextRatings();
	}

	@Override
	public Html getProductAbout() {		 
		return productModel.getProductAbout();
	}

	@Override
	public Html getProductBottomMedia() {		 
		return productModel.getProductBottomMedia();
	}

	@Override
	public List<ProductModel> getProductBundle() {		 
		return productModel.getProductBundle();
	}

	@Override
	public Html getProductDescription() {		 
		return productModel.getProductDescription();
	}

	@Override
	public Html getProductDescriptionNote() {		 
		return productModel.getProductDescriptionNote();
	}

	@Override
	public EnumProductLayout getProductLayout() {		 
		return productModel.getProductLayout();
	}

	@Override
	public EnumProductLayout getProductLayout(EnumProductLayout defValue) {		 
		return productModel.getProductLayout(defValue);
	}

	@Override
	public Html getProductQualityNote() {		 
		return productModel.getProductQualityNote();
	}

	@Override
	public String getProductRating() throws FDResourceException {		 
		return productModel.getProductRating();
	}

	@Override
	public String getProductRating(String skuCode) throws FDResourceException {		 
		return productModel.getProductRating(skuCode);
	}

	@Override
	public EnumOrderLineRating getProductRatingEnum()
			throws FDResourceException {		 
		return productModel.getProductRatingEnum();
	}

	@Override
	public Html getProductTerms() {		 
		return productModel.getProductTerms();
	}

	@Override
	public Html getProductTermsMedia() {		 
		return productModel.getProductTermsMedia();
	}

	@Override
	public float getQuantityIncrement() {		 
		return productModel.getQuantityIncrement();
	}

	@Override
	public float getQuantityMaximum() {	 
		return productModel.getQuantityMaximum();
	}

	@Override
	public float getQuantityMinimum() {		 
		return productModel.getQuantityMinimum();
	}

	@Override
	public String getQuantityText() {		 
		return productModel.getQuantityText();
	}

	@Override
	public String getQuantityTextSecondary() {		 
		return productModel.getQuantityTextSecondary();
	}

	@Override
	public List<DomainValue> getRating() {		 
		return productModel.getRating();
	}

	@Override
	public String getRatingProdName() {		 
		return productModel.getRatingProdName();
	}

	@Override
	public Image getRatingRelatedImage() {		 
		return productModel.getRatingRelatedImage();
	}

	@Override
	public Html getRecommendTable() {		 
		return productModel.getRecommendTable();
	}

	@Override
	public List<ContentNodeModel> getRecommendedAlternatives() {		 
		return productModel.getRecommendedAlternatives();
	}

	@Override
	public String getRedirectUrl() {		 
		return productModel.getRedirectUrl();
	}

	@Override
	public List<Recipe> getRelatedRecipes() {		 
		return productModel.getRelatedRecipes();
	}

	@Override
	public Image getRolloverImage() {		 
		return productModel.getRolloverImage();
	}

	@Override
	public Html getSalesUnitDescription() {		 
		return productModel.getSalesUnitDescription();
	}

	@Override
	public String getSalesUnitLabel() {		 
		return productModel.getSalesUnitLabel();
	}

	@Override
	public String getSeafoodOrigin() {		 
		return productModel.getSeafoodOrigin();
	}

	@Override
	public String getSeasonText() {		 
		return productModel.getSeasonText();
	}

	@Override
	public String getSellBySalesunit() {	 
		return productModel.getSellBySalesunit();
	}

	@Override
	public String getServingSuggestion() {		 
		return productModel.getServingSuggestion();
	}

	@Override
	public SkuModel getSku(int idx) {		 
		return productModel.getSku(idx);
	}

	@Override
	public SkuModel getSku(String skuCode) {		 
		return productModel.getSku(skuCode);
	}

	@Override
	public List<String> getSkuCodes() {		 
		return productModel.getSkuCodes();
	}

	@Override
	public List<SkuModel> getSkus() {		 
		return productModel.getSkus();
	}

	@Override
	public ProductModel getSourceProduct() {		 
		return productModel.getSourceProduct();
	}

	@Override
	public String getSubtitle() {		 
		return productModel.getSubtitle();
	}

	@Override
	public String getSustainabilityRating() throws FDResourceException {		 
		return productModel.getSustainabilityRating();
	}

	@Override
	public String getSustainabilityRating(String skuCode)
			throws FDResourceException {		 
		return productModel.getSustainabilityRating(skuCode);
	}

	@Override
	public EnumSustainabilityRating getSustainabilityRatingEnum()
			throws FDResourceException {		 
		return productModel.getSustainabilityRatingEnum();
	}

	@Override
	public EnumTemplateType getTemplateType() {		 
		return productModel.getTemplateType();
	}

	@Override
	public int getTemplateType(int defaultValue) {		 
		return productModel.getTemplateType(defaultValue);
	}

	@Override
	public Image getThumbnailImage() {		 
		return productModel.getThumbnailImage();
	}

	@Override
	public int getTieredDealPercentage() {		 
		return productModel.getTieredDealPercentage();
	}

	@Override
	public int getTieredDealPercentage(String skuCode) {		 
		return productModel.getTieredDealPercentage();
	}

	@Override
	public String getTieredPrice(double savingsPercentage) {		 
		return productModel.getTieredPrice(savingsPercentage);
	}

	@Override
	public DomainValue getUnitOfMeasure() {		 
		return productModel.getUnitOfMeasure();
	}

	@Override
	public List<Domain> getUsageList() {		 
		return productModel.getUsageList();
	}

	@Override
	public SkuModel getValidSkuCode(PricingContext ctx, String skuCode) {		 
		return productModel.getValidSkuCode(ctx, skuCode);
	}

	@Override
	public List<Domain> getVariationMatrix() {		 
		return productModel.getVariationMatrix();
	}

	@Override
	public List<Domain> getVariationOptions() {		 
		return productModel.getVariationOptions();
	}

	@Override
	public String getWasPriceFormatted(double savingsPercentage) {		 
		return productModel.getWasPriceFormatted(savingsPercentage);
	}

	@Override
	public List<ProductModel> getWeRecommendImage() {		 
		return productModel.getWeRecommendImage();
	}

	@Override
	public List<ProductModel> getWeRecommendText() {		 
		return productModel.getWeRecommendText();
	}

	@Override
	public String getWineAging() {		 
		return productModel.getWineAging();
	}

	@Override
	public String getWineAlchoholContent() {		 
		return productModel.getWineAlchoholContent();
	}

	@Override
	public String getWineCity() {		 
		return productModel.getWineCity();
	}

	@Override
	public String getWineClassification() {		 
		return productModel.getWineClassification();
	}

	@Override
	public List getWineClassifications() {		 
		return productModel.getWineClassifications();
	}

	@Override
	public DomainValue getWineCountry() {		 
		return productModel.getWineCountry();
	}

	@Override
	public ContentKey getWineCountryKey() {		 
		return productModel.getWineCountryKey();
	}

	@Override
	public Set<DomainValue> getWineDomainValues() {		 
		return productModel.getWineDomainValues();
	}

	@Override
	public String getWineFyi() {		 
		return productModel.getWineFyi();
	}

	@Override
	public String getWineImporter() {		 
		return productModel.getWineImporter();
	}

	@Override
	public List<DomainValue> getWineRating1() {		 
		return productModel.getWineRating1();
	}

	@Override
	public List<DomainValue> getWineRating2() {		 
		return productModel.getWineRating2();
	}

	@Override
	public List<DomainValue> getWineRating3() {		 
		return productModel.getWineRating3();
	}

	@Override
	public DomainValue getWineRatingValue1() {		 
		return productModel.getWineRatingValue1();
	}

	@Override
	public DomainValue getWineRatingValue2() {		 
		return productModel.getWineRatingValue2();
	}

	@Override
	public DomainValue getWineRatingValue3() {		 
		return productModel.getWineRatingValue3();
	}

	@Override
	public String getWineRegion() {		 
		return productModel.getWineRegion();
	}

	@Override
	public Html getWineReview1() {		 
		return productModel.getWineReview1();
	}

	@Override
	public Html getWineReview2() {		 
		return productModel.getWineReview2();
	}

	@Override
	public Html getWineReview3() {		 
		return productModel.getWineReview3();
	}

	@Override
	public String getWineType() {		 
		return productModel.getWineType();
	}

	@Override
	public List<DomainValue> getWineVarietal() {		 
		return productModel.getWineVarietal();
	}

	@Override
	public List<DomainValue> getWineVintage() {		 
		return productModel.getWineVintage();
	}

	@Override
	public List<CategoryModel> getYmalCategories() {		 
		return productModel.getYmalCategories();
	}

	@Override
	public List<ProductModel> getYmalProducts() {		 
		return productModel.getYmalProducts();
	}

	@Override
	public List<ProductModel> getYmalProducts(Set<FDSku> removeSkus) {		 
		return productModel.getYmalProducts(removeSkus);
	}

	@Override
	public List<Recipe> getYmalRecipes() {		 
		return productModel.getYmalRecipes();
	}

	@Override
	public List<ContentNodeModel> getYmals() {		 
		return productModel.getYmals();
	}

	@Override
	public Image getZoomImage() {		 
		return productModel.getZoomImage();
	}

	@Override
	public boolean hasComponentGroups() {		 
		return productModel.hasComponentGroups();
	}

	@Override
	public boolean hasTerms() {		 
		return productModel.hasTerms();
	}

	@Override
	public boolean hasWineOtherRatings() {		 
		return productModel.hasWineOtherRatings();
	}

	@Override
	public boolean isAutoconfigurable() {		 
		return productModel.isAutoconfigurable();
	}

	@Override
	public boolean isBackInStock() {		 
		return productModel.isBackInStock();
	}

	@Override
	public boolean isCharacteristicsComponentsAvailable(FDConfigurableI config) {		 
		return productModel.isCharacteristicsComponentsAvailable(config);
	}

	@Override
	public boolean isDisplayableBasedOnCms() {		 
		return productModel.isDisplayableBasedOnCms();
	}

	@Override
	public boolean isExcludedRecommendation() {		 
		return productModel.isExcludedRecommendation();
	}

	@Override
	public boolean isFrozen() {		 
		return productModel.isFrozen();
	}

	@Override
	public boolean isFullyAvailable() {		 
		return productModel.isFullyAvailable();
	}

	@Override
	public boolean isGrocery() {		 
		return productModel.isGrocery();
	}

	@Override
	public boolean isHasPartiallyFrozen() {		 
		return productModel.isHasPartiallyFrozen();
	}

	@Override
	public boolean isHasSalesUnitDescription() {		 
		return productModel.isHasSalesUnitDescription();
	}

	@Override
	public boolean isHideIphone() {		 
		return productModel.isHideIphone();
	}

	@Override
	public boolean isHideWineRatingPricing() {		 
		return productModel.isHideWineRatingPricing();
	}

	@Override
	public boolean isInPrimaryHome() {		 
		return productModel.isInPrimaryHome();
	}

	@Override
	public boolean isIncrementMaxEnforce() {		 
		return productModel.isIncrementMaxEnforce();
	}

	@Override
	public boolean isInvisible() {		 
		return productModel.isInvisible();
	}

	@Override
	public boolean isNew() {		 
		return productModel.isNew();
	}

	@Override
	public boolean isNotSearchable() {		 
		return productModel.isNotSearchable();
	}

	@Override
	public boolean isNutritionMultiple() {		 
		return productModel.isNutritionMultiple();
	}

	@Override
	public boolean isPerishable() {		 
		return productModel.isPerishable();
	}

	@Override
	public boolean isPlatter() {		 
		return productModel.isPlatter();
	}

	@Override
	public boolean isPreconfigured() {		 
		return productModel.isPreconfigured();
	}

	@Override
	public boolean isQualifiedForPromotions() throws FDResourceException {		 
		return productModel.isQualifiedForPromotions();
	}

	@Override
	public boolean isShowSalesUnitImage() {		 
		return productModel.isShowSalesUnitImage();
	}

	@Override
	public boolean isShowTopTenImage() {		 
		return productModel.isShowTopTenImage();
	}

	@Override
	public boolean isShowWineRatings() {		 
		return productModel.isShowWineRatings();
	}

	@Override
	public boolean isSoldBySalesUnits() {		 
		return productModel.isSoldBySalesUnits();
	}

	@Override
	public boolean isTemporaryUnavailableOrAvailable() {		 
		return productModel.isTemporaryUnavailableOrAvailable();
	}

	@Override
	public boolean showDefaultSustainabilityRating() {		 
		return productModel.showDefaultSustainabilityRating();
	}

	@Override
	public Date getEarliestAvailability() {		 
		return productModel.getEarliestAvailability();
	}

	

	@Override
	public boolean isAvailableWithin(int days) {		 
		return productModel.isAvailableWithin(days);
	}

	@Override
	public boolean isDiscontinued() {		 
		return productModel.isDiscontinued();
	}

	@Override
	public boolean isOutOfSeason() {		 
		return productModel.isOutOfSeason();
	}

	@Override
	public boolean isTempUnavailable() {		 
		return productModel.isTempUnavailable();
	}

	@Override
	public boolean isUnavailable() {		 
		return productModel.isUnavailable();
	}

	@Override
	public List<ProductModel> getRelatedProducts() {		 
		return productModel.getRelatedProducts();
	}

	@Override
	public String getYmalHeader() {		 
		return productModel.getYmalHeader();
	}

	@Override
	public void resetActiveYmalSetSession() {		 
		productModel.resetActiveYmalSetSession();
	}

	@Override
	public AttributeDefI getAttributeDef(String name) {		 
		return productModel.getAttributeDef(name);
	}

	@Override
	public Object getCmsAttributeValue(String name) {		 
		return productModel.getCmsAttributeValue(name);
	}

	@Override
	public ContentKey getContentKey() {		 
		return productModel.getContentKey();
	}

	@Override
	public String getContentName() {		 
		return productModel.getContentName();
	}

	@Override
	public String getContentType() {		 
		return productModel.getContentType();
	}

	@Override
	public Html getEditorial() {		 
		return productModel.getEditorial();
	}

	@Override
	public String getEditorialTitle() {		 
		return productModel.getEditorialTitle();
	}

	@Override
	public Object getNotInheritedAttributeValue(String name) {		 
		return productModel.getNotInheritedAttributeValue(name);
	}

	@Override
	public String getParentId() {
		return productModel.getParentId();
	}

	@Override
	public Collection<ContentKey> getParentKeys() {		 
		return productModel.getParentKeys();
	}

	@Override
	public ContentNodeModel getParentNode() {		 
		return productModel.getParentNode();
	}

	@Override
	public String getPath() {		 
		return productModel.getPath();
	}

	@Override
	public Image getSideNavImage() {		 
		return productModel.getSideNavImage();
	}

	@Override
	public boolean hasParentWithName(String[] contentNames) {		 
		return productModel.hasParentWithName(contentNames);
	}

	@Override
	public boolean isHidden() {		 
		return productModel.isHidden();
	}

	@Override
	public boolean isOrphan() {
		return productModel.isOrphan();
	}

	@Override
	public boolean isSearchable() {		 
		return productModel.isSearchable();
	}

	@Override
	public int getPriority() {		 
		return productModel.getPriority();
	}

	@Override
	public YmalSetSource getParentYmalSetSource() {		 
		return productModel.getParentYmalSetSource();
	}

	@Override
	public List<YmalSet> getYmalSets() {		 
		return productModel.getYmalSets();
	}

	@Override
	public boolean hasActiveYmalSets() {		 
		return productModel.hasActiveYmalSets();
	}

	@Override
    public Object clone() {
		return productModel.clone();
	}
	
	@Override
    public boolean isExcludedForEBTPayment(){
		return productModel.isExcludedForEBTPayment();
	}

	@Override
	public boolean isDisabledRecommendations() {
		return productModel.isDisabledRecommendations();
	}

	@Override
	public boolean isRetainOriginalSkuOrder() {
		return productModel.isRetainOriginalSkuOrder();
	}

	@Override
	public EnumOrderLineRating getProductRatingEnum(String skuCode) throws FDResourceException {
		return productModel.getProductRatingEnum(skuCode);
	}

	@Override
	public Image getPackageImage() {
		return productModel.getPackageImage();
	}
	
	@Override
	public List<TagModel> getTags() {
		return productModel.getTags();
	}

	@Override
	public Set<TagModel> getAllTags() {
		return productModel.getAllTags();
	}

	
	@Override
	public Set<DomainValue> getAllDomainValues() {
		return productModel.getAllDomainValues();
	}



	/**
	 * @see {@link ProductModel#getUpSellProducts()}
	 */
	@Override
	public List<ProductModel> getUpSellProducts() {
		return productModel.getUpSellProducts();
	}

	/**
	 * @see {@link ProductModel#getCrossSellProducts()}
	 */
	@Override
	public List<ProductModel> getCrossSellProducts() {
		return productModel.getCrossSellProducts();
	}
	
	@Override
	public String getBrowseRecommenderType(){
		return productModel.getBrowseRecommenderType();
	}

	/**
	 * @see {@link ProductModel#getHeatRating()}
	 */
	@Override
	public int getHeatRating() {
		return productModel.getHeatRating();
	}

	@Override
	public Image getJumboImage() {
		return productModel.getJumboImage();
	}

	@Override
	public Image getItemImage() {
		return productModel.getItemImage();
	}

	@Override
	public Image getExtraImage() {
		return productModel.getExtraImage();
	}
	
	@Override
	public boolean isDisableAtpFailureRecommendation(){
		return productModel.isDisableAtpFailureRecommendation();
	}


	@Override
	public EnumProductLayout getSpecialLayout() {
		return productModel.getSpecialLayout();
	}

	@Override
	public List<ProductModel> getCompleteTheMeal() {
		return productModel.getCompleteTheMeal();
	}

	@Override
    public List<ProductModel> getIncludeProducts() {
        return productModel.getIncludeProducts();
    }

    @Override
	public String getPageTitle() {
		return productModel.getPageTitle();
	}

	@Override
	public String getSEOMetaDescription() {
		return productModel.getSEOMetaDescription();
	}

	@Override
	public String getPairItHeading() {
		return productModel.getPairItHeading();
	}

	@Override
	public String getPairItText() {
		return productModel.getPairItText();
	}
}
