package com.freshdirect.fdstore.customer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Category;

import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.common.customer.EnumWebServiceType;
import com.freshdirect.customer.EnumDeliveryType;
import com.freshdirect.customer.EnumSaleType;
import com.freshdirect.customer.ErpAbstractOrderModel;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpAppliedCreditModel;
import com.freshdirect.customer.ErpChargeLineModel;
import com.freshdirect.customer.ErpCreateOrderModel;
import com.freshdirect.customer.ErpDeliveryInfoModel;
import com.freshdirect.customer.ErpDeliveryPlantInfoModel;
import com.freshdirect.customer.ErpDepotAddressModel;
import com.freshdirect.customer.ErpModifyOrderModel;
import com.freshdirect.customer.ErpOrderLineModel;
import com.freshdirect.fdlogistics.model.FDDeliveryZoneInfo;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * Translates an FDOrder into an ErpOrder.
 *
 * @version $Revision:16$
 * @author $Author:Mike Rose$
 */
public class FDOrderTranslator {
	private static Category LOGGER = LoggerFactory.getInstance(FDOrderTranslator.class);

	public static ErpCreateOrderModel getErpCreateOrderModel(FDCartModel cart, EnumSaleType saleType) throws FDResourceException {
		return getErpCreateOrderModel(cart,saleType, false, false);
	}

	public static ErpCreateOrderModel getErpCreateOrderModel(FDCartModel cart) throws FDResourceException {
		return getErpCreateOrderModel(cart,false,false);
	}
	public static ErpCreateOrderModel getErpCreateOrderModel(FDCartModel cart, boolean skipModifyLines, boolean sameDeliveryDate) throws FDResourceException {
		ErpCreateOrderModel createOrder = new ErpCreateOrderModel();
		translateOrder(cart, createOrder, skipModifyLines, sameDeliveryDate);
		return createOrder;
	}

	public static ErpModifyOrderModel getErpModifyOrderModel(FDCartModel cart) throws FDResourceException {
		ErpModifyOrderModel modifyOrder = new ErpModifyOrderModel();
		translateOrder(cart, modifyOrder, false, false);
		return modifyOrder;
	}
	
	public static ErpCreateOrderModel getErpCreateOrderModel(FDCartModel cart,EnumSaleType saleType, boolean skipModifyLines, boolean sameDeliveryDate) throws FDResourceException {
		ErpCreateOrderModel createOrder = new ErpCreateOrderModel();
		//createOrder.setSaleType(saleType);
		//if(EnumSaleType.REGULAR.equals(saleType.getSaleType())) {
			translateOrder(cart, createOrder, skipModifyLines, sameDeliveryDate);
		//}
		/*else if(EnumSaleType.SUBSCRIPTION.equals(saleType)) {
			translateSubscriptionOrder(cart, createOrder, skipModifyLines);
		}*/
		return createOrder;
	}

