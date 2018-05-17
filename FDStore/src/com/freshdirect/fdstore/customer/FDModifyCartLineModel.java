/*
 * $Workfile:FDModifyCartLineModel.java$
 *
 * $Date:4/4/2002 8:34:49 PM$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer;

import com.freshdirect.fdstore.FDConfiguration;

/**
 *
 * @version    $Revision:1$
 * @author     $Author:Viktor Szathmary$
 * @stereotype fd-model
 */
class FDModifyCartLineModel extends FDCartLineModel implements FDModifyCartLineI {
	private static final long serialVersionUID = 7177055603220148537L;

	/**
	 * 
	 */
	private final FDCartLineI originalOrderLine;

	public FDModifyCartLineModel(FDCartLineI origLine) {
		super(origLine.getSku(), origLine.getProductRef().lookupProductModel(), origLine
				.getConfiguration(), origLine.getCartlineId(), origLine
				.getRecipeSourceId(), origLine.isRequestNotification(),
				origLine.getVariantId(), origLine.getUserContext(), origLine.getClientCodes());
		this.orderLine.setPlantID(origLine.getPlantId());
		this.originalOrderLine = origLine;
		
	}

	public FDCartLineI getOriginalOrderLine() {
		return this.originalOrderLine;
	}

	public void setConfiguration(FDConfiguration conf) {
		if (this.originalOrderLine.getQuantity() < conf.getQuantity()) {
			throw new IllegalArgumentException("Quantity cannot be increased beyond original amount");
		}
		if (!conf.getSalesUnit().equals(this.originalOrderLine.getSalesUnit())) {
			throw new IllegalArgumentException("Sales Unit cannot be altered");
		}
		if (!conf.getOptions().equals(conf.getOptions())) {
			throw new RuntimeException("Configuration options cannot be altered");
		}
		super.setConfiguration(conf);
	}

	/*
	// !!! what's up with these?
	public void setPromotion(PromotionModel promotion) {
	
	*/
}
