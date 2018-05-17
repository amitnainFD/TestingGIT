/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */

package com.freshdirect.fdstore.customer;


import java.rmi.RemoteException;
import java.util.Date;

/**
 * FDCustomer interface
 *
 * @version    $Revision$
 * @author     $Author$
 * @stereotype fd-interface
 */
public interface FDCustomerI {
    /**
     * @clientCardinality 1
     * @supplierCardinality 1 */
    /*#FDShoppingListI lnkCustomerList;*/
    
    /**
     * @clientCardinality 1
     * @supplierCardinality 1
     */
    /*#ErpCustomerI lnkErpCustomerI;*/
    
    /**
     * @clientCardinality 1
     * @supplierCardinality 1
     */
    /*# ProfileI lnkProfile; */
    
    /**
     * @clientCardinality 1
     * @supplierCardinality 1
     */
    /*#FDCartModel lnkFDShoppingCart;*/
    
    /**@link aggregationByValue
     * @clientCardinality 1
     * @supplierCardinality 0..**/
    /*#FDOrderModel lnkFDOrder;*/
    
	public String getErpCustomerPK() throws RemoteException;

	public void setErpCustomerPK(String erpCustomerPK) throws RemoteException;
    
    //public FDCartModel getShoppingCart() throws RemoteException;
    
    //public void setShoppingCart(FDCartModel cart) throws RemoteException;
    
    public void incrementLoginCount() throws RemoteException;
    
    public int getLoginCount() throws RemoteException;
    
    public Date getLastLogin() throws RemoteException;
    
    String getDefaultShipToAddressPK() throws RemoteException;
    
    void setDefaultShipToAddressPK(String addressPK) throws RemoteException;
    
    String getDefaultPaymentMethodPK() throws RemoteException;
    
    void setDefaultPaymentMethodPK(String pmPK) throws RemoteException;
    
    String getDefaultDepotLocationPK() throws RemoteException;
    
    void setDefaultDepotLocationPK(String locationId) throws RemoteException;
    
    public int incrementPymtVerifyAttempts() throws RemoteException;
	public void setPymtVerifyAttempts(int pymtVerifyAttempts)  throws RemoteException;
	public int getPymtVerifyAttempts()  throws RemoteException;
	public void resetPymtVerifyAttempts() throws RemoteException;

    
}

