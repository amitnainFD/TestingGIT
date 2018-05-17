package com.freshdirect.smartstore.fdstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class contains the implementation of the cohort selection algorithm based on cohort weight, and user ID randomization. The main user of this class is the VariantSelector,
 * which decides which recommendation service associated with which cohort in the different site features.
 * 
 * @see VariantSelectorFactory
 * @see VariantSelector
 * @author zsombor
 *
 */
public class CohortSelector {
	
	private static Map<String, Integer> cohorts = null;
	private static int cohortSum = 0;
	private static List<String> cohortNames = null;

	private static CohortSelector instance;

	/**
	 * Compare variants by mass. Used explicitly to (binary) search the cohort
	 * the user belongs to.
	 */
	protected static Comparator<Object> MASS_COMPARATOR = new Comparator<Object>() {
		// get mass as integer
		private int getMass(Object o) {
			if (o instanceof Integer) {
				return ((Integer) o).intValue();
			}
			
			return ((CohortAssignment) o).getMass();
		}

		public int compare(Object o1, Object o2) {
			return getMass(o1) - getMass(o2);
		}
	};

    protected static byte[][] RANDOM_DIGIT_MAP = new byte[10][10];
    
    static {
    	Object generator = new Object() {
    		private long state = 439094301L;
    		private int bits = 8;
    		
    		public int hashCode() {
    			state = (state * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        		return (int)(state >>> (48-bits));	
    		}
    	};
    	
    	for(int i = 0; i< RANDOM_DIGIT_MAP.length; ++i) {
    		for(int j=0; j<RANDOM_DIGIT_MAP[i].length; ++j) {
    			RANDOM_DIGIT_MAP[i][j] = (byte)j;
    		}
    		for(int j=2; j<RANDOM_DIGIT_MAP[i].length; ++j) {
    			int o = Math.abs(generator.hashCode()) % j;
    			
    			byte tmp = RANDOM_DIGIT_MAP[i][o];
    			RANDOM_DIGIT_MAP[i][o] = RANDOM_DIGIT_MAP[i][j];
    			RANDOM_DIGIT_MAP[i][j] = tmp;
    		}	
    	}
    }

    /**
	 * Stores a cohort, the corresponding variant, frequency and mass.
	 * 
	 */
    protected static class CohortAssignment implements Comparable<CohortAssignment> {
        private String cohort;   // id
        private int    frequency; // cohort freq
        private int    mass;     // the cumulative mass

        // "to the left"

        /**
		 * Constructor.
		 * 
		 * @param cohort
		 *            id
		 * @param variant
		 *            service
		 * @param frequency
		 *            cohort's
		 */
        protected CohortAssignment(String cohort, int frequency) {
            this.cohort = cohort;
            this.frequency = frequency;
            this.mass = 0;
        }
        
        @Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((cohort == null) ? 0 : cohort.hashCode());
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
			final CohortAssignment other = (CohortAssignment) obj;
			if (cohort == null) {
				if (other.cohort != null)
					return false;
			} else if (!cohort.equals(other.cohort))
				return false;
			return true;
		}
		
		@Override
		public int compareTo(CohortAssignment o) {
			return cohort.compareTo(o.cohort);
		}

		/**
         * Get cohort.
         * 
         * @return cohort id
         */
        public String getCohort() {
            return cohort;
        }

        /**
         * Get mass. Return the cumulative mass of cohorts "to the left"; that
         * is all cohorts that precede this, plus this frequency.
         * 
         * @return mass "to the left"
         */
        public int getMass() {
            return mass;
        }

        public String toString() {
            return new StringBuffer().append("[mass = ").append(mass)
            		.append(" freq = ").append(frequency).append(" cohort = ")
            		.append(cohort).append("]").toString();
        }
    }

    private static int calculateSum() {
        int sum = 0;
        for (Iterator<Integer> iter = cohorts.values().iterator(); iter.hasNext();) {
            Integer n = iter.next();
            sum += n.intValue();
        }
        return sum;
    }

    /**
     * Returns the cohort->weight map
     * 
     * @return Map<Cohort,weight>
     */
    public static synchronized Map<String, Integer> getCohorts() {
        if (cohorts == null) {
            // cache cohort map
            cohorts = VariantSelection.getInstance().getCohorts();
            cohortSum = calculateSum();
        }
        return cohorts;
    }

