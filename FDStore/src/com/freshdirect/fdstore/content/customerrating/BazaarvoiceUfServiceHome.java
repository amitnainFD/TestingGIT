package com.freshdirect.fdstore.content.customerrating;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

@Deprecated
public interface BazaarvoiceUfServiceHome extends EJBHome{
	
	public final static String JNDI_HOME = "freshdirect.fdstore.BazaarvoiceUfService";

	public BazaarvoiceUfServiceSB create() throws CreateException, RemoteException;

}
