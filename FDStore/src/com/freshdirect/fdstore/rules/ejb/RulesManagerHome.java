/*
 * DlvManagerHome.java
 *
 * Created on August 27, 2001, 7:03 PM
 */

package com.freshdirect.fdstore.rules.ejb;

/**
 *
 * @author  knadeem
 * @version 
 */
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface RulesManagerHome extends EJBHome{
    
    public RulesManagerSB create() throws CreateException, RemoteException;

}

