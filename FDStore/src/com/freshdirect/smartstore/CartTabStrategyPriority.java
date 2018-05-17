package com.freshdirect.smartstore;

import java.io.Serializable;

public class CartTabStrategyPriority implements Comparable<CartTabStrategyPriority>, Serializable {
	
	private static final long serialVersionUID = -7596346515085057189L;

	private String strategy;
	private String siteFeature;
	private int primaryPriority;
	private int secondaryPriority;
	
	public CartTabStrategyPriority(String strategy, String siteFeature,
			int primaryPriority, int secondaryPriority) {
		if (strategy == null)
			throw new IllegalArgumentException("strategy must not be null");
		if (siteFeature == null)
			throw new IllegalArgumentException("site feature must not be null");
		
		this.strategy = strategy;
		this.siteFeature = siteFeature;
		this.primaryPriority = primaryPriority;
		this.secondaryPriority = secondaryPriority;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((siteFeature == null) ? 0 : siteFeature.hashCode());
		result = prime * result
				+ ((strategy == null) ? 0 : strategy.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CartTabStrategyPriority other = (CartTabStrategyPriority) obj;
		if (siteFeature == null) {
			if (other.siteFeature != null)
				return false;
		} else if (!siteFeature.equals(other.siteFeature))
			return false;
		if (strategy == null) {
			if (other.strategy != null)
				return false;
		} else if (!strategy.equals(other.strategy))
			return false;
		return true;
	}

	public int compareTo(CartTabStrategyPriority p) {
		if (primaryPriority != p.primaryPriority)
			return primaryPriority - p.primaryPriority;
		else if (secondaryPriority != p.secondaryPriority)
			return secondaryPriority - p.secondaryPriority;

		int c = siteFeature.compareTo(p.siteFeature);
		if (c != 0)
			return c;
		
		return strategy.compareTo(p.strategy);
	}
	
	public String toString() {
		return "CartTabStrategyPriority[" + strategy + "," + siteFeature + ","
				+ primaryPriority + "," + secondaryPriority + "]";
	}

	public String getStrategyId() {
		return strategy;
	}

	public String getSiteFeatureId() {
		return siteFeature;
	}

	public int getPrimaryPriority() {
		return primaryPriority;
	}

	public int getSecondaryPriority() {
		return secondaryPriority;
	}
}
