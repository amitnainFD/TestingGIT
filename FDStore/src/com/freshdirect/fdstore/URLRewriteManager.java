package com.freshdirect.fdstore;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.customer.ejb.FDCustomerManagerHome;
import com.freshdirect.fdstore.customer.ejb.FDCustomerManagerSB;
import com.freshdirect.framework.core.ServiceLocator;
import com.freshdirect.framework.util.ExpiringReference;
import com.freshdirect.framework.util.log.LoggerFactory;

public class URLRewriteManager {

	private static Category LOGGER = LoggerFactory.getInstance(URLRewriteManager.class);

	private final ServiceLocator locator;
	private static URLRewriteManager instance;

	private ExpiringReference<List<URLRewriteRule>> rewriteRules = new ExpiringReference<List<URLRewriteRule>>(5 * 60 * 1000) {
		protected List<URLRewriteRule> load() {
			return loadRewriteRules();
		}
	};

	private URLRewriteManager() throws NamingException {
		this.locator = new ServiceLocator(FDStoreProperties.getInitialContext());
	}

	public static URLRewriteManager getInstance() {
		if (instance == null) {

			try {
				instance = new URLRewriteManager();
			} catch (NamingException e) {
				throw new FDRuntimeException(e);
			}
		}

		return instance;
	}

	public URLRewriteRule getRedirect(String originalURL) {
		List<URLRewriteRule> lst = this.rewriteRules.get();
		for (Iterator<URLRewriteRule> i = lst.iterator(); i.hasNext();) {
			URLRewriteRule r = i.next();
			if (!r.isDisabled() && r.match(originalURL)) {
				return r;
			}
		}
		return null;
	}

	private List<URLRewriteRule> loadRewriteRules() {
		try {
			FDCustomerManagerSB sb = getFDCustomerManager();
			return sb.loadRewriteRules();
		} catch (Exception e) {
			LOGGER.error("Could not load rewrite rules due to: ", e);
			return Collections.emptyList();
		}
	}

	private FDCustomerManagerSB getFDCustomerManager() {
		try {
			FDCustomerManagerHome home = (FDCustomerManagerHome) locator.getRemoteHome(
				FDStoreProperties.getFDCustomerManagerHome());
			return home.create();
		} catch (Exception e) {
			throw new EJBException("Cannot obtain reference to FDCustomerManagerSB to load rewrite rules", e);
		}
	}
}
