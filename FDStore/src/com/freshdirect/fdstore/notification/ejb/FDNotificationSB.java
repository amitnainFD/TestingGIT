package com.freshdirect.fdstore.notification.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBObject;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.notification.FDNotification;

public interface FDNotificationSB extends EJBObject{
	
	public Collection<FDNotification> loadCustomerNotifications(FDIdentity identity) throws FDResourceException, RemoteException;
	
	public Collection<FDNotification> loadAllNotifications() throws FDResourceException, RemoteException;
	
	public void checkNotificationForCustomer(FDActionInfo info,FDIdentity identity, FDNotification notification) throws FDResourceException, RemoteException;
	
	public void insertNotification(FDActionInfo info, FDNotification notification) throws FDResourceException, RemoteException;
	
	public void delete(FDActionInfo info, FDNotification notification) throws FDResourceException, RemoteException;

}
