package com.freshdirect.fdstore.oas.ejb;

import java.rmi.RemoteException;
import javax.ejb.*;

/**@author ekracoff on May 25, 2004*/
public interface AdServerGatewayHome extends EJBHome{
	
	public AdServerGatewaySB create() throws CreateException, RemoteException;
	
}
