/*
 * FDCreditSummary.java
 *
 * Created on February 26, 2003, 8:31 PM
 */

package com.freshdirect.fdstore.customer;

import java.util.*;

/**
 *
 * @author  mrose
 * @version 
 */
public class FDCreditSummary implements java.io.Serializable {

    /** Holds value of property customerName. */
    private String customerName;    
    
    /** Holds value of property orderNumber. */
    private String orderNumber;    

    /** Holds value of property deliveryDate. */
    private Date deliveryDate;
    
    /** Holds value of property invoiceAmount. */
    private double invoiceAmount;
    
    /** Holds value of property creditAmount. */
    private double creditAmount;
    
    /** Holds value of property previousCreditAmount. */
    private double previousCreditAmount;
    
    /** Holds value of property numberOfOrders. */
    private int numberOfOrders;
    
    private List items;
    
    /** Holds value of property note. */
    private String note;
    
    /** Holds value of property status. */
    private String status;
    
    /** Creates new FDCreditSummary */
    public FDCreditSummary() {
        items = new ArrayList();
    }
    
    /** Getter for property customerName.
     * @return Value of property customerName.
     */
    public String getCustomerName() {
        return customerName;
    }    
    
    /** Setter for property customerName.
     * @param customerName New value of property customerName.
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }    
    
    /** Getter for property orderNumber.
     * @return Value of property orderNumber.
     */
    public String getOrderNumber() {
        return orderNumber;
    }
    
    /** Setter for property orderNumber.
     * @param orderNumber New value of property orderNumber.
     */
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    /** Getter for property deliveryDate.
     * @return Value of property deliveryDate.
     */
    public Date getDeliveryDate() {
        return deliveryDate;
    }
    
    /** Setter for property deliveryDate.
     * @param deliveryDate New value of property deliveryDate.
     */
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    /** Getter for property invoiceAmount.
     * @return Value of property invoiceAmount.
     */
    public double getInvoiceAmount() {
        return invoiceAmount;
    }
    
    /** Setter for property invoiceAmount.
     * @param invoiceAmount New value of property invoiceAmount.
     */
    public void setInvoiceAmount(double invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }
    
    /** Getter for property creditAmount.
     * @return Value of property creditAmount.
     */
    public double getCreditAmount() {
        return creditAmount;
    }
    
    /** Setter for property creditAmount.
     * @param creditAmount New value of property creditAmount.
     */
    public void setCreditAmount(double creditAmount) {
        this.creditAmount = creditAmount;
    }
    
    /** Getter for property previousCreditAmount.
     * @return Value of property previousCreditAmount.
     */
    public double getPreviousCreditAmount() {
        return previousCreditAmount;
    }
    
    /** Setter for property previousCreditAmount.
     * @param previousCreditAmount New value of property previousCreditAmount.
     */
    public void setPreviousCreditAmount(double previousCreditAmount) {
        this.previousCreditAmount = previousCreditAmount;
    }
    
    /** Getter for property numberOfOrders.
     * @return Value of property numberOfOrders.
     */
    public int getNumberOfOrders() {
        return numberOfOrders;
    }
    
    /** Setter for property numberOfOrders.
     * @param numberOfOrders New value of property numberOfOrders.
     */
    public void setNumberOfOrders(int numberOfOrders) {
        this.numberOfOrders = numberOfOrders;
    }
    
    public void addItem(Item item) {
        this.items.add(item);
    }
    
    public List getItems() {
        return Collections.unmodifiableList(items);
    }
    
    /** Getter for property note.
     * @return Value of property note.
     */
    public String getNote() {
        return note;
    }
    
    /** Setter for property note.
     * @param note New value of property note.
     */
    public void setNote(String note) {
        this.note = note;
    }
    
    /** Getter for property status.
     * @return Value of property status.
     */
    public String getStatus() {
        return status;
    }
    
    /** Setter for property status.
     * @param status New value of property status.
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    public static class Item implements java.io.Serializable {
        
        /** Holds value of property description. */
        private String description;
        
        /** Holds value of property configuration. */
        private String configuration;
        
        /** Holds value of property skuCode. */
        private String skuCode;
        
        /** Holds value of property quantity. */
        private double quantity;
        
        /** Holds value of property reason. */
        private String reason;
        
        public Item() {
        }
        
        /** Getter for property description.
         * @return Value of property description.
         */
        public String getDescription() {
            return description;
        }
        
        /** Setter for property description.
         * @param description New value of property description.
         */
        public void setDescription(String description) {
            this.description = description;
        }
        
        /** Getter for property configuration.
         * @return Value of property configuration.
         */
        public String getConfiguration() {
            return configuration;
        }
        
        /** Setter for property configuration.
         * @param configuration New value of property configuration.
         */
        public void setConfiguration(String configuration) {
            this.configuration = configuration;
        }
        
        /** Getter for property skuCode.
         * @return Value of property skuCode.
         */
        public String getSkuCode() {
            return skuCode;
        }
        
        /** Setter for property skuCode.
         * @param skuCode New value of property skuCode.
         */
        public void setSkuCode(String skuCode) {
            this.skuCode = skuCode;
        }
        
        /** Getter for property quantity.
         * @return Value of property quantity.
         */
        public double getQuantity() {
            return quantity;
        }
        
        /** Setter for property quantity.
         * @param quantity New value of property quantity.
         */
        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }
        
        /** Getter for property reason.
         * @return Value of property reason.
         */
        public String getReason() {
            return reason;
        }
        
        /** Setter for property reason.
         * @param reason New value of property reason.
         */
        public void setReason(String reason) {
            this.reason = reason;
        }
        
    }

}
