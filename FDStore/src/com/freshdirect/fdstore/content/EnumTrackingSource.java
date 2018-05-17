package com.freshdirect.fdstore.content;

public enum EnumTrackingSource {
	AUTO(null) {
		@Override
		public String getValue() {
			throw new UnsupportedOperationException();
		}
	},
	UNDEFINED(null),
	SIDENAV("snav"), GLOBALNAV("gnav"),
	MODIFY_CART_ITEM("pmod"), OAS("promo"),
	DEPARTMENT("dpage"), CATEGORY("cpage"), WINE_FILTER("wfilt");

	private String value;

	private EnumTrackingSource(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
