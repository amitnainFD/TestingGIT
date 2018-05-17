package com.freshdirect.fdstore.customer;

public enum EnumQuickbuyStatus {
	NO_OP("", 0),
	ADDED_TO_CART("Added to Cart", 0),
	NO_ITEMS_ADDED("No items were added", 1),
	SPECIFY_QUANTITY("Specify quantity", 2),
	QUANTITY_LIMIT("Maximum limit reached", 2),
	ERROR("Error processing the request", 2),
	NOT_LOGGED_IN("Not logged in", 1);

	private String message;
	private int severity;

	private EnumQuickbuyStatus(String message, int severity) {
		this.message = message;
		this.severity = severity;
	}

	public String getMessage() {
		return message;
	}

	public boolean isSuccess() {
		return severity == 0;
	}

	public boolean isWarning() {
		return severity == 1;
	}

	public boolean isError() {
		return severity == 2;
	}
}
