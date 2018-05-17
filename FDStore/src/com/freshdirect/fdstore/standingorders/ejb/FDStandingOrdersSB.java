package com.freshdirect.fdstore.standingorders.ejb;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBObject;

import com.freshdirect.customer.ErpActivityRecord;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.lists.FDCustomerList;
import com.freshdirect.fdstore.standingorders.FDStandingOrder;
import com.freshdirect.fdstore.standingorders.FDStandingOrderAltDeliveryDate;
import com.freshdirect.fdstore.standingorders.FDStandingOrderFilterCriteria;
import com.freshdirect.fdstore.standingorders.FDStandingOrderInfoList;
import com.freshdirect.fdstore.standingorders.FDStandingOrderSkuResultInfo;
import com.freshdirect.fdstore.standingorders.SOResult.Result;
import com.freshdirect.fdstore.standingorders.UnavDetailsReportingBean;
import com.freshdirect.framework.core.PrimaryKey;

public interface FDStandingOrdersSB extends EJBObject {
	public FDStandingOrder createStandingOrder(FDCustomerList list) throws FDResourceException, RemoteException;
	public Collection<FDStandingOrder> loadActiveStandingOrders() throws FDResourceException, RemoteException;
	public Collection<FDStandingOrder> loadCustomerStandingOrders(FDIdentity identity) throws FDResourceException, RemoteException;
	public FDStandingOrder load(PrimaryKey pk) throws FDResourceException, RemoteException;
	public void delete(FDActionInfo info, FDStandingOrder so) throws FDResourceException, RemoteException;
	public String save(FDActionInfo info, FDStandingOrder so, String saleId) throws FDResourceException, RemoteException;
	public void assignStandingOrderToOrder(PrimaryKey salePK, PrimaryKey standingOrderPK) throws FDResourceException, RemoteException;
	public void markSaleAltDeliveryDateMovement(PrimaryKey salePK) throws FDResourceException, RemoteException;
	public void logActivity(ErpActivityRecord record) throws FDResourceException, RemoteException;
	public FDStandingOrderInfoList getActiveStandingOrdersCustInfo(FDStandingOrderFilterCriteria filter)throws FDResourceException, RemoteException;
	public void clearStandingOrderErrors(String[] soIDs,String agentId)throws FDResourceException, RemoteException;
	public FDStandingOrderInfoList getFailedStandingOrdersCustInfo()throws FDResourceException, RemoteException;
	public FDStandingOrderInfoList getMechanicalFailedStandingOrdersCustInfo()throws FDResourceException, RemoteException;
	public Map<Date,Date> getStandingOrdersAlternateDeliveryDates() throws FDResourceException, RemoteException;
	public List<FDStandingOrderAltDeliveryDate> getStandingOrderAltDeliveryDates() throws FDResourceException, RemoteException;
	public void addStandingOrderAltDeliveryDate(FDStandingOrderAltDeliveryDate altDeliveryDate) throws FDResourceException, RemoteException;	
	public void updateStandingOrderAltDeliveryDate(FDStandingOrderAltDeliveryDate altDeliveryDate) throws FDResourceException, RemoteException;
	public void deleteStandingOrderAltDeliveryDate(FDStandingOrderAltDeliveryDate altDeliveryDate) throws FDResourceException, RemoteException;
	public boolean lock(FDStandingOrder so, String lockId) throws FDResourceException,RemoteException;
	public boolean unlock(FDStandingOrder so, String lockId) throws FDResourceException,RemoteException;
	public String getLockId(String soId) throws FDResourceException,RemoteException;
	public void checkForDuplicateSOInstances(FDIdentity identity) throws FDResourceException,RemoteException;
	public void insertIntoCoremetricsUserinfo(FDUserI fdUser, int flag) throws FDResourceException, RemoteException;
	public boolean getCoremetricsUserinfo(FDUserI fdUser) throws FDResourceException, RemoteException;
	public Map<Date, List<FDStandingOrderAltDeliveryDate>> getStandingOrdersGlobalAlternateDeliveryDates() throws FDResourceException, RemoteException;
	public FDStandingOrderAltDeliveryDate getStandingOrderAltDeliveryDateById(String id) throws FDResourceException, RemoteException ;
	public void deleteStandingOrderAltDeliveryDateById(String[] altIds) throws FDResourceException, RemoteException;
	public void addStandingOrderAltDeliveryDates(List<FDStandingOrderAltDeliveryDate> altDeliveryDates) throws FDResourceException, RemoteException;
	public boolean checkIfAlreadyExists(FDStandingOrderAltDeliveryDate altDate) throws FDResourceException,RemoteException;
	public boolean isValidSoId(String soId) throws FDResourceException,RemoteException;
	public FDStandingOrderSkuResultInfo replaceSkuCode(String existingSku, String replacementSku) throws FDResourceException,RemoteException;
	public FDStandingOrderSkuResultInfo validateSkuCode(String existingSku, String replacementSku) throws FDResourceException,RemoteException;
	public void persistUnavailableDetailsToDB(List<Result> resultsList) throws FDResourceException,RemoteException;
	public UnavDetailsReportingBean getDetailsForReportGeneration() throws FDResourceException,RemoteException;
}
