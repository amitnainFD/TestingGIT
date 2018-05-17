/*
 * $Workfile:InventoryWarmupSessionBean.java$
 *
 * $Date:6/20/03 1:48:40 PM$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */

package com.freshdirect.fdstore.warmup.ejb;

import java.util.*;
import java.sql.*;

import javax.ejb.*;
import javax.transaction.*;

import com.freshdirect.framework.core.*;

import com.freshdirect.fdstore.*;
import com.freshdirect.fdstore.customer.*;
import com.freshdirect.fdstore.util.CartFactory;
import com.freshdirect.fdstore.util.CartLineFactory;
import com.freshdirect.fdstore.content.*;

import org.apache.log4j.*;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * performs inventory checks on skus to sync inventory on the site with inventory levels in the plant.
 *
 * @version $Revision:8$
 * @author $Author:Viktor Szathmary$
 */
public class InventoryWarmupSessionBean extends SessionBeanSupport {
    
    private static Category LOGGER = LoggerFactory.getInstance( InventoryWarmupSessionBean.class );
    
    public InventoryWarmupSessionBean() {
        super();
    }
    
    public void syncInventory(String dept) throws FDResourceException {
        //
        // find the dept in the store
        //
        ContentFactory cFactory = ContentFactory.getInstance();
        DepartmentModel deptModel = null;
        for (Iterator dIter = cFactory.getStore().getDepartments().iterator(); dIter.hasNext(); ) {
            DepartmentModel d = (DepartmentModel) dIter.next();
            if (d.getContentName().equalsIgnoreCase(dept)) {
                // this one...
                deptModel = d;
                break;
            }
        }
        if (deptModel == null) {
            throw new EJBException("No such department \"" + dept + "\"");
        }
        LOGGER.info("checking inventory for department " + deptModel.getFullName());
        //
        // pick a random identity and make a cart to use
        //
        FDIdentity identity = getRandomCustomerIdentity();
        FDCartModel cart = CartFactory.createCart(identity);
        //
        // walk the dept to find all the skus to check
        //
        for (Iterator catIter = deptModel.getCategories().iterator(); catIter.hasNext(); ) {
            syncInventory((CategoryModel) catIter.next(), identity, cart);
        }
    }
    
    private void syncInventory(CategoryModel catModel, FDIdentity identity, FDCartModel cart) throws FDResourceException {
        
        LOGGER.info("checking inventory for category " + catModel.getFullName());
        //
        // do the products in this category
        //
		Collection prods = catModel.getProducts();
		
		cart.setOrderLines( new CartLineFactory().createOrderLines(prods,ContentFactory.getInstance().getCurrentUserContext()) );

        //
        // perform availability check
        //
        FDCustomerManager.checkAvailability(identity, cart, 5*60*1000);
        //
        // walk the subcategories
        //
        for (Iterator subcatIter = catModel.getSubcategories().iterator(); subcatIter.hasNext(); ) {
            syncInventory((CategoryModel) subcatIter.next(), identity, cart);
        }
        
    }
    
    
    public FDIdentity getRandomCustomerIdentity() throws FDResourceException {
        
        UserTransaction utx = getSessionContext().getUserTransaction();
        try {
            utx.begin();
            //
            // set a timeout period for this transaction (in seconds)
            //
            utx.setTransactionTimeout(3000);
            
            List idents = new ArrayList();
            try {
                //
                //  grab a database connection
                //
                Connection conn = getConnection();
                //
                // get the identities of a bunch of active users
                //
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("select c.id, fdc.id from cust.fdcustomer fdc, cust.customer c, cust.paymentmethod pm, cust.address ad " +
                "where fdc.ERP_CUSTOMER_ID=c.id and pm.CUSTOMER_ID=c.id and ad.CUSTOMER_ID=c.id and c.ACTIVE='1'");
                while (rs.next()) {
                    idents.add(new FDIdentity(rs.getString(1), rs.getString(2)));
                }
                rs.close();
                stmt.close();
                conn.close();
                
                utx.commit();
            } catch (Exception rune) {
                utx.setRollbackOnly();
                throw new FDResourceException(rune);
            }
            //
            // shuffle
            //
            java.util.Collections.shuffle(idents);
            return (FDIdentity) idents.get(0);
            
        } catch (NotSupportedException nse) {
            throw new FDResourceException(nse);
        } catch (SystemException se) {
            throw new FDResourceException(se);
        }
        
    }
    
}
