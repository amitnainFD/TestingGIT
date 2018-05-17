package com.freshdirect.fdstore.temails.ejb;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;

import oracle.jdbc.OraclePreparedStatement;

import com.freshdirect.fdstore.temails.TEmailTemplateInfo;
import com.freshdirect.fdstore.temails.TransEmailInfoModel;
import com.freshdirect.framework.core.SequenceGenerator;
import com.freshdirect.framework.mail.EmailAddress;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.mail.EnumEmailType;
import com.freshdirect.mail.EnumTEmailProviderType;
import com.freshdirect.mail.EnumTEmailStatus;
import com.freshdirect.mail.EnumTranEmailType;

public class TEmailInfoDAO {

	private static Category LOGGER = LoggerFactory.getInstance( TEmailInfoDAO.class );
	
	private static final String TEMPLATE_COUNT_SQL="SELECT COUNT(*) COUNT FROM CUST.TRANS_EMAIL_TYPES WHERE TRANS_TYPE=? AND (EMAIL_TYPE=? OR EMAIL_TYPE='ALL') and ACTIVE = 'X'";

	public static int getTEmailTemplateCount(Connection con,EnumTranEmailType tranType,EnumEmailType emailType) throws SQLException{
		
		  Connection conn = con;
		   	
		   int count=0;
		   
	       try {
	    	   PreparedStatement ps = conn.prepareStatement(TEMPLATE_COUNT_SQL);
	    	   ps.setString(1,tranType.getName());
	    	   ps.setString(2,emailType.getName());
	    	   ResultSet rs = ps.executeQuery();
	           if (rs.next()) {
	                count=rs.getInt("COUNT"); 
	           }
	           if(rs!=null) rs.close();
	           if(ps!=null) ps.close();
	       }catch(SQLException e){
	      	 throw e;
	       }
	       LOGGER.info("TEmailTemplateInfo tranType "+tranType+" count is :"+count);
		   return count; 				
	}

	
	
	
	//insert into cust.TRANS_EMAIL_TYPES (ID,  PROVIDER, TEMPLATE_ID,  TRANS_TYPE,  EMAIL_TYPE ,  DESCRIPTION ,  ACTIVE , FROM_ADDR,  SUBJECT)  values(?,?,?,?,?,?,?,?,?)
	
	private static final String TEMPLATE_SELECT_SQL="SELECT ID, TARGET_PROG_ID, PROVIDER, TEMPLATE_ID,  TRANS_TYPE,  EMAIL_TYPE ,  DESCRIPTION ,  ACTIVE , IS_PROD_READY,  FROM_ADDR,  SUBJECT FROM CUST.TRANS_EMAIL_TYPES WHERE TRANS_TYPE=? AND (EMAIL_TYPE=? OR EMAIL_TYPE='ALL') and ACTIVE = 'X'";
	
	public static TEmailTemplateInfo getTEmailTemplateInfo(Connection con,EnumTranEmailType tranType,EnumEmailType emailType) throws SQLException{
		
		  Connection conn = con;
		   TEmailTemplateInfo info=null;	
		   
	       try {
	    	   PreparedStatement ps = conn.prepareStatement(TEMPLATE_SELECT_SQL);
	    	   ps.setString(1,tranType.getName());
	    	   ps.setString(2,emailType.getName());
	    	   ResultSet rs = ps.executeQuery();
	           if (rs.next()) {
	           	 	info=new TEmailTemplateInfo();
	           	 	info.setId(rs.getString("ID"));
	           	    info.setFromAddress(rs.getString("FROM_ADDR"));
	           	    info.setTargetProgId(rs.getString("TARGET_PROG_ID"));	           	 
	           	    info.setSubject(rs.getString("SUBJECT"));	           	  
	           	    info.setEmailType(rs.getString("EMAIL_TYPE")==null?EnumEmailType.HTML:EnumEmailType.getEnum(rs.getString("EMAIL_TYPE")));
	           	    info.setActive(rs.getString("ACTIVE")==null?false:(rs.getString("ACTIVE").equalsIgnoreCase("X")));
	           	    info.setDescription(rs.getString("DESCRIPTION"));
	           	    info.setTemplateId(rs.getString("TEMPLATE_ID"));
	           	    info.setProductionReady("X".equalsIgnoreCase(rs.getString("IS_PROD_READY"))?true:false);
	           	    info.setProvider(rs.getString("PROVIDER")==null?EnumTEmailProviderType.CHEETAH:EnumTEmailProviderType.getEnum(rs.getString("PROVIDER")));
	           	    info.setTransactionType(tranType);
	           }
	           if(rs!=null) rs.close();
	           if(ps!=null) ps.close();
	       }catch(SQLException e){
	      	 throw e;
	       }
	       LOGGER.info("TEmailTemplateInfo is :"+info);
		   return info; 				
	}
	
