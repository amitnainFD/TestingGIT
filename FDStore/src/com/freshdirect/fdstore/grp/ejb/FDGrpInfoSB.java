package com.freshdirect.fdstore.grp.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBObject;

import com.freshdirect.customer.ErpGrpPriceModel;
import com.freshdirect.fdstore.FDGroup;

public interface FDGrpInfoSB extends EJBObject{
	
    public Collection<FDGroup> loadAllGrpInfoMaster() throws RemoteException;
    
    public int getLatestVersionNumber(String grpId) throws RemoteException;   


}
