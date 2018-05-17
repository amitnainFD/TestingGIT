package com.freshdirect.fdstore.customer;

import java.util.List;

import org.apache.log4j.Category;

import com.freshdirect.common.context.UserContext;
import com.freshdirect.common.pricing.EnumTaxationType;
import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.common.pricing.util.GroupScaleUtil;
import com.freshdirect.customer.EnumATCContext;
import com.freshdirect.customer.ErpClientCode;
import com.freshdirect.customer.ErpInvoiceLineI;
import com.freshdirect.customer.ErpOrderLineModel;
import com.freshdirect.customer.ErpReturnLineModel;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.EnumOrderLineRating;
import com.freshdirect.fdstore.EnumSustainabilityRating;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDConfigurableI;
import com.freshdirect.fdstore.FDGroup;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSku;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.framework.event.EnumEventSource;
import com.freshdirect.framework.util.StringUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.sap.PosexUtil;

/**
 *
 * @version    $Revision:16$
 * @author     $Author:Kashif Nadeem$
 * @stereotype fd-model
 */
public class FDCartLineModel extends AbstractCartLine {

	private static final long	serialVersionUID	= 6554964787371568944L;
	@SuppressWarnings("unused")
	private final static Category LOGGER = LoggerFactory.getInstance(FDCartLineModel.class);
	private EnumEventSource source;
	private String cartonNumber;
	private String atcItemId;
	
	public FDCartLineModel(ErpOrderLineModel orderLine) {
		this(orderLine, false);
	}

	/**
	 * Convenience constructor
	 * 
	 * @param orderLine
	 * @param lazy
	 */
	public FDCartLineModel(ErpOrderLineModel orderLine, final boolean lazy) {
		this(orderLine, null, null, null, lazy);
	}

	
	
	public FDCartLineModel(
		ErpOrderLineModel orderLine,
		ErpInvoiceLineI firstInvoiceLine,
		ErpInvoiceLineI lastInvoiceLine,
		ErpReturnLineModel returnLine) {
		super(orderLine, firstInvoiceLine, lastInvoiceLine, returnLine, false);
	}

	public FDCartLineModel(
		ErpOrderLineModel orderLine,
		ErpInvoiceLineI firstInvoiceLine,
		ErpInvoiceLineI lastInvoiceLine,
		ErpReturnLineModel returnLine,
		boolean lazy) {
		super(orderLine, firstInvoiceLine, lastInvoiceLine, returnLine, lazy);
	}

	public FDCartLineModel(FDSku sku, ProductModel productRef, FDConfigurableI configuration, String variantId,UserContext userCtx) {
		super(sku, productRef, configuration, variantId, userCtx);
		this.orderLine.setCartlineId(ID_GENERATOR.getNextId());
	}
	
	public FDCartLineModel(FDSku sku, ProductModel productRef, FDConfigurableI configuration, String cartlineId, String recipeSourceId,
			boolean requestNotification, String variantId, UserContext userCtx, List<ErpClientCode> clientCodes) {
		super(sku, productRef, configuration, variantId, userCtx);
		this.orderLine.setCartlineId(cartlineId);
		this.orderLine.setRecipeSourceId(recipeSourceId);
		this.orderLine.setRequestNotification(requestNotification);
		this.orderLine.setVariantId(variantId);
		this.orderLine.getClientCodes().addAll(clientCodes);
	}
	
	public FDCartLineModel( FDProductSelectionI ps ) {
		this( ps.getSku(), ps.getProductRef().lookupProductModel(), ps.getConfiguration(), null, ps.getUserContext());
	}

