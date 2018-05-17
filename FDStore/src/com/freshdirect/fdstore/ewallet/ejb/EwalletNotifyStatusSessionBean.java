/*
 * Created on Oct 6, 2015
 *
 */
package com.freshdirect.fdstore.ewallet.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;

import com.freshdirect.ErpServicesProperties;
import com.freshdirect.fdstore.ewallet.EnumEwalletType;
import com.freshdirect.fdstore.ewallet.EwalletRequestData;
import com.freshdirect.fdstore.ewallet.EwalletResponseData;
import com.freshdirect.fdstore.ewallet.EwalletServiceFactory;
import com.freshdirect.fdstore.ewallet.IEwallet;
import com.freshdirect.fdstore.ewallet.EwalletPostBackModel;
import com.freshdirect.framework.core.DataSourceLocator;
import com.freshdirect.framework.core.SessionBeanSupport;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * @author Ismail Mohammed
 *
 */
public class EwalletNotifyStatusSessionBean extends SessionBeanSupport {
	
	/**
	 * Generated
	 */
	private static final long serialVersionUID = -3040908271623920175L;
	
	@SuppressWarnings( "unused" )
	private static Category LOGGER = LoggerFactory.getInstance(EwalletNotifyStatusSessionBean.class);
	
	//TODO get all wallet types and loop through for posting
	private static final String MASTERPASS_EWALLET_TYPE="MP";
	
	public void loadTrxnsForPostBack(int maxDays) throws RemoteException {
		Connection conn = null;
		try {
			if (maxDays <= 0) {
				maxDays = ErpServicesProperties.geteWalletPostbackMaxDays();
			}
			conn = this.getConnection();
			EwalletTxNotifyDAO dao = new EwalletTxNotifyDAO();
			dao.prepareForPostBack(conn, maxDays);
			conn.commit();
		} catch(SQLException e) {
			LOGGER.error("SQLException: ", e);
			try {
				conn.rollback();
			} catch (SQLException e2) {
				LOGGER.warn("SQLException while rollback in loadTrxnsFor Postback ", e);
			}
			throw new RemoteException(e.getMessage());
		} finally {
			try {
				if(conn != null) {
					conn.close();
				}
			} catch(SQLException e){
				LOGGER.warn("SQLException while cleaningup", e);
			}
		}
	}
	
	/* 
	 * Facade for posting trxns to Ewallet provider
	 */
	public void postTrxnsToEwallet() throws RemoteException {
		long time_method_start = System.currentTimeMillis();
		long curr = System.currentTimeMillis();
		
		List<EwalletPostBackModel> trxns = new ArrayList<EwalletPostBackModel>();
		Connection conn = null;
		try {
			conn = this.getConnection();
			EwalletTxNotifyDAO dao = new EwalletTxNotifyDAO();
			
			for (Iterator<EnumEwalletType> enumIter = EnumEwalletType.iterator(); enumIter.hasNext(); ) {
				EnumEwalletType ewType = enumIter.next();
				trxns = dao.getAllTrxnsForPostback(conn, ewType);
				LOGGER.debug("Time taken for the loading all trxns (millis) " + (System.currentTimeMillis() - curr));
				curr = System.currentTimeMillis();
				
				int chunkSize = ErpServicesProperties.geteWalletPostbackChunkSize();
				
				if (trxns.size() > 0) {
					int i = 0;
					if (chunkSize > 0) {
						for (; i < (trxns.size() / chunkSize); i++) {
							EwalletResponseData resp = postTrxns(new ArrayList<EwalletPostBackModel>(trxns.subList(i*chunkSize, (i + 1)*chunkSize)), 
									ewType);
							
							if (resp.getTrxns() != null && resp.getTrxns().size() > 0) {
								dao.updateTrxnStatus(conn, resp.getTrxns());
								conn.commit();
							}
							
							LOGGER.debug("Time taken for the method postTrxnsToEwallet - process chunk, incl. update (millis) " +
														i + " is" + (System.currentTimeMillis() - curr));
							curr = System.currentTimeMillis();
						}
					}
					EwalletResponseData remaining = postTrxns(new ArrayList<EwalletPostBackModel>(trxns.subList(i*chunkSize, trxns.size())), 
							ewType);
					
					if (remaining.getTrxns() != null && remaining.getTrxns().size() > 0) {
						dao.updateTrxnStatus(conn, remaining.getTrxns());
						conn.commit();
					}
					
					LOGGER.debug("Time taken for the method postTrxnsToEwallet - process chunk incl. update (millis) " +
							i + " is" + (System.currentTimeMillis() - curr));
					curr = System.currentTimeMillis();
				} else {
					LOGGER.info("No transactions are posted for today");
				}
			}

		} catch(SQLException e) {
			LOGGER.error("SQLException: ", e);
			try {
				conn.rollback();
			} catch (SQLException e2) {
				LOGGER.warn("SQLException while rollback in postTrxnsToEwallet ", e);
			}

			throw new RemoteException("SQL Exception while posting trxns for Postback ", e.getCause());
		} finally {
			try {
				if(conn != null) {
					conn.close();
				}
			} catch(SQLException e){
				LOGGER.warn("SQLException while cleaningup", e);
			}
		}
		LOGGER.debug("Time taken for the method postTrxnsToEwallet (millis) " + (System.currentTimeMillis() - time_method_start));
	}

    /**
     * Retreives a database connection. Caches the underlying DataSource
     * (using the key from template method <CODE>getResourceCacheKey()</CODE>).
     *
     * @see DataSourceLocator
     *
     * @throws SQLException if any problems were encountered while trying
     * to find that DataSource that retreives connections from a pool
     */
    public Connection getConnection() throws SQLException {
   		return DataSourceLocator.getConnection( this.getResourceCacheKey() );
    }
    

	/**
	 * Template method that returns the cache key to use for caching resources.
	 * 
	 * @return the bean's home interface name
	 */
	protected String getResourceCacheKey() {
		return "com.freshdirect.fdstore.ewallet.ejb.EwalletNotifyStatusHome";
	}
    
    private EwalletResponseData postTrxns(List<EwalletPostBackModel> trxns, EnumEwalletType walletType) {
    	EwalletRequestData req = new EwalletRequestData();
    	EwalletResponseData resp = new EwalletResponseData();
    	req.setEnumeWalletType(walletType);
    	if (trxns != null && trxns.size() > 0) {
    		req.setTrxns(trxns);
    	}
    	else {
    		return resp;
    	}
    	
    	IEwallet.NotificationService notificaionSrvc = new EwalletServiceFactory().getEwalletNotificationService(req);
    	
    	try {
    		resp = notificaionSrvc.postbackTrxns(req);
    	} catch (Exception e) {
    		LOGGER.error("Postback failed during MP interaction ", e);
    	}
    	
    	if (resp == null) {
    		return new EwalletResponseData();
    	}
    	
    	return resp;
    }
}