	public static final String INSERT_TRANS_EMAIL_MASTER="INSERT INTO CUST.TRANS_EMAIL_MASTER "+ 
												   "(ID, TARGET_PROG_ID,ORDER_ID, CUSTOMER_ID, TRANS_TYPE, STATUS,EMAIL_TYPE,PROVIDER, CROMOD_DATE) "+  
												   " VALUES(?,?,?,?,?,?,?,?,SYSDATE)"; 
	
	public static void storeTransactionEmailInfo(Connection conn,TransEmailInfoModel model) throws SQLException {
		try
		{
			
			LOGGER.debug("--------------------------------Saving transaction email log");
			
		   String id = getNextId(conn, "CUST");
		   model.setId(id);		  
   	       PreparedStatement ps = conn.prepareStatement(INSERT_TRANS_EMAIL_MASTER);
   	       ps.setString(1, model.getId());
   	       ps.setString(2, model.getTargetProgId());
   	       if(model.getOrderId()!=null && model.getOrderId().trim().length()>0)
   	            ps.setString(3, model.getOrderId());
   	       else
   	    	   ps.setNull(3, Types.NULL);
   	       if(model.getCustomerId()!=null && model.getCustomerId().trim().length()>0)
   	               ps.setString(4, model.getCustomerId());
   	       else
   	    	       ps.setNull(4, Types.NULL);
   	       ps.setString(5, model.getEmailTransactionType());
   	       ps.setString(6, model.getEmailStatus());
   	       ps.setString(7, model.getEmailType());
   	       ps.setString(8, model.getProvider());
   	       int count=ps.executeUpdate();
   	       if(count==0){
   	    	   throw new SQLException("could not create TransactionEmailInfo Master ");
   	       }
   	       
   	       ps.close();
   	       
   	       storeTransactionEmailDetails(conn,model);
   	       
		}catch(SQLException e){
	      	 throw e;
	    }
	    
		    
	}
	
	
	public static final String INSERT_TRANS_EMAIL_DETAIL="INSERT INTO CUST.TRANS_EMAIL_DETAILS "+
                                                        " ( ID,  TRANS_EMAIL_ID ,  FROM_ADDR , TO_ADDR , CC_ADDR,  BCC_ADDR,  SUBJECT, TEMPLATE_CONTENT, SENT_DATE ) "+ 
                                                        " VALUES (CUST.SYSTEM_SEQ.NEXTVAL,?,?,?,?,?,?, ?, sysdate)"; 
	

	
	public static void storeTransactionEmailDetails(Connection conn,TransEmailInfoModel model) throws SQLException {
		try
		{
		   //String id = getNextId(conn, "CUST");
		   //model.setId(id);		  
			OraclePreparedStatement ps = (OraclePreparedStatement)conn.prepareStatement(INSERT_TRANS_EMAIL_DETAIL);
   	       ps.setString(1, model.getId());
   	       
   	       ps.setString(2, model.getFromAddress().getAddress());
   	       ps.setString(3, model.getRecipient());
   	       if(model.getCCList()!=null && model.getCCList().size()>0){
   	           ps.setString(4, model.getCCListInStr());
   	       }else{
   	    	   ps.setNull(4, Types.NULL);
   	       }
   	       if(model.getBCCList()!=null && model.getBCCList().size()>0){
   	           ps.setString(5, model.getBCCListInStr());
   	       }else{
   	    	   ps.setNull(5, Types.NULL);
   	       }   	       
   	       ps.setString(6, model.getSubject());
   	          	      
   	        ps.setStringForClob(7, model.getEmailContent());
   	     
   	       int count=ps.executeUpdate();
	       if(count==0){
	    	   throw new SQLException("could not create TransactionEmailInfo Master ");
	       }
	       
	       ps.close();
	    
	     /*  
	       
	       PreparedStatement stmt = conn.prepareStatement("SELECT TEMPLATE_CONTENT FROM CUST.TRANS_EMAIL_DETAILS "+
	  	         "WHERE TRANS_EMAIL_ID =? FOR UPDATE");
	       stmt.setString(1,model.getId());	       	       
	       ResultSet lobDetails = stmt.executeQuery();
	       Clob obj=null;
	       if( lobDetails.next() ) {
	    	    // Get the Blob locator and open output stream for the Blob
	    	   obj = (java.sql.Clob)lobDetails.getClob(1);
	    	   obj.setString(1, model.getEmailContent());    	   	    	   	    	   
	    	   
	       }   
	       
	       if(obj!=null){
	    	
	    	   PreparedStatement stmt1 = conn.prepareStatement("UPDATE  CUST.TRANS_EMAIL_DETAILS SET TEMPLATE_CONTENT=?  "+
	  	         "WHERE TRANS_EMAIL_ID =?");
	    	   stmt1.setClob(1, obj);
	    	   stmt1.setString(2,model.getId());	  
	    	   int count1=stmt1.executeUpdate();
		       if(count1==0){
		    	   throw new SQLException("could not create TransactionEmailInfo Master ");
		       }
		       
		       //stmt1.close();
	       }
	       */	     
	      	       
		}catch(SQLException e){
	      	 throw e;
	    }
	       
		    
	}
	
	
	  /** gets the next unique Id to use when writing a persistent object
   * to a persistent store for the first time
   * @param conn a SQLConnection to use to find an Id
   * @throws SQLException any problems while getting a unique id
   * @return an id that uniquely identifies a persistent object
   */    
  protected static String getNextId(Connection conn, String schema) throws SQLException {
		return SequenceGenerator.getNextId(conn, schema);
  }
  
  
  
  
  public static List getFailedTransactions(Connection conn) throws SQLException{
	 return getFailedTransactions(conn,999,true);  
  }
  

