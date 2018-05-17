package com.freshdirect.fdstore.content;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.pricing.ProductPricingFactory;

public class WineFilter implements Serializable, Cloneable {
	private static final long serialVersionUID = 3245612819349391160L;

	private static final String ENUM_WINE_PRICE_PREFIX = "_" + EnumWinePrice.class.getSimpleName();
	private static final String ENUM_WINE_RATING_PREFIX = "_" + EnumWineRating.class.getSimpleName();
	
	private static final ThreadLocal<PricingContext> threadPricingContext = new ThreadLocal<PricingContext>() {
		@Override
		protected PricingContext initialValue() {
			return PricingContext.DEFAULT;
		}
	};
	
	private static final ThreadLocal<Map<ContentKey, Boolean>> availabilityCache = new ThreadLocal<Map<ContentKey,Boolean>>() {
		@Override
		protected java.util.Map<ContentKey,Boolean> initialValue() {
			return new HashMap<ContentKey, Boolean>();
		};
	};
	
	public static void clearAvailabilityCache(PricingContext pricingContext) {
		threadPricingContext.set(pricingContext);
		availabilityCache.get().clear();
	}

	private Set<WineFilterValue> values;
	private Map<String,Set<WineFilterValue>> domains;
	private PricingContext pricingContext;
	
	private transient Set<ProductModel> products;

	public WineFilter(PricingContext pricingContext) {
		super();
		this.pricingContext = pricingContext;
		this.values = new HashSet<WineFilterValue>();
		this.domains = new HashMap<String, Set<WineFilterValue>>();
	}
	
	@Override
	public WineFilter clone() {
		WineFilter wf = new WineFilter(pricingContext);
		for (WineFilterValue value : values)
			wf.addFilterValue(value);
		return wf;
	}

	private void checkDomainValue(DomainValue value) {
		if (!value.getDomain().getContentKey().getId().startsWith("wine_"))
			throw new IllegalArgumentException();
	}
	
	private void addToDomains(WineFilterValue value) {
		String domain = value.getDomainEncoded();
		if (!domains.containsKey(domain))
			domains.put(domain, new HashSet<WineFilterValue>());
		domains.get(domain).add(value);
	}

	private void removeFromDomains(WineFilterValue value) {
		String domain = value.getDomainEncoded();
		if (domains.get(domain) != null) { 
			domains.get(domain).remove(value);
			if (domains.get(domain).isEmpty())
				domains.remove(domain);
		}
	}

	public void addFilterValue(WineFilterValue value) {
		if (value.getWineFilterValueType() == EnumWineFilterValueType.CMS)
			checkDomainValue((DomainValue) value);
		addToDomains(value);
		values.add(value);
		invalidateProductsCache();
	}

	public void removeDomain(EnumWineFilterDomain domain) {
		for (WineFilterValue filter : domain.getFilterValues()) {
			removeFilterValue(filter);
		}
	}
	
	public void removeFilterValue(WineFilterValue value) {
		if (value.getWineFilterValueType() == EnumWineFilterValueType.CMS) {
			checkDomainValue((DomainValue) value);
			Collection<DomainValue> subValues =
					ContentFactory.getInstance().getSubDomainValuesForWineDomainValue((DomainValue) value);
			if (subValues != null) {
				for (DomainValue val : subValues) {
					removeFromDomains(val);
					values.remove(val);
				}
			}
		}
		removeFromDomains(value);
		values.remove(value);
	}
	
	public boolean hasFilterValue(WineFilterValue value) {
		if (value.getWineFilterValueType() == EnumWineFilterValueType.CMS)
			checkDomainValue((DomainValue) value);
		return values.contains(value);
	}
	
	public Set<WineFilterValue> getFilterValuesForDomain(String domainEncoded) {
		Set<WineFilterValue> forDomain = new HashSet<WineFilterValue>();
		for (WineFilterValue value : values)
			if (value.getDomainEncoded().equals(domainEncoded))
				forDomain.add(value);

		return forDomain;
	}

	public Set<WineFilterValue> getFilterValues() {
		return Collections.unmodifiableSet(values);
	}
	
	public boolean isFiltering() {
		return values.size() > 0;
	}
	
	public void invalidateProductsCache() {
		products = null;
	}
	
