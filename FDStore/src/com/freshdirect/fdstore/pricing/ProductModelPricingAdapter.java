package com.freshdirect.fdstore.pricing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.freshdirect.cms.AttributeDefI;
import com.freshdirect.cms.ContentKey;
import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.content.nutrition.ErpNutritionInfoType;
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

public class ProductModelPricingAdapter implements ProductModel, Serializable,
		Cloneable, PrioritizedI {

	private static final long serialVersionUID = -6112229358347075169L;

	private final UserContext userCtx;
	private final ProductModel prodModel;

	public ProductModelPricingAdapter(ProductModel pModel) {
		if (pModel == null) {
			throw new IllegalArgumentException("product model cannot be null");
		}
		this.prodModel = pModel;
		this.userCtx = pModel.getUserContext();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ProductModel) {
			return getContentKey().equals(((ProductModel) obj).getContentKey());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getContentKey().hashCode();
	}

	@Override
	public boolean enforceQuantityMax() {
		return false;
	}

	@Override
    public int getPriority() {
		return ((PrioritizedI) prodModel).getPriority();
	}

	@Override
	public SkuModel getValidSkuCode(PricingContext ctx, String skuCode) {
		return prodModel.getValidSkuCode(ctx, skuCode);
	}

	@Override
    public PriceCalculator getPriceCalculator() {
		return new PriceCalculator(userCtx.getPricingContext(), prodModel, prodModel
				.getDefaultSku(userCtx.getPricingContext()));
	}

	@Override
    public PriceCalculator getPriceCalculator(String skuCode) {
		return new PriceCalculator(userCtx.getPricingContext(), prodModel, prodModel
				.getValidSkuCode(userCtx.getPricingContext(), skuCode));
	}
	
	@Override
	public PriceCalculator getPriceCalculator(SkuModel sku) {
	    return new PriceCalculator(userCtx.getPricingContext(), prodModel, sku);
	}

	@Override
    public PriceCalculator getPriceCalculator(PricingContext pricingContext) {
		return new PriceCalculator(pricingContext, prodModel, prodModel
				.getDefaultSku(pricingContext));
	}

	@Override
    public PriceCalculator getPriceCalculator(String skuCode, PricingContext pricingContext) {
		return new PriceCalculator(pricingContext, prodModel, prodModel
				.getValidSkuCode(pricingContext, skuCode));
	}
	
	@Override
	public PriceCalculator getPriceCalculator(SkuModel sku, PricingContext pricingContext) {
	    return new PriceCalculator(pricingContext, prodModel, sku);
	}
	
	/* price calculator calls */

    /**
     * use priceCalculator which can be cached for a request. 
     * @return
     */
    @Deprecated
	public double getDefaultPriceValue() {
		return getPriceCalculator().getDefaultPriceValue();
	}

    /**
     * use priceCalculator which can be cached for a request. 
     * @return
     */
    @Deprecated
    @Override
	public String getDefaultPrice() {
		return getPriceCalculator().getDefaultPrice();
	}

    /**
     * use priceCalculator which can be cached for a request. 
     * @return
     */
    @Deprecated
	public String getDefaultPriceOnly() {
		return getPriceCalculator().getDefaultPriceOnly();
	}

    /**
     * use priceCalculator which can be cached for a request. 
     * @return
     */
    @Deprecated
	public String getDefaultUnitOnly() {
		return getPriceCalculator().getDefaultUnitOnly();
	}

    /**
     * use priceCalculator which can be cached for a request. 
     * @return
     */
    @Deprecated
    @Override
	public int getDealPercentage(String skuCode) {
		return getPriceCalculator(skuCode).getDealPercentage();
	}

    /**
     * use priceCalculator which can be cached for a request. 
     * @return
     */
    @Deprecated
    @Override
	public int getTieredDealPercentage(String skuCode) {
		return getPriceCalculator(skuCode).getTieredDealPercentage();
	}

    /**
     * use priceCalculator which can be cached for a request. 
     * @return
     */
    @Deprecated
    @Override
	public int getHighestDealPercentage(String skuCode) {
		return getPriceCalculator(skuCode).getHighestDealPercentage();
	}

    /**
     * use priceCalculator which can be cached for a request. 
     * @return
     */
    @Deprecated
    @Override
	public String getTieredPrice(double savingsPercentage) {
		return getPriceCalculator().getTieredPrice(savingsPercentage);
	}

	    /**
	     * use priceCalculator which can be cached for a request. 
	     * @return
	     */
	    @Deprecated
	    @Override
	public double getPrice(double savingsPercentage) {
		return getPriceCalculator().getPrice(savingsPercentage);
	}

        /**
         * use priceCalculator which can be cached for a request. 
         * @return
         */
        @Deprecated
        @Override
	public String getPriceFormatted(double savingsPercentage) {
		return getPriceCalculator().getPriceFormatted(savingsPercentage);
	}

        /**
         * use priceCalculator which can be cached for a request. 
         * @return
         */
        @Deprecated
        @Override
	public String getWasPriceFormatted(double savingsPercentage) {
		return getPriceCalculator().getWasPriceFormatted(savingsPercentage);
	}

        /**
         * use priceCalculator which can be cached for a request. 
         * @return
         */
        @Deprecated
        @Override
	public String getAboutPriceFormatted(double savingsPercentage) {
		return getPriceCalculator().getAboutPriceFormatted(savingsPercentage);
	}

        /**
         * use priceCalculator which can be cached for a request. 
         * @return
         */
        @Deprecated
	public String getKosherSymbol() throws FDResourceException {
		return getPriceCalculator().getKosherSymbol();
	}

        /**
         * use priceCalculator which can be cached for a request. 
         * @return
         */
        @Deprecated
	public String getKosherType() throws FDResourceException {
		return getPriceCalculator().getKosherType();
	}

        /**
         * use priceCalculator which can be cached for a request. 
         * @return
         */
        @Deprecated
	public boolean isKosherProductionItem() throws FDResourceException {
		return getPriceCalculator().isKosherProductionItem();
	}

        /**
         * use priceCalculator which can be cached for a request. 
         * @return
         */
        @Deprecated
	public int getKosherPriority() throws FDResourceException {
		return getPriceCalculator().getKosherPriority();
	}

	/* end of the price calculator calls */

	@Override
	public YmalSet getActiveYmalSet() {
		return this.prodModel.getActiveYmalSet();
	}

	@Override
	public String getAka() {
		return this.prodModel.getAka();
	}

	@Override
	public ProductModel getAlsoSoldAs(int idx) {
		return this.prodModel.getAlsoSoldAs(idx);
	}

	@Override
	public List<ProductModel> getAlsoSoldAs() {
		return this.prodModel.getAlsoSoldAs();
	}

	@Override
	public String getAlsoSoldAsName() {
		return this.prodModel.getAlsoSoldAsName();
	}

	@Override
	public List<ProductModel> getAlsoSoldAsRefs() {
		return this.prodModel.getAlsoSoldAsRefs();
	}

	@Override
	public String getAltText() {
		return this.prodModel.getAltText();
	}

	@Override
	public Image getAlternateImage() {
		return this.prodModel.getAlternateImage();
	}

	@Override
	public Image getAlternateProductImage() {
		return this.prodModel.getAlternateProductImage();
	}

	/**
	 * Return the auto-configuration for the product, if applicable.
	 * 
	 * @return the configuration describing the auto-configuration of the product,
	 *         or null if the product can not be auto-configured.
	 */
	@Override
    public FDConfigurableI getAutoconfiguration() {
		FDConfigurableI ret = null;
		SkuModel sku = getDefaultSku();
		if (sku != null) {
			try {
				boolean soldBySalesUnits = isSoldBySalesUnits();
				if (sku.getProduct().isAutoconfigurable(soldBySalesUnits)) {
					ret = sku.getProduct().getAutoconfiguration(soldBySalesUnits,
							getQuantityMinimum());
				}
			} catch (Exception exc) {
			}
		}
		return ret;
	}

	@Override
	public DayOfWeekSet getBlockedDays() {
		return this.prodModel.getBlockedDays();
	}

	@Override
	public String getBlurb() {
		return this.prodModel.getBlurb();
	}

	@Override
	public List<BrandModel> getBrands() {
		return this.prodModel.getBrands();
	}

	@Override
	public Image getCategoryImage() {
		return this.prodModel.getCategoryImage();
	}

	@Override
	public Set getCommonNutritionInfo(ErpNutritionInfoType type)
			throws FDResourceException {
		return this.prodModel.getCommonNutritionInfo(type);
	}

	@Override
	public List<ComponentGroupModel> getComponentGroups() {
		return this.prodModel.getComponentGroups();
	}

	@Override
	public Image getConfirmImage() {
		return this.prodModel.getConfirmImage();
	}

	@Override
	public Double getContainerWeightHalfPint() {
		return this.prodModel.getContainerWeightHalfPint();
	}

	@Override
	public Double getContainerWeightPint() {
		return this.prodModel.getContainerWeightPint();
	}

	@Override
	public Double getContainerWeightQuart() {
		return this.prodModel.getContainerWeightQuart();
	}

	@Override
	public List<String> getCountryOfOrigin() throws FDResourceException {
		return this.prodModel.getCountryOfOrigin();
	}

    /**
     * use priceCalculator which can be cached for a request. 
     * @return
     */
    @Deprecated
    @Override
	public int getDealPercentage() {
		return getDealPercentage(null);
	}

	/**
	 * 
	 * Returns the preferred SKU for a product if defined. Otherwise returns the
	 * minimum price SKU among the available SKUs for the product. If the product
	 * only has one available SKU, that will be returned.
	 * 
	 * @return the preferred SKU for the product, or the minimally priced SKU that
	 *         is available. if no SKUs are available, returns null.
	 */
	@Override
    public SkuModel getDefaultSku() {
		SkuModel defaultSku = this.prodModel.getDefaultSku(userCtx.getPricingContext());
		return defaultSku != null ? new SkuModelPricingAdapter(defaultSku,
				userCtx.getPricingContext()) : null;
	}

	@Override
	public SkuModel getDefaultSku(PricingContext ctx) {
		return prodModel.getDefaultSku(ctx);
	}
	
	@Override
	public SkuModel getDefaultTemporaryUnavSku() {
		return prodModel.getDefaultTemporaryUnavSku();
	}

	@Override
	public DepartmentModel getDepartment() {
		return this.prodModel.getDepartment();
	}

	@Override
	public CategoryModel getCategory() {
		return prodModel.getCategory();
	}

	@Override
	public Image getDescriptiveImage() {
		return this.prodModel.getDescriptiveImage();
	}

	@Override
	public Image getDetailImage() {
		return this.prodModel.getDetailImage();
	}

	@Override
	public List getDisplayableBrands() {
		return this.prodModel.getDisplayableBrands();
	}

	@Override
	public List getDisplayableBrands(int numberOfBrands) {
		return this.prodModel.getDisplayableBrands(numberOfBrands);
	}

	@Override
	public List<Html> getDonenessGuide() {
		return this.prodModel.getDonenessGuide();
	}

	@Override
	public int getExpertWeight() {
		return this.prodModel.getExpertWeight();
	}

	@Override
	public Html getFddefFrenching() {
		return this.prodModel.getFddefFrenching();
	}

	@Override
	public Html getFddefGrade() {
		return this.prodModel.getFddefGrade();
	}

	@Override
	public Html getFddefRipeness() {
		return this.prodModel.getFddefRipeness();
	}

	@Override
	public Image getFeatureImage() {
		return this.prodModel.getFeatureImage();
	}

	@Override
	public Html getFreshTips() {
		return this.prodModel.getFreshTips();
	}

	@Override
	public String getFullName() {
		return this.prodModel.getFullName();
	}

	@Override
	public List getGiftcardType() {
		return this.prodModel.getGiftcardType();
	}

	@Override
	public String getGlanceName() {
		return this.prodModel.getGlanceName();
	}

	@Override
	public String getHideUrl() {
		return this.prodModel.getHideUrl();
	}

	@Deprecated
	@Override
	public int getHighestDealPercentage() {
		return getHighestDealPercentage(null);
	}

	@Override
	public List<CategoryModel> getHowtoCookitFolders() {
		return this.prodModel.getHowtoCookitFolders();
	}

	@Override
	public String getKeywords() {
		return this.prodModel.getKeywords();
	}

	@Override
	public EnumLayoutType getLayout() {
		return this.prodModel.getLayout();
	}

	@Override
	public String getNavName() {
		return this.prodModel.getNavName();
	}

	@Override
	public List<DomainValue> getNewWineRegion() {
		return this.prodModel.getNewWineRegion();
	}

	@Override
	public List getNewWineType() {
		return this.prodModel.getNewWineType();
	}

	@Override
	public String getPackageDescription() {
		return this.prodModel.getPackageDescription();
	}

	@Override
	public Html getPartallyFrozen() {
		return this.prodModel.getPartallyFrozen();
	}

	@Override
	public CategoryModel getPerfectPair() {
		return this.prodModel.getPerfectPair();
	}

	@Override
	public SkuModel getPreferredSku() {
		return new SkuModelPricingAdapter(this.prodModel.getPreferredSku(),
				userCtx.getPricingContext());
	}

	@Override
	public String getPrimaryBrandName() {
		return this.prodModel.getPrimaryBrandName();
	}

	@Override
	public String getPrimaryBrandName(String productName) {
		return this.prodModel.getPrimaryBrandName(productName);
	}

	@Override
	public CategoryModel getPrimaryHome() {
		return this.prodModel.getPrimaryHome();
	}

	@Override
	public Image getProdImage() {
		return this.prodModel.getProdImage();
	}

	@Override
	public String getProdPageRatings() {
		return this.prodModel.getProdPageRatings();
	}

	@Override
	public String getProdPageTextRatings() {
		return this.prodModel.getProdPageTextRatings();
	}

	@Override
	public Html getProductAbout() {
		return this.prodModel.getProductAbout();
	}

	@Override
	public Html getProductBottomMedia() {
		return this.prodModel.getProductBottomMedia();
	}

	@Override
	public List<ProductModel> getProductBundle() {
		return this.prodModel.getProductBundle();
	}

	@Override
	public Html getProductDescription() {
		return this.prodModel.getProductDescription();
	}

	@Override
	public Html getProductDescriptionNote() {
		return this.prodModel.getProductDescriptionNote();
	}

	@Override
	public EnumProductLayout getProductLayout() {
		return this.prodModel.getProductLayout();
	}

	@Override
	public Html getProductQualityNote() {
		return this.prodModel.getProductQualityNote();
	}

	@Override
	public String getProductRating() throws FDResourceException {
		return this.prodModel.getProductRating();
	}

	@Override
	public EnumOrderLineRating getProductRatingEnum() throws FDResourceException {
		return this.prodModel.getProductRatingEnum();
	}

	@Override
	public Html getProductTerms() {
		return this.prodModel.getProductTerms();
	}

	@Override
	public Html getProductTermsMedia() {
		return this.prodModel.getProductTermsMedia();
	}

	@Override
	public float getQuantityIncrement() {
		return this.prodModel.getQuantityIncrement();
	}

	@Override
	public float getQuantityMaximum() {
		return this.prodModel.getQuantityMaximum();
	}

	@Override
	public float getQuantityMinimum() {
		return this.prodModel.getQuantityMinimum();
	}

	@Override
	public String getQuantityText() {
		return this.prodModel.getQuantityText();
	}

	@Override
	public String getQuantityTextSecondary() {
		return this.prodModel.getQuantityTextSecondary();
	}

	@Override
	public List<DomainValue> getRating() {
		return this.prodModel.getRating();
	}

	@Override
	public String getRatingProdName() {
		return this.prodModel.getRatingProdName();
	}

	@Override
	public Image getRatingRelatedImage() {
		return this.prodModel.getRatingRelatedImage();
	}

	@Override
	public Html getRecommendTable() {
		return this.prodModel.getRecommendTable();
	}

	@Override
	public List<ContentNodeModel> getRecommendedAlternatives() {
		return this.prodModel.getRecommendedAlternatives();
	}

	@Override
	public String getRedirectUrl() {
		return this.prodModel.getRedirectUrl();
	}

	@Override
	public List<Recipe> getRelatedRecipes() {
		return this.prodModel.getRelatedRecipes();
	}

	@Override
	public Image getRolloverImage() {
		return this.prodModel.getRolloverImage();
	}

	@Override
	public Html getSalesUnitDescription() {
		return this.prodModel.getSalesUnitDescription();
	}

	@Override
	public String getSalesUnitLabel() {
		return this.prodModel.getSalesUnitLabel();
	}

	@Override
	public String getSeafoodOrigin() {
		return this.prodModel.getSeafoodOrigin();
	}

	@Override
	public String getSeasonText() {
		return this.prodModel.getSeasonText();
	}

	@Override
	public String getSellBySalesunit() {
		return this.prodModel.getSellBySalesunit();
	}

	@Override
	public String getServingSuggestion() {
		return this.prodModel.getServingSuggestion();
	}

	@Override
	public SkuModel getSku(int idx) {
		return getSkus().get(idx);
	}

	@Override
	public SkuModel getSku(String skuCode) {
		List<SkuModel> skus = getSkus();
		for (SkuModel s : skus) {
			if (s.getSkuCode().equalsIgnoreCase(skuCode)) {
				return s;
			}
		}
		return null;
	}

	@Override
	public List<String> getSkuCodes() {
		return this.prodModel.getSkuCodes();
	}

	/**
	 * Getter for property skus.
	 * 
	 * @return Value of property skus.
	 */
	@Override
    public List<SkuModel> getSkus() {
		List<SkuModel> skuModels = prodModel.getSkus();
		List<SkuModel> skuAdapters = new ArrayList<SkuModel>(skuModels.size());
		for (Iterator<SkuModel> it = skuModels.iterator(); it.hasNext();) {
			SkuModel sku = it.next();
			skuAdapters.add(new SkuModelPricingAdapter(sku, userCtx.getPricingContext()));
		}
		return skuAdapters;

	}

	@Override
	public ProductModel getSourceProduct() {
		return this.prodModel.getSourceProduct();
	}

	@Override
	public String getSubtitle() {
		return this.prodModel.getSubtitle();
	}

	@Override
	public EnumTemplateType getTemplateType() {
		return this.prodModel.getTemplateType();
	}

	@Override
	public Image getThumbnailImage() {
		return this.prodModel.getThumbnailImage();
	}

	@Override
	@Deprecated
	public int getTieredDealPercentage() {
		return getTieredDealPercentage(null);
	}

	@Override
	public DomainValue getUnitOfMeasure() {
		return this.prodModel.getUnitOfMeasure();
	}

	@Override
	public List<Domain> getUsageList() {
		return this.prodModel.getUsageList();
	}

	@Override
	public List<Domain> getVariationMatrix() {
		return this.prodModel.getVariationMatrix();
	}

	@Override
	public List<Domain> getVariationOptions() {
		return this.prodModel.getVariationOptions();
	}

	@Override
	public List<ProductModel> getWeRecommendImage() {
		return this.prodModel.getWeRecommendImage();
	}

	@Override
	public List<ProductModel> getWeRecommendText() {
		return this.prodModel.getWeRecommendText();
	}

	@Override
	public String getWineAging() {
		return this.prodModel.getWineAging();
	}

	@Override
	public String getWineAlchoholContent() {
		return this.prodModel.getWineAlchoholContent();
	}

	@Override
	public String getWineCity() {
		return this.prodModel.getWineCity();
	}

	@Override
	public String getWineClassification() {
		return this.prodModel.getWineClassification();
	}

	@Override
	public List getWineClassifications() {
		return this.prodModel.getWineClassifications();
	}

	@Override
	public DomainValue getWineCountry() {
		return this.prodModel.getWineCountry();
	}

	@Override
	public String getWineFyi() {
		return this.prodModel.getWineFyi();
	}

	@Override
	public String getWineImporter() {
		return this.prodModel.getWineImporter();
	}

	@Override
	public List<DomainValue> getWineRating1() {
		return this.prodModel.getWineRating1();
	}

	@Override
	public List<DomainValue> getWineRating2() {
		return this.prodModel.getWineRating2();
	}

	@Override
	public List<DomainValue> getWineRating3() {
		return this.prodModel.getWineRating3();
	}

	@Override
	public DomainValue getWineRatingValue1() {
		return this.prodModel.getWineRatingValue1();
	}

	@Override
	public DomainValue getWineRatingValue2() {
		return this.prodModel.getWineRatingValue2();
	}

	@Override
	public DomainValue getWineRatingValue3() {
		return this.prodModel.getWineRatingValue3();
	}

	@Override
	public boolean hasWineOtherRatings() {
		return this.prodModel.hasWineOtherRatings();
	}

	@Override
	public String getWineRegion() {
		return this.prodModel.getWineRegion();
	}

	@Override
	public Html getWineReview1() {
		return this.prodModel.getWineReview1();
	}

	@Override
	public Html getWineReview2() {
		return this.prodModel.getWineReview2();
	}

	@Override
	public Html getWineReview3() {
		return this.prodModel.getWineReview3();
	}

	@Override
	public String getWineType() {
		return this.prodModel.getWineType();
	}

	@Override
	public List getWineVarietal() {
		return this.prodModel.getWineVarietal();
	}

	@Override
	public List<DomainValue> getWineVintage() {
		return this.prodModel.getWineVintage();
	}

	@Override
	public List<CategoryModel> getYmalCategories() {
		return this.prodModel.getYmalCategories();
	}

	@Override
	public List<ProductModel> getYmalProducts() {
		return this.prodModel.getYmalProducts();
	}

	@Override
	public List<ProductModel> getYmalProducts(Set<FDSku> removeSkus) {
		return this.prodModel.getYmalProducts(removeSkus);
	}

	@Override
	public List<Recipe> getYmalRecipes() {
		return this.prodModel.getYmalRecipes();
	}

	@Override
	public List<ContentNodeModel> getYmals() {
		return this.prodModel.getYmals();
	}

	@Override
	public Image getZoomImage() {
		// Fix for APPDEV-847
		return this.prodModel.getZoomImage();
	}

	@Override
	public boolean hasComponentGroups() {
		return this.prodModel.hasComponentGroups();
	}

	@Override
	public boolean hasTerms() {
		return this.prodModel.hasTerms();
	}

	@Override
	public boolean isHideIphone() {
		return this.prodModel.isHideIphone();
	}

	@Override
	public boolean isAutoconfigurable() {
		return this.prodModel.isAutoconfigurable();
	}

	@Override
	public boolean isCharacteristicsComponentsAvailable(FDConfigurableI config) {
		return this.prodModel.isCharacteristicsComponentsAvailable(config);
	}

	@Override
	public boolean isDisplayableBasedOnCms() {
		return this.prodModel.isDisplayableBasedOnCms();
	}

	@Override
	public boolean isExcludedRecommendation() {
		return this.prodModel.isExcludedRecommendation();
	}

	@Override
	public boolean isFrozen() {
		return this.prodModel.isFrozen();
	}

	@Override
	public boolean isGrocery() {
		return this.prodModel.isGrocery();
	}

	@Override
	public boolean isIncrementMaxEnforce() {
		return this.prodModel.isIncrementMaxEnforce();
	}

	@Override
	public boolean isInvisible() {
		return this.prodModel.isInvisible();
	}

	@Override
	public boolean isNew() {
		return this.prodModel.isNew();
	}

	@Override
	public double getAge() {
		return this.prodModel.getAge();
	}

	@Override
	public double getNewAge() {
		return this.prodModel.getNewAge();
	}

	@Override
	public Date getNewDate() {
		return this.prodModel.getNewDate();
	}

	@Override
	public boolean isBackInStock() {
		return this.prodModel.isBackInStock();
	}

	@Override
	public double getBackInStockAge() {
		return this.prodModel.getBackInStockAge();
	}

	@Override
	public Date getBackInStockDate() {
		return this.prodModel.getBackInStockDate();
	}

	@Override
	public boolean isNotSearchable() {
		return this.prodModel.isNotSearchable();
	}

	@Override
	public boolean isNutritionMultiple() {
		return this.prodModel.isNutritionMultiple();
	}

	@Override
	public boolean isPerishable() {
		return this.prodModel.isPerishable();
	}

	@Override
	public boolean isPlatter() {
		return this.prodModel.isPlatter();
	}

	@Override
	public boolean isPreconfigured() {
		return this.prodModel.isPreconfigured();
	}

	@Override
	public boolean isQualifiedForPromotions() throws FDResourceException {
		return this.prodModel.isQualifiedForPromotions();
	}

	@Override
	public boolean isShowSalesUnitImage() {
		return this.prodModel.isShowSalesUnitImage();
	}

	@Override
	public boolean isShowTopTenImage() {
		return this.prodModel.isShowTopTenImage();
	}

	@Override
	public boolean isSoldBySalesUnits() {
		return this.prodModel.isSoldBySalesUnits();
	}

	@Override
	public Html getEditorial() {
		return this.prodModel.getEditorial();
	}

	@Override
	public String getEditorialTitle() {
		return this.prodModel.getEditorialTitle();
	}

	@Override
	public Collection<ContentKey> getParentKeys() {
		return this.prodModel.getParentKeys();
	}

	@Override
	public String getPath() {
		return this.prodModel.getPath();
	}

	@Override
	public boolean isHidden() {
		return this.prodModel.isHidden();
	}

	@Override
	public boolean isOrphan() {
		return this.prodModel.isOrphan();
	}

	@Override
	public boolean isSearchable() {
		return this.prodModel.isSearchable();
	}

	@Override
	public ContentKey getContentKey() {
		return this.prodModel.getContentKey();
	}

	@Override
	public String getContentName() {
		return this.prodModel.getContentName();
	}

	@Override
	public String getContentType() {
		return this.prodModel.getContentType();
	}

	@Override
	public ContentNodeModel getParentNode() {
		return this.prodModel.getParentNode();
	}

	@Override
	public boolean hasParentWithName(String[] contentNames) {
		return this.prodModel.hasParentWithName(contentNames);
	}

	@Override
	public Date getEarliestAvailability() {
		return this.prodModel.getEarliestAvailability();
	}

	@Override
	public boolean isAvailableWithin(int days) {
		return this.prodModel.isAvailableWithin(days);
	}

	@Override
	public boolean isDiscontinued() {
		return this.prodModel.isDiscontinued();
	}

	@Override
	public boolean isOutOfSeason() {
		return this.prodModel.isOutOfSeason();
	}

	@Override
	public boolean isTempUnavailable() {
		return this.prodModel.isTempUnavailable();
	}

	@Override
	public boolean isUnavailable() {
		return this.prodModel.isUnavailable();
	}
	

	@Override
	public List<ProductModel> getRelatedProducts() {
		return this.prodModel.getRelatedProducts();
	}

	@Override
	public String getYmalHeader() {
		return this.prodModel.getYmalHeader();
	}

	@Override
	public void resetActiveYmalSetSession() {
		this.prodModel.resetActiveYmalSetSession();
	}

	@Override
	public List<YmalSet> getYmalSets() {
		return this.prodModel.getYmalSets();
	}
	
	@Override
	public boolean hasActiveYmalSets() {
		return this.prodModel.hasActiveYmalSets();
	}
	
	@Override
	public YmalSetSource getParentYmalSetSource() {
		return this.prodModel.getParentYmalSetSource();
	}
	
	@Override
    public Object clone() {
		return prodModel.clone();
	}

	/**
	 * Very conveniently returns contentName.
	 */
	@Override
    public String toString() {
		return this.getContentName();
	}

	@Override
    public UserContext getUserContext() {
		return this.userCtx;
	}

	@Override
	public EnumSustainabilityRating getSustainabilityRatingEnum()throws FDResourceException {
		return this.prodModel.getSustainabilityRatingEnum();
	}

	@Override
	public String getSustainabilityRating() throws FDResourceException {
		return this.prodModel.getSustainabilityRating();
	}

	@Override
	public String getSustainabilityRating(String skuCode)throws FDResourceException {
		return this.prodModel.getSustainabilityRating(skuCode);
	}
	
	@Override
	public FDGroup getFDGroup() throws FDResourceException {
		return this.prodModel.getFDGroup();
	}
	@Override
	public String getDefaultSkuCode() {
		SkuModel sku = getDefaultSku();
		return sku != null ? sku.getSkuCode() : null;
	}

	@Override
	public Html getFddefSource() {
		return prodModel.getFddefSource();
	}

	@Override
	public String getFreshnessGuaranteed() throws FDResourceException {
		return prodModel.getFreshnessGuaranteed();
	}

	@Override
	public MediaI getMedia(String name) {
		return prodModel.getMedia(name);
	}

	@Override
	@Deprecated
	public String getPriceFormatted(double savingsPercentage, String skuCode) {
		return prodModel.getPriceFormatted(savingsPercentage, skuCode);
	}

	@Override
	public ProductModel getPrimaryProductModel() {
		return prodModel.getPrimaryProductModel();
	}

	@Override
	public List<SkuModel> getPrimarySkus() {
		return prodModel.getPrimarySkus();
	}

	@Override
	public EnumProductLayout getProductLayout(EnumProductLayout defValue) {
		return prodModel.getProductLayout(defValue);
	}

	@Override
	public String getProductRating(String skuCode) throws FDResourceException {
		return prodModel.getProductRating(skuCode);
	}

	@Override
	public int getTemplateType(int defaultValue) {
		return prodModel.getTemplateType(defaultValue);
	}

	@Override
	public ContentKey getWineCountryKey() {
		return prodModel.getWineCountryKey();
	}

	@Override
	public boolean isHasPartiallyFrozen() {
		return prodModel.isHasPartiallyFrozen();
	}

	@Override
	public boolean isHasSalesUnitDescription() {
		return prodModel.isHasSalesUnitDescription();
	}

	@Override
	public boolean isInPrimaryHome() {
		return prodModel.isInPrimaryHome();
	}

	@Override
	public AttributeDefI getAttributeDef(String name) {
		return prodModel.getAttributeDef(name);
	}

	@Override
	public Object getCmsAttributeValue(String name) {
		return prodModel.getCmsAttributeValue(name);
	}

	@Override
	public Object getNotInheritedAttributeValue(String name) {
		return prodModel.getNotInheritedAttributeValue(name);
	}

	@Override
	public String getParentId() {
		return prodModel.getParentId();
	}

	@Override
	public Image getSideNavImage() {
		return prodModel.getSideNavImage();
	}

	@Override
	public boolean isFullyAvailable() {
		return prodModel.isFullyAvailable();
	}

	@Override
	public boolean isTemporaryUnavailableOrAvailable() {
		return prodModel.isTemporaryUnavailableOrAvailable();
	}

	public ProductModel getRealProduct() {
		return prodModel;
	}

	@Override
	public Set<DomainValue> getWineDomainValues() {
		return prodModel.getWineDomainValues();
	}

	@Override
	public boolean isHideWineRatingPricing() {
		return prodModel.isHideWineRatingPricing();
	}

	@Override
	public boolean isShowWineRatings() {
		// TODO Auto-generated method stub
		return prodModel.isShowWineRatings();
	}

	@Override
	public boolean isRetainOriginalSkuOrder() {
		return prodModel.isRetainOriginalSkuOrder();
	}

	@Override
	public boolean showDefaultSustainabilityRating() {
		return this.prodModel.showDefaultSustainabilityRating();
	}
	
	@Override
    public boolean isExcludedForEBTPayment(){
		return this.prodModel.isExcludedForEBTPayment();
	}

	@Override
	public boolean isDisabledRecommendations() {
		return this.prodModel.isDisabledRecommendations();
	}

	@Override
	public EnumOrderLineRating getProductRatingEnum(String skuCode) throws FDResourceException {
		return this.prodModel.getProductRatingEnum(skuCode);
	}

	@Override
	public Image getPackageImage() {
		return this.prodModel.getPackageImage();
	}
	
	@Override
	public List<TagModel> getTags() {
		return prodModel.getTags();
	}

	@Override
	public Set<TagModel> getAllTags() {
		return prodModel.getAllTags();
	}

	
	@Override
	public Set<DomainValue> getAllDomainValues() {
		return prodModel.getAllDomainValues();
	}

	/**
	 * @see {@link ProductModel#getUpSellProducts()}
	 */
	@Override
	public List<ProductModel> getUpSellProducts() {
		return this.prodModel.getUpSellProducts();
	}

	/**
	 * @see {@link ProductModel#getCrossSellProducts()}
	 */
	@Override
	public List<ProductModel> getCrossSellProducts() {
		return this.prodModel.getCrossSellProducts();
	}
	
	@Override
	public String getBrowseRecommenderType(){
		return prodModel.getBrowseRecommenderType();
	}


	/**
	 * @see {@link ProductModel#getHeatRating()}
	 */
	@Override
	public int getHeatRating() {
		return this.prodModel.getHeatRating();
	}

	@Override
	public Image getJumboImage() {
		return this.prodModel.getJumboImage();
	}

	@Override
	public Image getItemImage() {
		return this.prodModel.getItemImage();
	}

	@Override
	public Image getExtraImage() {
		return this.prodModel.getExtraImage();
	}
	
	@Override
	public boolean isDisableAtpFailureRecommendation(){
		return prodModel.isDisableAtpFailureRecommendation();
	}


	
	@Override
	public EnumProductLayout getSpecialLayout() {
		return prodModel.getSpecialLayout();
	}

	@Override
	public List<ProductModel> getCompleteTheMeal() {
		return prodModel.getCompleteTheMeal();
	}

    @Override
    public List<ProductModel> getIncludeProducts() {
        return prodModel.getIncludeProducts();
    }

	@Override
	public String getPageTitle() {
		return prodModel.getPageTitle();
	}

	@Override
	public String getSEOMetaDescription() {
		return prodModel.getSEOMetaDescription();
	}

	@Override
	public String getPairItHeading() {
		return prodModel.getPairItHeading();
	}

	@Override
	public String getPairItText() {
		return prodModel.getPairItText();
	}
}
