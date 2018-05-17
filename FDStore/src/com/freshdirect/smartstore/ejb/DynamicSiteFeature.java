package com.freshdirect.smartstore.ejb;

import java.io.Serializable;


public class DynamicSiteFeature implements Comparable<DynamicSiteFeature>, Serializable {
	private static final long serialVersionUID = 4834283257586887527L;

	private String name;
	
	private String title;
	
	@Deprecated
	private String prez_title;
	
	@Deprecated
	private String prez_desc;
	
	@Deprecated
	boolean smartSaving;

	protected DynamicSiteFeature(String name, String title, String prez_title, String prez_desc, boolean smartSaving) {
		if (name == null)
			throw new IllegalArgumentException("name must not be null");
		
		this.name = name;
		this.title = title;
		this.smartSaving = smartSaving;
	}

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public int hashCode() {
		return ((name == null) ? 0 : name.hashCode());
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DynamicSiteFeature other = (DynamicSiteFeature) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public int compareTo(DynamicSiteFeature o) {
		return name.compareTo(o.name);
	}

	public String toString() {
		return "DynamicSiteFeature[" + name + "]";
	}

	@Deprecated
	public String getPresentationTitle() {
		return prez_title;
	}

	@Deprecated
	public String getPresentationDescription() {
		return prez_desc;
	}

	@Deprecated
	public boolean isSmartSaving() {
		return smartSaving;
	}

	@Deprecated
	public void setSmartSaving(boolean smartSaving) {
		this.smartSaving = smartSaving;
	}
}
