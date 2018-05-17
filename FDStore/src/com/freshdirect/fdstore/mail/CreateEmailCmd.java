package com.freshdirect.fdstore.mail;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDAuthenticationException;
import com.freshdirect.framework.util.log.LoggerFactory;

public class CreateEmailCmd {
	private static final Logger LOG = LoggerFactory.getInstance(CreateEmailCmd.class);

	public static void main(String[] args) throws FDResourceException, FDAuthenticationException, RemoteException, CreateException, TransformerException, FileNotFoundException {
		String orderId = args[0];
		CreateEmailHome home = lookupCreateEmailHome();
		CreateEmailSB sb = home.create();
		PrintWriter w = new PrintWriter(new FileOutputStream("email.txt"));
		w.println(sb.getTextEmail(orderId));
		w.close();
		w = null;
		w = new PrintWriter(new FileOutputStream("email.html"));
		w.println(sb.getHtmlEmail(orderId));
		w.close();
		w = null;
	}

	private static CreateEmailHome lookupCreateEmailHome() throws FDResourceException {
		Context ctx = null;
		try {
			ctx = FDStoreProperties.getInitialContext();
			return (CreateEmailHome) ctx.lookup(CreateEmailHome.JNDI_HOME);
		} catch (NamingException ne) {
			throw new FDResourceException(ne);
		} finally {
			try {
				if (ctx != null) {
					ctx.close();
				}
			} catch (NamingException ne) {
				LOG.warn("cannot close Context while trying to cleanup", ne);
			}
		}
	}
}
