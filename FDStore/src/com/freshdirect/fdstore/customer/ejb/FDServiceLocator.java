package com.freshdirect.fdstore.customer.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.NamingException;

import com.freshdirect.common.ERPServiceLocator;
import com.freshdirect.customer.ejb.ActivityLogHome;
import com.freshdirect.customer.ejb.ErpCustomerHome;
import com.freshdirect.customer.ejb.ErpCustomerManagerHome;
import com.freshdirect.customer.ejb.ErpFraudPreventionHome;
import com.freshdirect.customer.ejb.ErpSaleHome;
import com.freshdirect.delivery.ejb.DlvManagerHome;
import com.freshdirect.deliverypass.ejb.DlvPassManagerHome;
import com.freshdirect.erp.ejb.ErpEWalletHome;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.accounts.external.ExternalAccountManagerHome;
import com.freshdirect.fdstore.ejb.FDFactoryHome;
import com.freshdirect.fdstore.ejb.FDFactorySB;
import com.freshdirect.fdstore.ewallet.ejb.EwalletServiceHome;
import com.freshdirect.fdstore.ewallet.impl.ejb.MasterpassServiceHome;
import com.freshdirect.fdstore.ewallet.impl.ejb.MasterpassServiceHome;
import com.freshdirect.fdstore.survey.ejb.FDSurveyHome;
import com.freshdirect.fdstore.survey.ejb.FDSurveySB;
import com.freshdirect.fdstore.zone.ejb.FDZoneInfoHome;
import com.freshdirect.fdstore.zone.ejb.FDZoneInfoSB;
import com.freshdirect.giftcard.ejb.GiftCardManagerHome;
import com.freshdirect.mail.ejb.MailerGatewayHome;
import com.freshdirect.monitor.ejb.ErpMonitorHome;
import com.freshdirect.payment.ejb.PaymentManagerHome;
import com.freshdirect.smartstore.ejb.OfflineRecommenderHome;
import com.freshdirect.smartstore.ejb.OfflineRecommenderSB;
import com.freshdirect.smartstore.ejb.ScoreFactorHome;
import com.freshdirect.smartstore.ejb.SmartStoreServiceConfigurationHome;
import com.freshdirect.smartstore.ejb.SmartStoreServiceConfigurationSB;
import com.freshdirect.smartstore.ejb.VariantSelectionHome;
import com.freshdirect.smartstore.ejb.VariantSelectionSB;
import com.freshdirect.fdstore.temails.ejb.TEmailInfoHome;
import com.freshdirect.mail.ejb.TEmailerGatewayHome;

public class FDServiceLocator extends ERPServiceLocator {

    static FDServiceLocator INSTANCE = null;
    
    public static FDServiceLocator getInstance() {
        try {
            if (INSTANCE == null) {
                INSTANCE = new FDServiceLocator(FDStoreProperties.getInitialContext());
            }
            return INSTANCE;
        } catch (NamingException e) {
            throw new FDRuntimeException(e);
        }
    }
    
    /**
     * For testing purposes only.
     * @param instance
     */
    public static void setInstance(FDServiceLocator instance) {
        INSTANCE = instance;
    }
    
    /**
     * @deprecated FDServiceLocator.getInstance() probably a better choice instead of creating a new instance.
     */
    public FDServiceLocator() {
    }

    
    /**
     * FDServiceLocator.getInstance() probably a better choice instead of creating a new instance.
     */
    public FDServiceLocator(Context ctx) {
        super(ctx);
    }

