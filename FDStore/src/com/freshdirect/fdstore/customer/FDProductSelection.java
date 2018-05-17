package com.freshdirect.fdstore.customer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.freshdirect.affiliate.ErpAffiliate;
import com.freshdirect.affiliate.ExternalAgency;
import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.Discount;
import com.freshdirect.common.pricing.EnumDiscountType;
import com.freshdirect.common.pricing.EnumTaxationType;
import com.freshdirect.common.pricing.MaterialPrice;
import com.freshdirect.common.pricing.Price;
import com.freshdirect.common.pricing.Pricing;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.common.pricing.PricingEngine;
import com.freshdirect.common.pricing.PricingException;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.customer.ErpClientCode;
import com.freshdirect.customer.ErpCouponDiscountLineModel;
import com.freshdirect.customer.ErpOrderLineModel;
import com.freshdirect.customer.TaxCalculatorUtil;
import com.freshdirect.fdstore.EnumOrderLineRating;
import com.freshdirect.fdstore.EnumSustainabilityRating;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDConfigurableI;
import com.freshdirect.fdstore.FDConfiguration;
import com.freshdirect.fdstore.FDConfiguredPrice;
import com.freshdirect.fdstore.FDGroup;
import com.freshdirect.fdstore.FDPricingEngine;
import com.freshdirect.fdstore.FDProduct;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDSalesUnit;
import com.freshdirect.fdstore.FDSku;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.ZonePriceModel;
import com.freshdirect.fdstore.content.BrandModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.ProductReference;
import com.freshdirect.fdstore.content.ProductReferenceImpl;
import com.freshdirect.fdstore.content.ProxyProduct;
import com.freshdirect.fdstore.content.SkuReference;
import com.freshdirect.fdstore.pricing.ProductPricingFactory;
import com.freshdirect.framework.util.MathUtil;

public class FDProductSelection implements FDProductSelectionI {

	private static final long	serialVersionUID	= 4143825923906335052L;
	
