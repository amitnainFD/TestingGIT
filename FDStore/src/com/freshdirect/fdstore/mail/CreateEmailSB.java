package com.freshdirect.fdstore.mail;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;
import javax.xml.transform.TransformerException;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDAuthenticationException;

public interface CreateEmailSB extends EJBObject {
	public String getTextEmail(String orderId) throws FDResourceException, FDAuthenticationException, TransformerException, RemoteException;

	public String getHtmlEmail(String orderId) throws FDResourceException, FDAuthenticationException, TransformerException, RemoteException;
}