	public ErpOrderLineModel buildErpOrderLines(int baseLineNumber) throws FDResourceException, FDInvalidConfigurationException {
		this.refreshConfiguration();
		ErpOrderLineModel ol = (ErpOrderLineModel) this.orderLine.deepCopy();
		
      
		try {
			if(ol.getSku()!=null){

				FDProductInfo productInfo = FDCachedFactory.getProductInfo(ol.getSku().getSkuCode());
				
			
				//String plantID=(ol.getUserContext().getFulfillmentContext()!=null)?ol.getUserContext().getFulfillmentContext().getPlantId():ol.getPlantID();
				String plantID=getPlantId();
				EnumOrderLineRating rating=productInfo.getRating(plantID);
				EnumSustainabilityRating sustainabilityRating=productInfo.getSustainabilityRating(plantID);

				ol.setProduceRating(rating);
				ol.setSustainabilityRating(sustainabilityRating);
				ol.setBasePrice(productInfo.getZonePriceInfo(ol.getUserContext().getPricingContext().getZoneInfo()).getSellingPrice());
				ol.setBasePriceUnit(productInfo.getDefaultPriceUnit());	
				//Check if qualified group  scale qty > 0. If yes then set FDGroup appropriately.
				if(ol.getGroupQuantity() > 0 && ol.getFDGroup() != null){
					
					/*
					ZoneInfo pz=GroupScaleUtil.getGroupPricingZoneId(ol.getFDGroup(), getUserContext().getPricingContext().getZoneInfo());
						
						
					ol.setPricingZoneId(getUserContext().getPricingContext().getZoneInfo().getPricingZoneId());
					ol.setSalesOrg(getUserContext().getPricingContext().getZoneInfo().getSalesOrg());
					ol.setDistChannel(getUserContext().getPricingContext().getZoneInfo().getDistributionChanel());
					*/
				} else {
					//not qualified for group scale. clear FD group if present.
					ol.setFDGroup(null);
					/*ZoneInfo pz=getUserContext().getPricingContext().getZoneInfo();
					ol.setPricingZoneId(pz.getPricingZoneId());
					ol.setSalesOrg(pz.getSalesOrg());
					ol.setDistChannel(pz.getDistributionChanel());*/
				}
				ol.setUpc(productInfo.getUpc());
				//ol.seteStoreType(ol.getUserContext().getStoreContext().getEStoreId().name());
			}			
		} catch (FDResourceException e) {
			e.printStackTrace();
		} catch (FDSkuNotFoundException e) {
			e.printStackTrace();
		}
		
		ol.setOrderLineNumber(PosexUtil.getPosex(baseLineNumber));
      
		return ol;
	}

	public int getErpOrderLineSize() {
		return 1;
	}

	public FDCartLineI createCopy() {
		FDCartLineModel newLine = new FDCartLineModel(this.getSku(), this
				.getProductRef().lookupProductModel(), this.getConfiguration(), this.getVariantId(), this.getUserContext());
		newLine.setRecipeSourceId(this.getRecipeSourceId());
		newLine.setRequestNotification(this.isRequestNotification());
		newLine.setSource(this.source);
		newLine.setExternalAgency(getExternalAgency());
		newLine.setExternalSource(getExternalSource());
		newLine.setExternalGroup(getExternalGroup());
		return newLine;
	}
	
