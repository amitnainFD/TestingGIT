package com.freshdirect.fdstore;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Category;

import com.freshdirect.framework.core.ModelSupport;
import com.freshdirect.framework.util.log.LoggerFactory;

public class URLRewriteRule extends ModelSupport {
	
	private static final Category LOGGER = LoggerFactory.getInstance(URLRewriteRule.class); 
	
	private String name;
	private boolean disabled;
	private Pattern from;
	private Pattern redirect;
	private String comments;
	private List options;
	private int priority;
	
	private boolean valid = true;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public String getFrom() {
		return this.from != null ? this.from.pattern() : "";
	}
	
	public void setFrom(String from) {
		try{
			if(isCaseInsensitive()) {
				this.from = Pattern.compile(from, Pattern.CASE_INSENSITIVE);
			}else{
				this.from = Pattern.compile(from);
			}
		}catch (Exception e) {
			this.valid = false;
			LOGGER.warn("Malformed from REGEX: "+ from + " for RewriteRule: " + this.name, e );
		}
	}
	
	public String getRedirect() {
		return this.redirect != null ? redirect.pattern() : "" ;
	}
	
	public void setRedirect(String redirect) {
		try{
			if(isCaseInsensitive()){
				this.redirect = Pattern.compile(redirect, Pattern.CASE_INSENSITIVE);
			}else{
				this.redirect = Pattern.compile(redirect);
			}
		}catch (Exception e) {
			this.valid = false;
			LOGGER.warn("Malformed Redirect REGEX: "+ redirect + " for RewriteRule: " + this.name, e );
		}
	}
	
	public String getComments() {
		return comments;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public List getOptions() {
		return options;
	}
	
	public void setOptions(List options) {
		this.options = options;
	}
	
	public int getPriority () {
		return this.priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public boolean isValid() {
		return this.valid;
	}
	
	public boolean match(String url) {
		try{
			Matcher m = this.from.matcher(url);
			return m.matches();
		}catch(Exception e) {
			LOGGER.warn("Exception in match for rule: "+this.name, e);
			return false;
		}
	}
	
	private boolean isCaseInsensitive () {
		if(this.options != null && !this.options.isEmpty() && this.options.contains("N")) {
			return true;
		}
		
		return false;
	}
	
}
