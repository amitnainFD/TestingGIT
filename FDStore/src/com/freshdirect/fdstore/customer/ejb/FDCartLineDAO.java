package com.freshdirect.fdstore.customer.ejb;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.freshdirect.affiliate.ExternalAgency;
import com.freshdirect.common.context.StoreContext;
import com.freshdirect.customer.EnumATCContext;
import com.freshdirect.customer.ErpClientCode;
import com.freshdirect.customer.ErpOrderLineModel;
import com.freshdirect.fdstore.EnumEStoreId;
import com.freshdirect.fdstore.FDConfiguration;
import com.freshdirect.fdstore.FDSku;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.event.EnumEventSource;
import com.freshdirect.framework.util.NVL;
import com.freshdirect.framework.util.StringUtil;

public class FDCartLineDAO {

	private FDCartLineDAO() {
	}

	private final static String QUERY_CARTLINES =
		"SELECT ID, SKU_CODE, VERSION, QUANTITY, SALES_UNIT, CONFIGURATION, RECIPE_SOURCE_ID, REQUEST_NOTIFICATION, VARIANT_ID, ADDED_FROM_SEARCH, DISCOUNT_APPLIED, SAVINGS_ID, CM_PAGE_ID, CM_PAGE_CONTENT_HIERARCHY, ADDED_FROM, CM_VIRTUAL_CATEGORY, EXTERNAL_AGENCY, EXTERNAL_SOURCE, EXTERNAL_GROUP, E_STORE, SOURCE"
			+ " FROM CUST.FDCARTLINE WHERE FDUSER_ID = ? AND NVL(E_STORE,'FreshDirect')=?";
	
	private final static String QUERY_CARTLINE_CLIENTCODES =
		"SELECT CLIENT_CODE, QUANTITY, CARTLINE_ID FROM CUST.FDCARTLINE_CLIENTCODE WHERE FDUSER_ID = ? ORDER BY CARTLINE_ID, ORDINAL";

	public static List<ErpOrderLineModel> loadCartLines(Connection conn, PrimaryKey fdUserPk, EnumEStoreId eStoreId) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(QUERY_CARTLINE_CLIENTCODES);
		ps.setString(1, fdUserPk.getId());
		ResultSet rs = ps.executeQuery();
		Map<String,List<ErpClientCode>> clientCodes = new HashMap<String, List<ErpClientCode>>();
		while (rs.next()) {
			ErpClientCode cc = new ErpClientCode();
			cc.setClientCode(rs.getString("CLIENT_CODE"));
			cc.setQuantity(rs.getInt("QUANTITY"));
			String cartLine = rs.getString("CARTLINE_ID");
			if (!clientCodes.containsKey(cartLine))
				clientCodes.put(cartLine, new ArrayList<ErpClientCode>());
			clientCodes.get(cartLine).add(cc);
		}
		rs.close();
		ps.close();

		List<ErpOrderLineModel> lst = new LinkedList<ErpOrderLineModel>();
		ps = conn.prepareStatement(QUERY_CARTLINES);
		ps.setString(1, fdUserPk.getId());
		ps.setString(2, eStoreId.getContentId());
		rs = ps.executeQuery();
		while (rs.next()) {

			ErpOrderLineModel line = new ErpOrderLineModel();
			FDSku sku = new FDSku(rs.getString("SKU_CODE"), rs.getInt("VERSION"));
			line.setSku(sku);
			FDConfiguration config =
				new FDConfiguration(
					rs.getDouble("QUANTITY"),
					rs.getString("SALES_UNIT"),
					convertStringToHashMap(NVL.apply(rs.getString("CONFIGURATION"), "")));
			line.setConfiguration(config);
			line.setCartlineId(rs.getString("ID"));
			line.setRecipeSourceId(rs.getString("RECIPE_SOURCE_ID"));
			line.setRequestNotification(NVL.apply(rs.getString("REQUEST_NOTIFICATION"), "").equals("X"));
			line.setVariantId(rs.getString("VARIANT_ID"));
			line.setAddedFromSearch("X".equals(rs.getString("ADDED_FROM_SEARCH")));
			if(rs.getString("DISCOUNT_APPLIED")!=null && rs.getString("DISCOUNT_APPLIED").equalsIgnoreCase("X")){
				line.setDiscountFlag(true);
				line.setSavingsId(rs.getString("SAVINGS_ID"));
			}
			line.setCoremetricsPageId(rs.getString("CM_PAGE_ID"));
			line.setCoremetricsPageContentHierarchy(rs.getString("CM_PAGE_CONTENT_HIERARCHY"));
			line.setAddedFrom(EnumATCContext.getEnum(rs.getString("ADDED_FROM")));
			line.setCoremetricsVirtualCategory(rs.getString("CM_VIRTUAL_CATEGORY"));
			line.setExternalAgency(ExternalAgency.safeValueOf(rs.getString("EXTERNAL_AGENCY")));
			line.setExternalSource(rs.getString("EXTERNAL_SOURCE"));
			line.setExternalGroup(rs.getString("EXTERNAL_GROUP"));
			line.setEStoreId(EnumEStoreId.valueOfContentId(rs.getString("E_STORE")));
			String source = rs.getString("SOURCE");
			line.setSource((null!=source && !"".equals(source))?EnumEventSource.valueOf(source):null);
			lst.add(line);
		}
		rs.close();
		ps.close();
		
