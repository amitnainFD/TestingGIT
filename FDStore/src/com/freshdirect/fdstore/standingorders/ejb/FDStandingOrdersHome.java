package com.freshdirect.fdstore.standingorders.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface FDStandingOrdersHome extends EJBHome {
    
    public final static String JNDI_HOME = "freshdirect.fdstore.StandingOrders";

	public FDStandingOrdersSB create() throws CreateException, RemoteException;
}
