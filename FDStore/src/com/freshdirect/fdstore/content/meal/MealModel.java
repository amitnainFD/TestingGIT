/*
 * Meal.java
 *
 * Created on December 5, 2002, 5:34 PM
 */

package com.freshdirect.fdstore.content.meal;

import java.util.*;

import com.freshdirect.framework.core.*;

/**
 *
 * @author  mrose
 * @version 
 */
public class MealModel extends ModelSupport {
    
    private List            items;
    private String          name;
    private Date            delivery;
    private EnumMealStatus  status;
    private double          price;

    /** Creates new Meal */
    public MealModel() {
        super();
        items = new ArrayList();
        name = null;
        delivery = null;
        status = EnumMealStatus.NEW;
        price = 0.0;
    }
    
    public void setItem(int idx, MealItemModel item) {
        if (item.getQuantity() == 0) this.removeItem(idx);
        this.items.set(idx, item);
    }
    
    public void removeItem(int idx) {
        this.items.remove(idx);
    }
    
    public void addItem(MealItemModel item) {
        this.items.add(item);
    }
    
    public void addAllItems(List items) {
        this.items.addAll(items);
    }
    
    public List getItems(EnumMealItemType type) {
        List ret = new ArrayList();
        for (Iterator iter = this.items.iterator(); iter.hasNext(); ) {
            MealItemModel mi = (MealItemModel) iter.next();
            if (mi.getType().equals(type)) {
                ret.add(mi);
            }
        }
        return Collections.unmodifiableList(ret);
    }
    
    public List getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public void setItems(List items) {
        this.items.clear();
        this.items.addAll(items);
    }
    
    public void setDelivery(Date d) {
        this.delivery = d;
    }
    
    public Date getDelivery() {
        return this.delivery;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setStatus(EnumMealStatus s) {
        this.status = s;
    }
    
    public EnumMealStatus getStatus() {
        return this.status;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public double getPrice() {
        return this.price;
    }
   
    
}
