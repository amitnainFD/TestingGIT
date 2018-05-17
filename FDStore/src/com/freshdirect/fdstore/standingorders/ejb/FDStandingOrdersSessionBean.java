package com.freshdirect.fdstore.standingorders.ejb;

import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Category;

import com.freshdirect.common.address.AddressModel;
import com.freshdirect.customer.EnumAccountActivityType;
import com.freshdirect.customer.EnumStandingOrderType;
import com.freshdirect.customer.EnumTransactionSource;
import com.freshdirect.customer.ErpActivityRecord;
import com.freshdirect.customer.ejb.ErpLogActivityCommand;
import com.freshdirect.fdlogistics.model.FDDeliveryZoneInfo;
import com.freshdirect.fdlogistics.model.FDInvalidAddressException;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDAuthenticationException;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDOrderHistory;
import com.freshdirect.fdstore.customer.FDOrderInfoI;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.ejb.FDSessionBeanSupport;
import com.freshdirect.fdstore.lists.FDCustomerList;
import com.freshdirect.fdstore.lists.FDStandingOrderList;
import com.freshdirect.fdstore.mail.FDEmailFactory;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;
import com.freshdirect.fdstore.standingorders.FDStandingOrderAltDeliveryDate;
import com.freshdirect.fdstore.standingorders.FDStandingOrderFilterCriteria;
import com.freshdirect.fdstore.standingorders.FDStandingOrderInfo;
import com.freshdirect.fdstore.standingorders.FDStandingOrderInfoList;
import com.freshdirect.fdstore.standingorders.FDStandingOrderSkuResultInfo;
import com.freshdirect.fdstore.standingorders.SOResult;
import com.freshdirect.fdstore.standingorders.UnavDetailsReportingBean;
import com.freshdirect.fdstore.standingorders.FDStandingOrder.ErrorCode;
import com.freshdirect.fdstore.standingorders.SOResult.Result;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.mail.XMLEmailI;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.logistics.delivery.dto.CustomerAvgOrderSize;
import com.freshdirect.mail.ejb.MailerGatewaySB;

public class FDStandingOrdersSessionBean extends FDSessionBeanSupport {
	
	private static final long serialVersionUID = 1021328260150726448L;

	private final static Category LOGGER = LoggerFactory.getInstance(FDStandingOrdersSessionBean.class);
	private static final int LOCK_TIMEOUT = 120; //seconds

