package com.freshdirect.fdstore.lists.ejb;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.EJBObject;

import com.freshdirect.common.context.StoreContext;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDProductSelectionI;
import com.freshdirect.fdstore.customer.ejb.EnumCustomerListType;
import com.freshdirect.fdstore.lists.FDCustomerCreatedList;
import com.freshdirect.fdstore.lists.FDCustomerList;
import com.freshdirect.fdstore.lists.FDCustomerListExistsException;
import com.freshdirect.fdstore.lists.FDCustomerListInfo;
import com.freshdirect.fdstore.lists.FDCustomerProductList;
import com.freshdirect.fdstore.lists.FDCustomerShoppingList;
import com.freshdirect.fdstore.lists.FDStandingOrderList;
import com.freshdirect.framework.core.PrimaryKey;

public interface FDListManagerSB extends EJBObject {

    public List<FDProductSelectionI> getEveryItemEverOrdered(FDIdentity identity) throws FDResourceException, RemoteException;
    
    public FDCustomerList getCustomerList(FDIdentity identity, EnumCustomerListType type, String listName) throws FDResourceException, RemoteException;
    
    public FDCustomerList getCustomerListById(FDIdentity identity, EnumCustomerListType type, String listId) throws FDResourceException, RemoteException;

    public FDCustomerShoppingList generateEveryItemEverOrderedList(FDIdentity identity) throws FDResourceException, RemoteException;
    
    public List<FDProductSelectionI> getQsSpecificEveryItemEverOrderedList(FDIdentity identity, StoreContext storeContext) throws FDResourceException, RemoteException;
    //APPDEV-4179 - Item quantities should NOT be honored in "Your Top Items"
    public List<FDProductSelectionI> getQsSpecificEveryItemEverOrderedListTopItems(FDIdentity identity, StoreContext storeContext) throws FDResourceException, RemoteException;

    public String storeCustomerList(FDCustomerList list) throws FDResourceException, RemoteException;
    
    public boolean removeCustomerListItem(FDIdentity identity, PrimaryKey id) throws FDResourceException, RemoteException;

	public String createCustomerCreatedList(FDIdentity identity, StoreContext storeContext, String listName) throws FDResourceException, RemoteException,  FDCustomerListExistsException;
	
	public void deleteCustomerCreatedList(FDIdentity identity, String listName, StoreContext storeContext) throws FDResourceException, RemoteException;
	
	public void deleteShoppingList( String listId ) throws FDResourceException, RemoteException;		
	
	public List<FDCustomerCreatedList> getCustomerCreatedLists(FDIdentity identity, StoreContext storeContext) throws FDResourceException, RemoteException;

	public List<FDCustomerListInfo> getCustomerCreatedListInfos(FDIdentity identity, StoreContext storeContext) throws FDResourceException, RemoteException;
	
	public List<FDCustomerListInfo> getStandingOrderListInfos(FDIdentity identity) throws FDResourceException, RemoteException;

	public void modifyCustomerCreatedList(FDCustomerList list) throws FDResourceException, RemoteException;
	
	public void copyCustomerCreatedList(FDCustomerList oldList,FDCustomerList newList) throws FDResourceException, RemoteException, FDCustomerListExistsException ;
	
	public boolean isCustomerList(FDIdentity identity, EnumCustomerListType type, String listName) throws FDResourceException, RemoteException ;
	
	public FDCustomerCreatedList getCustomerCreatedList(FDIdentity identity,String ccListId) throws FDResourceException, RemoteException;
	
	public String getListName(FDIdentity identity, String ccListId) throws FDResourceException, RemoteException;
	
	public void renameCustomerCreatedList(FDIdentity identity, String oldName, String newName) throws FDCustomerListExistsException, FDResourceException, RemoteException;

	public void renameCustomerList(FDActionInfo info, EnumCustomerListType type, String oldName, String newName) throws FDCustomerListExistsException, FDResourceException, RemoteException;

	public void renameShoppingList(String listId, String newName) throws FDResourceException, RemoteException;

	// SmartStore
	public FDCustomerProductList getOrderDetails(String erpCustomerId, List<String> skus) throws FDResourceException, RemoteException;

	public FDStandingOrderList getStandingOrderList(FDIdentity identity, String soListId) throws FDResourceException, RemoteException;
}
