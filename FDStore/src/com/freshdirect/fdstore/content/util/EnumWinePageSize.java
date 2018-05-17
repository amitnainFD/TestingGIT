package com.freshdirect.fdstore.content.util;

public enum EnumWinePageSize {
	TWENTY(20), FOURTY(40), ALL(Integer.MAX_VALUE);

	private int size;

	private EnumWinePageSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}
}
