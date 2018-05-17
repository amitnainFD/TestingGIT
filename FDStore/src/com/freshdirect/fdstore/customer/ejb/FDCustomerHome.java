/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */

package com.freshdirect.fdstore.customer.ejb;

import java.rmi.RemoteException;
import javax.ejb.*;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.framework.core.*;

/**
 * FDCustomer entity home interface.
 *
 * @version    $Revision$
 * @author     $Author$
 */
public interface FDCustomerHome extends EJBHome {

	public FDCustomerEB create() throws CreateException, RemoteException;

	public FDCustomerEB create(ModelI model) throws CreateException, RemoteException;

	public FDCustomerEB findByPrimaryKey(PrimaryKey pk) throws FinderException, RemoteException;

	public FDCustomerEB findByErpCustomerId(String erpId) throws FinderException, RemoteException;

	public FDCustomerEB findByCookie(String cookie) throws FinderException, RemoteException;

	public FDCustomerEB findByUserId(String email) throws FinderException, RemoteException;

	public FDCustomerEB findByUserId(String email, EnumServiceType type) throws FinderException, RemoteException;

	public FDCustomerEB findByUserIdAndPasswordRequest(String email, String passReq) throws FinderException, RemoteException;

}

