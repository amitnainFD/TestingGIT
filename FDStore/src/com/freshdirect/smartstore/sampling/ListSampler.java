package com.freshdirect.smartstore.sampling;

import java.util.Random;

/**
 * Select list elements randomly.
 * 
 * The probability of the object at index <i>i</i> being selected
 * is derived from its {@link #cumulativeWeight(int, int) CDF} (<i>Cumulative
 * Distribution Function</i>) value.
 * 
 * @author istvan
 *
 */
public abstract class ListSampler {
	
	// random stream to use
	private Random R;
	
	/**
	 * Constructor.
	 * @param R random stream to use
	 */
	protected ListSampler(Random R) {
		this.R = R;
	}
		
	/**
	 * Weight of the specific item.
	 * 
	 * This is different from <i>probability<i>. The probability is
	 * of the <i>i</i><sup>th</sup> item being selected is
	 * actually {@link #weight(int, int) weight}(<i>i</i>,<i>n</i>)/<i>n</i>.
	 * 
	 * This class should be overridden if there is a simpler way in obtaining
	 * this value than {@link #cumulativeWeight(int, int) CDF}(<i>i</i>,<i>n</i>) -
	 * {@link #cumulativeWeight(int, int) CDF}(<i>i-1</i>,<i>n</i>). 
	 * @param i list index
	 * @param n total elements in the list
	 * @return the weight of 
	 */
	public int weight(int i, int n) {
		return cumulativeWeight(i, n) - (i >= 0 ? cumulativeWeight(i-1, n) : 0) ;  
	}
	
	/**
	 * The Cumulative Weight at a specific index.
	 * 
	 * This method <i>drives</i> the sampling process.
	 * 
	 * @param i index (within 0 and <i>n-1</i>)
	 * @param n total items (list length)
	 * @return sum of all weights from index 0 upto and including <i>i</i>
	 */
	public abstract int cumulativeWeight(int i, int n);
	
	/**
	 * Change random stream.
	 * @param R new random stream
	 */
	public void changeRandom(Random R) {
		this.R = R;
	}
	
	/**
	 * Change weight callback.
	 * 
	 * In case the distribution is maintained explicitly,
	 * the context can send messages of weight changes at
	 * certain indexes.
	 * 
	 * @param i index of item
	 * @param nw new weight
	 */
	public void changeWeight(int i, double nw) {	
	}
	
	/**
	 * Choose a index randomly.
	 * 
	 * Picks an index in [0,<i>n-1</i>] according to <i>this</i>
	 * distribution.
	 * 
	 * @param n number of elements in the list
	 * @return a randomly chosen index
	 */
	public int next(int n) {
		// there is only one element
		if (n == 1) {
			return 0;
		}
		
		int cw = cumulativeWeight(n-1, n);
		
		// all elements have been zeroed out (e.g. negatives)
		// there is "no" information left to choose from
		// the candidates, but it is known that there are
		// n of them: pick one uniformly randomly
		if (cw == 0) {
			return R.nextInt(n);
		}
		
		int w = R.nextInt(cw) + 1;			
		
		int l = 0;
		int h = n;
		while(l < h) {
			int m = (l + h) >> 1;
			if (cumulativeWeight(m,n) < w) {
				l = m + 1;
			} else {
				h = m;
			}
		}
		return l;
	}

	public String toString() {
	    return getClass().getSimpleName() + "[method=" + getName() + "]";
	}
	
	public boolean isDeterministic() {
		return false;
	}
	
	
	/**
	 * Returns sampler name. This name is used as
	 * the value of 'sampling_strat' config key
	 * 
	 * @author segabor
	 */
	public abstract String getName();
	
	/**
	 * Implementation of the Uniform Distribution.
	 * 
	 * This is exactly the same as if the sampling was totally
	 * random (i.e. index ignoring).
	 * 
	 * @author istvan
	 *
	 */
	public static class Uniform extends ListSampler {
		
		public Uniform(Random R) {
			super(R);
		}
		
		public int cumulativeWeight(int i, int n) {
			return i+1;
		}

		public int weight(int i, int n) {
			return 1;
		}
		
		public String getName() { return "uniform"; }
		
		public String toString() {
		    return getName();
		}
		
	};
	
	
	/**
	 * Distribution where index weights decrease linearly.
	 * 
	 * @author istvan
	 *
	 */
	public static class Linear extends ListSampler {
		
		public Linear(Random R) {
			super(R);
		}
				
		/**
		 * cw(i) = n*(n+1)/2 - (n-i-1)*(n-i)/2
		 */
		public int cumulativeWeight(int i, int n) {
			return (i+1)*n  - ((i+1)*i)/2;
		}
		
		/**
		 * w(i) = n-1;
		 */
		public int weight(int i, int n) {
			return n-i;
		}
		
		public String getName() { return "linear"; }
		
		public String toString() {
		    return getName();
		}
	}
	
	/**
	 * Relative weights resemble a quadratic function.
	 * 
	 * @author istvan
	 *
	 */
	public static class Quadratic extends ListSampler {
		
		public Quadratic(Random R) {
			super(R);
		}
		
		/**
		 * cw(i) = n^3/3 + n^2/2 + n/6 - ( (n-i-1)^3/3 + (n-i-1)^2/2 + (n-i-1)/6 ).
		 * 
		 */
		public int cumulativeWeight(int i, int n) {
			return (
				(6*i + 6)*n*n - 
				6*i*(i + 1)*n + 
				2*i*i*i + 
				3*i*i +
				i
			)/6;
		}
		
