package com.freshdirect.fdstore.services.tax;

import org.apache.log4j.Logger;

import com.freshdirect.common.pricing.MunicipalityInfo;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDCartI;
import com.freshdirect.fdstore.customer.FDCartLineI;
/*
 * @author: Nakkeeran Annamalai 
 */

public class FDTaxStrategyImpl implements TaxStrategy{
	private static final Logger LOGGER = Logger.getLogger(FDTaxStrategyImpl.class);
	@Override
	public String getType() {
		return "FD_TRADITIONAL_TAX";
	}
	
	public AvalaraContext getTaxResponse(AvalaraContext avalaraContext) throws FDResourceException {
		FDCartI cart = avalaraContext.getCart();
		LOGGER.info("FD_TRADITIONAL_TAX is selected ");
		MunicipalityInfo municipalityInfo = FDTaxUtil.getMunicipalityInformation(cart);
		for (FDCartLineI line: cart.getOrderLines()) {
			line.setTaxRate(municipalityInfo.getTaxRate());
			line.setDepositValue(municipalityInfo.getBottleDeposit());
			line.setTaxationType(municipalityInfo.getTaxationType());
		}
		return avalaraContext;
	}

	@Override
	public boolean cancelTax(AvalaraContext avalaraContext) {
		//Do Nothing no cancel tax in traditional tax calculation.
		return true;
	}
}