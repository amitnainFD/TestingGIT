package com.freshdirect.fdstore.mail;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface CreateEmailHome extends EJBHome {
	public static final String JNDI_HOME = "freshdirect.fdstore.CreateEmail";

	public CreateEmailSB create() throws CreateException, RemoteException;
}