	//This is not a deep copy, just passing values
	public boolean copyInto(FDCartLineI into){
		if(into == null)
			return false; 
		
		try{
			
			into.setAddedFrom(getAddedFrom());
			into.setAddedFromSearch(isAddedFromSearch());
			into.setAtcItemId(getAtcItemId());
			into.setCartonNumber(getCartonNumber());
			into.setConfiguration(getConfiguration());
			into.setConfigurationDesc(getConfigurationDesc());
			into.setCoremetricsPageContentHierarchy(getCoremetricsPageContentHierarchy());
			into.setCoremetricsPageId(getCoremetricsPageId());
			into.setCoremetricsVirtualCategory(getCoremetricsVirtualCategory());
//			into.setCouponApplied()
			into.setCouponDiscount(getCouponDiscount());
			into.setCouponStatus(getCouponStatus());
			into.setCustomerListLineId(getCustomerListLineId());
			into.setDeliveryStartDate(getDeliveryStartDate());
			into.setDepartmentDesc(getDepartmentDesc());
			into.setDepositValue(getDepositValue());
			into.setDiscount(getDiscount());
			into.setDiscountFlag(isDiscountFlag());
			into.setEStoreId(getEStoreId());
			into.setExternalAgency(getExternalAgency());
			into.setExternalGroup(getExternalGroup());
			into.setExternalSource(getExternalSource());
			into.setFDGroup(getFDGroup());
			into.setFixedPrice(getFixedPrice());
			into.setGroupQuantity(getGroupQuantity());
			into.setOptions(getOptions());
			into.setOrderId(getOrderId());
			into.setOrderLineId(getOrderLineId());
			into.setOriginatingProductId(getOriginatingProductId());
			into.setPlantId(getPlantId());
			into.setQuantity(getQuantity());
			into.setRecipeSourceId(getRecipeSourceId());
			into.setRequestNotification(isRequestNotification());
			into.setSaleStatus(getSaleStatus());
			into.setSalesUnit(getSalesUnit());
			into.setSavingsId(getSavingsId());
			into.setSku(getSku());
			//into.setSource(getSource());
			into.setStatistics(getStatistics());
			into.setTaxationType(getTaxationType());
			into.setTaxRate(getTaxRate());
			into.setUserContext(getUserContext());
			into.setYmalCategoryId(getYmalCategoryId());
			into.setYmalSetId(getYmalSetId());
			
			
		} catch (ClassCastException e){
			//Not a FDCarLineModel instance i guess
			return false;			
		}
		return true;
	}
	
	@Override
	public void setOrderLineId(String orderLineId){
		this.orderLine.setOrderLineId(orderLineId);
	}

	/**
	 *  Set the source of the event.
	 *  
	 *  @param source the part of the site this event was generated from.
	 */
	public void setSource(EnumEventSource source) {
		this.source = source;
	}
	
	/**
	 *  Get the source of the event.
	 *  
	 *  @return the part of the site this event was generated from.
	 */
	public EnumEventSource getSource() {
		return source;
	}

	public void setErpOrderLineSource(EnumEventSource source){
	this.orderLine.setSource(source);
	}
	
	public EnumEventSource getErpOrderLineSource(){
		return this.orderLine.getSource();
	}
	
	public void setSavingsId(String savingsId){
		this.orderLine.setSavingsId(savingsId);
	}
	
	public String getSavingsId(){
		return this.orderLine.getSavingsId();
	}
	
	public void removeLineItemDiscount(){
		this.setDiscountAmount(0.0);
		this.setDiscount(null);
	}

	public boolean hasDiscount(String promoCode) {
		if(this.getDiscount() != null && this.getDiscount().getPromotionCode().equals(promoCode)) {
			return true;	
		}
		return false;
	}

	public String getCartonNumber() {
		// TODO Auto-generated method stub
		return cartonNumber;
	}

	public void setCartonNumber(String no) {
		// TODO Auto-generated method stub
		this.cartonNumber=no;
	}

	public FDGroup getOriginalGroup() {
		return this.orderLine.getFDGroup();
	}

	public boolean isAddedFromSearch() {
		return orderLine.isAddedFromSearch();
	}

	public void setAddedFromSearch(boolean addedFromSearch) {
		orderLine.setAddedFromSearch(addedFromSearch);
	}

	public EnumTaxationType getTaxationType() {
		return orderLine.getTaxationType();
	}

	public void setTaxationType(EnumTaxationType taxationType) {
		orderLine.setTaxationType(taxationType);
	}
	
	public void setAddedFrom(EnumATCContext atcContext) {
		orderLine.setAddedFrom(atcContext);
	}
	
	public EnumATCContext getAddedFrom(){
		return orderLine.getAddedFrom();
	}

	@Override
	public String getAtcItemId() {
		return atcItemId;
	}

	@Override
	public void setAtcItemId(String atcItemId) {
		this.atcItemId = atcItemId;
	}

	@Override
	public void setTaxCode(String taxCode) {
		orderLine.setTaxCode(taxCode);		
	}

	@Override
	public String getTaxCode() {
		return orderLine.getTaxCode();
	}

	

		

}