    /**
     * Return an <b>ordered</b> list of cohort names.
     * 
     * @return
     */
    public synchronized static List<String> getCohortNames() {
        if (cohortNames == null) {
            // cache cohort map
            cohortNames = VariantSelection.getInstance().getCohortNames();
        }
        return cohortNames;
    }
    
    /**
     * !!! THIS METHOD IS ONLY FOR TESTING PURPOSES - DO NOT USE IT OTHERWISE !!!
     * 
     * @param cohortNames
     */
    public synchronized static void setCohortNames(List<String> cohortNames) {
        CohortSelector.cohortNames = cohortNames;
    }

    /**
     * !!! THIS METHOD IS ONLY FOR TESTING PURPOSES - DO NOT USE IT OTHERWISE !!!
     * 
     * @param cohorts
     */
    public static void setCohorts(Map<String, Integer> cohorts) {
        CohortSelector.cohorts = cohorts;
        cohortSum = calculateSum();
    }

    public synchronized static CohortSelector getInstance() {
        if (instance == null) {
            Map<String,Integer> ch = getCohorts();
            List<String> cohortNames = CohortSelector.getCohortNames();

            CohortSelector cs = new CohortSelector();
            
            for (Iterator<String> iter = cohortNames.iterator(); iter.hasNext();) {
                String name = iter.next();
                int freq = ((Number) ch.get(name)).intValue();
                cs.addCohort(name, freq);
            }
            instance = cs;
        }
        return instance;
    }

    /**
	 * The Cumulative Distribution of cohorts. The actual sorted list of
	 * variants with cumulative masses in cohort order.
	 */
	protected List<CohortAssignment> cdf = new ArrayList<CohortAssignment>();

    /**
	 * Add a cohort group definition.
	 * 
	 * @param cohort
	 *            id
	 * @param frequency
	 *            relative frequency of cohort
	 */
    protected void addCohort(String cohort, int frequency) {
        CohortAssignment vf = new CohortAssignment(cohort, frequency);
        int p = Collections.binarySearch(cdf, vf);
        if (p < 0) {
        	// new
            p = -p - 1;
            // shift
            cdf.add(p, vf);
            if (p > 0)
                vf.mass = cdf.get(p - 1).getMass();
        } else {
        	// exists
            // adjust
            cdf.get(p).frequency += frequency;
        }

        for (int i = p; i < cdf.size(); ++i) {
            cdf.get(i).mass += frequency;
        }
    }

    /**
     * Select a cohort based on user id.
     * 
     * @param erpUserId
     *            user id
     * @return variant
     */
    protected CohortAssignment getCohortAssignment(String erpUserId) {
        Integer v = new Integer(getCohortIndex(erpUserId));
        int p = Collections.binarySearch(cdf, v, MASS_COMPARATOR);
        if (p < 0) {
            p = -p - 1;
        } else {
            ++p;
        }
        return cdf.get(p);
    }

    public String getCohortName(String erpCustomerId) {
        CohortAssignment cohortAssignment = getCohortAssignment(erpCustomerId);
        if (cohortAssignment != null) {
            return cohortAssignment.getCohort();
       
        }
        return null;
    }
    
    /**
     * Randomize user id.
     * 
     * The algorithm requires randomly distributed keys. Since raw user ids
     * aren't sufficiently random, this function maps them to a more random set.
     * The requirements are the following.
     * <ul>
     * <li>if u1 == u2, then randomize(u1) == randomize(u2)</li>
     * <li>the distribution of (randomize(u) % m) is approximately U(0, 1, ...,
     * m-1) for small m</li>
     * </ul>
     * 
     * @param erpUserId
     *            user id
     * @return randomized user id
     */
    protected int randomize(String erpUserId) {
    	int z = 0;
    	int r = 0;
    	if (erpUserId.length() > 0) {
    		r = Math.abs(erpUserId.charAt(erpUserId.length()-1)) % RANDOM_DIGIT_MAP.length; 
    	}
    	
    	for(int i=0; i < erpUserId.length(); ++i) {
    		int c = erpUserId.charAt(i);
    		int x = (byte)c;
    		if (c >= '0' && c <= '9') {
    			x = RANDOM_DIGIT_MAP[r][c - '0'] + '0';
    		} 
    		z = 31*z + x;
    	}
    	return Math.abs(z);
    }
	
    /**
     * 
     * @param erpUserId
     * @return
     */
    protected int getCohortIndex(String erpUserId) {
        return erpUserId != null ? randomize(erpUserId) % cohortSum : 0;
    }
}
