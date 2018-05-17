package com.freshdirect.smartstore.fdstore;

import java.util.Collections;
import java.util.Set;

/**
 * 
 * Convert a factor range.
 * 
 * Instances of this abstract class implement normalization or discretization of database raw score values.
 * They can also implement <i>virtual</i> factors composed from other factors.
 * 
 * These instances either use the global or the personalized scores tables. They have to declare what
 * columns they need from these ({@link #requiresGlobalDatabaseColumns()} and {@link #requiresPersonalizedDatabaseColumns()})
 * and must implement the {@link #map(String, ScoreRangeProvider) map(<i>factor</i>,ScoreRangeProvider)} method which
 * returns the calculated values for the range.
 * 
 * If the factor is personalized, then the {@link #isPersonalized()} method <i>must</i> return true.
 * @author istvan
 *
 */
public abstract class FactorRangeConverter {
	
	// duplicate range
	protected static double[] dup(double[] range) {
		double[] copy = new double[range.length];
		System.arraycopy(range, 0, copy, 0, range.length);
		return copy;
	}
	
	// sum range values
	protected static double sum(double[] range) {
		double s = 0;
		for(int i=0; i< range.length; ++i) {
			s += range[i];
		}
		return s;
	}
	
	// max value
	protected static double max(double[] range) {
		double m = Double.MIN_VALUE;
		for(int i=0; i< range.length; ++i) {
			if (range[i] > m) {
				m = range[i];
			}
		}
		return m;
	}
	
	// min value
	protected static double min(double[] range) {
		double m = Double.MAX_VALUE;
		for(int i=0; i< range.length; ++i) {
			if (range[i] < m) {
				m = range[i];
			}
		}
		return m;
	}
	
	// average value
	@SuppressWarnings( "cast" )
	protected static double average(double[] range) {
		return sum(range)/(double)range.length;
	}
	
	// v*v
	protected static double sqr(double v) {
		return v*v;
	}
	
	// variance of range
	@SuppressWarnings( "cast" )
	protected static double variance(double[] range) {
		if (range.length == 0) return Double.NaN;
		double a = average(range);
		double v = 0;
		for(int i=0; i< range.length; ++i) {
			v += sqr(range[i] - a); 
		}
		return v/(double)range.length;
	}
	
	// standard deviation of range
	protected static double std(double[] range) {
		if (range.length == 0) return Double.NaN;
		return Math.sqrt(variance(range));
	}
	
	// divide if not 0
	protected static void divide(double[] range, double v) {
		if (v != 0) {
			for(int i=0; i< range.length; ++i) {
				range[i] /= v;
			}
		}
	}
	
	/**
	 * 
	 * Map numbers greater than 1 to [1,a] and numbers less then 1 to [1/a,a]
	 * @param range
	 * @param a asymptote
	 */
	protected static void positiveBiasedSigmoidNormalize(double[] range, double a) {
		double b = 1./(a - 1.);
		
		for(int i=0; i< range.length; ++i) {
			if (range[i] < 1) {
				range[i] = (b + Math.exp(-1./range[i] + 1.))/(b+1);
			} else {
				range[i] = (1. + b)/(b + Math.exp(-range[i] +1));
			}
		}
	}
	
	/**
	 * Get the columns that need to be loaded from the personalized scores table.
	 * 
	 * Instances that need such columns must override this method.
	 * 
	 * @return empty set
	 */
	public Set<String> requiresPersonalizedDatabaseColumns() {
		return Collections.emptySet();
	}
	
	/**
	 * Get the columns that need to be loaded from the global scores table.
	 * 
	 * Instances that need such columns must override this method.
	 * 
	 * @return empty set
	 */
	public Set<String> requiresGlobalDatabaseColumns() {
		return Collections.emptySet();
	}
	
	/**
	 * Is range calculated is personalized (user bound).
	 * 
	 * Instances that implement personalizes scores <i>must</i> override this.
	 * 
	 * @return false
	 */
	public boolean isPersonalized() {
		return false;
	}
	
	/**
	 * Calculate (map) the database stored factors into a new factor.
	 * 
	 * The range of values return should correspond to {@link ScoreRangeProvider#products(String) ScoreRangeProvider.products(userId)} of that 
	 * factor.
	 * 
	 * @param provider
	 * @param userId customer id (null should be ok, meaning it is not in context)
	 * @return new scores
	 * @throws Exception
	 */
	public abstract double[] map(String userId, ScoreRangeProvider provider) throws Exception;
	
	
	/**
	 * Convenience converter which returns the raw values from the personalized database table.
	 * 
	 * @param factor name
	 * @return values verbatim from personalized table
	 */
	public static FactorRangeConverter getRawPersonalizedScores(final String factor) {
		
		return new FactorRangeConverter() {

			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				return provider.getRange(userId, factor);
			}

			public Set<String> requiresPersonalizedDatabaseColumns() {
				return Collections.singleton(factor);
			}			
			
			public boolean isPersonalized() {
				return true;
			}
		};
	}

	/**
	 Convenience converter which returns the raw values from the global database table.
	 * 
	 * @param factor name
	 * @return values verbatim from global table
	 */
	public static FactorRangeConverter getRawGlobalScores(final String factor) {
		return new FactorRangeConverter() {

			public double[] map(String userId, ScoreRangeProvider provider) throws Exception {
				return provider.getRange(userId, factor);
			}

			public Set<String> requiresGlobalDatabaseColumns() {
				return Collections.singleton(factor);
			}
			
			public boolean isPersonalized() {
				return false;
			}
			
		};
		
	}

}
