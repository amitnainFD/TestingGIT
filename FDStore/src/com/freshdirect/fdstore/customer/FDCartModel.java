package com.freshdirect.fdstore.customer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.affiliate.ErpAffiliate;
import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.address.PhoneNumber;
import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.Discount;
import com.freshdirect.common.pricing.EnumDiscountType;
import com.freshdirect.common.pricing.EnumTaxationType;
import com.freshdirect.common.pricing.MaterialPrice;
import com.freshdirect.common.pricing.MunicipalityInfo;
import com.freshdirect.common.pricing.MunicipalityInfoWrapper;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.common.pricing.util.GroupScaleUtil;
import com.freshdirect.customer.EnumChargeType;
import com.freshdirect.customer.EnumTransactionSource;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpAppliedCreditModel;
import com.freshdirect.customer.ErpChargeLineModel;
import com.freshdirect.customer.ErpDeliveryPlantInfoModel;
import com.freshdirect.customer.ErpDepotAddressModel;
import com.freshdirect.customer.ErpDiscountLineModel;
import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionReason;
import com.freshdirect.delivery.restriction.FDRestrictedAvailabilityInfo;
import com.freshdirect.deliverypass.DeliveryPassType;
import com.freshdirect.deliverypass.DlvPassAvailabilityInfo;
import com.freshdirect.deliverypass.DlvPassConstants;
import com.freshdirect.fdlogistics.model.EnumDeliveryFeeTier;
import com.freshdirect.fdlogistics.model.FDDeliveryZoneInfo;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDException;
import com.freshdirect.fdstore.FDGroup;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDSku;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.ZonePriceListing;
import com.freshdirect.fdstore.atp.FDAvailabilityHelper;
import com.freshdirect.fdstore.atp.FDAvailabilityI;
import com.freshdirect.fdstore.atp.FDAvailabilityInfo;
import com.freshdirect.fdstore.atp.FDCompositeAvailabilityInfo;
import com.freshdirect.fdstore.atp.NullAvailability;
import com.freshdirect.fdstore.content.BrandModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.Recipe;
import com.freshdirect.fdstore.content.RecipeSource;
import com.freshdirect.fdstore.promotion.PromotionFactory;
import com.freshdirect.fdstore.promotion.PromotionI;
import com.freshdirect.fdstore.rules.EligibilityCalculator;
import com.freshdirect.fdstore.rules.FDRuleContextI;
import com.freshdirect.fdstore.rules.FeeCalculator;
import com.freshdirect.fdstore.rules.TieredPrice;
import com.freshdirect.fdstore.services.tax.AvalaraContext;
import com.freshdirect.fdstore.services.tax.TaxFactory;
import com.freshdirect.fdstore.services.tax.TaxFactoryImpl;
import com.freshdirect.fdstore.util.TimeslotLogic;
import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.util.DateRange;
import com.freshdirect.framework.util.FormatterUtil;
import com.freshdirect.framework.util.MathUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.giftcard.ErpGiftCardModel;
import com.freshdirect.logistics.fdstore.StateCounty;
import com.freshdirect.payment.EnumPaymentMethodType;

/**
 * FDShoppingCart model class.
 *
 * @version	$Revision:30$
 * @author	 $Author:Viktor Szathmary$
 * @stereotype fd-model
 */
public class FDCartModel extends ModelSupport implements FDCartI {
	
	private static final long serialVersionUID = -7672203060098085507L;
	 private static Category LOGGER = LoggerFactory.getInstance( FDCartModel.class );

	private void checkLimitPlus(int delta) {
		if (numberOfOrderLines() + delta >= ErpServicesProperties.getCartOrderLineLimit()) {
			throw new OrderLineLimitExceededException("Adding " + delta + " additional item" + (delta == 1 ? "" : "s") + " will exceed the limit of " +
					ErpServicesProperties.getCartOrderLineLimit());
		}
	}
	
	private void checkLimitTotal(int total) {
		if (total >= ErpServicesProperties.getCartOrderLineLimit()) {
			throw new OrderLineLimitExceededException("Limit of " + ErpServicesProperties.getCartOrderLineLimit() + " reached");
		}
	}
	
	/** Order cartlines by cartline description, configuration desc, quantity */
	public final static Comparator<FDCartLineI> NAME_COMPARATOR = new Comparator<FDCartLineI>() {
		public int compare(FDCartLineI cartLine1, FDCartLineI cartLine2) {
			int retValue = 0;
			retValue = cartLine1.getDescription().compareTo(cartLine2.getDescription());
			if (retValue == 0) {
				retValue = cartLine1.getConfigurationDesc().compareTo(cartLine2.getConfigurationDesc());
				if (retValue == 0) { // dept * desc * configDesc matches, check quantity
					if (cartLine1.getQuantity() <= cartLine2.getQuantity()) {
						retValue = -1;
					} else {
						retValue = 1;
					}
				}
			}
			return retValue;
		}
	};

	/** Order cartlines by department desc, description, configuration desc, quantity */
	public final static Comparator<FDCartLineI> LINE_COMPARATOR = new Comparator<FDCartLineI>() {
		public int compare(FDCartLineI cartLine1, FDCartLineI cartLine2) {
				///order by Department and then by product.
			int retValue = 0;
			if(null != cartLine1.getDepartmentDesc() && null != cartLine2.getDepartmentDesc()){
			retValue = cartLine1.getDepartmentDesc().compareTo(cartLine2.getDepartmentDesc());			
			if (cartLine1.getDepartmentDesc().compareTo(cartLine2.getDepartmentDesc()) == 0) {
				retValue = cartLine1.getDescription().compareTo(cartLine2.getDescription());
				if (retValue == 0) {
					retValue = cartLine1.getConfigurationDesc().compareTo(cartLine2.getConfigurationDesc());
					if (retValue == 0) { //dept * desc * configDesc matches, check quantity
						if (cartLine1.getQuantity() <= cartLine2.getQuantity()) {
							retValue = -1;
						} else {
							retValue = 1;
						}
					}
				}
			}
			}
			return retValue;
		}
	};

	public final static Comparator<FDCartLineI> PRODUCT_SAMPLE_COMPARATOR = new Comparator<FDCartLineI>() {
		public int compare(FDCartLineI cartLine1, FDCartLineI cartLine2) {
			// /order by Department and then by product.
			int retValue = 0;
			if (null != cartLine1.getDepartmentDesc()&& null != cartLine2.getDepartmentDesc()) {
				retValue = cartLine1.getDepartmentDesc().compareTo(cartLine2.getDepartmentDesc());
				if (cartLine1.getDepartmentDesc().compareTo(cartLine2.getDepartmentDesc()) == 0) {
					retValue = cartLine1.getDescription().compareTo(cartLine2.getDescription());
					if (retValue == 0) {
						retValue = cartLine1.getConfigurationDesc().compareTo(cartLine2.getConfigurationDesc());
						if (retValue == 0) { // dept * desc * configDesc
												// matches, check quantity
							if (cartLine1.getQuantity() <= cartLine2.getQuantity()) {
								retValue = -1;
							} else {
								retValue = 1;
							}
						}
					}
				} else if (null != cartLine1.getDiscount() && cartLine1.getDiscount().getDiscountType().equals(EnumDiscountType.FREE)) {
					retValue = 1;
				} else if (null != cartLine2.getDiscount() && cartLine2.getDiscount().getDiscountType().equals(EnumDiscountType.FREE)) {
					retValue = -1;
				}
			}
			return retValue;
		}
	};

	public final static Comparator<FDCartLineI> EXTERNAL_GROUPS_IN_FRONT_COMPARATOR = new Comparator<FDCartLineI>() {
		public int compare(FDCartLineI cartLine1, FDCartLineI cartLine2) {
			String externalGroup1 = cartLine1.getExternalGroup();
			String externalGroup2 = cartLine2.getExternalGroup();
			
			if (externalGroup1==null){
				if (externalGroup2==null){
					return 0;
				} else {
					return 1;
				}
			} else {
				if (externalGroup2==null){
					return -1;
				} else {
					return externalGroup1.compareTo(externalGroup2);
				}
			}
		}
	};
	
	/**
	 * @associates <{com.freshdirect.fdstore.customer.FDCartLineI}>
	 * @link aggregationByValue
	 * @clientCardinality 1
	 * @supplierCardinality 0..*
	 */
	protected final List<FDCartLineI> orderLines = new ArrayList<FDCartLineI>();
	
	protected final List<FDCartLineI> invalidLines = new ArrayList<FDCartLineI>();

