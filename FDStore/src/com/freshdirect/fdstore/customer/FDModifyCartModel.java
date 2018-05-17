/*
 * $Workfile:FDModifyCartModel.java$
 *
 * $Date:5/22/03 7:19:08 PM$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.freshdirect.affiliate.ErpAffiliate;
import com.freshdirect.common.pricing.Discount;
import com.freshdirect.common.pricing.MaterialPrice;
import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.common.pricing.util.GroupScaleUtil;
import com.freshdirect.customer.ErpCouponDiscountLineModel;
import com.freshdirect.fdlogistics.model.FDDeliveryZoneInfo;
import com.freshdirect.fdlogistics.model.FDInvalidAddressException;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDGroup;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSku;
import com.freshdirect.fdstore.customer.adapter.FDOrderAdapter;
import com.freshdirect.logistics.delivery.model.EnumZipCheckResponses;

/**
 *
 * @version	$Revision:10$
 * @author	 $Author:Viktor Szathmary$
 * @stereotype fd-model
 */
public class FDModifyCartModel extends FDCartModel {

	private static final long	serialVersionUID	= -3926647074819221032L;
	
	private final FDOrderAdapter originalOrder;
	private final Set<String> originalOrderCoupons = new HashSet<String>();

	/**
	 * Default constructor.
	 */
	public FDModifyCartModel(FDOrderAdapter originalOrder) {
		this.setCharges(originalOrder.getCharges());
		this.originalOrder = originalOrder;
		
		// populate orderlines from orig.order
		for (FDCartLineI origLine : originalOrder.getOrderLines()) {
			addOriginalOrderLine(origLine);
		}

		// initialize from original order
		this.setDeliveryAddress(originalOrder.getCorrectedDeliveryAddress());
		this.setDeliveryPlantInfo(originalOrder.getDeliveryPlantInfo());
		this.setPaymentMethod(originalOrder.getPaymentMethod());

		this.setDeliveryReservation(originalOrder.getDeliveryReservation());
		this.setModificationCutoffTime(originalOrder.getDeliveryInfo().getDeliveryCutoffTime());

		// !!! partially reconstruct the original zoneInfo (we don't need the full state, as it will be set later)
		FDDeliveryZoneInfo zoneInfo = new FDDeliveryZoneInfo(originalOrder.getDeliveryZone(), null, null, 
				EnumZipCheckResponses.DELIVER, originalOrder.getDeliveryReservation().getRegionSvcType());
		
		this.setZoneInfo(zoneInfo);

		this.setCustomerServiceMessage(originalOrder.getCustomerServiceMessage());
		this.setMarketingMessage(originalOrder.getMarketingMessage());
		this.setEStoreId(originalOrder.getEStoreId());

	}

/*	public FDDeliveryZoneInfo getZoneInfo(){
		try{
			return FDDeliveryManager.getInstance().getZoneInfo(originalOrder.getCorrectedDeliveryAddress(), originalOrder.getRequestedDate(),
				null, originalOrder.getDeliveryReservation().getRegionSvcType());
		}catch(FDInvalidAddressException e){
			System.out.println(e.getMessage());
		}catch(FDResourceException e){
			System.out.println(e.getMessage());
		}
		return new FDDeliveryZoneInfo(originalOrder.getDeliveryZone(), null, null, 
				EnumZipCheckResponses.DELIVER, originalOrder.getDeliveryReservation().getRegionSvcType());
		
	}*/
	public void addOriginalOrderLine(FDCartLineI origLine){
		
		FDCartLineI cartLine = new FDModifyCartLineModel(origLine);
		Discount d = origLine.getDiscount();
		if( d != null && !(d.getDiscountType().isSample())) {
			cartLine.setDiscount(d);
			cartLine.setDiscountFlag(true);
		}
		if(origLine.getSavingsId() != null)
			cartLine.setSavingsId(origLine.getSavingsId());
		if(origLine.getFDGroup() != null)
			cartLine.setFDGroup(origLine.getFDGroup());			
		if(null !=origLine.getCouponDiscount()){
			ErpCouponDiscountLineModel origCouponDiscount = origLine.getCouponDiscount();
			ErpCouponDiscountLineModel couponDiscount =new ErpCouponDiscountLineModel(origCouponDiscount.getCouponId(),origCouponDiscount.getDiscountAmt(),origCouponDiscount.getVersion(),origCouponDiscount.getRequiredQuantity(),origCouponDiscount.getCouponDesc(),origCouponDiscount.getDiscountType());
			cartLine.setCouponDiscount(couponDiscount);
			originalOrderCoupons.add(origCouponDiscount.getCouponId());
		}
		
		cartLine.setExternalAgency(origLine.getExternalAgency());
		cartLine.setExternalSource(origLine.getExternalSource());
		cartLine.setExternalGroup(origLine.getExternalGroup());
		cartLine.setErpOrderLineSource(origLine.getErpOrderLineSource());
		
		this.addOrderLine(cartLine);
	}
	
	public FDOrderAdapter getOriginalOrder() {
		return this.originalOrder;
	}

	public String getOriginalReservationId() {
		return this.originalOrder.getDeliveryReservationId();
	}

	@Override
	public boolean isDlvPassAlreadyApplied() {
		if(this.originalOrder.getDeliveryPassId() !=  null){
			//Delivery pass was already applied in this order either during create
			//or during last modification. So preserve the applied pass.
			return true;
		}
		return false;
	}
	
