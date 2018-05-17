package com.freshdirect.fdstore.oas;


import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class AdServerSweeperCronRunner {

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.println("Start AdServerSweeperCronRunner..");		
		Connection conn = null;
		try {
			try {
				conn = getConnection(args);
				conn.setAutoCommit(false);
				if(null != args && args.length>5){
					updateOASdatabase(conn, args[5]);
				}else{
					updateOASdatabase(conn, null);
				}
				conn.commit();
				conn.setAutoCommit(true);
				System.out.println("Stop AdServerSweeperCronRunner..");
			}catch (SQLException e) {
				if(null != conn)
					conn.rollback();
				e.printStackTrace();
				System.out.println("Error in AdServerSweeperCronRunner.."+e.getMessage());
			} catch (NamingException e) {
				e.printStackTrace();
				System.out.println("Error in AdServerSweeperCronRunner.."+e.getMessage());
			} finally {
				if(null !=conn && !conn.isClosed()){
						conn.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in AdServerSweeperCronRunner.."+e.getMessage());
		}
		
	} 

	
	private static void updateOASdatabase(Connection conn, String campaigns) throws SQLException{
		
		System.out.println("Inside updateOASdatabase() method.");	
		long time= System.currentTimeMillis();
		removeZoneTermFromSearchTerm(conn);
		System.out.println("Query Time for Removing:"+(System.currentTimeMillis()-time)/1000+ "secs");
		PreparedStatement  ps = null;
		time= System.currentTimeMillis();
		if(null != campaigns && campaigns.trim().length()>0){
			ps = conn.prepareStatement("select c.CampaignKey, c.SearchTerm from Campaign c where c.CampaignKey in("+campaigns+")");			
		}else{
			
			ps = conn.prepareStatement("select distinct c.CampaignKey, c.SearchTerm from Campaign c,Campaign_Creative cc where c.CampaignKey = cc.CampaignKey and cc.CreativeKey in(Select Distinct(CreativeKey) From Creative c1, CreativeUpdate_Zone cu where c1.DisplayFlag ='Yes' and instr(c1.extraHtml, Concat('productId=', cu.ProductId, '&')) and cu.ZONETYPE<>'M' and cu.ZONETYPE<>'')");
			
		}
		ResultSet rs = ps.executeQuery();
		System.out.println("Query Time:"+(System.currentTimeMillis()-time)/1000+ "secs");
		PreparedStatement  ucps = conn.prepareStatement("update Campaign c set c.SearchTerm = ?,c.SearchAnyAll='LOG' where c.CampaignKey=?");
		PreparedStatement iczps = conn.prepareStatement("insert into CampaignZoneTerm(CampaignKey,ZoneSearchTerm,TimeStamp) values(?,?,?)");
		Date date = new Date();		
		while(rs.next()){
			int campaignKey = rs.getInt(1);
			String oldsearchTerm = rs.getString(2);
			int count =getCreativesCountForCampaign(campaignKey,conn);
			if(count>1){
				updateCreativesStatusForCampaign(campaignKey,conn);
			}else{
				prepareZonePricingSearchTerm(conn, ucps, iczps, date, campaignKey,
					oldsearchTerm);
			}
			
		}
		rs.close();
		ps.close();
		int[] result = ucps.executeBatch();
		System.out.println("Updated "+result.length+" Campaign records.");
		ucps.close();
		
		result = iczps.executeBatch();
		System.out.println("Inserted "+result.length+" CampaignZoneTerm records.");
		iczps.close();
		System.out.println("Completed updateOASdatabase() method.");
	}


	private static void removeZoneTermFromSearchTerm(Connection conn) throws SQLException{
		long time= System.currentTimeMillis();
		//PreparedStatement ps = conn.prepareStatement("select c.CampaignKey,c.SearchTerm,czt.ZoneSearchTerm from Campaign c, CampaignZoneTerm czt where c.SearchTerm like '%zonelevel%' and c.CampaignKey=czt.CampaignKey and czt.TimeStamp =(select max(TimeStamp) from CampaignZoneTerm where CampaignKey=c.CampaignKey) order by c.CampaignKey");
		PreparedStatement ps = conn.prepareStatement("SELECT c.CampaignKey, c.SearchTerm, czt.ZoneSearchTerm FROM Campaign c, CampaignZoneTerm czt WHERE c.SearchTerm like '%zonelevel%' AND c.CampaignKey=czt.CampaignKey AND czt.TimeStamp =(SELECT TimeStamp FROM tmp_MaxTime t WHERE CampaignKey=c.CampaignKey) ORDER BY c.CampaignKey;");
		
		PreparedStatement  ucps = conn.prepareStatement("update Campaign c set c.SearchTerm = ? where c.CampaignKey=?");
		ResultSet rs =ps.executeQuery();
		System.out.println("Query Time in Removing:"+(System.currentTimeMillis()-time)/1000+ "secs");
		while(rs.next()){
			int campaignKey = rs.getInt(1);
			String searchTerm = rs.getString(2);
			String zoneSearchTerm = rs.getString(3);
			if(null!=searchTerm && !searchTerm.trim().equals("")){
				if(null!=zoneSearchTerm && !zoneSearchTerm.equals(""))
				searchTerm = searchTerm.replace(zoneSearchTerm, "");
			}else{
				searchTerm = "";
			}
			ucps.setString(1, searchTerm);
			ucps.setInt(2, campaignKey);
			ucps.addBatch();			
		}		
		int[] rowsUpdated = ucps.executeBatch();
		System.out.println("Updated "+rowsUpdated.length+" Campaign records with old searchTerms.");
		ucps.close();
		rs.close();
		ps.close();		
	}


	private static void updateCreativesStatusForCampaign(int campaignKey,Connection conn) throws SQLException{
		
		PreparedStatement  ps = conn.prepareStatement("update Creative c1 set c1.DisplayFlag='No' where c1.CreativeKey in(Select Distinct(cc.CreativeKey) From Campaign_Creative cc, CreativeUpdate_Zone cu  where instr(c1.extraHtml, Concat('productId=', cu.ProductId, '&')) and cu.ZONETYPE<>'M' and cu.ZONETYPE<>'' and c1.DisplayFlag='Yes' and cc.CampaignKey="+campaignKey+")");
		int count= ps.executeUpdate();
//		System.out.println(count+" creatives were updated to 'No' for the campaign:"+campaignKey);
		ps.close();
		
	}


	private static int getCreativesCountForCampaign(int campaignKey,Connection conn) throws SQLException{
		PreparedStatement  ps = null;
		int count = 0;
		ps = conn.prepareStatement("select count(cc.CreativeKey) from Campaign_Creative cc where cc.CampaignKey="+campaignKey);
		ResultSet rs = ps.executeQuery();
		if(rs.next()){
			count = rs.getInt(1);
		}
		rs.close();
		ps.close();
		return count;
		
	}


	private static void prepareZonePricingSearchTerm(Connection conn,
			PreparedStatement ucps, PreparedStatement iczps, Date date,
			int campaignKey, String oldsearchTerm) throws SQLException {
		String searchTerm ="";
		/*if(null != oldsearchTerm && !oldsearchTerm.trim().equals("")){
			searchTerm = searchTerm +"AND((zonelevel!?)OR((zonelevel=true)";				
		}else{*/
			searchTerm = "((zonelevel!?)OR((zonelevel=true)";
//		}
//				searchTerm = searchTerm +"&& (zonelevel=null ||(zonelevel=true ";
		{
			searchTerm = searchTerm+"AND(";			
			
			String zidExclusionList="";

			zidExclusionList = getZidExclusionString(conn, campaignKey,
					zidExclusionList);
			if(!zidExclusionList.equalsIgnoreCase("")){
				searchTerm = searchTerm+"("+zidExclusionList+")";
				searchTerm=searchTerm+"AND";
			}
			String zidInclusionList ="";
			zidInclusionList = getZidInclusionString(conn, campaignKey,
					zidInclusionList);
			if(!zidInclusionList.equalsIgnoreCase("")){
				/*if(!zidExclusionList.equalsIgnoreCase("")){
					searchTerm=searchTerm+"AND";
				}*/
				searchTerm = searchTerm+"("+zidInclusionList;
				searchTerm=searchTerm+"OR";
			}		
			
			String szidExclusionList="";
			String szidInclusionList="";
			PreparedStatement ps3 = conn.prepareStatement("select count(cuz.zoneId) from Campaign_Creative CC, Creative cr, CreativeUpdate_Zone cuz where cr.CreativeKey in(Select Distinct(CreativeKey) From Creative c1 where instr(c1.ExtraHtml, Concat('productId=', cuz.ProductId, '&')) and cuz.price is not null and cuz.price<>'' and cuz.ZONETYPE='S')	and cr.CreativeKey = CC.CreativeKey and CC.CampaignKey =?;");
			ps3.setInt(1, campaignKey);
			ResultSet rs3 = ps3.executeQuery();			
			if(rs3.next() && rs3.getInt(1)>0){

				szidExclusionList = getSzidExclusionString(conn, campaignKey,
						szidExclusionList);

				if(!szidExclusionList.equalsIgnoreCase("")){					
					searchTerm = searchTerm+"("+szidExclusionList;
					searchTerm=searchTerm+"AND";
				}				
				szidInclusionList = getSzidInclusionString(conn, campaignKey,
						szidInclusionList);
				if(!szidInclusionList.equalsIgnoreCase("")){
					/*if(!szidExclusionList.equalsIgnoreCase("")){
						searchTerm=searchTerm+"AND";
					}*/
					searchTerm=searchTerm+"("+szidInclusionList;
				}				
			}
//			searchTerm = searchTerm +"OR((zid!?)AND(szid!?)AND(";
			String mzidList="";
			PreparedStatement ps5 = conn.prepareStatement("select distinct(cuz.zoneId) from Campaign_Creative CC, Creative cr, CreativeUpdate_Zone cuz where cuz.ZoneId<>'' and cr.CreativeKey in(Select Distinct(CreativeKey) From Creative c1 where instr(c1.extraHtml, Concat('productId=', cuz.ProductId, '&')) and cuz.price is not null and cuz.price<>'' and (c1.LinkText = cuz.Price or c1.LinkText='') and cuz.ZONETYPE='M')	and cr.CreativeKey = CC.creativeKey and CC.CampaignKey =?;");
			ps5.setInt(1, campaignKey);
			ResultSet rs5= ps5.executeQuery();			
			if(rs5.next()){				
				/*if(rs5.isFirst()){
					searchTerm = searchTerm +"OR((zid!?)AND(szid!?)AND(";
					isMasterZonePriceMatched = true;
				}
				if(!rs5.isFirst()){
					searchTerm = searchTerm +"OR";
				}*/
				mzidList = mzidList +"(mzid="+rs5.getString(1)+")";
				/*if(rs5.isLast())
					searchTerm = searchTerm+"))";*/
			}else{
				ps5 = conn.prepareStatement("select distinct(cuz.zoneId) from Campaign_Creative CC, Creative cr, CreativeUpdate_Zone cuz where cuz.ZoneId<>'' and cr.CreativeKey in(Select Distinct(CreativeKey) From Creative c1 where instr(c1.extraHtml, Concat('productId=', cuz.ProductId, '&')) and cuz.price is not null and cuz.price<>'' and c1.LinkText <> cuz.Price and cuz.ZONETYPE='M')	and cr.CreativeKey = CC.creativeKey and CC.CampaignKey =?;");
				ps5.setInt(1, campaignKey);
				rs5= ps5.executeQuery();
				if(rs5.next()){
					mzidList = mzidList +"(mzid!="+rs5.getString(1)+")";
				}
			}
			if(!mzidList.equalsIgnoreCase("")){
				if(!szidInclusionList.equalsIgnoreCase("")){
					searchTerm=searchTerm+"OR";
				}
				searchTerm = searchTerm+"("+mzidList+")";
				
			}
//			searchTerm = searchTerm+"))";
			if(!szidInclusionList.equalsIgnoreCase("")){
				searchTerm = searchTerm +")";
			}
			if(!szidExclusionList.equalsIgnoreCase("")){
				searchTerm = searchTerm +")";
			}
			if(!zidInclusionList.equalsIgnoreCase("")){
				searchTerm = searchTerm +")";
			}
			/*if(!zidExclusionList.equalsIgnoreCase("")){
				searchTerm = searchTerm +")";
			}*/
			searchTerm = searchTerm +")";
		}
		searchTerm = searchTerm +"))";
		
		//update each campaign with the updated searchterm
		PreparedStatement sczps = conn.prepareStatement("select ZoneSearchTerm from CampaignZoneTerm where CampaignKey=? and TimeStamp = (select max(TimeStamp) from CampaignZoneTerm where CampaignKey=?)");
		sczps.setInt(1, campaignKey);
		sczps.setInt(2, campaignKey);
		ResultSet rscz = sczps.executeQuery();
		String zoneSearchTerm ="";
		if(rscz.next()){
			zoneSearchTerm = rscz.getString(1);
		}
//		if(null !=zoneSearchTerm && !zoneSearchTerm.equals(""))
//			oldsearchTerm = oldsearchTerm.replace(zoneSearchTerm, "");
		if(null != oldsearchTerm && !oldsearchTerm.trim().equals("")){
			searchTerm="AND"+searchTerm;
			if(!oldsearchTerm.trim().startsWith("(") || !oldsearchTerm.trim().endsWith(")")){
				oldsearchTerm="("+oldsearchTerm.trim()+")";
			}
		}else{
			oldsearchTerm = "";
		}
		
		ucps.setString(1, oldsearchTerm+searchTerm);
		ucps.setInt(2, campaignKey);
		ucps.addBatch();
		
		iczps.setInt(1, campaignKey);
		iczps.setString(2,searchTerm);
		iczps.setTimestamp(3, new java.sql.Timestamp(date.getTime()));
		iczps.addBatch();
	}


	private static String getSzidInclusionString(Connection conn,
			int campaignKey, String szidInclusionList) throws SQLException {
		PreparedStatement ps4 = conn.prepareStatement("select distinct(cuz.zoneId) from Campaign_Creative CC, Creative cr, CreativeUpdate_Zone cuz where cuz.ZoneId<>'' and cr.CreativeKey in(Select Distinct(CreativeKey) From Creative c1 where instr(c1.extraHtml, Concat('productId=', cuz.ProductId, '&')) and cuz.price is not null and cuz.price<>'' and (c1.LinkText = cuz.Price or c1.LinkText='') and cuz.ZONETYPE='S')	and cr.CreativeKey = CC.creativeKey and CC.CampaignKey =?;");
		ps4.setInt(1, campaignKey);
		ResultSet rs4= ps4.executeQuery();
		while(rs4.next()){
			if(rs4.isFirst())	
				szidInclusionList = szidInclusionList+"((szid!?)OR((szid?)AND(";
			if(!rs4.isFirst()){
				szidInclusionList = szidInclusionList +"OR";
			}
			szidInclusionList = szidInclusionList+"(szid="+rs4.getString(1)+")";
			if(rs4.isLast())
				szidInclusionList = szidInclusionList+")))";					
		}
		return szidInclusionList;
	}


	private static String getSzidExclusionString(Connection conn,
			int campaignKey, String szidExclusionList) throws SQLException {
		PreparedStatement ps3;
		ResultSet rs3;
		ps3 = conn.prepareStatement("select distinct(cuz.zoneId) from Campaign_Creative CC, Creative cr, CreativeUpdate_Zone cuz where cuz.ZoneId<>'' and cr.CreativeKey in(Select Distinct(CreativeKey) From Creative c1 where instr(c1.ExtraHtml, Concat('productId=', cuz.ProductId, '&')) and cuz.price is not null and cuz.price<>'' and c1.LinkText <>'' and c1.LinkText <> cuz.Price and cuz.ZONETYPE='S')	and cr.CreativeKey = CC.CreativeKey and CC.CampaignKey =?;");
		ps3.setInt(1, campaignKey);
		rs3 = ps3.executeQuery();
		while(rs3.next()){
			if(rs3.isFirst())
				szidExclusionList = szidExclusionList +"((szid!?)OR((szid?)AND(";
			szidExclusionList = szidExclusionList + "(szid!="+rs3.getString(1)+")";
			if(!rs3.isLast())
				szidExclusionList = szidExclusionList+"AND";
			if(rs3.isLast())
				szidExclusionList = szidExclusionList +")))";
		}
		return szidExclusionList;
	}


	private static String getZidInclusionString(Connection conn,
			int campaignKey, String zidInclusionList) throws SQLException {
		PreparedStatement ps2 = conn.prepareStatement("select distinct(cuz.zoneId) from Campaign_Creative CC, Creative cr, CreativeUpdate_Zone cuz where cuz.ZoneId<>'' and cr.CreativeKey in(Select Distinct(CreativeKey) From Creative c1 where instr(c1.ExtraHtml, Concat('productId=', cuz.ProductId, '&')) and cuz.price is not null and cuz.price<>'' and (c1.LinkText = cuz.Price or c1.LinkText=''))	and cr.CreativeKey = CC.CreativeKey and CC.CampaignKey =?;");
		ps2.setInt(1, campaignKey);
		ResultSet rs2 = ps2.executeQuery();			
		while(rs2.next()){
			if(rs2.isFirst())
				zidInclusionList = zidInclusionList+"((zid!?)OR((zid?)AND(";
			zidInclusionList = zidInclusionList + "(zid="+rs2.getString(1)+")";
			if(!rs2.isLast())
				zidInclusionList = zidInclusionList+"OR";
			if(rs2.isLast())
				zidInclusionList = zidInclusionList +")))";
			
		}
		return zidInclusionList;
	}


	private static String getZidExclusionString(Connection conn,
			int campaignKey, String zidExclusionList) throws SQLException {
		PreparedStatement ps1 = conn.prepareStatement("select distinct(cuz.ZoneId) from Campaign_Creative CC, Creative cr, CreativeUpdate_Zone cuz where cr.CreativeKey in(Select Distinct(CreativeKey) From Creative c1 where instr(c1.ExtraHtml, Concat('productId=', cuz.ProductId, '&')) and cuz.ZONETYPE<>'M' and cuz.price is not null and cuz.price<>'' and c1.LinkText<>'' and c1.LinkText <> cuz.Price)and cr.CreativeKey = CC.creativeKey and CC.CampaignKey =?;");
		ps1.setInt(1, campaignKey);
		ResultSet rs1 = ps1.executeQuery();
		while(rs1.next()){
			if(rs1.isFirst())
				zidExclusionList = zidExclusionList+"((zid!?)OR((zid?)AND(";											
			zidExclusionList = zidExclusionList + "(zid!="+rs1.getString(1)+")";	
			if(!rs1.isLast())
				zidExclusionList = zidExclusionList+"AND";
			if(rs1.isLast())
				zidExclusionList = zidExclusionList+")))";
		}
		return zidExclusionList;
	}


	private static Connection getConnection(String[] args) throws SQLException, NamingException {

		/*Context ctx = new InitialContext();
		if (ctx == null)
			throw new NamingException("No Context found");

		DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/AdServer");
		Connection conn = null;
		if (ds != null) {
		conn = ds.getConnection();
	}*/

		Connection conn = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			if(null !=args && args.length>4){
				System.out.println("Arguments Size:"+args.length);
				conn = DriverManager.getConnection("jdbc:mysql://"+args[0]+":"+args[1]+"/"+args[2],args[3], args[4]);
			}else{
				System.out.println("Arguments Size:0");
				conn = DriverManager.getConnection("jdbc:mysql://nyc1stam01.nyc1.freshdirect.com:3306/OAS","fdadmin", "fd8848admin");
			}
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return conn;
	}
}
