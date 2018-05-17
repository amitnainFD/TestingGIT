package com.freshdirect.fdstore.content.browse.sorter;

import java.util.Comparator;


public abstract class OptionalObjectComparator<T,Q extends Comparable<Q>> implements Comparator<T> {
	private int nullSafeComparator(final Q one, final Q two) {
	    if (one == null ^ two == null) {
	        return (one == null) ? -1 : 1;
	    }

	    if (one == null && two == null) {
	        return 0;
	    }

	    return one.compareTo(two);
	}

	@Override
	public int compare(T o1, T o2) {
		if (o1 != null) {
			if (o2 != null) {
				Q v1 = getValue(o1);
				Q v2 = getValue(o2);
				
				return nullSafeComparator(v1, v2);
			} else {
				// o1 > NULL
				return -1;
			}
		} else {
			// NULL < o2
			return o2 != null ? 1 : 0;
		}
	}

	abstract protected Q getValue(T obj);
}