	@Override
	protected void checkNewLinesForUpgradedGroup(ZoneInfo pricingZone,
			Map<FDGroup, Double> groupMap,
			Map<FDGroup, Double> qualifiedGroupMap,
			Map<String, FDGroup> qualifiedGrpIdMap) throws FDResourceException {
		//Map to maintain the unqualified groups for further evaluation.
			Map<String, FDGroup> nonQualifiedGrpIdMap = new HashMap<String, FDGroup>(); 
		//Only do this for Modify Order.
		Iterator<FDGroup> it = groupMap.keySet().iterator();
		while(it.hasNext()){
			FDGroup newGroup = it.next();
			String grpId = newGroup.getGroupId();
			if(!qualifiedGroupMap.containsKey(newGroup)){
				if(qualifiedGrpIdMap.containsKey(grpId)) {
					//Group Id is a part of a fully qualified Group
					//This is a different version of same group. Check if the old version and new version
					//has same price and same scale qty. 
					FDGroup qGroup = qualifiedGrpIdMap.get(grpId);
					MaterialPrice qMatPrice = GroupScaleUtil.getGroupScalePrice(qGroup, pricingZone);
					MaterialPrice newMatPrice = GroupScaleUtil.getGroupScalePrice(newGroup, pricingZone);
					//Check if the old version and new version has same price and same scale qty. 
					//If yes add the old qty to new qty and set it both old group and new group.
					if(qMatPrice!= null && newMatPrice != null && qMatPrice.getPrice() == newMatPrice.getPrice() &&
							qMatPrice.getScaleLowerBound() == newMatPrice.getScaleLowerBound()){
						double quantity = groupMap.get(qGroup);
						double newQty = groupMap.get(newGroup);
						quantity += newQty;
						qualifiedGroupMap.put(qGroup, quantity);
						qualifiedGroupMap.put(newGroup, quantity);
						if(newGroup.getVersion() > qGroup.getVersion()){
							qualifiedGrpIdMap.put(grpId, newGroup);
						}
					 }
				}else{
					//Group Id can be a part of a partially qualified Group
					if(nonQualifiedGrpIdMap.containsKey(grpId)){
						//This is a different version of same group. Check if the old version and new version
						//has same price and same scale qty. 
						FDGroup nqGroup = nonQualifiedGrpIdMap.get(grpId);
						MaterialPrice qMatPrice = GroupScaleUtil.getGroupScalePrice(nqGroup, pricingZone);
						MaterialPrice newMatPrice = GroupScaleUtil.getGroupScalePrice(newGroup, pricingZone);
						//Check if the old version and new version has same price and same scale qty. 
						//If yes add the old qty to new qty and set it both old group and new group.
						if(qMatPrice!= null && newMatPrice != null && qMatPrice.getPrice() == newMatPrice.getPrice() &&
								qMatPrice.getScaleLowerBound() == newMatPrice.getScaleLowerBound()){
							double quantity = groupMap.get(nqGroup);
							double newQty = groupMap.get(newGroup);
							quantity += newQty;
							FDGroup maxGroup = nqGroup;
							if(newGroup.getVersion() > nqGroup.getVersion()){
								maxGroup = newGroup;
							} 
							
							if(quantity >= qMatPrice.getScaleLowerBound()){
								//Reached qualified limit. Add both Groups to qualified Map.
								qualifiedGroupMap.put(nqGroup, quantity);
								qualifiedGroupMap.put(newGroup, quantity);
								//Max version group to qualifiedGrpIdMap.
								qualifiedGrpIdMap.put(grpId, maxGroup);
							}
							nonQualifiedGrpIdMap.put(grpId, maxGroup);
						 } 
					} else {
						 //add the group Id to nonQualifiedGrpIdMap
						 nonQualifiedGrpIdMap.put(grpId, newGroup);
					}
				}
			}
		}
	}

	public WebOrderViewI getOrderView(ErpAffiliate affiliate) {
		// return WebOrderViewFactory.getOrderView(orderLines, affiliate, true);
		// APPDEV-2031 we implemented separate new items feature but due to
		// recipe grouping discrepancy we switched off this feature
		return WebOrderViewFactory.getOrderView(orderLines, affiliate, false);
	}

	public List<WebOrderViewI> getOrderViews() {
		// return WebOrderViewFactory.getOrderViews(orderLines, true);
		// APPDEV-2031 we implemented separate new items feature but due to
		// recipe grouping discrepancy we switched off this feature
		return WebOrderViewFactory.getOrderViews(orderLines, false);
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Set<String> getOriginalOrderCoupons() {
		return originalOrderCoupons;
	}
	
	/*public  void calculateScaleQuantity() {
		//String key=null;
		Map<FDSku,Double> scaleQuantityMap=new HashMap<FDSku, Double>();
		Double scale=null;
		for ( FDCartLineI cartLine : orderLines ) {
			//key=populateScaleKey(cartLine);
			if(!(cartLine instanceof FDModifyCartLineI)){
				scale=scaleQuantityMap.get(cartLine.getSku());
				if(scale==null){
					scale=new Double(0.0);
				}
				scale=scale+cartLine.getQuantity();
				scaleQuantityMap.put(cartLine.getSku(), scale);
			}
		}
		
		for(FDCartLineI cartLine : orderLines){
			//key=populateScaleKey(cartLine);
			if(!(cartLine instanceof FDModifyCartLineI)){
				cartLine.setScaleQuantity(scaleQuantityMap.get(cartLine.getSku()));
			}
		}
	}*/
}
