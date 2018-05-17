package com.freshdirect.fdstore.services.tax.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoTaxResult // Result of tax/get verb GET
{
    @JsonProperty("Rate")
	public Double rate;

    @JsonProperty("Tax")
    public Double tax;

    @JsonProperty("TaxDetails")
    public TaxDetail[] taxDetails;

    @JsonProperty("ResultCode")
    public CommonResponse.SeverityLevel resultCode;

    @JsonProperty("Messages")
    public CommonResponse.Message[] messages;
    
	public Double getRate(){return rate;};
	public Double getTax(){return tax;};
	public TaxDetail[] getTaxDetails(){return taxDetails;};
	public CommonResponse.SeverityLevel getResultCode(){return resultCode;}
	public CommonResponse.Message[] getMessages(){return messages;}
	
	public void setRate(Double rate){this.rate = rate;	}
	public void setTax(Double tax){this.tax = tax;	}
	public void setTaxDetails(TaxDetail[] taxDetails){this.taxDetails = taxDetails;	}
	public void setResultCode(CommonResponse.SeverityLevel resultCode){this.resultCode = resultCode;}
	public void setMessages(CommonResponse.Message[] messages){this.messages = messages;}
}
