package com.freshdirect.fdstore.temails.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

/**
 *
 *
 * @version $Revision$
 * @author $Author$
 */
public interface TEmailInfoHome extends EJBHome {
    
    public TEmailInfoSB create() throws CreateException, RemoteException;

}
