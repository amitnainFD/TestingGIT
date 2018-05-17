package com.freshdirect.fdstore.coremetrics;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.util.log.LoggerFactory;


/**
 * CoreMetrics Context
 * 
 * Context class designed for CoreMetrics clients.
 * There are several ways to obtain context objects
 * 
 * Get current CM context: <code>CmContext#getContext()</code>
 * Create context for particular store: <code>CmContext#createContextFor(EnumEStoreId, CmFacade, boolean)</code>
 * Create Global context: <code>CmContext#createGlobalContext()</code>
 * 
 * Also builder is provided to setup custom contexts.
 * 
 * Constants
 * 
 * {@link CmContext#ENTERPRISE_ID} CoreMetrics Enterprise ID assigned to FreshDirect.
 * 
 * 
 * Context Properties
 * 
 * {@link CmContext#compoundId}: provides compound client ID (eg. enterprise ID and CM client ID) required for reporting.
 * {@link CmContext#clientId}: provides CM Client ID required for CDF generation task
 * {@link CmContext#testAccount}: indicated that CM client runs with 'test' ID. False value means production mode.
 * {@link CmContext#instance}: value of {@link CmInstance} describes the context in which the client lives.
 * {@link CmContext#isEnabled}: <code>true</code> value enables reporting to CoreMetrics site. 
 * 
 * Helper Methods
 * 
 * {@link CmContext#prefixedCategoryId(String)} This utility method prefixes Category ID with CM Instance name.
 * 
 * @author segabor
 * 
 * @see FDStoreProperties#getCoremetricsClientId()
 * 
 * @ticket APPDEV-4337
 *
 */
public class CmContext implements Serializable {
	private static final long serialVersionUID = -2813839304782457210L;

	private static final Logger LOGGER = LoggerFactory.getInstance(CmContext.class);
	
	
	private static CmContext sharedInstance = null;

	/**
	 * Enterprise ID := FD client ID in CoreMetrics system
	 */
	public static final String ENTERPRISE_ID 		= "51640000";
	public static final String ENTERPRISE_ID_TEST	= "81640000";
	
	
	/**
	 * If enabled, CM sends events from storefront
	 */
	private boolean isEnabled = true;
	
	
	/**
	 * CoreMetrics instance
	 */
	private CmInstance instance = CmInstance.FDW;


	/**
	 * CoreMetrics Compound Client ID
	 * 
	 * Compound ID consists of ENTERPRISE ID and Client ID, separated by a pipe symbol
	 * 
	 * @see CmInstance
	 */
	private String compoundId;

	/**
	 * Context running with test account
	 */
	private boolean testAccount;

	/**
	 * CoreMetrics client ID
	 * 
	 * @see FDStoreProperties#getCoremetricsClientId()
	 */
	private String clientId = null;
	
	/**
	 * CoreMetrics site ID 
	 */
	private String siteId = null;


	protected void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
	
	protected void setInstance(CmInstance instance) {
		this.instance = instance;
	}
	
	public CmInstance getInstance() {
		return instance;
	}
	
	/**
	 * 
	 * @param compoundId
	 */
	protected void setCompoundId(String compoundId) {
		this.compoundId = compoundId;
	}
	
	public String getCompoundId() {
		return compoundId;
	}
	
	protected void setTestAccount(boolean testAccount) {
		this.testAccount = testAccount;
	}
	
	public boolean isTestAccount() {
		return testAccount;
	}


	/**
	 * Set client and compound IDs
	 * 
	 * @param clientId
	 */
	protected void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * Return CM Client ID
	 * @return
	 */
	public String getClientId() {
		return clientId;
	}
	

	protected void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	
	
	public String getSiteId() {
		return siteId;
	}
	
	/**
	 * Default context getter
	 * @return
	 */
	public static CmContext getContext() {
		if (sharedInstance == null) {
			synchronized(CmContext.class) {
				if (sharedInstance == null) {
					sharedInstance = new Builder()
					// group #1 setters
						.setDefaultClientId()
					// computed values
						.setCmInstance()
						/* .setCompoundId() */
						.setEnabled()
					// bake context
						.build();
					
					LOGGER.info("CmContext " + sharedInstance + " is created");
				}
			}
		}

		return sharedInstance;
	}



