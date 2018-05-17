package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface DyfModelHome extends EJBHome {
	public DyfModelSB create() throws CreateException, RemoteException;
}
