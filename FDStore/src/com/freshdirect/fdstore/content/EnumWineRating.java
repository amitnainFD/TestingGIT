package com.freshdirect.fdstore.content;

import com.freshdirect.fdstore.EnumOrderLineRating;

public enum EnumWineRating implements WineFilterValue {
	NOT_RATED(0), ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5);
	
	private final int starCount;
	
	private EnumWineRating(int starCount) {
		this.starCount = starCount;
	}

	public String getEncoded() {
		return "_" + EnumWineRating.class.getSimpleName() + ":" + name();
	}
	
	@Override
	public String getDomainName() {
		return "Rating";
	}
	
	public String getDomainEncoded() {
		return getWineDomain();
	}
	
	@Override
	public EnumWineFilterValueType getWineFilterValueType() {
		return EnumWineFilterValueType.RATING;
	}
	
	public int getStarCount() {
		return starCount;
	}
	
	public static EnumWineRating getEnumByRating(EnumOrderLineRating productRatingEnum) {
		switch (productRatingEnum) {
			case TERRIBLE:
				return NOT_RATED;
			case BELOW_AVG:
			case BELOW_AVG_PLUS:
				return ONE;
			case AVERAGE:
			case AVERAGE_PLUS:
				return TWO;
			case GOOD:
			case GOOD_PLUS:
				return THREE;
			case VERY_GOOD:
			case VERY_GOOD_PLUS:
			case PEAK_PRODUCE_8:
			case PEAK_PRODUCE_9:
				return FOUR;
			case PERFECT:
			case PEAK_PRODUCE_10:
				return FIVE;
			default:
				return NOT_RATED;
		}
	}
	
	public static String getWineDomain() {
		return "_" + EnumWineRating.class.getSimpleName() + "Domain:wine_rating";		
	}
	
	@Override
	public String getFilterRepresentation() {
		if (starCount == 0)
			return "Not Rated";
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < starCount; i++)
			buf.append("<span class=\"wine-rating-sm\"></span>");
		return buf.toString();
	}
}
