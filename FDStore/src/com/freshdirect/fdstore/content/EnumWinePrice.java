package com.freshdirect.fdstore.content;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.util.log.LoggerFactory;

public enum EnumWinePrice implements WineFilterValue {
	ONE(1), TWO(2), THREE(3), FOUR(4);
	
	private static final Logger LOGGER = LoggerFactory.getInstance(EnumWinePrice.class); 

	// exclusive value
	private Double lowerBound = null;
	// inclusive value
	private Double upperBound = null;

	// I do not trust in ordinal(), sorry
	private int dollarCount;

	private EnumWinePrice(int dollarCount) {
		this.dollarCount = dollarCount;
	}

	public String getEncoded() {
		return "_" + EnumWinePrice.class.getSimpleName() + ":" + name();
	}

	@Override
	public String getDomainName() {
		return "Price";
	}
		
	public String getDomainEncoded() {
		return getWineDomain();
	}

	public Double getLowerBound() {
		if (lowerBound == null) {
			lowerBound = getLowerBoundFor(this);
			LOGGER.info("lower bound of " + name() + " is set to " + lowerBound);
		}
		return lowerBound;
	}

	public Double getUpperBound() {
		if (upperBound == null) {
			upperBound = getUpperBoundFor(this);
			LOGGER.info("upper bound of " + name() + " is set to " + upperBound);
		}
		return upperBound;
	}

	public int getDollarCount() {
		return dollarCount;
	}

	@Override
	public EnumWineFilterValueType getWineFilterValueType() {
		return EnumWineFilterValueType.PRICE;
	}

	private static double getLowerBoundFor(EnumWinePrice price) {
		int index = price.ordinal();
		if (index == 0) {
			return 0.0;
		} else {
			return FDStoreProperties.getWinePriceBucketBound(index);
		}
	}

	private static double getUpperBoundFor(EnumWinePrice price) {
		int index = price.ordinal() + 1;
		if (index == values().length) {
			return Double.POSITIVE_INFINITY;
		} else {
			return FDStoreProperties.getWinePriceBucketBound(index);
		}
	}

	public static EnumWinePrice getByDollarCount(int count) {
		return (count >= 1 && count <= 5) ? values()[count - 1] : null;
	}

	public static EnumWinePrice getEnumByPrice(double price) {
		for (EnumWinePrice e : values())
			if (price > e.getLowerBound() && price <= e.getUpperBound())
				return e;

		// with negative or zero price possible one of the cheapest wine
		return ONE;
	}

	public static String getWineDomain() {
		return "_" + EnumWinePrice.class.getSimpleName() + "Domain:wine_price";
	}

	@Override
	public String getFilterRepresentation() {
		StringBuilder buf = new StringBuilder();
		buf.append("-");
		for (int i = 0; i < dollarCount; i++)
			buf.append("$");
		buf.append("-");
		return buf.toString();
	}
}
