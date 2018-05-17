/**
 * 
 */
package com.freshdirect.smartstore.sampling;

import java.util.List;

/**
 * Defines the highest limit of either topN or topP where
 * <ul>
 * <li><b>topN</b> is the maximum number of items to recommend</li>
 * <li><b>topP</b> is the maximum percentage of items in the input ranked
 * content</li>
 * </ul>
 * <b>SimpleLimit</b> returns the maximum of either bounds.
 * 
 * @author unknown
 */
public final class SimpleLimit implements ConsiderationLimit {
	private final double topP;
	private final int topN;

	public SimpleLimit(double topP, int topN) {
		this.topP = topP;
		this.topN = topN;
	}

	public int max(List<? extends RankedContent> rankedItems) {
		return Math.max((int) ((topP * rankedItems.size()) / 100.0), topN);
	}

	public String toString() {
		return this.getClass().getSimpleName() + "[topP=" + topP + ",topN=" + topN + "]";
	}
}