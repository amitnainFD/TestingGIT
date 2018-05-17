/*
 * Created on Oct 6, 2015
 *
 */
package com.freshdirect.fdstore.ewallet.ejb;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 * @author imohammed
 *
 */
public interface EwalletNotifyStatusSB extends EJBObject {
	//Batch
	void postTrxnsToEwallet() throws RemoteException;
	void loadTrxnsForPostBack(int maxDays) throws RemoteException;
}