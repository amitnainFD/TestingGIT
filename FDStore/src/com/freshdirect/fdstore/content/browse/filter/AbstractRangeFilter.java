package com.freshdirect.fdstore.content.browse.filter;

import com.freshdirect.fdstore.content.AbstractProductItemFilter;
import com.freshdirect.fdstore.content.ProductFilterModel;

public abstract class AbstractRangeFilter extends AbstractProductItemFilter {
	/**
	 * Lower bound of range (inclusive)
	 */
	protected Double lowerBound;
	/**
	 * Upper bound of range (inclusive)
	 */
	protected Double upperBound;
	
	public AbstractRangeFilter(ProductFilterModel model, String parentId) {
		super(model, parentId);
		
		// TODO: check if order of lower and upper bound is not mixed
		lowerBound = model.getFromValue();
		upperBound = model.getToValue();
	}
	
	protected boolean isWithinRange(final double value) {
		// sort out extreme case
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return false;
		}

		if (lowerBound != null) {
			// lower != null
			if (upperBound != null) {
				return lowerBound <= value && value <= upperBound;
			} else {
				return lowerBound <= value;
			}
		} else {
			// lower = null
			if (upperBound != null) {
				return value <= upperBound;
			} else {
				// ordinary real numbers always fall between negative infinity and positive infinity
				return true;
			}
		}
	}
}
