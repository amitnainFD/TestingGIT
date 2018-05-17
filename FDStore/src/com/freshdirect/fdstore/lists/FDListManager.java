package com.freshdirect.fdstore.lists;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Category;

import com.freshdirect.common.context.StoreContext;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDProductSelectionI;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.ejb.EnumCustomerListType;
import com.freshdirect.fdstore.lists.ejb.FDListManagerHome;
import com.freshdirect.fdstore.lists.ejb.FDListManagerSB;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDListManager {

	private final static Category LOGGER = LoggerFactory.getInstance(FDListManager.class);
	
	public static final int QUICKSHOP_ORDER_LIMIT = 100;

	private static FDListManagerHome managerHome = null;

	public static List<FDProductSelectionI> getEveryItemEverOrdered(FDIdentity identity)
			throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.getEveryItemEverOrdered(identity);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static FDCustomerRecipeList getEveryRecipeList(FDIdentity identity)
			throws FDResourceException {
		return (FDCustomerRecipeList) getCustomerList(identity,
				EnumCustomerListType.RECIPE_LIST,
				FDCustomerRecipeList.EVERY_RECIPE_LIST);
	}

	public static FDCustomerShoppingList generateEveryItemEverOrdered(
			FDIdentity identity) throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.generateEveryItemEverOrderedList(identity);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	public static List<FDProductSelectionI> getQsSpecificEveryItemEverOrderedList(FDIdentity identity, StoreContext storeContext) throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.getQsSpecificEveryItemEverOrderedList(identity, storeContext);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	//APPDEV-4179 - Item quantities should NOT be honored in "Your Top Items" 
	public static List<FDProductSelectionI> getQsSpecificEveryItemEverOrderedListTopItems(FDIdentity identity, StoreContext storeContext) throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.getQsSpecificEveryItemEverOrderedListTopItems(identity,storeContext);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static FDCustomerList getCustomerList(FDIdentity identity,
			EnumCustomerListType type, String listName)
			throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.getCustomerList(identity, type, listName);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static FDCustomerList getCustomerListById(FDIdentity identity,
			EnumCustomerListType type, String listId)
			throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.getCustomerListById(identity, type, listId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static String storeCustomerList(FDCustomerList list) throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.storeCustomerList(list);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}

	public static void removeCustomerListItem(FDUserI user, PrimaryKey id)
			throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			sb.removeCustomerListItem(user.getIdentity(), id);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}

	}

	// CCL
	// create new list
	public static String createCustomerCreatedList(FDUserI user, String listName)
			throws FDResourceException, FDCustomerListExistsException {
		lookupManagerHome();
		try {

			FDListManagerSB sb = managerHome.create();
			return sb.createCustomerCreatedList(user.getIdentity(), user.getUserContext().getStoreContext(), listName);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	// CCL
	// rename list: use store with new list name

	// CCL
	// delete: use store with no items
	public static void deleteCustomerCreatedList(FDUserI user, String listName)
			throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			sb.deleteCustomerCreatedList(user.getIdentity(), listName, user.getUserContext().getStoreContext());
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static void deleteShoppingList(String listId) throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			sb.deleteShoppingList( listId );
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	// CCL
	// get all customer created lists
	public static List<FDCustomerCreatedList> getCustomerCreatedLists(FDUserI user)
			throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.getCustomerCreatedLists(user.getIdentity(), user.getUserContext().getStoreContext());
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	// CCL
	// get all customer created list infos
	public static List<FDCustomerListInfo> getCustomerCreatedListInfos(FDUserI user)
			throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.getCustomerCreatedListInfos(user.getIdentity(), user.getUserContext().getStoreContext());
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	// SO
	// get all customer created list infos
	public static List<FDCustomerListInfo> getStandingOrderListInfos(FDUserI user)
			throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.getStandingOrderListInfos(user.getIdentity());
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
	
	// CCL
	public static void modifyCustomerCreatedList(FDCustomerList list)
			throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			sb.modifyCustomerCreatedList(list);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		} catch (FDResourceException e) {
			invalidateManagerHome();
			throw new FDResourceException(e,
					"Error modifying customer created list");
		}
	}

	// CCL
	/**
	 * @deprecated Use {@link FDListManager#isCustomerList(FDUserI, EnumCustomerListType, String)} instead.
	 */
	@Deprecated
	public static boolean isCustomerCreatedList(FDUserI user, String listName)
			throws FDResourceException {
		return isCustomerList(user, EnumCustomerListType.CC_LIST, listName);
	}

	// CCL
	public static boolean isCustomerList(FDUserI user, EnumCustomerListType type, String listName)
			throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.isCustomerList(user.getIdentity(), type != null ? type : EnumCustomerListType.CC_LIST, listName);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		} catch (FDResourceException e) {
			invalidateManagerHome();
			throw new FDResourceException(e,
					"Error checking the existence of customer created list");
		}
	}

	// CCL
	public static void copyCustomerCreatedList(FDCustomerList oldList,
			FDCustomerList newList) throws FDResourceException,
			FDCustomerListExistsException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			sb.copyCustomerCreatedList(oldList, newList);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		} catch (FDResourceException e) {
			invalidateManagerHome();
			throw new FDResourceException(e,
					"Error modifying customer created list");
		}

	}

	// CCL
	public static FDCustomerCreatedList getCustomerCreatedList(
			FDIdentity identity, String ccListId) throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.getCustomerCreatedList(identity, ccListId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}

	public static String getListName( FDIdentity identity, String ccListId ) throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.getListName( identity, ccListId );
		} catch ( CreateException ce ) {
			invalidateManagerHome();
			throw new FDResourceException( ce, "Error creating session bean" );
		} catch ( RemoteException re ) {
			invalidateManagerHome();
			throw new FDResourceException( re, "Error talking to session bean" );
		} catch ( FDResourceException e ) {
			invalidateManagerHome();
			throw new FDResourceException( e, "Error retrieving list name" );
		}
	}

	public static void renameCustomerCreatedList(FDIdentity identity,
			String oldName, String newName)
			throws FDCustomerListExistsException, FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			sb.renameCustomerCreatedList(identity, oldName, newName);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		} catch (FDResourceException e) {
			invalidateManagerHome();
			throw new FDResourceException(e, "Error renaming list");
		}
	}

	public static void renameCustomerList(FDActionInfo info, EnumCustomerListType type, 
			String oldName, String newName)
			throws FDCustomerListExistsException, FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			sb.renameCustomerList(info, type, oldName, newName);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		} catch (FDResourceException e) {
			invalidateManagerHome();
			throw new FDResourceException(e, "Error renaming list");
		}
	}
	
	public static void renameShoppingList(String listId, String newName) throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			sb.renameShoppingList(listId, newName);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		} catch (FDResourceException e) {
			invalidateManagerHome();
			throw new FDResourceException(e, "Error renaming list");
		}
	}

	private static void invalidateManagerHome() {
		managerHome = null;
	}

	private static void lookupManagerHome() throws FDResourceException {
		if (managerHome != null) {
			return;
		}
		Context ctx = null;
		try {
			ctx = FDStoreProperties.getInitialContext();
			managerHome = (FDListManagerHome) ctx
					.lookup(FDListManagerHome.JNDI_HOME);
		} catch (NamingException ne) {
			throw new FDResourceException(ne);
		} finally {
			try {
				if (ctx != null) {
					ctx.close();
				}
			} catch (NamingException ne) {
				LOGGER.warn("Cannot close Context while trying to cleanup", ne);
			}
		}
	}

	// SmartStore
	public static FDCustomerProductList getOrderDetails(String erpCustomerId,List<String> skus) throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.getOrderDetails(erpCustomerId, skus);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		} catch (FDResourceException e) {
			invalidateManagerHome();
			throw new FDResourceException(e,
					"Error retrieving product details for customer "
							+ erpCustomerId);
		}
	}



	/**
	 * Returns a standing order list
	 * 
	 * @param identity
	 * @param soListId
	 * @return
	 * @throws FDResourceException
	 */
	public static FDStandingOrderList getStandingOrderList(	FDIdentity identity, String soListId ) throws FDResourceException {
		lookupManagerHome();
		try {
			FDListManagerSB sb = managerHome.create();
			return sb.getStandingOrderList(identity, soListId);
		} catch (CreateException ce) {
			invalidateManagerHome();
			throw new FDResourceException(ce, "Error creating session bean");
		} catch (RemoteException re) {
			invalidateManagerHome();
			throw new FDResourceException(re, "Error talking to session bean");
		}
	}
}