	/**
	 * Build context based on parameters
	 * 
	 * @param eStore
	 * @param facade
	 * @param test
	 * @return
	 */
	public static CmContext createContextFor(EnumEStoreId eStore, CmFacade facade, boolean test) {
		CmContext ctx = new Builder()
			// group #2 setters
			.setEStoreId(eStore)
			.setFacade(facade)
			.setTestAcc(test)
			// computed values
			.setCmInstance()
			/* .setCompoundId() */
			.setEnabled()
			// bake context
			.build();
		
		LOGGER.info("CmContext " + ctx + " is created");

		return ctx;
	}
	
	
	
	public static CmContext createGlobalContext() {
		CmContext ctx = new Builder()
			// group #1 setters
			.setDefaultClientId()
			.setGlobal()
			// computed values
			.setCmInstance()
			/* .setCompoundId() */
			// bake context
			.build();
	
		LOGGER.info("CmContext " + ctx + " is created");
	
		return ctx;
	}
	
	
	/**
	 * Use {@link CmContext#getContext()} instead of using constructor.
	 */
	public CmContext() {
	}



	/**
	 * Return category ID prefixed with CM instance name
	 * Note, that categories are not prefixed when context is global
	 * 
	 * @param categoryId
	 * @return
	 */
	public String prefixedCategoryId(String categoryId) {
		return categoryId != null
				? (instance != null && CmInstance.GLOBAL != instance)
					? instance.name() + "_" + categoryId
					: categoryId
				: null
		;
	}

	@Override
	public String toString() {

		if (instance == null || CmInstance.UNKNOWN == instance) {
			return "UNKNOWN/UNDEFINED";
		} else {
			StringBuilder s = new StringBuilder();

			if (CmInstance.GLOBAL == instance) {
				s.append("{")
					.append(instance.name())
					.append(", store: '<multi>'")
					.append(", facade: 'N/A'")
					.append(", test: '"+ testAccount + "'")
					.append(", clientId: '"+ clientId + "'")
					.append(", siteId: '"+ siteId + "'")
					.append("}")
				;

			} else {
				s.append("{")
					.append(instance.name())
					.append(", store: '" + instance.getEStoreId() + "'")
					.append(", facade: '"+ instance.getFacade() + "'")
					.append(", test: '"+ testAccount + "'")
					.append(", clientId: '"+ clientId + "'")
					.append(", siteId: '"+ siteId + "'")
					.append("}")
				;

			}
			
			return s.toString();
		}
	}
	
	

	/**
	 * CoreMetrics context builder
	 * 
	 * @author segabor
	 *
	 */
	public static class Builder {
		// group #1
		private String clientId = null;
		private boolean global = false; // retained for global configuration
		
		// group #2
		private EnumEStoreId eStoreId = null;
		private CmFacade facade = null;
		
		// calculated
		private boolean isEnabled;
		// calculated
		private CmInstance instance = CmInstance.FDW;
		// calculated / group #2
		private boolean testAcc = true;
		// by default, compound client id consists of FD ID and FDW client ID (test)
		// calculated
		@Deprecated
		private String compoundId = ENTERPRISE_ID + "|" + FDStoreProperties.getCoremetricsClientId();
		
		
		// --- group #1 setters ---

        /**
         * Read out client ID from fdstore.properties file (required)
         * 
         * @see {@link FDStoreProperties#getCoremetricsClientId()}
         * 
         * @return
         */
		public Builder setDefaultClientId() {
			this.clientId = FDStoreProperties.getCoremetricsClientId(); return this;
		}

		
		/**
		 * Set client ID explicitly (required, alternative to {@link #setDefaultClientId()})
		 * 
		 * @param clientId
		 * @return
		 */
		public Builder setClientId(String clientId) {
			this.clientId = clientId; return this;
		}

