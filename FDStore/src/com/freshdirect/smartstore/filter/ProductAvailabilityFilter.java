package com.freshdirect.smartstore.filter;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * check availabilities light version (no available alternatives check)
 * 
 * @author zsombor
 * 
 */
public class ProductAvailabilityFilter extends ContentFilter {

    private static final Logger LOGGER = LoggerFactory.getInstance(ProductAvailabilityFilter.class);

    final boolean showTempUnavailable;

    public ProductAvailabilityFilter(boolean showTempUnavailable) {
        this.showTempUnavailable = showTempUnavailable;
    }

    /**
     * It calls {@link #filter(ProductModel)}.
     * 
     * Not overridable.
     * 
     * @param key
     * @return null if filter applies, <i>key</i> otherwise
     */
    @SuppressWarnings("unchecked")
    final public <X extends ContentNodeModel> X filter(X in) {
        ProductModel model = (ProductModel) in;

        ProductModel product = filterProduct(model);
        if (product != null)
            return (X) product;
        else {
            //LOGGER.debug("not available: " + model);
            return null;
        }
    }

    /**
     * Override this!
     * 
     * @param model
     *            can be null
     * @return null if filter applies, <i>model</i> otherwise
     */
    protected ProductModel filterProduct(ProductModel model) {
        if (model == null || available(model)) {
            return model;
        } else {
            return null;
        }
    }

    /**
     * @param model
     * @return if the product is available and not excluded from recommendations
     */
    final protected boolean available(ProductModel model) {
        return (showTempUnavailable ? model.isTemporaryUnavailableOrAvailable() : model.isFullyAvailable()) && !model.isExcludedRecommendation();
    }
}