	private static void translateOrder(FDCartModel cart, ErpAbstractOrderModel order, boolean skipModifyLines, boolean sameDeliveryDate) throws FDResourceException {
//		try {
		    order.seteStoreId(cart.getEStoreId());
			order.setPaymentMethod(cart.getPaymentMethod());
			//System.out.println("Selected gift cards "+cart.getSelectedGiftCards() != null ? cart.getSelectedGiftCards().size() : 0);
			order.setSelectedGiftCards(cart.getSelectedGiftCards());
			FDReservation deliveryReservation = cart.getDeliveryReservation();
			ErpDeliveryInfoModel deliveryInfo = new ErpDeliveryInfoModel();
			ErpDeliveryPlantInfoModel plantInfoModel=cart.getDeliveryPlantInfo();
			if(plantInfoModel!=null) {
				ErpDeliveryPlantInfoModel dpi=new ErpDeliveryPlantInfoModel();
				
				dpi.setPlantId(cart.getDeliveryPlantInfo().getPlantId());
				dpi.setSalesOrg(cart.getDeliveryPlantInfo().getSalesOrg());
				dpi.setDistChannel(cart.getDeliveryPlantInfo().getDistChannel());
				dpi.setDivision(cart.getDeliveryPlantInfo().getDivision());
				deliveryInfo.setDeliveryPlantInfo(dpi);
			} else {
				LOGGER.warn("Defaulting DeliveryPlantInfo for customer : "+order.getCustomerId()+ " and eStore :"+cart.getEStoreId());
				ErpDeliveryPlantInfoModel dpi=FDUserUtil.getDefaultDeliveryPlantInfo();
				deliveryInfo.setDeliveryPlantInfo(dpi);
			}
			if(deliveryReservation!=null)
				deliveryInfo.setDeliveryReservationId(deliveryReservation.getPK().getId());
			if(cart.getDeliveryAddress()!=null) {
				deliveryInfo.setDeliveryAddress(cart.getDeliveryAddress());
				order.setGlCode(lookupGLCode(cart.getDeliveryAddress()));
			}
			
		 	   deliveryInfo.setOrderMobileNumber(cart.getOrderMobileNumber());
			
			if(deliveryReservation!=null && deliveryReservation.getTimeslot()!=null) {
				deliveryInfo.setDeliveryStartTime(deliveryReservation.getStartTime());
				deliveryInfo.setDeliveryEndTime(deliveryReservation.getEndTime());
				
				if(EnumEStoreId.FDX.name().equals(cart.getEStoreId().name())){
					LOGGER.warn("Customer : "+order.getCustomerId()+ " and eStore :"+cart.getEStoreId()+" delivery plant :"+deliveryInfo.getDeliveryPlantInfo());
					deliveryInfo.setOriginalCutoffTime(deliveryReservation.getTimeslot().getOriginalCutoffDateTime()); // this is used by CSR
					//give minimum time specified in  deliveryReservation.getTimeslot().getMinDurationForModStart() to start order modification
					// if the sysdate + deliveryReservation.getTimeslot().getMinDurationForModStart() is after the timeslot cutoff then 
					// customer will get till sysdate + deliveryReservation.getTimeslot().getMinDurationForModStart() to start order MOD. or 
					// if the  sysdate + deliveryReservation.getTimeslot().getMinDurationForModStart() is before timeslot cutoff then
					// cusotmer will get min  sysdate + deliveryReservation.getTimeslot().getMinDurationForModStart()
					// and max till timeslot cutoff to modify the  order
					
					Date timeslotCutoff = deliveryReservation.getTimeslot().getCutoffDateTime();
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MINUTE, (int)deliveryReservation.getTimeslot().getMinDurationForModStart());
					if(timeslotCutoff.before(cal.getTime())){
						deliveryInfo.setDeliveryCutoffTime(cal.getTime());
					}else{
						deliveryInfo.setDeliveryCutoffTime(timeslotCutoff);
					}
					
				}else{
					deliveryInfo.setDeliveryCutoffTime(deliveryReservation.getCutoffTime());
				}
				
				order.setRequestedDate(deliveryReservation.getStartTime());
				deliveryInfo.setMinDurationForModStart(deliveryReservation.getTimeslot().getMinDurationForModStart());
				deliveryInfo.setMinDurationForModification(deliveryReservation.getTimeslot().getMinDurationForModification());
			}
			else {
				Calendar startTime=Calendar.getInstance();
				Calendar endTime=Calendar.getInstance();
				Calendar cutOffTime=Calendar.getInstance();
				cutOffTime.add(Calendar.DATE, -1);
				endTime.add(Calendar.HOUR, 2);
				deliveryInfo.setDeliveryStartTime(startTime.getTime());
				deliveryInfo.setDeliveryEndTime(endTime.getTime());
				order.setRequestedDate(startTime.getTime());
				deliveryInfo.setDeliveryCutoffTime(cutOffTime.getTime());
			}
			
