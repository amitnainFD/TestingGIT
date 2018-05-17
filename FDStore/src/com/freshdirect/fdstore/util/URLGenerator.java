package com.freshdirect.fdstore.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * General purpose URL generator that maintains request parameters
 * and passes to URL. It also allows them to modify.
 * 
 * @author zsgegesy
 *
 */
public class URLGenerator {

    final static Object NULL       = new Object();

    Map                 requestParams;
    String              requestUri;
    Map                 parameters = new HashMap();
    Set                 extraKeys  = new HashSet();
    boolean             escapeAndSign = true; 

    public URLGenerator(HttpServletRequest request) {
    	// This is necessary to avoid clash when invoking remove(key) on
    	//   weblogic.servlet.internal.ParamMap class.
        this.requestParams = new HashMap(request.getParameterMap());
        this.requestUri = request.getRequestURI();
    }

    public URLGenerator(String requestUri, Map params) {
        this.requestParams = params;
        this.requestUri = requestUri;
    }



    private URLGenerator(Map requestParams, String requestUri, Map parameters, Set extraKeys, boolean escapeSign) {
        this.requestParams = requestParams;
        this.requestUri = requestUri;
        this.escapeAndSign = escapeSign;
    	
        this.parameters = new HashMap(parameters);
        this.extraKeys = new HashSet(extraKeys);
    }
    
    public void setEscapeAndSign(boolean escapeAndSign) {
        this.escapeAndSign = escapeAndSign;
    }
    
    public boolean isEscapeAndSign() {
        return escapeAndSign;
    }
    
    public Object clone() {
    	return new URLGenerator(requestParams, requestUri, parameters, extraKeys, escapeAndSign);
    }

    /**
     * Sets or overrides a parameter with new value
     * 
     * @param name
     * @param value
     * 
     * @return
     */
    public URLGenerator set(String name, Object value) {
        if (value != null) {
            this.parameters.put(name, value.toString());
            if (this.requestParams.get(name) == null) {
                this.extraKeys.add(name);
            }
        } else {
            remove(name);
        }
        return this;
    }


    /**
     * Sets or overrides a parameter with new value
     * 
     * @param name
     * @param value
     * 
     * @return
     */
    public URLGenerator set(String name, int value) {
        return set(name, String.valueOf(value));
    }


    /**
     * Removes a parameter
     * 
     * @param name
     * @return
     */
    public URLGenerator remove(String name) {
        if (this.requestParams.get(name) != null) {
            // we must NULL-out
            this.parameters.put(name, NULL);
        } else {
            this.parameters.remove(name);
            this.extraKeys.remove(name);
        }
        return this;
    }
    
    
    /**
     * Returns a request parameter with name
	 *
     * @param name
     * @return
     */
    public String get(String name) {
        Object result = this.parameters.get(name);
        if (result != null) {
            if (result == NULL) {
                return null;
            } else {
                return (String) result;
            }
        }
        result = this.requestParams.get(name);
        if (result instanceof String[]) {
            return ((String[]) result)[0];
        } else {
            if (result instanceof String) {
                return (String) result;
            }
            if (result != null) {
                return result.toString();
            }
        }
        return null;
    }


    /**
     * Renames a parameter by removing the old instance and
     * creating a new with the original value
     * 
     * Note, that the change cannot be undoed with reset operation.
     * 
     * @author segabor
     * 
     * @param old_name Name of old parameter
     * @param new_name Name of new parameter
     * 
     * @return navigator object itself
     * 
     */
    public URLGenerator rename(String old_name, String new_name) {
		String val = getOriginal(old_name);
		if (!old_name.equalsIgnoreCase(new_name) &&
				this.requestParams.get(old_name) != null &&
				this.requestParams.get(new_name) == null) {
			// rename if not renamed yet
			// remove(oldName);
			// set(newName, val);
			this.requestParams.remove(old_name);
			this.requestParams.put(new_name, val);
		}

    	return this;
    }



