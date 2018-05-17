package com.freshdirect.fdstore.util.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.enums.Enum;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Category;
import org.apache.tools.ant.util.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.freshdirect.delivery.EnumDeliveryOption;
import com.freshdirect.fdstore.promotion.EnumPromotionStatus;
import com.freshdirect.fdstore.promotion.management.FDPromoChangeDetailModel;
import com.freshdirect.fdstore.promotion.management.FDPromoChangeModel;
import com.freshdirect.fdstore.promotion.management.FDPromoContentModel;
import com.freshdirect.fdstore.promotion.management.FDPromoCustStrategyModel;
import com.freshdirect.fdstore.promotion.management.FDPromoDlvTimeSlotModel;
import com.freshdirect.fdstore.promotion.management.FDPromoDlvZoneStrategyModel;
import com.freshdirect.fdstore.promotion.management.FDPromoDollarDiscount;
import com.freshdirect.fdstore.promotion.management.FDPromoPaymentStrategyModel;
import com.freshdirect.fdstore.promotion.management.FDPromoStateCountyRestriction;
import com.freshdirect.fdstore.promotion.management.FDPromotionNewModel;
import com.freshdirect.fdstore.promotion.management.WSAdminInfo;
import com.freshdirect.fdstore.promotion.management.WSPromotionInfo;
import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.metaparadigm.jsonrpc.AbstractSerializer;
import com.metaparadigm.jsonrpc.MarshallException;
import com.metaparadigm.jsonrpc.ObjectMatch;
import com.metaparadigm.jsonrpc.Serializer;
import com.metaparadigm.jsonrpc.SerializerState;
import com.metaparadigm.jsonrpc.UnmarshallException;

public class FDPromotionJSONSerializer extends AbstractSerializer {
	private static final long serialVersionUID = 4602538095592746033L;

	private static Category		LOGGER				= LoggerFactory.getInstance( FDPromotionJSONSerializer.class );

	private static Class<?>[] _serializableClasses = new Class[] {
		FDPromoContentModel.class, FDPromoCustStrategyModel.class,
		FDPromoPaymentStrategyModel.class, FDPromotionNewModel.class,
		FDPromoChangeModel.class, FDPromoChangeDetailModel.class,
		FDPromoDlvZoneStrategyModel.class, FDPromoDlvTimeSlotModel.class, WSPromotionInfo.class,WSAdminInfo.class, EnumPromotionStatus.class,
		FDPromoDollarDiscount.class,FDPromoStateCountyRestriction.class,EnumDeliveryOption.class
	};

	private static Class<?>[] _JSONClasses = new Class[] { JSONObject.class };

	
	private static Serializer instance = new FDPromotionJSONSerializer();

	private boolean appendClassInfo = true;
	
	/** Use getInstance() */
	private FDPromotionJSONSerializer() {}
	
	/** Get a reusable instance of this serializer.
	 * 
	 * @return serializer instance
	 */
	public static Serializer getInstance() { return instance; }

	public void setAppendClassInfo(boolean appendClassInfo) {
		this.appendClassInfo = appendClassInfo;
	}

