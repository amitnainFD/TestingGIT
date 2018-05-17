package com.freshdirect.fdstore.promotion;

import java.util.Date;
import java.util.Set;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.core.PrimaryKey;

public class PromotionGeography extends ModelSupport {

	private final Date startDate;
	private final ExclusionSet zips = new ExclusionSet(false);
	private final ExclusionSet depots = new ExclusionSet(false);

	public PromotionGeography(PrimaryKey pk, Date startDate) {
		this.setPK(pk);
		this.startDate = startDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public boolean isAllowedZipCode(String zipCode) {
		return zips.isAllowed(zipCode);
	}

	public boolean isAllowAllZipCodes() {
		return zips.isAllowAll();
	}

	public void setAllowAllZipCodes(boolean allowAll) {
		zips.setAllowAll(allowAll);
	}

	public boolean isAllowedDepotCode(String depotCode) {
		return depots.isAllowed(depotCode);
	}

	public boolean isAllowAllDepotCodes() {
		return depots.isAllowAll();
	}

	public void setAllowAllDepotCodes(boolean allowAll) {
		depots.setAllowAll(allowAll);
	}

	public Set getExcludedZipCodes() {
		return zips.getExclusions();
	}

	public void excludeZipCode(String zipCode) {
		zips.exclude(zipCode);
	}

	public Set getExcludedDepotCodes() {
		return depots.getExclusions();
	}

	public void excludeDepotCode(String depotCode) {
		depots.exclude(depotCode);
	}

	public String toString() {
		return "PromotionGeography[from " + this.startDate + " zips=" + zips + " depots=" + depots + "]";
	}
}
