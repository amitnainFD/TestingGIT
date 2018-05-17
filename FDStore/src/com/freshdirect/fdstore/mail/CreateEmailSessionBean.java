package com.freshdirect.fdstore.mail;

import javax.xml.transform.TransformerException;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDAuthenticationException;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.customer.FDUser;
import com.freshdirect.fdstore.customer.ejb.FDSessionBeanSupport;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;
import com.freshdirect.fdstore.standingorders.FDStandingOrdersManager;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.mail.XMLEmailI;
import com.freshdirect.framework.xml.XSLTransformer;

public class CreateEmailSessionBean extends FDSessionBeanSupport {
	private static final long serialVersionUID = -6034395607525171051L;

	public String getTextEmail(String orderId) throws FDResourceException, FDAuthenticationException, TransformerException {
		FDOrderI order = FDCustomerManager.getOrder(orderId);
		FDUser fdUser = FDCustomerManager.getFDUser(new FDIdentity(order.getCustomerId()));
		FDCustomerInfo customer = FDCustomerManager.getCustomerInfo(fdUser.getIdentity());
		FDStandingOrder standingOrder = FDStandingOrdersManager.getInstance().load(new PrimaryKey(order.getStandingOrderId()));
		customer.setHtmlEmail(false);
		return renderEmail(FDEmailFactory.getInstance().createStandingOrderErrorEmail(customer, standingOrder));
	}

	public String getHtmlEmail(String orderId) throws FDResourceException, FDAuthenticationException, TransformerException {
		FDOrderI order = FDCustomerManager.getOrder(orderId);
		FDUser fdUser = FDCustomerManager.getFDUser(new FDIdentity(order.getCustomerId()));
		FDCustomerInfo customer = FDCustomerManager.getCustomerInfo(fdUser.getIdentity());
		FDStandingOrder standingOrder = FDStandingOrdersManager.getInstance().load(new PrimaryKey(order.getStandingOrderId()));
		customer.setHtmlEmail(true);
		return renderEmail(FDEmailFactory.getInstance().createStandingOrderErrorEmail(customer, standingOrder));
	}

	private String renderEmail(XMLEmailI email) throws TransformerException {
		XSLTransformer transformer = new XSLTransformer();
		return transformer.transform(email.getXML(), email.getXslPath());
	}
}
