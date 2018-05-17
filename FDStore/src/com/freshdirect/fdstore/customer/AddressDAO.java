package com.freshdirect.fdstore.customer;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Category;

import com.freshdirect.common.address.PhoneNumber;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.core.SequenceGenerator;
import com.freshdirect.framework.util.NVL;
import com.freshdirect.framework.util.log.LoggerFactory;

public class AddressDAO {
	
	
	private static Category LOGGER = LoggerFactory.getInstance(AddressDAO.class);

	
    /** gets the next unique Id to use when writing a persistent object
     * to a persistent store for the first time
     * @param conn a SQLConnection to use to find an Id
     * @throws SQLException any problems while getting a unique id
     * @return an id that uniquely identifies a persistent object
     */    
    protected String getNextId(Connection conn, String schema) throws SQLException {
		return SequenceGenerator.getNextId(conn, schema);
    }
    
    
    
	private final static String STORE_ADDRESS_QUERY =
			"INSERT INTO CUST.ADDRESS (" +
			   "ID ,CUSTOMER_ID, FIRST_NAME, LAST_NAME, ADDRESS1, ADDRESS2, APARTMENT, CITY, STATE, ZIP," +
			   "COUNTRY, PHONE, PHONE_EXT, PHONE_TYPE, DELIVERY_INSTRUCTIONS, SCRUBBED_ADDRESS, ALT_DEST, ALT_FIRST_NAME," +
			   "ALT_LAST_NAME, ALT_APARTMENT, ALT_PHONE, ALT_PHONE_EXT, LONGITUDE, LATITUDE, GEOLOC, SERVICE_TYPE," +
			   "COMPANY_NAME, ALT_CONTACT_PHONE, ALT_CONTACT_EXT, ALT_CONTACT_TYPE, UNATTENDED_FLAG, UNATTENDED_INSTR) " +
	        " values (?,?,?,?,?,?,REPLACE(REPLACE(UPPER(?),'-'),' '),?,?,?,?,replace(replace(replace(replace(replace(?,'('),')'),' '),'-'),'.'),?,?,?,?,?,?,?,?,replace(replace(replace(replace(replace(?,'('),')'),' '),'-'),'.'),?,?,?,MDSYS.SDO_GEOMETRY(2001, 8265, MDSYS.SDO_POINT_TYPE (?, ?,NULL),NULL,NULL),?,?,replace(replace(replace(replace(replace(?,'('),')'),' '),'-'),'.'),?,?,?,?)";

		public void create(Connection conn, ErpAddressModel addressModel, String customerId) throws SQLException {
			String id = this.getNextId(conn, "CUST");
			PreparedStatement ps = conn.prepareStatement(STORE_ADDRESS_QUERY);

			ps.setString(1, id);
			//ps.setString(2, this.getParentPK().getId());
			ps.setString(2, customerId);
			ps.setString(3, addressModel.getFirstName());
			ps.setString(4, addressModel.getLastName());
			ps.setString(5, addressModel.getAddress1());
			ps.setString(6, ("".equals(addressModel.getAddress2()) ? " " : addressModel.getAddress2()));
			ps.setString(7, ("".equals(addressModel.getApartment()) ? " " : addressModel.getApartment()));
			ps.setString(8, addressModel.getCity());
			ps.setString(9, addressModel.getState());
			ps.setString(10, addressModel.getZipCode());
			ps.setString(11, addressModel.getCountry());
			ps.setString(12, this.convertPhone(addressModel.getPhone()));
			ps.setString(13, this.convertExtension(addressModel.getPhone()));
			ps.setString(14, this.convertType(addressModel.getPhone()));
			ps.setString(15, addressModel.getInstructions());
			ps.setString(16, addressModel.getAddressInfo()!=null ? addressModel.getAddressInfo().getScrubbedStreet() : addressModel.getAddress1());
			ps.setString(17, addressModel.getAltDelivery()!=null ? addressModel.getAltDelivery().getDeliveryCode(): "");
			ps.setString(18, addressModel.getAltFirstName());
			ps.setString(19, addressModel.getAltLastName());				
			ps.setString(20, addressModel.getAltApartment());
			ps.setString(21, this.convertPhone(addressModel.getAltPhone()));
			ps.setString(22, this.convertExtension(addressModel.getAltPhone()));
			ps.setBigDecimal(23, addressModel.getAddressInfo()!=null ? new BigDecimal(String.valueOf(addressModel.getAddressInfo().getLongitude())) : null);
			ps.setBigDecimal(24, addressModel.getAddressInfo()!=null ? new BigDecimal(String.valueOf(addressModel.getAddressInfo().getLatitude())) : null);
			ps.setBigDecimal(25, addressModel.getAddressInfo()!=null ? new BigDecimal(String.valueOf(addressModel.getAddressInfo().getLongitude())) : null);
			ps.setBigDecimal(26, addressModel.getAddressInfo()!=null ? new BigDecimal(String.valueOf(addressModel.getAddressInfo().getLatitude())) : null);
			if(addressModel.getServiceType() == null){
				ps.setNull(27, Types.VARCHAR);
			} else {
				ps.setString(27, addressModel.getServiceType().getName());
			}
			ps.setString(28, addressModel.getCompanyName());
			ps.setString(29, this.convertPhone(addressModel.getAltContactPhone()));
			ps.setString(30, this.convertExtension(addressModel.getAltContactPhone()));
			ps.setString(31, this.convertType(addressModel.getAltContactPhone()));
			
			ps.setString(32, addressModel.getUnattendedDeliveryFlag() != null ? addressModel.getUnattendedDeliveryFlag().toSQLValue() : null);
			ps.setString(33, addressModel.getUnattendedDeliveryInstructions());
					
			try {
				if (ps.executeUpdate() != 1) {
					throw new SQLException("Row not created");
				}
				//this.setPK(new PrimaryKey(id));
			} catch (SQLException sqle) {
				throw sqle;
			} finally {	
				ps.close();
				ps = null;
			}

			// create children here

			//this.unsetModified();
			//return this.getPK();
			return;
		}    
    
		
		private final String convertPhone(PhoneNumber phoneNumber) {
			return phoneNumber==null ? null : phoneNumber.getPhone();
		}

		private final String convertExtension(PhoneNumber phoneNumber) {
			return phoneNumber==null ? null : phoneNumber.getExtension();
		}
		
		private final String convertType(PhoneNumber phoneNumber) {
			return phoneNumber==null ? null : phoneNumber.getType();
		}

		private final PhoneNumber convertPhoneNumber(String phone, String extension) {
			return convertPhoneNumber(phone, extension, "");
		}
		
		private final PhoneNumber convertPhoneNumber(String phone, String extension, String type) {
			return "() -".equals(phone) ? null : new PhoneNumber(phone, NVL.apply(extension, ""), NVL.apply(type, ""));
		}		
    
    

}
