package com.freshdirect.fdstore.customer;

import com.freshdirect.fdstore.FDException;

/**
 * FDInvalidConfigurationException
 */
public class FDInvalidConfigurationException extends FDException {
	
	private static final long	serialVersionUID	= 7085168429331418714L;

	public static class Unavailable extends FDInvalidConfigurationException {
		
		private static final long	serialVersionUID	= -7775775973179867512L;

		public Unavailable(String s) { super(s); }
		public Unavailable(Exception ex, String s) { super(ex,s); }
	}
	
	public static class InvalidSalesUnit extends FDInvalidConfigurationException {
		
		private static final long	serialVersionUID	= 1197913630335991344L;

		public InvalidSalesUnit(String s) { super(s); }
		public InvalidSalesUnit(Exception ex, String s) { super(ex,s); }
	}
	
	public static class MissingVariation extends FDInvalidConfigurationException {
		
		private static final long	serialVersionUID	= 1500398290533956159L;

		public MissingVariation(String s) { super(s); }
		public MissingVariation(Exception ex, String s) { super(ex,s); }
	}
	
	public static class InvalidOption extends FDInvalidConfigurationException {
		
		private static final long	serialVersionUID	= 7359658604186623814L;

		public InvalidOption(String s) { super(s); }
		public InvalidOption(Exception ex, String s) { super(ex,s); }
	}
		

	/**
	 * 
	 */
	public FDInvalidConfigurationException() {
		super();
	}

	/**
	 * @param message
	 */
	public FDInvalidConfigurationException(String message) {
		super(message);
	}

	/**
	 * @param ex
	 */
	public FDInvalidConfigurationException(Exception ex) {
		super(ex);
	}

	/**
	 * @param ex
	 * @param message
	 */
	public FDInvalidConfigurationException(Exception ex, String message) {
		super(ex, message);
	}

}
