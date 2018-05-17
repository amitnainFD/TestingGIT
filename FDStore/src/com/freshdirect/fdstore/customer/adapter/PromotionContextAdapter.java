package com.freshdirect.fdstore.customer.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Category;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.common.pricing.Discount;
import com.freshdirect.common.pricing.EnumDiscountType;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpChargeLineModel;
import com.freshdirect.customer.ErpDepotAddressModel;
import com.freshdirect.customer.ErpDiscountLineModel;
import com.freshdirect.fdlogistics.model.FDDeliveryDepotModel;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.DCPDPromoProductCache;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDModifyCartModel;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.ProfileModel;
import com.freshdirect.fdstore.promotion.AssignedCustomerParam;
import com.freshdirect.fdstore.promotion.EnumOfferType;
import com.freshdirect.fdstore.promotion.EnumOrderType;
import com.freshdirect.fdstore.promotion.EnumPromotionType;
import com.freshdirect.fdstore.promotion.PromotionContextI;
import com.freshdirect.fdstore.promotion.PromotionFactory;
import com.freshdirect.fdstore.promotion.PromotionI;
import com.freshdirect.fdstore.promotion.SignupDiscountRule;
import com.freshdirect.framework.util.MathUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.logistics.controller.data.PickupData;

public class PromotionContextAdapter implements PromotionContextI {

	private final FDUserI user;
	private List<String> rulePromoCodes;
	private Date now;
	
	
	@SuppressWarnings( "unused" )
	private static Category LOGGER = LoggerFactory.getInstance(PromotionContextAdapter.class);


	public PromotionContextAdapter(FDUserI user) {		
		this.user = user;
		now = new Date();
	}
	
	
	/**
	 * @return total price of orderlines in USD, with taxes, charges without discounts applied
	 */
	public double getPreDeductionTotal(){
		return this.user.getShoppingCart().getPreDeductionTotal();
	}

	/**
	 * 
	 * @return total price of orderlines - exclude skus in USD, with taxes, charges without discounts applied
	 */
	public double getPreDeductionTotal(Set<String> excludeSkus){
	      double preTotal = 0.0;
	        preTotal += MathUtil.roundDecimal(this.getSubTotal(excludeSkus));			
	        preTotal += MathUtil.roundDecimal(this.user.getShoppingCart().getTaxValue());
	        preTotal += MathUtil.roundDecimal(this.user.getShoppingCart().getDepositValue());

			// apply charges
			for ( ErpChargeLineModel charge : this.user.getShoppingCart().getCharges()) {
				preTotal += MathUtil.roundDecimal( charge.getTotalAmount() );
			}
         return MathUtil.roundDecimal(preTotal);
		
	}
	/*
	 * New getSubtotal Logic excludes skus that are specified at promotion level(non-Javadoc)
	 * @see com.freshdirect.fdstore.promotion.PromotionContextI#getSubTotal()
	 */
	public double getSubTotal(Set<String> excludeSkus) {
		double subTotal = 0.0;
		for ( FDCartLineI cartLineModel : this.user.getShoppingCart().getOrderLines() ) {
			boolean e = excludeSkus != null &&  excludeSkus.size() > 0 ? !excludeSkus.contains(cartLineModel.getSkuCode()) : true;
			if(e)
				subTotal += MathUtil.roundDecimal( cartLineModel.getPrice() );
		}
		return MathUtil.roundDecimal(subTotal);
	}

	public void addSampleLine(FDCartLineI cartLine) {
		this.user.getShoppingCart().addSampleLine(cartLine);
	}

	public boolean isAddressMismatch() {
		return this.user.getShoppingCart().isAddressMismatch();
	}

	public void setPromotionAddressMismatch(boolean b) {
		this.user.setPromotionAddressMismatch(b);
	}

	public void setSignupDiscountRule(SignupDiscountRule discountRule) {
		this.user.setSignupDiscountRule(discountRule);
	}

