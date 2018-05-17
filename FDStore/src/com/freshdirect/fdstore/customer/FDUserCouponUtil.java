package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.freshdirect.customer.ErpCouponDiscountLineModel;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.adapter.FDOrderAdapter;
import com.freshdirect.fdstore.ecoupon.EnumCouponContext;
import com.freshdirect.fdstore.ecoupon.EnumCouponDisplayStatus;
import com.freshdirect.fdstore.ecoupon.EnumCouponStatus;
import com.freshdirect.fdstore.ecoupon.FDCouponFactory;
import com.freshdirect.fdstore.ecoupon.FDCouponProductInfo;
import com.freshdirect.fdstore.ecoupon.FDCustomerCoupon;
import com.freshdirect.fdstore.ecoupon.model.FDCouponInfo;
import com.freshdirect.fdstore.ecoupon.model.FDCustomerCouponWallet;
import com.freshdirect.framework.util.StringUtil;

public class FDUserCouponUtil implements Serializable {

	private static final Map<EnumCouponContext,Map<EnumCouponStatus,Boolean>> displayStatusMessageMap = new HashMap<EnumCouponContext,Map<EnumCouponStatus,Boolean>>();
	static{
		Map<EnumCouponStatus,Boolean> statusMap = new HashMap<EnumCouponStatus,Boolean>();
		displayStatusMessageMap.put(EnumCouponContext.VIEWCART, statusMap);
		statusMap.put(EnumCouponStatus.COUPON_APPLIED,true);
		statusMap.put(EnumCouponStatus.COUPON_MIN_QTY_NOT_MET,true);
		statusMap.put(EnumCouponStatus.COUPON_CLIPPED_EXPIRED,true);
		statusMap.put(EnumCouponStatus.COUPON_CLIPPED_FILTERED,false);
		statusMap.put(EnumCouponStatus.COUPON_CLIPPED_ACTIVE,false);
		
		statusMap = new HashMap<EnumCouponStatus,Boolean>();
		displayStatusMessageMap.put(EnumCouponContext.PRODUCT, statusMap);
		statusMap.put(EnumCouponStatus.COUPON_APPLIED,false);
		statusMap.put(EnumCouponStatus.COUPON_MIN_QTY_NOT_MET,false);
		statusMap.put(EnumCouponStatus.COUPON_CLIPPED_EXPIRED,false);
		statusMap.put(EnumCouponStatus.COUPON_CLIPPED_FILTERED,false);
		statusMap.put(EnumCouponStatus.COUPON_CLIPPED_ACTIVE,false);
		
		statusMap = new HashMap<EnumCouponStatus,Boolean>();
		displayStatusMessageMap.put(EnumCouponContext.CHECKOUT, statusMap);
		statusMap.put(EnumCouponStatus.COUPON_APPLIED,false);
		statusMap.put(EnumCouponStatus.COUPON_MIN_QTY_NOT_MET,false);
		statusMap.put(EnumCouponStatus.COUPON_CLIPPED_EXPIRED,false);
		statusMap.put(EnumCouponStatus.COUPON_CLIPPED_FILTERED,false);
		statusMap.put(EnumCouponStatus.COUPON_CLIPPED_ACTIVE,false);
		
		statusMap = new HashMap<EnumCouponStatus,Boolean>();
		displayStatusMessageMap.put(EnumCouponContext.VIEWORDER, statusMap);
		statusMap.put(EnumCouponStatus.COUPON_APPLIED,false);
		statusMap.put(EnumCouponStatus.COUPON_MIN_QTY_NOT_MET,false);
		statusMap.put(EnumCouponStatus.COUPON_CLIPPED_EXPIRED,false);
		statusMap.put(EnumCouponStatus.COUPON_CLIPPED_FILTERED,false);
		statusMap.put(EnumCouponStatus.COUPON_CLIPPED_ACTIVE,false);
	}
	//Get Coupon Customer based on UPC
	public static FDCustomerCoupon getCustomerCoupon(FDUserI user,String upc, EnumCouponContext ctx,FDCustomerCouponWallet couponWallet) {	
		FDCustomerCoupon customerCoupon = null;
		if(isCouponsInfoDisplayable(user,ctx) && isValidUPC(upc)){
			FDProductInfo prodInfo =null;
			if(null !=upc){
				prodInfo =FDCachedFactory.getProductInfoByUpc(upc);
			}
			String prodId=null;
			String catId=null;
			if(!EnumCouponContext.VIEWORDER.equals(ctx) && null !=prodInfo) {
				ProductModel pm = getProductModel(prodInfo.getSkuCode());
				if(null!=pm){
					catId=pm.getParentId();
					prodId=pm.getContentName();
				}
			}
			customerCoupon =getCustomerCoupon(user,prodInfo, ctx, catId, prodId, couponWallet);
		}
		return customerCoupon;
	}
	
