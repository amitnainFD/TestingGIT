/**
 * @author ekracoff
 * Created on Aug 9, 2005*/

package com.freshdirect.fdstore.oas.ejb;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import javax.mail.MessagingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.mail.ErpMailSender;


public class InventoryUpdater {
	private final static Category LOGGER = LoggerFactory.getInstance(InventoryUpdater.class);

	public static void main(String[] args) {
		Context ctx = null;
		try {
			ctx = getInitialContext();
			AdServerGatewayHome home = (AdServerGatewayHome) ctx.lookup("freshdirect.fdstore.AdServerGateway");
			AdServerGatewaySB sb = home.create();
			
			long startTime = System.currentTimeMillis();
			LOGGER.info("-- Starting Inventory Update -- ");
			sb.run();
			LOGGER.info("-- Finished -- process took " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(new StringBuilder("InventoryUpdater failed with Exception...").append(e.toString()).toString());
			LOGGER.error(e);
			email(Calendar.getInstance().getTime(), e.toString());
		} finally {
			try {
				if (ctx != null) {
					ctx.close();
					ctx = null;
				}
			} catch (NamingException ne) {
				//could not do the cleanup
			}
		}
	}

	static public Context getInitialContext() throws NamingException {
		Hashtable h = new Hashtable();
		h.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
		h.put(Context.PROVIDER_URL, ErpServicesProperties.getProviderURL());
		return new InitialContext(h);
	}
	
	private static void email(Date processDate, String exceptionMsg) {
		// TODO Auto-generated method stub
		try {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, MMM d, yyyy");
			String subject="InventoryUpdater:	"+ (processDate != null ? dateFormatter.format(processDate) : " date error");

			StringBuffer buff = new StringBuffer();

			buff.append("<html>").append("<body>");			
			
			if(exceptionMsg != null) {
				buff.append("b").append(exceptionMsg).append("/b");
			}
			buff.append("</body>").append("</html>");

			ErpMailSender mailer = new ErpMailSender();
			mailer.sendMail(ErpServicesProperties.getCronFailureMailFrom(),
					ErpServicesProperties.getCronFailureMailTo(),ErpServicesProperties.getCronFailureMailCC(),
					subject, buff.toString(), true, "");
			
		}catch (MessagingException e) {
			LOGGER.warn("Error Sending InventoryUpdater Cron report email: ", e);
		}
		
	}
}