		for (ErpOrderLineModel item : lst)
			if (clientCodes.containsKey(item.getCartlineId()))
				item.getClientCodes().addAll(clientCodes.get(item.getCartlineId()));
		return lst;

	}

	public static void storeCartLines(Connection conn, PrimaryKey fdUserPk, List<ErpOrderLineModel> erpOrderlines,StoreContext storeContext) throws SQLException {
		Map<String,List<ErpClientCode>> clientCodes = new HashMap<String, List<ErpClientCode>>();
		for (ErpOrderLineModel item : erpOrderlines) {
			// basic error resolution
			if (item.getCartlineId() == null)
				continue;
			
			ArrayList<ErpClientCode> ccList = new ArrayList<ErpClientCode>();
			ccList.addAll(item.getClientCodes());

			// we'll overwrite possible duplicates
			clientCodes.put(item.getCartlineId(), ccList);
		}
		
		PreparedStatement ps = conn.prepareStatement("DELETE FROM CUST.FDCARTLINE_CLIENTCODE WHERE FDUSER_ID = ?");
		ps.setString(1, fdUserPk.getId());
		ps.executeUpdate();
		ps.close();

		ps = conn.prepareStatement("DELETE FROM CUST.FDCARTLINE WHERE FDUSER_ID = ? AND NVL(E_STORE,'FreshDirect')=?");
		ps.setString(1, fdUserPk.getId());
		ps.setString(2, storeContext.getEStoreId().getContentId());
		int c=ps.executeUpdate();
		ps.close();

		if(erpOrderlines.size()==0)
			return;
		ps =
			conn.prepareStatement(
				"INSERT INTO CUST.FDCARTLINE (ID, FDUSER_ID, SKU_CODE, VERSION, QUANTITY, SALES_UNIT, CONFIGURATION, RECIPE_SOURCE_ID, REQUEST_NOTIFICATION, VARIANT_ID, DISCOUNT_APPLIED, SAVINGS_ID, ADDED_FROM_SEARCH, CM_PAGE_ID, CM_PAGE_CONTENT_HIERARCHY, ADDED_FROM, CM_VIRTUAL_CATEGORY, EXTERNAL_AGENCY, EXTERNAL_SOURCE, EXTERNAL_GROUP, E_STORE, SOURCE) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		for ( ErpOrderLineModel line : erpOrderlines ) {
			ps.setString(1, line.getCartlineId());
			ps.setString(2, fdUserPk.getId());
			ps.setString(3, line.getSku().getSkuCode());
			ps.setInt(4, line.getSku().getVersion());
			ps.setBigDecimal(5, new BigDecimal(String.valueOf(line.getQuantity())));
			ps.setString(6, line.getSalesUnit());
			ps.setString(7, convertHashMapToString(line.getOptions()));
			ps.setString(8, line.getRecipeSourceId());
			ps.setString(9, line.isRequestNotification() ? "X" : "");
			ps.setString(10, line.getVariantId());
			ps.setString(11, line.isDiscountFlag()? "X" : "");			
			ps.setString(12, line.getSavingsId());
			ps.setString(13, line.isAddedFromSearch()? "X" : "");
			ps.setString(14, line.getCoremetricsPageId());
			ps.setString(15, line.getCoremetricsPageContentHierarchy());
			ps.setString(16, null !=line.getAddedFrom()?line.getAddedFrom().getName():null);
			ps.setString(17, line.getCoremetricsVirtualCategory());
			ps.setString(18, StringUtil.crop(line.getExternalAgency(), 30));
			ps.setString(19, StringUtil.crop(line.getExternalSource(), 30));
			ps.setString(20, StringUtil.crop(line.getExternalGroup(), 256));
			ps.setString(21, null !=line.getEStoreId()? line.getEStoreId().getContentId():
				(null!=storeContext && null !=storeContext.getEStoreId() ? storeContext.getEStoreId().getContentId():EnumEStoreId.FD.getContentId()));
			ps.setString(22, (line.getSource()!=null? line.getSource().toString():null));
			
			ps.addBatch();
		}

		ps.executeBatch();
		ps.close();

		ps =
			conn.prepareStatement(
				"INSERT INTO CUST.FDCARTLINE_CLIENTCODE (CLIENT_CODE, QUANTITY, ORDINAL, FDUSER_ID, CARTLINE_ID) values (?, ?, ?, ?, ?)");

		for (Map.Entry<String, List<ErpClientCode>> cartItem : clientCodes.entrySet()) {
			for (int i = 0; i < cartItem.getValue().size(); i++) {
				ErpClientCode ccItem = cartItem.getValue().get(i);
				ps.setString(1, ccItem.getClientCode());
				ps.setInt(2, ccItem.getQuantity());
				ps.setInt(3, i);
				ps.setString(4, fdUserPk.getId());
				ps.setString(5, cartItem.getKey());
				ps.addBatch();
			}
		}

		ps.executeBatch();
		ps.close();
	}

	private static HashMap<String,String> convertStringToHashMap(String configuration) {
		StringTokenizer st = new StringTokenizer(configuration, ",");
		HashMap<String,String> ret = new HashMap<String,String>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			int idx = token.indexOf("=");
			String key = token.substring(0, idx++);
			String value = token.substring(idx, token.length());
			ret.put(key, value);
		}

		return ret;

	}

	private static String convertHashMapToString(Map<String,String> map) {
		StringBuffer ret = new StringBuffer();
		for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			ret.append(key);
			ret.append("=");
			ret.append(map.get(key));
			if (i.hasNext()) {
				ret.append(",");
			}
		}
		return ret.toString();
	}
}
