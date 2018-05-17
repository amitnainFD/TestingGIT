package com.freshdirect.fdstore.standingorders;

public enum EnumStandingOrderFrequency {
	
	ONE_WEEK	(1, "Every Week"),
	TWO_WEEKS	(2, "Every Two Weeks"),
	THREE_WEEKS	(3, "Every Three Weeks"),
	FOUR_WEEKS	(4, "Every Four Weeks");
	
	private int frequency;
	private String title;
	
	private EnumStandingOrderFrequency(int frequency, String title) {
		this.frequency = frequency;
		this.title = title;
	}

	/**
	 * Frequency in weeks
	 * @return
	 */
	public int getFrequency() {
		return frequency;
	};
	
	/**
	 * Human readable description
	 * @return
	 */
	public String getTitle() {
		return title;
	}

//	public static EnumStandingOrderFrequency getEnumByWebId(String webId) {
//		for (EnumStandingOrderFrequency item : values()) {
//			if (item.getWebId().equals(webId))
//				return item;
//		}
//		return null;
//	}
}
