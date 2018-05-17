package com.freshdirect.fdstore.services.tax;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.freshdirect.common.pricing.MunicipalityInfo;
import com.freshdirect.customer.ErpChargeLineModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDCartI;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.services.tax.data.GetTaxResult;
import com.freshdirect.fdstore.services.tax.data.TaxLine;
import com.freshdirect.fdstore.services.tax.data.CommonResponse.Message;
import com.freshdirect.fdstore.services.tax.data.CommonResponse.SeverityLevel;

public class AvalaraTaxResponseConverter {
	
	private static Logger LOGGER = Logger.getLogger(AvalaraTaxResponseConverter.class);
	
	public AvalaraContext convert(GetTaxResult result, AvalaraContext avalaraContext) {
		boolean isTaxProds = false;
		boolean tmpIsTaxProds = false;
		FDCartI cart = avalaraContext.getCart();
		
		Message[] messages = result.getMessages();
		if(messages != null){
			for(Message msg: messages){
				Priority priority = Level.DEBUG;
				if(SeverityLevel.Error.equals(result.getResultCode())){
					priority = Level.ERROR;
				}
				LOGGER.log(priority,"Messages Summary" + msg.getSummary());
				LOGGER.log(priority,"Messages Details" + msg.getDetails());
				LOGGER.log(priority,"Messages Source" + msg.getSource());
				LOGGER.log(priority,"Messages Severity" + msg.getSeverity());
			}
		}
		
		TaxLine[] taxLines = result.getTaxLines();
		if(!ArrayUtils.isEmpty(taxLines) )
		{
			for(TaxLine lineResult : taxLines){
			//Tax line will be cart line .
				tmpIsTaxProds = populateCartLinesWithResult(cart, lineResult);
				if(!isTaxProds){isTaxProds = tmpIsTaxProds;}
				}
			
			if(isTaxProds){
				for(TaxLine lineResult : taxLines){
				populateErpChargeLinesWithResult(cart, lineResult);
				}
			}
			// send for Bottle deposit
		getMunicipalityCharges(cart);
		}
		avalaraContext.setDocCode(result.getDocCode());
		return avalaraContext;
	}
	
	private boolean populateCartLinesWithResult(FDCartI cart, TaxLine lineResult) {
		boolean isTaxProds = false;
		List<FDCartLineI> cartLines = cart.getOrderLines();
		if(CollectionUtils.isNotEmpty(cartLines)){
			for(FDCartLineI cartLine : cartLines){
				if(StringUtils.equals(lineResult.getLineNo(),cartLine.getCartlineId()) || StringUtils.equals(lineResult.getLineNo(),cartLine.getOrderLineId())){
					cartLine.setTaxRate(lineResult.getRate());
					if(lineResult.getRate()>0.0)
					isTaxProds = true;
					continue;
				} 
			}
		}
		return isTaxProds;
	}
	
	private boolean populateErpChargeLinesWithResult(FDCartI cart, TaxLine taxLine){
		boolean isTaxSet = false;
		Collection<ErpChargeLineModel> charges = cart.getCharges();
		if(CollectionUtils.isNotEmpty(charges)){
			for(ErpChargeLineModel charge: charges){
				if(charge.getType().getCode().equals(taxLine.getLineNo())){
					charge.setTaxRate(taxLine.getRate());
					isTaxSet = true;
					continue;
				}
			}
		}
		return isTaxSet;
	}
	public void getMunicipalityCharges(FDCartI cart)  {
		MunicipalityInfo municipalityInfo;
		try {
			municipalityInfo = FDTaxUtil.getMunicipalityInformation(cart);
			for (FDCartLineI line: cart.getOrderLines()) {
				line.setDepositValue(municipalityInfo.getBottleDeposit());
			}
		} catch (FDResourceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	//	return cart;
	}

}