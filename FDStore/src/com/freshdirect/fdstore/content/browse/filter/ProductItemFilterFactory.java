package com.freshdirect.fdstore.content.browse.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.AbstractProductItemFilter;
import com.freshdirect.fdstore.content.AndFilter;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.FilterCacheStrategy;
import com.freshdirect.fdstore.content.FilteringProductItem;
import com.freshdirect.fdstore.content.OrFilter;
import com.freshdirect.fdstore.content.ProductContainer;
import com.freshdirect.fdstore.content.ProductFilterGroupI;
import com.freshdirect.fdstore.content.ProductFilterGroupImpl;
import com.freshdirect.fdstore.content.ProductFilterGroupModel;
import com.freshdirect.fdstore.content.ProductFilterModel;
import com.freshdirect.fdstore.content.ProductFilterMultiGroupModel;
import com.freshdirect.fdstore.content.ProductFilterType;
import com.freshdirect.fdstore.content.ProductItemFilterI;
import com.freshdirect.fdstore.content.TagModel;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.framework.util.log.LoggerFactory;

public class ProductItemFilterFactory {
	
	private static final Logger LOGGER = LoggerFactory.getInstance(ProductItemFilterFactory.class);
	
	private static ProductItemFilterFactory instance;
	
	public static ProductItemFilterFactory getInstance(){
		
		if(instance==null){
			instance = new ProductItemFilterFactory();
		}
		
		return instance;
	}

	private static final ProductItemFilterI NULL_FILTER = new AbstractProductItemFilter(){
		@Override
		public boolean apply(FilteringProductItem prod) {
			return true;
		}
		
		@Override
		public FilterCacheStrategy getCacheStrategy() {
			return FilterCacheStrategy.CMS_ONLY;
		}
	};
	
	/**
	 * Transforms a model to a concrete filter implementation
	 * 
	 * @param filterModel A model object containing necessary filter configuration
	 * @param parentId
	 * @return Filter instance
	 */
	public ProductItemFilterI getProductFilter(ProductFilterModel filterModel, String parentId, FDUserI user){
		
		switch (ProductFilterType.toEnum(filterModel.getType())){
		
		case AND:
			return new AndFilter(filterModel.getContentName(), parentId, filterModel.getName(), filterModel.isInvert(), createInnerFilters(filterModel, user));
		
		case OR:
			return new OrFilter(filterModel.getContentName(), parentId, filterModel.getName(), filterModel.isInvert(), createInnerFilters(filterModel, user));
		
		case ALLERGEN:
			return new AllergenFilter(filterModel, parentId);
			
		case BACK_IN_STOCK:
			return new BackInStockFilter(filterModel, parentId);
			
		case BRAND:
			return new BrandFilter(filterModel, parentId);
			
		case CLAIM:
			return new ErpsyClaimFilter(filterModel, parentId);
		
		case CUSTOMER_RATING:
			return new CustomerRatingFilter(filterModel, parentId);

		case DOMAIN_VALUE:
			return new DomainValueFilter(filterModel, parentId);

		case EXPERT_RATING:
			return new ExpertRatingFilter(filterModel, parentId);
			
		case FRESHNESS:
			if (FDStoreProperties.IsFreshnessGuaranteedEnabled()) {
				return new FreshnessFilter(filterModel, parentId);
			} else {
				return NULL_FILTER;
			}

		case KOSHER:
			return new KosherFilter(filterModel, parentId);
					
		case NEW:
			return new NewProductFilter(filterModel, parentId);

		case NUTRITION:
			return new NutritionFilter(filterModel, parentId);

		case ON_SALE:
			return new OnSaleFilter(filterModel, parentId);

		case ORGANIC:
			return new OrganicFilter(filterModel, parentId);

		case PRICE:
			return new PriceFilter(filterModel, parentId, user);
		
		case SUSTAINABILITY_RATING:
			return new SustainabilityRatingFilter(filterModel, parentId,user);
			
		case TAG:
			return new TagFilter(filterModel, parentId);


		default:
			LOGGER.error("type not found, returning always true filter");
			return NULL_FILTER;
		}		
	}
	
