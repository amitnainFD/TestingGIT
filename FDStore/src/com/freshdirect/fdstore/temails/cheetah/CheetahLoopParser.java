package com.freshdirect.fdstore.temails.cheetah;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.rules.OgnlCondition;
import com.freshdirect.rules.RulesRuntimeException;
import com.freshdirect.temails.ParserI;
import com.freshdirect.temails.TEmailRuntimeException;
import com.freshdirect.temails.TemailRuntimeI;

public class CheetahLoopParser implements ParserI {

	private final static Logger LOGGER = LoggerFactory.getInstance(OgnlCondition.class);
	
	private String expression;
	private Object formattedObj;
	private String order;
	private String loopLength;
	private String condition;
	private CheetahLoopParser loopParser;
	private String parentLoopIndex;
	private String id;
	private String parentId;
	private String formattedExpression;
	private Map indexMap=null;
	private int level; 
	
	public CheetahLoopParser(String expression){
		this.expression=expression;
	}
	

	public CheetahLoopParser(CheetahLoopParser loopParser){
		this.loopParser=loopParser;
	}
	
	
	public String parse(Object target, TemailRuntimeI ct) {
		try {
			
			if(this.getCondition()!=null && this.getCondition().trim().length()>0){
				Boolean b=(Boolean)Ognl.getValue(this.getCondition(), new OgnlContext(), target);
				if(!b.booleanValue()) return "";
			}
			Number index=null;
			
			if(getIndexMap()==null){ indexMap=new HashMap(); }
			
		   index=(Number)Ognl.getValue(this.getLoopLength(), new OgnlContext(), target);									
					
			//if(parentLoopIndex!=null && parentLoopIndex.trim().length()>0){
			
			StringBuffer exp=new StringBuffer();
			for(int i=0;i<index.intValue();i++){			
				if(i>0){
				    exp.append("+\'&&\'+");	
				}
				//if(parentLoopIndex!=null && parentLoopIndex.trim().length()>0)	exp.append(getFormattedExpression().replaceAll("\\{index"+level+"\\}",""+(i+1)));
								
				exp.append(getExpression().replaceAll("\\{index"+level+"\\}",""+(i+1)));
					
							
				//System.out.println("exp:"+exp.toString());
			}						
			//System.out.println("loop expression after parse :"+exp.toString());
			String result=null;
			
			indexMap=null;
			result = (String) Ognl.getValue(exp.toString(), new OgnlContext(), target);
							
			System.out.println("loop result :"+result);
			formattedExpression=null;
			return result;
		} catch (OgnlException e) {
			LOGGER.warn("Failed to evaluate expression '" + expression + "'", e);
			throw new TEmailRuntimeException(e);
			//return "Error";
		}
	}
	
	
	private Object getFormattedObject() {
		if (this.formattedObj != null) {
			return this.formattedObj;
		}
		try {
			this.formattedObj = Ognl.parseExpression(expression);
			return this.formattedObj;
		} catch (OgnlException e) {
			throw new RulesRuntimeException(e);
		}
	}

	@Override
	public String getOrder() {
		// TODO Auto-generated method stub
		return this.order;
	}

	@Override
	public void setOrder(String order) {
		// TODO Auto-generated method stub
        this.order=order; 
	}

	public boolean validate() {
		try {
			Ognl.parseExpression(this.expression);
			return true;
		} catch (OgnlException e) {
			return false;
		}
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}


	public String getLoopLength() {
		return loopLength;
	}


	public void setLoopLength(String loopLength) {
		this.loopLength = loopLength;
	}
	
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}


	public void setChildParser(ParserI p){
		this.loopParser=(CheetahLoopParser)p;
	}


	public ParserI getChildParser(){
		return this.loopParser;
	}

	
	public String getParentLoopIndex() {
		return parentLoopIndex;
	}


	public void setParentLoopIndex(String parentLoopIndex) {
		this.parentLoopIndex = parentLoopIndex;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getParentId() {
		return parentId;
	}


	public void setParentId(String parentId) {
		this.parentId = parentId;
	}


	public String getFormattedExpression() {
		return formattedExpression;
	}


	public void setFormattedExpression(String formattedExpression) {
		this.formattedExpression = formattedExpression;
	}


	public int getLevel() {
		return level;
	}


	public void setLevel(int level) {
		this.level = level;
	}


	public Map getIndexMap() {
		return indexMap;
	}


	public void setIndexMap(Map indexMap) {
		this.indexMap = indexMap;
	}
}