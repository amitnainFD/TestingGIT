package com.freshdirect.fdstore.customer.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.freshdirect.giftcard.EnumGCDeliveryMode;
import com.freshdirect.giftcard.EnumGiftCardType;
import com.freshdirect.giftcard.RecipientModel;
import com.freshdirect.fdstore.customer.SavedRecipientModel;
import com.freshdirect.framework.core.SequenceGenerator;

public class SavedRecipientDAO {
	
	private static String INSERT_SAVED_RECIPIENT =
		"INSERT INTO CUST.SAVED_RECIPIENT( "+
		"ID,FDUSER_ID,SENDER_NAME,SENDER_EMAIL,RECIP_NAME,RECIP_EMAIL,TEMPLATE_ID,DELIVERY_MODE,AMOUNT,PERSONAL_MSG,DONOR_ORGNAME,GIFTCARD_TYPE)"+ 
	    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
	
	private static String nvl(String str) {
		return (str == null) ? "" : str;
	}
	
	public static void storeSavedRecipients(Connection conn, String fdUserId, List<? extends RecipientModel> recipientList) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("DELETE FROM CUST.SAVED_RECIPIENT WHERE FDUSER_ID = ?");
		ps.setString(1, fdUserId);
		ps.executeUpdate();
		ps.close();

		ps = conn.prepareStatement(INSERT_SAVED_RECIPIENT);

		for(int i=0;i<recipientList.size();i++)
		{
			RecipientModel model = recipientList.get(i);
			
			String id=SequenceGenerator.getNextId(conn, "CUST");
			
			ps.setString(1, id);
			ps.setString(2, nvl(fdUserId));
			ps.setString(3, nvl(model.getSenderName()));
			ps.setString(4, nvl(model.getSenderEmail()));
			ps.setString(5, nvl(model.getRecipientName()));
			ps.setString(6, nvl(model.getRecipientEmail()));
			ps.setString(7, nvl(model.getTemplateId()));			
			ps.setString(8, nvl(model.getDeliveryMode().getName()));
			//ps.setDouble(9, model.getAmount());
			ps.setBigDecimal(9, new java.math.BigDecimal(model.getAmount()));
			ps.setString(10, nvl(model.getPersonalMessage()));
			ps.setString(11, nvl(model.getDonorOrganizationName()));
			ps.setString(12, nvl(model.getGiftCardType().getName()));		
			ps.addBatch();
		} 
			
