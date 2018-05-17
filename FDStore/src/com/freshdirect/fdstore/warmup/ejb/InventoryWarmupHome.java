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

/**
 *
 *
 * @version $Revision$
 * @author $Author$
 */
public interface InventoryWarmupHome extends EJBHome {
    
    public InventoryWarmupSB create() throws CreateException, RemoteException;

}

