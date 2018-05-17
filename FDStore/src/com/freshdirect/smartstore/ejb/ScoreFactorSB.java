package com.freshdirect.smartstore.ejb;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBObject;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.content.EnumWinePrice;


/**
 * 
 * @author istvan
 *
 */
public interface ScoreFactorSB extends EJBObject {
	
	/**
	 * Get the required personalized factors for user.
	 * 
	 * The returned object is a map indexed by product ids and the values
	 * are arrays of doubles containing the factor scores exactly in the order listed
	 * in the <tt>factors</tt> parameter. Null values in the database translate to 
	 * zeros as scores. Integers are converted into doubles.
	 * 
	 * @param eStoreId {@link EnumEStoreId}
	 * @param erpCustomerId 
	 * @param factors List<FactorName:{@link String}>
	 * @throws RemoteException
	 * @return Map<ProductId:{@link String},double[]>
	 */
	public Map<String,double[]> getPersonalizedFactors(final String eStoreId, String erpCustomerId, List<String> factors) throws RemoteException;
	
	/**
	 * Get the required global factors.
	 * 
	 * The returned object is has the same semantics as that of
	 * {@link #getPersonalizedFactors(String, List)}.
	 * 
	 * @param eStoreId {@link EnumEStoreId}
	 * @param factors List<FactorName:{@link String}>
	 * @throws RemoteException
	 * @return Map<ProductId:{@link String},double[]>
	 */	
	public Map<String,double[]> getGlobalFactors(final String eStoreId, List<String> factors) throws RemoteException;
	
	/**
	 * Get personalized factor names.
	 * 
	 * @return Set<{@link String}>
	 * @throws RemoteException
	 */
	public Set<String> getPersonalizedFactorNames() throws RemoteException;

	/**
	 * Get the global factor names.
	 * 
	 * @return Set<{@link String>
	 * @throws RemoteException
	 */
	public Set<String> getGlobalFactorNames() throws RemoteException;
	
	/**
	 * Get the product ids that have any scores.
	 * 
	 * @param eStoreId {@link EnumEStoreId}
	 * @return Set<ProductId:String>
	 * @throws RemoteException
	 */
	public Set<String> getGlobalProducts(final String eStoreId) throws RemoteException;
	
	/**
	 * Get the product ids for the user that have any scores.
	 * 
	 * @param eStoreId {@link EnumEStoreId}
	 * @param erpCustomerId 
	 * @return Set<ProductId:String>
	 * @throws RemoteException
	 */
	public Set<String> getPersonalizedProducts(final String eStoreId, String erpCustomerId) throws RemoteException;
	

	/**
	 * Return a list of product recommendation for a given product by a recommender vendor.
	 * 
	 * @deprecated
	 * 
	 * @param recommender
	 * @param key
	 * @return List<ContentKey>
	 * @throws RemoteException
	 */
	@Deprecated
	public List<ContentKey> getProductRecommendations(String recommender, ContentKey key) throws RemoteException;
	
	/**
	 * Return a list of personal recommendation (ContentKey-s) for a user by a recommender vendor.
	 *  
	 * @deprecated
	 * 
	 * @param recommender
	 * @param erpCustomerId
	 * @return List<ContentKey> 
	 * @throws RemoteException
	 */
	@Deprecated
	public List<ContentKey> getPersonalRecommendations(String recommender, String erpCustomerId) throws RemoteException;

	@Deprecated
	public EnumWinePrice getPreferredWinePrice(String customerId) throws RemoteException;
	
}
