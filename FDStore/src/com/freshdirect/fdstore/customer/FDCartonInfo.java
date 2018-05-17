/*
 * Created on Mar 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.List;

import com.freshdirect.customer.ErpCartonInfo;

/**
 * @author htai
 *
 */
public class FDCartonInfo implements Serializable {
	private static final long serialVersionUID = -170713885086126982L;

	private ErpCartonInfo cartonInfo;
	private List<FDCartonDetail> cartonDetails;

	public FDCartonInfo(ErpCartonInfo cartonInfo, List<FDCartonDetail> cartonDetails) {
		this.cartonInfo = cartonInfo;
		this.cartonDetails = cartonDetails;
	}

	public List<FDCartonDetail> getCartonDetails() {
		return this.cartonDetails;
	}

	public ErpCartonInfo getCartonInfo() {
		return this.cartonInfo;
	}

	public FDCartonDetail containsCartonInfo(String orderlineNumber) {
		if (cartonDetails == null)
			return null;

		for (FDCartonDetail detail : cartonDetails) {
			if (detail.getCartonDetail().getOrderLineNumber().equals(orderlineNumber))
				return detail;
		}
		return null;

	}

}
