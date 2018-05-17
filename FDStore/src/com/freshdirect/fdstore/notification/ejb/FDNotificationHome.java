package com.freshdirect.fdstore.notification.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface FDNotificationHome extends EJBHome {

	public final static String JNDI_HOME = "freshdirect.fdstore.Notifications";

	public FDNotificationSB create() throws CreateException, RemoteException;

}
