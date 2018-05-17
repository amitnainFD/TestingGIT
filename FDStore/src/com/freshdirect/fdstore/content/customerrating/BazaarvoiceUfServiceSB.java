package com.freshdirect.fdstore.content.customerrating;

import java.rmi.RemoteException;
import java.util.Map;

import javax.ejb.EJBObject;

import com.freshdirect.fdstore.FDResourceException;

@Deprecated
public interface BazaarvoiceUfServiceSB extends EJBObject{
	
	public BazaarvoiceFeedProcessResult processFile() throws RemoteException;
	public BazaarvoiceFeedProcessResult processRatings() throws RemoteException;
	public long getLastRefresh() throws FDResourceException, RemoteException;
	public Map<String,CustomerRatingsDTO> getCustomerRatings() throws FDResourceException, RemoteException;
}
