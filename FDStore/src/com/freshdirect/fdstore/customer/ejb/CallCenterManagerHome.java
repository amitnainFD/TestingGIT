/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

/**
 *
 *
 * @version $Revision$
 * @author $Author$
 */
public interface CallCenterManagerHome extends EJBHome {

    public CallCenterManagerSB create() throws CreateException, RemoteException;

}

