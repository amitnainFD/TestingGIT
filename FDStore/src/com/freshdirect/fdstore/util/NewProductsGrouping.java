/**
 * 
 */
package com.freshdirect.fdstore.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.EnumSortingValue;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.FilteringSortingItem;

/**
 * @author skrishnasamy
 * 
 */
public class NewProductsGrouping {
	public static final Logger LOGGER = Logger.getLogger(NewProductsGrouping.class);

	private static int isRecencySymbol(char symbol) {
		if (symbol == '<')
			return TimeRange.NEWER_THAN;
		if (symbol == '>')
			return TimeRange.OLDER_THAN;
		return -1;
	}

	private static int isDurationSymbol(char symbol) {
		if (symbol == 'D')
			return TimeRange.DAY;
		if (symbol == 'W')
			return TimeRange.WEEK;
		if (symbol == 'M')
			return TimeRange.MONTH;
		return -1;
	}
	
	private List<TimeRange> timeRanges = new ArrayList<TimeRange>();
	private boolean reverse;

	public NewProductsGrouping(boolean reverse) {
		init();
		this.reverse = reverse;
	}

	private void init() {
		/*
		 * Eg: <W2,W2-W4,M1-M2,M2-M3,>M3
		 * timeRanges.add(new TimeRange(1,2,0,TimeRange.WEEK, TimeRange.NEWER_THAN ));
		 * timeRanges.add(new TimeRange(2,2,4,TimeRange.WEEK, TimeRange.NULL ));
		 * timeRanges.add(new TimeRange(3,1,2,TimeRange.MONTH, TimeRange.NULL ));
		 * timeRanges.add(new TimeRange(4,2,3,TimeRange.MONTH, TimeRange.NULL));
		 * timeRanges.add(new TimeRange(5,3,0,TimeRange.MONTH, TimeRange.OLDER_THAN ));
		*/

		String groups = FDStoreProperties.getNewProductsGrouping();
		if (groups == null || groups.length() == 0) {
			LOGGER.error("New product grouping cannot be null or empty");
			return;
		}
		StringTokenizer tokens = new StringTokenizer(groups, ",");
		int sequence = 1;
		outer: while (tokens.hasMoreTokens()) {
			int recencyType = -1;
			int fromValue = -1;
			int toValue = -1;
			int duration = -1;
			boolean tokenValid = true;
			int symbolCnt = 0;
			int validSymCnt = 0;

			String token = tokens.nextToken();
			if (token != null && token.length() > 0) {
				if (token.indexOf("-") != -1) {
					// contains both from and to value.
					StringTokenizer subTokens = new StringTokenizer(token, "-");
					if (subTokens.countTokens() > 2) {
						// Token is invalid.
						LOGGER.debug("Invalid Token Level 2 Check");
					}
					// Read FROM Value;
					String subToken = subTokens.nextToken();
					if (subToken != null && subToken.length() > 1) {
						char symbol = subToken.charAt(0);
						try {
							Integer.parseInt(String.valueOf(symbol));
						} catch (NumberFormatException nfe) {
							// Not a number. Parse the symbol.
							if (isDurationSymbol(symbol) != -1) {
								duration = isDurationSymbol(symbol);
								String remainingSubToken = subToken.substring(1);
								try {
									fromValue = Integer.parseInt(String.valueOf(remainingSubToken));
								} catch (NumberFormatException e) {
									// invalid token
									LOGGER.debug("Invalid fromValue");
									tokenValid = false;
									continue outer; // scan next token.
								}
							} else {
								// invalid token
								LOGGER.debug("Invalid duration");
								tokenValid = false;
								continue outer; // scan next token.
							}
						}
					}
					if (!tokenValid)
						continue outer; // scan next token.
					// Read TO Value;
					subToken = subTokens.nextToken();
					if (subToken != null && subToken.length() > 1) {
						char symbol = subToken.charAt(0);
						try {
							Integer.parseInt(String.valueOf(symbol));
						} catch (NumberFormatException nfe) {
							// Not a number. Parse the symbol.
							if (isDurationSymbol(symbol) != -1) {
								duration = isDurationSymbol(symbol);
								String remainingSubToken = subToken.substring(1);
								try {
									toValue = Integer.parseInt(String.valueOf(remainingSubToken));
								} catch (NumberFormatException e) {
									// invalid token
									LOGGER.debug("Invalid toValue");
									tokenValid = false;
									continue outer; // scan next token.
								}
							} else {
								// invalid token
								LOGGER.debug("Invalid duration");
								tokenValid = false;
								continue outer; // scan next token.
							}
						}
					}
					if (tokenValid) {
						TimeRange tr = new TimeRange(sequence, fromValue, toValue, duration, TimeRange.NULL);
						timeRanges.add(tr);
						sequence++;
					}
				} else {
					// contains only from value.
					for (int i = 0; i < token.length(); i++) {
						char symbol = token.charAt(i);
						symbolCnt++;
						try {
							Integer.parseInt(String.valueOf(symbol));
						} catch (NumberFormatException nfe) {
							// Not a number. Parse the symbol.
							if (isRecencySymbol(symbol) != -1) {
								recencyType = isRecencySymbol(symbol);
							}
							if (isDurationSymbol(symbol) != -1) {
								duration = isDurationSymbol(symbol);
							}
							if (recencyType == -1 && duration == -1) {
								// invalid token
								LOGGER.debug("Invalid duration or recency type");
								tokenValid = false;
								break;
							}

							if (symbolCnt == 2 && duration == -1) {
								// invalid token
								LOGGER.debug("Invalid duration");
								tokenValid = false;
								break;
							}
							validSymCnt++;
						}
						if (symbolCnt == 2)
							break; // Read
					}
					if (tokenValid) {
						String remainingToken = token.substring(symbolCnt);
						try {
							fromValue = Integer.parseInt(String.valueOf(remainingToken));
							TimeRange tr = new TimeRange(sequence, fromValue, 0, duration, recencyType);
							timeRanges.add(tr);
							sequence++;
						} catch (NumberFormatException nfe) {
							// invalid token
							LOGGER.debug("Invalid fromValue");
							tokenValid = false;
							continue outer; // scan next token.
						}
					} else {
						continue outer; // scan next token.
					}
				}
			} else {
				// Token is invalid.
				LOGGER.debug("Invalid Token Level 1 Check");
				continue outer; // scan next token.
			}
		}

		// !!! HACK !!!
		// this hack is needed to eliminate the gap between 4W and 1M
		// the previous implementation was conceptually wrong
		for (int i = 0; i < timeRanges.size() - 1; i++) {
			TimeRange curr = timeRanges.get(i);
			TimeRange next = timeRanges.get(i + 1);
			if (curr.getRecencyType() == TimeRange.NEWER_THAN)
				next.adjustDayRangeFrom(curr.getDaysRangeFrom());
			else
				next.adjustDayRangeFrom(curr.getDaysRangeTo());
		}
	}