		/**
		 * Set Global context mode (optional)
		 * @return
		 */
		public Builder setGlobal() {
			this.global = true; return this;
		}

		// --- group #2 setters ---
		
		public Builder setEStoreId(EnumEStoreId eStoreId) {
			this.eStoreId = eStoreId; return this;
		}
		
		public Builder setFacade(CmFacade facade) {
			this.facade = facade; return this;
		}
		
		public Builder setTestAcc(boolean testAcc) {
			this.testAcc = testAcc; return this;
		}
		
		
		// --- configurators ---
		
		/**
		 * Determine CoreMetrics instance from
		 * values set via group #1 or group #2 setters.
		 * 
		 * @return
		 */
		public Builder setCmInstance() {
			if (clientId != null) {
				// Try to determine instance from client ID
				//

				CmInstance result = CmInstance.lookupByClientId(clientId);
				if (result != null) {
					this.instance = result; this.testAcc = false;
					LOGGER.debug("Found production instance " + result);
				} else {
					result = CmInstance.lookupByTestClientId(clientId);
					if (result != null) {
						this.instance = result; this.testAcc = true;
						LOGGER.debug("Found test instance " + result);
					} else {
						throw new IllegalStateException("Builder was not able to determine CM instance from client ID " + clientId);
					}
				}
			} else if ( eStoreId != null && facade != null ) {
				// Guess instance from given store and facade
				//

				for (CmInstance i : CmInstance.values()) {
					if (CmInstance.UNKNOWN == i)
						continue;
					
					if (eStoreId == i.getEStoreId() && facade == i.getFacade()) {
						this.instance = i;
						this.clientId = this.instance.getClientId( this.testAcc );
						break;
					}
				}
			} else {
				throw new IllegalStateException("Error configuring instance!");
			}
			
			// adjust global mode if found instance is global
			if (CmInstance.GLOBAL == this.instance) {
				global = true;
			}

			return this;
		}


		/**
		 * Determines whether CM reporting is enabled for an instance (optional)
		 * Defaults to off
		 * 
		 * May not be called before {@link CmContext.Builder#setCmInstance()
		 */
		public Builder setEnabled() {
			// CM events are turned on by default
			this.isEnabled = !( CmInstance.SDSW == instance ); return this;
		}

		public CmContext build() {
			CmContext ctx = new CmContext();

			final String siteId = global
					? CmInstance.GLOBAL.getSiteId()
					: this.instance.getSiteId();
			
			if (global) {
				// Do not send events as global ctx is abstract
				ctx.setEnabled(false);
				ctx.setInstance(CmInstance.GLOBAL);
				ctx.setTestAccount(testAcc);
				ctx.setClientId( clientId );
				ctx.setSiteId( siteId );
			} else {
				ctx.setEnabled(isEnabled);
				ctx.setInstance(instance);
				ctx.setTestAccount(testAcc);
				ctx.setClientId( clientId );
				ctx.setSiteId( siteId );
			}

			if (clientId != null) {
				StringBuilder bld = new StringBuilder();
				if (testAcc) {
					bld.append(ENTERPRISE_ID_TEST);
				} else {
					bld.append(ENTERPRISE_ID);
				}

				bld.append("|");

				if (CmInstance.FDW == this.instance) {
					bld.append( this.clientId );
				} else {
					bld.append( siteId );
				}

				ctx.setCompoundId( bld.toString() );
			}

			return ctx;
		}
	}
	
	/*** TEST CODE FOR APPDEV-4496
	public static void main(String[] args) {
		
		
		for (final EnumEStoreId eStore : EnumEStoreId.values()) {
			for (final CmFacade facade : CmFacade.values()) {
				CmContext ctx1 = CmContext.createContextFor(eStore, facade, false);
				CmContext ctx2 = CmContext.createContextFor(eStore, facade, true);
				
				System.out.println( eStore + "/" + facade + " --> " + ctx1.getCompoundId() );
				System.out.println( eStore + "/" + facade + " --> " + ctx2.getCompoundId() );
			}
		}
	}
	***/
}
