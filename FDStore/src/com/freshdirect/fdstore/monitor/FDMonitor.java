/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.monitor;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.NamingException;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.monitor.ejb.FDMonitorHome;
import com.freshdirect.fdstore.monitor.ejb.FDMonitorSB;


/**
 *
 *
 * @version $Revision$
 * @author $Author$
 */
public class FDMonitor {
   
    private static FDMonitorHome monitorHome = null;

    public static void healthCheck() throws FDResourceException {
        lookupMonitorHome();
        
        try {
            FDMonitorSB sb = monitorHome.create();
            sb.healthCheck();
        } catch (CreateException ce) {
            invalidateMonitorHome();
            throw new FDResourceException(ce, "Error creating session bean");
        } catch (RemoteException re) {
            invalidateMonitorHome();
            throw new FDResourceException(re, "Error talking to session bean");
        }
    }
    
    private static void invalidateMonitorHome() {
        monitorHome = null;
    }
    
    private static void lookupMonitorHome() throws FDResourceException {
        if (monitorHome!=null) {
            return;
        }
        Context ctx = null;
        try {
            ctx = FDStoreProperties.getInitialContext();
            monitorHome = (FDMonitorHome) ctx.lookup("freshdirect.fdstore.monitor.Monitor");
        } catch (NamingException ne) {
            throw new FDResourceException(ne);
        } finally {
            try {
                ctx.close();
            } catch (NamingException e) {}
        }
    }
    
    
}