	public static final String GET_ALL_FAILED_TRANS_MAIL_SQL= "select  M.ID,   M.TARGET_PROG_ID,   (SELECT IS_PROD_READY FROM cust.TRANS_EMAIL_TYPES WHERE TARGET_PROG_ID =M.TARGET_PROG_ID and rownum<1)  IS_PROD_READY,  M.ORDER_ID, M.CUSTOMER_ID, M.TRANS_TYPE, M.STATUS,EMAIL_TYPE,M.PROVIDER, M.CROMOD_DATE, "+  
															" D.TRANS_EMAIL_ID ,  D.FROM_ADDR , D.TO_ADDR , D.CC_ADDR,  D.BCC_ADDR,  D.SUBJECT, D.TEMPLATE_CONTENT "+ 
															" from cust.TRANS_EMAIL_MASTER M,CUST.TRANS_EMAIL_DETAILS D where "+  
															" M.ID=D.TRANS_EMAIL_ID and "+ 
															" M.status='FLD' and rownum<? order by M.CROMOD_DATE DESC ";
	
	
	
	 public static List getFailedTransactions(Connection conn, int max_count,boolean isEmailContentReqd) throws SQLException{
		 
		
		   List failedTransList=new ArrayList();	
		   if(max_count==0) max_count=999;
	       try {
	    	   PreparedStatement ps = conn.prepareStatement(GET_ALL_FAILED_TRANS_MAIL_SQL);
	    	   
	    	   ps.setInt(1, max_count);
	    	   ResultSet rs = ps.executeQuery();
	           while (rs.next()) {
	        	   TransEmailInfoModel model=new TransEmailInfoModel();
	           	 
	           	 	model.setId(rs.getString("ID"));
	           	    model.setTargetProgId(rs.getString("TARGET_PROG_ID"));
	           	    model.setOrderId(rs.getString("ORDER_ID"));
	           	    model.setProductionReady("X".equalsIgnoreCase(rs.getString("IS_PROD_READY"))?true:false);
	           	    model.setCustomerId(rs.getString("CUSTOMER_ID"));
	           	    model.setEmailTransactionType(EnumTranEmailType.getEnum(rs.getString("TRANS_TYPE")));
	           	    model.setEmailStatus(EnumTEmailStatus.getEnum(rs.getString("STATUS")));
	           	    model.setEmailType(EnumEmailType.getEnum(rs.getString("EMAIL_TYPE")));
	           	    model.setProvider(EnumTEmailProviderType.getEnum(rs.getString("PROVIDER")));
	           	    model.setFromAddress(new EmailAddress(null,rs.getString("FROM_ADDR")));
	           	    model.setRecipient(rs.getString("TO_ADDR"));
	           	    model.setSubject(rs.getString("SUBJECT"));
	    			java.util.Date startDate = new java.util.Date(rs.getTimestamp("CROMOD_DATE").getTime());
	           	    model.setCroModDate(startDate);	           	   	           	    
	           	    model.setEmailContent(rs.getString("TEMPLATE_CONTENT"));
	           	    model.setCCListInStr(rs.getString("CC_ADDR"));
	           	    model.setBCCListInStr(rs.getString("BCC_ADDR"));
	           	    
	           	    if(isEmailContentReqd)
	           	    {
		           	    Clob clob = rs.getClob("TEMPLATE_CONTENT");
			    	   try{
			    	      		    				    		  
			    		   Reader clobStream = clob.getCharacterStream();		    		 
			    		   StringBuffer contents = new StringBuffer();
			    		   int nchars = 0; 
			    		   char[] buffer = new char[10];
			    		   while((nchars = clobStream.read(buffer)) != -1) 
			    			   contents.append(buffer, 0, nchars); 
			    		   clobStream.close();
			    		   model.setEmailContent(contents.toString());
			    		   
			    	   }catch(IOException e){e.printStackTrace();throw new SQLException(e);}
	           	    }
	           	    failedTransList.add(model);
	           	    	           	    
	           }
	           ps.close();
	       }catch(SQLException e){
	      	 throw e;
	       }
	       LOGGER.info("failedTransList size is :"+failedTransList.size());
		   return failedTransList; 				
		 
	 }
  
	 
	 public static final String GET_FAILED_TRANS_MAIL_SQL=" select count(m.id) count, m.trans_type "+  
	 													" from cust.TRANS_EMAIL_MASTER M,CUST.TRANS_EMAIL_DETAILS D where "+
	 													" M.ID=D.TRANS_EMAIL_ID and M.status='FLD' group by m.trans_type"; 
		



	public static Map getFailedTransactionsDetails(Connection conn) throws SQLException{
				
				Map transMap=new HashMap();
				int totalCount=0;
				try {
				PreparedStatement ps = conn.prepareStatement(GET_FAILED_TRANS_MAIL_SQL);
							
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
				
				   int count=rs.getInt("count");
				   totalCount=totalCount+count;
				   transMap.put(rs.getString("trans_type"),""+count);
				
				}
				transMap.put("total_count",""+totalCount);
				
				
				ps.close();
				}catch(SQLException e){
					throw e;
				}
				LOGGER.info("failedTransList size is :"+transMap.size());
			return transMap; 				
				
		}
				  
}
