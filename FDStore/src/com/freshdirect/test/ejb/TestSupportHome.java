package com.freshdirect.test.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

import com.freshdirect.test.ejb.TestSupportSB;


public interface TestSupportHome extends EJBHome {
	public TestSupportSB create() throws CreateException, RemoteException;
}
