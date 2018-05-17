package com.freshdirect.fdstore.content;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.freshdirect.cms.fdstore.FDContentTypes;

public class FilteringSortingMenuBuilder<N extends ContentNodeModel> extends GenericFilteringMenuBuilder<FilteringSortingItem<N>> {

	public FilteringSortingMenuBuilder(Map<FilteringValue, List<Object>> filterValues, Set<FilteringValue> filters) {
		super(filterValues, new HashSet<FilteringValue>(filters));
	}

	@Override
	public void buildMenu(List<FilteringSortingItem<N>> items) { 
		
		for (FilteringValue value : filters) {
			buildMenuForFilter(items, value, false, false);
		}

		narrowTree(filterValues);
		mergeTempMenus();

	}
	
	@Override
	public void buildMenuForFilter(List<FilteringSortingItem<N>> items, FilteringValue filter, boolean removeFilter, boolean temporary){
		
		Map<String, FilteringMenuItem> domain = new HashMap<String, FilteringMenuItem>();
		boolean isExpertRating = filter == EnumSearchFilteringValue.EXPERT_RATING;			
		boolean isCustRating = filter == EnumSearchFilteringValue.CUSTOMER_RATING;
		
		if ( isExpertRating || isCustRating ) {
			// Always add all expert and customer ratings				
			domain.put( "5", new FilteringMenuItem("5", "5", 0, filter) );
			domain.put( "4", new FilteringMenuItem("4", "4", 0, filter) );
			domain.put( "3", new FilteringMenuItem("3", "3", 0, filter) );
			domain.put( "2", new FilteringMenuItem("2", "2", 0, filter) );
			domain.put( "1", new FilteringMenuItem("1", "1", 0, filter) );
		}
		
		for (FilteringSortingItem<N> item : items) {
			Set<FilteringMenuItem> menuItems = item.getMenuValue(filter);

			if (menuItems != null) {
				for (FilteringMenuItem menuItem : menuItems) {
					
					if(temporary){
						menuItem = menuItem.clone(menuItem);
					}

					String menuName = menuItem.getFilteringUrlValue();
					FilteringMenuItem mI = domain.get(menuName);

					if ( mI == null ) {
						mI = menuItem;
					}
					
					mI.setCounter(mI.getCounter() + 1);							
					domain.put(menuName, mI);
				}
			}
		}
		
		checkSelected(domain.values(), filterValues.get(filter));
		if(temporary){ //temporary menus will be handled at the end of the filtering flow (re calculating numbers etc.)
			tempDomains.put(filter, domain);
		}else{
			domains.put(filter, domain);			
		}
		
		//these are the static empty menuItems (e.g. kosher preference on quickshop)
		if(filter.isShowIfEmpty() && domains.get(filter).isEmpty()){
			
			FilteringMenuItem menu = new FilteringMenuItem();
			menu.setName(filter.getDisplayName());
			menu.setFilteringUrlValue(filter.getName());
			menu.setFilter(filter);
			domains.get(filter).put(menu.getFilteringUrlValue(), menu);
			
		}
		
		if(removeFilter){
			filters.remove(filter);			
		}
	}
	
	private void mergeTempMenus(){
			
		//merge the temporary menu with the final (some domains - e.g departments on quickshop - needs to show 0 valued menuItems after a lower positioned filter - e.g preferences)
		//please note that these are the DYNAMIC empty menuItems!
		for(FilteringValue filter: tempDomains.keySet()){
			
			Map<String, FilteringMenuItem> tempDomain = tempDomains.get(filter);
			
			for(String tempKey: tempDomain.keySet()){
				FilteringMenuItem tempItem = tempDomain.get(tempKey);
				
				if(domains.get(filter)!=null && domains.get(filter).get(tempKey)==null){
					tempItem.setCounter(0);
					domains.get(filter).put(tempKey, tempItem);
				}
			}
		}
	}

