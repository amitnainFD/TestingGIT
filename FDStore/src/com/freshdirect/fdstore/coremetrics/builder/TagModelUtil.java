package com.freshdirect.fdstore.coremetrics.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.freshdirect.customer.EnumDeliveryType;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.framework.util.log.LoggerFactory;



public class TagModelUtil  {

	private static final Logger LOGGER = LoggerFactory.getInstance(TagModelUtil.class);
	public static final String PAGE_ID_DELIMITER = ": ";
	
	public static String dropExtension(String filename){
		int dotIndex = filename.lastIndexOf(".");
		return dotIndex > -1 ? filename.substring(0, dotIndex) : filename;
	}
	
	public static int getOrderType (EnumDeliveryType type){
		
		int typeValue=-1;
		if(type.equals(EnumDeliveryType.HOME) || type.equals(EnumDeliveryType.DEPOT)){
			typeValue=1;
		}else if(type.equals(EnumDeliveryType.CORPORATE)){
			typeValue=2;
		}else if(type.equals(EnumDeliveryType.PICKUP)){
			typeValue=3;
		}else if(type.equals(EnumDeliveryType.GIFT_CARD_CORPORATE) || type.equals(EnumDeliveryType.GIFT_CARD_PERSONAL)){
			typeValue=4;
		}else if(type.equals(EnumDeliveryType.DONATION_BUSINESS) || type.equals(EnumDeliveryType.DONATION_INDIVIDUAL)){
			typeValue=5;
		}
		
		return typeValue;
	}
	
	public static String getCmOrderId(FDOrderI order){
		return getOrderType(order.getDeliveryType()) + "_" + order.getErpSalesId();
	}

	public static int getOrderCount(FDUserI user) throws SkipTagException{
		try {
			return user.getOrderHistory().getTotalOrderCount();
		} catch (FDResourceException e) {
			LOGGER.error(e);
			throw new SkipTagException("FDResourceException occured", e);
		}
	}
	
	public static ErpAddressModel getDefaultShipToErpAddressModel(FDUserI user) throws SkipTagException{
		try {
			ErpAddressModel erpAddressModel = null;
			FDIdentity identity = user.getIdentity();
			String addrPk = FDCustomerManager.getDefaultShipToAddressPK(identity);
			
			if (addrPk!=null){
				erpAddressModel = FDCustomerManager.getAddress(identity, addrPk);
			}
			
			return erpAddressModel;
		} catch (FDResourceException e) {
			LOGGER.error(e);
			throw new SkipTagException("FDResourceException occured", e);
		}
	}
	
	public static List<ContentNodeModel> getPageLocationSubset(ContentNodeModel baseContentNodeModel) {
		
		List<ContentNodeModel> subset = new ArrayList<ContentNodeModel>();

		if (baseContentNodeModel == null) return subset;
		
		if ("C".equals(baseContentNodeModel.getContentType()) || "D".equals(baseContentNodeModel.getContentType())) {
			subset.add(baseContentNodeModel);
		}
		
		if ("C".equals(baseContentNodeModel.getContentType())) {

			ContentNodeModel subCategory = baseContentNodeModel.getParentNode();
			subset.add(subCategory);

			if ("C".equals(subCategory.getContentType())) {

				ContentNodeModel category = subCategory.getParentNode();
				subset.add(category);

				if ("C".equals(category.getContentType())) {
				
					ContentNodeModel department = category.getParentNode();
					subset.add(department);

					while ("C".equals(subset.get(3).getContentType())) {
						subset = shiftSubset(subset);
					}
				}
			}
		}
		Collections.reverse(subset); 
		return subset;
	}
	
	public static String getPageIdFromProductModel(ProductModel productModel) throws SkipTagException{
		return ("PRODUCT" + PAGE_ID_DELIMITER + productModel.getFullName() + " ("+ productModel.getContentKey().getId() +")");
	}

	private static List<ContentNodeModel> shiftSubset(List<ContentNodeModel> subset) {
		
		subset.set(1, subset.get(2));
		subset.set(2, subset.get(3));
		subset.set(3, subset.get(3).getParentNode());
		
		return subset;
	}
}