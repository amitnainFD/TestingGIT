package com.freshdirect.fdstore.promotion;

import com.freshdirect.common.customer.EnumServiceType;
import com.freshdirect.customer.EnumChargeType;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpDepotAddressModel;
import com.freshdirect.deliverypass.DlvPassConstants;
import com.freshdirect.fdstore.customer.FDCartModel;
import com.freshdirect.fdstore.customer.FDUserI;

/**
 * Ensures delivery charge is waived if the CSR requests it, or a particular depot has the "waive dlv charge" flag set.
 */
public class WaiveDeliveryCharge {

    private final static String PROMO_CODE = "DELIVERY";

    public static boolean apply(FDUserI user) {
        FDCartModel cart = user.getShoppingCart();
        if (user.getMasqueradeContext() != null ? user.getMasqueradeContext().isCsrWaivedDeliveryCharge() : false) {
            cart.setChargeWaived(EnumChargeType.DELIVERY, true, PROMO_CODE);
            // If a delivery pass is applied, revoke it back.
            cart.setDlvPassApplied(false);
            return true;
        }
        // Make sure the dlv pass is not applied if the service type is not HOME.
        if (cart.isDlvPassApplied()) {
            if (user.getSelectedServiceType() == EnumServiceType.HOME) {
                cart.setChargeWaived(EnumChargeType.DELIVERY, true, DlvPassConstants.PROMO_CODE, user.isWaiveDPFuelSurCharge(false));
            } else {
                // Else if coporate delivery revoke the delivery pass.
                cart.setDlvPassApplied(false);
                cart.setChargeWaived(EnumChargeType.DELIVERY, false, DlvPassConstants.PROMO_CODE);
            }
        }

        ErpAddressModel address = cart.getDeliveryAddress();
        if (address != null && address instanceof ErpDepotAddressModel) {
            ErpDepotAddressModel depot = (ErpDepotAddressModel) address;
            if (depot.isDeliveryChargeWaived()) {
                cart.setChargeWaived(EnumChargeType.DELIVERY, true, PROMO_CODE);
                return true;
            }
        }

        if (user.getMasqueradeContext() != null ? user.getMasqueradeContext().isCsrWaivedDeliveryPremium() : false) {
            cart.setChargeWaived(EnumChargeType.DLVPREMIUM, true, PROMO_CODE);
            return true;
        }

        return false;
    }

}
