package com.freshdirect.smartstore.external;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class ExternalRecommenderRequest implements Serializable {
	private static final long serialVersionUID = 2821967296889574402L;

	private String customerId;

	private List<RecommendationItem> items;

	public ExternalRecommenderRequest(String customerId) {
		super();
		this.customerId = customerId;
	}

	public ExternalRecommenderRequest(List<RecommendationItem> items) {
		super();
		this.items = Collections.unmodifiableList(items);
	}

	public ExternalRecommenderRequest(String customerId, List<RecommendationItem> items) {
		super();
		this.customerId = customerId;
		this.items = Collections.unmodifiableList(items);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((customerId == null) ? 0 : customerId.hashCode());
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ExternalRecommenderRequest other = (ExternalRecommenderRequest) obj;
		if (customerId == null) {
			if (other.customerId != null)
				return false;
		} else if (!customerId.equals(other.customerId))
			return false;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		return true;
	}

	public String getCustomerId() {
		return customerId;
	}

	public List<RecommendationItem> getItems() {
		return items;
	}

	/**
	 * Checks whether the request can be used as a cache key.
	 * 
	 * @return
	 */
	public boolean isCacheable() {
		return customerId != null && customerId.length() != 0 || items != null && !items.isEmpty();
	}
}
