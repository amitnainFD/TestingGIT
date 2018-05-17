package com.freshdirect.fdstore.customer.ejb;


/**
 *  Enumeration of the different list types the customer might have.
 */
public enum EnumCustomerListType {
	
	/** The customer's shopping list. */
	SHOPPING_LIST("AUTO"),	
	
	/** A recipe list. */
	RECIPE_LIST("RECIPE"),
	
	/** The customer's own list, created and maintained by the customer. */
	CC_LIST("CCL"),			
	
	/** List belonging to a standing order. */
	SO("SO");				
	
	String name;
	
	/**
	 *  Constructor.
	 *  
	 *  @param typeName the name of the list type
	 */
	private EnumCustomerListType(String typeName) {
		this.name = typeName;
	}

	public String getName() {
		return name;
	}

	
	/**
	 *  Return the list type enumerated object by specifying the list type
	 *  name.
	 *  
	 *  @param typeName the name of the customer list's type
	 *  @return the enumerated object based on the specified name.
	 */
	public static EnumCustomerListType getEnum(String typeName) {
		for (EnumCustomerListType t : EnumCustomerListType.values()) {
			if (t.getName().equalsIgnoreCase(typeName)) {
				return t;
			}
		}
		
		return null;
	}

	/**
	 *  Return a string representation of this customer list type object.
	 *  
	 *  @return the name of the customer list type.
	 */
	@Override
	public String toString() {
		return this.getName();
	}
}
