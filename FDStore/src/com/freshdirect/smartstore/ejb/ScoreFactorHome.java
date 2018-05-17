package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * Session bean home.
 * @author istvan
 * @see ScoreFactorSB
 * @see ScoreFactorSessionBean
 */
public interface ScoreFactorHome extends EJBHome {
	
	public ScoreFactorSB create() throws CreateException, RemoteException;
}
