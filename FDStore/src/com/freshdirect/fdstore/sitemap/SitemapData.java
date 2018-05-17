package com.freshdirect.fdstore.sitemap;

import java.util.ArrayList;
import java.util.List;

public class SitemapData {
	String name;
	String id;
	int countAll;
	int countAvailable;
	int countTempUnavailable;
	int countDiscontinued;
	List<SitemapData> children = new ArrayList<SitemapData>();
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{").append("\n");
		sb.append("id:\"").append(id).append("\",\n");
		sb.append("name:\"").append(name == null ? "" : name.replace("\"", "\\\"")).append("\",\n");
		sb.append("countAll:").append(countAll).append(",\n");
		sb.append("countAvailable:").append(countAvailable).append(",\n");
		sb.append("countTempUnavailable:").append(countTempUnavailable).append(",\n");
		sb.append("countDiscontinued:").append(countDiscontinued).append(",\n");
		sb.append("children: [\n");
		boolean first = true;
		for (SitemapData childData : children){
			if (first){
				first = false;
			} else { 
				sb.append(",");
			}
			
			sb.append(childData);
		}
		sb.append("]\n");
		sb.append("}").append("\n");
		return sb.toString();
	}
	
}