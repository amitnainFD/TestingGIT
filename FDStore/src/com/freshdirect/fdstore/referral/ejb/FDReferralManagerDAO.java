/*
 * Created on Jun 2, 2005
 *
 */
package com.freshdirect.fdstore.referral.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;

import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.fdstore.referral.EnumReferralProgramStatus;
import com.freshdirect.fdstore.referral.EnumReferralStatus;
import com.freshdirect.fdstore.referral.FDReferralProgramModel;
import com.freshdirect.fdstore.referral.FDReferralReportLine;
import com.freshdirect.fdstore.referral.ReferralCampaign;
import com.freshdirect.fdstore.referral.ReferralChannel;
import com.freshdirect.fdstore.referral.ReferralHistory;
import com.freshdirect.fdstore.referral.ReferralObjective;
import com.freshdirect.fdstore.referral.ReferralPartner;
import com.freshdirect.fdstore.referral.ReferralProgram;
import com.freshdirect.fdstore.referral.ReferralProgramInvitaionModel;
import com.freshdirect.fdstore.referral.ReferralSearchCriteria;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.core.SequenceGenerator;
import com.freshdirect.framework.util.log.LoggerFactory;

/**
 * @author gopal
 *
 */
public class FDReferralManagerDAO {
	private static final Category LOGGER = LoggerFactory.getInstance(FDReferralManagerDAO.class);
    public static PrimaryKey createReferralChannel(Connection conn, ReferralChannel channel) throws SQLException {
		
		PreparedStatement ps = null;
		String id = null;
		
		try {
			id = SequenceGenerator.getNextId(conn, "CUST");
			ps = conn.prepareStatement("INSERT INTO CUST.REF_CHANNEL (ID, CHANNEL_NAME, CHANNEL_TYPE, CHANNEL_DESC) values (?,?,?,?)");
			int index = 1;
			ps.setString(index++, id);
			ps.setString(index++, channel.getName());
			ps.setString(index++, channel.getType());
			if(channel.getDescription()==null){
			   ps.setNull(index++,Types.VARCHAR);
			}
			else{	
			   ps.setString(index++, channel.getDescription());
			}
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");
			}
		} finally {
			if (ps != null) ps.close();			
		}
				
