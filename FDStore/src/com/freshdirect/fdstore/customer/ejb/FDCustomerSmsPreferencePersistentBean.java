package com.freshdirect.fdstore.customer.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.freshdirect.common.address.PhoneNumber;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.framework.core.DependentPersistentBeanSupport;
import com.freshdirect.framework.core.ModelI;
import com.freshdirect.framework.core.PrimaryKey;

/**
 * 
 * @author sathish Merugu
 *
 */

public class FDCustomerSmsPreferencePersistentBean extends DependentPersistentBeanSupport{

	private static final long serialVersionUID = 7976628071415662502L;
	private FDCustomerEStoreModel model;
	private String orderNotices=null;
	private String orderExceptions=null;
	private String offers=null;
	private String partnerMessages=null;
	private String smsPreferenceflag;
	private String fdxSmsPreferenceflag;
	private String fdxOrderNotices=null;
	private String fdxOrderExceptions=null;
	private String fdxOffers=null;
	private String fdxPartnerMessages;
	
	boolean OfferNotification=false;
	boolean DeliveryNotification=false;
	boolean FdxOfferNotification=false;
	boolean FdxDeliveryNotification=false;
	private PhoneNumber mobileNumber;
	private PhoneNumber fdxMobileNumber;
	
	
	
	public FDCustomerSmsPreferencePersistentBean() {
		super();
		model = new FDCustomerEStoreModel();
	}

	public FDCustomerSmsPreferencePersistentBean(PrimaryKey pk) {
		super(pk);
		model = new FDCustomerEStoreModel();
	}

	

