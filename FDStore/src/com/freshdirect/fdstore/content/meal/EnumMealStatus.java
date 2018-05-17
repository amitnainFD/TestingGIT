/*
 * EnumMealItemType.java
 *
 * Created on December 5, 2002, 5:34 PM
 */

package com.freshdirect.fdstore.content.meal;

import java.util.*;

/**
 *
 * @author  mrose
 * @version
 */
public class EnumMealStatus implements java.io.Serializable {
    
    public final static EnumMealStatus NEW = new EnumMealStatus(0, "NEW", "Newly Placed Order");
    public final static EnumMealStatus DEP = new EnumMealStatus(1, "DEP", "Deposit Taken");
    public final static EnumMealStatus CAN = new EnumMealStatus(2, "CAN", "Cancelled");
    public final static EnumMealStatus CHG = new EnumMealStatus(3, "COL", "Charged In Full");
    public final static EnumMealStatus DEL = new EnumMealStatus(4, "DEL", "Delivered");
    
    private static List types = new ArrayList();
    static {
        types.add(NEW);
        types.add(DEP);
        types.add(CAN);
        types.add(CHG);
        types.add(DEL);
    }
    
    public static List getStatuses() {
        return Collections.unmodifiableList(types);
    }   
    
    private final int id;
    private final String typeName;
    private final String displayName;
    
    private EnumMealStatus(int id, String typeName, String displayName) {
        this.id = id;
        this.typeName = typeName;
        this.displayName = displayName;
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getTypeName() {
        return this.typeName;
    }
    
    public String getDisplayName() {
        return this.displayName;
    }
    
    public static EnumMealStatus getType(String tn) {
        for (Iterator iter = types.iterator(); iter.hasNext(); ) {
            EnumMealStatus t = (EnumMealStatus) iter.next();
            if (t.getTypeName().equalsIgnoreCase(tn)) {
                return t;
            }
        }
        return null;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof EnumMealStatus) {
            EnumMealStatus mit = (EnumMealStatus) obj;
            if (this.id == mit.id) {
                return true;
            }
        }
        return false;
    }
    
}
