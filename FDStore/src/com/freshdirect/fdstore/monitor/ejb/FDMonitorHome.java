/*
 * $Workfile$
 *
 * $Date$
 * 
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.monitor.ejb;

import javax.ejb.*;
import java.rmi.*;

/**
 *
 *
 * @version $Revision$
 * @author $Author$
 */
public interface FDMonitorHome extends EJBHome {
    
    public FDMonitorSB create() throws CreateException, RemoteException;

}