    // Getting the original request URL's parameters
    public String getOriginal(String name) {
    	Object result = this.requestParams.get(name);
        
        if (result instanceof String[]) {
            return ((String[]) result)[0];
        } else {
            if (result instanceof String) {
                return (String) result;
            }
            if (result != null) {
                return result.toString();
            }
        }
        return null;
    }

    
    /**
     * Resets to the original state by
     * wiping added / modified entries away
     * 
     */
    public void reset() {
        this.parameters.clear();
        this.extraKeys.clear();
    }

    public URLGenerator setURI(String uri) {
        this.requestUri = uri;
        return this;
    }
    
    public String getRequestUri() {
        return requestUri;
    }
    

    /**
     * Creates URI pointing to the search page
     * having parameters appended to it
     * 
     * @return
     */
    public String build() {
        StringBuffer buffer = new StringBuffer(100);
        buffer.append(requestUri);
        boolean first = true;
        for (Iterator iter = this.requestParams.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            String value = get(key);
            first = appendToQuery(buffer, first, key, value);
        }
        for (Iterator iter = this.extraKeys.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            String value = get(key);
            first = appendToQuery(buffer, first, key, value);
        }
        return buffer.toString();
    }


    /**
     * Generates a URI with the current values then
     * resets to the original state.
     * 
     * @return
     */
    public String buildNew() {
        try {
            return build();
        } finally {
            reset();
        }
    }

    
    // convenience method
    public String getURI() {
    	return buildNew();
    }


    /**
     * Convenience method that returns a HTML Anchor element
     * 
     * @param title anchor title
     *
     * @return HTML chunk
     */
    public String getFullAnchor(String title) {
        try {
            return "<a href=\"" + build() + "\">" + title + "</a>";
        } finally {
            reset();
        }
    }

    /**
     * Convenience method that returns a HTML Anchor element with custom style
     * 
     * @param title anchor title
     * @param className CSS class name
     *
     * @return HTML chunk
     */
    public String getFullAnchor(String title, String className) {
        try {
            return "<a href=\"" + build() + "\" class=\"" + className +"\">" + title + "</a>";
        } finally {
            reset();
        }
    }
    
    /**
     * Hides a given parameter in form fields
     * 
     * @return Piece of HTML code that contains the parameter
     */
    public String buildHiddenField(String key) {
        try {
            StringBuffer buffer = new StringBuffer(100);
            String value = get(key);
            appendHiddenField(buffer, key, value);
            return buffer.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("No UTF-8 character set presents:" + e.getMessage(), e);
        }
    }

    /**
     * Hides parameters in form fields
     * 
     * @return Piece of HTML code that contains parameters
     */
    public String buildHiddenFields() {
        try {
            StringBuffer buffer = new StringBuffer(100);
            for (Iterator iter = this.requestParams.keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                String value = get(key);
                appendHiddenField(buffer, key, value);
            }
            for (Iterator iter = this.extraKeys.iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                String value = get(key);
                appendHiddenField(buffer, key, value);
            }
            return buffer.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("No UTF-8 character set presents:" + e.getMessage(), e);
        } finally {
            reset();
        }

    }



    private void appendHiddenField(StringBuffer buffer, String key, String value) throws UnsupportedEncodingException {
        if (value!=null) {
            buffer.append("<input type=\"hidden\" name=\"").append(key).append("\" value=\"").append(StringEscapeUtils.escapeHtml(value)).append("\"/>\n");
        }
    }

    private boolean appendToQuery(StringBuffer buffer, boolean first, String key, String value) {
        if (value != null) {
            if (first) {
                buffer.append('?');
            } else {
            	// Note: HTML 4.01 requires ampersand as entity
                if (escapeAndSign) {
                    buffer.append("&amp;");
                } else {
                    buffer.append('&');
                }
            }
            try {
                buffer.append(key).append('=').append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("No UTF-8 character set presents:" + e.getMessage(), e);
            }
            first = false;
        }
        return first;
    }

}