			if (cart.getDeliveryAddress() instanceof ErpDepotAddressModel) {
				ErpDepotAddressModel depotAddress = (ErpDepotAddressModel) cart.getDeliveryAddress();
				deliveryInfo.setDepotLocationId(depotAddress.getLocationId());
				if (depotAddress.isPickup()) {
					deliveryInfo.setDeliveryType(EnumDeliveryType.PICKUP);
				}else{
					deliveryInfo.setDeliveryType(EnumDeliveryType.DEPOT);
				}
		} else {
			ErpAddressModel address = cart.getDeliveryAddress();
			if (address != null) {
				
				if (EnumServiceType.WEB.equals(address.getServiceType())) {
					EnumWebServiceType webServiceType = address
							.getWebServiceType();
					deliveryInfo.setDeliveryType(EnumDeliveryType
							.getDeliveryType(webServiceType.getName()));
				} else {

					if (EnumEStoreId.FDX.equals(cart.getEStoreId())) {
						deliveryInfo.setDeliveryType(EnumDeliveryType.FDX);
					} else if (EnumServiceType.CORPORATE.equals(address
							.getServiceType())) {
						deliveryInfo
								.setDeliveryType(EnumDeliveryType.CORPORATE);
					} else {
						if (EnumServiceType.WEB
								.equals(address.getServiceType())) {
							EnumWebServiceType webServiceType = address
									.getWebServiceType();
							deliveryInfo.setDeliveryType(EnumDeliveryType
									.getDeliveryType(webServiceType.getName()));
						} else if (EnumServiceType.CORPORATE.equals(address
								.getServiceType())) {
							deliveryInfo
									.setDeliveryType(EnumDeliveryType.CORPORATE);
						} else {
							deliveryInfo.setDeliveryType(EnumDeliveryType.HOME);
						}
					}
				}
			}
		}
			FDDeliveryZoneInfo zoneInfo = cart.getZoneInfo();
			if (zoneInfo != null) { //this may be null in express checkout flow
				deliveryInfo.setDeliveryZone(zoneInfo.getZoneCode());
				deliveryInfo.setDeliveryRegionId(zoneInfo.getRegionId());
			}
			
			order.setDeliveryInfo(deliveryInfo);
			order.setPricingDate(Calendar.getInstance().getTime());
			
			order.setTax(cart.getTaxValue());
			
			// instead we add the actual subtotal which is subtotal + disount amount for the order 
			//order.setSubTotal(cart.getSubTotal());
			order.setSubTotal(cart.getActualSubTotal());
			
			List<ErpOrderLineModel> orderLines = new ArrayList<ErpOrderLineModel>();
			translateOrderLines(cart, skipModifyLines, sameDeliveryDate,orderLines);
			order.setOrderLines(orderLines);

			//
			// Convert cart's customer credits to applied credits for the order
			// We cannot just take credits from the original order and set them on 
			// modify order as they will have the PK which is not correct
			List<ErpAppliedCreditModel> aList = new ArrayList<ErpAppliedCreditModel>();
			for( ErpAppliedCreditModel m : cart.getCustomerCredits() ) {
				aList.add( new ErpAppliedCreditModel(m) );
			}
			order.setAppliedCredits(aList);

			//
			// Transfer cart charges to order model
			// We cannot just take charges from the original order and set them on 
			// modify order as they will have the PK which is not correct
			List<ErpChargeLineModel> cList = new ArrayList<ErpChargeLineModel>();
			for( ErpChargeLineModel m : cart.getCharges() ) {
				cList.add( new ErpChargeLineModel( m ) );
			}
			order.setCharges(cList);

			// add discount to promotion list
			if (cart.getDiscounts() != null && cart.getDiscounts().size() > 0) {
				order.setDiscounts(cart.getDiscounts());				
			}
			
			//
			// Set miscellaneous messages
			//
			order.setCustomerServiceMessage(cart.getCustomerServiceMessage() == null ? "" : cart.getCustomerServiceMessage());
			order.setMarketingMessage(cart.getMarketingMessage() == null ? "" : cart.getMarketingMessage());
			//set the delivery pass info.
			order.setDeliveryPassCount(cart.getDeliveryPassCount());
			order.setDlvPassApplied(cart.isDlvPassApplied());
			order.setDlvPromotionApplied(cart.isDlvPromotionApplied());
			
			order.setBufferAmt(cart.getBufferAmt());
			order.setDlvPassExtendDays(cart.getDlvPassExtendDays());
			order.setCurrentDlvPassExtendDays(cart.getCurrentDlvPassExtendDays());
			