	/**
	 * @param fValues
	 * 
	 *            if subcategory or category selected first then domains above
	 *            them (department, category) needs to be narrowed this method
	 *            only needed when multiselection is not supported!
	 *            Only used on search page
	 */
	private void narrowTree(Map<FilteringValue, List<Object>> fValues) {

		String dept = fValues.get(EnumSearchFilteringValue.DEPT) != null ? (String) fValues.get(EnumSearchFilteringValue.DEPT).get(0) : null;
		String cat = fValues.get(EnumSearchFilteringValue.CAT) != null ? (String) fValues.get(EnumSearchFilteringValue.CAT).get(0) : null;
		String subCat = fValues.get(EnumSearchFilteringValue.SUBCAT) != null ? (String) fValues.get(EnumSearchFilteringValue.SUBCAT).get(0) : null;
		String recipe = fValues.get(EnumSearchFilteringValue.RECIPE_CLASSIFICATION) != null ? (String) fValues.get(EnumSearchFilteringValue.RECIPE_CLASSIFICATION).get(0) : null;
		String brand = fValues.get(EnumSearchFilteringValue.BRAND) != null ? (String) fValues.get(EnumSearchFilteringValue.BRAND).get(0) : null;

		if (subCat != null && cat == null) {
			ContentNodeModel subCatModel = ContentFactory.getInstance().getContentNode(FDContentTypes.CATEGORY, subCat);
			cat = subCatModel.getParentNode().getContentKey().getId();
		}

		if (cat != null && dept == null) {
			ContentNodeModel catModel = ContentFactory.getInstance().getContentNode(FDContentTypes.CATEGORY, cat);
			dept = catModel.getParentNode().getContentKey().getId();
		}

		if (subCat != null) {
			narrowDomain(EnumSearchFilteringValue.SUBCAT, subCat, false, null);
		}
		if (cat != null) {
			narrowDomain(EnumSearchFilteringValue.CAT, cat, false, null);
			narrowDomain(EnumSearchFilteringValue.SUBCAT, subCat, true, cat);				
		}
		if (dept != null) {
			narrowDomain(EnumSearchFilteringValue.DEPT, dept, false, null);
			narrowDomain(EnumSearchFilteringValue.CAT, cat, true, dept);
			Map<String, FilteringMenuItem> subCats = domains.get(EnumSearchFilteringValue.SUBCAT);
			// narrow subcat's of department's cats
			if (subCats != null && !subCats.isEmpty() && cat == null) {
				DepartmentModel deptNode = (DepartmentModel) ContentFactory.getInstance().getContentNode(FDContentTypes.DEPARTMENT, dept);
				if (deptNode != null) {
					List<CategoryModel> categories = deptNode.getCategories();
					Set<String> parentCatIds = new HashSet<String>(categories.size());
					for (CategoryModel catNode : categories)
						parentCatIds.add(catNode.getContentKey().getId());
					
					Iterator<Entry<String, FilteringMenuItem>> it = subCats.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, FilteringMenuItem> item = it.next();
						ContentNodeModel itemNode = ContentFactory.getInstance().getContentNode(FDContentTypes.CATEGORY, item.getKey());
						if (itemNode == null || itemNode.getParentNode() == null
								|| !parentCatIds.contains(itemNode.getParentNode().getContentKey().getId()))
							it.remove();
					}
				}
			}
		}
		if(recipe != null ){
			narrowDomain(EnumSearchFilteringValue.RECIPE_CLASSIFICATION, recipe, false, null);
		}
		if(brand != null){
			narrowDomain(EnumSearchFilteringValue.BRAND,brand,false,null);
		}
	}

	private void narrowDomain(FilteringValue domainId, String selected, boolean multiValue, String parent) {

		Map<String, FilteringMenuItem> domain = domains.get(domainId);
		Map<String, FilteringMenuItem> narrowedDomain = new HashMap<String, FilteringMenuItem>();
		
		if(domain != null) {
			if (!multiValue) {
				for (String menuId : domain.keySet()) {
					if (menuId.equals(selected)) {
						narrowedDomain.put(menuId, domain.get(menuId));
						break;
					}
				}
			} else {		
				for (String menuId : domain.keySet()) {
					ContentNodeModel subCatModel = ContentFactory.getInstance().getContentNode(FDContentTypes.CATEGORY, menuId);
					if(subCatModel != null && parent.equals(subCatModel.getParentNode().getContentKey().getId())){
						narrowedDomain.put(menuId, domain.get(menuId));
					}
				}
			}

			domains.put(domainId, narrowedDomain);
			
		}
	}

	private static void checkSelected(Collection<FilteringMenuItem> menuItems, List<Object> itemFilteringValues) {
		if (itemFilteringValues == null) {
			return;
		}
		for (FilteringMenuItem menuItem : menuItems) {
			if (itemFilteringValues.contains(menuItem.getFilteringUrlValue())) {
				menuItem.setSelected(true);
			}
		}
	}

}