	public static FDCustomerCoupon getCustomerCoupon(FDUserI user,FDProductInfo prodInfo, EnumCouponContext ctx,String catId,String prodId,FDCustomerCouponWallet couponWallet) {	
		
		FDCustomerCoupon customerCoupon = null;
		if(isCouponsInfoDisplayable(user,ctx) && null != prodInfo && isValidUPC(prodInfo.getUpc())){
			FDCouponInfo coupon = FDCouponFactory.getInstance().getCouponByUpc(prodInfo.getUpc());
			if(null !=coupon && null !=user.getCouponWallet() && !(EnumCouponContext.PRODUCT.equals(ctx) && (user.getCouponWallet().isExpired(coupon.getCouponId()) || user.getCouponWallet().isRedeemPending(coupon.getCouponId()))) && checkForPendingCoupons(user,coupon.getCouponId(),ctx)){
				EnumCouponStatus couponStatus = getCustomerCouponStatus(null !=coupon?coupon.getCouponId():null, couponWallet,ctx);
				if(couponStatus != null) {
					boolean displayMessage = getDisplayStatusMessage(ctx, couponStatus);
					 FDCouponProductInfo couponProductInfo = new FDCouponProductInfo(prodInfo,catId,prodId);
					 customerCoupon = new FDCustomerCoupon(coupon, couponStatus,couponProductInfo,displayMessage,ctx);
				}
				assignCouponDisplayStatus(customerCoupon, ctx);
			}
		}
		
		return customerCoupon;		
	}

	public static FDCustomerCoupon getCustomerCoupon(FDUserI user,FDCartLineI cartLine, EnumCouponContext ctx,FDCustomerCouponWallet couponWallet) {
		FDCustomerCoupon customerCoupon = null;
		if(isCouponsInfoDisplayable(user,ctx) && cartLine != null && isValidUPC(cartLine.getUpc())){ 
			String catId = null;
			String prodId = null;
			if(!EnumCouponContext.VIEWORDER.equals(ctx)) {
				ProductModel pm = getProductModel(cartLine.getSkuCode());
				if(null!=pm){
					catId=pm.getParentId();
					prodId=pm.getContentName();
				}
			}
			customerCoupon = getCustomerCoupon(user,cartLine, ctx, catId, prodId, couponWallet);
		}
		return customerCoupon;
		
	}
	//Get Coupon Customer based on CartLine
	public static FDCustomerCoupon getCustomerCoupon(FDUserI user,FDCartLineI cartLine, EnumCouponContext ctx,String catId,String prodId,FDCustomerCouponWallet couponWallet) {
		FDCustomerCoupon customerCoupon = null;
		EnumCouponStatus couponStatus = null;
		FDCouponInfo coupon = null;
		String couponId = null;
		boolean isCouponAvailable =false;
		ErpCouponDiscountLineModel couponDiscount = null;
		// Check if the cartline is associated to a coupon if so fetch the coupon, else fetch the current coupon for product
		if(isCouponsInfoDisplayable(user,ctx) && cartLine != null  && isValidUPC(cartLine.getUpc())){ 
			if(!EnumCouponContext.VIEWORDER.equals(ctx)) {
				
				if(cartLine.getCouponDiscount() != null && cartLine.getCouponDiscount().getCouponId() != null){
					couponDiscount = cartLine.getCouponDiscount();
				}else if(cartLine instanceof FDModifyCartLineI && ((FDModifyCartLineI)cartLine).getOriginalOrderLine().getCouponDiscount()!=null ){
					couponDiscount = ((FDModifyCartLineI)cartLine).getOriginalOrderLine().getCouponDiscount();
				}
				if(couponDiscount != null){
					coupon = FDCouponFactory.getInstance().getCoupon(couponDiscount.getCouponId());
				} else {
					coupon = FDCouponFactory.getInstance().getCouponByUpc(cartLine.getUpc());
				}
				if(null !=coupon){
					isCouponAvailable=true;
					couponId=coupon.getCouponId();
				}
			}else{
				if(cartLine.getCouponDiscount() != null){
					isCouponAvailable=true;
					couponId =cartLine.getCouponDiscount().getCouponId();
				}
			}
			
			if(isCouponAvailable && !(EnumCouponContext.PRODUCT.equals(ctx)&& user.getCouponWallet().isExpired(couponId)) && (null !=couponDiscount || checkForPendingCoupons(user,couponId,ctx))){
				// Check if the cartline has specific status, else fetch the current status for coupon & customer
				if(cartLine != null && cartLine.getCouponStatus() != null) {
					couponStatus = cartLine.getCouponStatus();
				} else {			
					couponStatus = getCustomerCouponStatus(couponId, couponWallet,ctx);			
				}
				
				boolean displayMessage = getDisplayStatusMessage(ctx, couponStatus);
				if(EnumCouponContext.VIEWORDER.equals(ctx)){
					FDCouponProductInfo couponProductInfo = new FDCouponProductInfo(cartLine,catId,prodId);
					customerCoupon = new FDCustomerCoupon(cartLine, EnumCouponStatus.COUPON_CLIPPED_PENDING,couponProductInfo,displayMessage);//Coupon should be either in 'Redeem Pending' or 'Redeemed'.
				}else if(couponStatus != null) {
					FDCouponProductInfo couponProductInfo = new FDCouponProductInfo(cartLine,catId,prodId);
					customerCoupon = new FDCustomerCoupon(coupon, couponStatus,couponProductInfo,displayMessage,ctx);
				}
				assignCouponDisplayStatus(customerCoupon, ctx);
			}
		}
		if(null !=customerCoupon && EnumCouponContext.CHECKOUT.equals(ctx)){
			if(!(EnumCouponStatus.COUPON_APPLIED.equals(customerCoupon.getStatus()) || EnumCouponStatus.COUPON_CLIPPED_FILTERED.equals(customerCoupon.getStatus()) ) ){
				customerCoupon = null;// For 'checkout' context, return only coupons in 'applied' or 'filtered' status.
			}
		}
		return customerCoupon;
	}
	
