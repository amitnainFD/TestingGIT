/*
 * Created on Sept 26, 2015
 *
 */
package com.freshdirect.fdstore.ewallet.impl.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * @author imohammed
 *
 */
public interface MasterpassServiceHome extends EJBHome {
    public MasterpassServiceSB create() throws CreateException, RemoteException;
}
