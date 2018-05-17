package com.freshdirect.test.ejb;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJBObject;

/**
 * Tests Supporter Session Bean interface.
 * @author segabor
 *
 */
public interface TestSupportSB extends EJBObject {
	public boolean ping() throws RemoteException;

	public List<Long> getDYFEligibleCustomerIDs() throws RemoteException;
	public List<Long> getErpCustomerIds() throws RemoteException;
	public String getFDCustomerIDForErpId(String erpCustomerPK) throws RemoteException;
	public String getErpIDForUserID(String userID) throws RemoteException;

	public Collection<String> getSkuCodes() throws RemoteException;
}
