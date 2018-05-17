package com.freshdirect.fdstore.services.tax;

import com.freshdirect.fdstore.FDException;
import com.freshdirect.fdstore.customer.FDCartI;
import com.freshdirect.fdstore.services.tax.data.CancelTaxRequest;
import com.freshdirect.fdstore.services.tax.data.CancelTaxRequest.CancelCode;
import com.freshdirect.fdstore.services.tax.data.DocType;
import com.freshdirect.fdstore.services.tax.data.GetTaxRequest;
import com.freshdirect.fdstore.services.tax.data.GetTaxResult;

/*
 * @author Nakkeeran Annamalai
 */
public class AvalaraTaxStrategyImpl implements TaxStrategy{
	private AvalaraTaxRequestConverter requestConverter = new AvalaraTaxRequestConverter();
	private AvalaraTaxResponseConverter responseConverter = new AvalaraTaxResponseConverter();
	
	@Override
	public String getType() {
		return "AVALARA_TAX";
	}
	
	@Override
	public AvalaraContext getTaxResponse(AvalaraContext avalaraContext) throws FDException {
		GetTaxRequest request = requestConverter.convert(avalaraContext);
		AvalaraTaxService taxService = new AvalaraTaxService();
		GetTaxResult result = taxService.getTax(request);
		if(result == null){
			throw new FDException();
		}
		
		return responseConverter.convert(result,avalaraContext);
	}

	@Override
	public boolean cancelTax(AvalaraContext avalaraContext) {
		AvalaraTaxService taxService = new AvalaraTaxService();
		CancelTaxRequest cancelRequest = new CancelTaxRequest();
		cancelRequest.setCancelCode(CancelCode.DocVoided);
		cancelRequest.setDocCode(avalaraContext.getDocCode());
		cancelRequest.setDocType(DocType.SalesInvoice);
		taxService.cancelTax(cancelRequest);
		return false;
	}
}