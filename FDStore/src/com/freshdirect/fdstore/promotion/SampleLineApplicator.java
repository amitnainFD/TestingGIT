package com.freshdirect.fdstore.promotion;

import org.apache.log4j.Category;

import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.Discount;
import com.freshdirect.common.pricing.EnumDiscountType;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.FDConfiguration;
import com.freshdirect.fdstore.FDProduct;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDSalesUnit;
import com.freshdirect.fdstore.FDSku;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.ProductReference;
import com.freshdirect.fdstore.content.SkuModel;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartLineModel;
import com.freshdirect.fdstore.customer.FDInvalidConfigurationException;
import com.freshdirect.fdstore.pricing.ProductPricingFactory;
import com.freshdirect.framework.util.log.LoggerFactory;

public class SampleLineApplicator implements PromotionApplicatorI {

	private final static Category LOGGER = LoggerFactory.getInstance(SampleStrategy.class);

	private final ProductReference sampleProduct;
	private final double minSubtotal;
	private DlvZoneStrategy zoneStrategy;

	private CartStrategy cartStrategy;
	
	public SampleLineApplicator(ProductReference sampleProduct, double minSubtotal) {
		this.sampleProduct = sampleProduct;
		this.minSubtotal = minSubtotal;
	}

	public ProductModel getSampleProduct() {
		return this.sampleProduct.lookupProductModel();
	}
	
	public ProductReference getProductReference() {
	    return this.sampleProduct;
	}

	public boolean apply(String promotionCode, PromotionContextI context) {
		//If delivery zone strategy is applicable please evaluate before applying the promotion.
		int e = zoneStrategy != null ? zoneStrategy.evaluate(promotionCode, context) : PromotionStrategyI.ALLOW;
		if(e == PromotionStrategyI.DENY) return false;
		
		e = cartStrategy != null ? cartStrategy.evaluate(promotionCode, context, true) : PromotionStrategyI.ALLOW;
		if(e == PromotionStrategyI.DENY) return false;
		
		PromotionI promo = PromotionFactory.getInstance().getPromotion(promotionCode);
		if (context.getSubTotal(promo.getExcludeSkusFromSubTotal()) < this.minSubtotal) {
			return false;
		}
		try {
			FDCartLineI cartLine = this.createSampleLine(promotionCode, context.getUserContext());
			if (cartLine != null) {
				context.addSampleLine(cartLine);
				return true;
			}
			return false;
		} catch (FDResourceException fe) {
			throw new FDRuntimeException(fe);
		}
	}

	/** @return null if product is not found */
	private FDCartLineI createSampleLine(String promotionCode, UserContext userCtx) throws FDResourceException {
		ProductModel product = null;	
		try{
			product = ProductPricingFactory.getInstance().getPricingAdapter(this.sampleProduct.lookupProductModel(),userCtx.getPricingContext());	

		}catch(Exception ex){
			// This is to handle when a invalid category id or product id is set to the sampe promo. 
			LOGGER.error("The category id or product id for the sample promo "+promotionCode+" is not invalid.");
		}
		
		if (product == null) {
			LOGGER.info("Sample product " + this.sampleProduct + " not in store");
			return null;
		}

		SkuModel sku = product.getDefaultSku();
		if (sku == null) {
			LOGGER.info("Default SKU not found for " + this.sampleProduct);
			return null;
		}

		FDProduct fdp;
		try {
			fdp = sku.getProduct();
		} catch (FDSkuNotFoundException e) {
			LOGGER.info("FDProduct not found for " + sku);
			return null;
		}

		FDSalesUnit su = fdp.getSalesUnits()[0];

		FDCartLineModel cartLine =
			new FDCartLineModel(
				new FDSku(fdp),
				product,
				new FDConfiguration(product.getQuantityMinimum(), su.getName()), null, userCtx);

		cartLine.setDiscount(new Discount(promotionCode, EnumDiscountType.SAMPLE, 1.0));

		try {
			cartLine.refreshConfiguration();
		} catch (FDInvalidConfigurationException e) {
			throw new FDResourceException(e);
		}

		return cartLine;
	}

	public double getMinSubtotal() {
		return this.minSubtotal;
	}

	public void setZoneStrategy(DlvZoneStrategy zoneStrategy) {
		this.zoneStrategy = zoneStrategy;
	}

	public DlvZoneStrategy getDlvZoneStrategy() {
		return this.zoneStrategy;
	}
	
	public String toString() {
		return "SampleLineApplicator[" + this.sampleProduct + " min $" + this.minSubtotal + "]";
	}

	@Override
	public void setCartStrategy(CartStrategy cartStrategy) {
		this.cartStrategy = cartStrategy;
	}

	@Override
	public CartStrategy getCartStrategy() {
		return this.cartStrategy;
	}

}
