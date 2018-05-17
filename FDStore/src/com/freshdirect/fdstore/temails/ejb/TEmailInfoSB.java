package com.freshdirect.fdstore.temails.ejb;

/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */


import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBObject;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.mail.EnumTranEmailType;

/**
 * performs inventory checks on skus to sync inventory on the site with inventory levels in the plant.
 *
 * @version $Revision$
 * @author $Author$
 */
public interface TEmailInfoSB extends EJBObject {

	/**
	 * do inventory checks for all skus in a department
	 *
	 * @throws FDResourceException if an error occured using remote resources
	 */
	public void sendEmail(EnumTranEmailType tranType,Map input) throws FDResourceException, RemoteException;

    //public FDIdentity getRandomCustomerIdentity() throws FDResourceException, RemoteException;
	
	public void sendFailedTransactions(int timeout) throws RemoteException;
	
	public List getFailedTransactionList(int max_count,boolean isMailContentReqd) throws RemoteException;
	
	public Map getFailedTransactionStats() throws RemoteException;

}

