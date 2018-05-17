/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.warmup;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.NamingException;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.warmup.ejb.InventoryWarmupHome;
import com.freshdirect.fdstore.warmup.ejb.InventoryWarmupSB;


/**
 *
 *
 * @version $Revision$
 * @author $Author$
 */
public class InventoryWarmupManager {

    private static InventoryWarmupHome inventoryWarmupHome = null;

    public static void inventorySync(String dept) throws FDResourceException {
        if(inventoryWarmupHome == null){
            lookupManagerHome();
        }
        try {
            InventoryWarmupSB sb = inventoryWarmupHome.create();
            sb.syncInventory(dept);
        } catch(CreateException ce) {
            inventoryWarmupHome = null;
            throw new FDResourceException(ce, "Error creating session bean");
        } catch(RemoteException re) {
            inventoryWarmupHome = null;
            throw new FDResourceException(re, "Error talking to session bean");
        }
	}

    
    protected static void lookupManagerHome() throws FDResourceException {
        Context ctx = null;
        try {
            ctx = FDStoreProperties.getInitialContext();
            inventoryWarmupHome = (InventoryWarmupHome) ctx.lookup("freshdirect.fdstore.InventoryWarmup");
        } catch (NamingException ne) {
            throw new FDResourceException(ne);
        } finally {
            try {
                ctx.close();
            } catch (NamingException e) {}
        }
    }


}