	public static boolean checkForPendingCoupons(FDUserI user,String couponId,EnumCouponContext ctx){
		
		boolean isOk = true;
		if(null !=couponId && null !=user.getCouponWallet()){
			FDCartI cart = user.getShoppingCart();
			if(!EnumCouponContext.VIEWORDER.equals(ctx) &&user.getCouponWallet().isRedeemPending(couponId) && !user.getCouponWallet().isClipped(couponId)){
				if(cart instanceof FDModifyCartModel){
					FDModifyCartModel modifyCart =(FDModifyCartModel)cart;					
					if(!modifyCart.getOriginalOrderCoupons().contains(couponId) && !user.getCouponWallet().getClippedFdFilteredIds().contains(couponId)){
						isOk = false;
					}
				}else{
					isOk= false;
				}
			}
		}
		return isOk;
	}
	
	public static void assignCouponDisplayStatus(FDCustomerCoupon customerCoupon, EnumCouponContext ctx) {
		
		if(customerCoupon != null && customerCoupon.getStatus() != null) {
			if(customerCoupon.getStatus().equals(EnumCouponStatus.COUPON_ACTIVE)) {
				if((EnumCouponContext.PRODUCT.equals(ctx) ||EnumCouponContext.VIEWCART.equals(ctx))){
					customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_CLIPPABLE);
				}else{
					customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_USED_DONOTDISPLAY);
				}
			} else if(customerCoupon.getStatus().equals(EnumCouponStatus.COUPON_CLIPPED_ACTIVE)) {
				if(!EnumCouponContext.VIEWORDER.equals(ctx) && !EnumCouponContext.CHECKOUT.equals(ctx)){
					customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_CLIPPED_DISABLED);
				}else{
					customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_USED_DONOTDISPLAY);
				}
			} else if(customerCoupon.getStatus().equals(EnumCouponStatus.COUPON_CLIPPED_PENDING)){
				 if(!EnumCouponContext.PRODUCT.equals(ctx)){
					 customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_CLIPPED_DISABLED);
				 }else{
					 customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_USED_DONOTDISPLAY);
				 }
			} else if(customerCoupon.getStatus().equals(EnumCouponStatus.COUPON_CLIPPED_REDEEMED)){
				if(!EnumCouponContext.VIEWORDER.equals(ctx)){
					customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_USED_DONOTDISPLAY);
				}else{
					 customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_CLIPPED_DISABLED);
				}
			}else if(customerCoupon.getStatus().equals(EnumCouponStatus.COUPON_CLIPPED_EXPIRED)){
				if(EnumCouponContext.VIEWCART.equals(ctx) /*|| EnumCouponContext.CHECKOUT.equals(ctx)*/){
					customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_CLIPPED_DISABLED);
				}else{
					customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_USED_DONOTDISPLAY);
				}
			}else if(customerCoupon.getStatus().equals(EnumCouponStatus.COUPON_APPLIED)){
				if(EnumCouponContext.VIEWORDER.equals(ctx)){
					customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_USED_DONOTDISPLAY);
				}else{
					 customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_CLIPPED_DISABLED);
				}
			}else if(customerCoupon.getStatus().equals(EnumCouponStatus.COUPON_MIN_QTY_NOT_MET)){
				if(EnumCouponContext.VIEWORDER.equals(ctx) || EnumCouponContext.CHECKOUT.equals(ctx) ){
					customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_USED_DONOTDISPLAY);
				}else{
					 customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_CLIPPED_DISABLED);
				}
			}else if(customerCoupon.getStatus().equals(EnumCouponStatus.COUPON_CLIPPED_FILTERED)){
				if(EnumCouponContext.CHECKOUT.equals(ctx)){
					customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_CLIPPED_DISABLED);
				}else{
					customerCoupon.setDisplayStatus(EnumCouponDisplayStatus.COUPON_USED_DONOTDISPLAY);
				}
			}
		}
	}	
	
	public static EnumCouponStatus getCustomerCouponStatus(String couponId,FDCustomerCouponWallet couponWallet,EnumCouponContext ctx) {

		EnumCouponStatus couponStatus = null;
		if(couponWallet != null && couponId != null) {	    	
			if(couponWallet.getClippedMinQtyNotMetIds().contains(couponId)) {
				couponStatus = EnumCouponStatus.COUPON_MIN_QTY_NOT_MET;
			}else if(EnumCouponContext.CHECKOUT.equals(ctx) && couponWallet.getClippedFdFilteredIds().contains(couponId)){
				couponStatus = EnumCouponStatus.COUPON_CLIPPED_FILTERED;
			}else if(couponWallet.getClippedActiveIds().contains(couponId)){
				couponStatus = EnumCouponStatus.COUPON_CLIPPED_ACTIVE;
			}else if(couponWallet.getAvailableIds().contains(couponId)){
				couponStatus = EnumCouponStatus.COUPON_ACTIVE;
			}else if(couponWallet.getClippedRedeemedIds().contains(couponId)){
				couponStatus = EnumCouponStatus.COUPON_CLIPPED_REDEEMED;
			}else if(couponWallet.getClippedPendingIds().contains(couponId)){
				couponStatus = EnumCouponStatus.COUPON_CLIPPED_PENDING;
			}else if(couponWallet.getClippedExpiredIds().contains(couponId)){
				couponStatus = EnumCouponStatus.COUPON_CLIPPED_EXPIRED;
			}
		}
		return couponStatus;
	}
	
	public static void updateClippedCoupon(String couponId,FDCustomerCouponWallet couponWallet){
		if(null !=couponId && null !=couponWallet){
			 if(couponWallet.getAvailableIds().contains(couponId)){
				 couponWallet.getAvailableIds().remove(couponId);
			 }
			 couponWallet.getClippedActiveIds().add(couponId);
			
		}
	}
	


	private static ProductModel getProductModel(String skuCode) {
		ProductModel pm =null;
		try {
			pm = ContentFactory.getInstance().getProduct(skuCode);
		} catch (FDSkuNotFoundException e) {
		}
		return pm;

	}

	public static boolean isValidUPC(String upc){
		boolean isValid = false;
		if(null != upc && !"".equalsIgnoreCase(upc.trim()) && StringUtil.isNumeric(upc)){
			isValid= true;
		}
		return isValid;
	}
	
	private static boolean isCouponsInfoDisplayable(FDUserI user,EnumCouponContext context) {
		try {
			if(user.isCouponsSystemAvailable() || EnumCouponContext.VIEWORDER.equals(context) ){
				return true;
			}
		} catch (FDResourceException e) {
			
		}
		return false;
	}
	
	public static boolean getDisplayStatusMessage(EnumCouponContext ctx, EnumCouponStatus status){
		boolean displayMessage = false;
		if(null !=displayStatusMessageMap.get(ctx) && null!=displayStatusMessageMap.get(ctx).get(status)){
			displayMessage =(Boolean)displayStatusMessageMap.get(ctx).get(status);
		}
		return displayMessage;
	}
	
	public static EnumCouponStatus getCouponStatus(FDCustomerCoupon currCoupon, Set<String> recentlyAppliedCoupons){
		if(null == currCoupon){
			return null;
		}
		EnumCouponStatus changedStatus = currCoupon.getStatus();
		if(null != recentlyAppliedCoupons){
			if(EnumCouponStatus.COUPON_APPLIED.equals(currCoupon.getStatus()) && !recentlyAppliedCoupons.contains(currCoupon.getCouponId())){
				changedStatus = null;
			}else if(EnumCouponStatus.COUPON_CLIPPED_ACTIVE.equals(currCoupon.getStatus()) && recentlyAppliedCoupons.contains(currCoupon.getCouponId())){
				changedStatus = EnumCouponStatus.COUPON_APPLIED;
			}
		}
		return changedStatus;
	}
}