	/**
	 * Creates a new instance based on an existing customer list
	 * 
	 * @param list
	 * @return
	 * @throws FDResourceException 
	 */
	public FDStandingOrder createStandingOrder(FDCustomerList list) throws FDResourceException {
		if (list == null) {
			return null;
		}

		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			String pk = dao.createEmptyStandingOrder(conn, list.getCustomerPk().getId(), list.getId());
			if (pk != null) {
				return new FDStandingOrder(new PrimaryKey(pk), list);
			}
			LOGGER.error("Failed to create standing order instance for customer list " + list.getId());
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in createStandingOrder() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}		
		return null;	
	}


	public Collection<FDStandingOrder> loadActiveStandingOrders() throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			Collection<FDStandingOrder> ret = dao.loadActiveStandingOrders(conn);
			
			return ret;
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in loadActiveStandingOrders() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public Collection<FDStandingOrder> loadCustomerStandingOrders(FDIdentity identity) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			Collection<FDStandingOrder> ret = dao.loadCustomerStandingOrders(conn, identity);
			
			return ret;
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in loadCustomerStandingOrders() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public FDStandingOrder load(PrimaryKey pk) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			FDStandingOrder ret = dao.load(conn, pk.getId());
			
			return ret;
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in load() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void delete(FDActionInfo info, FDStandingOrder so) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			dao.deleteStandingOrder(conn, so.getId(), so.getCustomerListId());
			ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.STANDINGORDER_DELETED);
			rec.setStandingOrderId(so.getId());
			this.logActivity(rec);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in delete() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public String save(FDActionInfo info, FDStandingOrder so, String saleId) throws FDResourceException {
		String primaryKey = null;
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			try {
				FDStandingOrderList customerList = so.getCustomerList();
				if (customerList != null)
					customerList.setModificationDate( new Date() );
			} catch (FDResourceException e) {
				LOGGER.warn( "FDResourceException catched", e );
			}
			
			if (so.getId() == null) {
				// create object
				LOGGER.debug( "Creating new standing order." );
				primaryKey = dao.createStandingOrder(conn, so);
				ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.STANDINGORDER_CREATED);
				rec.setChangeOrderId(saleId);
				rec.setStandingOrderId(primaryKey);
				this.logActivity(rec);
			} else {
				LOGGER.debug( "Updating existing standing order." );
				dao.updateStandingOrder(conn, so);
				primaryKey = so.getId();
				ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.STANDINGORDER_MODIFIED);
				rec.setChangeOrderId(saleId);
				rec.setStandingOrderId(primaryKey);
				this.logActivity(rec);
			}
							
			return primaryKey;
			
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in save() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public void assignStandingOrderToOrder(PrimaryKey salePK, PrimaryKey standingOrderPK) throws FDResourceException {		

		LOGGER.debug( "assigning SO["+standingOrderPK+"] to SALE["+salePK+"]" );
		Connection conn=null;
		try {
			 conn = getConnection();
			
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			dao.assignToSale(conn, standingOrderPK.getId(), salePK.getId());
			
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in assignStandingOrderToOrder() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		}finally {
			close(conn);
		}
	}

	public void markSaleAltDeliveryDateMovement(PrimaryKey salePK) throws FDResourceException {		

		Connection conn=null;
		try {
			conn = getConnection();
			
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			dao.markSaleAltDeliveryDateMovement(conn, salePK.getId());
			
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in markSaleAltDeliveryDateMovement() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		}finally {
			close(conn);
		}
	}

	public void logActivity(ErpActivityRecord record) {
		new ErpLogActivityCommand(LOCATOR, record).execute();
	}
	
	public FDStandingOrderInfoList getActiveStandingOrdersCustInfo(FDStandingOrderFilterCriteria filter) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			FDStandingOrderInfoList ret = dao.getActiveStandingOrdersCustInfo(conn,filter);
			
			return ret;
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in loadActiveStandingOrders() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public void clearStandingOrderErrors(String[] soIDs,String agentId)throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			if(null !=soIDs && soIDs.length>0){
				Date date = new Date();
				String[] ids = new String[soIDs.length];
				for (int i = 0; i < soIDs.length; i++) {
					ids[i]=soIDs[i].split(",")[0];
				}
				dao.clearStandingOrderErrors(conn,ids);
				
				for (int i = 0; i < soIDs.length; i++) {
					ErpActivityRecord rec = new ErpActivityRecord();
					rec.setActivityType(EnumAccountActivityType.STANDINGORDER_ERROR_CLEARED);
					rec.setSource(EnumTransactionSource.CUSTOMER_REP);
					rec.setInitiator(agentId);
					rec.setStandingOrderId(soIDs[i].split(",")[0]);
					rec.setCustomerId(soIDs[i].split(",")[1]);
					rec.setDate(date);
					this.logActivity(rec);
				}
				
			}			
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in loadActiveStandingOrders() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public FDStandingOrderInfoList getFailedStandingOrdersCustInfo() throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			FDStandingOrderInfoList ret = dao.getFailedStandingOrdersCustInfo(conn);
			
			return ret;
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in getFailedStandingOrdersCustInfo() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public FDStandingOrderInfoList getMechanicalFailedStandingOrdersCustInfo() throws FDResourceException {
		Connection conn = null;
		FDStandingOrderInfoList ret = null;
		
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			ret = dao.getMechanicalFailedStandingOrdersCustInfo(conn);

		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in getMechanicalFailedStandingOrdersCustInfo() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}

		// Add Zone info to Standing Order
		if(ret !=null ){
			addZoneInfoToStandingOrder(ret);
		}
		
		return ret;
	}
	
	
	private void addZoneInfoToStandingOrder(FDStandingOrderInfoList ret){
		
		List<FDStandingOrderInfo> standingOrdersInfo = ret.getStandingOrdersInfo();
		
		for(FDStandingOrderInfo soInfo : standingOrdersInfo ){
			
			try {
				// retrieve standing order id
				String soId = soInfo.getSoID();
				
				// load Standing Order
				FDStandingOrder so = load( new PrimaryKey( soId ) );
								
				// retrieve address from Standign Order
				AddressModel deliveryAddressModel = FDCustomerManager.getAddress( so.getCustomerIdentity(), so.getAddressId() );
				
				//Date startDateTime = soInfo.getStartTime(); 
				Date startDateTime = soInfo.getNextDate();
				FDDeliveryZoneInfo zoneInfo = null;
				
				try {
					// zoneInfo = FDDeliveryManager.getInstance().getZoneInfo(deliveryAddressModel, selectedTimeslot.getStartDateTime(), customerUser.getHistoricOrderSize(), (reservation!=null)?reservation.getRegionSvcType():null);
					Calendar cal = Calendar.getInstance();
					cal.setTime(startDateTime);
					CustomerAvgOrderSize historicSize = FDCustomerManager.getHistoricOrderSize(so.getCustomerIdentity().getErpCustomerPK());
					if(deliveryAddressModel!=null && StringUtils.isNotBlank(deliveryAddressModel.getAddress1())){
						zoneInfo = FDDeliveryManager.getInstance().getZoneInfo(deliveryAddressModel, cal.getTime(), historicSize, null);
					}
				} catch (FDInvalidAddressException e) {
					LOGGER.info( "Invalid zone info. - FDInvalidAddressException", e );
				}
				
				// add zone to Standing Order
				if(zoneInfo != null){
					soInfo.setZone(zoneInfo.getZoneCode());
				}
			} catch (FDResourceException re) {
				LOGGER.error( "Could not retrieve standing order! - FDResourceException", re );
			}
			
		}
		
	}		
	
	
	public Map<Date,Date> getStandingOrdersAlternateDeliveryDates() throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			Map<Date,Date> map = dao.getStandingOrdersAlternateDeliveryDates(conn);
			
			return map;
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in getStandingOrdersAlternateDeliveryDates() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public List<FDStandingOrderAltDeliveryDate> getStandingOrderAltDeliveryDates()  throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			List<FDStandingOrderAltDeliveryDate> altDeliveryDates = dao.getStandingOrderAltDeliveryDates(conn);
			
			return altDeliveryDates;
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in getStandingOrdersAlternateDeliveryDates() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public Map<Date, List<FDStandingOrderAltDeliveryDate>> getStandingOrdersGlobalAlternateDeliveryDates() throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			Map<Date, List<FDStandingOrderAltDeliveryDate>> altDeliveryDates = dao.getStandingOrdersGlobalAlternateDeliveryDates(conn);
			
			return altDeliveryDates;
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in getStandingOrdersAlternateDeliveryDates() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public void addStandingOrderAltDeliveryDate(FDStandingOrderAltDeliveryDate altDeliveryDate) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();			
			dao.addStandingOrderAltDeliveryDate(conn, altDeliveryDate);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in addStandingOrderAltDeliveryDate() : " + e.getMessage(), e );
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public boolean lock(FDStandingOrder so, String lockId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			return dao.lockStandingOrder(conn, so.getId(), lockId, LOCK_TIMEOUT);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in lock() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public boolean unlock(FDStandingOrder so, String lockId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			return dao.unlockStandingOrder(conn, so.getId(), lockId, LOCK_TIMEOUT);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in unlock() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public String getLockId(String soId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			
			return dao.getLockIdStandingOrder(conn, soId, LOCK_TIMEOUT);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in getLockId() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		
	}
	
	
	public void checkForDuplicateSOInstances(FDIdentity identity) throws FDResourceException {
		
		Connection conn=null;
		try{
			conn=getConnection();
			FDStandingOrderDAO dao=new FDStandingOrderDAO();
			List<String> hashes=dao.getDuplicateWarningInfos(conn, identity.getErpCustomerPK());
		
			FDOrderHistory history = (FDOrderHistory) FDCustomerManager.getOrderHistoryInfo(identity); //or should we get the FDUser object instead?
			List<FDOrderInfoI> soiList = (List<FDOrderInfoI>) history.getStandingOrderInstances(null);
			
			//load customer Standing Orders
			List<FDStandingOrder> standingOrders=(List<FDStandingOrder>) loadCustomerStandingOrders(identity);
			
			//group orders by standing order id
			Map<String,List<FDOrderInfoI>> soIds = new HashMap<String, List<FDOrderInfoI>>();
			for(FDOrderInfoI order : soiList){
				if(soIds.get(order.getStandingOrderId())!=null){
					soIds.get(order.getStandingOrderId()).add(order);
				}else{
					List<FDOrderInfoI> local = new ArrayList<FDOrderInfoI>();
					local.add(order);
					soIds.put(order.getStandingOrderId(), local);
				}
			}				
		
			//check if multiple SOI found for one SO
			for(String key : soIds.keySet()) {
				
				if(soIds.get(key).size() > 1) {
					
					List<Long> ids=new ArrayList<Long>();
					for(FDOrderInfoI o:soIds.get(key)){
						ids.add(Long.parseLong(o.getErpSalesId()));
					}

					String hash=createHash(ids);
					if(!hashes.contains(hash)){ //check if email already sent about these orders
						FDStandingOrder actual=null;
						
						for(FDStandingOrder so:standingOrders){ //find the real SO object for the orders
							if(so.getId().equals(key)){
								actual=so;
								break;
							}
						}
						
						LOGGER.debug("Sending email to customer...");
						doEmail(FDEmailFactory.getInstance().createDuplicateSOInstanceEmail( FDCustomerManager.getCustomerInfo(identity), actual, soIds.get(key)));
						//dao.storeDuplicateWarningInfo(conn, identity.getErpCustomerPK(), hash);
					}
					
				}
			}
		
		}catch (SQLException e) {
			LOGGER.error( "SQL ERROR in checkForDuplicateSOInstances() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}

	}
	
	private String createHash(List<Long> source){
		
		Collections.sort(source);
		StringBuilder builder=new StringBuilder();
		for(Long instance:source){
			builder.append(instance);
		}
		
		return builder.toString();
	}
	
	public void doEmail(XMLEmailI email) throws FDResourceException {
		try {
			MailerGatewaySB mailer = getMailerHome().create();
			mailer.enqueueEmail(email);
		} catch (CreateException ce) {
			throw new FDResourceException(ce, "Cannot create MailerGatewayBean");
		} catch (RemoteException re) {
			throw new FDResourceException(re, "Cannot talk to MailerGatewayBean");
		}
	}
	
	public void updateStandingOrderAltDeliveryDate(FDStandingOrderAltDeliveryDate altDeliveryDate) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();			
			dao.updateStandingOrderAltDeliveryDate(conn, altDeliveryDate);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in updateStandingOrderAltDeliveryDate() : " + e.getMessage(), e );
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public void deleteStandingOrderAltDeliveryDate(FDStandingOrderAltDeliveryDate altDeliveryDate) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();			
			dao.deleteStandingOrderAltDeliveryDate(conn, altDeliveryDate);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in deleteStandingOrderAltDeliveryDate() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public void insertIntoCoremetricsUserinfo(FDUserI fdUser, int flag) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();			
			dao.insertIntoCoremetricsUserinfo(conn, fdUser, flag);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in deleteStandingOrderAltDeliveryDate() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}

	public boolean getCoremetricsUserinfo(FDUserI fdUser) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();			
			return dao.getCoremetricsUserinfo(conn, fdUser);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in deleteStandingOrderAltDeliveryDate() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
	public FDStandingOrderAltDeliveryDate getStandingOrderAltDeliveryDateById(String id) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();			
			return dao.getStandingOrderAltDeliveryDateById(conn,id);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in deleteStandingOrderAltDeliveryDate() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		} 
	}
	
	public void deleteStandingOrderAltDeliveryDateById(String[] altIds) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();			
			dao.deleteStandingOrderAltDeliveryDateById(conn, altIds);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in deleteStandingOrderAltDeliveryDate() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDRuntimeException(e);
		} finally {
			close(conn);
		}
	}
	
	public void addStandingOrderAltDeliveryDates(List<FDStandingOrderAltDeliveryDate> altDeliveryDates) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();			
			dao.addStandingOrderAltDeliveryDates(conn, altDeliveryDates);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in addStandingOrderAltDeliveryDate() : " + e.getMessage(), e );
			throw new FDRuntimeException(e);
		} finally {
			close(conn);
		}
	}
	
	public boolean checkIfAlreadyExists(FDStandingOrderAltDeliveryDate altDate) throws FDResourceException{
		Connection conn = null;
		boolean isDuplicate=false;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();			
			isDuplicate =dao.checkIfAlreadyExists(conn, altDate);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in addStandingOrderAltDeliveryDate() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return isDuplicate;
	}
	
	public boolean isValidSoId(String soId) throws FDResourceException{
		Connection conn = null;
		boolean isValid=false;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();			
			isValid =dao.isValidSoId(conn, soId);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in addStandingOrderAltDeliveryDate() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
		return isValid;
	}
	
	public FDStandingOrderSkuResultInfo replaceSkuCode(String existingSku,
			String replacementSku) throws FDResourceException {

		Connection conn = null;
		FDStandingOrderSkuResultInfo result = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			result = dao.replaceSkuCode(conn,
					existingSku, replacementSku);
			FDStandingOrderAuditDataDAO auditDataDAO = new FDStandingOrderAuditDataDAO();
			auditDataDAO.auditCall(conn,
					prepareAuditInfoBean(existingSku, replacementSku,result.getProductSkuList().size()));
			return result;
		} catch (SQLException e) {
			result = new FDStandingOrderSkuResultInfo();
			LOGGER.error("SQL ERROR in replaceSkuCode() : " + e.getMessage(), e);			
					result.setErrorMessage("Error while Auditing the User Activity.Rolling back the Trasaction");
					getSessionContext().setRollbackOnly();
					return result;
			}
			
		 finally {
				close(conn);
			}
		}

	private AuditDataBeanInfo prepareAuditInfoBean(String existingSku,
			String replacementSku, int numofStandingOrders) {
		AuditDataBeanInfo auditDataBeanInfo = new AuditDataBeanInfo();
		auditDataBeanInfo.setUserId(getSessionContext().getCallerPrincipal().getName());
		auditDataBeanInfo.setType(EnumStandingOrderType.SO_SKU_REPLACEMENT.getName());
		auditDataBeanInfo.setComment(replacementSku + " replaced " + existingSku + " in " + numofStandingOrders + " Standing Orders");
		return auditDataBeanInfo;
	}
	
	public FDStandingOrderSkuResultInfo validateSkuCode(String existingSku, String replacementSku) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();			
			return dao.validateSkuCode(conn,existingSku,replacementSku);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in validateSkuCode() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}			
	}
	
	public void persistUnavailableDetailsToDB(List<Result> resultsList) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			dao.persistUnavailableDetailsToDB(conn,resultsList);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in validateSkuCode() : " + e.getMessage(), e );
			e.printStackTrace();
			getSessionContext().setRollbackOnly();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}			
	}
	
	public UnavDetailsReportingBean getDetailsForReportGeneration() throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDStandingOrderDAO dao = new FDStandingOrderDAO();
			return dao.getDetailsForReportGeneration(conn);
		} catch (SQLException e) {
			LOGGER.error( "SQL ERROR in validateSkuCode() : " + e.getMessage(), e );
			e.printStackTrace();
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}			
	}
}
