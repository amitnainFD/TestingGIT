package com.freshdirect.fdstore.oauth.provider.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * @author Tamas Gelesz
 */
public interface OAuthProviderHome extends EJBHome {

	   public OAuthProviderSB create() throws CreateException, RemoteException;

}
