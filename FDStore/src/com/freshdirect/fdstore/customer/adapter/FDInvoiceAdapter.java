package com.freshdirect.fdstore.customer.adapter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.freshdirect.customer.EnumChargeType;
import com.freshdirect.customer.ErpAppliedCreditModel;
import com.freshdirect.customer.ErpChargeLineModel;
import com.freshdirect.customer.ErpDiscountLineModel;
import com.freshdirect.customer.ErpInvoiceLineI;
import com.freshdirect.customer.ErpInvoiceLineModel;
import com.freshdirect.customer.ErpInvoiceModel;

public class FDInvoiceAdapter implements Serializable {
	
	private static final long	serialVersionUID	= -3625161041187207843L;
	
	private ErpInvoiceModel invoice;
	
	public FDInvoiceAdapter(ErpInvoiceModel invoice){
		this.invoice = invoice;
	}
	
	public double getInvoicedTotal(){
		return invoice.getAmount();
	}
	
	public double getInvoicedSubTotal(){
        return invoice.getSubTotal();
    }
    
    public double getInvoicedTaxValue(){
        return invoice.getTax();
    }
    
    public double getInvoicedDepositValue() {
    	return invoice.getDepositValue();
    }
    
    public double getActualDiscountValue(){
    	List<ErpDiscountLineModel> d = invoice.getDiscounts();
		if (d == null || d.isEmpty()) {
			return 0;
		}
		double totalDiscountAmount = 0.0;
		for ( ErpDiscountLineModel discountLine : d ) {
			totalDiscountAmount += discountLine.getDiscount().getAmount();
		}
		return totalDiscountAmount;
    }
    
    /**
     * Retrieves the list of applied credits recorded for a given order.
     * @return read-only Collection of ErpAppliedCreditModel objects.
     */
    public Collection<ErpAppliedCreditModel> getAppliedCredits() {
        return invoice.getAppliedCredits();
    }

    public double getCustomerCreditsValue(){
        Collection<ErpAppliedCreditModel> appliedCredits = getAppliedCredits();
        double creditValue = 0;

        for ( ErpAppliedCreditModel ac : appliedCredits ) {
            creditValue += ac.getAmount();
        }
        return creditValue;
    }
    
    // new Added methods
    public List<ErpInvoiceLineModel> getInvoiceLines(){
    	return invoice.getInvoiceLines();
    }
    
    public ErpInvoiceLineI getInvoiceLine(String lineNumber){
    	return invoice.getInvoiceLine(lineNumber);
    }
    
    public List<ErpChargeLineModel> getInvoicedCharges(){
    	return invoice.getCharges();
    }
    
    public double getInvoicedDeliveryCharge(){
    	
    	return getInvoicedDeliverySurcharge() + getInvoicedDeliveryPremium();
    }
    
    public double getInvoicedDeliverySurcharge(){
    	ErpChargeLineModel c = getCharge(EnumChargeType.DELIVERY);
    	return c != null ? c.getTotalAmount() : 0.0;
    }
    
    public double getInvoicedDeliveryPremium(){
    	ErpChargeLineModel c = getCharge(EnumChargeType.DLVPREMIUM);
    	return c != null ? c.getTotalAmount() : 0.0;
    }
    
    
	public ErpChargeLineModel getCharge(EnumChargeType chargeType) {
		for ( ErpChargeLineModel curr : invoice.getCharges() ) {
			if (chargeType.equals( curr.getType() )) {
				return curr;
			}
		}
		return null;
	}

    public List<ErpDiscountLineModel> getActualDiscounts(){    	
    	return invoice.getDiscounts();
    }
    
    public double getInvoicedTip(){
    	ErpChargeLineModel c = getCharge(EnumChargeType.TIP);
    	return c != null ? c.getTotalAmount() : 0.0;
    }
	
}
