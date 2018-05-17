package com.freshdirect.fdstore.content.meal.ejb;

/**
 * 
 * @author knadeem
 */
import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;

import com.freshdirect.framework.core.*;
import com.freshdirect.fdstore.content.meal.*;

public class MealPersistentBean extends DependentPersistentBeanSupport {
	
	private static final long	serialVersionUID	= 126776166681782318L;
	
	private MealModel meal;
    private String agent;
	
	/**
	 * Constructor for ErpReturnInvoicePersistentBean.
	 */
	public MealPersistentBean() {
		super();
        meal = null;
	}

	/**
	 * Constructor for ErpReturnInvoicePersistentBean.
	 * @param pk
	 * @param conn
	 * @throws SQLException
	 */
	public MealPersistentBean(PrimaryKey pk, Connection conn) throws SQLException {
		this();
		this.setPK(pk);
		load(conn);
	}
	

	/**
	 * Constructor for ErpReturnInvoicePersistentBean.
	 * @param model
	 */
	public MealPersistentBean(MealModel model) {
		this();
		this.setFromModel(model);
	}
	
	/**
	 * Copy into model.
	 * @return ErpAbstractOrderModel object.
	 */
	public ModelI getModel() {
		return meal;
	}
	
	/**
	 * set state of the bean from given model
	 * 
	 * @param ModelI model to copy properties from
	 */
	
	public void setFromModel(ModelI m) {
	    this.meal = (MealModel) m;
	    this.setPK(m.getPK());
	}
    
    public String getAgent() {
        return this.agent;
    }
    
    public void setAgent(String agent) {
        this.agent = agent;
    }
	
