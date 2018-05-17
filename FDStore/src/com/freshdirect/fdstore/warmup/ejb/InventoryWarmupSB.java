/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.warmup.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDIdentity;

/**
 * performs inventory checks on skus to sync inventory on the site with inventory levels in the plant.
 *
 * @version $Revision$
 * @author $Author$
 */
public interface InventoryWarmupSB extends EJBObject {

	/**
	 * do inventory checks for all skus in a department
	 *
	 * @throws FDResourceException if an error occured using remote resources
	 */
	public void syncInventory(String dept) throws FDResourceException, RemoteException;

    public FDIdentity getRandomCustomerIdentity() throws FDResourceException, RemoteException;

}

