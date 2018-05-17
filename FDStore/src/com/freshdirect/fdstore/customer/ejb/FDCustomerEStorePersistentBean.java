package com.freshdirect.fdstore.customer.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.framework.core.DependentPersistentBeanSupport;
import com.freshdirect.framework.core.ModelI;
import com.freshdirect.framework.core.PrimaryKey;

/**
 * 
 * @author ksriram
 *
 */
public class FDCustomerEStorePersistentBean extends DependentPersistentBeanSupport{

	private static final long serialVersionUID = 8004436858355527189L;
	
	private FDCustomerEStoreModel model;
	
	public FDCustomerEStorePersistentBean() {
		super();
		model = new FDCustomerEStoreModel();
	}

	public FDCustomerEStorePersistentBean(PrimaryKey pk) {
		super(pk);
		model = new FDCustomerEStoreModel();
	}

	/*public static List findByParent(Connection conn, PrimaryKey parentPK) throws SQLException {
		EnumEStoreId eStoreId =getCustomerEStoreId();
		java.util.List lst = new java.util.LinkedList();
		PreparedStatement ps = conn.prepareStatement("SELECT DEFAULT_SHIPTO, DEFAULT_PAYMENT, DEFAULT_DEPOT_LOCATION FROM CUST.FDCUSTOMER_ESTORE WHERE FDCUSTOMER_ID=? AND E_STORE=?");
		ps.setString(1, parentPK.getId());
		ps.setString(2, eStoreId.getContentId());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			FDCustomerEStorePersistentBean bean = new FDCustomerEStorePersistentBean( parentPK);
			FDCustomerEStoreModel model = new FDCustomerEStoreModel();
			model.setDefaultShipToAddressPK(rs.getString("DEFAULT_SHIPTO"));
			model.setDefaultPaymentMethodPK(rs.getString("DEFAULT_PAYMENT"));
			model.setDefaultDepotLocationPK(rs.getString("DEFAULT_DEPOT_LOCATION"));
			bean.setParentPK(parentPK);			
			bean.setFromModel(model);
			lst.add(bean);
		}
		rs.close();
		ps.close();
		return lst;
	}*/

	@Override
	public ModelI getModel() {
		if(this.model!=null)
			return this.model.deepCopy();
		return new FDCustomerEStoreModel().deepCopy();
	}

	@Override
	public PrimaryKey create(Connection conn) throws SQLException {
		this.setPK(this.getParentPK());
		PreparedStatement ps = conn.prepareStatement("INSERT INTO CUST.FDCUSTOMER_ESTORE (FDCUSTOMER_ID, E_STORE, DEFAULT_SHIPTO, DEFAULT_PAYMENT, DEFAULT_DEPOT_LOC,EMAIL_OPTIN,TC_AGREE_DATE,TC_AGREE) values (?,?,?,?,?,?,?,?)");
		ps.setString(1, this.getParentPK().getId());
		ps.setString(2, model.geteStoreId().getContentId());
		ps.setString(3, model.getDefaultShipToAddressPK());
		ps.setString(4, model.getDefaultPaymentMethodPK());
		ps.setString(5, model.getDefaultDepotLocationPK());
		ps.setString(6, model.getEmailOptIn()?"X":"");
		ps.setTimestamp(7, new Timestamp(new Date().getTime()));
		ps.setString(8, "X");

		try {
			if (ps.executeUpdate() != 1) {
				throw new SQLException("Row not created");
			}
		} catch (SQLException sqle) {
			this.setPK(null);
			throw sqle;
		} finally {
			ps.close();
		}
		return this.getParentPK();
	}

	@Override
	public void load(Connection conn) throws SQLException {
		EnumEStoreId eStoreId =getCustomerEStoreId();

		PreparedStatement ps = conn.prepareStatement("SELECT DEFAULT_SHIPTO, DEFAULT_PAYMENT, DEFAULT_DEPOT_LOC,TC_AGREE,EMAIL_OPTIN FROM CUST.FDCUSTOMER_ESTORE WHERE FDCUSTOMER_ID=? AND E_STORE=?");

		ps.setString(1, this.getParentPK().getId());
		ps.setString(2, eStoreId.getContentId());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			model.setPK(getPK());
			model.seteStoreId(eStoreId);
			model.setDefaultShipToAddressPK(rs.getString("DEFAULT_SHIPTO"));
			model.setDefaultPaymentMethodPK(rs.getString("DEFAULT_PAYMENT"));
			model.setDefaultDepotLocationPK(rs.getString("DEFAULT_DEPOT_LOC"));		
			model.setTcAcknowledge("X".equalsIgnoreCase(rs.getString("TC_AGREE")) ? true : false);
			model.setEmailOptIn("X".equalsIgnoreCase(rs.getString("EMAIL_OPTIN"))?true:false);
			if(EnumEStoreId.FDX.equals(eStoreId)){
				model.setFdxEmailOptIn("X".equalsIgnoreCase(rs.getString("EMAIL_OPTIN"))?true:false);
			}else{
				ps = conn.prepareStatement("SELECT EMAIL_OPTIN FROM CUST.FDCUSTOMER_ESTORE WHERE FDCUSTOMER_ID=? AND E_STORE=?");
				ps.setString(1, this.getParentPK().getId());
				ps.setString(2, EnumEStoreId.FDX.getContentId());
				rs = ps.executeQuery();
				if(rs.next()){
					model.setFdxEmailOptIn("X".equalsIgnoreCase(rs.getString("EMAIL_OPTIN"))?true:false);
				}
			}
		}
		rs.close();
		ps.close();
		
	}

	@Override
	public void store(Connection conn) throws SQLException {
		EnumEStoreId eStoreId = getCustomerEStoreId();
		PreparedStatement ps = conn.prepareStatement("UPDATE CUST.FDCUSTOMER_ESTORE SET DEFAULT_SHIPTO=?, DEFAULT_PAYMENT=?, DEFAULT_DEPOT_LOC=? WHERE FDCUSTOMER_ID=? AND E_STORE=?");
		ps.setString(1, model.getDefaultShipToAddressPK());
		ps.setString(2, model.getDefaultPaymentMethodPK());
		ps.setString(3, model.getDefaultDepotLocationPK());
		ps.setString(4, this.getParentPK().getId());
		ps.setString(5, eStoreId.getContentId());
		if(ps.executeUpdate() < 1){
			create(conn);
		}
		ps.close();
		this.unsetModified();
		
	}

	@Override
	public void remove(Connection conn) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("DELETE FROM CUST.FDCUSTOMER_ESTORE WHERE FDCUSTOMER_ID= ?");
		ps.setString(1, this.getPK().getId());
		if (ps.executeUpdate() != 1) {
			throw new SQLException("Row not deleted");
		}
		ps.close();		
	}

	@Override
	public void setFromModel(ModelI model) {
		this.model =(FDCustomerEStoreModel)model;
		
	}

	private static EnumEStoreId getCustomerEStoreId(){
		return EnumEStoreId.valueOfContentId((ContentFactory.getInstance().getStoreKey().getId()));
	}

}
