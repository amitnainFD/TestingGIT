package com.freshdirect.fdstore.zone.ejb;



import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.customer.EnumZoneServiceType;
import com.freshdirect.customer.ErpZoneMasterInfo;
import com.freshdirect.customer.ejb.ErpCustomerManagerHome;
import com.freshdirect.erp.ejb.ErpZoneInfoHome;
import com.freshdirect.erp.ejb.ErpZoneInfoSB;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.core.ServiceLocator;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDZoneInfoSessionBean extends SessionBeanSupport{
	
    /** logger for messages
     */
    private static Category LOGGER = LoggerFactory.getInstance( FDZoneInfoSessionBean.class );

	private final static ServiceLocator LOCATOR = new ServiceLocator();
	
	public ErpZoneMasterInfo findZoneInfoMaster(String zoneId) throws RemoteException {
	     
		
        ErpZoneMasterInfo zoneInfo=null;
		try{
			ErpZoneInfoHome home=getErpZoneInfoHome();
			ErpZoneInfoSB remote= home.create();
			zoneInfo=remote.findZoneInfoMaster(zoneId);
		}catch(CreateException sqle){
			LOGGER.error("Unable to load all loadAllZoneInfoMaster " , sqle);			
			throw new EJBException(sqle);
		}
		return zoneInfo;
				
	}
    
    public Collection loadAllZoneInfoMaster() throws RemoteException{
    	 Collection zoneInfo=null;
 		try{
 			ErpZoneInfoHome home=getErpZoneInfoHome();
 			ErpZoneInfoSB remote= home.create();
 			zoneInfo=remote.loadAllZoneInfoMaster();
 		}catch(CreateException sqle){
 			LOGGER.error("Unable to load all loadAllZoneInfoMaster " , sqle);			
 			throw new EJBException(sqle);
 		}
 		return zoneInfo;
    }
    
    public  String findZoneId(String serviceType,String zipCode) throws RemoteException{
    	String zoneServType=null;
    	String zoneId=null;
    	if(serviceType!=null || serviceType.trim().length()>0)
    	{
    	   EnumServiceType servType=EnumServiceType.getEnum(serviceType);
    	   if(EnumServiceType.HOME.equals(servType)){
    		   zoneServType=EnumZoneServiceType.RES.getName();
    	   }else if(EnumServiceType.CORPORATE.equals(servType)){
    		   zoneServType=EnumZoneServiceType.CORP.getName();
    	   }
    	   else if (EnumServiceType.PICKUP.equals(servType)){
    		   zoneServType=EnumZoneServiceType.RES.getName();
    		   zipCode=FDStoreProperties.getDefaultPickupZoneId();
    	   }
    	}
    	
    	  if(zoneServType==null || zoneServType.trim().length()==0)
    		   zoneServType=EnumZoneServiceType.ALL.getName();    	    	
    	  
    	try{
 			ErpZoneInfoHome home=getErpZoneInfoHome();
 			ErpZoneInfoSB remote= home.create();
 			 			 			
 			if(zipCode!=null && zipCode.trim().length()>0)
 			    zoneId=remote.findZoneId(zoneServType, zipCode);
 		
 			if(zoneId==null || zoneId.trim().length()==0)
 				 zoneId=remote.findZoneId(zoneServType);
 			
 		}catch(CreateException sqle){
 			LOGGER.error("Unable to load all loadAllZoneInfoMaster " , sqle);			
 			throw new EJBException(sqle);
 		}
    	
    	
    	return zoneId;
    }
    
    
    private ErpZoneInfoHome getErpZoneInfoHome() {
		try {
			return (ErpZoneInfoHome) LOCATOR.getRemoteHome("freshdirect.erp.ZoneInfoManager");
		} catch (NamingException e) {
			throw new EJBException(e);
		}
	}

}
