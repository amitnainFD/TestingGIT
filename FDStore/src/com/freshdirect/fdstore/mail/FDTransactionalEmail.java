package com.freshdirect.fdstore.mail;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.content.Image;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDOrderI;

public class FDTransactionalEmail extends FDInfoEmail {

	private FDOrderI order;
	private EnumEStoreId eStoreId;

	public FDTransactionalEmail(FDCustomerInfo customer, FDOrderI order) {
		super(customer);
		this.order = order;
	}
	
	public FDTransactionalEmail(FDCustomerInfo customer, FDOrderI order, EnumEStoreId eStoreId) {
		super(customer);
		this.order = order;
		this.eStoreId = eStoreId;
	}

	public FDOrderI getOrder() {
		return this.order;
	}

	public void setOrder(FDOrderI order) {
		this.order = order;
	}

	public EnumEStoreId geteStoreId() {
		return eStoreId;
	}

	public void seteStoreId(EnumEStoreId eStoreId) {
		this.eStoreId = eStoreId;
	}
	

	/**
	 * @see com.freshdirect.fdstore.mail.FDInfoEmail#decorateMap(java.util.Map)
	 */
	protected void decorateMap(Map map) {
		super.decorateMap(map);
		map.put("order", this.getOrder());
		
		if (this.geteStoreId() != null && (this.geteStoreId()).equals(EnumEStoreId.FDX)) {
			this.populateExtraProductInfo(map);
		}
	}

	protected void populateExtraProductInfo(Map<String, Object> map) {
		Map<String, Map<String, Object>> prodInfos = new HashMap<String, Map<String, Object>>();
		
		List<FDCartLineI> lines = this.getOrder().getOrderLines();
		
		for (Iterator<FDCartLineI> i = lines.iterator(); i.hasNext();) {
			FDCartLineI cartLine = i.next();
			ProductModel productNode = cartLine.lookupProduct();
			
			if (null == productNode && null !=cartLine.getProductRef()) {
				productNode = cartLine.getProductRef().lookupProductModel();
			}
			if (productNode != null) {
				Map<String, Object> prodInfo = new HashMap<String, Object>();
				Image prodImage = productNode.getProdImage();
				
				if(prodImage!=null) {
				prodInfo.put("imageUri", prodImage.getPathWithPublishId());
				prodInfo.put("imageH", prodImage.getHeight());
				prodInfo.put("imageW", prodImage.getWidth());
				}
				
				/* email needs separate brand name and product name */
				String brandName = productNode.getPrimaryBrandName();
				String nameNoBrand = productNode.getFullName();
				if (!"".equals(brandName)) {
					nameNoBrand = (nameNoBrand.substring(brandName.length())).trim();
				}
				prodInfo.put("brandName", brandName);
				prodInfo.put("nameNoBrand", nameNoBrand);
				
				//don't use just a number as a key, xml doesn't like that
				prodInfos.put("id"+cartLine.getCartlineId(), prodInfo);
			} else{
				Map<String, Object> prodInfo = new HashMap<String, Object>();
				prodInfo.put("nameNoBrand", cartLine.getDescription());
				
				//don't use just a number as a key, xml doesn't like that
				prodInfos.put("id"+cartLine.getCartlineId(), prodInfo);				
			}
		}
		
		map.put("prodInfos", prodInfos);	
	}
	

}
