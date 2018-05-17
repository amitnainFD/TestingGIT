package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * Service configuration bean's home.
 * @author istvan
 *
 */
public interface SmartStoreServiceConfigurationHome extends EJBHome {
	
	/**
	 * Create the service configuration session bean.
	 * @return session bean
	 * @throws CreateException
	 * @throws RemoteException
	 */
	public SmartStoreServiceConfigurationSB create() throws CreateException, RemoteException;

}
