package com.freshdirect.fdstore.standingorders;

public enum UnavailabilityReason {
	INVALID_CONFIG, /* a line item has invalid configuration */
	ATP, /* ATP check failed on an item */
	UNAV, /* item is unavailable */
	GENERAL /* general issue raised */, 
	DISC
}
