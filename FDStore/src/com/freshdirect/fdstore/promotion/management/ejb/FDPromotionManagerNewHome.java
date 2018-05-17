package com.freshdirect.fdstore.promotion.management.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface FDPromotionManagerNewHome extends EJBHome{

	public FDPromotionManagerNewSB create() throws CreateException, RemoteException;
}
