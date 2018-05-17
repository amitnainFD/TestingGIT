package com.freshdirect.fdstore.oas.ejb;

import java.io.Serializable;

import com.freshdirect.common.pricing.ZoneInfo;

/**@author ekracoff on Jul 22, 2004*/
public class AdServerRow implements Serializable{
	private final String productId;
	private final boolean available;
	private final String price;
	private final ZoneInfo zone;
	private final String zoneType;

	public AdServerRow(String productId, boolean isAvailable, String price) {
		this.productId = productId;
		this.available = isAvailable;
		this.price = price;
		this.zone= null;
		this.zoneType = null;
	}

	public AdServerRow(String productId, boolean available, String price,
			ZoneInfo zone, String zoneType) {
		super();
		this.productId = productId;
		this.available = available;
		this.price = price;
		this.zone = zone;
		this.zoneType = zoneType;
	}

	public boolean isAvailable() {
		return available;
	}

	public String getPrice() {
		return price;
	}

	public String getProductId() {
		return productId;
	}

	public boolean equals(Object o) {
		if (!(o instanceof AdServerRow))
			return false;
		if (this.productId.equals(((AdServerRow) o).getProductId()))
			return true;

		return false;

	}

	/**
	 * @return the zoneId
	 */
	public ZoneInfo getZone() {
		return zone;
	}

	/**
	 * @return the zoneType
	 */
	public String getZoneType() {
		return zoneType;
	}
	
}