	@Override
	public ModelI getModel() {
		if(this.model!=null)
			return this.model.deepCopy();
		return new FDCustomerEStoreModel().deepCopy();
	}

	
	@Override
	public PrimaryKey create(Connection conn) throws SQLException {
		this.setPK(this.getParentPK());
			PreparedStatement ps = conn.prepareStatement("INSERT INTO CUST.FDCUSTOMER_ESTORE (FDCUSTOMER_ID, E_STORE) values (?,?)"); 
		ps.setString(1, this.getParentPK().getId());
		ps.setString(2, model.getCrmStore());
		try {
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created"+model.getCrmStore());
			}
		} catch (SQLException sqle) {
			this.setPK(null);
			throw sqle;
		} finally {
			ps.close();
		}
		this.store(conn);
		return this.getParentPK();
	}
	
	@Override
	public void load(Connection conn) throws SQLException {
		PreparedStatement ps, ps1;
		ResultSet rs, rs1;
		 ps = conn.prepareStatement("SELECT MOBILE_NUMBER, ORDER_NOTIFICATION, ORDEREXCEPTION_NOTIFICATION, SMS_OFFERS_ALERT, PARTNERMESSAGE_NOTIFICATION," +
		 							" SMS_OPTIN_DATE, DELIVERY_NOTIFICATION, OFFERS_NOTIFICATION, SMS_PREFERENCE_FLAG FROM CUST.FDCUSTOMER_ESTORE" +
										" WHERE FDCUSTOMER_ID=? AND E_STORE='FreshDirect'");
		ps.setString(1, this.getParentPK().getId());
		 rs = ps.executeQuery();
		if (rs.next()) {
				model.setPK(getPK());
				if(null!=rs.getString("MOBILE_NUMBER"))
				mobileNumber=new PhoneNumber(rs.getString("MOBILE_NUMBER"));
				this.orderNotices=rs.getString("ORDER_NOTIFICATION");	
				this.orderExceptions=rs.getString("ORDEREXCEPTION_NOTIFICATION");	
				this.offers=rs.getString("SMS_OFFERS_ALERT");		
				this.partnerMessages=rs.getString("PARTNERMESSAGE_NOTIFICATION");
				model.setSmsOptinDate(rs.getTimestamp("SMS_OPTIN_DATE"));
				this.DeliveryNotification="Y".equalsIgnoreCase(rs.getString("DELIVERY_NOTIFICATION"))?true:false;
				this.OfferNotification="Y".equalsIgnoreCase(rs.getString("OFFERS_NOTIFICATION"))?true:false;
				this.smsPreferenceflag= rs.getString("SMS_PREFERENCE_FLAG");
				
		}
		 ps1 = conn.prepareStatement("SELECT MOBILE_NUMBER, ORDER_NOTIFICATION, ORDEREXCEPTION_NOTIFICATION, SMS_OFFERS_ALERT, PARTNERMESSAGE_NOTIFICATION, SMS_OPTIN_DATE, DELIVERY_NOTIFICATION, OFFERS_NOTIFICATION, SMS_PREFERENCE_FLAG  FROM CUST.FDCUSTOMER_ESTORE" +
				" WHERE FDCUSTOMER_ID=? AND E_STORE='FDX'");
				ps1.setString(1, this.getParentPK().getId());
				 rs1 = ps1.executeQuery();
				if (rs1.next()) {
				fdxMobileNumber=rs1.getString("MOBILE_NUMBER")==null?model.getMobileNumber():new PhoneNumber(rs1.getString("MOBILE_NUMBER"));
				this.fdxOrderNotices=rs1.getString("ORDER_NOTIFICATION");	
				this.fdxOrderExceptions=rs1.getString("ORDEREXCEPTION_NOTIFICATION");	
				this.fdxOffers=rs1.getString("SMS_OFFERS_ALERT");		
				this.fdxPartnerMessages=rs1.getString("PARTNERMESSAGE_NOTIFICATION");
				model.setFdxSmsOptinDate(rs1.getTimestamp("SMS_OPTIN_DATE"));
				this.FdxDeliveryNotification="Y".equalsIgnoreCase(rs1.getString("DELIVERY_NOTIFICATION"))?true:false;
				this.FdxOfferNotification="Y".equalsIgnoreCase(rs1.getString("OFFERS_NOTIFICATION"))?true:false;
				this.fdxSmsPreferenceflag=rs1.getString("SMS_PREFERENCE_FLAG");
				}
				model.setMobileNumber(mobileNumber);
				model.setFdxMobileNumber(fdxMobileNumber!=null?fdxMobileNumber:mobileNumber);
				model.setOrderNotices(this.orderNotices !=null ? this.orderNotices :"N");	
				model.setOrderExceptions(this.orderExceptions !=null ? this.orderExceptions :"N");	
				model.setOffers(this.offers!=null ? this.offers:"N");		
				model.setPartnerMessages(this.partnerMessages !=null ? this.partnerMessages :"N");
				model.setFdxOrderNotices(this.fdxOrderNotices!=null?this.fdxOrderNotices:"N");
				model.setFdxOrderExceptions(this.fdxOrderExceptions!=null ?this.fdxOrderExceptions:"N");	
				model.setFdxOffers(this.fdxOffers!=null?this.fdxOffers:"N");		
				model.setFdxPartnerMessages(this.fdxPartnerMessages!=null?this.fdxPartnerMessages:"N");
				model.setOffersNotification(this.OfferNotification);
				model.setDeliveryNotification(this.DeliveryNotification);
				model.setFdxOffersNotification(this.FdxOfferNotification);
				model.setFdxdeliveryNotification(this.FdxDeliveryNotification);
        model.setSmsPreferenceflag(this.smsPreferenceflag);
        model.setFdxSmsPreferenceflag(this.fdxSmsPreferenceflag);
				
				ps.close();
				rs.close();
				ps1.close();
				rs1.close();
		}
	

	@Override
	public void store(Connection conn) throws SQLException {
		
		PreparedStatement ps,ps1 = null;
			
		Date optinDate = new Date();
		
		if(model!=null && EnumEStoreId.FD.getContentId().equalsIgnoreCase(model.getCrmStore()) && model.getMobileNumber()!=null && model.getMobileNumber().getPhone()!=null)
		{
		ps = conn.prepareStatement("UPDATE CUST.FDCUSTOMER_ESTORE SET  MOBILE_NUMBER=?, ORDER_NOTIFICATION=?, ORDEREXCEPTION_NOTIFICATION=?, SMS_OFFERS_ALERT=?, PARTNERMESSAGE_NOTIFICATION=?," +
										" SMS_OPTIN_DATE=?, DELIVERY_NOTIFICATION=?, OFFERS_NOTIFICATION=?, SMS_PREFERENCE_FLAG=? WHERE FDCUSTOMER_ID=? AND E_STORE=?");
				
				ps.setString(1, model.getMobileNumber().getPhone());
				ps.setString(2, model.getOrderNotices());
				ps.setString(3, model.getOrderExceptions());
				ps.setString(4, model.getOffers());
				ps.setString(5, model.getPartnerMessages());
				ps.setTimestamp(6, new java.sql.Timestamp(optinDate.getTime()));
				if(model.getDeliveryNotification()!=null)
					ps.setString(7,  model.getDeliveryNotification()?"Y":"N");
				else
					ps.setString(7,  "N");
				
				if(model.getOffersNotification()!=null)
					ps.setString(8,  model.getOffersNotification()?"Y":"N");
				else
					ps.setString(8,  "N");
				ps.setString(9, model.getSmsPreferenceflag());
				ps.setString(10, this.getParentPK().getId());
				ps.setString(11, EnumEStoreId.FD.getContentId());
				
				if(ps.executeUpdate() < 1){
					create(conn);
				}
				 ps.close();
		   }
			if(model!=null && EnumEStoreId.FDX.getContentId().equalsIgnoreCase(model.getCrmStore()) && model.getFdxMobileNumber()!=null && model.getFdxMobileNumber().getPhone()!=null)
			  {
				ps1 = conn.prepareStatement("UPDATE CUST.FDCUSTOMER_ESTORE SET  MOBILE_NUMBER=?, ORDER_NOTIFICATION=?, ORDEREXCEPTION_NOTIFICATION=?, SMS_OFFERS_ALERT=?, PARTNERMESSAGE_NOTIFICATION=?," +
						" SMS_OPTIN_DATE=?, DELIVERY_NOTIFICATION=?, OFFERS_NOTIFICATION=?, SMS_PREFERENCE_FLAG=? WHERE FDCUSTOMER_ID=? AND E_STORE=?");
					ps1.setString(1, model.getFdxMobileNumber().getPhone()); 
					ps1.setString(2, model.getFdxOrderNotices());
					ps1.setString(3, model.getFdxOrderExceptions());
					ps1.setString(4, model.getFdxOffers());
					ps1.setString(5, model.getFdxPartnerMessages());
					ps1.setTimestamp(6, new java.sql.Timestamp(optinDate.getTime()));
					if(model.getDeliveryNotification()!=null)
						ps1.setString(7,  model.getDeliveryNotification()?"Y":"N");
					else
						ps1.setString(7,  "N");
					if(model.getOffersNotification()!=null)
						ps1.setString(8,  model.getOffersNotification()?"Y":"N");
					else
						ps1.setString(8,  "N");
					ps1.setString(9, model.getFdxSmsPreferenceflag());
					ps1.setString(10, this.getParentPK().getId());
					ps1.setString(11, EnumEStoreId.FDX.getContentId());
				
					if(ps1.executeUpdate() < 1){
						create(conn);
					}
					ps1.close();
		}
			  
		this.setModified();
	}

	@Override
	public void remove(Connection conn){
	}

	@Override
	public void setFromModel(ModelI model) {
		this.model =(FDCustomerEStoreModel)model;
		
	}

		
}

