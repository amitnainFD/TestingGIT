package com.freshdirect.fdstore.customer;

import java.util.Random;
import java.util.Set;

import com.freshdirect.affiliate.ErpAffiliate;
import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.Discount;
import com.freshdirect.common.pricing.EnumDiscountType;
import com.freshdirect.content.attributes.EnumAttributeName;
import com.freshdirect.customer.ErpCouponDiscountLineModel;
import com.freshdirect.customer.ErpInvoiceLineI;
import com.freshdirect.customer.ErpOrderLineModel;
import com.freshdirect.customer.ErpReturnLineI;
import com.freshdirect.customer.ErpReturnLineModel;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionReason;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDConfigurableI;
import com.freshdirect.fdstore.FDProduct;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDSalesUnit;
import com.freshdirect.fdstore.FDSku;
import com.freshdirect.fdstore.content.AvailabilityFactory;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.ecoupon.EnumCouponStatus;

public abstract class AbstractCartLine extends FDProductSelection implements FDCartLineI {
	
	private static final long	serialVersionUID	= -2991659761444741368L;

	protected final static IDGenerator ID_GENERATOR = new HiLoGenerator("CUST", "CARTLINE_SEQ");
	
	private final static Random RND = new Random();
	/** Random ID, not persisted */
	private final int randomId = RND.nextInt();

	private final ErpInvoiceLineI firstInvoiceLine;
	private final ErpInvoiceLineI lastInvoiceLine;
	private final ErpReturnLineModel returnLine;

	private final String variantId;
	private EnumCouponStatus couponStatus;
	@Deprecated private boolean couponApplied;

	protected AbstractCartLine(FDSku sku, ProductModel productRef, FDConfigurableI configuration, String variantId, UserContext userCtx) {
		super(sku, productRef, configuration, variantId, userCtx);

		this.firstInvoiceLine = null;
		this.lastInvoiceLine = null;
		this.returnLine = null;
		
		this.variantId = variantId;
	}

	public AbstractCartLine(
			ErpOrderLineModel orderLine,
			ErpInvoiceLineI firstInvoiceLine,
			ErpInvoiceLineI lastInvoiceLine,
			ErpReturnLineModel returnLine) {
		this(orderLine, firstInvoiceLine, lastInvoiceLine, returnLine, false);
	}

	public AbstractCartLine(
		ErpOrderLineModel orderLine,
		ErpInvoiceLineI firstInvoiceLine,
		ErpInvoiceLineI lastInvoiceLine,
		ErpReturnLineModel returnLine,
		boolean lazy) {
		super(orderLine, lazy);

		this.firstInvoiceLine = firstInvoiceLine;
		this.lastInvoiceLine = lastInvoiceLine;
		this.returnLine = returnLine;

		this.variantId = orderLine.getVariantId();
	}

	public int getRandomId() {
		return this.randomId;
	}
	
	public String getCartlineId(){
		return this.orderLine.getCartlineId();
	}

	//
	// PRICING
	//

	public Discount getDiscount() {
		return this.orderLine.getDiscount();
	}

	public void setDiscount(Discount discount) {
		this.orderLine.setDiscount(discount);
		this.fireConfigurationChange();
	}

	
	public ErpCouponDiscountLineModel getCouponDiscount() {
		return this.orderLine.getCouponDiscount();
	}

	public void setCouponDiscount(ErpCouponDiscountLineModel discount) {
		this.orderLine.setCouponDiscount(discount);
		this.fireConfigurationChange();
	}
	
	public void clearCouponDiscount(){
		this.setCouponDiscount(null);
		this.setCouponStatus(null);
		this.setCouponApplied(false);
	}
	//
	// INVOICE, RETURN
	//

	public boolean hasInvoiceLine() {
		return this.getInvoiceLine() != null;
	}

	public ErpInvoiceLineI getInvoiceLine() {
		return this.lastInvoiceLine;
	}

	public ErpInvoiceLineI getFirstInvoiceLine() {
		return this.firstInvoiceLine;
	}

	public ErpInvoiceLineI getLastInvoiceLine() {
		return this.lastInvoiceLine;
	}

	public ErpReturnLineI getReturnLine() {
		return this.returnLine;
	}

	public boolean hasReturnLine() {
		return this.getReturnLine() != null;
	}

	public boolean hasRestockingFee() {
		return this.returnLine == null ? false : this.returnLine.isRestockingOnly();
	}

	//
	// CONVENIENCE
	//

