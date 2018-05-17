/*
 * MealItem.java
 *
 * Created on December 5, 2002, 5:34 PM
 */

package com.freshdirect.fdstore.content.meal;

import com.freshdirect.framework.core.*;

/**
 *
 * @author  mrose
 * @version
 */
public class MealItemModel extends ModelSupport {
    
    private EnumMealItemType type;
    private String name;
    private int quantity;
    private double unitPrice;
    
    public MealItemModel(EnumMealItemType type, String name, double unitPrice) {
        this();
        this.type = type;
        this.name = name;
        this.unitPrice = unitPrice;
    }
    
    public MealItemModel() {
        super();
        this.type = null;
        this.name = null;
        this.quantity = 0;
        this.unitPrice = 0.0;
    }
    
    public EnumMealItemType getType() {
        return this.type;
    }
    
    public void setType(EnumMealItemType type) {
        this.type = type;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getQuantity() {
        return this.quantity;
    }
    
    public void setQuantity(int q) {
        this.quantity = q;
    }
    
    public double getUnitPrice() {
        return this.unitPrice;
    }
    
    public void setUnitPrice(double price) {
        this.unitPrice = price;
    }
    
}
