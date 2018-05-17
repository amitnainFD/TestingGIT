package com.freshdirect.fdstore.content;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeFilterValueDecorator extends GenericFilterDecorator<FilteringSortingItem<Recipe>> {

	public RecipeFilterValueDecorator(Set<FilteringValue> filters) {
		super(filters);
	}

	@Override
	public void decorateItem(FilteringSortingItem<Recipe> item) {
		
		Recipe recipe=item.getNode();
		
		for (FilteringValue filterSource : filters) {
			
			if(!(filterSource instanceof EnumSearchFilteringValue)){
				throw new IllegalArgumentException("Only EnumSearchFilteringValue allowed here.");
			}
			
			EnumSearchFilteringValue filter = (EnumSearchFilteringValue) filterSource;
			
			FilteringMenuItem menu = new FilteringMenuItem();
			Set<FilteringMenuItem> menus = new HashSet<FilteringMenuItem>();
			
			switch (filter) {
			case RECIPE_CLASSIFICATION: {
				
				Set<String> cl=new HashSet<String>();
				RecipeSearchPage recipeSearchPage = RecipeSearchPage.getDefault();
				List<Domain> classificationDomains = recipeSearchPage.getFilterByDomains();
				
				for(DomainValue dv: recipe.getClassifications()){
					cl.add(dv.getContentKey().getId());
					
					if(classificationDomains.contains(dv.getDomain())){
						menu.setName(dv.getLabel());
						menu.setFilteringUrlValue(dv.getContentKey().getId());
						menu.setFilter(filter);
						menus.add(menu);
						menu=new FilteringMenuItem();						
					}
				}
				item.putFilteringValue(filter, cl);
				item.putMenuValue(EnumSearchFilteringValue.RECIPE_CLASSIFICATION, menus);
			}
			}
		}
		
	}

}
