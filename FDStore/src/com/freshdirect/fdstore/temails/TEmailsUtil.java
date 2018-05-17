package com.freshdirect.fdstore.temails;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;

import com.freshdirect.customer.EnumDeliveryType;
import com.freshdirect.customer.EnumSaleType;
import com.freshdirect.customer.ErpComplaintModel;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.content.Recipe;
import com.freshdirect.fdstore.customer.FDCustomerInfo;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.mail.TellAFriend;
import com.freshdirect.fdstore.mail.TellAFriendProduct;
import com.freshdirect.fdstore.mail.TellAFriendRecipe;
import com.freshdirect.fdstore.temails.cheetah.CheetahTEmailContextImpl;
import com.freshdirect.fdstore.temails.ejb.TEmailInfoSessionBean;
import com.freshdirect.framework.mail.EmailAddress;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.giftcard.ErpGCDlvInformationHolder;
import com.freshdirect.mail.EnumTEmailProviderType;
import com.freshdirect.mail.EnumTEmailStatus;
import com.freshdirect.mail.EnumTranEmailType;

public final class TEmailsUtil {
	
	public static final SimpleDateFormat df = new SimpleDateFormat("EEEE, MMM d yyyy");
	
	private static Category LOGGER = LoggerFactory.getInstance( TEmailsUtil.class );
	
	public static boolean isTransEmailEnabled(EnumTranEmailType tranType){        
		 return FDStoreProperties.isTransactionEmailEnabled(tranType.getName());		 		 
	}
	
	
	public static String formatSubject(String subject,String[] args){
		
	
		MessageFormat form = new MessageFormat(subject);
        String message=form.format(args);
		
		return message;
	}
	
    public static TEmailContextI getTranEmailContext(EnumTranEmailType tranType,EnumTEmailProviderType provider,Map input){
		
		
		// create the provider context
		// set the appropriate
		TEmailContextI context=null;
		
		if(EnumTEmailProviderType.CHEETAH==provider){
			CheetahTEmailContextImpl tmpContext=new CheetahTEmailContextImpl();
			if(EnumTranEmailType.ORDER_SUBMIT==tranType){
				tmpContext.setCustomer((FDCustomerInfo)input.get(TEmailConstants.CUSTOMER_INP_KEY));
				tmpContext.setOrder((FDOrderI)input.get(TEmailConstants.ORDER_INP_KEY));
			}else if(EnumTranEmailType.ORDER_MODIFY==tranType){
				tmpContext.setCustomer((FDCustomerInfo)input.get(TEmailConstants.CUSTOMER_INP_KEY));
				tmpContext.setOrder((FDOrderI)input.get(TEmailConstants.ORDER_INP_KEY));
			}else if(EnumTranEmailType.CUST_SIGNUP==tranType){
				tmpContext.setCustomer((FDCustomerInfo)input.get(TEmailConstants.CUSTOMER_INP_KEY));
				//tmpContext.setOrder((FDOrderI)input.get(TEmailConstants.ORDER_INP_KEY));
			}						
			return tmpContext;
		}
						
		throw new FDRuntimeException("We are not supporting the provider :"+provider.getName()+"at this time");				
	  }	
    
    
      public static TransEmailInfoModel createTransEmailModel(TEmailTemplateInfo info, Map input,String content){
    	  TransEmailInfoModel model=new TransEmailInfoModel();	
    	  
    	  FDCustomerInfo custInfo=(FDCustomerInfo)input.get(TEmailConstants.CUSTOMER_ID_INP_KEY);;
    	  //if(custInfo!=null)   model.setOrderId(custInfo.getLastOrderId());
  		FDOrderI order=(FDOrderI)input.get(TEmailConstants.ORDER_INP_KEY);
  		if(order!=null){
  			model.setCustomerId(order.getCustomerId());
  			model.setOrderId(order.getErpSalesId());
  		}
  		model.setTemplateId(info.getTemplateId());
  		model.setTargetProgId(info.getTargetProgId());
  		model.setEmailTransactionType(info.getTransactionType());
  		model.setEmailContent(content);
  		model.setEmailType(info.getEmailType());
  		model.setProvider(info.getProvider());
  		model.setProductionReady(info.isProductionReady());
  		model.setFromAddress(new EmailAddress(TEmailConstants.GENERAL_LABEL,info.getFromAddress()));
  		// this is to be corrceted
  		String recipent=(String)input.get(TEmailConstants.EMAIL_RECIPENT);
  		if(recipent==null) model.setRecipient(custInfo.getEmailAddress());
  		else model.setRecipient(recipent); 
  		List list=(List)input.get(TEmailConstants.BCC_INP_KEY);
  		if(list!=null) model.setBCCList(list);
  		
  		List ccList=(List)input.get(TEmailConstants.CC_INP_KEY);
  		if(ccList!=null) model.setCCList(ccList);

  		System.out.println("model.getCCList() :"+model.getCCList());
  		
  		String subject=(String)input.get(TEmailConstants.SUBJECT_KEY);
  		if(subject!=null) model.setSubject(subject);
  		else {
  			if(EnumTranEmailType.ORDER_SUBMIT==info.getTransactionType()){
  				subject=formatSubject(info.getSubject(), new String[]{df.format(order.getRequestedDate())});  				
  			}
  			if(EnumTranEmailType.FINAL_INCOICE==info.getTransactionType()){
  				subject=formatSubject(info.getSubject(), new String[]{df.format(order.getRequestedDate())});
  				if (EnumDeliveryType.PICKUP.equals(order.getDeliveryType())) {
  					subject= subject + " is ready to be picked up.";
  				} else {
  					subject= subject +  " is on its way";
  				}
  			} 
  			else{
  				subject=info.getSubject();
  			}
  			model.setSubject(subject);
  		}
  		model.setEmailStatus(EnumTEmailStatus.NEW);
  		
  		if(input.get("oasQuery") != null)
  			model.setOasQueryString((String) input.get("oasQuery"));	
  		
  		LOGGER.debug("---------------------------model:"+ model.toString());
    	  
    	  return model;
    	  
      }
	
      
      
		
}