		/*} catch (FDInvalidConfigurationException ex) {
			throw new FDResourceException(ex, "Invalid configuration encountered");
		}*/
	}

	public static void translateOrderLines(FDCartModel cart,
			boolean skipModifyLines, boolean sameDeliveryDate,
			List<ErpOrderLineModel> orderLines) throws FDResourceException {
		int num = 0;
		try {
			if(null != cart && null != cart.getOrderLines() && null != orderLines){
				for ( FDCartLineI line : cart.getOrderLines() ) {
						//Regular Availability item.
						if (skipModifyLines && line instanceof FDModifyCartLineI) {
							continue;
						}
					
					num += addTranslatedLine(num, line, orderLines);
				}
				for ( FDCartLineI line : cart.getSampleLines() ) {
					num += addTranslatedLine(num, line, orderLines);
				}
			}
		} catch (Exception e) {
			throw new FDResourceException(e, "Invalid configuration encountered");
		}
	}

	public static void translateOrderLines(FDCartModel cart,
			List<ErpOrderLineModel> orderLines) throws FDResourceException {
		translateOrderLines(cart, false, false, orderLines);
	}
	public static void translateSubscriptionOrder(FDCartModel cart, ErpAbstractOrderModel order, boolean skipModifyLines) throws FDResourceException {
		try {
			order.setPaymentMethod(cart.getPaymentMethod());
			order.seteStoreId(EnumEStoreId.FD);
			order.setPricingDate(Calendar.getInstance().getTime());
			order.setRequestedDate(order.getPricingDate());
			order.setTax(cart.getTaxValue());
			order.setSubTotal(cart.getSubTotal());
			

			List<ErpOrderLineModel> orderLines = new ArrayList<ErpOrderLineModel>();
			int num = 0;
			for ( FDCartLineI line : cart.getOrderLines() ) {
				if (skipModifyLines && (line instanceof FDModifyCartLineI)) {
					continue;
				}
				num += addTranslatedLine(num, line, orderLines);
			}
			for ( FDCartLineI line : cart.getSampleLines() ) {
				num += addTranslatedLine(num, line, orderLines);
			}
			order.setOrderLines(orderLines);


			//
			// Transfer cart charges to order model
			// We cannot just take charges from the original order and set them on 
			// modify order as they will have the PK which is not correct
			List<ErpChargeLineModel> cList = new ArrayList<ErpChargeLineModel>();
			for( ErpChargeLineModel m : cart.getCharges() ) {
				cList.add( new ErpChargeLineModel( m ) );
			}
			order.setCharges(cList);

			// add discount to promotion list
			if (cart.getDiscounts() != null && cart.getDiscounts().size() > 0) {
				order.setDiscounts(cart.getDiscounts());				
			}
			
			//
			// Set miscellaneous messages
			//
			order.setCustomerServiceMessage(cart.getCustomerServiceMessage() == null ? "" : cart.getCustomerServiceMessage());
			order.setMarketingMessage(cart.getMarketingMessage() == null ? "" : cart.getMarketingMessage());
			//set the delivery pass info.
			order.setDeliveryPassCount(cart.getDeliveryPassCount());
		} catch (FDInvalidConfigurationException ex) {
			throw new FDResourceException(ex, "Invalid configuration encountered");
		}
	}

	/** @return number of lines added */
	private static int addTranslatedLine(int baseLineNumber, FDCartLineI cartLine, List<ErpOrderLineModel> orderLines) throws FDResourceException, FDInvalidConfigurationException {
		ErpOrderLineModel erpLine = cartLine.buildErpOrderLines(baseLineNumber);
		erpLine.setCouponDiscount(cartLine.getCouponDiscount());
		orderLines.add(erpLine);
		return 1;
	}

	
	private static String lookupGLCode(AddressModel address) throws FDResourceException {
		FDDeliveryManager dlvMan = FDDeliveryManager.getInstance();
		return dlvMan.getMunicipalityInfos().getGlCode(address.getState(), dlvMan.getCounty(address), address.getCity());
	}

}