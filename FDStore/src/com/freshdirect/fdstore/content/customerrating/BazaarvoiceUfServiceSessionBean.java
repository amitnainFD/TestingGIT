package com.freshdirect.fdstore.content.customerrating;

import java.util.Map;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.framework.core.SessionBeanSupport;

@Deprecated
public class BazaarvoiceUfServiceSessionBean extends SessionBeanSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4735004684491213606L;
	
	private static CustomerRatingsDAO customerRatingsDAO = new CustomerRatingsDAO();
	
	public BazaarvoiceFeedProcessResult processFile() {
		 return new UploadFeedProcessTask().process();
	}

	public BazaarvoiceFeedProcessResult processRatings() {
		BazaarvoiceFeedProcessResult result = new DownloadFeedProcessTask().process();
		if (result.isSuccess()) {
			result = new StoreFeedTask().process();
		}
		return result;
	}
	
	public long getLastRefresh() throws FDResourceException{
		return customerRatingsDAO.getTimestamp();
	}
	
	public Map<String,CustomerRatingsDTO> getCustomerRatings() throws FDResourceException{
		return customerRatingsDAO.getCustomerRatings();
	}
}
