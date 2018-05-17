/*
 * Created on Jun 10, 2005
 *
 */
package com.freshdirect.fdstore.referral.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 * @author jng
 *
 */
public interface FDReferralManagerHome extends EJBHome {
    public FDReferralManagerSB create() throws CreateException, RemoteException;
}
