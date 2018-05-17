package com.freshdirect.fdstore.ecoupon;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.ecoupon.model.FDCouponInfo;
import com.freshdirect.framework.util.DateUtil;

public class FDCouponProductInfo implements Serializable {
	
	private String catId;
	private String productId;
	private String skuCode;
	private String upc;
	
	public FDCouponProductInfo(FDProductInfo productInfo, String catId, String productId) {
		super();
		if(null != productInfo){
			this.upc = productInfo.getUpc();
			this.skuCode = productInfo.getSkuCode();
			this.catId = catId;
			this.productId = productId;
		}
	}

	public FDCouponProductInfo(FDCartLineI cartLine, String catId, String productId) {
		super();
		if(null !=cartLine){
			this.upc = cartLine.getUpc();
			this.skuCode = cartLine.getSkuCode();
			this.catId = catId;
			this.productId = productId;
		}
	}

	public String getCatId() {
		return catId;
	}

	public String getProductId() {
		return productId;
	}

	public String getSkuCode() {
		return skuCode;
	}

	public String getUpc() {
		return upc;
	}
	
	
}
