package com.freshdirect.smartstore.offline;

import java.io.Serializable;

public class OfflineBatchRecommendationsResult implements Serializable {
	private static final long serialVersionUID = 8267567613808150409L;

	private int userCount;

	public OfflineBatchRecommendationsResult(int userCount) {
		super();
		this.userCount = userCount;
	}

	public int getUserCount() {
		return userCount;
	}
}
