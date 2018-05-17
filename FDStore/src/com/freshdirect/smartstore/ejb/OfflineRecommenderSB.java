package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;
import java.util.Set;

import javax.ejb.EJBObject;

import com.freshdirect.fdstore.FDResourceException;

public interface OfflineRecommenderSB extends EJBObject {
	public Set<String> getRecentCustomers(int days) throws RemoteException,
			FDResourceException;

	public Set<String> getUpdatedCustomers(int age) throws RemoteException,
	FDResourceException;

	public int removeOldRecommendation(int age) throws RemoteException;
	
	public void checkSiteFeature(String siteFeature) throws RemoteException,
			FDResourceException;

	public int recommend(String[] siteFeatures, String customerId,
			String currentNode) throws RemoteException, FDResourceException;
}
