package com.freshdirect.smartstore.external.scarab;

import com.freshdirect.smartstore.external.ExternalRecommender;
import com.scarabresearch.recommendation.api.ScarabService;

public abstract class AbstractScarabExternalRecommender implements ExternalRecommender {
	protected ScarabService service;

	public AbstractScarabExternalRecommender(ScarabService service) {
		this.service = service;
	}
}