	public ErpAffiliate getAffiliate() {
		return this.orderLine.getAffiliate();
	}

	public boolean isSample() {
		return this.getDiscount() != null && EnumDiscountType.SAMPLE.equals(this.getDiscount().getDiscountType());
	}

	public boolean hasTax() {
		double value = this.hasInvoiceLine() ? this.getInvoiceLine().getTaxValue() : this.getTaxValue();
		return value > 0;
	}

	public boolean hasDepositValue() {
		double value = this.hasInvoiceLine() ? this.getInvoiceLine().getDepositValue() : this.getDepositValue();
		return value > 0;
	}

	public String getMaterialNumber() {
		return this.orderLine.getMaterialNumber();
	}

	public Set<EnumDlvRestrictionReason> getApplicableRestrictions() {
		FDProduct fdp = this.lookupFDProduct();
		FDProductInfo fdpi = this.lookupFDProductInfo();
		return AvailabilityFactory.getApplicableRestrictions(fdp,fdpi);
	}

	public String getOrderLineId() {
		if(this.orderLine.getPK() == null)
			return this.orderLine.getOrderLineId() == null? "": this.orderLine.getOrderLineId();
		else
			return this.orderLine.getPK().getId();
	}

	public String getOrderLineNumber() {
		return this.orderLine.getOrderLineNumber();
	}
		
	//
	// FORMATTING, DISPLAY
	// 

	public String getDeliveredQuantity() {
		if (!this.hasInvoiceLine()) {
			return "";
		}
		if (this.isSoldBySalesUnits() && this.isPricedByLb()) {
			return QUANTITY_FORMATTER.format(this.getFirstInvoiceLine().getWeight());
		} else {
			return QUANTITY_FORMATTER.format(this.getFirstInvoiceLine().getQuantity());
		}
	}

	public String getReturnedQuantity() {
		if (!this.hasReturnLine()) {
			return "";
		}
		if (this.isSoldBySalesUnits() && this.isPricedByLb()) {
			return QUANTITY_FORMATTER.format(this.getFirstInvoiceLine().getWeight());
		} else {
			return QUANTITY_FORMATTER.format(this.getReturnLine().getQuantity());
		}
	}

	public String getDisplayQuantity() {
		StringBuffer qty = new StringBuffer();

		if (this.isSoldBySalesUnits()) {

			FDSalesUnit unit = this.lookupFDSalesUnit();

			qty.append(unit.getDescriptionQuantity());

			if (this.isPricedByLb()) {

				if (this.hasInvoiceLine()) {
					qty.append("/").append(QUANTITY_FORMATTER.format(this.getFirstInvoiceLine().getWeight()));
				}

				if (this.hasReturnLine()) {
					qty.append("/").append(QUANTITY_FORMATTER.format(this.getFirstInvoiceLine().getWeight()));
				}

			}
			qty.append(" ");
			qty.append(unit.getDescriptionUnit());

		} else {
			qty.append(QUANTITY_FORMATTER.format(this.getQuantity()));

			if (this.hasInvoiceLine()) {
				qty.append("/").append(QUANTITY_FORMATTER.format(this.getFirstInvoiceLine().getQuantity()));
			}
			if (this.hasReturnLine()) {
				qty.append("/").append(QUANTITY_FORMATTER.format(this.getReturnLine().getQuantity()));
			}

		}
		return qty.toString();
	}

	public String getReturnDisplayQuantity() {

		StringBuffer qty = new StringBuffer();

		qty.append(QUANTITY_FORMATTER.format(this.getQuantity()));

		if (this.isSoldBySalesUnits()) {
			FDSalesUnit unit = this.lookupFDSalesUnit();

			qty.append('(').append(unit.getDescriptionQuantity()).append(')');

			if (this.isPricedByLb()) {
				if (this.hasInvoiceLine()) {
					qty.append('/').append(QUANTITY_FORMATTER.format(this.getInvoiceLine().getQuantity()));
					qty.append('(').append(QUANTITY_FORMATTER.format(this.getInvoiceLine().getWeight())).append(')');
				}
				if (this.hasReturnLine()) {
					qty.append('/').append(QUANTITY_FORMATTER.format(this.getInvoiceLine().getQuantity()));
					qty.append('(').append(QUANTITY_FORMATTER.format(this.getReturnLine().getQuantity())).append(')');
				}
			}
			qty.append(' ').append(unit.getDescriptionUnit());

		} else {
			if (this.hasInvoiceLine()) {
				qty.append('/').append(QUANTITY_FORMATTER.format(this.getInvoiceLine().getQuantity()));
			}
			if (this.hasReturnLine()) {
				qty.append('/').append(QUANTITY_FORMATTER.format(this.getReturnLine().getQuantity()));
			}
		}
		return qty.toString();
	}

