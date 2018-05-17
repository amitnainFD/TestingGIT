package com.freshdirect.fdstore.content.productfeed;


import javax.ejb.*;
import java.rmi.RemoteException;

public interface FDProductFeedHome extends EJBHome {

	public FDProductFeedSB create() throws CreateException, RemoteException;
}
