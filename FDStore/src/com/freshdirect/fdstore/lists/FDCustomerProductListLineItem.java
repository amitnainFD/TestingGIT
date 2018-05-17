/**
 * @author ekracoff
 * Created on Oct 1, 2004*/

package com.freshdirect.fdstore.lists;

import com.freshdirect.common.context.UserContext;
import com.freshdirect.fdstore.FDCachedFactory;
import com.freshdirect.fdstore.FDConfiguration;
import com.freshdirect.fdstore.FDProductInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSku;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.FDInvalidConfigurationException;
import com.freshdirect.fdstore.customer.FDProductSelection;
import com.freshdirect.fdstore.customer.FDProductSelectionI;
import com.freshdirect.fdstore.customer.OrderLineUtil;
import com.freshdirect.framework.util.NVL;

public class FDCustomerProductListLineItem extends FDCustomerListItem {
	
	private static final long serialVersionUID = -3141592912506538031L;
	
	private FDConfiguration configuration; // may not be null for a valid object
	private String skuCode; // may not be null for a valid object
	private String recipeSourceId; // null if not recipe source
	
	private transient ProductModel cachedProduct;

	public boolean equals(Object o) {
		if (!(o instanceof FDCustomerProductListLineItem))
			return false;
		FDCustomerProductListLineItem li = (FDCustomerProductListLineItem) o;
		return NVL.nullEquals(skuCode, li.skuCode)
				&& NVL.nullEquals(recipeSourceId, li.recipeSourceId)
				&& NVL.nullEquals(configuration, li.configuration);
	}
	
	public int hashCode() {
		return skuCode.hashCode() ^ configuration.hashCode();
	}
	
	public FDCustomerProductListLineItem(String skuCode, FDConfiguration configuration, String recipeSourceId) {
		this.skuCode = skuCode;
		this.configuration = configuration;
		this.recipeSourceId = recipeSourceId;
	}
	
	public FDCustomerProductListLineItem(String skuCode, FDConfiguration configuration) {
		this(skuCode,configuration,null);
	}
	
	
	
	public FDCustomerProductListLineItem(FDProductSelectionI selection) {
		this(selection.getSkuCode(), new FDConfiguration(selection.getConfiguration()), selection.getRecipeSourceId());
	}
	
	/**
	 * @return Returns the selection.
	 */
	public FDConfiguration getConfiguration() {
		return configuration;
	}
	/**
	 * @param selection The selection to set.
	 */
	public void setConfiguration(FDConfiguration configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * 
	 * @param recipeSourceId
	 */
	public void setRecipeSourceId(String recipeSourceId) {
		this.recipeSourceId = recipeSourceId;
	}
	
	/**
	 * @return recipe id, or null
	 */
	public String getRecipeSourceId() {
		return recipeSourceId;
	}
	
	/**
	 * @return Returns the skuCode.
	 */
	public String getSkuCode() {
		return skuCode;
	}


	/**
	 * @param skuCode The skuCode to set.
	 */
	public void setSkuCode(String skuCode) {
		this.skuCode = skuCode;
		this.cachedProduct = null;
	}

	/**
	 * @return ProductModel for this item (never null)
	 * 
	 * @throws FDSkuNotFoundException if the sku is not set, or does not exist
	 */
	public ProductModel getProduct() throws FDSkuNotFoundException {
		if (getSkuCode() == null) {
			throw new FDSkuNotFoundException("SKU code not set");
		}
		
		if (cachedProduct == null) {
			cachedProduct = ContentFactory.getInstance().getProduct(getSkuCode());
		}

		return cachedProduct; 
	}
	 
	public UserContext getUserContext() {
		return ContentFactory.getInstance().getCurrentUserContext();
	}

	public FDProductSelectionI convertToSelection() throws IllegalStateException, FDSkuNotFoundException, FDResourceException{
		FDProductInfo prodInfo = FDCachedFactory.getProductInfo(this.getSkuCode());
		ProductModel prod = getProduct();
		
		FDProductSelection r = new  FDProductSelection(new FDSku(prodInfo.getSkuCode(), prodInfo.getVersion()),
									  prod,
									  this.configuration, getUserContext());
		
		r.setCustomerListLineId(this.getPK() == null ? null : this.getPK().getId());
		
		try{
			OrderLineUtil.cleanup(r);
			r.setStatistics(this);
			r.setRecipeSourceId(recipeSourceId);
			OrderLineUtil.describe(r);
		} catch (FDInvalidConfigurationException e) {
			r.setInvalidConfig(true);
			r.setStatistics(this);
			r.setDescription(prod.getFullName());
			r.setDepartmentDesc(prod.getDepartment().getFullName());
			r.setConfigurationDesc("");
		}	

		return r;
	}
	
	
	//
	// convenience getter methods
	//

	/**
	 * Get Product full name.
	 * @return product full name
	 */
	public String getFullName() {
		try {
			return getProduct().getFullName();
		} catch (FDSkuNotFoundException e) {
			return "";
		} catch (IllegalStateException e) {
			return "";
		}
	}
	
	public String getCategoryId() {
		try {
			return getProduct().getParentNode().getContentName();
		} catch (FDSkuNotFoundException e) {
			return "";
		}
	}

	public String getProductId() {
		try {
			return getProduct().getContentName();
		} catch (FDSkuNotFoundException e) {
			return "";
		}
	}
	
}
