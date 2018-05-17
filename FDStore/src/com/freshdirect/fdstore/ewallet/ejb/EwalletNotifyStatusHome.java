/*
 * Created on Oct 6, 2015
 *
 */
package com.freshdirect.fdstore.ewallet.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * @author imohammed
 *
 */
public interface EwalletNotifyStatusHome extends EJBHome {
    public EwalletNotifyStatusSB create() throws CreateException, RemoteException;
}