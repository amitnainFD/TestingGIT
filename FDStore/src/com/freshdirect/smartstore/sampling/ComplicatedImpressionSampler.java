/**
 * 
 */
package com.freshdirect.smartstore.sampling;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ComplicatedImpressionSampler extends AbstractImpressionSampler {
	/**
	 * Sampler that interprets the content score ratios as actual probabilities.
	 * 
	 * This is an expensive sampler, since it needs to repeatedly recalculate
	 * the distribution after an item has been drawn.
	 * 
	 * @author istvan
	 */
	private static class ExplicitSampler extends ListSampler {
		private int[] weights; // individual weights
		private int[] cumWeights; // sum of weights
		private boolean recalculate = false; // lazy stuff

		/**
		 * Constructor.
		 * 
		 * @param R
		 * @param items
		 *            List<{@link RankedContent}>
		 */
		private ExplicitSampler(Random R, List<? extends RankedContent> items) {
			super(R);
			weights = new int[items.size()];
			cumWeights = new int[items.size()];
			int i = 0;
			for (Iterator<? extends RankedContent> it = items.iterator(); it.hasNext();) {
				RankedContent item = it.next();
				int w = weight(item.getScore());
				if (w < 0) {
					w = 0;
				}
				weights[i++] = w;
			}
			calculateCumulativeWeights(items.size());
		}

		private int weight(double w) {
			return (int) (100 * w);
		}

		private void calculateCumulativeWeights(int n) {
			for (int i = 0; i < n; ++i) {
				int j = i;
				while (weights[j] == 0) { // remove zeros
					if (j >= weights.length - 1)
						break;
					++j;
				}
				if (j > i) {
					for (int k = i; k < weights.length - (j - i); ++k) {
						weights[k] = weights[k + j - i];
					}
				}
				cumWeights[i] = weights[i] + (i > 0 ? cumWeights[i - 1] : 0);
			}
			recalculate = false;
		}

		public int cumulativeWeight(int i, int n) {
			return cumWeights[i];
		}

		public int weight(int i, int n) {
			return weights[i];
		}

		public int next(int n) {
			if (recalculate)
				calculateCumulativeWeights(n);
			return super.next(n);
		}

		public void changeWeight(int i, double nw) {
			int w = weight(nw);
			if (w < 0) {
				w = 0;
			}
			if (w != weights[i]) {
				weights[i] = w;
				recalculate = true;
			}
		}

		public String getName() {
			return "explicit";
		}
		
		public static String toString2() {
			return "ExplicitSampler[method=explicit]";
		}
	}

	private Random random;

	public ComplicatedImpressionSampler(ConsiderationLimit considerationLimit,
			boolean categoryAggregationEnabled, boolean useAlternatives) {
		super(considerationLimit, categoryAggregationEnabled, useAlternatives);
		random = new Random();
	}

	@Override
	protected ListSampler createSampler(List<RankedContent> limitedRankedContent) {
		return new ExplicitSampler(random, limitedRankedContent);
	}

	@Override
	public boolean isDeterministic() {
		return false;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[listSampler=" + ExplicitSampler.toString2()
				+ ", considerationLimit=" + getConsiderationLimit()
				+ ", categoryAggregationEnabled=" + isCategoryAggregationEnabled()
				+ ", useAlternatives=" + isUseAlternatives() + "]";
	}
}