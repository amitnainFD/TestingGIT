package com.freshdirect.fdstore.zone.ejb;


import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBObject;
import javax.ejb.ObjectNotFoundException;

import com.freshdirect.customer.ErpZoneMasterInfo;
import com.freshdirect.erp.model.ErpInventoryModel;
import com.freshdirect.erp.model.ErpProductInfoModel;

/**
 * the remote interface for the ErpMaterialInfo session bean
 *
 *
 * @version $Revision$
 * @author $Author$
 */
public interface FDZoneInfoSB extends EJBObject {
    
	public ErpZoneMasterInfo findZoneInfoMaster(String zoneId) throws RemoteException;
    
    public Collection loadAllZoneInfoMaster() throws RemoteException;       
    
    public  String findZoneId(String serviceType,String zipCode) throws RemoteException;
          
}