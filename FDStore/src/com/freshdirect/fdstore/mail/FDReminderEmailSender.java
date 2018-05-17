package com.freshdirect.fdstore.mail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.log4j.Category;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.mail.ErpMailSender;

public class FDReminderEmailSender {

	private static Category LOGGER = LoggerFactory.getInstance(FDReminderEmailSender.class);

	public static void main(String[] args) {
		try {
			List custIds = FDCustomerManager.getReminderListForToday();
			for (Iterator i = custIds.iterator(); i.hasNext();) {
				String id = (String) i.next();
				FDCustomerManager.sendReminderEmail(id);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info(new StringBuilder("Sending Reminder Emails failed with Exception...").append(e.toString()).toString());
			LOGGER.error(e);
			email(Calendar.getInstance().getTime(), e.toString());			
		}		
	}
	
	private static void email(Date processDate, String exceptionMsg) {
		// TODO Auto-generated method stub
		try {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, MMM d, yyyy");
			String subject="Sending Reminder Emails Cron :	"+ (processDate != null ? dateFormatter.format(processDate) : " date error");

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
			LOGGER.warn("Error Sending FDReminderEmailSender cron report email: ", e);
		}
		
	}
}
