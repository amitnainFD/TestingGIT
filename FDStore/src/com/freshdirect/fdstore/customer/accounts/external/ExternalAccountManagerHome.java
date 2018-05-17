package com.freshdirect.fdstore.customer.accounts.external;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;


public interface ExternalAccountManagerHome extends EJBHome {
	
	public ExternalAccountManagerSB create() throws CreateException, RemoteException;

}
