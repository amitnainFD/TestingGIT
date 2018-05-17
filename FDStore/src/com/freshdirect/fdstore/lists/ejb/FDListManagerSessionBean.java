package com.freshdirect.fdstore.lists.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Category;

import com.freshdirect.common.context.StoreContext;
import com.freshdirect.customer.EnumAccountActivityType;
import com.freshdirect.customer.ErpActivityRecord;
import com.freshdirect.customer.ErpCustomerInfoModel;
import com.freshdirect.customer.ejb.ErpLogActivityCommand;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDActionInfo;
import com.freshdirect.fdstore.customer.FDAuthenticationException;
import com.freshdirect.fdstore.customer.FDCustomerFactory;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.customer.FDProductSelectionI;
import com.freshdirect.fdstore.customer.FDUser;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.OrderLineUtil;
import com.freshdirect.fdstore.customer.ejb.EnumCustomerListType;
import com.freshdirect.fdstore.customer.ejb.FDSessionBeanSupport;
import com.freshdirect.fdstore.customer.ejb.FDUserDAO;
import com.freshdirect.fdstore.lists.FDCustomerCreatedList;
import com.freshdirect.fdstore.lists.FDCustomerList;
import com.freshdirect.fdstore.lists.FDCustomerListExistsException;
import com.freshdirect.fdstore.lists.FDCustomerListInfo;
import com.freshdirect.fdstore.lists.FDCustomerListItem;
import com.freshdirect.fdstore.lists.FDCustomerProductList;
import com.freshdirect.fdstore.lists.FDCustomerProductListLineItem;
import com.freshdirect.fdstore.lists.FDCustomerRecipeList;
import com.freshdirect.fdstore.lists.FDCustomerRecipeListLineItem;
import com.freshdirect.fdstore.lists.FDCustomerShoppingList;
import com.freshdirect.fdstore.lists.FDQsProductListLineItem;
import com.freshdirect.fdstore.lists.FDStandingOrderList;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDListManagerSessionBean extends FDSessionBeanSupport {

	private static final long	serialVersionUID	= 3548977965120918261L;
	
	private final static Category LOGGER = LoggerFactory.getInstance(FDListManagerSessionBean.class);

	public List<FDProductSelectionI> getEveryItemEverOrdered(FDIdentity identity) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();

			List<FDProductSelectionI> retList = new ArrayList<FDProductSelectionI>();
			FDCustomerShoppingList list = (FDCustomerShoppingList) dao.load(conn, identity /* new PrimaryKey(identity.getErpCustomerPK()) */, EnumCustomerListType.SHOPPING_LIST, FDCustomerShoppingList.EVERY_ITEM_LIST);

			if (list == null) {
				list = dao.generateEveryItemEverOrderedList(conn, identity);
				dao.store(conn, list);
				list = (FDCustomerShoppingList) dao.load(conn, identity /* new PrimaryKey(identity.getErpCustomerPK()) */, EnumCustomerListType.SHOPPING_LIST, FDCustomerShoppingList.EVERY_ITEM_LIST);
			}

			for (Iterator<FDCustomerListItem> i = list.getLineItems().iterator(); i.hasNext();) {
				FDCustomerProductListLineItem item = (FDCustomerProductListLineItem) i.next();
				try {
					if(item.getSkuCode().equals(FDStoreProperties.getGiftcardSkucode())){
						i.remove();
						continue;
					}
					if(item.getSkuCode().equals(FDStoreProperties.getRobinHoodSkucode())){
						i.remove();
						continue;
					}
					if (item.getDeleted() == null)
						retList.add(item.convertToSelection());
				} catch (FDSkuNotFoundException e) {
					LOGGER.warn("Loaded an invalid sku - skipping", e);
				}
			}

			return retList;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
		    close(conn);
		}
	}
	
	public FDCustomerShoppingList generateEveryItemEverOrderedList(FDIdentity identity) throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			return dao.generateEveryItemEverOrderedList(conn, identity);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
                    close(conn);
		}
	}
	
	public List<FDProductSelectionI> getQsSpecificEveryItemEverOrderedList(FDIdentity identity, StoreContext storeContext) throws FDResourceException {
		//long startTime=System.currentTimeMillis();
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			List<FDProductSelectionI> retList = new ArrayList<FDProductSelectionI>(100);
			List<FDQsProductListLineItem> source = dao.getQsSpecificEveryItemEverOrderedList(conn, identity, storeContext);
			for(FDQsProductListLineItem item : source){
				if(item.getDeleted()==null){
					try {
						retList.add(item.convertToSelection());
					} catch (FDSkuNotFoundException e) {
						LOGGER.warn("Loaded an invalid sku - skipping", e);
					}
				}
			}
			//System.out.println("Time taken in FDListManagerSB.getQsSpecificEveryItemEverOrderedList() ::"+((System.currentTimeMillis()-startTime)) );
			return retList;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
            close(conn);
		}
	}
	
	//APPDEV-4179 - Item quantities should NOT be honored in "Your Top Items" 
	public List<FDProductSelectionI> getQsSpecificEveryItemEverOrderedListTopItems(FDIdentity identity,StoreContext storeContext) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			List<FDProductSelectionI> retList = new ArrayList<FDProductSelectionI>(100);
			List<FDQsProductListLineItem> source;
			try {
				source = dao.getQsSpecificEveryItemEverOrderedListTopItemsTopItems(conn, identity, storeContext);
			} catch (FDSkuNotFoundException e1) {
				throw new FDResourceException(e1);
			}
			for(FDQsProductListLineItem item : source){
				if(item.getDeleted()==null){
					try {
						retList.add(item.convertToSelection());
					} catch (FDSkuNotFoundException e) {
						LOGGER.warn("Loaded an invalid sku - skipping", e);
					}
				}
			}
			return retList;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
            close(conn);
		}
	}

	public FDCustomerList getCustomerList(FDIdentity identity, EnumCustomerListType type, String listName) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			return dao.load(conn, identity, type, listName);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
                    close(conn);
		}
	}

	public FDCustomerList getCustomerListById(FDIdentity identity, EnumCustomerListType type, String listId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			return dao.lookupById(conn, identity, type, listId);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after getCustomerList", e);
				}
			}
		}
	}
	
	public String storeCustomerList( FDCustomerList list ) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			LOGGER.debug( "FDListManagerSessionBean:storeCustomerList()" );
			return dao.store( conn, list );
		} catch ( SQLException e ) {
			//in case of sqlexception the container won't roll back the transaction so we have to take care of consistency -- bugfix [APPDEV-2208]
			this.getSessionContext().setRollbackOnly();
			throw new FDResourceException( e );
		} finally {
			close( conn );
		}
	}

	public boolean removeCustomerListItem(FDIdentity identity, PrimaryKey id) throws FDResourceException {
		Connection conn = null;
		boolean result = false;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			result = dao.removeItem(conn, identity, id);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
                    close(conn);
		}
		return result;
	}

    // CCL
	public String createCustomerCreatedList(FDIdentity identity, StoreContext storeContext, String listName) throws FDResourceException, FDCustomerListExistsException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			if (dao.isCustomerList(conn,identity,EnumCustomerListType.CC_LIST,listName)) throw new FDCustomerListExistsException();
			return dao.createCustomerCreatedList(conn, identity, listName, storeContext);
		} catch (FDCustomerListExistsException e) {
			this.getSessionContext().setRollbackOnly();
			throw e;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}
	}
	
    // CCL
	public void deleteCustomerCreatedList(FDIdentity identity, String listName, StoreContext storeContext) throws FDResourceException {
		Connection conn = null;
		if (getCustomerCreatedLists(identity, storeContext).size() <= 1) {
			// don't delete the last list.
			return;
		}
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			dao.deleteCustomerCreatedList(conn, identity, listName);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
                    close(conn);
		}
	}

    // CCL
	public void deleteShoppingList( String listId ) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			dao.deleteShoppingListById( conn, listId );
		} catch ( SQLException e ) {
			throw new FDResourceException( e );
		} finally {
			close( conn );
		}
	}
	
    // CCL
	public boolean isCustomerList(FDIdentity identity, EnumCustomerListType type, String listName) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			return dao.isCustomerList(conn, identity, type != null ? type : EnumCustomerListType.CC_LIST, listName);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
                    close(conn);
		}
	}

	private static final String DEFAULT_CCL_NAME_SUFFIX = "'s List";
	
	// CCL
	public List<FDCustomerCreatedList> getCustomerCreatedLists(FDIdentity identity, StoreContext storeContext) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();

			List<FDCustomerCreatedList> lists = dao.getCustomerCreatedLists(conn, identity, storeContext); 
			if (lists.isEmpty()) { 
				/*FDUser user = FDUserDAO.recognizeWithIdentity(conn, identity);
				if (user.isAnonymous()) {
					throw new FDResourceException("User does not exists with identity "+identity);
				}*/
				ErpCustomerInfoModel customerInfo =FDCustomerFactory.getErpCustomerInfo(identity);
				if (null == customerInfo) {
					throw new FDResourceException("User does not exists with identity "+identity);
				}
				String firstName = null !=customerInfo.getFirstName()? customerInfo.getFirstName():"";
				String defaultName = firstName+ DEFAULT_CCL_NAME_SUFFIX;
				dao.createCustomerCreatedList(conn, identity, defaultName, storeContext);
				lists = dao.getCustomerCreatedLists(conn, identity, storeContext);
			}
			OrderLineUtil.cleanProductLists(lists);
			return lists;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
                    close(conn);
		}
	}
	
	// CCL
	public List<FDCustomerListInfo> getCustomerCreatedListInfos(FDIdentity identity, StoreContext storeContext) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();

			List<FDCustomerListInfo> lists = dao.getCustomerCreatedListInfos(conn, identity); 
			if (lists.isEmpty()) { 
//				FDUserI user = FDCustomerManager.recognize(identity);
				ErpCustomerInfoModel customerInfo =FDCustomerFactory.getErpCustomerInfo(identity);
				String firstName = null !=customerInfo && null !=customerInfo.getFirstName()? customerInfo.getFirstName():"";
				String defaultName = firstName+ DEFAULT_CCL_NAME_SUFFIX;
				dao.createCustomerCreatedList(conn, identity, defaultName, storeContext);
				lists = dao.getCustomerCreatedListInfos(conn, identity);
			}
			return lists;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} /*catch (FDAuthenticationException e) {
			throw new FDResourceException(e);
		}*/ finally {
                    close(conn);
		}
	}

	// CCL
	public List<FDCustomerListInfo> getStandingOrderListInfos(FDIdentity identity) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();

			return dao.getStandingOrderListInfos(conn, identity);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after removeCustomerListItem", e);
				}
			}
		}
	}

	// this is a generic api modify the customer list. just make sure name does not duplicate before calling this 
	public void modifyCustomerCreatedList(FDCustomerList list) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			// need to decide static or this way
			FDCustomerListDAO dao = new FDCustomerListDAO();
			dao.updateCustomerList(conn, list);
		} catch(SQLException e) {
			throw new FDResourceException(e);
		} finally {
                    close(conn);
		}
	}


	// this API will copy the customer list from source to target.
	// currently this is not getting used	
	public void copyCustomerCreatedList(FDCustomerList sourceCCList,FDCustomerList targetCCList) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			// need to decide static or this way
			FDCustomerListDAO dao = new FDCustomerListDAO();
			// first figure out it is product or recipe before merging
			if(sourceCCList instanceof FDCustomerProductList && targetCCList instanceof FDCustomerProductList) 
			{
				FDCustomerProductList targerList=(FDCustomerProductList)targetCCList;
				//	iterate through the new list and add individual Item to the OldList
				for (Iterator<FDCustomerListItem> i = sourceCCList.getLineItems().iterator(); i.hasNext();)
				{						
					FDCustomerProductListLineItem item = (FDCustomerProductListLineItem) i.next();
					item.setLastPurchase(new Date());
					targerList.mergeLineItem(item);
				}					
			}
			else if(sourceCCList instanceof FDCustomerRecipeList && targetCCList instanceof FDCustomerRecipeList)
			{
				FDCustomerRecipeList targerList=(FDCustomerRecipeList)targetCCList;
				//	iterate through the new list and add individual Item to the OldList
				for (Iterator<FDCustomerListItem> i = sourceCCList.getLineItems().iterator(); i.hasNext();)
				{						
					FDCustomerRecipeListLineItem item = (FDCustomerRecipeListLineItem) i.next();
					item.setLastPurchase(new Date());
					targerList.mergeRecipe(item.getRecipeId(),false);
				}					
			}
			else
			{
				// cannot copy since expected customer list type cannot be found
				throw new FDResourceException("unexpected customer List type found while copying");
			}
			
			// store the updated list
			dao.store(conn, targetCCList);
		}
		catch(SQLException e)
		{
			throw new FDResourceException(e);
		} finally {
                    close(conn);
		}
	}

	public FDCustomerCreatedList getCustomerCreatedList(FDIdentity identity,String ccListId) throws FDResourceException
	{
		Connection conn = null;			
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			FDCustomerCreatedList ccl = dao.getCustomerCreatedList(conn, identity,ccListId);
			if ( ccl != null ) {
				ccl.cleanList();
			} else { 
				LOGGER.warn ( "Customer list not found for id : " + ccListId + "; returning null.");
			}
			return ccl;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}			
	}
	
	public String getListName(FDIdentity identity, String ccListId) throws FDResourceException {
		Connection conn = null;			
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			return dao.getListName(conn, identity, ccListId);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			close(conn);
		}			
	}
	
	public void renameCustomerCreatedList(FDIdentity identity, String oldName, String newName) throws FDCustomerListExistsException, FDResourceException {
		Connection conn = null;			
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			if (dao.isCustomerList(conn, identity, EnumCustomerListType.CC_LIST, newName)) throw new FDCustomerListExistsException();
			dao.renameCustomerCreatedList(conn, identity, oldName, newName);
		}catch (FDCustomerListExistsException e) {
			getSessionContext().setRollbackOnly();
			throw e;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after renameCustomerCreatedList", e);
				}
			}
		}	
	}
	
	public void renameShoppingList(String listId, String newName) throws FDResourceException {
			Connection conn = null;			
			try {
				conn = getConnection();
				FDCustomerListDAO dao = new FDCustomerListDAO();
				dao.renameShoppingList( conn, listId, newName );
			}catch (SQLException e) {
				throw new FDResourceException(e);
			} finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						LOGGER.warn("Trouble closing connection after renameShoppingList", e);
					}
				}
			}	
		}
	
	public void renameCustomerList(FDActionInfo info, EnumCustomerListType type, String oldName, String newName) throws FDCustomerListExistsException, FDResourceException {
		Connection conn = null;			
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			if (type == null)
				type = EnumCustomerListType.CC_LIST;
			if (dao.isCustomerList(conn, info.getIdentity(), type, newName)) throw new FDCustomerListExistsException();
			dao.renameCustomerList(conn, info.getIdentity(), type, oldName, newName);
			ErpActivityRecord rec = info.createActivity(EnumAccountActivityType.STANDINGORDER_MODIFIED);
			rec.setStandingOrderId(dao.getStandingOrderIdByListName(conn, info.getIdentity(), newName));
			this.logActivity(rec);
		}catch (FDCustomerListExistsException e) {
			getSessionContext().setRollbackOnly();
			throw e;
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after renameCustomerList", e);
				}
			}
		}	
	}
	
	public FDCustomerProductList getOrderDetails(String erpCustomerId, List<String> skus) throws FDResourceException {
		Connection conn = null;			
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();

            return dao.getOrderDetails(conn,erpCustomerId,skus);
		} catch (SQLException e) {
			System.out.println(">>> " + e);
			throw new FDResourceException(e);
		} finally {
                    close(conn);
		}	
	}


	public FDStandingOrderList getStandingOrderList(FDIdentity identity, String soListId) throws FDResourceException
	{
		Connection conn = null;			
		try {
			conn = getConnection();
			FDCustomerListDAO dao = new FDCustomerListDAO();
			return dao.getStandingOrderList(conn, identity, soListId);
		} catch (SQLException e) {
			throw new FDResourceException(e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.warn("Trouble closing connection after getStandingOrderList", e);
				}
			}
		}
	}

	public void logActivity(ErpActivityRecord record) {
		new ErpLogActivityCommand(LOCATOR, record).execute();
	}
}