	public Set<ProductModel> getProducts() {
		if (products != null)
			return products;
		
		if (!pricingContext.equals(threadPricingContext.get())) {
			clearAvailabilityCache(pricingContext);
		}

		Set<ContentKey> keys = new HashSet<ContentKey>(1000);
		if (values.isEmpty()) {
			keys.addAll(ContentFactory.getInstance().getAllWineProductKeys());
		} else {
			boolean isAdded = false;
			for (Map.Entry<String, Set<WineFilterValue>> entry : domains.entrySet()) {
				Set<ContentKey> unionKeys = new HashSet<ContentKey>(1000);
				for (WineFilterValue value : entry.getValue())
					unionKeys.addAll(getProductKeysForDomainValue(value));
	
				if (isAdded)
					keys.retainAll(unionKeys);
				else {
					keys.addAll(unionKeys);
					isAdded = true;
				}
			}
		}
		// availability checking and pricing context wrapping
		Set<ProductModel> products = new HashSet<ProductModel>(keys.size());
		for (ContentKey key : keys) {
			Boolean available = availabilityCache.get().get(key);
			if (available != null) {
				if (available) {
					ProductModel p = (ProductModel) ContentFactory.getInstance().getContentNodeByKey(key);
					p = ProductPricingFactory.getInstance().getPricingAdapter(p, pricingContext);
					if (p != null)
						products.add(p);
				}
			} else {
				ProductModel p = (ProductModel) ContentFactory.getInstance().getContentNodeByKey(key);
				p = ProductPricingFactory.getInstance().getPricingAdapter(p, pricingContext);
				if (p != null) {
					available = p.isFullyAvailable();
					availabilityCache.get().put(key, available);
					if (available)
						products.add(p);
				} else {
					availabilityCache.get().put(key, false);
				}
			}
		}
		return this.products = products;
	}
	
	private Collection<ContentKey> getProductKeysForDomainValue(WineFilterValue value) {
		if (value.getWineFilterValueType() == EnumWineFilterValueType.CMS)
			return ContentFactory.getInstance().getWineProductKeysByDomainValue((DomainValue) value);
		else if (value.getWineFilterValueType() == EnumWineFilterValueType.PRICE)
			return WineFilterPriceIndex.getInstance().get().get(pricingContext).get((EnumWinePrice) value);
		else if (value.getWineFilterValueType() == EnumWineFilterValueType.RATING)
			return WineFilterRatingIndex.getInstance().get().get((EnumWineRating) value);
		else
			throw new IllegalArgumentException("unknown type of wine filter value: " + value.getWineFilterValueType().name());
	}

	public boolean isFilterValueApplicable(WineFilterValue value) {
		Collection<ContentKey> keys = getProductKeysForDomainValue(value);
		Set<ProductModel> products = getProducts();
		for (ProductModel product : products)
			if (keys.contains(product.getContentKey()))
				return true;
		return false;
	}
	
	public boolean hasDomainForValue(WineFilterValue value) {
		return domains.containsKey(value.getDomainEncoded());
	}
	
	public void removeDomainForValue(WineFilterValue value) {
		if (hasDomainForValue(value)) {
			for (WineFilterValue fv : new HashSet<WineFilterValue>(domains.get(value.getDomainEncoded())))
				removeFilterValue(fv);
		}
	}
	
	public String getEncoded() {
		StringBuilder buf = new StringBuilder();
		Iterator<WineFilterValue> it = values.iterator();
		if (it.hasNext())
			buf.append(it.next().getEncoded());
		while (it.hasNext()) {
			buf.append(',');
			buf.append(it.next().getEncoded());
		}
		return buf.toString();
	}
	
	public static WineFilter decode(PricingContext context, String encoded) {
		WineFilter filter = new WineFilter(context);
		for (String item : encoded.split(",")) {
			WineFilterValue value = decodeFilterValue(item);
			if (value != null)
				filter.addFilterValue(value);
		}
		return filter;
	}

	public static WineFilterValue decodeFilterValue(String item) {
		String[] split = item.split(":");
		if (split.length != 2)
			return null;
		if ("DomainValue".equals(split[0])) {
			return (DomainValue) ContentFactory.getInstance().getContentNode("DomainValue", split[1]);
		} else if (ENUM_WINE_PRICE_PREFIX.equals(split[0])) {
			try {
				return EnumWinePrice.valueOf(split[1]);
			} catch (RuntimeException e) {
			}
		} else if (ENUM_WINE_RATING_PREFIX.equals(split[0])) {
			try {
				return EnumWineRating.valueOf(split[1]);
			} catch (RuntimeException e) {
			}
		}
		return null;
	}


	/**
	 * "Clear All" function
	 */
	public void clearAll() {
		this.values.clear();
	}
}
