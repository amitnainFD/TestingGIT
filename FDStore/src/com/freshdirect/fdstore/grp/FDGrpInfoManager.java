package com.freshdirect.fdstore.grp;


import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.customer.ErpGrpPriceModel;
import com.freshdirect.fdstore.FDGroup;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.grp.ejb.FDGrpInfoHome;
import com.freshdirect.fdstore.grp.ejb.FDGrpInfoSB;
import com.freshdirect.framework.core.ServiceLocator;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDGrpInfoManager {
	
	
	  
	    private static Category LOGGER = LoggerFactory.getInstance( FDGrpInfoManager.class );

		private final static ServiceLocator LOCATOR = new ServiceLocator();
		
		private static FDGrpInfoHome managerHome = null;
		
	    public static Collection<FDGroup> loadAllGrpInfoMaster() throws FDResourceException{
	    		    	
	    	 Collection<FDGroup> zoneInfo=null;
	 		try{
	 			lookupManagerHome();
	 			FDGrpInfoSB sb = managerHome.create();	 			
	 			zoneInfo=sb.loadAllGrpInfoMaster();
	 		}catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}
	 		return zoneInfo;
	    }
	
	    public static int getLatestVersionNumber(String grpId) throws FDResourceException{
	 		try{
	 			lookupManagerHome();
	 			FDGrpInfoSB sb = managerHome.create();	 			
	 			return sb.getLatestVersionNumber(grpId);
	 		}catch (CreateException ce) {
				invalidateManagerHome();
				throw new FDResourceException(ce, "Error creating session bean");
			} catch (RemoteException re) {
				invalidateManagerHome();
				throw new FDResourceException(re, "Error talking to session bean");
			}
	    } 
	  
	    
	    private static void lookupManagerHome() throws FDResourceException {
			if (managerHome != null) {
				return;
			}
			Context ctx = null;
			try {
				ctx = FDStoreProperties.getInitialContext();
				managerHome = (FDGrpInfoHome) ctx.lookup("freshdirect.fdstore.GrpInfoManager");
			} catch (NamingException ne) {
				throw new FDResourceException(ne);
			} finally {
				try {
					if (ctx != null) {
						ctx.close();
					}
				} catch (NamingException ne) {
					LOGGER.warn("Cannot close Context while trying to cleanup", ne);
				}
			}
		}
	    
	    
	    private static void invalidateManagerHome() {
			managerHome = null;
		}

}