		/**
		 * 
		 * w(i) = (n-1)^2
		 */
		public int weight(int i, int n) {
			return (n-i)*(n-i);
		}
		
		public String getName() { return "quadratic"; }
		
		public String toString() {
		    return getName();
		}
	}
	
	/**
	 * Relative weights resemble the cubic function.
	 * 
	 * @author istvan
	 *
	 */
	public static class Cubic extends ListSampler {
		public Cubic(Random R) {
			super(R);
		}
				
		/**
		 * 
		 * cw(i) = n^4/4 + n^3/2 + n^2/4 - ( (n-i-1)^4/4 + (n-i-1)^3/2 + (n-i-1)^2/4 ).
		 */
		public int cumulativeWeight(int i, int n) {
			return (
				4*(i+1)*n*n*n - 
				6*i*(i+1)*n*n + 
				2*(2*i*i*i + 3*i*i + i)*n - 
				i*i*i*i - 
				2*i*i*i - i*i
			)/4;
		}
		
		/**
		 * w(i) = (n-1)^3
		 */
		public int weight(int i,int n) {
			int a = (n-i);
			return a*a*a;
		}
		
		public String getName() { return "cubic"; }
		
		public String toString() {
		    return getName();
		}

	}
	
	/**
	 * The Cumulative Weights resemble the Harmonic Numbers.
	 * 
	 * @author istvan
	 *
	 */
	public static class Harmonic extends ListSampler {
		
		private static double EULER_MASCHERONI = 0.57721;
		private double scale;
			
		private double harmonicSum(int k) {
			return Math.log(k) + 1 + EULER_MASCHERONI;
		}
		
		private double norm(int n) {
			return n <= 1 ? 1 : Math.log(n) - Math.log(n-1);
		}
		
		/**
		 * Constructor.
		 * 
		 * @param R
		 * @param scale resolution scaler
		 */
		public Harmonic(Random R, double scale) {
			super(R);
			this.scale = scale;
		}
		
		public Harmonic(Random R) {
			this(R,100.0);
		}
		
		/**
		 * cw = scale * ( log(i+1) + 1 + (EULER MASCHERONI constant))
		 */
		public int cumulativeWeight(int i, int n) {
			return (int)(scale*(harmonicSum(i+1))/norm(n));
		}
		
		public String getName() { return "harmonic"; }
		
		public String toString() {
		    return getName() + "("+scale+')';
		}
	}
	
	/**
	 * The Cumulative Weights resemble the square root function.
	 * 
	 * @author istvan
	 *
	 */
	public static class SquareRootCDF extends ListSampler {
		
		private double scale;
		
		public SquareRootCDF(Random R, double scale) {
			super(R);
			this.scale = scale;
		}
		
		public SquareRootCDF(Random R) {
			this(R,100.0);
		}
		
		private double norm(int n) {
			return Math.sqrt(n) - Math.sqrt(n-1);
		}

		/**
		 * 
		 * cw(i) = scale * sqrt(i+1)
		 */
		public int cumulativeWeight(int i, int n) {
			return (int)(scale*(Math.sqrt(i+1)/norm(n)));
		}

		public String getName() { return "sqrt"; }
		
        public String toString() {
            return getName() + "("+scale+')';
        }
		
	}
	
	/**
	 * Cumulative Weights defined as the power function.
	 * 
	 * @author istvan
	 *
	 */
	public static class PowerCDF extends ListSampler {
		private static int N = 30;
		private double p;
		private double scale;
		
		private double[] powers = new double[N];
		
		/**
		 * Constructor.
		 * For this to make sense, p should be in [0,1],
		 * where 1 is totally random, 0 only chooses the first and any
		 * number in between favors the smaller indexes according to
		 * p.
		 *  
		 * @param R
		 * @param p 
		 * @param scale
		 */
		public PowerCDF(Random R, double p, double scale) {
			super(R);
			this.p = p;
			this.scale = scale;
			for(int i=0; i< powers.length;++i) {
				powers[i] = Math.pow(i,p);
			}
		}
		
		public PowerCDF(Random R, double p) {
			this(R,p,100.0);
		}
		
		private double power(int x) {
			if (x >= powers.length) {
				double[] newPowers = new double[5*x/4 + 1];
				System.arraycopy(powers, 0, newPowers, 0, powers.length);
				for(int i=powers.length; i< newPowers.length; ++i) {
					newPowers[i] = Math.pow(i,p);
				}
				powers = newPowers;
			}
			return powers[x];
		}
		
		private double norm(int n) {
			return power(n) - power(n-1);
		}
		
		/**
		 * cw(i) = scale * (i+1)^p
		 */
		public int cumulativeWeight(int i, int n) {
			return (int)(scale*power(i+1)/norm(n));
		}
		
		public String getName() { return "power"; }
		
		public String toString() {
		    return getClass().getSimpleName() + "[method=" + getName() + ",exponent=" + p + "]";
		}
	}
	
	/**
	 * Always returns 0 for {@link #next(int)}.
	 * 
	 * This sampler can be used in "without replacement" sampling
	 * to take the first items off the list in order.
	 * 
	 * @author istvan
	 *
	 */
	public static ListSampler ZERO = new ListSampler(null) {
			
		public int cumulativeWeight(int i, int n) {
			return i == 0 ? 1 : 0;
		}
		
		public int weight(int i, int n) {
			return i == 0 ? 1 : 0;
		}
		
		public int next(int n) {
			return 0;
		}
		
		public String getName() { return "deterministic"; }

		public String toString() {
		    return getName();
		}

		public boolean isDeterministic() {
			return true;
		}
	};
}
