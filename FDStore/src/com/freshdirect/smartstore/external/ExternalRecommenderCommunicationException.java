package com.freshdirect.smartstore.external;

/**
 * Thrown if there is an error while communicating with the external
 * recommender.
 * 
 * @author csongor
 */
public class ExternalRecommenderCommunicationException extends Exception {
	private static final long serialVersionUID = 7204418730158757286L;

	/**
	 * @see Exception#Exception()
	 */
	public ExternalRecommenderCommunicationException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 * @see Exception#Exception(String, Throwable)
	 */
	public ExternalRecommenderCommunicationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @see Exception#Exception(String)
	 */
	public ExternalRecommenderCommunicationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 * @see Exception#Exception(Throwable)
	 */
	public ExternalRecommenderCommunicationException(Throwable cause) {
		super(cause);
	}
}
