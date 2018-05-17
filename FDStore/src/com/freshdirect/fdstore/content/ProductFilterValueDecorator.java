package com.freshdirect.fdstore.content;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.fdstore.FDContentTypes;
import com.freshdirect.content.nutrition.EnumClaimValue;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.content.customerrating.CustomerRatingsContext;
import com.freshdirect.fdstore.content.customerrating.CustomerRatingsDTO;
import com.freshdirect.fdstore.pricing.ProductModelPricingAdapter;
import com.freshdirect.framework.util.log.LoggerFactory;

public class ProductFilterValueDecorator extends GenericFilterDecorator<FilteringSortingItem<ProductModel>> {
	
	private static final Logger LOG = LoggerFactory.getInstance( ProductFilterValueDecorator.class );

	public ProductFilterValueDecorator(Set<FilteringValue> filters) {
		super(filters);
	}

	public void decorateItem(FilteringSortingItem<ProductModel> item) {

		ProductModelPricingAdapter node = (ProductModelPricingAdapter) item.getNode();

		List<ProductModel> parents = collectParents(node);
		
		boolean available = node.isFullyAvailable();
		try {
			for (FilteringValue filterSource : filters) {
				
				if(!(filterSource instanceof EnumSearchFilteringValue)){
					throw new IllegalArgumentException("Only EnumSearchFilteringValue allowed here.");
				}
				
				EnumSearchFilteringValue filter = (EnumSearchFilteringValue) filterSource;
				
				FilteringMenuItem menu = new FilteringMenuItem();
				Set<FilteringMenuItem> menus = new HashSet<FilteringMenuItem>();
				
				switch (filter) {
					case DEPT: {
						Set<String> parentIds = new HashSet<String>();
						for (ProductModel parent : parents) {
							
							//prepare the filterValues
							parentIds.add(parent.getDepartment().getContentKey().getId());
							
							//prepare the menus
							menu.setName(parent.getDepartment().getFullName());
							menu.setFilteringUrlValue(parent.getDepartment().getContentKey().getId());
							menu.setFilter(filter);
							menus.add(menu);
							menu = new FilteringMenuItem();
						}
						
						item.putFilteringValue(EnumSearchFilteringValue.DEPT, parentIds);
						item.putMenuValue(EnumSearchFilteringValue.DEPT, menus);
						break;
					}
					case CAT: {
						Set<String> parentIds = new HashSet<String>();
						for (ProductModel parent : parents) {
							ContentNodeModel parentModel = parent.getParentNode();
							ContentNodeModel found = null;
							while (parentModel != null && !FDContentTypes.STORE.equals(parentModel.getContentKey().getType())) {
								if (parentModel.getParentNode() != null &&
										FDContentTypes.DEPARTMENT.equals(parentModel.getParentNode().getContentKey().getType())) {
									found = parentModel;
									break;
								}
								parentModel = parentModel.getParentNode();
							}
							if (found != null){
								
								parentIds.add(found.getContentKey().getId());
								
								//prepare the menus
								menu.setName(found.getFullName());
								menu.setFilteringUrlValue(found.getContentKey().getId());
								menu.setFilter(filter);
								menus.add(menu);
								menu = new FilteringMenuItem();
							}
						}
	
						item.putFilteringValue(EnumSearchFilteringValue.CAT, parentIds);
						item.putMenuValue(EnumSearchFilteringValue.CAT, menus);
						break;
					}
					case SUBCAT: {
						Set<String> parentIds = new HashSet<String>();
						for (ProductModel parent : parents) {
							ContentNodeModel parentModel = parent.getParentNode();
							ContentNodeModel found = null;
							while (parentModel != null && !FDContentTypes.STORE.equals(parentModel.getContentKey().getType())) {
								if (parentModel.getParentNode() != null &&
										FDContentTypes.CATEGORY.equals(parentModel.getParentNode().getContentKey().getType())) {
									if (parentModel.getParentNode().getParentNode() != null &&
											FDContentTypes.DEPARTMENT.equals(parentModel.getParentNode().getParentNode().getContentKey().getType())) {
										found = parentModel;
										break;
									}
								}
								parentModel = parentModel.getParentNode();
							}
							if (found != null){
								
								parentIds.add(found.getContentKey().getId());

								//prepare the menus
								menu.setName(found.getFullName());
								menu.setFilteringUrlValue(found.getContentKey().getId());
								menu.setFilter(filter);
								menus.add(menu);
								menu = new FilteringMenuItem();
							}
						}
						item.putFilteringValue(EnumSearchFilteringValue.SUBCAT, parentIds);
						item.putMenuValue(EnumSearchFilteringValue.SUBCAT, menus);
						break;
					}
					case BRAND: {
						Set<String> bk = new HashSet<String>();
						for (BrandModel brand : node.getBrands()) {
							bk.add(brand.getContentKey().getId());
							
							//prepare the menus
							menu.setName(brand.getFullName());
							menu.setFilteringUrlValue(brand.getContentKey().getId());
							menu.setFilter(filter);
							menus.add(menu);
							menu = new FilteringMenuItem();
						}
						item.putFilteringValue(EnumSearchFilteringValue.BRAND, bk);
						item.putMenuValue(EnumSearchFilteringValue.BRAND, menus);
						break;
					}
					case EXPERT_RATING: {
						if (available) {
							item.putFilteringValue(EnumSearchFilteringValue.EXPERT_RATING,(node.getProductRatingEnum().getValue() + 1) / 2 + "");
							
							if (node.getProductRatingEnum().getValue() != 0) {
								if (node.getProductRating() != null) {
									menu.setName((node.getProductRatingEnum().getValue() + 1) / 2 + "");
									menu.setFilteringUrlValue((node.getProductRatingEnum().getValue() + 1) / 2 + "");
									menu.setFilter(filter);
									menus.add(menu);
									item.putMenuValue(EnumSearchFilteringValue.EXPERT_RATING, menus);
								}
							}
						}
						break;
					}
					case CUSTOMER_RATING: {
						if (available) {
							CustomerRatingsDTO customerRatingsDTO = CustomerRatingsContext.getInstance().getCustomerRatingByProductId(node.getContentKey().getId());
							if (customerRatingsDTO != null && (node.getProductRating() == null || node.getProductRatingEnum().getValue() == 0)) {
								
								BigDecimal averageRating = customerRatingsDTO.getAverageOverallRating();
								int starValue = (int) Math.ceil(averageRating.doubleValue());
								
								item.putFilteringValue(EnumSearchFilteringValue.CUSTOMER_RATING,starValue + "");
								
								//prepare the menus
								menu.setName(customerRatingsDTO.getRatingValue() + "");
								menu.setFilteringUrlValue(customerRatingsDTO.getRatingValue() + "");
								menu.setFilter(filter);
								menus.add(menu);
								item.putMenuValue(EnumSearchFilteringValue.CUSTOMER_RATING, menus);
							}
						}
						break;
					}
					case ON_SALE: {
						if (available) {
							PriceCalculator pricing = node.getPriceCalculator();
							if (pricing.getDealPercentage() > 0 || pricing.getTieredDealPercentage() > 0 || pricing.getGroupPrice() != 0.0) {
								item.putFilteringValue(EnumSearchFilteringValue.ON_SALE, "1");
								
								//prepare the menus
								menu.setName("On Sale");
								menu.setFilteringUrlValue("1");
								menu.setFilter(filter);
								menus.add(menu);
								item.putMenuValue(EnumSearchFilteringValue.ON_SALE, menus);
							} else {
								item.putFilteringValue(EnumSearchFilteringValue.ON_SALE, "0");
							}
						}
						break;
					}
					case NEW_OR_BACK: {
						if (available) {
							if (node.isBackInStock()) {
								item.putFilteringValue(EnumSearchFilteringValue.NEW_OR_BACK, "2");
								
								menu.setName("Back in stock");
								menu.setFilteringUrlValue("2");
								menu.setFilter(filter);
								menus.add(menu);
								item.putMenuValue(EnumSearchFilteringValue.NEW_OR_BACK, menus);
							} else if (node.isNew()) {
								item.putFilteringValue(EnumSearchFilteringValue.NEW_OR_BACK, "1");
								
								menu.setName("New");
								menu.setFilteringUrlValue("1");
								menu.setFilter(filter);
								menus.add(menu);
								item.putMenuValue(EnumSearchFilteringValue.NEW_OR_BACK, menus);
							} else {
								item.putFilteringValue(EnumSearchFilteringValue.NEW_OR_BACK, "0");
							}
						}
						break;
					}
					case KOSHER: {
						if (available) {
							if (node.getPriceCalculator().getKosherPriority() != 999 && node.getPriceCalculator().getKosherPriority() != 0) {
								item.putFilteringValue(EnumSearchFilteringValue.KOSHER, "1");
								
								menu.setName("Kosher");
								menu.setFilteringUrlValue("1");
								menu.setFilter(filter);
								menus.add(menu);
								item.putMenuValue(EnumSearchFilteringValue.KOSHER, menus);
							}
						}
						break;
					}
					case GLUTEN_FREE: {
						if (available) {
							item.putFilteringValue(EnumSearchFilteringValue.GLUTEN_FREE, "0");
							if (node.getPriceCalculator().getProduct().getClaims() != null) {
								for (EnumClaimValue claim : node.getPriceCalculator().getProduct().getClaims()) {
									if ("FR_GLUT".equals(claim.getCode())) {
										item.putFilteringValue(EnumSearchFilteringValue.GLUTEN_FREE, "1");
										
										menu.setName("Gluten free");
										menu.setFilteringUrlValue("1");
										menu.setFilter(filter);
										menus.add(menu);
										item.putMenuValue(EnumSearchFilteringValue.GLUTEN_FREE, menus);
									}
								}
							}
						}
					}
				}
			}
		} catch (FDResourceException e) {
			LOG.error( "Error while decorating product", e );
		} catch (FDSkuNotFoundException e) {
			LOG.error( "Error while decorating product", e );
		}

	}

	private List<ProductModel> collectParents(ProductModelPricingAdapter node) {

		List<ProductModel> parentNodes = new ArrayList<ProductModel>();

		Collection<ContentKey> parents = node.getParentKeys();
		if (parents != null) {
			for (ContentKey parentKey : parents) {
				ContentNodeModel contentNodeModel = ContentFactory.getInstance().getContentNodeByKey(parentKey);
				boolean hideFilteringCategory = false;
				while (contentNodeModel != null && "c".equalsIgnoreCase(contentNodeModel.getContentType()) && !hideFilteringCategory) {
					if(((CategoryModel)contentNodeModel).isHideIfFilteringIsSupported() && nav.isFilteringSupportedForUser()) {
						hideFilteringCategory = true;
					} else {
						contentNodeModel = contentNodeModel.getParentNode();
					}
				}
				if(hideFilteringCategory) {
					continue;
				}
				ProductModel nodeByKey = ContentFactory.getInstance().getProductByName(
						parentKey.getId(),
						node.getContentKey().getId());
				if (nodeByKey != null && nodeByKey.isDisplayableBasedOnCms() && nodeByKey.isSearchable()) {
					parentNodes.add(nodeByKey);
				}
			}
		}

		return parentNodes;
	}

}
