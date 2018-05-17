package com.freshdirect.fdstore.coremetrics.builder;

import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.coremetrics.CmContext;
import com.freshdirect.fdstore.coremetrics.tagmodel.ProductViewTagModel;


public class ProductViewTagModelBuilder {
	
	private ProductModel productModel;
	private boolean quickbuy;
	
	private ProductViewTagModel model = new ProductViewTagModel();
	
	public void setProductModel(ProductModel productModel) {
		this.productModel = productModel;
	}

	public void setQuickbuy(boolean quickbuy) {
		this.quickbuy = quickbuy;
	}

	public void setVirtualCategoryId(String virtualCategoryId) {
		model.setVirtualCategoryId(virtualCategoryId);
	}

	public ProductViewTagModel buildTagModel()  throws SkipTagException {
		CmContext context = CmContext.getContext();
		
		if (productModel == null) {
			throw new SkipTagException("productModel is null");
		} else {
			
			model.setProductId(productModel.getContentKey().getId());
			model.setProductName(productModel.getFullName());
			model.setCategoryId( context.prefixedCategoryId( productModel.getCategory().getContentKey().getId() ) );
			
			model.getAttributesMaps().put(1, productModel.getDefaultSkuCode());
			if(productModel.getAutoconfiguration()!=null && productModel.getAutoconfiguration().getOptions()!=null && productModel.getAutoconfiguration().getOptions().size()>0){
				model.getAttributesMaps().put(2, productModel.getAutoconfiguration().getOptions().toString());				
			}
			model.getAttributesMaps().put(3, quickbuy ? "quick_buy" : "normal");
			
			return model;
		}
	}
}