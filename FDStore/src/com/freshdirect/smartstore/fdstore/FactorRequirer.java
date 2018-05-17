package com.freshdirect.smartstore.fdstore;

import java.util.Collection;

/**
 * This interface is used to provide the factor names which the recommender needs for correct operation.
 * 
 * @author zsombor
 *
 */
public interface FactorRequirer {

    /**
     * Collect needed factors into the buffer
     * @param buffer Collection<String>
     */
    public void collectFactors(Collection<String> buffer);

}