	public List<TimeRange> getTimeRanges() {
		return this.timeRanges;
	}

	public TimeRange getTimeRangeForProduct(FilteringSortingItem<ProductModel> product) {
		for (TimeRange timeRange : timeRanges)
			if (timeRange.fallsIn(product.getSortingValue(EnumSortingValue.NEWNESS).floatValue()))
				return timeRange;
		throw new IllegalStateException("time ranges are badly constructed");
	}

	public SortedMap<TimeRange, List<ProductModel>> groupBy(List<FilteringSortingItem<ProductModel>> products) {
		if (this.timeRanges.isEmpty())
			throw new IllegalStateException("time ranges are badly constructed");
		SortedMap<TimeRange, List<ProductModel>> groupedMap;
		if (reverse)
			groupedMap = new TreeMap<TimeRange, List<ProductModel>>(Collections.reverseOrder());
		else
			groupedMap = new TreeMap<TimeRange, List<ProductModel>>();
		for (TimeRange timeRange : timeRanges)
			groupedMap.put(timeRange, new ArrayList<ProductModel>(products.size()));
		for (FilteringSortingItem<ProductModel> product : products) {
			TimeRange timeRange = getTimeRangeForProduct(product);
			groupedMap.get(timeRange).add(product.getModel());
		}
		return groupedMap;
	}
	
	public Comparator<FilteringSortingItem<ProductModel>> getTimeRangeComparator() {
		// NOTE that this will not use reverse ordering as the sorting algorithm will reverse the ordering of the whole sorting !!!
		return new Comparator<FilteringSortingItem<ProductModel>>() {
			@Override
			public int compare(FilteringSortingItem<ProductModel> o1, FilteringSortingItem<ProductModel> o2) {
				TimeRange tr1 = getTimeRangeForProduct(o1);
				TimeRange tr2 = getTimeRangeForProduct(o2);
				return tr1.getSequence() - tr2.getSequence();
			}
		};
	}
}