	protected final static NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US);
	protected final static DecimalFormat QUANTITY_FORMATTER = new DecimalFormat("0.##");
	
	private final ProductReference productRef;
	protected final ErpOrderLineModel orderLine;

	protected FDConfiguredPrice price;
	protected boolean dirty = true;
	
	private SaleStatisticsI statistics;
	private String customerListLineId;
	private boolean invalidConfig = false;
	private double fixedPrice;
	
	//used by quickshop
	private String orderId;
	private Date deliveryStartDate;
	private EnumSaleStatus saleStatus;
	
	public FDProductSelection(FDSku sku, ProductModel productRef, FDConfigurableI configuration, UserContext ctx) {
		this(sku, productRef, configuration, null, ctx);
	}

	public FDProductSelection(FDSku sku, ProductModel productRef, FDConfigurableI configuration, String variantId, UserContext ctx) {
		this.orderLine = new ErpOrderLineModel();

		this.orderLine.setSku(sku);
		this.productRef = new ProductReferenceImpl((productRef instanceof ProxyProduct) ? ((ProxyProduct) productRef).getProduct() : productRef);
		this.orderLine.setConfiguration( new FDConfiguration(configuration) );
		
		this.orderLine.setVariantId(variantId);
		//For now setting the default. Need to be parameterized
		this.orderLine.setUserContext(ctx);
		
		this.orderLine.setEStoreId(ctx.getStoreContext().getEStoreId());
		
		
		
		
	}

	protected FDProductSelection(ErpOrderLineModel orderLine, final boolean lazy) {
		this.orderLine = orderLine;

		if (!lazy) {
			this.productRef = new SkuReference(this.getSkuCode());
		} else {
			this.productRef = ProductReferenceImpl.NULL_REF;
		}
	}

	//
	// CONFIGURATION
	//

	public FDSku getSku() {
		return this.orderLine.getSku();
	}

	public void setSku(FDSku sku) {
		this.orderLine.setSku(sku);
		this.fireConfigurationChange();
	}

	public ProductReference getProductRef() {
		return this.productRef;
	}

	public FDConfigurableI getConfiguration() {
		return this.orderLine.getConfiguration();
	}

	public void setConfiguration(FDConfigurableI configuration) {
		this.orderLine.setConfiguration(new FDConfiguration(configuration));
		this.fireConfigurationChange();
	}

	//
	// CONFIGURATION CONVENIENCE METHODS
	//

	public String getSkuCode() {
		return this.getSku().getSkuCode();
	}

	public int getVersion() {
		return this.getSku().getVersion();
	}

	public String getCategoryName() {
		return this.getProductRef().getCategoryId();
	}

	public String getProductName() {
		return this.getProductRef().getProductId();
	}

	public double getQuantity() {
		return this.orderLine.getQuantity();
	}

	public final void setQuantity(double quantity) {
		this.setConfiguration(
			new FDConfiguration(quantity, this.getConfiguration().getSalesUnit(), this.getConfiguration().getOptions()));
	}

	public String getSalesUnit() {
		return this.orderLine.getSalesUnit();
	}

	public final void setSalesUnit(String salesUnit) {
		this.setConfiguration(
			new FDConfiguration(this.getConfiguration().getQuantity(), salesUnit, this.getConfiguration().getOptions()));
	}

	public Map<String,String> getOptions() {
		return this.orderLine.getOptions();
	}

	public final void setOptions(Map<String,String> options) {
		this.setConfiguration(
			new FDConfiguration(this.getConfiguration().getQuantity(), this.getConfiguration().getSalesUnit(), options));
	}

	//
	// DESCRIPTIONS
	//

	public String getDescription() {
		return this.orderLine.getDescription();
	}

	public void setDescription(String desc) {
		this.orderLine.setDescription(desc);
	}

	public String getDepartmentDesc() {
		return this.orderLine.getDepartmentDesc();
	}

	public void setDepartmentDesc(String deptDesc) {
		this.orderLine.setDepartmentDesc(deptDesc);
	}

	public String getConfigurationDesc() {
		return this.orderLine.getConfigurationDesc();
	}

	public void setConfigurationDesc(String configDesc) {
		this.orderLine.setConfigurationDesc(configDesc);
	}

	//
	// DIRTY CHECKING
	//

	protected void fireConfigurationChange() {
		this.dirty = true;
	}

	protected boolean isDirty() {
		return this.dirty;
	}

	public void refreshConfiguration() throws FDResourceException, FDInvalidConfigurationException {
		if (this.dirty) {

			FDProduct fdProduct = this.lookupFDProduct();
			if (fdProduct != null) {
				this.orderLine.setMaterialNumber(fdProduct.getMaterial().getMaterialNumber());
				this.orderLine.setAlcohol(fdProduct.isAlcohol());
				this.orderLine.setWine(fdProduct.isWine());
				this.orderLine.setBeer(fdProduct.isBeer());
				this.orderLine.setAffiliate(fdProduct.getAffiliate(this.getUserContext().getStoreContext().getEStoreId()));
				this.orderLine.setDeliveryPass(fdProduct.isDeliveryPass());
				
			}
			
			this.performPricing();

			
			if (this.productRef == null || ProductReferenceImpl.NULL_REF.equals( this.productRef )) {
				// salvage original descriptions
				this.setDepartmentDesc(this.orderLine.getDepartmentDesc());
				this.setDescription(this.orderLine.getDescription());
				this.setConfigurationDesc(this.orderLine.getConfigurationDesc());				
			} else {			
				ProductModel pm = this.lookupProduct();
				if (pm != null) {
					this.orderLine.setPerishable(pm.isPerishable());
					OrderLineUtil.describe(this);				
				}
			}

			if(this.lookupProduct()!=null){
				OrderLineUtil.describe(this);
				if(null != orderLine.getDiscount() && EnumDiscountType.FREE.equals(orderLine.getDiscount().getDiscountType())){
					orderLine.setDepartmentDesc("FREE SAMPLE(S)");					
				}
			}

			this.dirty = false;
		}
	}

	//
	// CONVENIENCE
	//

	public ProductModel lookupProduct() {
		// In CRM no product models are expected
		if (this.productRef == null || ProductReferenceImpl.NULL_REF.equals(productRef)) {
			return null;
		}
		
	    return ProductPricingFactory.getInstance().getPricingAdapter(this.productRef.lookupProductModel(),
                getUserContext().getPricingContext() != null ? getUserContext().getPricingContext() : PricingContext.DEFAULT);
	}

	public FDProduct lookupFDProduct() {
		try {
			return FDCachedFactory.getProduct(this.orderLine.getSku());
		} catch (FDSkuNotFoundException e) {
			return null;
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}

	public FDProductInfo lookupFDProductInfo() {
		try {
			return FDCachedFactory.getProductInfo(this.getSkuCode());
		} catch (FDSkuNotFoundException e) {
			return null;
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}

	//
	// PRICING
	//

	public double getPrice() {
		return this.orderLine.getPrice();
	}

	public double getActualPrice() {
		return this.orderLine.getActualPrice();
	}
	
	public double getTaxRate() {
		return this.orderLine.getTaxRate();
	}
	
	public EnumTaxationType getTaxationType() {
		return this.orderLine.getTaxationType();
	}
	
	public void setTaxRate(double taxRate){
		if(!this.lookupFDProduct().isTaxable()){
			taxRate = 0.0;
		}
		this.orderLine.setTaxRate(taxRate);
	}

	public double getTaxValue() {
//		return MathUtil.roundDecimal((getConfiguredPrice() - getPromotionValue() - getCouponDiscountValue()) * getTaxRate());
		return TaxCalculatorUtil.getTaxValue(getConfiguredPrice(),getPromotionValue(), getCouponDiscountValue(), getTaxRate(), getTaxationType());
	}

	public double getDepositValue() {
		return this.orderLine.getDepositValue();
	}
	
	public void setDepositValue(double depositRate){
		double val = MathUtil.roundDecimal(getQuantity() * this.lookupFDProduct().getDepositsPerEach() * depositRate);
		this.orderLine.setDepositValue(val);
	}

	public double getPromotionValue() {
		return this.price.getPromotionValue();
	}

	public double getCouponDiscountValue() {
		return this.price.getCouponDiscountValue();
	}
	
	public String getUnitPrice() {
		// dirty requirement so got to do this
		Price discountP=null;
		double disAmount=this.price.getBasePrice();
		Price p=new Price(this.price.getBasePrice());
		if(this.getDiscount()!=null){
			if(this.getDiscount().getSkuLimit() > 0 && !this.price.getBasePriceUnit().equalsIgnoreCase("lb") && this.getDiscount().getSkuLimit() != this.getQuantity()) {
				
				disAmount=this.price.getBasePrice();
				
			//APPDEV-4148-Displaying unit price if quantity is greater than sku limit - START
				
				if(this.getDiscount().getSkuLimit() < this.getQuantity()){
					if(this.getDiscount().getDiscountType().equals(EnumDiscountType.PERCENT_OFF))
					{											
						disAmount=((this.price.getBasePrice() * this.getQuantity()) - ((this.price.getBasePrice() * this.getDiscount().getSkuLimit()) * this.getDiscount().getAmount() )) / this.getQuantity();
					}
					else
					{
						disAmount=((this.price.getBasePrice() * (this.getQuantity())) - (this.getDiscount().getAmount() * this.getDiscount().getSkuLimit()))/ this.getQuantity() ;
					}
				}
				
			//APPDEV-4148-Displaying unit price if quantity is greater than sku limit - END
				
			} else if(this.getDiscount().getSkuLimit() > 0 && this.price.getBasePriceUnit().equalsIgnoreCase("lb") && this.getDiscount().getDiscountType().equals(EnumDiscountType.DOLLAR_OFF)) {
				disAmount=this.price.getBasePrice();
			} else {
				if(this.getDiscount().getMaxPercentageDiscount() > 0) {
					disAmount = this.orderLine.getPrice()/this.orderLine.getQuantity();
				} else {
					try {
						discountP=PricingEngine.applyDiscount(p,1,this.getDiscount(),this.price.getBasePriceUnit());
						disAmount=discountP.getBasePrice();
					} catch (PricingException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		//Apply the coupon discount on top of line item discount and calculate the final base price.
		if (this.getCouponDiscount() != null) {
			try {
				discountP = PricingEngine.applyCouponDiscount(null!=discountP?discountP:p, 1/this.getQuantity(), this.getCouponDiscount(), this.price.getBasePriceUnit());
				disAmount = discountP.getBasePrice();
			} catch (PricingException e) {
				disAmount = 0.0;
			}
		} 
			 
		return CURRENCY_FORMATTER.format(disAmount) + "/" + this.price.getBasePriceUnit().toLowerCase();
	}

	protected ErpCouponDiscountLineModel getCouponDiscount() {
		return this.orderLine.getCouponDiscount();
	}
	
	public double getConfiguredPrice() {
		return this.price.getConfiguredPrice();
	}

	//
	// CONVENIENCE
	//

	public boolean isAlcohol() {
		return this.orderLine.isAlcohol();
	}
	
	public boolean isWine() {
		return this.orderLine.isWine();
	}

	public boolean isBeer() {
		return this.orderLine.isBeer();
	}

	public boolean isPerishable() {
		return this.orderLine.isPerishable();
	}

	public boolean isKosher() {
		try {
			FDProduct pr = lookupFDProduct();
			return pr.isKosherProduction(getUserContext().getFulfillmentContext().getPlantId());
		} catch (Exception exc) {
			return false;
		}
	}

	public boolean isPlatter() {
		try {
			FDProduct fdProduct = this.lookupFDProduct();
			return fdProduct.isPlatter(getUserContext().getFulfillmentContext().getPlantId());
		} catch (Exception exc) {
			return false;
		}
	}

	public boolean isSoldBySalesUnits() {
		ProductModel productNode = this.lookupProduct();
		return productNode == null ? false : productNode.isSoldBySalesUnits();
	}

	public boolean isPricedByLb() {
		FDProduct fdProduct = this.lookupFDProduct();
		return fdProduct.isPricedByLb();
	}

	public boolean isSoldByLb() {
		FDProduct fdProduct = this.lookupFDProduct();
		return fdProduct.isSoldByLb();
	}

	public boolean hasScaledPricing() {
		FDProduct fdProduct = this.lookupFDProduct();
		Pricing price = fdProduct.getPricing();
		ZonePriceModel zpm=price.getZonePrice(getUserContext().getPricingContext().getZoneInfo());
		MaterialPrice[] materialPrices=null;
		if(zpm!=null)
			materialPrices = zpm.getMaterialPrices();
		else return false;
		
		if(materialPrices==null)
			return false;
		return !materialPrices[0].isWithinBounds(this.getQuantity());
	}

	public boolean isEstimatedPrice() {
		FDProduct fdProduct = this.lookupFDProduct();

		boolean displaySalesUnitsOnly =
			isSoldBySalesUnits() || (!fdProduct.hasSingleSalesUnit() && fdProduct.isSoldByLb() && fdProduct.isPricedByLb());
		boolean displayEstimatedQuantity = !displaySalesUnitsOnly && fdProduct.isPricedByLb() && !fdProduct.isSoldByLb();

		return displayEstimatedQuantity || fdProduct.isPricedByLb();
	}

	//
	// FORMATTING, DISPLAY
	// 

	public String getSalesUnitDescription() {
		return "LB".equalsIgnoreCase(this.getSalesUnit()) ? "lb" : "";
	}

	protected FDSalesUnit lookupFDSalesUnit() {
		return this.lookupFDProduct().getSalesUnit(this.getSalesUnit());
	}

	public String getOrderedQuantity() {
		if (this.isSoldBySalesUnits()) {
			return this.lookupFDSalesUnit().getDescriptionQuantity();

		} else {
			return QUANTITY_FORMATTER.format(this.getQuantity());
		}
	}

	public String getUnitsOfMeasure() {
		return this.isSoldBySalesUnits() ? this.lookupFDSalesUnit().getDescriptionUnit() : "";
	}

	public String getDisplayQuantity() {
		StringBuffer qty = new StringBuffer();
		if (this.isSoldBySalesUnits()) {
			FDSalesUnit unit = this.lookupFDSalesUnit();
			qty.append(unit.getDescriptionQuantity());
			qty.append(" ");
			qty.append(unit.getDescriptionUnit());
		} else {
			qty.append(QUANTITY_FORMATTER.format(this.getQuantity()));
		}
		return qty.toString();
	}

	public String getLabel() {
		ProductModel prod = this.lookupProduct();
		String quantText = prod == null ? null : prod.getQuantityTextSecondary();
		if (quantText != null) {
			if ("lb".equalsIgnoreCase(quantText) || "oz".equalsIgnoreCase(quantText)) {
				return quantText;
			}
			return this.getSalesUnitDescription();
		}
		return this.isSoldByLb() ? "lb" : "";
	}

	public ErpAffiliate getAffiliate() {
		return this.lookupFDProduct().getAffiliate(this.orderLine.getUserContext().getStoreContext().getEStoreId());
	}

	protected void performPricing() {
		String pricingUnit = "";
		if(this.lookupFDProduct().isPricedByLb()) {
			pricingUnit = "lb";
		}
		try {
			if(FDStoreProperties.getGiftcardSkucode().equalsIgnoreCase(this.getSkuCode()) || FDStoreProperties.getRobinHoodSkucode().equalsIgnoreCase(this.getSkuCode())){
				this.price = FDPricingEngine.doPricing(this.lookupFDProduct(), this, this.getDiscount(), this.orderLine.getUserContext().getPricingContext(), null, 0.0, pricingUnit,null,this.orderLine.getScaleQuantity());
				this.orderLine.setPrice(this.getFixedPrice());
				this.orderLine.setDiscountAmount(0);
			}else
			{
				FDGroup group = this.getFDGroup();

				this.price = FDPricingEngine.doPricing(this.lookupFDProduct(), this, this.getDiscount(), this.orderLine.getUserContext().getPricingContext(), group, this.getGroupQuantity(), pricingUnit,this.getCouponDiscount(),this.orderLine.getScaleQuantity());
				this.orderLine.setPrice(price.getConfiguredPrice() - price.getPromotionValue() - price.getCouponDiscountValue());
				this.orderLine.setDiscountAmount(price.getPromotionValue());
				this.orderLine.setPricingZoneId(price.getZoneInfo().getPricingZoneId());
				this.orderLine.setSalesOrg(price.getZoneInfo().getSalesOrg());
				this.orderLine.setDistChannel(price.getZoneInfo().getDistributionChanel());
			}	
			
			//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$Price is set here?" + this.getDescription() + " -price:" + this.orderLine.getPrice() + " -discount:" + this.orderLine.getDiscountAmount());

		} catch (PricingException e) {
			throw new FDRuntimeException(e, "PricingException occured on "+getSkuCode() + " - " + getConfiguration());
		}
	}

	/** template method */
	protected Discount getDiscount() {
		return this.orderLine.getDiscount();
	}

	public SaleStatisticsI getStatistics() {
		return this.statistics;
	}
	
	public void setStatistics(SaleStatisticsI stats){
		this.statistics = stats;
	}

	public String getCustomerListLineId() {
		return customerListLineId;
	}

	public void setCustomerListLineId(String customerListLineId) {
		this.customerListLineId = customerListLineId;
	}
	
	public void setInvalidConfig(boolean invalidConfig){
		this.invalidConfig = invalidConfig;
	}
	
	public boolean isInvalidConfig(){
		return invalidConfig;
	}
	
	public String getRecipeSourceId() {
		return this.orderLine.getRecipeSourceId();
	}
	
	public void setRecipeSourceId(String recipeSourceId) {
		this.orderLine.setRecipeSourceId(recipeSourceId);
	}
	
	public String getYmalCategoryId() {
		return this.orderLine.getYmalCategoryId();
	}
	
	public void setYmalCategoryId(String ymalCategoryId) {
		this.orderLine.setYmalCategoryId(ymalCategoryId);
	}
	
	public String getYmalSetId() {
		return this.orderLine.getYmalSetId();
	}
	
	public void setYmalSetId(String ymalSetId) {
		this.orderLine.setYmalSetId(ymalSetId);
	}
	
	public String getOriginatingProductId() {
		return this.orderLine.getOriginatingProductId();
	}
	
	public void setOriginatingProductId(String originatingProductId) {
		this.orderLine.setOriginatingProductId(originatingProductId);
	}
	
	public boolean isRequestNotification() {
		return orderLine.isRequestNotification();
	}
	
	public void setRequestNotification(boolean requestNotification) {
		this.orderLine.setRequestNotification(requestNotification);
	}

	public EnumOrderLineRating getProduceRating() {
		return orderLine.getProduceRating();
	}


	public double getDiscountAmount() {
		return orderLine.getDiscountAmount();
	}
	public void setDiscountAmount(double discountAmount) {
		this.orderLine.setDiscountAmount(discountAmount);
	}
	
	public void setFDGroup(FDGroup group) {
        this.orderLine.setFDGroup(group);
	}
	
	 public FDGroup getFDGroup() {
		 FDGroup group = this.orderLine.getFDGroup();
			if(group == null) {//If not in the line item level check sku level.
				FDProductInfo _p=this.lookupFDProductInfo();
				if(_p!=null)
					group = _p.getGroup(this.getUserContext().getPricingContext().getZoneInfo().getSalesOrg(),this.getUserContext().getPricingContext().getZoneInfo().getDistributionChanel());
			}
		 
		 return group;
	 }
	
	public boolean isDiscountFlag(){
		return orderLine.isDiscountFlag();
	}
	
	public void setDiscountFlag(boolean discountApplied){
		this.orderLine.setDiscountFlag(discountApplied);
	}

	public double getFixedPrice() {
		return this.fixedPrice;
	}

	public void setFixedPrice(double price) {
		this.fixedPrice=price;
	}
	
	public UserContext getUserContext() {
		return orderLine.getUserContext();
	}
	
	public void setUserContext(UserContext uCtx) {
		this.orderLine.setUserContext(uCtx);
	}
	
	public List<ErpClientCode> getClientCodes() {
		return this.orderLine.getClientCodes();
	}
	
	public boolean hasBrandName(Set<String> brandNames) {
			ProductModel pm = ContentFactory.getInstance().getProductByName(this.getCategoryName(), this.getProductName());
			if(pm != null && pm.getBrands() != null) {
				for ( BrandModel brand : pm.getBrands() ) {
					if (brandNames.contains(brand.getContentName())) {
						return true;
					}
				}
			}
		return false;
	}
	
	public void setGroupQuantity(double quantity){
		this.orderLine.setGroupQuantity(quantity);
		this.fireConfigurationChange();
	}
	
	public double getGroupQuantity(){
		return this.orderLine.getGroupQuantity();
	}
	
	public double getGroupScaleSavings() {
		double savings = 0.0;
		try {
				FDGroup group = this.getFDGroup();
				if(group != null) {
					//System.out.println("getGroupScaleSavings=>"+this.lookupFDProduct().getSkuCode());
					FDConfiguredPrice regPrice = FDPricingEngine.doPricing(this.lookupFDProduct(), this, this.getDiscount(), this.orderLine.getUserContext().getPricingContext(), group, this.getGroupQuantity(), this.price.getBasePriceUnit(),this.getCouponDiscount(),this.orderLine.getScaleQuantity());
					savings = regPrice.getConfiguredPrice() - (regPrice.getPromotionValue() + this.orderLine.getPrice()+regPrice.getCouponDiscountValue());
				}
		} catch (PricingException e) {
			throw new FDRuntimeException(e, "PricingException occured in getGroupScaleSavings() on "+getSkuCode() + " - " + getConfiguration());
		}
		return savings;
	}
	@Override
	public EnumSustainabilityRating getSustainabilityRating() {
		return orderLine.getSustainabilityRating();
	}
	
	public double getBasePrice() {
		if(this.price == null) {
			return 0.0;
		}
		return this.price.getBasePrice();
	}
	
	public String getUpc(){
		FDProductInfo prodInfo =lookupFDProductInfo();
		return null !=prodInfo?prodInfo.getUpc():"";
	}

	public Date getDeliveryStartDate() {
		return deliveryStartDate;
	}

	public void setDeliveryStartDate(Date deliveryStartDate) {
		this.deliveryStartDate = deliveryStartDate;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public EnumSaleStatus getSaleStatus() {
		return saleStatus;
	}

	public void setSaleStatus(EnumSaleStatus saleStatus) {
		this.saleStatus = saleStatus;
	}
	
	public void setOrderLineId(String orderLineId){
		this.orderLine.setOrderLineId(orderLineId);
	}
	
	public String getOrderLineId(){
		return this.orderLine.getOrderLineId();
	}

	@Override
	public String getExternalGroup() {
		return this.orderLine.getExternalGroup();
	}

	@Override
	public void setExternalGroup(String externalGroup) {
		this.orderLine.setExternalGroup(externalGroup);
	}

	@Override
	public ExternalAgency getExternalAgency() {
		return this.orderLine.getExternalAgency();
	}

	@Override
	public void setExternalAgency(ExternalAgency externalAgency) {
		this.orderLine.setExternalAgency(externalAgency);
	}

	@Override
	public String getExternalSource() {
		return this.orderLine.getExternalSource();
	}

	@Override
	public void setExternalSource(String externalSource) {
		this.orderLine.setExternalSource(externalSource);
	}

	@Override
	public Double getScaleQuantity() {
		this.orderLine.getScaleQuantity();
		return null;
	}

	@Override
	public void setScaleQuantity(Double scaleQuantity) {
		this.orderLine.setScaleQuantity(scaleQuantity);
		this.fireConfigurationChange();		
	}
	
}
