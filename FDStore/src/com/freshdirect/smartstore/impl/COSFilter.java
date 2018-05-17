package com.freshdirect.smartstore.impl;

import java.util.Collections;
import java.util.List;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.WrapperRecommendationService;

public class COSFilter extends WrapperRecommendationService {

    String  cosFilter;
    boolean corporate;

    public COSFilter(RecommendationService internal, String filterValue) {
        super(internal);
        if (filterValue == null) {
            throw new NullPointerException("cosFilterValue");
        }
        this.cosFilter = filterValue;
        this.corporate = cosFilter.equalsIgnoreCase(EnumServiceType.CORPORATE.getName());
    }

    public List<ContentNodeModel> recommendNodes(SessionInput input) {
        EnumServiceType customerServiceType = input.getCustomerServiceType();

        if (corporate && EnumServiceType.CORPORATE.equals(customerServiceType)) {
            return internal.recommendNodes(input);
        } else if (!corporate && !EnumServiceType.CORPORATE.equals(customerServiceType)) {
            return internal.recommendNodes(input);
        } else {
            return Collections.emptyList();
        }
    }

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[internalRecommender=" + internal + ",cosFilter=" + cosFilter + "]";
	}
}
