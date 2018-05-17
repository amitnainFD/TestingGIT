package com.freshdirect.fdstore.content;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.util.FilteringNavigator;
import com.freshdirect.framework.util.log.LoggerFactory;

public abstract class FilteringFlow<N extends ContentNodeModel> {

	@SuppressWarnings( "unused" )
	private final static Logger LOG = LoggerFactory.getInstance(FilteringFlow.class);

	public FilteringFlowResult<N> doFlow(FilteringNavigator nav, List<FilteringSortingItem<N>> items) {
		
		// Get & pre-process the items - implemented by subclass
		if(items==null){
			items = getItems();			
		}
		preProcess(items);

		// Decorate all items (filter values and menu values)
		GenericFilterDecorator<FilteringSortingItem<N>> filterValueDecorator = createFilterValueDecorator();
		filterValueDecorator.setNav(nav);
		for (FilteringSortingItem<N> item : items) {
			filterValueDecorator.decorateItem(item);
		}

		// Prepare the filters
		Map<FilteringValue, List<Object>> filterValues = nav.getFilterValues();
		if (filterValues == null) {
			filterValues = new HashMap<FilteringValue, List<Object>>();
		}

		GenericFilteringMenuBuilder<FilteringSortingItem<N>> menuBuilder = createMenuBuilder(filterValues);

		// Filter the items and build the menu domains where needed
		GenericFilter<FilteringSortingItem<N>> filter = createFilter(filterValues);
		filter.applyFilters(items, menuBuilder, this);

		// Sort the remaining results
		sortItems(items);
		// Special re-sorting for favourites
		if (FDStoreProperties.isFavouritesTopNumberFilterSwitchedOn() && nav.getSortBy()!=null && nav.getSortBy().equals(SearchSortType.BY_RELEVANCY)) {
			items = reOrganizeFavourites(items);
		}

		// Build the rest of the menu
		menuBuilder.buildMenu(items);

		// Post-process items
		postProcess(items, menuBuilder);
		
		return new FilteringFlowResult<N>(items, menuBuilder.getDomains());
	}
	
	public void sortItems(List<FilteringSortingItem<N>> items){
		Comparator<FilteringSortingItem<N>> comparator = createComparator(items);
		if (comparator != null) {
			Collections.sort(items, comparator);
		}
	}

	protected abstract GenericFilterDecorator<FilteringSortingItem<N>> createFilterValueDecorator();

	protected abstract GenericFilterDecorator<FilteringSortingItem<N>> createMenuValueDecorator();

	protected abstract Comparator<FilteringSortingItem<N>> createComparator(List<FilteringSortingItem<N>> items);

	protected abstract List<FilteringSortingItem<N>> reOrganizeFavourites(List<FilteringSortingItem<N>> items);

	protected abstract List<FilteringSortingItem<N>> getItems();

	protected abstract Set<FilteringValue> getFilterEnums();

	protected abstract void postProcess(List<FilteringSortingItem<N>> items, GenericFilteringMenuBuilder<FilteringSortingItem<N>> menuBuilder);
	
	protected abstract void midProcess(List<FilteringSortingItem<N>> items);

	protected abstract void preProcess(List<FilteringSortingItem<N>> items);

	protected abstract Set<FilteringValue> getFilters();

	protected GenericFilteringMenuBuilder<FilteringSortingItem<N>> createMenuBuilder(Map<FilteringValue, List<Object>> filterValues) {
		return new FilteringSortingMenuBuilder<N>(filterValues, getFilters());
	}

	protected GenericFilter<FilteringSortingItem<N>> createFilter(Map<FilteringValue, List<Object>> filterValues) {
		return new FilteringSortingItemFilter<N>(filterValues, getFilters());
	}

	protected GenericFilterValueDecoder createDecoder() {
		return new UrlFilterValueDecoder(getFilters());
	}

}
