package com.freshdirect.smartstore.external;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The external recommender registry. Any user of a {@link ExternalRecommender}
 * implementation should be responsible to register in this registry.
 * 
 * @author csongor
 * 
 */
public class ExternalRecommenderRegistry {
	private static class RegistryKey {
		String providerName;
		ExternalRecommenderType recommenderType;

		public RegistryKey(String providerName, ExternalRecommenderType recommenderType) {
			super();
			this.providerName = providerName;
			this.recommenderType = recommenderType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((providerName == null) ? 0 : providerName.hashCode());
			result = prime * result + ((recommenderType == null) ? 0 : recommenderType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final RegistryKey other = (RegistryKey) obj;
			if (providerName == null) {
				if (other.providerName != null)
					return false;
			} else if (!providerName.equals(other.providerName))
				return false;
			if (recommenderType == null) {
				if (other.recommenderType != null)
					return false;
			} else if (!recommenderType.equals(other.recommenderType))
				return false;
			return true;
		}
	}

	/**
	 * recommender registry
	 */
	private static final Map<RegistryKey, ExternalRecommender> recommenders = new HashMap<RegistryKey, ExternalRecommender>();

	/**
	 * default cache capacity (1000)
	 */
	private static final int DEFAULT_CACHE_CAPACITY = 1000;

	/**
	 * default cache timeout (10 minutes in milliseconds)
	 */
	private static final long DEFAULT_CACHE_TIMEOUT = 10 * 60 * 1000;

	/**
	 * Returns a new recommender instance.
	 * 
	 * See {@link ExternalRecommender} for further details about external
	 * recommenders.
	 * 
	 * @see ExternalRecommender
	 * 
	 * @param providerName
	 *            name of the provider
	 * @param recommenderType
	 *            type of the recommender
	 * @return the requested recommender
	 * @throws IllegalArgumentException
	 *             if <code>recommenderName</code> is null or empty
	 * @throws NoSuchExternalRecommenderException
	 *             if the recommender is not known
	 */
	public synchronized static ExternalRecommender getInstance(String providerName, ExternalRecommenderType recommenderType) throws IllegalArgumentException, NoSuchExternalRecommenderException {
		if (providerName == null || providerName.length() == 0)
			throw new IllegalArgumentException("providerName cannot be null");
		if (recommenderType == null)
			throw new IllegalArgumentException("recommenderType cannot be null");
		
		ExternalRecommender recommender = recommenders.get(new RegistryKey(providerName, recommenderType));
		if (recommender == null)
			throw new NoSuchExternalRecommenderException();
		
		return recommender;
	}

	/**
	 * Registers the specified recommender by overwriting the existing
	 * registration.
	 * 
	 * @param providerName
	 *            the desired name of the recommender
	 * @param recommenderType
	 *            the type of the recommender
	 * @param recommender
	 *            recommender to be registered
	 * @return false if the new registration overwrote a previous registration
	 * @throws IllegalArgumentException
	 *             if the <code>recommender</code> is null
	 */
	public synchronized static boolean registerRecommender(String providerName, ExternalRecommenderType recommenderType, ExternalRecommender recommender) throws IllegalArgumentException {
		return registerRecommender(providerName, recommenderType, recommender, DEFAULT_CACHE_CAPACITY, DEFAULT_CACHE_TIMEOUT);
	}

	/**
	 * Registers the specified recommender by overwriting the existing registration.
	 * 
	 * @param providerName
	 *            the desired name of the recommender
	 * @param recommenderType
	 *            the type of the recommender
	 * @param recommender
	 *            recommender to be registered
	 * @param capacity
	 *            cache capacity
	 * @param timeout
	 *            cache expiration in milliseconds
	 * @return false if the new registration overwrote a previous registration
	 * @throws IllegalArgumentException
	 *             if the <code>recommender</code> is null
	 */
	public synchronized static boolean registerRecommender(String providerName, ExternalRecommenderType recommenderType, ExternalRecommender recommender, int capacity, long timeout) throws IllegalArgumentException {
		if (providerName == null || providerName.length() == 0)
			throw new IllegalArgumentException("providerName cannot be null");
		if (recommenderType == null)
			throw new IllegalArgumentException("recommenderType cannot be null");
		if (recommender == null)
			throw new IllegalArgumentException("recommender cannot be null");
		RegistryKey registryKey = new RegistryKey(providerName, recommenderType);
		if (recommenderType == ExternalRecommenderType.PERSONALIZED)
			recommender = new CachingExternalRecommender(recommender, capacity, timeout);
		Object previous = recommenders.put(registryKey, recommender);
		return previous == null;
	}

	/**
	 * Unregisters an existing recommender registration.
	 * 
	 * @param providerName
	 *            the name of the recommender provider to unregister
	 * @param recommenderType
	 *            the type of the recommender
	 * @return if the operation succeeds (there were an existing registration
	 *         under the give name)
	 * @throws IllegalArgumentException
	 *             if <code>recommenderName</code> is null or empty string
	 */
	public synchronized static boolean unregisterRecommender(String providerName, ExternalRecommenderType recommenderType) throws IllegalArgumentException {
		if (providerName == null || providerName.length() == 0)
			throw new IllegalArgumentException("recommenderName cannot be null");
		if (recommenderType == null)
			throw new IllegalArgumentException("recommenderType cannot be null");
		ExternalRecommender existing = recommenders.remove(new RegistryKey(providerName, recommenderType));
		return existing != null;
	}

	/**
	 * Retrieve the provider names of registered recommenders based on the
	 * recommender type
	 * 
	 * @param recommenderType
	 *            the type is used to filter
	 * @return the filtered results of provider names
	 */
	public synchronized static Set<String> getRegisteredRecommenders(ExternalRecommenderType recommenderType) {
		Set<String> names = new HashSet<String>();
		for (RegistryKey key : recommenders.keySet())
			if (key.recommenderType == recommenderType)
				names.add(key.providerName);
		return names;
	}
}