	private final List<FDCartLineI> sampleLines = new ArrayList<FDCartLineI>();

	private final List<ErpChargeLineModel> charges = new ArrayList<ErpChargeLineModel>();

	private List<ErpAppliedCreditModel> customerCredits = new ArrayList<ErpAppliedCreditModel>();
	
	private FDReservation deliveryReservation = null;
	
	private Date modificationCutoffTime =  null;

	private ErpAddressModel deliveryAddress;

	private ErpPaymentMethodI paymentMethod;

	private FDDeliveryZoneInfo zoneInfo;

	private String customerServiceMessage;
	private String marketingMessage;

	private boolean ageVerified = false;
	
	private String expCouponDeliveryDate = null;
	
	private List<ErpDiscountLineModel> discounts = new ArrayList<ErpDiscountLineModel>();
	
	//
	// "transient" fields below
	//

	private final List<FDCartLineI> recentOrderLines = new ArrayList<FDCartLineI>();
	
	private transient FDAvailabilityI availability;
	//This attribute is to hold the count of deliverypasses this cart holds.
	private int deliveryPassCount = 0;

	//This attribute flag is to denote whether a delivery pass was applied to this cart.
	private boolean dlvPassApplied;
	
	private boolean dlvPassPremiumAllowedTC;
	
	//This attribute flag is to denote whether a delivery promotion was applied to this cart.
	private boolean dlvPromotionApplied;
	
	//This attribute contains the list of unavailable delivery passes to the user.
	private List<DlvPassAvailabilityInfo> unavailablePasses;
	
	//Selected gift cards
	private List<ErpGiftCardModel> selectedGiftCards = null;
	
	private double bufferAmt = 0.0;
	
	private int currentDlvPassExtendDays;
	
	private ExtendDPDiscountModel dlvPassExtn;
	
	private Map<String,Integer> skuCount = new HashMap<String,Integer>();
	
	private List<FDCartLineI> ebtIneligibleOrderLines = new ArrayList<FDCartLineI>();
	private Set<String> recentlyAppliedCoupons = new HashSet<String>();
	
	private EnumTransactionSource source;
	private EnumEStoreId eStore;
	private ErpDeliveryPlantInfoModel deliveryPlantInfo;
	
	private PhoneNumber orderMobileNumber;
	
	private boolean isCustomTip;

	public void incrementSkuCount(String promoCode, int quantity) {
		Integer count =skuCount.get(promoCode);
		if(null== count){
			count=new Integer(0);
		}
		count += quantity;
		skuCount.put(promoCode, count);
	}
	
	public Map<String,Integer> getSkuCount() {
		return skuCount;		
	}
	
	public void clearSkuCount(){
		skuCount.clear();
	}
	
	/*public void setSkuCount(int skuCount) {
		this.skuCount = skuCount;
	}*/
	
	public boolean isDlvPassApplied() {
		if(EnumEStoreId.FDX.equals(getEStoreId()))
			return false;
		return dlvPassApplied;
	}

	public void setDlvPassApplied(boolean dlvPassApplied) {
		this.dlvPassApplied = dlvPassApplied;
	}

	
	public boolean isDlvPromotionApplied() {
		return dlvPromotionApplied;
	}

	public void setDlvPromotionApplied(boolean dlvPromotionApplied) {
		this.dlvPromotionApplied = dlvPromotionApplied;
	}

	public int getDeliveryPassCount() {
		return this.deliveryPassCount;
	}

	public void setDeliveryPassCount(int dlvPassCount) {
		this.deliveryPassCount = dlvPassCount;
	}
	public double getPremiumFee(FDRuleContextI ctx)
	{
		EligibilityCalculator calc = new EligibilityCalculator("DLVPREMIUM");
		return calc.getPremiumFee(ctx);
	}

	
	public FDCartModel() {
	}

	public FDCartModel(FDCartModel cart) {
	
		List<FDCartLineI> orderLinesCopied = new ArrayList<FDCartLineI>();
		// For FDX - to avoid modifying the current cart when we check against the inventory - order line object is also cloned.
		if(cart!=null && cart.getEStoreId()!=null && cart.getEStoreId().getContentId()!=null && "FDX".equalsIgnoreCase(cart.getEStoreId().getContentId())) {
		for(FDCartLineI orderLineItr : cart.orderLines) {
			FDCartLineModel newLine = new FDCartLineModel(orderLineItr.getSku(), orderLineItr
					.getProductRef().lookupProductModel(), orderLineItr.getConfiguration(), orderLineItr.getVariantId(), orderLineItr.getUserContext());
			orderLinesCopied.add(newLine);
		} 	
		setOrderLines(orderLinesCopied);		
		} else {
			setOrderLines(cart.orderLines);
		}
		setCharges(cart.charges);
		setDiscounts(cart.discounts);
		setCustomerCredits(new ArrayList<ErpAppliedCreditModel>(cart.customerCredits));

		setZoneInfo(cart.zoneInfo);
		setDeliveryReservation(cart.deliveryReservation);
		setDeliveryAddress(cart.deliveryAddress);
		setDeliveryPlantInfo(cart.deliveryPlantInfo);
		setPaymentMethod(cart.paymentMethod);
		setEStoreId(cart.getEStoreId());
	}

	
	
	//public void calculateGroupPrice(String zoneId){		
		
		/*
		Map grpMap=new HashMap();	
		List<FDCartLineI> lineList=this.getOrderLines();
		
		for(FDCartLineI line:lineList){												
			String grpId=null;
			FDProduct product;
			try {
				product = FDCachedFactory.getProduct(line.getSku());
				grpId=product.getGrpId();
				if(grpId!=null){
					List cartList=(List)grpMap.get(grpId);
					if(cartList==null){
						cartList=new ArrayList();
						cartList.add(line);
						grpMap.put(grpId, cartList);
					}else{
						cartList.add(line);
					}
					
					if(grpMap.size()>0){
						Set<String> keySet=grpMap.keySet();
						for(String key:keySet){
							ErpGrpPriceModel model=FDCachedFactory.getGrpInfo(key);
							if(model!=null){
								List<FDCartLineI> cartLines=(List)grpMap.get(key);																
							     ErpGrpPriceZoneModel zonePriceModel= model.getGrpPriceModel(zoneId).;
								 double reqQty=zonePriceModel.getQty();								 
								 double totalQty=0.0;
								 
								 for(FDCartLineI li:cartLines){
									 totalQty=totalQty+li.getQuantity();
								 }
								 
								 if(reqQty>=totalQty){									 
									 for(FDCartLineI li:cartLines){
										li.setGrpId(key);
										li.setGrpVersion(zonePriceModel.getVersion());
										try {
											li.refreshConfiguration();
										} catch (FDException e) {
											// !!! improve error handling
											throw new FDRuntimeException(e);
										}							
									 } 
								 }
								 else{
									 for(FDCartLineI li:cartLines){
											li.setGrpId(null);
											li.setGrpVersion(0);
											try {
												li.refreshConfiguration();
											} catch (FDException e) {
												// !!! improve error handling
												throw new FDRuntimeException(e);
											}							
										 } 									 
								 }
							}																													
						}
					}
				}
				
			} catch (FDResourceException fdre) {
				//LOGGER.warn("Error accessing resource", fdre);
				throw new RuntimeException("Error accessing resource " + fdre.getMessage());
			} catch (FDSkuNotFoundException fdsnfe) {
				//LOGGER.warn("SKU not found", fdsnfe);
				throw new RuntimeException("SKU not found", fdsnfe);
			}		
		}
		*/
	//}
	
	
	
	public void addOrderLine(FDCartLineI orderLine) {
		checkLimitPlus(1);
		this.orderLines.add(orderLine);
		this.recentOrderLines.clear();
		this.recentOrderLines.add(orderLine);
		this.clearAvailability();
		
	}

	public void addOrderLines(Collection<FDCartLineI> cartLines) {
		checkLimitPlus(cartLines.size());
		this.orderLines.addAll(cartLines);
		this.recentOrderLines.clear();
		this.recentOrderLines.addAll(cartLines);
		this.clearAvailability();
	}

	public void mergeCart(FDCartModel cart) {
		this.addOrderLines(cart.getOrderLines());
	}

	public int numberOfOrderLines() {
		return this.orderLines.size();
	}

	public FDCartLineI getOrderLine(int index) {
		return this.orderLines.get(index);
	}

