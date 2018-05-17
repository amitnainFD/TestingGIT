package com.freshdirect.smartstore.external;

/**
 * Thrown if the provider with the specified name does not exist (not
 * registered.)
 * 
 * @author csongor
 */
public class NoSuchExternalRecommenderException extends Exception {
	private static final long serialVersionUID = 3701131508293809086L;

	/**
	 * @see Exception#Exception()
	 */
	public NoSuchExternalRecommenderException() {
		super();
	}

	/**
	 * @param message
	 * @see Exception#Exception(String)
	 */
	public NoSuchExternalRecommenderException(String message) {
		super(message);
	}
}
