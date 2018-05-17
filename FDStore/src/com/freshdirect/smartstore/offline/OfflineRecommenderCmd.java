package com.freshdirect.smartstore.offline;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.mail.MessagingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.mail.ErpMailSender;
import com.freshdirect.smartstore.ejb.OfflineRecommenderHome;
import com.freshdirect.smartstore.ejb.OfflineRecommenderSB;

public class OfflineRecommenderCmd {
	private static final Logger LOG = Logger
			.getLogger(OfflineRecommenderCmd.class);

	private static Iterator<String> customerIterator;	

	public static void main(String[] args) {
		int days = 365;
		int age = 5;
		int threadCount = 5;
		int windowLength = 300;
		String[] siteFeatures = null;
		try {
			days = FDStoreProperties.getOfflineRecommenderNoOfRecentDays();
		} catch (NumberFormatException e) {
			LOG.fatal("no of recent days must be an integer");
			System.exit(-1);
		}
		try {
			age = FDStoreProperties.getOfflineRecommenderMaxAge();
		} catch (NumberFormatException e) {
			LOG.fatal("maximum recommendation age must be an integer");
			System.exit(-1);
		}
		try {
			threadCount = FDStoreProperties.getOfflineRecommenderThreadCount();
		} catch (NumberFormatException e) {
			LOG.fatal("thread count must be an integer");
			System.exit(-1);
		}
		try {
			windowLength = FDStoreProperties
					.getOfflineRecommenderWindowLength();
		} catch (NumberFormatException e) {
			LOG.fatal("window length must be an integer");
			System.exit(-1);
		}
		try {
			siteFeatures = FDStoreProperties
					.getOfflineRecommenderSiteFeatures();
		} catch (NullPointerException e) {
			LOG.fatal("site features parameter is required");
			System.exit(-1);
		}
		validateSiteFeatures(siteFeatures);
		LOG.info("no of recent days: " + days);
		LOG.info("max recommendation age in days: " + age);
		LOG.info("site features: " + arrayToString(siteFeatures));
		LOG.info("thread count: " + threadCount);
		LOG.info("time window length in minutes: " + windowLength);
		Set<String> customerIds = lookupCustomers(days, age);
		customerIterator = customerIds.iterator();
		Thread[] workers = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			workers[i] = new Thread(new Worker(siteFeatures,
					windowLength * 60 * 1000), "Worker #" + (i + 1));
			workers[i].start();
			LOG.info("started " + workers[i].getName());
		}
		for (int i = 0; i < threadCount; i++) {
			try {
				workers[i].join();
			} catch (InterruptedException e) {
				LOG
						.info("join for " + workers[i].getName()
								+ " interrupted", e);
			}
			LOG.info(workers[i].getName() + " has stopped");
		}
	}

	private static class Worker implements Runnable {
		long endTime;
		String[] siteFeatures;

		public Worker(String[] siteFeatures, long windowLength) {
			endTime = System.currentTimeMillis();
			endTime += windowLength;
			this.siteFeatures = siteFeatures;
		}

		@Override
		public void run() {
			int nCustomers = 0;
			while (System.currentTimeMillis() < endTime) {
				Set<String> customerIds = getNextNCustomers(10);
				if (customerIds.isEmpty()) {
					LOG.info("no more customer ids - halting");
					break;
				}
				for (String customerId : customerIds)
					generateRecommendations(customerId, siteFeatures);
				nCustomers += customerIds.size();
				if (nCustomers % 100 < 5 || nCustomers % 100 > 95)
					LOG.info("processed " + nCustomers + " customers so far");
			}
			if (System.currentTimeMillis() >= endTime) {
				LOG.info("run out the time window - halting");
			}
			LOG.info("processed " + nCustomers + " customers overall");
		}
	}

	private static synchronized Set<String> getNextNCustomers(int n) {
		if (customerIterator == null)
			throw new IllegalStateException();
		Set<String> customerIds = new HashSet<String>(n);
		for (int i = 0; i < n; i++)
			if (customerIterator.hasNext())
				customerIds.add(customerIterator.next());
			else
				break;
		return customerIds;
	}

	private static void validateSiteFeatures(String[] siteFeatures) {
		for (int i = 0; i < siteFeatures.length; i++)
			try {
				lookupOfflineRecommenderHome();
				OfflineRecommenderSB sb = offlineRecommenderHome.get().create();
				sb.checkSiteFeature(siteFeatures[i]);
				LOG.info("site feature valid: " + siteFeatures[i]);
			} catch (Exception e) {
				invalidateOfflineRecommenderHome();
				LOG.error("error while validating site feature: "
						+ siteFeatures[i], e);
				LOG.fatal("exiting: failed to validate site feature");
				System.exit(-1);
			}
	}

	private static Set<String> lookupCustomers(int days, int age) {
		try {
			lookupOfflineRecommenderHome();
			OfflineRecommenderSB sb = offlineRecommenderHome.get().create();
			return lookupCustomers(sb, days, age);
		} catch (Exception e) {
			invalidateOfflineRecommenderHome();
			LOG.error("error while retrieving recent customers", e);
			LOG.fatal("exiting: failed to retrieve recent customers");
			LOG.info(new StringBuilder("OfflineRecommenderCron failed with Exception...").append(e.toString()).toString());
			LOG.error(e);
			email(Calendar.getInstance().getTime(), e.toString());
			System.exit(-1);
			return null;
		}
	}

    /**
     * @param sb
     * @param days
     * @param age
     * @return
     * @throws RemoteException
     * @throws FDResourceException
     */
    public static Set<String> lookupCustomers(OfflineRecommenderSB sb, int days, int age) throws RemoteException, FDResourceException {
        Set<String> customerIds = sb.getRecentCustomers(days);
        LOG.info("customers counted for the last " + days + " days: "
        		+ customerIds.size());
        int count = sb.removeOldRecommendation(days);
        
        LOG.info("removed " + count + " offline recommendation which are older than " + days+ " days.");
        
        Set<String> updatedCustomerIds = sb.getUpdatedCustomers(age);
        LOG.info("customers updated in the last " + age + " days: "
        		+ updatedCustomerIds.size());
        customerIds.removeAll(updatedCustomerIds);
        LOG.info("need to update customers: " + customerIds.size());
        return customerIds;
    }

	private static void generateRecommendations(String customerId,
			String[] siteFeatures) {
		try {
			lookupOfflineRecommenderHome();
			OfflineRecommenderSB sb = offlineRecommenderHome.get().create();
			int n = sb.recommend(siteFeatures, customerId, null);
			//LOG.debug("recommendations (" + arrayToString(siteFeatures)
			//		+ ") for customer #" + customerId + ": " + n);
		} catch (Exception e) {
			invalidateOfflineRecommenderHome();
			LOG.error("exception while generating "
					+ arrayToString(siteFeatures)
					+ " recommendations for customer #" + customerId, e);
			LOG.info(new StringBuilder("OfflineRecommenderCron failed with Exception...").append(arrayToString(siteFeatures)+" recommendations for customer #"+ customerId).append(e.toString()).toString());
			LOG.error(e);
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			email(Calendar.getInstance().getTime(), sw.getBuffer().toString());
		}
	}

	private static Context getInitialContext() throws NamingException {
		Hashtable h = new Hashtable();
		h.put(Context.INITIAL_CONTEXT_FACTORY,
				"weblogic.jndi.WLInitialContextFactory");
		h.put(Context.PROVIDER_URL, ErpServicesProperties.getProviderURL());
		return new InitialContext(h);
	}

	private static ThreadLocal<OfflineRecommenderHome> offlineRecommenderHome = new ThreadLocal<OfflineRecommenderHome>();

	private static void lookupOfflineRecommenderHome()
			throws FDResourceException {
		if (offlineRecommenderHome.get() != null) {
			return;
		}
		Context ctx = null;
		try {
			ctx = getInitialContext();
			offlineRecommenderHome.set((OfflineRecommenderHome) ctx
					.lookup(OfflineRecommenderHome.JNDI_HOME));
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

	private static void invalidateOfflineRecommenderHome() {
		offlineRecommenderHome.set(null);
	}

	private static String arrayToString(String[] strs) {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		if (strs.length > 0)
			buf.append(strs[0]);
		for (int i = 1; i < strs.length; i++) {
			buf.append(", ");
			buf.append(strs[0]);
		}
		buf.append("]");
		return buf.toString();
	}
	
	private static void email(Date processDate, String exceptionMsg) {
		// TODO Auto-generated method stub
		try {
			SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, MMM d, yyyy");
			String subject="OfflineRecommenderCron:	"+ (processDate != null ? dateFormatter.format(processDate) : " date error");

			StringBuffer buff = new StringBuffer();

			buff.append("<html>").append("<body>");			
			
			if(exceptionMsg != null) {
				buff.append("Exception is :").append("\n");
				buff.append(exceptionMsg);
			}
			buff.append("</body>").append("</html>");

			ErpMailSender mailer = new ErpMailSender();
			mailer.sendMail(ErpServicesProperties.getCronFailureMailFrom(),
					ErpServicesProperties.getCronFailureMailTo(),ErpServicesProperties.getCronFailureMailCC(),
					subject, buff.toString(), true, "");
			
		}catch (MessagingException e) {
			LOG.warn("Error Sending OfflineRecommenderCron report email: ", e);
		}
		
	}
}