		PrimaryKey pk = new PrimaryKey(id);
		channel.setPK(pk);
		return pk;
	}
	

	public static PrimaryKey createReferralObjective(Connection conn, ReferralObjective objective) throws SQLException {
		
		PreparedStatement ps = null;
		String id = null;

		
		try {
			id = SequenceGenerator.getNextId(conn, "CUST");
			ps = conn.prepareStatement("INSERT INTO CUST.REF_OBJECTIVE (ID, OBJECTIVE_NAME, OBJECTIVE_DESC) values (?,?,?)");
			int index = 1;
			ps.setString(index++, id);
			ps.setString(index++, objective.getName());
			
			if(objective.getDescription()==null){
			   ps.setNull(index++,Types.VARCHAR);
			}
			else{	
			   ps.setString(index++, objective.getDescription());
			}						
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");
			}
		} finally {
			if (ps != null) ps.close();			
		}
				
		PrimaryKey pk = new PrimaryKey(id);
		objective.setPK(pk);
		return pk;
	}


	public static PrimaryKey createReferralpartner(Connection conn, ReferralPartner partner) throws SQLException {
		
		PreparedStatement ps = null;
		String id = null;

		
		try {
			id = SequenceGenerator.getNextId(conn, "CUST");
			ps = conn.prepareStatement("INSERT INTO CUST.REF_PARTNER (ID, PARTNER_NAME, PARTNER_DESC) values (?,?,?)");
			int index = 1;
			ps.setString(index++, id);
			ps.setString(index++, partner.getName());
			
			if(partner.getDescription()==null){
			   ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, partner.getDescription());
			}			
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");
			}
		} finally {
			if (ps != null) ps.close();			
		}
				
		PrimaryKey pk = new PrimaryKey(id);
		partner.setPK(pk);
		return pk;
	}

	
	public static PrimaryKey createReferralCampaign(Connection conn, ReferralCampaign campaign) throws SQLException {
		
		PreparedStatement ps = null;
		String id = null;
		
		try {
			id = SequenceGenerator.getNextId(conn, "CUST");
			ps = conn.prepareStatement("INSERT INTO CUST.REF_CAMPAIGN (ID, CAMPAIGN_NAME, CAMPAIGN_DESC, REF_OBJ_ID) values (?,?,?,?)");
			int index = 1;
			ps.setString(index++, id);
			ps.setString(index++, campaign.getName());
			
			if(campaign.getDescription()==null){
				ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, campaign.getDescription());
			}						
			ps.setString(index++, campaign.getObjective().getPK().getId());
			
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");			
			}										
						
		} finally {
			if (ps != null) ps.close();			
		}
				
		PrimaryKey pk = new PrimaryKey(id);
		campaign.setPK(pk);
		return pk;
	}
	
	
	public static PrimaryKey createReferralProgram(Connection conn, ReferralProgram program) throws SQLException {		
		PreparedStatement ps = null;
		String id = null;		
		try {
			id = SequenceGenerator.getNextId(conn, "CUST");
            ps = conn.prepareStatement("INSERT INTO CUST.REF_PROGRAM (ID, PROGRAM_NAME, PROGRAM_DESC, PROGRAM_START_DATE, PROGRAM_EXP_DATE, CREATIVE_DESC, STATUS, REF_CHANNEL_ID, REF_PARTNER_ID, REF_CAMPAIGN_ID, REF_PROMOTION_CODE, CREATIVE_URL) values (?,?,?,?,?,?,?,?,?,?,?,?)");
			int index = 1;
			ps.setString(index++, id);
			ps.setString(index++, program.getName());
			
			if(program.getDescription()==null){
				ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, program.getDescription());									
			}
			ps.setDate(index++, new java.sql.Date(program.getStartDate().getTime()));
			ps.setDate(index++, new java.sql.Date(program.getExpDate().getTime()));
			
			if( program.getCreativeDesc()==null){
				ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, program.getCreativeDesc());												
			}
			ps.setString(index++, program.getStatus().getName());			
			ps.setString(index++, program.getChannel().getPK().getId());			
			ps.setString(index++, program.getPartner().getPK().getId());			
			ps.setString(index++, program.getCampaign().getPK().getId());			
			if( program.getPromotionCode()==null){
				ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, program.getPromotionCode());
			}
			if( program.getCreativeUrl()==null){
				ps.setNull(index++,Types.VARCHAR);
			}	
			else{	
				ps.setString(index++, program.getCreativeUrl());
			}						
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");			
			}								
						
		} finally {
			if (ps != null) ps.close();			
		}
				
		PrimaryKey pk = new PrimaryKey(id);
		program.setPK(pk);
		return pk;
	}
	
	
	public static PrimaryKey createReferralHistory(Connection conn, ReferralHistory history) throws SQLException {
		
		PreparedStatement ps = null;
		String id = null;		
		try {
			id = SequenceGenerator.getNextId(conn, "CUST");
			ps = conn.prepareStatement("INSERT INTO CUST.REF_HISTORY (ID, REF_PROG_ID, REF_PROG_INVT_ID, DATE_CREATED, FD_USERID, REF_TRK_KEY_DTLS) values (?,?,?,?,?,?)");
			int index = 1;
			ps.setString(index++, id);
			
	        ps.setString(index++, history.getReferralProgramId());

			if( history.getRefprgInvtId()==null){
				ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, history.getRefprgInvtId());												
			}			
			ps.setTimestamp(index++,  new Timestamp(history.getDateCreated().getTime()));//Original Expiration Date.
			ps.setString(index++, history.getFdUserId());
			
			if( history.getRefTrkKeyDtls()==null){
				ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, history.getRefTrkKeyDtls());												
			}						
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");			
			}	
										
		} finally {
			if (ps != null) ps.close();			
		}
				
		PrimaryKey pk = new PrimaryKey(id);
		history.setPK(pk);
		return pk;
	}
	
	
	public static PrimaryKey createReferral(Connection conn, ReferralProgramInvitaionModel model) throws SQLException {
		
		PreparedStatement ps = null;
		String id = null;
		
		try {
            LOGGER.debug("creating the referral in DAO");
		 	id = SequenceGenerator.getNextId(conn, "CUST");
			ps = conn.prepareStatement("INSERT INTO CUST.REF_PROG_INVITATION (ID, REFERRER_CUST_ID, REFERRAL_DATE, STATUS, REFERRAL_NAME, REFERRAL_EMAIL_ADDR, REF_PROG_ID, MODIFIED_DATE) values (?,?,?,?,?,?,?,?)");
			int index = 1;
			ps.setString(index++, id);			
	        ps.setString(index++, model.getReferrerCustomerId());
	        ps.setTimestamp(index++, new Timestamp(model.getReferralCreatedDate().getTime()));
	        ps.setString(index++, model.getStatus().getName());
	        ps.setString(index++, model.getReferralName());
			ps.setString(index++, model.getReferrelEmailAddress());			
			ps.setString(index++, model.getReferralProgramId());	
			if(model.getReferralModifiedDate()!=null){
			   ps.setTimestamp(index++, new Timestamp(model.getReferralModifiedDate().getTime()));
			}
			else{
			   ps.setNull(index++, Types.TIMESTAMP);							
			}
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");			
			}	
			LOGGER.debug("referral created in DAO");
										
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally {
		
			if (ps != null) ps.close();			
		}
				
		PrimaryKey pk = new PrimaryKey(id);
		model.setPK(pk);
		return pk;
	}
		
	
	public static void updateReferral(Connection conn, ReferralProgramInvitaionModel model) throws SQLException {
		
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("UPDATE CUST.REF_PROG_INVITATION SET REFERRER_CUST_ID = ?, REFERRAL_DATE = ?, STATUS = ?, REFERRAL_NAME = ?, REFERRAL_EMAIL_ADDR=?, REF_PROG_ID=?, MODIFIED_DATE=?  WHERE ID = ?");
			int index = 1;
		    ps.setString(index++, model.getReferrerCustomerId());
	        ps.setDate(index++, new java.sql.Date(model.getReferralCreatedDate().getTime()));
	        ps.setString(index++, model.getStatus().getName());
	        ps.setString(index++, model.getReferralName());
			ps.setString(index++, model.getReferrelEmailAddress());
			ps.setString(index++, model.getReferralProgramId());
			ps.setDate(index++, new java.sql.Date(model.getReferralModifiedDate().getTime()));			
			
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not updated");
			}
		} finally {
			if (ps != null) ps.close();
		}	

	}
	
	
	public static void updateReferralStatus(Connection conn, String referralId,String status) throws SQLException {
		
		PreparedStatement ps = null;
		try {
			
			int index = 1;
			ps = conn.prepareStatement("UPDATE CUST.REF_PROG_INVITATION SET STATUS = ?, MODIFIED_DATE=SYSDATE  WHERE ID = ?");			
			ps.setString(index++,status );
			ps.setString(index++, referralId);		    
	       			
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not updated");
			}
		} finally {
			if (ps != null) ps.close();
		}	
	}
	
	
	public static void updateReferralChannel(Connection conn, ReferralChannel channel) throws SQLException {
		
		PreparedStatement ps = null;
		String id = null;		
		try {
			
			int index = 1;
			ps = conn.prepareStatement("UPDATE CUST.REF_CHANNEL SET CHANNEL_NAME=?, CHANNEL_TYPE=?, CHANNEL_DESC=? WHERE ID=? ");					
			ps.setString(index++, channel.getName());
			ps.setString(index++, channel.getType());
			
			if( channel.getDescription()==null){
				ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, channel.getDescription());					
			}			
			ps.setString(index++, channel.getPK().getId());

			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");
			}
		} finally {
			if (ps != null) ps.close();			
		}				
	}

	
	public static void updateReferralPartner(Connection conn, ReferralPartner partner) throws SQLException {
		
		PreparedStatement ps = null;				
		try {
			
			int index = 1;
			ps = conn.prepareStatement("UPDATE CUST.REF_PARTNER SET PARTNER_NAME=?, PARTNER_DESC=? WHERE ID=? ");					
			ps.setString(index++, partner.getName());
			
			if( partner.getDescription()==null){
				ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, partner.getDescription());											
			}
			ps.setString(index++, partner.getPK().getId());
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");
			}
		} finally {
			if (ps != null) ps.close();			
		}
				
	}
	

	public static void updateReferralObjective(Connection conn, ReferralObjective objective) throws SQLException {
		
		PreparedStatement ps = null;		
		try {
			
			int index = 1;
			ps = conn.prepareStatement("UPDATE CUST.REF_OBJECTIVE SET OBJECTIVE_NAME=?, OBJECTIVE_DESC=? WHERE ID=? ");					
			ps.setString(index++, objective.getName());
			
			if( objective.getDescription()==null){
				ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, objective.getDescription());
			}					
			ps.setString(index++, objective.getPK().getId());

			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");
			}
		} finally {
			if (ps != null) ps.close();			
		}
				
	}

	
	public static void updateReferralCampaign(Connection conn, ReferralCampaign campaign) throws SQLException {
		
		PreparedStatement ps = null;			
		try {
			
			int index = 1;
			ps = conn.prepareStatement("UPDATE CUST.REF_CAMPAIGN SET CAMPAIGN_NAME=?, CAMPAIGN_DESC=?, REF_OBJ_ID=? WHERE ID=? ");					
			ps.setString(index++, campaign.getName());		
			
			if( campaign.getDescription()==null){
				ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, campaign.getDescription());											
			}			
			ps.setString(index++, campaign.getObjective().getPK().getId());
			ps.setString(index++, campaign.getPK().getId());

			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");
			}
		} finally {
			if (ps != null) ps.close();			
		}				
	}
	
	
	public static void updateReferralProgram(Connection conn, ReferralProgram program) throws SQLException {
		
		PreparedStatement ps = null;			
		try {
			
			int index = 1;
			ps = conn.prepareStatement("UPDATE CUST.REF_PROGRAM SET PROGRAM_NAME=?, PROGRAM_DESC=?, PROGRAM_START_DATE=?, PROGRAM_EXP_DATE=?, CREATIVE_DESC=?, STATUS=?,  REF_CHANNEL_ID=?,  REF_PARTNER_ID=?, REF_CAMPAIGN_ID=?, REF_PROMOTION_CODE=?, CREATIVE_URL=? WHERE ID=? ");					
			
			ps.setString(index++, program.getName());
			
			if( program.getDescription()==null){
				ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, program.getDescription());														
			}
			ps.setDate(index++, new java.sql.Date(program.getStartDate().getTime()));
			ps.setDate(index++, new java.sql.Date(program.getExpDate().getTime()));
			
			if( program.getCreativeDesc()==null){
				ps.setNull(index++,Types.VARCHAR);
			}
			else{	
				ps.setString(index++, program.getCreativeDesc());																	
			}
			ps.setString(index++, program.getStatus().getName());

			if(program.getChannel()!=null && program.getChannel().getPK()!=null){
					ps.setString(index++, program.getChannel().getPK().getId());
			}

			if(program.getPartner()!=null && program.getPartner().getPK()!=null){
					ps.setString(index++, program.getPartner().getPK().getId());
			}
			
			if(program.getCampaign()!=null && program.getCampaign().getPK()!=null){
					ps.setString(index++, program.getCampaign().getPK().getId());
			}
			
			if(program.getPromotionCode() !=null && program.getPromotionCode()!=null){
					ps.setString(index++, program.getPromotionCode());
			}

			if(program.getCreativeUrl() !=null && program.getCreativeUrl()!=null){
					ps.setString(index++, program.getCreativeUrl());
			}			

			ps.setString(index++, program.getPK().getId());
						
			
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");
			}
		} finally {
			if (ps != null) ps.close();			
		}				
	}
	
	
	public static void deleteReferralChannel(Connection conn, String refChaIds[]) throws SQLException {								
		PreparedStatement ps = null;			
		try {						
			
			if(refChaIds!=null && refChaIds.length>0)
			{							
				ps = conn.prepareStatement("DELETE FROM CUST.REF_CHANNEL WHERE ID=?");
				for(int i=0;i<refChaIds.length;i++){
					int index = 1;
					ps.setString(index++, refChaIds[i]);
					ps.addBatch();
				}
				int rows[]=ps.executeBatch();
				
				if (rows.length<=0) {
					throw new SQLException("Row not deleted");
				}				
			}									

		} finally {
			if (ps != null) ps.close();			
		}														
	}

		
	public static void deleteReferralObjective(Connection conn, String refObjIds[]) throws SQLException {		
		PreparedStatement ps = null;
		String id = null;				
		try {						
			
			if(refObjIds!=null && refObjIds.length>0)
			{				
				ps = conn.prepareStatement("DELETE FROM CUST.REF_OBJECTIVE WHERE ID=?");
				for(int i=0;i<refObjIds.length;i++){
					int index = 1;
					ps.setString(index++, refObjIds[i]);
					ps.addBatch();
				}
				int rows[]=ps.executeBatch();				
				if (rows.length<=0) {
					throw new SQLException("Row not deleted");
				}				
			}									

		} finally {
			if (ps != null) ps.close();			
		}										
	}

	


	public static void deleteReferralPartner(Connection conn, String refPartIds[]) throws SQLException {		
		PreparedStatement ps = null;
		String id = null;				
		try {						
			
			if(refPartIds!=null && refPartIds.length>0){				
				ps = conn.prepareStatement("DELETE FROM CUST.REF_PARTNER WHERE ID=?");
				for(int i=0;i<refPartIds.length;i++){
					int index = 1;
					ps.setString(index++, refPartIds[i]);
					ps.addBatch();
				}
				int rows[]=ps.executeBatch();
				if (rows.length<=0) {
					throw new SQLException("Row not deleted");
				}				
			}									

		} finally {
			if (ps != null) ps.close();			
		}										
	}

	
	
	public static void deleteReferralCampaign(Connection conn, String refCampIds[]) throws SQLException {
				
		PreparedStatement ps = null;
		String id = null;				
		try {						
			
			if(refCampIds!=null && refCampIds.length>0)	{				
				ps = conn.prepareStatement("DELETE FROM CUST.REF_CAMPAIGN WHERE ID=?");
				for(int i=0;i<refCampIds.length;i++){
					int index = 1;
					ps.setString(index++, refCampIds[i]);
					ps.addBatch();
				}
				int rows[]=ps.executeBatch();
				if (rows.length<=0) {
					throw new SQLException("Row not deleted");
				}				
			}									

		} finally {
			if (ps != null) ps.close();			
		}										
	}


	public static void deleteReferralProgram(Connection conn, String refProgIds[]) throws SQLException {
		
		PreparedStatement ps = null;
		String id = null;				
		try {						
			
			if(refProgIds!=null && refProgIds.length>0){			
				ps = conn.prepareStatement("DELETE FROM CUST.REF_PROGRAM WHERE ID=?");
				for(int i=0;i<refProgIds.length;i++){
					int index = 1;
					ps.setString(index++, refProgIds[i]);
					ps.addBatch();
				}
				int rows[]=ps.executeBatch();
				if (rows.length<=0) {
					throw new SQLException("Row not deleted");
				}				
			}									

		} finally {
			if (ps != null) ps.close();			
		}				
	}

	
	private static final String LOAD_REFERRAL_QUERY = 
		"select ID, REFERRER_CUST_ID,REFERRAL_DATE,STATUS,REFERRAL_NAME,REFERRAL_EMAIL_ADDR,REF_PROG_ID,MODIFIED_DATE   FROM CUST.REF_PROG_INVITATION";
	
	public static ReferralProgramInvitaionModel loadReferralFromPK(Connection conn, PrimaryKey pk) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = LOAD_REFERRAL_QUERY + " WHERE r.id = ?";
		ReferralProgramInvitaionModel referral = null;
		try {			
			ps = conn.prepareStatement(sql);
			ps.setString(1, pk.getId());
			rs = ps.executeQuery();
			if (rs.next()) { 
				List list = loadReferralFromResultSet(rs);
				if (list == null || list.size()==0) {
					throw new SQLException("Referral not found.  PK = " + pk.getId());
				}
				referral=(ReferralProgramInvitaionModel)list.get(0);				
			}
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referral;
	}

	public static List loadReferralsFromReferrerCustomerId(Connection conn, String referrerCustomerId, java.util.Date startDate) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String sql = LOAD_REFERRAL_QUERY + " WHERE REFERRER_CUST_ID = ? AND STATUS <> ?";
		
		if (startDate != null) {
			sql += " AND REFERRAL_DATE >= ?";
		}
		sql += " ORDER BY REFERRAL_DATE";
		

		List referralList = new ArrayList();
		try {			
			ps = conn.prepareStatement(sql);
			int index = 1;
			ps.setString(index++, referrerCustomerId);
			ps.setString(index++, EnumReferralStatus.DELETED.getName());
			if (startDate != null) { 
				ps.setDate(index++, new java.sql.Date(startDate.getTime()));
			}
			rs = ps.executeQuery();
			referralList = loadReferralFromResultSet(rs);						
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referralList;
	}
	
	

	private static final String LOAD_REFERRAL_FROM_REF_EMAIL_ADDR_QUERY = 
		"SELECT DISTINCT r.id, r.referrer_cust_id, r.referral_date,r.status, r.referral_prog_id" +
		",c.user_id, ci.first_name, ci.last_name" +
		" FROM CUST.REFERRAL_LOG rl, CUST.REFERRAL r, CUST.CUSTOMER c, CUST.CUSTOMERINFO ci" +
		" WHERE rl.referral_id=r.id AND r.referrer_cust_id=c.id AND c.id=ci.customer_id" +
		" AND rl.referral_email_addr = ? AND r.status <> ? ORDER BY r.referral_date";
	
	public static List loadReferralsFromReferralEmailAddress(Connection conn, String referralEmailAddress) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = LOAD_REFERRAL_QUERY+" WHERE REFERRAL_EMAIL_ADDR = ? AND STATUS <> ?";
		sql += " ORDER BY REFERRAL_DATE";
		
		List referralList = new ArrayList();
		try {			
			ps = conn.prepareStatement(sql);
			int index = 1;
			ps.setString(index++, referralEmailAddress);
			ps.setString(index++, EnumReferralStatus.DELETED.getName());
			//ps.setString(index++, EnumReferralActionType.NONE.getName());
			rs = ps.executeQuery();
			referralList = loadReferralFromResultSet(rs);
				
			if (referralList == null || referralList.size()==0) {
					throw new SQLException("Referrals not found.  Referral Email Address = " + referralEmailAddress);
			}
				
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referralList;
	}


	public static List loadReferralsFromReferralProgramId(Connection conn, String referralProgramId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String sql = LOAD_REFERRAL_QUERY+" WHERE REF_PROG_ID = ? AND STATUS <> ?";
		sql += " ORDER BY REFERRAL_DATE";
						
		List referralList = new ArrayList();
		try {			
			ps = conn.prepareStatement(sql);
			ps.setString(1, referralProgramId);
			ps.setString(2, EnumReferralStatus.DELETED.getName());
			rs = ps.executeQuery();

			referralList = loadReferralFromResultSet(rs);
			if (referralList == null || referralList.size() == 0) {
					throw new SQLException("Referral not found.  Referral Program Id = " + referralProgramId);
			}
			

		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referralList;
	}

	

	private static final String LOAD_REFERRAL_PROGRAM_QUERY =	 "SELECT   P.ID ID,    P.PROGRAM_NAME PROGRAM_NAME, P.PROGRAM_DESC PROGRAM_DESC,P.PROGRAM_START_DATE PROGRAM_START_DATE,"+ 
	                                                             " P.PROGRAM_EXP_DATE PROGRAM_EXP_DATE, P.CREATIVE_DESC CREATIVE_DESC,P.STATUS STATUS,P.CREATIVE_URL CREATIVE_URL, P.REF_CHANNEL_ID REF_CHANNEL_ID, P.REF_CAMPAIGN_ID REF_CAMPAIGN_ID,"+ 
	                                                             " P.REF_PARTNER_ID REF_PARTNER_ID,P.REF_PROMOTION_CODE REF_PROMOTION_CODE, C.CHANNEL_NAME CHANNEL_NAME, C.CHANNEL_TYPE CHANNEL_TYPE,C.CHANNEL_DESC CHANNEL_DESC,"+ 
	                                                             " PART.PARTNER_NAME PARTNER_NAME, PART.PARTNER_DESC PARTNER_DESC,CAMP.CAMPAIGN_NAME CAMPAIGN_NAME, CAMP.CAMPAIGN_DESC CAMPAIGN_DESC,"+ 
	                                                             " OBJ.ID OBJ_ID,OBJ.OBJECTIVE_NAME OBJECTIVE_NAME, OBJ.OBJECTIVE_DESC OBJECTIVE_DESC"+ 
	                                                             " FROM CUST.REF_PROGRAM P,CUST.REF_CHANNEL C,CUST.REF_PARTNER PART,CUST.REF_CAMPAIGN CAMP,CUST.REF_OBJECTIVE OBJ"+ 
	                                                             " WHERE C.ID=P.REF_CHANNEL_ID AND P.REF_PARTNER_ID=PART.ID AND P.REF_CAMPAIGN_ID=CAMP.ID AND CAMP.REF_OBJ_ID=OBJ.ID";
	
	
	
	// private static final String LOAD_REFERRAL_PROGRAM_QUERY="SELECT * FROM CUST.REF_PROGRAM";
	                                                            
	public static ReferralProgram loadReferralProgramFromPK(Connection conn, PrimaryKey pk) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReferralProgram referralProgram =null;
		String sql = LOAD_REFERRAL_PROGRAM_QUERY + " AND p.id = ?";
		
		try {			
			ps = conn.prepareStatement(sql);
			ps.setString(1, pk.getId());
			rs = ps.executeQuery();

			List list = loadReferralProgramFromResultSet(rs);
			if(list.size()>0)
				referralProgram=(ReferralProgram)list.get(0);
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referralProgram;
	}
	
	
	public static ReferralChannel loadReferralChannelFromPK(Connection conn, PrimaryKey pk) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReferralChannel referralChannel =null;
		String sql = LOAD_REFERRAL_CHANNEL_QUERY + " WHERE id = ?";
		
		try {			
			ps = conn.prepareStatement(sql);
			ps.setString(1, pk.getId());
			rs = ps.executeQuery();
			if(rs.next()){
			  referralChannel = loadReferralChannelFromResultSet(rs);
			}
			else
			{
				throw new SQLException("no results found");
			}
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referralChannel;
	}
	
	public static ReferralObjective loadReferralObjectiveFromPK(Connection conn, PrimaryKey pk) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReferralObjective referralObjective =null;
		String sql = LOAD_REFERRAL_OBJ_QUERY + " WHERE id = ?";
		
		try {			
			ps = conn.prepareStatement(sql);
			ps.setString(1, pk.getId());
			rs = ps.executeQuery();
			if(rs.next()){
				referralObjective = loadReferralObjectiveFromResultSet(rs);
			}
			else
			{
				throw new SQLException("no results found");
			}												
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referralObjective;
	}

	
	public static ReferralPartner loadReferralpartnerFromPK(Connection conn, PrimaryKey pk) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReferralPartner referralpartner =null;
		String sql = LOAD_REFERRAL_PARTNER_QUERY + " WHERE id = ?";
		
		try {			
			ps = conn.prepareStatement(sql);
			ps.setString(1, pk.getId());
			rs = ps.executeQuery();
			if(rs.next()){
				referralpartner = loadReferralPartnerFromResultSet(rs);
			}
			else
			{
				throw new SQLException("no results found");
			}						
									
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referralpartner;
	}


	public static ReferralCampaign loadReferralCampaignFromPK(Connection conn, PrimaryKey pk) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReferralCampaign referralCampaign =null;
		String sql = LOAD_REFERRAL_CAMPAIGN_QUERY + " AND C.ID = ?";
		
		try {			
			ps = conn.prepareStatement(sql);
			ps.setString(1, pk.getId());
			rs = ps.executeQuery();
			if(rs.next()){
				referralCampaign = loadReferralCampaignFromResultSet(rs);
			}
			else
			{
				throw new SQLException("no results found");
			}																		
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referralCampaign;
	}


	
	public static List loadAllReferralPrograms(Connection conn) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List list=null;
		String sql = LOAD_REFERRAL_PROGRAM_QUERY; 				
		try {			
			ps = conn.prepareStatement(sql);			
			rs = ps.executeQuery();
			
			list = loadReferralProgramFromResultSet(rs);
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return list;
	}
	
    private static final String LOAD_REFERRAL_CHANNEL_QUERY="  SELECT ID AS REF_CHANNEL_ID,CHANNEL_NAME,CHANNEL_TYPE,CHANNEL_DESC FROM CUST.REF_CHANNEL ";
	
	
	public static List loadAllReferralChannels(Connection conn) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List list=new ArrayList();
		String sql = LOAD_REFERRAL_CHANNEL_QUERY; 		
		Map refMap=new HashMap();
		try {			
			ps = conn.prepareStatement(sql);			
			rs = ps.executeQuery();
			while(rs.next()){
			  ReferralChannel channel=loadReferralChannelFromResultSet(rs);
			  list.add(channel);			  
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return list;
	}

	private static final String LOAD_REFERRAL_PARTNER_QUERY="  SELECT ID AS REF_PARTNER_ID,PARTNER_NAME,PARTNER_DESC FROM CUST.REF_PARTNER";
	
	public static List loadAllReferralPartners(Connection conn) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List list=new ArrayList();
		String sql = LOAD_REFERRAL_PARTNER_QUERY; 			
		try {			
			ps = conn.prepareStatement(sql);			
			rs = ps.executeQuery();
			while(rs.next()){
			  ReferralPartner partner=loadReferralPartnerFromResultSet(rs);
			  list.add(partner);			  
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return list;
	}

	private static final String LOAD_REFERRAL_OBJ_QUERY="  SELECT ID AS OBJ_ID, OBJECTIVE_NAME, OBJECTIVE_DESC FROM CUST.REF_OBJECTIVE";
	
	public static List loadAllReferralObjective(Connection conn) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List list=new ArrayList();
		String sql = LOAD_REFERRAL_OBJ_QUERY; 				
		try {			
			ps = conn.prepareStatement(sql);			
			rs = ps.executeQuery();
			while(rs.next()){
			  ReferralObjective obj=loadReferralObjectiveFromResultSet(rs);
			  list.add(obj);			  
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return list;
	}
	
	
	private static final String LOAD_REFERRAL_CAMPAIGN_QUERY="  SELECT C.ID AS REF_CAMPAIGN_ID,C.CAMPAIGN_NAME,C.CAMPAIGN_DESC,O.ID AS OBJ_ID,O.OBJECTIVE_NAME,O.OBJECTIVE_DESC  FROM CUST.REF_CAMPAIGN C,CUST.REF_OBJECTIVE O WHERE C.REF_OBJ_ID=O.ID(+)";
	
	public static List loadAllReferralCampaigns(Connection conn) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		List list=new ArrayList();		
		String sql = LOAD_REFERRAL_CAMPAIGN_QUERY; 		
		
		try {			
			ps = conn.prepareStatement(sql);			
			rs = ps.executeQuery();
			while(rs.next()){
			  ReferralCampaign campaign = loadReferralCampaignFromResultSet(rs);
			  list.add(campaign);			  
			}
									
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return list;
	}
	

	public static ReferralProgram loadLatestActiveReferralProgram(Connection conn) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReferralProgram referralProgram = null;
		String sql = LOAD_REFERRAL_PROGRAM_QUERY + 
		" AND P.REF_CHANNEL_ID = (SELECT ID FROM CUST.REF_CHANNEL where CHANNEL_NAME='EMAIL' AND CHANNEL_TYPE='INTERNAL')"+ 
        " AND P.REF_PARTNER_ID = ( SELECT ID FROM CUST.REF_PARTNER  WHERE PARTNER_NAME='FD')"+ 
        " AND P.REF_CAMPAIGN_ID= ( SELECT ID FROM CUST.REF_CAMPAIGN WHERE CAMPAIGN_NAME='TELL_A_FRIEND')"+ 
        " AND SYSDATE BETWEEN P.PROGRAM_START_DATE AND P.PROGRAM_EXP_DATE AND P.STATUS='ACT'";
		
		try {			
			ps = conn.prepareStatement(sql);			
			rs = ps.executeQuery();
			List list = loadReferralProgramFromResultSet(rs);
			if(list.size()>0)
				referralProgram=(ReferralProgram)list.get(0); 
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referralProgram;
	}

	// need to work on this later
	
	private static final String REFERRAL_FROM_REFERRER_CUST_ID_RPT_QUERY = 
		 "SELECT DISTINCT "+ 
		 "r.id AS referral_id, "+ 
		 "r.referrer_cust_id AS referrer_cust_id, "+ 
		 "r.referral_date AS referral_date, "+
		 "r.status AS referral_status, "+ 
		 "r.referral_email_addr AS email_addr, "+ 
		 "r.referral_name AS name, "+		 
		 "rp.id AS referral_prog_id, "+ 
		 "camp.campaign_name AS referral_prog_campaign_code, "+ 
		 "rp.program_desc AS referral_prog_desc, "+ 
		 "+rp.status AS referral_prog_status, "+ 
		 "rp.PROGRAM_START_DATE AS referral_prog_start_date, "+
		 "rp.PROGRAM_EXP_DATE AS referral_prog_exp_date, "+
		 "( "+ 
		 "SELECT DECODE(COUNT(1), 0, 'N', 'Y') "+ 
		 "FROM CUST.CUSTOMERINFO ci_1 "+  
		 "WHERE ci_1.REF_PROG_INVT_ID=r.id "+
		 "AND ci_1.customer_id=signup_info.referral_cust_id "+
		 ") AS is_referral_accepted, "+
		 "( "+
		 "SELECT COUNT(1) "+ 
		 "FROM  CUST.CUSTOMERINFO ci_2, CUST.SALE s_2 "+   
		 "WHERE ci_2.REF_PROG_INVT_ID=r.id "+
		 "AND ci_2.customer_id=s_2.customer_id "+
		 "and s_2.type = 'REG' "+
		 "AND s_2.status IN (?, ?, ?) "+
		 "AND s_2.customer_id=signup_info.referral_cust_id "+
		 ") AS num_delivered_orders, "+
		 "signup_info.signup_email_addr AS email_addr_2 "+
		 "FROM CUST.REF_PROG_INVITATION r, CUST.REF_PROGRAM rp,CUST.REF_CAMPAIGN camp, "+
		 "( "+
		 "SELECT DISTINCT r1.id AS referral_id, c.id AS referral_cust_id, c.user_id AS signup_email_addr "+
		 "FROM cust.REF_PROG_INVITATION r1, cust.CUSTOMERINFO ci, cust.CUSTOMER c "+
		 "WHERE r1.id=ci.REF_PROG_INVT_ID "+
		 "AND ci.customer_id=c.id "+
		 "AND r1.referrer_cust_id=? "+
		 ") signup_info "+
		 "WHERE  r.REF_PROG_ID=rp.id "+ 
		 "AND r.referrer_cust_id = ? "+
		 "AND r.id=signup_info.referral_id (+) "+
		 "AND rp.REF_CAMPAIGN_ID=camp.id(+) "+ 
		 "ORDER BY r.referral_date";
		 
		 
	public static List loadReferralReportFromReferrerCustomerId(Connection conn, String referrerCustomerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		FDReferralReportLine refReportLine = null;
		List referralReportList = new ArrayList();
		try {			
			ps = conn.prepareStatement(REFERRAL_FROM_REFERRER_CUST_ID_RPT_QUERY);
			int index = 1;
			ps.setString(index++, EnumSaleStatus.CAPTURE_PENDING.getStatusCode());
			ps.setString(index++, EnumSaleStatus.PAYMENT_PENDING.getStatusCode());
			ps.setString(index++, EnumSaleStatus.SETTLED.getStatusCode());
			ps.setString(index++, referrerCustomerId);
			ps.setString(index++, referrerCustomerId);
			rs = ps.executeQuery();
			while (rs.next()) {
				refReportLine = loadReferralReportLineFromResultSet(rs);
				if (refReportLine == null) {
					throw new SQLException("Referral Line invalid.  Referrer Customer id = " + referrerCustomerId);
				}
				referralReportList.add(refReportLine);
			}
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referralReportList;
	}
	
	private static final String REFERRAL_FROM_REFERRAL_CUST_ID_RPT_QUERY = 
		" SELECT DISTINCT r.id AS referral_id, "+ 
		" r.referrer_cust_id AS referrer_cust_id, "+    
		 "r.referral_date AS referral_date, "+  
		 "r.status AS referral_status, "+  
		 " c_referrer.user_id AS email_addr, "+  
		 "ci_referrer.first_name || ' ' || ci_referrer.last_name AS name, "+  
		 "rp.id AS referral_prog_id, "+  
		 " camp.campaign_name AS referral_prog_campaign_code, "+  
		 "rp.program_desc AS referral_prog_desc, "+  
		 "rp.status AS referral_prog_status, "+  
		 "rp.PROGRAM_START_DATE AS referral_prog_start_date, "+ 
		 "rp.PROGRAM_EXP_DATE AS referral_prog_exp_date, "+ 
		 "( "+ 
		 "SELECT DECODE(COUNT(1), 0, 'N', 'Y') "+  
		 "FROM CUST.CUSTOMERINFO ci_1 "+ 
		 "WHERE ci_1.REF_PROG_INVT_ID=r.id "+ 
		 "AND ci_1.customer_id=? "+ 
		 ") AS is_referral_accepted, "+ 
		 "( "+ 
		 "SELECT COUNT(1) "+  
		 "FROM CUST.CUSTOMERINFO ci_2, CUST.SALE s_2 "+    
		 "WHERE ci_2.REF_PROG_INVT_ID=r.id "+ 
		 "AND ci_2.customer_id=s_2.customer_id "+ 
		 "and s_2.type = 'REG' "+
		 "AND s_2.status IN (?,?,?) "+ 
		 "AND s_2.customer_id=? "+ 
		 ") AS num_delivered_orders, "+ 
		 "r.referral_email_addr AS email_addr_2 "+  
		 "FROM cust.REF_PROG_INVITATION  r, cust.REF_PROGRAM rp, cust.CUSTOMER c_referrer, cust.CUSTOMERINFO ci_referrer, cust.REF_CAMPAIGN camp "+   
		 "WHERE  r.REF_PROG_ID=rp.id "+  
		 "AND r.REFERRER_CUST_ID=c_referrer.id "+ 
		 "AND rp.REF_CAMPAIGN_ID=camp.id(+) "+ 
		 "AND c_referrer.id=ci_referrer.customer_id "+ 
		 "AND r.REFERRAL_EMAIL_ADDR IN ( "+ 
		 "SELECT c.user_id "+ 
		 "FROM cust.CUSTOMER c "+ 
		 "WHERE c.id = ? "+ 
		 "UNION "+ 
		 "SELECT r_3.referral_email_addr "+ 
		 "FROM cust.CUSTOMERINFO ci_3, cust.REF_PROG_INVITATION r_3 "+ 
		 "WHERE ci_3.REF_PROG_INVT_ID=r_3.id "+ 
		 "AND ci_3.customer_id=? "+ 
		 ") "+ 
		 "ORDER BY r.referral_date "; 

	
	
	public static List loadReferralReportFromReferralCustomerId(Connection conn, String referralCustomerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		FDReferralReportLine refReportLine = null;
		List referralReportList = new ArrayList();
		try {			
			ps = conn.prepareStatement(REFERRAL_FROM_REFERRAL_CUST_ID_RPT_QUERY);
			int index = 1;
			ps.setString(index++, referralCustomerId);
			ps.setString(index++, EnumSaleStatus.CAPTURE_PENDING.getStatusCode());
			ps.setString(index++, EnumSaleStatus.PAYMENT_PENDING.getStatusCode());
			ps.setString(index++, EnumSaleStatus.SETTLED.getStatusCode());
			ps.setString(index++, referralCustomerId);
			ps.setString(index++, referralCustomerId);
			ps.setString(index++, referralCustomerId);
			rs = ps.executeQuery();
			while (rs.next()) {
				refReportLine = loadReferralReportLineFromResultSet(rs);
				if (refReportLine == null) {
					throw new SQLException("Referral Line invalid.  Referrer Customer id = " + referralCustomerId);
				}
				referralReportList.add(refReportLine);
			}
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referralReportList;
	}

	private static final String REFERRER_NAME_FROM_REFERRAL_CUST_ID_RPT_QUERY = 
		 "SELECT ci_rfr.first_name || ' ' || ci_rfr.last_name AS referrer_name "+
		 "FROM CUST.CUSTOMERINFO ci_rfl, CUST.REF_PROG_INVITATION r, CUST.CUSTOMER c_rfr, CUST.CUSTOMERINFO ci_rfr "+
		 "WHERE ci_rfl.REF_PROG_INVT_ID=r.id "+
		 "AND r.referrer_cust_id=c_rfr.id "+
		 "AND c_rfr.id=ci_rfr.customer_id "+
		 "AND ci_rfl.customer_id=? ";

	public static String loadReferrerNameFromReferralCustomerId(Connection conn, String referralCustomerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String referrerName = null;
		try {			
			ps = conn.prepareStatement(REFERRER_NAME_FROM_REFERRAL_CUST_ID_RPT_QUERY);
			int index = 1;
			ps.setString(index++, referralCustomerId);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getString("REFERRER_NAME");
			}
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return referrerName;
	}
	
	private static List loadReferralFromResultSet(ResultSet rs) throws SQLException {
		
		List list=new ArrayList();
		while(rs.next()){
			PrimaryKey pk = new PrimaryKey(rs.getString("ID"));
			ReferralProgramInvitaionModel model = new ReferralProgramInvitaionModel(pk);
			model.setReferrerCustomerId(rs.getString("REFERRER_CUST_ID"));
			model.setReferralCreatedDate(rs.getDate("REFERRAL_DATE"));
			model.setStatus(EnumReferralStatus.getEnum(rs.getString("STATUS")));	
			model.setReferralName(rs.getString("REFERRAL_NAME"));		
			model.setReferrelEmailAddress(rs.getString("REFERRAL_EMAIL_ADDR"));
			model.setReferralProgramId(rs.getString("REF_PROG_ID"));
			model.setReferralModifiedDate(rs.getDate("MODIFIED_DATE"));
			list.add(model);
		}
		return list;		
	}


	private static List loadReferralProgramFromResultSet(ResultSet rs) throws SQLException {
		
		List list=new ArrayList();
			
		while(rs.next()){
			PrimaryKey pk = new PrimaryKey(rs.getString("ID"));								
			//ReferralProgram referralProgram = new ReferralProgram(pk,rs.getString("PROGRAM_NAME"),loadReferralChannelFromResultSet(rs,refChannelMap),loadReferralCampaignFromResultSet(rs,refCampaignMap,refObjectiveMap),loadReferralPartnerFromResultSet(rs,refPartnerMap));
			ReferralProgram referralProgram = new ReferralProgram(pk);
			referralProgram.setName(rs.getString("PROGRAM_NAME"));
			referralProgram.setDescription(rs.getString("PROGRAM_DESC"));
			referralProgram.setCreativeDesc(rs.getString("CREATIVE_DESC"));		
			referralProgram.setStatus(EnumReferralProgramStatus.getEnum(rs.getString("STATUS")));
			referralProgram.setExpDate(rs.getDate("PROGRAM_EXP_DATE"));
			referralProgram.setStartDate(rs.getDate("PROGRAM_START_DATE"));
			referralProgram.setCreativeUrl(rs.getString("CREATIVE_URL"));
			referralProgram.setPromotionCode(rs.getString("REF_PROMOTION_CODE"));
			if(rs.getString("REF_CHANNEL_ID")!=null){
				PrimaryKey chaPk = new PrimaryKey(rs.getString("REF_CHANNEL_ID"));
				ReferralChannel channel=new ReferralChannel(chaPk,rs.getString("CHANNEL_NAME"));
				referralProgram.setChannel(channel);
			}
			if(rs.getString("REF_CAMPAIGN_ID")!=null){
				PrimaryKey chaPk = new PrimaryKey(rs.getString("REF_CAMPAIGN_ID"));
				ReferralCampaign campaign=new ReferralCampaign(chaPk);
				campaign.setName(rs.getString("CAMPAIGN_NAME"));
				referralProgram.setCampaign(campaign);
			}
			if(rs.getString("REF_PARTNER_ID")!=null){
				PrimaryKey chaPk = new PrimaryKey(rs.getString("REF_PARTNER_ID"));
				ReferralPartner partner=new ReferralPartner(chaPk,rs.getString("PARTNER_NAME"));				
				referralProgram.setPartner(partner);
			}			
			list.add(referralProgram);
		}
		return list;
	}


	private static ReferralChannel loadReferralChannelFromResultSet(ResultSet rs) throws SQLException {
				
			PrimaryKey pk = new PrimaryKey(rs.getString("REF_CHANNEL_ID"));			
			ReferralChannel refferralChannel = new ReferralChannel(pk,rs.getString("CHANNEL_NAME"));
			refferralChannel.setType(rs.getString("CHANNEL_TYPE"));
			refferralChannel.setDescription(rs.getString("CHANNEL_DESC"));
			return refferralChannel;
	}
	
	private static ReferralObjective loadReferralObjectiveFromResultSet(ResultSet rs) throws SQLException {
		
			PrimaryKey pk = new PrimaryKey(rs.getString("OBJ_ID"));			
			ReferralObjective referralObjective = new ReferralObjective(pk,rs.getString("OBJECTIVE_NAME"));	
			referralObjective.setDescription(rs.getString("OBJECTIVE_DESC"));
			return referralObjective;
    }


	private static ReferralPartner loadReferralPartnerFromResultSet(ResultSet rs) throws SQLException {
	
			PrimaryKey pk = new PrimaryKey(rs.getString("REF_PARTNER_ID"));		
			ReferralPartner referralPartner = new ReferralPartner(pk,rs.getString("PARTNER_NAME"));	
			referralPartner.setDescription(rs.getString("PARTNER_DESC"));
			return referralPartner;
		
    }


	private static ReferralCampaign loadReferralCampaignFromResultSet(ResultSet rs) throws SQLException {
		
			PrimaryKey pk = new PrimaryKey(rs.getString("REF_CAMPAIGN_ID"));
			ReferralCampaign referralCampaign = new ReferralCampaign(pk);
			referralCampaign.setName(rs.getString("CAMPAIGN_NAME"));
			referralCampaign.setDescription(rs.getString("CAMPAIGN_DESC"));
			if(rs.getString("OBJ_ID")!=null){				
				  PrimaryKey objPk = new PrimaryKey(rs.getString("OBJ_ID"));				  
				  ReferralObjective obj=new ReferralObjective(objPk,rs.getString("OBJECTIVE_NAME"));
				  referralCampaign.setObjective(obj);
			}

			return referralCampaign;
    }

	
	private static FDReferralReportLine loadReferralReportLineFromResultSet(ResultSet rs) throws SQLException {
		FDReferralReportLine refReportLine = new FDReferralReportLine();
		refReportLine.setReferralId(rs.getString("REFERRAL_ID"));
		refReportLine.setReferrerCustomerId(rs.getString("REFERRER_CUST_ID"));
		refReportLine.setReferralDate(rs.getDate("REFERRAL_DATE"));
		refReportLine.setReferralStatus(EnumReferralStatus.getEnum(rs.getString("REFERRAL_STATUS")));		
		refReportLine.setEmailAddress(rs.getString("EMAIL_ADDR"));
		refReportLine.setName(rs.getString("NAME"));
		refReportLine.setReferralProgramId(rs.getString("REFERRAL_PROG_ID"));
		refReportLine.setReferralProgramCampaignCode(rs.getString("REFERRAL_PROG_CAMPAIGN_CODE"));
		refReportLine.setReferralProgramDesc(rs.getString("REFERRAL_PROG_DESC"));
		refReportLine.setReferralProgramStatus(EnumReferralProgramStatus.getEnum(rs.getString("REFERRAL_PROG_STATUS")));		
		refReportLine.setReferralProgramStartDate(rs.getDate("REFERRAL_PROG_START_DATE"));
		refReportLine.setReferralProgramExpirationDate(rs.getDate("REFERRAL_PROG_EXP_DATE"));
		refReportLine.setIsReferralAccepted("Y".equalsIgnoreCase(rs.getString("IS_REFERRAL_ACCEPTED")));
		refReportLine.setNumDeliveredOrders(rs.getInt("NUM_DELIVERED_ORDERS"));
		refReportLine.setEmailAddress2(rs.getString("EMAIL_ADDR_2"));
		return refReportLine;
	}

	public static void storeReferral(Connection conn, ReferralProgramInvitaionModel referral) throws SQLException {
		
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("UPDATE CUST.REF_PROG_INVITATION SET REFERRER_CUST_ID=?,REFERRAL_DATE=?,STATUS=?,REFERRAL_NAME=?,REFERRAL_EMAIL_ADDR=?,REF_PROG_ID=?,MODIFIED_DATE=? WHERE ID=?");
			int index = 1;
			ps.setString(index++, referral.getReferrerCustomerId());
			ps.setDate(index++, new java.sql.Date(referral.getReferralCreatedDate().getTime()));
			ps.setString(index++, referral.getStatus().getName());
			ps.setString(index++, referral.getReferralName());
			ps.setString(index++, referral.getReferrelEmailAddress());
			ps.setString(index++, referral.getReferralProgramId());
			ps.setDate(index++, new java.sql.Date(referral.getReferralModifiedDate().getTime()));
			ps.setString(index++, referral.getPK().getId());
			
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not updated");
			}
		} finally {
			if (ps != null) ps.close();
		}				

	}

	

	

	public static void storeReferralProgram(Connection conn, FDReferralProgramModel referralProgram) throws SQLException {
		
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("UPDATE CUST.REFERRAL_PROGRAM SET CAMPAIGN_CODE = ?, STATUS = ?, START_DATE = ?, EXPIRATION_DATE = ?, DESCRIPTION = ? WHERE ID = ?");
			int index = 1;
			ps.setString(index++, referralProgram.getCampaignCode());
			ps.setString(index++, referralProgram.getStatus().getName());
			ps.setDate(index++, new java.sql.Date(referralProgram.getStartDate().getTime()));
			ps.setDate(index++, new java.sql.Date(referralProgram.getExpirationDate().getTime()));
			ps.setString(index++, referralProgram.getDescription());
			ps.setString(index++, referralProgram.getPK().getId());
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not updated");
			}
		} finally {
			if (ps != null) ps.close();
		}

	}
	
	
		
	public static List loadReferralProgramForChannel(Connection conn,String chaIds[]) throws SQLException{
	
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReferralProgram referralProgram =null;
		StringBuffer buffer=new StringBuffer();
		String sql = LOAD_REFERRAL_PROGRAM_QUERY + " AND P.REF_CHANNEL_ID in (";
		List refPrgList=null;
		try {						
			if(chaIds!=null && chaIds.length>0){				
				for(int i=0;i<chaIds.length;i++){
					if(i==0){
						buffer.append(chaIds[i]);
					}
					else{
						buffer.append(",").append(chaIds[i]);
					}
				}
				buffer.append(")");
			}
			sql=sql+buffer.toString();
			
			ps = conn.prepareStatement(sql);			
			rs = ps.executeQuery();
			refPrgList = loadReferralProgramFromResultSet(rs);		
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return refPrgList;		
	}
	
	
	public static List loadReferralProgramForCampaign(Connection conn,String campIds[]) throws SQLException{
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuffer buffer=new StringBuffer();
		String sql = LOAD_REFERRAL_PROGRAM_QUERY + " AND P.REF_CAMPAIGN_ID in (";
		List refPrgList=null;
		try {			
			
			if(campIds!=null && campIds.length>0)
			{				
				for(int i=0;i<campIds.length;i++)
				{
					if(i==0)
						buffer.append(campIds[i]);
					else
						buffer.append(",").append(campIds[i]);
				}
				buffer.append(")");
			}
			sql=sql+buffer.toString();			
			ps = conn.prepareStatement(sql);			
			rs = ps.executeQuery();
			refPrgList = loadReferralProgramFromResultSet(rs);						
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return refPrgList;
		
	}
	
	
public static List loadReferralCampaignForRefObjective(Connection conn,String objIds[]) throws SQLException{
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReferralProgram referralProgram =null;
		StringBuffer buffer=new StringBuffer();
		List list=new ArrayList();	
		String sql = LOAD_REFERRAL_CAMPAIGN_QUERY + "  AND O.ID in ( ";		
		try {			
			
			if(objIds!=null && objIds.length>0)
			{				
				for(int i=0;i<objIds.length;i++)
				{
					if(i==0)
						buffer.append(objIds[i]);
					else
						buffer.append(",").append(objIds[i]);
				}
				buffer.append(")");
			}
			sql=sql+buffer.toString();			
			ps = conn.prepareStatement(sql);			
			rs = ps.executeQuery();
			while(rs.next()){
			  ReferralCampaign campaign = (ReferralCampaign)loadReferralCampaignFromResultSet(rs);
			  list.add(campaign);			  	
			}
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return list;
		
	}
	

public static List loadReferralProgramForPartner(Connection conn,String partnerIds[]) throws SQLException{		
		PreparedStatement ps = null;
		ResultSet rs = null;
		ReferralProgram referralProgram =null;
		StringBuffer buffer=new StringBuffer();
		String sql = LOAD_REFERRAL_PROGRAM_QUERY + " AND  P.REF_PARTNER_ID in (";
		List refPrgList=null;
		try {			
			
			if(partnerIds!=null && partnerIds.length>0)
			{				
				for(int i=0;i<partnerIds.length;i++)
				{
					if(i==0){
						buffer.append(partnerIds[i]);
					}
					else{
						buffer.append(",").append(partnerIds[i]);
					}
				}
				buffer.append(")");
			}
			sql=sql+buffer.toString();
			
			ps = conn.prepareStatement(sql);			
			rs = ps.executeQuery();

			refPrgList = loadReferralProgramFromResultSet(rs);			
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return refPrgList;
		
	}

   public static int getTotalCountOfRefChannel(Connection conn,String name,String type) throws SQLException{
	   
	    PreparedStatement ps = null;
		ResultSet rs = null;
		List list=new ArrayList();
		int count=0;
		String sql = "SELECT COUNT(*) COUNTER FROM CUST.REF_CHANNEL WHERE UPPER(CHANNEL_NAME)=UPPER(?) AND UPPER(CHANNEL_TYPE)=UPPER(?)"; 				
		try {			
			ps = conn.prepareStatement(sql);		
			int index = 1;
			ps.setString(index++, name);
			ps.setString(index++, type);
			rs = ps.executeQuery();
			if(rs.next()){
			   count=rs.getInt("COUNTER");
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return count;	   
   }

   public static int getTotalCountOfRefObjective(Connection conn,String name) throws SQLException{
	   
	    PreparedStatement ps = null;
		ResultSet rs = null;
		List list=new ArrayList();
		int count=0;
		String sql = "SELECT COUNT(*) COUNTER FROM CUST.REF_OBJECTIVE WHERE UPPER(OBJECTIVE_NAME)=UPPER(?)"; 				
		try {			
			ps = conn.prepareStatement(sql);		
			int index = 1;
			ps.setString(index++, name);			
			rs = ps.executeQuery();
			if(rs.next()){
			   count=rs.getInt("COUNTER");
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return count;	   
   }
   
   
   public static int getTotalCountOfRefCampaign(Connection conn,String name) throws SQLException{	   
	    PreparedStatement ps = null;
		ResultSet rs = null;
		List list=new ArrayList();
		int count=0;
		String sql = "SELECT COUNT(*) COUNTER FROM CUST.REF_CAMPAIGN WHERE UPPER(CAMPAIGN_NAME)=UPPER(?)"; 				
		try {			
			ps = conn.prepareStatement(sql);		
			int index = 1;
			ps.setString(index++, name);			
			rs = ps.executeQuery();
			if(rs.next()){
			   count=rs.getInt("COUNTER");
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return count;	   
   }
   
   
   public static int getTotalCountOfRefPartner(Connection conn,String name) throws SQLException{
	   
	    PreparedStatement ps = null;
		ResultSet rs = null;
		List list=new ArrayList();
		int count=0;
		String sql = "SELECT COUNT(*) COUNTER  FROM CUST.REF_PARTNER WHERE UPPER(PARTNER_NAME)=UPPER(?)"; 				
		try {			
			ps = conn.prepareStatement(sql);		
			int index = 1;
			ps.setString(index++, name);			
			rs = ps.executeQuery();
			if(rs.next()){
			   count=rs.getInt("COUNTER");
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return count;	   
   }

   public static int getTotalCountOfRefProgram(Connection conn,String name) throws SQLException{	   
	    PreparedStatement ps = null;
		ResultSet rs = null;
		int count=0;
		String sql = "SELECT COUNT(*) COUNTER  FROM CUST.REF_PROGRAM WHERE UPPER(PROGRAM_NAME)=UPPER(?)"; 				
		try {			
			ps = conn.prepareStatement(sql);		
			int index = 1;
			ps.setString(index++, name);			
			rs = ps.executeQuery();
			if(rs.next()){
			   count=rs.getInt("COUNTER");
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return count;	   
   }

   public static int getTotalReferralProgramCount(Connection conn) throws SQLException
   {
	    PreparedStatement ps = null;
		ResultSet rs = null;
		int count=0;
		String sql = "SELECT COUNT(*) COUNTER  FROM CUST.REF_PROGRAM"; 				
		try {			
			ps = conn.prepareStatement(sql);		
			rs = ps.executeQuery();
			if(rs.next()){
			   count=rs.getInt("COUNTER");
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return count;
	   
   }
   
   public static List getReferralPrograms(Connection conn,ReferralSearchCriteria criteria) throws SQLException{	   
	    PreparedStatement ps = null;
		ResultSet rs = null;
		List list=null;
		String sql = "select *   from  (select a.*, rownum rnum   from  ("+LOAD_REFERRAL_PROGRAM_QUERY+"  order by "+criteria.getSortByColumnName()+") a   where rownum <= ? ) where rnum > ?"; 				
		try {									
			int index = 1;
			ps = conn.prepareStatement(sql);						
			ps.setInt(index++,criteria.getEndIndex());
			ps.setInt(index++,criteria.getStartIndex());
			rs = ps.executeQuery();
			list=loadReferralProgramFromResultSet(rs);
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return list;	   
  }
   
   public static int getTotalReferralChannelCount(Connection conn) throws SQLException
   {
	    PreparedStatement ps = null;
		ResultSet rs = null;
		int count=0;
		String sql = "SELECT COUNT(*) COUNTER  FROM CUST.REF_CHANNEL"; 				
		try {			
			ps = conn.prepareStatement(sql);		
			int index = 1;					
			rs = ps.executeQuery();
			if(rs.next()){
			   count=rs.getInt("COUNTER");
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return count;
	   
   }
 
   
   public static List getReferralChannel(Connection conn,ReferralSearchCriteria criteria) throws SQLException{	   
	    PreparedStatement ps = null;
		ResultSet rs = null;
		List list=new ArrayList();
		String sql = "select *   from  (select a.*, rownum rnum   from  ("+LOAD_REFERRAL_CHANNEL_QUERY+"  order by "+criteria.getSortByColumnName()+") a   where rownum <= ? ) where rnum > ?"; 				
		try {									
			int index = 1;
			ps = conn.prepareStatement(sql);						
			ps.setInt(index++,criteria.getEndIndex());
			ps.setInt(index++,criteria.getStartIndex());
			rs = ps.executeQuery();
			while(rs.next()){
			   list.add(loadReferralChannelFromResultSet(rs));
			}
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return list;	   
 }

   public static int getTotalReferralCampaignCount(Connection conn) throws SQLException
   {
	    PreparedStatement ps = null;
		ResultSet rs = null;
		int count=0;
		String sql = "SELECT COUNT(*) COUNTER  FROM CUST.REF_CAMPAIGN"; 				
		try {			
			ps = conn.prepareStatement(sql);		
			rs = ps.executeQuery();
			if(rs.next()){
			   count=rs.getInt("COUNTER");
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return count;
	   
   }

   
   public static List getReferralCampaign(Connection conn,ReferralSearchCriteria criteria) throws SQLException{	   
	    PreparedStatement ps = null;
		ResultSet rs = null;
		List list=new ArrayList();
		String sql = "select *   from  (select a.*, rownum rnum   from  ("+LOAD_REFERRAL_CAMPAIGN_QUERY+"  order by C."+criteria.getSortByColumnName()+") a   where rownum <= ? ) where rnum > ?"; 				
		try {									
			int index = 1;
			ps = conn.prepareStatement(sql);						
			ps.setInt(index++,criteria.getEndIndex());
			ps.setInt(index++,criteria.getStartIndex());
			rs = ps.executeQuery();
			while(rs.next()){
			   list.add(loadReferralCampaignFromResultSet(rs));
			}
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return list;	   
   }
   
   
   public static int getTotalReferralPartnerCount(Connection conn) throws SQLException
   {
	    PreparedStatement ps = null;
		ResultSet rs = null;
		int count=0;
		String sql = "SELECT COUNT(*) COUNTER  FROM CUST.REF_PARTNER"; 				
		try {			
			ps = conn.prepareStatement(sql);		
			rs = ps.executeQuery();
			if(rs.next()){
			   count=rs.getInt("COUNTER");
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return count;
	   
   }

   
   public static List getReferralPartner(Connection conn,ReferralSearchCriteria criteria) throws SQLException{	   
	    PreparedStatement ps = null;
		ResultSet rs = null;
		List list=new ArrayList();
		String sql = "select *   from  (select a.*, rownum rnum   from  ("+LOAD_REFERRAL_PARTNER_QUERY+"  order by "+criteria.getSortByColumnName()+") a   where rownum <= ? ) where rnum > ?"; 				
		try {									
			int index = 1;
			ps = conn.prepareStatement(sql);						
			ps.setInt(index++,criteria.getEndIndex());
			ps.setInt(index++,criteria.getStartIndex());
			rs = ps.executeQuery();
			while(rs.next()){
			   list.add(loadReferralPartnerFromResultSet(rs));
			}
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return list;	   
  }


   public static int getTotalReferralObjectiveCount(Connection conn) throws SQLException
   {
	    PreparedStatement ps = null;
		ResultSet rs = null;
		int count=0;
		String sql = "SELECT COUNT(*) COUNTER  FROM CUST.REF_OBJECTIVE"; 				
		try {			
			ps = conn.prepareStatement(sql);		
			rs = ps.executeQuery();
			if(rs.next()){
			   count=rs.getInt("COUNTER");
			}
			
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return count;
	   
   }

   
   public static List getReferralObjective(Connection conn,ReferralSearchCriteria criteria) throws SQLException{	   
	    PreparedStatement ps = null;
		ResultSet rs = null;
		List list=new ArrayList();
		String sql = "select *   from  (select a.*, rownum rnum   from  ("+LOAD_REFERRAL_OBJ_QUERY+"  order by "+criteria.getSortByColumnName()+") a   where rownum <= ? ) where rnum > ?"; 				
		try {									
			int index = 1;
			ps = conn.prepareStatement(sql);						
			ps.setInt(index++,criteria.getEndIndex());
			ps.setInt(index++,criteria.getStartIndex());
			rs = ps.executeQuery();
			while(rs.next()){
			   list.add(loadReferralObjectiveFromResultSet(rs));
			}
		} finally {
			if (rs != null) rs.close();
			if (ps != null) ps.close();
		}
		return list;	   
 }
   
}


