package com.freshdirect.fdstore.request;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FDProductRequestDAO {

	public static void storeRequest(Connection conn, List<FDProductRequest> request) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("INSERT INTO CUST.PRODUCT_REQ(ID,CUSTOMER_ID,DEPT,CATEGORY,SUB_CATEGORY,PRODUCT_NAME,STATUS,CREATE_DATE) VALUES(?, ?, ?, ?, ?, ?, 'NEW', SYSDATE)");
		
		FDProductRequest prodReq=null;
		for(int i=0;i<request.size();i++) {
			prodReq=request.get(i);
			ps.setString(1, prodReq.getId());
			ps.setString(2, prodReq.getCustomerId());
			setField(ps,3,prodReq.getDept());
			setField(ps,4,prodReq.getCategory());
			setField(ps,5,prodReq.getSubCategory());
			setField(ps,6,prodReq.getProductName());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
	}
	
	private static void setField(PreparedStatement ps, int index, String value) throws SQLException {
		
		if(value==null) {
			ps.setNull(index, Types.VARCHAR);
		} else {
			ps.setString(index, value);
		}
	}

	public static List<HashMap<String, String>> fetchAllMappings(Connection conn) throws SQLException {

		List<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();

		PreparedStatement ps = conn.prepareStatement(
			"select DEPTID, CATID, OBSOLETE from CUST.PRODUCT_REQ_MAP"
		);

		ResultSet rs = ps.executeQuery();
		
		while (rs.next()) {
			HashMap<String, String> curMap = new HashMap<String, String>();
			curMap.put("DEPTID",(String)rs.getString("DEPTID"));
			curMap.put("CATID",(String)rs.getString("CATID"));
			curMap.put("OBSOLETE",(String)rs.getString("OBSOLETE"));
			mapList.add(curMap);
		}
		
		ps.close();

		return mapList;
	}

	public static List<HashMap<String, String>> fetchAllCats(Connection conn) throws SQLException {

		List<HashMap<String, String>> catList = new ArrayList<HashMap<String, String>>();

		PreparedStatement ps = conn.prepareStatement(
			"select ID, CATID, NAME, CATIDNAME, OBSOLETE from CUST.PRODUCT_REQ_CAT"
		);

		ResultSet rs = ps.executeQuery();
		
		while (rs.next()) {
			HashMap<String, String> curCat = new HashMap<String, String>();
			curCat.put("ID",(String)rs.getString("ID"));
			curCat.put("CATID",(String)rs.getString("CATID"));
			curCat.put("NAME",(String)rs.getString("NAME"));
			curCat.put("CATIDNAME",(String)rs.getString("CATIDNAME"));
			curCat.put("OBSOLETE",(String)rs.getString("OBSOLETE"));
			catList.add(curCat);
		}
		
		ps.close();
		
		return catList;
	}

	public static List<HashMap<String, String>> fetchAllDepts(Connection conn) throws SQLException {

		List<HashMap<String, String>> deptList = new ArrayList<HashMap<String, String>>();

		PreparedStatement ps = conn.prepareStatement(
			"select ID, DEPTID, NAME, DEPTIDNAME, OBSOLETE from CUST.PRODUCT_REQ_DEPT"
		);

		ResultSet rs = ps.executeQuery();
		
		while (rs.next()) {
			HashMap<String, String> curDept = new HashMap<String, String>();
			curDept.put("ID",(String)rs.getString("ID"));
			curDept.put("DEPTID",(String)rs.getString("DEPTID"));
			curDept.put("NAME",(String)rs.getString("NAME"));
			curDept.put("DEPTIDNAME",(String)rs.getString("DEPTIDNAME"));
			curDept.put("OBSOLETE",(String)rs.getString("OBSOLETE"));
			deptList.add(curDept);
		}
		
		ps.close();
		
		return deptList;
	}

}
