package com.freshdirect.smartstore.fdstore;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.application.CmsManager;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.content.EnumWinePrice;
import com.freshdirect.fdstore.customer.ejb.FDServiceLocator;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.ejb.ScoreFactorSB;

public class DatabaseScoreFactorProvider {

    private static Logger                      LOGGER         = LoggerFactory.getInstance(DatabaseScoreFactorProvider.class);

    private static DatabaseScoreFactorProvider instance       = null;

    private ScoreFactorSB                      scoreFactorSB  = null;

    private String eStoreId	= EnumEStoreId.FD.getContentId();
    
    public synchronized static DatabaseScoreFactorProvider getInstance() {
        if (instance == null) {
            try {
                instance = new DatabaseScoreFactorProvider();
            } catch (RemoteException e) {
                LOGGER.warn("Could not create " + DatabaseScoreFactorProvider.class.getName() + " instance", e);
                throw new FDRuntimeException(e);
            } catch (CreateException e) {
                LOGGER.warn("Could not create " + DatabaseScoreFactorProvider.class.getName() + " instance", e);
                throw new FDRuntimeException(e);
            } catch (NamingException e) {
                LOGGER.warn("Could not create " + DatabaseScoreFactorProvider.class.getName() + " instance", e);
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public Map<String,double[]> getPersonalizedFactors(String erpCustomerId, List<String> factors) {
        try {
            if (erpCustomerId == null) {
                return Collections.emptyMap();
            }
            return getSessionBean().getPersonalizedFactors(eStoreId, erpCustomerId, factors);
        } catch (RemoteException e) {
            LOGGER.warn(e);
            throw new FDRuntimeException(e);
        }
    }

    public Map<String,double[]> getGlobalFactors(List<String> factors) {
        try {
            return getSessionBean().getGlobalFactors(eStoreId, factors);
        } catch (RemoteException e) {
            LOGGER.warn(e);
            throw new FDRuntimeException(e);
        }
    }

    public Set<String> getGlobalFactorNames() {
        try {
            return getSessionBean().getGlobalFactorNames();
        } catch (RemoteException e) {
            LOGGER.warn(e);
            throw new FDRuntimeException(e);
        }
    }

    public Set<String> getPersonalizedFactorNames() {
        try {
            return getSessionBean().getPersonalizedFactorNames();
        } catch (RemoteException e) {
            LOGGER.warn(e);
            throw new FDRuntimeException(e);
        }
    }

    public Set<String> getPersonalizedProducts(String erpCustomerId) {
        try {
            return getSessionBean().getPersonalizedProducts(eStoreId, erpCustomerId);
        } catch (RemoteException e) {
            LOGGER.warn(e);
            throw new FDRuntimeException(e);
        }
    }

    public Set<String> getGlobalProducts() {
        try {
            return getSessionBean().getGlobalProducts(eStoreId);
        } catch (RemoteException e) {
            LOGGER.warn(e);
            throw new FDRuntimeException(e);
        }
    }

    /**
     * Return a list of product recommendation for a given product by a
     * recommender vendor.
     * 
     * @param recommender
     * @param key
     * @return List<ContentKey>
     * @throws RemoteException
     */
	@Deprecated
    public List<ContentKey> getProductRecommendations(String recommender, ContentKey key) {
        try {
            return getSessionBean().getProductRecommendations(recommender, key);
        } catch (RemoteException e) {
            LOGGER.warn(e);
            throw new FDRuntimeException(e);
        }
    }

    /**
     * Return a list of personal recommendation (ContentKey-s) for a user by a
     * recommender vendor.
     * 
     * @param recommender
     * @param erpCustomerId
     * @return List<ContentKey>
     * @throws RemoteException
     */
	@Deprecated
    public List<ContentKey> getPersonalRecommendations(String recommender, String erpCustomerId) {
        try {
            return getSessionBean().getPersonalRecommendations(recommender, erpCustomerId);
        } catch (RemoteException e) {
            LOGGER.warn(e);
            throw new FDRuntimeException(e);
        }
    }

    private DatabaseScoreFactorProvider() throws NamingException, RemoteException, CreateException {
    	this.eStoreId = CmsManager.getInstance().getEStoreId();

        scoreFactorSB = FDServiceLocator.getInstance().getScoreFactorHome().create();
    }


    private final ScoreFactorSB getSessionBean() {
        return scoreFactorSB;
    }
    
    @Deprecated
    public EnumWinePrice getPreferredWinePrice(String erpCustomerId) {
        try {
            return scoreFactorSB.getPreferredWinePrice(erpCustomerId);
        } catch (RemoteException e) {
            LOGGER.warn(e);
            throw new FDRuntimeException(e);
        }
    }
    
}
