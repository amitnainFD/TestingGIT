package com.freshdirect.fdstore.util;

import java.io.Serializable;

public class IgnoreCaseString implements Comparable<IgnoreCaseString>, Serializable {
	private static final long serialVersionUID = -3341925694223083166L;

	final String string;
	
	public IgnoreCaseString(String string) {
		super();
		if (string == null)
			throw new IllegalArgumentException();
		this.string = string;
	}

	@Override
	public int hashCode() {
		return string.toLowerCase().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof String)
			return equals(new IgnoreCaseString((String) obj));
		if (getClass() != obj.getClass())
			return false;
		IgnoreCaseString other = (IgnoreCaseString) obj;
		if (!string.equalsIgnoreCase(other.string))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return string;
	}

	@Override
	public int compareTo(IgnoreCaseString o) {
		return string.compareToIgnoreCase(o.string);
	}
}
