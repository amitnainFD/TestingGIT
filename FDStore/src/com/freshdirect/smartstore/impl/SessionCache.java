package com.freshdirect.smartstore.impl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Session cache.
 * 
 * This implementation is an access-ordered LRU cache,
 * which purges the least recently accessed item when
 * the cache size reaches the given capacity.
 * 
 * @author istvan
 *
 */
public class SessionCache<K,V> extends LinkedHashMap<K,V> {

	private static final long serialVersionUID = 6732419652977205549L;
	
	/**
	 * Actual capacity limit.
	 */
	protected int capacity;
	
	/**
	 * Constructor.
	 * 
	 * Creates a cache for 100 entries.
	 */
	public SessionCache() {
		this(100, 0.75f);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param capacity cache capacity
	 * @param loadFactor load factor
	 */
	public SessionCache(int capacity, float loadFactor) {
		super(3*capacity/2+1,loadFactor,true);
		this.capacity = capacity;
	}
	
	/**
	 * Decide whether to remove entry.
	 * @param e ignored (the entry that gets potentially removed)
	 * @return whether the number of entries has reached the capacity
	 */
	protected boolean removeEldestEntry(Map.Entry<K,V> e) {
		return size() > capacity;
	}
	
	
	/**
	 * Retrieve object.
	 *  
	 * @param key 
	 * @return object stored under key or null
	 */
	public synchronized V get(Object key) { 
		return super.get(key);
	}
	
	/**
	 * Cache object.
	 * @param key 
	 * @param value
	 */
	public synchronized V put(K key, V value) {
		return super.put(key, value);
	}
	
	/**
	 * An entry that has an associated expiry.
	 * 
	 */
	public static class TimedEntry<V> {
		
		private long duration;
		private long timeRecorded;
		
		private V payLoad;
		
		/**
		 * Constructor.
		 * 
		 * Time is recorded at this moment (the clock is ticking :)
		 * @param payLoad object stored
		 * @param duration expiry in milliseconds
		 */
		public TimedEntry(V payLoad, long duration) {
			timeRecorded = System.currentTimeMillis();
			this.duration = duration;
			this.payLoad = payLoad;
		}
		
		/**
		 * Is payload's recency expired?
		 * @return whether object expired
		 */
		public boolean expired() {
			long diff = System.currentTimeMillis() - timeRecorded;
			return  diff > duration;
		}
		
		/**
		 * Get payload.
		 * @return payload
		 */
		public V getPayload() {
			return payLoad;
		}
		
		/** Set a new payload.
		 * 
		 * @param payload payload object
		 * @param resetClock whether to reset the clock
		 */
		public void setPayload(V payload, boolean resetClock) {
			if (resetClock) {
			    timeRecorded = System.currentTimeMillis();
			}
			this.payLoad = payload;
		}
	}
}
