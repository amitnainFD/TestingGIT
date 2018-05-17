package com.freshdirect.smartstore.sampling;

import java.util.List;

/**
 * A rule that decides how many items to consider in sampling.
 * 
 * @author istvan
 * 
 */
public interface ConsiderationLimit {
	/**
	 * 
	 * @param rankedItems
	 *            (List<{@link RankedContent}>)
	 * @return how many to remove max from ranked items
	 */
	public int max(List<? extends RankedContent> rankedItems);
}