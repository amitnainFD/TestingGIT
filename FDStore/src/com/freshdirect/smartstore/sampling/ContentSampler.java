package com.freshdirect.smartstore.sampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.smartstore.filter.ContentFilter;
import com.freshdirect.smartstore.scoring.HelperFunctions;

/**
 * Sampler for lists of ranked content.
 * 
 * 
 * @author istvan
 * 
 */
public class ContentSampler {
	private List<RankedContent> sortedItems;

	private int n;

	public ContentSampler(List<? extends RankedContent> allSortedItems, ContentFilter filter, ConsiderationLimit cl) {
		n = cl.max(allSortedItems);
		sortedItems = new LinkedList<RankedContent>();

		boolean reSort = false;
		double lastScore = Float.MAX_VALUE;
		int total = 0;

		for (Iterator<? extends RankedContent> i = allSortedItems.iterator(); i.hasNext();) {
			if (total >= n)
				break;
			RankedContent item = i.next();
			if (item instanceof RankedContent.Aggregate) {
				RankedContent.Aggregate aggregateItem = (RankedContent.Aggregate) item;
				aggregateItem.filterWith(filter);
				if (aggregateItem.getCount() == 0) {
					continue;
				} else {
					total += aggregateItem.getCount();
				}
			} else { // Single
				final RankedContent.Single singleItem = (RankedContent.Single) item;
                                if (!singleItem.filter(filter)) {
                                    continue;
                                }
				total++;
			}

			if (item.getScore() > lastScore) {
				reSort = true;
			}

			sortedItems.add(item);
			lastScore = item.getScore();
		}

		if (reSort)
			Collections.sort(sortedItems);
	}

	public List<RankedContent> getSortedItems() {
		return sortedItems;
	}

	public List<ContentKey> drawWithoutReplacement(ListSampler sampler) {
		List<ContentKey> items = new ArrayList<ContentKey>(n);
		for (int i = 0; i < n && sortedItems.size() > 0; i++) {

			int ind = sampler.next(sortedItems.size());

			ListIterator<RankedContent> it = sortedItems.listIterator(ind);
			RankedContent item = it.next();

			RankedContent.Single itemToUse = null;

			if (item instanceof RankedContent.Aggregate) { // must re-calculate
				// distribution
				RankedContent.Aggregate aggregate = (RankedContent.Aggregate) item;
				itemToUse = aggregate.take();
				if (aggregate.getCount() == 0) {
					// lucky, we can just remove
					it.remove();
				} else {
					// not so lucky, needs to re-insert node in proper place,
					// plus re-calculate the distribution
					ListIterator<RankedContent> curPos = sortedItems.listIterator(ind);
					ListIterator<RankedContent> nextPos = it;
					sampler.changeWeight(ind, aggregate.getScore());
					// shift to the right position
					while (nextPos.hasNext()) {
						RankedContent curItem = curPos.next();
						RankedContent nextItem = nextPos.next();
						if (curItem.getScore() < nextItem.getScore()) {
							// swap
							curPos.set(nextItem);
							nextPos.set(curItem);
							sampler.changeWeight(ind, nextItem.getScore());
							sampler.changeWeight(++ind, curItem.getScore());
						} else {
							break;
						}
					}
				}
			} else { // Single (and how simple)
				itemToUse = (RankedContent.Single) item;
				it.remove();
				sampler.changeWeight(ind, 0);
			}

			// items.add(itemToUse.getContentKey());
			items.add(itemToUse.getContentKey());
		}
		return items;
	}
}
