package com.freshdirect.fdstore.util;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionReason;
import com.freshdirect.fdlogistics.model.FDDeliveryZoneInfo;
import com.freshdirect.fdlogistics.model.FDInvalidAddressException;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.promotion.PromotionHelper;
import com.freshdirect.logistics.delivery.model.TimeslotContext;

public class AddressFinder {

	public static ErpAddressModel getShipToAddress(FDUserI user,String addressId, TimeslotContext timeslotCtx, HttpServletRequest request) throws FDResourceException{
		
		Collection<ErpAddressModel> shipToAddresses = new ArrayList<ErpAddressModel>();
		ErpAddressModel address = null;
			addressId = request.getParameter("addressId");
			if(addressId != null && !"".equals(addressId)){
				address = FDCustomerManager.getShipToAddress(user.getIdentity(), addressId);
				return address;
			}else{
				if(user!=null&&!timeslotCtx.equals(TimeslotContext.CHECK_AVAIL_SLOTS_NO_USER)&&!timeslotCtx.equals(TimeslotContext.CHECK_SLOTS_FOR_ADDRESS_CRM)&& !timeslotCtx.equals(TimeslotContext.CHECKOUT_TIMESLOTS))
					shipToAddresses = FDCustomerManager.getShipToAddresses(user.getIdentity());
					if(shipToAddresses.size()>0){
						if(shipToAddresses.size() > 1){
							if(addressId==null){
								addressId = FDCustomerManager.getDefaultShipToAddressPK(user.getIdentity()); // default address
							}
							for (ErpAddressModel a : shipToAddresses) {
								if ( a.getPK().getId().equals(addressId) ) {
									address = a;
									break;
								} 
							}
						}else{
							if(user.getShoppingCart()!=null) {
								address=user.getShoppingCart().getDeliveryAddress();
								user.getShoppingCart().setDeliveryAddress(null);
								user.getShoppingCart().setDeliveryPlantInfo(null);
								
							}					
						}
						if(address==null) {
							address = (ErpAddressModel)shipToAddresses.iterator().next();
						}
					}
			}
		
		
		if(timeslotCtx.equals(TimeslotContext.CHECKOUT_TIMESLOTS)|| timeslotCtx.equals(TimeslotContext.CHECK_AVAIL_SLOTS_NO_USER)
										|| timeslotCtx.equals(TimeslotContext.CHECK_SLOTS_FOR_ADDRESS_CRM)){
			if(user.getShoppingCart()!=null){
				address=user.getShoppingCart().getDeliveryAddress();
			}
		}
		
	  return address;
	}		
	
	public static Collection<ErpAddressModel> getShipToAddresses(FDUserI user) throws FDResourceException {
	
		return FDCustomerManager.getShipToAddresses(user.getIdentity());

	}

	//get amount for zone promotion
	public static double getZonePromoAmount(FDUserI user, ErpAddressModel erpAddress, TimeslotContext timeslotCtx) throws FDResourceException {
		FDDeliveryZoneInfo zInfo = null;
		double zonePromoAmount=0.0;
		if(erpAddress!=null){
			try {
				zInfo = FDDeliveryManager.getInstance().getZoneInfo(erpAddress, new java.util.Date(), user.getHistoricOrderSize(), 
						 user.getRegionSvcType(erpAddress.getId()));
			} catch (FDInvalidAddressException e) {
				e.printStackTrace();
			}    
			if(zInfo!=null){
				zonePromoAmount = PromotionHelper.getDiscount(user,zInfo.getZoneCode());
			}
		}
		return zonePromoAmount;
		
	}
	
	public static void getApplicableRestrictions(HttpServletRequest request, FDCartModel cart){
		boolean thxgivingRestriction = false;
		boolean easterRestriction = false;
		boolean easterMealRestriction = false; //easter meals
		boolean valentineRestriction = false;
		boolean kosherRestriction = false;
		boolean alcoholRestriction = false;
	    boolean thxgiving_meal_Restriction=false;
	   
	    for (EnumDlvRestrictionReason reason : cart.getApplicableRestrictions()) {
			if(EnumDlvRestrictionReason.THANKSGIVING.equals(reason)){
				thxgivingRestriction = true;
				continue;
			}
	        if(EnumDlvRestrictionReason.THANKSGIVING_MEALS.equals(reason)){
	           thxgiving_meal_Restriction=true;
	           continue;
	        }
			//easter
	        if(EnumDlvRestrictionReason.EASTER.equals(reason)){
	           easterRestriction=true;
	           continue;
	        }
			//easter meals
	        if(EnumDlvRestrictionReason.EASTER_MEALS.equals(reason)){
	           easterMealRestriction=true;
	           continue;
	        }
			if(EnumDlvRestrictionReason.ALCOHOL.equals(reason)){
				alcoholRestriction = true;
				continue;
			}
			if(EnumDlvRestrictionReason.KOSHER.equals(reason)){
				kosherRestriction = true;
				continue;
			}
			if(EnumDlvRestrictionReason.VALENTINES.equals(reason)){
				valentineRestriction = true;
				continue;
			}
		}
		request.setAttribute("thxgivingRestriction", thxgivingRestriction);
		request.setAttribute("thxgiving_meal_Restriction", thxgiving_meal_Restriction);
		request.setAttribute("easterRestriction", easterRestriction);
		request.setAttribute("easterMealRestriction", easterMealRestriction);
		request.setAttribute("alcoholRestriction", alcoholRestriction);
		request.setAttribute("kosherRestriction", kosherRestriction);
		request.setAttribute("valentineRestriction", valentineRestriction);		
		
	}
		
}//End class
