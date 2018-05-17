package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface VariantSelectionHome extends EJBHome {
	public VariantSelectionSB create() throws CreateException, RemoteException;
}
