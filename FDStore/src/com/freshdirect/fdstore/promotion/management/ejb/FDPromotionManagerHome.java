package com.freshdirect.fdstore.promotion.management.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

import com.freshdirect.fdstore.promotion.management.ejb.FDPromotionManagerSB;

public interface FDPromotionManagerHome extends EJBHome {

	   public FDPromotionManagerSB create() throws CreateException, RemoteException;

}
