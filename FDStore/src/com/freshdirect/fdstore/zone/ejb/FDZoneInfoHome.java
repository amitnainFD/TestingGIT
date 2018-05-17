package com.freshdirect.fdstore.zone.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface FDZoneInfoHome extends EJBHome {
	public FDZoneInfoSB create() throws CreateException, RemoteException;
}
