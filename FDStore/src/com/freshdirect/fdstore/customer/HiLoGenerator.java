package com.freshdirect.fdstore.customer;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;

/**
 * Unique ID generator using a hi-lo pattern for performance.
 * 
 * The most significant digits of the ID are obtained from a database sequence,
 * while the least significant digits are assigned from an in-memory counter.
 * When the least significant part reaches its maximum, a new high portion is
 * obtained from the DB sequence.
 */
public class HiLoGenerator implements IDGenerator {

	/** How many digits to utilize for the low portion **/
	private final static int ID_LO_LENGTH = 4;
	
	private final static int ID_LO_MAX = (int) Math.pow(10, ID_LO_LENGTH);
	
	private final String schema;
	private final String sequence;

	private String hi;
	private int lo;

	public HiLoGenerator(String schema, String sequence) {
		this.schema = schema;
		this.sequence = sequence;
	}
	
	private void padZeros(int width, StringBuffer result, String keyPart) {
		for(int i=0; i< width-keyPart.length(); ++i) { 
		    result.append('0');
		}
		result.append(keyPart);
	}

	/* (non-Javadoc)
     * @see com.freshdirect.fdstore.customer.IDGenerator#getNextId()
     */
	public synchronized String getNextId() {
		try {
			if (hi == null || "".equals(hi) || lo >= ID_LO_MAX) {
				hi = FDCustomerManager.getNextId(schema, sequence);
				lo = 0;
			}

			StringBuffer result = new StringBuffer(hi);
			padZeros(ID_LO_LENGTH, result, String.valueOf(lo++));

			return result.toString();
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e);
		}
	}
}
