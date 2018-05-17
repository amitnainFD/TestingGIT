/**
 * 
 */
package com.freshdirect.fdstore.customer.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;

import com.freshdirect.customer.ejb.ActivityLogHome;
import com.freshdirect.customer.ejb.ErpCustomerHome;
import com.freshdirect.customer.ejb.ErpCustomerManagerHome;
import com.freshdirect.customer.ejb.ErpFraudPreventionHome;
import com.freshdirect.customer.ejb.ErpSaleHome;
import com.freshdirect.delivery.ejb.DlvManagerHome;
import com.freshdirect.deliverypass.ejb.DlvPassManagerHome;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.giftcard.ejb.GiftCardManagerHome;
import com.freshdirect.mail.ejb.MailerGatewayHome;
import com.freshdirect.monitor.ejb.ErpMonitorHome;
import com.freshdirect.monitor.ejb.ErpMonitorSB;
import com.freshdirect.payment.ejb.PaymentManagerHome;
import com.freshdirect.fdstore.temails.ejb.TEmailInfoHome;
import com.freshdirect.fdstore.temails.ejb.TEmailInfoSB;

/**
 * @author zsombor
 * 
 */
public class FDSessionBeanSupport extends SessionBeanSupport {

	
	protected FDServiceLocator LOCATOR = FDServiceLocator.getInstance();
 
    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getActivityLogHome()
     */
    protected ActivityLogHome getActivityLogHome() {
        return LOCATOR.getActivityLogHome();
    }

    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getDlvManagerHome()
     */
    protected DlvManagerHome getDlvManagerHome() {
        return LOCATOR.getDlvManagerHome();
    }

    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getDlvPassManagerHome()
     */
    protected DlvPassManagerHome getDlvPassManagerHome() {
        return LOCATOR.getDlvPassManagerHome();
    }

    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getErpCustomerHome()
     */
    protected ErpCustomerHome getErpCustomerHome() {
        return LOCATOR.getErpCustomerHome();
    }

    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getErpCustomerManagerHome()
     */
    protected ErpCustomerManagerHome getErpCustomerManagerHome() {
        return LOCATOR.getErpCustomerManagerHome();
    }

    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getErpFraudHome()
     */
    protected ErpFraudPreventionHome getErpFraudHome() {
        return LOCATOR.getErpFraudHome();
    }

    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getErpSaleHome()
     */
    protected ErpSaleHome getErpSaleHome() {
        return LOCATOR.getErpSaleHome();
    }

    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getFdCustomerHome()
     */
    protected FDCustomerHome getFdCustomerHome() {
        return LOCATOR.getFdCustomerHome();
    }

    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getGiftCardGManagerHome()
     */
    protected GiftCardManagerHome getGiftCardGManagerHome() {
        return LOCATOR.getGiftCardGManagerHome();
    }

    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getMailerHome()
     */
    protected MailerGatewayHome getMailerHome() {
        return LOCATOR.getMailerHome();
    }

    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getPaymentManagerHome()
     */
    protected PaymentManagerHome getPaymentManagerHome() {
        return LOCATOR.getPaymentManagerHome();
    }
    
    protected FDCustomerManagerSB getFDCustomerManager() {
        return LOCATOR.getFDCustomerManagerSessionBean();
    }

    

    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getErpMonitorHome()
     */
    protected ErpMonitorHome getErpMonitorHome() {
        return LOCATOR.getErpMonitorHome();
    }
    
    protected ErpMonitorSB getErpMonitor() throws RemoteException, CreateException {
        return getErpMonitorHome().create();
    }
    
    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getMailerHome()
     */
    protected TEmailInfoHome getTMailerHome() {
        return LOCATOR.getTMailerHome();
    }
}