	public String getModifiedSaleId() {
		FDCartModel cart = user.getShoppingCart();
		

		if (cart instanceof FDModifyCartModel) {
			return ((FDModifyCartModel) cart).getOriginalOrder().getErpSalesId();
		}
		return null;
	}

	public int getPromotionUsageCount(String promotionCode) {
		String ignoreSaleId = this.getModifiedSaleId();
		try {
			return user.getPromotionHistory().getPromotionUsageCount(promotionCode, ignoreSaleId);
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}

	public String getSubscribedSignupPromotionCode() {
		String ignoreSaleId = this.getModifiedSaleId();
		Set<String> promoCodes = PromotionFactory.getInstance().getPromotionCodesByType(EnumPromotionType.SIGNUP);
		try {

			Set<String> allPromos = user.getPromotionHistory().getUsedPromotionCodes(ignoreSaleId);
			promoCodes.retainAll(allPromos);

			if (promoCodes.isEmpty()) {
				return null;
			}
			return promoCodes.iterator().next();

		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}

	public String getZipCode() {
		ErpAddressModel addr = this.user.getShoppingCart().getDeliveryAddress();
		if (addr != null) {
			if (addr instanceof ErpDepotAddressModel) {
				return null;
			}
			return addr.getZipCode();
		}
		return this.user.getZipCode();
	}

	public String getDepotCode() {
		ErpAddressModel addr = this.user.getShoppingCart().getDeliveryAddress();
		if (addr != null && addr instanceof ErpDepotAddressModel) {
			String locationId = ((ErpDepotAddressModel) addr).getLocationId();
			FDDeliveryDepotModel depot;
			try {
				depot = FDDeliveryManager.getInstance().getDepotByLocationId(locationId);
			} catch (FDResourceException e) {
				throw new FDRuntimeException(e);
			}
			return depot.getDepotCode();
		}
		return user.getDepotCode();
	}

	public String getDepotCode(AddressModel addr) {		
		if (addr != null && addr instanceof ErpDepotAddressModel) {
			String locationId = ((ErpDepotAddressModel) addr).getLocationId();
			FDDeliveryDepotModel depot;
			try {
				depot = FDDeliveryManager.getInstance().getDepotByLocationId(locationId);
			} catch (FDResourceException e) {
				throw new FDRuntimeException(e);
			}
			return depot.getDepotCode();
		}
		return user.getDepotCode();
	}
	public PromotionI getRedeemedPromotion() {
		return this.user.getRedeemedPromotion();
	}

	public boolean isFraudulent() {
		try {
			return this.user.isFraudulent();
		} catch (FDResourceException e) {
			throw new FDRuntimeException();
		}
	}

	public FDCartModel getShoppingCart() {
		return this.user.getShoppingCart();
	}

	public int getAdjustedValidOrderCount() {
		try {
			return this.user.getAdjustedValidOrderCount();
		} catch (FDResourceException e) {
			throw new FDRuntimeException();
		}
	}

	public double getApplicableSignupAmount(double amount, double maxAmountPerSku) {
		return OrderPromotionHelper.getApplicableSignupAmount(this.user.getShoppingCart(), amount, maxAmountPerSku);
	}

	public EnumOrderType getOrderType() {
		ErpAddressModel address = this.user.getShoppingCart().getDeliveryAddress();
        return getOrderType(address);

		/*ErpAddressModel address = this.user.getShoppingCart().getDeliveryAddress();
		if (address != null) {
			if (address instanceof ErpDepotAddressModel) {
				if (((ErpDepotAddressModel) address).isPickup()) {
					return EnumOrderType.PICKUP;
				}
				return EnumOrderType.DEPOT;
			}
			if (EnumServiceType.CORPORATE.equals(address.getServiceType())) {
				return EnumOrderType.CORPORATE;
			}

			return EnumOrderType.HOME;
		}

		// no address, work out from user
		if (user.isPickupOnly()) {
			return EnumOrderType.PICKUP;
		}

		if (user.isCorporateUser()) {
			return EnumOrderType.CORPORATE;
		}

		if (user.isDepotUser()) {
			return EnumOrderType.DEPOT;
		}

		return EnumOrderType.HOME;*/
	}
	
	public EnumOrderType getOrderType(AddressModel address) {
		EnumEStoreId eStoreId = this.user.getUserContext().getStoreContext().getEStoreId();
        if(EnumEStoreId.FDX.equals(eStoreId)){
               return EnumOrderType.FDX;
        }
	
		if (address != null) {
			if (address instanceof ErpDepotAddressModel) {
				if (((ErpDepotAddressModel) address).isPickup()) {
					return EnumOrderType.PICKUP;
				}
				return EnumOrderType.DEPOT;
			}
			if (EnumServiceType.CORPORATE.equals(address.getServiceType())) {
				return EnumOrderType.CORPORATE;
			}

			return EnumOrderType.HOME;
		}

		// no address, work out from user
		if (user.isPickupOnly()) {
			return EnumOrderType.PICKUP;
		}

		if (user.isCorporateUser()) {
			return EnumOrderType.CORPORATE;
		}

		if (user.isDepotUser()) {
			return EnumOrderType.DEPOT;
		}

		return EnumOrderType.HOME;
	}

	public boolean hasProfileAttribute(String attributeName, String desiredValue) {
		try {
			if (user.getIdentity() == null)
				return false;
			ProfileModel pm = user.getFDCustomer().getProfile();
			if (pm == null)
				return false;

			String attribValue = pm.getAttribute(attributeName);
			if (desiredValue == null)
				return attribValue != null;
			return (desiredValue.equalsIgnoreCase(attribValue));
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}

	public FDIdentity getIdentity() {
		return user.getIdentity();
	}

	public void setRulePromoCode(List<String> rulePromoCodes) {
		this.rulePromoCodes = rulePromoCodes;

	}

	public boolean hasRulePromoCode(String promoCode) {
		if(this.rulePromoCodes.isEmpty()) {
			return false;
		}
		return this.rulePromoCodes.contains(promoCode);
	}

	public FDUserI getUser() {
		return user;
	}

	public void addDiscount(Discount discount) {
		this.user.getShoppingCart().addDiscount(discount);
	}

	public Date getCurrentDate() {
		return now;
	}
	public AssignedCustomerParam getAssignedCustomerParam(String promoId){
		return this.user.getAssignedCustomerParam(promoId);
	}
	
	@Override
	public List<FDCartLineI> getEligibleLinesForDCPDiscount(String promoId, Set<ContentKey> contentKeys) {
		if(getShoppingCart().isEmpty()){
			return Collections.<FDCartLineI>emptyList();
		}
		List<FDCartLineI> eligibleLines = new ArrayList<FDCartLineI>();

		List<FDCartLineI> orderLines = getShoppingCart().getOrderLines();
		for ( FDCartLineI cartLine : orderLines ) {
			boolean eligible = false;
			String recipeSourceId = cartLine.getRecipeSourceId();
			if(recipeSourceId != null && recipeSourceId.length() > 0){
				////Check if the line item is eligible for a recipe discount.
				eligible = OrderPromotionHelper.isRecipeEligible(recipeSourceId, contentKeys);
			}
			if(!eligible){
				ProductModel model = cartLine.getProductRef().lookupProductModel();
				String productId = null !=model ?model.getContentKey().getId():"";
				DCPDPromoProductCache dcpdCache = this.user.getDCPDPromoProductCache();
				//Check if the line item product is already evaluated.
				if(dcpdCache.isEvaluated(productId, promoId)){
					eligible = dcpdCache.isEligible(productId, promoId);
				}else{
					//Check if the line item is eligible for a category or department discount.
					eligible = OrderPromotionHelper.evaluateProductForDCPDPromo(model, contentKeys);
					//Set the eligiblity info to user session.
					dcpdCache.setPromoProductInfo(productId, promoId, eligible);
				}
			}
			if(eligible){
				//Cartline is eligible for discount.
				eligibleLines.add(cartLine);
			}
		}
		return eligibleLines;
	}
	
	public boolean applyHeaderDiscount(PromotionI promo, double promotionAmt) {
		//Poll the promotion context to know if this is the max discount amount.
		if(promo.isRedemption() || promo.isCombineOffer()){
			//Add this discount since it is combinable.
			Discount discount = new Discount(promo.getPromotionCode(), EnumDiscountType.DOLLAR_OFF, promotionAmt);
			this.addDiscount(discount);
			return true;
		}	
		//Poll the promotion context to know if this is the max discount amount.
		else {
			Discount applied = this.getHeaderDiscount();
			if(this.isMaxDiscountAmount(promotionAmt, promo.getPriority(), applied)){
				//Clear the previous discount.
				if(applied != null)
					this.getShoppingCart().removeDiscount(applied.getPromotionCode());
				//Add this discount.
				Discount discount = new Discount(promo.getPromotionCode(), EnumDiscountType.DOLLAR_OFF, promotionAmt);
				this.addDiscount(discount);
				return true;
			}
		}
		return false;
	}
	
	public boolean applyLineItemDiscount(PromotionI promo, FDCartLineI lineItem, double percentOff, int skuLimit, double maxPercentageDiscount) {
		
		//Poll the promotion context to know if this is the max discount amount.
		Discount applied = lineItem.getDiscount();
		if(promo.isRedemption() || this.isMaxDiscountAmount(percentOff, promo.getPriority(), applied)){
			//Clear the previous discount.
			lineItem.removeLineItemDiscount();
			//Add this discount.
			Discount discount = new Discount(promo.getPromotionCode(), EnumDiscountType.PERCENT_OFF, percentOff);
			discount.setSkuLimit(skuLimit);
			if(maxPercentageDiscount > 0)
				discount.setMaxPercentageDiscount(maxPercentageDiscount);
			lineItem.setDiscount(discount);
			return true;
		}
		return false;
	}

	public boolean applyLineItemDollarOffDiscount(PromotionI promo, FDCartLineI lineItem, double promotionAmt, int skuLimit) {
		
		//Poll the promotion context to know if this is the max discount amount.
		Discount applied = lineItem.getDiscount();
		if(promo.isRedemption() || this.isMaxDiscountAmount(promotionAmt, promo.getPriority(), applied)){
			//Clear the previous discount.
			lineItem.removeLineItemDiscount();
			double pAmt = ((lineItem.getPrice()/lineItem.getQuantity())-promotionAmt>=0.0?promotionAmt:lineItem.getPrice()/lineItem.getQuantity());
			if(skuLimit > 0 && lineItem.getUnitPrice().indexOf("lb") != -1) {
				//For lineitems with LB, consider quantity as 1
				if(lineItem.getPrice() > promotionAmt)
					pAmt = promotionAmt;
				else
					pAmt = lineItem.getPrice();
			}
			//Add this discount.
			Discount discount = new Discount(promo.getPromotionCode(), EnumDiscountType.DOLLAR_OFF, pAmt);
			discount.setSkuLimit(skuLimit);
			lineItem.setDiscount(discount);
			return true;
		}
		return false;
	}

	public boolean applyZoneDiscount(PromotionI promo, double promotionAmt) {
		
		//Poll the promotion context to know if this is the max discount amount.
		Discount applied = this.getZoneDiscount();
		if(promo.isRedemption() || this.isMaxDiscountAmount(promotionAmt, promo.getPriority(), applied)) {
			//Clear the previous discount.
			if(applied != null)
				this.getShoppingCart().removeDiscount(applied.getPromotionCode());
			//Add this discount.
			Discount discount = new Discount(promo.getPromotionCode(), EnumDiscountType.DOLLAR_OFF, promotionAmt);
			this.addDiscount(discount);
			return true;
		}
		return false;
	}
	
	public void clearHeaderDiscounts(){
		//Clear all header discounts.
		this.user.getShoppingCart().setDiscounts(new ArrayList<ErpDiscountLineModel>());
	}
	
	private boolean isMaxDiscountAmount(double promotionAmt, int priority, Discount applied) {
		if(applied == null) return true;
		boolean flag = false;
		String appliedCode = applied.getPromotionCode();
		PromotionI appliedPromo = PromotionFactory.getInstance().getPromotion(appliedCode);
		if((priority < appliedPromo.getPriority()) ||
				(priority == appliedPromo.getPriority() &&
						promotionAmt > applied.getAmount())){
			//The applied promo priority is less than the one that is being applied.
			//or the applied promo amount is less than the one that is being applied.
			flag = true;
		}
		return flag;
	}

	public Discount getHeaderDiscount() {
		List<ErpDiscountLineModel> l = this.getShoppingCart().getDiscounts();
		if(l.isEmpty())
			return null;

		Iterator<ErpDiscountLineModel> i = l.iterator();
		//Get the applied discount from the cart.
		ErpDiscountLineModel model = i.next();
		if(model==null) return null;
		Discount applied = model.getDiscount();
		return applied; 
	}
	
	public Discount getZoneDiscount() {
		List<ErpDiscountLineModel> l = this.getShoppingCart().getDiscounts();
		if(l.isEmpty())
			return null;

		for(Iterator<ErpDiscountLineModel> i = l.iterator();i.hasNext();){
			//Get the applied discount from the cart.
			ErpDiscountLineModel model = i.next();
			if(model==null) return null;
			Discount applied = model.getDiscount();
			PromotionI promo = PromotionFactory.getInstance().getPromotion(applied.getPromotionCode());
			if( promo.getOfferType() != null &&promo.getOfferType().equals(EnumOfferType.WINDOW_STEERING)){
				return applied;
			}
		}
		return null;
	}
	
	public PromotionI getNonCombinableHeaderPromotion() {
		return this.getShoppingCart().getNonCombinableHeaderPromotion();
	}
	
	public boolean isPostPromoConflictEnabled(){
		return this.getUser().isPostPromoConflictEnabled();
	}

	public void clearLineItemDiscounts() {
		FDCartModel cart = this.getUser().getShoppingCart();
		cart.clearLineItemDiscounts();
	}

	public double getTotalLineItemDiscount() {
		FDCartModel cart = this.getUser().getShoppingCart();
		return cart.getTotalLineItemsDiscountAmount();
	}

	public Set<String> getLineItemDiscountCodes(){
		FDCartModel cart = this.getUser().getShoppingCart();
		return cart.getLineItemDiscountCodes();	
	}
	
	public UserContext getUserContext() {
		return this.getUser().getUserContext();
	}
	
	public int getSettledECheckOrderCount() {
		try {
			return this.user.getOrderHistory().getSettledECheckOrderCount();
		} catch (FDResourceException e) {
			throw new FDRuntimeException();
		}
	}
	
	public String getDeliveryZone(){
		return this.getUser().getShoppingCart().getDeliveryZone();
	}
	
	public FDReservation getDeliveryReservation() {
		return this.getUser().getShoppingCart().getDeliveryReservation();
	}
	
	public boolean isAlreadyRedeemedPromotion(String promoCode){
		boolean result = false;
		FDCartModel cart = this.getShoppingCart();
		if(cart instanceof FDModifyCartModel){
			FDModifyCartModel modifyCart = (FDModifyCartModel) cart;
			Set<String> usedPromoCodes = modifyCart.getOriginalOrder().getUsedPromotionCodes();
			result = usedPromoCodes.contains(promoCode);
		}
		return result;
	}
	
	public String getUsedWSPromotionCode(){
		String promoCode = null;
		FDCartModel cart = this.getShoppingCart();
		if(cart instanceof FDModifyCartModel){
			FDModifyCartModel modifyCart = (FDModifyCartModel) cart;
			promoCode = modifyCart.getOriginalOrder().getWSPromotionCode();
		}
		return promoCode;
	}
}
