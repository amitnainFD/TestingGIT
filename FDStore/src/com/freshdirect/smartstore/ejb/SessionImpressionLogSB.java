package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBObject;

import com.freshdirect.smartstore.SessionImpressionLogEntry;

public interface SessionImpressionLogSB extends EJBObject {
	void saveLogEntry(SessionImpressionLogEntry entry) throws RemoteException;
	void saveLogEntries(Collection entries) throws RemoteException;
}