	/**
	 * Find an orderline by it's randomId.
	 *
	 * @return index of orderline, -1 if not found
	 */
	public int getOrderLineIndex(int randomId) {
		int c = 0;
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext(); c++) {
			if (randomId == i.next().getRandomId()) {
				return c;
			}
		}
		return -1;
	}

	public FDCartLineI getOrderLineById(int randomId) {
		int idx = this.getOrderLineIndex(randomId);
		//FDCartLineI c=this.getOrderLineById(randomId);
		return idx == -1 ? null : this.getOrderLine(idx);
	}

	public void setOrderLines(List<FDCartLineI> lines) {
		checkLimitTotal(lines.size());
		this.orderLines.clear();
		this.orderLines.addAll(lines);
		this.recentOrderLines.clear();
		this.recentOrderLines.addAll(lines);
		this.clearAvailability();
	}

	public void setOrderLine(int index, FDCartLineI orderLine) {
		this.orderLines.set(index, orderLine);
		this.recentOrderLines.clear();
		this.recentOrderLines.add(orderLine);
		this.clearAvailability();
	}

	public void removeOrderLine(int index) {
		this.orderLines.remove(index);
		this.recentOrderLines.clear();
		this.clearAvailability();
		
	}

	public boolean removeOrderLineById(int randomId) {
		int idx = this.getOrderLineIndex(randomId);
		if (idx == -1) {
			return false;
		}
		this.removeOrderLine(idx);
		return true;
	}

	public boolean removeOrderLine( FDCartLineI cartLine ) {
		return removeOrderLineById( cartLine.getRandomId() );
	}
	
	/**
	 *  Tell if the shopping cart contains any items that were ordered
	 *  by selecting a recipe.
	 *  
	 *  @return true if the shopping cart contains items ordered through
	 *          a recipe, false otherwise
	 */
	public boolean containsRecipeItems() {
		for ( FDCartLineI line : orderLines ) {
			if (line.getRecipeSourceId() != null) {
				return true;
			}
		}		
		return false;
	}
	
	/**
	 *  Check if the shopping cart contains items ordered through
	 *  some recipes.
	 *
	 *  @param recipeIds a list of key codes (Strings) of recipes.
	 *  @return true if the shopping cart contains items from all the supplied
	 *          recipes IDs, false otherwise.
	 */
	public boolean containsRecipeItems(List<String> recipeIds) {
		Set<String> ids = new HashSet<String>(recipeIds);
		
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext() && !ids.isEmpty();) {
			FDCartLineI line     = i.next();
			String      recipeId = line.getRecipeSourceId();
			
			if (recipeId != null) {
				ids.remove(recipeId);
			}
		}
		
		return ids.isEmpty();
	}

	/**
	 *  Check if the shopping cart has recipes from a specific recipe book.
	 *
	 *  @param sourceId the ID of the book to check for.
	 *  @return true if the shopping cart contains items from a recipe from
	 *          the specified book, false otherwise.
	 */
	public boolean containsRecipeFromBook(String sourceId) {
		Set<String> recipesIds = new HashSet<String>();
		
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line     = i.next();
			String      recipeId = line.getRecipeSourceId();
			
			if (recipeId == null) {
				continue;
			}
			if (recipesIds.contains(recipeId)) {
				// check each recipe only once
				continue;
			}
			
			recipesIds.add(recipeId);
			
			Recipe       recipe   = (Recipe) ContentFactory.getInstance().getContentNode(recipeId);
			RecipeSource source   = recipe.getSource();
			
			if (source != null && source.getContentKey().getId().equals(sourceId)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Remove all orderlines with a specific recipe source ID.
	 * 
	 * @return number of items removed
	 */
	public List<FDCartLineI> removeOrderLinesByRecipe(String recipeId) {
		List<FDCartLineI> cartLinesRemoved = new ArrayList<FDCartLineI>();
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if (recipeId.equals(line.getRecipeSourceId())) {
				cartLinesRemoved.add(line);
				i.remove();
			}
		}
		this.recentOrderLines.clear();
		this.clearAvailability();
		return cartLinesRemoved;
	}

	public void clearOrderLines() {
		this.orderLines.clear();
		this.recentOrderLines.clear();
		this.clearAvailability();
	}

	public List<FDCartLineI> getOrderLines() {
		return Collections.unmodifiableList(this.orderLines);
	}

	public List<FDCartLineI> getSampleLines() {
		return Collections.unmodifiableList(this.sampleLines);
	}

	public void addSampleLine(FDCartLineI orderLine) {
		this.sampleLines.add(orderLine);
	}

	public void clearSampleLines() {
		this.sampleLines.clear();
	}

	/**
	 *  Return the recently ordered line items.
	 *  
	 *  @return an unmodifiable list of FDCartLineModel objects.
	 */
	public List<FDCartLineI> getRecentOrderLines() {
		return Collections.unmodifiableList(this.recentOrderLines);
	}

	/**
	 * Sort orderlines with default comparator.
	 */
	public void sortOrderLines() {
		Collections.sort(this.orderLines, PRODUCT_SAMPLE_COMPARATOR);
	}

	public boolean isEstimatedPrice() {
		for (Iterator<FDCartLineI> i = orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if (line.isEstimatedPrice()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return total price of orderlines in USD, with no taxes, charges or discounts applied
	 */	
	public double getSubTotal() {
		double subTotal = 0.0;
		for ( FDCartLineI cartLineModel : orderLines ) {			
			subTotal += MathUtil.roundDecimal( cartLineModel.getPrice() );
		}
		return MathUtil.roundDecimal(subTotal);
	}
	
	
	/**
	 * @return total price of orderlines in USD, with no taxes and charges, but with discounts applied
	 */	
	public double getActualSubTotal() {
		double subTotal = 0.0;
		for ( FDCartLineI cartLineModel : orderLines ) {			
			subTotal += MathUtil.roundDecimal(cartLineModel.getPrice());
			// add the discount amount for reporting
			subTotal += MathUtil.roundDecimal(cartLineModel.getDiscountAmount());
		}
				
		return MathUtil.roundDecimal(subTotal);
	}
	
	
	/**  
	 * @return total price of orderlines in USD, with taxes, charges without discounts applied
	 */    
	public double getPreDeductionTotal(){	      
	      double preTotal = 0.0;
	        preTotal += MathUtil.roundDecimal(this.getSubTotal());			
	        preTotal += MathUtil.roundDecimal(this.getTaxValue());
	        preTotal += MathUtil.roundDecimal(this.getDepositValue());

			// apply charges
			for ( ErpChargeLineModel charge : charges ) {
				preTotal += MathUtil.roundDecimal( charge.getTotalAmount() );
			}
           return MathUtil.roundDecimal(preTotal);
	}

	/**
	 * @return amount of tax USD
	 */
	public double getTaxValue() {
		double taxValue = 0.0;
	
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			taxValue += i.next().getTaxValue();
		}
		
		for(Iterator<ErpChargeLineModel> i = this.charges.iterator(); i.hasNext(); ) {
			ErpChargeLineModel c = i.next();
			taxValue += c.getTotalAmount() * c.getTaxRate();
		}
		
		return taxValue;
	}

	public double getAvalaraTaxValue(AvalaraContext avalaraContext){
		if(FDStoreProperties.getAvalaraTaxEnabled()){
			TaxFactory taxFactory = new TaxFactoryImpl();
			taxFactory.getTax(avalaraContext);
		}		
		return getTaxValue();
	}
	
	public double getDepositValue() {
		double depositValue = 0.0;
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			depositValue += i.next().getDepositValue();
		}
		return depositValue;
	}

	/**
	 * @return total price of order in USD, with discounts, taxes, etc
	 */
	public double getTotal() {
		double total = 0.0;
		total += MathUtil.roundDecimal(this.getSubTotal());		
		total += MathUtil.roundDecimal(this.getTaxValue());
		total += MathUtil.roundDecimal(this.getDepositValue());
		// apply charges
		for (Iterator<ErpChargeLineModel> i = this.charges.iterator(); i.hasNext();) {
			total += MathUtil.roundDecimal(i.next().getTotalAmount());
		}
		total -= this.getTotalDiscountValue();
		total -= this.getCustomerCreditsValue();

		return MathUtil.roundDecimal(total);
	}

	public FDReservation getDeliveryReservation() {
		return this.deliveryReservation;
	}

	public void setDeliveryReservation(FDReservation deliveryReservation) {
		this.deliveryReservation = deliveryReservation;
	}
	
	public Date getModificationCutoffTime() {
		return this.modificationCutoffTime;
	}

	public void setModificationCutoffTime(Date modificationCutoffTime) {
		this.modificationCutoffTime = modificationCutoffTime;
	}
	
	public String getDeliveryZone() {
		if(this.zoneInfo != null)
			return this.zoneInfo.getZoneCode();
		else
			return null;
	}

	//
	//
	// charge accessor methods
	//
	//

	public Collection<ErpChargeLineModel> getCharges() {
		return Collections.unmodifiableCollection(this.charges);
	}

	protected void setCharges(Collection<ErpChargeLineModel> charges) {
		this.charges.clear();
		this.charges.addAll(charges);
	}

	public ErpChargeLineModel getCharge(EnumChargeType chargeType) {
		ErpChargeLineModel charge = null;
		for (Iterator<ErpChargeLineModel> i = this.charges.iterator(); i.hasNext();) {
			ErpChargeLineModel curr = i.next();
			if (chargeType.equals(curr.getType())) {
				if (charge != null) {
					// throw new RuntimeException("Multiple charges present of type " + chargeType);
				}
				charge = curr;
			}
		}
		return charge;
	}
	
	public void clearCharge(EnumChargeType chargeType) {
		for (Iterator<ErpChargeLineModel> i = this.charges.iterator(); i.hasNext();) {
			ErpChargeLineModel curr = i.next();
			if (chargeType.equals(curr.getType())) {
				i.remove();
			}
		}
	}

	public double getChargeAmount(EnumChargeType chargeType) {
		ErpChargeLineModel charge = this.getCharge(chargeType);
		return charge == null ? 0.0 : charge.getAmount();
	}
	
	public double getChargeAmountDiscountApplied(EnumChargeType chargeType) {
		ErpChargeLineModel charge = this.getCharge(chargeType);
		return charge == null ? 0.0 : charge.getTotalAmount();
	}
	
	public void setChargeAmount(EnumChargeType chargeType, double amount) {
		ErpChargeLineModel charge = this.getCharge(chargeType);
		if (charge == null) {
			// not found, create it
			charge = new ErpChargeLineModel();
			charge.setType(chargeType);
			this.charges.add(charge);
		}
		charge.setAmount(amount);
	}

	
	public boolean isChargeWaived(EnumChargeType chargeType) {
		ErpChargeLineModel charge = this.getCharge(chargeType);
		return charge == null ? false : charge.getDiscount() != null;
	}

	public boolean isChargeTaxable(EnumChargeType chargeType) {
		ErpChargeLineModel charge = this.getCharge(chargeType);
		return charge == null ? false : charge.getTaxRate() > 0;
	}

	public void setChargeWaived(EnumChargeType chargeType, boolean waived, String promotionCode) {
		ErpChargeLineModel charge = this.getCharge(chargeType);
		if (charge == null) {
			// not found, create it
			charge = new ErpChargeLineModel();
			charge.setType(chargeType);
			this.charges.add(charge);
		}
		charge.setDiscount(waived ? new Discount(promotionCode, EnumDiscountType.PERCENT_OFF, 1.0) : null);

		// MISCELLANEOUS charge waiving is pegged to DELIVERY
		if (EnumChargeType.DELIVERY.equals(chargeType)) {
			setChargeWaived(EnumChargeType.MISCELLANEOUS, waived, promotionCode);
		}
	}
	
	public void setChargeWaived(EnumChargeType chargeType, boolean waived, String promotionCode, boolean fuelSurcharge) {
		ErpChargeLineModel charge = this.getCharge(chargeType);
		if (charge == null) {
			// not found, create it
			charge = new ErpChargeLineModel();
			charge.setType(chargeType);
			this.charges.add(charge);
		}
		charge.setDiscount(waived ? new Discount(promotionCode, EnumDiscountType.PERCENT_OFF, 1.0) : null);

		// MISCELLANEOUS charge waiving is pegged to DELIVERY
		if (EnumChargeType.DELIVERY.equals(chargeType)) {
			setChargeWaived(EnumChargeType.MISCELLANEOUS, fuelSurcharge, promotionCode);
		}
	}

	//
	//
	// simplified charge accessor methods
	//
	//

	
	public double getDeliverySurcharge() {
		return this.getChargeAmount(EnumChargeType.DELIVERY)+this.getChargeAmount(EnumChargeType.DLVPREMIUM);
	}
	
	public double getDeliveryCharge() {
		return this.getChargeAmountDiscountApplied(EnumChargeType.DELIVERY)+this.getChargeAmountDiscountApplied(EnumChargeType.DLVPREMIUM);
	}
	
	public double getDeliveryPremium() {
		return this.getChargeAmount(EnumChargeType.DLVPREMIUM);
	}

	public boolean isDeliveryChargeWaived() {
		return this.isChargeWaived(EnumChargeType.DELIVERY) && (this.getCharge(EnumChargeType.DLVPREMIUM)==null || this.isChargeWaived(EnumChargeType.DLVPREMIUM) );
	}
	
	public boolean isDeliverySurChargeWaived() {
		return this.isChargeWaived(EnumChargeType.MISCELLANEOUS);
	}
	
	public boolean isDeliveryChargeTaxable() {
		return this.isChargeTaxable(EnumChargeType.DELIVERY) || this.isChargeTaxable(EnumChargeType.DLVPREMIUM);
	}
	
	public double getPhoneCharge() {
		return this.getChargeAmount(EnumChargeType.PHONE);
	}
	
	public boolean isPhoneChargeWaived() {
		return this.isChargeWaived(EnumChargeType.PHONE);
	}

	public boolean isPhoneChargeTaxable() {
		return this.isChargeTaxable(EnumChargeType.PHONE);
	}

	public double getMiscellaneousCharge() {
		return this.getChargeAmount(EnumChargeType.MISCELLANEOUS);
	}

	public boolean isMiscellaneousChargeWaived() {
		return this.isChargeWaived(EnumChargeType.MISCELLANEOUS);
	}
	
	public boolean isMiscellaneousChargeTaxable() {
		return this.isChargeTaxable(EnumChargeType.MISCELLANEOUS);
	}

	public double getCCDeclinedCharge() {
		return this.getChargeAmount(EnumChargeType.CC_DECLINED);
	}
	
	//
	//
	//

	public ErpAddressModel getDeliveryAddress() {
		return this.deliveryAddress;
	}

	public void setDeliveryAddress(ErpAddressModel deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
		//
		/*ErpDeliveryPlantInfoModel dpi=this.getDeliveryPlantInfo();
		if (dpi!=null) {
			dpi.setPlantId("1000");
			dpi.setSalesOrg("1000");
			dpi.setDistChannel("1000");
			
		}*/
		
		
			
	}
	
	public boolean isAddressMismatch() {
		if (this.deliveryAddress == null || this.paymentMethod == null) {
			return false;
		}

		if (this.deliveryAddress instanceof ErpDepotAddressModel) {
			return false;
		}

		AddressModel billAddr = paymentMethod.getAddress();

		return !this.deliveryAddress.isSameLocation(billAddr);
	}

	public ErpPaymentMethodI getPaymentMethod() {
		return this.paymentMethod;
	}

	public void setPaymentMethod(ErpPaymentMethodI paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public FDAvailabilityI getAvailability() {
		return this.availability == null ? NullAvailability.AVAILABLE : this.availability;
	}

	public void setAvailability(FDAvailabilityI availability) {
		this.availability = availability;
	}
	
	private void clearAvailability() {
		this.availability = null;
	}
	
	public boolean isAvailabilityChecked() {
		return null != this.availability && getUnavailabilityMap().size() == 0;
	}
	
	/**
	 * @return Map of cartLineId -> FDAvailabilityInfo of all unavailable lines for req. date
	 */
	public Map<String,FDAvailabilityInfo> getUnavailabilityMap() {
		if(this.deliveryReservation == null){
			return Collections.<String,FDAvailabilityInfo>emptyMap();
		}
		
		DateRange requestedRange = new DateRange(deliveryReservation.getStartTime(), deliveryReservation.getEndTime());
		FDAvailabilityInfo info = this.getAvailability().availableCompletely(requestedRange);
		if (info.isAvailable()) {
			return Collections.<String,FDAvailabilityInfo>emptyMap();
		}
		return ((FDCompositeAvailabilityInfo)info).getComponentInfo();
	}
	
	/**
	 * @return first date within horizon days on which whole cart is available null if no date
	 */
	public Date getFirstAvailableDate() {
		return FDAvailabilityHelper.getFirstAvailableDate(this.getAvailability());
	}

	public Set<String> getRestrictionMessages() {
		Set<String> rMsgs = new HashSet<String>();
		for ( FDAvailabilityInfo ai : getUnavailabilityMap().values() ) {
			if ( ai instanceof FDRestrictedAvailabilityInfo ) {
				FDRestrictedAvailabilityInfo info = (FDRestrictedAvailabilityInfo)ai;
				rMsgs.add( info.getRestriction().getMessage() );
			}
		}
		return rMsgs;
	}

	public boolean hasKosherRestrictions() {
		for ( FDAvailabilityInfo ai : getUnavailabilityMap().values() ) {
			if ( ai instanceof FDRestrictedAvailabilityInfo ) {
				FDRestrictedAvailabilityInfo info = (FDRestrictedAvailabilityInfo)ai;
				if ( EnumDlvRestrictionReason.KOSHER.equals( info.getRestriction().getReason() ) ) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isFullyAvailable() {
		return this.getUnavailabilityMap().isEmpty();
	}

	public void setCustomerCredits(List<ErpAppliedCreditModel> credits) {
		this.customerCredits = credits;
	}

	public List<ErpAppliedCreditModel> getCustomerCredits() {
		return this.customerCredits;
	}
	
	public double getCustomerCreditsValue() {
		double creditAmount = 0.0;
		for (Iterator<ErpAppliedCreditModel> it = this.customerCredits.iterator(); it.hasNext();) {
			ErpAppliedCreditModel creditModel = it.next();
			creditAmount += MathUtil.roundDecimal(creditModel.getAmount());
		}
		return MathUtil.roundDecimal(creditAmount);
	}
	
	public String getFormattedCustomerCreditsValue() {
		return FormatterUtil.formatToTwoDecimal(getCustomerCreditsValue());
	}

	public FDDeliveryZoneInfo getZoneInfo() {
		return this.zoneInfo;
	}

	/**
	 * Set the zone info, update delivery charge from zoneInfo.
	 */
	public void setZoneInfo(FDDeliveryZoneInfo zoneInfo) {
		this.zoneInfo = zoneInfo;
		LOGGER.info("SETTING ZONEINFO"+ zoneInfo);
		//this.setChargeAmount(EnumChargeType.DELIVERY, zoneInfo == null ? 0.0 : zoneInfo.getDeliveryCharges());
	}

	
	
	public String getCustomerServiceMessage() {
		return this.customerServiceMessage;
	}
	public void setCustomerServiceMessage(String s) {
		this.customerServiceMessage = s;
	}

	public String getMarketingMessage() {
		return this.marketingMessage;
	}
	public void setMarketingMessage(String s) {
		this.marketingMessage = s;
	}
	
	public String getDeliveryInstructions(){
		if(this.deliveryAddress != null){
			return this.deliveryAddress.getInstructions();
		}
		return "";
	}

	public boolean isAgeVerified() {
		return ageVerified;
	}

	public void setAgeVerified(boolean ageVerified) {
		this.ageVerified = ageVerified;
	}

	public boolean hasItemsFrom(String[] contentNames) throws FDResourceException {
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			ProductModel pm = ContentFactory.getInstance().getProductByName(line.getCategoryName(), line.getProductName());
			if(null !=pm){
				if (pm.hasParentWithName(contentNames)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasBrandName(String brandName) throws FDResourceException {
		Set<String> brands = new HashSet<String>();
		brands.add(brandName);
		return this.hasBrandName(brands);
	}

	public boolean hasBrandName(Set<String> brandNames) throws FDResourceException {
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			ProductModel pm = ContentFactory.getInstance().getProductByName(line.getCategoryName(), line.getProductName());
			if(null !=pm){
				for ( BrandModel brand : pm.getBrands() ) {
					if (brandNames.contains(brand.getContentName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean containsAlcohol() {
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if (line.isAlcohol()) {
				return true;
			}
		}
		return false;
	}

	public boolean containsWineAndSpirit() {
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if (line.isAlcohol() && line.isWine()) {
				return true;
			}
		}
		return false;
	}
	public double getSubTotalWithoutAlcohol() {
		double subTotal = 0.0;
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if (!line.isAlcohol()) {
				subTotal += line.getPrice();
			}
		}

		return subTotal;
	}

	public double getSubTotalWithoutWineAndSpirit() {
		double subTotal = 0.0;
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if (!line.isWine()) {
				subTotal += line.getPrice();
			}
		}

		return subTotal;
	}

	public void removeAlcoholicLines() {
		for (ListIterator<FDCartLineI> i = this.orderLines.listIterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if (line.isAlcohol()) {
				i.remove();
			}
		}
	}

	public void removeWineAndSpiritLines() {
		for (ListIterator<FDCartLineI> i = this.orderLines.listIterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if (line.isAlcohol() && line.isWine()) {
				i.remove();
			}
		}
	}

	public boolean containsKosherItems() {
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if (line.isKosher()) {
				return true;
			}
		}
		return false;
	}

	public Set<EnumDlvRestrictionReason> getApplicableRestrictions() {
		Set<EnumDlvRestrictionReason> restrictions = new HashSet<EnumDlvRestrictionReason>();
		for ( FDCartLineI line : orderLines ) {
			restrictions.addAll(line.getApplicableRestrictions());
		}
		return restrictions;
	}
	
	public double getTotalQuantity(ProductModel product, boolean checkCategory){
		String categoryName = product.getParentNode().getContentName();
		String productName = product.getContentName();
		double sum = 0;
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if (productName.equals(line.getProductName())) {
				
				if((!checkCategory) || (checkCategory && categoryName.equals(line.getCategoryName()))){
					sum += line.getQuantity();					
				}
			}
		}
		return sum;
	}
	
	public Double getProductSampleQuantity(ProductModel productModel) {		
		String productName = productModel.getContentName();
		double sum = 0.0;
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if (productName.equals(line.getProductName())) {
			if(EnumDiscountType.FREE.equals(line.getDiscount()!=null?line.getDiscount().getDiscountType():null)){
				sum += line.getQuantity();
				}
			}
		}
		return sum;
	}

	/**
	 * Get the total quantity of all occurrences of a product in the cart.
	 */
	public double getTotalQuantity(ProductModel product) {		
		return getTotalQuantity(product, true);
	}

	/**
	 * Get the total item count in cart.
	 */
	public int getItemCount() {
		int sum = 0;
		for (FDCartLineI cartLine : orderLines ) {	
			if ( cartLine.isSoldBySalesUnits() ) 
				sum++;
			else
				sum += Math.round( (float)cartLine.getQuantity() );
		}
		return sum;
	}
	
	/**
	 * Remove stale orderlines, reprice w/ current prices, finally sort the cart.
	 */
	public void doCleanup() throws FDResourceException {
		if (this.numberOfOrderLines() == 0) {
			return;
		}

		List<FDCartLineI> cleanLines = OrderLineUtil.update(orderLines,true);

		this.setOrderLines(cleanLines);
		this.sortOrderLines();
		
		if( this.orderLines.size()>0){		  	
			PricingContext pCtx=orderLines.get(0).getUserContext().getPricingContext();
			  this.calculateGroupPrice((pCtx!=null)?pCtx.getZoneInfo():ZonePriceListing.DEFAULT_ZONE_INFO);
			  this.calculateScaleQuantity();
		}
		
	}

	public void refreshAll(boolean recalculateGroupScale) throws FDResourceException, FDInvalidConfigurationException {
		if( this.orderLines.size()>0 && recalculateGroupScale){		  	
			PricingContext pCtx=orderLines.get(0).getUserContext().getPricingContext();
			  this.calculateGroupPrice((pCtx!=null)?pCtx.getZoneInfo():ZonePriceListing.DEFAULT_ZONE_INFO);
			  this.calculateScaleQuantity();

		}

		for ( FDCartLineI cartLine : orderLines ) {
			cartLine.refreshConfiguration();
		}
		
	}

	protected void calculateGroupPrice(ZoneInfo pricingZone ) throws FDResourceException{
		List<FDCartLineI> eligibleCartLines = new ArrayList<FDCartLineI>();
		Map<FDGroup, Double> groupMap = new HashMap<FDGroup, Double>();
		Map<FDGroup, Double> qualifiedGroupMap = new HashMap<FDGroup, Double>();
		Map<String, FDGroup> qualifiedGrpIdMap = new HashMap<String, FDGroup>();
		for ( FDCartLineI cartLine : orderLines ) {
			//Clear group quantity.
			cartLine.setGroupQuantity(0.0);
			FDGroup group = cartLine.getFDGroup();
			if(group != null){
				MaterialPrice matPrice = GroupScaleUtil.getGroupScalePrice(group, pricingZone);
				if(matPrice != null){
					eligibleCartLines.add(cartLine);
					if(!groupMap.containsKey(group)){
						groupMap.put(group, cartLine.getQuantity());
					}else{
						//Group already in Map.
						double quantity = groupMap.get(group);
						quantity += cartLine.getQuantity();
						groupMap.put(group, quantity);
					}
					double qty = groupMap.get(group);
					if(qty >= matPrice.getScaleLowerBound()){
						//Reached qualified limit. Add Group to qualified Map.
						qualifiedGroupMap.put(group, qty);
						if(qualifiedGrpIdMap.containsKey(group.getGroupId())){
							//Already a group with same id with diff. version was qualified.
							//add the group that has max version.
							FDGroup qGroup = qualifiedGrpIdMap.get(group.getGroupId());
							if(group.getVersion() > qGroup.getVersion())
								qualifiedGrpIdMap.put(group.getGroupId(), group);
						} else {
							qualifiedGrpIdMap.put(group.getGroupId(), group);
						}
					}
					/*if(true) {
						String grpId = group.getGroupId();
						//Evaluate for duplicate groups(Groups with same ID and different version).
						if(duplicateGrps == null) 
							duplicateGrps = new HashMap<String, Set<FDGroup>>();
						Set<FDGroup> groups = duplicateGrps.get(grpId);
						if(groups == null){
							groups = new HashSet<FDGroup>();
							duplicateGrps.put(grpId, groups);
						}
						groups.add(group);
						if(duplicateGrpsQtyMap == null)
							duplicateGrpsQtyMap =  new HashMap<String, Double>();
						double duplicateGrpQty = duplicateGrpsQtyMap.get(grpId);
						duplicateGrpQty += cartLine.getQuantity();
						duplicateGrpsQtyMap.put(grpId, duplicateGrpQty);
					}*/
				}
			}
		} 
		checkNewLinesForUpgradedGroup(pricingZone, groupMap, qualifiedGroupMap,
				qualifiedGrpIdMap);
		for ( FDCartLineI qCartLine : eligibleCartLines ) {
			FDGroup qGroup = qCartLine.getFDGroup();
			if(qualifiedGroupMap.containsKey(qGroup)){
				//set the group quantity to the qualified cartline.
				qCartLine.setGroupQuantity(qualifiedGroupMap.get(qGroup));
				//Associate the FDgroup to the cartline.
				qCartLine.setFDGroup(qGroup);
			}
		}
	}

	public  void calculateScaleQuantity() {
		//String key=null;
		Map<FDSku,Double> scaleQuantityMap=new HashMap<FDSku, Double>();
		Double scale=null;
		for ( FDCartLineI cartLine : orderLines ) {
			//key=populateScaleKey(cartLine);
			scale=scaleQuantityMap.get(cartLine.getSku());
			if(scale==null){
				scale=new Double(0.0);
			}
			scale=scale+cartLine.getQuantity();
			scaleQuantityMap.put(cartLine.getSku(), scale);
		}
		
		for(FDCartLineI cartLine : orderLines){
			//key=populateScaleKey(cartLine);

			cartLine.setScaleQuantity(scaleQuantityMap.get(cartLine.getSku()));
		}
	}


	protected void checkNewLinesForUpgradedGroup(ZoneInfo pricingZone,
			Map<FDGroup, Double> groupMap,
			Map<FDGroup, Double> qualifiedGroupMap,
			Map<String, FDGroup> qualifiedGrpIdMap) throws FDResourceException{
		//Default implementation
		return;
	}

	//
	// order views
	//
	
	public WebOrderViewI getOrderView(ErpAffiliate affiliate) {
		return WebOrderViewFactory.getOrderView(orderLines, affiliate, false);
	}

	public List<WebOrderViewI> getOrderViews() {
		return WebOrderViewFactory.getOrderViews(orderLines, false);
	}
	
	public void recalculateTaxAndBottleDeposit(String zipcode) throws FDResourceException{
		double taxRate = 0.0;
		double depositRate = 0.0;
		EnumTaxationType taxationType = null;
		
		FDDeliveryManager fdMan = FDDeliveryManager.getInstance();
		
		
		MunicipalityInfoWrapper miw = fdMan.getMunicipalityInfos();
		MunicipalityInfo mi = null;
		
		if(deliveryAddress != null){	
			String county = fdMan.getCounty(deliveryAddress);
			mi = miw.getMunicipalityInfo(deliveryAddress.getState(), county, deliveryAddress.getCity());
		} else {
			StateCounty stateCounty = fdMan.getStateCountyByZipcode(zipcode);
			mi = miw.getMunicipalityInfo((stateCounty!=null)?stateCounty.getState():null, (stateCounty!=null)?stateCounty.getCounty():null, null);
		}
		
		if(mi != null ){
			if(null ==getPaymentMethod() || !EnumPaymentMethodType.EBT.equals(getPaymentMethod().getPaymentMethodType())){
				taxRate = mi.getTaxRate();
			}
			depositRate = mi.getBottleDeposit();
			taxationType = mi.getTaxationType();
		}
	
		 for( FDCartLineI cartline : orderLines ) {
             cartline.setDepositValue(depositRate);
             if(!FDStoreProperties.getAvalaraTaxEnabled()){
                   cartline.setTaxRate(taxRate);
                   cartline.setTaxationType(taxationType);
             }
      }
	}

	public List<ErpDiscountLineModel> getDiscounts() {
		return this.discounts;
	}
	
	

	public void setDiscounts(List<ErpDiscountLineModel> discounts) {
		this.discounts = discounts;
	}
	
	public void addDiscount(ErpDiscountLineModel discount) {
		this.discounts.add(discount);
	}

	public void removeDiscount(String promoCode) {
		if(promoCode == null) return;
		if (this.discounts != null && this.discounts.size() > 0) {
			for (Iterator<ErpDiscountLineModel> iter = this.discounts.iterator(); iter.hasNext();) {
				ErpDiscountLineModel discountLine = iter.next();
				if(discountLine == null) continue;
				Discount d1 =  discountLine.getDiscount();
				if(d1 == null) continue;
				if(d1.getPromotionCode().equals(promoCode))
					//remove the discount.
					iter.remove();
			}
		}
	}
	

	public Discount getDiscount(String promoCode) {
		if(promoCode == null) return null;
		if (this.discounts != null && this.discounts.size() > 0) {
			for (Iterator<ErpDiscountLineModel> iter = this.discounts.iterator(); iter.hasNext();) {
				ErpDiscountLineModel discountLine = iter.next();
				if(discountLine == null) continue;
				Discount d1 =  discountLine.getDiscount();
				if(d1 == null) continue;
				if(d1.getPromotionCode().equals(promoCode))
					return d1;
			}
		}
		return null;
	}
	
	public void addDiscount(Discount discount) {
		boolean isDuplicate = false;
		if(!discounts.isEmpty()){
			for (ErpDiscountLineModel erpDiscountLineModel : discounts) {
				if(erpDiscountLineModel.getDiscount().getPromotionCode().equalsIgnoreCase(discount.getPromotionCode())){
					isDuplicate= true;
					break;
				}
			}
		}
		if(!isDuplicate){
			this.discounts.add(new ErpDiscountLineModel(discount));
		}
	}

	public double getTotalDiscountValue() {
		double discountValue = 0.0;
		if (this.discounts != null && this.discounts.size() > 0) {
			for (Iterator<ErpDiscountLineModel> iter = this.discounts.iterator(); iter.hasNext();) {
				ErpDiscountLineModel discountLine = iter.next();
				discountValue += MathUtil.roundDecimal(discountLine.getDiscount().getAmount());
			}
		}
		return MathUtil.roundDecimal(discountValue);
	}
	
	public double getDiscountValue(String promoCode) {
		double discountValue = 0.0;
		if (this.discounts != null && this.discounts.size() > 0) {
			for (Iterator<ErpDiscountLineModel> iter = this.discounts.iterator(); iter.hasNext();) {
				ErpDiscountLineModel discountLine = iter.next();
				if(promoCode.equals(discountLine.getDiscount().getPromotionCode())) {
					discountValue = MathUtil.roundDecimal(discountLine.getDiscount().getAmount());
					break;
				}
			}
		}
		return MathUtil.roundDecimal(discountValue);
	}
	
	public void handleDeliveryPass() {
		if(EnumEStoreId.FDX.equals(getEStoreId()))
			return;
		
		int count = 0;
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if(line.lookupFDProduct().isDeliveryPass()){
				count++;
			}
			
		}
		setDeliveryPassCount(count);
		if (this.getChargeAmount(EnumChargeType.DELIVERY) == 0.0) {
			//If there is no applicable delivery charge then return;
			return;
		}//otherwise
		if(getDeliveryPassCount() > 0 ) {
			//Cart contains delivery pass. So Waive delivery fee.
			if(!this.isChargeWaived(EnumChargeType.DELIVERY)){
				//If not already waived. The charge is already waived because
				//of a delivery promotion in which case promotion takes the
				//precedence.
				setChargeWaived(EnumChargeType.DELIVERY, true, DlvPassConstants.PROMO_CODE);	
				this.setDlvPassApplied(true);
				Calendar cal = Calendar.getInstance();
				this.setDlvPassPremiumAllowedTC(cal.getTime().after(FDStoreProperties.getDlvPassNewTCDate()));
			}

		}else{
			//Either Cart contains no delivery pass or the pass was removed. So apply delivery fee
			//only if delivery promotion was not applied.
			if(!this.isDlvPromotionApplied()){
				//If delivery promotion was applied, do not revoke the waiving of dlv charge.
				setChargeWaived(EnumChargeType.DELIVERY, false, DlvPassConstants.PROMO_CODE);
			}
			this.setDlvPassApplied(false);
		}
	}
	
	public boolean containsDlvPassOnly(){
		if(getDeliveryPassCount() > 0 && getDeliveryPassCount() == this.getOrderLines().size()){
			//Cart contains only delivery pass.
			return true;
		}else{
			return false;
		}
	}

	public List<DlvPassAvailabilityInfo> getUnavailablePasses() {
		return unavailablePasses;
	}

	public void setUnavailablePasses(List<DlvPassAvailabilityInfo> unavailablePasses) {
		this.unavailablePasses = unavailablePasses;
	}
	
	/**
	 * This method returns true or false if the delivery pass was already applied
	 * when the order was created or previously modified. This method is exclusively
	 * return for modify orders and used in handleDeliveryPass method.
	 * FDCartModel class has a default implementation. The actual implementation is
	 * provided by FDModifyCartModel.
	 * @return
	 */
	public boolean isDlvPassAlreadyApplied(){
		return false;
	}
	
	public boolean containsUnlimitedPass(){
		if(EnumEStoreId.FDX.equals(getEStoreId()))
			return false;
		boolean contains = false;
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI line = i.next();
			if(line.lookupFDProduct().isDeliveryPass()){
				String skuCode = line.getSkuCode();
				if(DeliveryPassType.getEnum(skuCode).isUnlimited()){
					contains = true;
					break;
				}
			}
		}
		return contains;
	}

	/*
	 * Returns true if an item in the cart has the material attriubte of AOF (Advance Order Flag)
	 */
	
	public boolean hasAdvanceOrderItem() {
		boolean hasAOItem = false;

		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext() && !hasAOItem;) {
			FDCartLineI line = i.next();
			hasAOItem = line.hasAdvanceOrderFlag();
		}
		return hasAOItem;
	}
	
	/*
	 * Check if cart is Empty.
	 */
	public boolean isEmpty() {
		return this.getOrderLines().isEmpty();
	}
	
	/*
	 * Get the product keys of the line items.
	 */
	public Set<String> getProductKeysForLineItems() {
		Set<String> prodKeysInCart = new HashSet<String>();
		for ( FDCartLineI cartLine : getOrderLines() ) {
			String productId = cartLine.getProductRef().getContentKey().getId();
			prodKeysInCart.add(productId);
		}
		return prodKeysInCart;
	}
	/**
	 * This returns a redeem sample promo desc if any otherwise returns Empty String.
	 * @return java.lang.String.
	 */
	public String getRedeemedSampleDescription() {
		String desc = "NONE";
		//Show any redeemed sample line if any.
		if ( this.sampleLines != null && this.sampleLines.size() > 0) {
			for(Iterator<FDCartLineI> i = this.sampleLines.iterator(); i.hasNext();){
				FDCartLineI cartLine = i.next();
				Discount discount =  cartLine.getDiscount();
				String code = discount.getPromotionCode();
				PromotionI promotion = PromotionFactory.getInstance().getPromotion(code);
				if (promotion != null && promotion.isRedemption()) {
					desc = promotion.getDescription();
				}
			}
		}
		return desc;
	}

	public int getLineItemDiscountCount(String promoCode){
		//Set<String> uniqueDiscountedProducts =new HashSet<String>();
		int count = 0;
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI cartLine = i.next();
			if(cartLine.hasDiscount(promoCode)) {
				count++;
			}
		}
		//return uniqueDiscountedProducts.size();
		return count;
	}
	
	public void clearLineItemDiscounts(){
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI cartLine = i.next();
			if(cartLine.getDiscount() !=  null) cartLine.removeLineItemDiscount();
			
			try {
				cartLine.refreshConfiguration();
			} catch (FDException e) {
				// !!! improve error handling
				throw new FDRuntimeException(e);
			}
		}
	}

	public double getTotalLineItemsDiscountAmount() {
		double discountAmt=0;
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI cartLine = i.next();
			if(cartLine.getDiscount() !=  null){
				discountAmt+=cartLine.getDiscountAmount();
			}
		}
        return discountAmt;
	}
	
	public double getLineItemDiscountAmount(String promoCode) {
		double discountAmt=0;
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI cartLine = i.next();
			if(cartLine.getDiscount() !=  null && cartLine.getDiscount().getPromotionCode().equals(promoCode)){
				discountAmt+=cartLine.getDiscountAmount();
			}
		}
        return discountAmt;
	}
	
	public Set<String> getLineItemDiscountCodes() {
		Set<String> codes = new HashSet<String>();
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI cartLine = i.next();
			if(cartLine.getDiscount() !=  null){
				codes.add(cartLine.getDiscount().getPromotionCode());
			}
		}
        return codes;
	}
	
	public boolean isDiscountInCart(String promoCode) {
		for (Iterator<FDCartLineI> i = this.orderLines.iterator(); i.hasNext();) {
			FDCartLineI cartLine = i.next();
			if(cartLine.getDiscount() !=  null){
				String discountCode = cartLine.getDiscount().getPromotionCode();
				if(discountCode.equals(promoCode)) return true;
			}
		}
        return false;
	}
	
	public Set<String> getUniqueSavingsIds() {
		Set<String> saveIdsInCart = new HashSet<String>();
		for ( FDCartLineI cartLine : getOrderLines() ) {
			saveIdsInCart.add(cartLine.getSavingsId());;
		}
		return saveIdsInCart;
	}
	
	public boolean hasHeaderDiscount() {
		List<ErpDiscountLineModel> l = this.getDiscounts();
		if(l.isEmpty())
			return false;
		return true;
	}

	public List<ErpGiftCardModel> getSelectedGiftCards() {
		return selectedGiftCards;
	}

	public void setSelectedGiftCards(List<ErpGiftCardModel> selectedGiftCards) {
		this.selectedGiftCards = selectedGiftCards;
	}

	public double getTotalAppliedGCAmount(){
		double gcBal = 0.0;		
		if ( this.getSelectedGiftCards() != null && this.getSelectedGiftCards().size() > 0 ) {
			for ( ErpGiftCardModel model : getSelectedGiftCards() ) {
				gcBal += model.getBalance();
			}
		}
		return Math.min(gcBal, this.getTotal());
	}
	
	public double getCCPaymentAmount() {
		return this.getTotal() - this.getTotalAppliedGCAmount();
	}
	
	public double getBufferAmt() {
		return bufferAmt;
	}

	/**
	 * @param bufferAmt the bufferAmt to set
	 */
	public void setBufferAmt(double bufferAmt) {
		this.bufferAmt = bufferAmt;
	}
	
	/**
	 * Override to prevent saving it
	 * @return
	 */
	public boolean isPersistent() {
		return true;
	}

	public void setUserContextToOrderLines(UserContext uCtx) {
		List<FDCartLineI> invalids=new ArrayList<FDCartLineI>(orderLines.size());
		for (FDCartLineI cartLine : this.orderLines) {
			cartLine.setUserContext(uCtx);
			
			try {
				OrderLineUtil.cleanup(cartLine);
			} catch (FDInvalidConfigurationException e) {
				invalids.add(cartLine);
				LOGGER.error(e.getMessage());
			} catch(FDResourceException e1){
				LOGGER.error(e1.getMessage());
			}
			
		}
		
		if(invalids.size()>0)
			this.setInvalidOrderLines(invalids);
		for(FDCartLineI cartLine:invalids) {
			this.orderLines.remove(cartLine);
		}
		/*try {
			this.refreshAll(true);
		}catch ( FDResourceException e ) {
			LOGGER.error( "Cart refresh failed", e );
		} catch ( FDInvalidConfigurationException e ) {
			LOGGER.error( "Cart refresh failed, invalid configuration", e );
		}*/
	}

	public int getDlvPassExtendDays() {
		return dlvPassExtn != null ? dlvPassExtn.getExtendedDays() : 0;
	}

	public void setDlvPassExtn(ExtendDPDiscountModel dlvPassExtn) {
		this.dlvPassExtn = dlvPassExtn;
	}

	public int getCurrentDlvPassExtendDays() {
		return currentDlvPassExtendDays;
	}

	public void setCurrentDlvPassExtendDays(int currentDlvPassExtendDays) {
		this.currentDlvPassExtendDays = currentDlvPassExtendDays;
	}
	
	/**
	 * This returns a redeem extend DP promo desc if any otherwise returns Empty String.
	 * @return java.lang.String.
	 */
	public String getExtendDPDiscountDescription() {
		String desc = "NONE";
		//Show any redeemed extend DP promo if any.
		if ( this.dlvPassExtn != null) {
			String code = dlvPassExtn.getPromotionCode();
			PromotionI promotion = PromotionFactory.getInstance().getPromotion(code);
			if (promotion != null && promotion.isExtendDeliveryPass()) {
				desc = promotion.getDescription();
			}
		}
		return desc;
	}

	@Override
	public boolean hasClientCodes() {
		for (FDCartLineI cl : orderLines)
			if (!cl.getClientCodes().isEmpty())
				return true;
		return false;
	}
	
	public PromotionI getNonCombinableHeaderPromotion() {
		List<ErpDiscountLineModel> l = this.getDiscounts();
		if(l.isEmpty())
			return null;

		Iterator<ErpDiscountLineModel> i = l.iterator();
		while(i.hasNext()) {
			//Get the non combinable applied discount from the cart.
			ErpDiscountLineModel model = i.next();
			if(model==null) continue;
			Discount applied = model.getDiscount();
			if(applied == null) continue;
			PromotionI promo = PromotionFactory.getInstance().getPromotion(applied.getPromotionCode());
			if(promo != null && !promo.isRedemption() && !promo.isCombineOffer())
				return promo;
		}
		return null;
	}




    public void updateSurcharges(FDRuleContextI ctx) {
		this.clearCharge(EnumChargeType.DELIVERY);
		this.clearCharge(EnumChargeType.MISCELLANEOUS);
		this.clearCharge(EnumChargeType.DLVPREMIUM);
		AddressModel address = this.getDeliveryAddress();
		
		// final FDRulesContextImpl ctx = new FDRulesContextImpl(user);
		if (address != null) {
			
			// DLV
			FeeCalculator calc = new FeeCalculator("DLV");
			double dlvFee = 0.0;
			if(null ==getPaymentMethod() || !EnumPaymentMethodType.EBT.equals(getPaymentMethod().getPaymentMethodType())){
				
				//TIER DLV FEE. This has higher priority if there is tier price defined at the timeslot level.
				if(FDStoreProperties.isDlvFeeTierEnabled() 
						&& this.getDeliveryReservation()!=null 
						&& this.getDeliveryReservation().getTimeslot()!=null){
					TieredPrice tieredPrice =  TimeslotLogic.getTieredDlvFee(ctx.getUser(), (this.getDeliveryReservation().getDeliveryFeeTier()!=null)? EnumDeliveryFeeTier.getEnum(this.getDeliveryReservation().getDeliveryFeeTier()):null);
					if(tieredPrice!=null){
						dlvFee = tieredPrice.getPromoPrice();
					}else{ // fall back to deliver fee if 
						dlvFee = calc.calculateFee(ctx);
					}
				}else{ // fall back to deliver fee if 
					dlvFee = calc.calculateFee(ctx);
				}
			}
			this.setChargeAmount(EnumChargeType.DELIVERY, dlvFee);
			double premiumFee = 0.0;
			
			// DLV PREMIUM
			premiumFee = this.getPremiumFee(ctx);
			if(premiumFee>0)
				this.setChargeAmount(EnumChargeType.DLVPREMIUM, premiumFee);
			
			// MISC
			calc = new FeeCalculator("MISC");
			double miscFee = 0.0;
			if(null ==getPaymentMethod() || !EnumPaymentMethodType.EBT.equals(getPaymentMethod().getPaymentMethodType())){
				miscFee =calc.calculateFee(ctx);
			}
			this.setChargeAmount(EnumChargeType.MISCELLANEOUS, miscFee);

		}

		// DLV & MISC tax
		double taxRate = 0.0;
		EnumTaxationType taxationType = null;
		for (Iterator<FDCartLineI> i = this.getOrderLines().iterator(); i.hasNext();) {
			FDCartLineI cartLine = i.next();
			if (cartLine.hasTax()) {
				taxRate = cartLine.getTaxRate();
				taxationType = cartLine.getTaxationType();
				break;
			}
		}
		ErpChargeLineModel c = this.getCharge(EnumChargeType.DELIVERY);
		if (c != null) {
			c.setTaxRate(taxRate);
			c.setTaxationType(taxationType);
		}
		c = this.getCharge(EnumChargeType.DLVPREMIUM);
		if (c != null) {
			c.setTaxRate(taxRate);
			c.setTaxationType(taxationType);
		}
		c = this.getCharge(EnumChargeType.MISCELLANEOUS);
		if (c != null) {
			c.setTaxRate(taxRate);
			c.setTaxationType(taxationType);
		}

	}   

	public void setTip(double tip) {
		this.setChargeAmount(EnumChargeType.TIP, tip);
	}
	
	public double getTip() {
		return this.getChargeAmount(EnumChargeType.TIP);
	}
	
	public boolean isCustomTip() {
		return isCustomTip;
	}

	public void setCustomTip(boolean isCustomTip) {
		this.isCustomTip = isCustomTip;
	}
	
    public void setDlvPassPremiumAllowedTC(boolean dlvPassPremiumAllowedTC){
    	this.dlvPassPremiumAllowedTC = dlvPassPremiumAllowedTC;
    }
	public boolean isDlvPassPremiumAllowedTC() {
		return dlvPassPremiumAllowedTC;
	}	

	public void setEbtIneligibleOrderLines(List<FDCartLineI> ebtIneligibleOrderLines) {
		this.ebtIneligibleOrderLines = ebtIneligibleOrderLines;
	}
	
	public List<FDCartLineI> getEbtIneligibleOrderLines() {
		return ebtIneligibleOrderLines;
	}

	public String getExpCouponDeliveryDate() {
		return expCouponDeliveryDate;
	}

	public void setExpCouponDeliveryDate(String expCouponDeliveryDate) {
		this.expCouponDeliveryDate = expCouponDeliveryDate;
	}

	public Set<String> getRecentlyAppliedCoupons() {
		return recentlyAppliedCoupons;
	}
	
	public void setTransactionSource(EnumTransactionSource source) {
		this.source = source;
	}
	
	public EnumTransactionSource getTransactionSource() {
		return source;
	}
	
	
	public EnumEStoreId getEStoreId() {
		
		return eStore;
}
	
	public void setEStoreId(EnumEStoreId eStore) {
		this.eStore = eStore;
	}

	@Override
	public ErpDeliveryPlantInfoModel getDeliveryPlantInfo() {
		return this.deliveryPlantInfo;
	}
	
	public  void setDeliveryPlantInfo(ErpDeliveryPlantInfoModel deliveryPlantInfo) {
		 this.deliveryPlantInfo=deliveryPlantInfo;
	}
	
	public void setInvalidOrderLines(List<FDCartLineI> lines) {
		
		this.invalidLines.clear();
		this.invalidLines.addAll(lines);
	}
	
	public List<FDCartLineI> getInvalidOrderLines() {
		return invalidLines;
	}
	
	public boolean hasInvalidLines() {
		
		return invalidLines.size()>0?true:false;
	}
	
	public PhoneNumber getOrderMobileNumber() {
		return orderMobileNumber;
	}

	public void setOrderMobileNumber(PhoneNumber orderMobileNumber) {
		this.orderMobileNumber = orderMobileNumber;
	}

	
}
