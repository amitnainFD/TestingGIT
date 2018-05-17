package com.freshdirect.fdstore.pricing;

import java.util.Date;
import java.util.List;

import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.FDProduct;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.content.BrandModel;
import com.freshdirect.fdstore.content.DomainValue;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.SkuModel;

public class SkuModelPricingAdapter extends SkuModel {
	private PricingContext pricingCtx;
	private SkuModel innerSku;
	
	public SkuModelPricingAdapter(SkuModel sku, PricingContext pCtx) {
		super(sku.getContentKey());
		this.innerSku = sku;
		this.pricingCtx = pCtx;

	}
	public Object clone() {
		return innerSku.clone();
	}

	public String getSkuCode(){
	    return innerSku.getSkuCode();
	}
	
	public List<DomainValue> getVariationMatrix() {
		return innerSku.getVariationMatrix();
	}
	
	public List<DomainValue> getVariationOptions() {
		return innerSku.getVariationOptions();
	}
	
	public boolean isDiscontinued() {
		return innerSku.isDiscontinued();
	}
	
	public boolean isTempUnavailable() {
		return innerSku.isTempUnavailable();
	}

	public boolean isOutOfSeason() {
		return innerSku.isOutOfSeason();
	}
	
	public boolean isUnavailable() {
		return innerSku.isUnavailable();
	}
		
	
	public boolean isAvailableWithin(int days) {
		return innerSku.isAvailableWithin(days);
	}
	
	public Date getEarliestAvailability() {
		return innerSku.getEarliestAvailability();
	}
	
	/** @return null if sku is available */
	public String getEarliestAvailabilityMessage() {
		return innerSku.getEarliestAvailabilityMessage();
	}

	
	public FDProductInfo getProductInfo() throws FDResourceException, FDSkuNotFoundException {
		return innerSku.getProductInfo();
	}
    
    public FDProduct getProduct() throws FDResourceException, FDSkuNotFoundException {
    	return innerSku.getProduct();
	}

    public ProductModel getProductModel() {
    	return ProductPricingFactory.getInstance().getPricingAdapter(innerSku.getProductModel(),pricingCtx);
    }
    
	public List<BrandModel> getBrands() {
		return innerSku.getBrands();
	}
	
	public PricingContext getPricingContext() {
		return this.pricingCtx;
	}
	
	public SkuModel getRealSku() {
		return innerSku;
	}

}
