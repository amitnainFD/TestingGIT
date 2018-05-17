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
public class EnumMealItemType implements java.io.Serializable {
    
    public final static EnumMealItemType ENTR = new EnumMealItemType(0, "ENTR", "Entree");
    public final static EnumMealItemType SIDE = new EnumMealItemType(1, "SIDE", "Side Dish");
    public final static EnumMealItemType DESS = new EnumMealItemType(2, "DESS", "Dessert");
    public final static EnumMealItemType APPE = new EnumMealItemType(3, "APPE", "Appetizer");
    public final static EnumMealItemType SALD = new EnumMealItemType(4, "SALD", "Salad");
    public final static EnumMealItemType FOND = new EnumMealItemType(5, "FOND", "Fondue");
    
    private static List types = new ArrayList();
    static {
        types.add(ENTR);
        types.add(SIDE);
        types.add(DESS);
        types.add(APPE);
        types.add(SALD);
        types.add(FOND);
    }
    
    private final int id;
    private final String typeName;
    private final String displayName;
    
    private EnumMealItemType(int id, String typeName, String displayName) {
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
    
    public static EnumMealItemType getType(String tn) {
        for (Iterator iter = types.iterator(); iter.hasNext(); ) {
            EnumMealItemType t = (EnumMealItemType) iter.next();
            if (t.getTypeName().equalsIgnoreCase(tn)) {
                return t;
            }
        }
        return null;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof EnumMealItemType) {
            EnumMealItemType mit = (EnumMealItemType) obj;
            if (this.id == mit.id) {
                return true;
            }
        }
        return false;
    }
    
}
