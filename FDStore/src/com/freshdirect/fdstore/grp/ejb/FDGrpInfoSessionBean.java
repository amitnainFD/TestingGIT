package com.freshdirect.fdstore.grp.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.customer.ErpGrpPriceModel;
import com.freshdirect.erp.ejb.ErpGrpInfoHome;
import com.freshdirect.erp.ejb.ErpGrpInfoSB;
import com.freshdirect.fdstore.FDGroup;
import com.freshdirect.framework.core.ServiceLocator;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDGrpInfoSessionBean extends SessionBeanSupport{
	
    /** logger for messages
     */
    private static Category LOGGER = LoggerFactory.getInstance( FDGrpInfoSessionBean.class );

	private final static ServiceLocator LOCATOR = new ServiceLocator();
   
    
    public Collection<FDGroup> loadAllGrpInfoMaster() throws RemoteException{
   	 Collection<FDGroup> groups=null;
		try{
			ErpGrpInfoHome home=getErpGrpInfoHome();
			ErpGrpInfoSB remote= home.create();
			groups=remote.loadAllGrpInfoMaster();
		}catch(CreateException sqle){
			LOGGER.error("Unable to load all loadAllZoneInfoMaster " , sqle);			
			throw new EJBException(sqle);
		}
		return groups;
   }
    public int getLatestVersionNumber(String grpId) {
 		try{
 			ErpGrpInfoHome home=getErpGrpInfoHome();
 			ErpGrpInfoSB remote= home.create();
 			return remote.getLatestVersionNumber(grpId);
 		}catch(CreateException ce){
 			LOGGER.error("Unable to get Latest Version " , ce);			
 			throw new EJBException(ce);
 		}catch(RemoteException re){
			LOGGER.error("Unable to get Latest Version " , re);			
			throw new EJBException(re);
		}
    }
    /*
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
 			ErpGrpInfoHome home=getErpGrpInfoHome();
 			ErpGrpInfoSB remote= home.create();
 			 			 			
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
    
    */
    private ErpGrpInfoHome getErpGrpInfoHome() {
		try {
			return (ErpGrpInfoHome) LOCATOR.getRemoteHome("freshdirect.erp.GrpInfoManager");
		} catch (NamingException e) {
			throw new EJBException(e);
		}
	}

}

