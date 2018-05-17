package com.freshdirect.fdstore.customer.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;


public interface FDCustomerManagerHome extends EJBHome {
    
    public FDCustomerManagerSB create() throws CreateException, RemoteException;

}