	private List<ProductItemFilterI> createInnerFilters(ProductFilterModel filterModel, FDUserI user){
		
		List<ProductItemFilterI> innerFilters = new ArrayList<ProductItemFilterI>();
		
		for(ProductFilterModel innerFilter : filterModel.getFilters()){
			innerFilters.add(getProductFilter(innerFilter, filterModel.getContentName(), user));
		}
		
		return innerFilters;
	}
	
	public ProductFilterGroupI getProductFilterGroup(ProductFilterGroupModel groupModel, FDUserI user){
		
		List<ProductItemFilterI> productFilters = new ArrayList<ProductItemFilterI>();
		
		String contentName = groupModel.getContentName();
		for (ProductFilterModel filter : groupModel.getProductFilterModels()) {
			productFilters.add(getProductFilter(filter, contentName, user));
		}

		ProductFilterGroupImpl group = new ProductFilterGroupImpl();
		group.setProductFilters(productFilters);
		group.setId(groupModel.getContentName());
		group.setName(groupModel.getName());
		group.setType(groupModel.getType());
		group.setAllSelectedLabel(groupModel.getAllSelectedLabel());
		group.setDisplayOnCategoryListingPage(groupModel.isDisplayOnCategoryListingPage());
		return group;
	}
	
	
	public List<ProductFilterGroupI> getProductFilterGroups(ProductFilterMultiGroupModel multiGroupModel, List<TagModel> selection){
		List<ProductFilterGroupI> list = new ArrayList<ProductFilterGroupI>();
		
		//level 1
		ProductFilterGroupImpl l1 = new ProductFilterGroupImpl();
		l1.setName(multiGroupModel.getLevel1Name());
		l1.setType(multiGroupModel.getLevel1Type());
		l1.setAllSelectedLabel(multiGroupModel.getLevel1AllSelectedLabel());
		l1.setId(multiGroupModel.getContentName()+"_l1");
		l1.setMultiGroupModel(true);
		
		l1.setProductFilters(getProductFilters(multiGroupModel.getRootTag(), l1.getId()));
		list.add(l1);

		String l2Name = multiGroupModel.getLevel2Name();
		if (l2Name != null && l2Name.length()>0){
			
			//level 2		
			ProductFilterGroupImpl l2 = new ProductFilterGroupImpl();
			l2.setName(l2Name);
			l2.setType(multiGroupModel.getLevel2Type());
			l2.setAllSelectedLabel(multiGroupModel.getLevel2AllSelectedLabel());
			l2.setId(multiGroupModel.getContentName()+"_l2");
			l2.setMultiGroupModel(true);
			
			if (selection==null || selection.size()<1){
				l2.setProductFilters(Collections.<ProductItemFilterI>emptyList());
			} else {
				l2.setProductFilters(getProductFilters(selection.get(0), l2.getId()));
				
			}
			list.add(l2);
		}
		return list;
	}
	
	/**
	 * @param tag
	 * @param parentId
	 * @return
	 * 
	 * create tag filters from the given tagmodels
	 */
	public List<ProductItemFilterI> getProductFilters(TagModel tag, String parentId){
		List<ProductItemFilterI> list = new ArrayList<ProductItemFilterI>();
		
		if (tag!=null){
			List<TagModel> children = tag.getChildren();
			if (children != null){
				for (TagModel child : children){
					list.add(new TagFilter(child, parentId));
				}
			}
		}
		return list;
	}
	
	/**
	 * @param productContainer
	 * @param selectionMap
	 * @return
	 * 
	 * create a flat group hierarchy, 
	 * create simple groups from the multigroups and create the x level for the selected multigroup filters (selectionMap)
	 */
	public List<ProductFilterGroupI> getDefaultProductFilterGroups(ProductContainer productContainer, Map<String, List<TagModel>> selectionMap, FDUserI user){
		
		List<ProductFilterGroupI> list = new ArrayList<ProductFilterGroupI>();
		
		for (ContentNodeModel item : productContainer.getProductFilterGroups()){
			if (item instanceof ProductFilterGroupModel){
				list.add(getProductFilterGroup((ProductFilterGroupModel)item, user));
			
			} else if (item instanceof ProductFilterMultiGroupModel){
				list.addAll(getProductFilterGroups((ProductFilterMultiGroupModel)item, selectionMap.get(item.getContentName())));
			}
		}
		
		return list;
	}

}