	public boolean hasAdvanceOrderFlag() {
		FDProduct fdp = this.lookupFDProduct();
		return fdp.getAttributeBoolean(EnumAttributeName.ADVANCE_ORDER_FLAG.getName(),false);
	}
	

	public String getVariantId() {
		return this.variantId;
	}

	public void setOrderLineId(String orderLineId){
		this.orderLine.setOrderLineId(orderLineId);
	}
	
	public boolean isDiscountApplied() {
		return ((this.getDiscount() != null && (EnumDiscountType.DOLLAR_OFF.equals(this.getDiscount().getDiscountType()) 
				|| EnumDiscountType.PERCENT_OFF.equals(this.getDiscount().getDiscountType())))|| 
				(this.getCouponDiscount()!=null && EnumDiscountType.DOLLAR_OFF.equals(this.getCouponDiscount().getDiscountType())));
	}
	
	public String getDiscountedUnitPrice(){
		String discountedUnitPrice="";
		double discountAmt =0.0;
		if(!isDiscountApplied()) {
			return "";
		}
		if(null !=getDiscount()){
			if(EnumDiscountType.DOLLAR_OFF.equals(this.getDiscount().getDiscountType())) {
				discountAmt=this.getDiscount().getAmount();
			}else if(EnumDiscountType.PERCENT_OFF.equals(this.getDiscount().getDiscountType())){
				discountAmt = this.price.getBasePrice() * this.getDiscount().getAmount();
			}else {
				throw new IllegalArgumentException("Invalid Discount Type");			
			}
		}
		if(null!=getCouponDiscount()){
			discountAmt = discountAmt+getCouponDiscount().getDiscountAmt();
		}
		discountedUnitPrice = CURRENCY_FORMATTER.format(this.price.getBasePrice() - discountAmt)  + "/" + this.price.getBasePriceUnit().toLowerCase();
		return discountedUnitPrice;
	}
	public String getLineItemDiscount() {
		return CURRENCY_FORMATTER.format(this.price.getPromotionValue());
	}

	public String getLineItemCouponDiscount() {
		return CURRENCY_FORMATTER.format(this.price.getCouponDiscountValue());
	}
	
	/**
	 * @return the couponStatus
	 */
	public EnumCouponStatus getCouponStatus() {
		return couponStatus;
	}

	/**
	 * @param couponStatus the couponStatus to set
	 */
	public void setCouponStatus(EnumCouponStatus couponStatus) {
		this.couponStatus = couponStatus;
	}
	
	@Override @Deprecated
	public boolean hasCouponApplied() {
		return couponApplied;
	}

	@Override @Deprecated
	public void setCouponApplied(boolean applied) {
		this.couponApplied =applied;
		
	}

	public String getCoremetricsPageId() {
		return this.orderLine.getCoremetricsPageId();
	}

	public void setCoremetricsPageId(String coremetricsPageId) {
		this.orderLine.setCoremetricsPageId(coremetricsPageId);
	}

	public String getCoremetricsPageContentHierarchy() {
		return this.orderLine.getCoremetricsPageContentHierarchy();
	}

	public void setCoremetricsPageContentHierarchy(
			String coremetricsPageContentHierarchy) {
		this.orderLine.setCoremetricsPageContentHierarchy(coremetricsPageContentHierarchy);
	}

	public String getCoremetricsVirtualCategory() {
		return this.orderLine.getCoremetricsVirtualCategory();
	}

	public void setCoremetricsVirtualCategory(String coremetricsVirtualCategory) {
		this.orderLine.setCoremetricsVirtualCategory(coremetricsVirtualCategory);
	}

	@Override
	public void setEStoreId(EnumEStoreId eStore) {
		this.orderLine.setEStoreId(eStore);
		
	}

	@Override
	public EnumEStoreId getEStoreId() {
		return this.orderLine.getEStoreId();
	}

	@Override
	public void setPlantId(String plantId) {
		
		 this.orderLine.setPlantID(plantId);
	}

	@Override
	public String getPlantId() {
		
		return this.orderLine.getPlantID();
	}
}
