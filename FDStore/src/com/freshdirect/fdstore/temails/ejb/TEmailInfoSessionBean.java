package com.freshdirect.fdstore.temails.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.ejb.FDSessionBeanSupport;
import com.freshdirect.fdstore.temails.TEmailConstants;
import com.freshdirect.fdstore.temails.TEmailContentFactory;
import com.freshdirect.fdstore.temails.TEmailTemplateInfo;
import com.freshdirect.fdstore.temails.TransEmailInfoModel;
import com.freshdirect.framework.mail.TEmailI;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.mail.EnumEmailType;
import com.freshdirect.mail.EnumTranEmailType;
import com.freshdirect.mail.ejb.TEmailerGatewayHome;
import com.freshdirect.mail.ejb.TMailerGatewaySB;
import com.freshdirect.temails.TEmailRuntimeException;

public class TEmailInfoSessionBean extends FDSessionBeanSupport{
    
	private static final long serialVersionUID = 1L;
	private static Category LOGGER = LoggerFactory.getInstance( TEmailInfoSessionBean.class );

	public void sendEmail(EnumTranEmailType tranType,Map input) throws FDResourceException, RemoteException{
	    // check template is active 	
		// get the email content from the factory
		// store it in db
		// put it in the jms queue
		// throw exception any error comes in
		Connection con=null;
		boolean isTemplateExist=true;
		try
		{
			con=getConnection();
						
			
			//XMLEmailI xml=FDEmailFactory.getInstance().createConfirmOrderEmail((FDCustomerInfo)input.get(TEmailConstants.CUSTOMER_INP_KEY), (FDOrderI)input.get(TEmailConstants.ORDER_INP_KEY));
			//System.out.println(xml.getXML());
			
			
			TEmailTemplateInfo info=TEmailInfoDAO.getTEmailTemplateInfo(con,tranType,input.get(TEmailConstants.EMAIL_TYPE)!=null?EnumEmailType.getEnum((String)input.get(TEmailConstants.EMAIL_TYPE)):EnumEmailType.TEXT);			
			if(info==null){
				isTemplateExist=false;	
				throw new TEmailRuntimeException("No active templateId exist for tranType :"+tranType.getName());
			} else {
				LOGGER.debug("------------------------------------info:" + info.toString());
				System.out.println("---------------------------info" + info.toString());
			}
								
			TEmailI mail=TEmailContentFactory.getInstance().createTransactionEmailModel(info, input);
									
			System.out.println("TEmailI :"+mail.getEmailContent());
			
			TEmailInfoDAO.storeTransactionEmailInfo(con, (TransEmailInfoModel)mail);			
			
			TEmailerGatewayHome home=getTMailerGatewayHome();
			TMailerGatewaySB remote= home.create();
			remote.enqueue(mail);			
				
			
		}		
		catch(SQLException re){
			re.printStackTrace();
			getSessionContext().setRollbackOnly();
			throw new TEmailRuntimeException(re);				
		}		
		catch(TEmailRuntimeException re){
			if(isTemplateExist) re.printStackTrace();			   
			getSessionContext().setRollbackOnly();
			throw re;
		}catch (Exception e){
			e.printStackTrace();
			getSessionContext().setRollbackOnly();
			throw new TEmailRuntimeException(e);
		} finally{
			close(con);
		}		
	}
	
	
	
	public void sendFailedTransactions(int timeout){
		Connection con=null;
		try
		{
			con=getConnection();
			List<TEmailI> failedTranList=TEmailInfoDAO.getFailedTransactions(con);
			long startTime = System.currentTimeMillis();			
			TEmailerGatewayHome home=getTMailerGatewayHome();
			TMailerGatewaySB remote= home.create();
			for(TEmailI mail: failedTranList ){			
				if (System.currentTimeMillis() - startTime > timeout) {
					LOGGER.warn(" transaction send mail was running longer than" + timeout / 60 / 1000);
					break;
				}
			
				try{	
					  remote.enqueue(mail);			
				} catch(TEmailRuntimeException re){
						re.printStackTrace();							
				}catch (Exception e){
						e.printStackTrace();							
				}
			}	
		}		
		catch(SQLException re){
			re.printStackTrace();			
			throw new TEmailRuntimeException(re);				
		}
		catch (Exception e){
			e.printStackTrace();							
		}
		finally{
			close(con);
		}							
	}
	
	
	public List getFailedTransactionList(int max_count,boolean isEmailContentReqd){
		Connection con=null;
		List failedTranList=null;
		try
		{
			con=getConnection();
			 failedTranList=TEmailInfoDAO.getFailedTransactions(con,max_count,isEmailContentReqd);				
		}		
		catch(SQLException re){
			re.printStackTrace();			
			throw new TEmailRuntimeException(re);				
		}
		catch (Exception e){
			e.printStackTrace();				
			throw new TEmailRuntimeException(e);
		}
		finally{
			close(con);
		}	
		return failedTranList;
	}
	
	
	public Map getFailedTransactionStats(){
		Connection con=null;
		Map failedTranList=null;
		try
		{
			con=getConnection();
			 failedTranList=TEmailInfoDAO.getFailedTransactionsDetails(con);				
		}		
		catch(SQLException re){
			re.printStackTrace();			
			throw new TEmailRuntimeException(re);				
		}
		catch (Exception e){
			e.printStackTrace();			
			throw new TEmailRuntimeException(e);
		}
		finally{
			close(con);
		}	
		return failedTranList;
	}
	

    /**
     * @return
     * @see com.freshdirect.fdstore.customer.ejb.FDServiceLocator#getMailerHome()
     */
    protected TEmailerGatewayHome getTMailerGatewayHome() {
        return LOCATOR.getTMailerGatewayHome();
    }

	

}
