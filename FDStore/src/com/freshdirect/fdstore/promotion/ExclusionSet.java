package com.freshdirect.fdstore.promotion;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ExclusionSet implements Serializable {

	private boolean allowAll;
	private final Set exclusions = new HashSet();

	public ExclusionSet(boolean allowAll) {
		this.allowAll = allowAll;
	}

	public boolean isAllowAll() {
		return this.allowAll;
	}

	public void setAllowAll(boolean allowAll) {
		this.allowAll = allowAll;
	}

	public void exclude(Object o) {
		this.exclusions.add(o);
	}

	public Set getExclusions() {
		return Collections.unmodifiableSet(this.exclusions);
	}

	public boolean isAllowed(Object o) {
		return this.allowAll ^ this.exclusions.contains(o);
	}

	public String toString() {
		return (this.allowAll ? "allow" : "deny") + " all" + (this.exclusions.isEmpty() ? "" : " except " + this.exclusions);
	}

}