		ps.executeBatch();
		ps.close();
	}
	
	public static void storeSavedRecipient(Connection conn, String fdUserId, SavedRecipientModel model) throws SQLException {
		PreparedStatement ps = 
			conn.prepareStatement(INSERT_SAVED_RECIPIENT);
			
			String id=SequenceGenerator.getNextId(conn, "CUST");
			
			ps.setString(1, id);
			ps.setString(2, nvl(fdUserId));
			ps.setString(3, nvl(model.getSenderName()));
			ps.setString(4, nvl(model.getSenderEmail()));
			ps.setString(5, nvl(model.getRecipientName()));
			ps.setString(6, nvl(model.getRecipientEmail()));
			ps.setString(7, nvl(model.getTemplateId()));
			ps.setString(8, nvl(model.getDeliveryMode().getName()));
			//ps.setDouble(9, model.getAmount());
			ps.setBigDecimal(9, new java.math.BigDecimal(model.getAmount()));
			ps.setString(10, nvl(model.getPersonalMessage()));
			ps.setString(11, nvl(model.getDonorOrganizationName()));
			ps.setString(12, nvl(model.getGiftCardType().getName()));
			ps.execute();
			
			ps.close();
	}
	
	public static void updateSavedRecipient(Connection conn, String fdUserId, SavedRecipientModel model) throws SQLException {
		PreparedStatement ps = 
			conn.prepareStatement("update cust.SAVED_RECIPIENT set FDUSER_ID=?,SENDER_NAME=?,SENDER_EMAIL=?,RECIP_NAME=?," +
					"RECIP_EMAIL=?,TEMPLATE_ID=?,DELIVERY_MODE=?,AMOUNT=?,PERSONAL_MSG=?,GIFTCARD_TYPE=?,DONOR_ORGNAME=? " +
					"WHERE ID=?");
			
			ps.setString(1, nvl(fdUserId));System.out.println("fdUserId = " + fdUserId);
			ps.setString(2, nvl(model.getSenderName()));
			ps.setString(3, nvl(model.getSenderEmail()));
			ps.setString(4, nvl(model.getRecipientName()));
			ps.setString(5, nvl(model.getRecipientEmail()));
			ps.setString(6, nvl(model.getTemplateId()));
			ps.setString(7, nvl(model.getDeliveryMode().getName()));
			//ps.setDouble(8, model.getAmount());
			ps.setBigDecimal(8, new java.math.BigDecimal(model.getAmount()));
			ps.setString(9, nvl(model.getPersonalMessage()));
			ps.setString(10, nvl(model.getGiftCardType().getName()));
			ps.setString(11, nvl(model.getDonorOrganizationName()));
			ps.setString(12, model.getPK().getId());
			
			
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not updated");
			}
			ps.close();
	}
	
	
	public static void deleteSavedRecipients(Connection conn, String fdUserId, String giftcardType) throws SQLException {
		PreparedStatement ps = 
			conn.prepareStatement("delete from cust.SAVED_RECIPIENT where FDUSER_ID=? and GIFTCARD_TYPE=?");
			
			ps.setString(1, fdUserId);
			ps.setString(2, giftcardType);
			ps.execute();
			ps.close();
	}
	
	public static void deleteSavedRecipient(Connection conn, String savedRecipientId) throws SQLException {
		PreparedStatement ps = 
			conn.prepareStatement("delete from cust.SAVED_RECIPIENT where ID=?");
			
			ps.setString(1, savedRecipientId);
			ps.execute();
			ps.close();
	}
	
	private static final String SELECT_RECIPIENTS_SQL="select ID,FDUSER_ID,SENDER_NAME,SENDER_EMAIL,RECIP_NAME,RECIP_EMAIL,TEMPLATE_ID,DELIVERY_MODE,AMOUNT,PERSONAL_MSG,DONOR_ORGNAME,GIFTCARD_TYPE from cust.SAVED_RECIPIENT where FDUSER_ID=?";
	
	public static List<SavedRecipientModel> loadSavedRecipients(Connection conn, String fdUserId) throws SQLException{
		List<SavedRecipientModel> recipientList = new ArrayList<SavedRecipientModel>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = conn.prepareStatement(SELECT_RECIPIENTS_SQL);
			ps.setString(1,fdUserId);
			rs = ps.executeQuery();
			while (rs.next()) {
				SavedRecipientModel model=new SavedRecipientModel();
				
	            //ErpComplaintReason ecr = new ErpComplaintReason(rs.getString(1), rs.getString(2),rs.getString(3), rs.getString(4));
	            //List reasons = (List) results.get(ecr.getDepartmentCode());
	            model.setId(rs.getString("ID"));
	            model.setSenderName(rs.getString("SENDER_NAME"));
	            model.setSenderEmail(rs.getString("SENDER_EMAIL"));
	            model.setRecipientName(rs.getString("RECIP_NAME"));
	            model.setRecipientEmail(rs.getString("RECIP_EMAIL"));
	            model.setFdUserId(fdUserId);
	            model.setTemplateId(rs.getString("TEMPLATE_ID"));
	            model.setDeliveryMode(EnumGCDeliveryMode.getEnum(rs.getString("DELIVERY_MODE")));
	            model.setPersonalMessage(rs.getString("PERSONAL_MSG"));
	            model.setAmount(rs.getDouble("AMOUNT"));
	            model.setDonorOrganizationName(rs.getString("DONOR_ORGNAME"));
	            model.setGiftCardType(EnumGiftCardType.getEnum(rs.getString("GIFTCARD_TYPE")));
	            recipientList.add(model);                                        
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(ps!=null)
				ps.close();
		}
		
		return recipientList;
	}

}
