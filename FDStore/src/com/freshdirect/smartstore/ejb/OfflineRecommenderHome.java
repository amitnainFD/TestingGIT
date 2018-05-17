package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface OfflineRecommenderHome extends EJBHome {
	public static final String JNDI_HOME = "freshdirect.smartstore.OfflineRecommender";

	public OfflineRecommenderSB create() throws CreateException, RemoteException;
}