    public FDCustomerHome getFdCustomerHome() {
        try {
            return (FDCustomerHome) getRemoteHome("java:comp/env/ejb/FDCustomer");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }

    public ErpCustomerManagerHome getErpCustomerManagerHome() {
        try {
            return (ErpCustomerManagerHome) getRemoteHome("freshdirect.erp.CustomerManager");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }

    public ErpFraudPreventionHome getErpFraudHome() {
        try {
            return (ErpFraudPreventionHome) getRemoteHome("java:comp/env/ejb/FraudManager");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }

    public DlvManagerHome getDlvManagerHome() {
        try {
            return (DlvManagerHome) getRemoteHome("java:comp/env/ejb/DlvManager");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }

    public PaymentManagerHome getPaymentManagerHome() {
        try {
            return (PaymentManagerHome) getRemoteHome("java:comp/env/ejb/PaymentManager");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }

    public DlvPassManagerHome getDlvPassManagerHome() {
        try {
            return (DlvPassManagerHome) getRemoteHome("java:comp/env/ejb/DlvPassManager");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }

    public GiftCardManagerHome getGiftCardGManagerHome() {
        try {
            return (GiftCardManagerHome) getRemoteHome("java:comp/env/ejb/GiftCardManager");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }

    public ActivityLogHome getActivityLogHome() {
        try {
            return (ActivityLogHome) getRemoteHome("freshdirect.customer.ActivityLog");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    
    public FDSurveyHome getSurveyHome() {
        try {
            return (FDSurveyHome) getRemoteHome(FDStoreProperties.getFDSurveyHome());
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    
    
    public FDCustomerManagerHome getFDCustomerManagerHome() {
        try {
            return (FDCustomerManagerHome) getRemoteHome(FDStoreProperties.getFDCustomerManagerHome());
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    
    public EwalletServiceHome getEwalletServiceHome() {
        try {
            return (EwalletServiceHome) getRemoteHome(FDStoreProperties.getEwalletServiceHome());
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    
    public ExternalAccountManagerHome getExternalLoginManagerHome() {
        try {
            return (ExternalAccountManagerHome) getRemoteHome(FDStoreProperties.getExternalAccountManagerHome());
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
 
    
	public ErpEWalletHome getErpEWalletHome() {
        try {
            return (ErpEWalletHome) getRemoteHome(FDStoreProperties.getErpEWalletHome());
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    
    public MasterpassServiceHome getMasterpassServiceHome() {
        try {
            return (MasterpassServiceHome) getRemoteHome(FDStoreProperties.getMPServiceHome());
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    public ErpSaleHome getErpSaleHome() {
        try {
            return (ErpSaleHome) getRemoteHome("freshdirect.erp.Sale");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }

    public ErpCustomerHome getErpCustomerHome() {
        try {
            return (ErpCustomerHome) getRemoteHome("java:comp/env/ejb/ErpCustomer");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }

    public MailerGatewayHome getMailerHome() {
        try {
            return (MailerGatewayHome) getRemoteHome("freshdirect.mail.MailerGateway");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    
    public ErpMonitorHome getErpMonitorHome() {
        try {
            return (ErpMonitorHome) getRemoteHome("freshdirect.monitor.Monitor");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }

    
    public FDSurveySB getSurveySessionBean() {
        try {
            return getSurveyHome().create();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } catch (CreateException e) {
            throw new EJBException(e);
        }
    }
    
    public FDCustomerManagerSB getFDCustomerManagerSessionBean() {
        try {
            return getFDCustomerManagerHome().create();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } catch (CreateException e) {
            throw new EJBException(e);
        }
    }
    
    public OfflineRecommenderHome getOfflineRecommenderHome() {
        try {
            return (OfflineRecommenderHome) getRemoteHome(OfflineRecommenderHome.JNDI_HOME);
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    
    public OfflineRecommenderSB getOfflineRecommender() {
        try {
            return getOfflineRecommenderHome().create();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } catch (CreateException e) {
            throw new EJBException(e);
        }
    }
    
    public VariantSelectionHome getVariantSelectionHome() {
        try {
            return (VariantSelectionHome) getRemoteHome("freshdirect.smartstore.VariantSelection");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    
    public VariantSelectionSB getVariantSelectionSessionBean() {
        try {
            return getVariantSelectionHome().create();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } catch (CreateException e) {
            throw new EJBException(e);
        }
    }
    
    public FDZoneInfoHome getFDZoneInfoHome() {
        try {
            return (FDZoneInfoHome) getRemoteHome("freshdirect.fdstore.ZoneInfoManager");
        } catch (NamingException e) {
            throw new EJBException(e);
        }        
    }
    
    public FDZoneInfoSB getFDZoneInfoSessionBean() {
        try {
            return getFDZoneInfoHome().create();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } catch (CreateException e) {
            throw new EJBException(e);
        }        
    }
    
    public FDFactoryHome getFDFactoryHome() {
        try {
            return (FDFactoryHome) getRemoteHome( FDStoreProperties.getFDFactoryHome() );
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    
    public FDFactorySB getFDFactorySB() {
        try {
            return getFDFactoryHome().create();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } catch (CreateException e) {
            throw new EJBException(e);
        }
    }

    // get service configuration home bean
    private SmartStoreServiceConfigurationHome getServiceConfigurationHome() {
        try {
            return (SmartStoreServiceConfigurationHome) getRemoteHome("freshdirect.smartstore.SmartStoreServiceConfiguration");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    
    public SmartStoreServiceConfigurationSB getSmartStoreServiceConfiguration() {
        try {
            return getServiceConfigurationHome().create();
        } catch (RemoteException e) {
            throw new EJBException(e);
        } catch (CreateException e) {
            throw new EJBException(e);
        }
    }
    
    public ScoreFactorHome getScoreFactorHome() throws NamingException {
        return (ScoreFactorHome) getRemoteHome("freshdirect.smartstore.ScoreFactorHome");
    }
    
    public TEmailInfoHome getTMailerHome() {
        try {
            return (TEmailInfoHome) getRemoteHome("java:comp/env/ejb/TEmailInfoManager");//freshdirect.fdstore.TEmailInfoManager");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    
    public TEmailerGatewayHome getTMailerGatewayHome() {
        try {
            return (TEmailerGatewayHome) getRemoteHome("freshdirect.mail.TMailerGateway");
        } catch (NamingException e) {
            throw new EJBException(e);
        }
    }
    

}
