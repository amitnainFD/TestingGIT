package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface SessionImpressionLogHome extends EJBHome {
	public static final String JNDI_HOME = "freshdirect.smartstore.SessionImpressionLog";

	public SessionImpressionLogSB create() throws CreateException, RemoteException;
}
