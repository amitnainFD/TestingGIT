package com.freshdirect.fdstore.promotion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class GeographyStrategy implements PromotionStrategyI {

	private final List<PromotionGeography> geographies = new ArrayList<PromotionGeography>();

	private final static Comparator<PromotionGeography> GEO_DATE_COMPARATOR = new Comparator<PromotionGeography>() {
		public int compare(PromotionGeography g1, PromotionGeography g2) {
			return g1.getStartDate().compareTo(g2.getStartDate());
		}
	};

	public void addGeography(PromotionGeography geo) {
		this.geographies.add(geo);
		Collections.sort(this.geographies, GEO_DATE_COMPARATOR);
	}

	public List<PromotionGeography> getGeographies() {
		return Collections.unmodifiableList(this.geographies);
	}

	protected PromotionGeography getGeography(Date date) {
		PromotionGeography match = null;
		for (PromotionGeography g : this.geographies) {
			if (date.before(g.getStartDate())) {
				break;
			}
			match = g;
		}
		return match;
	}

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {

		PromotionGeography g = this.getGeography(new Date());
		if (g == null || !isAllowedGeography(context, g)) {
			context.getUser().addPromoErrorCode(promotionCode, PromotionErrorType.NO_ELIGIBLE_ADDRESS_SELECTED.getErrorCode());
			return DENY;
		}

		return ALLOW;
	}

	
	private boolean isAllowedGeography(PromotionContextI context, PromotionGeography geo) {

		EnumOrderType orderType = context.getOrderType();
		if (EnumOrderType.HOME.equals(orderType) || EnumOrderType.CORPORATE.equals(orderType) || EnumOrderType.FDX.equals(orderType)) {
			return geo.isAllowedZipCode(context.getZipCode());

		} else if (EnumOrderType.DEPOT.equals(orderType) || EnumOrderType.PICKUP.equals(orderType)) {
			return geo.isAllowedDepotCode(context.getDepotCode());
		}

		// not deliverable
		return false;
	}


	public boolean evaluate(String promotionCode, EnumOrderType orderType, String zipCode, String depotCode) {

		PromotionGeography g = this.getGeography(new Date());
		if (g == null || !isAllowedGeography(orderType, g,zipCode, depotCode)) {
			return false;
		}

		return true;
	}
	private boolean isAllowedGeography(EnumOrderType orderType, PromotionGeography geo, String zipCode, String depotCode) {

		
		if (EnumOrderType.HOME.equals(orderType) || EnumOrderType.FDX.equals(orderType) ) {
			return geo.isAllowedZipCode(zipCode);

		} else if (EnumOrderType.DEPOT.equals(orderType) || EnumOrderType.PICKUP.equals(orderType)) {
			return geo.isAllowedDepotCode(depotCode);
		}

		// not deliverable
		return false;
	}
	
	@Override
	public int getPrecedence() {
		return 3000;
	}

	public String toString() {
		return "GeographyStrategy[" + this.geographies + "]";
	}

	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
