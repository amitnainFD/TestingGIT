package com.freshdirect.fdstore.temails;

import java.text.SimpleDateFormat;
import java.util.Map;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.temails.ejb.TEmailInfoSessionBean;
import com.freshdirect.framework.mail.TEmailI;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.temails.TEmailEngineI;
import com.freshdirect.temails.TEmailsRegistry;

public final class TEmailContentFactory {

	private static TEmailContentFactory factory=null;
	public static final SimpleDateFormat df = new SimpleDateFormat("EEEE, MMM d yyyy");
	public static final SimpleDateFormat DT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	
	private static Category LOGGER = LoggerFactory.getInstance( TEmailContentFactory.class );

	
	private TEmailContentFactory()
	{
		
	}
	
	public static  TEmailContentFactory getInstance(){
	
		if(factory==null){
			factory=new TEmailContentFactory();
		}
		return factory;
	}
		
	public  TEmailI createTransactionEmailModel(TEmailTemplateInfo info, Map input) 
	{
		
		
		// get the context;
		// get the formated email data		
		TEmailContextI context=TEmailsUtil.getTranEmailContext(info.getTransactionType(),info.getProvider(),input);
		
		TEmailEngineI engine= TEmailsRegistry.getTEmailsEngine(info.getProvider().getName());				
		String content=(String)engine.formatTemplates(context, info.getTemplateId());		
		
		LOGGER.debug("--------------------------------------------Email Content: " + content);
		// create the TEMAILINFOMODEL data and set everything
		// return the same				
		return  TEmailsUtil.createTransEmailModel(info,input,content);
	}
	
		    				
}
