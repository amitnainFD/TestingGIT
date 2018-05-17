package com.freshdirect.fdstore.grp.ejb;


import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface FDGrpInfoHome extends EJBHome {
	public FDGrpInfoSB create() throws CreateException, RemoteException;
}