	public boolean isAppendClassInfo() {
		return appendClassInfo;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Class[] getJSONClasses() {
		return _JSONClasses;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class[] getSerializableClasses() {
		return _serializableClasses;
	}

	@Override
	public Object marshall(SerializerState state, Object obj)
			throws MarshallException {
		return serializeRightHandSide(obj);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object obj) throws UnmarshallException {
		unmarshall(state, clazz, obj);
		return ObjectMatch.OKAY;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshall(SerializerState state, Class clazz, Object jsonObj)
			throws UnmarshallException {

		return deserializeRightHandSize(jsonObj);
	}


	/**
	 * @param obj
	 */
	Map<String, Method> collectProperties(Class<?> klass) {
		Map<String,Method> props = new HashMap<String,Method>();

		final boolean isModel = ModelSupport.class.isAssignableFrom(klass);
		
		// find getters
		for (Method m : klass.getMethods()) {
			if (m.getDeclaringClass().isAssignableFrom(ModelSupport.class))
				continue;
			
			if (m.getName().startsWith("get")) {
				if (isModel && (m.getName().equals("getId") || m.getName().equals("getPK") )) {
					continue;
				} else if (m.getName().equals("getClass")) {
					continue;
				} else if (m.getParameterTypes().length > 0) {
					continue;
				}

				final String prop = java.beans.Introspector.decapitalize(m.getName().substring(3));
				
				if (getSetter(klass, prop, m.getReturnType()) != null) {
					props.put(prop, m);
				} else {
					LOGGER.warn("[collectProperties] Prop '" + prop + "' skipped, no setter");
				}
			} else if (m.getName().startsWith("is")) {
				// boolean type
				final String prop = java.beans.Introspector.decapitalize(m.getName().substring(2));
				if (getSetter(klass, prop, m.getReturnType()) != null) {
					props.put(prop, m);
				} else {
					LOGGER.warn("[collectProperties] Prop '" + prop + "' skipped, no setter");
				}
			}
		}
		
		return props;
	}
	
	protected Method getSetter(Class<?> klazz, String prop, Class<?> valueType) {
		char chars[] = prop.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		final String setterName = "set"+new String(chars);

		Method setter = null;
		try {
			 setter = klazz.getMethod(setterName, valueType);
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		}

		return void.class.equals(setter.getReturnType()) ? setter : null;		
	}
	
	public void doSerialize(Object obj, Map<String, Method> props, JSONObject target) {
		List<String> sortedPropKeys = new ArrayList<String>(props.keySet());
		Collections.sort(sortedPropKeys, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});

		for (String propName : sortedPropKeys) {
			final Method m = props.get(propName);

			try {
				Object val = m.invoke(obj);
				if (val != null) {
					serializeKeyValuePair(target, propName, val);
				}
			} catch (IllegalArgumentException e) {
				LOGGER.error("Failed to serialize property " + propName, e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Failed to serialize property " + propName, e);
			} catch (InvocationTargetException e) {
				LOGGER.error("Failed to serialize property " + propName, e);
			}
		}
	}
	
	public void serializeKeyValuePair(JSONObject target, Object key, Object val) {
		if (key == null || val == null)
			return;

		final String serKey = getSerializedValue(key).toString();
		
		target.put(serKey, serializeRightHandSide(val));
	}


	protected Object serializeRightHandSide(Object val) {
		final String klassName = val.getClass().getName();
		if (val.getClass().isArray()) {
			JSONArray jarr = new JSONArray();
			serializeArray(jarr, Arrays.asList((Object[])val) );
			return jarr;
		} else if (val instanceof Iterable<?>) {
			JSONArray jarr = new JSONArray();
			serializeArray(jarr, (Iterable<?>)val);
			return jarr;
		} else if (val instanceof Map<?,?>) {
			JSONObject jarr = new JSONObject();
			serializeMap(jarr, (Map<?,?>)val);
			return jarr;
		} else if (klassName.startsWith("java.lang") ||
				klassName.startsWith("java.util") ||
				klassName.startsWith("java.sql") ||
				val instanceof org.apache.commons.lang.enums.Enum ||
				val instanceof java.lang.Enum<?>) {
			return getSerializedValue(val);
		} else {
			JSONObject jsonObj = new JSONObject();
			serializeObject(jsonObj, val);				
			return jsonObj;
		}
	}
	
	
	/**
	 * Serializes a map
	 * 
	 * @param target
	 * @param map
	 */
	public void serializeMap(JSONObject target, Map<?,?> map) {
		for (Object key : map.keySet()) {
			serializeKeyValuePair(target, key, map.get(key));
		}
	}
	
	
	/**
	 * Serializes an iterable set to arr object
	 * @param arr Iterable object (Collection, Set, List, etc)
	 * @param arrval
	 */
	public void serializeArray(JSONArray arr, Iterable<?> arrval) {
		for (Object val : arrval) {
			arr.put(serializeRightHandSide(val));
		}
	}

	/**
	 * Serializes a non-primitive object by visiting its getters
	 * and serializing retrieved values
	 * @param target
	 * @param anObject
	 */
	public void serializeObject(JSONObject target, Object anObject) {
		Map<String,Method> props = collectProperties(anObject.getClass());
		doSerialize(anObject, props, target);
		
		if (appendClassInfo) {
			target.put("javaClass", anObject.getClass().getName());
		}
	}
	
	
	public Object getSerializedValue(Object val) {
		if (val instanceof String) {
			return val;
		} else if (val instanceof java.lang.Number) {
			return val;
		} else if (val instanceof org.apache.commons.lang.enums.Enum) {
			final Enum enum1 = (org.apache.commons.lang.enums.Enum)val;
			
			for (Field f : val.getClass().getFields()) {
				try {
					if (f.get(null).equals(enum1)) {
						return f.getName();
					}
				} catch (IllegalArgumentException e) {
					LOGGER.error("Failed to decode enum " + enum1, e);
				} catch (IllegalAccessException e) {
					LOGGER.error("Failed to decode enum " + enum1, e);
				}
			}
			
			LOGGER.debug("Serialize Apache enum " + val.getClass().getName() + " value " + val + " to " + enum1.getName() );
			return enum1.getName();
		} else if (val instanceof java.lang.Enum<?>) {
			java.lang.Enum<?> enm = (java.lang.Enum<?>) val;
			return enm.name();
		} else if (val instanceof java.sql.Date || val instanceof java.sql.Timestamp) {
			return Long.toString( ((java.util.Date)val).getTime() );
		} else if (val instanceof java.util.Date) {
			return DateFormatUtils.ISO_DATETIME_FORMAT.format((Date) val);
		} else {
			return val.toString();
		}
	}



	public Object deserializeRightHandSize(Object obj) {
		if (obj instanceof JSONObject) {
			JSONObject jsObject = (JSONObject) obj;
			if (jsObject.has("javaClass")) {
				try {
					return restoreObject(jsObject);
				} catch (NoSuchElementException e) {
					LOGGER.error("deserializeRightHandSize", e);
					return null;
				} catch (ClassNotFoundException e) {
					LOGGER.error("deserializeRightHandSize", e);
					return null;
				}
			} else {
				// MAP ??
			}
		} else if (obj instanceof JSONArray) {
			JSONArray arr = (JSONArray) obj;
			
			List l = new ArrayList();
			for (int i=0; i<arr.length(); i++) {
				Object v = arr.get(i);
				
				Object tv = deserializeRightHandSize(v);
				
				l.add(tv);
			}
			
			return l;
		}

		// return primitive type in original string format
		return obj;
	}
	
	public Object restoreObject(JSONObject jsObject) throws NoSuchElementException, ClassNotFoundException {
		Class<?> klass = Class.forName(jsObject.getString("javaClass"));
		
		// restore class
		try {
			Object obj = klass.newInstance();
			
			Map<String, Method> props = collectProperties(klass);
			for (String prop : props.keySet()) {
				Method getter = props.get(prop);
				
				// no data, skip property
				if (!jsObject.has(prop))
					continue;
				
				// skip if method has no return value
				Class<?> valueType = getter.getReturnType();
				if (valueType == null)
					continue;
				
				Method setter = getSetter(klass, prop, valueType);
				if (setter == null)
					continue;
				
				// deserialize value
				Object rhs = deserializeRightHandSize(jsObject.get(prop));
				
				if (valueType.isArray()) {
					//
					// ARRAY
					//
					if (rhs instanceof Collection) {
						Collection coll = (Collection) rhs;
						
		                Object vals = Array.newInstance(valueType.getComponentType(), coll.size());
						int k = 0;
						for (Object o : coll) {
		                    Array.set(vals, k++, getDeserializedValue(o, valueType.getComponentType()));
						}
						
						// set value
						silentInvoke(obj, setter, vals);
					} else {
						// Value is in not expected format, skip ...
						// System.err.println("BANG[1] rhs:" + rhs.getClass().getName() + "/ valueType: " + valueType);
						// LOGGER.warn("Unexpected rhs:" + rhs.getClass().getName() + "/ valueType: " + valueType));
						continue;
					}
				} else if (Iterable.class.isAssignableFrom(valueType)) {
					//
					// COLLECTION
					//
					
					Collection coll = (Collection) rhs;
					Collection valami;
					if (List.class.equals(valueType)) {
						valami = new ArrayList();
					} else if (Set.class.equals(valueType)) {
						valami = new HashSet();
					} else {
						valami = (Collection) valueType.newInstance();
					}

					for (Object o : coll) {
						valami.add(deserializeRightHandSize(o));
					}

					silentInvoke(obj, setter, valami);
				} else if (Map.class.isAssignableFrom(valueType)) {
					ParameterizedType rt = (ParameterizedType) getter.getGenericReturnType();
					final Class<?> keyType = (Class<?>) rt.getActualTypeArguments()[0];
					final Class<?> valType = (Class<?>) rt.getActualTypeArguments()[1];
					
					// Map map = (Map) valueType.newInstance();
					Map map;
					if (Map.class.equals(valueType)) {
						map = new HashMap();
					} else {
						map = (Map) valueType.newInstance();
					}
					
					for (Iterator it = ((JSONObject)rhs).keys(); it.hasNext(); ) {
						String key = (String) it.next();
						Object val = ((JSONObject)rhs).get(key);
						
						Object tKey = getDeserializedValue(key, keyType);
						Object tVal = deserializeRightHandSize(val);
						
						map.put(tKey, tVal);
					}
					
					silentInvoke(obj, setter, map);
				} else {
					silentInvoke(obj, setter, getDeserializedValue(rhs, valueType));
				}
				
			}
			
			return obj;
		} catch (InstantiationException e) {
			LOGGER.error("restoreObject", e);
		} catch (IllegalAccessException e) {
			LOGGER.error("restoreObject", e);
		}
		
		return null;
	}

	
	/**
	 * @param obj
	 * @param setter
	 * @param arg
	 * @throws IllegalAccessException
	 */
	private void silentInvoke(Object obj, Method setter, Object arg)
			throws IllegalAccessException {
		try {
			setter.invoke(obj, arg);
		} catch (IllegalArgumentException e) {
			LOGGER.error("silentIvoke crashed. Target: " + obj.getClass() + " / Setter: "+setter.getName() + " / Arg: " + arg + " / Arg Class: " + arg.getClass(), e);
		} catch (InvocationTargetException e) {
			LOGGER.error("silentIvoke crashed. Target: " + obj.getClass() + " / Setter: "+setter.getName() + " / Arg: " + arg + " / Arg Class: " + arg.getClass(), e);
		}
	}
	

	public Object getDeserializedValue(Object rhs, Class<?> valueType) {
		// Debug
		LOGGER.debug("Decode '" + rhs + "' of type "+ valueType.getName());
		
		if (java.sql.Date.class.isAssignableFrom(valueType)) {
			return new java.sql.Date(Long.parseLong(rhs.toString()));
		} else if (java.sql.Timestamp.class.isAssignableFrom(valueType)) {
			return new java.sql.Timestamp(Long.parseLong(rhs.toString()));
		} else if (java.util.Date.class.isAssignableFrom(valueType)) {
			// Date type
			java.util.Date d = null;
			try {
				d = DateUtils.parseIso8601DateTime(rhs.toString());
			} catch (ParseException e) {
			}
			
			if (d == null) {
				d = new Date(Long.parseLong(rhs.toString()));
			}
			
			if (d != null)
				return d;
		} else if (org.apache.commons.lang.enums.Enum.class.isAssignableFrom(valueType)) {
			try {
				Field decl = valueType.getField(rhs.toString());
				Object fld = decl.get(valueType);
				return fld;
			} catch (IllegalAccessException e) {
				LOGGER.error("Failed to decode Apache enum " + valueType, e);
			} catch (SecurityException e) {
				LOGGER.error("Failed to decode Apache enum " + valueType, e);
			} catch (NoSuchFieldException e) {
				LOGGER.error("Failed to decode Apache enum " + valueType, e);
			}
		} else if (java.lang.Double.class.isAssignableFrom(valueType)) {
			return Double.parseDouble((String.valueOf(rhs)));
		} else if (java.lang.Float.class.isAssignableFrom(valueType)) {
			return Float.parseFloat(String.valueOf(rhs));
		} else if (boolean.class.isAssignableFrom(valueType) || java.lang.Boolean.class.isAssignableFrom(valueType)) {
			return Boolean.parseBoolean(String.valueOf(rhs));
		} else {
			LOGGER.debug("[getDeserializedValue] Type: " + valueType + " <- " + rhs);
			
			return rhs;
		}
		
		LOGGER.warn("Failed to decode value '" + rhs + "' with type " + valueType.getName());
		
		return null;
	}
}
