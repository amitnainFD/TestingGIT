package com.freshdirect.fdstore.content.util;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.freshdirect.fdstore.FDRuntimeException;

public class QueryParameterCollection implements Serializable, Cloneable {
	private static final long serialVersionUID = 2412299841709561378L;

	public static QueryParameterCollection decode(String encoded) {
		QueryParameterCollection params = new QueryParameterCollection();
		if (encoded == null || encoded.length() == 0)
			return params;

		int i = 0;
		if (encoded.charAt(0) == '?')
			i++;

		MAIN: while (i < encoded.length())
			try {
				// search for first '='
				int j = i;
				while (encoded.charAt(j) != '=') {
					j++;
					if (j >= encoded.length())
						break MAIN;
				}

				String name = URLDecoder.decode(encoded.substring(i, j), "utf-8");
				if (name.length() == 0)
					break MAIN; // we do not parse rubbish
				i = j + 1;
				
				// search for first &
				j = i;
				if( j >= encoded.length() ) break;
				
				while (encoded.charAt(j) != '&') {
					j++;
					if (j >= encoded.length())
						break; // the string has ended
				}
				
				String value = encoded.substring(i, j);
				try {
					value = URLDecoder.decode(encoded.substring(i, j), "utf-8");
				} catch(IllegalArgumentException exc) {
					System.out.println("Warning Range cannot be decoded: "+ encoded.substring(i, j) );
				}
				
				if (value.length() != 0)
					params.addParameterValue(name, value);
				i = j + 1;
			} catch (UnsupportedEncodingException e) {
			}
		return params;
	}

	private Map<String, List<QueryParameter>> parameters;

	public QueryParameterCollection() {
		parameters = new HashMap<String, List<QueryParameter>>();
	}

	public void addParameterValue(String name, String value) {
		addParameter(new QueryParameter(name, value));
	}

	public void addParameter(QueryParameter parameter) {
		if (!parameters.containsKey(parameter.getName()))
			parameters.put(parameter.getName(), new ArrayList<QueryParameter>());

		parameters.get(parameter.getName()).add(parameter);
	}

	public void addParameters(Collection<QueryParameter> parameters) {
		for (QueryParameter parameter : parameters)
			addParameter(parameter);
	}
	
	public void removeParameter(String name) {
		if (parameters.containsKey(name)) {
			parameters.get(name).remove(0);
			if (parameters.get(name).size() == 0)
				parameters.remove(name);
		}
	}
	
	public void removeParameters(String name) {
		parameters.remove(name);
	}

	public QueryParameter getParameter(String name) {
		if (!parameters.containsKey(name))
			return null;

		return parameters.get(name).get(0);
	}

	public Collection<QueryParameter> getParameters(String name) {
		if (!parameters.containsKey(name))
			return null;

		return Collections.unmodifiableCollection(parameters.get(name));
	}

	public String getParameterValue(String name) {
		return this.getParameterValue(name, null);
	}

	public String getParameterValue(String name, String defaultValue) {
		if (!parameters.containsKey(name))
			return defaultValue;

		return parameters.get(name).get(0).getValue();
	}
	
	public String[] getParameterValues(String name) {
		if (!parameters.containsKey(name))
			return null;

		String[] ret = new String[parameters.get(name).size()];
		int i = 0;
		for (QueryParameter p : parameters.get(name)) {
			ret[i++] = p.getValue();
		}
		return ret;
	}

	public QueryParameterCollection add(QueryParameterCollection parameters) {
		for (Map.Entry<String, List<QueryParameter>> e : parameters.parameters.entrySet())
			addParameters(e.getValue());
		return this;
	}

	public QueryParameterCollection concatenate(QueryParameterCollection parameters) {
		QueryParameterCollection qpc = this.clone();
		for (Map.Entry<String, List<QueryParameter>> e : parameters.parameters.entrySet())
			qpc.addParameters(e.getValue());
		return qpc;
	}
	
	public void setParameterValue(String name, String value) {
		this.removeParameters(name);
		this.addParameterValue(name, value);
	}
	
	@Override
	public QueryParameterCollection clone() {
		QueryParameterCollection qpc = new QueryParameterCollection();
		for (Map.Entry<String, List<QueryParameter>> e : this.parameters.entrySet())
			qpc.addParameters(e.getValue());
		return qpc;
	}

	public String getEncoded() {
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, List<QueryParameter>> e : parameters.entrySet())
			for (QueryParameter p : e.getValue()) {
				if (!first)
					buf.append('&');
				else
					first = false;

				try {
					buf.append(URLEncoder.encode(p.getName(), "utf-8"));
					buf.append('=');
					buf.append(URLEncoder.encode(p.getValue(), "utf-8"));
				} catch (UnsupportedEncodingException e1) {
					throw new FDRuntimeException(e1, "utf-8 encoding is not supported. consider replacing your JVM");
				}
			}
		return buf.toString();
	}

	
	public void reset(String encoded) {
		parameters.clear();
		QueryParameterCollection qpc = QueryParameterCollection.decode(encoded);
		for (Map.Entry<String, List<QueryParameter>> e : qpc.parameters.entrySet())
			this.addParameters(e.getValue());
	}
}
