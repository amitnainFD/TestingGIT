package com.freshdirect.fdstore.oas.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

/**@author ekracoff on May 25, 2004*/
public interface AdServerGatewaySB extends EJBObject{

	public void run() throws RemoteException;

}
