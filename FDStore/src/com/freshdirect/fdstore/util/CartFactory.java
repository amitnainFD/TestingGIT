package com.freshdirect.fdstore.util;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.NamingException;

import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpPaymentMethodI;
import com.freshdirect.fdlogistics.model.FDDeliveryZoneInfo;
import com.freshdirect.fdlogistics.model.FDInvalidAddressException;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdlogistics.model.FDTimeslot;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.fdstore.customer.FDIdentity;
import com.freshdirect.fdstore.warmup.ejb.InventoryWarmupHome;
import com.freshdirect.fdstore.warmup.ejb.InventoryWarmupSB;
import com.freshdirect.logistics.delivery.model.EnumReservationType;

public class CartFactory {

	public static FDCartModel createCart(FDIdentity identity) throws FDResourceException {
        
        FDCartModel cart = new FDCartModel();
        
        Collection addrs = FDCustomerManager.getShipToAddresses(identity);
        ErpAddressModel address = (ErpAddressModel)(addrs.toArray())[0];
        
        cart.setDeliveryAddress(address);
        Collection ccards = FDCustomerManager.getPaymentMethods(identity);
        cart.setPaymentMethod((ErpPaymentMethodI)((ccards.toArray())[0]));
        
        Calendar begCal = Calendar.getInstance();
        begCal.add(Calendar.DATE, 1);
        
        Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.DATE, 7);
        endCal.set(Calendar.HOUR, 12);
        endCal.set(Calendar.MINUTE, 00);
        endCal.set(Calendar.AM_PM, Calendar.AM);
        
        //FDDeliveryManager.getInstance().scrubAddress(dlvAddress);
            
        try {
            FDDeliveryZoneInfo zInfo = FDDeliveryManager.getInstance().getZoneInfo(address, new java.util.Date(), null, null);
            
			FDReservation deliveryReservation =
				new FDReservation(
					null,
					new FDTimeslot(),
					endCal.getTime(), EnumReservationType.STANDARD_RESERVATION, identity.getErpCustomerPK(), 
					null, false, null,20,null,false,null,null);
            
            cart.setZoneInfo(zInfo);
            cart.setDeliveryReservation(deliveryReservation);
            
            return cart;
            
        } catch (FDInvalidAddressException fdiae) {
            throw new FDResourceException(fdiae);
        }
    }
    

	public static FDIdentity getRandomIdentity() throws FDResourceException {
		try {
			Context ctx = FDStoreProperties.getInitialContext();
			InventoryWarmupHome home = (InventoryWarmupHome) ctx.lookup("freshdirect.fdstore.InventoryWarmup");
			InventoryWarmupSB sb = home.create();
			
			return sb.getRandomCustomerIdentity();
		} catch (RemoteException ex) {
			throw new FDResourceException(ex);
		} catch (NamingException ex) {
			throw new FDResourceException(ex);
		} catch (CreateException ex) {
			throw new FDResourceException(ex);
		}
	}

}
