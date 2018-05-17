package com.freshdirect.fdstore.coremetrics;

import java.util.HashMap;
import java.util.Map;

import com.freshdirect.fdstore.EnumEStoreId;

/**
 * CoreMetrics Instance
 * 
 * CM Instances determines the particular "channel"
 * in which CoreMetrics client lives.
 * 
 * Instances consist of the following properties
 * 
 * # Store (FD, FDX, ...)
 * # Facade (Web, Phone, Tablet)
 * 
 * "UNKNOWN" case means instance cannot be determined
 * "GLOBAL" case is retained for Global CDF generation 
 * 
 * @author segabor
 * 
 * @ticket APPDEV-4337
 *
 */
public enum CmInstance {
	UNKNOWN,
	/** FD Storefront */
	FDW(EnumEStoreId.FD, CmFacade.WEB),
	/** FD Phone Apps */
	FDP(EnumEStoreId.FD, CmFacade.PHONE),
	/** FD Tablet App */
	FDT(EnumEStoreId.FD, CmFacade.TABLET),
	/** FDX Storefront */
	SDSW(EnumEStoreId.FDX, CmFacade.WEB),
	/** FDX Phone App */
	SDSP(EnumEStoreId.FDX, CmFacade.PHONE),
	/** FDX Tablet App */
	SDST(EnumEStoreId.FDX, CmFacade.TABLET),
	/** Global Context : no store, no facade */
	GLOBAL(null, null)
	;

	EnumEStoreId eStoreId;
	CmFacade facade;

	CmInstance() {
		this.eStoreId = null;
		this.facade = null;
	}

	CmInstance(EnumEStoreId eStoreId, CmFacade facade) {
		this.eStoreId = eStoreId;
		this.facade = facade;
	}


	public EnumEStoreId getEStoreId() {
		return eStoreId;
	}

	/**
	 * Get up-front type
	 * @see CmFacade 
	 * @return
	 */
	public CmFacade getFacade() {
		return facade;
	}


	private static final Map<String,CmInstance> client2inst = new HashMap<String,CmInstance>(7);
	private static final Map<String,CmInstance> client2inst_tst = new HashMap<String,CmInstance>(7);
	private static final Map<String,CmInstance> site2inst = new HashMap<String,CmInstance>(7);
	static {
		// production client IDs
		client2inst.put("90391309", FDW);
		client2inst.put("51640003", FDP);
		client2inst.put("51640002", FDT);
		client2inst.put("51640006", SDSW);
		client2inst.put("51640004", SDSP);
		client2inst.put("51640005", SDST);

		client2inst.put("51640000", GLOBAL);

		// test client IDs
		client2inst_tst.put("60391309", FDW);
		client2inst_tst.put("81640004", FDP);
		client2inst_tst.put("81640005", FDT);
		client2inst_tst.put("81640006", SDSW);
		client2inst_tst.put("81640002", SDSP);
		client2inst_tst.put("81640007", SDST);

		client2inst_tst.put("81640000", GLOBAL);

		// site IDs
		site2inst.put("33000000", FDW);
		site2inst.put("33000001", FDP);
		site2inst.put("33000002", FDT);
		site2inst.put("33000004", SDSW);
		site2inst.put("33000003", SDSP);
		site2inst.put("33000005", SDST);
		
		site2inst.put("51640000", GLOBAL);
	}


	/**
	 * Determine CoreMetrics instance by CM Client ID
	 * 
	 * @param clientId CoreMetrics client ID
	 * @return
	 */
	public static CmInstance lookupByClientId(final String clientId) {
		return client2inst.get(clientId);
	}

	/**
	 * Determine CoreMetrics instance by CM test Client ID
	 * 
	 * @param clientId CoreMetrics test client ID
	 * @return
	 */
	public static CmInstance lookupByTestClientId(final String clientId) {
		return client2inst_tst.get(clientId);
	}

	
	/**
	 * Return corresponding CM client ID
	 * @param test look for test account
	 */
	public String getClientId(final boolean test) {
		if (test) {
			for (Map.Entry<String, CmInstance> e : client2inst_tst.entrySet()) {
				if (this.equals(e.getValue())) {
					return e.getKey();
				}
			}
		} else {
			for (Map.Entry<String, CmInstance> e : client2inst.entrySet()) {
				if (this.equals(e.getValue())) {
					return e.getKey();
				}
			}
		}
		return null;
	}


	/**
	 * Return CM Site ID
	 * @return
	 */
	public String getSiteId() {
		for (Map.Entry<String, CmInstance> e : site2inst.entrySet()) {
			if (this.equals(e.getValue())) {
				return e.getKey();
			}
		}
		return null;
	}
}
