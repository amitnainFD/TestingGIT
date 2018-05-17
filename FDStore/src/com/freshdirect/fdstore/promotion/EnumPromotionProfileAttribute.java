package com.freshdirect.fdstore.promotion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

public class EnumPromotionProfileAttribute extends Enum {
	
	public static final EnumPromotionProfileAttribute CHEFS_TABLE = new EnumPromotionProfileAttribute("Chefs Table","ChefsTable","1");
	public static final EnumPromotionProfileAttribute VIP_CUSTOMER = new EnumPromotionProfileAttribute("VIP/Reserved Dlv", "VIPCustomer","true");
	public static final EnumPromotionProfileAttribute INACTIVE = new EnumPromotionProfileAttribute("Inactive", "MetalCategory","0");
	public static final EnumPromotionProfileAttribute GOLD = new EnumPromotionProfileAttribute("Gold", "MetalCategory","1");
	public static final EnumPromotionProfileAttribute SILVER = new EnumPromotionProfileAttribute("Silver", "MetalCategory","2");
	public static final EnumPromotionProfileAttribute BRONZE = new EnumPromotionProfileAttribute("Bronze", "MetalCategory","3");
	public static final EnumPromotionProfileAttribute COPPER = new EnumPromotionProfileAttribute("Copper", "MetalCategory","4");
	public static final EnumPromotionProfileAttribute TIN = new EnumPromotionProfileAttribute("Tin", "MetalCategory","5");
	public static final EnumPromotionProfileAttribute NEW = new EnumPromotionProfileAttribute("New", "MetalCategory","6");
	
	private String attribute;
	private String value;
	public EnumPromotionProfileAttribute(String name, String attribute, String value) {
		super(name);
		this.attribute = attribute;
		this.value = value;
	}
	
	public static Map getEnumMap() {
		return getEnumMap(EnumPromotionProfileAttribute.class);
	}

	public static List getEnumList() {
		return getEnumList(EnumPromotionProfileAttribute.class);
	}

	public static Iterator iterator() {
		return iterator(EnumPromotionProfileAttribute.class);
	}
	
	public static String getName(String attribute, String value){
		List enumList =getEnumList();
		for (Iterator iterator = enumList.iterator(); iterator.hasNext();) {
			EnumPromotionProfileAttribute enumPromotionProfileAttribute = (EnumPromotionProfileAttribute) iterator.next();
			if(enumPromotionProfileAttribute.getAttribute().equalsIgnoreCase(attribute) && enumPromotionProfileAttribute.getValue().equalsIgnoreCase(value)){
				return enumPromotionProfileAttribute.getName();
			}
		}
		return "";		
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