	/**
	 * Find ErpReturnInvoicePersistentBean objects for a given parent.
	 * @param conn the database connection to operate on
	 * @param parentPK primary key of parent
	 * @return a List of ErpCreateOrderPersistentBean objects (empty if found none).
	 * @throws SQLException if any problems occur talking to the database
	 */
	public static List findByParent(Connection conn, PrimaryKey parentPK) throws SQLException {
		java.util.List lst = new java.util.LinkedList();
		PreparedStatement ps = conn.prepareStatement("SELECT ID FROM CUST.HOLIDAYMEAL WHERE CUSTOMER_ID=?");
		ps.setString(1, parentPK.getId());
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			MealPersistentBean bean = new MealPersistentBean(new PrimaryKey(rs.getString(1)), conn);
			bean.setParentPK(parentPK);
			lst.add(bean);
		}
		rs.close();
		rs = null;
		ps.close();
		ps = null;
		return lst;
	}
	
	public PrimaryKey create(Connection conn) throws SQLException {
		String id = this.getNextId(conn, "CUST");
		PreparedStatement ps = null;
		
		ps = conn.prepareStatement("INSERT INTO CUST.HOLIDAYMEAL (ID, CUSTOMER_ID, NAME, DELIVERY, DATE_CREATED, AGENT, DATE_LASTMODIFIED, STATUS, PRICE) values (?,?,?,?,SYSDATE,?,SYSDATE,?,?)");
		ps.setString(1, id);
		ps.setString(2, this.getParentPK().getId());
		ps.setString(3, this.meal.getName());
        ps.setTimestamp(4, new java.sql.Timestamp(this.meal.getDelivery().getTime()));
        ps.setString(5, this.getAgent());
        ps.setString(6, this.meal.getStatus().getTypeName());
        //ps.setDouble(7, this.meal.getPrice());
        ps.setBigDecimal(7, new java.math.BigDecimal(this.meal.getPrice()));
		
		if (ps.executeUpdate() != 1) {
            throw new SQLException("Row not created");
		}
		this.setPK(new PrimaryKey(id));
        ps.close();

		// create children
        ps = conn.prepareStatement("INSERT INTO CUST.HOLIDAYMEAL_ITEMS (HMEAL_ID, TYPE, NAME, QUANTITY) values (?,?,?,?)");
		for (Iterator iter = meal.getItems().iterator(); iter.hasNext(); ) {
            MealItemModel item = (MealItemModel) iter.next();
            ps.clearParameters();
            ps.setString(1, id);
            ps.setString(2, item.getType().getTypeName());
            ps.setString(3, item.getName());
            ps.setInt(4, item.getQuantity());
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Row not created");
            }
        }
        ps.close();
		
		this.unsetModified();
		return this.getPK();
	}
	
	public void load(Connection conn) throws SQLException {
		
		PreparedStatement ps = conn.prepareStatement("SELECT NAME, DELIVERY, DATE_CREATED, AGENT, DATE_LASTMODIFIED, STATUS, PRICE FROM CUST.HOLIDAYMEAL WHERE ID = ?");
		ps.setString(1, this.getPK().getId());
		ResultSet rs = ps.executeQuery();
		if (!rs.next()) {
            rs.close();
            ps.close();
            throw new SQLException("No such Meal PK: " + this.getPK());
            
        }
        this.meal = new MealModel();
        super.decorateModel(this.meal);
		this.meal.setDelivery(new java.util.Date(rs.getTimestamp("DELIVERY").getTime()));
        this.meal.setName(rs.getString("NAME"));
        this.setAgent(rs.getString("AGENT"));
        this.meal.setStatus(EnumMealStatus.getType(rs.getString("STATUS")));
        this.meal.setPrice(rs.getDouble("PRICE"));
        rs.close();
        ps.close();
		
        ps = conn.prepareStatement("SELECT TYPE, NAME, QUANTITY FROM CUST.HOLIDAYMEAL_ITEMS WHERE HMEAL_ID=?");
        ps.setString(1, this.getPK().getId());
        rs = ps.executeQuery();
        while (rs.next()) {
            MealItemModel item = new MealItemModel();
            item.setType(EnumMealItemType.getType(rs.getString("TYPE")));
            item.setName(rs.getString("NAME"));
            item.setQuantity(rs.getInt("QUANTITY"));
            meal.addItem(item);
        }
		rs.close();
		ps.close();
        
        this.unsetModified();
		
    }
    
    public void store(Connection conn) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("UPDATE CUST.HOLIDAYMEAL SET NAME=?, DELIVERY=?, DATE_LASTMODIFIED=SYSDATE, STATUS=?, PRICE=? WHERE ID=?");
		
		ps.setString(1, this.meal.getName());
		ps.setTimestamp(2, new java.sql.Timestamp(this.meal.getDelivery().getTime()));
        ps.setString(3, this.meal.getStatus().getTypeName());
        //ps.setDouble(4, this.meal.getPrice());
        ps.setBigDecimal(4, new java.math.BigDecimal(this.meal.getPrice()));
        ps.setString(5, this.getPK().getId());
        
		if (ps.executeUpdate() != 1) {
			throw new SQLException("Row not updated");
		}
		ps.close();
        
        ps = conn.prepareStatement("DELETE FROM CUST.HOLIDAYMEAL_ITEMS WHERE HMEAL_ID=?");
        ps.setString(1, this.getPK().getId());
        ps.executeUpdate();
        ps.close();
        
        ps = conn.prepareStatement("INSERT INTO CUST.HOLIDAYMEAL_ITEMS (HMEAL_ID, TYPE, NAME, QUANTITY) values (?,?,?,?)");
		for (Iterator iter = meal.getItems().iterator(); iter.hasNext(); ) {
            MealItemModel item = (MealItemModel) iter.next();
            ps.clearParameters();
            ps.setString(1, this.getPK().getId());
            ps.setString(2, item.getType().getTypeName());
            ps.setString(3, item.getName());
            ps.setInt(4, item.getQuantity());
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Row not updated");
            }
        }
        ps.close();
        
		this.unsetModified();
	}

	public void remove(Connection conn) throws SQLException {
		// remove self
		PreparedStatement ps = conn.prepareStatement("DELETE FROM CUST.HOLIDAYMEAL WHERE ID = ?");
		ps.setString(1, this.getPK().getId());
		if (ps.executeUpdate() != 1) {
			throw new SQLException("Row not deleted");
		}
		ps.close();
        
        ps = conn.prepareStatement("DELETE FROM CUST.HOLIDAYMEAL_ITEMS WHERE HMEAL_ID=?");
        ps.setString(1, this.getPK().getId());
        ps.executeUpdate();
        ps.close();

		this.setPK(null); // make it anonymous
	}
    

}
