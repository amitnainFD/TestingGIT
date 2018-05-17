package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBObject;

import com.freshdirect.fdstore.util.EnumSiteFeature;

public interface VariantSelectionSB extends EJBObject {
	public Map<String,String> getVariantMap(EnumSiteFeature feature) throws RemoteException;
	public Map<String,String> getVariantMap(EnumSiteFeature feature, Date date) throws RemoteException;
	public Map<String, Integer> getCohorts() throws RemoteException;
	public List<String> getCohortNames() throws RemoteException;
	public List<String> getVariants(EnumSiteFeature feature) throws RemoteException;
	public List<Date> getStartDates() throws RemoteException;
}
