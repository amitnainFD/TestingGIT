package com.freshdirect.fdstore.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.ContentType;
import com.freshdirect.cms.application.CmsManager;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.Domain;
import com.freshdirect.fdstore.content.DomainValue;
import com.freshdirect.fdstore.content.Recipe;
import com.freshdirect.fdstore.content.RecipeSubcategory;

/**
 * Utilities for recipes filtering. (Refactoring)
 * 
 * @author kocka
 *
 */
public class RecipesUtil {

	private static final String REQUEST_ATTR_FILTERBYKEYS = "com.freshdirect.recipe.filterbykeys";
	private static final String REQUEST_ATTR_RECIPES = "com.freshdirect.recipe.recipes";
	
	/**
	 * Cached 
	 * @param recipeSubCat
	 * @param filter
	 * @param filterByKeys
	 * @param request
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	public static Map<Object,List<Recipe>> findRecipes(RecipeSubcategory recipeSubCat, DomainValue filter, List<DomainValue> filterByKeys, ServletRequest request) {
		if(request.getAttribute(REQUEST_ATTR_FILTERBYKEYS) == null || request.getAttribute(REQUEST_ATTR_RECIPES) == null) {
			Map<Object,List<Recipe>> ret = findRecipes(recipeSubCat, filter, filterByKeys);
			request.setAttribute(REQUEST_ATTR_FILTERBYKEYS, filterByKeys);
			request.setAttribute(REQUEST_ATTR_RECIPES, ret);
			return ret;
		}
		filterByKeys.clear();
		List<DomainValue> cachedFilterBykeys = (List<DomainValue>) request.getAttribute(REQUEST_ATTR_FILTERBYKEYS);
		filterByKeys.addAll(cachedFilterBykeys);
		return (Map<Object,List<Recipe>>) request.getAttribute(REQUEST_ATTR_RECIPES);
	}

	
	/**
	 * Find all {@link Recipe}s in the given {@link RecipeSubcategory} matching the specified (optional) filter criteria.
	 * 
	 * @param recipeSubCat recipe subcategory (never null)
	 * @param filter recipe classification domain value (optional)
	 * @param filterByKeys mutable List of DomainValue (never null). This list gets narrowed to the filters applicable to the recipes found
	 * 
	 * @return Map of DomainValue (classification) -> List of {@link Recipe} (recipes with this classification).
	 * 
	 * @FIXME returned map contains "noGroupBy" string key. use null-key
	 *  instead.
	 */
	public static Map<Object,List<Recipe>> findRecipes(RecipeSubcategory recipeSubCat, DomainValue filter, List<DomainValue> filterByKeys) {
		Map<Object,List<Recipe>> groupByMap = new HashMap<Object,List<Recipe>>();

		// go get all the recipes
		ContentType cType = ContentType.get("Recipe");
		Set<ContentKey> allRecipeKeys = CmsManager.getInstance().getContentKeysByType(cType);

		List<DomainValue> groupBy = recipeSubCat.getGroupBy();

		// List of DomainValue - all classifications (before filtering applied)
		Set<DomainValue> classifications = new HashSet<DomainValue>();

		boolean isFilterInGroup = filter==null ? false : groupBy.contains(filter);
		for ( ContentKey cKey : allRecipeKeys ) {
			Recipe recipe = (Recipe)ContentFactory.getInstance().getContentNode( cKey.getId() );
			List<DomainValue> recipeClassifications = recipe.getClassifications();
			// skip it if recipe not in Subcategories classification?
	        if (!recipe.isActive() || !recipeClassifications.contains(recipeSubCat.getClassification())) {
				continue;
			}
			
			//ok, is it available...
	        if (!recipe.isAvailable()) {
				continue;
			}

			classifications.addAll( recipeClassifications );
			
			// skip it if filter specified and recipe not in filter classification?
			if (filter!=null && !recipeClassifications.contains(filter)) continue;
			

			// place recipe in participating groups... if no groupbys, then make a dummy group entry in the map
			if (groupBy==null || groupBy.size() < 1) {
				List<Recipe> recipeList = groupByMap.get("noGroupBy");
				if (recipeList==null) {
					recipeList=new ArrayList<Recipe>();
					groupByMap.put("noGroupBy",recipeList);
				}
				recipeList.add(recipe);
			} else {
				List<Recipe> recipeList = null;
				boolean inAGroup=false;
				//creative says to let recipe appear in multipe group by's.  if they reconsider, then add inAGroup to loop condition
				for ( DomainValue dvGroupBy : groupBy ) {
					if (isFilterInGroup && !dvGroupBy.equals(filter)){
						if (recipeClassifications.contains(dvGroupBy))	{
							inAGroup=true;
						}
						continue;
					}
					
					if (recipeClassifications.contains(dvGroupBy)) {
					   recipeList = groupByMap.get(dvGroupBy);
					   inAGroup=true;
					   if (recipeList==null) {
						recipeList=new ArrayList<Recipe>();
						groupByMap.put(dvGroupBy,recipeList);
					   }
					   recipeList.add(recipe);
					}

				}
				if (!inAGroup) {
					recipeList = groupByMap.get("Other");
					if (recipeList==null) {
					   recipeList=new ArrayList<Recipe>();
					   groupByMap.put("Other",recipeList);
					}
					
					recipeList.add(recipe);
				}
				
			}
		}
		
		// retain only matching domainvalues
		filterByKeys.retainAll( classifications );
		 
		return groupByMap;
	}

	public static String getFilterParam(ServletRequest request) {
		return request.getParameter("filter")!=null ?  request.getParameter("filter") : "";
	}
	
	public static DomainValue getFilter(List<DomainValue> filterByKeys, ServletRequest request){
		DomainValue filter = null;
		for ( DomainValue dv : filterByKeys ) {
			if (dv.getContentName().equals(getFilterParam(request)) ) { 
				filter = dv;
				break;
			}
		}
		return filter;
	}
	

	/**
	 * Collects classifications from recipes
	 * 
	 * @param excludeDomains
	 * @param includeDomains
	 * @param recipes
	 * 
	 * @return List of {@link DomainValue} classifications
	 */
	public static List<DomainValue> collectClassifications(Set<Domain> excludeDomains, Set<Domain> includeDomains, List<Recipe> recipes) {
		Set<DomainValue> clDomainValues = new HashSet<DomainValue>();
		for ( Recipe recipe : recipes ) {
			List<DomainValue> cls = recipe.getClassifications();
			for ( DomainValue dv : cls ) {
				Domain d = dv.getDomain();
				if (includeDomains.contains(d) && !excludeDomains.contains(d)) {
					clDomainValues.add(dv);
				}
			}
		}
		List<DomainValue> classifications = new ArrayList<DomainValue>(clDomainValues);
		Collections.sort(classifications, new Comparator<DomainValue>() {
			// sort by parent domain name and domain value label
			public int compare(DomainValue dv1, DomainValue dv2) {
				String d1 = dv1.getDomain().getName();
				String d2 = dv2.getDomain().getName();
				int c = d1.compareTo(d2);
				return c == 0 ? dv1.getLabel().compareTo(dv2.getLabel()) : c;
			}
		});
		return classifications;
	}
}
