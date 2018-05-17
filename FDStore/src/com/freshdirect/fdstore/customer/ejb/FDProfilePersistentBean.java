package com.freshdirect.fdstore.customer.ejb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.freshdirect.fdstore.customer.ProfileModel;
import com.freshdirect.framework.core.DependentPersistentBeanSupport;
import com.freshdirect.framework.core.ModelI;
import com.freshdirect.framework.core.PrimaryKey;

public class FDProfilePersistentBean extends DependentPersistentBeanSupport {
	
	private Map attributes = new HashMap();
	
	public FDProfilePersistentBean() {
	}
	
	public void setFromModel(ModelI model) {
		ProfileModel m = (ProfileModel)model;
		this.attributes = new HashMap( m.getAttributes() );
		this.setModified();
	}
	
	public ModelI getModel(){
		ProfileModel model = new ProfileModel();
		model.setAttributes(this.attributes);
		super.decorateModel(model);
		return model;
	}	
	
	public PrimaryKey create(Connection conn) throws SQLException {
		this.insertAttributes(conn);

		this.setPK(this.getParentPK());
		
		this.unsetModified();
		return this.getPK();
	}

	public void load(Connection conn) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT PROFILE_NAME, PROFILE_VALUE FROM CUST.PROFILE WHERE CUSTOMER_ID=?");
		ps.setString(1, this.getPK().getId());
		ResultSet rs = ps.executeQuery();
		
		while (rs.next()) {
			String name = rs.getString("PROFILE_NAME");
			String value = rs.getString("PROFILE_VALUE");
			this.attributes.put(name, value); 
		}

		rs.close();
		ps.close();
	}
	
	public void remove(Connection conn) throws SQLException {
		// remove self
		this.deleteAttributes(conn, true);
		this.setPK(null); // make it anonymous
	}

	private void insertAttributes(Connection conn) throws SQLException {
		if (this.attributes.isEmpty()) {
			return;
		}
		
		PreparedStatement ps = conn.prepareStatement("INSERT INTO CUST.PROFILE (CUSTOMER_ID, PROFILE_TYPE, PROFILE_NAME, PROFILE_VALUE, PRIORITY) VALUES(?,'S',?,?,-1)");
		for (Iterator i = this.attributes.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry e = (Map.Entry)i.next();
			
			ps.setString(1, this.getParentPK().getId());
			ps.setString(2, (String)e.getKey());
			ps.setString(3, (String)e.getValue());

			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
	}
	
	private void deleteAttributes(Connection conn, boolean doCheck) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("DELETE FROM CUST.PROFILE WHERE CUSTOMER_ID=?");
		ps.setString(1, this.getPK().getId());
		int rowCount = ps.executeUpdate();
		if (doCheck && rowCount<1) {
			throw new SQLException("Row not deleted");
		}
		ps.close();
	}

	public void store(Connection conn) throws SQLException {
		this.deleteAttributes(conn, false);
		this.insertAttributes(conn);
		this.unsetModified();
	}
	
	public PrimaryKey getPK(){
		return this.getParentPK();
	}

	public void setAttribute(String name, String value) {
		this.attributes.put(name, value);
		this.setModified();
	}
		
	public void removeAttribute(String name) {
		this.attributes.remove(name);
		this.setModified();
	}
}